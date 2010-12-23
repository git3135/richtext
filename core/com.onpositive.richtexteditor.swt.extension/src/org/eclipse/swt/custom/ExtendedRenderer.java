package org.eclipse.swt.custom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedRenderer.ExtendedLineInfo;
import org.eclipse.swt.custom.StyledTextRenderer.LineInfo;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextLayoutOps;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

/**
 * StyledTextRenderer, which supports some new features, like right line margin
 * and line spacing Should be used with {@link ExtendedStyledText} class; There
 * are left margin/line indent support and wrappers for methods supporting right
 * margin and line spacing;
 * 
 * @author Dmitry "32kda" Karpenko (c) OnPositive, made in USSR
 */
public class ExtendedRenderer extends StyledTextRenderer {

	static final int SPACING = 1 << 5;
	protected ArrayList<Integer> rightLineIndents;
	protected int defaultLineSpacing;

	static class ExtendedLineInfo extends LineInfo {

		int lineSpacing;

		public ExtendedLineInfo() {
		}

		public ExtendedLineInfo(LineInfo info) {
			super(info);
			if (info instanceof ExtendedLineInfo)
				lineSpacing = ((ExtendedLineInfo) info).lineSpacing;
		}

	}

	public ExtendedRenderer(Device device, StyledText styledText) {
		super(device, styledText);
		if (styledText instanceof ExtendedStyledText) {
			;
		}
		setContent(styledText.getContent());
	}

	public int getLineSpacing(int index) {

		if (lines == null || lines[index] == null)
			return defaultLineSpacing;
		if (lines[index] instanceof ExtendedLineInfo) {
			ExtendedLineInfo info = (ExtendedLineInfo) lines[index];
			if (info != null && (info.flags & SPACING) != 0) {
				return ((ExtendedLineInfo) info).lineSpacing;
			}
		} else {
			System.err.println(lines[index] + " at " + index
					+ " not an instance of ExtendedLineInfo!");
			return defaultLineSpacing;
		}
		return defaultLineSpacing;
	}

	/**
	 * Sets the line spacing for logical (non-wrapped) lines from startLine to
	 * startLine + count - 1 Normally should be called only from wrapper
	 * {@link ExtendedStyledText}.setLineSpacing
	 * 
	 * @param startLine
	 *            first line to set spacing to
	 * @param count
	 *            line count
	 * @param spacing
	 *            spacing in pixels to set
	 */
	public void setLineSpacing(int startLine, int count, int spacing) {
		if (lines == null)
			lines = new LineInfo[lineCount];
		for (int i = startLine; i < startLine + count; i++) {
			if (lines[i] == null) {
				lines[i] = new ExtendedLineInfo();
			}
			lines[i].flags |= SPACING;
			((ExtendedLineInfo) lines[i]).lineSpacing = spacing;
		}

	}

	/**
	 * Sets the right line indent for logical (non-wrapped) lines from startLine
	 * to startLine + count - 1 Normally should be called only from wrapper
	 * {@link ExtendedStyledText}.putRightIndent(int, int)
	 * 
	 * @param startLine
	 *            first line to set right indent to
	 * @param indent
	 *            indent in pixels to set
	 */
	public void putRightIndent(int line, int indent) {
		if (line == rightLineIndents.size())
			rightLineIndents.add(indent);
		else
			rightLineIndents.set(line, indent);
	}

	public int getRightIndent(int line) {
		if (rightLineIndents==null||rightLineIndents.size() <= line) {
			return 0;
		}
		return rightLineIndents.get(line);
	}

	protected int rightMargin(int line) {

		int k = 0;
		if (rightLineIndents != null) {
			if (line >= rightLineIndents.size()) {
				for (int l = rightLineIndents.size(); l <= line; l++)
					rightLineIndents.add(0);
			}
			int value = rightLineIndents.get(line);
			k += value;
		}
		return k;
	}

	public int getDefaultLineSpacing() {
		return defaultLineSpacing;
	}

	public void setDefaultLineSpacing(int defaultLineSpacing) {
		this.defaultLineSpacing = defaultLineSpacing;
	}

