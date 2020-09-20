/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.picmanager;

import domesticfinancesystem.IntegerDocumentFilter;
import domesticfinancesystem.calendar.Database;
import domesticfinancesystem.calendar.DatePickerNewDialog;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.FileSystem;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import jdk.nashorn.internal.runtime.Undefined;

/**
 *
 * @author sneha
 */
public class PictureManager extends javax.swing.JFrame {

    /**
     * Creates new form PictureManager
     */
    
    private DefaultListModel<HolidayPics> holidayModel = new DefaultListModel<HolidayPics>();
    private Image img;
    private Database dc;
    private Connection con;
    private Statement stmt;
    private File bulkFile;
    private  File curfile ;
    private MyTableModel tableModel;
    private File imagefile;
    private boolean imageBrowsed;
    private SimpleDateFormat regDateFormatter = new SimpleDateFormat("dd/MM");
    private SimpleDateFormat irregDateFormatter = new SimpleDateFormat("dd/MM/yyyy");
    private File tempFile = new File("data","HolidayPics");
    
    public class NameLengthFilter extends DocumentFilter
    {
        private String numregex = "^[a-zA-Z0-9]+$";
        private Pattern patNum = Pattern.compile(numregex);

        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException
        {
            Document doc = fb.getDocument();
            String lpart = doc.getText(0, offset);
            int p = offset + length;
            String rpart = doc.getText(p, doc.getLength()-p);
            String str = lpart+text+rpart;
            str = str.trim();
            boolean valid  = true ;
            if(str != null && str.length() > 0)
            {
                   if(str.length() > 30)
                       valid = false;
                   else
                       valid = true;
            }
            if(valid)
                super.replace(fb, offset, length, text, attrs); //To change body of generated methods, choose Tools | Templates.
    }
    
}
    private class MyTableModel extends AbstractTableModel
    {
        final int COLS = 7;
        String[] colNames = {"HolType","Comm","HolName","Date","HPicPath","Picture","New HolName"};
        Class[] colTypes = {Character.class,Character.class,String.class,Date.class,String.class,Image.class,String.class} ;
        ArrayList<HolidayCommandItem> arl = new ArrayList<HolidayCommandItem>();

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
            HolidayCommandItem hci = (HolidayCommandItem)arl.get(rowIndex);
            if(columnIndex == 0)
                 return hci.gethType();
            else if(columnIndex == 1)
                return hci.getCommand();
            else if(columnIndex == 2)
                return hci.getName();
            else if(columnIndex == 3)
                return hci.getDt();
            else if(columnIndex == 4)
            {
               File file = hci.gethPicFile();
               if(file!=null)
                return hci.gethPicFile().getName();
               else
                   return null;
            }
            else if(columnIndex == 5)
            {
               Image img = hci.getImage();
                   return img;
            }
            else
                return hci.getNewName();
           
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
           return false;
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            HolidayCommandItem hci = arl.get(rowIndex);
            if(columnIndex == 0)
                hci.sethType((Character)aValue);
            else if(columnIndex == 1)
                hci.setCommand((Character)aValue);
            else if(columnIndex == 2)
                hci.sethName((String)aValue);
            else if(columnIndex == 3)
                hci.setDt((Date)aValue);
            else if(columnIndex == 4)
              hci.sethPicFile((File)aValue);
            else if(columnIndex == 5)
            {
                hci.setImage((Image)aValue);
            }
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
        
        public void addRow(HolidayCommandItem hci)
        {
            arl.add(hci);
        }
        
        public void setRowCount()
        {
            arl.clear();
            fireTableStructureChanged();
        }
    }
 
  
   class ImageCellRenderer extends DefaultTableCellRenderer
    {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
           super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column); //To change body of generated methods, choose Tools | Templates.

