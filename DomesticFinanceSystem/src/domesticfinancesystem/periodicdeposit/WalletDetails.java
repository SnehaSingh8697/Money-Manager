/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.periodicdeposit;

/**
 *
 * @author sneha
 */
public class WalletDetails {
   
   private String name;
   private int walletId;
   private int walletDgtAmtOld;
   private int walletLiqAmtOld;
   private int walletDgtAmtNew;
   private int walletLiqAmtNew;

    public WalletDetails(int walletId, String name,int walletDgtAmtOld, int walletLiqAmtOld) {
        this.walletId = walletId;
        this.name = name;
        this.walletDgtAmtOld = walletDgtAmtOld;
        this.walletLiqAmtOld = walletLiqAmtOld;
        walletLiqAmtNew = 0;
        walletDgtAmtNew = 0;
    }

    public WalletDetails(String name, int walletId, int walletDgtAmtOld, int walletLiqAmtOld, int walletDgtAmtNew, int walletLiqAmtNew) {
        this.name = name;
        this.walletId = walletId;
        this.walletDgtAmtOld = walletDgtAmtOld;
        this.walletLiqAmtOld = walletLiqAmtOld;
        this.walletDgtAmtNew = walletDgtAmtNew;
        this.walletLiqAmtNew = walletLiqAmtNew;
    }
    
    
   
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWalletDgtAmtNew() {
        return walletDgtAmtNew;
    }

    public void setWalletDgtAmtNew(int walletDgtAmtNew) {
        this.walletDgtAmtNew = walletDgtAmtNew;
    }

    public int getWalletLiqAmtNew() {
        return walletLiqAmtNew;
    }

    public void setWalletLiqAmtNew(int walletLiqAmtNew) {
        this.walletLiqAmtNew = walletLiqAmtNew;
    }
    
    public int getWalletId() {
        return walletId;
    }

    public void setWalletId(int walletId) {
        this.walletId = walletId;
    }

    public int getWalletDgtAmtOld() {
        return walletDgtAmtOld;
    }

    public void setWalletDgtAmtOld(int walletDgtAmtOld) {
        this.walletDgtAmtOld = walletDgtAmtOld;
    }

    public int getWalletLiqAmtOld() {
        return walletLiqAmtOld;
    }

    public void setWalletLiqAmtOld(int walletLiqAmtOld) {
        this.walletLiqAmtOld = walletLiqAmtOld;
    }

    @Override
    public String toString() {
        return "WalletDetails{" + "name=" + name + ", walletId=" + walletId + ", walletDgtAmtOld=" + walletDgtAmtOld + ", walletLiqAmtOld=" + walletLiqAmtOld + ", walletDgtAmtNew=" + walletDgtAmtNew + ", walletLiqAmtNew=" + walletLiqAmtNew + '}';
    }

}
