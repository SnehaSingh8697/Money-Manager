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
public class WalletInfo {
    
    private String name;
    private int liquidAmount;
    private int digitalAmount;
    private int id;
    private boolean isLiquid;

    public WalletInfo(int id,String name, int liquidAmount,int digitalAmount,boolean isLiquid) {
        this.id = id;
        this.name = name;
        this.liquidAmount = liquidAmount;
        this.digitalAmount = digitalAmount;
        this.isLiquid = isLiquid;
    }

    public boolean isIsLiquid() {
        return isLiquid;
    }

    public void setIsLiquid(boolean isLiquid) {
        this.isLiquid = isLiquid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLiquidAmount() {
        return liquidAmount;
    }

    public void setLiquidAmount(int liquidAmount) {
        this.liquidAmount = liquidAmount;
    }

    public int getDigitalAmount() {
        return digitalAmount;
    }

    public void setDigitalAmount(int digitalAmount) {
        this.digitalAmount = digitalAmount;
    }

    @Override
    public String toString() {
        return "WalletInfo{" + "name=" + name + ", liquidAmount=" + liquidAmount + ", digitalAmount=" + digitalAmount + ", id=" + id + ", isLiquid=" + isLiquid + '}';
    }

    
    
    
    
}
