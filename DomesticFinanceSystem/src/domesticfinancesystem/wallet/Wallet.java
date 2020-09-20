/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.wallet;

import java.awt.Image;

/**
 *
 * @author sneha
 */
public class Wallet {
    
    private int id;
    private String name;
    private Image img;
    private int liqbal;
    private int digbal;

    public Wallet(int id,String name, Image img,int liqbal,int digbal) {
        this.id = id;
        this.name = name;
        this.img = img;
        this.liqbal = liqbal;
        this.digbal = digbal;
    }

    public Wallet(int id, String name) {
        this.id = id;
        this.name = name;
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

    public Image getImg() {
        return img;
    }

    public void setImg(Image img) {
        this.img = img;
    }

    public float getLiqbal() {
        return liqbal;
    }

    public void setLiqbal(int liqbal) {
        this.liqbal = liqbal;
    }

    public float getDigbal() {
        return digbal;
    }

    public void setDigbal(int digbal) {
        this.digbal = digbal;
    }
    
    
    
    
}
