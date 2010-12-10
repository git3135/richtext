package org.eclipse.swt.graphics;

import org.eclipse.swt.widgets.Display;

public class TextLayoutOps {

	public static void setLineY(TextLayout layout, int line, int y) {
		layout.lineY[line] = y;
	}

	public static void shiftLines(TextLayout layout, int line, int delta) {
		if (layout.lineY[layout.lineY.length - 1] == 1) {
			layout.lineY[layout.lineY.length - 1] = 0;
		}
		for (int a = line + 1; a < layout.lineY.length; a++) {
			layout.lineY[a] += delta;
		}
	}

	public static boolean needsSpacing(TextLayout layout,
			TextLayout localLayout, int height, int practicalHeight) {
		return height == practicalHeight; // == 0
	}

	public static int getLineY(TextLayout layout, int line) {
		return layout.lineY[line];
	}

	public static int getHeight(TextLayout t) {
		return t.lineY[t.lineY.length - 1];
	}

	public static int getLineHeight(TextLayout textLayout, int a) {
		if (a < textLayout.lineY.length - 1) {
			if (a == 0) {
				return textLayout.lineY[1];
			} else {
				return (textLayout.lineY[a + 1] - textLayout.lineY[a]);
			}
		}
		if (textLayout.lineY.length == 1) {
			return textLayout.lineY[a];
		}
		return 1;

		/*
		 * if (a > 0) { return textLayout.lineY[a] - textLayout.lineY[a - 1]; }
		 * return textLayout.lineY[a];
		 */
	}

	public static void setFirstLineSpacing(TextLayout textLayout,
			TextLayout localLayout, int paragraphSpacing) {

		if (textLayout.lineY[textLayout.lineY.length - 1] == 1) {
			textLayout.lineY[textLayout.lineY.length - 1] = 0;
		}
		for (int a = 0; a < textLayout.lineY.length; a++) {
			textLayout.lineY[a] += paragraphSpacing;
		}
		// TextLayoutOps.shiftLines(textLayout, 0, paragraphSpacing);
	}

	public static int getLineAscent(TextLayout layout, int i) {
		return 0;
	}

	public static int getExtra() {
		return 0;
	}

	public static void forceComputeRuns(TextLayout layout, GC gc) {
		layout.computeRuns(gc);
	}

	/**
	 * We need to draw first afterpage invivsible line under some OSes because
	 * this line can contain something useful for use, like pagebraks
	 * 
	 * @return false under Win, true under Mac OS X
	 */
	public static boolean needToDrawFirstInvivsibleLine() {
		return false;
	}

	public static Point getScreenSize() { // Stub implementation
		Point dpi = Display.getDefault().getDPI();
		Rectangle bounds = Display.getDefault().getBounds();
		return new Point((int) Math.round(bounds.width * 1.0 / dpi.x * 25.4),
				(int) Math.round(bounds.height * 1.0 / dpi.y * 25.4));
	}

	public static Point getLocation(TextLayout layout, int offset,
			boolean trailing) {
		Point p = layout.getLocation(offset, trailing);
		//int delta = layout.lineY[0];
		//p.y -= delta;
		return p;
	}
	public static Rectangle getBounds(TextLayout layout, int start, int end){
		int indexStart =  layout.getLineIndex(start);
		Rectangle r = layout.getBounds( start,end);
		int delta = layout.lineY[0];
		if( indexStart == 0){
			r.y -= delta;	
			//if(indexEnd == 0){
			r.height +=delta;
			//}
		}
		return r;
	}
	
	public static Rectangle getLineBounds(TextLayout layout, int index){
		Rectangle r = layout.getLineBounds(index);
		if ( index == 0){
			r.y = 0;
			r.height +=layout.lineY[index];
		}
		return r;
	}
}
