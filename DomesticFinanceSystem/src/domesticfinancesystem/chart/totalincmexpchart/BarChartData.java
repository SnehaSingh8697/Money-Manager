/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.chart.totalincmexpchart;

import domesticfinancesystem.chart.compincmexpchart.*;

/**
 *
 * @author sneha
 */
public class BarChartData {
    private String date;
    private String amountType;
    private float amount;
    private String mnyr;
    private int pdNum;

    @Override
    public String toString() {
        return "date: "+date+", walletName: "+amountType+", amount: "+amount;
    }

    public BarChartData(String amountType, float amount, String mnyr) {
        this.amountType = amountType;
        this.amount = amount;
        this.mnyr = mnyr;
    }

    
    public BarChartData(String date, String amountType, float amount) {
        this.date = date;
        this.amountType = amountType;
        this.amount = amount;
    }
    
    public BarChartData(int pdNum, String amountType, float amount) {
        this.pdNum = pdNum;
        this.amountType = amountType;
        this.amount = amount;
    }

    public String getMnyr() {
        return mnyr;
    }

    public void setMnyr(String mnyr) {
        this.mnyr = mnyr;
    }
    

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getPdNum() {
        return pdNum;
    }

    public void setPdNum(int pdNum) {
        this.pdNum = pdNum;
    }
    
    

    public String getAmountType() {
        return amountType;
    }

    public void setAmountType(String amountType) {
        this.amountType = amountType;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
    
    
    
}
