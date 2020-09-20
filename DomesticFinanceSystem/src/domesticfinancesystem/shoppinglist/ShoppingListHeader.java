/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.shoppinglist;

import java.util.Date;

/**
 *
 * @author sneha
 */
public class ShoppingListHeader {
    
    private int id;
    private String name;
    private Date dt;

    public ShoppingListHeader(int id, String name, Date dt) {
        this.id = id;
        this.name = name;
        this.dt = dt;
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

    public Date getDt() {
        return dt;
    }

    public void setDt(Date dt) {
        this.dt = dt;
    }
    
    
    
    
    
    
}
