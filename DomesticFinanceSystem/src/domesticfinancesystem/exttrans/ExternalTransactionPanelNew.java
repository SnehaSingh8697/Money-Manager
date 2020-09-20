/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package domesticfinancesystem.exttrans;

import domesticfinancesystem.calendar.CalCellRenderer;
import domesticfinancesystem.calendar.Database;
import domesticfinancesystem.picmanager.PictureManager;
import domesticfinancesystem.periodicdeposit.PeriodicDeposit;
import domesticfinancesystem.periodicdeposit.WalletDetails;
import domesticfinancesystem.wallet.MyWalletPanel;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.AbstractDocument;
import domesticfinancesystem.*;
import domesticfinancesystem.calendar.DatePickerNewDialog;

/**
 *
 * @author sneha
 */
public class ExternalTransactionPanelNew extends javax.swing.JPanel {

    /** Creates new form ExternalTransaction */
    private Database dc;
    private Connection con;
    private ArrayList<PartyDetail> arlParty = new ArrayList<>();
    private ArrayList<WalletData> arlWal = new ArrayList<>();
    private ArrayList<TransDocs> arlTransDocs = new ArrayList<>();
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private DefaultListModel<TransDocs> docModel = new DefaultListModel<TransDocs>();
    private File imageFile;
    private File curfile ;
    private Image docImage;
    private Date fromDate;
    private Date toDate;
    private boolean pdFound;
    private SpinnerNumberModel snm;
    private final int horizontalSize = 81;
    private final int verticalSize = 70;
    private int maxPdNum = 0;
    private int sListId;
    private boolean isExtTransMade;
    private boolean isSListTrans;
    private boolean isRegServTrans;
    private Window parentWindow;
    private boolean imageSelected;
    
    private class MyTableModel extends AbstractTableModel
    {
        final int COLS = 8;
        String[] colNames = {"Date","Party/Purpose","Narration","Paid Amt","Recieved Amt","Mode","Refrence No","Wallet Affected"};
        Class[] colTypes = {String.class,String.class,String.class,Float.class,Float.class,String.class,String.class,String.class} ;
        ArrayList<ExtTransData> arl = new ArrayList<ExtTransData>();

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
            ExtTransData ed = (ExtTransData)arl.get(rowIndex);
            if(columnIndex == 0)
                return formatter.format(ed.getDt());
            else if(columnIndex == 1)
                return ed.getPname();
            else if(columnIndex == 2)
                return ed.getNarration();
            else if(columnIndex == 3)
            {
                return ed.getPaidAmt();
            }
            else if(columnIndex == 4)
            {
               return ed.getRecvAmt();
            }
            else if(columnIndex == 5)
                return ed.getMode();
            else if(columnIndex == 6)
                return ed.getRefNumber();
            else
                return ed.getwName();
        }
        
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            ExtTransData ed = arl.get(rowIndex);
            if(columnIndex == 0)
            {
                try {
                    Date dt = formatter.parse((String)aValue);
                    ed.setDt(dt);
                } catch (ParseException ex) {
                    Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if(columnIndex == 1)
                ed.setPname((String)aValue);
            else if(columnIndex == 2)
                ed.setNarration((String)aValue);
            else if(columnIndex == 3)
                ed.setPaidAmt((Float)aValue);
            else if(columnIndex == 4)
                 ed.setRecvAmt((Float)aValue);
            else if(columnIndex == 5)
              ed.setMode((String)aValue);
            else if(columnIndex == 6)
                ed.setRefNumber((String)aValue);
            else
                ed.setwName((String)aValue);
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
        
        public void addRow(ExtTransData ed)
        {
            arl.add(ed);
        }
        
        public void setRowCount()
        {
            arl.clear();
            fireTableStructureChanged();
        }
    }
    
    public ExternalTransactionPanelNew(String narration,float amount,int listId,Window w)
    {
        this();
        radEntry.doClick();
        radPaidAmt.doClick();
//        if(party!=null && !party.isEmpty())
//            ((JTextField)cmbParty.getEditor().getEditorComponent()).setText(party);
        if(narration!=null && !narration.isEmpty())
            txtNarration.setText(narration);
        if(amount>0)
            txtPaidAmt.setText(""+amount);
        
        cmbParty.setSelectedItem("Shopping");
        sListId = listId;
        
        radShow.setEnabled(false);
        
        btnCancel.setEnabled(false);
        btnDel.setEnabled(false);
        btnAddRow.setEnabled(false);
        
        btnShowImg.setEnabled(true);
        
        isExtTransMade = false;
        
        isSListTrans = true;
        
        parentWindow = w;
       
    }
    public ExternalTransactionPanelNew(Date dt)
    {
        this();
        try {
            tableModel.arl.clear();
            showTransaction(dt);
            radDate.setSelected(true);
            lblDtFromDt.setText(formatter.format(dt));
            lblDtToDate.setText(formatter.format(dt));
            btnDtFromDate.setEnabled(true);
            btnDtToDate.setEnabled(true);
            String d = formatter.format(new GregorianCalendar().getTime());
            Date dat = formatter.parse(d);
            if(dt.compareTo(dat) != 0)
            {
                System.out.println("hey sneha m here");
                radEntry.setEnabled(false);
            }
            else
                radEntry.setEnabled(true);
        } catch (ParseException ex) {
            Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(Level.SEVERE, null, ex);
        }
 
    }
    
    public ExternalTransactionPanelNew(String service,String narration,int amount,int walletId,Window w)
    {
        this();
        radEntry.doClick();
        radPaidAmt.doClick();
        if(service!=null && !service.isEmpty())
            ((JTextField)cmbParty.getEditor().getEditorComponent()).setText(service);
        
        if(narration!=null && !narration.isEmpty())
            txtNarration.setText(narration);
        
        if(amount>0)
            txtPaidAmt.setText(""+amount);
        
        sListId = -1;
        
        radShow.setEnabled(false);
        
        btnCancel.setEnabled(false);
        btnDel.setEnabled(false);
        btnAddRow.setEnabled(false);
        
        isExtTransMade = false;
        
        isRegServTrans = true;
        
        parentWindow = w;
        
        setWalletName(walletId);
    }
    
    private void setWalletName(int walletId)
    {
        for (WalletData walletData : arlWal) {
            
            if(walletData.getId() == walletId)
            {
                cmbWallet.setSelectedItem(walletData.getName());
                break;
            }
        }
    }
   
    
    public boolean isExtTransMade()
    {
        return isExtTransMade;
    }
    
    private Image scaleImage(Image img,int labelHeight,int labelWidth)
    {
        int imageHeight = img.getHeight(null);
        int imageWidth = img.getWidth(null);

       if(imageHeight>imageWidth)
       {
            int newWidth =(int) ((double)labelHeight/imageHeight*imageWidth);
            img = img.getScaledInstance(newWidth, labelHeight, Image.SCALE_SMOOTH);
       }
       else if(imageHeight<imageWidth)
       {
          int newHeight =(int) ((double)labelWidth/imageWidth*imageHeight);
          img = img.getScaledInstance( labelWidth,newHeight, Image.SCALE_SMOOTH); 
       }
       else
       {
          int newWidth =(int) ((double)labelHeight/imageHeight*imageWidth);
          img = img.getScaledInstance(newWidth, labelHeight, Image.SCALE_SMOOTH); 

       } 
       return img;
    }
    
    public Date subtractDate(Date dt, int days)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(dt);
        cal.add(Calendar.DATE, -days);
        return cal.getTime();
    }
    
