/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.picmanager;

import domesticfinancesystem.calendar.*;
import java.awt.Image;
import java.io.File;
import java.util.Date;

/**
 *
 * @author sneha
 */
public class HolidayPicsNew {
    
    private String name;
    private Image image;
    private char htype;
    private int day;
    private int month;
    private Date dt;

    public HolidayPicsNew(String name, Image image) {
        this.name = name;
        this.image = image;
    }

    public HolidayPicsNew(String name, Image image, char htype, int day, int month, Date dt) {
        this.name = name;
        this.image = image;
        this.htype = htype;
        this.day = day;
        this.month = month;
        this.dt = dt;
    }

    public char getHtype() {
        return htype;
    }

    public void setHtype(char htype) {
        this.htype = htype;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public Date getDt() {
        return dt;
    }

    public void setDt(Date dt) {
        this.dt = dt;
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
