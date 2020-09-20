/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.reports;

import domesticfinancesystem.MainFrame;
import domesticfinancesystem.calendar.Database;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.datatype.DataTypes;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalImageAlignment;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.exception.DRException;

/**
 *
 * @author sneha
 */
public class TestReport 
{
    
       private static Database dc;
       private static Connection con;
        private static void userReport(Connection con) 
        {                                         
        String sql = "select Name,Digitalbal,Liquidbal from Wallet order by 1" ;

        JasperReportBuilder report = DynamicReports.report() ;

        StyleBuilder columnTitleStyle = stl.style().bold().setFontSize(12) ;
        report.setColumnTitleStyle(columnTitleStyle) ;

        // ============
        //                                                  Label,   DB Field, Type
        TextColumnBuilder<String> nameColumn = col.column("Name", "Name", DataTypes.stringType()) ;
//        TextColumnBuilder<Date> dobColumn = col.column("Date of Birth", "dob", DataTypes.dateType()) ;
        TextColumnBuilder<Integer> digBalColumn = col.column("Digital Balance", "Digitalbal", DataTypes.integerType()) ;
        // =============
        
//        dobColumn.setPattern("dd/MM/yyyy") ;
//            dobColumn.setTitleStyle(stl.style(columnTitleStyle).setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)) ;
//        dobColumn.setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)) ;


//            idColumn.setWidth(5) ;
//            dateColumn.setWidth(10) ;
//            nameColumn.setFixedWidth(40) ;
//            digBalColumn.setFixedWidth(10) ;

//            report.ignorePageWidth() ;

//            report.title(Templates.createTitleComponent(mainFieldLabel + " check list")) ;
        StyleBuilder titleStyle = stl.style().setName("titleStyle").bold().setFontSize(16) ;
//            report.setTitleStyle(stl.style(titleStyle)) ;
//            report.title(Components.text("Song Track List")
//                    .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER).setStyle(titleStyle)) ;

/*            
        report.title(
                cmp.horizontalList().
                    add(cmp.image(DynamicReportTest.class.getResourceAsStream("/pband/samples/resources/images/BagShopLogo.png")).setStyle(stl.style().setHorizontalImageAlignment(HorizontalImageAlignment.CENTER)))
                    .newRow()
                    .add(
                        Components.text("Song Track List").setHorizontalTextAlignment(HorizontalTextAlignment.CENTER).setStyle(titleStyle),
                        Components.text("Date : " + strToday).setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT).setStyle(stl.style(titleStyle).setFontSize(14)))
                    .newRow()
                    .add(Components.text("As on today").setHorizontalTextAlignment(HorizontalTextAlignment.CENTER))
                    .newRow()
                    .add(cmp.filler().setStyle(stl.style().setTopBorder(stl.pen2Point())).setFixedHeight(10))
                );
*/

//        report.title(cmp.image(MainFrame.class.getResourceAsStream("/sourav/ssms/resources/images/BagShopLogo.png")).setStyle(stl.style().setHorizontalImageAlignment(HorizontalImageAlignment.CENTER)),
//                     cmp.horizontalList()
//                        .add(Components.text("User List").setHorizontalTextAlignment(HorizontalTextAlignment.CENTER).setStyle(titleStyle)) 
////                            .add(Components.text("Date : " + strToday).setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT).setStyle(stl.style(titleStyle).setFontSize(14)).setWidth(25))
//                     ) ;





//            trackColumn.setWidth(200) ;
//        report.columns(loginColumn, levelColumn, nameColumn, sexColumn, dobColumn, contactColumn) ;
        report.columns(nameColumn, digBalColumn) ;

//            report.setShowColumnTitle(false) ;

        report.pageFooter(Components.pageXofY()) ;


            report.setDataSource(sql, con) ;
        try
        {
            report.show(false) ;
        }
        catch (DRException ex)
        {
            JOptionPane.showMessageDialog(null, ex, "Report problem", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }                                        

    
    public static void main(String[] args) 
    { 
        dc = new Database("jdbc:oracle:thin:@localhost:1521:XE","dfs","dfsboss","oracle.jdbc.OracleDriver");
        con = dc.createConnection(); 
        userReport(con);
    }
    
    public static void someReport() {
        
        
        
        FileInputStream fis = null ;
            try
            {
                String sql = "select first_name, Email, Hire_date " +
                        "from Employees " ;
                JasperReportBuilder report = DynamicReports.report() ;
                StyleBuilder columnTitleStyle = stl.style().bold().setFontSize(12) ;
                report.setColumnTitleStyle(columnTitleStyle) ;
                // ============
                //                                                  Label,   DB Field, Type
                TextColumnBuilder<String> NameColumn= col.column("Name", "first_name", DataTypes.stringType()) ;
                TextColumnBuilder<String> EmailColumn = col.column("Email", "email", DataTypes.stringType()) ;
                TextColumnBuilder<Date> dhColumn = col.column("Hire Date", "Hire_Date", DataTypes.dateType()) ;
                // =============
                dhColumn.setPattern("dd/MM/yyyy") ;
                //            dobColumn.setTitleStyle(stl.style(columnTitleStyle).setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)) ;
                dhColumn.setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)) ;
                //            idColumn.setWidth(5) ;
    //            dateColumn.setWidth(10) ;
    //            nameColumn.setFixedWidth(40) ;

    //            report.ignorePageWidth() ;

    //            report.title(Templates.createTitleComponent(mainFieldLabel + " check list")) ;
    StyleBuilder titleStyle = stl.style().setName("titleStyle").bold().setFontSize(16) ;
    //            report.setTitleStyle(stl.style(titleStyle)) ;
    //            report.title(Components.text("Song Track List")
    //                    .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER).setStyle(titleStyle)) ;
    /*
                report.title(
                cmp.horizontalList().
                add(cmp.image(DynamicReportTest.class.getResourceAsStream("/pband/samples/resources/images/BagShopLogo.png")).setStyle(stl.style().setHorizontalImageAlignment(HorizontalImageAlignment.CENTER)))
                .newRow()
                .add(
                Components.text("Song Track List").setHorizontalTextAlignment(HorizontalTextAlignment.CENTER).setStyle(titleStyle),
                Components.text("Date : " + strToday).setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT).setStyle(stl.style(titleStyle).setFontSize(14)))
                .newRow()
                .add(Components.text("As on today").setHorizontalTextAlignment(HorizontalTextAlignment.CENTER))
                .newRow()
                .add(cmp.filler().setStyle(stl.style().setTopBorder(stl.pen2Point())).setFixedHeight(10))
                );
                 */
                fis = new FileInputStream(new File("C:\\Users\\sneha\\Desktop\\car.png"));
                report.title(cmp.image(fis).setStyle(stl.style().setHorizontalImageAlignment(HorizontalImageAlignment.CENTER)),
                        cmp.horizontalList()
                                .add(Components.text("User List").setHorizontalTextAlignment(HorizontalTextAlignment.CENTER).setStyle(titleStyle)) 
    //                            .add(Components.text("Date : " + strToday).setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT).setStyle(stl.style(titleStyle).setFontSize(14)).setWidth(25))
                ) ;
                //            trackColumn.setWidth(200) ;
                report.columns(NameColumn, EmailColumn, dhColumn) ;
                //            report.setShowColumnTitle(false) ;

                report.pageFooter(Components.pageXofY()) ;
                Database db = new Database("jdbc:oracle:thin:@localhost:1521:XE","hr","hr","oracle.jdbc.OracleDriver");
                try
                    (
                        Connection con = db.createConnection() ;
                        )
                {
                    report.setDataSource(sql, con) ;
                    report.show(true) ;
                }
                catch(SQLException ex)
                {
                    JOptionPane.showMessageDialog(null, ex, "Database Connection Problem", JOptionPane.ERROR_MESSAGE);
                }
                catch (DRException ex)
                {
                    JOptionPane.showMessageDialog(null, ex, "Report problem", JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            catch(FileNotFoundException ex)
            {
                Logger.getLogger(TestReport.class.getName()).log(Level.SEVERE,null, ex);
            } finally {
                try {
                    fis.close();
                } catch (IOException ex) {
                    Logger.getLogger(TestReport.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
    }
}
