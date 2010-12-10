package org.eclipse.swt.graphics;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.internal.win32.OS;

public class UIUtils
{

	public static void setFocus(int handle)
	{
		OS.SetFocus(handle);
	}
	
    /* From patch check it */
    public static void setFocus(StyledText styledText) {

        int window = styledText.handle;
        OS.SetFocus(window);
              
    }
}
