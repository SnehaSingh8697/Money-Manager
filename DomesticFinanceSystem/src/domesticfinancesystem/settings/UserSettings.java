/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.settings;

import domesticfinancesystem.IntegerDocumentFilter;
import domesticfinancesystem.MainFrame;
import static domesticfinancesystem.MainFrame.con;
import domesticfinancesystem.calendar.Database;
import domesticfinancesystem.settings.HolidayCommandItem;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.AbstractDocument;

/**
 *
 * @author sneha
 */
public class UserSettings extends javax.swing.JDialog {

    /**
     * Creates new form Settings
     */
    public char c;
    public char periodType;
    public int freq1;
    public int freq2;
    public int weekday;
    public int pdIntervalNumber;
    public char pdIntervalType;
    public char startHomeWith;
    public int liquidWarningPercent;
    public int DigitalWarningPercent;
    public int midMonthDate;
    private Database dc;
    private static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private static Connection con;
    private SpinnerNumberModel snmDays;
    private SpinnerNumberModel snmLiqWarnPercent;
    private SpinnerNumberModel snmDigWarnPercent;
    private SpinnerNumberModel snmHours;
    private SpinnerNumberModel snmMinutes;
    private SpinnerNumberModel snmMiddleMonth;
    private SpinnerNumberModel snmMonthDay;
    private SpinnerNumberModel snmMonth;
    private SpinnerNumberModel snmMonthWeek;
    private SpinnerNumberModel snmWeek;
    private ArrayList<Objects> arlCategory = new ArrayList<>();
    private ArrayList<Objects> arlItems = new ArrayList<>();
    private ArrayList<Objects> arlUOM = new ArrayList<>();
    private UserSettings userSettings;
    private static Date fileDate;
    private static HolidayUpdateDialog hud = null;

    public UserSettings(char c, char periodType, int freq1, int freq2, int weekday, int pdIntervalNumber, char pdIntervalType, char startHomeWith, int liquidWarningPercent, int DigitalWarningPercent, int midMonthDate) {
        this.c = c;
        this.periodType = periodType;
        this.freq1 = freq1;
        this.freq2 = freq2;
        this.weekday = weekday;
        this.pdIntervalNumber = pdIntervalNumber;
        this.pdIntervalType = pdIntervalType;
        this.startHomeWith = startHomeWith;
        this.liquidWarningPercent = liquidWarningPercent;
        this.DigitalWarningPercent = DigitalWarningPercent;
        this.midMonthDate = midMonthDate;
    }
    public UserSettings(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setLocationRelativeTo(null);
        dc = MainFrame.dc;
        con = dc.createConnection();
        
        snmDays = new SpinnerNumberModel(1, 1, 31, 1);
        spnDays.setModel(snmDays);
        
        snmLiqWarnPercent = new SpinnerNumberModel(40, 1, 100, 1);
        spnLiqWarnPercent.setModel(snmLiqWarnPercent);
        snmDigWarnPercent = new SpinnerNumberModel(40, 1, 100, 1);
        spnDigWarnPercent.setModel(snmDigWarnPercent);
        snmHours = new SpinnerNumberModel(1, 1, 24, 1);
        snmMinutes = new SpinnerNumberModel(1, 1, 60, 1);
        spnPdReminder.setModel(snmMinutes);
        snmMiddleMonth = new SpinnerNumberModel(15, 2, 27, 1);
        spnMiddleMonth.setModel(snmMiddleMonth);
        
        snmMonthDay = new SpinnerNumberModel(1, 1, 30, 1);
        spnMonthDays.setModel(snmMonthDay);
        setTag(lblDayType, 1);

        snmMonth = new SpinnerNumberModel(1, 1, 12, 1);
        spnMonth1.setModel(snmMonth);
        setTag(lblMonthType, 1);
        
        snmMonthWeek = new SpinnerNumberModel(1, 1, 4, 1);
        spnWeek1.setModel(snmMonthWeek);
        setTag(lblWeekType, 1);
        
        snmWeek = new SpinnerNumberModel(1, 1, 4, 1);
        spnWeek.setModel(snmWeek);
        setTag(lblWeekdayType, 1);

        
        userSettings = databaseToMemory('C', con);
        fetchFromMemory(userSettings);
        
        radCategory.setSelected(true);
        
        radCategory.setSelected(true);
        enableDisableComboboxes();
    }
    
    public UserSettings()
    {
        
    }

    public char getC() {
        return c;
    }

