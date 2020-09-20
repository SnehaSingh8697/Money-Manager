/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.chart.piechart;

import domesticfinancesystem.MainFrame;
import domesticfinancesystem.calendar.Database;
import domesticfinancesystem.chart.CheckboxListCellRenderer;
import domesticfinancesystem.calendar.DatePickerNewDialog;
import domesticfinancesystem.chart.Wallet;
import domesticfinancesystem.exttrans.ExternalTransactionPanel;
import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.general.DefaultPieDataset;

/**
 *
 * @author sneha
 */
public class PieChart extends javax.swing.JPanel {

    /**
     * Creates new form PieChart
     */
    private int maxPdNum;
    private Database dc;
    private Connection con;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private Date fromDate;
    private Date toDate;
    private DefaultListModel<Wallet> walletModel = new DefaultListModel<>();
    private boolean pdFound;
    private ArrayList<Integer> arlIds  = new ArrayList<>();
    private ArrayList<PieChartData> arlData  = new ArrayList<>();
    private ChartPanel cpanel;

    
    public PieChart() {
        initComponents();
        dc = MainFrame.dc;
        con = dc.createConnection();
        lstWallets.setModel(walletModel);
        cpanel = null;
        getNewPeriodicNum();
        setPdDates();
        getAllWallets();
        
                //  === Makes more than one check box in a list box checked at a time === //
        lstWallets.setSelectionModel(new DefaultListSelectionModel() 
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
        // ===================================================================== //
        lstWallets.setCellRenderer(new CheckboxListCellRenderer());
        
        chkZeroValues.setSelected(true);
        
        radIncome.setSelected(true);
        radLiqOnly.setSelected(true);
        
        for (int i = 0; i < walletModel.getSize(); i++){
           lstWallets.setSelectedIndex(i);
        }
          showChart();
    }
    
    private void showChart()
    {
        getWalletIds();
        if(arlIds.size() > 0)
        {
            fetchDatabase();
            showPieChart();
        }
        else
        {
            chartPanel.remove(cpanel);
            chartPanel.validate();
            chartPanel.repaint();
        }
    }
    
    private void getWalletIds()
    {
        arlIds.clear();
        int[] indices = lstWallets.getSelectedIndices();
        for (int indice : indices) {
            arlIds.add(walletModel.get(indice).getID());
        }
    }
    
    public Date subtractDate(Date dt, int days)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(dt);
        cal.add(Calendar.DATE, -days);
        return cal.getTime();
    }
    
