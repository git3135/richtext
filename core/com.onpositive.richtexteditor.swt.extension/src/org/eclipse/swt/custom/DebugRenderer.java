package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;

/**
 * Simple  renderer for debugging purposes only
 * @author 32kda
 *
 */
public class DebugRenderer extends StyledTextRenderer
{
	
	protected static final String CAPITALIZE_STR = "CAPITALIZE";

	DebugRenderer(Device device, StyledText styledText)
	{
		super(device, styledText);
	}
	
	TextLayout getTextLayout(int lineIndex)
	{
		TextLayout textLayout = super.getTextLayout(lineIndex);
		checkStyles(lineIndex, textLayout);
		return textLayout;
	}
	
	protected void checkStyles(int lineIndex, TextLayout textLayout) 
	{
		String text = textLayout.getText();
		int[] segments2 = textLayout.getRanges();		
		TextStyle[] styles2 = textLayout.getStyles();		
		for (int i = 0; i < styles2.length; i++) {
			if (styles2[i].data != null && styles2[i].data.equals(CAPITALIZE_STR))
			{
				
				int offset = segments2[i * 2];
				int end = segments2[i * 2 + 1];

				String newText = text.substring(offset, end + 1).toUpperCase();
				StringBuilder sb = new StringBuilder(text.substring(0, offset));
				sb.append(newText);
				sb.append(text.substring(end + 1));		
				text = sb.toString();
			}
		}
		textLayout.setText(text);
		for (int i = 0; i < styles2.length; i++)
		{
			textLayout.setStyle(styles2[i],segments2[i * 2],segments2[i * 2 + 1]);
		}
	}

}
