/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.regularservice;

import domesticfinancesystem.inttrans.*;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author sneha
 */
public class ItemComboBoxCellRenderer  extends ItemComboBoxCellPanel implements ListCellRenderer{
    
      @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
        setComponentOrientation(list.getComponentOrientation());
        setFont(list.getFont());
        if(isSelected)
        {
             setRendererBackground(Color.BLUE);
             setRendererForeground(Color.WHITE);
        }
        else
        {
            setRendererBackground(list.getBackground());
            setRendererForeground(list.getForeground());
            
        }
        setEnabled(list.isEnabled());
        setValue((Item)value);
        return this ;
    }
    
            
    
}