	void copyInto(StyledTextRenderer renderer) {
		if (ranges != null) {
			int[] newRanges = renderer.ranges = new int[styleCount << 1];
			System.arraycopy(ranges, 0, newRanges, 0, newRanges.length);
		}
		if (styles != null) {
			StyleRange[] newStyles = renderer.styles = new StyleRange[styleCount];
			for (int i = 0; i < newStyles.length; i++) {
				newStyles[i] = (StyleRange) styles[i].clone();
			}
			renderer.styleCount = styleCount;
		}
		if (lines != null) {
			LineInfo[] newLines = renderer.lines = new LineInfo[lineCount];
			for (int i = 0; i < newLines.length; i++) {
				newLines[i] = new ExtendedLineInfo(lines[i]);
			}
			renderer.lineCount = lineCount;
		}
		if (renderer instanceof ExtendedRenderer)
			((ExtendedRenderer)renderer).rightLineIndents = new ArrayList<Integer>(rightLineIndents);
	}
	
	
	
	
	int drawLine(int lineIndex, int paintX, int paintY, GC gc,
			Color widgetBackground, Color widgetForeground) {
		TextLayout layout = getTextLayout(lineIndex);
		String line = content.getLine(lineIndex);
		int lineOffset = content.getOffsetAtLine(lineIndex);
		int lineLength = line.length();
		Point selection = styledText.getSelection();
		int selectionStart = selection.x - lineOffset;
		int selectionEnd = selection.y - lineOffset;
		if (styledText.getBlockSelection()) {
			selectionStart = selectionEnd = 0;
		}
		Rectangle client = styledText.getClientArea();  
		Color lineBackground = getLineBackground(lineIndex, null);
		StyledTextEvent event = styledText.getLineBackgroundData(lineOffset, line);
		if (event != null && event.lineBackground != null) lineBackground = event.lineBackground;
		Rectangle bounds = layout.getBounds();
		int height = bounds.height;
		if (lineBackground != null) {
			gc.setBackground(lineBackground);
			gc.fillRectangle(client.x, paintY, client.width, height);
		} else {
			gc.setBackground(widgetBackground);
			styledText.drawBackground(gc, client.x, paintY, client.width, height);
		}
		ILineBackgroundPainter lineBackgroundPainter = ((ExtendedStyledText)styledText).lineBackgroundPainter;
		if (lineBackgroundPainter!=null){
			lineBackgroundPainter.paintLineBackground(lineIndex, lineOffset, line, layout, gc, new Rectangle(paintX,paintY,client.width,height));
		}
		gc.setForeground(widgetForeground);
		if (selectionStart == selectionEnd || (selectionEnd <= 0 && selectionStart > lineLength - 1)) {
			layout.draw(gc, paintX, paintY);
		} else {
			int start = Math.max(0, selectionStart);
			int end = Math.min(lineLength, selectionEnd);
			Color selectionFg = styledText.getSelectionForeground();
			Color selectionBg = styledText.getSelectionBackground();
			int flags;
			if ((styledText.getStyle() & SWT.FULL_SELECTION) != 0) {
				flags = SWT.FULL_SELECTION;
			} else {
				flags = SWT.DELIMITER_SELECTION;
			}
			if (selectionStart <= lineLength && lineLength < selectionEnd ) {
				flags |= SWT.LAST_LINE_SELECTION;
			}
			layout.draw(gc, paintX, paintY, start, end - 1, selectionFg, selectionBg, flags);
		}
		
		// draw objects
		Bullet bullet = null;
		int bulletIndex = -1;
		if (bullets != null) {
			if (bulletsIndices != null) {
				int index = lineIndex - topIndex;
				if (0 <= index && index < CACHE_SIZE) {
					bullet = bullets[index];
					bulletIndex = bulletsIndices[index];
				}
			} else {
				for (int i = 0; i < bullets.length; i++) {
					bullet = bullets[i];
					bulletIndex = bullet.indexOf(lineIndex);
					if (bulletIndex != -1) break;
				}
			}
		}
		if (bulletIndex != -1 && bullet != null) {
			FontMetrics metrics = layout.getLineMetrics(0);
			int lineAscent = metrics.getAscent() + metrics.getLeading();
			if (bullet.type == ST.BULLET_CUSTOM) {
				bullet.style.start = lineOffset;
				paintX += 5;
				if (lineIndex==0||styledText.getLineBullet(lineIndex-1)==null){
					paintY+=15;
				}
				styledText.paintObject(gc, paintX, paintY, lineAscent, metrics.getDescent(), bullet.style, bullet, bulletIndex);
			} else {
				drawBullet(bullet, gc, paintX, paintY, bulletIndex, lineAscent, metrics.getDescent(),lineIndex);
			}
		}
		TextStyle[] styles = layout.getStyles();
		int[] ranges = null;
		for (int i = 0; i < styles.length; i++) {
			if (styles[i].metrics != null) {
				if (ranges == null) ranges = layout.getRanges();
				int start = ranges[i << 1];
				int length = ranges[(i << 1) + 1] - start;
				Point point = TextLayoutOps.getLocation(layout,start,false);// layout.getLocation(start, false);
				FontMetrics metrics = layout.getLineMetrics(layout.getLineIndex(start));
				StyleRange style = (StyleRange)((StyleRange)styles[i]).clone();
				style.start = start + lineOffset;
				style.length = length;
				int lineAscent = metrics.getAscent() + metrics.getLeading();
				styledText.paintObject(gc, point.x + paintX, point.y + paintY, lineAscent, metrics.getDescent(), style, null, 0);
			}
		}
		disposeTextLayout(layout);
		return height;		
	}

