/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.calendar;

/**
 *
 * @author sneha
 */
public class RegHoli {
    
    private int month;
    private int day;
    private int hindex; //array index of holidays

    public RegHoli(int month, int day, int hindex) {
        this.month = month;
        this.day = day;
        this.hindex = hindex;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHindex() {
        return hindex;
    }

    public void setHindex(int hindex) {
        this.hindex = hindex;
    }
    
    
    
    
}
