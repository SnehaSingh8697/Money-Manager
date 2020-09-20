/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.regularservice;

import java.awt.Color;
import java.awt.Image;
import java.text.SimpleDateFormat;
import javax.swing.ImageIcon;

/**
 *
 * @author sneha
 */
public class ListPanel extends javax.swing.JPanel {

    /**
     * Creates new form ListPanel
     */
    private RegularService rs;
    public ListPanel() {
        initComponents();
    }
    
     public void setRendererBackground(Color color)
    {
       super.setBackground(color);
       lblServiceName.setBackground(color);
    }
    public void setRendererForeground(Color color)
    {
       super.setForeground(color);
       lblServiceName.setForeground(color);
    }
    
    public void setValue(RegularService obj)
    {
          rs = obj;
          lblServiceName.setText(rs.getName());
//          lblServiceName.setBackground(new Color(rs.getRgbVal()));
//          lblServiceName.setForeground(Color.white);
           lblServColor.setBackground(new Color(rs.getRgbVal()));
          
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblServiceName = new javax.swing.JLabel();
        lblServColor = new javax.swing.JLabel();

        lblServiceName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblServiceName.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        lblServColor.setOpaque(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblServColor, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblServiceName, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblServiceName, javax.swing.GroupLayout.DEFAULT_SIZE, 19, Short.MAX_VALUE)
                    .addComponent(lblServColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblServColor;
    private javax.swing.JLabel lblServiceName;
    // End of variables declaration//GEN-END:variables
}
