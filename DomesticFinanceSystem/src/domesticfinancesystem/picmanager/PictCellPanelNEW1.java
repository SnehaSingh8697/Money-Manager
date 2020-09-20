/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.picmanager;

import domesticfinancesystem.calendar.*;
import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author Sneha
 */
public class PictCellPanelNEW1 extends javax.swing.JPanel
{

    /**
     * Creates new form PictCellPanelNEW
     */
    
    private Image scaleImage(Image img)
    {
        int imageHeight = img.getHeight(null);
        int imageWidth = img.getWidth(null);
//        int labelHeight = lblItem.getMinimumSize().height;
//        int labelWidth =  lblItem.getMinimumSize().width;
        int labelHeight = 68;
        int labelWidth =  73;
        

                   if(imageHeight>imageWidth)
                   {
                        //System.out.println("Hi");
                        int newWidth =(int) ((double)labelHeight/imageHeight*imageWidth);
                        img = img.getScaledInstance(newWidth, labelHeight, Image.SCALE_SMOOTH);
                   }
                   else if(imageHeight<imageWidth)
                   {
                       //System.out.println("Hello");
                      int newHeight =(int) ((double)labelWidth/imageWidth*imageHeight);
                      img = img.getScaledInstance( labelWidth,newHeight, Image.SCALE_SMOOTH); 
                   }
                   else
                   {
                      int newWidth =(int) ((double)labelHeight/imageHeight*imageWidth);
                      img = img.getScaledInstance(newWidth, labelHeight, Image.SCALE_SMOOTH); 
                      
                   } 
                   //img = img.getScaledInstance(211, 126, Image.SCALE_SMOOTH);
                   return img;
    }
    public void setRendererBackground(Color color)
    {
       super.setBackground(color);
       lblName.setBackground(color);
    }
    public void setRendererForeground(Color color)
    {
       super.setForeground(color);
       lblName.setForeground(color);
    }
    
    public void setValue(HolidayPicsNew obj)
    {
//        try
//        {
            if(obj!=null)
            {
                Image img = null;
                img = obj.getImage();
                if(img!=null)
                {
                        img = scaleImage(img);
                        ImageIcon icon = new ImageIcon(img);
                        lblItem.setIcon(icon);
                }
                lblName.setText(obj.getName());
            }
//        } 
//        catch (IOException ex)
//        {
//            Logger.getLogger(PictCellPanelNEW.class.getName()).log(Level.SEVERE, null, ex);
//        }
                    
        
    }
   
    public PictCellPanelNEW1()
    {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblItem = new javax.swing.JLabel();
        lblName = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblItem, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblName, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblItem, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblName, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblItem;
    private javax.swing.JLabel lblName;
    // End of variables declaration//GEN-END:variables
}
