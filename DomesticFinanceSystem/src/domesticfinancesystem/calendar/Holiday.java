/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.calendar;

import java.awt.Image;

/**
 *
 * @author sneha
 */
public class Holiday {
    
    private String name;
    private Image image;

    public Holiday(String name, Image image) {
        this.name = name;
        this.image = image;
    }
    
    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
    
    
    
}
