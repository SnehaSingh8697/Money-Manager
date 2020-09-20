/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.chart.compincmexpchart;

import domesticfinancesystem.MainFrame;
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
public class ComparativeIncomeExpenseChart extends javax.swing.JPanel {

    /**
     * Creates new form ComparativeIncomeExpenseChart
     */
    private Database dc;
    private Connection con;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat chaartDateFormatter = new SimpleDateFormat("dd-MM-yyyy");
    private DefaultListModel<Wallet> walletModel = new DefaultListModel<>();
    private DefaultListModel<Wallet> partyModel = new DefaultListModel<>();
    private Date fromDate;
    private Date toDate;
    private int maxPdNum;
    private ArrayList<Integer> arlIds = new ArrayList<>();
    private ArrayList<BarChartData> arlData = new ArrayList<>();
    private ChartPanel cpanel;
    private String[] arrShortMonNames;
    private int curPdMonth = 0;
    private int curPdYear = 0;
    private int barChartType;
    
    public ComparativeIncomeExpenseChart(int barChartType)
    {
        this();
        this.barChartType = barChartType;
        showChartDaily();

    }
    public ComparativeIncomeExpenseChart() {
        initComponents();
        
        dc = MainFrame.dc;
        con = dc.createConnection();
        
        cpanel = null;
        
        setPeriodicNum();//adding max pd numm
        
        getShortMonthNames();
        
        //adding models to list
        lstWallet.setModel(walletModel);
        lstParty.setModel(partyModel);
        
        getAllWalletsAndParty();
        
        radWallet.setSelected(true);
        radExpense.setSelected(true);
        
                //  === Makes more than one check box in a list box checked at a time === //
        lstWallet.setSelectionModel(new DefaultListSelectionModel() 
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
        
        lstParty.setSelectionModel(new DefaultListSelectionModel() 
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
        
        lstWallet.setCellRenderer(new CheckboxListCellRenderer());
        lstParty.setCellRenderer(new CheckboxListCellRenderer());
        
        for (int i = 0; i < walletModel.getSize(); i++) {
           lstWallet.setSelectedIndex(i);
        }
        
        for (int i = 0; i < partyModel.getSize(); i++) {
           lstParty.setSelectedIndex(i);
        }
        
        
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
            Logger.getLogger(ComparativeIncomeExpenseChart.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void getShortMonthNames()
    {
        DateFormatSymbols dfs = new DateFormatSymbols();
        arrShortMonNames = dfs.getShortMonths();
        
    }
    private void getWalletIds()
    {
        arlIds.clear();
        
        if(radWallet.isSelected())
        {
            int[] indices = lstWallet.getSelectedIndices();
            for (int indice : indices) {
                arlIds.add(walletModel.get(indice).getID());
            }
        }
        else
        {
             int[] indices = lstParty.getSelectedIndices();
            for (int indice : indices) {
                arlIds.add(partyModel.get(indice).getID());
            }           
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
            
            rs.close();
            pstmt.close();
            
            return d;
        } catch (SQLException ex) {
            Logger.getLogger(ComparativeIncomeExpenseChart.class.getName()).log(Level.SEVERE, null, ex);
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
        
        getWalletIds();
        
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
                        
                        //getting the end date for a given date
                        
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
                                Logger.getLogger(ComparativeIncomeExpenseChart.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        rs.close();
                        pstmt.close();
            String str = "";
            if(radExpense.isSelected())
                str = "e.amount < 0";
            else
                str = "e.amount > 0";
                        
            if(radWallet.isSelected())
            {
                sql = "Select w.name,sum(e.amount) from wallet w , Exttrans e where w.id = e.walletid and "+str+" and w.id in(";

                String s = "";
                for (Integer arlIndice : arlIds) {
                    s+=arlIndice+",";
                }
                sql = sql + s.substring(0, s.length()-1);
                sql+=") and e.Dt between ? and ? group by w.name";
            }
            else
            {
                sql = "Select p.name,sum(e.amount) from pp p , Exttrans e where p.id = e.ppId and "+str+" and p.id in(";

                String s = "";
                for (Integer arlIndice : arlIds) {
                    s+=arlIndice+",";
                }
                sql = sql + s.substring(0, s.length()-1);
                sql+=") and e.Dt between ? and ? group by p.name";
                
            }
            java.sql.Date fdt = new java.sql.Date(fromDate.getTime());
            java.sql.Date tdt = new java.sql.Date(toDate.getTime());
            
            pstmt = con.prepareStatement(sql);
            pstmt.setDate(1, fdt);
            pstmt.setDate(2, tdt);
            rs = pstmt.executeQuery();
            
            while(rs.next())
            {
               String walletName = rs.getString(1);
               float amt = rs.getFloat(2);
               if(amt<0)
                    amt = -amt;
               BarChartData bcd = new BarChartData(fromPeriod, walletName, amt);
               arlData.add(bcd);
            }
                rs.close();
                pstmt.close();
                fromPeriod++;
            }
            showBarChart();
        } catch (SQLException ex) {
            Logger.getLogger(ComparativeIncomeExpenseChart.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void showChartDaily()
    {
        try {
            getWalletIds();
            
            arlData.clear();
            java.sql.Date fdt = new java.sql.Date(fromDate.getTime());
            java.sql.Date tdt = new java.sql.Date(toDate.getTime());
            String sql = "";
            String str = "";
            
            if(radExpense.isSelected())
                str = "e.amount < 0";
            else
                str = "e.amount > 0";
            
            if(radWallet.isSelected())
            {
                sql = "Select e.dt,w.name,sum(e.amount) from wallet w , Exttrans e where w.id = e.walletid and "+str+" and w.id in(";

                String s = "";
                for (Integer arlIndice : arlIds) {
                    s+=arlIndice+",";
                }
                sql = sql + s.substring(0, s.length()-1);
                sql+=") and e.Dt between ? and ? group by e.dt,w.name order by e.dt asc";
            }
            else
            {
                sql = "Select e.dt,p.name,sum(e.amount) from pp p , Exttrans e where p.id = e.ppId and "+str+" and p.id in(";

                String s = "";
                for (Integer arlIndice : arlIds) {
                    s+=arlIndice+",";
                }
                sql = sql + s.substring(0, s.length()-1);
                sql+=") and e.Dt between ? and ? group by e.dt,p.name order by p.name asc";
                
            }
            
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setDate(1, fdt);
            pstmt.setDate(2, tdt);
            ResultSet rs;
            rs = pstmt.executeQuery();
            while(rs.next())
            {
                String dt = chaartDateFormatter.format(rs.getDate(1));
                String name = rs.getString(2);
                float amt = rs.getInt(3);
                if(amt<0)
                    amt = -amt;
                BarChartData bd = new BarChartData(dt, name, amt);
                arlData.add(bd);
            }
            rs.close();
            pstmt.close();
            showBarChart();
        } catch (SQLException ex) {
            Logger.getLogger(ComparativeIncomeExpenseChart.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void showChart()
    {
        getWalletIds();
        if(arlIds.size() > 0)
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
            getWalletIds();
            
            arlData.clear();
            
            int fromYear = (int)spnFrmYear.getValue();
            int toYear = (int)spnToYear.getValue();
            
            int fromMonth = cmbFromMonth.getSelectedIndex() + 1;
            int toMonth = cmbToMonth.getSelectedIndex() + 1;
            
            String sql = "";
            String str = "";
            
            if(radExpense.isSelected())
                str = "e.amount < 0";
            else
                str = "e.amount > 0";
            
            ResultSet rs;
            PreparedStatement pstmt;
            if(fromYear == toYear)
            {
                   if(radWallet.isSelected())
                    {
                        sql = "Select w.name, sum(e.amount),extract(year from e.Dt) as yr,extract(month from e.Dt) as mnth from Exttrans e,Wallet w where e.WalletId = w.Id and "+str;
                        sql+=" and extract(month from dt) >= ? and extract(month from dt)<=? and extract(year from dt) = ? and w.id in("  ;     

                        String s = "";
                        for (Integer arlIndice : arlIds) {
                            s+=arlIndice+",";
                        }
                        sql = sql + s.substring(0, s.length()-1);
                        sql+=") group by extract(year from e.Dt), extract(month from e.Dt) ,w.name order by w.name asc, extract(year from e.Dt) asc, extract(month from e.Dt) asc";
                    }
                    else
                    {
                        sql = "Select p.name,sum(e.amount),extract(year from e.Dt) as yr,extract(month from e.Dt) as mnth from Exttrans e,PP p where e.PPId = pp.Id and "+str;
                        sql+=" and  extract(month from dt) >= ? and extract(month from dt)<=? and extract(year from dt) = ? and p.id in("  ;

                        String s = "";
                        for (Integer arlIndice : arlIds) {
                            s+=arlIndice+",";
                        }
                        sql = sql + s.substring(0, s.length()-1);
                        sql+=") group by extract(year from e.Dt),extract(month from e.Dt),p.name order by p.name asc, extract(year from e.Dt) asc, extract(month from e.Dt) asc";
                    }
                   
                    pstmt = con.prepareStatement(sql);
                    pstmt.setInt(1, fromMonth);
                    pstmt.setInt(2, toMonth);
                    pstmt.setInt(3, fromYear);
                    
                    rs = pstmt.executeQuery();
            }
            else
            {
                    if(radWallet.isSelected())
                    {
                        sql = "Select w.name, sum(e.amount),extract(year from e.Dt) as yr,extract(month from e.Dt) as mnth from Exttrans e,Wallet w where e.WalletId = w.Id and "+str;
                        sql+=" and ((extract(year from e.Dt) = ? and extract(month from e.Dt) >= ?) or (extract(year from e.Dt) = ? and extract(month from e.Dt) <= ?) or (extract(year from e.Dt) between ? and ?)) and w.id in("  ;     

                        String s = "";
                        for (Integer arlIndice : arlIds) {
                            s+=arlIndice+",";
                        }
                        sql = sql + s.substring(0, s.length()-1);
                        sql+=") group by extract(year from e.Dt), extract(month from e.Dt) ,w.name order by w.name asc, extract(year from e.Dt) asc, extract(month from e.Dt) asc";
                    }
                    else
                    {
                        sql = "Select p.name,sum(e.amount),extract(year from e.Dt) as yr,extract(month from e.Dt) as mnth from Exttrans e,PP p where e.PPId = pp.Id and "+str;
                        sql+=" and ((extract(year from e.Dt) = ? and extract(month from e.Dt) >= ?) or (extract(year from e.Dt) = ? and extract(month from e.Dt) <= ?) or (extract(year from e.Dt) between ? and ?)) and p.id in("  ;

                        String s = "";
                        for (Integer arlIndice : arlIds) {
                            s+=arlIndice+",";
                        }
                        sql = sql + s.substring(0, s.length()-1);
                        sql+=") group by extract(year from e.Dt),extract(month from e.Dt),p.name order by p.name asc, extract(year from e.Dt) asc, extract(month from e.Dt) asc";
                    }

                    pstmt = con.prepareStatement(sql);
                    pstmt.setInt(1, fromYear);
                    pstmt.setInt(2, fromMonth);
                    pstmt.setInt(3, toYear);
                    pstmt.setInt(4, toMonth);
                    pstmt.setInt(5, fromYear + 1);
                    pstmt.setInt(6, toYear - 1);

                    rs = pstmt.executeQuery();
            }

                    while(rs.next())
                    {
                        String name = rs.getString(1);
                        float amt = rs.getFloat(2);
                        int yr = rs.getInt(3);
                        int mnth = rs.getInt(4);
                        if(amt<0)
                            amt = -amt;
                        String mon = arrShortMonNames[mnth - 1];
                        String mnyr = mon+" "+yr;
                        BarChartData bd = new BarChartData(name, amt, mnyr);
                        arlData.add(bd);
                    }
                    rs.close();
                    pstmt.close();
                            
            showBarChart();
        } catch (SQLException ex) {
            Logger.getLogger(ComparativeIncomeExpenseChart.class.getName()).log(Level.SEVERE, null, ex);
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
                dataset.addValue(bd.getAmount(), bd.getWalletName(),bd.getDate());
            }
            str = "Dates";
        }
        else if(radMonthly.isSelected())
        {
            for (BarChartData bd : arlData) {
                dataset.addValue(bd.getAmount(), bd.getWalletName(),bd.getMnyr());
            }
            str = "Month-Year";
        }
        else
        {
            for (BarChartData bd : arlData) {
                dataset.addValue(bd.getAmount(), bd.getWalletName(),""+bd.getPdNum());
            }
            str = "Periodic Deposit";
        }
        JFreeChart barChart;
       
        System.out.println("barchart type "+barChartType);
        if(barChartType == 1)//create bar chart
              barChart = ChartFactory.createBarChart("Comparative Income/Expense Chart", str, "Amount", dataset, PlotOrientation.VERTICAL, true, true, false);
        else
            barChart = ChartFactory.createStackedBarChart("Income/Expense Report", str, "Amount", dataset, PlotOrientation.VERTICAL, true, true, false);
       
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
    
    private void getAllWalletsAndParty()
    {
        
        Statement stmt;
        try
        {
            //Data for wallet
            stmt = con.createStatement();
            ResultSet rs;
            rs = stmt.executeQuery("Select Id,Name from Wallet");
            while(rs.next())
            {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                Wallet w = new Wallet(id, name);
                walletModel.addElement(w);
            }
            rs.close();
            
            //Data for Party
        
           rs = stmt.executeQuery("Select Id,Name from PP");
            while(rs.next())
            {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                Wallet w = new Wallet(id, name);
                partyModel.addElement(w);
            }
        rs.close();
        stmt.close();
        
        

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
            java.util.logging.Logger.getLogger(ComparativeIncomeExpenseChart.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ComparativeIncomeExpenseChart.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ComparativeIncomeExpenseChart.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ComparativeIncomeExpenseChart.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                JFrame  f = new JFrame();
                f.setTitle("Bar Chart");
                f.add(new ComparativeIncomeExpenseChart(2));//showing stack bar chart for 2 , showing bar chart for 1
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
        upperPanel = new javax.swing.JPanel();
        radIncome = new javax.swing.JRadioButton();
        radExpense = new javax.swing.JRadioButton();
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
        radWallet = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstWallet = new javax.swing.JList<>();
        jRadioButton3 = new javax.swing.JRadioButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstParty = new javax.swing.JList<>();
        chartPanel = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();

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
                .addContainerGap()
                .addComponent(radIncome, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(radExpense)
                .addContainerGap(37, Short.MAX_VALUE))
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

        lblFromDate.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N
        lblFromDate.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        cmbToMonth.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        cmbToMonth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbToMonthActionPerformed(evt);
            }
        });

        btnFromDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/calendar.png"))); // NOI18N
        btnFromDate.setToolTipText("Date Picker");
        btnFromDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFromDateActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel6.setText("Year");

        jLabel3.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel3.setText("To");

        lblToDate.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
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
        btnToDate.setToolTipText("Date Picker");
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

        spnFrmYear.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N
        spnFrmYear.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnFrmYearStateChanged(evt);
            }
        });

        spnToYear.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N
        spnToYear.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnToYearStateChanged(evt);
            }
        });

        spnFromPeriod.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N
        spnFromPeriod.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnFromPeriodStateChanged(evt);
            }
        });

