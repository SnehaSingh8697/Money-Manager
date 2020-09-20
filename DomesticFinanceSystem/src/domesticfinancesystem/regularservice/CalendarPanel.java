/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.regularservice;

import domesticfinancesystem.calendar.*;
import domesticfinancesystem.MainFrame;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;


/**
 *
 * @author sneha
 */
public class CalendarPanel extends javax.swing.JPanel {

    /**
     * Creates new form CalendarFrame
     */
    private Database dc;
    private Connection con;
    private CalCellRenderer[] rendMonthdates = new CalCellRenderer[42];
    private int selectedDay;
    private int selectedMonth;
    private int selectedYear;
    private MouseListener enter;
    private MouseListener exit;
    private MouseListener click;
    private Date selecteddate = null;
    private volatile boolean toShowNextHolidays;
    private Timer timer;
    private int curindex = 0;
    private boolean holidayFound = true;
    private boolean checkhol = false;
    private boolean tempholidayshown;
    private ArrayList<Transaction> arl = new ArrayList<>();
    private SpinnerNumberModel spnYearModel;
    private int servDefId;
    private Color color;
    private Date serviceStartDate;
    private Color headColor ;

    
    private void getServiceStartDate()
    {
        try {
            String sql = "Select StrtDate from ServDef where id = "+servDefId;
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            if(rst.next())
                serviceStartDate = rst.getDate(1);
            
            rst.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(CalendarPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
   
    public CalendarPanel(int servDefId,Color color) 
    {
        initComponents();
        
        
        dc = MainFrame.dc;
        con = dc.createConnection();
        
        headColor = new Color(255, 255, 153);
        
        this.servDefId = servDefId;
        this.color = color;
        
        if(servDefId>=0)
            getServiceStartDate();
        
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        
        spnYearModel = new SpinnerNumberModel(2019, 1970, 2100, 1);
        spnYears.setModel(spnYearModel);
        spnYears.setEditor(new JSpinner.NumberEditor(spnYears, "####"));
        
        enter = new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
//                
                  super.mouseEntered(e); //To change body of generated methods, choose Tools | Templates.
                  CalCellRenderer cp = (CalCellRenderer)e.getComponent();
                  cp.highlightLabel(Color.GREEN);
            }
            
        };
        exit = new MouseAdapter()
        {
            @Override
            public void mouseExited(MouseEvent e)
            {
                super.mouseExited(e); //To change body of generated methods, choose Tools | Templates.
                int currentday = new GregorianCalendar().get(Calendar.DAY_OF_MONTH);
                int currentmonth = new GregorianCalendar().get(Calendar.MONTH);
                int currentyear = new GregorianCalendar().get(Calendar.YEAR);
                CalCellRenderer cp = (CalCellRenderer)e.getComponent();
                int num = cp.getDaynumber();
                if(num == currentday && selectedMonth == currentmonth && selectedYear == currentyear)
                      cp.highlightLabel(Color.ORANGE);
                else if(cp.getSatSun() == 'T')
                    cp.highlightLabel(Color.BLUE);
                else if(cp.getSatSun() == 'N')
                     cp.highlightLabel(Color.RED);
                else
                     cp.highlightLabel(Color.BLACK);
            }
        };
        GregorianCalendar dt;
        
        dt = new GregorianCalendar() ;
//        dt.setTime(date);
        
        DateFormatSymbols dfs = new DateFormatSymbols();

        String months[] = dfs.getMonths();
        
        setYearMonthsDay();
       
        selectedYear = dt.get(Calendar.YEAR);

        selectedDay = dt.get(Calendar.DAY_OF_MONTH);
        
        spnYears.setValue(selectedYear);

        ActionListener lsn = cmbmonth.getActionListeners()[0] ;
        cmbmonth.removeActionListener(lsn);
        for (String month : months)
        {
           cmbmonth.addItem(month);
        }
        cmbmonth.addActionListener(lsn);
        
        selectedMonth = dt.get(Calendar.MONTH);

        
        cmbmonth.setSelectedIndex(selectedMonth);

        printCalendar();

        lblnext.setEnabled(true);
        lblprev.setEnabled(true);
        
        lblnext.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int index = cmbmonth.getSelectedIndex();
               
                if(index == 11)
                {
                    cmbmonth.setSelectedIndex(0);
                    selectedMonth = 0;
                }
                else
                {
                  index++;
                  cmbmonth.setSelectedIndex(index);
                  selectedMonth = index;
                }
            }
        });
        
        lblprev.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int index = cmbmonth.getSelectedIndex();
                
                if(index == 0)
                {
                    cmbmonth.setSelectedIndex(11);
                    selectedMonth = 11;
                }
                else
                {
                  index--;
                  cmbmonth.setSelectedIndex(index);
                  selectedMonth = index;
                }
            }
        });
        
    }
    public void setIdColor(int id,Color color)
    {
        servDefId = id;
        this.color = color;
        getServiceStartDate();
        printCalendar();
    }
    
    public void getTransactionFramDatabase()
    {
        // Get the index of the rendMonthDates[] where it stores "1"
//        arl.clear();
        int index = 0 ;
        for (index = 0 ; index < rendMonthdates.length; index++) 
            if(rendMonthdates[index].getDaynumber() == 1)
                break ;
        int noOfDaysBefore = index ;   // No of days of previous month that are being shown
        index -- ;
        int year = (int)spnYears.getValue();
        
        int month = cmbmonth.getSelectedIndex() + Calendar.JANUARY ;

        GregorianCalendar gcStartDt = new GregorianCalendar(year, month-Calendar.JANUARY, 1) ;
        GregorianCalendar gcEndDt = new GregorianCalendar(year, month-Calendar.JANUARY, 1) ;

        gcStartDt.add(Calendar.DATE, -1) ; // Last date ofr previous month
        
        int noOfDaysOfPrevMonth = gcStartDt.get(Calendar.DATE);
        
        gcStartDt.add(Calendar.DATE, -noOfDaysBefore+1) ;
                
        gcEndDt.add(Calendar.MONTH, 1) ; // 1st of next month
        gcEndDt.add(Calendar.DATE, -1) ; // last of current month
        int noOfDaysInCurrentMonth = gcEndDt.get(Calendar.DATE);
        gcEndDt.add(Calendar.DATE, rendMonthdates.length - noOfDaysBefore - gcEndDt.get(Calendar.DATE)) ;
        
//        String sql = "Select Income,Expense,dt from Summary";
//        sql+=" where EXTRACT(month FROM dt) = "+(month+1)+" and  EXTRACT(year FROM dt) = "+year+"order by 3"; // orderby date asscending

//        System.out.println("start date = "+gcStartDt.getTime());
//        System.out.println("end   date = "+gcEndDt.getTime());

        String sql = "Select DtTm from RegServ where DtTm between ? and ? and ServDefId = ? and DeletedYN = ? order by 1";

        try {
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setDate(1, new java.sql.Date(gcStartDt.getTime().getTime()));
            pstmt.setDate(2, new java.sql.Date(gcEndDt.getTime().getTime()));
            pstmt.setInt(3, servDefId);
            pstmt.setString(4, "N");
            
            ResultSet rst = pstmt.executeQuery();
            
            int j;
//            System.out.println("start of while ====================>");
            while(rst.next())
            {
                Date dt = rst.getDate("DtTm");
                
                int d = dt.getDate();
                int n = dt.getMonth();
               
                if(n == month)//current month
                {
                    j = index + d;
                }
                else if(n < month)//previous month
                {
                    j = index - (noOfDaysOfPrevMonth - d); 
                }                    
                else//next month
                {
                    j = index + (noOfDaysInCurrentMonth + d);
                    
                }
                
//                System.out.println("Date = "+dt + ", income = " + income + ", expense = " + expense + ", Day = " + dt.getDate()+ ", Month = " + dt.getMonth());
//                System.out.println("d = "+d + ", j = " + j );
                
                CalCellRenderer rend = rendMonthdates[j] ;
//                rend.setBackgColor(Color.cyan);
                if(rend.isIsDateValid() == false)
                    rend.resetBackgColor();
                else
                   rend.setBackgColor(color);
            }
        } catch (SQLException ex) {
            Logger.getLogger(CalendarPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    public void dispose()
    {
        timer.stop();
    }
    
    public void printCalendar()
    {
        for (CalCellRenderer rend : rendMonthdates)
        {
                rend.setEnabled(true);
                rend.highlightLabel(Color.black);

        }
        
        curindex = 0;
        holidayFound = true;     //setting for next month view
        
        selectedYear = (int)spnYears.getValue();
        selectedMonth = cmbmonth.getSelectedIndex();
        
        GregorianCalendar gc = new GregorianCalendar(selectedYear,selectedMonth,1);
        GregorianCalendar dt = new GregorianCalendar(selectedYear,selectedMonth,1);
        
        gc.add(Calendar.DATE,-1);
                                                            //To get the total number of days in the previous month
        int prevmonthdays = gc.get(Calendar.DATE);
        
        int labelcount = 0;

        gc.add(Calendar.DATE,+1);     //First day of the current month

        int d = gc.get(Calendar.DAY_OF_WEEK);
        
	gc.add(Calendar.MONTH,1);
	
        gc.add(Calendar.DATE,-1);               //To get the total number of days of the current month

	int m = gc.get(Calendar.DATE);
	
        int prevday = d-1;                             //Number of days before the current month starts
        
        int w = 0;
        
        int day =(prevmonthdays - prevday)+1;           //Dates to be printed from the previous month
        
        int prevmonth,year;
        if(selectedMonth == Calendar.JANUARY)
        {
            prevmonth = Calendar.DECEMBER;
            year = selectedYear - 1;
        }
        else
        {
            prevmonth = selectedMonth - 1;
            year = selectedYear;
        }
        GregorianCalendar prevMonthDt = new GregorianCalendar(year, prevmonth, day);
        for (int i = 0; i<prevday ; i++)
        {
            rendMonthdates[i].resetBackgColor();
            boolean isDateValid = false;
            rendMonthdates[i].setValue(day,prevMonthDt.getTime(),'O',servDefId,color,isDateValid);
            rendMonthdates[i].setLabelsDisabled();
            day++;
            labelcount++;
            w++;
           prevMonthDt.add(Calendar.DATE, 1);
        }
        int j=1;
        
        int limit = m + (d-1);
        
        String str;
        
        int i;
        
        GregorianCalendar greg = new GregorianCalendar();
        
        int currentday = greg.get(Calendar.DATE);
        int currentmonth = greg.get(Calendar.MONTH);
        int currentyear = greg.get(Calendar.YEAR);
     
        for ( i = prevday; i<limit; i++)
        {
            w++;
            rendMonthdates[i].setLabelsEnabled();
            rendMonthdates[i].resetBackgColor();

            char ch;
             if(w % 7 == 0)
             {
                rendMonthdates[i].highlightLabel(Color.blue);
                ch = 'T';
                if(j == currentday && currentmonth == selectedMonth && currentyear == selectedYear)
                {
                    rendMonthdates[i].highlightLabel(Color.ORANGE);
                }
                
             }
             else if(w % 7 == 1)
             {
                rendMonthdates[i].highlightLabel(Color.red); 
                ch = 'N';
                if(j == currentday && currentmonth == selectedMonth && currentyear == selectedYear)
                {
                    rendMonthdates[i].highlightLabel(Color.ORANGE);
                }
             }
             else
             {
                if(j == currentday && currentmonth == selectedMonth && currentyear == selectedYear)
                {
                    rendMonthdates[i].highlightLabel(Color.ORANGE);
                }
                 ch = 'O';
             }
            boolean isDateValid = false;
            if(servDefId!=-1)
            {
                if(dt.getTime().compareTo(serviceStartDate)<0)
                    isDateValid = false;
                else
                    isDateValid = true;
            }
            rendMonthdates[i].setValue(j,dt.getTime(),ch,servDefId,color,isDateValid);
            dt.add(Calendar.DATE, 1);
            labelcount++;
            j++;
        }
        int nextmonthdays = 1;
        int nextmonth ;
        if(selectedMonth == Calendar.DECEMBER)
        {
            nextmonth = Calendar.JANUARY;
            year = selectedYear + 1;
        }
        else
        {
            nextmonth = selectedMonth + 1;
            year = selectedYear;
        }
        GregorianCalendar nextMonthDt = new GregorianCalendar(year, nextmonth, nextmonthdays);
        
        
            while(true)
            {
                if(labelcount<42)
                {
                    
                    rendMonthdates[i].resetBackgColor();
                    
                    boolean isDateValid = false;
                    rendMonthdates[i].setValue(nextmonthdays,nextMonthDt.getTime(),'O',servDefId,color,isDateValid);

                    rendMonthdates[i].setLabelsDisabled();

                    i++;

                    nextMonthDt.add(Calendar.DATE, 1);

                    nextmonthdays++;

                    labelcount++;
                }
                else
                    break;
            }
    
            getTransactionFramDatabase();
    }

    public void setYearMonthsDay()
    {
        JLabel lbldays[] = new JLabel[7];
        DateFormatSymbols dfs = new DateFormatSymbols();
        String months[] = dfs.getMonths();
        
        String[] days = dfs.getShortWeekdays();
        Font font = new Font("Garamond", Font.PLAIN, 16);

       
        for (int i = 0; i < lbldays.length; i++)
        {
            lbldays[i] = new JLabel();
            lbldays[i].setOpaque(true);
            lbldays[i].setBackground(headColor);
            lbldays[i].setVisible(true);
            lbldays[i].setHorizontalAlignment(JLabel.CENTER);
            lbldays[i].setFont(font);
                      
            
            if(i == 0)
            {
                lbldays[i].setForeground(Color.red);

                lbldays[i].setText(days[i+1]);
            }
            else if(i == 6)
            {
              lbldays[i].setForeground(Color.blue);  
              lbldays[i].setText(days[i+1]);  
            }
            else
                lbldays[i].setText(days[i+1]);

            dayPanel.add(lbldays[i]);
            
        }
        
        datePanel.setLayout(new GridLayout(6,7));
        
        for (int i = 0; i < rendMonthdates.length; i++)
        {
            rendMonthdates[i] = new CalCellRenderer();
            datePanel.add(rendMonthdates[i]);
            rendMonthdates[i].setBorder(BorderFactory.createLineBorder(Color.GRAY));
            rendMonthdates[i].addMouseListener(enter);
            rendMonthdates[i].addMouseListener(exit);
            rendMonthdates[i].addMouseListener(click);
        }
    }

   
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        UpperPanel = new javax.swing.JPanel();
        lblprev = new javax.swing.JLabel();
        cmbmonth = new javax.swing.JComboBox<>();
        lblnext = new javax.swing.JLabel();
        spnYears = new javax.swing.JSpinner();
        dayPanel = new javax.swing.JPanel();
        datePanel = new javax.swing.JPanel();

        lblprev.setText("      <");

        cmbmonth.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        cmbmonth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbmonthActionPerformed(evt);
            }
        });

        lblnext.setText(">");

        spnYears.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        spnYears.setModel(new javax.swing.SpinnerDateModel());
        spnYears.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnYearsStateChanged(evt);
            }
        });

        javax.swing.GroupLayout UpperPanelLayout = new javax.swing.GroupLayout(UpperPanel);
        UpperPanel.setLayout(UpperPanelLayout);
        UpperPanelLayout.setHorizontalGroup(
            UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(UpperPanelLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(lblprev, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(cmbmonth, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblnext, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(spnYears, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(305, Short.MAX_VALUE))
        );
        UpperPanelLayout.setVerticalGroup(
            UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(UpperPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblprev, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbmonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblnext)
                    .addComponent(spnYears, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        dayPanel.setBackground(new java.awt.Color(255, 255, 255));
        dayPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        dayPanel.setLayout(new java.awt.GridLayout(1, 7));

        datePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        datePanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        datePanel.setLayout(new java.awt.GridLayout(6, 7));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(dayPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(UpperPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(datePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(UpperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(dayPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(datePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 425, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cmbmonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbmonthActionPerformed
        //        if(str != null)
        //        {
            selectedYear = new GregorianCalendar().get(Calendar.YEAR);
            selectedMonth = cmbmonth.getSelectedIndex();
            printCalendar();
            //}
    }//GEN-LAST:event_cmbmonthActionPerformed

    private void spnYearsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnYearsStateChanged
        // TODO add your handling code here:
        printCalendar();
    }//GEN-LAST:event_spnYearsStateChanged

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex)
        {
            java.util.logging.Logger.getLogger(CalendarPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(CalendarPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(CalendarPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(CalendarPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                JFrame  f = new JFrame();
                
                f.setTitle("Calendar");
                MainFrame.initialize();

                f.add(new CalendarPanel(-1,Color.CYAN));
                f.pack();
                f.setLocationRelativeTo(null);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
                
            }
        });
    }


    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel UpperPanel;
    private javax.swing.JComboBox<String> cmbmonth;
    private javax.swing.JPanel datePanel;
    private javax.swing.JPanel dayPanel;
    private javax.swing.JLabel lblnext;
    private javax.swing.JLabel lblprev;
    private javax.swing.JSpinner spnYears;
    // End of variables declaration//GEN-END:variables
};
