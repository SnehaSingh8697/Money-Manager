/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.exttrans;

import java.awt.Image;

/**
 *
 * @author sneha
 */
public class TransDocs {
    private int id;
    private int exTransId;
    private String name;
    private Image img;

    public TransDocs(int id, int exTransId, String name, Image img) {
        this.id = id;
        this.exTransId = exTransId;
        this.name = name;
        this.img = img;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExTransId() {
        return exTransId;
    }

    public void setExTransId(int exTransId) {
        this.exTransId = exTransId;
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
    
    
    
}
