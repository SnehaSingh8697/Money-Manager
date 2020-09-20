/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.periodicdeposit;

import domesticfinancesystem.MainFrame;
import domesticfinancesystem.calendar.Database;
import domesticfinancesystem.exttrans.ExternalTransactionPanel;
import domesticfinancesystem.exttrans.WalletData;
import domesticfinancesystem.inttrans.IntTransData;
import domesticfinancesystem.inttrans.WalletInfo;
import java.awt.Image;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author sneha
 */
public class PeriodicDeposit extends javax.swing.JDialog {

    /**
     * Creates new form PeriodicDeposit
     */
    private Database dc;
    private Connection con;
    private Image cashImg;
    private Image digImg;
    private int newPdNum;
    private int maxPdNum;
    private boolean isFound;
    private MyTableModel tableModel;
    private ArrayList<WalletDetails> arlWalInfo = new ArrayList<>();
    private int bankWallletOld;
    private int	otherDigWalletOld;
    private int	cashWalletOld;
    private int	otherCashWalletOld;
    private int	bankWalletNew;
    private int otherDigWalletNew;
    private int cashWalletNew;
    private int otherCashWalletNew;
    private int totalDigitalBal;
    private int totalLiquidBal;
    private final int  size = 18;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private SpinnerNumberModel snmAmt;
   
    private boolean isAdd;
    private static boolean clickedOnAdd;
    
