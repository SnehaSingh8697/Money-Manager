/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.calendar;

import domesticfinancesystem.MainFrame;
import domesticfinancesystem.calendar.moonphase.MoonPhase;
import domesticfinancesystem.exttrans.ExternalTransactionPanel;
import domesticfinancesystem.regularservice.RegularServicePanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author sneha
 */
public class CalCellRenderer extends javax.swing.JPanel {

    /**
     * Creates new form CalendarCellRenderer
     */
    private int daynumber;
    private static final int maxholidaycount = 10;
    private int income;
    private int expense;
    private int curHolidayindex;
    private boolean isItToday;
    private boolean isSelected;
    private char monthtype;
    private char satsun;
    private int totHolidays;
    private Holiday[] arHolidays = new Holiday[maxholidaycount] ;
    private Date date;
    
    private Image tempholidayimg ;
    private String tempholidayname;

    public int getTotHolidays() {
        return totHolidays;
    }

    public int getIncome() {
        return income;
    }

    public void setIncome(int income) {
        this.income = income;
    }

    public int getExpense() {
        return expense;
    }

    public void setExpense(int expense) {
        this.expense = expense;
    }
    
    
    
    public void addHoliday(Holiday h)
    {
        arHolidays[totHolidays++] = h;
    }
    
    public void nextHoliday()
    {
        if(totHolidays>0)
        {
            curHolidayindex = (curHolidayindex + 1)%totHolidays;
            render();
        }
    }
    
    public void removeHolidays()
    {
        totHolidays = 0;
        curHolidayindex = 0;
    }
    
    private void render()
    {
        
        if(totHolidays == 0) //no holiday for this date
        {
           lblMiddle.setIcon(null);
            lblMiddle.setText(""+daynumber);
            lblLower.setText(null);

        }
        else//one or more holidays present for this date
        {
            lblMiddle.setText(null);
            lblMiddle.setIcon(new ImageIcon(arHolidays[curHolidayindex].getImage()));
            lblLower.setText(arHolidays[curHolidayindex].getName());
            
        }
    }
    
    public void tempReomveHoliday()
    {
        tempholidayimg = null;
        tempholidayname = null;
        render();
        
    }
    
    public void tempSetHoliday(Image img, String nm)
    {
        tempholidayimg = img;
        tempholidayname = nm;
        render();
    }
    
    public void renderIncomeExpense()
    {
        lblIncome.setText(""+income);
        lblExpense.setText(""+expense);
    }
   
   public  void setValue(int num,char ch)
    {
        lblMiddle.setText(""+num);
        lblDay.setText(String.format("%-2s", ""+num));
        renderIncomeExpense();
        daynumber = num;
        satsun = ch;
        render();
    }
    
    public char getSatSun()
    {
        return satsun;
    }
    
    public void displayMoonImage(int index)
    {
        Image img = MainFrame.moonImages[index];
        lblMoon.setIcon(new ImageIcon(img));
    }
     
     public void setMoonImageOLD(Date d)
     {
        date = d;
        MoonPhase mp = new MoonPhase(d);
        double mf = mp.moonFraction*1000;
        double phaseNumber = 1000.0/MainFrame.moonImages.length ;
        int index  = (int)(Math.round(mf/phaseNumber));
        if(index == MainFrame.moonImages.length)
            index = 0;
         displayMoonImage(index);
     }

     public void setMoonImage(Date d)
     {
        date = d;
        
//        GregorianCalendar gc = new GregorianCalendar(2019, Calendar.SEPTEMBER, 14) ;
//        d.setTime(gc.getTime().getTime()) ;
        MoonPhase mp = new MoonPhase(d);
        int index  = (int)(Math.round(mp.moonFraction*MainFrame.moonImages.length));
        if(index == MainFrame.moonImages.length)
            index = 0;
         displayMoonImage(index);
     }

    
    public void setLabelsDisabled()
    {
        lblDay.setEnabled(false);
        lblExpense.setEnabled(false);
        lblIncome.setEnabled(false);
        lblMoon.setEnabled(false);
        lblLower.setEnabled(false);
        lblMiddle.setEnabled(false);
    }
    
     public void setLabelsEnabled()
    {
        lblDay.setEnabled(true);
        lblExpense.setEnabled(true);
        lblIncome.setEnabled(true);
        lblMoon.setEnabled(true);
        lblLower.setEnabled(true);
        lblMiddle.setEnabled(true);
    }
    
