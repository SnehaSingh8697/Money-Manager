/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.chart.totalincmexpchart;

import domesticfinancesystem.MainFrame;
import domesticfinancesystem.chart.compincmexpchart.*;
import domesticfinancesystem.calendar.Database;
import domesticfinancesystem.chart.CheckboxListCellRenderer;
import domesticfinancesystem.calendar.DatePickerNewDialog;
import domesticfinancesystem.chart.piechart.PieChart;
import domesticfinancesystem.chart.Wallet;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
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
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFrame;
import javax.swing.SpinnerNumberModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.TextAnchor;

/**
 *
 * @author sneha
 */
public class TotalIncomeExpenseChart extends javax.swing.JPanel {

    /**
     * Creates new form ComparativeIncomeExpenseChart
     */
    private Database dc;
    private Connection con;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat chartDateFormatter = new SimpleDateFormat("dd-MM-yyyy");
    private Date fromDate;
    private Date toDate;
    private int maxPdNum;
    private ArrayList<BarChartData> arlData = new ArrayList<>();
    private ChartPanel cpanel;
    private String[] arrShortMonNames;
    private int curPdMonth = 0;
    private int curPdYear = 0;
    
    public TotalIncomeExpenseChart() {
        initComponents();
        
        dc = MainFrame.dc;
        con = dc.createConnection();
        
        cpanel = null;
        
        setPeriodicNum();//adding max pd numm
        
        getShortMonthNames();
        
        
//        //----------------setting monthly-------------
        
        SpinnerNumberModel snmYear1 = new SpinnerNumberModel(2019, 2019, 2030, 1);
        SpinnerNumberModel snmYear2 = new SpinnerNumberModel(2019, 2019, 2030, 1);
        spnFrmYear.setModel(snmYear1);
        spnToYear.setModel(snmYear2);
        
        getCurrentPdMonth();
//        
        spnFrmYear.setValue(curPdYear);
        spnToYear.setValue(curPdYear);

        getMonths();//adding months to the combobox

//        //-------------------------setting daily-------------------------
        GregorianCalendar gc = new GregorianCalendar();
        fromDate = getMaxPdDate();
        toDate = gc.getTime();
        lblFromDate.setText(""+formatter.format(fromDate));
        lblToDate.setText(""+formatter.format(toDate));
        
        radDaily.setSelected(true);
        showChartDaily();
//        //---------------------end-----------------------------
    }
    private void getCurrentPdMonth()
    {
        try {
            String sql = "Select extract(month from dt),extract(year from dt) from Pd where Num = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, maxPdNum);
            ResultSet rs;
            rs = pstmt.executeQuery();
            
            if(rs.next())
            {
               curPdMonth = rs.getInt(1);
               curPdYear = rs.getInt(2);
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(TotalIncomeExpenseChart.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void getShortMonthNames()
    {
        DateFormatSymbols dfs = new DateFormatSymbols();
        arrShortMonNames = dfs.getShortMonths();
        
    }
   
    private Date getMaxPdDate()
    {
        Date d = null;
        try {
            String sql = "Select dt from Pd where Num = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, maxPdNum);
            ResultSet rs;
            rs = pstmt.executeQuery();
            
            if(rs.next())
            {
                d = rs.getDate(1);
            }
            
            rs.close();
            pstmt.close();
            
            return d;
        } catch (SQLException ex) {
            Logger.getLogger(TotalIncomeExpenseChart.class.getName()).log(Level.SEVERE, null, ex);
        }
        return d;
    }
    public Date subtractDate(Date dt)
    {
        GregorianCalendar cal = new GregorianCalendar();
	cal.setTime(dt);
	cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }
    public void setPeriodicNum()
    {
        try {
            String sql = "Select Max(Num) from Pd";
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            if(rst.next())
            {
                maxPdNum = rst.getInt(1);
                if(maxPdNum!=0)
                {
                    SpinnerNumberModel snm1 = new SpinnerNumberModel(maxPdNum, 1, maxPdNum, 1);
                    SpinnerNumberModel snm2 = new SpinnerNumberModel(maxPdNum, 1, maxPdNum, 1);
                    spnFromPeriod.setModel(snm1);
                    spnFromPeriod.setValue(maxPdNum);
                    spnToPeriod.setModel(snm2);
                    spnToPeriod.setValue(maxPdNum);
                }
                else
                {
                    spnFromPeriod.setEnabled(false);
                    spnToPeriod.setEnabled(false);
                }
            }
            rst.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(PieChart.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void getMonths()
    {
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        
        ActionListener lsn1 = cmbFromMonth.getActionListeners()[0] ;
        cmbFromMonth.removeActionListener(lsn1);
        
        ActionListener lsn2 = cmbToMonth.getActionListeners()[0] ;
        cmbToMonth.removeActionListener(lsn2);
        
        for (String month : months) {
            
            cmbFromMonth.addItem(month);
            cmbToMonth.addItem(month);
        }
        
        cmbFromMonth.setSelectedIndex(curPdMonth - 1);
        cmbToMonth.setSelectedIndex(curPdMonth - 1);
        
        cmbFromMonth.addActionListener(lsn1);
        cmbToMonth.addActionListener(lsn2);
    }
    
    private void showChartPeriodically()
    {
        arlData.clear();
        int fromPeriod = (int)spnFromPeriod.getValue();
        int toPeriod = (int)spnToPeriod.getValue();
        
        try {
                while(fromPeriod <= toPeriod)
                {
                    Date startdate,fromdate = null;
                    
                       //getting the start date for a given period
                        String sql = "Select dt from Pd where Num = ?";
                        PreparedStatement pstmt = con.prepareStatement(sql);
                        pstmt.setInt(1, fromPeriod);
                        ResultSet rs;
                        rs = pstmt.executeQuery();
                        while(rs.next())
                        {
                            startdate = rs.getDate(1);
                        }
                        rs.close();
                        pstmt.close();
//                        
                        //getting the end date for a given period
                        
                        int nextPeriod = fromPeriod + 1;
                        sql = "Select dt from Pd where Num = ?";
                        pstmt = con.prepareStatement(sql);
                        pstmt.setInt(1, nextPeriod);
                        rs = pstmt.executeQuery();
                        Date date = null;
                        if(rs.next())
                        {
                            date = rs.getDate(1);
                        }
//                        
                        if(date != null)
                        {
                            toDate = subtractDate(date);
                        }
                        else
                        {
                            toDate = new GregorianCalendar().getTime();
                            String dt = formatter.format(toDate);
                            try {
                                   toDate = formatter.parse(dt);
                            } catch (ParseException ex) {
                                Logger.getLogger(TotalIncomeExpenseChart.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        rs.close();
                        pstmt.close();
                        
                        
                        //filling data for bar chart
                        
                        //getting income for the period
                        java.sql.Date fdt = new java.sql.Date(fromDate.getTime());
                        java.sql.Date tdt = new java.sql.Date(toDate.getTime());
                        sql = "Select sum(amount) from exttrans where dt between ? and ? and amount > 0";
           
                        pstmt = con.prepareStatement(sql);
                        pstmt.setDate(1, fdt);
                        pstmt.setDate(2, tdt);
                        rs = pstmt.executeQuery();
                        
                        float income = 0;
            
                        while(rs.next())
                        {
                            income = rs.getFloat(1);
                        }
                        rs.close();
                        pstmt.close();
               
                //getting expense for the period
                sql = "Select sum(amount) from exttrans where dt between ? and ? and amount < 0";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setDate(1, fdt);
                ps.setDate(2, tdt);
                ResultSet rst = ps.executeQuery();
                float expense = 0;
                while(rst.next())
                {
                    expense = rst.getFloat(1);
                    expense = -expense;
                }
                rst.close();
                ps.close();
                
                BarChartData bcd = new BarChartData(fromPeriod, "income", income);
                arlData.add(bcd);
                bcd = new BarChartData(fromPeriod, "expense", expense);
                arlData.add(bcd);
                fromPeriod++;
            }
           
            showBarChart();
        } catch (SQLException ex) {
            Logger.getLogger(TotalIncomeExpenseChart.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void showChartDaily()
    {
        try {
            
            arlData.clear();
            java.sql.Date fdt = new java.sql.Date(fromDate.getTime());
            java.sql.Date tdt = new java.sql.Date(toDate.getTime());
            String sql = "";
            String str = "";
            
            sql = "Select dt from exttrans where dt between ? and ? group by dt order by dt asc";
           
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setDate(1, fdt);
            pstmt.setDate(2, tdt);
            ResultSet rs;
            rs = pstmt.executeQuery();
            while(rs.next())
            {
                Date dat = rs.getDate(1);
                String dt = chartDateFormatter.format(dat);
                java.sql.Date sdat = new java.sql.Date(dat.getTime());
                
                //getting income for a given date
                String s = "Select sum(amount) from exttrans where amount > 0 and dt = ?";
                PreparedStatement ps = con.prepareStatement(s);
                ps.setDate(1, sdat);
                ResultSet rst = ps.executeQuery();
                float income = 0;
                if(rst.next())
                {
                    income = rst.getFloat(1);
                }
                rst.close();
                ps.close();
                
                //getting expense for a given date
                s = "Select sum(amount) from exttrans where amount < 0 and dt = ?";
                ps = con.prepareStatement(s);
                ps.setDate(1, sdat);
                rst = ps.executeQuery();
                float expense = 0;
                if(rst.next())
                {
                    expense = rst.getFloat(1);
                    if(expense < 0)
                       expense = -expense;
                }
                rst.close();
                ps.close();
                
                BarChartData bcd = new BarChartData(dt, "income", income);
                arlData.add(bcd);
                bcd = new BarChartData(dt, "expense", expense);
                arlData.add(bcd);
            }
            rs.close();
            pstmt.close();
            showBarChart();
        } catch (SQLException ex) {
            Logger .getLogger(TotalIncomeExpenseChart.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void showChart()
    {
        if(cpanel!=null)
        {
            if(radDaily.isSelected())
                 showChartDaily();
            else if(radMonthly.isSelected())
                showChartMonthly();
            else if(radPeriod.isSelected())
                showChartPeriodically();
        }
        else
        {
                chartPanel.remove(cpanel);
                chartPanel.validate();
                chartPanel.repaint();
        }
    }
    
    private void showChartMonthly()
    {
        try {
            
            arlData.clear();
            
            int fromYear = (int)spnFrmYear.getValue();
            int toYear = (int)spnToYear.getValue();
            
            int fromMonth = cmbFromMonth.getSelectedIndex() + 1;
            int toMonth = cmbToMonth.getSelectedIndex() + 1;
            
            String sql = "";
            String str = "";
            
            ResultSet rs;
            PreparedStatement pstmt;
            if(fromYear == toYear)
            {
               sql = "Select sum(e.amount),extract(year from e.Dt) as yr,extract(month from e.Dt) as mnth from Exttrans e where e.amount > 0 and ";
               sql+="extract(month from dt)>=? and extract(month from dt)<=? and extract(year from dt) = ? group by extract(year from e.Dt),extract(month from e.Dt) order by extract(year from e.Dt) asc, extract(month from e.Dt) asc"  ;     
            
                pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, fromMonth);
                pstmt.setInt(2, toMonth);
                pstmt.setInt(3, fromYear);
                
            }
            else
            {
                //get income 
                sql = "Select sum(e.amount),extract(year from e.Dt) as yr,extract(month from e.Dt) as mnth from Exttrans e where e.amount > 0 and ";
                sql+="((extract(year from e.Dt) = ? and extract(month from e.Dt) >= ?) or (extract(year from e.Dt) = ? and extract(month from e.Dt) <= ?) or (extract(year from e.Dt) between ? and ?)) group by extract(year from e.Dt),extract(month from e.Dt) order by extract(year from e.Dt) asc, extract(month from e.Dt) asc"  ;     

                pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, fromYear);
                pstmt.setInt(2, fromMonth);
                pstmt.setInt(3, toYear);
                pstmt.setInt(4, toMonth);
                pstmt.setInt(5, fromYear + 1);
                pstmt.setInt(6, toYear - 1);
            }
            rs = pstmt.executeQuery();
            while(rs.next())
            {
                float amt = rs.getFloat(1);
                int yr = rs.getInt(2);
                int mnth = rs.getInt(3);
                String mon = arrShortMonNames[mnth - 1];
                String mnyr = mon+" "+yr;
                BarChartData bd = new BarChartData("ïncome", amt, mnyr);
                arlData.add(bd);
            }
            
            rs.close();
            pstmt.close();
            
             //get expense 
             if(fromYear == toYear)
            {
               sql = "Select sum(e.amount),extract(year from e.Dt) as yr,extract(month from e.Dt) as mnth from Exttrans e where e.amount < 0 and ";
               sql+="extract(month from dt)>=? and extract(month from dt)<=? and extract(year from dt) = ? group by extract(year from e.Dt),extract(month from e.Dt) order by extract(year from e.Dt) asc, extract(month from e.Dt) asc"  ;     
            
                pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, fromMonth);
                pstmt.setInt(2, toMonth);
                pstmt.setInt(3, fromYear);
                
            }
            else
            {
                sql = "Select sum(e.amount),extract(year from e.Dt) as yr,extract(month from e.Dt) as mnth from Exttrans e where e.amount < 0 and ";
                sql+="((extract(year from e.Dt) = ? and extract(month from e.Dt) >= ?) or (extract(year from e.Dt) = ? and extract(month from e.Dt) <= ?) or (extract(year from e.Dt) between ? and ?)) group by extract(year from e.Dt),extract(month from e.Dt) order by extract(year from e.Dt) asc, extract(month from e.Dt) asc"  ;     

                pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, fromYear);
                pstmt.setInt(2, fromMonth);
                pstmt.setInt(3, toYear);
                pstmt.setInt(4, toMonth);
                pstmt.setInt(5, fromYear + 1);
                pstmt.setInt(6, toYear - 1);
            }
            rs = pstmt.executeQuery();
            while(rs.next())
            {
                float amt = rs.getFloat(1);
                int yr = rs.getInt(2);
                int mnth = rs.getInt(3);
                if(amt<0)
                    amt = -amt;
                String mon = arrShortMonNames[mnth - 1];
                String mnyr = mon+" "+yr;
                BarChartData bd = new BarChartData("expense", amt, mnyr);
                arlData.add(bd);
            }
            rs.close();
            pstmt.close();
            showBarChart();
        } catch (SQLException ex) {
            Logger.getLogger(TotalIncomeExpenseChart.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    private void showBarChart()
    {
        if(cpanel!= null)
        {
            chartPanel.remove(cpanel);
            chartPanel.repaint();
        }
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String str = null;
        if(radDaily.isSelected())
        {
            for (BarChartData bd : arlData) {
                dataset.addValue(bd.getAmount(), bd.getAmountType(),bd.getDate());
            }
            str = "Dates";
        }
        else if(radMonthly.isSelected())
        {
            for (BarChartData bd : arlData) {
                dataset.addValue(bd.getAmount(), bd.getAmountType(),bd.getMnyr());
            }
            str = "Month-Year";
        }
        else
        {
            for (BarChartData bd : arlData) {
                dataset.addValue(bd.getAmount(),bd.getAmountType(),""+bd.getPdNum());
            }
            str = "Periodic Deposit";
        }
        
       JFreeChart barChart = ChartFactory.createBarChart("Total Income/Expense Chart", str, "Amount", dataset, PlotOrientation.VERTICAL, true, true, false);
       Plot barPlot = barChart.getPlot();
       
        
        if(dataset.getColumnCount() == 0)
        {
            barPlot.setNoDataMessage("No data is available");
        }
        
        //showing data value for each bar
        CategoryItemRenderer renderer = ((CategoryPlot)barChart.getPlot()).getRenderer();
         
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setBaseItemLabelsVisible(true);
        ItemLabelPosition position = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, 
                TextAnchor.TOP_CENTER);
        renderer.setBasePositiveItemLabelPosition(position);

       
       cpanel = new ChartPanel(barChart);
       cpanel.setVisible(true);
       chartPanel.add(cpanel,BorderLayout.CENTER);
       chartPanel.validate();
       
    }
            
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TotalIncomeExpenseChart.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TotalIncomeExpenseChart.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TotalIncomeExpenseChart.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TotalIncomeExpenseChart.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                JFrame  f = new JFrame();
                f.setTitle("Bar Chart");
                f.add(new TotalIncomeExpenseChart());
                f.pack();
                f.setLocationRelativeTo(null);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
            }
        });
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        radgrpIncmExp = new javax.swing.ButtonGroup();
        radgrpWalType = new javax.swing.ButtonGroup();
        radgrpPeriod = new javax.swing.ButtonGroup();
        jPanel2 = new javax.swing.JPanel();
        radDaily = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        lblFromDate = new javax.swing.JLabel();
        cmbToMonth = new javax.swing.JComboBox<>();
        btnFromDate = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblToDate = new javax.swing.JLabel();
        radPeriod = new javax.swing.JRadioButton();
        btnToDate = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        radMonthly = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        cmbFromMonth = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        spnFrmYear = new javax.swing.JSpinner();
        spnToYear = new javax.swing.JSpinner();
        spnFromPeriod = new javax.swing.JSpinner();
        spnToPeriod = new javax.swing.JSpinner();
        chartPanel = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Interval", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 204))); // NOI18N

        radgrpPeriod.add(radDaily);
        radDaily.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radDaily.setText("Daily");
        radDaily.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radDailyActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel1.setText("From");

        jLabel5.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel5.setText("To Month");

        lblFromDate.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        cmbToMonth.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        cmbToMonth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbToMonthActionPerformed(evt);
            }
        });

        btnFromDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/calendar.png"))); // NOI18N
        btnFromDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFromDateActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel6.setText("Year");

        jLabel3.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel3.setText("To");

        lblToDate.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        radgrpPeriod.add(radPeriod);
        radPeriod.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radPeriod.setText("Periodically");
        radPeriod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radPeriodActionPerformed(evt);
            }
        });

        btnToDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/calendar.png"))); // NOI18N
        btnToDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnToDateActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel7.setText("From");

        radgrpPeriod.add(radMonthly);
        radMonthly.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radMonthly.setText("Monthly");
        radMonthly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radMonthlyActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel2.setText("From Month");

        jLabel8.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel8.setText("To");

        cmbFromMonth.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        cmbFromMonth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbFromMonthActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel4.setText("Year");

        spnFrmYear.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        spnFrmYear.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnFrmYearStateChanged(evt);
            }
        });

        spnToYear.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        spnToYear.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnToYearStateChanged(evt);
            }
        });

        spnFromPeriod.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        spnFromPeriod.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnFromPeriodStateChanged(evt);
            }
        });

        spnToPeriod.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        spnToPeriod.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnToPeriodStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(radDaily)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 41, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(radMonthly)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(cmbFromMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(spnFrmYear, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(spnFromPeriod, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                                            .addComponent(cmbToMonth, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(spnToYear, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(spnToPeriod))))))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(radPeriod)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnToDate)
                    .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(radDaily)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnFromDate))
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radMonthly)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbFromMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(spnFrmYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbToMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnToYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radPeriod)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnFromPeriod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnToPeriod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        chartPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        chartPanel.setLayout(new java.awt.BorderLayout());

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/chart.png"))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(chartPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 761, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(123, 123, 123)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(45, 45, 45))))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnFromDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFromDateActionPerformed
        // TODO add your handling code here:
         String str = lblFromDate.getText().trim();
            Date d;
            try
            {
               d = formatter.parse(str) ;
            }
            catch (ParseException ex)
            {
                lblFromDate.setText("");
                d = new GregorianCalendar().getTime();  
            }
        
        DatePickerNewDialog dlg;
        dlg = new DatePickerNewDialog(null, true, d);
        dlg.setVisible(true);
        Date dt = dlg.getSelectedDate();
       
        if(dt!=null)
        {
            try {
                String s = formatter.format(dt);
                dt = formatter.parse(s);
                lblFromDate.setText(formatter.format(dt));
            } catch (ParseException ex) {
                Logger.getLogger(TotalIncomeExpenseChart.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        fromDate = dt;
        dlg.dispose();
        
        showChartDaily();

    }//GEN-LAST:event_btnFromDateActionPerformed

    private void btnToDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnToDateActionPerformed
        // TODO add your handling code here:
            String str = lblToDate.getText().trim();
            Date d;
            try
            {
               d = formatter.parse(str) ;
            }
            catch (ParseException ex)
            {
                lblToDate.setText("");
                d = new GregorianCalendar().getTime();  
            }
        
        DatePickerNewDialog dlg;
        dlg = new DatePickerNewDialog(null, true, d);
        dlg.setVisible(true);
        Date dt = dlg.getSelectedDate();
       
        if(dt!=null)
        {
            try {
                String s = formatter.format(dt);
                dt = formatter.parse(s);
                lblToDate.setText(formatter.format(dt));
            } catch (ParseException ex) {
                Logger.getLogger(TotalIncomeExpenseChart.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        toDate = dt;
        dlg.dispose();
        showChartDaily();

    }//GEN-LAST:event_btnToDateActionPerformed

    private void radDailyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radDailyActionPerformed
        // TODO add your handling code here:
        showChartDaily();
    }//GEN-LAST:event_radDailyActionPerformed

    private void cmbFromMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbFromMonthActionPerformed
        // TODO add your handling code here:
        if(cmbFromMonth.getSelectedIndex()>=0)
        {
          showChart();

        }
    }//GEN-LAST:event_cmbFromMonthActionPerformed

    private void cmbToMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbToMonthActionPerformed
        // TODO add your handling code here:
        if(cmbFromMonth.getSelectedIndex()>=0)
        {
           showChart();
        }

    }//GEN-LAST:event_cmbToMonthActionPerformed

    private void spnFrmYearStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnFrmYearStateChanged
        // TODO add your handling code here:
        showChartMonthly();
    }//GEN-LAST:event_spnFrmYearStateChanged

    private void spnToYearStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnToYearStateChanged
        // TODO add your handling code here:
        showChartMonthly();
    }//GEN-LAST:event_spnToYearStateChanged

    private void radMonthlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radMonthlyActionPerformed
        // TODO add your handling code here:
        showChartMonthly();
    }//GEN-LAST:event_radMonthlyActionPerformed

    private void radPeriodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radPeriodActionPerformed
        // TODO add your handling code here:
        showChart();
    }//GEN-LAST:event_radPeriodActionPerformed

    private void spnFromPeriodStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnFromPeriodStateChanged
        // TODO add your handling code here:
        showChart();
    }//GEN-LAST:event_spnFromPeriodStateChanged

    private void spnToPeriodStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnToPeriodStateChanged
        // TODO add your handling code here:
        showChart();
    }//GEN-LAST:event_spnToPeriodStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFromDate;
    private javax.swing.JButton btnToDate;
    private javax.swing.JPanel chartPanel;
    private javax.swing.JComboBox<String> cmbFromMonth;
    private javax.swing.JComboBox<String> cmbToMonth;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JRadioButton radDaily;
    private javax.swing.JRadioButton radMonthly;
    private javax.swing.JRadioButton radPeriod;
    private javax.swing.ButtonGroup radgrpIncmExp;
    private javax.swing.ButtonGroup radgrpPeriod;
    private javax.swing.ButtonGroup radgrpWalType;
    private javax.swing.JSpinner spnFrmYear;
    private javax.swing.JSpinner spnFromPeriod;
    private javax.swing.JSpinner spnToPeriod;
    private javax.swing.JSpinner spnToYear;
    // End of variables declaration//GEN-END:variables
}
