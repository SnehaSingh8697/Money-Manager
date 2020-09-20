/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.regularservice;

import domesticfinancesystem.calendar.Database;
import domesticfinancesystem.exttrans.ExtTransData;
import domesticfinancesystem.exttrans.ExternalTransactionPanel;
import domesticfinancesystem.exttrans.WalletData;
import domesticfinancesystem.periodicdeposit.PeriodicDeposit;
import domesticfinancesystem.shoppinglist.CreateShoppingList;
import domesticfinancesystem.shoppinglist.ListRecord;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import sun.swing.table.DefaultTableCellHeaderRenderer;
import domesticfinancesystem.*;
import domesticfinancesystem.calendar.DatePickerNewDialog;
import javax.swing.text.AbstractDocument;

/**
 *
 * @author sneha
 */
public class ServiceDefinitionPanel extends javax.swing.JPanel {

    /**
     * Creates new form ServiceDefinitionPanel
     */
    private JToggleButton[] artglWeeks = new JToggleButton[7];
    private JToggleButton[] artglDays = new JToggleButton[30];
    private final File file = new File("ServiceFreq.dat");
    private SpinnerNumberModel snmNum;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat timeformatter = new SimpleDateFormat("h:mm");
    private ArrayList<WalletData> arlWal = new ArrayList<>();
    private Database dc;
    private Connection con;
    private SpinnerListModel slm;
    private MyTableModel tableModel = new MyTableModel();
    private ArrayList<String> arlItems = new ArrayList<>();
    private ArrayList<Integer> refArlItems = new ArrayList<>();
    private Color servColor;
    private int rgbVal;
    private boolean isEditMode;
    private int servDefId;
    private Date dt;
    private String timeregex = "((1[0-2]|0?[1-9]):([0-5][0-9]) ?([AaPp][Mm]))";
    private Pattern pattime = Pattern.compile(timeregex);
    private Window parentWindow;
    private RegularServiceMainPanel rsp;
    private  String[] ampmString = {"am", "pm"};
    private boolean madeChanges = false;
    
