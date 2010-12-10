package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;

/**
 * @author kor
 * 
 */
public interface ILineBackgroundPainter {

	void paintLineBackground(int lineNumber, int lineOffset, String lineText,
			TextLayout layout, GC gc,Rectangle lineBounds);
}
