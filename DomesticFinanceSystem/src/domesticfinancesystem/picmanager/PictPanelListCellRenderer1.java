/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem.picmanager;

import domesticfinancesystem.calendar.*;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

/**
 *
 * @author Sneha
 */
public class PictPanelListCellRenderer1 extends PictCellPanelNEW1 implements ListCellRenderer
{
    
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
        setComponentOrientation(list.getComponentOrientation());
        setFont(list.getFont());
        if(isSelected)
        {
//           setRendererBackground(UIManager.getLookAndFeelDefaults().getColor("List.selectionBackground"));
//           setRendererForeground(UIManager.getLookAndFeelDefaults().getColor("List.selectionForeground"));

             setRendererBackground(Color.BLUE);
             setRendererForeground(Color.WHITE);
        }
        else
        {
            setRendererBackground(list.getBackground());
            setRendererForeground(list.getForeground());
            
        }
        setEnabled(list.isEnabled());
        setValue((HolidayPicsNew)value);
        return this ;
    }
    
    
}
