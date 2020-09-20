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
public class ServDefRecord {
    private String itemName;
    private float price;
    private float qty;
    private String freqDesc;
    private int freq;
    private int id;
    private boolean isNew;

    public ServDefRecord(int id,String itemName, float price, float qty, String freqDesc, int freq, boolean isNew) {
        this.id = id;
        this.itemName = itemName;
        this.price = price;
        this.qty = qty;
        this.freqDesc = freqDesc;
        this.freq = freq;
        this.isNew = isNew;
    }

    public boolean isIsNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }
    
    

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getFreqDesc() {
        return freqDesc;
    }

    public void setFreqDesc(String freqDesc) {
        this.freqDesc = freqDesc;
    }

    public int getFreq() {
        return freq;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }
    
    
    
}