           if (tableModel.arl.get(row).getImage() == null)
           {
             setIcon(new ImageIcon());
           }
           else
           {
                setHorizontalAlignment(JLabel.CENTER);
                setIcon(new ImageIcon((BufferedImage)value));
                
           }
           return this;
        }
        
    }
    
    class DateCellRenderer extends DefaultTableCellRenderer
    {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
           super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column); //To change body of generated methods, choose Tools | Templates.

           if (value == null)
           {
             setText("");
           }
           else
           {
                HolidayCommandItem hci = tableModel.arl.get(row);
                char c = hci.gethType();
                String date = "";
                if(c == 'R')
                    date = regDateFormatter.format(hci.getDt());
                else
                    date = irregDateFormatter.format(hci.getDt());
                setHorizontalAlignment(JLabel.CENTER);
                setText(date);
                
           }
           return this;
        }
        
    }
    
    public PictureManager() {
        initComponents();
       ((AbstractDocument)txtName.getDocument()).setDocumentFilter(new NameLengthFilter());

        tableModel = new MyTableModel();
        tabHolCommand.setModel(tableModel);
        
        tabHolCommand.setRowHeight(50);
        tabHolCommand.setShowGrid(true);
        
        imageBrowsed = false;
        
        //setting the preferred column widths
        TableColumnModel columnModel = tabHolCommand.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(10);
        columnModel.getColumn(1).setPreferredWidth(10);
        columnModel.getColumn(2).setPreferredWidth(50);
        columnModel.getColumn(3).setPreferredWidth(50);    
        columnModel.getColumn(4).setPreferredWidth(350);    
        columnModel.getColumn(5).setPreferredWidth(100);    
        columnModel.getColumn(4).setPreferredWidth(100);
        
        tabHolCommand.setDefaultRenderer(Image.class, new ImageCellRenderer());
        tabHolCommand.setDefaultRenderer(Date.class, new DateCellRenderer());
        
        
        
        FileSystemView fsv = FileSystemView.getFileSystemView() ;
        curfile = fsv.getHomeDirectory() ;       // For Windows it is the Desktop
        
        setLocationRelativeTo(null);
        txtFolderName.setEditable(false);
        txtDate.setEditable(false);
        txtSelectPicture.setEditable(false);
        radRegular.setSelected(true);
        txtDate.setEnabled(false);        
        BtnPickDate.setEnabled(false);
        lstHolidays.setModel(holidayModel);
        lstHolidays.setCellRenderer(new PictPanelListCellRenderer1());
        dc = new Database("jdbc:oracle:thin:@localhost:1521:XE","dfs","dfsboss","oracle.jdbc.OracleDriver");
        con = dc.createConnection();
        setImagesToList();
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
    
     private void setImageToLabel(Image img,JLabel lbl)
     {
         if(img == null)
         {
             lbl.setIcon(null);
             return;
         }
                 img = scaleImage(img,140,137);
                 ImageIcon icon = new ImageIcon(img);
                 lbl.setIcon(icon);
         
     }
     private void setImagesToList()
     {
        try {
            stmt = con.createStatement();
            String sql = "Select * from HolidayPic";
            ResultSet rst = stmt.executeQuery(sql);
            while(rst.next())
            {
                int id = rst.getInt("Id");
                String name = rst.getString("Name");
                Blob blob = rst.getBlob("Pic");
                Image img = null;
                if(blob !=null)
                {
                   InputStream in = blob.getBinaryStream();
                   img = ImageIO.read(in);
                }
                File imageFile = null;
                char ch = rst.getString("Regular").charAt(0);
                if(ch == 'Y')
                {
                    PreparedStatement pst = con.prepareStatement("Select * from RegularHoliday where HPicId = ?");
                    pst.setInt(1,id);
                    ResultSet rs = pst.executeQuery();
                    int day = 0,month = 0;
                    if(rs.next())
                    {
                        day = rs.getInt("Day");
                        month = rs.getInt("Month");
                    }
                    
                    holidayModel.addElement(new HolidayPics(id,name, img, ch, day, month, null));
                    rs.close();
                    pst.close();
                }
                else
                {
                    PreparedStatement pst = con.prepareStatement("Select * from IrregularHoliday where HPicId = ?");
                    pst.setInt(1,id);
                    ResultSet rs = pst.executeQuery();
                    int day = 0,month = 0;
                    Date dt = null;
                    if(rs.next())
                    {
                        dt = rs.getDate("Dt");
                    }
                    
                    holidayModel.addElement(new HolidayPics(id,name, img, ch, day, month, dt));
                    rs.close();
                    pst.close();
                    
                }
            }
            rst.close();
            stmt.close();
            
        } catch (SQLException | IOException ex) {
            Logger.getLogger(PictureManager.class.getName()).log(Level.SEVERE, null, ex);
        }
                 
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
    
    public String getImageName(String name)
    {
                int ind = name.indexOf("jpg");
                String picname = name.substring(0, ind - 1);
                String[] namearr = picname.split("\\_");
                System.out.println("length: "+namearr.length);
                String str = "";
                if(namearr.length == 1)
                {
                    str = picname.substring(0,1).toUpperCase()+picname.substring(1);
                    return str;
                }
                else
                {
                    String nm = "";
                    for(int i = 0;i<namearr.length - 1;i++)
                    {
                        str = namearr[i].substring(0,1).toUpperCase()+namearr[i].substring(1);
                        nm +=str;
                        nm+=" ";
                    }
                    str = namearr[namearr.length - 1].substring(0,1).toUpperCase()+namearr[namearr.length - 1].substring(1);
                    nm+=str;
                    return nm;
               }

    }
    
    private byte createControlByte(HolidayCommandItem hci)
    {
        byte b = 0;
        byte mask;
        if(hci.gethType() == 'R')
            mask = (byte)0b00_0_00000;
        else
            mask = (byte)0b00_1_00000;
        
        b |= mask;
        
        if(hci.getCommand() == 'A')
            mask = (byte)0b000_01_000;
        else if(hci.getCommand() == 'U')
            mask = (byte)0b000_10_000;
        else
            mask = (byte)0b000_11_000;

        b|=mask;
        
        mask = 0b00000_1_00;
        if(hci.getImage()!=null)
            b|=mask;
        
        mask>>>=1;
        if(hci.getDt()!=null)
            b|=mask;
        
        mask>>>=1;
        if(hci.getNewName()!=null)
            b|=mask; 
       
        return b;
    }
    
    private short packedDate(Date dt)
    {
        short b = 0;
        short mask ;
        
        System.out.println("day: "+dt.getDate());
        System.out.println("month: "+(dt.getMonth()+1));
        System.out.println("year: "+dt.getYear());
        
        mask = (short)dt.getDate();
        mask <<= (16 - 5);
        System.out.println("day mask "+Integer.toBinaryString(mask));
        b |= mask;
        System.out.println("b = "+Integer.toBinaryString(b));
        
        mask = (short)(dt.getMonth() + 1);
        mask <<= (16 - 9);
        System.out.println("month mask "+Integer.toBinaryString(mask));
        b |= mask;
        System.out.println("b = "+Integer.toBinaryString(b));
        
        if(dt.getYear() < 0 )
            mask = 0;
        else
           mask = (short)(dt.getYear() % 100);
        
        System.out.println("year mask "+Integer.toBinaryString(mask));
        b |= mask;
        System.out.println("b = "+Integer.toBinaryString(b));
        
        
        return b ;
    }
   private HolidayCommandItem readHci(DataInputStream dis) throws IOException
   {
      
       byte cb = dis.readByte();
       System.out.println("control byte - "+Integer.toBinaryString(cb));
       HolidayCommandItem hci = new HolidayCommandItem();
       
       if ((cb & 0b00_1_00000) == 0)
           hci.sethType('R');
       else
          hci.sethType('I');
       
       if((cb & 0b000_11_000) == 0b000_11_000)
           hci.setCommand('D');
       else if((cb & 0b000_01_000) == 0b000_01_000)
           hci.setCommand('A');
       else if((cb & 0b000_10_000) == 0b000_10_000)
           hci.setCommand('U');
       
       boolean isPicture,isDate,isNewName;
       isPicture=isDate=isNewName = false;
       
       byte mask = 0b00000_100;
       if((cb & mask) != 0)
           isPicture = true;
       
       mask>>>=1;
       if((cb & mask) != 0)
           isDate = true;
       
       mask>>>=1;
       if((cb & mask) != 0)
           isNewName = true;
       
       int len = dis.readByte();
       byte[] arBytes = new byte[len];
       dis.read(arBytes);
       
       hci.sethName(new String(arBytes));
       
       
       if(isDate)
       {
           short packedDate = dis.readShort();
           System.out.println("date: "+Integer.toBinaryString(packedDate));
           hci.setDt(unpackDate(packedDate));
           
       }
       
       if(isPicture)
       {
           len = dis.readByte();
           arBytes = new byte[len];
           dis.read(arBytes);
           
           File file = new File(new String(arBytes));
          
           hci.sethPicFile(file);
           File imageFile = new File(tempFile,file.getName());
           Image image = ImageIO.read(imageFile);
           hci.setImage(image);

       }

       if(isNewName)
       {
           len = dis.readByte();
           arBytes = new byte[len];
          dis.read(arBytes);
          
           hci.setNewName(new String(arBytes));

       }
      return hci;
   }
   
   private void readHcis(DataInputStream dis) throws IOException
   {
       short size = dis.readShort();
       System.out.println("short size: "+size);
       short i = 0;
       while(i<size)
       {
           HolidayCommandItem hci = readHci(dis);
           tableModel.addRow(hci);
           int row = tableModel.getRowCount() - 1;
           tableModel.fireTableRowsInserted(row, row);
           tabHolCommand.setRowSelectionInterval(row, row);
           i++;
       }
   }
   
   private Date unpackDate(int packedDate)
   {
       System.out.println("packed date: "+Integer.toBinaryString(packedDate));
       int day = ((packedDate << 16) >>> (11 + 16)) ;
       System.out.println("Day: "+day);
       
       int month = ((packedDate << (5 + 16)) >>> (7 + 5 + 16)) ;
       System.out.println("month: "+ month);
       
       int year = (short)((packedDate << (9 + 16)) >>> (9 + 16)) ;
       if(year!=0)
          year = 2000+year;
       
       System.out.println("actual year: "+year);
       
       return new GregorianCalendar(year, month - 1, day).getTime();
       
   }
    
   private void writeallHCIs(File zipFile, ArrayList<HolidayCommandItem> arlHCi) throws IOException
   {
        OutputStream out = new FileOutputStream(zipFile) ;
        ZipOutputStream zout = new ZipOutputStream(out) ;

        ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
        DataOutputStream dos = new DataOutputStream(bos);
        
        zout.setMethod(ZipOutputStream.DEFLATED);
        zout.setLevel(Deflater.BEST_COMPRESSION);
       

       dos.writeShort(arlHCi.size());
       for (HolidayCommandItem hci : arlHCi) {
          File inputPicFile = hci.gethPicFile();
          if(inputPicFile!=null)
          {
            ZipEntry ze = new ZipEntry(inputPicFile.getName()) ;
            zout.putNextEntry(ze);
            FileInputStream fin = new FileInputStream(inputPicFile) ;
               
              streamCopy(fin, zout);
              fin.close();
          }
          
           writeHCI(dos, hci);
           
       }       
       
       dos.close();
       
       zout.putNextEntry(new ZipEntry("PicInfo.dat"));
       for(byte b : bos.toByteArray())
            zout.write(b); 
       zout.close();
       
   }
   private void writeHCI(DataOutputStream dos,HolidayCommandItem hci) throws IOException
   {
       dos.writeByte(createControlByte(hci));
//       String str = hci.getNewName();
//       if(hci.getCommand() == 'U')
          String str = hci.getName();
       dos.writeByte((int)str.length());
       dos.writeBytes(str);
       if(hci.getDt()!=null)
       {
           short date = packedDate(hci.getDt());
           dos.writeShort(date);
           System.out.println("from calling point - "+Integer.toBinaryString(date));
       }
       if(hci.gethPicFile()!=null)
       {
          dos.writeByte((int)hci.gethPicFile().getName().length());
          dos.writeBytes(hci.gethPicFile().getName()); 
       }
       
       if(hci.getCommand() == 'U' && hci.getNewName()!=null)
       {
           dos.writeByte((int)hci.getNewName().length());
           dos.writeBytes(hci.getNewName());
       }
   }
   
   private void fileCopy(File inputFile,File outputFile) throws IOException
   {
      byte[] arr = new byte[4024];
      FileInputStream in = new FileInputStream(inputFile);
      FileOutputStream out = new FileOutputStream(outputFile);
      
      while(in.read(arr)!=-1)
      {
          out.write(arr);
      }
      in.close();
      out.close();
   }
   
   private void streamCopy(InputStream in,OutputStream out) throws IOException
   {
      byte[] arr = new byte[4024];
      
      while(in.read(arr)!=-1)
      {
          out.write(arr);
      }
   }
   
   private void addImagesToFolder() throws IOException
   {
       File parentFolder = new File("data","HolidayPics");
       if(!parentFolder.exists())
           parentFolder.mkdirs();
       for (HolidayCommandItem hci : tableModel.arl) {
           
          File inputFile = hci.gethPicFile();
          if(inputFile!=null)
          {
              File outputFile = new File(parentFolder, inputFile.getName());
              if(!outputFile.exists())
              fileCopy(inputFile, outputFile);
          }
          
           
       }
   }
    
    public void addBulkImagesToDatabase(File bulkFile)
    {
        
        File[] files = bulkFile.listFiles();
        for(File f:files)
        {
            try 
            {
                Image image = ImageIO.read(f);
                if(image!=null)
                {
                    String nm = getImageName(f.getName().trim());
                    addImageToDatabase(image, nm,f);
                }
            } 
            catch (IOException ex) {
                Logger.getLogger(PictureManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void addImageToDatabase(Image img,String name,File imagefile)
    {
        try {
                con.setAutoCommit(false);
                PreparedStatement pstmt;
                boolean imagealreadyexists = false;
                String sql = "Select Id from HolidayPic where Name = ?";
                pstmt = con.prepareStatement(sql);
                pstmt.setString(1,name);
                ResultSet rst = pstmt.executeQuery();
                if(rst.next())
                {
                    imagealreadyexists = true;
                }
                if(imagealreadyexists)
                {
                    JOptionPane.showMessageDialog(this, "Image by the same name already exists!", "Message", JOptionPane.ERROR_MESSAGE);

                }
                else
                {
                    BufferedImage tempimg;
                    tempimg = imageToBufferedImage(img);
                    Blob blob = null;
                    blob = con.createBlob();
                    OutputStream out = blob.setBinaryStream(0);
                    ImageIO.write(tempimg, "jpg", out);

                    sql = "Insert into HolidayPic(Id,Name,Pic,Regular) ";
                    sql+="values(seq.nextval,?,?,?)";
                    pstmt = con.prepareStatement(sql);
                    String picname = name;
                    pstmt.setString(1,picname);
                    pstmt.setBlob(2, blob);
                    String ch ;
                    int mnth = 0;
                    int dy = 0;
                    if(radRegular.isSelected())
                    {
                        ch = "Y";
                    }
                    else
                    {
                        ch = "N";
                    }
                    pstmt.setString(3,ch);
                    pstmt.executeQuery();

                    sql = "Select Id from HolidayPic where Name = ?";
                    pstmt = con.prepareStatement(sql);
                    pstmt.setString(1,picname);
                    rst = pstmt.executeQuery();
                    int hpicid = 0;
                    if(rst.next())
                    {
                        hpicid = rst.getInt(1);
                    }

                    char htype;
                    Date udt = null;
                    HolidayCommandItem hci;
                    String newName = null;
                    if(radRegular.isSelected())
                    {
                        mnth = (int)spnMonth.getValue();
                        dy = (int)spnDay.getValue();
                        htype = 'Y';
                        sql = "Insert into RegularHoliday(Id,Month,Day,HPicId) ";
                        sql+="values(seq.nextval,?,?,?)";

                        pstmt = con.prepareStatement(sql);
                        pstmt.setInt(1, mnth);
                        pstmt.setInt(2, dy);
                        pstmt.setInt(3, hpicid);
                        
                        Date date = new GregorianCalendar(0, mnth - 1, dy).getTime();
                       
                        hci = new HolidayCommandItem('R', 'A', name, date, imagefile, img, newName);
                    }
                    else
                    {
                        htype = 'N';
                        sql = "Insert into IrregularHoliday(Id,Dt,HPicId) ";
                        sql+="values(seq.nextval,?,?)";
                        udt = new SimpleDateFormat("dd/MM/yyyy").parse(txtDate.getText());
                        java.sql.Date sdt = new java.sql.Date(udt.getTime());
                        pstmt = con.prepareStatement(sql);
                        pstmt.setDate(1, sdt);
                        pstmt.setInt(2, hpicid);
                        hci = new HolidayCommandItem('I', 'A', name, udt,imagefile, img, newName);

                    }
                    pstmt.executeQuery();
                    HolidayPics hp = null;
                    if(radRegular.isSelected())
                    {
                         hp = new HolidayPics(hpicid,picname, img, htype,dy,mnth,null);
                         holidayModel.addElement(hp);
                    }
                    else
                    {
                        hp = new HolidayPics(hpicid,picname, img, htype,0,0,udt);
                        holidayModel.addElement(hp);
                    }
                    int index = holidayModel.indexOf(hp);
                    lstHolidays.setSelectedIndex(index);

                    pstmt.close();
                    
                    tableModel.addRow(hci);
                    int row = tableModel.getRowCount() - 1;
                    tableModel.fireTableRowsInserted(row, row);
                    tabHolCommand.setRowSelectionInterval(row, row);
                    
           }
            con.commit();
            con.setAutoCommit(true);
                
        }
        catch (IOException | SQLException | ParseException ex) {
                    Logger.getLogger(PictureManager.class.getName()).log(Level.SEVERE, null, ex);
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

        btngrpHoliday = new javax.swing.ButtonGroup();
        UpperPanel = new javax.swing.JPanel();
        radRegular = new javax.swing.JRadioButton();
        radIrregular = new javax.swing.JRadioButton();
        CetralPanel = new javax.swing.JPanel();
        txtFolderName = new javax.swing.JTextField();
        btnBrowse = new javax.swing.JButton();
        btnAddAll = new javax.swing.JButton();
        btnRemoveAll = new javax.swing.JButton();
        btnWriteScript = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstHolidays = new javax.swing.JList<>();
        jLabel1 = new javax.swing.JLabel();
        lblPic = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtSelectPicture = new javax.swing.JTextField();
        btnBrow = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        btnUpdate = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        spnMonth = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        spnDay = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        txtDate = new javax.swing.JTextField();
        BtnPickDate = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabHolCommand = new javax.swing.JTable();
        btnWrScript = new javax.swing.JButton();
        btnReadScript = new javax.swing.JButton();
        btnDelRow = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        UpperPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        btngrpHoliday.add(radRegular);
        radRegular.setText("Regular");
        radRegular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radRegularActionPerformed(evt);
            }
        });

        btngrpHoliday.add(radIrregular);
        radIrregular.setText("Irregular");
        radIrregular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radIrregularActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout UpperPanelLayout = new javax.swing.GroupLayout(UpperPanel);
        UpperPanel.setLayout(UpperPanelLayout);
        UpperPanelLayout.setHorizontalGroup(
            UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(UpperPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(radRegular)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 222, Short.MAX_VALUE)
                .addComponent(radIrregular)
                .addGap(25, 25, 25))
        );
        UpperPanelLayout.setVerticalGroup(
            UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(UpperPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(UpperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radRegular, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radIrregular, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        CetralPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Bulk Manage", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 153))); // NOI18N

        txtFolderName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFolderNameActionPerformed(evt);
            }
        });

        btnBrowse.setText("Browse");
        btnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseActionPerformed(evt);
            }
        });

        btnAddAll.setText("Add All");
        btnAddAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddAllActionPerformed(evt);
            }
        });

        btnRemoveAll.setText("Remove All");
        btnRemoveAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout CetralPanelLayout = new javax.swing.GroupLayout(CetralPanel);
        CetralPanel.setLayout(CetralPanelLayout);
        CetralPanelLayout.setHorizontalGroup(
            CetralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CetralPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtFolderName, javax.swing.GroupLayout.PREFERRED_SIZE, 263, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(btnBrowse)
                .addGap(86, 86, 86)
                .addComponent(btnAddAll, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(btnRemoveAll)
                .addContainerGap(117, Short.MAX_VALUE))
        );
        CetralPanelLayout.setVerticalGroup(
            CetralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CetralPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(CetralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFolderName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBrowse)
                    .addComponent(btnAddAll)
                    .addComponent(btnRemoveAll))
                .addGap(34, 34, 34))
        );

        btnWriteScript.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lstHolidays.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstHolidaysValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstHolidays);

        jLabel1.setText("Preview");

        lblPic.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel3.setText("Select a picture");

        txtSelectPicture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSelectPictureActionPerformed(evt);
            }
        });

        btnBrow.setText("Browse");
        btnBrow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowActionPerformed(evt);
            }
        });

        jLabel4.setText("Name");

        txtName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNameActionPerformed(evt);
            }
        });

        btnUpdate.setText("Update");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnAdd.setText("Add");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnDelete.setText("Delete");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        jLabel2.setText("Month");

        spnMonth.setModel(new javax.swing.SpinnerNumberModel(1, 1, 12, 1));

        jLabel5.setText("Day");

        spnDay.setModel(new javax.swing.SpinnerNumberModel(1, 1, 31, 1));

        jLabel6.setText("Date ");

        BtnPickDate.setText("Pick Date");
        BtnPickDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPickDateActionPerformed(evt);
            }
        });

        tabHolCommand.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(tabHolCommand);

        btnWrScript.setText("Write Script");
        btnWrScript.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWrScriptActionPerformed(evt);
            }
        });

        btnReadScript.setText("Read Script");
        btnReadScript.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReadScriptActionPerformed(evt);
            }
        });

        btnDelRow.setText("Delete Row");
        btnDelRow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelRowActionPerformed(evt);
            }
        });

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout btnWriteScriptLayout = new javax.swing.GroupLayout(btnWriteScript);
        btnWriteScript.setLayout(btnWriteScriptLayout);
        btnWriteScriptLayout.setHorizontalGroup(
            btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(btnWriteScriptLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(btnWriteScriptLayout.createSequentialGroup()
                        .addComponent(jScrollPane2)
                        .addContainerGap())
                    .addGroup(btnWriteScriptLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(btnWriteScriptLayout.createSequentialGroup()
                                .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(btnWriteScriptLayout.createSequentialGroup()
                                        .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtSelectPicture, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(btnWriteScriptLayout.createSequentialGroup()
                                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(spnMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(29, 29, 29)
                                                .addComponent(jLabel5))
                                            .addGroup(btnWriteScriptLayout.createSequentialGroup()
                                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(txtDate, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(btnWriteScriptLayout.createSequentialGroup()
                                                .addGap(29, 29, 29)
                                                .addComponent(btnBrow))
                                            .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addGroup(btnWriteScriptLayout.createSequentialGroup()
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(btnDelete))
                                                .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(btnWriteScriptLayout.createSequentialGroup()
                                                        .addGap(4, 4, 4)
                                                        .addComponent(spnDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addGroup(btnWriteScriptLayout.createSequentialGroup()
                                                        .addGap(18, 18, 18)
                                                        .addComponent(BtnPickDate))))))
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(btnWriteScriptLayout.createSequentialGroup()
                                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(31, 31, 31)
                                        .addComponent(btnUpdate)))
                                .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(btnWriteScriptLayout.createSequentialGroup()
                                        .addGap(29, 29, 29)
                                        .addComponent(lblPic, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(btnWriteScriptLayout.createSequentialGroup()
                                        .addGap(59, 59, 59)
                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, btnWriteScriptLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnRefresh)
                                        .addGap(54, 54, 54)))))
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(btnWriteScriptLayout.createSequentialGroup()
                .addGap(199, 199, 199)
                .addComponent(btnDelRow, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(btnWrScript)
                .addGap(48, 48, 48)
                .addComponent(btnReadScript)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        btnWriteScriptLayout.setVerticalGroup(
            btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(btnWriteScriptLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(btnWriteScriptLayout.createSequentialGroup()
                            .addComponent(jLabel3)
                            .addGap(10, 10, 10)
                            .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(btnBrow)
                                .addComponent(txtSelectPicture, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(11, 11, 11)
                            .addComponent(jLabel4)
                            .addGap(10, 10, 10)
                            .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(15, 15, 15)
                            .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel2)
                                .addComponent(spnMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(spnDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(15, 15, 15)
                            .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel6)
                                .addComponent(txtDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(BtnPickDate))
                            .addGap(18, 18, 18)
                            .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(btnAdd)
                                .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(btnUpdate)
                                    .addComponent(btnDelete)
                                    .addComponent(btnRefresh))))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, btnWriteScriptLayout.createSequentialGroup()
                            .addComponent(jLabel1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(lblPic, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(45, 45, 45)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnWrScript)
                    .addComponent(btnReadScript)
                    .addComponent(btnDelRow))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(CetralPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(175, 175, 175)
                        .addComponent(UpperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnWriteScript, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(UpperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CetralPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnWriteScript, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtSelectPictureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSelectPictureActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSelectPictureActionPerformed

    private void btnBrowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowActionPerformed
        // TODO add your handling code here:
         
        txtName.setText("");
        txtSelectPicture.setText("");
        img = null;
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
                imagefile = ch.getSelectedFile();
                curfile = imagefile.getParentFile();
                
                
                String name = imagefile.getName();
                txtSelectPicture.setText(name);
                String str = getImageName(name);
                
                if(txtName.getText() == null || txtName.getText().isEmpty())
                     txtName.setText(str);
               
                img = ImageIO.read(imagefile);
                
                setImageToLabel(img, lblPic);
                
               imageBrowsed = true;

            } catch (IOException ex) {
                Logger.getLogger(PictureManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }//GEN-LAST:event_btnBrowActionPerformed

    private void txtNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNameActionPerformed

    private void BtnPickDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPickDateActionPerformed
        // TODO add your handling code here:
        Date d = new GregorianCalendar().getTime();
        DatePickerNewDialog dlg = new DatePickerNewDialog(this, true, d);
        dlg.setVisible(true);
        Date dt = dlg.getSelectedDate();
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        if(dt!=null)
        {
            txtDate.setText(df.format(dt));
        }
        dlg.dispose();

    }//GEN-LAST:event_BtnPickDateActionPerformed

    private void radRegularActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radRegularActionPerformed
        // TODO add your handling code here:
        txtDate.setEnabled(false);
        BtnPickDate.setEnabled(false);
        spnDay.setEnabled(true);
        spnDay.setValue(1);
        spnMonth.setEnabled(true);
        spnMonth.setValue(1);
    }//GEN-LAST:event_radRegularActionPerformed

    private void radIrregularActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radIrregularActionPerformed
        // TODO add your handling code here:
        txtDate.setEnabled(true);
        BtnPickDate.setEnabled(true);
        spnDay.setEnabled(false);
        spnMonth.setEnabled(false);
    }//GEN-LAST:event_radIrregularActionPerformed

    private void btnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseActionPerformed
        // TODO add your handling code here:
        JFileChooser ch = new JFileChooser();
        ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        FileNameExtensionFilter pfilters = new FileNameExtensionFilter("Picture files", "jpg","jpeg","jpe","jfif","bmp","png","dib");
        ch.addChoosableFileFilter(pfilters);
        ch.setAcceptAllFileFilterUsed(false);
        int result = ch.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION)
        {
            bulkFile = ch.getSelectedFile();
            txtFolderName.setText(bulkFile.getAbsolutePath());
        }
        
    }//GEN-LAST:event_btnBrowseActionPerformed

    private boolean isNameExists(String name)
    {
        for (int i = 0; i < holidayModel.size(); i++) {
            if(holidayModel.getElementAt(i).getName().equals(name))
                return false;
            
        }
        return true;
    }
    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:

            if(img!= null && (txtName.getText()!=null && !txtName.getText().isEmpty()))
            {
                if(isNameExists(txtName.getText().trim()))
                   addImageToDatabase(img, txtName.getText().trim(),imagefile);
                else
                  JOptionPane.showMessageDialog(this, "Holiday by this name already exists", "Message", JOptionPane.ERROR_MESSAGE);
 
            }
            else
            {
                if(txtName.getText() ==  null || txtName.getText().isEmpty())
                     JOptionPane.showMessageDialog(this, "Please enter a name", "Message", JOptionPane.ERROR_MESSAGE);
                if(img == null)
                  JOptionPane.showMessageDialog(this, "Please select an image", "Message", JOptionPane.ERROR_MESSAGE);


            }
       imageBrowsed = false;
       img = null;
    }//GEN-LAST:event_btnAddActionPerformed

    private void txtFolderNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFolderNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFolderNameActionPerformed

    private void lstHolidaysValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstHolidaysValueChanged
        // TODO add your handling code here:
        
        if(lstHolidays.getSelectedIndex()>=0)
        {
               imageBrowsed = false;

        txtSelectPicture.setText("");
        HolidayPics hdy = lstHolidays.getSelectedValue();
        setImageToLabel(hdy.getImage(), lblPic);
        txtName.setText(hdy.getName());
        char ch = hdy.getHtype();
        if(ch == 'Y')
        {
            radRegular.setSelected(true);
            spnDay.setEnabled(true);
            spnMonth.setEnabled(true);
            spnDay.setValue(hdy.getDay());
            spnMonth.setValue(hdy.getMonth());
            txtDate.setText("");
            txtDate.setEnabled(false);
            BtnPickDate.setEnabled(false);
        }
        else
        {
//            try {
                radIrregular.setSelected(true);
                BtnPickDate.setEnabled(true);
                spnDay.setValue(0);
                spnMonth.setValue(0);
                spnDay.setEnabled(false);
                spnMonth.setEnabled(false);
                txtDate.setEnabled(true);
                txtDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(hdy.getDt()));
//           
        }
        
      }  
    }//GEN-LAST:event_lstHolidaysValueChanged

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
        HolidayPics hp = lstHolidays.getSelectedValue();
        
            try 
            {
                con.setAutoCommit(false);
                PreparedStatement pst;
                if(hp.getHtype() == 'Y')
                {    
                     pst = con.prepareStatement("Delete from RegularHoliday where HPicId = ?");
                     pst.setInt(1, hp.getId());
                     pst.executeQuery();
                     spnDay.setValue(1);
                     spnMonth.setValue(1);
                }
                else
                {
                    pst = con.prepareStatement("Delete from IrregularHoliday where HPicId = ?");
                    pst.setInt(1, hp.getId());
                    pst.executeQuery();
                    txtDate.setText("");
                }
                pst = con.prepareStatement("Delete from HolidayPic where Id = ?");
                pst.setInt(1, hp.getId());
                pst.executeQuery();
                txtName.setText("");
                lblPic.setIcon(null);
                pst.close();
                
                String name = hp.getName();
                char c ;
                if(hp.getHtype() == 'Y')
                    c = 'R';
                else
                    c = 'I';
                
                holidayModel.removeElement(hp);
                Image image = null;
                
                HolidayCommandItem hci = new HolidayCommandItem(c, 'D', name, null, null,image, null);
                tableModel.addRow(hci);
                int row = tableModel.getRowCount() - 1;
                tableModel.fireTableRowsInserted(row, row);
                tabHolCommand.setRowSelectionInterval(row, row);
                
                imageBrowsed = false;
                
                con.commit();
                con.setAutoCommit(true);
                 
                 img = null;


            } 
            catch (SQLException ex) {
                Logger.getLogger(PictureManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        
    }//GEN-LAST:event_btnDeleteActionPerformed

  private boolean isUpdatePossible(HolidayPics hp)
  {
        try {
            System.out.println("imagebrowsed: "+imageBrowsed);
            if(!hp.getName().equals(txtName.getText().trim()))
                return true;
            
            else if(hp.getHtype() == 'Y' && ((hp.getMonth() != (Integer)spnMonth.getValue()) || (hp.getDay() != (Integer)spnDay.getValue())) )
            {
                    return true;
            }
            else if(hp.getHtype() == 'N' && hp.getDt().compareTo( new SimpleDateFormat("dd/MM/yyyy").parse(txtDate.getText())) != 0)
                    return true;
            else if(imageBrowsed)
            {
                System.out.println("yo");
                  return true;
            }
            
            return false;
        } catch (ParseException ex) {
            Logger.getLogger(PictureManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
  }
    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        // TODO add your handling code here:
        HolidayPics hp = lstHolidays.getSelectedValue();
        if(hp!=null && isUpdatePossible(hp))   //if hp is null thai is no item is selected from the list
        {
            File imgFile =  null;
        
            String name = txtName.getText();
            HolidayCommandItem hci;

            if(name!= null && !name.isEmpty())//if name is null or empty
            {
                int id = hp.getId();
                String sql = "";
                PreparedStatement pstmt;
                try {
                    
                    con.setAutoCommit(false);
                    String oldName = hp.getName();
                    
                    BufferedImage tempimg;
                    if(img!=null)
                       tempimg = imageToBufferedImage(img);
                    else
                     tempimg = imageToBufferedImage(hp.getImage());
 
                    Blob blob = null;
                    blob = con.createBlob();
                    OutputStream out = blob.setBinaryStream(0);
                    ImageIO.write(tempimg, "jpg", out);

                    if(hp.getHtype() == 'Y')//if holiday type is regular
                    {
                            if(radRegular.isSelected())//if holiday type is regular and regular radio button is selected
                            {
                                sql = "Update HolidayPic "
                                +"set Name = ? , Pic = ? where Id = ?";
                                pstmt = con.prepareStatement(sql);
                                pstmt.setString(1, name);
                                pstmt.setBlob(2, blob);
                                pstmt.setInt(3, id);
                                pstmt.executeUpdate();
                                
                                pstmt.close();

                                int day = (int)spnDay.getValue();
                                int month = (int)spnMonth.getValue();

                                sql = "Update RegularHoliday "
                                      +" set month = ?,day = ? where HPicId = ?";

                                pstmt = con.prepareStatement(sql);
                                pstmt.setInt(1, month);
                                pstmt.setInt(2, day);
                                pstmt.setInt(3, id);
                                pstmt.executeUpdate();

                                pstmt.close();
                                
                                
                                if(name.equals(oldName))   //if old name and new name are the same
                                {
                                    name = null;
                                }
                                else
                                   hp.setName(name);
                                    
                                
                                //To check if dates are equal or not
                                Date dt;
                                if(hp.getDay() == day && hp.getMonth() == month)
                                    dt = null;
                                else
                                {
                                    dt = new GregorianCalendar(0, month-1, day).getTime();
                                    hp.setDay(day);
                                    hp.setMonth(month);
                                }
                                
                                //To check if image is new or not
                                Image image = null;
                                if(img == hp.getImage())
                                {
                                    image = null;
                                    imgFile = null;
                                }
                                else if(img!=null)
                                {
                                   hp.setImage(img);
                                   image = img;
                                   imgFile = imagefile;
                                }
                               
                                hci = new HolidayCommandItem('R', 'U', oldName, dt, imgFile, image, name);
                                tableModel.addRow(hci);
                                int row = tableModel.getRowCount() - 1;
                                tableModel.fireTableRowsInserted(row, row);
                                tabHolCommand.setRowSelectionInterval(row, row);
                                
                                repaint();
                            }
                            else //if holiday type is regular but irregular radio button is selected
                            {
                                String dt = txtDate.getText();
                                if(dt!=null && !dt.isEmpty())
                                {
                                    Date udt = new SimpleDateFormat("dd/MM/yyyy").parse(dt);
                                    java.sql.Date sdt = new java.sql.Date(udt.getTime());

                                    sql = "Update HolidayPic "
                                    +"set Name = ?,Regular = ? where Id = ?";
                                    pstmt = con.prepareStatement(sql);
                                    pstmt.setString(1, name);
                                    pstmt.setString(2, "N");
                                    pstmt.setInt(3, id);
                                    pstmt.executeUpdate();
                                    
                                    pstmt.close();

                                    sql = "Delete from RegularHoliday where HPicId = ?";  
                                    pstmt = con.prepareStatement(sql);
                                    pstmt.setInt(1, id);
                                    pstmt.executeQuery();
                                    
                                    pstmt.close();

                                    sql = "Insert into IrregularHoliday(Id,Dt,HPicId) ";
                                    sql+="values(seq.nextval,?,?)";
                                    pstmt = con.prepareStatement(sql);
                                    pstmt.setDate(1, sdt);
                                    pstmt.setInt(2, id);
                                    pstmt.executeQuery();

                                    pstmt.close();

                                    hp.setName(name);
                                    hp.setDt(udt);
                                    hp.setDay(0);
                                    hp.setMonth(0);
                                    hp.setHtype('N');
                                    
                                    radIrregular.setSelected(true);

                                }
                                else
                                {
                                   JOptionPane.showMessageDialog(this, "Date not selected!", "Message", JOptionPane.ERROR_MESSAGE);

                                }
                            }
                     } 
                    else //if holiday type is irregular
                    {
                        if(radIrregular.isSelected()) //if holiday type is irregular and irregular radio button is selected
                        {

                                String dt = txtDate.getText();
                                Date udt = new SimpleDateFormat("dd/MM/yyyy").parse(dt);
                                java.sql.Date sdt = new java.sql.Date(udt.getTime());


                                sql = "Update HolidayPic "
                                +"set Name = ? , Pic = ? where Id = ?";
                                pstmt = con.prepareStatement(sql);
                                pstmt.setString(1, name);
                                pstmt.setBlob(2, blob);
                                pstmt.setInt(3, id);
                                pstmt.executeUpdate();
                                
                                pstmt.close();


                                sql = "Update IrregularHoliday "
                                +"set Dt = ? where  HPicId = ?";

                                pstmt = con.prepareStatement(sql);
                                pstmt.setDate(1, sdt);
                                pstmt.setInt(2, id);
                                pstmt.executeUpdate();

                                pstmt.close();

//                                hp.setName(name);
//                                hp.setDt(udt);


                               if(name.equals(oldName))   //if old name and new name are the same
                                {
                                    name = null;
                                }
                                else
                                   hp.setName(name);
                                    
                                
                                //To check if dates are equal or not
                                Date dat;
                                if(hp.getDt().compareTo(udt) == 0)
                                    dat = null;
                                else
                                {
                                    dat = udt;
                                    hp.setDt(dat);
                                }
                                
                                //To check if image is new or not
                                Image image = null;
                                if(img == hp.getImage())
                                {
                                    image = null;
                                    imgFile = null;
                                }
                                else if(img!=null)
                                {
                                   hp.setImage(img);
                                   image =img;
                                   imgFile = imagefile;
                                }
                             
                                
                              hci = new HolidayCommandItem('I', 'U', oldName, dat, imgFile, image, name);
                              tableModel.addRow(hci);
                              int row = tableModel.getRowCount() - 1;
                              tableModel.fireTableRowsInserted(row, row);
                              tabHolCommand.setRowSelectionInterval(row, row);
                              
                              repaint();
                        }
                        else //if holiday type is irregular but regular radio button is selected
                        {
                            int day = (int)spnDay.getValue();
                            int month = (int)spnMonth.getValue();

                            sql = "Update HolidayPic "
                            +"set Name = ?,Regular = ? where Id = ?";
                            pstmt = con.prepareStatement(sql);
                            pstmt.setString(1, name);
                            pstmt.setString(2, "Y");
                            pstmt.setInt(3, id);
                            pstmt.executeUpdate();
                            
                            pstmt.close();

                            sql = "Delete from IrregularHoliday where HPicId = ?";  
                            pstmt = con.prepareStatement(sql);
                            pstmt.setInt(1, id);
                            pstmt.executeQuery();

                            sql = "Insert into RegularHoliday(Id,Month,Day,HPicId) ";
                            sql+="values(seq.nextval,?,?,?)";
                            pstmt = con.prepareStatement(sql);
                            pstmt.setInt(1, month);
                            pstmt.setInt(2, day);
                            pstmt.setInt(3, id);
                            pstmt.executeQuery();

                            pstmt.close();

                            hp.setName(name);
                            hp.setDt(null);
                            hp.setDay(day);
                            hp.setMonth(month);
                            hp.setHtype('Y');
                            
                            radRegular.setSelected(true);

                        }

                    }
                    con.commit();
                    con.setAutoCommit(true);
                }
                catch (SQLException | ParseException | IOException ex) {
                    Logger.getLogger(PictureManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else
            {
                JOptionPane.showMessageDialog(this, "Name not selected!", "Message", JOptionPane.ERROR_MESSAGE);

            }

        }
        else
        {
            String str = "";
            if(hp == null)
                str = "Select an item to update!";
            else
                str = "Please make changes to update" ;
              JOptionPane.showMessageDialog(this, str, "Message", JOptionPane.ERROR_MESSAGE);
               
        }
        imageBrowsed = false;
        img = null;

    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnRemoveAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveAllActionPerformed
        try {
            // TODO add your handling code here:
            stmt = con.createStatement();
            String sql = "Delete from RegularHoliday";
            stmt.executeQuery(sql);
            sql = "Delete from IrregularHoliday";
            stmt.executeQuery(sql);
            sql = "Delete from HolidayPic";
            stmt.executeQuery(sql);
            
            stmt.close();
            
            holidayModel.clear();
            lblPic.setIcon(null);
            txtName.setText("");
            }
        catch (SQLException ex) {
            Logger.getLogger(PictureManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnRemoveAllActionPerformed

    private void btnAddAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddAllActionPerformed
        // TODO add your handling code here:
        if(bulkFile == null)
        {
           JOptionPane.showMessageDialog(this, "Select a folder!", "Message", JOptionPane.ERROR_MESSAGE);

        }
        else
        {
            radRegular.setSelected(true);
            txtDate.setText("");
            txtDate.setEnabled(false);
            txtName.setText("");
            txtSelectPicture.setText("");
            spnDay.setValue(1);
            spnMonth.setValue(1);
            addBulkImagesToDatabase(bulkFile);
        }
    }//GEN-LAST:event_btnAddAllActionPerformed

    private void btnWrScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWrScriptActionPerformed
        try {
//            addImagesToFolder();
            File file = new File("data", "PicInfo.dat");
            FileOutputStream fos = new FileOutputStream(file);
            DataOutputStream dos = new DataOutputStream(fos);
            writeallHCIs(new File("new.zip"), tableModel.arl);
            //adding images to HolidayPic Folder
            dos.close();
            fos.close();
        } catch (IOException ex) {
            Logger.getLogger(PictureManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnWrScriptActionPerformed

    private void btnReadScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReadScriptActionPerformed
        try {
            tableModel.arl.clear();
            FileInputStream fis = null;
            File file = new File("/data/PicInfo.dat");
            fis = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(fis);
            readHcis(dis);
            dis.close();
            fis.close();
        } catch (IOException ex) {
            Logger.getLogger(PictureManager.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }//GEN-LAST:event_btnReadScriptActionPerformed

    private void btnDelRowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelRowActionPerformed
        // TODO add your handling code here:
        int row = tabHolCommand.getSelectedRow() ;
        
        if(row != -1)
        {
            tableModel.arl.remove(row);
            tableModel.fireTableRowsDeleted(row, row);
            int rows = tabHolCommand.getRowCount() ;
            if(rows > 0)
            {
                if(row == rows)
                    row = rows - 1 ;
                tabHolCommand.setRowSelectionInterval(row, row);
            }
        }
                
    }//GEN-LAST:event_btnDelRowActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
        txtName.setText("");
        radRegular.setSelected(true);
        spnDay.setEnabled(true);
        spnDay.setValue(1);
        spnMonth.setEnabled(true);
        spnMonth.setValue(1);
        txtDate.setText("");
    }//GEN-LAST:event_btnRefreshActionPerformed

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
            java.util.logging.Logger.getLogger(PictureManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PictureManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PictureManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PictureManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PictureManager().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BtnPickDate;
    private javax.swing.JPanel CetralPanel;
    private javax.swing.JPanel UpperPanel;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnAddAll;
    private javax.swing.JButton btnBrow;
    private javax.swing.JButton btnBrowse;
    private javax.swing.JButton btnDelRow;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnReadScript;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnRemoveAll;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JButton btnWrScript;
    private javax.swing.JPanel btnWriteScript;
    private javax.swing.ButtonGroup btngrpHoliday;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblPic;
    private javax.swing.JList<HolidayPics> lstHolidays;
    private javax.swing.JRadioButton radIrregular;
    private javax.swing.JRadioButton radRegular;
    private javax.swing.JSpinner spnDay;
    private javax.swing.JSpinner spnMonth;
    private javax.swing.JTable tabHolCommand;
    private javax.swing.JTextField txtDate;
    private javax.swing.JTextField txtFolderName;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtSelectPicture;
    // End of variables declaration//GEN-END:variables
}