    private  BufferedImage imageToBufferedImage(Image img)
    {
        BufferedImage bImg = null ;
        
        if(img instanceof BufferedImage)
            bImg = (BufferedImage) img ;
        else
        {
            bImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB) ;
            Graphics g = bImg.createGraphics() ;
            g.drawImage(img, 0, 0, null) ;
            g.dispose();
        }
        return bImg ;
    }
    
    public void showTransaction(Date dat)
    {
        tableModel.arl.clear();
        String sql = "";
        PreparedStatement  pstmt = null;
       
            try {
                if(dat == null)
                {
                    if(radToday.isSelected())
                    {
                            Date date = todayDate();
                            java.sql.Date sdt = new java.sql.Date(date.getTime());
                            sql = "Select * from ExtTrans where Dt = ? order by Dt"; 
                            pstmt =  con.prepareStatement(sql);
                            pstmt.setDate(1,sdt);

                    }
                    else
                    {
                        sql = "Select * from ExtTrans where Dt between ? AND ? order by Dt";
                        pstmt =  con.prepareStatement(sql);
                        java.sql.Date sfromDate = new java.sql.Date(fromDate.getTime());
                        pstmt.setDate(1,sfromDate);
                        java.sql.Date sToDate = new java.sql.Date(toDate.getTime());
                        pstmt.setDate(2,sToDate);

                    }
                }
                else
                {
                    java.sql.Date sdt = new java.sql.Date(dat.getTime());
                    sql = "Select * from ExtTrans where Dt = ? order by Dt"; 
                    pstmt =  con.prepareStatement(sql);
                    pstmt.setDate(1,sdt);
                    
                }
                
//                String sql = "Select * from ExtTrans where Dt = to_date('10/04/2019','dd/mm/yyyy')";
                
                ResultSet rst = pstmt.executeQuery();
                while(rst.next())
                {
                    int id = rst.getInt("Id");
                    Date dt = rst.getDate("Dt");
                    GregorianCalendar cal = new GregorianCalendar() ;
                    cal.setTime(dt);
                    int ppId = rst.getInt("PPId");
                    String narration = rst.getString("Narration");
                    int walletId = rst.getInt("WalletId");
                    float amount = rst.getFloat("Amount");
                    String mode = rst.getString("ModeNo");
                    String referenceNo = rst.getString("ReferenceNo");
                    float paidAmount;
                    float recAmount;
                    if(amount > 0)
                    {
                        paidAmount = 0;
                        recAmount = amount;
                    }
                    else
                    {
                        paidAmount = -amount;
                        recAmount = 0;
                    }
                    
                    String s= "Select Name from PP where Id = ?";
                    PreparedStatement  pst =  con.prepareStatement(s);
                    pst.setInt(1, ppId);
                    ResultSet rs = pst.executeQuery();
                    String partyName = "";
                    if(rs.next())
                    {
                        partyName = rs.getString("Name");
                    }
                    rs.close();
                    pst.close();
                    
                    s= "Select Name from Wallet where Id = ?";
                    pst =  con.prepareStatement(s);
                    pst.setInt(1, walletId);
                    rs = pst.executeQuery();
                    String walletName = "";
                    if(rs.next())
                    {
                        walletName = rs.getString("Name");
                    }
                    rs.close();
                    pst.close();
                    
                    s= "Select * from Transdocs where ExTransId = ?";
                    pst =  con.prepareStatement(s);
                    pst.setInt(1,id);
                    rs = pst.executeQuery();
                    while(rs.next())
                    {
                        int docId = rs.getInt("Id");
                        int exTransId = rs.getInt("ExTransId");
                        String name = rs.getString("Name");
                        Blob blob = rs.getBlob("Pic");
                        Image img = null;
                        if(blob !=null)
                        {
                           InputStream in = blob.getBinaryStream();
                           img = ImageIO.read(in);
                        }
                        TransDocs td = new TransDocs(id, exTransId, name, img);
                        arlTransDocs.add(td);
                    }
                    rs.close();
                    pst.close();
                    
                    String modeName = getModeCharToString(mode);
                    ExtTransData ed = new ExtTransData(dt, ppId, partyName, narration, walletId, paidAmount, recAmount, modeName, referenceNo, walletName, id);
                    tableModel.addRow(ed);
                    
                    int row = tableModel.getRowCount() - 1;
                    tableModel.fireTableRowsInserted(row, row);
                    tabExtTrans.setRowSelectionInterval(row, row);
                    
                    
                }
               
                
                rst.close();
                pstmt.close();
            } catch (SQLException | IOException ex) {
                Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
     private void setImageToLabel(Image img,JLabel lbl)
     {
         if(img == null)
         {
             lbl.setIcon(null);
             return;
         }
         int height = verticalSize;
         int width = horizontalSize;
         img = scaleImage(img,height,width);
         ImageIcon icon = new ImageIcon(img);
         lbl.setIcon(icon);
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
    
    public boolean checkValidity()
    {
        String mode = (String)cmbMode.getSelectedItem();
        if(mode == null || mode.isEmpty())
        {
          JOptionPane.showMessageDialog(this, "Mode not selected", "Message", JOptionPane.ERROR_MESSAGE);
          return false;
        }
        String party = (String)cmbParty.getSelectedItem();
        if(party == null || party.isEmpty())
        {
          JOptionPane.showMessageDialog(this, "Party/Purpose not selected", "Message", JOptionPane.ERROR_MESSAGE);
          return false;
        }
        
        if(radPaidAmt.isSelected() && (txtPaidAmt.getText() == null || txtPaidAmt.getText().isEmpty()))
        {
            JOptionPane.showMessageDialog(this, "Amount not entered", "Message", JOptionPane.ERROR_MESSAGE);
            txtPaidAmt.setText("");
            txtPaidAmt.setFocusable(true);
            return false; 
        }
        
        if(radRecvAmt.isSelected() && (txtRecievedAmt.getText() == null || txtRecievedAmt.getText().isEmpty()))
        {
            JOptionPane.showMessageDialog(this, "Amount not entered", "Message", JOptionPane.ERROR_MESSAGE);
            txtRecievedAmt.setText("");
            txtRecievedAmt.setFocusable(true);
            return false; 
        }
        float paidAmount = Float.parseFloat(txtPaidAmt.getText());
        float recAmount = Float.parseFloat(txtRecievedAmt.getText());
        
        if(radPaidAmt.isSelected() && paidAmount == 0)
        {
            JOptionPane.showMessageDialog(this, "Amount not entered", "Message", JOptionPane.ERROR_MESSAGE);
            txtPaidAmt.setText("");
            txtPaidAmt.setFocusable(true);
            return false; 
        }
        
        if(radRecvAmt.isSelected() && recAmount == 0)
        {
            JOptionPane.showMessageDialog(this, "Amount not entered", "Message", JOptionPane.ERROR_MESSAGE);
            txtRecievedAmt.setText("");
            txtRecievedAmt.setFocusable(true);
            return false; 
        }
        
        
        String wallet = (String)cmbWallet.getSelectedItem();
        if(wallet == null || wallet.isEmpty())
        {
          JOptionPane.showMessageDialog(this, "Wallet not selected", "Message", JOptionPane.ERROR_MESSAGE);
          return false;
        }
        
        String ref = txtRefNum.getText();
        if(ref!= null && !ref.isEmpty())
        {
           int refNum = Integer.parseInt(ref);
           if(refNum == 0 || refNum<0)
           {
              JOptionPane.showMessageDialog(this, "Invalid Refrence Number", "Message", JOptionPane.ERROR_MESSAGE);
              txtRefNum.setText("");
              txtRefNum.setFocusable(true);
              return false;
           }
            
        }
        String name = (String)cmbParty.getSelectedItem();
        boolean found = false;
        for (PartyDetail pd : arlParty) {
           if(pd.getName().equals(name))
           {
               found = true;
               break;
           }
        }
        if(found == false)
        {
            int type = JOptionPane.showConfirmDialog(this,"Would you like to add new party/purpose?", "Message",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(type == JOptionPane.YES_OPTION)
                 addNewPartyPurpose(name);
            else
            {
                 cmbParty.requestFocus();
                 return false;
            }
        }
        if(radPaidAmt.isSelected())
        {
            boolean valid = checkWalletBal();
            if(valid == false)
            {
               String wname = (String)cmbWallet.getSelectedItem();
               JOptionPane.showMessageDialog(this, wname+" has insufficient balance", "Message", JOptionPane.ERROR_MESSAGE);
               cmbWallet.requestFocus();
               return false;
            }
        }
        String docName = txtDocName.getText().trim();
        if(imageSelected == true &&(docName.isEmpty() || docName == null))
        {
          JOptionPane.showMessageDialog(this, "Please enter the name of the refrence document", "Message", JOptionPane.ERROR_MESSAGE);
          txtDocName.requestFocus();
          return false;

        }
        if(imageSelected == false && docName != null && !docName.isEmpty())
        {
            System.out.println("doc not selected");
            JOptionPane.showMessageDialog(this, "Please select a refrence document", "Message", JOptionPane.ERROR_MESSAGE);
            btnBrowse.requestFocus();
            return false;
        }
        return true;
    }
    
    public void addNewPartyPurpose(String name)
    {
        try {
                String sql = "Select seq.nextval from dual";
                Statement stmt =  con.createStatement();
                ResultSet rst = stmt.executeQuery(sql);
                int id = 0;
                if(rst.next())
                {
                    id = rst.getInt(1);
                    
                }
                rst.close();
                stmt.close();
                
                cmbParty.addItem(name);
                
                sql = "Insert into PP(Id,Name) values(?,?)";
                PreparedStatement pstmt =  con.prepareStatement(sql);
                pstmt.setInt(1, id);
                pstmt.setString(2, name);
                pstmt.executeUpdate();
                pstmt.close();
                
                PartyDetail pd = new PartyDetail(id, name);
                arlParty.add(pd);
            } catch (SQLException ex) {
                Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    public void updateSummaryTable(java.sql.Date dt,float amount,boolean isExpense)
    {
        try {
            String sql = "Select * from Summary where dt = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setDate(1, dt);
            ResultSet rst = pstmt.executeQuery();
            if(rst.next())
            {
                float income = rst.getFloat("Income");
                float expense = rst.getFloat("Expense");
                int id = rst.getInt("Id");
                String s;
                PreparedStatement pst;
                float a = 0;
                if(isExpense == true)
                {
                    a = expense - amount;
                    s = "Update Summary set Expense = ? where id = ?";
                }
                else
                {
                    a = income - amount;
                    s = "Update Summary set Income = ? where id = ?";
                }
                pst = con.prepareStatement(s);
                pst.setFloat(1, a);
                pst.setInt(2, id);
                pst.executeUpdate();
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void makeEntryInSummaryTable(java.sql.Date dt,float amount)
    {
        try {
            boolean isExpense = false;
            if(amount<0)
            {
                amount = -amount;
                isExpense = true;
            }
            String sql = "Select * from Summary where dt = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setDate(1, dt);
            ResultSet rst = pstmt.executeQuery();
            if(rst.next())
            {
                float income = rst.getInt("Income");
                float expense = rst.getInt("Expense");
                int id = rst.getInt("Id");
                String s;
                PreparedStatement pst;
                float a = 0;
                if(isExpense == true)
                {
                    a = expense+amount;
                    s = "Update Summary set Expense = ? where id = ?";
                }
                else
                {
                    a = income+amount;
                    s = "Update Summary set Income = ? where id = ?";
                }
                pst = con.prepareStatement(s);
                pst.setFloat(1, a);
                pst.setInt(2, id);
                pst.executeUpdate();
            }
            else
            {
                if(isExpense)
                {
                   String s = "Insert into Summary(Id,dt,Income,Expense) values(seq.nextval,?,?,?)";
                   PreparedStatement pst = con.prepareStatement(s);
                   pst.setDate(1, dt);
                   pst.setFloat(2, 0);
                   pst.setFloat(3, amount);
                   pst.executeQuery();
                }
                else
                {
                   String s = "Insert into Summary(Id,dt,Income,Expense) values(seq.nextval,?,?,?)";
                   PreparedStatement pst = con.prepareStatement(s);
                   pst.setDate(1, dt);
                   pst.setFloat(2, amount);
                   pst.setFloat(3, 0);
                   pst.executeQuery();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
     
     
    public void enabledDisabled()
    {
        boolean val = radEntry.isSelected();
        
        radToday.setEnabled(!val);
        radPeriod.setEnabled(!val);
        btnFromDate.setEnabled(!val);
        btnToDate.setEnabled(!val);
        radDate.setEnabled(!val);
        btnDtFromDate.setEnabled(!val);
        btnDtToDate.setEnabled(!val);
        spnPdNum.setEnabled(!val);
        
        clearComponents(val);
        
        txtDocName.setEnabled(val);
        btnBrowse.setEnabled(val);
        btnShowImg.setEnabled(val);
        
        btnShowImg.setEnabled(true);
        
        btnSave.setEnabled(val);
        btnDel.setEnabled(val);
        btnCancel.setEnabled(val);
        btnAddRow.setEnabled(val);
        
    }
     
    private MyTableModel tableModel = new MyTableModel();
    
    
    public void addMode()
    {
        cmbMode.addItem("Cash");
        cmbMode.addItem("Credit Card");
        cmbMode.addItem("Debit Card");
        cmbMode.addItem("Cheque");
    }
    
    public void getWalletInfo()
    {
      try {
            cmbWallet.removeAllItems();
            arlWal.clear();
            String sql = "Select Id,Name from Wallet";
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
    
    public void getWalletInfoForPayment()
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
            Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }
   
    
    public void getPartyInfo()
    {
        try {
            String sql = "Select Id,Name from PP";
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            while(rst.next())
            {
                int id = rst.getInt(1);
                String name = rst.getString(2);
                PartyDetail pd = new PartyDetail(id, name);
                arlParty.add(pd);
                cmbParty.addItem(name);
            }
        } catch (SQLException ex) {
            Logger.getLogger(PeriodicDeposit.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }
    
    public static boolean extTransExists(Date dt)
    {
        try {
            String sql = "Select count(Id) from Exttrans";
            boolean found = false;
            ResultSet rst ;
            PreparedStatement pstmt = null;
            Statement stmt = null;
            if(dt!=null)
            {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

                String d = formatter.format(dt);
                Date dat = formatter.parse(d);
                java.sql.Date sdt = new java.sql.Date(dat.getTime());
                sql+=" where Dt = ?";
                pstmt = MainFrame.con.prepareStatement(sql);
                pstmt.setDate(1, sdt);
                rst = pstmt.executeQuery();
            }
            else
            {
                stmt = MainFrame.con.createStatement();
                rst = stmt.executeQuery(sql);
            }
            if(rst.next())
            {
                int val = rst.getInt(1);
                if(val == 0)
                    found = false;
                else
                    found = true;
            }
            rst.close();
            if(stmt!=null)
               stmt.close();
            if(pstmt!=null)
               pstmt.close();
            
            return found;
        } catch (SQLException | ParseException ex) {
            Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public ExternalTransactionPanelNew() {
        initComponents();
        
        isSListTrans = false;
        isRegServTrans = false;

        dc = MainFrame.dc;
        con = dc.createConnection();
        
        ((AbstractDocument)txtPaidAmt.getDocument()).setDocumentFilter(new IntegerDocumentFilter());
        ((AbstractDocument)txtRecievedAmt.getDocument()).setDocumentFilter(new IntegerDocumentFilter());
        ((AbstractDocument)txtRefNum.getDocument()).setDocumentFilter(new AlphaNumericFilter());

        
        pdFound = false;
        
        FileSystemView fsv = FileSystemView.getFileSystemView() ;
        curfile = fsv.getHomeDirectory() ;
        
        lstRefDoc.setModel(docModel);
        lstRefDoc.setCellRenderer(new PictPanelListCellRenderer());
        
        tabExtTrans.setModel(tableModel);
        tabExtTrans.setRowHeight(25);
        tabExtTrans.setShowGrid(true); //to show table border for each cell
        ((DefaultTableCellRenderer)tabExtTrans.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        
        cmbParty.setEditable(true);
        
        sListId = -1;
        
        tabExtTrans.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                int row = tabExtTrans.getSelectedRow() ;
                
                if(row != -1)
                {
                    ExtTransData ed = tableModel.arl.get(row);
                    getExtTransData(ed);
                    docModel.clear();
                    lblPic.setIcon(null);
                    txtDocName.setText("");
                    
                    for (TransDocs td : arlTransDocs) {
                        
                        if(td.getExTransId() == ed.getId())
                        {
                            docModel.addElement(td);
                            int ind = docModel.indexOf(td);
                            lstRefDoc.setSelectedIndex(ind);
                            setDocument(td);
                        }
                        
                    }
                }
                
            }
        });
        getWalletInfoForPayment();
        addMode();
        getPartyInfo();  
        if(extTransExists(null))
        {
            radShow.doClick();
        }
        else
        {
            radEntry.doClick();
        }
    }
    
    public void disableComponents()
    {
        cmbMode.setEnabled(false);
        cmbParty.setEnabled(false);
        cmbWallet.setEnabled(false);
        txtNarration.setEnabled(false);
        txtPaidAmt.setEnabled(false);
        txtRecievedAmt.setEnabled(false);
        radPaidAmt.setEnabled(false);
        radRecvAmt.setEnabled(false);
        txtRefNum.setEnabled(false);
    }
    
    public boolean checkWalletBal()
    {
            float amt = Float.parseFloat(txtPaidAmt.getText());
            String wname = (String)cmbWallet.getSelectedItem();
            int wid = -1;
            for (WalletData wd : arlWal) {
                if(wname.equals(wd.getName()))
                {
                    wid = wd.getId();
                    break;
                }
            }
            String mode = (String)cmbMode.getSelectedItem();
            String sql = "";
            if(mode.equals("Cash"))
            {
                sql = "Select Liquidbal from Wallet where Id = "+wid;
            }
            else
            {
                sql = "Select Digitalbal from Wallet where Id = "+wid;
            }
        try {
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            float bal = 0;
            if(rst.next())
            {
                bal = rst.getFloat(1);
            }
            if(amt>bal)
                return false;
            } catch (SQLException ex) {
            Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(Level.SEVERE, null, ex);
        }
         return true;
    }
    
    public void updateWallet(int walletId,float amount,String mode,boolean isDel)
    {
        
            try {
                System.out.println("Wallet id: "+walletId);
                System.out.println("ch mode: "+mode);
                String sql;
                if(mode.equals("C"))
                    sql = "Select Liquidbal from Wallet where Id = ?";
                else
                    sql = "Select Digitalbal from Wallet where Id = ?";
                PreparedStatement pstmt = con.prepareStatement(sql);  
                pstmt.setInt(1, walletId);
                ResultSet rst = pstmt.executeQuery();
                float bal = 0;
                if(rst.next())
                {
                    bal = rst.getFloat(1);
                }
                System.out.println("amount: "+amount);
//                if(isDel == true && amount < 0 )
//                {
//                    amount = -(amount);
//                }
//                if(isDel == true && amount > 0 )
//                {
//                    amount = -(amount);
//                }
            //Deleting the external transaction so  subtract transaction amount from the balance in any case
//               amount = -amount;
//                bal += amount;
                if(isDel)
                bal = bal - amount;
                else
                   bal = bal + amount;
                rst.close();
                pstmt.close();
                if(mode.equals("C"))
                    sql = "Update Wallet set Liquidbal = ? where Id = ?";
                else
                    sql = "Update Wallet set Digitalbal = ? where Id = ?";
                
                pstmt = con.prepareStatement(sql);
                pstmt.setFloat(1, bal);
                pstmt.setInt(2, walletId);
                pstmt.executeUpdate();
                pstmt.close();
                
                
            }
            catch (SQLException ex) {
                Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    public void clearComponents(boolean val)
    {
        cmbMode.setSelectedIndex(0);
        cmbMode.setEnabled(val);
        
        if(arlParty.size()>0)
           cmbParty.setSelectedIndex(0);
        
        cmbParty.setEnabled(val);
        
        if(arlWal.size()>0)
            cmbWallet.setSelectedIndex(0);
        
        cmbWallet.setEnabled(val);
        
        txtNarration.setText("");
        txtNarration.setEnabled(val);
        txtPaidAmt.setText("");
        txtPaidAmt.setEnabled(val);
        txtRecievedAmt.setText("");
        txtRecievedAmt.setEnabled(val);
        txtRefNum.setText("");
        txtRefNum.setEnabled(val);
        txtDocName.setText("");
        txtDocName.setEnabled(val);
        
        radPaidAmt.setEnabled(val);
        radRecvAmt.setEnabled(val);
        
        lblPic.setIcon(null);
        
        if(val == true)
        {
//            radPaidAmt.setEnabled(true);
//            radPaidAmt.setSelected(true);
//            txtPaidAmt.setText("");
//            txtPaidAmt.setEnabled(true);
//            txtRecievedAmt.setText("0");
//            txtRecievedAmt.setEnabled(false);
            radPaidAmt.doClick();
            
        }
       
    }
    
    public void clearDateLabels()
    {
        lblDtFromDt.setText("");
        lblDtToDate.setText("");
        lblFromDate.setText("");
        lblToDate.setText("");
        
        tableModel.setRowCount();
        lblPic.setIcon(null);
        txtDocName.setText("");
        docModel.clear();
        arlTransDocs.clear();
        tabExtTrans.clearSelection();
        tableModel.arl.clear();
    }
    
    
    public void setPdDates()
    {
        try
        {
            int pdNum = (int)spnPdNum.getValue();
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
            
            lblToDate.setText(""+formatter.format(toDate));
        } catch (SQLException ex) {
            Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setDocument(TransDocs td)
    {
        txtDocName.setText(td.getName());
        setImageToLabel(td.getImg(), lblPic);
    }
    
    public void getExtTransData(ExtTransData ed)
    {
        cmbParty.setSelectedItem(ed.getPname());
        txtNarration.setText(ed.getNarration());
        float paidAmt = ed.getPaidAmt();
        if(paidAmt>0)
        {
            radPaidAmt.setSelected(true);
            txtPaidAmt.setText(""+paidAmt);
            txtRecievedAmt.setText("0");
            txtRecievedAmt.setEnabled(false);
        }
        else
        {
            radRecvAmt.setSelected(true);
            txtPaidAmt.setText("0");
            txtPaidAmt.setEnabled(false);
            txtRecievedAmt.setText(""+ed.getRecvAmt());
            txtRecievedAmt.setEnabled(false);
        }
        cmbWallet.setSelectedItem(ed.getwName());
        String refNum = ed.getRefNumber();
        txtRefNum.setText(refNum);
        cmbMode.setSelectedItem(getModeCharToString(ed.getMode()));
    }
    
    public String getModeStringToChar()
    {
        String mode = (String)cmbMode.getSelectedItem();
        String c;
        if(mode.equals("Cash"))
            c = "C";
        else if(mode.equals("Credit Card"))
            c = "R";
        else if(mode.equals("Debit Card"))
            c = "D";
        else//if cheque
            c = "H";
         
       return c;
    }
    public String getModeCharToString(String s)
    {
        if(s.equals("C"))
           return "Cash";
        else if(s.equals("R"))
            return "Credit Card";
        else if(s.equals("D"))
            return "Debit Card";
        else//if cheque
           return "Cheque";
    }
    
    public String convertModeStringToChar(String s)
    {
        if(s.equals("Cash"))
           return "C";
        else if(s.equals("Credit Card"))
            return "R";
        else if(s.equals("Debit Card"))
            return "D";
        else//if cheque
           return "H";
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        radgrpShowEntry = new javax.swing.ButtonGroup();
        btngrpDt = new javax.swing.ButtonGroup();
        btnAmtType = new javax.swing.ButtonGroup();
        upperPanel = new javax.swing.JPanel();
        radEntry = new javax.swing.JRadioButton();
        radShow = new javax.swing.JRadioButton();
        middlePanel = new javax.swing.JPanel();
        radToday = new javax.swing.JRadioButton();
        radPeriod = new javax.swing.JRadioButton();
        spnPdNum = new javax.swing.JSpinner();
        lblFromDate = new javax.swing.JLabel();
        btnFromDate = new javax.swing.JButton();
        lblToDate = new javax.swing.JLabel();
        btnToDate = new javax.swing.JButton();
        radDate = new javax.swing.JRadioButton();
        lblDtFromDt = new javax.swing.JLabel();
        btnDtFromDate = new javax.swing.JButton();
        lblDtToDate = new javax.swing.JLabel();
        btnDtToDate = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabExtTrans = new javax.swing.JTable();
        panelTransDetails = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cmbParty = new javax.swing.JComboBox<>();
        Narration = new javax.swing.JLabel();
        txtPaidAmt = new javax.swing.JTextField();
        txtNarration = new javax.swing.JTextField();
        radPaidAmt = new javax.swing.JRadioButton();
        radRecvAmt = new javax.swing.JRadioButton();
        txtRecievedAmt = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        cmbMode = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        txtRefNum = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        cmbWallet = new javax.swing.JComboBox<>();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstRefDoc = new javax.swing.JList<>();
        btnDelDoc = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        txtDocName = new javax.swing.JTextField();
        lblPic = new javax.swing.JLabel();
        btnBrowse = new javax.swing.JButton();
        btnShowImg = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnDel = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        btnAddRow = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();

        radgrpShowEntry.add(radEntry);
        radEntry.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radEntry.setText("Entry");
        radEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radEntryActionPerformed(evt);
            }
        });

        radgrpShowEntry.add(radShow);
        radShow.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radShow.setText("Show");
        radShow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radShowActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout upperPanelLayout = new javax.swing.GroupLayout(upperPanel);
        upperPanel.setLayout(upperPanelLayout);
        upperPanelLayout.setHorizontalGroup(
            upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upperPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(radEntry, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(radShow, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(628, Short.MAX_VALUE))
        );
        upperPanelLayout.setVerticalGroup(
            upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upperPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radEntry)
                    .addComponent(radShow))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        middlePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Show Transactions for", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 153))); // NOI18N

        btngrpDt.add(radToday);
        radToday.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radToday.setText("Today");
        radToday.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radTodayActionPerformed(evt);
            }
        });

        btngrpDt.add(radPeriod);
        radPeriod.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radPeriod.setText("Period");
        radPeriod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radPeriodActionPerformed(evt);
            }
        });

        spnPdNum.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnPdNumStateChanged(evt);
            }
        });

        lblFromDate.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblFromDate.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnFromDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/calendar.png"))); // NOI18N
        btnFromDate.setToolTipText("Date Picker");
        btnFromDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFromDateActionPerformed(evt);
            }
        });

        lblToDate.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblToDate.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnToDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/calendar.png"))); // NOI18N
        btnToDate.setToolTipText("Date Picker");
        btnToDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnToDateActionPerformed(evt);
            }
        });

        btngrpDt.add(radDate);
        radDate.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radDate.setText("Date");
        radDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radDateActionPerformed(evt);
            }
        });

        lblDtFromDt.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDtFromDt.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnDtFromDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/calendar.png"))); // NOI18N
        btnDtFromDate.setToolTipText("Date Picker");
        btnDtFromDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDtFromDateActionPerformed(evt);
            }
        });

        lblDtToDate.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDtToDate.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnDtToDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/calendar.png"))); // NOI18N
        btnDtToDate.setToolTipText("Date Picker");
        btnDtToDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDtToDateActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout middlePanelLayout = new javax.swing.GroupLayout(middlePanel);
        middlePanel.setLayout(middlePanelLayout);
        middlePanelLayout.setHorizontalGroup(
            middlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(middlePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(middlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(middlePanelLayout.createSequentialGroup()
                        .addGroup(middlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(middlePanelLayout.createSequentialGroup()
                                .addComponent(radPeriod, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(spnPdNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, middlePanelLayout.createSequentialGroup()
                                .addComponent(radDate, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblDtFromDt, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(middlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDtFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(middlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblDtToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(middlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDtToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(radToday, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(269, Short.MAX_VALUE))
        );
        middlePanelLayout.setVerticalGroup(
            middlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(middlePanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(radToday, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(middlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spnPdNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(middlePanelLayout.createSequentialGroup()
                        .addComponent(radPeriod, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(radDate))
                    .addGroup(middlePanelLayout.createSequentialGroup()
                        .addGroup(middlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnFromDate)
                            .addComponent(btnToDate)
                            .addComponent(lblFromDate, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(middlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDtToDate, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblDtFromDt, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDtFromDate)
                            .addComponent(btnDtToDate)))))
        );

        tabExtTrans.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N
        jScrollPane1.setViewportView(tabExtTrans);

        panelTransDetails.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Enter Transaction Details", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 153))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel1.setText("Party/Purporse");

        cmbParty.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N
        cmbParty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbPartyActionPerformed(evt);
            }
        });

        Narration.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        Narration.setText("Narration");

        txtPaidAmt.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N

        txtNarration.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N
        txtNarration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNarrationActionPerformed(evt);
            }
        });

        btnAmtType.add(radPaidAmt);
        radPaidAmt.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radPaidAmt.setText("Paid Amount");
        radPaidAmt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radPaidAmtActionPerformed(evt);
            }
        });

        btnAmtType.add(radRecvAmt);
        radRecvAmt.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radRecvAmt.setText("Recieved Amount");
        radRecvAmt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radRecvAmtActionPerformed(evt);
            }
        });

        txtRecievedAmt.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N

        jLabel2.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel2.setText("Mode");

        cmbMode.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N

        jLabel3.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel3.setText("Refrence No.");

        txtRefNum.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N
        txtRefNum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtRefNumActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel4.setText("Wallet Affected");

        cmbWallet.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N
        cmbWallet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbWalletActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelTransDetailsLayout = new javax.swing.GroupLayout(panelTransDetails);
        panelTransDetails.setLayout(panelTransDetailsLayout);
        panelTransDetailsLayout.setHorizontalGroup(
            panelTransDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTransDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTransDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radRecvAmt)
                    .addGroup(panelTransDetailsLayout.createSequentialGroup()
                        .addGroup(panelTransDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(radPaidAmt, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(32, 32, 32)
                        .addGroup(panelTransDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbWallet, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelTransDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtPaidAmt, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                                .addComponent(txtNarration, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                                .addComponent(cmbParty, 0, 134, Short.MAX_VALUE)
                                .addComponent(txtRecievedAmt)
                                .addComponent(txtRefNum)
                                .addComponent(cmbMode, 0, 134, Short.MAX_VALUE))))
                    .addComponent(Narration, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(53, Short.MAX_VALUE))
        );
        panelTransDetailsLayout.setVerticalGroup(
            panelTransDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTransDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTransDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbParty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelTransDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Narration)
                    .addComponent(txtNarration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelTransDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPaidAmt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radPaidAmt))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelTransDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radRecvAmt)
                    .addComponent(txtRecievedAmt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelTransDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cmbWallet)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelTransDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelTransDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtRefNum, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(26, 26, 26))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Refrence Document(s)", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 153))); // NOI18N

        lstRefDoc.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstRefDocValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(lstRefDoc);

        btnDelDoc.setText("Delete Ref Docs");
        btnDelDoc.setToolTipText("Delete");
        btnDelDoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelDocActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel5.setText("Name");

        txtDocName.setFont(new java.awt.Font("Garamond", 0, 11)); // NOI18N

        lblPic.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnBrowse.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnBrowse.setText("Browse...");
        btnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseActionPerformed(evt);
            }
        });

        btnShowImg.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnShowImg.setText("Show Original Image");
        btnShowImg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowImgActionPerformed(evt);
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
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPic, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtDocName, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnBrowse)
                            .addComponent(btnShowImg, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 30, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnDelDoc)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(71, 71, 71)
                        .addComponent(btnDelDoc))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDocName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(btnBrowse)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnShowImg))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(lblPic, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/save.png"))); // NOI18N
        btnSave.setToolTipText("Save Row");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnDel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/delete.gif"))); // NOI18N
        btnDel.setToolTipText("Delete Row");
        btnDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelActionPerformed(evt);
            }
        });

        btnCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/cancel.png"))); // NOI18N
        btnCancel.setToolTipText("Cancel Row");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        btnAddRow.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/Add.gif"))); // NOI18N
        btnAddRow.setToolTipText("Add Row");
        btnAddRow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddRowActionPerformed(evt);
            }
        });

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/external transaction.png"))); // NOI18N
        jLabel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 204, 255)));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(upperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(panelTransDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(btnAddRow)
                                        .addGap(17, 17, 17)
                                        .addComponent(btnCancel)
                                        .addGap(14, 14, 14)
                                        .addComponent(btnDel)
                                        .addGap(18, 18, 18)
                                        .addComponent(btnSave)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(middlePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 682, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(21, 21, 21)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 584, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(57, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(upperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(jLabel6))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(middlePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(panelTransDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 247, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(btnAddRow)
                                    .addComponent(btnCancel)
                                    .addComponent(btnDel)
                                    .addComponent(btnSave)))
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(22, Short.MAX_VALUE))
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
                Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        fromDate = dt;
        dlg.dispose();
        showExtTransactions();
    }//GEN-LAST:event_btnFromDateActionPerformed

    private void radEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radEntryActionPerformed
        // TODO add your handling code here:
        clearDateLabels();
        enabledDisabled();
        
    }//GEN-LAST:event_radEntryActionPerformed

    private void txtNarrationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNarrationActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNarrationActionPerformed

    private void txtRefNumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRefNumActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtRefNumActionPerformed

    private void btnDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelActionPerformed
        // TODO add your handling code here:
        int row = tabExtTrans.getSelectedRow() ;
        if(row != -1)
        {
            try {
                con.setAutoCommit(false);
                
                ExtTransData ed = tableModel.arl.get(row);
                int id = ed.getId();
                
                //delete from transdocs if any rcord exists
                
                String sql = "Delete from TransDocs where ExTransId = ?";
                PreparedStatement pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, id);
                pstmt.executeQuery();
                pstmt.close();
                
                int size = docModel.getSize();
                
                for (int i = 0; i < size; i++) {
                    if(docModel.get(i).getExTransId() == id)
                    {
                        
                        docModel.remove(i);
                    }
                }
                
                //delete from external transaction
                
                sql = "Delete from ExtTrans where Id = ?";
                pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                pstmt.close();
                
                tableModel.arl.remove(row);
                tableModel.fireTableRowsDeleted(row, row);
                
                int rows = tabExtTrans.getRowCount() ;
                if(rows > 0)
                {
                    if(row == rows)
                        row = rows - 1 ;

                    tabExtTrans.setRowSelectionInterval(row, row);
                }
                
                //updating the wallet
                
                int walletId = ed.getWalletId();
                float amount;
                boolean isExpense;
                if(ed.getPaidAmt()!=0)
                {
                    amount = ed.getPaidAmt();
                    amount = - amount;
                    isExpense = true;
                }
                else
                {
                    amount = ed.getRecvAmt();
                    isExpense = false;
                }
                String ch = convertModeStringToChar(ed.getMode());
                updateWallet(walletId, amount, ch, true);
                
                //updating summary table
                Date date = todayDate();
                java.sql.Date sdt = new java.sql.Date(date.getTime());
                updateSummaryTable(sdt, amount, isExpense);
                
                clearComponents(false);
                
                con.commit();
                con.setAutoCommit(true);
                
                 if(ch.equals("C"))
                    MainFrame.currentLiqBalance-=amount;
                else
                   MainFrame.currentDigitBalance-=amount; 
                 
                 getBalaceMeterPercent();
                 
                 imageSelected = false;
                
                
            } catch (SQLException ex) {
                Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }//GEN-LAST:event_btnDelActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        // TODO add your handling code here:
        if(checkValidity() == true)
        {
            try {
                int row = tableModel.getRowCount();
                
                Date date = todayDate();
                
                String partyName = (String)cmbParty.getSelectedItem();
                int partyId = 0;
                for(PartyDetail pd:arlParty)
                {
                    if(pd.getName().equals(partyName))
                    {
                        partyId = pd.getId();
                        break;
                    }
                }
                
                String narration = txtNarration.getText();
                
                float paidAmount = Float.parseFloat(txtPaidAmt.getText());
                
                float recvAmount = Float.parseFloat(txtRecievedAmt.getText());
                
                String c = getModeStringToChar();
//                String mode = (String)cmbMode.getSelectedItem();
                
                String ref = txtRefNum.getText();
               
                String walletName = (String)cmbWallet.getSelectedItem();
                int walletId = 0;
                for(WalletData wd:arlWal)
                {
                    if(wd.getName().equals(walletName))
                    {
                        walletId = wd.getId();
                        break;
                    }
                }
                
                
                //Making entry in database
                
                con.setAutoCommit(false);
                String sql;
                java.sql.Date sdt = new java.sql.Date(date.getTime());
                PreparedStatement pstmt = null;
                
                Calendar cd = Calendar.getInstance();
                int secs = cd.get(Calendar.HOUR_OF_DAY) * 3600 + cd.get(Calendar.MINUTE)*60 + cd.get(Calendar.SECOND);
                
                
                //fetching the id for external transaction
                
               int id = MainFrame.getIdFromDual();
                
                sql = "Insert into ExtTrans(Id,Dt,NoOfSeconds,PPId,Narration,WalletId,Amount,ModeNo,ReferenceNo,SListId) ";
                sql+="values(?,?,?,?,?,?,?,?,?,?)";

//                sql = "Insert into ExtTrans(Id,Dt,DtString,PPId,Narration,WalletId,Amount,ModeNo,ReferenceNo) ";
//                sql+="values(seq.nextval,?,?,?,?,?,?,?,?)";
                
                pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, id);
                pstmt.setDate(2, sdt);
                pstmt.setInt(3, secs);
                pstmt.setInt(4, partyId);
                pstmt.setString(5, narration);
                pstmt.setInt(6, walletId);
                float amt = 0;
                if(radPaidAmt.isSelected())
                {
                  amt = -paidAmount ;
                }
                else
                {
                   amt = recvAmount;
                }
                pstmt.setFloat(7, amt);
                pstmt.setString(8,c);
                pstmt.setString(9, ref);
                pstmt.setInt(10, sListId);
                
                pstmt.executeUpdate();
                pstmt.close();
                
                //--------------------------adding refrence doc in the database----------------------
                
                if(imageSelected)
                {
                        int transDocId = MainFrame.getIdFromDual();
                        String docName = txtDocName.getText().trim();
                
                        BufferedImage tempimg;
                        tempimg = imageToBufferedImage(docImage);
                        Blob blob = null;
                        blob = con.createBlob();
                        OutputStream out = blob.setBinaryStream(0);
                        ImageIO.write(tempimg, "png", out);
                        sql = "Insert into TransDocs(Id,ExTransId,Name,Pic)values(?,?,?,?)";
                        pstmt =  con.prepareStatement(sql);
                        pstmt.setInt(1, transDocId);
                        pstmt.setInt(2, id);
                        pstmt.setString(3, docName);
                        pstmt.setBlob(4, blob);
                        pstmt.executeQuery();
                        pstmt.close();

                       TransDocs td = new TransDocs(transDocId, id, docName, docImage);
                       docModel.addElement(td);
                       arlTransDocs.add(td);
                       int index = docModel.indexOf(td);
                       lstRefDoc.setSelectedIndex(index);
                    }
                
                
               //-----------------------------------end of code----------------------------------------- 
                
                //changing affected wallet
                updateWallet(walletId, amt, c , false);
                
                //Making entry in summarytable
                makeEntryInSummaryTable(sdt, amt);
                
            
                //Making entry in the table
                ExtTransData ed = new ExtTransData(date, partyId, partyName, narration, walletId, paidAmount, recvAmount, getModeCharToString(c), ref, walletName,id);
                tableModel.arl.add(ed);
                int indx = tableModel.arl.indexOf(ed);
//                row = tableModel.getRowCount() - 1;
//                tableModel.fireTableRowsInserted(row, row);
//                tabExtTrans.setRowSelectionInterval(row, row);

//                 row = tableModel.getRowCount() - 1;
                tableModel.fireTableRowsInserted(indx, indx);
//                System.out.println("indx: "+indx);
                tabExtTrans.setRowSelectionInterval(indx, indx);
                
                con.commit();
                con.setAutoCommit(true);
                
                //enabledDisabled(true);
                disableComponents();
                radShow.setEnabled(true);
                
                isExtTransMade = true;
                
                if(isSListTrans == true || isRegServTrans == true)
                {
                    parentWindow.dispose();
                }
                
                if(c.equals("C"))
                    MainFrame.currentLiqBalance+=amt;
                else
                   MainFrame.currentDigitBalance+=amt; 
                
                getBalaceMeterPercent();
                
                imageSelected = false;
               
            } catch (SQLException | IOException ex) {
                Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void radPaidAmtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radPaidAmtActionPerformed
        // TODO add your handling code here:
        txtPaidAmt.setText("");
        txtPaidAmt.setEnabled(true);
        txtRecievedAmt.setText("0");
        txtRecievedAmt.setEnabled(false);
        getWalletInfoForPayment();
    }//GEN-LAST:event_radPaidAmtActionPerformed

    private void radRecvAmtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radRecvAmtActionPerformed
        // TODO add your handling code here:
        txtRecievedAmt.setText("");
        txtRecievedAmt.setEnabled(true);
        txtPaidAmt.setText("0");
        txtPaidAmt.setEnabled(false);
        getWalletInfo();
    }//GEN-LAST:event_radRecvAmtActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        // TODO add your handling code here:
        enabledDisabled();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseActionPerformed
        // TODO add your handling code here:
        JFileChooser ch = new JFileChooser();
        ch.setFileSelectionMode(JFileChooser.FILES_ONLY);
        ch.setCurrentDirectory(curfile);
        FileNameExtensionFilter pfilters = new FileNameExtensionFilter("Picture files", "jpg","jpeg","jpe","jfif","bmp","png","dib");
        ch.addChoosableFileFilter(pfilters);
        ch.setAcceptAllFileFilterUsed(false);
        int result = ch.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION)
        {
            
            try {
                    File imagefile = ch.getSelectedFile();
                    String filename = imagefile.getName();
                    int index = filename.indexOf(".");
                    curfile = imagefile.getParentFile();
                    docImage = ImageIO.read(imagefile);
                    setImageToLabel(docImage, lblPic);
                    imageSelected = true;
                
            } catch (IOException ex) {
                Logger.getLogger(PictureManager.class.getName()).log(Level.SEVERE, null, ex);
            }
//            txtDocName.setText("");
        }
        else
            imageSelected = false;
    }//GEN-LAST:event_btnBrowseActionPerformed

    private void btnDelDocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelDocActionPerformed
        // TODO add your handling code here:
        int index = lstRefDoc.getSelectedIndex();
        if(index<0)
        {
            JOptionPane.showMessageDialog(this, "Please select document to delete", "Message", JOptionPane.ERROR_MESSAGE);
        }
        else
        {
                docModel.remove(index);
                txtDocName.setText("");
                lblPic.setIcon(null);
                imageSelected = false;
        }
    }//GEN-LAST:event_btnDelDocActionPerformed

    private void lstRefDocValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstRefDocValueChanged
        // TODO add your handling code here:
            int index = lstRefDoc.getSelectedIndex();
            if(index>=0)
            {
                TransDocs td = lstRefDoc.getSelectedValue();
                setImageToLabel(td.getImg(), lblPic);
                txtDocName.setText(td.getName());
            }
           
    }//GEN-LAST:event_lstRefDocValueChanged

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
        showExtTransactions();

    }//GEN-LAST:event_btnToDateActionPerformed

    private void btnDtFromDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDtFromDateActionPerformed
        // TODO add your handling code here:
        
            String str = lblDtFromDt.getText().trim();
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
                lblDtFromDt.setText(formatter.format(dt));
                fromDate = dt;
            } catch (ParseException ex) {
                Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        dlg.dispose();
        showExtTransactions();
    }//GEN-LAST:event_btnDtFromDateActionPerformed

    private void btnDtToDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDtToDateActionPerformed
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
                lblDtToDate.setText(formatter.format(dt));
                toDate = dt;
            } catch (ParseException ex) {
                Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        dlg.dispose();
        showExtTransactions();
    }//GEN-LAST:event_btnDtToDateActionPerformed

    private void showExtTransactions()
    {
        tableModel.setRowCount();
        lblPic.setIcon(null);
        txtDocName.setText("");
        docModel.clear();
        arlTransDocs.clear();
        
        showTransaction(null); 
    }
    private void btnAddRowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRowActionPerformed
        // TODO add your handling code here:
        enabledDisabled();
        docModel.clear();
        cmbMode.removeAllItems();
        cmbWallet.setSelectedIndex(0);