    public void setC(char c) {
        this.c = c;
    }

    public char getPeriodType() {
        return periodType;
    }

    public void setPeriodType(char periodType) {
        this.periodType = periodType;
    }

    public int getFreq1() {
        return freq1;
    }

    public void setFreq1(int freq1) {
        this.freq1 = freq1;
    }

    public int getFreq2() {
        return freq2;
    }

    public void setFreq2(int freq2) {
        this.freq2 = freq2;
    }

    public int getWeekday() {
        return weekday;
    }

    public void setWeekday(int weekday) {
        this.weekday = weekday;
    }

    public int getPdIntervalNumber() {
        return pdIntervalNumber;
    }

    public void setPdIntervalNumber(int pdIntervalNumber) {
        this.pdIntervalNumber = pdIntervalNumber;
    }

    public char getPdIntervalType() {
        return pdIntervalType;
    }

    public void setPdIntervalType(char pdIntervalType) {
        this.pdIntervalType = pdIntervalType;
    }

    public char getStartHomeWith() {
        return startHomeWith;
    }

    public void setStartHomeWith(char startHomeWith) {
        this.startHomeWith = startHomeWith;
    }

    public int getLiquidWarningPercent() {
        return liquidWarningPercent;
    }

    public void setLiquidWarningPercent(int liquidWarningPercent) {
        this.liquidWarningPercent = liquidWarningPercent;
    }

    public int getDigitalWarningPercent() {
        return DigitalWarningPercent;
    }

    public void setDigitalWarningPercent(int DigitalWarningPercent) {
        this.DigitalWarningPercent = DigitalWarningPercent;
    }

    public int getMidMonthDate() {
        return midMonthDate;
    }

    public void setMidMonthDate(int midMonthDate) {
        this.midMonthDate = midMonthDate;
    }
    
    private void fetchFromMemory(UserSettings settings)
    {
        
        //setting warning meters
        spnLiqWarnPercent.setValue((int)settings.getLiquidWarningPercent());
        spnDigWarnPercent.setValue((int)settings.getDigitalWarningPercent());
        
        //Interval for periodic deposit reminder
        char pdIntervalType = settings.getPdIntervalType();
        if(pdIntervalType == 'H')
        {
            cmbPdReminder.setSelectedItem("Hours");
            spnPdReminder.setModel(snmHours);
        }
        else
        {
            cmbPdReminder.setSelectedItem("Minutes");
            spnPdReminder.setModel(snmMinutes);
        }
        spnPdReminder.setValue((int)settings.getPdIntervalNumber());
        
        //setting mid day of month
        spnMiddleMonth.setValue(settings.getMidMonthDate());
        
        //setting view preference
        char startHomeWith = settings.getStartHomeWith();
        if(startHomeWith == 'C')
            radCalendar.setSelected(true);
        else
            radExternalTransaction.setSelected(true);
        
        //duration of each period
        char periodType = settings.getPeriodType();
        int freq1 = settings.getFreq1();
        int freq2 = settings.getFreq2();
        if(periodType == 'W')
        {
            spnWeek1.setValue(freq1);
            spnMonth1.setValue(freq2);
            radMonthWeek.setSelected(true);
        }
        else if(periodType == 'D')
        {
           snmMonthDay.setValue(freq1);
           radMonthDate.setSelected(true);
        }
        else if(periodType == 'T')
        {
            spnWeek.setValue(freq1);
            cmbDays.setSelectedIndex(settings.getWeekday() - 1);
            radMonthDay.setSelected(true);
        }
        else
        {
            spnDays.setValue((int)freq1);
            radDays.setSelected(true);
        }
        
        enableDisableComponenets(periodType);
        
        //Fill Category Combobox
        readData("Select * from Category", arlCategory,cmbCategory);
        //Fill ListItem Combobox
        readData("Select * from ListItem", arlItems,cmbItems);
        //Fill UOM Combobox
        readData("Select * from UOM", arlUOM,cmbUOM);
    }
    private void enableDisableComboboxes()
    {
        cmbCategory.setEnabled(false);
        cmbItems.setEnabled(false);
        cmbUOM.setEnabled(false);
        if(radCategory.isSelected())
             cmbCategory.setEnabled(true);
        else if(radItem.isSelected())
             cmbItems.setEnabled(true);
        else
            cmbUOM.setEnabled(true);

    }
     //----------------------------------updating holidays code-----------------------------
     