	protected TextLayout getTextLayout(int lineIndex) {
		int width = -1;
		int alignment = 0;
		StyledTextContent content2 = styledText.getContent();
		width = getWrapWidth(lineIndex, content2);
		if (content2 instanceof IMarginedStyledTextContent) {
            IMarginedStyledTextContent mc = (IMarginedStyledTextContent) content2;
		    alignment = mc.getParagraphAlignment(lineIndex);
		}
		if (lineHeight == null) {
			layouts = null;
		}
		
		TextLayout textLayout = super.getTextLayout(lineIndex, styledText
				.getOrientation(), width, getLineSpacing(lineIndex));
		if (getLineBullet(lineIndex, null) != null) //We need to "deshift" first line for bullet space to make pargraph more pretty look
            textLayout.setIndent(textLayout.getIndent() - ExtendedStyledText.BULLET_SPACE);
	    textLayout.setAlignment(alignment);
		return textLayout;
	}

	protected void drawBullet(Bullet bullet, GC gc, int paintX, int paintY, int index, int lineAscent, int lineDescent, int lineIndex)
	{
		paintX -= ExtendedStyledText.BULLET_SPACE;
		super.drawBullet(bullet, gc, paintX, paintY, index-1, lineAscent, lineDescent);
	}

	protected int getWrapWidth(int lineIndex, StyledTextContent content2) {
		int width;
		if (content2 instanceof IMarginedStyledTextContent) {

			IMarginedStyledTextContent mc = (IMarginedStyledTextContent) content2;
			int wrapAndMarginDelta = mc.getWrapWidthAt(lineIndex)
					- mc.getMarginAt(lineIndex);
			width = wrapAndMarginDelta;
					//* ((ExtendedStyledText) styledText).averageCharWidth; TODO
		} else {
			int pageWidth2 = getPageWidth();
			if (pageWidth2 != -1) {
				width = pageWidth2 - rightMargin(lineIndex);
				if (width <= 0) {
					width = 1;

				}
			} else {
				width = -1;
				if (styledText.wordWrap) {
					width = ((ExtendedStyledText) styledText)
							.getWrapWidth(lineIndex);
				}
			}
		}
		return width;
	}

	protected int getPageWidth() {
		return ((ExtendedStyledText) styledText).getPageWidth();
	}

	// We override all this setSomethingToLine only because we should create
	// ExtendedLineInfo instead of LineInfo

