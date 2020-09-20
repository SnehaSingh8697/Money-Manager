/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domesticfinancesystem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 *
 * @author Sneha
 */
public class IntegerDocumentFilter extends DocumentFilter
{
    private String numregex = "^-?(\\d*\\.)?\\d+$";
    private Pattern patNum = Pattern.compile(numregex);

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException
    {
        Document doc = fb.getDocument();
        String lpart = doc.getText(0, offset);
        int p = offset + length;
        String rpart = doc.getText(p, doc.getLength()-p);
        String str = lpart+text+rpart;
        str = str.trim();
        boolean valid  = true ;
        if(str != null && str.length() > 0)
        {
                Matcher matdig = patNum.matcher(str);
                 if(matdig.matches())
                     valid = true;
                 else
                     valid = false;
        }
        if(valid)
            super.replace(fb, offset, length, text, attrs); //To change body of generated methods, choose Tools | Templates.
    }
    
}
