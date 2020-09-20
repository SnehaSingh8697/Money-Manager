/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.inttrans;

import domesticfinancesystem.calendar.Database;
import domesticfinancesystem.wallet.MyWalletPanel;
import domesticfinancesystem.wallet.Wallet;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Window;
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
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.AbstractDocument;
import domesticfinancesystem.*;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *
 * @author sneha
 */
public class InternalTransactionPanel extends javax.swing.JPanel {

    /**
     * Creates new form InternalTransation
     */
    private Wallet wallet;
    private Date dt;
    private Image cashImg;
    private Image digImg;
    private Database dc;
    private Connection con;
    private int trgtLiqBal;
    private int trgrtDigBal;
    private int totLiqTransAmt = 0;
    private int totDigTransAmt = 0;
    private int targetWalletId;
    private ArrayList<String> restrictedLiqWalNames = new ArrayList<>();
    private ArrayList<String> restrictedDigWalNames = new ArrayList<>();
    private MyTableModel tableModel;
    private Window parentWindow;
    
    
    private class MyTableModel extends AbstractTableModel
    {
        final int COLS = 3;
        String[] colNames = {"Liquid/Digital","Source Wallet","Transfer Amount"};
        Class[] colTypes = {Image.class, String.class,Integer.class} ;
        ArrayList<IntTransData> arl = new ArrayList<IntTransData>();

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
           IntTransData itd = (IntTransData)arl.get(rowIndex);
           if(columnIndex == 0)
           {
               boolean isLiq = itd.isIsLiq();
               Image img;
                if(isLiq)
                    img = cashImg;
                else
                    img = digImg;
               return img;
           }
           else if(columnIndex == 1)
               return  (String)itd.getSourceWalletName();
           else 
           {
               int transferAmt = 0;
               boolean isLiq = itd.isIsLiq();
               if(isLiq)
                    transferAmt = itd.getTransLiqAmt();
                else
                    transferAmt = itd.getTransDgtAmt();
               return transferAmt;
           }
        }
        
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            IntTransData itd = arl.get(rowIndex);
            if(columnIndex == 1)
                itd.setSourceWalletName((String)aValue); 
            else if(columnIndex == 2)
            {
                if(itd.isIsLiq() == true)
                    itd.setTransLiqAmt((Integer)aValue);
                else
                  itd.setTransDgtAmt((Integer)aValue);  
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
        public void addRow(WalletInfo wi,int amount)
        {
           int transDigAmt = 0;
           int transLiqAmt = 0;
           boolean isLiq;
           if(wi.isIsLiquid() == true)
           {
               transDigAmt = 0;
               transLiqAmt = amount;
               isLiq = true;
           }
           else
           {
               transDigAmt = amount;
               transLiqAmt = 0;
               isLiq = false;
           }
           String name = wi.getName();
           arl.add(new IntTransData(wi.getId(), targetWalletId, wi.getDigitalAmount(), wi.getLiquidAmount(), trgrtDigBal, trgtLiqBal, transDigAmt, transLiqAmt,isLiq,wi.getName())) ;
           
           if(wi.isIsLiquid() == true)
               trgtLiqBal += transLiqAmt;
           else
               trgrtDigBal+= transDigAmt;
        }
    }
    
    class CellRenderer extends DefaultTableCellRenderer
    {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
           super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); //To change body of generated methods, choose Tools | Templates.

               if(column == 0)
               {
//                    Image img = scaleImage((Image)value, 10, 10);
                    Image img = (Image)value ;
                    ImageIcon icon = new ImageIcon(img);
                    setIcon(icon);
                    setHorizontalAlignment(JLabel.CENTER);
                    setText("");
               }
               
