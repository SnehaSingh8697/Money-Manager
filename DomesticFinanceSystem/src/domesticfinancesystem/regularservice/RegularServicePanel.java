/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.regularservice;

import domesticfinancesystem.calendar.Database;
import domesticfinancesystem.shoppinglist.CreateShoppingList;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import domesticfinancesystem.*;
import javax.swing.text.AbstractDocument;

/**
 *
 * @author sneha
 */
public class RegularServicePanel extends javax.swing.JPanel {

    /**
     * Creates new form RegularServicePanel
     */
    private Date dt;
    private int servDefId;
    private MyTableModel tableModel;
    private Database dc;
    private Connection con;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat timeformatter = new SimpleDateFormat("h:mm a");
    private ArrayList<Item> arlItems = new ArrayList<>();
    private boolean isEditMode;
    private int regServId;
    private String paid;
    private int itemCount = 0;
    private Date time;
    private Window parentWindow;
    private boolean saved = false;
    private boolean changesMade = false;
    
    private class MyTableModel extends AbstractTableModel
    {
        final int COLS = 4;
        String[] colNames = {"Item","Price","Quantity","Amount"};
        Class[] colTypes = {String.class,Float.class,Float.class,Float.class} ;
        ArrayList<RegServDetails> arl = new ArrayList<RegServDetails>();

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
            RegServDetails rsd = (RegServDetails)arl.get(rowIndex);
            if(columnIndex == 0)
                return rsd.getItemName();
            else if(columnIndex == 1)
                return rsd.getPrice();
            else if(columnIndex == 2)
                return rsd.getQty();
            else if(columnIndex == 3)
            {
                return (float)(rsd.getAmt());
            }
            else
                return null;
        }
        
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            RegServDetails rsd = arl.get(rowIndex);
            if(columnIndex == 0)
            {
                rsd.setItemName((String)aValue);
            }
            else if(columnIndex == 1)
                rsd.setPrice((float)aValue);
            
            else if(columnIndex == 2)
                rsd.setQty((float)aValue);
            
                    else if(columnIndex == 3)
                rsd.setAmt((float)aValue);
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
        