	void setLineAlignment(int startLine, int count, int alignment) {
		if (lines == null)
			lines = new LineInfo[lineCount];
		for (int i = startLine; i < startLine + count; i++) {
			if (lines[i] == null) {
				lines[i] = new ExtendedLineInfo();
			}
			lines[i].flags |= ALIGNMENT;
			lines[i].alignment = alignment;
		}
	}

	void setLineBackground(int startLine, int count, Color background) {
		if (lines == null)
			lines = new LineInfo[lineCount];
		for (int i = startLine; i < startLine + count; i++) {
			if (lines[i] == null) {
				lines[i] = new ExtendedLineInfo();
			}
			lines[i].flags |= BACKGROUND;
			lines[i].background = background;
		}
	}

	void setLineIndent(int startLine, int count, int indent) {
		if (lines == null)
			lines = new LineInfo[lineCount];
		for (int i = startLine; i < startLine + count; i++) {
			if (lines[i] == null) {
				lines[i] = new ExtendedLineInfo();
			}
			lines[i].flags |= INDENT;
			lines[i].indent = indent;
		}
	}

	void setLineJustify(int startLine, int count, boolean justify) {
		if (lines == null)
			lines = new LineInfo[lineCount];
		for (int i = startLine; i < startLine + count; i++) {
			if (lines[i] == null) {
				lines[i] = new ExtendedLineInfo();
			}
			lines[i].flags |= JUSTIFY;
			lines[i].justify = justify;
		}
	}

	void setLineSegments(int startLine, int count, int[] segments) {
		if (lines == null)
			lines = new LineInfo[lineCount];
		for (int i = startLine; i < startLine + count; i++) {
			if (lines[i] == null) {
				lines[i] = new ExtendedLineInfo();
			}
			lines[i].flags |= SEGMENTS;
			lines[i].segments = segments;
		}
	}

	public LineInfo[] createLineInfoArray(int size) {
		return new ExtendedLineInfo[size];

	}

	public void textChanged(int firstLineIdx, int delta) {
		if (delta > 0) {
			int firstRightIndent = 0;
			if (rightLineIndents.size() > firstLineIdx)
				firstRightIndent = rightLineIndents.get(firstLineIdx);
			if (rightLineIndents.size() < firstLineIdx + 1)
				fillWithDefaultValue(rightLineIndents, firstLineIdx,
						firstRightIndent);
			int unfilled = firstLineIdx + 1 - rightLineIndents.size();
			for (int i = 0; i < unfilled; i++)
				rightLineIndents.add(0);
			for (int i = Math.min(firstLineIdx + 1, rightLineIndents.size()); i < firstLineIdx + 1 + delta; i++) {
				rightLineIndents.add(firstLineIdx + 1, firstRightIndent);
			}
		} else if (delta < 0) {
			if (rightLineIndents.size() > firstLineIdx + (-delta)) {
				for (int i = firstLineIdx + 1; i < firstLineIdx + 1 + (-delta); i++) {
					rightLineIndents.remove(firstLineIdx + 1);
				}
			}
		}
	}

	protected void fillWithDefaultValue(List<Integer> list, int value,
			int neededCount) {
		for (int i = list.size(); i <= neededCount; i++) {
			list.add(value);
		}
	}

	void setContent(StyledTextContent content) {
		reset();
		this.content = content;
		lineCount = content.getLineCount();
		lineWidth = new int[lineCount];
		lineHeight = new int[lineCount];
		rightLineIndents = new ArrayList<Integer>();
		reset(0, lineCount);
	}
	
