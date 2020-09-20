/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.settings;

import static domesticfinancesystem.MainFrame.con;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sneha
 */
public class SystemSettings {
    private int openingBankBalance;
    private int openingCashBalance;
    private int refDigitBalance;
    private int refLiqBalance;
    private Date holidatUpdationDate;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");


    public SystemSettings() {
    }
    
    public SystemSettings(int openingBankBalance, int openingCashBalance, int refDigitBalance, int refLiqBalance, Date holidatUpdationDate) {
        try {
            this.openingBankBalance = openingBankBalance;
            this.openingCashBalance = openingCashBalance;
            this.refDigitBalance = refDigitBalance;
            this.refLiqBalance = refLiqBalance;
            this.holidatUpdationDate = holidatUpdationDate;
            String dat = formatter.format(holidatUpdationDate);
            holidatUpdationDate = formatter.parse(dat);
        } catch (ParseException ex) {
            Logger.getLogger(SystemSettings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getOpeningBankBalance() {
        return openingBankBalance;
    }

    public void setOpeningBankBalance(int openingBankBalance) {
        this.openingBankBalance = openingBankBalance;
    }

    public int getOpeningCashBalance() {
        return openingCashBalance;
    }

    public void setOpeningCashBalance(int openingCashBalance) {
        this.openingCashBalance = openingCashBalance;
    }

    public int getRefDigitBalance() {
        return refDigitBalance;
    }

    public void setRefDigitBalance(int refDigitBalance) {
        this.refDigitBalance = refDigitBalance;
    }

    public int getRefLiqBalance() {
        return refLiqBalance;
    }

    public void setRefLiqBalance(int refLiqBalance) {
        this.refLiqBalance = refLiqBalance;
    }

    public Date getHolidatUpdationDate() {
        return holidatUpdationDate;
    }

    public void setHolidatUpdationDate(Date holidatUpdationDate) {
        this.holidatUpdationDate = holidatUpdationDate;
    }
    
    
    public SystemSettings readFromDatabase(Connection con)
    {
        SystemSettings sysSettings = null;
        try {
            String sql = "Select * from SystemSettings";
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(sql);
            if(rst.next())
            {
                openingBankBalance = rst.getInt("openingBankBalance");
                openingCashBalance = rst.getInt("openingCashBalance");
                refDigitBalance = rst.getInt("refDigitBalance");
                refLiqBalance = rst.getInt("refLiqBalance");
                holidatUpdationDate = rst.getDate("holidayUpdationDate");
            }
            rst.close();
            stmt.close();
            sysSettings = new SystemSettings(openingBankBalance, openingCashBalance, refDigitBalance, refLiqBalance, holidatUpdationDate);
        } catch (SQLException ex) {
            Logger.getLogger(SystemSettings.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sysSettings;
    }
    
    
}
