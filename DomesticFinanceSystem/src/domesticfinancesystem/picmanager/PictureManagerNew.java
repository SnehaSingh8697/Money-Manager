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
import java.util.zip.ZipFile;
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
public class PictureManagerNew extends javax.swing.JFrame {

    /**
     * Creates new form PictureManager
     */
    
    private DefaultListModel<HolidayPicsNew> holidayModel = new DefaultListModel<HolidayPicsNew>();
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
    private ArrayList<HolidayCommandItem>arlHCIs = new ArrayList<>();
    private Date fileDate;
    
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
    
    public PictureManagerNew() {
        initComponents();
       ((AbstractDocument)txtName.getDocument()).setDocumentFilter(new NameLengthFilter());

        tableModel = new MyTableModel();
        tabHolCommand.setModel(tableModel);
        
        tabHolCommand.setRowHeight(50);
        tabHolCommand.setShowGrid(true);
        
        imageBrowsed = false;
        
        radCurrentDate.setSelected(true);
        radCurrentDate.doClick();
        
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
        setHolidayPicsToList();
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
     private void setHolidayPicsToList()
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
                    
                    holidayModel.addElement(new HolidayPicsNew(name, img, ch, day, month, null));
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
                    
                    holidayModel.addElement(new HolidayPicsNew(name, img, ch, day, month, dt));
                    rs.close();
                    pst.close();
                    
                }
            }
            rst.close();
            stmt.close();
            
        } catch (SQLException | IOException ex) {
            Logger.getLogger(PictureManagerNew.class.getName()).log(Level.SEVERE, null, ex);
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
        byte mask = 0;
        
        if(hci.gethType() == '-')
            return b;
        
        if(hci.gethType() == 'R')
            mask = (byte)0b00_0_00000;
        else if(hci.gethType() == 'I')
            mask = (byte)0b00_1_00000;
        
        b |= mask;
        
        if(hci.getCommand() == 'A')
            mask = (byte)0b000_01_000;
        else if(hci.getCommand() == 'U')
            mask = (byte)0b000_10_000;
        else if(hci.getCommand() == 'D')
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
       
       if(cb == 0)
       {
           hci.setCommand('R');
           return hci;
       }
       else if((cb & 0b000_11_000) == 0b000_11_000)
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
//           File imageFile = new File(file.getName());
//           Image image = ImageIO.read(imageFile);
//           hci.setImage(image);

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
      
       short date = packedDate(fileDate);
       dos.writeShort(date);
       
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
       if(str!=null)
       {
        dos.writeByte((int)str.length());
        dos.writeBytes(str);
       }
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
//   
//   private void addImagesToFolder() throws IOException
//   {
//       File parentFolder = new File("data","HolidayPics");
//       if(!parentFolder.exists())
//           parentFolder.mkdirs();
//       for (HolidayCommandItem hci : tableModel.arl) {
//           
//          File inputFile = hci.gethPicFile();
//          if(inputFile!=null)
//          {
//              File outputFile = new File(parentFolder, inputFile.getName());
//              if(!outputFile.exists())
//              fileCopy(inputFile, outputFile);
//          }
//          
//           
//       }
//   }
    
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
                    addNewHci(image, nm,f);
                }
            } 
            catch (IOException ex) {
                Logger.getLogger(PictureManagerNew.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void addNewHci(Image img,String name,File imagefile)
    {
        try {
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
                    HolidayCommandItem hci;
                    String newName = null;
                    HolidayPicsNew hp;
                    char htype;
                    if(radRegular.isSelected())
                    {
                        htype = 'Y';
                        int mnth = (int)spnMonth.getValue();
                        int dy = (int)spnDay.getValue();
                        Date date = new GregorianCalendar(0, mnth - 1, dy).getTime();
                        hci = new HolidayCommandItem('R', 'A', name, date, imagefile, img, newName);
                        hp = new HolidayPicsNew(name, img, htype,dy,mnth,null);
                        holidayModel.addElement(hp);
                    }
                    else
                    {
                        htype = 'N';
                        Date udt = new SimpleDateFormat("dd/MM/yyyy").parse(txtDate.getText());
                        java.sql.Date sdt = new java.sql.Date(udt.getTime());
                        hci = new HolidayCommandItem('I', 'A', name, udt,imagefile, img, newName);
                        hp = new HolidayPicsNew(name, img, htype,0,0,udt);
                        holidayModel.addElement(hp);


                    }
                    arlHCIs.add(hci); 
                 }
        } catch (ParseException | SQLException ex) {
            Logger.getLogger(PictureManagerNew.class.getName()).log(Level.SEVERE, null, ex);
        }
           
    }
    private void addHciToDatabase(HolidayCommandItem hci,Connection con,ZipFile zipfile) throws SQLException,IOException
    {
       Blob blob = null;
       blob = con.createBlob();
       OutputStream out = blob.setBinaryStream(0);
       if(zipfile == null)
       {
            BufferedImage tempimg;
            tempimg = imageToBufferedImage(ImageIO.read(hci.gethPicFile()));
            ImageIO.write(tempimg, "jpg", out);
       }
       else
       {
           ZipEntry ze = null ;

           ze = zipfile.getEntry(hci.gethPicFile().getName());
           InputStream in = zipfile.getInputStream(ze) ;
        
            for (int b = in.read(); b !=-1; b = in.read()) {
                out.write(b);
            }
            in.close();
            out.close();
       }
       
       //Adding Holiday to database
       
        con.setAutoCommit(false);
        //Deleting record if record by this name already exists;
            String sql = "Select Id from HolidayPic where name = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, hci.getName());
            ResultSet rst = pstmt.executeQuery();
            int id = -1;
            if(rst.next())
            {
                id = rst.getInt(1);
            }
            rst.close();
            pstmt.close();
            if(id!=-1)
            {
                if(hci.gethType() == 'R')
                {
                    sql = "Delete from RegularHoliday where HPicId = "+id;
                }
                else
                {
                   sql = "Delete from IrregularHoliday where HPicId = "+id; 
                }
                stmt = con.createStatement(); 
                stmt.executeUpdate(sql);
                stmt.close();
                
                sql = "Delete from HolidayPic where Id = "+id;
                stmt = con.createStatement(); 
                stmt.executeUpdate(sql);
                stmt.close();
            }
        
            
            
        sql = "Insert into HolidayPic(Id,Name,Pic,Regular) ";
        sql+="values(seq.nextval,?,?,?)";
        pstmt = con.prepareStatement(sql);
        String picname = hci.getName();
        pstmt.setString(1,picname);
        pstmt.setBlob(2, blob);
        String ch ;
        int mnth = 0;
        int dy = 0;
        if(hci.gethType() == 'R')
        {
            ch = "Y";
        }
        else
        {
            ch = "N";
        }
        pstmt.setString(3,ch);
        pstmt.executeUpdate();
        pstmt.close();

        sql = "Select Id from HolidayPic where Name = ?";
        pstmt = con.prepareStatement(sql);
        pstmt.setString(1,picname);
        rst = pstmt.executeQuery();
        int hpicid = 0;
        if(rst.next())
        {
            hpicid = rst.getInt(1);
        }

        if(hci.gethType() == 'R')
        {
            mnth = hci.getDt().getMonth()+1;
            dy = hci.getDt().getDate();
            sql = "Insert into RegularHoliday(Id,Month,Day,HPicId) ";
            sql+="values(seq.nextval,?,?,?)";

            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, mnth);
            pstmt.setInt(2, dy);
            pstmt.setInt(3, hpicid);

        }
        else
        {
            sql = "Insert into IrregularHoliday(Id,Dt,HPicId) ";
            sql+="values(seq.nextval,?,?)";
            java.sql.Date sdt = new java.sql.Date(hci.getDt().getTime());
            pstmt = con.prepareStatement(sql);
            pstmt.setDate(1, sdt);
            pstmt.setInt(2, hpicid);
        }
        pstmt.executeUpdate();
        pstmt.close();
        
        con.commit();
        con.setAutoCommit(true);
       
    }
    private ArrayList importHCIsFromZip(ZipFile zipFile) throws IOException
    {
        ArrayList<HolidayCommandItem> arlHCIs = new ArrayList<>();
        ZipEntry ze = null ;
        ze = zipFile.getEntry("PicInfo.dat");
        InputStream in = zipFile.getInputStream(ze) ;
      
        arlHCIs = readAllHcis(in);
        
        return arlHCIs;
        
    }
    private ArrayList<HolidayCommandItem> readAllHcis(InputStream in) throws IOException
    {
        ArrayList<HolidayCommandItem> arlHCIs = new ArrayList<>();
        DataInputStream dis = new DataInputStream(in);
        
        short size = dis.readShort();
        short packedDate = dis.readShort();
        fileDate = unpackDate(packedDate);
        lblFileDate.setText(irregDateFormatter.format(fileDate));
        short i = 0;
        while(i<size)
        {
            HolidayCommandItem hci = readHci(dis);
            arlHCIs.add(hci);
            i++;
        }
        return arlHCIs;
    }
    private void removeAllHolidays()
    {
         try {
            // TODO add your handling code here:
            con.setAutoCommit(false);
            
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
            
            con.commit();
            con.setAutoCommit(true);

            }
        catch (SQLException ex) {
            Logger.getLogger(PictureManagerNew.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    private void executeHci(HolidayCommandItem hci,Connection con,ZipFile zipfile)
    {
        try {
                if(hci.getCommand() == 'R')
                {
                    removeAllHolidays();
                }
                else if(hci.getCommand() == 'A')//Adding a new holiday
                {
                        addHciToDatabase(hci, con, zipfile);
                }
                else if(hci.getCommand() == 'D')
                {
                    deleteHciFromDatabase(hci, con);
                }
                else
                {
                    updateHciInDatabase(hci, con, zipfile);
                }
                
         } 
         catch (SQLException |IOException ex) 
         {
                Logger.getLogger(PictureManagerNew.class.getName()).log(Level.SEVERE, null, ex);
         }
    }
    private void executeHci(HolidayCommandItem hci,Connection con)
    {
        executeHci(hci, con, null);
    }
    private ArrayList<HolidayCommandItem> addHcisToTable(ArrayList<HolidayCommandItem> hcis,ZipFile zipfile)
    {
        tableModel.arl.clear();
        for (HolidayCommandItem hci : hcis) {
            if(hci.gethPicFile()!=null && zipfile != null)
                hci.extractImage(zipfile);
            
            tableModel.addRow(hci);
            int row = tableModel.getRowCount() - 1;
            tableModel.fireTableRowsInserted(row, row);
            tabHolCommand.setRowSelectionInterval(row, row);     
            
        }
        return hcis;
    }
    private void addHolidaystoList(ArrayList<HolidayCommandItem> hcis)
    {
        HolidayPicsNew hpn;
        for (HolidayCommandItem hci : hcis) {
            Image img = hci.getImage();
            if(hci.getCommand() == 'A')
            {
                char ch ;
                Date dt = hci.getDt();
                if(hci.gethType() == 'R')
                {
                    ch = 'Y';
                    hpn = new HolidayPicsNew(hci.getName(), img, ch, dt.getDate(), dt.getMonth()+1, null);
                }
                else
                {
                    ch = 'N';
                    hpn = new HolidayPicsNew(hci.getName(), img, ch, 0, 0, hci.getDt());

                }
                holidayModel.addElement(hpn);
            }
            else if(hci.getCommand() == 'D')
            {
                String name = hci.getName();
                for (int i = 0; i < holidayModel.size(); i++) {
                   if(holidayModel.get(i).getName().equals(name))
                   {
                       holidayModel.remove(i);
                       txtName.setText("");
                       lblPic.setIcon(new ImageIcon());
                       break;
                   }
                }
            }
            else
            {
                String name = hci.getName();
                System.out.println("elemnt to updat: "+name);
                System.out.println("size model"+holidayModel.size());
                for (int i = 0; i < holidayModel.size(); i++) {
                   if(holidayModel.get(i).getName().equals(name))
                   {
                       System.out.println(name+" found");
                      hpn = holidayModel.get(i);
                      if(hci.getNewName()!=null)
                          hpn.setName(hci.getNewName());
                      if(hci.getImage()!=null)
                          hpn.setImage(hci.getImage());
                      if(hci.getDt()!=null)
                      {
                          Date dt = hci.getDt();
                          if(hci.gethType() == 'R')
                          {
                              hpn.setDay(dt.getDate());
                              hpn.setMonth(dt.getMonth() + 1);
                          }
                          else
                              hpn.setDt(dt);
                      }
                      holidayModel.remove(i);
                      holidayModel.addElement(hpn);
                      break;
                   }
                }
            }
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
        btngrpDate = new javax.swing.ButtonGroup();
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
        btnUpdateDatabase = new javax.swing.JButton();
        btnUpdateTable = new javax.swing.JButton();
        radCurrentDate = new javax.swing.JRadioButton();
        radAnyDate = new javax.swing.JRadioButton();
        btnDatePicker = new javax.swing.JButton();
        txtFileDate = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        lblFileDate = new javax.swing.JLabel();

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
                .addGap(27, 27, 27)
                .addComponent(btnBrowse)
                .addGap(87, 87, 87)
                .addComponent(btnAddAll, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(btnRemoveAll)
                .addContainerGap(117, Short.MAX_VALUE))
        );
        CetralPanelLayout.setVerticalGroup(
            CetralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CetralPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(CetralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddAll)
                    .addComponent(btnRemoveAll))
                .addGap(34, 34, 34))
            .addGroup(CetralPanelLayout.createSequentialGroup()
                .addGroup(CetralPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFolderName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBrowse))
                .addGap(0, 0, Short.MAX_VALUE))
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

        btnUpdateDatabase.setText("Update Database");
        btnUpdateDatabase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateDatabaseActionPerformed(evt);
            }
        });

        btnUpdateTable.setText("Update Table");
        btnUpdateTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateTableActionPerformed(evt);
            }
        });

        btngrpDate.add(radCurrentDate);
        radCurrentDate.setText("Current Date");
        radCurrentDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radCurrentDateActionPerformed(evt);
            }
        });

        btngrpDate.add(radAnyDate);
        radAnyDate.setText("Any Date");

        btnDatePicker.setText("...");
        btnDatePicker.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDatePickerActionPerformed(evt);
            }
        });

        txtFileDate.setEditable(false);

        jLabel7.setText("Read File Date");

        lblFileDate.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

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
                            .addGroup(btnWriteScriptLayout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(18, 18, 18)
                                .addComponent(spnDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(spnMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(btnWriteScriptLayout.createSequentialGroup()
                                .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(btnWriteScriptLayout.createSequentialGroup()
                                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(btnUpdate)
                                        .addGap(19, 19, 19)
                                        .addComponent(btnDelete))
                                    .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(btnWriteScriptLayout.createSequentialGroup()
                                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(txtDate, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(BtnPickDate))))
                                .addGap(18, 18, 18)
                                .addComponent(btnRefresh))
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(btnWriteScriptLayout.createSequentialGroup()
                                .addComponent(txtSelectPicture, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(39, 39, 39)
                                .addComponent(btnBrow)))
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(btnWriteScriptLayout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(lblFileDate, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(30, 30, 30))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, btnWriteScriptLayout.createSequentialGroup()
                                .addComponent(lblPic, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(101, 101, 101))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, btnWriteScriptLayout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(136, 136, 136))))))
            .addGroup(btnWriteScriptLayout.createSequentialGroup()
                .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(btnWriteScriptLayout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(radCurrentDate)
                            .addGroup(btnWriteScriptLayout.createSequentialGroup()
                                .addComponent(radAnyDate)
                                .addGap(18, 18, 18)
                                .addComponent(txtFileDate, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(btnWriteScriptLayout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(btnDelRow, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnWrScript)
                        .addGap(18, 18, 18)
                        .addComponent(btnReadScript)
                        .addGap(18, 18, 18)
                        .addComponent(btnUpdateDatabase)
                        .addGap(18, 18, 18)
                        .addComponent(btnUpdateTable)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        btnWriteScriptLayout.setVerticalGroup(
            btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(btnWriteScriptLayout.createSequentialGroup()
                .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, btnWriteScriptLayout.createSequentialGroup()
                        .addGap(267, 267, 267)
                        .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(radAnyDate)
                            .addComponent(txtFileDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDatePicker)))
                    .addGroup(btnWriteScriptLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(btnWriteScriptLayout.createSequentialGroup()
                                .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(btnWriteScriptLayout.createSequentialGroup()
                                            .addComponent(jLabel3)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(txtSelectPicture, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(btnBrow))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jLabel4)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(spnMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(spnDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel2))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jLabel6)
                                                .addComponent(txtDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(BtnPickDate))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(btnAdd)
                                                .addComponent(btnUpdate)
                                                .addComponent(btnDelete)
                                                .addComponent(btnRefresh)))
                                        .addGroup(btnWriteScriptLayout.createSequentialGroup()
                                            .addComponent(jLabel1)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(lblPic, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(radCurrentDate)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, btnWriteScriptLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(lblFileDate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(btnWriteScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDelRow)
                    .addComponent(btnWrScript)
                    .addComponent(btnReadScript)
                    .addComponent(btnUpdateDatabase)
                    .addComponent(btnUpdateTable))
                .addContainerGap(75, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(CetralPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnWriteScript, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(UpperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(UpperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CetralPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
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
//               img = null;

            } catch (IOException ex) {
                Logger.getLogger(PictureManagerNew.class.getName()).log(Level.SEVERE, null, ex);
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

    private boolean isNameUnique(String name)
    {
        for (int i = 0; i < holidayModel.size(); i++) {
            if(holidayModel.get(i).getName().equals(name))
                return false;
            
        }
        return true;
    }
    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:

            if(img!= null && (txtName.getText()!=null && !txtName.getText().isEmpty()))
            {
                if(isNameUnique(txtName.getText().trim()))
                   addNewHci(img, txtName.getText().trim(),imagefile);
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
        HolidayPicsNew hdy = lstHolidays.getSelectedValue();
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
    private void deleteHciFromDatabase(HolidayCommandItem hci,Connection con)
    {
        try {
            con.setAutoCommit(false);
            PreparedStatement pst;
            
            String sql = "Select Id from HolidayPic where Name = ?";
            pst = con.prepareStatement(sql);
            pst.setString(1,hci.getName());
            ResultSet rst = pst.executeQuery();
            
            int hpicId = -1;
            if(rst.next())
            {
                hpicId = rst.getInt(1);
            }
            pst.close();
            if(hpicId!=-1)
            {
               if(hci.gethType() == 'R')
                {    
                     pst = con.prepareStatement("Delete from RegularHoliday where HPicId = ?");
                }
                else
                {
                    pst = con.prepareStatement("Delete from IrregularHoliday where HPicId = ?");
                }
                pst.setInt(1, hpicId);
                pst.executeQuery();
                pst.close();
                
                pst = con.prepareStatement("Delete from HolidayPic where Id = ?");
                pst.setInt(1,hpicId);
                pst.executeUpdate();
                pst.close();
            }
            con.commit();
            con.setAutoCommit(true);
            
        } catch (SQLException ex) {
            Logger.getLogger(PictureManagerNew.class.getName()).log(Level.SEVERE, null, ex);
        }
        

    }
    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
               int index = lstHolidays.getSelectedIndex();
               if(index>=0)
               {
                    HolidayPicsNew hp = lstHolidays.getSelectedValue();

                    String name = hp.getName();
                    holidayModel.removeElement(hp);
                    Image image = null;

                    char c ;
                    if(hp.getHtype() == 'Y')
                        c = 'R';
                    else
                        c = 'I';

                    HolidayCommandItem hci = new HolidayCommandItem(c, 'D', name, null, null,image, null);
                    holidayModel.removeElement(hp);

                    arlHCIs.add(hci);
                    imageBrowsed = false;
                    img = null;

                    txtName.setText("");
                    lblPic.setIcon(new ImageIcon());
                    spnDay.setValue(1);
                    spnMonth.setValue(1);
                    txtDate.setText("");
               }
               else
               {
                   JOptionPane.showMessageDialog(this, "Please select an item to delete", "Message", JOptionPane.ERROR_MESSAGE);

               }

    }//GEN-LAST:event_btnDeleteActionPerformed

  private boolean isUpdatePossible(HolidayPicsNew hp)
  {
        try {
            if(!hp.getName().equals(txtName.getText().trim()))
                return true;
            
            else if(hp.getHtype() == 'Y' && ((hp.getMonth() != (Integer)spnMonth.getValue()) || (hp.getDay() != (Integer)spnDay.getValue())) )
            {
                    return true;
            }
            else if(hp.getHtype() == 'N' && hp.getDt().compareTo( new SimpleDateFormat("dd/MM/yyyy").parse(txtDate.getText())) != 0)
                    return true;
            else if(img!=null)
            {
                  return true;
            }
            
            return false;
        } catch (ParseException ex) {
            Logger.getLogger(PictureManagerNew.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
  }
  private void updateHciInDatabase(HolidayCommandItem hci,Connection con,ZipFile zipfile)
  {
        try {
            con.setAutoCommit(false);
            boolean isNameFound,isPicFound,isDateFound;
            isNameFound = isPicFound = isDateFound = false;
            String newName = null;
            Date dt = null;
            Image image = null;
            Blob blob = null;
            if(hci.getNewName()!=null)
            {
                isNameFound = true;
                newName = hci.getNewName();
            }     
            if(hci.getDt()!=null)
            {
                isDateFound = true;
                dt = hci.getDt();
            }     
            if(hci.getImage()!=null)
            {
                isPicFound = true;
                image = hci.getImage();
                BufferedImage tempimg;
                tempimg = imageToBufferedImage(hci.getImage());
                blob = con.createBlob();
                OutputStream out = blob.setBinaryStream(0);
                ImageIO.write(tempimg, "jpg", out);
            } 
            String name = hci.getName();
            
            String sql = "Select Id from HolidayPic where name = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, name);
            ResultSet rst = pstmt.executeQuery();
            int id = -1;
            if(rst.next())
            {
                id = rst.getInt(1);
            }
            rst.close();
            pstmt.close();
            if(id!=-1)
            {
                if(isNameFound == true || isPicFound == true)
                {
                    sql = "Update HolidayPic ";
                    if(isNameFound == true && isPicFound == true)
                        sql+="set Name = ?,Pic = ?";
                    else if(isNameFound)
                        sql+="set Name = ?";
                    else if(isPicFound)
                        sql+="set Pic = ?";
                    sql+=" where id = ?";
                    pstmt = con.prepareStatement(sql);
                    if(isNameFound == true && isPicFound == true)
                    {
                        pstmt.setString(1, hci.getNewName());
                        pstmt.setBlob(2, blob);
                        pstmt.setInt(3, id);
                    }
                    else if(isNameFound)
                    {
                        pstmt.setString(1, hci.getNewName());
                        pstmt.setInt(2, id);

                    }
                    else if(isPicFound)
                    {
                        pstmt.setBlob(1, blob);
                        pstmt.setInt(2, id);
                    }
                    pstmt.executeUpdate();
                    pstmt.close();
                }
                
                if(isDateFound)
                {
                        //updating regular holiday
                        if(hci.gethType() == 'R')
                        {
                             int day = dt.getDate();
                             int month = dt.getMonth() + 1;
                             
                             sql = "Update RegularHoliday set Day = ?,Month = ? where HPicId = ?";
                             pstmt = con.prepareStatement(sql);
                             pstmt.setInt(1, day);
                             pstmt.setInt(2, month);
                             pstmt.setInt(3, id);
                             pstmt.executeUpdate();
                             pstmt.close();
                        }
                        else
                        {
                             java.sql.Date sdt = new java.sql.Date(dt.getTime());
                             sql = "Update IrregularHoliday set Dt = ? where HPicId = ?";
                             pstmt = con.prepareStatement(sql);
                             pstmt.setDate(1, sdt);
                             pstmt.setInt(2, id);
                             pstmt.executeUpdate();
                             pstmt.close();
                        }
                }
            }
            con.commit();
           con.setAutoCommit(true);
        } catch (SQLException | IOException ex) {
            Logger.getLogger(PictureManagerNew.class.getName()).log(Level.SEVERE, null, ex);
        }
      
  }
    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        // TODO add your handling code here:
        HolidayPicsNew hp = lstHolidays.getSelectedValue();
        if(hp!=null && isUpdatePossible(hp))   //if hp is null thai is no item is selected from the list
        {
            File imgFile =  null;
        
            String name = txtName.getText();
            HolidayCommandItem hci;

            if(name!= null && !name.isEmpty())//if name is null or empty
            {
               
                    String oldName = hp.getName();
                    
                    if(hp.getHtype() == 'Y')//if holiday type is regular
                    {
                                int day = (int)spnDay.getValue();
                                int month = (int)spnMonth.getValue();

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
                                arlHCIs.add(hci);
                                repaint();
                           
                    } 
                    else                     {
                        try //if holiday type is irregular
                        {
                            String dt = txtDate.getText();
                            Date udt = new SimpleDateFormat("dd/MM/yyyy").parse(dt);
                            java.sql.Date sdt = new java.sql.Date(udt.getTime());
                            
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
                            arlHCIs.add(hci);
                            repaint();
                        } catch (ParseException ex) {
                            Logger.getLogger(PictureManagerNew.class.getName()).log(Level.SEVERE, null, ex);
                        }
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
      HolidayCommandItem hci = new HolidayCommandItem('-', 'R', null, null, null, null, null);
      arlHCIs.add(hci);
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
            writeallHCIs(new File("new.zip"), arlHCIs);
            JOptionPane.showMessageDialog(this, "File wriiten successfully", "Message", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            Logger.getLogger(PictureManagerNew.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnWrScriptActionPerformed

    private void btnReadScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReadScriptActionPerformed
        try {
            File inputFile = new File("new.zip") ;
            ZipFile zf = new ZipFile(inputFile);
            arlHCIs = importHCIsFromZip(zf);
            for (HolidayCommandItem arlHci : arlHCIs) {
                executeHci(arlHci, con, zf);
            }
            //Adding holidays to jtable
            arlHCIs = addHcisToTable(arlHCIs, zf);
            //Adding holidays to list model
            addHolidaystoList(arlHCIs);
            JOptionPane.showMessageDialog(this, "File read successfully", "Message", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            Logger.getLogger(PictureManagerNew.class.getName()).log(Level.SEVERE, null, ex);
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
        lblPic.setIcon(new ImageIcon());
        img = null;
        curfile =  null;
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnUpdateDatabaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateDatabaseActionPerformed
        // TODO add your handling code here:
        for (HolidayCommandItem arlHCI : arlHCIs) {
            executeHci(arlHCI, con);
        }
        JOptionPane.showMessageDialog(this, "Database updated successfully!", "Message", JOptionPane.INFORMATION_MESSAGE);

    }//GEN-LAST:event_btnUpdateDatabaseActionPerformed

    private void btnUpdateTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateTableActionPerformed
        // TODO add your handling code here:
        addHcisToTable(arlHCIs, null);
         JOptionPane.showMessageDialog(this, "Table updated successfully!", "Message", JOptionPane.INFORMATION_MESSAGE);

    }//GEN-LAST:event_btnUpdateTableActionPerformed

    private void btnDatePickerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDatePickerActionPerformed
        // TODO add your handling code here:
         String str = txtFileDate.getText().trim();
            Date d;
            try
            {
               d = irregDateFormatter.parse(str) ;
            }
            catch (ParseException ex)
            {
                txtFileDate.setText("");
                d = new GregorianCalendar().getTime();  
            }
        
        DatePickerNewDialog dlg;
        dlg = new DatePickerNewDialog(null, true, d);
        dlg.setVisible(true);
        Date dt = dlg.getSelectedDate();
       
        if(dt!=null)
        {
            try {
                String s = irregDateFormatter.format(dt);
                dt = irregDateFormatter.parse(s);
                txtFileDate.setText(irregDateFormatter.format(dt));
            } catch (ParseException ex) {
                Logger.getLogger(PictureManagerNew.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        fileDate = dt;
        dlg.dispose();
    }//GEN-LAST:event_btnDatePickerActionPerformed

    private void radCurrentDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radCurrentDateActionPerformed
        try {
            // TODO add your handling code here:
            String date = irregDateFormatter.format(new GregorianCalendar().getTime());
            fileDate = irregDateFormatter.parse(date);
        } catch (ParseException ex) {
            Logger.getLogger(PictureManagerNew.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_radCurrentDateActionPerformed

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
            java.util.logging.Logger.getLogger(PictureManagerNew.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PictureManagerNew.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PictureManagerNew.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PictureManagerNew.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PictureManagerNew().setVisible(true);
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
    private javax.swing.JButton btnDatePicker;
    private javax.swing.JButton btnDelRow;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnReadScript;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnRemoveAll;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JButton btnUpdateDatabase;
    private javax.swing.JButton btnUpdateTable;
    private javax.swing.JButton btnWrScript;
    private javax.swing.JPanel btnWriteScript;
    private javax.swing.ButtonGroup btngrpDate;
    private javax.swing.ButtonGroup btngrpHoliday;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblFileDate;
    private javax.swing.JLabel lblPic;
    private javax.swing.JList<HolidayPicsNew> lstHolidays;
    private javax.swing.JRadioButton radAnyDate;
    private javax.swing.JRadioButton radCurrentDate;
    private javax.swing.JRadioButton radIrregular;
    private javax.swing.JRadioButton radRegular;
    private javax.swing.JSpinner spnDay;
    private javax.swing.JSpinner spnMonth;
    private javax.swing.JTable tabHolCommand;
    private javax.swing.JTextField txtDate;
    private javax.swing.JTextField txtFileDate;
    private javax.swing.JTextField txtFolderName;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtSelectPicture;
    // End of variables declaration//GEN-END:variables
}
