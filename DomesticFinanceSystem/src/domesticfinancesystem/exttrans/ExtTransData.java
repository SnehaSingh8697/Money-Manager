/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.exttrans;

import java.util.Date;

/**
 *
 * @author sneha
 */
public class ExtTransData {
    private Date dt;
    private int ppId;
    private String pname;
    private String narration;
    private int walletId;
    private float paidAmt;
    private float recvAmt;
    private String mode;
    private String refNumber;
    private String wName;
    private int id;

    public ExtTransData(Date dt, int ppId, String pname, String narration, int walletId, float paidAmt, float recvAmt,String mode, String refNumber,String wName,int id) {
        this.dt = dt;
        this.ppId = ppId;
        this.pname = pname;
        this.narration = narration;
        this.walletId = walletId;
        this.wName = wName;
        this.paidAmt = paidAmt;
        this.recvAmt = recvAmt;
        this.mode = mode;
        this.refNumber = refNumber;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    
    public String getwName() {
        return wName;
    }

    public void setwName(String wName) {
        this.wName = wName;
    }
    
    

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }
    
    

    public Date getDt() {
        return dt;
    }

    public void setDt(Date dt) {
        this.dt = dt;
    }

    public int getPpId() {
        return ppId;
    }

    public void setPpId(int ppId) {
        this.ppId = ppId;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public int getWalletId() {
        return walletId;
    }

    public void setWalletId(int walletId) {
        this.walletId = walletId;
    }

    public float getPaidAmt() {
        return paidAmt;
    }

    public void setPaidAmt(float paidAmt) {
        this.paidAmt = paidAmt;
    }

    public float getRecvAmt() {
        return recvAmt;
    }

    public void setRecvAmt(float recvAmt) {
        this.recvAmt = recvAmt;
    }

   

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getRefNumber() {
        return refNumber;
    }

    public void setRefNumber(String refNumber) {
        this.refNumber = refNumber;
    }
    
    
    
}
