/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.calendar;

import domesticfinancesystem.regularservice.*;
import domesticfinancesystem.exttrans.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

/**
 *
 * @author Sneha
 */
public class DatePickerNewDialog extends javax.swing.JDialog
{

    /**
     * Creates new form DatePickerNewDialog
     */
    private JLabel[] lblmonthdates = new JLabel[42];
    private int selectedDay;
    private int selectedMonth;
    private int selectedYear;
    private MouseListener enter;
    private MouseListener exit;
    private MouseListener click;
    private Date selecteddate = null;
    private Color headColor ;
    private Font font;

    public DatePickerNewDialog(java.awt.Frame parent, boolean modal)
    {
        this(parent, modal, new GregorianCalendar().getTime()) ;

    }
    
    public Date getSelectedDate()
    {

        return selecteddate;
    }
    
    public void setYearMonthsDay()
    {
        JLabel lbldays[] = new JLabel[7];
        DateFormatSymbols dfs = new DateFormatSymbols();
        String months[] = dfs.getMonths();
        
        String[] days = dfs.getShortWeekdays();
        Font fnt = new Font("Garamond", Font.PLAIN, 16);

        
        
        for (int i = 0; i < lbldays.length; i++)
        {
            lbldays[i] = new JLabel();
//            lbldays[i].setBorder(BorderFactory.createLineBorder(Color.GRAY));
            lbldays[i].setFont(fnt);
             lbldays[i].setOpaque(true);
            lbldays[i].setBackground(headColor);
            lbldays[i].setVisible(true);
            lbldays[i].setHorizontalAlignment(JLabel.CENTER);
            
            if(i == 0)
            {
                lbldays[i].setForeground(Color.red);

                lbldays[i].setText(days[i+1]);
            }
            else
                lbldays[i].setText(days[i+1]);

            dayPanel.add(lbldays[i]);
            
        }
        
        //datePanel.setLayout(new GridLayout(6,7));
        
        
        for (int i = 0; i < lblmonthdates.length; i++)
        {
            lblmonthdates[i] = new JLabel();
            datePanel.add(lblmonthdates[i]);
            lblmonthdates[i].setBorder(BorderFactory.createLineBorder(Color.GRAY));
            lblmonthdates[i].addMouseListener(enter);
            lblmonthdates[i].addMouseListener(exit);
            lblmonthdates[i].addMouseListener(click);
            
        }
    }
    
