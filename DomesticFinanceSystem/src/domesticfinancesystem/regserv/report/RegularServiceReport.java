/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.regserv.report;

import domesticfinancesystem.MainFrame;
import domesticfinancesystem.calendar.Database;
import domesticfinancesystem.exttrans.ExternalTransactionPanelNew;
import domesticfinancesystem.calendar.DatePickerNewDialog;
import domesticfinancesystem.shoppinglist.ListRecord;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractCellEditor;
import javax.swing.CellRendererPane;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author sneha
 */
public class RegularServiceReport extends javax.swing.JPanel {

    /**
     * Creates new form RegularServiceReport
     */
    
    private int servId;
    private int walletId;
    private MyTableModel tableModel ;
    private Database dc;
    private Connection con;
    private Date fromDate;
    private Date toDate;
    private boolean isReverseSelect;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private Font font;
    private ArrayList<Integer> arlSelectedIds = new ArrayList<>();
    
    private class MyTableModel extends AbstractTableModel
    {
        final int COLS = 6;
        String[] colNames = {"Paid","Date","Item Name","Price","Quantity","Amount"};
        Class[] colTypes = {Boolean.class,Date.class,String.class,Float.class,Float.class,Float.class} ;
        ArrayList<RegServRecs> arl = new ArrayList<RegServRecs>();

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
            RegServRecs rsr = (RegServRecs)arl.get(rowIndex);
            if(columnIndex == 0)
                 return rsr.isIsPaid();
            else if(columnIndex == 1)
                return rsr.getDate();
            else if(columnIndex == 2)
                return rsr.getItemName();
            else if(columnIndex == 3)
                return rsr.getPrice();
            else if(columnIndex == 4)
                return rsr.getQty();
            else 
                return rsr.getAmount();
           
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return columnIndex == 0 && tableModel.arl.get(rowIndex).isIsPayEditable() == true;
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            RegServRecs rsr = arl.get(rowIndex);
            if(columnIndex == 0)
            {
               boolean val = (boolean)aValue;
               
               if(rsr.isIsPayEditable())
                     rsr.setIsPaid(val);
               
               
                if(rsr.isIsPayEditable() && isReverseSelect == false)
                {
                    for (int i = 0; i < tableModel.arl.size(); i++) {
                       RegServRecs rs = arl.get(i);
                       if(rs.getDate().compareTo(rsr.getDate()) == 0 && rs.isIsPayEditable())
                       {
                           rs.setIsPaid(val);
                           tableModel.fireTableCellUpdated(i, 0);
                       }
                    }
                }
                calcSelectedUnpaid();
            }
            else if(columnIndex == 1)
                rsr.setDate((Date)aValue);
            else if(columnIndex == 2)
                rsr.setItemName((String)aValue);
            else if(columnIndex == 3)
                rsr.setPrice((Float)aValue);
            else if(columnIndex == 4)
              rsr.setQty((Float)aValue);
            else if(columnIndex == 5)
                rsr.setAmount((Float)aValue);
           
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
        
        public void addRow(RegServRecs rsr)
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
    
    class ColorCellRenderer extends DefaultTableCellRenderer
    {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
           super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); //To change body of generated methods, choose Tools | Templates.

            RegServRecs rsr = tableModel.arl.get(row);
            if(rsr.isIsPayEditable())
                this.setForeground(Color.red);
            else
              this.setForeground(new Color(0, 100, 0));
            
//            this.setFont(font);
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
        
         @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
           super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); //To change body of generated methods, choose Tools | Templates.

