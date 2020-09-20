/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.shoppinglist;

/**
 *
 * @author sneha
 */
public class ListRecord {
    private int itemId;
    private String itemName;
    private float qty;
    private float price;
    private String remarks;
    private float indPrice;
    private int uomId;
    private String uomName;
    private boolean checkedYN;

    public ListRecord(int itemId, String itemName, float qty, float price, String remarks,float ip,int uomId,String uomName,boolean checkedYN) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.qty = qty;
        this.price = price;
        this.remarks = remarks;
        indPrice = ip;
        this.uomId = uomId;
        this.uomName = uomName;
        this.checkedYN = checkedYN;
    }

    public float getIndPrice() {
        return indPrice;
    }

    public void setIndPrice(float indPrice) {
        this.indPrice = indPrice;
    }


    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public float getQty() {
        return qty;
    }

    public void setQty(float qty) {
        this.qty = qty;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public int getUomId() {
        return uomId;
    }

    public void setUomId(int uomId) {
        this.uomId = uomId;
    }

    public String getUomName() {
        return uomName;
    }

    public void setUomName(String uomName) {
        this.uomName = uomName;
    }

    public boolean isCheckedYN() {
        return checkedYN;
    }

    public void setCheckedYN(boolean checkedYN) {
        this.checkedYN = checkedYN;
    }
    
    
    
}
