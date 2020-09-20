/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.shoppinglist;

import domesticfinancesystem.calendar.Database;
import domesticfinancesystem.exttrans.ExtTransData;
import domesticfinancesystem.exttrans.ExternalTransactionPanelNew;
import domesticfinancesystem.exttrans.PartyDetail;
import domesticfinancesystem.exttrans.TransDocs;
import domesticfinancesystem.inttrans.InternalTransactionPanel;
import domesticfinancesystem.periodicdeposit.PeriodicDeposit;
import domesticfinancesystem.wallet.Wallet;
import domesticfinancesystem.wallet.WalletDialog;
import java.awt.Component;
import java.awt.Image;
import java.awt.Window;
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
import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import domesticfinancesystem.*;
import domesticfinancesystem.calendar.CalendarPanel;
import domesticfinancesystem.exttrans.ExternalTransactionPanelNew;
import javax.swing.text.AbstractDocument;

/**
 *
 * @author sneha
 */
public class CreateShoppingList extends javax.swing.JPanel {

    /**
     * Creates new form CreateShoppingList
     */
    private Date date;
    private String title;
    private Database dc;
    private Connection con;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private ArrayList<Category> arlCategory = new ArrayList<>();
    private ArrayList<ListItem> arlListItems = new ArrayList<>();
    private ArrayList<UOM> arlUOMItems = new ArrayList<>();
    private boolean start;
    private int totalAmt;
    private MyTableModel tableModel ;
    private ShoppingListMainPanel sp;
    private boolean isEditMode = false;
    private int slistId = 0;
    private Window parentWindow;
    private boolean isExtTrans;
    private boolean isExtTransBeMade ;
    private boolean categoryAdded = false;
    
    private class MyTableModel extends AbstractTableModel
    {
        final int COLS = 7;
        String[] colNames = {"Checked","S.No","Item Name","Quantity","Total Price","UOM","Remarks"};
        Class[] colTypes = {Boolean.class,Integer.class,String.class,Integer.class,Float.class,String.class,String.class} ;
        ArrayList<ListRecord> arl = new ArrayList<ListRecord>();

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
            ListRecord lr = (ListRecord)arl.get(rowIndex);
            if(columnIndex == 0)
                 return lr.isCheckedYN();
            else if(columnIndex == 1)
                return rowIndex + 1 ;
            else if(columnIndex == 2)
                return lr.getItemName();
            else if(columnIndex == 3)
                return lr.getQty();
            else if(columnIndex == 4)
                return lr.getPrice();
            else if(columnIndex == 5)
                 return lr.getUomName();
            else
                return lr.getRemarks();
           
        }
        
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return columnIndex == 0 ;
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            ListRecord lr = arl.get(rowIndex);
            
            if(columnIndex == 0)
            {
                lr.setCheckedYN((boolean)aValue);
                calcTotal() ;
            }
            else if(columnIndex == 2)
                lr.setItemName((String)aValue);
            else if(columnIndex == 3)
                lr.setQty((int)aValue);
            else if(columnIndex == 4)
              lr.setPrice((Float)aValue);
            else if(columnIndex == 5)
                lr.setUomName((String)aValue);
            else if(columnIndex == 6)
               lr.setRemarks((String)aValue);
           
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
        
        public void addRow(ListRecord lr)
        {
            arl.add(lr);
        }
        
