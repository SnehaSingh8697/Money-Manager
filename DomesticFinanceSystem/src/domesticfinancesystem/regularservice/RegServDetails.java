/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.regularservice;

/**
 *
 * @author sneha
 */
public class RegServDetails {
    int itemid;
    String itemName;
    float indPrice;
    float qty;
    float amt;

    public RegServDetails(int itemid, float price, float qty,String itemName,float amt) {
        this.itemid = itemid;
        this.indPrice = price;
        this.qty = qty;
        this.itemName = itemName;
        this.amt = amt;
    }

    public RegServDetails(int itemid, float indPrice, float qty) {
        this.itemid = itemid;
        this.indPrice = indPrice;
        this.qty = qty;
    }
    
    

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public float getAmt() {
        return amt;
    }

    public void setAmt(float amt) {
        this.amt = amt;
    }
    
    
    

    public int getItemid() {
        return itemid;
    }

    public void setItemid(int itemid) {
        this.itemid = itemid;
    }

    public float getPrice() {
        return indPrice;
    }

    public void setPrice(float price) {
        this.indPrice = price;
    }

    public float getQty() {
        return qty;
    }

    public void setQty(float qty) {
        this.qty = qty;
    }
    
    
    
}
