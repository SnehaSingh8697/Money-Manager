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
public class RegularService {
    private int id;
    private String name;
    private int rgbVal;

    public RegularService(int id, String name, int rgbVal) {
        this.id = id;
        this.name = name;
        this.rgbVal = rgbVal;
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

    public int getRgbVal() {
        return rgbVal;
    }

    public void setRgbVal(int rgbVal) {
        this.rgbVal = rgbVal;
    }
    
    
    
}
