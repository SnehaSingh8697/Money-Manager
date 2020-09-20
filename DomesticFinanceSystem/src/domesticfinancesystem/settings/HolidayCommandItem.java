/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.settings;

import domesticfinancesystem.picmanager.*;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/**
 *
 * @author sneha
 */
public class HolidayCommandItem {
    private char hType;
    private char command;
    private String hName;
    private Date dt;
    private File hPicFile;
    private Image image;
    private String newName;

    public HolidayCommandItem() {
    }

    public HolidayCommandItem(char hType, char command, String hName, Date dt, File hPicPath, Image image, String newName) {
        this.hType = hType;
        this.command = command;
        this.hName = hName;
        this.dt = dt;
        this.hPicFile = hPicPath;
        this.image = image;
        this.newName = newName;
    }

    public char gethType() {
        return hType;
    }

    public void sethType(char hType) {
        this.hType = hType;
    }

    public char getCommand() {
        return command;
    }

    public void setCommand(char command) {
        this.command = command;
    }

    public String getName() {
        return hName;
    }

    public void sethName(String hName) {
        this.hName = hName;
    }

    public Date getDt() {
        return dt;
    }

    public void setDt(Date dt) {
        this.dt = dt;
    }

    public File gethPicFile() {
        return hPicFile;
    }

    public void sethPicFile(File hPicPath) {
        this.hPicFile = hPicPath;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }
    
    public Image extractImage(ZipFile zipfile)
    {
        Image img = null;
        try {
            InputStream in = null;
            ZipEntry ze = zipfile.getEntry(gethPicFile().getName());
            in = zipfile.getInputStream(ze);
            img = ImageIO.read(in);
            setImage(img);
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(HolidayCommandItem.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return img;
    }
   
}