        spnToPeriod.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N
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
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(radDaily)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        radgrpWalType.add(radWallet);
        radWallet.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radWallet.setText("Wallet");
        radWallet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radWalletActionPerformed(evt);
            }
        });

        lstWallet.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jScrollPane1.setViewportView(lstWallet);

        radgrpWalType.add(jRadioButton3);
        jRadioButton3.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jRadioButton3.setText("Party");
        jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton3ActionPerformed(evt);
            }
        });

        lstParty.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jScrollPane2.setViewportView(lstParty);

        chartPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        chartPanel.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N
        chartPanel.setLayout(new java.awt.BorderLayout());

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/chart ana.png"))); // NOI18N
        jLabel9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 153)));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(radWallet)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jRadioButton3)))
                            .addComponent(chartPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 761, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel9))
                    .addComponent(upperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(88, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(upperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(radWallet)
                                    .addComponent(jRadioButton3))
                                .addGap(2, 2, 2)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                                    .addComponent(jScrollPane2)))
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(57, 57, 57))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 547, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void radIncomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radIncomeActionPerformed
        // TODO add your handling code here:
        showChart();
            
    }//GEN-LAST:event_radIncomeActionPerformed

    private void radExpenseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radExpenseActionPerformed
        // TODO add your handling code here:
       showChart();

    }//GEN-LAST:event_radExpenseActionPerformed

    private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton3ActionPerformed
        // TODO add your handling code here:
               showChart();

    }//GEN-LAST:event_jRadioButton3ActionPerformed

    private void radWalletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radWalletActionPerformed
        // TODO add your handling code here:
       showChart();
    }//GEN-LAST:event_radWalletActionPerformed

    private void spnToPeriodStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnToPeriodStateChanged
        // TODO add your handling code here:
        showChart();
    }//GEN-LAST:event_spnToPeriodStateChanged

    private void spnFromPeriodStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnFromPeriodStateChanged
        // TODO add your handling code here:
        showChart();
    }//GEN-LAST:event_spnFromPeriodStateChanged

    private void spnToYearStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnToYearStateChanged
        // TODO add your handling code here:
        showChartMonthly();
    }//GEN-LAST:event_spnToYearStateChanged

    private void spnFrmYearStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnFrmYearStateChanged
        // TODO add your handling code here:
        showChartMonthly();
    }//GEN-LAST:event_spnFrmYearStateChanged

    private void cmbFromMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbFromMonthActionPerformed
        // TODO add your handling code here:
        if(cmbFromMonth.getSelectedIndex()>=0)
        {
            showChart();

        }
    }//GEN-LAST:event_cmbFromMonthActionPerformed

    private void radMonthlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radMonthlyActionPerformed
        // TODO add your handling code here:
        showChartMonthly();
    }//GEN-LAST:event_radMonthlyActionPerformed

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
                Logger.getLogger(ComparativeIncomeExpenseChart.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        toDate = dt;
        dlg.dispose();
        showChartDaily();
    }//GEN-LAST:event_btnToDateActionPerformed

    private void radPeriodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radPeriodActionPerformed
        // TODO add your handling code here:
        showChart();
    }//GEN-LAST:event_radPeriodActionPerformed

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
                Logger.getLogger(ComparativeIncomeExpenseChart.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        fromDate = dt;
        dlg.dispose();

        showChartDaily();
    }//GEN-LAST:event_btnFromDateActionPerformed

    private void cmbToMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbToMonthActionPerformed
        // TODO add your handling code here:
        if(cmbFromMonth.getSelectedIndex()>=0)
        {
            showChart();

        }
    }//GEN-LAST:event_cmbToMonthActionPerformed

    private void radDailyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radDailyActionPerformed
        // TODO add your handling code here:
        showChartDaily();
    }//GEN-LAST:event_radDailyActionPerformed


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
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JList<Wallet> lstParty;
    private javax.swing.JList<Wallet> lstWallet;
    private javax.swing.JRadioButton radDaily;
    private javax.swing.JRadioButton radExpense;
    private javax.swing.JRadioButton radIncome;
    private javax.swing.JRadioButton radMonthly;
    private javax.swing.JRadioButton radPeriod;
    private javax.swing.JRadioButton radWallet;
    private javax.swing.ButtonGroup radgrpIncmExp;
    private javax.swing.ButtonGroup radgrpPeriod;
    private javax.swing.ButtonGroup radgrpWalType;
    private javax.swing.JSpinner spnFrmYear;
    private javax.swing.JSpinner spnFromPeriod;
    private javax.swing.JSpinner spnToPeriod;
    private javax.swing.JSpinner spnToYear;
    private javax.swing.JPanel upperPanel;
    // End of variables declaration//GEN-END:variables
}