    public int getDaynumber()
    {
        return daynumber;
    }
    
    
    public void highlightLabel(Color color)
    {
        lblMiddle.setForeground(color);
        lblMiddle.setText(lblMiddle.getText());
    }
   
    public CalCellRenderer() {
        initComponents();
        Dimension d = lblLower.getPreferredSize() ;
        lblLower.setMaximumSize(d);
        lblLower.setMinimumSize(d);
        lblLower.setPreferredSize(d);
        
        MouseListener click = new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if(ExternalTransactionPanel.extTransExists(date))
                {
                    JFrame f = null;
                    JDialog dlg= new JDialog(f,true);
                    ExternalTransactionPanel etp = new ExternalTransactionPanel(date);
                    dlg.add(etp);
                    dlg.setTitle("External Transaction");
                    dlg.pack();
                    dlg.setLocationRelativeTo(null);
                    dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    dlg.setVisible(true);
                }
                else
                {
                  JOptionPane.showMessageDialog(null, "No external transcations available", "Message", JOptionPane.INFORMATION_MESSAGE);
                }
        }

    };
      this.addMouseListener(click);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        upperCellPanel = new javax.swing.JPanel();
        lblDay = new javax.swing.JLabel();
        lblIncome = new javax.swing.JLabel();
        lblExpense = new javax.swing.JLabel();
        lblMoon = new javax.swing.JLabel();
        lblMiddle = new javax.swing.JLabel();
        southernPanel = new javax.swing.JPanel();
        lblLower = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setMaximumSize(new java.awt.Dimension(95, 50));
        setPreferredSize(new java.awt.Dimension(95, 100));
        setLayout(new java.awt.BorderLayout());

        upperCellPanel.setBackground(new java.awt.Color(255, 255, 255));

        lblDay.setBackground(new java.awt.Color(255, 255, 255));
        lblDay.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblDay.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDay.setText("23");
        lblDay.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));

        lblIncome.setBackground(new java.awt.Color(255, 255, 255));
        lblIncome.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblIncome.setForeground(new java.awt.Color(0, 153, 0));
        lblIncome.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblIncome.setText("5368");
        lblIncome.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblExpense.setBackground(new java.awt.Color(255, 255, 255));
        lblExpense.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblExpense.setForeground(new java.awt.Color(255, 51, 0));
        lblExpense.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblExpense.setText("2085");
        lblExpense.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblMoon.setBackground(new java.awt.Color(255, 255, 255));
        lblMoon.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblMoon.setForeground(new java.awt.Color(255, 204, 0));
        lblMoon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/domesticfinancesystem/calendar/moonSample.jpg"))); // NOI18N

        javax.swing.GroupLayout upperCellPanelLayout = new javax.swing.GroupLayout(upperCellPanel);
        upperCellPanel.setLayout(upperCellPanelLayout);
        upperCellPanelLayout.setHorizontalGroup(
            upperCellPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upperCellPanelLayout.createSequentialGroup()
                .addComponent(lblDay)
                .addGap(2, 2, 2)
                .addComponent(lblIncome, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(lblExpense, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblMoon)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        upperCellPanelLayout.setVerticalGroup(
            upperCellPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upperCellPanelLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(upperCellPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblMoon, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(upperCellPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblDay)
                        .addComponent(lblIncome)
                        .addComponent(lblExpense))))
        );

        add(upperCellPanel, java.awt.BorderLayout.PAGE_START);

        lblMiddle.setBackground(new java.awt.Color(255, 255, 255));
        lblMiddle.setFont(new java.awt.Font("Imprint MT Shadow", 0, 42)); // NOI18N
        lblMiddle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMiddle.setText("23");
        add(lblMiddle, java.awt.BorderLayout.CENTER);

        southernPanel.setBackground(new java.awt.Color(255, 255, 255));
        southernPanel.setLayout(new java.awt.BorderLayout());

        lblLower.setBackground(new java.awt.Color(255, 255, 255));
        lblLower.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lblLower.setForeground(new java.awt.Color(153, 0, 153));
        lblLower.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLower.setText("Nothing Special");
        southernPanel.add(lblLower, java.awt.BorderLayout.CENTER);

        add(southernPanel, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblDay;
    private javax.swing.JLabel lblExpense;
    private javax.swing.JLabel lblIncome;
    private javax.swing.JLabel lblLower;
    private javax.swing.JLabel lblMiddle;
    private javax.swing.JLabel lblMoon;
    private javax.swing.JPanel southernPanel;
    private javax.swing.JPanel upperCellPanel;
    // End of variables declaration//GEN-END:variables
}
