/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.chart.incmpexpcompforwallet;

import domesticfinancesystem.chart.compincmexpchart.*;

/**
 *
 * @author sneha
 */
public class BarChartData {
    private String year;
    private float amount;
    private String month;
    private String dtMonth;

    public BarChartData(String year, float amount,String month) {
        this.month = month;
        this.year = year;
        this.amount = amount;
    }
    
    public BarChartData(String dtMonth, String year, float amount) {
        this.dtMonth = dtMonth;
        this.year = year;
        this.amount = amount;
    }


    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDtMonth() {
        return dtMonth;
    }

    public void setDtMonth(String dtMonth) {
        this.dtMonth = dtMonth;
    }
    
    
    
}
