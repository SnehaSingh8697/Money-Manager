/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.regserv.report;

import java.util.Date;

/**
 *
 * @author sneha
 */
public class RegServRecs {
    private int id;
    private boolean isPaid;
    private Date date;
    private String itemName;
    private float price;
    private float qty;
    private float amount;
    private boolean isPayEditable;

    public RegServRecs(int id,boolean isPaid, Date date,String itemName, float price, float qty, float amount, boolean isPayEditable) {
        this.id = id;
        this.isPaid = isPaid;
        this.date = date;
        this.itemName = itemName;
        this.price = price;
        this.qty = qty;
        this.amount = amount;
        this.isPayEditable = isPayEditable;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public boolean isIsPaid() {
        return isPaid;
    }

    public void setIsPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getQty() {
        return qty;
    }

    public void setQty(float qty) {
        this.qty = qty;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public boolean isIsPayEditable() {
        return isPayEditable;
    }

    public void setIsPayEditable(boolean isPayEditable) {
        this.isPayEditable = isPayEditable;
    }
    
}