            return this;
        
        }
        
    }
 
    public void setValuesToLabels()
    {
        lblLiqTransferAmt.setText(""+totLiqTransAmt);
        lblDigTransferAmt.setText(""+totDigTransAmt);
        lblTrgWalLiqBal1.setText(""+(trgtLiqBal));
        lblTrgWalDigBal1.setText(""+(trgrtDigBal));
    }
    
    public InternalTransactionPanel(Wallet wallet,Window f) {
        try {
            parentWindow = f;
            initComponents();
            
            ((AbstractDocument)txtTransferAmt.getDocument()).setDocumentFilter(new domesticfinancesystem.IntegerDocumentFilter());
            
            this.wallet = wallet;
            tableModel = new MyTableModel() ;
            tabTable.setModel(tableModel);
            tabTable.setRowHeight(25);
            
            tabTable.setShowGrid(true); //to show table border for each cell
            
            ((DefaultTableCellRenderer)tabTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
            
            //setting renderer fir cash and digital image
            CellRenderer cr = new CellRenderer();
            tabTable.setDefaultRenderer(Image.class,cr);
            
            
            //setting table cell data at center
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
            tabTable.setDefaultRenderer(String.class,centerRenderer);
            tabTable.setDefaultRenderer(Integer.class,centerRenderer);
             
            cashImg = ImageIO.read(getClass().getResource("/testimages/liqbal.png"));
            digImg =  ImageIO.read(getClass().getResource("/testimages/dig.jpg"));
            
            ((AbstractDocument)txtTransferAmt.getDocument()).setDocumentFilter(new IntegerDocumentFilter());
            
            targetWalletId = wallet.getId();
            
            lblTargetWalletName.setText(wallet.getName());
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date date = new Date();
            String time = formatter.format(date);
            lblTransTime.setText(time);
            dc = MainFrame.dc;
            con = dc.createConnection();
            cmbSrcWallet.setRenderer(new WalletComboBoxCellRenderer());
            
            if(wallet.getName().equals("Cash"))
            {
                radDigital.setEnabled(false);
                radLiquid.setEnabled(true);
                radLiquid.setSelected(true);
            }
            else if(wallet.getName().equals("Bank"))
            {
                radDigital.setEnabled(true);
                radDigital.setSelected(true);
                radLiquid.setEnabled(false);
            }
            else
                radLiquid.setSelected(true);
            
            
            getWalletAmountFromDatabase();
            setWalletsToComboBox();
        } 
        catch (IOException ex) {
            Logger.getLogger(InternalTransactionPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void getWalletAmountFromDatabase()
    {
        try {
            String sql = "Select * from Wallet where Id = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, wallet.getId());
            ResultSet rst = pstmt.executeQuery();
            if(rst.next())
            {
                trgtLiqBal = rst.getInt("Liquidbal");
                trgrtDigBal = rst.getInt("Digitalbal");
                lblTrgWalLiqBal.setText(""+trgtLiqBal);
                lblTrgWalDigBal.setText(""+trgrtDigBal);
            }
            rst.close();
            pstmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(InternalTransactionPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    public void setWalletsToComboBox()
    {
        try {
            cmbSrcWallet.removeAllItems();
            String walletname = wallet.getName();
            String sql;
            boolean isLiquid;
            ArrayList<String> restrictedWalNames;
            if(radLiquid.isSelected())
            {
                sql = "Select * from Wallet where Name Not like ? and Liquidbal > 0";
                isLiquid = true;
                restrictedWalNames = restrictedLiqWalNames;
            }
            else
            {
               sql = "Select * from Wallet where Name Not like ? and Digitalbal > 0"; 
               isLiquid = false;
               restrictedWalNames = restrictedDigWalNames;
            }
                PreparedStatement pstmt = con.prepareStatement(sql);
                pstmt.setString(1, walletname);
                ResultSet rst = pstmt.executeQuery();
                while(rst.next())
                {
                    boolean found = false;
                    int id = rst.getInt("Id");
                    String name = rst.getString("Name");
                    for (String nm : restrictedWalNames) {
                        if(name.equals(nm))
                        {
                            found = true;
                            break;
                        }
                    }
                    if(found == false)
                    {
                        int liquidAmt = rst.getInt("Liquidbal");
                        int digitalAmt = rst.getInt("Digitalbal");
                        cmbSrcWallet.addItem(new WalletInfo(id,name,liquidAmt,digitalAmt,isLiquid));
                    }
                }
                rst.close();
                pstmt.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(InternalTransactionPanel.class.getName()).log(Level.SEVERE, null, ex);
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

        radGrpAmtType = new javax.swing.ButtonGroup();
        upperPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lblTargetWalletName = new javax.swing.JLabel();
        lblTime = new javax.swing.JLabel();
        lblTransTime = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        lblLiqBal = new javax.swing.JLabel();
        lblDigBal = new javax.swing.JLabel();
        lblTrgWalLiqBal = new javax.swing.JLabel();
        lblTrgWalDigBal = new javax.swing.JLabel();
        midlePanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabTable = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        radLiquid = new javax.swing.JRadioButton();
        radDigital = new javax.swing.JRadioButton();
        cmbSrcWallet = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        txtTransferAmt = new javax.swing.JTextField();
        btnAdd = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        btnTransferAmount = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        lblLiqAmt = new javax.swing.JLabel();
        lblLiqTransferAmt = new javax.swing.JLabel();
        lblDigAmt = new javax.swing.JLabel();
        lblDigTransferAmt = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        lblLiqBal1 = new javax.swing.JLabel();
        lblDigBal1 = new javax.swing.JLabel();
        lblTrgWalLiqBal1 = new javax.swing.JLabel();
        lblTrgWalDigBal1 = new javax.swing.JLabel();

        jLabel1.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel1.setText("Transfer To");

        lblTargetWalletName.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.background"));
        lblTargetWalletName.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblTargetWalletName.setOpaque(true);

        lblTime.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        lblTime.setText("Date");

        lblTransTime.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.background"));
        lblTransTime.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        lblTransTime.setAlignmentX(0.5F);
        lblTransTime.setOpaque(true);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Balance Before Transfer", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 204))); // NOI18N

        lblLiqBal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/liqbal.png"))); // NOI18N

        lblDigBal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/dig.jpg"))); // NOI18N

        lblTrgWalLiqBal.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.background"));
        lblTrgWalLiqBal.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblTrgWalLiqBal.setForeground(new java.awt.Color(0, 102, 0));
        lblTrgWalLiqBal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTrgWalLiqBal.setOpaque(true);

        lblTrgWalDigBal.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.background"));
        lblTrgWalDigBal.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblTrgWalDigBal.setForeground(new java.awt.Color(0, 0, 102));
        lblTrgWalDigBal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTrgWalDigBal.setOpaque(true);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblLiqBal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblDigBal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTrgWalDigBal, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTrgWalLiqBal, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblLiqBal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblTrgWalLiqBal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblDigBal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblTrgWalDigBal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout upperPanelLayout = new javax.swing.GroupLayout(upperPanel);
        upperPanel.setLayout(upperPanelLayout);
        upperPanelLayout.setHorizontalGroup(
            upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upperPanelLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblTransTime, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                    .addComponent(lblTargetWalletName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        upperPanelLayout.setVerticalGroup(
            upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upperPanelLayout.createSequentialGroup()
                .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(upperPanelLayout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTargetWalletName, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTime, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTransTime, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(upperPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        midlePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Source Wallet", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 153))); // NOI18N

        tabTable.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N
        jScrollPane2.setViewportView(tabTable);

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder(null, new java.awt.Color(204, 204, 204)));

        radGrpAmtType.add(radLiquid);
        radLiquid.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radLiquid.setText("Liquid");
        radLiquid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radLiquidActionPerformed(evt);
            }
        });

        radGrpAmtType.add(radDigital);
        radDigital.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radDigital.setText("Digital");
        radDigital.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radDigitalActionPerformed(evt);
            }
        });

        cmbSrcWallet.setFont(new java.awt.Font("Courier New", 0, 11)); // NOI18N
        cmbSrcWallet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbSrcWalletActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel2.setText("Transfer Amount");

        txtTransferAmt.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        txtTransferAmt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        btnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/Add.gif"))); // NOI18N
        btnAdd.setToolTipText("Add");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/delete.gif"))); // NOI18N
        btnDelete.setToolTipText("Delete");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(radLiquid, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cmbSrcWallet, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(radDigital, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(txtTransferAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(31, 31, 31)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnDelete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(radLiquid)
                        .addComponent(cmbSrcWallet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnAdd))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtTransferAmt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnDelete)
                    .addComponent(radDigital))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout midlePanelLayout = new javax.swing.GroupLayout(midlePanel);
        midlePanel.setLayout(midlePanelLayout);
        midlePanelLayout.setHorizontalGroup(
            midlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(midlePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(midlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 482, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        midlePanelLayout.setVerticalGroup(
            midlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(midlePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnTransferAmount.setFont(new java.awt.Font("Garamond", 1, 20)); // NOI18N
        btnTransferAmount.setForeground(new java.awt.Color(102, 0, 0));
        btnTransferAmount.setText("Transfer Amount");
        btnTransferAmount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTransferAmountActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Garamond", 1, 16)); // NOI18N
        jLabel3.setText("Total Transfer Amount");

        lblLiqTransferAmt.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.background"));
        lblLiqTransferAmt.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblLiqTransferAmt.setForeground(new java.awt.Color(0, 102, 0));
        lblLiqTransferAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblLiqTransferAmt.setOpaque(true);

        lblDigTransferAmt.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.background"));
        lblDigTransferAmt.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblDigTransferAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblDigTransferAmt.setOpaque(true);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Balance After Transfer", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 204))); // NOI18N

        lblLiqBal1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/liqbal.png"))); // NOI18N

        lblDigBal1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/dig.jpg"))); // NOI18N

        lblTrgWalLiqBal1.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.background"));
        lblTrgWalLiqBal1.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblTrgWalLiqBal1.setForeground(new java.awt.Color(0, 102, 0));
        lblTrgWalLiqBal1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTrgWalLiqBal1.setOpaque(true);

        lblTrgWalDigBal1.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.background"));
        lblTrgWalDigBal1.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblTrgWalDigBal1.setForeground(new java.awt.Color(0, 0, 102));
        lblTrgWalDigBal1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTrgWalDigBal1.setOpaque(true);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblLiqBal1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblDigBal1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTrgWalDigBal1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTrgWalLiqBal1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblLiqBal1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblTrgWalLiqBal1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblDigBal1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblTrgWalDigBal1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(lblLiqAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblLiqTransferAmt, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(lblDigAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblDigTransferAmt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(58, 58, 58)
                        .addComponent(btnTransferAmount))
                    .addComponent(jLabel3))
                .addGap(50, 50, 50)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblLiqAmt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnTransferAmount)
                            .addComponent(lblLiqTransferAmt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblDigAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblDigTransferAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 17, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addComponent(midlePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(upperPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(upperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(midlePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void radLiquidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radLiquidActionPerformed
        // TODO add your handling code here:
        setWalletsToComboBox();
    }//GEN-LAST:event_radLiquidActionPerformed

    private void radDigitalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radDigitalActionPerformed
        // TODO add your handling code here:
        setWalletsToComboBox();
    }//GEN-LAST:event_radDigitalActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
        WalletInfo wi = (WalletInfo)cmbSrcWallet.getSelectedItem();
        if(txtTransferAmt.getText() != null && !txtTransferAmt.getText().isEmpty())
        {
            int transferAmt = Integer.parseInt(txtTransferAmt.getText());
            int walletAmount = 0;
            if(radLiquid.isSelected())
                walletAmount = wi.getLiquidAmount();
            else
                walletAmount = wi.getDigitalAmount();

            if(transferAmt>walletAmount)
            {
               JOptionPane.showMessageDialog(this, "Transfer Amount Exceeds Wallet Limit", "Message", JOptionPane.ERROR_MESSAGE);
            }
            else
            {
               tableModel.addRow(wi,transferAmt);
              
               int row = tableModel.getRowCount() -1 ;
               
                tableModel.fireTableRowsInserted(row, row);
                tabTable.setRowSelectionInterval(row, row);
               
               if(radDigital.isSelected())
               {
                   totDigTransAmt+=transferAmt;
                   restrictedDigWalNames.add(wi.getName());

               }
               else
               {
                   totLiqTransAmt+=transferAmt;
                   restrictedLiqWalNames.add(wi.getName());

               }
               setValuesToLabels();
               txtTransferAmt.setText("");
               setWalletsToComboBox();
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "Please Enter Transfer Amount", "Message", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
        int row = tabTable.getSelectedRow() ;
        if(row != -1)
        {
            IntTransData itd = tableModel.arl.get(row);
            
            if(itd.isIsLiq())
            {
                int amt = itd.getTransLiqAmt();
                totLiqTransAmt-=amt;
                restrictedLiqWalNames.remove(itd.getSourceWalletName());
                trgtLiqBal -= itd.getTransLiqAmt();
            }
            else
            {
               int amt = itd.getTransDgtAmt();
                totLiqTransAmt-=amt; 
                restrictedDigWalNames.remove(itd.getSourceWalletName());
                trgrtDigBal-= itd.getTransDgtAmt();
            }
            cmbSrcWallet.addItem(new WalletInfo(itd.getSourceWalletId(), itd.getSourceWalletName(), itd.getSourceWalletOldLiqAmt(), itd.getSourceWalletOldDgtAmt(), itd.isIsLiq()));
            tableModel.arl.remove(row) ;
            tableModel.fireTableRowsDeleted(row, row);
            
            int rows = tabTable.getRowCount() ;
            if(rows > 0)
            {
                if(row == rows)
                    row = rows - 1 ;
                
                tabTable.setRowSelectionInterval(row, row);
            }
        }
        
        setValuesToLabels();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnTransferAmountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTransferAmountActionPerformed
        
        if(tableModel.arl.size()>0)
        {
        try {
            con.setAutoCommit(false);
            String sql;
            Date udt = new SimpleDateFormat("dd/MM/yyyy").parse(lblTransTime.getText().trim());
            java.sql.Date sdt = new java.sql.Date(udt.getTime());
            PreparedStatement pstmt = null;
            
            Calendar c = Calendar.getInstance();
            int secs = c.get(Calendar.HOUR_OF_DAY) * 3600 + c.get(Calendar.MINUTE)*60 + c.get(Calendar.SECOND);
            
            int i = 0 ;
            for (IntTransData itd : tableModel.arl) {
                
                System.out.print("Iteration " + i++);
                sql = "Insert into IntTrans(Id,Dt,NoOfSeconds,SourceWalletId,TargetWalletId,SourceWalletOldDgtAmt,SourceWalletOldLiqAmt,TargetWalletOldDgtAmt,TargetWalletOldLiqAmt,TransDgtAmt,TransLiqAmt) ";
                sql+="values(seq.nextval,?,?,?,?,?,?,?,?,?,?)";
                pstmt = con.prepareStatement(sql);
                pstmt.setDate(1, sdt);
                pstmt.setInt(2, secs);
                pstmt.setInt(3, itd.getSourceWalletId());
                pstmt.setInt(4, itd.getTargetWalletId());
                pstmt.setInt(5, itd.getSourceWalletOldDgtAmt());
                pstmt.setInt(6, itd.getSourceWalletOldLiqAmt());
                pstmt.setInt(7, itd.getTargetWalletOldDgtAmt());
                pstmt.setInt(8, itd.getTargetWalletOldLiqAmt());
                pstmt.setInt(9, itd.getTransDgtAmt());
                pstmt.setInt(10, itd.getTransLiqAmt());
                
                pstmt.executeQuery();
                pstmt.close();
                
                //Updating the source Wallet
                
                int sourceWalletId = itd.getSourceWalletId();
                int amount = 0;
                if(itd.isIsLiq())
                {
                    amount = itd.getSourceWalletOldLiqAmt() - itd.getTransLiqAmt();
                    sql = "Update Wallet set Liquidbal = ? where Id = ?";
                }
                else
                {
                    amount = itd.getSourceWalletOldDgtAmt() - itd.getTransDgtAmt();
                    sql = "Update Wallet set Digitalbal = ? where Id = ?";
                }
                pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, amount);
                pstmt.setInt(2, sourceWalletId);
                
                pstmt.executeUpdate();
                pstmt.close();
                System.out.println(" done");
            }
            
            //Update the Target Wallet
            
            int totLiquidbal = trgtLiqBal;
            int totDigitalbal = trgrtDigBal;
            
            sql = "Update Wallet set Liquidbal = ?,Digitalbal = ? where Id = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, totLiquidbal);
            pstmt.setInt(2, totDigitalbal);
            pstmt.setInt(3, targetWalletId);
                
            pstmt.executeUpdate();
            
            con.commit();
            con.setAutoCommit(true);
            pstmt.close();
//            con.close();
            
            parentWindow.dispose() ;
        
        } catch (ParseException | SQLException ex) {
            Logger.getLogger(InternalTransactionPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No Records Found", "Message", JOptionPane.ERROR_MESSAGE);
        }
        
    }//GEN-LAST:event_btnTransferAmountActionPerformed

    private void cmbSrcWalletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSrcWalletActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbSrcWalletActionPerformed
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
            java.util.logging.Logger.getLogger(InternalTransactionPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(InternalTransactionPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(InternalTransactionPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(InternalTransactionPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                JFrame  f = new JFrame();
                
                f.setTitle("Internal Transaction");
                f.add(new InternalTransactionPanel(new Wallet(106, "Transport"),f));
                f.pack();
                f.setLocationRelativeTo(null);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
                
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnTransferAmount;
    private javax.swing.JComboBox<WalletInfo> cmbSrcWallet;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblDigAmt;
    private javax.swing.JLabel lblDigBal;
    private javax.swing.JLabel lblDigBal1;
    private javax.swing.JLabel lblDigTransferAmt;
    private javax.swing.JLabel lblLiqAmt;
    private javax.swing.JLabel lblLiqBal;
    private javax.swing.JLabel lblLiqBal1;
    private javax.swing.JLabel lblLiqTransferAmt;
    private javax.swing.JLabel lblTargetWalletName;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTransTime;
    private javax.swing.JLabel lblTrgWalDigBal;
    private javax.swing.JLabel lblTrgWalDigBal1;
    private javax.swing.JLabel lblTrgWalLiqBal;
    private javax.swing.JLabel lblTrgWalLiqBal1;
    private javax.swing.JPanel midlePanel;
    private javax.swing.JRadioButton radDigital;
    private javax.swing.ButtonGroup radGrpAmtType;
    private javax.swing.JRadioButton radLiquid;
    private javax.swing.JTable tabTable;
    private javax.swing.JTextField txtTransferAmt;
    private javax.swing.JPanel upperPanel;
    // End of variables declaration//GEN-END:variables
}
