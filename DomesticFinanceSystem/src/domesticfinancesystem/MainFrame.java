/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem;

import com.sun.scenario.Settings;
import domesticfinancesystem.calendar.CalendarPanel;
import domesticfinancesystem.calendar.Database;
import domesticfinancesystem.calendar.Holiday;
import domesticfinancesystem.calendar.RegHoli;
import domesticfinancesystem.calendar.moonphase.MoonPhaseForm;
import domesticfinancesystem.chart.compincmexpchart.ComparativeIncomeExpenseChart;
import domesticfinancesystem.chart.incmpexpcompforwallet.IncomeExpenseChartForWallet;
import domesticfinancesystem.chart.piechart.PieChart;
import domesticfinancesystem.chart.totalincmexpchart.TotalIncomeExpenseChart;
import domesticfinancesystem.exttrans.ExternalTransactionPanel;
import domesticfinancesystem.exttrans.ExternalTransactionPanelNew;
import domesticfinancesystem.periodicdeposit.PeriodicDeposit;
import domesticfinancesystem.regularservice.RegServDetails;
import domesticfinancesystem.regularservice.RegularServiceMainPanel;
import domesticfinancesystem.regularservice.RegularServicePanel;
import domesticfinancesystem.regularservice.ServiceDefinitionPanel;
import domesticfinancesystem.settings.SystemSettings;
import domesticfinancesystem.settings.UserSettings;
import domesticfinancesystem.shoppinglist.ShoppingListMainPanel;
import domesticfinancesystem.wallet.NewMyWalletPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.Border;
import jdk.nashorn.internal.objects.NativeRegExp;
import oracle.jdbc.driver.DatabaseError;

/**
 *
 * @author sneha
 */
public class MainFrame extends javax.swing.JFrame {

    public static MainFrame frame;
    
    /**
     * Creates new form MainFrame
     */
    public static Holiday[] hdays;
    public static RegHoli[] rh;
    public static HashMap<Integer,Integer> holiIdToIndex;
    public static Database dc;
    public static Connection con;
    private CalendarPanel cp;
    private static final int moonLabelWidth = 21;
    private static final int moonLabelHeight = 21;
    public static Image[] moonImages;
    private ActionListener menuListner;
    private String currentPanelName = null;
    private JPanel scenePanel = null;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private static Timer regServTimer;
    private static Timer pdTimer;
    public static UserSettings userSettings;
    public static SystemSettings sysSettings;
    public static boolean periodicDepositMade = false;
    public static PeriodicDepositDialog pdDepositDialog = null;
    public static int currentLiqBalance;
    public static int currentDigitBalance;
    private final Color liqColor = new Color(34,139,34) ;
    private final Color digColor = Color.BLUE ;
        
    private final Color liqWarnColor = new Color(236, 34, 151);
    private final Color digWarnColor = new Color(217, 116, 55);
    
