/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.settings;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author sneha
 */
public class Test {
    
    public static void main(String[] args) {
        GregorianCalendar dt = new GregorianCalendar();
        dt.set(Calendar.DATE, 1);
        dt.getTime();
//        System.out.println(dt.getTime());
        dt.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        dt.add(Calendar.WEEK_OF_MONTH, 2);
        System.out.println(dt.getTime());
    }
    
}