        public void addRow(RegServDetails rsd)
        {
            arl.add(rsd);
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
    private boolean isServExists()
    {
        boolean isServExists = false;
        try {
            java.sql.Date sdt = new java.sql.Date(dt.getTime());
            
            String sql = "Select Id from RegServ where ServDefId = ? and Dttm = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, servDefId);
            pstmt.setDate(2, sdt);
            ResultSet rst = pstmt.executeQuery();
            
            if(rst.next())
                isServExists =  true;
            else
                isServExists = false;
            
            rst.close();
            pstmt.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(RegularServicePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isServExists;
    }
    private void setRecordDetails(RegServDetails rsd)
    {
        Item arlItem = null;
        for (int i = 0;i<arlItems.size();i++) {
            arlItem = arlItems.get(i);
            if(arlItem.getId() == rsd.getItemid() && arlItem.getItemName().equals(rsd.getItemName()))
                break;
        }
        cmbItem.setSelectedItem(arlItem);
        txtQty.setText(""+rsd.getQty());
        txtPrice.setText(""+rsd.getPrice());
    }
    public boolean isServiceSaved()
    {
        return saved;
    }
    
    public void addItemsFromDb()
    {
        ActionListener lsn = cmbItem.getActionListeners()[0] ;
        cmbItem.removeActionListener(lsn);
        
        cmbItem.addItem(new Item(-2,"Add New Item",0,0));
        try {
            
            String sql = "Select Id,ItemName,Price,DefaultQty from ServDefDetail where ServDefId = "+servDefId;
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            
            
            while(rst.next())
            {
                int id = rst.getInt(1);
                String name = rst.getString(2);
                float price = rst.getFloat(3);
                float qty = rst.getFloat(4);
                Item item = new Item(id, name,qty,price);
                cmbItem.addItem(item);
                arlItems.add(item);
                itemCount++;
            }
            rst.close();
            stmt.close();
            
            cmbItem.addActionListener(lsn);
            
            if(itemCount>0)
                cmbItem.setSelectedIndex(1);
        } catch (SQLException ex) {
            Logger.getLogger(RegularServicePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean areAllValid()
    {
        if(tableModel.arl.size() == 0)
        {
           JOptionPane.showMessageDialog(this,"No record entered", "Message",JOptionPane.ERROR_MESSAGE);
           cmbItem.requestFocus();
           return false;
        }
        return true;
    }
    
    public boolean checkValidity()
    {
        if(cmbItem.getSelectedIndex() == 0)
        {
           JOptionPane.showMessageDialog(this,"Item not selected", "Message",JOptionPane.ERROR_MESSAGE); 
           return false;
        }
        String qty = txtQty.getText().trim();
        if(qty == null || qty.isEmpty())
        {
          JOptionPane.showMessageDialog(this,"Quantity not entered", "Message",JOptionPane.ERROR_MESSAGE);
          txtQty.requestFocus();
          return false;
        }
        String price = txtPrice.getText().trim();
        if(price == null || price.isEmpty())
        {
          JOptionPane.showMessageDialog(this,"Price not entered", "Message",JOptionPane.ERROR_MESSAGE);
          txtPrice.requestFocus();
          return false;
        }
        return true;
    }
    
    public void getRecordsFromDatabase()
    {
        try {
            
            java.sql.Date sdt = new java.sql.Date(dt.getTime());
            String sql = "Select * from RegServ where ServDefId = ? and Dttm = ?";
            PreparedStatement pstmt =  con.prepareStatement(sql);
            pstmt.setInt(1,servDefId);
            pstmt.setDate(2,sdt);
            ResultSet rst = pstmt.executeQuery();
            boolean found = false;
            while(rst.next())
            {
               regServId = rst.getInt("Id");
               paid = rst.getString("Paid");
               time = rst.getDate("Ontime");
               lblTime.setText(timeformatter.format(time));
               found = true;
               isEditMode = true;
            }
            
            rst.close();
            pstmt.close();
            
            if(found)
            {
            
                sql = "Select * from RegServDetails where RegServId = ? order by SlNo";
                pstmt =  con.prepareStatement(sql);
                pstmt.setInt(1,regServId);
                rst = pstmt.executeQuery();
                while(rst.next())
                {
                    int id = rst.getInt("ServItemId");
                    float price = rst.getFloat("Price");
                    float qty = rst.getFloat("Qty");
                    
                    float amt = price * qty;
                    
                    String s = "Select ItemName from ServDefDetail where Id = ?";
                    PreparedStatement ps = con.prepareStatement(s);
                    ps.setInt(1, id);
                    ResultSet rs = ps.executeQuery();
                    
                    String itemName = "";
                    if(rs.next())
                        itemName = rs.getString(1);
                    
                    rs.close();
                    ps.close();
                    
                    RegServDetails rsd = new RegServDetails(id, price, qty, itemName, amt);
                    tableModel.addRow(rsd);
                    
                    int indx = tableModel.arl.indexOf(rsd);
                    tableModel.fireTableRowsInserted(indx, indx);
                }
                if(paid.equals("Y"))
                {
                    btnDeleteService.setEnabled(false);
                    btnAdd.setEnabled(false);
                    btnCancelService.setEnabled(false);
                    btnDelete.setEnabled(false);
                    btnReset.setEnabled(false);
                    btnSaveService.setEnabled(false);
                    btnEdit.setEnabled(false);
                }
                    
                rst.close();
                pstmt.close();
               calcTotal();
            }
                
        } catch (SQLException ex) {
            Logger.getLogger(ServiceDefinitionPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private Date getServiceStartDate()
    {
        Date strtdt = null;
        try {
            String sql = "Select StrtDate from ServDef where Id = ?";
            PreparedStatement pstmt =  con.prepareStatement(sql);
            pstmt.setInt(1,servDefId);
            ResultSet rst = pstmt.executeQuery();
            
            while(rst.next())
            {
                strtdt = rst.getDate(1);
            }
            rst.close();
            pstmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(RegularServicePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return strtdt;
    }
    
    private int noOfDays(Date startDate, Date endDate)
    {
//        System.out.println(""+endDate.getTime());
//        System.out.println(""+startDate.getTime());
        return (int)(Math.ceil((endDate.getTime() - startDate.getTime())/(double)(24 * 3600 * 1000)));
    }
    
    public boolean extractFreqData(Date startdate,int n)
    {
        int freqType  = (n)>>>30;
//        System.out.println("freq type = "+freqType);
        
        int mask = 0b00_111111_11111111_11111111_11111111 ; //to reset leftmost two bits of n
        n = n & mask;                      //resetting leftmost two bits of n

        if(freqType == 0)
        {
            if(n!=0)
            {
                if(n == 1)
                    return true;
                else if((noOfDays(startdate, dt) == 0))
                    return true;
                else if(noOfDays(startdate, dt)%n == 0)
                    return true;
                else
                    return false;
            }
            else
                return false;
        }
        else if(freqType == 1)
        {
            
            mask = 0b10000000 ; // 0x80 
            int dayOfWeek = dt.getDay();
            
             mask >>>= dayOfWeek ;
            
                 if((n & mask) != 0)   // Corresponding bit is set
                    return true;
                 else
                     return false;
        }
        else if(freqType == 2)
        {
            mask = 0b00_100000_00000000_00000000_00000000 ;
            int day = dt.getDate();
            mask >>>= day;
                 if((n & mask) != 0)   // Corresponding bit is set
                     return true;
                 else
                    return false;
        }
        else
        {
            if(dt.getDate() == 1)
            {
               mask = 0b10000000;     // ox80
               if( (n & mask)!= 0)
                   return true;
            }
            
            if(dt.getDate() == 30)
            {
               mask = 0b01000000;
               if( (n & mask)!= 0)
                   return true;
            }
           
           if(dt.getDate() == 15)
            {
               mask = 0b00100000;
               if( (n & mask)!= 0)
                   return true;
            }
           return false;
        }
    }
   
    private void fetchDefaultServRecs()
    {
        try {
            Date startDate = getServiceStartDate();
            
            String sql = "Select * from ServDefDetail where ServDefId = ?";
            PreparedStatement pstmt =  con.prepareStatement(sql);
            pstmt.setInt(1,servDefId);
            ResultSet rst = pstmt.executeQuery();
            
            while(rst.next())
            {
                int n = rst.getInt("Frequency");
                if(extractFreqData(startDate, n))
                {
                    int itemId = rst.getInt("Id");
                    String itemName = rst.getString("ItemName");
                    float price = rst.getFloat("Price");
                    float qty = rst.getFloat("DefaultQty");
                    float amt = price * qty;
                    RegServDetails rsd = new RegServDetails(itemId, price, qty, itemName, amt);
                    
                    tableModel.addRow(rsd);
                    
                    int indx = tableModel.arl.indexOf(rsd);
                    tableModel.fireTableRowsInserted(indx, indx);
                    
                    tabRegServItems.setRowSelectionInterval(indx, indx);

                }
            }
            rst.close();
            pstmt.close();
            calcTotal();
        } catch (SQLException ex) {
            Logger.getLogger(RegularServicePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    

    public RegularServicePanel(Date dt, int servDefId,Window window) {
        
        this();
        parentWindow = window;
        this.dt = dt;
        this.servDefId = servDefId;
               
        addItemsFromDb();

        lblDate.setText(formatter.format(dt));
        lblTime.setText(timeformatter.format(Calendar.getInstance().getTime()));
        getRecordsFromDatabase();
        
        if(tableModel.arl.size() == 0)
        {
            paid = "N";
            isEditMode = false;
            regServId = -1;
            fetchDefaultServRecs();
            
            Calendar cal = Calendar. getInstance();
            time = cal. getTime();
            lblTime.setText(timeformatter.format(time));
            
            btnDeleteService.setEnabled(false);
        }
        
        parentWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e); //To change body of generated methods, choose Tools | Templates.
                if(changesMade == true)
                {
                    int type = JOptionPane.showConfirmDialog(null,"Would you like to save the changes?", "Message",JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if(type == JOptionPane.NO_OPTION)
                    {
                        if(isServExists())
                           saved = true;
                        else
                            saved = false;
                        parentWindow.dispose();
                    }
                    else
                    {
                        ((JDialog)parentWindow).setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                        btnSaveService.requestFocus();
                    }
                }
                else 
                {
                    if(isServExists())
                           saved = true;
                    else
                           saved = false;
                    parentWindow.dispose();
                }
            }
          
       });
    }
    public void clearComponents()
    {
        if(itemCount > 0)
            cmbItem.setSelectedIndex(1);
        else
           cmbItem.setSelectedIndex(0);
        cmbItem.requestFocus();
        txtPrice.setText("");
        txtQty.setText("");
    }
    public void calcTotal()
    {
        float totalAmt = 0;
        for (RegServDetails rsd : tableModel.arl) {
                totalAmt += rsd.getAmt();
        }
        lblTotBal.setText(""+totalAmt);
    }
    
    private void deleteRegServRecs()
    {
        try {
            
            String sql = "Delete from RegServDetails where RegServId = "+regServId;
            Statement stmt = con.createStatement();
            stmt.executeQuery(sql);
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(RegularServicePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
            
    
    public RegularServicePanel() {
        initComponents();
        
        dc = new Database("jdbc:oracle:thin:@localhost:1521:XE","dfs","dfsboss","oracle.jdbc.OracleDriver");
        con = dc.createConnection();
        
        ((AbstractDocument)txtPrice.getDocument()).setDocumentFilter(new IntegerDocumentFilter());
        ((AbstractDocument)txtQty.getDocument()).setDocumentFilter(new IntegerDocumentFilter());
        
        lblTotBal.setText("0");
        
        cmbItem.setRenderer(new ItemComboBoxCellRenderer());
        
        
        tableModel = new MyTableModel();
        tabRegServItems.setModel(tableModel);
        tabRegServItems.setRowHeight(25);
        tabRegServItems.setShowGrid(true); //to show table border for each cell
        ((DefaultTableCellRenderer)tabRegServItems.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        
        CellRenderer cr = new CellRenderer();
        tabRegServItems.setDefaultRenderer(String.class, cr);
        tabRegServItems.setDefaultRenderer(Float.class, cr);
        
        tabRegServItems.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                int row = tabRegServItems.getSelectedRow() ;
                
                if(row != -1)
                {
                    clearComponents();
                    RegServDetails rsd = tableModel.arl.get(row);
                    setRecordDetails(rsd);
                }
                
            }
        });
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        lblDate = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblTime = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabRegServItems = new javax.swing.JTable();
        btnReset = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnDeleteService = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        lblTotBal = new javax.swing.JLabel();
        btnSaveService = new javax.swing.JButton();
        btnCancelService = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtQty = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtPrice = new javax.swing.JTextField();
        cmbItem = new javax.swing.JComboBox<>();
        btnEdit = new javax.swing.JButton();

        jLabel1.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel1.setText("Date");

        lblDate.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N

        jLabel3.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel3.setText("Time");

        lblTime.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N

        tabRegServItems.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N
        tabRegServItems.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tabRegServItems);

        btnReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/reset.png"))); // NOI18N
        btnReset.setToolTipText("Reset");
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

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

        btnDeleteService.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/delete.gif"))); // NOI18N
        btnDeleteService.setToolTipText("Delete Service");
        btnDeleteService.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteServiceActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        jLabel2.setText("Total Amount");

        lblTotBal.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblTotBal.setForeground(new java.awt.Color(204, 0, 0));
        lblTotBal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnSaveService.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/save.png"))); // NOI18N
        btnSaveService.setToolTipText("Save Service");
        btnSaveService.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveServiceActionPerformed(evt);
            }
        });

        btnCancelService.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/cancel.png"))); // NOI18N
        btnCancelService.setToolTipText("Cancel Service");
        btnCancelService.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelServiceActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel4.setText("Item Name");

        jLabel5.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel5.setText("Quantity");

        txtQty.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel6.setText("Price per piece");

        txtPrice.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N

        cmbItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbItemActionPerformed(evt);
            }
        });

        btnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/document_edit.png"))); // NOI18N
        btnEdit.setToolTipText("Edit");
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblTime, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(txtQty, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(cmbItem, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel2)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(btnReset)
                                        .addGap(25, 25, 25)
                                        .addComponent(btnAdd)
                                        .addGap(28, 28, 28)
                                        .addComponent(btnDelete))))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(195, 195, 195)
                                .addComponent(btnSaveService)
                                .addGap(32, 32, 32)
                                .addComponent(btnCancelService)
                                .addGap(32, 32, 32)
                                .addComponent(btnDeleteService)
                                .addGap(193, 193, 193)))
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTotBal, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(45, 45, 45))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDate, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTime, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnReset)
                    .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEdit)
                    .addComponent(cmbItem))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnCancelService)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtQty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(5, 5, 5)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                                .addComponent(btnSaveService)))
                        .addGap(67, 67, 67))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(78, 78, 78)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDeleteService)
                            .addComponent(lblTotBal, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        // TODO add your handling code here:
       clearComponents();
    }//GEN-LAST:event_btnResetActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
        int row = tabRegServItems.getSelectedRow() ;
        if(row != -1)
        {
            
            tableModel.arl.remove(row);
            clearComponents();
            tableModel.fireTableRowsDeleted(row, row);
            int rows = tabRegServItems.getRowCount() ;
            if(rows > 0)
            {
                if(row == rows)
                    row = rows - 1 ;
                tabRegServItems.setRowSelectionInterval(row, row);
                
            }
            changesMade = true;
            calcTotal();
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    
    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
        if(checkValidity())
        {
            Item item = ((Item)(cmbItem.getSelectedItem()));
            String itemName = item.getItemName();
            float price = Float.parseFloat(txtPrice.getText().trim());
            float qty = Float.parseFloat(txtQty.getText().trim());
//            float indPrice = price / qty;
            float amt = price * qty;
            int id = item.getId();
            
            RegServDetails rsd = new RegServDetails(id, price, qty, itemName, amt);
            tableModel.addRow(rsd);
            int row = tableModel.getRowCount() - 1;
            tableModel.fireTableRowsInserted(row, row);
            tabRegServItems.setRowSelectionInterval(row, row);
            
            changesMade = true;
           
            calcTotal();
        }
    }//GEN-LAST:event_btnAddActionPerformed
    public int getIdFromDual()
    {
        int id = -1;
        try {
            String sql = "Select seq.nextval from dual";
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            
            if(rst.next())
            {
                id = rst.getInt(1);
            }
            rst.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(CreateShoppingList.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }
    
    private String getAutoAddYN(int regServId)
    {
         String autoAdd = "";
        try {
            String sql = "Select AutoAddYN from RegServ where Id = "+regServId;
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
           
            if(rst.next())
            {
                autoAdd = rst.getString(1);
            }
            rst.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(RegularServicePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return autoAdd;
    }
    
    public void addRegularService()
    {
        try {
            
            dt = formatter.parse(lblDate.getText().trim());
            java.sql.Date sdt = new java.sql.Date(dt.getTime());
            java.sql.Date sTime = new java.sql.Date(time.getTime());
            
            int noOfSeconds = time.getHours()*3600 + time.getMinutes() * 60;
            
            String sql;
            int sId;
            String deletedYN = "N";
            String autoAddYN ;
            
            if(isEditMode == false)
            {
                sql = "Insert into RegServ(Id,ServDefId,Dttm,Paid,Ontime,NoOfSeconds,AutoAddYN,DeletedYN) ";
                sql+="values(?,?,?,?,?,?,?,?)";
                sId = getIdFromDual();
                regServId = sId;
                autoAddYN = "N";
            }
            else
            {
                sId = regServId;
                sql = "Update RegServ set Id = ?,ServDefId = ?,Dttm = ?,Paid = ?,Ontime = ?,NoOfSeconds = ?,AutoAddYN = ?,DeletedYN = ? where Id = "+sId;
                autoAddYN = getAutoAddYN(regServId);
            }
                PreparedStatement pstmt;
                pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, sId);
                pstmt.setInt(2, servDefId);
                pstmt.setDate(3,sdt);
                pstmt.setString(4,paid);
                pstmt.setDate(5,sTime);
                pstmt.setInt(6,noOfSeconds);
                pstmt.setString(7,autoAddYN);
                pstmt.setString(8,deletedYN);
                pstmt.executeUpdate();
                pstmt.close();  
            
                
                //Storing service items in database
                
                int count = 0;
                for (RegServDetails rsd : tableModel.arl) {
                    
                    int regServId = sId;
                    int slno = ++count;
                    int itemid = rsd.getItemid();
                    float qty = rsd.getQty();
                    float price = rsd.getPrice();
                    
                    if(rsd.getItemid() == -1)
                    {
                        rsd.setItemid(getIdFromDual());
                        
                        String s = "Insert into ServDefDetail(Id,ServDefId,ItemName,Price,DefaultQty,Frequency) values(?,?,?,?,?,?)";
                        
                        PreparedStatement pst = con.prepareStatement(s);
                        pst.setInt(1, rsd.getItemid());
                        pst.setInt(2, servDefId);
                        pst.setString(3,rsd.getItemName());
                        pst.setFloat(4,price);
                        pst.setFloat(5,qty);
                        pst.setInt(6,0);
                        pst.executeUpdate();
                        
                        pst.close(); 
                        
                        itemid = rsd.getItemid();
                    }
                   
                    
                    sql = "Insert into RegServDetails(RegServId,SlNo,ServItemId,Price,Qty) values(?,?,?,?,?)";
                    
                    pstmt = con.prepareStatement(sql);
                    pstmt.setInt(1, regServId);
                    pstmt.setInt(2, slno);
                    pstmt.setInt(3,itemid);
                    pstmt.setFloat(4,price);
                    pstmt.setFloat(5,qty);
                    pstmt.executeUpdate();
                    pstmt.close();  
                    
                    saved = true;
            }
                
            
        } catch (ParseException|SQLException ex) {
            Logger.getLogger(ServiceDefinitionPanel.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }
    private void btnSaveServiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveServiceActionPerformed
        // TODO add your handling code here:
        if(areAllValid())
        {
            
                try {
                            con.setAutoCommit(false);
                            deleteRegServRecs();
                            addRegularService();
                            con.commit();
                            con.setAutoCommit(true);
                            parentWindow.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(ServiceDefinitionPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }//GEN-LAST:event_btnSaveServiceActionPerformed

    private void cmbItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbItemActionPerformed
        // TODO add your handling code here:
             int index = cmbItem.getSelectedIndex();
             if(index == 0)
             {
                ElementDialog dlg = new ElementDialog(null, true,servDefId);//pass reference of parent frame later
                dlg.setVisible(true);
                Item item = dlg.getItem();
                dlg.dispose();
                if(item!=null)
                {
                   cmbItem.addItem(item);
                   cmbItem.setSelectedItem(item);
                   itemCount++;
                }
                else
                {
                    if(itemCount>0)
                    cmbItem.setSelectedIndex(1);
                }
             }
             else
             {
                 Item item = (Item)cmbItem.getSelectedItem();
                 txtPrice.setText(""+item.getPrice());
                 txtQty.setText(""+item.getQty());
             }
    }//GEN-LAST:event_cmbItemActionPerformed

    private void btnCancelServiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelServiceActionPerformed
        // TODO add your handling code here:
        if(regServId == -1)
            saved = false;
        else
            saved = true;
        parentWindow.dispose();
    }//GEN-LAST:event_btnCancelServiceActionPerformed

    private void btnDeleteServiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteServiceActionPerformed
        // TODO add your handling code here:
        try {
                  con = dc.createConnection();
                  int type = JOptionPane.showConfirmDialog(null,"Are you sure you want to delete the service?", "Message",JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if(type == JOptionPane.YES_OPTION)
                    {
                        con.setAutoCommit(false);
                        System.out.println("Hi we r in delete");
                        System.out.println("regServId = "+regServId);
                        String autoAdd = getAutoAddYN(regServId);
                        System.out.println("auto add YN = "+autoAdd);
                        if(autoAdd.equals("N"))
                        {
                            deleteRegServRecs();
                            String sql = "Delete from RegServ where Id = "+regServId;
                            Statement stmt = con.createStatement();
                            stmt.executeQuery(sql);
                            stmt.close();
                        }
                        else
                        {
                            String sql = "Update RegServ set DeletedYN = 'Y' where Id = "+regServId;
                            Statement stmt = con.createStatement();
                            stmt.executeQuery(sql);
                            stmt.close();
                        }
                        con.commit();
                        con.setAutoCommit(true);
                        saved = false;
                        parentWindow.dispose();
                    }
                    else
                    {
                        
                    }
            
        } catch (SQLException ex) {
            Logger.getLogger(RegularServicePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }//GEN-LAST:event_btnDeleteServiceActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // TODO add your handling code here:
        int row = tabRegServItems.getSelectedRow() ;
        if(row>=0)
        {
                if(checkValidity())
                {
                    String itemName = ((Item)(cmbItem.getSelectedItem())).getItemName();
                    tableModel.setValueAt(itemName, row, 0);

                    String p = txtPrice.getText();
                    float price = 0;
                    if(p!=null && !p.isEmpty())
                    {
                        price = Float.parseFloat(p);
                    }
                    tableModel.setValueAt(price, row, 1);

                    float qty = Float.parseFloat(txtQty.getText().trim());
                    tableModel.setValueAt(qty, row, 2);

                    float amt = qty * price;
                    tableModel.setValueAt(amt, row, 3);
                    
                    changesMade = true;
                    
                    calcTotal();

                }
        }            

    }//GEN-LAST:event_btnEditActionPerformed

    public static void main(String args[])
    {
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
            java.util.logging.Logger.getLogger(RegularServicePanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RegularServicePanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RegularServicePanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RegularServicePanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                JFrame  f = new JFrame();
                
                f.setTitle("Regular Service Panel");
                f.add(new RegularServicePanel(Calendar.getInstance().getTime(),27,null));
                f.pack();
                f.setLocationRelativeTo(null);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
                
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnCancelService;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnDeleteService;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSaveService;
    private javax.swing.JComboBox<Item> cmbItem;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTotBal;
    private javax.swing.JTable tabRegServItems;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtQty;
    // End of variables declaration//GEN-END:variables
}
