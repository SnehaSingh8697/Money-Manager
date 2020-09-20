/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.wallet;

import domesticfinancesystem.MainFrame;
import domesticfinancesystem.calendar.Database;
import domesticfinancesystem.inttrans.InternalTransactionPanel;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

/**
 *
 * @author sneha
 */
public class NewMyWalletPanel extends javax.swing.JPanel {

    /**
     * Creates new form WalletPanel
     */
    public DefaultListModel<Wallet> walletModel = new DefaultListModel<Wallet>();
    private Database dc;
    private Connection con;
    public NewMyWalletPanel() {
        initComponents();
        lstWallets.setModel(walletModel);
        lstWallets.setCellRenderer(new WalletListRenderer());
        dc = MainFrame.dc;
        con = dc.createConnection();
        
        
//        JPopupMenu pm = new JPopupMenu();
//        JMenuItem item1 = new JMenuItem("Add");
//        JMenuItem item2 = new JMenuItem("Edit");
//        JMenuItem item3 = new JMenuItem("Delete");
//        pm.add(item1);
//        pm.add(item2);
//        pm.add(item3);
          itmAddMoney.setText("Add Money Here");
          itmDeleteWallet.setText("Delete Wallet");
          itmEditWallet.setText("Edit Wallet");
          popWalletOptions.setBorderPainted(true);
          
          if(checkPdExists() == false)
              itmAddMoney.setEnabled(false);
        
       
        setListItems();
        
    }
     
    private boolean isWalletBeDeleted(int id)
    {
        try {
            String s = "Select dt from exttrans where walletid = ? union Select dt from inttrans where SourceWalletId = ? or TargetWalletId = ?";
            PreparedStatement pstmt = con.prepareStatement(s);
            pstmt.setInt(1, id);
            pstmt.setInt(2, id);
            pstmt.setInt(3, id);
            ResultSet rst =  pstmt.executeQuery();
            
            if(rst.next())
            {
               rst.close();
                pstmt.close();
                return false;
            }
            else
            {
                int liquidbal,digitalbal;
                liquidbal = digitalbal = 0;
                s = "Select Liquidbal , Digitalbal from Wallet where Id = ?";
                pstmt = con.prepareStatement(s);
                pstmt.setInt(1, id);
                rst =  pstmt.executeQuery();
                if(rst.next())
                {
                    liquidbal = rst.getInt(1);
                    digitalbal = rst.getInt(2);
                    if(liquidbal!=0 || digitalbal !=0)
                        return false;
                }
                
            }
            return true;
            
        } catch (SQLException ex) {
            Logger.getLogger(NewMyWalletPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    public boolean checkPdExists()
    {
        try {
            Statement stmt = con.createStatement();
            String sql = "Select * from Pd";
            ResultSet rst = stmt.executeQuery(sql);
            if(rst.next())
                return true;
            else
                return false;
        } catch (SQLException ex) {
            Logger.getLogger(NewMyWalletPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    public void setListItems()
    {
        
        try {
            walletModel.clear();
            Statement stmt = con.createStatement();
            String sql = "Select * from Wallet";
            ResultSet rst = stmt.executeQuery(sql);
            while(rst.next())
            {
                int id = rst.getInt("Id");
                String name = rst.getString("Name");
                Blob blob = rst.getBlob("Pic");
                int liqbal = rst.getInt("Liquidbal");
                int digbal = rst.getInt("Digitalbal");

                Image img = null;
                if(blob !=null)
                {
                    InputStream in = blob.getBinaryStream();
                    img = ImageIO.read(in);
                }
                walletModel.addElement(new Wallet(id, name, img,liqbal,digbal));
                
            }
            rst.close();
            stmt.close();
        } catch (SQLException | IOException ex) {
            Logger.getLogger(NewMyWalletPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
            
        

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popWalletOptions = new javax.swing.JPopupMenu();
        itmEditWallet = new javax.swing.JMenuItem();
        itmDeleteWallet = new javax.swing.JMenuItem();
        itmAddMoney = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstWallets = new javax.swing.JList<>();
        btnAddWallet = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        itmEditWallet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/document_edit.png"))); // NOI18N
        itmEditWallet.setText("jMenuItem1");
        itmEditWallet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmEditWalletActionPerformed(evt);
            }
        });
        popWalletOptions.add(itmEditWallet);

        itmDeleteWallet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/delete.gif"))); // NOI18N
        itmDeleteWallet.setText("jMenuItem1");
        itmDeleteWallet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmDeleteWalletActionPerformed(evt);
            }
        });
        popWalletOptions.add(itmDeleteWallet);

        itmAddMoney.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/transfer.png"))); // NOI18N
        itmAddMoney.setText("jMenuItem1");
        itmAddMoney.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmAddMoneyActionPerformed(evt);
            }
        });
        popWalletOptions.add(itmAddMoney);

        setMinimumSize(new java.awt.Dimension(0, 614));

        lstWallets.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstWalletsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(lstWallets);