	void setFont(Font font, int tabs) {
		TextLayout layout = new TextLayout(device);
		if (regularFont != null && !regularFont.isDisposed()) 
			layout.setFont(regularFont);
		if (font != null) {
			if (boldFont != null) boldFont.dispose();
			if (italicFont != null) italicFont.dispose();
			if (boldItalicFont != null) boldItalicFont.dispose();
			boldFont = italicFont = boldItalicFont = null;
			regularFont = font;
			layout.setText("    ");
			layout.setFont(font);
			layout.setStyle(new TextStyle(getFont(SWT.NORMAL), null, null), 0, 0);
			layout.setStyle(new TextStyle(getFont(SWT.BOLD), null, null), 1, 1);
			layout.setStyle(new TextStyle(getFont(SWT.ITALIC), null, null), 2, 2);
			layout.setStyle(new TextStyle(getFont(SWT.BOLD | SWT.ITALIC), null, null), 3, 3);
			FontMetrics metrics = layout.getLineMetrics(0);
			ascent = metrics.getAscent() + metrics.getLeading();
			descent = metrics.getDescent();
			boldFont.dispose();
			italicFont.dispose();
			boldItalicFont.dispose();
			boldFont = italicFont = boldItalicFont = null;
		}
		layout.dispose();
		layout = new TextLayout(device);
		layout.setFont(regularFont);
		StringBuffer tabBuffer = new StringBuffer(tabs);
		for (int i = 0; i < tabs; i++) {
			tabBuffer.append(' ');
		}
		layout.setText(tabBuffer.toString());
		tabWidth = layout.getBounds().width;
		layout.dispose();
		if (styledText != null) {
		    Display.getDefault().syncExec(new Runnable() {
		       public void run() {
		           GC gc = new GC(styledText);
		           averageCharWidth = gc.getFontMetrics().getAverageCharWidth();
		           fixedPitch = gc.stringExtent("l").x == gc.stringExtent("W").x; //$NON-NLS-1$ //$NON-NLS-2$
		           gc.dispose();
		       }
		    });
		}
	}

	public void bulkSet(int[] aligns, int[] indents, Bullet[] bl) {
		if (lines == null)
			lines = new LineInfo[lineCount];
		bullets =null;
		for (int a=0;a<aligns.length;a++){
			
			if (lines[a]==null){
				lines[a]=new ExtendedLineInfo();
			}
			lines[a].flags |= ALIGNMENT;
			lines[a].alignment = aligns[a];

//			lines[a].flags |= INDENT;
//			lines[a].indent = indents[a];
			
			if (bulletsIndices != null) {
				bulletsIndices = null;
				
			}
			
			if (bullets == null) {
				if (bl[a] == null) continue;
				bullets = new Bullet[1];
				bullets[0] = bl[a];
			}
			int index = 0;
			Bullet bullet = bl[a];
			if (bullet==null&&bullets!=null){
				//here
			}
			while (index < bullets.length) {
				
				if (bullet == bullets[index]) break;
				index++;
			}
			if (bullet != null) {
				if (index == bullets.length) {
					Bullet[] newBulletsList = new Bullet[bullets.length + 1];
					System.arraycopy(bullets, 0, newBulletsList, 0, bullets.length);
					newBulletsList[index] = bullet;
					bullets = newBulletsList;
				}
				bullet.addIndices(a, 1);
			} else {
				for (int i = 0; i < bullets.length; i++) {
					Bullet bll = bullets[i];
					int[] lines = bll.removeIndices(a, 1, 0,false);
					if (lines != null) {
						if (redrawLines == null) {
							redrawLines = lines;
						} else {
							int[] newRedrawBullets = new int[redrawLines.length + lines.length];
							System.arraycopy(redrawLines, 0, newRedrawBullets, 0, redrawLines.length);
							System.arraycopy(lines, 0, newRedrawBullets, redrawLines.length, lines.length);
							redrawLines = newRedrawBullets;
						}
					}
				}
				int removed = 0;
				for (int i = 0; i < bullets.length; i++) {
					if (bullets[i].size() == 0) removed++;
				}
				if (removed > 0) {
					if (removed == bullets.length) {
						bullets = null;
					} else {
						Bullet[] newBulletsList = new Bullet[bullets.length - removed];
						for (int i = 0, j = 0; i < bullets.length; i++) {
							Bullet bll = bullets[i];
							if (bll.size() > 0) newBulletsList[j++] = bll;
						}
						bullets = newBulletsList;
					}
				}							
				redrawLines = null;
			}
		}	
	}

}
