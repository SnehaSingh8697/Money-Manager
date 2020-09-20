/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.reports;

import java.util.Date;

/**
 *
 * @author sneha
 */
public class TransactionRecs {
    private Date date;
    private String PartyWallet;
    private int amount;
    private String strAmount;
    private int newLiqBal;
    private int newDigBal;
    private String narration;
    private boolean isLiquid;

    public TransactionRecs(Date date, String PartyWallet, int amount, String strAmount, int newLiqBal, int newDigBal, String narration,boolean isLiquid) {
        this.date = date;
        this.PartyWallet = PartyWallet;
        this.amount = amount;
        this.strAmount = strAmount;
        this.newLiqBal = newLiqBal;
        this.newDigBal = newDigBal;
        this.narration = narration;
        this.isLiquid = isLiquid;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPartyWallet() {
        return PartyWallet;
    }

    public void setPartyWallet(String PartyWallet) {
        this.PartyWallet = PartyWallet;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getStrAmount() {
        return strAmount;
    }

    public void setStrAmount(String strAmount) {
        this.strAmount = strAmount;
    }

    public int getNewLiqBal() {
        return newLiqBal;
    }

    public void setNewLiqBal(int newLiqBal) {
        this.newLiqBal = newLiqBal;
    }

    public int getNewDigBal() {
        return newDigBal;
    }

    public void setNewDigBal(int newDigBal) {
        this.newDigBal = newDigBal;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public boolean isIsLiquid() {
        return isLiquid;
    }

    public void setIsLiquid(boolean isLiquid) {
        this.isLiquid = isLiquid;
    }

}