    private boolean isToDateValid(Date tdt)
    {
        try {
            String sql = "Select min(dt) from Pd";
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            Date dt = null;
            if(rst.next())
            {
                dt = rst.getDate(1);
            }
            rst.close();
            stmt.close();
            if(tdt.compareTo(dt) < 0)
                return false;
            else
                return true ;
        } catch (SQLException ex) {
            Logger.getLogger(PieChart.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    private void fetchDatabaseForIncomeExpense()
    {
        try {
            arlData.clear();
            java.sql.Date fdt = new java.sql.Date(fromDate.getTime());
            java.sql.Date tdt = new java.sql.Date(toDate.getTime());
            String sql = "";
            
            if(radExpense.isSelected())
            {
                String st = "";
                if(radDigOnly.isSelected())
                {
                    st = "e.modeno != \'C\' and ";
                }
                else if(radLiqOnly.isSelected())
                {
                    st = "e.modeno = \'C\' and ";
                }
                sql = "Select w.name,sum(e.amount) from wallet w inner join Exttrans e on w.id = e.walletid where e.amount < 0 and "+st+"w.id in(";
                
                String s = "";
                for (Integer arlIndice : arlIds) {
                    s+=arlIndice+",";
                }
                sql = sql + s.substring(0, s.length()-1);
                sql+=") and e.Dt between ? and ? group by w.id,w.name";
            }
            else if(radIncome.isSelected())
            {
                String st = "";
                if(radDigOnly.isSelected())
                {
                    st = "e.modeno != \'C\' and ";
                }
                else if(radLiqOnly.isSelected())
                {
                    st = "e.modeno = \'C\' and ";
                }
                sql = "Select w.name,sum(e.amount) from wallet w inner join Exttrans e on w.id = e.walletid where e.amount > 0 and "+st+"w.id in(";
                
                String s = "";
                for (Integer arlIndice : arlIds) {
                    s+=arlIndice+",";
                }
                sql = sql + s.substring(0, s.length()-1);
                sql+=") and e.Dt between ? and ? group by w.id,w.name";
            }
            
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setDate(1, fdt);
            pstmt.setDate(2, tdt);
            ResultSet rs;
            rs = pstmt.executeQuery();
            while(rs.next())
            {
                String name = rs.getString(1);
                float amt = rs.getInt(2);
                if(amt<0)
                    amt = -amt;
                PieChartData pd = new PieChartData(name, amt);
                arlData.add(pd);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(PieChart.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }
    private void fetchDatabase()
    {
        if(radBalance.isSelected())
            fetchDatabaseForBalance();
        else
            fetchDatabaseForIncomeExpense();
    }
    private void fetchDatabaseForBalance()
    {
        arlData.clear();
        String sql = null;       
        
        try {
        
          if(isToDateValid(toDate))
          {
                java.sql.Date tdt = new java.sql.Date(toDate.getTime());

               String st = "";
                if(radLiqOnly.isSelected())
                {
                    st = "and transliqamt >= 0 ";
                }
                else if(radDigOnly.isSelected())
                {
                    st = "and transdgtamt >= 0 ";
                }

                for (Integer arlIndice : arlIds) {

                      Date d1 = new GregorianCalendar(1970, 1, 1).getTime();
                      int intTransNoOfSecs = 0;
                      java.sql.Date sd1 = new java.sql.Date(d1.getTime());
//                       sql = "Select * from (Select * from Inttrans where (sourcewalletid = ? or targetwalletid = ?) "+st+" and Dt <= ? order by Dt Desc, noofseconds desc "+s+" ) where rownum < = 1";
                      sql = "Select * from (Select * from Inttrans where (sourcewalletid = ? or targetwalletid = ?) "+st+" and Dt <= ? order by Dt Desc, noofseconds desc) where rownum < = 1";
                      PreparedStatement pstmt = con.prepareStatement(sql);
                      pstmt.setInt(1, arlIndice);
                      pstmt.setInt(2, arlIndice);
                      pstmt.setDate(3, tdt);

                      ResultSet rst = pstmt.executeQuery();

                      float bal = 0;
                      if(rst.next())
                      {
                          d1 = rst.getDate("Dt");
                          sd1 = new java.sql.Date(d1.getTime());
                          intTransNoOfSecs = rst.getInt("NOOFSECONDS");
                          int sourcewalletid = rst.getInt("SourcewalletId");
                          float oldWalletAmt;
                          float transAmt;
                          if(sourcewalletid == arlIndice)
                          {
                              if(radLiqOnly.isSelected())
                              {
                                  oldWalletAmt = rst.getFloat("SOURCEWALLETOLDLIQAMT");
                                  transAmt = rst.getFloat("TRANSLIQAMT");
                                  bal = oldWalletAmt - transAmt;
                              }
                              else if(radDigOnly.isSelected())
                              {
                                  oldWalletAmt = rst.getFloat("SOURCEWALLETOLDDGTAMT");
                                  transAmt = rst.getFloat("TRANSDGTAMT");
                                  bal = oldWalletAmt - transAmt;
                              }
                              else // if both selected
                              {
                                  float oldLiqWalletAmt = rst.getFloat("SOURCEWALLETOLDLIQAMT");
                                  transAmt = rst.getFloat("TRANSLIQAMT");
                                  float liqBal = oldLiqWalletAmt - transAmt;

                                  float oldDigWalletAmt = rst.getFloat("SOURCEWALLETOLDDGTAMT");
                                  transAmt = rst.getFloat("TRANSDGTAMT");
                                  float digBal = oldDigWalletAmt - transAmt;

                                  bal = liqBal + digBal;

                              }

                          }
                          else
                          {
                              if(radLiqOnly.isSelected())
                              {
                                  oldWalletAmt = rst.getFloat("TARGETWALLETOLDLIQAMT");
                                  transAmt = rst.getFloat("TRANSLIQAMT");
                                  bal = oldWalletAmt + transAmt;
                              }
                              else if(radDigOnly.isSelected())
                              {
                                  oldWalletAmt = rst.getFloat("TARGETWALLETOLDDGTAMT");
                                  transAmt = rst.getFloat("TRANSDGTAMT");
                                  bal = oldWalletAmt + transAmt;
                              }
                              else
                              {
                                  float oldLiqWalletAmt = rst.getFloat("TARGETWALLETOLDLIQAMT");
                                  transAmt = rst.getFloat("TRANSLIQAMT");
                                  float liqBal = oldLiqWalletAmt + transAmt;

                                  float oldDigWalletAmt = rst.getFloat("TARGETWALLETOLDDGTAMT");
                                  transAmt = rst.getFloat("TRANSDGTAMT");
                                  float digBal = oldDigWalletAmt + transAmt;

                                  bal = liqBal + digBal;
                              }
                          }
                          rst.close();
                          pstmt.close();
                      }
                      else //if record not found
                      {
                          bal = 0;
                          //If no record found in internal transaction for the wallet

                          sql = "Select max(p.Dt) from Pd p,PdDetails d where p.Id =  d.PdId and d.walletId = ? and p.Dt < ?";
                          pstmt = con.prepareStatement(sql);
                          pstmt.setInt(1, arlIndice);
                          pstmt.setDate(2, tdt);
                          rst = pstmt.executeQuery();

                          Date pdDt = null;

                          if(rst.next())
                          {
                              pdDt = rst.getDate(1);
                          }

                          rst.close();
                          pstmt.close();

                          if(pdDt!=null)//Other wallets other tha cash or bank
                          {
                                  java.sql.Date spdDt = new java.sql.Date(pdDt.getTime());

                                  sql = "Select WALLETDGTAMTNEW, WALLETLIQAMTNEW from Pd p, PdDetails d where p.Id = d.pdId and p.Dt = ? and d.walletId = ?";
                                  pstmt = con.prepareStatement(sql);
                                  pstmt.setDate(1, spdDt);
                                  pstmt.setInt(2, arlIndice);
                                  rst = pstmt.executeQuery();

                                  float liqBal = 0,digBal = 0;
                                  if(rst.next())
                                  {
                                      digBal = rst.getFloat(1);
                                      liqBal = rst.getFloat(2);
                                  }
                                  if(radLiqOnly.isSelected())
                                  {
                                      bal = liqBal;
                                  }
                                  else if(radDigOnly.isSelected())
                                  {
                                      bal = digBal;
                                  }
                                  else
                                  {
                                      bal = liqBal + digBal;
                                  }

                                  rst.close();
                                  pstmt.close();

                          }
                          else // Th wallet is cash or bank
                          {
                              sql = "Select Name from Wallet where Id = ?";
                              pstmt = con.prepareStatement(sql);
                              pstmt.setInt(1, arlIndice);
                              rst = pstmt.executeQuery();

                              String name = "";
                              if(rst.next())
                              {
                                  name = rst.getString(1);
                              }

                              rst.close();
                              pstmt.close();

                             sql = "Select max(Dt) from Pd where Dt < ? ";
                             pstmt = con.prepareStatement(sql);
                             pstmt.setDate(1, tdt);
                             rst = pstmt.executeQuery();

                             pdDt = null;
                             if(rst.next())
                             {
                                 pdDt = rst.getDate(1);
                             }
                             rst.close();
                             pstmt.close();

                             if(pdDt!=null)
                             {
                                  java.sql.Date spdDt = new java.sql.Date(pdDt.getTime());

                                  sql = "Select BankWalletNew,CashWalletNew from Pd where Dt = ? ";
                                  pstmt = con.prepareStatement(sql);
                                  pstmt.setDate(1, spdDt);
                                  rst = pstmt.executeQuery();

                                  float digBal = 0,liqBal = 0;

                                  if(rst.next())
                                  {
                                      digBal = rst.getFloat(1);
                                      liqBal = rst.getFloat(2);
                                  }

                                  rst.close();
                                  pstmt.close();

                                   if(name.equals("Bank"))
                                   {
                                       bal = digBal;
                                       if(radLiqOnly.isSelected())
                                           bal = 0;
                                   }
                                   else if(name.equals("Cash"))
                                   {
                                       bal = liqBal;
                                        if(radDigOnly.isSelected())
                                           bal = 0;
                                   }
                            }  

                          }
                      }

                     //Calculating external transaction
                     String str = "";
                      if(radLiqOnly.isSelected())
                      {
                          str = "and modeno = \'C\' ";
                      }
                      else if(radDigOnly.isSelected())
                      {
                          str = "and modeno != \'C\' ";
                      }

                      sql = "Select sum(amount) from Exttrans e where walletid = ? "+str+"and (dt > ? or (dt = ? and noofseconds > ?)) and dt <= ?";
                      pstmt = con.prepareStatement(sql);
                      pstmt.setInt(1, arlIndice);
                      pstmt.setDate(2, sd1);
                      pstmt.setDate(3, sd1);
                      pstmt.setInt(4, intTransNoOfSecs);
                      pstmt.setDate(5, tdt);

                      rst = pstmt.executeQuery();


                      float extAmt = 0;
                      if(rst.next())
                      {
                          extAmt = rst.getFloat(1);
                      }
                      rst.close();

                      //Fetching the wallet name
                      String wname = null;
                      for (int i = 0; i < walletModel.size(); i++) {
                          Wallet w = walletModel.get(i);
                          if(arlIndice == w.getID())
                          {
                              wname = w.getName();
                              break;
                          }
                    }
                      float balance = bal + extAmt;
//                      
//                      System.out.println("bal "+bal);
//                      System.out.println("extAmt "+extAmt);
//                      System.out.println("Tot balance "+balance);
//                      System.out.println("wallet name: "+wname);
                      
                      PieChartData pd = new PieChartData(wname, balance);
                      arlData.add(pd);

                  }
          } 
       
            } catch (SQLException ex) {
                Logger.getLogger(PieChart.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    private void showPieChart()
    {
        if(cpanel!= null)
        {
            chartPanel.remove(cpanel);
            chartPanel.repaint();
        }
        
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        for (PieChartData pd : arlData) {
        dataset.setValue(pd.getWalletName(),pd.getAmount());
        }
        
        String title = "";
        if(radBalance.isSelected())
            title = "Balance Pie Chart";
        else if(radIncome.isSelected())
            title = "Income Pie Chart";
        else
           title = "Expense Pie Chart"; 
        
        JFreeChart piechart = ChartFactory.createPieChart(title, dataset, true, true, false);
        

        PiePlot piePlot = (PiePlot)piechart.getPlot();
        
        if(dataset.getItemCount() == 0)
        {
            piePlot.setNoDataMessage("No data is available for "+title);
        }
        
        if(chkZeroValues.isSelected() == false)
            piePlot.setIgnoreZeroValues(true);
        
        StandardPieSectionLabelGenerator generator = new StandardPieSectionLabelGenerator("Wallet name - {0}\nValue - {1}\n Percentage - {2}"); ;
        piePlot.setLabelGenerator(generator);
        
        cpanel = new ChartPanel(piechart);
        cpanel.setVisible(true);
        chartPanel.add(cpanel,BorderLayout.CENTER);
        chartPanel.validate();
    }
    
    private void getAllWallets()
    {
        Statement stmt;
        try
        {
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
        stmt.close();

        } catch (SQLException ex)
        {
            Logger.getLogger(PieChart.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
    public void setPdDates()
    {
        try
        {
            if(pdFound == false)
            {
                try {
                    
                 String date = formatter.format(new GregorianCalendar().getTime());
                 fromDate = formatter.parse(date);
                 lblFromDate.setText(""+date);
                 
                 toDate = fromDate;
                 lblToDate.setText(""+date);
                 
                } catch (ParseException ex) {
                    Logger.getLogger(PieChart.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else
            {
                int pdNum = (int)spnPDNum.getValue();
                String sql = "Select Dt from Pd where Num = ?";
                PreparedStatement pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, pdNum);
                ResultSet rst = pstmt.executeQuery();
                if(rst.next())
                {
                    fromDate = rst.getDate(1);
                }
                rst.close();
                pstmt.close();

                lblFromDate.setText(""+formatter.format(fromDate));

                sql = "Select Dt from Pd where Num = ?";
                pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, pdNum + 1);
                rst = pstmt.executeQuery();
                if(rst.next())
                {
                    Date dt = rst.getDate(1);
                    toDate = subtractDate(dt, 1);
                }
                else
                    toDate = new GregorianCalendar().getTime();
                
                rst.close();
                pstmt.close();
                
                 String date = formatter.format(toDate);
                 toDate = formatter.parse(date);

                lblToDate.setText(""+formatter.format(toDate));
                
            }
        } catch (SQLException  | ParseException ex) {
            Logger.getLogger(ExternalTransactionPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void getNewPeriodicNum()
    {
        try {
            String sql = "Select Max(Num) from Pd";
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            if(rst.next())
            {
                maxPdNum = rst.getInt(1);
                System.out.println("Max pd num: "+maxPdNum);
                if(maxPdNum!=0)
                {
                    SpinnerNumberModel snm = new SpinnerNumberModel(maxPdNum, 1, maxPdNum, 1);
                    spnPDNum.setModel(snm);
                    spnPDNum.setValue(maxPdNum);
                    pdFound = true;
                }
                else
                {
                    spnPDNum.setEnabled(false);
                    pdFound = false;
                }
            }
            rst.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(PieChart.class.getName()).log(Level.SEVERE, null, ex);
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

        btngrpIncmExp = new javax.swing.ButtonGroup();
        btngrpAmtType = new javax.swing.ButtonGroup();
        upperPanel = new javax.swing.JPanel();
        radIncome = new javax.swing.JRadioButton();
        radExpense = new javax.swing.JRadioButton();
        radBalance = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        spnPDNum = new javax.swing.JSpinner();
        lblTitleFromDt = new javax.swing.JLabel();
        lblFromDate = new javax.swing.JLabel();
        btnFromDate = new javax.swing.JButton();
        lblTitleToDt = new javax.swing.JLabel();
        lblToDate = new javax.swing.JLabel();
        btnToDate = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        chkZeroValues = new javax.swing.JCheckBox();
        radLiqOnly = new javax.swing.JRadioButton();
        radDigOnly = new javax.swing.JRadioButton();
        radBoth = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstWallets = new javax.swing.JList<>();
        jPanel1 = new javax.swing.JPanel();
        lblPic = new javax.swing.JLabel();
        chartPanel = new javax.swing.JPanel();

        btngrpIncmExp.add(radIncome);
        radIncome.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radIncome.setText("Income");
        radIncome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radIncomeActionPerformed(evt);
            }
        });

        btngrpIncmExp.add(radExpense);
        radExpense.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radExpense.setText("Expense");
        radExpense.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radExpenseActionPerformed(evt);
            }
        });

        btngrpIncmExp.add(radBalance);
        radBalance.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radBalance.setText("Balance");
        radBalance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radBalanceActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel1.setText("Periodic Deposit No.");

        spnPDNum.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N
        spnPDNum.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnPDNumStateChanged(evt);
            }
        });

        lblTitleFromDt.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        lblTitleFromDt.setText("From Date");

        lblFromDate.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        lblFromDate.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnFromDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/calendar.png"))); // NOI18N
        btnFromDate.setToolTipText("Date Picker");
        btnFromDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFromDateActionPerformed(evt);
            }
        });

        lblTitleToDt.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        lblTitleToDt.setText("To Date");

        lblToDate.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        lblToDate.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnToDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/calendar.png"))); // NOI18N
        btnToDate.setToolTipText("Date Picker");
        btnToDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnToDateActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel3.setText("Wallets");

        chkZeroValues.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        chkZeroValues.setText("Allow zero values");
        chkZeroValues.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkZeroValuesActionPerformed(evt);
            }
        });

        btngrpAmtType.add(radLiqOnly);
        radLiqOnly.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radLiqOnly.setText("Liquid only");
        radLiqOnly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radLiqOnlyActionPerformed(evt);
            }
        });

