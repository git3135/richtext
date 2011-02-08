package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;

public interface ITextLayoutOps {

	boolean needsSpacing(TextLayout textLayout, TextLayout localLayout,
			int height, int practicalHeight);

	void setFirstLineSpacing(TextLayout textLayout, TextLayout localLayout,
			int paragraphSpacing2);

	int getLineHeight(TextLayout textLayout, int lineInLayout);

	int getLineY(TextLayout layout, int i);

	int getHeight(TextLayout layout);

	void shiftLines(TextLayout textLayout, int lineInLayout, int delta);

	int getExtra();

	Rectangle getLineBounds(TextLayout layout, int lineIndex);

	Rectangle getBounds(TextLayout layout, int i, int j);

	Point getLocation(TextLayout layout, int i, boolean b);

	int getLineAscent(TextLayout layout, int i);

	boolean needToDrawFirstInvivsibleLine();

	Point getScreenSize();

	
}
	