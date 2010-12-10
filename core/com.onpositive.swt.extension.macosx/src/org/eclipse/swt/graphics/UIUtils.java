package org.eclipse.swt.graphics;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.internal.carbon.OS;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class UIUtils {

	/*public static void setFocus(int handle) {
		int window = OS.GetControlOwner(handle);
		OS.SetKeyboardFocus(window, handle, (short) OS.kControlFocusNextPart);
	}*/

	/* From patch check it */
	public static void setFocus(StyledText styledText) {

		int window = OS.GetControlOwner(styledText.handle);
		OS.SetKeyboardFocus(window, styledText.handle,
				(short) OS.kControlFocusNextPart);
		//OS.SetFocus(styledText.handle);
	}

}