    private Border brdButton =  javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 153, 0), 2);
    private JButton currentSelectedButton;
    
    public void getCurrenetLiqAndDigitBal()
    {
        try {
            String sql = "Select sum(Liquidbal) from Wallet";
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            if(rst.next())
            {
                currentLiqBalance = rst.getInt(1);
            }
            rst.close();
            stmt.close();
            
            sql = "Select sum(Digitalbal) from Wallet";
            stmt = con.createStatement();
            rst = stmt.executeQuery(sql);
            if(rst.next())
            {
                currentDigitBalance = rst.getInt(1);
            }
            rst.close();
            stmt.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private void showBalanceMeters()
    {
        double liqbalpercent = (double)((MainFrame.currentLiqBalance * 100) / MainFrame.sysSettings.getRefLiqBalance());
        double digbalpercent = (double)((MainFrame.currentDigitBalance * 100) / MainFrame.sysSettings.getRefDigitBalance());
        updateBalanceMeters(digbalpercent, liqbalpercent);
    }
    
    public boolean isToMakePeriodicDeposit(Date dt,Connection con)
    {
        Date date = getLastPeriodicDepositDate();
        if(date == null)
        {
            System.out.println("pd doesnt exist");
            return true;
        }
        else
        {
            try {
                Date dat = getNextPeriodicDepositDate(date);
                String strdat = formatter.format(dat);
                dat = formatter.parse(strdat);
                
                System.out.println("next periodic deposit date: "+dat);
                
                Date currentDate = new GregorianCalendar().getTime();
                String strcurrentDate = formatter.format(currentDate);
                currentDate = formatter.parse(strcurrentDate);
                
                System.out.println("current date: "+currentDate);

                if(currentDate.compareTo(dat) >= 0)
                {
                    System.out.println("current date match");
                    return true;
                }
                else
                {
                    System.out.println("current date dont match");
                    return false;
                }
            } catch (ParseException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }
    private Date nthWeekOfMthMonth(Date ldt,int m,int n)
    {
        
        GregorianCalendar lgdt = new GregorianCalendar();
        lgdt.setTime(ldt);
        System.out.println("m = "+m);
        System.out.println("n = "+n);
        GregorianCalendar ndt = new GregorianCalendar();
        ndt.setTime(ldt);
        ndt.add(Calendar.MONTH,m);
        ndt.set(Calendar.DATE, (n-1)*7 + 1);
        ndt.getTime();
        ndt.set(Calendar.DAY_OF_WEEK, lgdt.get(Calendar.DAY_OF_WEEK));
        return ndt.getTime();
    }
    private Date nthDayOfNextMonth(Date ldt,int n)
    {
        GregorianCalendar ndt = new GregorianCalendar();
        ndt.setTime(ldt);
        ndt.add(Calendar.MONTH, 1);
        ndt.set(Calendar.DATE, n);
        return ndt.getTime();
        
    }
    private Date nthDayOfWeekOfNextMonth(Date ldt,int n,int weekday)
    {
        GregorianCalendar ndt = new GregorianCalendar();
        ndt.setTime(ldt);
        ndt.add(Calendar.MONTH, 1);
        ndt.set(Calendar.WEEK_OF_MONTH, n);
        ndt.set(Calendar.DAY_OF_WEEK, weekday);
        return ndt.getTime();
    }
    private Date nDaysAfter(Date ldt,int n)
    {
        GregorianCalendar ndt = new GregorianCalendar();
        ndt.setTime(ldt);
        ndt.add(Calendar.DATE,n);
        return ndt.getTime();
    }
    private Date getNextPeriodicDepositDate(Date dt)
    {
        char periodType = MainFrame.userSettings.getPeriodType();
        Date date;
        System.out.println("pd char "+periodType);
        if(periodType == 'W')
        {
            int m = MainFrame.userSettings.getFreq2();
            int n = MainFrame.userSettings.getFreq1();
            date = nthWeekOfMthMonth(dt, m, n);
        }
        else if(periodType == 'D')
        {
            int n = MainFrame.userSettings.getFreq1();
            date = nthDayOfNextMonth(dt,n);
        }
        else if(periodType == 'T')
        {
            int n = MainFrame.userSettings.getFreq1();
            int weekday = MainFrame.userSettings.getWeekday();
            date = nthDayOfWeekOfNextMonth(dt, n, weekday);
        }
        else
        {
           int n = MainFrame.userSettings.getFreq1();
           date = nDaysAfter(dt, n);
        }
        return date;
    }
    private Date getLastPeriodicDepositDate()
    {
        Date date = null;
        try {
                String sql = "Select max(dt) from pd";
                Statement stmt = con.createStatement();
                ResultSet rst = stmt.executeQuery(sql);
                if(rst.next())
                {
                    date = rst.getDate(1);
                }
        } catch (SQLException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        return date;
    }
    
    private void setOpeningBalances()
    {
           
            sysSettings = sysSettings.readFromDatabase(con);
            if(sysSettings.getOpeningCashBalance() == 0 || sysSettings.getOpeningBankBalance() == 0)
            {
                OpeningBalanceDialog dlg = new OpeningBalanceDialog(this, true,sysSettings.getOpeningCashBalance(),sysSettings.getOpeningBankBalance(),con);//pass reference of parent frame later
                dlg.setVisible(true);
                dlg.dispose();
            }
    }
    public static Image[] fillMoonImages()
    {
        File imageFolder = new File("New Moon Images");
        File[] imageFiles = imageFolder.listFiles();
        Arrays.sort(imageFiles, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) 
            {
                return f1.getName().compareTo(f2.getName()) ;
            }
        });
        int len = imageFiles.length;
        System.out.println("len: "+len);
        Image[] moonImages = new Image[len-1];
        for(int i = 0;i<len-1;i++) {
           try {
               File file = imageFiles[i];
               moonImages[i] = scaleImage(ImageIO.read(file),moonLabelHeight,moonLabelWidth);
           } catch (IOException ex) {
               Logger.getLogger(MoonPhaseForm.class.getName()).log(Level.SEVERE, null, ex);
           }
        }
        return moonImages;
    }
    
    private void setChart()
    {
        String[] chartNames = {"Pie Chart","Total Income Expense Chart","Comparative Income Expense Chart","Income Expense Chart For Wallet","Stacked Income/Expense Report"};
        for (int i = 0; i < chartNames.length; i++) {
           cmbCharts.addItem(chartNames[i]);
        }
        Dimension sz = cmbCharts.getPreferredSize();
        sz.width*=2;
        cmbCharts.setPreferredSize(sz);
        cmbCharts.setMaximumSize(sz);
        cmbCharts.setMinimumSize(sz);

    }
    
    public static Image scaleImage(Image img,int labelHeight,int labelWidth)
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
    public static void initialize()
    {
        try {
            //setting moon images to array
            moonImages = fillMoonImages();
           
            holiIdToIndex = new HashMap();
            String sql = "Select count(*) from HolidayPic";
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            int size = 0;
            if(rst.next())
            {
                size = rst.getInt(1);
            }
            rst.close();
            hdays = new Holiday[size];
            sql = "Select Id,Name,Pic from HolidayPic";
            rst = stmt.executeQuery(sql);
            int index = 0;
            while(rst.next())
            {
                int id = rst.getInt(1);
                String name = rst.getString(2);
                Blob blob = rst.getBlob(3);
                Image img = null;
                if(blob !=null)
                {
                   InputStream in = blob.getBinaryStream();
                   img = ImageIO.read(in);
                }
                hdays[index] = new Holiday(name, img);
                holiIdToIndex.put(id, index);
                index++;
            }
            rst.close();
            
            sql = "Select count(*) from RegularHoliday";
            rst = stmt.executeQuery(sql);
            if(rst.next())
            {
                size = rst.getInt(1);
            }
            rst.close();
            rh = new RegHoli[size];
            index = 0;
            sql = "Select * from RegularHoliday";
            rst = stmt.executeQuery(sql);
            while(rst.next())
            {
                int day = rst.getInt("Day");
                int month = rst.getInt("Month");
                int hpicid = rst.getInt("HPicId");
                int hindex = (int)holiIdToIndex.get(hpicid);  // array index
                rh[index] = new RegHoli(month, day, hindex);
                index++;
            }
            rst.close();
            stmt.close();
            
            
        } catch (SQLException | IOException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
     //         ---------------code for automatically adding service-------------
    public static int getIdFromDual()
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
            Logger.getLogger(RegularServiceMainPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }
    
    private void makeAutoRegServEntry(int servDefId,Date startDate,Date dt)
    {
        try {
            String sql = "Select * from ServDefDetail where ServDefId = ?";
            PreparedStatement pstmt =  con.prepareStatement(sql);
            pstmt.setInt(1,servDefId);
            ResultSet rst = pstmt.executeQuery();
            
            boolean found = false;
            
            ArrayList<RegServDetails> arlRegServDetails = new ArrayList<>();
            
            while(rst.next())
            {
                int n = rst.getInt("Frequency");
                if(RegularServiceMainPanel.extractFreqData(startDate, n,dt))
                {
                    int itemId = rst.getInt("Id");
                    float price = rst.getFloat("Price");
                    float qty = rst.getFloat("DefaultQty");
                    
                    RegServDetails rsd = new RegServDetails(itemId, price, qty);
                    arlRegServDetails.add(rsd);
                    
                    found = true;
                }
            }
            
            rst.close();
            pstmt.close();
            
            if(found)
            {
                java.sql.Date sdt = new java.sql.Date(dt.getTime());
                
                Date time = Calendar.getInstance().getTime();
                
                int noOfSeconds = time.getHours()*3600 + time.getMinutes() * 60;
                
                java.sql.Date sTime = new java.sql.Date(time.getTime());
                
                int sId = getIdFromDual();
                int count = 0;

                
                sql = "Insert into RegServ(Id,ServDefId,Dttm,Paid,Ontime,NoOfSeconds,AutoAddYN,DeletedYN) ";
                sql+="values(?,?,?,?,?,?,?,?)";
                
                pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, sId);
                pstmt.setInt(2, servDefId);
                pstmt.setDate(3, sdt);
                pstmt.setString(4,"N");
                pstmt.setDate(5,sTime);
                pstmt.setInt(6,noOfSeconds); 
                pstmt.setString(7,"Y"); 
                pstmt.setString(8,"N"); 
                pstmt.executeUpdate();
                
                pstmt.close();
                
                
                for (RegServDetails rsd : arlRegServDetails) {
                    
                    String s = "Insert into RegServDetails(RegServId,SlNo,ServItemId,Price,Qty) values(?,?,?,?,?)";
                     
                    PreparedStatement pst = con.prepareStatement(s);
                    pst.setInt(1, sId);
                    pst.setInt(2, ++count);
                    pst.setInt(3,rsd.getItemid());
                    pst.setFloat(4,rsd.getPrice());
                    pst.setFloat(5,rsd.getQty());
                    pst.executeUpdate();
                   
                    pst.close();
                }
                
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(RegularServicePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    private void addRegServFor(Date ndt,int ntime)
    {
        try {
            java.sql.Date sndt = new java.sql.Date(ndt.getTime());
            
            String sql = "Select Id,StrtDate,OnNoOfSeconds from ServDef where AutoAddYN = \'Y\' and StrtDate <= ? and OnNoOfSeconds <= ?";
            PreparedStatement pstmt =  con.prepareStatement(sql);
            pstmt.setDate(1,sndt);
            pstmt.setInt(2,ntime);
            ResultSet rst = pstmt.executeQuery();
            
            while(rst.next())
            {
                int id = rst.getInt(1);
                Date startdate = rst.getDate(2);
                int noOfSeconds = rst.getInt(3);
                
                String s = "Select * from RegServ rs , Servdef sd where rs.ServDefId = sd.Id and sd.Id = ? and rs.Dttm = ?";
                PreparedStatement pst =  con.prepareStatement(s);
                pst.setInt(1,id);
                pst.setDate(2,sndt);
                ResultSet rs = pst.executeQuery();
                
                if(rs.next() == false)
                {
                    makeAutoRegServEntry(id, startdate, ndt);
                }
                rs.close();
                pst.close();
            }
            rst.close();
            pstmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(RegularServiceMainPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void autoAddService()
    {
          try {
        con.setAutoCommit(false);
        GregorianCalendar gc = new GregorianCalendar();
        Date ndt = gc.getTime();
        int ntime = ndt.getHours() * 3600 + ndt.getMinutes() * 60;
        String date = formatter.format(ndt);
            try {
                ndt = formatter.parse(date);
            } catch (ParseException ex) {
                Logger.getLogger(RegularServiceMainPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        addRegServFor(ndt, ntime);
        con.commit();
        con.setAutoCommit(true);
         } catch (SQLException ex) {
                    Logger.getLogger(ServiceDefinitionPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
          
    }
    //--------------end of code---------------
    
    
    
    public MainFrame() {
        initComponents();
        
         dc = new Database("jdbc:oracle:thin:@localhost:1521:XE","dfs","dfsboss","oracle.jdbc.OracleDriver");
         con = dc.createConnection();
         
        initialize();
    
        userSettings = new UserSettings();
        userSettings = userSettings.databaseToMemory('C', con);
        
//        setChart();
        menuListner = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton btn = (JButton) e.getSource();
                String panelName = btn.getActionCommand();
                executePanel(panelName, (JButton) e.getSource());
        }
        };
        btnCalendar.addActionListener(menuListner);
        btnExtTrans.addActionListener(menuListner);
        btnShoppingList.addActionListener(menuListner);
        btnRegularServices.addActionListener(menuListner);
        btnWallet.addActionListener(menuListner);
        btnShowChart.addActionListener(menuListner);
        btnPeriodicDeposit.addActionListener(menuListner);
        btnSettings.addActionListener(menuListner);
        
        //depending on setting for exttrans or calendar , set the button
        if(userSettings.getStartHomeWith() == 'C')
           executePanel(btnCalendar.getActionCommand(),btnCalendar);
        else
          executePanel(btnExtTrans.getActionCommand(),btnExtTrans);

        this.pack();
        setLocationRelativeTo(null);
        
        deletePreviouslyDeletedRecs(); //delete all the records with DeleyedYN  from RegServ and RegServDetails table for which dt < maxDt
        
        autoAddPrevRecs();//call autoAddService starting from MaxDt of regServ till yesterday
        
        sysSettings = new SystemSettings();
        sysSettings.readFromDatabase(con);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                super.windowOpened(e); //To change body of generated methods, choose Tools | Templates.
                setOpeningBalances();

            }
            
        });
        regServTimer = new Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                autoAddService();
            }
        });
        startRegServTimer();
        
        if(periodicDepositMade == false)
        {
            boolean val = isToMakePeriodicDeposit(new GregorianCalendar().getTime(), con);
            if(val == false)
                periodicDepositMade = true;
            else
            {
//                showPdDepositDialog();
                int secs;
                int d = userSettings.getPdIntervalNumber();
                if(userSettings.getPdIntervalType() == 'H')
                    secs = d * 3600;
                else
                   secs = d * 60;
                secs = secs * 1000;
                System.out.println("secs: "+secs);
                pdTimer = new Timer(secs, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(periodicDepositMade == false)
                        {
                           showPdDepositDialog();
                        }
                    }
               });
               pdTimer.start();
            }
        }
        getCurrenetLiqAndDigitBal();
        showBalanceMeters();
    }
    
    private void showPdDepositDialog()
    {
        periodicDepositMade = true;
        pdDepositDialog = new PeriodicDepositDialog(frame, true);
        pdDepositDialog.setVisible(true);
        pdDepositDialog.dispose();
    }
    
    private void autoAddPrevRecs()
    {
        try {
            String sql = "Select Max(Dttm) from RegServ where DeletedYN = 'N'";
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            Date maxDt = null;
            if(rst.next())
            {
                maxDt = rst.getDate(1);
            }
            rst.close();
            int ntime = 11 * 3600 + 59 * 60;
            Date currDate = new GregorianCalendar().getTime();
            Date yesterDate = RegularServiceMainPanel.addsubtractDate(currDate, 1, false);
            
            if(maxDt!=null)
            {
                con.setAutoCommit(false);
                while(maxDt.compareTo(yesterDate) <= 0)
                {
                    addRegServFor(maxDt, ntime);
                    maxDt = RegularServiceMainPanel.addsubtractDate(maxDt, 1, true);
                }
                con.commit();
                con.setAutoCommit(true);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(RegularServiceMainPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void deletePreviouslyDeletedRecs()
    {
        try {
             
            con.setAutoCommit(false);

            String sql = "Select s.Id, Max(r.Dttm) from ServDef s,RegServ r where r.ServDefId = s.Id group by s.Id order by s.Id asc";
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            while(rst.next())
            {
                int id = rst.getInt(1);
                Date maxDt = rst.getDate(2);
                java.sql.Date sMaxDt = new java.sql.Date(maxDt.getTime());
                
                //Finding Records in RegServ which is to be deleted (DeletedYN == 'Y') and the date is less than the maxDt
                String s = "Select Id from RegServ where ServDefId = ? and Dttm < ? and DeletedYN = ?";
                PreparedStatement pstmt = con.prepareStatement(s);
                pstmt.setInt(1, id);
                pstmt.setDate(2, sMaxDt);
                pstmt.setString(3, "Y");
                ResultSet rs = pstmt.executeQuery();
                while(rs.next())
                {
                    int regServId = rs.getInt(1);
                    String str = "Delete from RegServDetails where RegServId = "+regServId;
                    Statement sts = con.createStatement();
                    sts.executeQuery(str);
                    sts.close();
                    
                    str = "Delete from RegServ where Id = "+regServId;
                    sts = con.createStatement();
                    sts.executeQuery(str);
                    sts.close();
                }
                rs.close();
            }
            rst.close();
            stmt.close();
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException ex) {
            Logger.getLogger(RegularServiceMainPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    public static void startRegServTimer()
    {
        regServTimer.start();
    }
    public static void stopRegServTimer()
    {
        regServTimer.stop();
    }
    
    public void updateBalanceMeters(double dgtBalPercent,double liqBalPercent)
    {
        
        if(dgtBalPercent>=0)
        {
            pgbDigBalance.setForeground(dgtBalPercent<=userSettings.getDigitalWarningPercent()?digWarnColor:digColor);
            pgbDigBalance.setValue((int)dgtBalPercent);
        }
        if(liqBalPercent>=0)
        {
         pgbLiqBalance.setForeground(liqBalPercent<=userSettings.getLiquidWarningPercent()?liqWarnColor:liqColor);
         pgbLiqBalance.setValue((int)liqBalPercent);

        }
    }
   
    private void executePanel(String panelName, JButton btn)
    {
        if(panelName.equals(currentPanelName))
            return;
        
         if(currentSelectedButton!=null)
                currentSelectedButton.setBorder(null);
         currentSelectedButton = btn;
         currentSelectedButton.setBorder(brdButton);
         
        if(panelName.equals("PeriodicDeposit"))
        {
            PeriodicDeposit dialog = new PeriodicDeposit(this, true);
            dialog.setVisible(true);
            
        }
        else if(panelName.equals("UserSettings"))
        {
            UserSettings usysDialog = new UserSettings(this, true);
            usysDialog.setVisible(true);
            
        }
        else
        {
        
            if(scenePanel!=null)
            {
              centralPanel.remove(scenePanel);
                scenePanel = null;
            }

            switch(panelName)
            {
                case "CalendarPanel" :
                    scenePanel = (CalendarPanel)new CalendarPanel();
                    currentPanelName = panelName;
                    lblPanelName.setText("Calendar");
                    break;
                case "ExternalTransactionPanelNew" :
                   scenePanel = (ExternalTransactionPanelNew)new ExternalTransactionPanelNew();
                   currentPanelName = panelName;
                   lblPanelName.setText("External Transaction");
                   break;
                case "ShoppingListMainPanel" :
                   scenePanel = (ShoppingListMainPanel)new ShoppingListMainPanel();
                   currentPanelName = panelName;
                   lblPanelName.setText("Shopping List");
                   break;
                case "RegularServiceMainPanel" :
                   stopRegServTimer();
                   scenePanel = (RegularServiceMainPanel)new RegularServiceMainPanel();
                   currentPanelName = panelName;
                   lblPanelName.setText("Regular Service");
                   break;
                case "NewMyWalletPanel" :
                   scenePanel = (NewMyWalletPanel)new NewMyWalletPanel();
                   currentPanelName = panelName;
                   lblPanelName.setText("Wallet Manager");
                   break;
    //            case "PeriodicDeposit" :
    //                PeriodicDeposit dialog = new PeriodicDeposit(this, true);
    //                dialog.setVisible(true);
    //               break;
    //             case "UserSettings" :
    //                UserSettings usysDialog = new UserSettings(this, true);
    //                usysDialog.setVisible(true);
    //               break;

                case "ChartPanel":
                   String chartName = (String)cmbCharts.getSelectedItem();

                       switch(chartName)
                       {
                           case "Pie Chart":
                           scenePanel = (PieChart)new PieChart();
                           currentPanelName = "PieChart";
                           lblPanelName.setText("Pie Chart");
                           break;    

                           case "Total Income Expense Chart":
                           scenePanel = (TotalIncomeExpenseChart)new TotalIncomeExpenseChart();
                           currentPanelName = "TotalIncomeExpenseChart";
                           lblPanelName.setText("Total Income Expense Chart");
                           break;

                           case "Comparative Income Expense Chart":
                           scenePanel = (ComparativeIncomeExpenseChart)new ComparativeIncomeExpenseChart(1);
                           currentPanelName = "ComparativeIncomeExpenseChart1";
                           lblPanelName.setText("Comparative Income Expense Chart");
                           break;


                           case "Income/ Expense Chart For Wallet":
                           scenePanel = (IncomeExpenseChartForWallet)new IncomeExpenseChartForWallet();
                           currentPanelName = "IncomeExpenseChartForWallet";
                           lblPanelName.setText("Income/ Expense Chart For Wallet");
                           break;

                           case "Stacked Income/Expense Report":
                           scenePanel = (ComparativeIncomeExpenseChart)new ComparativeIncomeExpenseChart(2);
                           currentPanelName = "ComparativeIncomeExpenseChart2";
                           lblPanelName.setText("Stacked Income/Expense Report");
                           break;

                   }
                   break; 
            }
            centralPanel.add(scenePanel);
            centralPanel.validate();
            centralPanel.repaint();
            
           
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

        jSplitPane1 = new javax.swing.JSplitPane();
        centralPanel = new javax.swing.JPanel();
        ComboHolderPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        pgbLiqBalance = new javax.swing.JProgressBar();
        pgbDigBalance = new javax.swing.JProgressBar();
        toolBarOptions = new javax.swing.JToolBar();
        btnCalendar = new javax.swing.JButton();
        btnExtTrans = new javax.swing.JButton();
        btnShoppingList = new javax.swing.JButton();
        btnRegularServices = new javax.swing.JButton();
        btnWallet = new javax.swing.JButton();
        btnPeriodicDeposit = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jPanel2 = new javax.swing.JPanel();
        cmbCharts = new javax.swing.JComboBox<>();
        btnShowChart = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        btnTransTransReport = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        btnSettings = new javax.swing.JButton();
        lblPanelName = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("DomesticFinance System");

        centralPanel.setLayout(new java.awt.BorderLayout());

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/liqbal.png"))); // NOI18N

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/dig.jpg"))); // NOI18N
        jLabel2.setText("D");

        pgbLiqBalance.setFocusable(false);
        pgbLiqBalance.setStringPainted(true);

        pgbDigBalance.setStringPainted(true);

        javax.swing.GroupLayout ComboHolderPanelLayout = new javax.swing.GroupLayout(ComboHolderPanel);
        ComboHolderPanel.setLayout(ComboHolderPanelLayout);
        ComboHolderPanelLayout.setHorizontalGroup(
            ComboHolderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ComboHolderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(pgbLiqBalance, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(pgbDigBalance, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        ComboHolderPanelLayout.setVerticalGroup(
            ComboHolderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ComboHolderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ComboHolderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pgbLiqBalance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pgbDigBalance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        toolBarOptions.setOpaque(false);

        btnCalendar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/calendar.png"))); // NOI18N
        btnCalendar.setToolTipText("Calendar");
        btnCalendar.setActionCommand("CalendarPanel");
        btnCalendar.setBorder(null);
        btnCalendar.setFocusable(false);
        btnCalendar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCalendar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarOptions.add(btnCalendar);

        btnExtTrans.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/money.png"))); // NOI18N
        btnExtTrans.setToolTipText("External Transaction");
        btnExtTrans.setActionCommand("ExternalTransactionPanelNew");
        btnExtTrans.setFocusable(false);
        btnExtTrans.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExtTrans.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarOptions.add(btnExtTrans);

        btnShoppingList.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/list.png"))); // NOI18N
        btnShoppingList.setToolTipText("Shopping List");
        btnShoppingList.setActionCommand("ShoppingListMainPanel");
        btnShoppingList.setFocusable(false);
        btnShoppingList.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnShoppingList.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnShoppingList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShoppingListActionPerformed(evt);
            }
        });
        toolBarOptions.add(btnShoppingList);

        btnRegularServices.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/shopping_cart.png"))); // NOI18N
        btnRegularServices.setToolTipText("Regular Services");
        btnRegularServices.setActionCommand("RegularServiceMainPanel");
        btnRegularServices.setFocusable(false);
        btnRegularServices.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRegularServices.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarOptions.add(btnRegularServices);

        btnWallet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/wallet.png"))); // NOI18N
        btnWallet.setToolTipText("Wallet");
        btnWallet.setActionCommand("NewMyWalletPanel");
        btnWallet.setFocusable(false);
        btnWallet.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnWallet.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnWallet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWalletActionPerformed(evt);
            }
        });
        toolBarOptions.add(btnWallet);
        btnWallet.getAccessibleContext().setAccessibleName("Wallets");

        btnPeriodicDeposit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/chronometer.png"))); // NOI18N
        btnPeriodicDeposit.setToolTipText("Periodic Deposit");
        btnPeriodicDeposit.setActionCommand("PeriodicDeposit");
        btnPeriodicDeposit.setFocusable(false);
        btnPeriodicDeposit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnPeriodicDeposit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarOptions.add(btnPeriodicDeposit);
        toolBarOptions.add(jSeparator2);

        jPanel2.setLayout(new java.awt.BorderLayout());

        cmbCharts.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pie Chart", "Total Income Expense Chart", "Comparative Income Expense Chart", "Income/ Expense Chart For Wallet", "Stacked Income/Expense Report" }));
        cmbCharts.setMaximumSize(new java.awt.Dimension(150, 30));
        cmbCharts.setMinimumSize(new java.awt.Dimension(200, 30));
        cmbCharts.setPreferredSize(new java.awt.Dimension(40, 20));
        cmbCharts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbChartsActionPerformed(evt);
            }
        });
        jPanel2.add(cmbCharts, java.awt.BorderLayout.PAGE_START);

        toolBarOptions.add(jPanel2);

        btnShowChart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/pie_chart.png"))); // NOI18N
        btnShowChart.setToolTipText("Show Chart");
        btnShowChart.setActionCommand("ChartPanel");
        btnShowChart.setFocusable(false);
        btnShowChart.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnShowChart.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnShowChart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowChartActionPerformed(evt);
            }
        });
        toolBarOptions.add(btnShowChart);
        toolBarOptions.add(jSeparator3);

        btnTransTransReport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/report.png"))); // NOI18N
        btnTransTransReport.setToolTipText("Transaction Report");
        btnTransTransReport.setFocusable(false);
        btnTransTransReport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnTransTransReport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnTransTransReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTransTransReportActionPerformed(evt);
            }
        });
        toolBarOptions.add(btnTransTransReport);
        toolBarOptions.add(jSeparator1);

        btnSettings.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/sett.png"))); // NOI18N
        btnSettings.setToolTipText("Settings");
        btnSettings.setActionCommand("UserSettings");
        btnSettings.setFocusable(false);
        btnSettings.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSettings.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarOptions.add(btnSettings);

        lblPanelName.setFont(new java.awt.Font("Garamond", 1, 20)); // NOI18N
        lblPanelName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(centralPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 1387, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(toolBarOptions, javax.swing.GroupLayout.PREFERRED_SIZE, 599, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblPanelName, javax.swing.GroupLayout.PREFERRED_SIZE, 398, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ComboHolderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(toolBarOptions, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPanelName, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ComboHolderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(centralPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 618, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnShoppingListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShoppingListActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnShoppingListActionPerformed

    private void btnTransTransReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTransTransReportActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnTransTransReportActionPerformed

    private void btnWalletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWalletActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnWalletActionPerformed

    private void cmbChartsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbChartsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbChartsActionPerformed

    private void btnShowChartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowChartActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnShowChartActionPerformed

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
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                frame =  new MainFrame();
                frame.add(new CalendarPanel(Calendar.getInstance().getTime()));
                frame.pack();
                frame.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ComboHolderPanel;
    private javax.swing.JButton btnCalendar;
    private javax.swing.JButton btnExtTrans;
    private javax.swing.JButton btnPeriodicDeposit;
    private javax.swing.JButton btnRegularServices;
    private javax.swing.JButton btnSettings;
    private javax.swing.JButton btnShoppingList;
    private javax.swing.JButton btnShowChart;
    private javax.swing.JButton btnTransTransReport;
    private javax.swing.JButton btnWallet;
    private javax.swing.JPanel centralPanel;
    private javax.swing.JComboBox<String> cmbCharts;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel lblPanelName;
    private javax.swing.JProgressBar pgbDigBalance;
    private javax.swing.JProgressBar pgbLiqBalance;
    private javax.swing.JToolBar toolBarOptions;
    // End of variables declaration//GEN-END:variables
}
