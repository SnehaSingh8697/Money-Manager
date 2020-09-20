/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.reports;

import domesticfinancesystem.MainFrame;
import static domesticfinancesystem.MainFrame.con;
import static domesticfinancesystem.MainFrame.dc;
import domesticfinancesystem.calendar.Database;
import domesticfinancesystem.calendar.DatePickerNewDialog;
import domesticfinancesystem.exttrans.WalletData;
import domesticfinancesystem.regularservice.RegularServiceMainPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author sneha
 */
public class WalletTransactionReport extends javax.swing.JPanel {

    /**
     * Creates new form WalletTransactionReport
     */
    private MyTableModel tableModel ;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private ArrayList<WalletData> arlWal = new ArrayList<>();
    private Database dc;
    private Connection con;
    private Date fromDate;
    private Date toDate;
    private int curLiqBal;
    private int curDigBal;

    private class MyTableModel extends AbstractTableModel
    {
        final int COLS = 6;
        String[] colNames = {"Date","Party/Wallet","Amount","New Liq Bal","New Dig Bal","Narration"};
        Class[] colTypes = {Date.class,String.class,String.class,Integer.class,Integer.class,String.class} ;
        ArrayList<TransactionRecs> arl = new ArrayList<TransactionRecs>();

        @Override
        public int getRowCount()
        {
            
            return arl.size();
        }

        @Override
        public int getColumnCount()
        {
            return COLS;
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            TransactionRecs tr = (TransactionRecs)arl.get(rowIndex);
            if(columnIndex == 0)
                 return tr.getDate();
            else if(columnIndex == 1)
                return tr.getPartyWallet();
            else if(columnIndex == 2)
                return tr.getStrAmount();
            else if(columnIndex == 3)
                return tr.getNewLiqBal();
            else if(columnIndex == 4)
                return tr.getNewDigBal();
            else 
                return tr.getNarration();
           
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            TransactionRecs tr = arl.get(rowIndex);
            if(columnIndex == 0)
            {
              tr.setDate((Date)aValue);
            }
            else if(columnIndex == 1)
               tr.setPartyWallet((String)aValue);
            else if(columnIndex == 2)
                tr.setStrAmount((String)aValue);
            else if(columnIndex == 3)
                tr.setNewLiqBal((int)aValue);
            else if(columnIndex == 4)
              tr.setNewDigBal((int)aValue);
            else if(columnIndex == 5)
                tr.setNarration((String)aValue);
           
            tableModel.fireTableCellUpdated(rowIndex, columnIndex);
        }
       

        @Override
        public String getColumnName(int column)
        {
           this.fireTableDataChanged();
           return colNames[column];
        }
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return colTypes[columnIndex] ;
        }
        
        public void addRow(TransactionRecs rsr)
        {
            arl.add(rsr);
        }
        
