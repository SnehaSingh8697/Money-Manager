/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.chart.compincmexpchart;

/**
 *
 * @author sneha
 */
public class BarChartData {
    private String date;
    private String walletName;
    private float amount;
    private String mnyr;
    private int pdNum;

    @Override
    public String toString() {
        return "date: "+date+", walletName: "+walletName+", amount: "+amount;
    }

    public BarChartData(String walletName, float amount, String mnyr) {
        this.walletName = walletName;
        this.amount = amount;
        this.mnyr = mnyr;
    }

    
    public BarChartData(String date, String walletName, float amount) {
        this.date = date;
        this.walletName = walletName;
        this.amount = amount;
    }
    
    public BarChartData(int pdNum, String walletName, float amount) {
        this.pdNum = pdNum;
        this.walletName = walletName;
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
    
    

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
    
    
    
}