    private class MyTableModel extends AbstractTableModel
    {
        final int COLS = 4;
        String[] colNames = {"Item","Price per piece","Default Quantity","Frequency"};
        Class[] colTypes = {String.class,Float.class,Float.class,String.class} ;
        ArrayList<ServDefRecord> arl = new ArrayList<ServDefRecord>();

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
            ServDefRecord sdr = (ServDefRecord)arl.get(rowIndex);
            if(columnIndex == 0)
                return sdr.getItemName();
            else if(columnIndex == 1)
                return sdr.getPrice();
            else if(columnIndex == 2)
                return sdr.getQty();
            else if(columnIndex == 3)
            {
                return sdr.getFreqDesc();
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
            ServDefRecord sdr = arl.get(rowIndex);
            if(columnIndex == 0)
            {
                sdr.setItemName((String)aValue);
            }
            else if(columnIndex == 1)
                sdr.setPrice((Float)aValue);
            else if(columnIndex == 2)
                sdr.setQty((float)aValue);
            else if(columnIndex == 3)
                sdr.setFreqDesc((String)aValue);
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
        
        public void addRow(ServDefRecord sdr)
        {
            arl.add(sdr);
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
    
    public boolean isItemUnique(String item)
    {
        for (String itemnames : arlItems) {
            if(item.equals(itemnames))
            {
                return false;
            }
            
        }
        return true;
    }
    
    public String freqDesc(int n)
    {
        int freqType  = (n)>>>30;
        int mask = 0b00_111111_11111111_11111111_11111111 ; //to reset leftmost two bits of n
        n = n & mask;
        
        if(freqType == 0)//after every n days
        {
            if(n == 0)//no frequency(this item wont be added automatically in the regular service)
            {
                return "*";
            }
            if(n == 1)
            {
                return "daily";
            }
            else
            {
                String s = "each "+n;
                if(n>=11&&n<=13)
                {
                    s+="th";
                }
                else
                {
                    switch(n % 10)
                    {
                        case 1:
                            s+="st";
                            break;
                        case 2:
                            s+="nd";
                            break;
                        case 3:
                            s+="rd";
                            break;
                        default:
                            s+="th";
                    }
                }
                s+=" day";
                return s;
                
            }
        }
        else if(freqType == 1)//weekdays
        {
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] weekdays =  dfs.getShortWeekdays();
            
            mask = 0b10000000 ; // 0x80 
           
            
           StringBuilder sb = new StringBuilder();
            for (int i = 1; i <=7; i++) {
                 if((n & mask) != 0)   // Corresponding bit is set
                 {
                    sb.append(weekdays[i]);
                    sb.append(", ");
                 }
                mask >>>= 1 ;
            }
            return sb.substring(0,sb.length()-2);
        }
        else if(freqType == 2)//month days
        {
            int sday , eday;
            sday = eday = -1;
            
            mask = 0b00_100000_00000000_00000000_00000000 ; 
           
            
           StringBuilder sb = new StringBuilder();
            for (int i = 1; i <=30; i++) {
                 if((n & mask) != 0)   // Corresponding bit is set
                 {
                    if (eday !=i-1)   // if end day not the previous day
                    {
                        sb.append(createDayString(sday, eday));
                        sday = i;
                    }
                    eday = i;
                          
                 }
                mask >>>= 1 ;
            }
            sb.append(createDayString(sday, eday)) ;
            return sb.substring(0,sb.length()-2);
        }
        else
        {
            mask = 0b10000000;     // ox80
            String s = "";
            boolean exists = false;
            
            if((n & mask) !=0)
            {
                if(exists == true)
                {
                    s+=" , ";
                }
                s+="First";
                exists = true;
            }
           
            mask>>>=1;
            
            if((n & mask) !=0)
            {
                if(exists == true)
                {
                    s+=" , ";
                }
                s+="Last";
                exists = true;
            }
            
            mask>>>=1;
            
            if((n & mask) !=0)
            {
                if(exists == true)
                {
                    s+=" , ";
                }
                s+="Middle"; 
                exists = true;
            }
            if(exists)
            {
                s+=" of the month";
            }
            return s;
        }
    }
    
    private String createDayString(int sday,int eday)
    {
        String s = "";
        if(sday != -1)
        {
            s = Integer.toString(sday);
            if(sday != eday)
                s+="-"+eday;
            
            return s + ", " ;
        }
        else
            return "" ;

    }
    
   public boolean isServiceColorUnique(String table,String fieldName,int fieldValue,boolean isEditMode,int id)
    {
        boolean found = false ;
        int dbId = -1;
        System.out.println("Table name: "+table);
        try {
            String sql = "Select id from " + table + " where " + fieldName + " = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, fieldValue);
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
    
    public boolean isUnique(String table,String fieldName,String fieldValue,boolean isEditMode,int id)
    {
        boolean found = false ;
        int dbId = -1;
        System.out.println("Table name: "+table);
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
    
    public void clearFreqComponents()
    {
        spnDays.setValue(1);
        
        for (int i = 0; i < 7; i++) {
           artglWeeks[i].setSelected(false);
        }
        
         for (int i = 0; i < 30; i++) {
            artglDays[i].setSelected(false);
        }
         
        chkFirst.setSelected(false);
        chkLast.setSelected(false);
        chkMiddle.setSelected(false);
        
    }
    
    public boolean areAllValid()
    {
        String title = txtServiceName.getText().trim();
        if(title.isEmpty())
        {
            JOptionPane.showMessageDialog(this,"Title not entered", "Message",JOptionPane.ERROR_MESSAGE);
            txtServiceName.requestFocus();
            return false;
        }
        if(tableModel.arl.size() == 0)
        {
           JOptionPane.showMessageDialog(this,"No record entered", "Message",JOptionPane.ERROR_MESSAGE);
           txtItemName.requestFocus();
           return false;
        }
        if(isUnique("ServDef", "Name", title, isEditMode, servDefId) == false)
        {
           JOptionPane.showMessageDialog(this,"Service by this name already exists.Please enter another name", "Message",JOptionPane.ERROR_MESSAGE);
           txtServiceName.requestFocus();
           return false;
        }
        if(rgbVal == -1)
        {
           JOptionPane.showMessageDialog(this,"Please choose a color for service definition", "Message",JOptionPane.ERROR_MESSAGE);
           btnServColor.requestFocus();
           return false;
        }
        if(isServiceColorUnique("ServDef", "RgbVal", rgbVal, isEditMode, servDefId) == false)
        {
           JOptionPane.showMessageDialog(this,"Service color already exists.Please choose another color", "Message",JOptionPane.ERROR_MESSAGE);
           btnServColor.requestFocus();
           return false;
        }
        if(chkAutoAddRecord.isSelected())
        {
            String time = txtTime.getText().trim();
            if(time !=null && !time.isEmpty())
            {
                if(isTimeValid() == false)
                {
                    JOptionPane.showMessageDialog(this,"Invalid time format", "Message",JOptionPane.ERROR_MESSAGE);
                    txtTime.requestFocus();
                    return false;  
                }
            }
            else
            {
                JOptionPane.showMessageDialog(this,"Time for adding record automatically not entered", "Message",JOptionPane.ERROR_MESSAGE);
                txtTime.requestFocus();
                return false;
            }
        }
        
        return true;
    }
    
    public int getWalletId()
    {
        int id = 0;
        String walletName = (String)cmbWallet.getSelectedItem();
        for (int i = 0; i < arlWal.size(); i++) {
            if(walletName.equals(arlWal.get(i).getName()))
            {
                id = arlWal.get(i).getId();
                break;
            }
            
        }
        return id;
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
    
    public RegularService addServiceDefinition()
    {
        RegularService rs = null;
        try {
            
            String title = txtServiceName.getText().trim();
            dt = formatter.parse(lblStartDate.getText().trim());
            java.sql.Date sdt = new java.sql.Date(dt.getTime());
            System.out.println("sdt: "+sdt);
            int walletId = getWalletId();
            Date onTime = null;
            String autoAddYN = "";
            int noOfSeconds;
            if(chkAutoAddRecord.isSelected())
            {
                onTime = timeformatter.parse(txtTime.getText());
                autoAddYN = "Y";
                noOfSeconds = onTime.getHours() * 3600 + onTime.getMinutes() * 60;
                
            }
            else
            {
               autoAddYN = "N"; 
               noOfSeconds = 0;
            }
            java.sql.Date sOnTime = new java.sql.Date(onTime.getTime());
            String isAM;
            if(spnTime.getValue().equals("am"))
                isAM = "Y";
            else
                isAM = "N";
            
            String sql = "";
            int sId = 0;
            if(isEditMode == false)
            {
                sql = "Insert into ServDef(Id,Name,Ontime,isAM,StrtDate,WalletId,AutoAddYN,RgbVal,OnNoOfSeconds) ";
                    sql+="values(?,?,?,?,?,?,?,?,?)";
                sId = getIdFromDual();
            }
            else
            {
                sId = servDefId;
                sql = "Update ServDef set Id = ?,Name = ?,Ontime = ?,isAM = ?,StrtDate = ?,WalletId = ?,AutoAddYN = ?,RgbVal = ?,OnNoOfSeconds = ? where Id = "+sId;
                
            }
                PreparedStatement pstmt;
                pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, sId);
                pstmt.setString(2, title);
                pstmt.setDate(3,sOnTime);
                pstmt.setString(4,isAM);
                pstmt.setDate(5,sdt);
                pstmt.setInt(6,walletId);
                pstmt.setString(7,autoAddYN);
                pstmt.setInt(8,rgbVal);
                pstmt.setInt(9,noOfSeconds);
                pstmt.executeUpdate();
                pstmt.close();  
            
                rs = new RegularService(sId, title, rgbVal);
                
                
                //===================== Storing service items in database =====================//
                
                // For the existing items to update or delete
                 for (Integer refItemId : refArlItems) 
                 {
                   boolean found = false;   
                  
                   ServDefRecord sdr = null ;
                   for (ServDefRecord sdfr : tableModel.arl) 
                       if(sdfr.getId() == refItemId)
                       {
                           sdr = sdfr ;
                           found = true;
                           break;
                       }
                  
                   if(found == false)
                   {//delete item with id = refItemId
                       sql = "delete from ServDefDetail where Id = "+refItemId;
                       Statement stmt = con.createStatement();
                       stmt.executeQuery(sql);
                       stmt.close();
                       
                   }
                   else  // found
                   {
                       //update item with id = refItemId
                         int id = sdr.getId();
                         int ServDefId = sId;
                         String itemName = sdr.getItemName();
                         float price = sdr.getPrice();
                         float qty = sdr.getQty();
                         int freq = sdr.getFreq();

                         sql = "Update ServDefDetail set Id = ?,ServDefId = ?,ItemName = ?,Price = ?,DefaultQty = ?,Frequency = ? where id = "+id;

                         pstmt = con.prepareStatement(sql);
                         pstmt.setInt(1, id);
                         pstmt.setInt(2, ServDefId);
                         pstmt.setString(3,itemName);
                         pstmt.setFloat(4,price);
                         pstmt.setFloat(5,qty);
                         pstmt.setInt(6,freq);
                         pstmt.executeUpdate();
                         pstmt.close();
                   }
                  }                

                 // For the new items to insert
                  for (ServDefRecord sdr : tableModel.arl) {
                    if(sdr.getId() == -1)
                        sdr.setId(getIdFromDual());
                    else
                        continue ;
                   
                    int id = sdr.getId();
                    int ServDefId = sId;
                    String itemName = sdr.getItemName();
                    float price = sdr.getPrice();
                    float qty = sdr.getQty();
                    int freq = sdr.getFreq();
                    
                    sql = "Insert into ServDefDetail(Id,ServDefId,ItemName,Price,DefaultQty,Frequency) values(?,?,?,?,?,?)";
                    
                    
                    pstmt = con.prepareStatement(sql);
                    pstmt.setInt(1, id);
                    pstmt.setInt(2, ServDefId);
                    pstmt.setString(3,itemName);
                    pstmt.setFloat(4,price);
                    pstmt.setFloat(5,qty);
                    pstmt.setInt(6,freq);
                    pstmt.executeUpdate();
                    pstmt.close();  
                   
            }
           //===================== Storing service items in database DONE =====================//
                
            
        } catch (ParseException|SQLException ex) {
            Logger.getLogger(ServiceDefinitionPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }
    
    public boolean checkValidity()
    {
        String name = txtItemName.getText().trim();
        if(name == null || name.isEmpty())
        {
          JOptionPane.showMessageDialog(this,"Item name not entered", "Message",JOptionPane.ERROR_MESSAGE);
          txtItemName.requestFocus();
          return false;
        }
        String qty = txtQuantity.getText().trim();
        if(qty == null || qty.isEmpty())
        {
          JOptionPane.showMessageDialog(this,"Default quantity not entered", "Message",JOptionPane.ERROR_MESSAGE);
          txtQuantity.requestFocus();
          return false;
        }
        if(Float.parseFloat(txtQuantity.getText().trim()) == 0)
        {
          JOptionPane.showMessageDialog(this,"Quantity cannot be zero", "Message",JOptionPane.ERROR_MESSAGE);
          txtQuantity.requestFocus();
          return false;
        }
        if(isFreqSelected() == false)
        {
          JOptionPane.showMessageDialog(this,"Frequenynot selected for the item", "Message",JOptionPane.ERROR_MESSAGE);
          return false;
        }
        return true;
    }
   
    public boolean isFreqSelected()
    {
        int n = saveFreqData();
        int mask = 0b00_111111_11111111_11111111_11111111 ; //to reset leftmost two bits of n
        n = n & mask;
        if(n == 0)
            return false;
        return true;
    }
    
//    public boolean areAllValid()
//    {
//        String title = txtTitle.getText().trim();
//        if(title.isEmpty())
//        {
//            JOptionPane.showMessageDialog(this,"Title not entered", "Message",JOptionPane.ERROR_MESSAGE);
//            txtTitle.requestFocus();
//            return false;
//        }
//        if(tableModel.arl.size() == 0)
//        {
//           JOptionPane.showMessageDialog(this,"No record entered", "Message",JOptionPane.ERROR_MESSAGE);
//           cmbItem.requestFocus();
//           return false;
//        }
//        if(isUnique("ShoppingList", "Name", title, isEditMode, slistId) == false)
////        if(isUnique(title, isEditMode, slistId) == false)
//        {
//           JOptionPane.showMessageDialog(this,"ShoppingList by this name already exists.Please enter another name", "Message",JOptionPane.ERROR_MESSAGE);
//           txtTitle.requestFocus();
//           return false;
//        }
//        
//        return true;
//    }

    
    public void getWalletInfo()
    {
      try {
            cmbWallet.removeAllItems();
            arlWal.clear();
            String sql = "Select Id,Name from Wallet where Liquidbal != 0 OR Digitalbal != 0";
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            while(rst.next())
            {
                int id = rst.getInt(1);
                String name = rst.getString(2);
                WalletData wd = new WalletData(id, name);
                arlWal.add(wd);
                cmbWallet.addItem(name);
            }
        } catch (SQLException ex) {
            Logger.getLogger(PeriodicDeposit.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }

    
    public int saveFreqData()
    {
        int b = 0 ;
        if(radEachNthDy.isSelected())
            b = (int)snmNum.getValue();
        else if(radDaysInWeek.isSelected())
        {
            b = 0b01000000_00000000_00000000_00000000 ;
            int mask = 0b10000000 ; // 0x80 
            
            for (int i = 0; i < 7; i++) {
                if(artglWeeks[i].isSelected())
                    b |= mask ;
                mask >>>= 1 ;
            }
            
            return b ;
        }
        else if(radDaysInMonth.isSelected())
        {
            b = 0b10_000000_00000000_00000000_00000000 ;
            int mask = 0b00_100000_00000000_00000000_00000000 ;
            
             for (int i = 0; i < 30; i++) {
                if(artglDays[i].isSelected())
                    b |= mask ;
                mask >>>= 1 ;
            }
            
            return b ;
        }
        else
        {
             b = 0b11_000000_00000000_00000000_00000000 ;
            int mask = 0b10000000 ; // 0x80
            if(chkFirst.isSelected())
                 b |= mask ;

            mask >>>= 1 ;
            if(chkLast.isSelected())
                 b |= mask ;

            mask >>>= 1 ;
            if(chkMiddle.isSelected())
                 b |= mask ;
        }
        
        return b;
    }
    
    public void extractFreqData(int n)
    {
        int freqType  = (n)>>>30;
        System.out.println("freq type = "+freqType);
        
        int mask = 0b00_111111_11111111_11111111_11111111 ; //to reset leftmost two bits of n
        n = n & mask;                      //resetting leftmost two bits of n

        if(freqType == 0)
        {
            radEachNthDy.setSelected(true);
            spnDays.setValue(n);
        }
        else if(freqType == 1)
        {
            radDaysInWeek.setSelected(true);
            mask = 0b10000000 ; // 0x80 
            
            for (int i = 0; i < 7; i++) {
                 if((n & mask) != 0)   // Corresponding bit is set
                    artglWeeks[i].setSelected(true);
                 else
                    artglWeeks[i].setSelected(false);
 
                mask >>>= 1 ;
            }

            
        }
        else if(freqType == 2)
        {
            radDaysInMonth.setSelected(true);
            
            mask = 0b00_100000_00000000_00000000_00000000 ;            
            for (int i = 0; i < 30; i++) {
                 if((n & mask) != 0)   // Corresponding bit is set
                    artglDays[i].setSelected(true);
                 else
                    artglDays[i].setSelected(false);
 
                mask >>>= 1 ;
            }
            
        }
        else
        {
            radFstLstMidMonth.setSelected(true);
            mask = 0b10000000;     // ox80
            
            if((n & mask) !=0)
                chkFirst.setSelected(true);
           
            mask>>>=1;
            
            if((n & mask) !=0)
                chkLast.setSelected(true);
            
            mask>>>=1;
            
            if((n & mask) !=0)
                chkMiddle.setSelected(true);
        }
            
    }
    
    public void clearComponents()
    {
        txtItemName.setText("");
        txtItemName.requestFocus();
        txtPrice.setText("0");
        txtQuantity.setText("");
        clearFreqComponents();
    }
    
    public boolean isTimeValid()
    {
        String time = txtTime.getText().trim()+" "+slm.getValue();
         Matcher mattime = pattime.matcher(time);
            
            if(mattime.matches() == false)
            {
                return false;
            }
            return true;
    }
    public void setWindowCloseListener()
    {
       parentWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e); //To change body of generated methods, choose Tools | Templates.
                if(madeChanges == true)
                {
                    int type = JOptionPane.showConfirmDialog(null,"Service definition not saved.Will you like to save it?", "Message",JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if(type == JOptionPane.NO_OPTION)
                    {
                        parentWindow.dispose();
                    }
                    else
                    {
                        ((JDialog)parentWindow).setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                    }
                }
                else 
                {
                    parentWindow.dispose();
                }
            }
          
        });
    }
    public ServiceDefinitionPanel(RegularServiceMainPanel rsp,Window f)
    {
        this();
        parentWindow = f;
        this.rsp = rsp;
        
        isEditMode = false;
        servDefId = -1;
        
        dt = new GregorianCalendar().getTime();
        lblStartDate.setText(formatter.format(dt));
        rgbVal = -1;
        
        setWindowCloseListener();
    }
    
    public ServiceDefinitionPanel(RegularServiceMainPanel rsp,Window f,int id)
    {
        this();
        parentWindow = f;
        this.rsp = rsp;
        
        isEditMode = true;
        servDefId = id;
        
        
        fetchInfoFromDatabase(id);
        
        if(isStartDateEditable() == false)
        {
            btnDatePicker.setEnabled(false);
        }
        
        setWindowCloseListener();

    }
    private boolean isStartDateEditable()
    {
        try {
            String sql = "Select * from RegServ where ServDefId = ?";
            PreparedStatement pstmt =  con.prepareStatement(sql);
            pstmt.setInt(1,servDefId);
            ResultSet rst = pstmt.executeQuery();
            
            if(rst.next())
            {
               return false;
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(ServiceDefinitionPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
            return true;
    }
    public boolean isRecordFound(int id)
    {
        boolean found = false;
        try {
            String sql = "Select * from RegServDetails where ServItemId = ?";
            PreparedStatement pstmt =  con.prepareStatement(sql);
            pstmt.setInt(1,id);
            ResultSet rst = pstmt.executeQuery();
            
            while(rst.next())
            {
                found = true;
                break;
            }
              
            } catch (SQLException ex) {
            Logger.getLogger(ServiceDefinitionPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return found;
    }
    
    public void fetchInfoFromDatabase(int index)
    {
        try {
            String sql = "Select * from ServDef where Id = ?";
            PreparedStatement pstmt =  con.prepareStatement(sql);
            pstmt.setInt(1,index);
            ResultSet rst = pstmt.executeQuery();
            while(rst.next())
            {
               String title = rst.getString(2);
               txtServiceName.setText(title);
               
               String autoAdd = rst.getString(7);
               if(autoAdd.equals("Y"))
               {
                    chkAutoAddRecord.setSelected(true);
                    
                    String time = timeformatter.format(rst.getDate(3));
                    txtTime.setText(time);

                    String s = rst.getString(4);
                    if(s.equals("Y"))
                        slm.setValue(ampmString[0]);
                    else
                       slm.setValue(ampmString[1]); 
               }
               else
               {
                   txtTime.setEnabled(false);
                   spnTime.setEnabled(false);
               }
               
               dt = rst.getDate(5);
               lblStartDate.setText(formatter.format(dt));
               
               int walletId = rst.getInt(6);
               String walletName = getWalletName(walletId);
               cmbWallet.setSelectedItem(walletName);
               
               rgbVal = rst.getInt(8);
               btnServColor.setBackground(new Color(rgbVal));
                   
            }
            
            rst.close();
            pstmt.close();
            
                sql = "Select * from ServDefDetail where ServDefId = ?";
                pstmt =  con.prepareStatement(sql);
                pstmt.setInt(1,index);
                rst = pstmt.executeQuery();
                while(rst.next())
                {
                    int id = rst.getInt(1);
                    refArlItems.add(id);
                    String itemName = rst.getString(3);
                    float price = rst.getFloat(4);
                    float qty = rst.getFloat(5);
                    int freq = rst.getInt(6);
                    String freqdesc = freqDesc(freq);
                    ServDefRecord sdr = new ServDefRecord(id, itemName, price, qty, freqdesc, freq,false);
                    tableModel.addRow(sdr);
                    
                    int indx = tableModel.arl.indexOf(sdr);
                    tableModel.fireTableRowsInserted(indx, indx);
                    
                    tabRegServItems.setRowSelectionInterval(indx, indx);

                }
                rst.close();
                pstmt.close();
                
        } catch (SQLException ex) {
            Logger.getLogger(ServiceDefinitionPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    
   }
    public String getWalletName(int id)
    {
        String nm = "";
        try {
            String sql = "Select Name from Wallet where Id = ?";
            PreparedStatement pstmt =  con.prepareStatement(sql);
            pstmt.setInt(1,id);
            ResultSet rst = pstmt.executeQuery();
            
            while(rst.next())
            {
                nm = rst.getString("Name");
                
            }
        }
        catch(SQLException ex)
        {
            Logger.getLogger(ServiceDefinitionPanel.class.getName()).log(Level.SEVERE, null, ex);

        }
        return nm;
    }
    public ServiceDefinitionPanel() {
        initComponents();
        
        dc = new Database("jdbc:oracle:thin:@localhost:1521:XE","dfs","dfsboss","oracle.jdbc.OracleDriver");
        con = dc.createConnection();
        
        ((AbstractDocument)txtPrice.getDocument()).setDocumentFilter(new IntegerDocumentFilter());
        ((AbstractDocument)txtQuantity.getDocument()).setDocumentFilter(new IntegerDocumentFilter());

        
        getWalletInfo();
        
        tabRegServItems.setModel(tableModel);
        tabRegServItems.setRowHeight(25);
        tabRegServItems.setShowGrid(true); //to show table border for each cell
        ((DefaultTableCellRenderer)tabRegServItems.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        
        CellRenderer cr = new CellRenderer();
        tabRegServItems.setDefaultRenderer(String.class, cr);
        tabRegServItems.setDefaultRenderer(Float.class, cr);
        
       
        slm = new SpinnerListModel(ampmString);
        spnTime.setModel(slm);
        
        chkAutoAddRecord.setSelected(true);
        
        
        DateFormatSymbols df = new DateFormatSymbols();
        
        JToggleButton tmp = new JToggleButton("WWW") ;
        String[] weekDays = df.getShortWeekdays();
        for (int i = 1; i < weekDays.length; i++) {
            JToggleButton tgl = new JToggleButton(weekDays[i]);
            tgl.setPreferredSize(tmp.getPreferredSize());
            outerMonthNamePanel.add(tgl);
            artglWeeks[i - 1] = tgl;
            
        }
        
        monthDatePanel.setLayout(new GridLayout(2, 15));
       
        tmp = new JToggleButton("99") ;
        Font fnt = tmp.getFont().deriveFont(10.0f) ;
        for (int i = 1; i <= 30 ; i++) {
            JToggleButton tgl = new JToggleButton(""+(i));
            tgl.setFont(fnt);
//            tgl.setPreferredSize(tmp.getPreferredSize());
            monthDatePanel.add(tgl);
            artglDays[i-1] = tgl;
            
        }
        
//        outerMonthNamePanel.invalidate();
//        monthDatePanel.invalidate();//to redraw the panel after adding/deleting some components to it

//            snmNum = new SpinnerNumberModel(1, 1, 2<<30, 1);
            snmNum = new SpinnerNumberModel(1, 1, 366, 1);
            spnDays.setModel(snmNum);
            spnDays.setValue(1);
            
            radEachNthDy.setSelected(true);
            
        tabRegServItems.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                int row = tabRegServItems.getSelectedRow() ;
                
                if(row != -1)
                {
                    clearFreqComponents();
                    ServDefRecord sdr = tableModel.arl.get(row);
                    getListDetails(sdr);
                }
                
            }
        });
        
    }
    
    public void getListDetails(ServDefRecord sdr)
    {
        txtItemName.setText(sdr.getItemName());
        txtPrice.setText(""+sdr.getPrice());
        txtQuantity.setText(""+sdr.getQty());
        extractFreqData(sdr.getFreq());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        FrequencyPanel = new javax.swing.JPanel();
        radEachNthDy = new javax.swing.JRadioButton();
        spnDays = new javax.swing.JSpinner();
        radDaysInWeek = new javax.swing.JRadioButton();
        outerMonthNamePanel = new javax.swing.JPanel();
        radDaysInMonth = new javax.swing.JRadioButton();
        monthDatePanel = new javax.swing.JPanel();
        radFstLstMidMonth = new javax.swing.JRadioButton();
        chkFirst = new javax.swing.JCheckBox();
        chkLast = new javax.swing.JCheckBox();
        chkMiddle = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        UpperPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtServiceName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        lblStartDate = new javax.swing.JLabel();
        btnDatePicker = new javax.swing.JButton();
        chkAutoAddRecord = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        txtTime = new javax.swing.JTextField();
        spnTime = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        cmbWallet = new javax.swing.JComboBox<>();
        btnServColor = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        MiddlePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabRegServItems = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        txtItemName = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtPrice = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtQuantity = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        btnReset = new javax.swing.JButton();
        btnAddRecord = new javax.swing.JButton();
        btnDeleteRecord = new javax.swing.JButton();
        btnEditRecord = new javax.swing.JButton();
        btnSaveServDef = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        FrequencyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Frequency Panel", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 153))); // NOI18N
        FrequencyPanel.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N

        buttonGroup1.add(radEachNthDy);
        radEachNthDy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radEachNthDyActionPerformed(evt);
            }
        });

        spnDays.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N

        buttonGroup1.add(radDaysInWeek);
        radDaysInWeek.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radDaysInWeekActionPerformed(evt);
            }
        });

        outerMonthNamePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        outerMonthNamePanel.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N

        buttonGroup1.add(radDaysInMonth);
        radDaysInMonth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radDaysInMonthActionPerformed(evt);
            }
        });

        monthDatePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        monthDatePanel.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N

        javax.swing.GroupLayout monthDatePanelLayout = new javax.swing.GroupLayout(monthDatePanel);
        monthDatePanel.setLayout(monthDatePanelLayout);
        monthDatePanelLayout.setHorizontalGroup(
            monthDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 776, Short.MAX_VALUE)
        );
        monthDatePanelLayout.setVerticalGroup(
            monthDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 46, Short.MAX_VALUE)
        );

