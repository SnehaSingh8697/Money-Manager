package domesticfinancesystem.calendar;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sneha
 */
public class Database
{
    private String dburl;
    private String drivername;
    private String user;
    private String passwd;
     private        Connection  con = null;

    
    public Database(String url,String usr,String password,String drivname)
    {
        dburl = url;
        user = usr;
        passwd = password;
        drivername = drivname;
        
        
        try
        {
//            System.out.println("Drivername: "+drivername);
            Class.forName(drivername);
        } 
        catch (ClassNotFoundException ex)
        {
        }
    }
    
    public Connection createConnection()
    {
        try
        {
            if(con == null || con.isClosed())
            {
               con = DriverManager.getConnection(dburl,user,passwd);
                System.out.println("con created ");
           }
            else
                System.out.println("connection maintained");
        }
        catch (SQLException ex)
        {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return con;
    }
    
    public Connection createConnectionOLD()
    {
         Connection  con = null;
        
        try
        {
           con = DriverManager.getConnection(dburl,user,passwd);
        } 
        catch (SQLException ex)
        {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return con;
    }
}
