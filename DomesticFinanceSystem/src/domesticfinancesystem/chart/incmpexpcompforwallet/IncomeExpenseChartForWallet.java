/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.chart.incmpexpcompforwallet;

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
public class IncomeExpenseChartForWallet extends javax.swing.JPanel {

    /**
     * Creates new form ComparativeIncomeExpenseChart
     */
    private Database dc;
    private Connection con;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat chaartDateFormatter = new SimpleDateFormat("dd-MM-yyyy");
    private DefaultListModel<Integer> yearModel = new DefaultListModel<>();
    private Date fromDate;
    private Date toDate;
    private int maxPdNum;
    private ArrayList<BarChartData> arlData = new ArrayList<>();
    private ArrayList<Wallet> arlWallets = new ArrayList<>();
    private ArrayList<Wallet> arlParty = new ArrayList<>();
    private ArrayList<Integer> arlYears = new ArrayList<>();
    private ChartPanel cpanel;
    private String[] arrShortMonNames;
    private int curPdMonth = 0;
    private int curPdYear = 0;
    private int[] arrDays = {31,29,31,30,31,30,31,31,30,31,30,31};
    private SpinnerNumberModel snmDays1;
    private SpinnerNumberModel snmDays2;
    
    public IncomeExpenseChartForWallet() {
        initComponents();
        
        dc = MainFrame.dc;
        con = dc.createConnection();
        
        cpanel = null;
        
        setPeriodicNum();//adding max pd numm
        
        getShortMonthNames();
        
        //adding models to list
        lstYear.setModel(yearModel);
        setYearsToList();
        
        getAllWalletsAndParty();
        
        radWallet.setSelected(true);
        radExpense.setSelected(true);
        
                //  === Makes more than one check box in a list box checked at a time === //
        lstYear.setSelectionModel(new DefaultListSelectionModel() 
        {
            @Override
            public void setSelectionInterval(int index0, int index1) 
            {
                if(super.isSelectedIndex(index0)) 
                {
                    super.removeSelectionInterval(index0, index1);
                    showChart();
                }
                else
                {
                    super.addSelectionInterval(index0, index1);
                    showChart();
                }
            }
        });
        
        lstYear.setCellRenderer(new CheckboxListCellRenderer());
        
        for (int i = 0; i < yearModel.getSize(); i++) {
           lstYear.setSelectedIndex(i);
        }
      
        getCurrentPdMonth();
//        
        getMonths();//adding months to the combobox
      
//        //-------------------------setting Periodically-------------------------
        
        snmDays1 = new SpinnerNumberModel(1, 1 , 30, 1);
        spnPdFromDt.setModel(snmDays1);
        snmDays2 = new SpinnerNumberModel(1, 1 , 30, 1);
        spnPdToDt.setModel(snmDays2);
//       
        
        cmbFromMonth.setSelectedIndex(curPdMonth - 1);
        cmbPdFromMonth.setSelectedIndex(curPdMonth - 1);
        
        
        fromDate = getMaxPdDate();
        int day = fromDate.getDate();
        snmDays1.setMaximum(arrDays[curPdMonth - 1]);
        snmDays1.setValue(day);
        
        GregorianCalendar gc = new GregorianCalendar();
        toDate = gc.getTime();
        day = toDate.getDate();
        cmbToMonth.setSelectedIndex(toDate.getMonth());
        cmbPdToMonth.setSelectedIndex(toDate.getMonth());
        snmDays2.setMaximum(arrDays[curPdMonth - 1]);
        snmDays2.setValue(day);
        
        
//        //---------------------end-----------------------------
        radMonthly.setSelected(true);
        showChartMonthly();
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
            Logger.getLogger(IncomeExpenseChartForWallet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void getShortMonthNames()
    {
        DateFormatSymbols dfs = new DateFormatSymbols();
        arrShortMonNames = dfs.getShortMonths();
    }
    
    private void setYearsToList()
    {
       try {
            String sql = "Select extract(year from dt) from exttrans group by extract(year from dt) order by extract(year from dt)";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while(rs.next())
            {
                int year = rs.getInt(1);
                yearModel.addElement(year);
            }
        } catch (SQLException ex) {
            Logger.getLogger(IncomeExpenseChartForWallet.class.getName()).log(Level.SEVERE, null, ex);
        } 
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
            return d;
        } catch (SQLException ex) {
            Logger.getLogger(IncomeExpenseChartForWallet.class.getName()).log(Level.SEVERE, null, ex);
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
        
        ActionListener lsn3 = cmbPdFromMonth.getActionListeners()[0] ;
        cmbPdFromMonth.removeActionListener(lsn3);
        
        ActionListener lsn4 = cmbPdToMonth.getActionListeners()[0] ;
        cmbPdToMonth.removeActionListener(lsn4);
        
        for (String month : months) {
            
            cmbFromMonth.addItem(month);
            cmbToMonth.addItem(month);
            cmbPdFromMonth.addItem(month);
            cmbPdToMonth.addItem(month);
        }
        
        cmbFromMonth.setSelectedIndex(curPdMonth - 1);
        cmbToMonth.setSelectedIndex(curPdMonth - 1);
        cmbPdFromMonth.setSelectedIndex(curPdMonth - 1);
        cmbPdToMonth.setSelectedIndex(curPdMonth - 1);
        
        cmbFromMonth.addActionListener(lsn1);
        cmbToMonth.addActionListener(lsn2);
        cmbPdFromMonth.addActionListener(lsn3);
        cmbPdToMonth.addActionListener(lsn4);
    }
    
//    private void showChartPeriodically()
//    {
//        arlData.clear();
//        int fromPeriod = (int)spnFromPeriod.getValue();
//        int toPeriod = (int)spnToPeriod.getValue();
//        
//        getWalletIds();
//        
//        try {
//                while(fromPeriod <= toPeriod)
//                {
//                    Date startdate,fromdate = null;
//                    
//                       //getting the start date for a given period
//                        String sql = "Select dt from Pd where Num = ?";
//                        PreparedStatement pstmt = con.prepareStatement(sql);
//                        pstmt.setInt(1, fromPeriod);
//                        ResultSet rs;
//                        rs = pstmt.executeQuery();
//                        while(rs.next())
//                        {
//                            startdate = rs.getDate(1);
//                        }
//                        rs.close();
//                        pstmt.close();
//                        
//                        //getting the end date for a given date
//                        
//                        int nextPeriod = fromPeriod + 1;
//                        sql = "Select dt from Pd where Num = ?";
//                        pstmt = con.prepareStatement(sql);
//                        pstmt.setInt(1, nextPeriod);
//                        rs = pstmt.executeQuery();
//                        Date date = null;
//                        if(rs.next())
//                        {
//                            date = rs.getDate(1);
//                        }
//                        
//                        if(date != null)
//                        {
//                            toDate = subtractDate(date);
//                        }
//                        else
//                        {
//                            toDate = new GregorianCalendar().getTime();
//                            String dt = formatter.format(toDate);
//                            try {
//                                   toDate = formatter.parse(dt);
//                            } catch (ParseException ex) {
//                                Logger.getLogger(ComparativeIncomeExpenseChartForWallet.class.getName()).log(Level.SEVERE, null, ex);
//                            }
//                        }
//                        rs.close();
//                        pstmt.close();
//            String str = "";
//            if(radExpense.isSelected())
//                str = "e.amount < 0";
//            else
//                str = "e.amount > 0";
//                        
//            if(radWallet.isSelected())
//            {
//                sql = "Select w.name,sum(e.amount) from wallet w , Exttrans e where w.id = e.walletid and "+str+" and w.id in(";
//
//                String s = "";
//                for (Integer arlIndice : arlIds) {
//                    s+=arlIndice+",";
//                }
//                sql = sql + s.substring(0, s.length()-1);
//                sql+=") and e.Dt between ? and ? group by w.name";
//            }
//            else
//            {
//                sql = "Select p.name,sum(e.amount) from pp p , Exttrans e where p.id = e.ppId and "+str+" and p.id in(";
//
//                String s = "";
//                for (Integer arlIndice : arlIds) {
//                    s+=arlIndice+",";
//                }
//                sql = sql + s.substring(0, s.length()-1);
//                sql+=") and e.Dt between ? and ? group by p.name";
//                
//            }
//            java.sql.Date fdt = new java.sql.Date(fromDate.getTime());
//            java.sql.Date tdt = new java.sql.Date(toDate.getTime());
//            
//            pstmt = con.prepareStatement(sql);
//            pstmt.setDate(1, fdt);
//            pstmt.setDate(2, tdt);
//            rs = pstmt.executeQuery();
//            
//            while(rs.next())
//            {
//               String walletName = rs.getString(1);
//               float amt = rs.getFloat(2);
//               if(amt<0)
//                    amt = -amt;
//               BarChartData bcd = new BarChartData(fromPeriod, walletName, amt);
//               arlData.add(bcd);
//            }
//            fromPeriod++;
//            }
//            showBarChart();
//        } catch (SQLException ex) {
//            Logger.getLogger(ComparativeIncomeExpenseChartForWallet.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//    
//    
    private void showChartPeriodically()
    {
        try {
            getYears();
            
            arlData.clear();
            int fromDt = (int)spnPdFromDt.getValue();
            int fromMonth = cmbPdFromMonth.getSelectedIndex() + 1;
            int toMonth = cmbPdToMonth.getSelectedIndex() + 1;
            
            String sql = "";
            String str = "";
            
            int id = 0;
            if(radExpense.isSelected())
                str = "e.amount < 0";
            else
                str = "e.amount > 0";
            
            if(radWallet.isSelected())
            {
                String walName = (String)cmbWallet.getSelectedItem();
                for (Wallet arlWallet : arlWallets) {
                    if(arlWallet.getName().equals(walName)){
                        id = arlWallet.getID();
                        break;
                    }
                }
                 
                sql = "Select sum(e.amount),extract(day from e.Dt) as day,extract(month from e.Dt) as mnth,extract(year from dt) as year from Exttrans e where e.WalletId = ? and "+str;
                sql+=" and extract(day from e.Dt) >= ? and extract(month from e.Dt) >= ? and extract(month from e.Dt) <= ? and extract(year from e.Dt) in("  ;     
               
            }
            else
            {
                String walName = (String)cmbParty.getSelectedItem();
                for (Wallet arlParty : arlParty) {
                    if(arlParty.getName().equals(walName)){
                        id = arlParty.getID();
                        break;
                    }
                }
                 sql = "Select sum(e.amount),extract(day from e.Dt) as day,extract(month from e.Dt) as mnth ,extract(year from dt) as year from Exttrans e where e.PPId = ? and "+str;
                sql+=" and extract(day from e.Dt) >= ? and extract(month from e.Dt) >= ? and extract(month from e.Dt) <= ? and extract(year from e.Dt) in("  ;     
               
            }
                String s = "";
                for (Integer arlIndice : arlYears) {
                    s+=arlIndice+",";
                }
                sql = sql + s.substring(0, s.length()-1);
                sql+=") group by extract(day from e.Dt),extract(month from e.Dt),extract(year from dt) order by extract(day from e.Dt) asc, extract(month from e.Dt) asc,extract(year from dt) asc";
            
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.setInt(2, fromDt);
            pstmt.setInt(3, fromMonth);
            pstmt.setInt(4, toMonth);
            ResultSet rs;
            rs = pstmt.executeQuery();
            while(rs.next())
            {
                float amt = rs.getInt(1);
                int day = rs.getInt(2);
                int month = rs.getInt(3);
                int year = rs.getInt(4);
                String mon = arrShortMonNames[month - 1];
                String datMnth = day+"-"+mon;
                if(amt<0)
                    amt = -amt;
                BarChartData bd = new BarChartData(datMnth, ""+year, amt);
                arlData.add(bd);
            }
            rs.close();
            pstmt.close();
            showBarChart();
        } catch (SQLException ex) {
            Logger.getLogger(IncomeExpenseChartForWallet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void showChart()
    {
        getYears();
        if(arlYears.size() > 0)
        {
            if(radPeriodically.isSelected())
                 showChartPeriodically();
            else if(radMonthly.isSelected())
                showChartMonthly();
        }
        else
        {
                chartPanel.remove(cpanel);
                chartPanel.validate();
                chartPanel.repaint();
        }
    }
    private void getYears()
    {
        arlYears.clear();
        
            int[] indices = lstYear.getSelectedIndices();
            for (int indice : indices) {
                arlYears.add(yearModel.get(indice));
            }
    }
    private void showChartMonthly()
    {
        try {
            getYears();
            arlData.clear();
            int fromMonth = cmbFromMonth.getSelectedIndex() + 1;
            int toMonth = cmbToMonth.getSelectedIndex() + 1;
            
            String sql = "";
            String str = "";
            
            if(radExpense.isSelected())
                str = "e.amount < 0";
            else
                str = "e.amount > 0";
            
            int id = 0;
            
            if(radWallet.isSelected())
            {
                String walName = (String)cmbWallet.getSelectedItem();
                for (Wallet arlWallet : arlWallets) {
                    if(arlWallet.getName().equals(walName)){
                        id = arlWallet.getID();
                        break;
                    }
                }
                
                sql = "Select sum(e.amount),extract(year from e.Dt) as yr,extract(month from e.Dt) as mnth from Exttrans e where e.WalletId = ? and "+str;
                sql+=" and ((extract(month from e.Dt) >= ?) and extract(month from e.Dt) <= ?) and extract(year from e.Dt) in("  ;     
                
               
                String s = "";
                for (Integer arlIndice : arlYears) {
                    s+=arlIndice+",";
                }
                sql = sql + s.substring(0, s.length()-1);
                sql+=") group by extract(month from e.Dt),extract(year from e.Dt) order by extract(month from e.Dt) asc, extract(year from e.Dt) asc";
            }
          else
          {
                String walName = (String)cmbParty.getSelectedItem();
                for (Wallet arlParty : arlParty) {
                    if(arlParty.getName().equals(walName)){
                        id = arlParty.getID();
                        break;
                    }
                }
                 sql = "Select sum(e.amount),extract(year from e.Dt) as yr,extract(month from e.Dt) as mnth from Exttrans e where e.PPId = ? and "+str;
                sql+=" and ((extract(month from e.Dt) >= ?) and extract(month from e.Dt) <= ?) and extract(year from e.Dt) in("  ;     
                
               
                String s = "";
                for (Integer arlIndice : arlYears) {
                    s+=arlIndice+",";
                }
                sql = sql + s.substring(0, s.length()-1);
                sql+=") group by extract(month from e.Dt),extract(year from e.Dt) order by extract(month from e.Dt) asc ,extract(year from e.Dt) asc ";
                
            }
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.setInt(2, fromMonth);
            pstmt.setInt(3, toMonth);
            ResultSet rs;
            rs = pstmt.executeQuery();
            while(rs.next())
            {
                float amt = rs.getFloat(1);
                int yr = rs.getInt(2);
                int mnth = rs.getInt(3);
                if(amt<0)
                    amt = -amt;
                String mon = arrShortMonNames[mnth - 1];
                BarChartData bd = new BarChartData(""+yr, amt, mon);
                arlData.add(bd);
            }
            rs.close();
            pstmt.close();
            showBarChart();
        } catch (SQLException ex) {
            Logger.getLogger(IncomeExpenseChartForWallet.class.getName()).log(Level.SEVERE, null, ex);
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
        if(radPeriodically.isSelected())
        {
            for (BarChartData bd : arlData) {
                dataset.addValue(bd.getAmount(), bd.getYear(),bd.getDtMonth());
            }
            str = "Dates";
        }
        else if(radMonthly.isSelected())
        {
            for (BarChartData bd : arlData) {
                dataset.addValue(bd.getAmount(), bd.getYear(),bd.getMonth());
            }
            str = "Month";
        }
        
       JFreeChart barChart = ChartFactory.createStackedBarChart("Comparative Income/Expense Chart For Wallet", str, "Amount", dataset, PlotOrientation.VERTICAL, true, true, false);
        Plot barPlot = barChart.getPlot();
        
        if(dataset.getColumnCount() == 0)
        {
            barPlot.setNoDataMessage("No data is available");
        }
       
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
    
    private void getAllWalletsAndParty()
    {
        
        Statement stmt;
        try
        {
            //Data for wallet
            ActionListener lsn1 = cmbWallet.getActionListeners()[0] ;
            cmbWallet.removeActionListener(lsn1);
            stmt = con.createStatement();
            ResultSet rs;
            rs = stmt.executeQuery("Select Id,Name from Wallet");
            while(rs.next())
            {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                Wallet w = new Wallet(id, name);
                arlWallets.add(w);
                cmbWallet.addItem(name);
            }
            rs.close();
            cmbWallet.addActionListener(lsn1);
            
            //Data for Party
           ActionListener lsn2 = cmbParty.getActionListeners()[0] ;
           cmbParty.removeActionListener(lsn2);
           rs = stmt.executeQuery("Select Id,Name from PP");
            while(rs.next())
            {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                Wallet w = new Wallet(id, name);
                arlParty.add(w);
                cmbParty.addItem(name);
            }
        rs.close();
        stmt.close();
        cmbParty.addActionListener(lsn2);

        } catch (SQLException ex)
        {
            Logger.getLogger(PieChart.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            java.util.logging.Logger.getLogger(IncomeExpenseChartForWallet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(IncomeExpenseChartForWallet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(IncomeExpenseChartForWallet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(IncomeExpenseChartForWallet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
                f.add(new IncomeExpenseChartForWallet());
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
        jLabel9 = new javax.swing.JLabel();
        upperPanel = new javax.swing.JPanel();
        radIncome = new javax.swing.JRadioButton();
        radExpense = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        radPeriodically = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        cmbToMonth = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        radMonthly = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        cmbFromMonth = new javax.swing.JComboBox<>();
        cmbPdFromMonth = new javax.swing.JComboBox<>();
        spnPdFromDt = new javax.swing.JSpinner();
        cmbPdToMonth = new javax.swing.JComboBox<>();
        spnPdToDt = new javax.swing.JSpinner();
        chartPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        radWalParty = new javax.swing.JRadioButton();
        cmbWallet = new javax.swing.JComboBox<>();
        radWallet = new javax.swing.JRadioButton();
        cmbParty = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstYear = new javax.swing.JList<>();

        jLabel9.setText("jLabel9");

        radgrpIncmExp.add(radIncome);
        radIncome.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radIncome.setText("Income");
        radIncome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radIncomeActionPerformed(evt);
            }
        });

        radgrpIncmExp.add(radExpense);
        radExpense.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radExpense.setText("Expense");
        radExpense.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radExpenseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout upperPanelLayout = new javax.swing.GroupLayout(upperPanel);
        upperPanel.setLayout(upperPanelLayout);
        upperPanelLayout.setHorizontalGroup(
            upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upperPanelLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(radIncome, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(radExpense)
                .addContainerGap(55, Short.MAX_VALUE))
        );
        upperPanelLayout.setVerticalGroup(
            upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upperPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radIncome)
                    .addComponent(radExpense))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Interval", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 204))); // NOI18N

        radgrpPeriod.add(radPeriodically);
        radPeriodically.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radPeriodically.setText("Periodically");
        radPeriodically.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radPeriodicallyActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel1.setText("From");

        jLabel5.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel5.setText("To Month");

        cmbToMonth.setFont(new java.awt.Font("Garamond", 0, 12)); // NOI18N
        cmbToMonth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbToMonthActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel3.setText("To");

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

        cmbFromMonth.setFont(new java.awt.Font("Garamond", 0, 12)); // NOI18N
        cmbFromMonth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbFromMonthActionPerformed(evt);
            }
        });

        cmbPdFromMonth.setFont(new java.awt.Font("Garamond", 0, 12)); // NOI18N
        cmbPdFromMonth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbPdFromMonthActionPerformed(evt);
            }
        });

        spnPdFromDt.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnPdFromDtStateChanged(evt);
            }
        });

        cmbPdToMonth.setFont(new java.awt.Font("Garamond", 0, 12)); // NOI18N
        cmbPdToMonth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbPdToMonthActionPerformed(evt);
            }
        });

        spnPdToDt.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnPdToDtStateChanged(evt);
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
                        .addComponent(radMonthly)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(cmbToMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbFromMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(radPeriodically)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(cmbPdToMonth, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cmbPdFromMonth, 0, 98, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(spnPdFromDt, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                            .addComponent(spnPdToDt))))
                .addGap(0, 10, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radMonthly)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbFromMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbToMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radPeriodically)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbPdFromMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnPdFromDt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbPdToMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnPdToDt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        chartPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        chartPanel.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        chartPanel.setLayout(new java.awt.BorderLayout());

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/chart ana.png"))); // NOI18N
        jLabel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 153)));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        radgrpWalType.add(radWalParty);
        radWalParty.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radWalParty.setText("Party");
        radWalParty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radWalPartyActionPerformed(evt);
            }
        });

        cmbWallet.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        cmbWallet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbWalletActionPerformed(evt);
            }
        });

        radgrpWalType.add(radWallet);
        radWallet.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radWallet.setText("Wallet");
        radWallet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radWalletActionPerformed(evt);
            }
        });

        cmbParty.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        cmbParty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbPartyActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel10.setText("Year");

        lstYear.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jScrollPane1.setViewportView(lstYear);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(radWalParty, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cmbParty, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(radWallet)
                        .addGap(18, 18, 18)
                        .addComponent(cmbWallet, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(radWallet)
                            .addComponent(cmbWallet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(radWalParty)
                            .addComponent(cmbParty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(34, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chartPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 761, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addContainerGap(41, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(upperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(upperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chartPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 363, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel4)))
                .addGap(25, 25, 25))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void radPeriodicallyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radPeriodicallyActionPerformed
        // TODO add your handling code here:
        showChartPeriodically();
    }//GEN-LAST:event_radPeriodicallyActionPerformed

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

    private void radMonthlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radMonthlyActionPerformed
        // TODO add your handling code here:
//        showChartMonthly();
    }//GEN-LAST:event_radMonthlyActionPerformed

    private void radIncomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radIncomeActionPerformed
        // TODO add your handling code here:
        showChart();
            
    }//GEN-LAST:event_radIncomeActionPerformed

    private void radExpenseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radExpenseActionPerformed
        // TODO add your handling code here:
       showChart();

    }//GEN-LAST:event_radExpenseActionPerformed

    private void radWalPartyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radWalPartyActionPerformed
        // TODO add your handling code here:
               showChart();

    }//GEN-LAST:event_radWalPartyActionPerformed

    private void radWalletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radWalletActionPerformed
        // TODO add your handling code here:
       showChart();
    }//GEN-LAST:event_radWalletActionPerformed

    private void cmbWalletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbWalletActionPerformed
        // TODO add your handling code here:
        showChart();
    }//GEN-LAST:event_cmbWalletActionPerformed

    private void cmbPartyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPartyActionPerformed
        // TODO add your handling code here:
        showChart();
    }//GEN-LAST:event_cmbPartyActionPerformed

    private void cmbPdFromMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPdFromMonthActionPerformed
        // TODO add your handling code here:
        int index = cmbPdFromMonth.getSelectedIndex();
        snmDays1.setMaximum(new Integer(arrDays[index]));
        showChart();
    }//GEN-LAST:event_cmbPdFromMonthActionPerformed

    private void cmbPdToMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPdToMonthActionPerformed
        // TODO add your handling code here:
        int index = cmbPdToMonth.getSelectedIndex();
        snmDays2.setMaximum(new Integer(arrDays[index]));
        showChart();
    }//GEN-LAST:event_cmbPdToMonthActionPerformed

    private void spnPdFromDtStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnPdFromDtStateChanged
        // TODO add your handling code here:
        showChart();
    }//GEN-LAST:event_spnPdFromDtStateChanged

    private void spnPdToDtStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnPdToDtStateChanged
        // TODO add your handling code here:
        showChart();
    }//GEN-LAST:event_spnPdToDtStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel chartPanel;
    private javax.swing.JComboBox<String> cmbFromMonth;
    private javax.swing.JComboBox<String> cmbParty;
    private javax.swing.JComboBox<String> cmbPdFromMonth;
    private javax.swing.JComboBox<String> cmbPdToMonth;
    private javax.swing.JComboBox<String> cmbToMonth;
    private javax.swing.JComboBox<String> cmbWallet;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<Integer> lstYear;
    private javax.swing.JRadioButton radExpense;
    private javax.swing.JRadioButton radIncome;
    private javax.swing.JRadioButton radMonthly;
    private javax.swing.JRadioButton radPeriodically;
    private javax.swing.JRadioButton radWalParty;
    private javax.swing.JRadioButton radWallet;
    private javax.swing.ButtonGroup radgrpIncmExp;
    private javax.swing.ButtonGroup radgrpPeriod;
    private javax.swing.ButtonGroup radgrpWalType;
    private javax.swing.JSpinner spnPdFromDt;
    private javax.swing.JSpinner spnPdToDt;
    private javax.swing.JPanel upperPanel;
    // End of variables declaration//GEN-END:variables
}
