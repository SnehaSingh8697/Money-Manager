/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.regularservice;

import java.util.Date;

/**
 *
 * @author sneha
 */
public class RegServ {
    private int id;
    private int ServDefId;
    private Date dt;

    public RegServ(int id, int ServDefId, Date dt) {
        this.id = id;
        this.ServDefId = ServDefId;
        this.dt = dt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getServDefId() {
        return ServDefId;
    }

    public void setServDefId(int ServDefId) {
        this.ServDefId = ServDefId;
    }

    public Date getDt() {
        return dt;
    }

    public void setDt(Date dt) {
        this.dt = dt;
    }
    
    
    
}
