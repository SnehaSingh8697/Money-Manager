/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.inttrans;

/**
 *
 * @author sneha
 */
public class IntTransData {
    private int sourceWalletId;
    private int targetWalletId;
    private int sourceWalletOldDgtAmt;
    private int sourceWalletOldLiqAmt;
    private int targetWalletOldDgtAmt;
    private int targetWalletOldLiqAmt;
    private int transDgtAmt;
    private int transLiqAmt;
    private boolean isLiq;
    private String sourceWalletName;

    public IntTransData(int sourceWalletId, int targetWalletId, int sourceWalletOldDgtAmt, int sourceWalletOldLiqAmt, int targetWalletOldDgtAmt, int targetWalletOldLiqAmt, int transDgtAmt, int transLiqAmt,boolean isLiq,String sourceWalletName) {
        this.sourceWalletId = sourceWalletId;
        this.targetWalletId = targetWalletId;
        this.sourceWalletOldDgtAmt = sourceWalletOldDgtAmt;
        this.sourceWalletOldLiqAmt = sourceWalletOldLiqAmt;
        this.targetWalletOldDgtAmt = targetWalletOldDgtAmt;
        this.targetWalletOldLiqAmt = targetWalletOldLiqAmt;
        this.transDgtAmt = transDgtAmt;
        this.transLiqAmt = transLiqAmt;
        this.isLiq = isLiq;
        this.sourceWalletName = sourceWalletName;
    }

    public String getSourceWalletName() {
        return sourceWalletName;
    }

    public void setSourceWalletName(String sourceWalletName) {
        this.sourceWalletName = sourceWalletName;
    }
    
    

    public boolean isIsLiq() {
        return isLiq;
    }

    public void setIsLiq(boolean isLiq) {
        this.isLiq = isLiq;
    }
    
    

    public int getSourceWalletId() {
        return sourceWalletId;
    }

    public void setSourceWalletId(int sourceWalletId) {
        this.sourceWalletId = sourceWalletId;
    }

    public int getTargetWalletId() {
        return targetWalletId;
    }

    public void setTargetWalletId(int targetWalletId) {
        this.targetWalletId = targetWalletId;
    }

    public int getSourceWalletOldDgtAmt() {
        return sourceWalletOldDgtAmt;
    }

    public void setSourceWalletOldDgtAmt(int sourceWalletOldDgtAmt) {
        this.sourceWalletOldDgtAmt = sourceWalletOldDgtAmt;
    }

    public int getSourceWalletOldLiqAmt() {
        return sourceWalletOldLiqAmt;
    }

    public void setSourceWalletOldLiqAmt(int sourceWalletOldLiqAmt) {
        this.sourceWalletOldLiqAmt = sourceWalletOldLiqAmt;
    }

    public int getTargetWalletOldDgtAmt() {
        return targetWalletOldDgtAmt;
    }

    public void setTargetWalletOldDgtAmt(int targetWalletOldDgtAmt) {
        this.targetWalletOldDgtAmt = targetWalletOldDgtAmt;
    }

    public int getTargetWalletOldLiqAmt() {
        return targetWalletOldLiqAmt;
    }

    public void setTargetWalletOldLiqAmt(int targetWalletOldLiqAmt) {
        this.targetWalletOldLiqAmt = targetWalletOldLiqAmt;
    }

    public int getTransDgtAmt() {
        return transDgtAmt;
    }

    public void setTransDgtAmt(int transDgtAmt) {
        this.transDgtAmt = transDgtAmt;
    }

    public int getTransLiqAmt() {
        return transLiqAmt;
    }

    public void setTransLiqAmt(int transLiqAmt) {
        this.transLiqAmt = transLiqAmt;
    }

    @Override
    public String toString() {
        return "InternalTransactionData{" + "sourceWalletId=" + sourceWalletId + ", targetWalletId=" + targetWalletId + ", sourceWalletOldDgtAmt=" + sourceWalletOldDgtAmt + ", sourceWalletOldLiqAmt=" + sourceWalletOldLiqAmt + ", targetWalletOldDgtAmt=" + targetWalletOldDgtAmt + ", targetWalletOldLiqAmt=" + targetWalletOldLiqAmt + ", transDgtAmt=" + transDgtAmt + ", transLiqAmt=" + transLiqAmt + ", isLiq=" + isLiq + ", sourceWalletName=" + sourceWalletName + '}';
    }
    
    
    
}
