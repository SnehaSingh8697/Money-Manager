/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem;

import domesticfinancesystem.calendar.Database;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author sneha
 */

class Field
{
    String name;
    int width;

    public Field(String name, int width) {
        this.name = name;
        this.width = width;
    }

    @Override
    public String toString() {
        return name;
    }
    
    
}

class Report
{
    private static String equalsLine,dashedLine,spacedLine;
    final static int MAXLENGTH = 256;
    public static String buildLine(char ch,int len)
    {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
           sb.append(ch);
        }
        return sb.toString();
    }
    
    static
    {
       equalsLine = buildLine('=', MAXLENGTH);
       dashedLine = buildLine('-', MAXLENGTH);
       spacedLine = buildLine(' ', MAXLENGTH);
    }
    
    public static String getEqualsLine(int len)
    {
        return equalsLine.substring(0, len);
    }
    
    public static String getdashedLine(int len)
    {
        return dashedLine.substring(0, len);
    }
    
    public static String getspacedLine(int len)
    {
        return spacedLine.substring(0, len);
    }
    
    
    public static String cAllign(String str,int width)
    {
        int sp = (width - str.length())/2;
        return getspacedLine(sp)+str+getspacedLine(width - str.length() - sp);
    }
    
    public static String lAllign(String str,int width)
    {
        return str + getspacedLine(width - str.length());
    }
    public static String rAllign(String str,int width)
    {
        return getspacedLine(width - str.length()) + str;
    }

}

public class TestFrame extends javax.swing.JFrame
{

    /**
     * Creates new form TestFrame
     */
    private Database dc;
    private Connection con;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    public TestFrame() {
            initComponents();

            PrintStream out = new PrintStream(System.out);
            dc = new Database("jdbc:oracle:thin:@localhost:1521:XE","dfs","dfsboss","oracle.jdbc.OracleDriver");
            con = dc.createConnection();
            Field fldName,fldLbal,fldDbal;
            fldName = new Field("Name", 30);
            fldLbal = new Field("Liquid Balance", 15);
            fldDbal = new Field("Digital Balance", 15);
            Date fromDt = new Date(2019, 10, 2);
            Date toDt = new Date(2019, 10, 18);
            Date repDate = Calendar.getInstance().getTime();
            
            int repWidth = fldName.width + 1 +fldLbal.width + 1 + fldDbal.width;
            String repTitle = "A Sample Report";
            String repSubTitle = "From "+sdf.format(fromDt)+" to "+sdf.format(toDt);
            
            out.println(Report.cAllign(repTitle, repWidth));
            out.println(Report.cAllign(Report.getEqualsLine(repTitle.length()),repWidth));
            
            out.println(Report.cAllign(repSubTitle, repWidth));
            out.println(Report.cAllign(Report.getdashedLine(repSubTitle.length()),repWidth));
            
            out.printf("%" +repWidth+ "s\n", sdf.format(repDate));
            out.println(Report.getEqualsLine(repWidth));
            out.println( Report.lAllign(fldName.name, fldName.width)
                    + "|"+Report.rAllign(fldLbal.name, fldLbal.width)
                    + "|"+Report.rAllign(fldDbal.name, fldDbal.width)
            );  
            out.println(Report.getEqualsLine(repWidth));
                
        try {
                String sql = "Select * from Wallet";
                Statement stmt = con.createStatement();
                ResultSet rst = stmt.executeQuery(sql);
                while(rst.next())
                {
                    String name = rst.getString("Name");
                    String liquidBal = new String(rst.getInt("LiquidBal")+"");
                    String digitalBal = ""+rst.getInt("DigitalBal");
                    
                    out.println(Report.lAllign(name, fldName.width)
                          + "|"+Report.rAllign(liquidBal, fldLbal.width)
                    + "|"+Report.rAllign(digitalBal, fldDbal.width)
                    +""
            );  
                }
        } catch (SQLException ex) {
            Logger.getLogger(TestFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        out.println(Report.getEqualsLine(repWidth));

        
        
    }
   
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 326, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 244, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
            java.util.logging.Logger.getLogger(TestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TestFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