        btnAddWallet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/Add.gif"))); // NOI18N
        btnAddWallet.setToolTipText("Add Wallet");
        btnAddWallet.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 153, 0)));
        btnAddWallet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddWalletActionPerformed(evt);
            }
        });

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/wallet New1.jpg"))); // NOI18N
        jLabel3.setText("jLabel3");
        jLabel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 153)));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 518, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(btnAddWallet, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 613, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(32, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(327, 327, 327)
                        .addComponent(btnAddWallet, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(245, 245, 245))
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddWalletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddWalletActionPerformed
        // TODO add your handling code here:
            WalletDialog dlg = new WalletDialog(null, true);//pass reference of parent frame later
            dlg.setTitle("Create Wallet");
            dlg.setVisible(true);
            Wallet wallet = dlg.getWallet();
            dlg.dispose();
            if(wallet!=null)
            {
                walletModel.addElement(wallet);
                int ind = walletModel.indexOf((Wallet)wallet);
                lstWallets.setSelectedIndex(ind);
            }
    }//GEN-LAST:event_btnAddWalletActionPerformed

    private void itmEditWalletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmEditWalletActionPerformed
        // TODO add your handling code here:
         int index = lstWallets.getSelectedIndex();
        if(index>=0)
        {
        Wallet wallet = lstWallets.getSelectedValue();
        String name = wallet.getName();
        if(name.equals("Bank")||name.equals("Cash"))
        {
           JOptionPane.showMessageDialog(this, name+" wallet cannot be edited!", "Message", JOptionPane.ERROR_MESSAGE);
        }
        else
        {
            WalletDialog dlg = new WalletDialog(null, true,wallet);
            dlg.setVisible(true);
            wallet = dlg.getUpdateWallet();
            dlg.dispose();
            walletModel.remove(index);
            walletModel.addElement(wallet);
            int ind = walletModel.indexOf((Wallet)wallet);
            lstWallets.setSelectedIndex(ind);
        }
        }
        else
        {
            JOptionPane.showMessageDialog(this,"Please select a wallet to edit", "Message", JOptionPane.ERROR_MESSAGE);

        }
    }//GEN-LAST:event_itmEditWalletActionPerformed

    private void itmDeleteWalletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmDeleteWalletActionPerformed
        // TODO add your handling code here:
         int index = lstWallets.getSelectedIndex();
        if(index>=0)
        {
            Wallet wallet = lstWallets.getSelectedValue();
            String name = wallet.getName();
            
            if(name.equals("Bank")||name.equals("Cash"))
            {
              JOptionPane.showMessageDialog(this, name+" wallet cannot be deleted!", "Message", JOptionPane.ERROR_MESSAGE);
              
            }
            else
            {
                try {
                    
                    if(isWalletBeDeleted(wallet.getId()) == false)
                    {
                       JOptionPane.showMessageDialog(this,"This wallet cannot be deleted", "Message", JOptionPane.ERROR_MESSAGE);
                    }
                    else
                    {
                       int type = JOptionPane.showConfirmDialog(this,"Are you sure you want to delete "+name+" wallet", "Message",JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if(type == JOptionPane.YES_OPTION)
                        {
                            PreparedStatement pst = con.prepareStatement("Delete from Wallet where Id = ?");
                            pst.setInt(1, wallet.getId());
                            pst.executeQuery();
                            pst.close();
                            walletModel.removeElement(wallet);
                        }
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(MyWalletPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this,"Select a wallet to delete", "Message", JOptionPane.ERROR_MESSAGE);
 
        }
    }//GEN-LAST:event_itmDeleteWalletActionPerformed

    private void itmAddMoneyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmAddMoneyActionPerformed
        // TODO add your handling code here:
        Wallet wallet = lstWallets.getSelectedValue();
//        JFrame  f = new JFrame();
//        f.setTitle("Internal Transaction");
//        f.add(new InternalTransactionPanel(wallet,f));
//        f.pack();
//        f.setLocationRelativeTo(null);
//        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        f.setVisible(true);

            JFrame f = new JFrame();
            JDialog dlg= new JDialog(f,true);
            InternalTransactionPanel itp = new InternalTransactionPanel(wallet, f);
            dlg.add(itp);
            dlg.pack();
            dlg.setLocationRelativeTo(null);
            dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dlg.setVisible(true);
            setListItems();

          
    }//GEN-LAST:event_itmAddMoneyActionPerformed

    private void lstWalletsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstWalletsMouseClicked
        if((evt.getButton() & MouseEvent.BUTTON2) == MouseEvent.BUTTON2)
        {
          lstWallets.setSelectedIndex(lstWallets.locationToIndex(evt.getPoint())) ;
          popWalletOptions.show(lstWallets, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_lstWalletsMouseClicked

    public static void main(String args[])
    {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex)
        {
            java.util.logging.Logger.getLogger(NewMyWalletPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(NewMyWalletPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(NewMyWalletPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(NewMyWalletPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                JFrame  f = new JFrame();
                
                f.setTitle("My Wallet");
                f.add(new NewMyWalletPanel());
                f.pack();
                f.setLocationRelativeTo(null);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
                
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddWallet;
    private javax.swing.JMenuItem itmAddMoney;
    private javax.swing.JMenuItem itmDeleteWallet;
    private javax.swing.JMenuItem itmEditWallet;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<Wallet> lstWallets;
    private javax.swing.JPopupMenu popWalletOptions;
    // End of variables declaration//GEN-END:variables
}