//        addMode();
        tabExtTrans.clearSelection();
        sListId = -1;
    }//GEN-LAST:event_btnAddRowActionPerformed

    private void radDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radDateActionPerformed
        try {
            // TODO add your handling code here:
            clearDateLabels();
            clearComponents(false);
            spnPdNum.setValue(maxPdNum);
            spnPdNum.setEnabled(false);
            btnFromDate.setEnabled(false);
            btnToDate.setEnabled(false);
            btnDtFromDate.setEnabled(true);
            btnDtToDate.setEnabled(true);
            
            
            lblDtFromDt.setText(formatter.format(Calendar.getInstance().getTime()));
            fromDate = formatter.parse(lblDtFromDt.getText());
            lblDtToDate.setText(formatter.format(Calendar.getInstance().getTime()));
            toDate = formatter.parse(lblDtToDate.getText());
            
            showExtTransactions();


        } catch (ParseException ex) {
            Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_radDateActionPerformed

    private void radShowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radShowActionPerformed
        // TODO add your handling code here:
        clearDateLabels();
        enabledDisabled();
        spnPdNum.setEnabled(false);
        btnFromDate.setEnabled(false);
        btnToDate.setEnabled(false);
        btnDtFromDate.setEnabled(false);
        btnDtToDate.setEnabled(false);
        radToday.setSelected(true);
        showTransaction(null);
    }//GEN-LAST:event_radShowActionPerformed

    private void radPeriodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radPeriodActionPerformed
        try {
            // TODO add your handling code here:
            clearDateLabels();
            clearComponents(false);
            btnFromDate.setEnabled(true);
            btnToDate.setEnabled(true);
            btnDtFromDate.setEnabled(false);
            btnDtToDate.setEnabled(false);
            
            
            btnDtFromDate.setEnabled(false);
            btnDtToDate.setEnabled(false);
            String sql = "Select Max(Num) from Pd";
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            if(rst.next())
            {
                maxPdNum = rst.getInt(1);
            }
            rst.close();
            stmt.close();
            
            if(maxPdNum == 0)
                pdFound = false;
            else
                pdFound = true;
            
            if(pdFound == true)
            {
                spnPdNum.setEnabled(true);
                snm = new SpinnerNumberModel(maxPdNum, 1, maxPdNum, 1);
                spnPdNum.setModel(snm);
                spnPdNum.setValue(maxPdNum);
                setPdDates();
            }
            else
            {
                lblFromDate.setText(""+formatter.format(Calendar.getInstance().getTime()));
                lblToDate.setText(""+formatter.format(Calendar.getInstance().getTime()));
                snm = new SpinnerNumberModel(0, 0, 0, 0);
                spnPdNum.setModel(snm);
                spnPdNum.setEnabled(false);
                fromDate = formatter.parse(lblFromDate.getText());
                toDate = formatter.parse(lblToDate.getText());
            }
            showExtTransactions();

        } catch (SQLException| ParseException ex) {
            Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }//GEN-LAST:event_radPeriodActionPerformed

    private void spnPdNumStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnPdNumStateChanged
       setPdDates();
       showExtTransactions();
    }//GEN-LAST:event_spnPdNumStateChanged

    private void radTodayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radTodayActionPerformed
        // TODO add your handling code here:
        clearDateLabels();
        clearComponents(false);
        spnPdNum.setValue(maxPdNum);
        spnPdNum.setEnabled(false);
        btnFromDate.setEnabled(false);
        btnToDate.setEnabled(false);
        btnDtFromDate.setEnabled(false);
        btnDtToDate.setEnabled(false);
        
        showExtTransactions();
    }//GEN-LAST:event_radTodayActionPerformed

    private void btnShowImgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowImgActionPerformed
        // TODO add your handling code here:
        int index = lstRefDoc.getSelectedIndex();
        if(index>=0)
        {
            TransDocs td = docModel.getElementAt(index);
            Image img = td.getImg();
            String name = td.getName();
            if(img!=null)
            {
                OriginalPictureDialog opd = new OriginalPictureDialog(null, true,img,name);
                opd.setVisible(true);
                
            }
        }
    }//GEN-LAST:event_btnShowImgActionPerformed

    private void cmbPartyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPartyActionPerformed
        // TODO add your handling code here:
