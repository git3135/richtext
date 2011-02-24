package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Display;

public class TextLayoutOpsProvider {

	static ITextLayoutOps generic=new ITextLayoutOps() {
		
		public void shiftLines(TextLayout textLayout, int lineInLayout, int delta) {
			
		}
		
		public void setFirstLineSpacing(TextLayout textLayout,
				TextLayout localLayout, int paragraphSpacing2) {
			
		}
		
		public boolean needsSpacing(TextLayout textLayout, TextLayout localLayout,
				int height, int practicalHeight) {
			return false;
		}
		
		public boolean needToDrawFirstInvivsibleLine() {
			return false;
		}
		
		public Point getScreenSize() {
			return Display.getCurrent().getDPI();
		}
		
		public Point getLocation(TextLayout layout, int i, boolean b) {
			return layout.getLocation(i, b);
		}
		
		public int getLineY(TextLayout layout, int i) {
			return layout.getLineBounds(i).y;
		}
		
		public int getLineHeight(TextLayout textLayout, int lineInLayout) {
			return getLineBounds(textLayout, lineInLayout).height;
		}
		
		public Rectangle getLineBounds(TextLayout layout, int lineIndex) {
			return layout.getLineBounds(lineIndex);
		}
		
		public int getLineAscent(TextLayout layout, int i) {
			return layout.getAscent();
		}
		
		public int getHeight(TextLayout layout) {
			return layout.getBounds().height;
		}
		
		public int getExtra() {
			return 0;
		}
		
		public Rectangle getBounds(TextLayout layout, int i, int j) {
			return layout.getBounds(i, j);
		}

		public boolean canShiftParagraphs() {
			return false;
		}
	};
	
	public static ITextLayoutOps getInstance() {		
		return generic;
	}
}