    private static ArrayList importHCIsFromZip(ZipFile zipFile) throws IOException
    {
        ArrayList<HolidayCommandItem> arlHCIs = new ArrayList<>();
        ZipEntry ze = null ;
        ze = zipFile.getEntry("PicInfo.dat");
        InputStream in = zipFile.getInputStream(ze) ;
      
        arlHCIs = readAllHcis(in);
        
        return arlHCIs;
        
    }
   private static Date unpackDate(int packedDate)
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

   private static HolidayCommandItem readHci(DataInputStream dis) throws IOException
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
 
    
    private static ArrayList<HolidayCommandItem> readAllHcis(InputStream in) throws IOException
    {
        ArrayList<HolidayCommandItem> arlHCIs = new ArrayList<>();
        DataInputStream dis = new DataInputStream(in);
        
        short size = dis.readShort();
        short packedDate = dis.readShort();
        fileDate = unpackDate(packedDate);
        System.out.println("file date: "+fileDate);
        System.out.println("sysdate date: "+MainFrame.sysSettings.getHolidatUpdationDate());
        int val = MainFrame.sysSettings.getHolidatUpdationDate().compareTo(fileDate);
        System.out.println("val: "+val);
        if( val<0 )
        {
            short i = 0;
            while(i<size)
            {
                HolidayCommandItem hci = readHci(dis);
                arlHCIs.add(hci);
                i++;
            }
            return arlHCIs;
        }
        else
            return null;
    }
    
    private static  BufferedImage imageToBufferedImage(Image img)
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
    
    private static void addHciToDatabase(HolidayCommandItem hci,Connection con,ZipFile zipfile) throws SQLException,IOException
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
                Statement stmt;
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
    