//                // TODO add your handling code here:
//        String name = (String)cmbParty.getSelectedItem();
//       
//        if(name.equals("Bank"))
//        {
//          cmbMode.removeItem("Cash");
//        }
//        else
//        {
//            cmbMode.removeAllItems();
//            addMode();
//        }
       
    }//GEN-LAST:event_cmbPartyActionPerformed

    private void cmbWalletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbWalletActionPerformed
        // TODO add your handling code here:
        if(cmbWallet.getSelectedIndex()>=0)
        {
            String walletName = (String)cmbWallet.getSelectedItem();
            if(walletName.equals("Bank"))
            {
              cmbMode.removeAllItems();
              addMode();
              cmbMode.removeItem("Cash");
            }
            else if(walletName.equals("Cash"))
            {
              cmbMode.removeAllItems();
              cmbMode.addItem("Cash");
            }
            else
            {
                cmbMode.removeAllItems();
                addMode();
            }
        }
    }//GEN-LAST:event_cmbWalletActionPerformed
    private void getBalaceMeterPercent()
    {
        String sql = null;
        PreparedStatement pstmt = null;
        try {
        if(MainFrame.currentLiqBalance > MainFrame.sysSettings.getRefLiqBalance())
        {
                MainFrame.sysSettings.setRefLiqBalance(MainFrame.currentLiqBalance);
                sql = "Update SystemSettings set refliqbalance = ?";
                pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, MainFrame.currentLiqBalance);
                pstmt.executeUpdate();
                pstmt.close();
        }
        if(MainFrame.currentDigitBalance > MainFrame.sysSettings.getRefDigitBalance())
        {
            MainFrame.sysSettings.setRefDigitBalance(MainFrame.currentDigitBalance);
            sql = "Update SystemSettings set refdigitbalance = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, MainFrame.currentDigitBalance);
            pstmt.executeUpdate();
           pstmt.close();
        }
        
        double liqbalpercent = (double)((MainFrame.currentLiqBalance * 100) / MainFrame.sysSettings.getRefLiqBalance());
        double digbalpercent = (double)((MainFrame.currentDigitBalance * 100) / MainFrame.sysSettings.getRefDigitBalance());
        MainFrame.frame.updateBalanceMeters(digbalpercent, liqbalpercent);
        
        
        } catch (SQLException ex) {
                Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
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
            java.util.logging.Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(ExternalTransactionPanelNew.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
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
                
                f.setTitle("External Transaction");
                f.add(new ExternalTransactionPanelNew());
                f.pack();
                f.setLocationRelativeTo(null);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
                
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Narration;
    private javax.swing.JButton btnAddRow;
    private javax.swing.ButtonGroup btnAmtType;
    private javax.swing.JButton btnBrowse;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnDel;
    private javax.swing.JButton btnDelDoc;
    private javax.swing.JButton btnDtFromDate;
    private javax.swing.JButton btnDtToDate;
    private javax.swing.JButton btnFromDate;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnShowImg;
    private javax.swing.JButton btnToDate;
    private javax.swing.ButtonGroup btngrpDt;
    private javax.swing.JComboBox<String> cmbMode;
    private javax.swing.JComboBox<String> cmbParty;
    private javax.swing.JComboBox<String> cmbWallet;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblDtFromDt;
    private javax.swing.JLabel lblDtToDate;
    private javax.swing.JLabel lblFromDate;
    private javax.swing.JLabel lblPic;
    private javax.swing.JLabel lblToDate;
    private javax.swing.JList<TransDocs> lstRefDoc;
    private javax.swing.JPanel middlePanel;
    private javax.swing.JPanel panelTransDetails;
    private javax.swing.JRadioButton radDate;
    private javax.swing.JRadioButton radEntry;
    private javax.swing.JRadioButton radPaidAmt;
    private javax.swing.JRadioButton radPeriod;
    private javax.swing.JRadioButton radRecvAmt;
    private javax.swing.JRadioButton radShow;
    private javax.swing.JRadioButton radToday;
    private javax.swing.ButtonGroup radgrpShowEntry;
    private javax.swing.JSpinner spnPdNum;
    private javax.swing.JTable tabExtTrans;
    private javax.swing.JTextField txtDocName;
    private javax.swing.JTextField txtNarration;
    private javax.swing.JTextField txtPaidAmt;
    private javax.swing.JTextField txtRecievedAmt;
    private javax.swing.JTextField txtRefNum;
    private javax.swing.JPanel upperPanel;
    // End of variables declaration//GEN-END:variables

}