    public void printCalendar()
    {
        for (JLabel lblmonthdate : lblmonthdates)
        {
                lblmonthdate.setEnabled(true);
                lblmonthdate.setForeground(Color.black);
                lblmonthdate.setOpaque(true);
                lblmonthdate.setBackground(Color.WHITE);
                lblmonthdate.setHorizontalAlignment(JLabel.CENTER);

        }
        
        selectedYear = Integer.parseInt(txtyear.getText());
        selectedMonth = cmbmonth.getSelectedIndex();
        
        GregorianCalendar gc = new GregorianCalendar(selectedYear,selectedMonth,1);
        
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
        
        int day =(prevmonthdays - prevday)+1;           //Dates to be printed from the previous month
        
        for (int i = 0; i<prevday ; i++)
        {
            lblmonthdates[i].setFont(font);
            lblmonthdates[i].setText(""+day);
            lblmonthdates[i].setEnabled(false);
            day++;
            labelcount++;
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
            lblmonthdates[i].setFont(font);
            str = j + ""; 

            if(j == currentday && currentmonth == selectedMonth && currentyear == selectedYear)
            {
                lblmonthdates[i].setForeground(Color.red);
            }
                lblmonthdates[i].setText(str);
                labelcount++;
            j++;
        }
        int nextmonthdays = 1;
        
        
        
            while(true)
            {
                if(labelcount<42)
                {
                    str = String.format("%d",nextmonthdays); 
                    lblmonthdates[i].setFont(font);

                    lblmonthdates[i].setText(str);

                    lblmonthdates[i].setEnabled(false);

                    i++;

                    nextmonthdays++;

                    labelcount++;
                }
                else
                    break;
            }
        
            
    }
            
            
    public DatePickerNewDialog(java.awt.Frame parent, boolean modal, Date date)
    {
        super(parent, modal);
        
        initComponents();
        
        font = new Font("Imprint MT Shadow", Font.PLAIN, 14);
        
        headColor = new Color(255, 255, 204);
        
        enter = new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                
                super.mouseEntered(e); //To change body of generated methods, choose Tools | Templates.
                JLabel lblTemp = (JLabel)e.getComponent();
                String str = lblTemp.getText();
                lblTemp.setForeground(Color.GREEN);
                lblTemp.setText(str);
            }
            
        };
        exit = new MouseAdapter()
        {
            @Override
            public void mouseExited(MouseEvent e)
            {
                super.mouseExited(e); //To change body of generated methods, choose Tools | Templates.
                JLabel lbltemp = (JLabel)e.getComponent();
                int currentday = new GregorianCalendar().get(Calendar.DAY_OF_MONTH);
                int currentmonth = new GregorianCalendar().get(Calendar.MONTH);
                int currentyear = new GregorianCalendar().get(Calendar.YEAR);
                
                String str = lbltemp.getText();
                
                if(Integer.parseInt(str) == currentday && selectedMonth == currentmonth && selectedYear == currentyear)
                    lbltemp.setForeground(Color.RED);
                else
                    lbltemp.setForeground(Color.BLACK);
                    
                lbltemp.setText(str);
            }
            
        };
        
        click = new MouseAdapter()
        {

            
            
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if(e.getClickCount() == 2)
                {
                    JLabel lbltemp = (JLabel)e.getComponent();
                    if(lbltemp.isEnabled() == true)
                    {
                            selectedDay = Integer.parseInt(lbltemp.getText());
                            selecteddate = new GregorianCalendar(selectedYear, selectedMonth, selectedDay).getTime();

                            setVisible(false);
                    }   
                }
                
            }
            
        };
        
       
        
        
        GregorianCalendar dt;
        
        dt = new GregorianCalendar() ;
        dt.setTime(date);
        
        DateFormatSymbols dfs = new DateFormatSymbols();

        String months[] = dfs.getMonths();

        setYearMonthsDay();
       
        selectedYear = dt.get(Calendar.YEAR);

        selectedDay = dt.get(Calendar.DAY_OF_MONTH);
        
        txtyear.setText(""+selectedYear);
        
        for (String month : months)
        {
           cmbmonth.addItem(month);
        }
        
        selectedMonth = dt.get(Calendar.MONTH);
        
        cmbmonth.setSelectedIndex(selectedMonth);

        printCalendar();

        setLocationRelativeTo(null);
        
        lblnext.setEnabled(true);
        lblprev.setEnabled(true);
        lblnext2.setEnabled(true);
        lblprev2.setEnabled(true);
        
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
                printCalendar();
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
                printCalendar();
            }
        });
        
        lblnext2.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                selectedYear = Integer.parseInt(txtyear.getText())+1;
                txtyear.setText(""+selectedYear);
                printCalendar();
            }
            

        });
        
        lblprev2.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                selectedYear = Integer.parseInt(txtyear.getText())-1;

                txtyear.setText(""+selectedYear);
                printCalendar();
            }
        });
        pack();
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
        lblprev2 = new javax.swing.JLabel();
        txtyear = new javax.swing.JTextField();
        lblnext2 = new javax.swing.JLabel();
        dayPanel = new javax.swing.JPanel();
        datePanel = new javax.swing.JPanel();
        btntoday = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        lblprev.setText("      <");

        cmbmonth.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        cmbmonth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbmonthActionPerformed(evt);
            }
        });

        lblnext.setText(">");

        lblprev2.setText("     <");

        txtyear.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        txtyear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtyearActionPerformed(evt);
            }
        });

        lblnext2.setText(">");

        javax.swing.GroupLayout UpperPanelLayout = new javax.swing.GroupLayout(UpperPanel);
        UpperPanel.setLayout(UpperPanelLayout);
        UpperPanelLayout.setHorizontalGroup(
            UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(UpperPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(lblprev, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cmbmonth, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblnext, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblprev2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(txtyear, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblnext2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(33, Short.MAX_VALUE))
        );
        UpperPanelLayout.setVerticalGroup(
            UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(UpperPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(UpperPanelLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(lblnext2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblprev, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cmbmonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblnext)
                        .addComponent(lblprev2)
                        .addComponent(txtyear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        dayPanel.setLayout(new java.awt.GridLayout(1, 7));

        datePanel.setLayout(new java.awt.GridLayout(6, 7));

        btntoday.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        btntoday.setForeground(new java.awt.Color(153, 0, 0));
        btntoday.setText("Today");
        btntoday.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btntodayActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        jLabel1.setText("double click to select date");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(UpperPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(dayPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(datePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btntoday)
                .addGap(47, 47, 47))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(UpperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dayPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(datePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btntoday))
                .addGap(9, 9, 9))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btntodayActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btntodayActionPerformed
    {//GEN-HEADEREND:event_btntodayActionPerformed
        GregorianCalendar gc = new GregorianCalendar();
        selecteddate = gc.getTime();
        setVisible(false);


    }//GEN-LAST:event_btntodayActionPerformed

    private void cmbmonthActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cmbmonthActionPerformed
    {//GEN-HEADEREND:event_cmbmonthActionPerformed
                selectedYear = new GregorianCalendar().get(Calendar.YEAR);
                selectedMonth = cmbmonth.getSelectedIndex();
                printCalendar();
    }//GEN-LAST:event_cmbmonthActionPerformed

    private void txtyearActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_txtyearActionPerformed
    {//GEN-HEADEREND:event_txtyearActionPerformed
        String str = txtyear.getText();
        try
        {
            int n = Integer.parseInt(str);
            selectedYear = n;
            txtyear.setText(""+selectedYear);
            printCalendar();
        }
        catch(NumberFormatException ex)
        {                                                                                                                                                                                                       
            GregorianCalendar grg = new GregorianCalendar();
            selectedYear = grg.get(Calendar.YEAR);
            txtyear.setText(""+selectedYear);
            printCalendar();
        }
    }//GEN-LAST:event_txtyearActionPerformed

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
            java.util.logging.Logger.getLogger(DatePickerNewDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(DatePickerNewDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(DatePickerNewDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(DatePickerNewDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                DatePickerNewDialog dialog = new DatePickerNewDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter()
                {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e)
                    {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel UpperPanel;
    private javax.swing.JButton btntoday;
    private javax.swing.JComboBox<String> cmbmonth;
    private javax.swing.JPanel datePanel;
    private javax.swing.JPanel dayPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblnext;
    private javax.swing.JLabel lblnext2;
    private javax.swing.JLabel lblprev;
    private javax.swing.JLabel lblprev2;
    private javax.swing.JTextField txtyear;
    // End of variables declaration//GEN-END:variables
}