        buttonGroup1.add(radFstLstMidMonth);
        radFstLstMidMonth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radFstLstMidMonthActionPerformed(evt);
            }
        });

        chkFirst.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        chkFirst.setText("First");

        chkLast.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        chkLast.setText("Last");

        chkMiddle.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        chkMiddle.setText("Middle");

        jLabel1.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel1.setText("of the month");

        javax.swing.GroupLayout FrequencyPanelLayout = new javax.swing.GroupLayout(FrequencyPanel);
        FrequencyPanel.setLayout(FrequencyPanelLayout);
        FrequencyPanelLayout.setHorizontalGroup(
            FrequencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FrequencyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(FrequencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(FrequencyPanelLayout.createSequentialGroup()
                        .addGroup(FrequencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(radDaysInMonth, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(radEachNthDy, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(radDaysInWeek, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(FrequencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spnDays, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(outerMonthNamePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(monthDatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(FrequencyPanelLayout.createSequentialGroup()
                        .addComponent(radFstLstMidMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(chkFirst)
                        .addGap(18, 18, 18)
                        .addComponent(chkLast)
                        .addGap(18, 18, 18)
                        .addComponent(chkMiddle)
                        .addGap(27, 27, 27)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        FrequencyPanelLayout.setVerticalGroup(
            FrequencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FrequencyPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(FrequencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(radEachNthDy)
                    .addComponent(spnDays, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(FrequencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radDaysInWeek)
                    .addComponent(outerMonthNamePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(FrequencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radDaysInMonth)
                    .addComponent(monthDatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(FrequencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(FrequencyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(chkFirst)
                        .addComponent(chkLast)
                        .addComponent(chkMiddle)
                        .addComponent(jLabel1))
                    .addComponent(radFstLstMidMonth)))
        );

        jLabel2.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel2.setText("Service Name");

        jLabel3.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel3.setText("Start Date");

        lblStartDate.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnDatePicker.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/calendar.png"))); // NOI18N
        btnDatePicker.setToolTipText("Date Picker");
        btnDatePicker.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDatePickerActionPerformed(evt);
            }
        });

        chkAutoAddRecord.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        chkAutoAddRecord.setText("Add record automatically");
        chkAutoAddRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkAutoAddRecordActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel4.setText("At or after");

        jLabel5.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel5.setText("Wallet Affected");

        cmbWallet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbWalletActionPerformed(evt);
            }
        });

        btnServColor.setText("..");
        btnServColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnServColorActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel9.setText("choose time in HH:MM  format");

        jLabel11.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel11.setText("choose a color for the service");

        javax.swing.GroupLayout UpperPanelLayout = new javax.swing.GroupLayout(UpperPanel);
        UpperPanel.setLayout(UpperPanelLayout);
        UpperPanelLayout.setHorizontalGroup(
            UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(UpperPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(UpperPanelLayout.createSequentialGroup()
                        .addGroup(UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtServiceName, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                            .addComponent(lblStartDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(UpperPanelLayout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cmbWallet, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(btnDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addGroup(UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkAutoAddRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(UpperPanelLayout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTime, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(spnTime, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel9))
                .addGap(8, 8, 8)
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnServColor, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(37, Short.MAX_VALUE))
        );
        UpperPanelLayout.setVerticalGroup(
            UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(UpperPanelLayout.createSequentialGroup()
                .addGroup(UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(UpperPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(UpperPanelLayout.createSequentialGroup()
                                .addGroup(UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtServiceName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(UpperPanelLayout.createSequentialGroup()
                                .addComponent(chkAutoAddRecord)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(spnTime)
                                    .addComponent(txtTime)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel9))))
                    .addGroup(UpperPanelLayout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnServColor)
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 0, 0)
                .addGroup(UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbWallet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        tabRegServItems.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
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

        jLabel6.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel6.setText("Item Name");

        txtItemName.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N

        jLabel7.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel7.setText("Price per item");

        txtPrice.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        txtPrice.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel8.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel8.setText("Quantity");

        txtQuantity.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N

        jLabel10.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        jLabel10.setText(" * - This item wont be added automatically in the regular service");

        javax.swing.GroupLayout MiddlePanelLayout = new javax.swing.GroupLayout(MiddlePanel);
        MiddlePanel.setLayout(MiddlePanelLayout);
        MiddlePanelLayout.setHorizontalGroup(
            MiddlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MiddlePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(MiddlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(MiddlePanelLayout.createSequentialGroup()
                        .addGroup(MiddlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(MiddlePanelLayout.createSequentialGroup()
                                .addGroup(MiddlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(21, 21, 21)
                                .addGroup(MiddlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtItemName, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                                    .addComponent(txtPrice)
                                    .addComponent(txtQuantity)))
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 390, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(417, Short.MAX_VALUE))))
        );
        MiddlePanelLayout.setVerticalGroup(
            MiddlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MiddlePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(MiddlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(MiddlePanelLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(txtItemName)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(MiddlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(MiddlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtQuantity))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/reset.png"))); // NOI18N
        btnReset.setToolTipText("Reset");
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        btnAddRecord.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/Add.gif"))); // NOI18N
        btnAddRecord.setToolTipText("Add Record");
        btnAddRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddRecordActionPerformed(evt);
            }
        });

        btnDeleteRecord.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/delete.gif"))); // NOI18N
        btnDeleteRecord.setToolTipText("Delete Record");
        btnDeleteRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteRecordActionPerformed(evt);
            }
        });

        btnEditRecord.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/document_edit.png"))); // NOI18N
        btnEditRecord.setToolTipText("Edit Record");
        btnEditRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditRecordActionPerformed(evt);
            }
        });

        btnSaveServDef.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/save.png"))); // NOI18N
        btnSaveServDef.setToolTipText("Save this service definition");
        btnSaveServDef.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveServDefActionPerformed(evt);
            }
        });

        btnCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/cancel.png"))); // NOI18N
        btnCancel.setToolTipText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addComponent(btnReset)
                .addGap(18, 18, 18)
                .addComponent(btnAddRecord)
                .addGap(18, 18, 18)
                .addComponent(btnDeleteRecord)
                .addGap(18, 18, 18)
                .addComponent(btnEditRecord)
                .addGap(59, 59, 59)
                .addComponent(btnSaveServDef)
                .addGap(18, 18, 18)
                .addComponent(btnCancel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(MiddlePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(FrequencyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(UpperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 12, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(UpperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(MiddlePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(FrequencyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDeleteRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnReset)
                    .addComponent(btnEditRecord)
                    .addComponent(btnSaveServDef, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(33, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void radFstLstMidMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radFstLstMidMonthActionPerformed
        // TODO add your handling code here:
        clearFreqComponents();
    }//GEN-LAST:event_radFstLstMidMonthActionPerformed

    private void btnDatePickerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDatePickerActionPerformed
        // TODO add your handling code here:
            String str = lblStartDate.getText().trim();
            Date d;
            try
            {
               d = formatter.parse(str) ;
            }
            catch (ParseException ex)
            {
                lblStartDate.setText("");
                d = new GregorianCalendar().getTime();  
            }
        
        DatePickerNewDialog dlg;
        dlg = new DatePickerNewDialog(null, true, d);
        dlg.setVisible(true);
        dt = dlg.getSelectedDate();
       
        if(dt!=null)
        {
            try {
                String s = formatter.format(dt);
                dt = formatter.parse(s);
                lblStartDate.setText(formatter.format(dt));
            } catch (ParseException ex) {
                Logger.getLogger(ExternalTransactionPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        dlg.dispose();
    }//GEN-LAST:event_btnDatePickerActionPerformed

    private void chkAutoAddRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkAutoAddRecordActionPerformed
        // TODO add your handling code here:
        if(chkAutoAddRecord.isSelected())
        {
            txtTime.setEnabled(true);
            spnTime.setEnabled(true);
        }
        else
        {
            txtTime.setEnabled(false);
            spnTime.setEnabled(false);
        }
    }//GEN-LAST:event_chkAutoAddRecordActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        // TODO add your handling code here:
       clearComponents();
       radEachNthDy.setSelected(true);

    }//GEN-LAST:event_btnResetActionPerformed

    private void btnAddRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRecordActionPerformed
        // TODO add your handling code here:
        if(checkValidity())
        {
            String itemName = txtItemName.getText().trim();
            int id = -1;
            float qty = Float.parseFloat(txtQuantity.getText().trim());
            String p = txtPrice.getText();
            float price = 0;
            if(p!=null && !p.isEmpty())
            {
                price = Float.parseFloat(p);
            }
            int n = saveFreqData();
            String freqDesc = freqDesc(n);
            ServDefRecord sdr = new ServDefRecord(id,itemName, price, qty, freqDesc, n,true);
            tableModel.addRow(sdr);
            int row = tableModel.getRowCount() - 1;
            tableModel.fireTableRowsInserted(row, row);
            tabRegServItems.setRowSelectionInterval(row, row);
            
            madeChanges = true;
        }
    }//GEN-LAST:event_btnAddRecordActionPerformed

    private void btnDeleteRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteRecordActionPerformed
        // TODO add your handling code here:
        int row = tabRegServItems.getSelectedRow() ;
        if(row != -1)
        {
            
            ServDefRecord sdr = tableModel.arl.get(row);
            if((sdr.getId() == -1) || (isRecordFound(sdr.getId()) == false))
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
                madeChanges = true;

            }
            else
            {
                JOptionPane.showMessageDialog(this,"This Record cannot be deleted", "Message",JOptionPane.ERROR_MESSAGE);

            }
        }
    }//GEN-LAST:event_btnDeleteRecordActionPerformed

    private void btnEditRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditRecordActionPerformed
        // TODO add your handling code here:
        int row = tabRegServItems.getSelectedRow() ;
        
        if(row>=0)
        {
                ServDefRecord sdr = tableModel.arl.get(row);

                if((sdr.getId() == -1) || (isRecordFound(sdr.getId()) == false))
                {
                        if(checkValidity())
                        {
                            String itemName = txtItemName.getText().trim();
                            tableModel.setValueAt(itemName, row, 0);
                            float qty = Float.parseFloat(txtQuantity.getText().trim());
                            tableModel.setValueAt(qty, row, 2);

                            String p = txtPrice.getText();
                            float price = 0;
                            if(p!=null && !p.isEmpty())
                            {
                                price = Float.parseFloat(p);
                            }
                            tableModel.setValueAt(price, row, 1);

                            int n = saveFreqData();
                            sdr.setFreq(n);

                            String freqDesc = freqDesc(n);
                            tableModel.setValueAt(freqDesc, row, 3);

                        }
                    madeChanges = true;

                }
                else
                {
                   JOptionPane.showMessageDialog(this,"This Record cannot be edited", "Message",JOptionPane.ERROR_MESSAGE);
                }
        }
    }//GEN-LAST:event_btnEditRecordActionPerformed

    private void btnServColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnServColorActionPerformed
        // TODO add your handling code here:
        servColor = JColorChooser.showDialog(this, "Choose a color", btnServColor.getBackground()) ;
        if(servColor != null)
            btnServColor.setBackground(servColor);
        
        rgbVal = servColor.getRGB();
    }//GEN-LAST:event_btnServColorActionPerformed

    private void btnSaveServDefActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveServDefActionPerformed
        // TODO add your handling code here:
        
        if(areAllValid())
        {
                try {
                            con.setAutoCommit(false);
                            RegularService rs = addServiceDefinition();
                            con.commit();
                            con.setAutoCommit(true);
                            
                            if(isEditMode == false)
                               rsp.addService(rs);
                            else
                                rsp.updateService(rs);
                            
                            parentWindow.dispose();
                } catch (SQLException ex) {
                    Logger.getLogger(ServiceDefinitionPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btnSaveServDefActionPerformed

    private void radEachNthDyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radEachNthDyActionPerformed
        // TODO add your handling code here:
        clearFreqComponents();
    }//GEN-LAST:event_radEachNthDyActionPerformed

    private void radDaysInWeekActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radDaysInWeekActionPerformed
        // TODO add your handling code here:
                clearFreqComponents();
    }//GEN-LAST:event_radDaysInWeekActionPerformed

    private void radDaysInMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radDaysInMonthActionPerformed
        // TODO add your handling code here:
                clearFreqComponents();

    }//GEN-LAST:event_radDaysInMonthActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        // TODO add your handling code here:
        parentWindow.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void cmbWalletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbWalletActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbWalletActionPerformed

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
            java.util.logging.Logger.getLogger(ServiceDefinitionPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ServiceDefinitionPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ServiceDefinitionPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ServiceDefinitionPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                JFrame  f = new JFrame();
                
                f.setTitle("Service Definition Panel");
                f.add(new ServiceDefinitionPanel(null,null,27));
                f.pack();
                f.setLocationRelativeTo(null);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
                
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel FrequencyPanel;
    private javax.swing.JPanel MiddlePanel;
    private javax.swing.JPanel UpperPanel;
    private javax.swing.JButton btnAddRecord;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnDatePicker;
    private javax.swing.JButton btnDeleteRecord;
    private javax.swing.JButton btnEditRecord;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSaveServDef;
    private javax.swing.JButton btnServColor;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox chkAutoAddRecord;
    private javax.swing.JCheckBox chkFirst;
    private javax.swing.JCheckBox chkLast;
    private javax.swing.JCheckBox chkMiddle;
    private javax.swing.JComboBox<String> cmbWallet;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblStartDate;
    private javax.swing.JPanel monthDatePanel;
    private javax.swing.JPanel outerMonthNamePanel;
    private javax.swing.JRadioButton radDaysInMonth;
    private javax.swing.JRadioButton radDaysInWeek;
    private javax.swing.JRadioButton radEachNthDy;
    private javax.swing.JRadioButton radFstLstMidMonth;
    private javax.swing.JSpinner spnDays;
    private javax.swing.JSpinner spnTime;
    private javax.swing.JTable tabRegServItems;
    private javax.swing.JTextField txtItemName;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtQuantity;
    private javax.swing.JTextField txtServiceName;
    private javax.swing.JTextField txtTime;
    // End of variables declaration//GEN-END:variables
}