    private static void deleteHciFromDatabase(HolidayCommandItem hci,Connection con)
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
            Logger.getLogger(UserSettings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
  private static void updateHciInDatabase(HolidayCommandItem hci,Connection con,ZipFile zipfile)
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
            Logger.getLogger(UserSettings.class.getName()).log(Level.SEVERE, null, ex);
        }
      
  }
    
 private static void removeAllHolidays()
    {
         try {
            // TODO add your handling code here:
            con.setAutoCommit(false);
            
            Statement stmt = con.createStatement();
            String sql = "Delete from RegularHoliday";
            stmt.executeQuery(sql);
            sql = "Delete from IrregularHoliday";
            stmt.executeQuery(sql);
            sql = "Delete from HolidayPic";
            stmt.executeQuery(sql);
            
            stmt.close();
            
            con.commit();
            con.setAutoCommit(true);

            }
        catch (SQLException ex) {
            Logger.getLogger(UserSettings.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }   
  private static void executeHci(HolidayCommandItem hci,Connection con,ZipFile zipfile)
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
                Logger.getLogger(UserSettings.class.getName()).log(Level.SEVERE, null, ex);
         }
    }  
    
    
    
    public  static void readZipFile(ZipFile zf)
    {
        try {
            ArrayList<HolidayCommandItem> arlHCIs = importHCIsFromZip(zf);
            if(arlHCIs == null)
            {
               JOptionPane.showMessageDialog(null, "Holidays already updated", "Message", JOptionPane.INFORMATION_MESSAGE);

            }
            else
            {
                    for (HolidayCommandItem arlHci : arlHCIs) {
                        executeHci(arlHci, con, zf);
                    }
            
             java.sql.Date sdt = new java.sql.Date(fileDate.getTime());
             String sql = "Update Systemsettings set holidayupdationdate = ?";
             PreparedStatement pstmt = con.prepareStatement(sql);
             pstmt.setDate(1, sdt);
             pstmt.executeUpdate();
             MainFrame.sysSettings.setHolidatUpdationDate(fileDate);
             MainFrame.initialize();
            JOptionPane.showMessageDialog(null, "Holidays updated successfully", "Message", JOptionPane.INFORMATION_MESSAGE);

            }
        } catch (IOException | SQLException ex) {
            Logger.getLogger(UserSettings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
   //----------------------------end of code------------------------------ 
    
   private void setTag(JLabel lbl,int n)
   {
       String s = "";
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
        lbl.setText(s);
   }
    
    
    private void readData(String sql,ArrayList<Objects> arl,JComboBox cmb)
    {
        try {
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            while(rst.next())
            {
                int id = rst.getInt("Id");
                String name = rst.getString("Name");
                Objects obj = new Objects(id, name);
                arl.add(obj);
                cmb.addItem(name);
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserSettings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public UserSettings databaseToMemory(char ch,Connection con)
    {
        UserSettings settings = null;
         try {
           
            String sql = "Select * from UserSettings where dc = ? ";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, String.valueOf(ch));
            ResultSet rst = pstmt.executeQuery();
            if(rst.next())
            {
                c = rst.getString("Dc").charAt(0);
                periodType = rst.getString("PeriodType").charAt(0);
                freq1 = rst.getInt("Freq1");
                freq2 = rst.getInt("Freq2");
                weekday = rst.getInt("weekday");
                pdIntervalNumber = rst.getInt("pdIntervalNumber");
                pdIntervalType = rst.getString("pdIntervalType").charAt(0);
                startHomeWith = rst.getString("startHomeWith").charAt(0);
                liquidWarningPercent = rst.getInt("liquidWarningPercent");
                DigitalWarningPercent = rst.getInt("DigitalWarningPercent");
                midMonthDate = rst.getInt("midMonthDate");
                
            }
            settings = new UserSettings(c,periodType, freq1, freq2, weekday, pdIntervalNumber, pdIntervalType, startHomeWith, liquidWarningPercent, DigitalWarningPercent, midMonthDate);
                    
            rst.close();
            pstmt.close();
            
      
        } catch (SQLException ex) {
            Logger.getLogger(UserSettings.class.getName()).log(Level.SEVERE, null, ex);
        }
            return settings;
        
    }
    public void saveToMemory(UserSettings s)
    {
                c = s.getC();
                periodType = s.getPeriodType();
                freq1 = s.getFreq1();
                freq2 = s.getFreq2();
                weekday = s.getWeekday();
                pdIntervalNumber = s.getPdIntervalNumber();
                pdIntervalType = s.getPdIntervalType();
                startHomeWith = s.getStartHomeWith();
                liquidWarningPercent = s.getLiquidWarningPercent();
                DigitalWarningPercent = s.getDigitalWarningPercent();
                midMonthDate = s.getMidMonthDate();
    }
    public UserSettings saveToMemory()
    {
        UserSettings us = new UserSettings();
        int liquidWarningPercent = (int)spnLiqWarnPercent.getValue();
        us.setLiquidWarningPercent(liquidWarningPercent);
        int digitalWarningPercent = (int)spnDigWarnPercent.getValue();
        us.setDigitalWarningPercent(digitalWarningPercent);
        if(radCalendar.isSelected())
            us.setStartHomeWith('C');
        else
            us.setStartHomeWith('X');
        us.setMidMonthDate((int)spnMiddleMonth.getValue());
        if(cmbPdReminder.getSelectedItem().equals("Hours"))
            us.setPdIntervalType('H');
        else
            us.setPdIntervalType('M');
        us.setPdIntervalNumber((int)spnPdReminder.getValue());
        
        //setting pd interval
        if(radMonthWeek.isSelected())
        {
            us.setPeriodType('W');
            us.setFreq1((int)spnWeek1.getValue());
            us.setFreq2((int)spnMonth1.getValue());
            us.setWeekday(-1);
        }
        else if(radMonthDate.isSelected())
        {
            us.setPeriodType('D');
            us.setFreq1((int)spnMonthDays.getValue());
            us.setFreq2(-1);
            us.setWeekday(-1);
        }
        else if(radMonthDay.isSelected())
        {
            us.setPeriodType('T');
            us.setFreq1((int)spnWeek.getValue());
            us.setFreq2(-1);
            us.setWeekday(cmbDays.getSelectedIndex()+1);
        }
        else
        {
            us.setPeriodType('N');
            us.setFreq1((int)spnDays.getValue());
            us.setFreq2(-1);
            us.setWeekday(-1);
        }
        return us;
    }
    
    public void memoryToDatabase(UserSettings s,char c,Connection con)
    {
        try {
            con.setAutoCommit(false);

            String sql = "Update UserSettings set dc = ?";
            sql+= ",PeriodType = ?,Freq1 = ?,Freq2 = ?,Weekday = ?,PdIntervalNumber = ?,PdIntervalType = ?,StartHomeWith = ?,LIQUIDWARNINGPERCENT = ?,DIGITALWARNINGPERCENT = ?,MIDMONTHDATE = ? where DC = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, ""+c);
            pstmt.setString(2, ""+s.getPeriodType());
            pstmt.setInt(3, s.getFreq1());
            pstmt.setInt(4, s.getFreq2());
            pstmt.setInt(5, s.getWeekday());
            pstmt.setInt(6, s.getPdIntervalNumber());
            pstmt.setString(7, ""+s.getPdIntervalType());
            pstmt.setString(8, ""+s.getStartHomeWith());
            pstmt.setString(9, ""+s.getLiquidWarningPercent());
            pstmt.setString(10, ""+s.getDigitalWarningPercent());
            pstmt.setInt(11, s.getMidMonthDate());
            pstmt.setString(12, ""+c);
            pstmt.executeUpdate();
            pstmt.close();
            
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException ex) {
            Logger.getLogger(UserSettings.class.getName()).log(Level.SEVERE, null, ex);
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

        radgrpPd = new javax.swing.ButtonGroup();
        radgrpView = new javax.swing.ButtonGroup();
        radItems = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        radMonthWeek = new javax.swing.JRadioButton();
        radMonthDate = new javax.swing.JRadioButton();
        radMonthDay = new javax.swing.JRadioButton();
        radDays = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cmbDays = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        spnDays = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        spnMonthDays = new javax.swing.JSpinner();
        spnWeek1 = new javax.swing.JSpinner();
        spnMonth1 = new javax.swing.JSpinner();
        spnWeek = new javax.swing.JSpinner();
        lblWeekType = new javax.swing.JLabel();
        lblDayType = new javax.swing.JLabel();
        lblWeekdayType = new javax.swing.JLabel();
        lblMonthType = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        spnLiqWarnPercent = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        spnDigWarnPercent = new javax.swing.JSpinner();
        jPanel3 = new javax.swing.JPanel();
        cmbPdReminder = new javax.swing.JComboBox<>();
        spnPdReminder = new javax.swing.JSpinner();
        jLabel12 = new javax.swing.JLabel();
        spnMiddleMonth = new javax.swing.JSpinner();
        jPanel4 = new javax.swing.JPanel();
        radCategory = new javax.swing.JRadioButton();
        cmbCategory = new javax.swing.JComboBox<>();
        radItem = new javax.swing.JRadioButton();
        cmbItems = new javax.swing.JComboBox<>();
        jRadioButton1 = new javax.swing.JRadioButton();
        cmbUOM = new javax.swing.JComboBox<>();
        btnDelete = new javax.swing.JButton();
        btnOK = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        radCalendar = new javax.swing.JRadioButton();
        radExternalTransaction = new javax.swing.JRadioButton();
        btnSetAsDefault = new javax.swing.JButton();
        btnRestoreDefault = new javax.swing.JButton();
        btnRestoreFactory = new javax.swing.JButton();
        btnUpdateHolidays = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("User Settings");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Duration of each period", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 51, 204))); // NOI18N

        radgrpPd.add(radMonthWeek);
        radMonthWeek.setText("jRadioButton1");
        radMonthWeek.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radMonthWeekActionPerformed(evt);
            }
        });

        radgrpPd.add(radMonthDate);
        radMonthDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radMonthDateActionPerformed(evt);
            }
        });

        radgrpPd.add(radMonthDay);
        radMonthDay.setText("jRadioButton1");
        radMonthDay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radMonthDayActionPerformed(evt);
            }
        });

        radgrpPd.add(radDays);
        radDays.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radDaysActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel1.setText("week of");

        jLabel2.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel2.setText("month");

        jLabel3.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel3.setText("On every");

        jLabel4.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel4.setText("of month");

        cmbDays.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" }));

        jLabel5.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel5.setText("of month");

        jLabel6.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel6.setText("After");

        jLabel7.setText("days");

        spnMonthDays.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnMonthDaysStateChanged(evt);
            }
        });

        spnWeek1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnWeek1StateChanged(evt);
            }
        });

        spnMonth1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnMonth1StateChanged(evt);
            }
        });

        spnWeek.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnWeekStateChanged(evt);
            }
        });

        lblWeekType.setOpaque(true);

        lblDayType.setOpaque(true);

        lblWeekdayType.setOpaque(true);

        lblMonthType.setOpaque(true);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(radMonthWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(radMonthDate, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addComponent(radDays, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addComponent(radMonthDay, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(spnDays, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(spnWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblWeekdayType, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cmbDays, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(spnWeek1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblWeekType, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnMonth1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblMonthType, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnMonthDays, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(lblDayType, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lblWeekType, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(radMonthWeek)
                            .addComponent(spnWeek1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spnMonth1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(lblMonthType, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblDayType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(spnMonthDays, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(5, 5, 5))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbDays, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spnWeek, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(radMonthDay)
                            .addComponent(lblWeekdayType, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(5, 5, 5))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(radMonthDate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(spnDays, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(radDays)))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Warning Percent Meters", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 51, 204))); // NOI18N

        jLabel10.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 102, 0));
        jLabel10.setText("Liquid");

        jLabel11.setFont(new java.awt.Font("Garamond", 1, 18)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 0, 102));
        jLabel11.setText("Digital");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spnLiqWarnPercent, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnDigWarnPercent, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(47, 47, 47))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnLiqWarnPercent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnDigWarnPercent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Interval for periodic deposit reminder", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 51, 153))); // NOI18N

        cmbPdReminder.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Hours", "Minutes" }));
        cmbPdReminder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbPdReminderActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addComponent(cmbPdReminder, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addComponent(spnPdReminder, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(56, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbPdReminder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnPdReminder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel12.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel12.setText("Middle Date for month");

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Delete", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 51, 204))); // NOI18N

        radItems.add(radCategory);
        radCategory.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radCategory.setText("Category");
        radCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radCategoryActionPerformed(evt);
            }
        });

        radItems.add(radItem);
        radItem.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radItem.setText("Item");
        radItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radItemActionPerformed(evt);
            }
        });

        radItems.add(jRadioButton1);
        jRadioButton1.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jRadioButton1.setText("Unit of Measurement");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/delete.gif"))); // NOI18N
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
                    .addComponent(radCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radItem, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButton1))
                .addGap(51, 51, 51)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbItems, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cmbUOM, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(btnDelete))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 81, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbItems, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(radItem, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbUOM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(btnDelete))
                .addGap(20, 20, 20))
        );

        btnOK.setFont(new java.awt.Font("Garamond", 1, 20)); // NOI18N
        btnOK.setForeground(new java.awt.Color(153, 0, 0));
        btnOK.setText("OK");
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOKActionPerformed(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        jLabel13.setText("View");

        radgrpView.add(radCalendar);
        radCalendar.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radCalendar.setText("Calendar");

        radgrpView.add(radExternalTransaction);
        radExternalTransaction.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radExternalTransaction.setText("External Transaction");

        btnSetAsDefault.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnSetAsDefault.setText("Set as Defaults");
        btnSetAsDefault.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetAsDefaultActionPerformed(evt);
            }
        });

        btnRestoreDefault.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnRestoreDefault.setText("Restore Defaults");
        btnRestoreDefault.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRestoreDefaultActionPerformed(evt);
            }
        });

        btnRestoreFactory.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnRestoreFactory.setText("Restore Factory Setting");
        btnRestoreFactory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRestoreFactoryActionPerformed(evt);
            }
        });

        btnUpdateHolidays.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnUpdateHolidays.setText("Update Holidays...");
        btnUpdateHolidays.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateHolidaysActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(16, 16, 16)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(spnMiddleMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(radCalendar)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(radExternalTransaction))))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(9, 9, 9)
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnSetAsDefault, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnRestoreDefault)
                                .addGap(18, 18, 18)
                                .addComponent(btnRestoreFactory)
                                .addGap(18, 18, 18)
                                .addComponent(btnUpdateHolidays))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(273, 273, 273)
                        .addComponent(btnOK, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSetAsDefault)
                    .addComponent(btnRestoreDefault)
                    .addComponent(btnRestoreFactory)
                    .addComponent(btnUpdateHolidays))
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spnMiddleMonth))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(radCalendar)
                            .addComponent(radExternalTransaction)))
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnOK)
                .addGap(20, 20, 20))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbPdReminderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPdReminderActionPerformed
        // TODO add your handling code here:
        String val = (String)cmbPdReminder.getSelectedItem();
        if(val.equals("Hours"))
            spnPdReminder.setModel(snmHours);
        else
           spnPdReminder.setModel(snmMinutes); 
    }//GEN-LAST:event_cmbPdReminderActionPerformed
   
    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
        // TODO add your handling code here:
        userSettings = saveToMemory();
        memoryToDatabase(userSettings, 'C', con);
        MainFrame.userSettings = saveToMemory();
        
    }//GEN-LAST:event_btnOKActionPerformed

    private void btnSetAsDefaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetAsDefaultActionPerformed
        // TODO add your handling code here:
          int type = JOptionPane.showConfirmDialog(this,"Are you sure you want to set as default settings", "Message",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
          if(type == JOptionPane.YES_OPTION)
          {
                userSettings = saveToMemory();
                memoryToDatabase(userSettings, 'C', con);
                MainFrame.userSettings = saveToMemory();
                memoryToDatabase(userSettings, 'D', con);
          }

    }//GEN-LAST:event_btnSetAsDefaultActionPerformed

    private void btnRestoreDefaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRestoreDefaultActionPerformed
        // TODO add your handling code here:
          int type = JOptionPane.showConfirmDialog(this,"Are you sure you want to restore default settings", "Message",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
          if(type == JOptionPane.YES_OPTION)
          {
                userSettings = databaseToMemory('D', con);
                fetchFromMemory(userSettings);  
          }    
    }//GEN-LAST:event_btnRestoreDefaultActionPerformed

    private void btnRestoreFactoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRestoreFactoryActionPerformed
        // TODO add your handling code here:
        
        userSettings = databaseToMemory('F', con);
        fetchFromMemory(userSettings);
    }//GEN-LAST:event_btnRestoreFactoryActionPerformed

    private void radMonthDayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radMonthDayActionPerformed
        // TODO add your handling code here:
                enableDisableComponenets('T');

    }//GEN-LAST:event_radMonthDayActionPerformed
    private boolean deleteItem(int id) throws SQLException
    {
        System.out.println("id = "+id);

        String sql = null;
        PreparedStatement pstmt;
        String str;
        if(radCategory.isSelected())
        {
            sql = "Select count(id) from ShoppingList where CatId = ?";
            str = "Category";
        }
        else if(radItem.isSelected())
        {
            sql = "Select count(SlNo) from SListDetail where ItemId = ?";
            str = "ListItem";
        }
        else
        {
            sql = "Select count(SlNo) from SListDetail where UOMId = ?";
            str = "UOM";

        }
        pstmt =  con.prepareStatement(sql);
        pstmt.setInt(1,id);
        ResultSet rst = pstmt.executeQuery();
        int count = 0;
        if(rst.next())
        {
            count = rst.getInt(1);
        }
        System.out.println("count = "+count);
        rst.close();
        pstmt.close();
        boolean val;
        if(count>0)
        {
            JOptionPane.showMessageDialog(this, "Delete not possible", "Message", JOptionPane.ERROR_MESSAGE);
            val = false;
        }
        else
        {
            sql = "Delete from "+str+" where Id = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1,id);
            pstmt.executeUpdate();
            val = true;
        }
        return val;
    }
    private void deleteObject(String item,ArrayList<Objects> arlObjects)
    {
        try {
        con.setAutoCommit(false);
        int id = -1;
            for (Objects arlObject : arlObjects) {
                if(item.equals(arlObject.getName()))
                {
                    id = arlObject.getId();
                    break;
                }
            }
            boolean val  = false;
            if(id!=-1)
                val = deleteItem(id);
            
            if(val == true)
            {
                if(radCategory.isSelected())
                {
                    cmbCategory.removeItem(item);
                }
                else if(radItem.isSelected())
                {
                    cmbItems.removeItem(item);
                }
                else
                {
                    cmbUOM.removeItem(item);
                }
                
                for (Objects arlObject : arlObjects) 
                {
                        if(item.equals(arlObject.getName()))
                        {
                            arlObjects.remove(arlObject);
                            break;
                        }
               }
                repaint();
            }
            con.commit();
            con.setAutoCommit(true);
        } 
        catch (SQLException ex) {
               Logger.getLogger(UserSettings.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    private void enableDisableComponenets(char c)
    {
        boolean val = false;
        spnWeek1.setEnabled(val);
        spnMonth1.setEnabled(val);
        spnMonthDays.setEnabled(val);
        spnWeek.setEnabled(val);
        cmbDays.setEnabled(val);
        spnDays.setEnabled(val);
        
        if(c == 'W')
        {
            spnWeek1.setEnabled(!val);
            spnMonth1.setEnabled(!val);
        }
        else if(c == 'D')
        {
           spnMonthDays.setEnabled(!val);
        }
        else if(c == 'T')
        {
           spnWeek.setEnabled(!val);
           cmbDays.setEnabled(!val);
        }
        else
            spnDays.setEnabled(!val);
    }
    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
        if(radCategory.isSelected())
            deleteObject((String)cmbCategory.getSelectedItem(), arlCategory);
        else if(radItem.isSelected())
            deleteObject((String)cmbItems.getSelectedItem(), arlItems);
        else
           deleteObject((String)cmbUOM.getSelectedItem(), arlUOM);


    }//GEN-LAST:event_btnDeleteActionPerformed

    private void radMonthWeekActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radMonthWeekActionPerformed
        // TODO add your handling code here:
        enableDisableComponenets('W');
    }//GEN-LAST:event_radMonthWeekActionPerformed

    private void radMonthDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radMonthDateActionPerformed
        // TODO add your handling code here:
        enableDisableComponenets('D');
    }//GEN-LAST:event_radMonthDateActionPerformed

    private void radDaysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radDaysActionPerformed
        // TODO add your handling code here:
       enableDisableComponenets('N');
    }//GEN-LAST:event_radDaysActionPerformed

    private void radCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radCategoryActionPerformed
        // TODO add your handling code here:
        enableDisableComboboxes();
    }//GEN-LAST:event_radCategoryActionPerformed

    private void radItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radItemActionPerformed
        // TODO add your handling code here:
       enableDisableComboboxes();

    }//GEN-LAST:event_radItemActionPerformed

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        // TODO add your handling code here:
       enableDisableComboboxes();
       

    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void btnUpdateHolidaysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateHolidaysActionPerformed
        // TODO add your handling code here:
        hud = new HolidayUpdateDialog(MainFrame.frame, true);
        hud.setVisible(true);
        hud.dispose();
        
    }//GEN-LAST:event_btnUpdateHolidaysActionPerformed

    private void spnWeek1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnWeek1StateChanged
        // TODO add your handling code here:
        int val = (int)spnWeek1.getValue();
        setTag(lblWeekType, val);
    }//GEN-LAST:event_spnWeek1StateChanged

    private void spnMonth1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnMonth1StateChanged
        // TODO add your handling code here:
         int val = (int)spnMonth1.getValue();
         setTag(lblMonthType, val);
    }//GEN-LAST:event_spnMonth1StateChanged

    private void spnMonthDaysStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnMonthDaysStateChanged
        // TODO add your handling code here:
         int val = (int)spnMonthDays.getValue();
        setTag(lblDayType, val);
    }//GEN-LAST:event_spnMonthDaysStateChanged

    private void spnWeekStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnWeekStateChanged
        // TODO add your handling code here:
         int val = (int)spnWeek.getValue();
        setTag(lblWeekdayType, val);
    }//GEN-LAST:event_spnWeekStateChanged

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
            java.util.logging.Logger.getLogger(UserSettings.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UserSettings.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UserSettings.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UserSettings.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UserSettings dialog = new UserSettings(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnOK;
    private javax.swing.JButton btnRestoreDefault;
    private javax.swing.JButton btnRestoreFactory;
    private javax.swing.JButton btnSetAsDefault;
    private javax.swing.JButton btnUpdateHolidays;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JComboBox<String> cmbDays;
    private javax.swing.JComboBox<String> cmbItems;
    private javax.swing.JComboBox<String> cmbPdReminder;
    private javax.swing.JComboBox<String> cmbUOM;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JLabel lblDayType;
    private javax.swing.JLabel lblMonthType;
    private javax.swing.JLabel lblWeekType;
    private javax.swing.JLabel lblWeekdayType;
    private javax.swing.JRadioButton radCalendar;
    private javax.swing.JRadioButton radCategory;
    private javax.swing.JRadioButton radDays;
    private javax.swing.JRadioButton radExternalTransaction;
    private javax.swing.JRadioButton radItem;
    private javax.swing.ButtonGroup radItems;
    private javax.swing.JRadioButton radMonthDate;
    private javax.swing.JRadioButton radMonthDay;
    private javax.swing.JRadioButton radMonthWeek;
    private javax.swing.ButtonGroup radgrpPd;
    private javax.swing.ButtonGroup radgrpView;
    private javax.swing.JSpinner spnDays;
    private javax.swing.JSpinner spnDigWarnPercent;
    private javax.swing.JSpinner spnLiqWarnPercent;
    private javax.swing.JSpinner spnMiddleMonth;
    private javax.swing.JSpinner spnMonth1;
    private javax.swing.JSpinner spnMonthDays;
    private javax.swing.JSpinner spnPdReminder;
    private javax.swing.JSpinner spnWeek;
    private javax.swing.JSpinner spnWeek1;
    // End of variables declaration//GEN-END:variables
}