        public void setRowCount()
        {
            arl.clear();
            fireTableStructureChanged();
        }
    }
    
    class CellRenderer extends DefaultTableCellRenderer
    {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
           super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); //To change body of generated methods, choose Tools | Templates.

                setHorizontalAlignment(JLabel.CENTER);
            return this;
        }
    }
    
   
    class DateRenderer extends DefaultTableCellRenderer
    {

        @Override
        protected void setValue(Object value)
        {
            this.setText(formatter.format(value));
        }
        
    }
    private void showExtTrans(int id) throws SQLException
    {
//        curDigBal = 0;
//        curLiqBal = 0;
        
        tableModel.arl.clear();
        
        java.sql.Date sfdt = new java.sql.Date(fromDate.getTime());
        java.sql.Date stdt = new java.sql.Date(toDate.getTime());

        String sql = "Select e.Dt,p.Name,e.Amount,e.Narration,e.ModeNo from ExtTrans e,PP p where p.id = e.ppId and e.Dt between ? and ? and WalletId = ?";
        
         PreparedStatement pstmt = con.prepareStatement(sql);
         pstmt.setDate(1, sfdt);
         pstmt.setDate(2, stdt);
         pstmt.setInt(3, id);
         ResultSet rst = pstmt.executeQuery();
         
         String strAmount;
         boolean found = false;
         while(rst.next())
         {
             found = true;
             Date dt = rst.getDate(1);
             String partyName = rst.getString(2);
             int amount = rst.getInt(3);
             String narration = rst.getString(4);
             char mode = rst.getString(5).charAt(0);
             boolean isLiq;
             int newLiqBal = 0;
             int newDigBal = 0;
             
             if(mode == 'C')
             {
                 curLiqBal+=amount;
                 isLiq = true;
                 strAmount = ""+"(Liq)"+amount;
             }
             else
             {
                 curDigBal+=amount;
                 isLiq = false;
                 strAmount = ""+"(Dig)"+amount;
             }
            newLiqBal = curLiqBal;
            newDigBal = curDigBal;
            
            TransactionRecs tr = new TransactionRecs(dt, partyName, amount, strAmount, newLiqBal, newDigBal, narration, isLiq);
            tableModel.arl.add(tr);
            int row = tableModel.getRowCount() - 1;
            tableModel.fireTableRowsInserted(row, row);
            tabTransRecs.setRowSelectionInterval(row, row);
         }
          
         if(found)
            lblTransDetail.setText("External Transactions For "+cmbWallets.getSelectedItem()+" from "+formatter.format(fromDate)+" to "+formatter.format(toDate));

        
         lblFinalLiqBal.setText(""+curLiqBal);
         lblFinalDigitBal.setText(""+curDigBal);
         int totbal = curLiqBal + curDigBal;
         lblFinalTotal.setText(""+totbal);
    }
    
    private void showTransaction()
    {
        try {
            curLiqBal = 0;
            curDigBal = 0;
            Date dt = addsubtractDate(fromDate, 1, false);
            String walletName = (String)cmbWallets.getSelectedItem();
            int walletid = -1;
            for (WalletData walletData : arlWal) {
                if(walletData.getName().equals(walletName))
                {
                    walletid = walletData.getId();
                    break;
                }
            }
            int initialLiqBal = getWalletBal(walletid, walletName, true, dt);
            lblInitialLiqBal.setText(""+initialLiqBal);
            curLiqBal = initialLiqBal;
            int initialDigBal = getWalletBal(walletid, walletName, false, dt);
            lblInitialDigitBal.setText(""+initialDigBal);
            curDigBal = initialDigBal;
            int totBal = initialLiqBal + initialDigBal;
            lblInitialTotal.setText(""+totBal);
            
            if(radextTrans.isSelected())
            {
               lblTransDetail.setText("No External Transactions Available For "+walletName+" from "+formatter.format(fromDate)+" to "+formatter.format(toDate));
               showExtTrans(walletid);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(WalletTransactionReport.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    public void getWalletInfoForPayment()
    {
      try {
            cmbWallets.removeAllItems();
            arlWal.clear();
            ActionListener lsn = cmbWallets.getActionListeners()[0] ;
            cmbWallets.removeActionListener(lsn);

            String sql = "Select Id,Name from Wallet";
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            while(rst.next())
            {
                int id = rst.getInt(1);
                String name = rst.getString(2);
                WalletData wd = new WalletData(id, name);
                arlWal.add(wd);
                cmbWallets.addItem(name);
            }
            
            cmbWallets.addActionListener(lsn);
        } catch (SQLException ex) {
            Logger.getLogger(WalletTransactionReport.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }
    public WalletTransactionReport() {
        try {
            initComponents();
//            dc = MainFrame.dc;
//            con = dc.createConnection();
            dc = new Database("jdbc:oracle:thin:@localhost:1521:XE","dfs","dfsboss","oracle.jdbc.OracleDriver");
            con = dc.createConnection();
            tableModel = new MyTableModel();
            
            tabTransRecs.setModel(tableModel);
            tabTransRecs.setRowHeight(25);
            tabTransRecs.setShowGrid(true); //to show table border for each cell
            ((DefaultTableCellRenderer)tabTransRecs.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
            CellRenderer cr = new CellRenderer();
            tabTransRecs.setDefaultRenderer(String.class, cr);
            tabTransRecs.setDefaultRenderer(Integer.class, cr);
            tabTransRecs.setDefaultRenderer(Date.class, cr);
            
            tabTransRecs.setDefaultRenderer(Date.class, new DateRenderer());
            
            fromDate = new GregorianCalendar().getTime();
            String strDt = formatter.format(fromDate);
            fromDate = formatter.parse(strDt);
            System.out.println("frm dat = "+fromDate);
            toDate = fromDate;
            
            getWalletInfoForPayment();
            cmbWallets.setSelectedIndex(0);
            
            lblFromDate.setText(formatter.format(new GregorianCalendar().getTime()));
            lblToDate.setText(formatter.format(new GregorianCalendar().getTime()));
            
            
            radextTrans.setSelected(true);
            showTransaction();
            
            
            
        } catch (ParseException ex) {
            Logger.getLogger(WalletTransactionReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public Date addsubtractDate(Date dt, int days,boolean isAdd)
    {
        GregorianCalendar cal = new GregorianCalendar();
         
        System.out.println("dt = "+dt);
        cal.setTime(dt);
        
        if(isAdd)
            cal.add(Calendar.DATE, days);
        else
           cal.add(Calendar.DATE, -days);
        
        return cal.getTime();
    }
    private int getWalletBal(int walletId,String walletName,boolean isLiq,Date d1) throws SQLException
    {
        System.out.println("wallet id = "+walletId);
        int walletBalance = 0;
        int intTransNoOfSecs = 0;
        java.sql.Date tdt = new java.sql.Date(d1.getTime());
        String sql = null;
        
        String st = "";
        
        if(isLiq == true)
        {
            st = "and transliqamt >= 0 ";
        }
        else 
        {
            st = "and transdgtamt >= 0 ";
        }
        
        //Fetcing internal transaction
         sql = "Select * from (Select * from Inttrans where (sourcewalletid = ? or targetwalletid = ?) "+st+" and Dt <= ? order by Dt Desc, noofseconds desc) where rownum < = 1";

          PreparedStatement pstmt = con.prepareStatement(sql);
          pstmt.setInt(1, walletId);
          pstmt.setInt(2, walletId);
          pstmt.setDate(3, tdt);

          ResultSet rst = pstmt.executeQuery();

          int bal = 0;
          
          Date dt = new GregorianCalendar(1970, 1, 1).getTime();
          intTransNoOfSecs = 0;
          java.sql.Date sd1 = new java.sql.Date(d1.getTime());
          
          if(rst.next())
          {
              d1 = rst.getDate("Dt");
              sd1 = new java.sql.Date(d1.getTime());
              intTransNoOfSecs = rst.getInt("NOOFSECONDS");
              int sourcewalletid = rst.getInt("SourcewalletId");
              int oldWalletAmt;
              int transAmt;
              if(sourcewalletid == walletId)//if wallet is source wallet
              {
                  if(isLiq == true)
                  {
                      oldWalletAmt = rst.getInt("SOURCEWALLETOLDLIQAMT");
                      transAmt = rst.getInt("TRANSLIQAMT");
                      bal = oldWalletAmt - transAmt;
                  }
                  else 
                  {
                      oldWalletAmt = rst.getInt("SOURCEWALLETOLDDGTAMT");
                      transAmt = rst.getInt("TRANSDGTAMT");
                      bal = oldWalletAmt - transAmt;
                  }
              }
              else //if wallet is source wallet
              {
                  if(isLiq == true)
                  {
                      oldWalletAmt = rst.getInt("TARGETWALLETOLDLIQAMT");
                      transAmt = rst.getInt("TRANSLIQAMT");
                      bal = oldWalletAmt + transAmt;
                  }
                  else
                  {
                      oldWalletAmt = rst.getInt("TARGETWALLETOLDDGTAMT");
                      transAmt = rst.getInt("TRANSDGTAMT");
                      bal = oldWalletAmt + transAmt;
                  }
                 
              }
              rst.close();
              pstmt.close();
          }
          else// If no record found for that wallet in internal trans
          {
              bal = 0;
                  //If no record found in internal transaction for the wallet

                  sql = "Select max(p.Dt) from Pd p,PdDetails d where p.Id =  d.PdId and d.walletId = ? and p.Dt < ?";
                  pstmt = con.prepareStatement(sql);
                  pstmt.setInt(1, walletId);
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
                          pstmt.setInt(2, walletId);
                          rst = pstmt.executeQuery();

                          int liqBal = 0,digBal = 0;
                          if(rst.next())
                          {
                              digBal = rst.getInt(1);
                              liqBal = rst.getInt(2);
                          }
                          if(isLiq == true)
                          {
                              bal = liqBal;
                          }
                          else
                          {
                              bal = digBal;
                          }
                          rst.close();
                          pstmt.close();

                  }
                  else // Th wallet is cash or bank
                  {
                      sql = "Select Name from Wallet where Id = ?";
                      pstmt = con.prepareStatement(sql);
                      pstmt.setInt(1, walletId);
                      rst = pstmt.executeQuery();

                      String name = "";
                      if(rst.next())
                      {
                          name = rst.getString(1);
                      }

                      rst.close();
                      pstmt.close();

                     sql = "Select max(Dt) from Pd where Dt <= ? ";
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

                          int digBal = 0,liqBal = 0;

                          if(rst.next())
                          {
                              digBal = rst.getInt(1);
                              liqBal = rst.getInt(2);
                          }

                          rst.close();
                          pstmt.close();

                           if(name.equals("Bank"))
                           {
                               bal = digBal;
                               if(isLiq)
                                   bal = 0;
                           }
                           else if(name.equals("Cash"))
                           {
                               bal = liqBal;
                                if(isLiq == false)
                                   bal = 0;
                           }
                    }  

                  }
            }
            System.out.println("bal = "+bal);
          //Calculating external transaction
              String str = "";
              if(isLiq)
              {
                  str = "and modeno = \'C\' ";
              }
              else 
              {
                  str = "and modeno != \'C\' ";
              }

              sql = "Select sum(amount) from Exttrans e where walletid = ? "+str+"and (dt > ? or (dt = ? and noofseconds > ?)) and dt <= ?";
              pstmt = con.prepareStatement(sql);
              pstmt.setInt(1, walletId);
              pstmt.setDate(2,sd1 );
              pstmt.setDate(3, sd1);
              pstmt.setInt(4, intTransNoOfSecs);
              pstmt.setDate(5, tdt);

              rst = pstmt.executeQuery();


              int extAmt = 0;
              if(rst.next())
              {
                  extAmt = rst.getInt(1);
              }
              rst.close();
              
              System.out.println("extAmt = "+extAmt);

             int balance = 0;
             balance = bal + extAmt;
             
            System.out.println("balance = "+balance);

             
             return balance;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        radgrpTrans = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        cmbWallets = new javax.swing.JComboBox<>();
        radIntTrans = new javax.swing.JRadioButton();
        radextTrans = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabTransRecs = new javax.swing.JTable();
        lblTransDetail = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lblFromDate = new javax.swing.JLabel();
        btnFromDate = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        lblToDate = new javax.swing.JLabel();
        btnToDate = new javax.swing.JButton();
        lblWalletName = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblLiqBal1 = new javax.swing.JLabel();
        lblInitialLiqBal = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        lblInitialDigitBal = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        lblInitialTotal = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lblLiqBal2 = new javax.swing.JLabel();
        lblFinalLiqBal = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        lblFinalDigitBal = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        lblFinalTotal = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();

        setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N

        jLabel1.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel1.setText("Wallet");

        cmbWallets.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        cmbWallets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbWalletsActionPerformed(evt);
            }
        });

        radgrpTrans.add(radIntTrans);
        radIntTrans.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radIntTrans.setText("Internal Transaction");

        radgrpTrans.add(radextTrans);
        radextTrans.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radextTrans.setText("External Transaction");

        tabTransRecs.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        tabTransRecs.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tabTransRecs);

        lblTransDetail.setFont(new java.awt.Font("Garamond", 1, 14)); // NOI18N
        lblTransDetail.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTransDetail.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblTransDetail.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jLabel2.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel2.setText("From Date");

        lblFromDate.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        lblFromDate.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnFromDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/calendar.png"))); // NOI18N
        btnFromDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFromDateActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel4.setText("To Date");

        lblToDate.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        lblToDate.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnToDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/calendar.png"))); // NOI18N
        btnToDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnToDateActionPerformed(evt);
            }
        });

        lblWalletName.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        lblWalletName.setText("Wallet Name");

        jLabel3.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel3.setText("Initial Balance : ");

        lblLiqBal1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/liqbal.png"))); // NOI18N

        lblInitialLiqBal.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        lblInitialLiqBal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblInitialLiqBal.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/dig.jpg"))); // NOI18N

        lblInitialDigitBal.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        lblInitialDigitBal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblInitialDigitBal.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jLabel5.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel5.setText("Total");

        lblInitialTotal.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        lblInitialTotal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblInitialTotal.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jLabel6.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel6.setText("Final Balance : ");

        lblLiqBal2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/liqbal.png"))); // NOI18N

        lblFinalLiqBal.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        lblFinalLiqBal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblFinalLiqBal.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/dig.jpg"))); // NOI18N

        lblFinalDigitBal.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        lblFinalDigitBal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblFinalDigitBal.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jLabel9.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel9.setText("Total");

        lblFinalTotal.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        lblFinalTotal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblFinalTotal.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jLabel10.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel10.setText("Periodic Deposit");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 19, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1010, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(radIntTrans)
                                        .addGap(40, 40, 40)
                                        .addComponent(radextTrans))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(cmbWallets, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(31, 31, 31)
                                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblFromDate, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                                    .addComponent(lblToDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblTransDetail, javax.swing.GroupLayout.PREFERRED_SIZE, 650, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(btnToDate, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                        .addComponent(btnFromDate, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblWalletName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addComponent(lblLiqBal1, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblInitialLiqBal, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(34, 34, 34)
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblInitialDigitBal, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(155, 155, 155)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblInitialTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblLiqBal2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblFinalLiqBal, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblFinalDigitBal, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(175, 175, 175)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblFinalTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(70, 70, 70)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbWallets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radextTrans)
                    .addComponent(radIntTrans))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnFromDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnToDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblTransDetail, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(lblWalletName, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblInitialTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblInitialDigitBal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblInitialLiqBal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblLiqBal1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblFinalTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblFinalDigitBal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblFinalLiqBal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblLiqBal2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(34, 34, 34))
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
                 d = new GregorianCalendar().getTime();
            }
        DatePickerNewDialog dlg = new DatePickerNewDialog(null, true, d);
        dlg.setVisible(true);

        Date dt = dlg.getSelectedDate();
        if(dt!=null)
        {
            try {
                String s = formatter.format(dt);
                dt = formatter.parse(s);
                lblFromDate.setText(formatter.format(dt));
                fromDate = dt;
            } catch (ParseException ex) {
                Logger.getLogger(WalletTransactionReport.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        dlg.dispose();
        showTransaction();
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
                 d = new GregorianCalendar().getTime();
            }
        DatePickerNewDialog dlg = new DatePickerNewDialog(null, true, d);
        dlg.setVisible(true);

        Date dt = dlg.getSelectedDate();
        if(dt!=null)
        {
            try {
                String s = formatter.format(dt);
                dt = formatter.parse(s);
                lblToDate.setText(formatter.format(dt));
                toDate = dt;
            } catch (ParseException ex) {
                Logger.getLogger(WalletTransactionReport.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        dlg.dispose();
        showTransaction();

    }//GEN-LAST:event_btnToDateActionPerformed
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
            java.util.logging.Logger.getLogger(RegularServiceMainPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(RegularServiceMainPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(RegularServiceMainPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(RegularServiceMainPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                JFrame  f = new JFrame();
                
                f.setTitle("Wallet Transaction Report");
                f.add(new WalletTransactionReport());
                f.pack();
                f.setLocationRelativeTo(null);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
                
            }
        });
    }
    private void cmbWalletsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbWalletsActionPerformed
        // TODO add your handling code here:
       showTransaction();
    }//GEN-LAST:event_cmbWalletsActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFromDate;
    private javax.swing.JButton btnToDate;
    private javax.swing.JComboBox<String> cmbWallets;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JLabel lblFinalDigitBal;
    private javax.swing.JLabel lblFinalLiqBal;
    private javax.swing.JLabel lblFinalTotal;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblInitialDigitBal;
    private javax.swing.JLabel lblInitialLiqBal;
    private javax.swing.JLabel lblInitialTotal;
    private javax.swing.JLabel lblLiqBal1;
    private javax.swing.JLabel lblLiqBal2;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblTransDetail;
    private javax.swing.JLabel lblWalletName;
    private javax.swing.JRadioButton radIntTrans;
    private javax.swing.JRadioButton radextTrans;
    private javax.swing.ButtonGroup radgrpTrans;
    private javax.swing.JTable tabTransRecs;
    // End of variables declaration//GEN-END:variables
}
