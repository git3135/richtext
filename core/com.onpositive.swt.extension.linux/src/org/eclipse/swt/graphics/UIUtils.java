package org.eclipse.swt.graphics;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.internal.gtk.OS;

public class UIUtils
{

	public static void setFocus(int handle)
	{
		//OS.gtk_widget_SetFocus(handle);
	}
	
    /* From patch check it */
    public static void setFocus(StyledText styledText) {
        styledText.setFocus();
              
    }
}
