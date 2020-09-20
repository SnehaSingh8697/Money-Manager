/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.shoppinglist;

import domesticfinancesystem.MainFrame;
import domesticfinancesystem.calendar.Database;
import domesticfinancesystem.inttrans.InternalTransactionPanel;
import domesticfinancesystem.periodicdeposit.PeriodicDeposit;
import domesticfinancesystem.wallet.Wallet;
import domesticfinancesystem.wallet.WalletListRenderer;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author sneha
 */
public class ShoppingListMainPanel extends javax.swing.JPanel {

    /**
     * Creates new form ShoppingListMainPanel
     */
    private Database dc;
    private Connection con;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private DefaultListModel<ShoppingListHeader> freshListModel = new DefaultListModel<ShoppingListHeader>();
    private DefaultListModel<ShoppingListHeader> templateListModel = new DefaultListModel<ShoppingListHeader>();
    private DefaultListModel<String> listItemModel = new DefaultListModel<String>();
    private ArrayList<ShoppingListHeader> arlLists = new ArrayList<>();
    
    public ShoppingListMainPanel() {
            
            initComponents();
            
            dc = MainFrame.dc;
            con = dc.createConnection();
            
            lstLists.setModel(freshListModel);
            lstLists.setCellRenderer(new ListRenderer());
            lstListDetails.setModel(listItemModel);
            fetchTemplateLists();
            fetchFreshLists();
            getTemplatesFromDb();
            if(arlLists.size()>0)
            {
                lstLists.setModel(templateListModel);
                radTemplate.setSelected(true);
                radFromTemplate.setSelected(true);
                if(templateListModel.size()>0)
                {
                    lstLists.setSelectedIndex(0);
                    fetchListItems();
                }
            }
            else
            {
                lstLists.setModel(freshListModel);
                radFreshList.setSelected(true);
                cmbTemplates.setEnabled(false);
                radRegular.setSelected(true);
                if(freshListModel.size()>0)
                {
                    lstLists.setSelectedIndex(0);
                    fetchListItems();
                }
            }
    }
    public void getTemplatesFromDb()
    {
        cmbTemplates.removeAllItems();
        arlLists.clear();
        try {
            String sql = "Select Id,Name,Dttm from ShoppingList where TemplateYN = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            String temp = "Y";
            pstmt.setString(1, temp);
            ResultSet rst = pstmt.executeQuery();
            while(rst.next())
            {
                int id = rst.getInt(1);
                String name = rst.getString(2);
                Date d = rst.getDate(3);
                ShoppingListHeader li = new ShoppingListHeader(id, name, d);
                arlLists.add(li);
                cmbTemplates.addItem(li.getName());
            }
            rst.close();
            pstmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(ShoppingListMainPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void addList(ShoppingListHeader li,boolean isTemplate)
    {
        if(isTemplate)
        {
            templateListModel.addElement(li);
            lstLists.setModel(templateListModel);
            radTemplate.setSelected(true);
            int index = templateListModel.indexOf(li);
            lstLists.setSelectedIndex(index);
            cmbTemplates.addItem(li.getName());

        }
        else
        {
            freshListModel.addElement(li);
            lstLists.setModel(freshListModel);
            radRegular.setSelected(true);
            int index = freshListModel.indexOf(li);
            lstLists.setSelectedIndex(index);
        }
        repaint();
    }
    public void removeList(int index,boolean isTemplate)
    {
        lblDate.setText("");
        lblDate.setText("");
        if(isTemplate)
        {
            lstLists.setModel(templateListModel);
            String listName = "";
           for (int i = 0; i < templateListModel.size(); i++) 
           {
                ShoppingListHeader li = templateListModel.get(i);
                 if(li.getId() == index)
                 {
                     listName = li.getName();
                     templateListModel.remove(i);
                     break;
                 }
           }
           if(templateListModel.size()>0)
            {
                lstLists.setSelectedIndex(0);
            }
//            else
//            {
//                templateListModel.clear();
//                listItemModel.clear();
//            }
            cmbTemplates.removeItem(listName);
            ShoppingListHeader l = null;
            for (ShoppingListHeader arlList : arlLists) {
                if(arlList.getName().equals(listName))
                {
                    l = arlList;
                    break;
                }
                
            }
            arlLists.remove(l);
            if(arlLists.size() == 0)
            {
                cmbTemplates.setEnabled(false);
                radRegular.setSelected(true);
                radFreshList.setSelected(true);
                lstLists.setModel(freshListModel);
                if(freshListModel.size()>0)
                {
                    lstLists.setSelectedIndex(0);
                }
            }
            
        }
        else
        {
           lstLists.setModel(freshListModel);
           for (int i = 0; i < freshListModel.size(); i++) 
           {
                ShoppingListHeader li = freshListModel.get(i);
                 if(li.getId() == index)
                 {
                     freshListModel.remove(i);
                     break;
                 }
           }
           if(freshListModel.size()>0)
            {
                lstLists.setSelectedIndex(0);
            }
            else
            {
                freshListModel.clear();
                listItemModel.clear();
            }
            
        }
        
        
    }
    public void updateList(int index,ShoppingListHeader li,boolean isTemplate)
    {
        if(isTemplate)
        {
            radTemplate.setSelected(true);
            lstLists.setModel(templateListModel);
            for(int i = 0;i<templateListModel.size();i++)
            {
                ShoppingListHeader l = templateListModel.get(i);
                if(index == l.getId())
                {
                   templateListModel.removeElementAt(i);
                   break; 
                }
            }
            templateListModel.addElement(li);
            int ind = templateListModel.indexOf(li);
            lstLists.setSelectedIndex(ind);
            cmbTemplates.removeItem(li.getName());
            cmbTemplates.addItem(li.getName());
        }
        else
        {
            radRegular.setSelected(true);
            lstLists.setModel(freshListModel);
            for(int i = 0;i<freshListModel.size();i++)
            {
                ShoppingListHeader l = freshListModel.get(i);
                if(index == l.getId())
                {
                   freshListModel.removeElementAt(i);
                   break; 
                }
            }
            freshListModel.addElement(li);
            int ind = freshListModel.indexOf(li);
            lstLists.setSelectedIndex(ind);
        }
        repaint();
    }
//    public void addListFromDb()
//    {
//        try {
//            String sql = "Select Id,Name,Dttm from ShoppingList";
//            Statement stmt = con.createStatement();
//            ResultSet rst = stmt.executeQuery(sql);
//            while(rst.next())
//            {
//                int id = rst.getInt(1);
//                String name = rst.getString(2);
//                Date dt = rst.getDate(3);
//                List li = new List(id, name, dt);
//                listModel.addElement(li);
//            }
//            rst.close();
//            stmt.close();
//        } catch (SQLException ex) {
//            Logger.getLogger(PeriodicDeposit.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    
//   
    
    public void fetchTemplateLists()
    {
        templateListModel.clear();
        listItemModel.clear();
         try {
            
            String sql = "Select Id,Name,Dttm from ShoppingList where TemplateYN = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            String temp = "Y";
            pstmt.setString(1, temp);
            ResultSet rst = pstmt.executeQuery();
            while(rst.next())
            {
                int id = rst.getInt(1);
                String name = rst.getString(2);
                Date d = rst.getDate(3);
                ShoppingListHeader li = new ShoppingListHeader(id, name, d);
                templateListModel.addElement(li);
            }
            rst.close();
            pstmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(ShoppingListMainPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void fetchFreshLists()
    {
         freshListModel.clear();
         listItemModel.clear();
         try {
           
            String sql = "Select Id,Name,Dttm from ShoppingList where TemplateYN = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            String temp = "N";
            pstmt.setString(1, temp);
            ResultSet rst = pstmt.executeQuery();
            while(rst.next())
            {
                int id = rst.getInt(1);
                String name = rst.getString(2);
                Date d = rst.getDate(3);
                ShoppingListHeader li = new ShoppingListHeader(id, name, d);
                freshListModel.addElement(li);
            }
            rst.close();
            pstmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(ShoppingListMainPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void fetchListItems()
    {
        try {
                int index = lstLists.getSelectedIndex();
                listItemModel.clear();
                ShoppingListHeader li;
                if(radRegular.isSelected())
                     li = freshListModel.getElementAt(index);
                else
                   li = templateListModel.getElementAt(index); 
                
                lblListName.setText(li.getName());
                lblDate.setText(formatter.format(li.getDt()));
                
                //Fetch list items from database
                String sql = "Select li.Name from ShoppingList sl ,SListDetail sld,ListItem li where sld.lstId = sl.Id and sld.ItemId = li.Id and sl.Id = ?";
                PreparedStatement pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, li.getId());
                ResultSet rst = pstmt.executeQuery();
                while(rst.next())
                {
                    String name = rst.getString(1);
                    listItemModel.addElement(name);
                }
                rst.close();
                pstmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(ShoppingListMainPanel.class.getName()).log(Level.SEVERE, null, ex);
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

        radgrpOptions = new javax.swing.ButtonGroup();
        radgrpListCreate = new javax.swing.ButtonGroup();
        upperPanel = new javax.swing.JPanel();
        midPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstLists = new javax.swing.JList<>();
        lblListName = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstListDetails = new javax.swing.JList<>();
        lblDate = new javax.swing.JLabel();
        btnDetails = new javax.swing.JButton();
        radTemplate = new javax.swing.JRadioButton();
        radRegular = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        lblListIcon = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        radFromTemplate = new javax.swing.JRadioButton();
        cmbTemplates = new javax.swing.JComboBox<>();
        radFreshList = new javax.swing.JRadioButton();
        btnCreate = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        lstLists.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        lstLists.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstListsValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstLists);

        lblListName.setFont(new java.awt.Font("Garamond", 1, 14)); // NOI18N
        lblListName.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lstListDetails.setFont(new java.awt.Font("Garamond", 1, 14)); // NOI18N
        jScrollPane2.setViewportView(lstListDetails);

        lblDate.setFont(new java.awt.Font("Garamond", 1, 14)); // NOI18N
        lblDate.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnDetails.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnDetails.setText("Edit or show details...");
        btnDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDetailsActionPerformed(evt);
            }
        });

        radgrpOptions.add(radTemplate);
        radTemplate.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radTemplate.setText("Template");
        radTemplate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radTemplateActionPerformed(evt);
            }
        });

        radgrpOptions.add(radRegular);
        radRegular.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radRegular.setText("Regular");
        radRegular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radRegularActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout midPanelLayout = new javax.swing.GroupLayout(midPanel);
        midPanel.setLayout(midPanelLayout);
        midPanelLayout.setHorizontalGroup(
            midPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(midPanelLayout.createSequentialGroup()
                .addGroup(midPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(midPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(midPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(midPanelLayout.createSequentialGroup()
                                .addGap(105, 105, 105)
                                .addComponent(btnDetails))
                            .addGroup(midPanelLayout.createSequentialGroup()
                                .addGap(56, 56, 56)
                                .addGroup(midPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblListName, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblDate, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(midPanelLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(radTemplate)
                        .addGap(52, 52, 52)
                        .addComponent(radRegular, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(34, 34, 34))
        );
        midPanelLayout.setVerticalGroup(
            midPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(midPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(midPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radTemplate)
                    .addComponent(radRegular))
                .addGroup(midPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(midPanelLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(midPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblListName, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(btnDetails)
                        .addGap(105, 105, 105))
                    .addGroup(midPanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblListIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/shop_list.png"))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Garamond", 1, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("My Shopping List");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Create New List", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 204))); // NOI18N

        radgrpListCreate.add(radFromTemplate);
        radFromTemplate.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radFromTemplate.setText("From Template");
        radFromTemplate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radFromTemplateActionPerformed(evt);
            }
        });

        radgrpListCreate.add(radFreshList);
        radFreshList.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        radFreshList.setText("Fresh List");
        radFreshList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radFreshListActionPerformed(evt);
            }
        });

        btnCreate.setFont(new java.awt.Font("Garamond", 0, 14)); // NOI18N
        btnCreate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/Add.gif"))); // NOI18N
        btnCreate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(radFromTemplate)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cmbTemplates, 0, 184, Short.MAX_VALUE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(radFreshList)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(108, 108, 108)
                        .addComponent(btnCreate, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(radFreshList)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radFromTemplate)
                    .addComponent(cmbTemplates, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addComponent(btnCreate))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblListIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblListIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout upperPanelLayout = new javax.swing.GroupLayout(upperPanel);
        upperPanel.setLayout(upperPanelLayout);
        upperPanelLayout.setHorizontalGroup(
            upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(midPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, upperPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        upperPanelLayout.setVerticalGroup(
            upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upperPanelLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(midPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/testimages/shopping list.jpg"))); // NOI18N
        jLabel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(upperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 626, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(upperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 641, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void radRegularActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radRegularActionPerformed
        // TODO add your handling code here:
        fetchFreshLists();
        lstLists.setModel(freshListModel);
        if(freshListModel.size()>0)
            lstLists.setSelectedIndex(0);
    }//GEN-LAST:event_radRegularActionPerformed

    private void radTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radTemplateActionPerformed
      fetchTemplateLists();
      lstLists.setModel(templateListModel);
        if(templateListModel.size()>0)
            lstLists.setSelectedIndex(0);
    }//GEN-LAST:event_radTemplateActionPerformed

    private void btnDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetailsActionPerformed
        // TODO add your handling code here:
        int index = lstLists.getSelectedIndex();
        if(index>=0)
        {
            ShoppingListHeader li = lstLists.getSelectedValue();
            JFrame f = null;
            JDialog dlg= new JDialog(f,true);
            CreateShoppingList csl = new CreateShoppingList(li.getId(),this,dlg,true);
            dlg.add(csl);
            dlg.pack();
            dlg.setLocationRelativeTo(null);
            dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dlg.setVisible(true);

        }
    }//GEN-LAST:event_btnDetailsActionPerformed

    private void lstListsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstListsValueChanged
        // TODO add your handling code here:
        int index = lstLists.getSelectedIndex();
        if(index>=0)
        {
            fetchListItems();
        }

    }//GEN-LAST:event_lstListsValueChanged

    private void btnCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateActionPerformed
        // TODO add your handling code here:
        JFrame f = null;
        JDialog dlg= new JDialog(f,true);
        CreateShoppingList csl = null;
        if(radFreshList.isSelected())
        {
                csl = new CreateShoppingList(this,dlg);
        }
        else
        {
            int index = cmbTemplates.getSelectedIndex();
            if(index>=0)
            {
               
               String name = cmbTemplates.getItemAt(index);
               int ind = -1;
                for (ShoppingListHeader lst : arlLists) {
                    if(lst.getName().equals(name))
                    {
                        ind = lst.getId();
                        break;
                    }
                }
                
            csl = new CreateShoppingList(this,dlg, ind);
            }
            else
            {
                JOptionPane.showMessageDialog(this,"Template not selected", "Message",JOptionPane.ERROR_MESSAGE);
            }
                
        }
        if(csl!=null)
        {
            dlg.add(csl);
            dlg.pack();
            dlg.setLocationRelativeTo(null);
            dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dlg.setVisible(true);
        }
    }//GEN-LAST:event_btnCreateActionPerformed

    private void radFromTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radFromTemplateActionPerformed
        // TODO add your handling code here:
        cmbTemplates.setEnabled(true);
        getTemplatesFromDb();
    }//GEN-LAST:event_radFromTemplateActionPerformed

    private void radFreshListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radFreshListActionPerformed
        // TODO add your handling code here:
        cmbTemplates.setEnabled(false);
    }//GEN-LAST:event_radFreshListActionPerformed

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
            java.util.logging.Logger.getLogger(domesticfinancesystem.shoppinglist.ShoppingListMainPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(domesticfinancesystem.shoppinglist.ShoppingListMainPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(domesticfinancesystem.shoppinglist.ShoppingListMainPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(domesticfinancesystem.shoppinglist.ShoppingListMainPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                JFrame  f = new JFrame();
                
                f.setTitle("Shopping List");
                f.add(new ShoppingListMainPanel());
                f.pack();
                f.setLocationRelativeTo(null);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
                
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCreate;
    private javax.swing.JButton btnDetails;
    private javax.swing.JComboBox<String> cmbTemplates;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblListIcon;
    private javax.swing.JLabel lblListName;
    private javax.swing.JList<String> lstListDetails;
    private javax.swing.JList<domesticfinancesystem.shoppinglist.ShoppingListHeader> lstLists;
    private javax.swing.JPanel midPanel;
    private javax.swing.JRadioButton radFreshList;
    private javax.swing.JRadioButton radFromTemplate;
    private javax.swing.JRadioButton radRegular;
    private javax.swing.JRadioButton radTemplate;
    private javax.swing.ButtonGroup radgrpListCreate;
    private javax.swing.ButtonGroup radgrpOptions;
    private javax.swing.JPanel upperPanel;
    // End of variables declaration//GEN-END:variables
}