    private class MyTableModel extends AbstractTableModel
    {
        final int COLS = 5;
        String[] colNames = {"Wallet","Old Liquid Bal","New Liquid Bal","Old Digital Bal","New Digital Bal"};
        Class[] colTypes = {String.class,Integer.class,Integer.class,Integer.class,Integer.class} ;
        ArrayList<WalletDetails> arl = new ArrayList<WalletDetails>();

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
            WalletDetails wd = (WalletDetails)arl.get(rowIndex);
            if(columnIndex == 0)
                return wd.getName();
            else if(columnIndex == 1)
                return wd.getWalletLiqAmtOld();
            else if(columnIndex == 2)
                return wd.getWalletLiqAmtNew();
            else if(columnIndex == 3)
                return wd.getWalletDgtAmtOld();
            else 
                return wd.getWalletDgtAmtNew();
        }
        
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            WalletDetails wd = arl.get(rowIndex);
            if(columnIndex == 0)
                wd.setName((String)aValue);
            else if(columnIndex == 2)
                wd.setWalletLiqAmtNew((int)aValue);
            else if(columnIndex == 4)
                wd.setWalletDgtAmtNew((int)aValue);
            else 
            {
                
            }
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
    }
    
    public boolean checkPdPresent()
    {
        try {
            Date date = todayDate();
            java.sql.Date sdt = new java.sql.Date(date.getTime());
            System.out.println(sdt.toString());
            String sql = "Select * from Pd where Dt = ?";
            PreparedStatement pstmt =  con.prepareStatement(sql);
            pstmt.setDate(1,sdt);
            ResultSet rst = pstmt.executeQuery();
            if(rst.next())
            {
                return true;
            }
            return false;
            
        } catch (SQLException ex) {
            Logger.getLogger(PeriodicDeposit.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    public Date todayDate()
     {
        Date date = null;
        try {
            String d = formatter.format(Calendar.getInstance().getTime());
            date = formatter.parse(d);
        } catch (ParseException ex) {
            Logger.getLogger(ExternalTransactionPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return date;
     }
    
    public void getPdDetails()
    {
        try {
            int pdNum = (int)spnPdNum.getValue();
            if(pdNum>0 && isAdd == false)
            {
                enabledDisabled(false);
                String sql = "Select * from Pd where Num = ?";
                PreparedStatement pstmt = con.prepareStatement(sql);
                pstmt.setInt(1,pdNum);
                ResultSet rst = pstmt.executeQuery();
                int pdId = 0;
                boolean found = false;
                while(rst.next())
                {
                    lblDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(rst.getDate("Dt")));
                    bankWallletOld = rst.getInt("BankWallletOld");
                    otherDigWalletOld = rst.getInt("OtherDigWalletOld");
                    cashWalletOld = rst.getInt("CashWalletOld");
                    otherCashWalletOld = rst.getInt("otherCashWalletOld");
                    
                    //before distribution
                    int totdigbal = bankWallletOld + otherDigWalletOld;
                    int totliqbal = cashWalletOld + otherCashWalletOld;
                    
                    lblDigBalTot.setText(""+totdigbal);
                    lblOldBankDigAmt.setText(""+bankWallletOld);
                    lblOldOtherDigAmt.setText(""+otherDigWalletOld);
                    
                    lblLiqBalTot.setText(""+totliqbal);
                    lblOldCashLiqAmt.setText(""+cashWalletOld);
                    lblOldOtherLiqAmt.setText(""+otherCashWalletOld);
                    
                    //after distribution
                    bankWalletNew  = rst.getInt("BankWalletNew");
                    otherDigWalletNew  = rst.getInt("OtherDigWalletNew");
                    cashWalletNew = rst.getInt("CashWalletNew");
                    otherCashWalletNew  = rst.getInt("OtherCashWalletNew");
                    
                    int totdigbalNew = bankWalletNew + otherDigWalletNew;
                    int totliqbalNew = cashWalletNew + otherCashWalletNew;
                    
                    lblDigBalTotNew.setText(""+totdigbalNew);
                    lblNewBankDigAmt.setText(""+bankWalletNew);
                    lblNewOtherDigAmt.setText(""+otherDigWalletNew);
                    
                    lblLiqBalTotNew.setText(""+totliqbalNew);
                    lblNewCashLiqAmt.setText(""+cashWalletNew);
                    lblNewOtherLiqAmt.setText(""+otherCashWalletNew);
                    
                    pdId = rst.getInt("Id");
                    found = true;
                }
                rst.close();
                pstmt.close();

                sql = "Select * from PdDetails where PdId = ?";
                pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, pdId);
                rst = pstmt.executeQuery();


                ArrayList<WalletDetails> arlWd = new ArrayList<>();

                PreparedStatement ps;

                while(rst.next())
                {
                   int walletId = rst.getInt("WalletId");
                   int walletDgtAmtOld = rst.getInt("WalletDgtAmtOld");
                   int walletDgtAmtNew= rst.getInt("WalletDgtAmtNew");
                   int walletLiqAmtOld = rst.getInt("WalletLiqAmtOld");
                   int walletLiqAmtNew = rst.getInt("WalletLiqAmtNew");

                   String s = "Select Name from Wallet where id = ?";
                   ps = con.prepareStatement(s);
                   ps.setInt(1, walletId);
                   ResultSet rs = ps.executeQuery();
                   String walletName = "";
                   if(rs.next())
                   {
                      walletName = rs.getString("Name");
                   }
                   WalletDetails wd = new WalletDetails(walletName, walletId, walletDgtAmtOld,walletLiqAmtOld,walletDgtAmtNew, walletLiqAmtNew);
                   arlWd.add(wd);

                }
                rst.close();
                pstmt.close();
                tableModel.arl = arlWd;
                tableModel.fireTableDataChanged();
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(PeriodicDeposit.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    public PeriodicDeposit(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        try {
            initComponents();
            setLocationRelativeTo(null);
            dc = MainFrame.dc;
            con = dc.createConnection();
            
            cashImg = ImageIO.read(getClass().getResource("/testimages/liqbal.png"));
            digImg =  ImageIO.read(getClass().getResource("/testimages/dig.jpg"));
            
            
            snmAmt = new SpinnerNumberModel(0, 0, 0, 100);
            spnAmount.setModel(snmAmt);
            spnAmount.setValue(0);
            
            
            clickedOnAdd = false;
          

            isAdd = false;


        tableModel = new MyTableModel();
        tabWalInfo.setModel(tableModel);
        getNewPeriodicNum();

        
        if(maxPdNum == 0)
        {
            radAdd.setSelected(true);
            radShow.setEnabled(false);
            enabledDisabled(true);
            addRecord();
            
        }
        else
        {
            SpinnerNumberModel snm = new SpinnerNumberModel(maxPdNum, 1, maxPdNum, 1);
            spnPdNum.setModel(snm);
            spnPdNum.setValue(maxPdNum);
            radShow.setSelected(true);
            getPdDetails();
            enabledDisabled(false);
            if(checkPdPresent())
            {
                radAdd.setEnabled(false);
            }
        }
        tabWalInfo.setRowHeight(25);

        tabWalInfo.setShowGrid(true); //to show table border for each cell

        ((DefaultTableCellRenderer)tabWalInfo.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

    } catch (IOException ex) {
            Logger.getLogger(PeriodicDeposit.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void getOldAmt()
    {
        try {
            otherCashWalletOld = 0;
            String sql = "Select Liquidbal from Wallet where name not like 'Cash'";
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            while(rst.next())
            {
              otherCashWalletOld += rst.getInt(1);
            }
            rst.close();
            stmt.close();
            
            cashWalletOld = 0;
            sql = "Select Liquidbal from Wallet where name like 'Cash'";
            stmt = con.createStatement();
            rst = stmt.executeQuery(sql);
            if(rst.next())
            {
               cashWalletOld = rst.getInt(1);
            }
            rst.close();
            stmt.close();
            
            totalLiquidBal =  cashWalletOld + otherCashWalletOld;
            
            otherDigWalletOld = 0;
            sql = "Select Digitalbal from Wallet where name not like 'Bank'";
            stmt = con.createStatement();
            rst = stmt.executeQuery(sql);
            while(rst.next())
            {
               otherDigWalletOld+= rst.getInt(1);
            }
            rst.close();
            stmt.close();
            
            bankWallletOld = 0;
            sql = "Select Digitalbal from Wallet where name like 'Bank'";
            stmt = con.createStatement();
            rst = stmt.executeQuery(sql);
            if(rst.next())
            {
               bankWallletOld = rst.getInt(1);
            }
            rst.close();
            stmt.close();
            
            totalDigitalBal = otherDigWalletOld + bankWallletOld;
            
            //setting labels for before distribution
            
            lblDigBalTot.setText(""+totalDigitalBal);
            lblOldBankDigAmt.setText(""+bankWallletOld);
            lblOldOtherDigAmt.setText(""+otherDigWalletOld);
            
            lblLiqBalTot.setText(""+totalLiquidBal);
            lblOldCashLiqAmt.setText(""+cashWalletOld);
            lblOldOtherLiqAmt.setText(""+otherCashWalletOld);
            
            //setting labels for after distribution
            lblDigBalTotNew.setText(""+totalDigitalBal);
            lblNewBankDigAmt.setText(""+bankWallletOld);
            lblNewOtherDigAmt.setText(""+otherDigWalletOld);
            
            lblLiqBalTotNew.setText(""+totalLiquidBal);
            lblNewCashLiqAmt.setText(""+cashWalletOld);
            lblNewOtherLiqAmt.setText(""+otherCashWalletOld);
            
            bankWalletNew = bankWallletOld;
            otherDigWalletNew = otherCashWalletOld;
            
            cashWalletNew = cashWalletOld;
            otherCashWalletNew = otherCashWalletOld;
            
            
        } catch (SQLException ex) {
            Logger.getLogger(PeriodicDeposit.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void setMaximumValue()
    {
        int maxval = 0;
        if(radLiquid.isSelected())
        {
            maxval = totalLiquidBal - otherCashWalletNew;
        }
        else
        {
           maxval = totalDigitalBal - otherDigWalletNew;
        }
           snmAmt.setMaximum(maxval); 
    }
    
    public void addRecord()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        String time = formatter.format(date);
        lblDate.setText(time);
        
        isAdd = true;
        
        getNewPeriodicNum();
        getWalletInfoFromDb();
        
        spnPdNum.setValue(newPdNum);
        spnPdNum.setEnabled(false);
        enabledDisabled(true);
        
        radLiquid.setSelected(true);
        
        getOldAmt();
        
        setMaximumValue();
        
        tableModel.arl = arlWalInfo; 
        tableModel.fireTableDataChanged();
        tabWalInfo.setRowSelectionInterval(0, 0);
    }
   
    public void enabledDisabled(boolean val)
    {
        btnAdd.setEnabled(val);
        btnSave.setEnabled(val);
        btnCancel.setEnabled(val);
        radLiquid.setEnabled(val);
        radDigital.setEnabled(val);
        spnAmount.setValue(0);
        spnAmount.setEnabled(val);
        lblTransferAmt.setEnabled(val);
        
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
                newPdNum = maxPdNum + 1;
                isFound = true;
            }
            else
            {
                newPdNum = 1;
                maxPdNum = 0;
            }
            rst.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(PeriodicDeposit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void getWalletInfoFromDb()
    {
        try {
            arlWalInfo.clear();
            String sql = "Select * from Wallet where name not like 'Cash' and name not like 'Bank'";
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            while(rst.next())
            {
                int id = rst.getInt(1);
                String name = rst.getString(2);
                int liqbal = rst.getInt(3);
                int digbal = rst.getInt(4);
                
                WalletDetails wd = new WalletDetails(id, name, digbal, liqbal);
                arlWalInfo.add(wd);
            }
            rst.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(PeriodicDeposit.class.getName()).log(Level.SEVERE, null, ex);
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

        btngrpPdDetail = new javax.swing.ButtonGroup();
        btngrpLiqDig = new javax.swing.ButtonGroup();
        btngrpTransferTarget = new javax.swing.ButtonGroup();
        radAdd = new javax.swing.JRadioButton();
        radShow = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        spnPdNum = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        lblDate = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabWalInfo = new javax.swing.JTable();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        lblDigIcon1 = new javax.swing.JLabel();
        lblDigBalTot = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblDigIcon2 = new javax.swing.JLabel();
        lblOldBankDigAmt = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        lblDigIcon3 = new javax.swing.JLabel();
        lblOldOtherDigAmt = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        lblCashIcon1 = new javax.swing.JLabel();
        lblCashIcon2 = new javax.swing.JLabel();
        lblCashIcon3 = new javax.swing.JLabel();
        lblLiqBalTot = new javax.swing.JLabel();
        lblOldCashLiqAmt = new javax.swing.JLabel();
        lblOldOtherLiqAmt = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        lblDigIcon7 = new javax.swing.JLabel();
        lblDigBalTotNew = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        lblDigIcon8 = new javax.swing.JLabel();
        lblNewBankDigAmt = new javax.swing.JLabel();
        lblDigIcon9 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        lblCashIcon7 = new javax.swing.JLabel();
        lblCashIcon8 = new javax.swing.JLabel();
        lblCashIcon9 = new javax.swing.JLabel();
        lblLiqBalTotNew = new javax.swing.JLabel();
        lblNewCashLiqAmt = new javax.swing.JLabel();
        lblNewOtherLiqAmt = new javax.swing.JLabel();
        lblOtherWallet = new javax.swing.JLabel();
        lblNewOtherDigAmt = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        radLiquid = new javax.swing.JRadioButton();
        radDigital = new javax.swing.JRadioButton();
        lblTransferAmt = new javax.swing.JLabel();
        spnAmount = new javax.swing.JSpinner();
        btnAdd = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Periodic Deposit");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        btngrpPdDetail.add(radAdd);
        radAdd.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radAdd.setText("Add");
        radAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radAddActionPerformed(evt);
            }
        });

        btngrpPdDetail.add(radShow);
        radShow.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radShow.setText("Show");
        radShow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radShowActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel1.setText("Periodic Deposit No.");

        spnPdNum.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        spnPdNum.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnPdNumStateChanged(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel2.setText("As on");

        lblDate.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N

        tabWalInfo.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        tabWalInfo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Old Digital Balance", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tabWalInfo);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/save.png"))); // NOI18N
        btnSave.setToolTipText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/cancel.png"))); // NOI18N
        btnCancel.setToolTipText("Cancel Periodic Deposit");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Before Distribution", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 153))); // NOI18N

        jLabel3.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 0, 102));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Total Digital Bal");
        jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        lblDigIcon1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/dig.jpg"))); // NOI18N

        lblDigBalTot.setBackground(new java.awt.Color(255, 255, 255));
        lblDigBalTot.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblDigBalTot.setForeground(new java.awt.Color(0, 0, 102));
        lblDigBalTot.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblDigBalTot.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        lblDigBalTot.setOpaque(true);

        jLabel4.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(0, 0, 102));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Bank Wallet");
        jLabel4.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        lblDigIcon2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/dig.jpg"))); // NOI18N

        lblOldBankDigAmt.setBackground(new java.awt.Color(255, 255, 255));
        lblOldBankDigAmt.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblOldBankDigAmt.setForeground(new java.awt.Color(0, 0, 102));
        lblOldBankDigAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblOldBankDigAmt.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        lblOldBankDigAmt.setOpaque(true);

        jLabel5.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 0, 102));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Other Wallets");
        jLabel5.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        lblDigIcon3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/dig.jpg"))); // NOI18N

        lblOldOtherDigAmt.setBackground(new java.awt.Color(255, 255, 255));
        lblOldOtherDigAmt.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblOldOtherDigAmt.setForeground(new java.awt.Color(0, 0, 102));
        lblOldOtherDigAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblOldOtherDigAmt.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        lblOldOtherDigAmt.setOpaque(true);

        jLabel10.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 102, 0));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Total Liquid Bal");
        jLabel10.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jLabel11.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 102, 0));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText("Cash Wallet");
        jLabel11.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jLabel12.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(0, 102, 0));
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel12.setText("Other Wallets");
        jLabel12.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        lblCashIcon1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/liqbal.png"))); // NOI18N

        lblCashIcon2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/liqbal.png"))); // NOI18N

        lblCashIcon3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/liqbal.png"))); // NOI18N

        lblLiqBalTot.setBackground(new java.awt.Color(255, 255, 255));
        lblLiqBalTot.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblLiqBalTot.setForeground(new java.awt.Color(0, 102, 0));
        lblLiqBalTot.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblLiqBalTot.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        lblLiqBalTot.setOpaque(true);

        lblOldCashLiqAmt.setBackground(new java.awt.Color(255, 255, 255));
        lblOldCashLiqAmt.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblOldCashLiqAmt.setForeground(new java.awt.Color(0, 102, 0));
        lblOldCashLiqAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblOldCashLiqAmt.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        lblOldCashLiqAmt.setOpaque(true);

        lblOldOtherLiqAmt.setBackground(new java.awt.Color(255, 255, 255));
        lblOldOtherLiqAmt.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblOldOtherLiqAmt.setForeground(new java.awt.Color(0, 102, 0));
        lblOldOtherLiqAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblOldOtherLiqAmt.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        lblOldOtherLiqAmt.setOpaque(true);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(lblCashIcon1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblLiqBalTot, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(lblCashIcon2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblOldCashLiqAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblCashIcon3, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblOldOtherLiqAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(32, 32, 32)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblDigIcon3, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblOldOtherDigAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblDigIcon1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblDigBalTot, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblDigIcon2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblOldBankDigAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblLiqBalTot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblDigIcon1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDigBalTot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblCashIcon1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblOldBankDigAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDigIcon2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblOldCashLiqAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCashIcon2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lblCashIcon3, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblDigIcon3, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblOldOtherDigAmt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblOldOtherLiqAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "After Distribution", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 153))); // NOI18N

        jLabel19.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(0, 0, 102));
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel19.setText("Total Digital Bal");

        lblDigIcon7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/dig.jpg"))); // NOI18N

        lblDigBalTotNew.setBackground(new java.awt.Color(255, 255, 255));
        lblDigBalTotNew.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblDigBalTotNew.setForeground(new java.awt.Color(0, 0, 102));
        lblDigBalTotNew.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblDigBalTotNew.setOpaque(true);

        jLabel20.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(0, 0, 102));
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel20.setText("Bank Wallet");

        lblDigIcon8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/dig.jpg"))); // NOI18N

        lblNewBankDigAmt.setBackground(new java.awt.Color(255, 255, 255));
        lblNewBankDigAmt.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblNewBankDigAmt.setForeground(new java.awt.Color(0, 0, 102));
        lblNewBankDigAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblNewBankDigAmt.setOpaque(true);

        lblDigIcon9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/dig.jpg"))); // NOI18N

        jLabel22.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(0, 102, 0));
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel22.setText("Total Liquid Bal");

        jLabel23.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(0, 102, 0));
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel23.setText("Cash Wallet");

        jLabel24.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(0, 102, 0));
        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel24.setText("Other Wallets");

        lblCashIcon7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/liqbal.png"))); // NOI18N

        lblCashIcon8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/liqbal.png"))); // NOI18N

        lblCashIcon9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/liqbal.png"))); // NOI18N

        lblLiqBalTotNew.setBackground(new java.awt.Color(255, 255, 255));
        lblLiqBalTotNew.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblLiqBalTotNew.setForeground(new java.awt.Color(0, 102, 0));
        lblLiqBalTotNew.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblLiqBalTotNew.setOpaque(true);

        lblNewCashLiqAmt.setBackground(new java.awt.Color(255, 255, 255));
        lblNewCashLiqAmt.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblNewCashLiqAmt.setForeground(new java.awt.Color(0, 102, 0));
        lblNewCashLiqAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblNewCashLiqAmt.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        lblNewCashLiqAmt.setOpaque(true);

        lblNewOtherLiqAmt.setBackground(new java.awt.Color(255, 255, 255));
        lblNewOtherLiqAmt.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblNewOtherLiqAmt.setForeground(new java.awt.Color(0, 102, 0));
        lblNewOtherLiqAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblNewOtherLiqAmt.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        lblNewOtherLiqAmt.setOpaque(true);

        lblOtherWallet.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        lblOtherWallet.setForeground(new java.awt.Color(0, 0, 102));
        lblOtherWallet.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblOtherWallet.setText("Other Wallets");

        lblNewOtherDigAmt.setBackground(new java.awt.Color(255, 255, 255));
        lblNewOtherDigAmt.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblNewOtherDigAmt.setForeground(new java.awt.Color(0, 0, 102));
        lblNewOtherDigAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblNewOtherDigAmt.setOpaque(true);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblCashIcon7, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblLiqBalTotNew, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel24, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                            .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(lblCashIcon8, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblNewCashLiqAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addComponent(lblCashIcon9, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblNewOtherLiqAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(32, 32, 32)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblDigIcon7, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblDigBalTotNew, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblOtherWallet, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(lblDigIcon9, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblNewOtherDigAmt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(lblDigIcon8, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblNewBankDigAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(48, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblLiqBalTotNew, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblDigIcon7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDigBalTotNew, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblCashIcon7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(11, 11, 11)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblDigIcon8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(lblCashIcon8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblNewCashLiqAmt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(lblNewBankDigAmt, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblNewOtherLiqAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lblCashIcon9, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblNewOtherDigAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblDigIcon9, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblOtherWallet, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder(null, new java.awt.Color(204, 204, 204)));

        btngrpLiqDig.add(radLiquid);
        radLiquid.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radLiquid.setText("Liquid");
        radLiquid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radLiquidActionPerformed(evt);
            }
        });

        btngrpLiqDig.add(radDigital);
        radDigital.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radDigital.setText("Digital");
        radDigital.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        radDigital.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radDigitalActionPerformed(evt);
            }
        });

        lblTransferAmt.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        lblTransferAmt.setText("Transfer Amount");

        spnAmount.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N

        btnAdd.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        btnAdd.setText("Add");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(radDigital)
                        .addGap(30, 30, 30)
                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(radLiquid)
                        .addGap(18, 18, 18)
                        .addComponent(lblTransferAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(51, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radLiquid)
                    .addComponent(lblTransferAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(radDigital)
                    .addComponent(btnAdd, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(70, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblDate, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(247, 247, 247)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(28, 28, 28)
                                .addComponent(spnPdNum, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(radAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(radShow, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(216, 216, 216)
                        .addComponent(btnSave)
                        .addGap(18, 18, 18)
                        .addComponent(btnCancel)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radShow))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDate, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addGap(18, 18, 18))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(spnPdNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)))
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnSave)
                    .addComponent(btnCancel))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void radAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radAddActionPerformed
        // TODO add your handling code here:
        addRecord();
    }//GEN-LAST:event_radAddActionPerformed

    private void radShowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radShowActionPerformed
        // TODO add your handling code here:
        if(clickedOnAdd == true)
        {
            int type = JOptionPane.showConfirmDialog(this,"Periodic Deposit not saved.Will you like to save it?", "Message",JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if(type == JOptionPane.NO_OPTION)
            {
                if(maxPdNum>0)
                {
                    isAdd = false;
                    enabledDisabled(false);
                    spnPdNum.setEnabled(true);
                    spnPdNum.setValue(maxPdNum);
                }
                clickedOnAdd = false;
                
            }
            else
            {
                radAdd.setSelected(true);
            }

        }
        else
        {
            if(maxPdNum>0)
            {
                isAdd = false;
                enabledDisabled(false);
                spnPdNum.setEnabled(true);
                spnPdNum.setValue(maxPdNum);
            }
        }
       
    }//GEN-LAST:event_radShowActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
        clickedOnAdd = true;
        int rowindex = tabWalInfo.getSelectedRow();
        int colindex = 0;
        int amount = (int)snmAmt.getValue();

        if(radLiquid.isSelected())
        {
                WalletDetails wd = tableModel.arl.get(rowindex);
                tableModel.setValueAt(amount, rowindex, 2);
                otherCashWalletNew = 0;
                for (WalletDetails w : tableModel.arl) {
                    otherCashWalletNew += w.getWalletLiqAmtNew();
                }
                cashWalletNew = totalLiquidBal - otherCashWalletNew;
                
                lblNewCashLiqAmt.setText(""+cashWalletNew);
                lblNewOtherLiqAmt.setText(""+otherCashWalletNew);
                
        }
        else
        {
                WalletDetails wd = tableModel.arl.get(rowindex);
                tableModel.setValueAt(amount, rowindex, 4);
                otherDigWalletNew= 0;
                for (WalletDetails w : tableModel.arl) {
                    otherDigWalletNew += w.getWalletDgtAmtNew();
                }
                bankWalletNew = totalDigitalBal - otherDigWalletNew;
                
                lblNewBankDigAmt.setText(""+bankWalletNew);
                lblNewOtherDigAmt.setText(""+otherDigWalletNew);
           
        }
        snmAmt.setValue(0);
        setMaximumValue();
    
    }//GEN-LAST:event_btnAddActionPerformed

    private void radLiquidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radLiquidActionPerformed
        // TODO add your handling code here:
        setMaximumValue();
    }//GEN-LAST:event_radLiquidActionPerformed

    private void radDigitalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radDigitalActionPerformed
        // TODO add your handling code here:
       setMaximumValue();
    }//GEN-LAST:event_radDigitalActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        try {
            // TODO add your handling code here:
            
            con.setAutoCommit(false);
            String sql;
            Date udt = new SimpleDateFormat("dd/MM/yyyy").parse(lblDate.getText().trim());
            java.sql.Date sdt = new java.sql.Date(udt.getTime());
            PreparedStatement pstmt = null;
            
            Calendar c = Calendar.getInstance();
            int secs = c.get(Calendar.HOUR_OF_DAY) * 3600 + c.get(Calendar.MINUTE)*60 + c.get(Calendar.SECOND);
            
            int i = 0 ;
            
            sql = "Insert into Pd(Id,Num,Dt,NoOfSeconds,BankWallletOld,OtherDigWalletOld,CashWalletOld,OtherCashWalletOld,BankWalletNew,OtherDigWalletNew,CashWalletNew,OtherCashWalletNew) ";
            sql+="values(seq.nextval,?,?,?,?,?,?,?,?,?,?,?)";
            
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, newPdNum);
            pstmt.setDate(2, sdt);
            pstmt.setInt(3, secs);
            pstmt.setInt(4,bankWallletOld);
            pstmt.setInt(5, otherDigWalletOld);
            pstmt.setInt(6, cashWalletOld);
            pstmt.setInt(7, otherCashWalletOld);
            pstmt.setInt(8, bankWalletNew);
            pstmt.setInt(9, otherDigWalletNew);
            pstmt.setInt(10, cashWalletNew);
            pstmt.setInt(11, otherCashWalletNew);
            
            pstmt.executeQuery();
            pstmt.close();
            
            sql = "Select Id from Pd where Num = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1,newPdNum);
            ResultSet rst = pstmt.executeQuery();
            int pdId = 0;
            if(rst.next())
            {
               pdId = rst.getInt(1);
            }
            rst.close();
            pstmt.close();
            
            for (i = 0;i<tableModel.arl.size();i++) {
                
                WalletDetails wd = tableModel.arl.get(i);
                
                sql = "Insert into PdDetails(PdId,WalletId,WalletDgtAmtOld,WalletDgtAmtNew,WalletLiqAmtOld,WalletLiqAmtNew) ";
                sql+="values(?,?,?,?,?,?)";
                pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, pdId);
                pstmt.setInt(2, wd.getWalletId());
                pstmt.setInt(3,wd.getWalletDgtAmtOld());
                pstmt.setInt(4,wd.getWalletDgtAmtNew());
                pstmt.setInt(5,wd.getWalletLiqAmtOld());
                pstmt.setInt(6,wd.getWalletLiqAmtNew());
                
                pstmt.executeUpdate();
                pstmt.close();
                
                sql = "Update Wallet set Liquidbal = ? where Id = ?";
                pstmt = con.prepareStatement(sql);
                pstmt.setInt(1,wd.getWalletLiqAmtNew());
                pstmt.setInt(2, wd.getWalletId());
                pstmt.executeUpdate();
                pstmt.close();
                
                
                sql = "Update Wallet set Digitalbal = ? where Id = ?";
                pstmt = con.prepareStatement(sql);
                pstmt.setInt(1,wd.getWalletDgtAmtNew());
                pstmt.setInt(2, wd.getWalletId());
                pstmt.executeUpdate();
                pstmt.close();
                
            }
            
             sql = "Update Wallet set Liquidbal = ? where Name like 'Cash'";
             pstmt = con.prepareStatement(sql);
             pstmt.setInt(1,cashWalletNew);
             pstmt.executeUpdate();
             pstmt.close();
             
             sql = "Update Wallet set Digitalbal = ? where Name like 'Bank'";
             pstmt = con.prepareStatement(sql);
             pstmt.setInt(1,bankWalletNew);
             pstmt.executeUpdate();
             pstmt.close();
            
            con.commit();
            con.setAutoCommit(true);
            MainFrame.periodicDepositMade = true;
             if(MainFrame.pdDepositDialog!=null)
                   MainFrame.pdDepositDialog.dispose();
            this.dispose();
            
        } catch (SQLException | ParseException ex) {
            Logger.getLogger(PeriodicDeposit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void spnPdNumStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnPdNumStateChanged
        getPdDetails();
    }//GEN-LAST:event_spnPdNumStateChanged

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        // TODO add your handling code here:
        MainFrame.periodicDepositMade = false;
        if(maxPdNum>0)
        {
            radShow.setSelected(true);
            clickedOnAdd = false;
            isAdd = false;
            enabledDisabled(false);
            spnPdNum.setEnabled(true);
            spnPdNum.setValue(maxPdNum);
        }
        else
        {
             if(MainFrame.pdDepositDialog!=null)
                      MainFrame.pdDepositDialog.dispose();
            this.dispose();
        }
    }//GEN-LAST:event_btnCancelActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
                       MainFrame.periodicDepositMade = false; 
                       if(MainFrame.pdDepositDialog!=null)
                                  MainFrame.pdDepositDialog.dispose();
                        if(clickedOnAdd == true)
                        {
                            int type = JOptionPane.showConfirmDialog(this,"Periodic Deposit not saved.Will you like to save it?", "Message",JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if(type == JOptionPane.NO_OPTION)
                            {
                                dispose();
                            }
                            else
                            {
                            }
                            
                        }
                        else 
                        {
                            dispose();
                        }
    }//GEN-LAST:event_formWindowClosing

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
            java.util.logging.Logger.getLogger(PeriodicDeposit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PeriodicDeposit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PeriodicDeposit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PeriodicDeposit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                PeriodicDeposit dialog = new PeriodicDeposit(new javax.swing.JFrame(), true);
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    private javax.swing.ButtonGroup btngrpLiqDig;
    private javax.swing.ButtonGroup btngrpPdDetail;
    private javax.swing.ButtonGroup btngrpTransferTarget;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCashIcon1;
    private javax.swing.JLabel lblCashIcon2;
    private javax.swing.JLabel lblCashIcon3;
    private javax.swing.JLabel lblCashIcon7;
    private javax.swing.JLabel lblCashIcon8;
    private javax.swing.JLabel lblCashIcon9;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblDigBalTot;
    private javax.swing.JLabel lblDigBalTotNew;
    private javax.swing.JLabel lblDigIcon1;
    private javax.swing.JLabel lblDigIcon2;
    private javax.swing.JLabel lblDigIcon3;
    private javax.swing.JLabel lblDigIcon7;
    private javax.swing.JLabel lblDigIcon8;
    private javax.swing.JLabel lblDigIcon9;
    private javax.swing.JLabel lblLiqBalTot;
    private javax.swing.JLabel lblLiqBalTotNew;
    private javax.swing.JLabel lblNewBankDigAmt;
    private javax.swing.JLabel lblNewCashLiqAmt;
    private javax.swing.JLabel lblNewOtherDigAmt;
    private javax.swing.JLabel lblNewOtherLiqAmt;
    private javax.swing.JLabel lblOldBankDigAmt;
    private javax.swing.JLabel lblOldCashLiqAmt;
    private javax.swing.JLabel lblOldOtherDigAmt;
    private javax.swing.JLabel lblOldOtherLiqAmt;
    private javax.swing.JLabel lblOtherWallet;
    private javax.swing.JLabel lblTransferAmt;
    private javax.swing.JRadioButton radAdd;
    private javax.swing.JRadioButton radDigital;
    private javax.swing.JRadioButton radLiquid;
    private javax.swing.JRadioButton radShow;
    private javax.swing.JSpinner spnAmount;
    private javax.swing.JSpinner spnPdNum;
    private javax.swing.JTable tabWalInfo;
    // End of variables declaration//GEN-END:variables
}