        btngrpAmtType.add(radDigOnly);
        radDigOnly.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radDigOnly.setText("Digital only");
        radDigOnly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radDigOnlyActionPerformed(evt);
            }
        });

        btngrpAmtType.add(radBoth);
        radBoth.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radBoth.setText("Both");
        radBoth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radBothActionPerformed(evt);
            }
        });

        lstWallets.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jScrollPane1.setViewportView(lstWallets);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 756, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 310, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout upperPanelLayout = new javax.swing.GroupLayout(upperPanel);
        upperPanel.setLayout(upperPanelLayout);
        upperPanelLayout.setHorizontalGroup(
            upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upperPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(upperPanelLayout.createSequentialGroup()
                        .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(upperPanelLayout.createSequentialGroup()
                                .addComponent(radIncome)
                                .addGap(24, 24, 24)
                                .addComponent(radExpense)
                                .addGap(18, 18, 18)
                                .addComponent(radBalance))
                            .addGroup(upperPanelLayout.createSequentialGroup()
                                .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblTitleToDt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblTitleFromDt, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(upperPanelLayout.createSequentialGroup()
                                .addComponent(chkZeroValues, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(radLiqOnly)
                                .addGap(18, 18, 18)
                                .addComponent(radDigOnly)
                                .addGap(18, 18, 18)
                                .addComponent(radBoth)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(upperPanelLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(spnPDNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(154, 154, 154))
            .addGroup(upperPanelLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        upperPanelLayout.setVerticalGroup(
            upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upperPanelLayout.createSequentialGroup()
                .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(upperPanelLayout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spnPDNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(2, 2, 2)
                        .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(radIncome)
                            .addComponent(radExpense)
                            .addComponent(radBalance))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnFromDate)
                            .addGroup(upperPanelLayout.createSequentialGroup()
                                .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblTitleFromDt, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblTitleToDt, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnToDate))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(chkZeroValues)
                            .addComponent(radLiqOnly)
                            .addComponent(radDigOnly)
                            .addComponent(radBoth)))
                    .addGroup(upperPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        lblPic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/chart.png"))); // NOI18N

        chartPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        chartPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(upperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 634, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chartPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 737, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblPic, javax.swing.GroupLayout.PREFERRED_SIZE, 795, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(101, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(chartPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 432, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(upperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(lblPic, javax.swing.GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE))
                .addContainerGap())
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
                if(pdFound)
                    d = fromDate;
                else
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
                Logger.getLogger(ExternalTransactionPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        fromDate = dt;
        dlg.dispose();
        showChart();
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
                lblFromDate.setText("");
                if(pdFound)
                    d = fromDate;
                else
                  d = new GregorianCalendar().getTime();  
            }
        
        DatePickerNewDialog dlg;
        dlg = new DatePickerNewDialog(null, true, d);
        dlg.setVisible(true);
        Date dt = dlg.getSelectedDate();
        if(dt!=null)
        {
           lblToDate.setText(formatter.format(dt));
        }
        toDate = dt;
        dlg.dispose();
        showChart();
    }//GEN-LAST:event_btnToDateActionPerformed

    private void radBalanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radBalanceActionPerformed
        // TODO add your handling code here:
        lblTitleFromDt.setEnabled(false);
        lblFromDate.setEnabled(false);
        btnFromDate.setEnabled(false);
        lblTitleToDt.setText("On");
        showChart();

    }//GEN-LAST:event_radBalanceActionPerformed

    private void radIncomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radIncomeActionPerformed
        // TODO add your handling code here:
        lblTitleFromDt.setEnabled(true);
        lblFromDate.setEnabled(true);
        btnFromDate.setEnabled(true);
        lblTitleToDt.setText("To Date");
        showChart();

    }//GEN-LAST:event_radIncomeActionPerformed

    private void radExpenseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radExpenseActionPerformed
        // TODO add your handling code here:
        lblTitleFromDt.setEnabled(true);
        lblFromDate.setEnabled(true);
        btnFromDate.setEnabled(true);
        lblTitleToDt.setText("To Date");
        showChart();

    }//GEN-LAST:event_radExpenseActionPerformed

    private void radLiqOnlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radLiqOnlyActionPerformed
        // TODO add your handling code here:
        showChart();

    }//GEN-LAST:event_radLiqOnlyActionPerformed

    private void radDigOnlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radDigOnlyActionPerformed
        // TODO add your handling code here:
       showChart();

    }//GEN-LAST:event_radDigOnlyActionPerformed

    private void radBothActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radBothActionPerformed
        // TODO add your handling code here:
       showChart();

    }//GEN-LAST:event_radBothActionPerformed

    private void chkZeroValuesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkZeroValuesActionPerformed
        // TODO add your handling code here:
        showChart();
    }//GEN-LAST:event_chkZeroValuesActionPerformed

    private void spnPDNumStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnPDNumStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_spnPDNumStateChanged

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
            java.util.logging.Logger.getLogger(PieChart.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PieChart.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PieChart.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PieChart.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                JFrame  f = new JFrame();
                f.setTitle("Pie Chart");
                f.add(new PieChart());
                f.pack();
                f.setLocationRelativeTo(null);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFromDate;
    private javax.swing.JButton btnToDate;
    private javax.swing.ButtonGroup btngrpAmtType;
    private javax.swing.ButtonGroup btngrpIncmExp;
    private javax.swing.JPanel chartPanel;
    private javax.swing.JCheckBox chkZeroValues;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblPic;
    private javax.swing.JLabel lblTitleFromDt;
    private javax.swing.JLabel lblTitleToDt;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JList<Wallet> lstWallets;
    private javax.swing.JRadioButton radBalance;
    private javax.swing.JRadioButton radBoth;
    private javax.swing.JRadioButton radDigOnly;
    private javax.swing.JRadioButton radExpense;
    private javax.swing.JRadioButton radIncome;
    private javax.swing.JRadioButton radLiqOnly;
    private javax.swing.JSpinner spnPDNum;
    private javax.swing.JPanel upperPanel;
    // End of variables declaration//GEN-END:variables
}