        public void setRowCount()
        {
            arl.clear();
            fireTableStructureChanged();
        }
    }
    
     public Date todayDate()
     {
        Date date = null;
        try {
            String d = formatter.format(Calendar.getInstance().getTime());
            date = formatter.parse(d);
        } catch (ParseException ex) {
            Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(Level.SEVERE, null, ex);
        }
        return date;
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
      
    public boolean isItemUnique(String name)
    {
        for (ListRecord li : tableModel.arl) {
            if(li.getItemName().equals(name))
            {
                return false;
            }
            
        }
        return true;
    }
    
    public boolean isItemUpdateUnique(String name,int slno)
    {
        for (int i = 0;i<tableModel.arl.size();i++) {
            ListRecord li = tableModel.arl.get(i);
            if((li.getItemName().equals(name)) &&  ((int)tableModel.getValueAt(i, 1)!= slno))
            {
                return false;
            }
            
        }
        return true;
    }
      
    public void calcTotal()
    {
        int totalAmt = 0;
        for (ListRecord lr : tableModel.arl) {
            if(lr.isCheckedYN())
            {
                totalAmt += lr.getPrice();
            }
        }
        lblTotAmt.setText(""+totalAmt);
    }
      
//    class CheckboxEditor extends AbstractCellEditor implements TableCellEditor
//    {
//        JCheckBox chkbox = new JCheckBox() ;
//        
//        @Override
//        public Object getCellEditorValue()
//        {
//            if(chkbox.isSelected())
//                return true;
//            else 
//                return false;
//        }
//
//        @Override
//        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
//        {
//            // boolean val = (boolean)value;
//             if(isSelected)
//             {
//                 chkbox.setSelected(true);
//                 ListRecord lr = tableModel.arl.get(row);
//                 lr.setCheckedYN(true);
//                 totalAmt+=lr.getPrice();
//                 lblTotAmt.setText(""+totalAmt);
//             }
//             else
//             {
//                 chkbox.setSelected(false);
//                 ListRecord lr = tableModel.arl.get(row);
//                 lr.setCheckedYN(false);
//                 totalAmt-=lr.getPrice();
//                 if(totalAmt < 0)
//                     totalAmt = 0;
//                 lblTotAmt.setText(""+totalAmt);
//                 
//             }
//             return chkbox ;
//        }
//        
//    }
//    
    public CreateShoppingList(ShoppingListMainPanel sp,Window f) //creating a fresh list
    {
        this();
        this.sp = sp;
        parentWindow = f;
        btnRemoveList.setEnabled(false);
        isEditMode = false;
        isExtTransBeMade = true;

    }
    public CreateShoppingList(ShoppingListMainPanel sp,Window f,int index) //creating a list from existing template
    {
        this();
        this.sp = sp;
        parentWindow = f;
        btnRemoveList.setEnabled(false);
        fetchListFromDatabase(index);
        calcTotal();
        txtTitle.setText("");
        txtTitle.requestFocus();
        isExtTransBeMade = true;
        isEditMode = false;
        categoryAdded = true;
    }
    
    public CreateShoppingList(int listIndex,ShoppingListMainPanel sp,Window f,boolean isView)
    {
        this();
        this.sp = sp;
        parentWindow = f;
        fetchListFromDatabase(listIndex);
        calcTotal();
        this.isEditMode = isView;
        slistId = listIndex;
        
            if(isExtTrans)
            {
                enableDisableComponents(false);
                isExtTransBeMade = false;
            }
            else
                isExtTransBeMade = true;
            
    }
    
    public void enableDisableComponents(boolean val)
    {
        cmbCategory.setEnabled(val);
        btnRemoveList.setEnabled(val);
        txtCategoryName.setEnabled(val);
        chkTemplateYN.setEnabled(val);
        btnAddCat.setEnabled(val);
        
        cmbItem.setEnabled(val);
        txtQuantity.setEnabled(val);
        cmbUOM.setEnabled(val);
        txtPrice.setEnabled(val);
        txtRemarks.setEnabled(val);
        
        btnReset.setEnabled(val);
        btnAddRecord.setEnabled(val);
        btnDelRecord.setEnabled(val);
        btnPrint.setEnabled(val);
        btnUpdate.setEnabled(val);
        btnSelectAll.setEnabled(val);
        btnRemoveSelect.setEnabled(val);
        btnUncheckSelectedItems.setEnabled(val);
        btnSelectedItems.setEnabled(val);
        
        chkTemplateYN.setEnabled(val);
        chkExtTrans.setEnabled(false);
        
    }
    
    public void extTransMade()
    {
        enableDisableComponents(false);
        btnCopyToNewList.setEnabled(true);
        txtTitle.setEnabled(true);
        isExtTransBeMade = false;
        
    }

    public void fetchListFromDatabase(int index)
    {
        try {
            String sql = "Select * from ShoppingList where Id = ?";
            PreparedStatement pstmt =  con.prepareStatement(sql);
            pstmt.setInt(1,index);
            ResultSet rst = pstmt.executeQuery();
            int catId = 0;
            while(rst.next())
            {
                String title = rst.getString("Name");
                txtTitle.setText(title);
                
                Date dt = rst.getDate("Dttm");
                lblDate.setText(formatter.format(dt));
                
                catId = rst.getInt("CatId");
                
                String s = rst.getString("TemplateYN");
                if(s.equals("Y"))
                    chkTemplateYN.setSelected(true);
                else
                    chkTemplateYN.setSelected(false);
                
                String transMade = rst.getString("TransMade");
                if(transMade.equals("Y"))
                {
                    chkExtTrans.setSelected(true);
                    isExtTrans = true;
                }
                else
                {
                    chkExtTrans.setSelected(false);
                    isExtTrans = false;
                }
            }
            rst.close();
            pstmt.close();
            
            //setting category name
            sql = "Select name from Category where Id = ?";
            pstmt =  con.prepareStatement(sql);
            pstmt.setInt(1,catId);
            rst = pstmt.executeQuery();
            while(rst.next())
            {
                String catName = rst.getString(1);
                cmbCategory.setSelectedItem(catName);
            }
            rst.close();
            pstmt.close();
            
            sql = "Select * from SListDetail where lstId = ?";
            pstmt =  con.prepareStatement(sql);
            pstmt.setInt(1,index);
            rst = pstmt.executeQuery();
            while(rst.next())
            {
                int slNo = rst.getInt("SlNo");
                int itemId = rst.getInt("ItemId");
                int uOMId = rst.getInt("UOMId");
                float qty = rst.getFloat("Qty");
                float price = rst.getFloat("Price");
                float indprice = price / qty;
                String checked = rst.getString("checkedYN");
                boolean check;
                if(checked.equals("Y"))
                    check = true;
                else
                    check = false;
                String remark = rst.getString("Remark");
                
                String s = "Select Name from ListItem where Id = ?";
                PreparedStatement pst =  con.prepareStatement(s);
                pst.setInt(1,itemId);
                ResultSet rs = pst.executeQuery();
                String itemName = "";
                while(rs.next())
                {
                    itemName = rs.getString(1);
                }
                rs.close();
                pst.close();
                
                s = "Select Name from UOM where Id = ?";
                pst =  con.prepareStatement(s);
                pst.setInt(1,uOMId);
                rs = pst.executeQuery();
                String uomName = "";
                while(rs.next())
                {
                    uomName = rs.getString(1);
                }
                rs.close();
                pst.close(); 
                
                ListRecord lr = new ListRecord(itemId, itemName, qty, price, remark, indprice, uOMId, uomName, check);
                tableModel.addRow(lr);
                
                int indx = tableModel.arl.indexOf(lr);
                tableModel.fireTableRowsInserted(indx, indx);
                
            }
            rst.close();
            pstmt.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(CreateShoppingList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public CreateShoppingList() {
        initComponents();
        dc = MainFrame.dc;
        
        ((AbstractDocument)txtPrice.getDocument()).setDocumentFilter(new IntegerDocumentFilter());
        ((AbstractDocument)txtQuantity.getDocument()).setDocumentFilter(new IntegerDocumentFilter());
       
        con = dc.createConnection();
        lblTotAmt.setText("0");
        tableModel = new MyTableModel();
        tabListItems.setModel(tableModel);
        tabListItems.setRowHeight(25);
        tabListItems.setShowGrid(true); //to show table border for each cell
        ((DefaultTableCellRenderer)tabListItems.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        CellRenderer cr = new CellRenderer();
        tabListItems.setDefaultRenderer(String.class, cr);
        tabListItems.setDefaultRenderer(Integer.class, cr);

        
        date = todayDate();
        lblDate.setText(""+formatter.format(date));
        addCategoryFromDb();
        
        start = true;
        addUOMItems();
        start = false;
        
        start = true;
        addItemsFromDb();
        start = false;
        
        int ind = cmbItem.getSelectedIndex();
        totalAmt = 0;
        
        tabListItems.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                int row = tabListItems.getSelectedRow() ;
                
                if(row != -1)
                {
                    ListRecord lr = tableModel.arl.get(row);
                    getListDetails(lr);
                    enabledDisabled(true);
                }
                
            }
        });
        
        txtQuantity.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                getPrice();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {                
               getPrice();

            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
               getPrice();

            }
        });
       
    }
    
    public void getPrice()
    {
        int itemIndex = cmbItem.getSelectedIndex();
        int uomIndex = cmbUOM.getSelectedIndex();
        String q = txtQuantity.getText().trim();
        if(itemIndex>0 && uomIndex > 0 && q!=null && !q.isEmpty())
        {
            try {
                String itemName = cmbItem.getItemAt(itemIndex);
                String uomName = cmbUOM.getItemAt(uomIndex);
                
                int itemId = 0;
                for (ListItem li : arlListItems) {
                    if(li.getName().equals(itemName))
                    {
                        itemId = li.getId();
                        break;
                    }
                }
                
                int uomId = 0;
                for (UOM uom : arlUOMItems) {
                    if(uom.getName().equals(uomName))
                    {
                        uomId = uom.getId();
                        break;
                    }
                }
                
                String sql = "Select price from ItemPrice where ItemId = ? and UOMId = ?";
                PreparedStatement pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, itemId);
                pstmt.setInt(2, uomId);
                ResultSet rst = pstmt.executeQuery();
                float price = 0;
                
                while(rst.next())
                {
                    price = rst.getFloat(1);
                }
                
                rst.close();
                pstmt.close();
               
                float qty = Float.parseFloat(q);
                float totPrice = qty * price;
                txtPrice.setText(""+totPrice);
                
            } catch (SQLException ex) {
                Logger.getLogger(CreateShoppingList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            txtPrice.setText("0");
        }
    }
    
    public void getListDetails(ListRecord lr)
    {
        cmbItem.setSelectedItem(lr.getItemName());
        txtQuantity.setText(""+lr.getQty());
        if(lr.getRemarks() != null && !lr.getRemarks().isEmpty())
              txtRemarks.setText(""+lr.getRemarks());
        else
            txtRemarks.setText("");
        cmbUOM.setSelectedItem(lr.getUomName());
        enabledDisabled(true);
        txtPrice.setText(""+lr.getPrice());
    }
    
    public boolean checkValidity()
    {
        String qty = txtQuantity.getText();
        if(qty == null || qty.isEmpty())
        {
          JOptionPane.showMessageDialog(this,"Quantity not entered", "Message",JOptionPane.ERROR_MESSAGE);
          txtQuantity.requestFocus();
          return false;
        }
        if(Float.parseFloat(txtQuantity.getText().trim()) == 0)
        {
          JOptionPane.showMessageDialog(this,"Quantity cannot be zero", "Message",JOptionPane.ERROR_MESSAGE);
          txtQuantity.requestFocus();
          return false;
        }
        String price = txtPrice.getText();
        if(price == null || price.isEmpty())
        {
          JOptionPane.showMessageDialog(this,"Price not entered", "Message",JOptionPane.ERROR_MESSAGE);
          txtPrice.requestFocus();
          return false; 
        }
        if(cmbItem.getSelectedIndex() == 0)
        {
           JOptionPane.showMessageDialog(this,"Item not selected", "Message",JOptionPane.ERROR_MESSAGE); 
           return false;
        }
        if(cmbUOM.getSelectedIndex() == 0)
        {
           JOptionPane.showMessageDialog(this,"Unit of measurement not selected", "Message",JOptionPane.ERROR_MESSAGE); 
           return false;
        }
        return true;
    }
    
    public void clearComponents()
    {
        txtQuantity.setText("");
        txtPrice.setText("");
        txtRemarks.setText("");
        start = true;
        
        if(arlListItems.size()>0)
            cmbItem.setSelectedIndex(1);
        else
           cmbItem.setSelectedIndex(0);
        
        if(arlUOMItems.size()>0)
            cmbUOM.setSelectedIndex(1);
        else
           cmbUOM.setSelectedIndex(0);

        start = false;
        
    }
    
    public void delShoppingList(int index)
    {
        try {
            //Delete records from SListDetail
            String sql = "Delete from SListDetail where lstId = ?";
            PreparedStatement pstmt =  con.prepareStatement(sql);
            pstmt.setInt(1,index);
            pstmt.executeUpdate();
            pstmt.close();
            
            //del shopping list
            sql = "Delete ShoppingList where Id = ?";
            pstmt =  con.prepareStatement(sql);
            pstmt.setInt(1,index);
            pstmt.executeUpdate();
            pstmt.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(CreateShoppingList.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
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
    public int addCategory(Category c)
    {
        String categoryName = c.getCategory();
        int catId = 0;
        PreparedStatement pstmt = null;
        
            try {
                catId = getIdFromDual();
                c.setId(catId);
                c.setIsPresent(true);
                
                //Adding new category to database
                String sql = "Insert into Category(Id,Name) ";
                sql+="values(?,?)";
                pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, catId);
                pstmt.setString(2, categoryName);
                pstmt.executeUpdate();
                pstmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(CreateShoppingList.class.getName()).log(Level.SEVERE, null, ex);
            }
            return catId;
    }
    
    public ShoppingListHeader addShoppingList()
    {
        ShoppingListHeader li = null;
        try {
                 String title = txtTitle.getText().trim();
                 String transMade = "";
        
                if(chkExtTrans.isSelected())
                    transMade = "Y";
                else
                    transMade = "N";
                
                String templateYN = "";
                
                if(chkTemplateYN.isSelected())
                    templateYN = "Y";
                else
                    templateYN = "N";
                
                
                int index = cmbCategory.getSelectedIndex();
                Category c = arlCategory.get(index);
                int catId ;
                if(c.isIsPresent() == false)//add category
                {
                    catId = addCategory(c);
                }
                else
                {
                    catId = c.getId();
                }
                
                //Adding Shopping list to database
                
                int sId;
                String sql;
//                if(isEditMode == false)
//                {
//                    sId = getIdFromDual();
//                    sql = "Insert into ShoppingList(Id,Dttm,NoOfSeconds,Name,CatId,TransMade,TemplateYN) ";
//                    sql+="values(?,?,?,?,?,?,?)";
//                }
//                else
//                {
//                    sId = slistId;
//                    sql = "Update ShoppingList set Id = ?,Dttm = ?,NoOfSeconds = ?,Name = ?,CatId = ?,TransMade = ?,TemplateYN = ? where Id = "+sId;
//                }

                if(isEditMode == true && isExtTrans == true)
                {
                    sId = slistId;
                    sql = "Update ShoppingList set Id = ?,Dttm = ?,NoOfSeconds = ?,Name = ?,CatId = ?,TransMade = ?,TemplateYN = ? where Id = "+sId;
                }
                else
                {
                    sId = getIdFromDual();
                    sql = "Insert into ShoppingList(Id,Dttm,NoOfSeconds,Name,CatId,TransMade,TemplateYN) ";
                    sql+="values(?,?,?,?,?,?,?)";
                }
                
                java.sql.Date sdt = new java.sql.Date(date.getTime());
               
                Calendar cd = Calendar.getInstance();
                int secs = cd.get(Calendar.HOUR_OF_DAY) * 3600 + cd.get(Calendar.MINUTE)*60 + cd.get(Calendar.SECOND);
                
                PreparedStatement pstmt;
                pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, sId);
                pstmt.setDate(2,sdt);
                pstmt.setInt(3,secs);
                pstmt.setString(4,title);
                pstmt.setInt(5,catId);
                pstmt.setString(6,transMade);
                pstmt.setString(7,templateYN);
                pstmt.executeUpdate();
                pstmt.close();
                
                li = new ShoppingListHeader(sId, title, sdt);
           
                if(!(isEditMode == true && isExtTrans == true))
                {
                int sno = 1 ;
                for (ListRecord lr : tableModel.arl) {
                    int itemId = lr.getItemId();
                    int uomId = lr.getUomId();
                    float qty = lr.getQty();
                    float price = lr.getPrice();
                    String remarks = lr.getRemarks();
                    
                    String isChecked = "";
                    
                    if(lr.isCheckedYN())
                        isChecked = "Y";
                    else
                        isChecked = "N";
                            
                    String remark = lr.getRemarks();
                         
                    
                    sql = "Insert into SListDetail(SlNo,lstId,ItemId,UOMId,Qty,price,checkedYN,Remark) ";
                    sql+="values(?,?,?,?,?,?,?,?)";
                    pstmt = con.prepareStatement(sql);
                    pstmt.setInt(1, sno);
                    pstmt.setInt(2, sId);
                    pstmt.setInt(3, itemId);
                    pstmt.setInt(4, uomId);
                    pstmt.setFloat(5, qty);
                    pstmt.setFloat(6, price);
                    pstmt.setString(7, isChecked);
                    pstmt.setString(8, remarks);
                    
                    pstmt.executeUpdate();
                    pstmt.close();

                    sno ++ ;
                    
                    //Make new item in ItemPrice
                    boolean found = false;
                    sql = "Select * from ItemPrice where ItemId = ? and UOMId = ?";
                    pstmt = con.prepareStatement(sql);
                    pstmt.setInt(1, itemId);
                    pstmt.setInt(2, uomId);
                    ResultSet rst = pstmt.executeQuery();
                    if(rst.next())
                    {
                        found = true;
                    }
                    rst.close();
                    pstmt.close();
                    
                    if(found == false)
                    {
                        float indPrice = lr.getIndPrice();
                        sql = "Insert into ItemPrice(ItemId,UOMId,Price,Dt) values(?,?,?,?)";
                        pstmt = con.prepareStatement(sql);
                        pstmt.setInt(1, itemId);
                        pstmt.setInt(2, uomId);
                        pstmt.setFloat(3, indPrice);
                        pstmt.setDate(4, sdt);
                        pstmt.executeUpdate();
                        pstmt.close();
                    }
                }
             }
                
            } catch (SQLException ex) {
                Logger.getLogger(CreateShoppingList.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        
        return li;
    }
    
    public void enabledDisabled(boolean val)
    {
        txtQuantity.setEnabled(val);
        txtPrice.setEnabled(val);
        txtRemarks.setEnabled(val);
        cmbItem.setEnabled(val);
        cmbUOM.setEnabled(val);
    }
    
    public void addCategoryFromDb()
    {
        try {
            String sql = "Select Id,Name from Category";
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            while(rst.next())
            {
                int id = rst.getInt(1);
                String name = rst.getString(2);
                Category c = new Category(id, name,true);
                arlCategory.add(c);
                cmbCategory.addItem(name);
            }
            rst.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(PeriodicDeposit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addItemsFromDb()
    {
        cmbItem.addItem("Add New Item...");
        try {
            String sql = "Select Id,Name from ListItem";
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            while(rst.next())
            {
                int id = rst.getInt(1);
                String name = rst.getString(2);
                ListItem li = new ListItem(id, name);
                cmbItem.addItem(name);
                arlListItems.add(li);
            }
            rst.close();
            stmt.close();
            if(arlListItems.size()>0)
                cmbItem.setSelectedIndex(1);
        } catch (SQLException ex) {
            Logger.getLogger(PeriodicDeposit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addUOMItems()
    {
       cmbUOM.addItem("Create new UOM...");
       try {
            String sql = "Select Id,Name from UOM";
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            while(rst.next())
            {
                int id = rst.getInt(1);
                String name = rst.getString(2);
                UOM um = new UOM(id, name);
                cmbUOM.addItem(name);
                arlUOMItems.add(um);
            }
            rst.close();
            stmt.close();
            if(arlUOMItems.size()>0)
                cmbUOM.setSelectedIndex(1);
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

        upperPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lblDate = new javax.swing.JLabel();
        btnRemoveList = new javax.swing.JButton();
        txtTitle = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        txtCategoryName = new javax.swing.JTextField();
        btnAddCat = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        cmbCategory = new javax.swing.JComboBox<>();
        Reset = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabListItems = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        cmbItem = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtRemarks = new javax.swing.JTextField();
        btnReset = new javax.swing.JButton();
        btnAddRecord = new javax.swing.JButton();
        btnDelRecord = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        lblTotAmt = new javax.swing.JLabel();
        btnSaveList = new javax.swing.JButton();
        chkExtTrans = new javax.swing.JCheckBox();
        txtPrice = new javax.swing.JTextField();
        txtQuantity = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        cmbUOM = new javax.swing.JComboBox<>();
        btnUpdate = new javax.swing.JButton();
        btnPrint = new javax.swing.JButton();
        btnSelectAll = new javax.swing.JButton();
        btnRemoveSelect = new javax.swing.JButton();
        btnSelectedItems = new javax.swing.JButton();
        btnUncheckSelectedItems = new javax.swing.JButton();
        btnCopyToNewList = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        chkTemplateYN = new javax.swing.JCheckBox();

        upperPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel1.setText("Date");

        jLabel2.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel2.setText("Title");

        lblDate.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        lblDate.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnRemoveList.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnRemoveList.setText("Remove List");
        btnRemoveList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveListActionPerformed(evt);
            }
        });

        txtTitle.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Category", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 102))); // NOI18N

        txtCategoryName.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N

        btnAddCat.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnAddCat.setText("Add Category");
        btnAddCat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddCatActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel3.setText("Select Category");

        cmbCategory.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        cmbCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCategoryActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txtCategoryName, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnAddCat))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCategoryName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddCat))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout upperPanelLayout = new javax.swing.GroupLayout(upperPanel);
        upperPanel.setLayout(upperPanelLayout);
        upperPanelLayout.setHorizontalGroup(
            upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upperPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(64, 64, 64)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(lblDate, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(btnRemoveList, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );
        upperPanelLayout.setVerticalGroup(
            upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upperPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(upperPanelLayout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 92, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(upperPanelLayout.createSequentialGroup()
                        .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(lblDate, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnRemoveList, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(0, 78, Short.MAX_VALUE))))
        );

        tabListItems.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jScrollPane1.setViewportView(tabListItems);

        jLabel4.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel4.setText("Item Name");

        cmbItem.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        cmbItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbItemActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel5.setText("Quantity");

        jLabel6.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel6.setText("Price");

        jLabel7.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel7.setText("Remarks");

        txtRemarks.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N

        btnReset.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/reset.png"))); // NOI18N
        btnReset.setToolTipText("Reset");
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        btnAddRecord.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnAddRecord.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/Add.gif"))); // NOI18N
        btnAddRecord.setToolTipText("Add Record");
        btnAddRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddRecordActionPerformed(evt);
            }
        });

        btnDelRecord.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnDelRecord.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/delete.gif"))); // NOI18N
        btnDelRecord.setToolTipText("Delete Record");
        btnDelRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelRecordActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        jLabel8.setText("Total Amount");

        lblTotAmt.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblTotAmt.setForeground(new java.awt.Color(255, 51, 51));
        lblTotAmt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotAmt.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnSaveList.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnSaveList.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/save.png"))); // NOI18N
        btnSaveList.setToolTipText("Save List");
        btnSaveList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveListActionPerformed(evt);
            }
        });

        chkExtTrans.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        chkExtTrans.setForeground(new java.awt.Color(0, 51, 255));
        chkExtTrans.setText("Make External Transaction");
        chkExtTrans.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkExtTransActionPerformed(evt);
            }
        });

        txtPrice.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        txtPrice.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        txtQuantity.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N

        jLabel9.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel9.setText("Unit of Measurement");

        cmbUOM.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        cmbUOM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbUOMActionPerformed(evt);
            }
        });

        btnUpdate.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/update.png"))); // NOI18N
        btnUpdate.setToolTipText("Update Record");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnPrint.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/Print.gif"))); // NOI18N
        btnPrint.setToolTipText("Print");

        btnSelectAll.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnSelectAll.setText("Select All");
        btnSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectAllActionPerformed(evt);
            }
        });

        btnRemoveSelect.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnRemoveSelect.setText("Remove Select");
        btnRemoveSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveSelectActionPerformed(evt);
            }
        });

        btnSelectedItems.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnSelectedItems.setText("Check Selected Items");
        btnSelectedItems.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectedItemsActionPerformed(evt);
            }
        });

        btnUncheckSelectedItems.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnUncheckSelectedItems.setText("Uncheck Selected Items");
        btnUncheckSelectedItems.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUncheckSelectedItemsActionPerformed(evt);
            }
        });

        btnCopyToNewList.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnCopyToNewList.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/copy.png"))); // NOI18N
        btnCopyToNewList.setToolTipText("Copy to New List");
        btnCopyToNewList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCopyToNewListActionPerformed(evt);
            }
        });

        btnCancel.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/cancel.png"))); // NOI18N
        btnCancel.setToolTipText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        chkTemplateYN.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        chkTemplateYN.setText("set as template");

        javax.swing.GroupLayout ResetLayout = new javax.swing.GroupLayout(Reset);
        Reset.setLayout(ResetLayout);
        ResetLayout.setHorizontalGroup(
            ResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ResetLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(ResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(ResetLayout.createSequentialGroup()
                        .addComponent(chkExtTrans)
                        .addGroup(ResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(ResetLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(chkTemplateYN, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnCancel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnSaveList)
                                .addGap(18, 18, 18)
                                .addComponent(btnCopyToNewList)
                                .addGap(33, 33, 33)
                                .addComponent(jLabel8)
                                .addGap(18, 18, 18)
                                .addComponent(lblTotAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(53, 53, 53))
                            .addGroup(ResetLayout.createSequentialGroup()
                                .addGap(258, 258, 258)
                                .addComponent(btnDelRecord)
                                .addGap(40, 40, 40)
                                .addComponent(btnPrint)
                                .addGap(30, 30, 30)
                                .addComponent(btnUpdate)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(ResetLayout.createSequentialGroup()
                        .addGroup(ResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(ResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(ResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(cmbUOM, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cmbItem, javax.swing.GroupLayout.Alignment.LEADING, 0, 128, Short.MAX_VALUE))
                            .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtRemarks, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(ResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ResetLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnSelectAll, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnRemoveSelect)
                                .addGap(18, 18, 18)
                                .addComponent(btnSelectedItems)
                                .addGap(18, 18, 18)
                                .addComponent(btnUncheckSelectedItems)
                                .addGap(51, 51, 51))
                            .addGroup(ResetLayout.createSequentialGroup()
                                .addGap(39, 39, 39)
                                .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(34, 34, 34)
                                .addComponent(btnAddRecord)
                                .addContainerGap())))))
            .addGroup(ResetLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 896, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        ResetLayout.setVerticalGroup(
            ResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ResetLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addGroup(ResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ResetLayout.createSequentialGroup()
                        .addGroup(ResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbItem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(ResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ResetLayout.createSequentialGroup()
                        .addGap(0, 27, Short.MAX_VALUE)
                        .addGroup(ResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnAddRecord)
                            .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDelRecord)
                            .addComponent(btnPrint)
                            .addComponent(btnUpdate))))
                .addGroup(ResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ResetLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(ResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbUOM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(ResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(ResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtRemarks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(ResetLayout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(ResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnSelectAll)
                            .addComponent(btnRemoveSelect)
                            .addComponent(btnSelectedItems)
                            .addComponent(btnUncheckSelectedItems))))
                .addGroup(ResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ResetLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addGroup(ResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(chkExtTrans, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(chkTemplateYN)))
                    .addGroup(ResetLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(ResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnCopyToNewList, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(ResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblTotAmt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnCancel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnSaveList, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(41, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(upperPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(Reset, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 897, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(upperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Reset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(17, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddCatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCatActionPerformed
        // TODO add your handling code here:
        String cat = txtCategoryName.getText();
        boolean found = false;
        if(cat!=null && !cat.isEmpty())
        {
           for (Category category : arlCategory) {
           if(category.getCategory().equals(cat))
           {
               found = true;
               break;
           }
        }
        if(found)
        {
          JOptionPane.showMessageDialog(this,"Category by this name already exists", "Message",JOptionPane.ERROR_MESSAGE);
          txtCategoryName.requestFocus();
        }
        else
        {
            arlCategory.add(new Category(0, cat, false));
            cmbCategory.addItem(cat);
            cmbCategory.setSelectedItem(cat);
            categoryAdded = true;
        }
        }
        else
        {
           JOptionPane.showMessageDialog(this,"Category not entered", "Message",JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAddCatActionPerformed

    private void cmbItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbItemActionPerformed
        if(start == false)
        {
            int index = cmbItem.getSelectedIndex();
             if(index == 0)
             {
                ElementDialog dlg = new ElementDialog(null, true,"Item Name",1);//pass reference of parent frame later
                dlg.setVisible(true);
                ListItem li = (ListItem)dlg.getOBject();
                dlg.dispose();
                if(li!=null)
                {
                   arlListItems.add(li);
                   cmbItem.addItem(li.getName());
                   cmbItem.setSelectedItem(li.getName());
                   txtPrice.setText("");
                   txtPrice.requestFocus();
                   txtQuantity.setText("");
                }
                else
                {
                    if(arlListItems.size()>0)
                    cmbItem.setSelectedIndex(1);
                }
             }
             else
             {
                try {
                String itemName = (String)cmbItem.getSelectedItem();
                int itemId = -1;
                for (ListItem li : arlListItems) {
                    if(li.getName().equals(itemName))
                    {
                        itemId = li.getId();
                        break;
                    }
                }
                int uomId = -1;
                String uomName = (String)cmbUOM.getSelectedItem();
                for (UOM uom : arlUOMItems) {
                    if(uom.getName().equals(uomName))
                    {
                        uomId = uom.getId();
                        break;
                    }
                }
                
                String sql = "Select price from ItemPrice where ItemId = ? and UOMId = ?";
                PreparedStatement pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, itemId);
                pstmt.setInt(2, uomId);
                ResultSet rst = pstmt.executeQuery();
                float price = 0;
                
                while(rst.next())
                {
                    price = rst.getFloat(1);
                }
                String q = txtQuantity.getText();
                if(q!= null && !q.isEmpty())
                {
                    float qty = Float.parseFloat(txtQuantity.getText());
                    float totPrice = qty * price;
                    txtPrice.setText(""+totPrice);
                }
                else
                {
                    txtPrice.setText("0");
                    
                }
                rst.close();
                pstmt.close();
                categoryAdded = true;

            } catch (SQLException ex) {
                Logger.getLogger(CreateShoppingList.class.getName()).log(Level.SEVERE, null, ex);
            }
            
             }
        }
    }//GEN-LAST:event_cmbItemActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        // TODO add your handling code here:
        enabledDisabled(true);
        clearComponents();
    }//GEN-LAST:event_btnResetActionPerformed

    private void btnAddRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRecordActionPerformed
        // TODO add your handling code here:
        if(checkValidity())
        {
           String itemName = (String)cmbItem.getSelectedItem();
           if(isItemUnique(itemName))
           {
           int itemId = 0;
            for (ListItem li : arlListItems) {
                if(itemName.equals(li.getName()))
                {
                    itemId = li.getId();
                    break;
                }
            }
            int uomId = 0;
            String uomName = (String)cmbUOM.getSelectedItem();
             for (UOM um : arlUOMItems) {
                if(uomName.equals(um.getName()))
                {
                    uomId = um.getId();
                    break;
                }
            }
            float qty = Float.parseFloat(txtQuantity.getText());
            float price = Float.parseFloat(txtPrice.getText());
            float pr = price / qty;
            String remarks = txtRemarks.getText();
            
            ListRecord lr = new ListRecord(itemId, itemName, qty, price, remarks,pr,uomId,uomName,true);
            tableModel.addRow(lr);
            int row = tableModel.getRowCount() - 1;
            tableModel.fireTableRowsInserted(row, row);
            tabListItems.setRowSelectionInterval(row, row);
            calcTotal();
           }
           else
           {
               JOptionPane.showMessageDialog(this,"Item by this name already exists in the list", "Message",JOptionPane.ERROR_MESSAGE);
               cmbItem.requestFocus();
           }
            
        }
    }//GEN-LAST:event_btnAddRecordActionPerformed

    private void btnDelRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelRecordActionPerformed
        // TODO add your handling code here:
        int row = tabListItems.getSelectedRow() ;
        if(row != -1)
        {
            
            tableModel.arl.remove(row);
            clearComponents();
            tableModel.fireTableRowsDeleted(row, row);
            int rows = tabListItems.getRowCount() ;
            if(rows > 0)
            {
                for (int i = 0; i < tableModel.arl.size(); i++) {
                    tableModel.setValueAt(i+1, i, 1);
                }
                
                if(row == rows)
                    row = rows - 1 ;
                tabListItems.setRowSelectionInterval(row, row);
                
            }
            calcTotal();
        }
            
    }//GEN-LAST:event_btnDelRecordActionPerformed

    public boolean areAllValid()
    {
        String title = txtTitle.getText().trim();
        if(title.isEmpty())
        {
            JOptionPane.showMessageDialog(this,"Title not entered", "Message",JOptionPane.ERROR_MESSAGE);
            txtTitle.requestFocus();
            return false;
        }
        if(tableModel.arl.size() == 0)
        {
           JOptionPane.showMessageDialog(this,"No record entered", "Message",JOptionPane.ERROR_MESSAGE);
           cmbItem.requestFocus();
           return false;
        }
        if(isUnique("ShoppingList", "Name", title, isEditMode, slistId) == false)
        {
           JOptionPane.showMessageDialog(this,"ShoppingList by this name already exists.Please enter another name", "Message",JOptionPane.ERROR_MESSAGE);
           txtTitle.requestFocus();
           return false;
        }
        if(categoryAdded == false && isEditMode == false)
        {
           JOptionPane.showMessageDialog(this,"Please add category", "Message",JOptionPane.ERROR_MESSAGE);
           btnAddCat.requestFocus();
           return false;
        }
        
        return true;
    }
    
    public boolean isUnique(String table,String fieldName,String fieldValue,boolean isEditMode,int id)
    {
        boolean found = false ;
        int dbId = -1;
        try {
            String sql = "Select id from " + table + " where " + fieldName + " = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, fieldValue);
            ResultSet rst = pstmt.executeQuery();
            found = rst.next() ;
            if(found)
               dbId = rst.getInt(1);
            rst.close();
            pstmt.close();
                        
            
        } catch (SQLException ex) {
            Logger.getLogger(CreateShoppingList.class.getName()).log(Level.SEVERE, null, ex);
        }
        return  !(found && (!isEditMode || id!=dbId));
    }
    
    public boolean isUnique(String fieldValue,boolean isEditMode,int id)
    {
        boolean found = false ;
        int dbId = -1;
        try {
            String sql = "Select id from ShoppingList where Name = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, fieldValue);
            ResultSet rst = pstmt.executeQuery();
//            found = rst.next() ;
//            dbId = rst.getInt(1);
            if(rst.next())
            {
                found = true;
                dbId = rst.getInt(1);
            }
            rst.close();
            pstmt.close();
                        
            
        } catch (SQLException ex) {
            Logger.getLogger(CreateShoppingList.class.getName()).log(Level.SEVERE, null, ex);
        }
        return  !(found && (!isEditMode || id!=dbId));
    }
    
    private void btnSaveListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveListActionPerformed
        ShoppingListHeader li = null;
        if(areAllValid())
        {
                try {
                if(isExtTransBeMade == false || chkExtTrans.isSelected() == false)//if no external transaction has been made
                {
                    if(isEditMode == true)//editing existing list
                    {
                            con.setAutoCommit(false);
                            delShoppingList(slistId);
                            li = addShoppingList();
                            con.commit();
                            con.setAutoCommit(true);

                            if(chkTemplateYN.isSelected())
                                sp.updateList(slistId,li,true);
                            else
                                sp.updateList(slistId,li,false);
                    }
                    else//adding new list
                    {
                            con.setAutoCommit(false);
                            li = addShoppingList();
                            slistId = li.getId();
                            con.commit();
                            con.setAutoCommit(true);

                                if(chkTemplateYN.isSelected())
                                    sp.addList(li,true);
                                else
                                    sp.addList(li,false);
                    }
                   parentWindow.dispose() ;
                }
                else //external transaction has  been made
                {    
                        JFrame f = null;
                        JDialog dlg= new JDialog(f,true);
                        ExternalTransactionPanelNew etp = new ExternalTransactionPanelNew(txtTitle.getText().trim(), Float.parseFloat(lblTotAmt.getText()),slistId,dlg);
                        dlg.add(etp);
                        dlg.setTitle("External Transaction");
                        dlg.pack();
                        dlg.setLocationRelativeTo(null);
                        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                        
                        dlg.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowOpened(WindowEvent e) {
                                super.windowOpened(e); //To change body of generated methods, choose Tools | Templates.
                                JOptionPane.showMessageDialog(dlg, "To make external transaction save row","Message", JOptionPane.INFORMATION_MESSAGE);

                            }
                             });
                        
                        
                        dlg.setVisible(true);
                        
                        if(etp.isExtTransMade())
                        {
                                if(isEditMode == true)//editing existing list
                                {
                                        con.setAutoCommit(false);
                                        delShoppingList(slistId);
                                        li = addShoppingList();
                                        con.commit();
                                        con.setAutoCommit(true);

                                        if(chkTemplateYN.isSelected())
                                            sp.updateList(slistId,li,true);
                                        else
                                            sp.updateList(slistId,li,false);
                                }
                                else//adding new list
                                {
                                        con.setAutoCommit(false);
                                        li = addShoppingList();
                                        slistId = li.getId();
                                        con.commit();
                                        con.setAutoCommit(true);

                                            if(chkTemplateYN.isSelected())
                                                sp.addList(li,true);
                                            else
                                                sp.addList(li,false);
                                }
                                              parentWindow.dispose() ;
                         }
                         else
                            btnSaveList.requestFocus();
                
                }

                } catch (SQLException ex) 
                {
                        Logger.getLogger(CreateShoppingList.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
           
    }//GEN-LAST:event_btnSaveListActionPerformed

    private void chkExtTransActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkExtTransActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chkExtTransActionPerformed

    private void cmbUOMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbUOMActionPerformed
        // TODO add your handling code here:
        if(start == false)
        {
        int index = cmbUOM.getSelectedIndex();
         if(index == 0)
         {
            ElementDialog dlg = new ElementDialog(null, true,"Unit of Measurement",0);//pass reference of parent frame later
            dlg.setVisible(true);
            UOM um = (UOM)dlg.getOBject();
            dlg.dispose();
            if(um!=null)
            {
               arlUOMItems.add(um);
               cmbUOM.addItem(um.getName());
               cmbUOM.setSelectedItem(um.getName());
               txtPrice.setText("");
               txtPrice.requestFocus();
               txtQuantity.setText("");
            }
            else
            {
                if(arlUOMItems.size()>0)
                    cmbUOM.setSelectedIndex(1);
            }
         }
         else
         {
            try {
                String itemName = (String)cmbItem.getSelectedItem();
                int itemId = 0;
                for (ListItem li : arlListItems) {
                    if(li.getName().equals(itemName))
                    {
                        itemId = li.getId();
                        break;
                    }
                }
                int uomId = 0;
                String uomName = (String)cmbUOM.getSelectedItem();
                for (UOM uom : arlUOMItems) {
                    if(uom.getName().equals(uomName))
                    {
                        uomId = uom.getId();
                        break;
                    }
                }
                
                String sql = "Select price from ItemPrice where ItemId = ? and UOMId = ?";
                PreparedStatement pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, itemId);
                pstmt.setInt(2, uomId);
                ResultSet rst = pstmt.executeQuery();
                float price = 0;
                while(rst.next())
                {
                    price = rst.getFloat(1);
                }
                String q = txtQuantity.getText();
                if(q!= null && !q.isEmpty())
                {
                    float qty = Float.parseFloat(q);
                    float totPrice = qty * price;
                    txtPrice.setText(""+totPrice);
                }
                else
                {
                    txtPrice.setText("0");
                }
                rst.close();
                pstmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(CreateShoppingList.class.getName()).log(Level.SEVERE, null, ex);
            }
             
         }
         
        }
    }//GEN-LAST:event_cmbUOMActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        // TODO add your handling code here:
        int row = tabListItems.getSelectedRow() ;
        if(row>=0)
        {
                ListRecord lr = tableModel.arl.get(row);
                float price = 0;

                 String itemName = (String)cmbItem.getSelectedItem();

                 if(checkValidity() && isItemUpdateUnique(itemName, (int)tableModel.getValueAt(tabListItems.getSelectedRow(),1)))
                 {
                        lr.setItemName(itemName);
                        tableModel.setValueAt(itemName, row, 2);

                           int itemId = 0;
                              for (ListItem li : arlListItems) {
                                  if(itemName.equals(li.getName()))
                                  {
                                      itemId = li.getId();
                                      break;
                                  }
                              }
                           lr.setItemId(itemId);

                           float qty = Float.parseFloat(txtQuantity.getText());
                           lr.setQty(qty);
                           tableModel.setValueAt((int)qty, row, 3);

                           float pr = Float.parseFloat(txtPrice.getText());
                           lr.setPrice(pr);
                           tableModel.setValueAt(pr, row, 4);

                           float prc = pr / qty;
                           lr.setIndPrice(prc);

                           String uomName = (String)cmbUOM.getSelectedItem();
                           lr.setUomName(uomName);
                           tableModel.setValueAt((String)uomName, row, 5);

                           int uomId = 0;
                           for (UOM um : arlUOMItems) {
                               if(uomName.equals(um.getName()))
                               {
                                   uomId = um.getId();
                                   break;
                               }
                           }
                           lr.setUomId(uomId);

                           String remarks = txtRemarks.getText();
                           lr.setRemarks(remarks);
                           tableModel.setValueAt((String)remarks, row, 6);

                           calcTotal();
                 }
        }
        
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void cmbCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCategoryActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbCategoryActionPerformed

    private void btnSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectAllActionPerformed
        // TODO add your handling code here:
        for (int i = 0; i < tableModel.arl.size(); i++) {
            ListRecord lr = tableModel.arl.get(i);
            tableModel.setValueAt(true, i , 0);
        }
        tableModel.fireTableDataChanged();
    }//GEN-LAST:event_btnSelectAllActionPerformed

    private void btnRemoveSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveSelectActionPerformed
        // TODO add your handling code here:
         for (int i = 0; i < tableModel.arl.size(); i++) {
            ListRecord lr = tableModel.arl.get(i);
            tableModel.setValueAt(false, i , 0);
        }
        tableModel.fireTableDataChanged();
    }//GEN-LAST:event_btnRemoveSelectActionPerformed

    private void btnSelectedItemsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectedItemsActionPerformed
        // TODO add your handling code here:
        totalAmt = 0;
        int[] indices = tabListItems.getSelectedRows();
        for (int indice : indices) {
            tableModel.setValueAt(true, indice, 0);
        }
        tableModel.fireTableDataChanged();
    }//GEN-LAST:event_btnSelectedItemsActionPerformed

    private void btnUncheckSelectedItemsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUncheckSelectedItemsActionPerformed
        // TODO add your handling code here:
        int[] indices = tabListItems.getSelectedRows();
        for (int indice : indices) {
           tableModel.setValueAt(false, indice, 0);
        }
        tableModel.fireTableDataChanged();

    }//GEN-LAST:event_btnUncheckSelectedItemsActionPerformed

    private void btnRemoveListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveListActionPerformed
        try {
            
            int type = JOptionPane.showConfirmDialog(this,"Are you sure you want to delete the list?", "Message",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(type == JOptionPane.YES_OPTION)
            {
                con.setAutoCommit(false);
                delShoppingList(slistId);
                con.commit();
                con.setAutoCommit(true);

                if(chkTemplateYN.isSelected())
                    sp.removeList(slistId,true);
                else
                   sp.removeList(slistId,false); 

                parentWindow.dispose() ;
            }

            
        } catch (SQLException ex) {
            Logger.getLogger(CreateShoppingList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnRemoveListActionPerformed

    private void btnCopyToNewListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCopyToNewListActionPerformed
        // TODO add your handling code here:
        date = todayDate();
        lblDate.setText(""+formatter.format(date));
        enableDisableComponents(true);
        txtTitle.setText("");
        txtTitle.requestFocus();
        chkExtTrans.setSelected(false);
        chkExtTrans.setEnabled(true);
        chkTemplateYN.setSelected(false);
        isEditMode = false;
        isExtTransBeMade = true;
        btnCopyToNewList.setEnabled(false);
        btnRemoveList.setEnabled(false);
        categoryAdded = true;
    }//GEN-LAST:event_btnCopyToNewListActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        // TODO add your handling code here:
       parentWindow.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

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
            java.util.logging.Logger.getLogger(CalendarPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(CalendarPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(CalendarPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(CalendarPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                JFrame  f = new JFrame();
                
                f.setTitle("Create Shpping List");
                f.add(new CreateShoppingList());
                f.pack();
                f.setLocationRelativeTo(null);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
                
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Reset;
    private javax.swing.JButton btnAddCat;
    private javax.swing.JButton btnAddRecord;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnCopyToNewList;
    private javax.swing.JButton btnDelRecord;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnRemoveList;
    private javax.swing.JButton btnRemoveSelect;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSaveList;
    private javax.swing.JButton btnSelectAll;
    private javax.swing.JButton btnSelectedItems;
    private javax.swing.JButton btnUncheckSelectedItems;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JCheckBox chkExtTrans;
    private javax.swing.JCheckBox chkTemplateYN;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JComboBox<String> cmbItem;
    private javax.swing.JComboBox<String> cmbUOM;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblTotAmt;
    private javax.swing.JTable tabListItems;
    private javax.swing.JTextField txtCategoryName;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtQuantity;
    private javax.swing.JTextField txtRemarks;
    private javax.swing.JTextField txtTitle;
    private javax.swing.JPanel upperPanel;
    // End of variables declaration//GEN-END:variables
}