            RegServRecs rsr = tableModel.arl.get(row);
            if(rsr.isIsPayEditable())
                this.setForeground(Color.red);
            else
              this.setForeground(new Color(0, 100, 0));
            
//            this.setFont(font);
            return this;
        
        }
        
    }
    
    
    class PayRenderer extends DefaultTableCellRenderer
    {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
           
            JCheckBox checkBox = new JCheckBox();
            checkBox.setHorizontalAlignment(SwingConstants.CENTER);
            checkBox.setBackground(Color.white);
            
            RegServRecs rsr = tableModel.arl.get(row);
            
           boolean val = rsr.isIsPayEditable();
           
           if(val == false)
           {
               checkBox.setSelected(true);
           }
           else
           {
              
               if((boolean)value == true)
                 checkBox.setSelected(true);
           }
           checkBox.setEnabled(val);
           
           return checkBox;
        
        }
        
    }
    
    private void getSelectedIds()
    {
        arlSelectedIds.clear();
        for (int i = 0; i < tableModel.arl.size(); i++) {
            RegServRecs rsr = tableModel.arl.get(i);
            if(rsr.isIsPaid() == true && rsr.isIsPayEditable() == true)
            {
                arlSelectedIds.add(rsr.getId());
                rsr.setIsPayEditable(false);
                tableModel.setValueAt(true, i, 0);
            }
        }
        tableModel.fireTableDataChanged();
   
    }
    
    private void updateRegularService()
    {
        
        for (Integer arlSelectedId : arlSelectedIds) {
            
            
            try {
                
                String sql = "Update RegServ set Paid = ? where id = ?";
                PreparedStatement pstmt =  con.prepareStatement(sql);
                pstmt.setString(1,"Y");
                pstmt.setInt(2,arlSelectedId);
                ResultSet rst = pstmt.executeQuery();
                rst.close();
                
            } catch (SQLException ex) {
                Logger.getLogger(RegularServiceReport.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        calcTotalUnpaid();
        calcSelectedUnpaid();
    }
    
    public RegularServiceReport() {
        initComponents();
        
        isReverseSelect = false;
        
        dc = MainFrame.dc;
        con = dc.createConnection();
        tableModel = new MyTableModel();
        
        tabRegServ.setSelectionModel(new DefaultListSelectionModel(){
            @Override
            public boolean isSelectedIndex(int index) 
            { 
                if(tableModel.arl.get(index).isIsPayEditable() == false)
                    return false;
                else
                    return super.isSelectedIndex(index);
            }
          
        }
        );
        
//        font = new Font("Tahoma 11 Plain", Font.BOLD, 12);
        
        tabRegServ.setModel(tableModel);
        tabRegServ.setRowHeight(25);
        tabRegServ.setShowGrid(true); //to show table border for each cell
        ((DefaultTableCellRenderer)tabRegServ.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        CellRenderer cr = new CellRenderer();
        tabRegServ.setDefaultRenderer(String.class, cr);
        tabRegServ.setDefaultRenderer(Integer.class, cr);
        
        //Checkbox renderer
        PayRenderer dr = new PayRenderer();
        tabRegServ.setDefaultRenderer(Boolean.class, dr);
        
        ColorCellRenderer ccr = new ColorCellRenderer();
        tabRegServ.setDefaultRenderer(String.class, ccr);
        tabRegServ.setDefaultRenderer(Float.class, ccr);
        tabRegServ.setDefaultRenderer(Date.class, ccr);
        
        tabRegServ.setDefaultRenderer(Date.class, new DateRenderer());

    }

    public RegularServiceReport(int servId) {
        this();
        this.servId = servId;
        
        toDate = new GregorianCalendar().getTime();
        lblToDate.setText(formatter.format(toDate));
        
        radAll.setSelected(true);
        
        setServiceInfo();
        setServiceRecords();
        
        calcTotalUnpaid();
        calcSelectedUnpaid();
    }
    
    private void setServiceInfo()
    {
        try {
            String sql = "Select Name,StrtDate,WalletId from ServDef where Id = ?";
            PreparedStatement pstmt =  con.prepareStatement(sql);
            pstmt.setInt(1,servId);
            ResultSet rst = pstmt.executeQuery();
            
            if(rst.next())
            {
                String name = rst.getString(1);
                lblServiceName.setText(name);
                
                Date date = rst.getDate(2);
                fromDate = date;
                lblFromDate.setText(formatter.format(date));
                
                walletId = rst.getInt(3);
            }
            
            rst.close();
            pstmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(RegularServiceReport.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void calcTotalUnpaid()
    {
        int totalAmt = 0;
        for (RegServRecs rsr : tableModel.arl) {
            if(rsr.isIsPaid() == false)
            {
                totalAmt += rsr.getAmount();
            }
        }
        lblTotalUnpaid.setText(""+totalAmt);
    }
    
    public void calcSelectedUnpaid()
    {
        int totalAmt = 0;
        for (RegServRecs rsr : tableModel.arl) {
            if(rsr.isIsPaid() == true && rsr.isIsPayEditable() == true)
            {
                totalAmt += rsr.getAmount();
            }
        }
        lblTotalSelectedUnpaid.setText(""+totalAmt);
    }

    
    private void setServiceRecords()
    {
        try {
            tableModel.arl.clear();
            
            java.sql.Date fdt = new java.sql.Date(fromDate.getTime());
            java.sql.Date tdt = new java.sql.Date(toDate.getTime());
            
            String pay = "";
            
            if(radOnlyPaid.isSelected())
            {
                pay = " and Paid = \'Y\'";
            }
            else if(radOnlyUnpaid.isSelected())
            {
               pay = " and Paid = \'N\'"; 
            }
            
            String sql = "Select Id,Dttm,Paid from RegServ where ServDefId = ? and Dttm between ? and ?"+pay+" order by Dttm asc";
            PreparedStatement pstmt =  con.prepareStatement(sql);
            pstmt.setInt(1,servId);
            pstmt.setDate(2,fdt);
            pstmt.setDate(3,tdt);
            ResultSet rst = pstmt.executeQuery();
            
            while(rst.next())
            {
                int id = rst.getInt(1);
                Date date = rst.getDate(2);
                String paid = rst.getString(3);
                
                
                boolean isPaid;
                boolean isPayEditable;
                
                if(paid.equals("Y"))
                {
                    isPaid = true;
                    isPayEditable = false;
                }    
                else
                {
                    isPaid = false;
                    isPayEditable = true;
                }
                
                 String s = "Select s.ItemName,r.price,r.qty from ServDefDetail s,RegServDetails r where r.RegServId = ? and r.ServItemId = s.Id";
                 PreparedStatement pst =  con.prepareStatement(s);
                 pst.setInt(1,id);
                 ResultSet rs = pst.executeQuery();
                 while(rs.next())
                 {
                     String itemName = rs.getString(1);
                     float price = rs.getFloat(2);
                     float qty = rs.getFloat(3);
                     float amount = price * qty;
                     
                     RegServRecs rsr = new RegServRecs(id,isPaid, date, itemName, price, qty, amount,isPayEditable);
                     tableModel.addRow(rsr);
                     int row = tableModel.getRowCount() - 1;
                     tableModel.fireTableRowsInserted(row, row);
                     tabRegServ.setRowSelectionInterval(row, row);
                     
                 }
                 rs.close();
                 pst.close();
            }
            
            rst.close();
            pstmt.close();
            
            calcSelectedUnpaid();
            calcTotalUnpaid();
            
        } catch (SQLException ex) {
            Logger.getLogger(RegularServiceReport.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
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
            java.util.logging.Logger.getLogger(RegularServiceReport.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(RegularServiceReport.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(RegularServiceReport.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(RegularServiceReport.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                JFrame  f = new JFrame();
                
                f.setTitle("Regular Service Report");
                f.add(new RegularServiceReport(68));
                f.pack();
                f.setLocationRelativeTo(null);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
                
            }
        });
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        radPayType = new javax.swing.ButtonGroup();
        lblServiceName = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        lblFromDate = new javax.swing.JLabel();
        btnFromDate = new javax.swing.JButton();
        btnToDate = new javax.swing.JButton();
        lbl = new javax.swing.JLabel();
        lblToDate = new javax.swing.JLabel();
        radOnlyPaid = new javax.swing.JRadioButton();
        radOnlyUnpaid = new javax.swing.JRadioButton();
        radAll = new javax.swing.JRadioButton();
        btnSelectAll = new javax.swing.JButton();
        btnReverseSelect = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabRegServ = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblTotalUnpaid = new javax.swing.JLabel();
        lblTotalSelectedUnpaid = new javax.swing.JLabel();
        btnExtTrans = new javax.swing.JButton();

        lblServiceName.setFont(new java.awt.Font("Garamond", 1, 24)); // NOI18N
        lblServiceName.setText("jLabel1");

        jLabel1.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel1.setText("From Date");

        lblFromDate.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnFromDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/calendar.png"))); // NOI18N
        btnFromDate.setToolTipText("Date Picker");
        btnFromDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFromDateActionPerformed(evt);
            }
        });

        btnToDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/calendar.png"))); // NOI18N
        btnToDate.setToolTipText("Date Picker");
        btnToDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnToDateActionPerformed(evt);
            }
        });

        lbl.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        lbl.setText("To Date");

        lblToDate.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        radPayType.add(radOnlyPaid);
        radOnlyPaid.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radOnlyPaid.setText("Only paid");
        radOnlyPaid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radOnlyPaidActionPerformed(evt);
            }
        });

        radPayType.add(radOnlyUnpaid);
        radOnlyUnpaid.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radOnlyUnpaid.setText("Only unpaid");
        radOnlyUnpaid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radOnlyUnpaidActionPerformed(evt);
            }
        });

        radPayType.add(radAll);
        radAll.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radAll.setText("All");
        radAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radAllActionPerformed(evt);
            }
        });

        btnSelectAll.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnSelectAll.setText("Select All");
        btnSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectAllActionPerformed(evt);
            }
        });

        btnReverseSelect.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnReverseSelect.setText("Reverse Selected");
        btnReverseSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReverseSelectActionPerformed(evt);
            }
        });

        tabRegServ.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        tabRegServ.setForeground(new java.awt.Color(204, 0, 0));
        tabRegServ.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tabRegServ);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        jLabel2.setText("Total Unpaid");

        jLabel3.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        jLabel3.setText("Selected Unpaid");

        lblTotalUnpaid.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblTotalUnpaid.setForeground(new java.awt.Color(204, 0, 0));
        lblTotalUnpaid.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotalUnpaid.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lblTotalSelectedUnpaid.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        lblTotalSelectedUnpaid.setForeground(new java.awt.Color(204, 0, 0));
        lblTotalSelectedUnpaid.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotalSelectedUnpaid.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblTotalUnpaid, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                    .addComponent(lblTotalSelectedUnpaid, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(90, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
                    .addComponent(lblTotalUnpaid, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                    .addComponent(lblTotalSelectedUnpaid, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        btnExtTrans.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        btnExtTrans.setForeground(new java.awt.Color(0, 51, 204));
        btnExtTrans.setText("Make External Transaction...");
        btnExtTrans.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExtTransActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 777, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(95, 95, 95)
                        .addComponent(btnExtTrans, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(btnFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addComponent(lbl, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(btnToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(radOnlyPaid)
                                .addGap(36, 36, 36)
                                .addComponent(radOnlyUnpaid)
                                .addGap(37, 37, 37)
                                .addComponent(radAll)))
                        .addGap(44, 44, 44)
                        .addComponent(btnSelectAll, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(btnReverseSelect))
                    .addComponent(lblServiceName, javax.swing.GroupLayout.PREFERRED_SIZE, 520, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(40, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(lblServiceName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnFromDate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lbl, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnToDate))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(radOnlyUnpaid)
                            .addComponent(radAll)
                            .addComponent(btnSelectAll)
                            .addComponent(btnReverseSelect)
                            .addComponent(radOnlyPaid))
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnExtTrans)
                        .addGap(69, 69, 69))))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectAllActionPerformed
        // TODO add your handling code here:
        for (int i = 0; i < tableModel.arl.size(); i++) {
            RegServRecs rsr = tableModel.arl.get(i);
            if(rsr.isIsPayEditable() == true)
                tableModel.setValueAt(true, i , 0);
        }
        tableModel.fireTableDataChanged();
    }//GEN-LAST:event_btnSelectAllActionPerformed

    private void btnReverseSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReverseSelectActionPerformed
        // TODO add your handling code here:
        
        isReverseSelect = true;
        for (int i = 0; i < tableModel.arl.size(); i++) {
            RegServRecs rsr = tableModel.arl.get(i);
            if(rsr.isIsPayEditable() == true)
            {
                boolean val = (boolean)tableModel.getValueAt(i, 0);
                boolean newVal = !val;
//                System.out.printf("i = "+i+" , val = "+val+", new val = "+newVal+"\n");
                tableModel.setValueAt(newVal, i , 0);
            }
        }
        tableModel.fireTableDataChanged();
        isReverseSelect = false;

    }//GEN-LAST:event_btnReverseSelectActionPerformed

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
           lblFromDate.setText(formatter.format(dt));
        }
        else
            dt = d;
        fromDate = dt;
        dlg.dispose();
        setServiceRecords();
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
           lblToDate.setText(formatter.format(dt));
        }
        else
            dt = d;
        toDate = dt;
        dlg.dispose();
        setServiceRecords();
    }//GEN-LAST:event_btnToDateActionPerformed

    private void radOnlyPaidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radOnlyPaidActionPerformed
        // TODO add your handling code here:
        setServiceRecords();
    }//GEN-LAST:event_radOnlyPaidActionPerformed

    private void radOnlyUnpaidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radOnlyUnpaidActionPerformed
        // TODO add your handling code here:
        setServiceRecords();
    }//GEN-LAST:event_radOnlyUnpaidActionPerformed

    private void radAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radAllActionPerformed
        // TODO add your handling code here:
        setServiceRecords();
    }//GEN-LAST:event_radAllActionPerformed

    private void btnExtTransActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExtTransActionPerformed
        // TODO add your handling code here:
        JFrame f = null;
        JDialog dlg= new JDialog(f,true);
        dlg.setTitle("External Transaction");
        int amount = Integer.parseInt(lblTotalSelectedUnpaid.getText());
        if(amount > 0)
        {
                ExternalTransactionPanelNew etp = new ExternalTransactionPanelNew(lblServiceName.getText().trim(), "payment for "+lblServiceName.getText().trim(),amount,walletId,dlg);
                dlg.add(etp);
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
                    getSelectedIds();
                    updateRegularService();
                }
        }
                        
    }//GEN-LAST:event_btnExtTransActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExtTrans;
    private javax.swing.JButton btnFromDate;
    private javax.swing.JButton btnReverseSelect;
    private javax.swing.JButton btnSelectAll;
    private javax.swing.JButton btnToDate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbl;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblServiceName;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JLabel lblTotalSelectedUnpaid;
    private javax.swing.JLabel lblTotalUnpaid;
    private javax.swing.JRadioButton radAll;
    private javax.swing.JRadioButton radOnlyPaid;
    private javax.swing.JRadioButton radOnlyUnpaid;
    private javax.swing.ButtonGroup radPayType;
    private javax.swing.JTable tabRegServ;
    // End of variables declaration//GEN-END:variables
}
