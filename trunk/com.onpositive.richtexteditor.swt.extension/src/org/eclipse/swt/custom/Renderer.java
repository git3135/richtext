/*******************************************************************************
 * Copyright (c) 2007, 2008 OnPositive Technologies (http://www.onpositive.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     OnPositive Technologies (http://www.onpositive.com/) - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.WeakHashMap;

import javax.sound.sampled.LineListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledTextRenderer.LineInfo;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextLayoutOps;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.IME;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;

/**
 * This Renderer implements experimental support
 * 
 * @author kor, 32kda
 * 
 */
public class Renderer extends ExtendedRenderer {

	protected static final String CAPITALIZE_STR = "CAPITALIZE";

	static final int PAGE_NUMBER_OFFSET = 5;

	public static final int PAGE_BREAK_BACKSIZE = 10;

	public static final int PAGE_BREAK_SIZE = 10;

	protected int linesPerPage = 10;
	
	/**
	 * Do we need to draw pagw breaks? In case of printing renderer, we don't
	 * need to do this
	 */
	protected boolean visualizePageBreaks = true;

	protected int pageHeight = 180;
	protected int pageLineCount = 10;
	protected int pageBreakSize = PAGE_BREAK_SIZE;
	private int lastLinePosition = -1;
	// protected int breakSize = 100;

	protected int[] lineY;
	protected int[] originalLineHeight;
	protected int[] lineCounts;
	protected int[] physLineWithBreaks;
	protected int[] pageLineCounts;
	protected int[] pageFirstLineOffsets;

	protected WeakHashMap<TextLayout, Integer> scaledLayouts = new WeakHashMap<TextLayout, Integer>();
	protected IdentityHashMap<Font, Font> scaledFonts = new IdentityHashMap<Font, Font>();
	protected IdentityHashMap<Font, Font> originalFonts = new IdentityHashMap<Font, Font>();
	protected IPageInformationRenderer pageInformationRenderer;

	protected WeakHashMap<TextLayout, Integer> firstLineShifts;
	protected WeakHashMap<TextLayout, Integer> firsltLinesBreaks;
	protected WeakHashMap<TextLayout, Integer> paragraphSpacingOfFirstLine;

	protected ExtendedStyledText extendedStyledText;

	/**
	 * y which is exactly calculated right, without -1's before
	 */
	protected int lastAdequateY = -1;

	/**
	 * Spacing between logical lines(paragraphs)
	 */
	protected int paragraphSpacing = 0;
	protected boolean pagingEnabled = false;
	protected boolean takeParagraphSpacingInAccount = true;
	private int lastLineWithBreak = -1;

	public Renderer(Device device, StyledText styledText) {
		super(device, styledText);

		defaultLineSpacing = styledText.getLineSpacing();
		rightLineIndents = new ArrayList<Integer>();
		firstLineShifts = new WeakHashMap<TextLayout, Integer>();
		firsltLinesBreaks = new WeakHashMap<TextLayout, Integer>();
		paragraphSpacingOfFirstLine = new WeakHashMap<TextLayout, Integer>();
		extendedStyledText = (ExtendedStyledText) styledText;
		createPageInformationRenderer();
	}

	protected void createPageInformationRenderer() {
		pageInformationRenderer = new PageInformationRenderer(this);
	}

	int getLineY(int lineIndex) {
		int firstInadequate = checkActualY(lineIndex);
		if (firstInadequate != -1)
			calculate(firstInadequate, lineIndex - firstInadequate + 1);
		return lineY[lineIndex];
	}

	/**
	 * Returns -1, if all y-array before lineIndex is actual Returns first index
	 * of line with non-actual y, if any
	 * 
	 * @param lineIndex
	 *            - index of line to check y up to
	 * @return see before
	 */
	protected int checkActualY(int lineIndex) {
		if (lineIndex <= lastAdequateY)
			return -1;
		for (int i = 0; i < lineIndex; i++)
			if (lineY[i] == -1 || (i > 0 && lineY[i] <= lineY[i - 1]))
				return i;

		return -1;
	}

	int getLineCount(int lineIndex) {
		int firstInadequate = checkActualCount(lineIndex);
		if (firstInadequate != -1)
			calculateCounts(firstInadequate, lineIndex - firstInadequate + 1);
		return lineCounts[lineIndex];
	}

	int getLineCountWithParagraphSpacing(int lineIndex) {
		int firstInadequate = checkActualCount(lineIndex);
		if (firstInadequate != -1)
			calculateCounts(firstInadequate, lineIndex - firstInadequate + 1);
		return pageLineCounts[lineIndex];
	}

	/**
	 * Returns -1, if all y-array before lineIndex is actual Returns first index
	 * of line with non-actual y, if any
	 * 
	 * @param lineIndex
	 *            - index of line to check y up to
	 * @return see before ^
	 */
	protected int checkActualCount(int lineIndex) {
		/*
		 * if (lineIndex <= lastAdequateY) return -1;
		 */
		for (int i = 0; i <= lineIndex; i++)
			if (lineCounts[i] == -1)
				return i;

		return -1;
	}

	public int getScaledLineHeight() {
		return (int) Math.round(getLineHeight() * getScalingFactor());
	}

	int getLineHeight(int lineIndex) {
		if (lineHeight == null) {
			return 0;
		}
		
		if (lineHeight[lineIndex] == -1) {
			calculate(lineIndex, 1);
		}
		return lineHeight[lineIndex];
	}

	protected int rightMargin(int line) {
		return (int) Math.round(super.rightMargin(line) * getScalingFactor());
	}

	protected int getPageWidth() {
		double a = super.getPageWidth()
				* ((ExtendedStyledText) styledText).scalingFactor;
		return (int) Math.round(a);
	}

	public void changeScale(double newScale) {
		if (lineY != null) {
			lineY[0] = 0;
			for (int i = 0; i < originalLineHeight.length; i++) {
				if (originalLineHeight[i] != -1) {
					lineHeight[i] = (int) Math.round(originalLineHeight[i]
							* newScale);
				}
				if (i > 0 && lineY[i - 1] > -1) {
					lineY[i] = lineY[i - 1] + lineHeight[i];
				}
			}
		}
		disposeRegistries();
	}

	public void setPagingEnabled(boolean pagingEnabled) {
		if (pagingEnabled != this.pagingEnabled) {
			this.pagingEnabled = pagingEnabled;
			reset();

		}
	}

	protected void disposeRegistries() {
		disposeFonts();
		scaledFonts.clear();

		firsltLinesBreaks.clear();
		firstLineShifts.clear();
		paragraphSpacingOfFirstLine.clear();

		scaledLayouts.clear();
	}

	/**
	 * Scales a font by styledText's scaling factor, by creating and returning a
	 * new font. Such scaled fonts are stored into special registry
	 * 
	 * @param font
	 *            Font to scale
	 * @return Scaled font
	 */
	public Font scaleFont(Font font) {
		if (font == null)
			return null;
		Font scaled = scaledFonts.get(font);
		if (scaled != null)
			return scaled;
		FontData fd = font.getFontData()[0];
		double scalingFactor = getScalingFactor();
		FontData fd2 = new FontData(fd.getName(),
				(int) (fd.getHeight() * scalingFactor), fd.getStyle());
		fd2.height = (int) (fd.getHeight() * scalingFactor);
		scaled = new Font(styledText.getDisplay(), fd2);
		scaledFonts.put(font, scaled);
		originalFonts.put(scaled, font);
		return scaled;
	}

	public TextLayout getTextLayout(int lineIndex) {
		// int lineSpacing = styledText.lineSpacing;
		/*
		 * return super.getTextLayout(lineIndex, styledText.getOrientation(),
		 * ((ExtendedStyledText) styledText).getWrapWidth(lineIndex),
		 * lineSpacing);
		 */
		TextLayout textLayout = super.getTextLayout(lineIndex);
		StyledTextContent content2 = styledText.getContent();
		/*
		 * if (content2 instanceof IMarginedStyledTextContent) {
		 * textLayout.setSpacing((int) Math.round(((IMarginedStyledTextContent)
		 * content2).getLineSpacing(lineIndex) *
		 * textLayout.getFont().getFontData()[0].height)); }
		 */
		if (getScalingFactor() != 1.0) {
			scaleLayoutIfNecessary(lineIndex, textLayout);
		} else if (originalLineHeight != null) {
			originalLineHeight[lineIndex] = textLayout.getBounds().height;
		}
		checkStyles(textLayout);
		if (pagingEnabled
				|| ((ExtendedStyledText) styledText).isAllowParagraphSpacing()) {
			checkPaging(lineIndex, textLayout);
		} else {
			textLayout.getBounds();
		}
		return textLayout;
	}

	protected void checkStyles(TextLayout textLayout) {
		String text = textLayout.getText();
		int[] segments2 = textLayout.getRanges();
		TextStyle[] styles2 = textLayout.getStyles();
		for (int i = 0; i < styles2.length; i++) {
			if (styles2[i].data != null
					&& styles2[i].data.equals(CAPITALIZE_STR)) {

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
		for (int i = 0; i < styles2.length; i++) {
			textLayout.setStyle(styles2[i], segments2[i * 2],
					segments2[i * 2 + 1]);
		}
	}

	StyleRange getStyleRange(StyleRange style) {
		if (style.underline && style.underlineStyle == SWT.UNDERLINE_LINK)
			hasLinks = true;
		if (style.start == 0 && style.length == 0
				&& style.fontStyle == SWT.NORMAL)
			return style;
		StyleRange clone = (StyleRange) style.clone();
		clone.start = clone.length = 0;
		// clone.fontStyle = SWT.NORMAL; TODO
		if (clone.font == null)
			clone.font = getFont(style.fontStyle);
		return clone;
	}

	private void scaleLayoutIfNecessary(int lineIndex, TextLayout textLayout) {
		Integer oldAscent = scaledLayouts.get(textLayout);
		double scalingFactor = getScalingFactor();
		if (oldAscent == null || !oldAscent.equals(textLayout.getAscent())) {
			if (originalLineHeight != null) {
				originalLineHeight[lineIndex] = textLayout.getBounds().height;
			}
			boolean needCheck = ((double) (int) scalingFactor) != scalingFactor;
			int[] lineOffsets = needCheck ? textLayout.getLineOffsets() : null;
			TextStyle[] textStyles = textLayout.getStyles();
			for (int i = 0; i < textStyles.length; i++) {
				if (textStyles[i].font != null
						&& textStyles[i].font.isDisposed())
					textStyles[i].font = originalFonts.get(textStyles[i].font);
				textStyles[i].font = scaleFont(textStyles[i].font);
			}
			Font font = textLayout.getFont();
			if (font.isDisposed())
				font = originalFonts.get(font);
			Font scaleFont = scaleFont(font);
			textLayout.setFont(scaleFont);
			textLayout.setSpacing((int) Math.round(textLayout.getSpacing()
					* scalingFactor));
			int ascent2 = (int) Math.round(textLayout.getAscent()
					* scalingFactor);
			textLayout.setAscent(ascent2);

			int descent2 = (int) Math.round(textLayout.getDescent()
					* scalingFactor);
			textLayout.setDescent(descent2);
			textLayout
					.setIndent((int) (textLayout.getIndent() * scalingFactor));
			textLayout.setWidth((int) (textLayout.getWidth() * scalingFactor));
			if (needCheck) {
				safeCheckScaling(textLayout, lineOffsets, scalingFactor);
			}
			scaledLayouts.put(textLayout, ascent2);
		}
	}

	protected double getScalingFactor() {
		return ((ExtendedStyledText) styledText).getScalingFactor();
	}

	private void safeCheckScaling(TextLayout textLayout, int[] lineOffsets,
			double scalingFactor) {
		int[] lineOffsets2 = textLayout.getLineOffsets();
		textLayout.setAlignment(SWT.LEFT);
		textLayout.setJustify(false);
		if (!Arrays.equals(lineOffsets, lineOffsets2)) {
			// determine should we increase or decrease wrap width
			int max = 0;
			int[] widths = new int[lineOffsets.length - 1];
			for (int b = 0; b < lineOffsets.length - 1; b++) {
				TextLayout createLayoutForVisibleLine = createLayoutForVisibleLine(
						textLayout, b, lineOffsets);
				widths[b] = createLayoutForVisibleLine.getBounds().width;
				max = Math.max(max, widths[b]);
				createLayoutForVisibleLine.dispose();
			}

			textLayout.setWidth(max + 1);
			StringBuilder mm = new StringBuilder(textLayout.getText());
			for (int b = 0; b < lineOffsets.length - 1; b++) {
				if (widths[b] < max) {
					// style.metrics=new GlyphMetrics(10, 10, max-widths[b]);
					int index = lineOffsets[b + 1] - 1;
					if (index > mm.length()) {
						char c = mm.charAt(index);
						if (Character.isWhitespace(c)) {
							mm.setCharAt(index, '\n');
						}
					}
				}
			}
			textLayout.setText(mm.toString());
			lineOffsets2 = textLayout.getLineOffsets();
			if (!Arrays.equals(lineOffsets, lineOffsets2)) {
				if (lineOffsets2.length != lineOffsets.length) {
					System.err.println("Was not able fully  maintain wrapping");
				}
			}

		}

	}

	protected void checkPaging(int lineIndex, TextLayout textLayout) {
		int prevTotalVisibleLineCount = getPrevLineCountForPaging(lineIndex);
		int linesInLayout = textLayout.getLineCount();
		int scaledBreakSize = getScaledBreakSize();
		double scalingFactor = getScalingFactor();		
		for (int a = 0; a < linesInLayout; a++) {
			TextLayout localLayout = adjustPagingForVisibleLine(lineIndex,
					textLayout, prevTotalVisibleLineCount, linesInLayout,
					scaledBreakSize, scalingFactor, a);

			firsltLinesBreaks.remove(localLayout);
			firstLineShifts.remove(localLayout);
			paragraphSpacingOfFirstLine.remove(localLayout);

			localLayout.dispose();
		}
	}

	protected TextLayout adjustPagingForVisibleLine(int logicalLineIndex,
			TextLayout textLayout, int prevTotalVisibleLineCount,
			int linesInLayout, int scaledBreakSize, double scalingFactor,
			int lineInLayout) {

		boolean isFirstLineInLayout = lineInLayout == 0 && pagingEnabled;
		boolean flgg = isLastLineInText(logicalLineIndex, linesInLayout,
				lineInLayout);
		TextLayout localLayout = createLayoutForVisibleLine(textLayout,
				lineInLayout);
		int practicalHeight = getPracticalLayoutHeight(localLayout);
		int subLine = -1;

		int actualHeight = TextLayoutOps
				.getLineHeight(textLayout, lineInLayout);

		// int prevHeight = lineInLayout == 0 ? 0 : TextLayoutOps.getLineHeight(
		// textLayout, lineInLayout - 1);
		// int prevLinesInsideLayout = prevHeight
		// / (int) (getLineHeight() * scalingFactor);

		// int reverseToSpacing = logicalLineIndex == 0 ? 0
		// : getParagraphSpacingInSymbols(logicalLineIndex);
		int i = lineInLayout + prevTotalVisibleLineCount;// - reverseToSpacing;

		boolean needBreak = false;

		int loopBegin = 0;
		int loopEnd = 0;
		int numOfLines = 0;
		// if (TextLayoutOps.getExtra() == 0) {
		//
		// int paragraphSpacingInSymbols =
		// getParagraphSpacingInSymbols(logicalLineIndex);
		//
		// int prev = prevTotalVisibleLineCount - paragraphSpacingInSymbols;
		// loopBegin = prev;
		// loopEnd = i;
		// numOfLines = i;
		//
		// } else {

		if (lineInLayout == 0 && logicalLineIndex != 0) {
			loopBegin = prevTotalVisibleLineCount
					- getParagraphSpacingInSymbols(logicalLineIndex) + 1;
			loopEnd = i + 1;

			numOfLines = i + 1;
		} else {
			loopBegin = i + 1;
			loopEnd = i + /*- */1;
			numOfLines = i + 1;

			if (logicalLineIndex == 0) {
				numOfLines = loopBegin = loopEnd += getParagraphSpacingInSymbols(logicalLineIndex);
			}
		}

		// }

		if (lineInLayout != 0 || !takeParagraphSpacingInAccount) {
			needBreak = numOfLines != 0 && numOfLines % linesPerPage == 0;
			subLine = numOfLines;
		} else {
			for (int a = loopBegin; a <= loopEnd; a++) {
				if (a != 0 && (a) % linesPerPage == 0) {
					subLine = a;
					needBreak = true;
				}
			}
		}

		// TODO
		// lastLineInText = false;
		boolean isBreakInsideSpacing = subLine - loopBegin < getParagraphSpacingInSymbols(logicalLineIndex)
				&& subLine >= 0;
		boolean pageBreakInsideSpacing = isBreakInsideSpacing && needBreak
				&& pagingEnabled;
		int paragraphSpacingInSymbols = getParagraphSpacingInSymbols(logicalLineIndex);
		int paragraphSpacing = -1;
		int delta = scaledBreakSize;
		if (pagingEnabled && needBreak) {
			int currLastBreak = subLine - loopBegin;
			if (actualHeight <= practicalHeight || actualHeight == 0) {
				if (isFirstLineInLayout && paragraphSpacingInSymbols != 0) {
					checkParagraphSpacing(logicalLineIndex, textLayout,
							prevTotalVisibleLineCount, lineInLayout,
							localLayout, practicalHeight, actualHeight,
							scaledBreakSize, i, pageBreakInsideSpacing);

					if (!isBreakInsideSpacing) {
						shiftLines(textLayout, lineInLayout, delta);
					}
				} else {
					shiftLines(textLayout, lineInLayout, delta);
				}
				// int pageWithBreak = logicalLineIndex == 0 ? currLastBreak
				// : pageLineCounts[logicalLineIndex - 1] + currLastBreak;
				// physLineWithBreaks[pageWithBreak] = 1;
				physLineWithBreaks[logicalLineIndex] = currLastBreak;
				firsltLinesBreaks.put(textLayout, currLastBreak);
				paragraphSpacingOfFirstLine.put(textLayout,
						paragraphSpacingInSymbols);
			} else if (isFirstLineInLayout && paragraphSpacingInSymbols != 0) {
				int spacing = (int) (paragraphSpacingInSymbols
						* getLineHeight() * scalingFactor);
				int theoreticalHeight = spacing + scaledBreakSize;
				if (actualHeight < theoreticalHeight) {
					// we should insert break
					if (currLastBreak < paragraphSpacingInSymbols) {
						if (TextLayoutOps.getExtra() != 0) {
							shiftLines(textLayout, lineInLayout, -actualHeight
									+ practicalHeight);
							TextLayoutOps.setFirstLineSpacing(textLayout,
									localLayout, theoreticalHeight);// (int)
							firstLineShifts.put(textLayout, theoreticalHeight);
						} else {
							TextLayoutOps.setFirstLineSpacing(textLayout,
									localLayout, theoreticalHeight
											- actualHeight + practicalHeight);// (int)
							firstLineShifts.put(textLayout, theoreticalHeight);
						}
					} else {
						shiftLines(textLayout, lineInLayout, delta);
					}
					firsltLinesBreaks.put(textLayout, currLastBreak);
					physLineWithBreaks[logicalLineIndex] = currLastBreak;
					if (paragraphSpacingInSymbols != 0) {
						paragraphSpacingOfFirstLine.put(textLayout,
								paragraphSpacingInSymbols);
					}
				} else {
					// maybe we should shift break
					shiftPageBreak(textLayout, scaledBreakSize, scalingFactor,
							lineInLayout, localLayout, currLastBreak,
							logicalLineIndex);
				}
			}
		} else if (isFirstLineInLayout || lineInLayout == 0) {
			// System.out.println("l: " + textLayout.getText());
			boolean flg = checkParagraphSpacing(logicalLineIndex, textLayout,
					prevTotalVisibleLineCount, lineInLayout, localLayout,
					practicalHeight, actualHeight, scaledBreakSize, i,
					pageBreakInsideSpacing);
			paragraphSpacing = flg ? 1 : 0;
			if (flg) {
				paragraphSpacingOfFirstLine.put(textLayout,
						paragraphSpacingInSymbols);
			}
		}

		int changedPpSpacing = paragraphSpacingInSymbols;
		if (paragraphSpacingOfFirstLine.containsKey(textLayout)) {
			changedPpSpacing = paragraphSpacingOfFirstLine.get(textLayout);
		}

		// We should remove incorrect breaks.
		if (!(needBreak && pagingEnabled)
				&& paragraphSpacing <= 0
				&& (actualHeight >= (practicalHeight + paragraphSpacingInSymbols
						* getLineHeight() * scalingFactor)
						+ scaledBreakSize)
				&& !(logicalLineIndex == 0 && lineInLayout == 0)
				&& lineInLayout == 0) {
			int deltta = practicalHeight - actualHeight;
			// if (lineInLayout == 0) {
			// int lastBreak = firsltLinesBreaks.get(textLayout);//
			// physLineWithBreaks[logicalLineIndex];
			int lineShift = -1;
			if (firstLineShifts.containsKey(textLayout)) {
				lineShift = firstLineShifts.get(textLayout);
			}
			if (lineShift > 0) {
				if (TextLayoutOps.getExtra() != 0) {
					int paragraphSpacing2 = getParagraphSpacing(logicalLineIndex);
					shiftLines(textLayout, lineInLayout, deltta);
					if (paragraphSpacing2 != 0) {
						TextLayoutOps.setFirstLineSpacing(textLayout,
								localLayout, paragraphSpacing2);
						firstLineShifts.put(textLayout, paragraphSpacing2);
					}
				} else {
					int paragraphSpacing2 = getParagraphSpacing(logicalLineIndex);
					if (paragraphSpacing2 == lineShift) {
						TextLayoutOps.shiftLines(textLayout, 0, (-1)
								* scaledBreakSize);
					} else {
						TextLayoutOps.setFirstLineSpacing(textLayout,
								localLayout, deltta + paragraphSpacing2);
						if (paragraphSpacing2 != 0) {
							firstLineShifts.put(textLayout, paragraphSpacing2);
						}
					}
				}
			} else if (paragraphSpacingInSymbols == 0) {
				if (TextLayoutOps.getExtra() != 0) {
					shiftLines(textLayout, lineInLayout, deltta);
					firstLineShifts.remove(textLayout);
				} else {
					TextLayoutOps.shiftLines(textLayout, 0, deltta);
					firstLineShifts.remove(textLayout);
				}
			} else {
				if (TextLayoutOps.getExtra() != 0) {
					shiftLines(textLayout, lineInLayout, scaledBreakSize * (-1));
				} else {
					int paragraphSpacing2 = getParagraphSpacing(logicalLineIndex);
					TextLayoutOps.setFirstLineSpacing(textLayout, localLayout,
							deltta);
					if (paragraphSpacing2 != 0) {
						firstLineShifts.put(textLayout, paragraphSpacing2);
					}
				}
			}
			firsltLinesBreaks.remove(textLayout);
			physLineWithBreaks[logicalLineIndex] = -1;
			// } else {
			// shiftLines(textLayout, lineInLayout, deltta);
			// }
			// changeSpacing
		} else if (!(needBreak && pagingEnabled)
				&& paragraphSpacing <= 0
				&& (paragraphSpacingInSymbols != changedPpSpacing)// check
				// paragraphSpacing!!!!
				&& !(logicalLineIndex == 0 && lineInLayout == 0)
				&& lineInLayout == 0) {
			int paragraphSpacing2 = getParagraphSpacing(logicalLineIndex);
			if (TextLayoutOps.getExtra() != 0) {
				shiftLines(
						textLayout,
						lineInLayout,
						(-1)
								* (int) (changedPpSpacing * getLineHeight() * scalingFactor));
				if (paragraphSpacing2 != 0) {
					TextLayoutOps.setFirstLineSpacing(textLayout, localLayout,
							paragraphSpacing2);
					firstLineShifts.put(textLayout, paragraphSpacing2);
				}

				if (paragraphSpacingInSymbols != 0) {
					paragraphSpacingOfFirstLine.put(textLayout,
							paragraphSpacingInSymbols);
				}
			} else {
				int deltah = TextLayoutOps.getLineHeight(textLayout, 0);
				TextLayoutOps.setFirstLineSpacing(textLayout, localLayout,
						-deltah + (int) (getLineHeight() * scalingFactor));
				// deltah = TextLayoutOps.getLineHeight(textLayout,0);
				TextLayoutOps.setFirstLineSpacing(textLayout, localLayout,
						paragraphSpacing2);
				if (paragraphSpacing2 != 0) {
					firstLineShifts.put(textLayout, paragraphSpacing2);
				}
				if (paragraphSpacingInSymbols != 0) {
					paragraphSpacingOfFirstLine.put(textLayout,
							paragraphSpacingInSymbols);
				}
			}
		} else if (!(needBreak && pagingEnabled) && paragraphSpacing <= 0
				&& actualHeight >= (practicalHeight + scaledBreakSize)
				&& lineInLayout != 0) {
			if (TextLayoutOps.getExtra() == 0
					&& actualHeight - practicalHeight >= scaledBreakSize) {
				TextLayoutOps.shiftLines(textLayout, lineInLayout,
						practicalHeight - actualHeight);
			}
		}

		return localLayout;
	}

	private void shiftPageBreak(TextLayout textLayout, int scaledBreakSize,
			double scalingFactor, int lineInLayout, TextLayout localLayout,
			int currLastBreak, int logicalLineIndex) {
		int lastLineWithBreak = firsltLinesBreaks.get(textLayout);// physLineWithBreaks[logicalLineIndex];
		boolean flg = false;

		if (lastLineWithBreak <= -1) {
			return; // flg = true;
		}

		int delta = scaledBreakSize;

		if (currLastBreak != lastLineWithBreak) {
			int paragraphSpacingInSymbols = getParagraphSpacingInSymbols(logicalLineIndex);
			if (currLastBreak == paragraphSpacingInSymbols) {
				if (TextLayoutOps.getExtra() != 0) {
					shiftLines(
							textLayout,
							lineInLayout,
							-(int) (paragraphSpacingInSymbols * getLineHeight() * scalingFactor));
					if (paragraphSpacingInSymbols != 0) {
						TextLayoutOps.setFirstLineSpacing(textLayout,
								localLayout, (int) (paragraphSpacingInSymbols
										* getLineHeight() * scalingFactor));
						firstLineShifts.put(textLayout,
								(int) (paragraphSpacingInSymbols
										* getLineHeight() * scalingFactor));
					}
					// shiftLines(textLayout, lineInLayout,delta);
				} else {
					TextLayoutOps.setFirstLineSpacing(textLayout, localLayout,
							(int) (-1 * paragraphSpacingInSymbols
									* getLineHeight() * scalingFactor - delta));
					if (paragraphSpacingInSymbols != 0) {
						TextLayoutOps.setFirstLineSpacing(textLayout,
								localLayout, (int) (paragraphSpacingInSymbols
										* getLineHeight() * scalingFactor));
						firstLineShifts.put(textLayout,
								(int) (paragraphSpacingInSymbols
										* getLineHeight() * scalingFactor));
					}
					shiftLines(textLayout, lineInLayout, delta);

				}
			} else if (lastLineWithBreak == paragraphSpacingInSymbols || flg) {
				if (TextLayoutOps.getExtra() != 0) {
					shiftLines(textLayout, lineInLayout, -delta
							- (int) (paragraphSpacingInSymbols
									* getLineHeight() * scalingFactor));
					TextLayoutOps.setFirstLineSpacing(textLayout, localLayout,
							(int) (delta + paragraphSpacingInSymbols
									* getLineHeight() * scalingFactor));
					firstLineShifts.put(textLayout,
							(int) (delta + paragraphSpacingInSymbols
									* getLineHeight() * scalingFactor));
				} else {
					shiftLines(textLayout, lineInLayout, -delta);
					TextLayoutOps.setFirstLineSpacing(textLayout, localLayout,
							(int) (delta));
					firstLineShifts.put(textLayout,
							(int) (delta + paragraphSpacingInSymbols
									* getLineHeight() * scalingFactor));
				}
			}
			// int pageWithBreak = logicalLineIndex == 0 ? currLastBreak
			// : pageLineCounts[logicalLineIndex - 1] + currLastBreak;
			// physLineWithBreaks[logicalLineIndex]=
			paragraphSpacingOfFirstLine.put(textLayout,
					paragraphSpacingInSymbols);
			firsltLinesBreaks.put(textLayout,
					currLastBreak >= 0 ? currLastBreak : -1);

		}
	}

	protected void shiftLines(TextLayout textLayout, int lineInLayout, int delta) {
		Integer prevShift = firstLineShifts.get(textLayout);
		if (prevShift == null)
			prevShift = 0;
		if (lineInLayout == 0) {
			if (TextLayoutOps.getExtra() != 0) {
				firstLineShifts.put(textLayout, delta + prevShift);
			}
		}
		TextLayoutOps.shiftLines(textLayout, lineInLayout, delta);
	}

	public int getFirstLayoutLineShift(TextLayout layout) {
		Integer value = firstLineShifts.get(layout);
		if (value == null)
			return 0;
		return value;
	}

	private boolean isLastLineInText(int logicalLineIndex, int linesInLayout,
			int lineInLayout) {
		return logicalLineIndex == styledText.getLineCount() - 1
				&& lineInLayout == linesInLayout - 1;
	}

	protected int getPrevLineCountForPaging(int lineIndex) {
		int prevIndex = lineIndex - 1;
		int prevTotalVisibleLineCount = prevIndex == -1 ? 0
				: takeParagraphSpacingInAccount ? (getLineCountWithParagraphSpacing(prevIndex) + getParagraphSpacingInSymbols(lineIndex))
						: getLineCount(prevIndex);
		return prevTotalVisibleLineCount;
	}

	protected int getScaledBreakSize() {
		if (extendedStyledText.pageInformation == null) {
			return 0;
		}
		return (int) (extendedStyledText.pageInformation.getBreakSize(
				IBreakSize.BREAK_SIZE_IN_LINES).getValue()
				* getLineHeight() * getScalingFactor()); // (int)
		// Math.round(ExtendedStyledTextConstants.BREAK_SIZE*
		// getScalingFactor());
	}

	private int getPracticalLayoutHeight(TextLayout localLayout) {
		int practicalHeight = localLayout.getBounds().height;
		if (localLayout.getText().length() == 0) {
			practicalHeight += localLayout.getSpacing() / 2;
		}
		return practicalHeight;
	}

	protected boolean isPageBreak(int i, int next) {
		if (this.takeParagraphSpacingInAccount) {
			for (int a = i; a <= next; a++) {
				if (a != 0 && a % linesPerPage == 0) { // a % linesPerPage == 0
					return true;
				}
			}
			return false;
		} else
			return i != 0 && i % linesPerPage == 0;// i % linesPerPage == 0;
	}

	// Returns 'true', if line was shifted due to spacing really, false
	// otherwise
	protected boolean checkParagraphSpacing(int lineIndex,
			TextLayout textLayout, int prevTotalLineCount, int a,
			TextLayout localLayout, int practicalHeight, int height,
			int scaledBreakSize, int i, boolean breakInsideSpacing) {
		if (TextLayoutOps.needsSpacing(textLayout, localLayout, height,
				practicalHeight)
				&& i >= 0) {
			int paragraphSpacing2 = getParagraphSpacing(lineIndex);
			if (a == 0 && prevTotalLineCount == 0 && pagingEnabled) {
				paragraphSpacing2 += scaledBreakSize / 2 + 3 * PAGE_BREAK_SIZE
						/ 2;// / 2 + 100;
			}

			if (breakInsideSpacing) {
				paragraphSpacing2 += scaledBreakSize;
			}

			TextLayoutOps.setFirstLineSpacing(textLayout, localLayout,
					paragraphSpacing2);
			if (paragraphSpacing2 != 0) {
				firstLineShifts.put(textLayout, paragraphSpacing2);
			}
			return true;
		}
		return false;
	}

	int getParagraphSpacingInSymbols(int a) {

		StyledTextContent content2 = styledText.getContent();
		if (content2 instanceof IMarginedStyledTextContent) {
			IMarginedStyledTextContent mc = (IMarginedStyledTextContent) content2;
			return (int) mc.getParagraphSpacing(a);
		}
		return 0;
	}

	protected int getParagraphSpacing(int a) {
		int dlt = 0;
		StyledTextContent content2 = styledText.getContent();
		if (content2 instanceof IMarginedStyledTextContent) {
			IMarginedStyledTextContent mc = (IMarginedStyledTextContent) content2;
			ExtendedStyledText extendedStyledText = (ExtendedStyledText) styledText;
			dlt += (mc.getParagraphSpacing(a)
					* extendedStyledText.averageCharHeight * getScalingFactor());
		}
		return dlt + paragraphSpacing; // 0
	}

	protected void shiftLogicalArrays(int lineIndex, int delta) {
		// if(true && lineHeight[lineIndex] < delta){
		// return;
		// }
		lineHeight[lineIndex] += delta;
		for (int i = lineIndex + 1; i < lineY.length && lineY[i] > -1; i++) {
			lineY[i] += delta;
		}
	}

	protected TextLayout createLayoutForVisibleLine(TextLayout originalLayout,
			int visibleLineIdx) {
		TextLayout localLayout = new TextLayout(styledText.getDisplay());
		int[] lineOffsets = originalLayout.getLineOffsets();

		String contents = originalLayout.getText();
		int start = lineOffsets[visibleLineIdx];
		int end = lineOffsets[visibleLineIdx + 1];
		String lineContents = contents.substring(start, end);
		if (lineContents.length() == 0) {
			lineContents = " ";
		}
		localLayout.setText(lineContents);
		int length = originalLayout.getText().length();
		if (length > 0 && length < start) {

			localLayout
					.setStyle(originalLayout.getStyle(start), 0, end - start);
		}

		localLayout.setAlignment(originalLayout.getAlignment());
		localLayout.setAscent(originalLayout.getAscent());
		localLayout.setDescent(originalLayout.getDescent());
		localLayout.setFont(originalLayout.getFont());
		localLayout.setIndent(originalLayout.getIndent());
		localLayout.setJustify(originalLayout.getJustify());
		localLayout.setOrientation(originalLayout.getOrientation());
		localLayout.setSegments(originalLayout.getSegments());
		localLayout.setSpacing(originalLayout.getSpacing());
		localLayout.setTabs(originalLayout.getTabs());
		return localLayout;
	}

	protected TextLayout createLayoutForVisibleLine(TextLayout originalLayout,
			int visibleLineIdx, int[] lineOffsets) {
		TextLayout localLayout = new TextLayout(styledText.getDisplay());
		String contents = originalLayout.getText();
		int start = lineOffsets[visibleLineIdx];
		int end = lineOffsets[visibleLineIdx + 1];
		String lineContents = contents.substring(start, end);
		if (lineContents.length() == 0) {
			lineContents = " ";
		}
		localLayout.setText(lineContents);
		if (originalLayout.getText().length() > 0) {
			localLayout
					.setStyle(originalLayout.getStyle(start), 0, end - start);
		}

		localLayout.setAlignment(originalLayout.getAlignment());
		localLayout.setAscent(originalLayout.getAscent());
		localLayout.setDescent(originalLayout.getDescent());
		localLayout.setFont(originalLayout.getFont());
		localLayout.setIndent(originalLayout.getIndent());
		localLayout.setJustify(originalLayout.getJustify());
		localLayout.setOrientation(originalLayout.getOrientation());
		localLayout.setSegments(originalLayout.getSegments());
		localLayout.setSpacing(originalLayout.getSpacing());
		localLayout.setTabs(originalLayout.getTabs());
		return localLayout;
	}

	void calculateCounts(int startLine, int lineCount) {
		int endLine = startLine + lineCount;
		for (int i = startLine; i < endLine; i++) {
			TextLayout layout = getTextLayout(i);
			int prevCount = 0;
			int prev = 0;
			if (i > 0) {
				prevCount = lineCounts[i - 1];
				prev = pageLineCounts[i - 1];
			}
			int lineCount2 = layout.getLineCount();
			lineCounts[i] = lineCount2 + prevCount;
			StyledTextContent content2 = styledText.getContent();
			if (content2 instanceof IMarginedStyledTextContent) {
				IMarginedStyledTextContent mc = (IMarginedStyledTextContent) content2;
				pageLineCounts[i] = (int) (prev + mc.getParagraphSpacing(i))
						+ lineCount2;
			} else {
				pageLineCounts[i] = lineCount2 + prev;
			}
			disposeTextLayout(layout);
		}

		for (int i = startLine; i < physLineWithBreaks.length; i++) {
			// physLineWithBreaks[i] = -2;
		}
		/*
		 * int fullLineCount = lineCounts[this.lineCount - 1]; if (fullLineCount
		 * != physLineWithBreaks.length) { int[] newArr = new
		 * int[fullLineCount]; int ll = fullLineCount <
		 * physLineWithBreaks.length ? fullLineCount :
		 * physLineWithBreaks.length; System.arraycopy(physLineWithBreaks, 0,
		 * newArr, 0, ll); if (ll > 0) { for (int i = ll - 1; i < fullLineCount;
		 * i++) { newArr[i] = -1; } }
		 * 
		 * int startLineC = -1; if (startLine == 0) { startLineC = 0; } else {
		 * startLineC = pageLineCounts[startLine - 1]; } for (int i =
		 * startLineC; i < fullLineCount; i++) { newArr[i] = -1; } }
		 */
	}

	void calculate(int startLine, int lineCount) {
		int endLine = startLine + lineCount;
		if (lineWidth == null) {
			return;
		}
		if (startLine < 0 || endLine > lineWidth.length) {
			return;
		}
		int hTrim = ((ExtendedStyledText) styledText).leftMargin(-1)
				+ styledText.rightMargin + styledText.getCaretWidth(); // TODO
		// maybe
		// a
		// cause
		// of
		// incorrect
		// wrapping
		int curLineY = 0;
		if (startLine > 0)
			curLineY = lineY[startLine - 1];
		for (int i = startLine; i < endLine; i++) {
			if (lineWidth[i] == -1 || lineHeight[i] == -1) {

				TextLayout layout = getTextLayout(i);
				lineHeight[i] = TextLayoutOps.getHeight(layout);
				Rectangle rect = layout.getBounds();
				lineWidth[i] = rect.width + hTrim;

				int prevCount = 0;
				int prevPageLineCount = 0;
				if (i > 0) {
					prevCount = lineCounts[i - 1];
					prevPageLineCount = pageLineCounts[i - 1];
				}
				if (prevCount != -1) {
					int lineCount2 = layout.getLineCount();
					lineCounts[i] = lineCount2 + prevCount;
					StyledTextContent content2 = styledText.getContent();
					if (content2 instanceof IMarginedStyledTextContent) {
						IMarginedStyledTextContent mc = (IMarginedStyledTextContent) content2;
						pageLineCounts[i] = (int) (lineCount2
								+ mc.getParagraphSpacing(i) + prevPageLineCount);
					} else {
						pageLineCounts[i] = lineCount2 + prevPageLineCount;
					}
				}
				disposeTextLayout(layout);
			}
			if (lineWidth[i] > maxWidth) {
				maxWidth = lineWidth[i];
				maxWidthLineIndex = i;
			}
			if (i > 0 && curLineY >= 0)
				curLineY = curLineY + lineHeight[i - 1];
			lineY[i] = curLineY;
		}

		/*
		 * int lastLine = pageLineCounts.length - 1; if (lastLine >= 0) { int
		 * fullLineCount = pageLineCounts[lastLine]; if (fullLineCount !=
		 * physLineWithBreaks.length && fullLineCount > 0) { int[] newArr = new
		 * int[fullLineCount]; int ll = fullLineCount <
		 * physLineWithBreaks.length ? fullLineCount :
		 * physLineWithBreaks.length; System.arraycopy(physLineWithBreaks, 0,
		 * newArr, 0, ll); if (ll > 0) { for (int i = ll - 1; i < fullLineCount;
		 * i++) { newArr[i] = -1; } } } }
		 */
		for (int i = startLine; i < physLineWithBreaks.length; i++) {
			// physLineWithBreaks[i] = -1;
		}

		// for(int i = startLine; i < physLineWithBreaks.length; i++){
		// physLineWithBreaks[i] = -1;
		// }
		if (curLineY >= 0)
			lastAdequateY = endLine - 1;
	}

	public int getLinesPerPage() {
		return linesPerPage;
	}

	protected void setLinesPerPage(int linesPerPage) {
		this.linesPerPage = linesPerPage; // 10
	}

	int drawLine(int lineIndex, int paintX, int paintY, GC gc,
			Color widgetBackground, Color widgetForeground) {

		// long free1 =Runtime.getRuntime().freeMemory();

		TextLayout layout = getTextLayout(lineIndex);
		String line = content.getLine(lineIndex);
		int lineOffset = content.getOffsetAtLine(lineIndex);
		int lineLength = line.length();
		Point selection = styledText.getSelection();
		int selectionStart = selection.x - lineOffset;
		int selectionEnd = selection.y - lineOffset;
		Rectangle client = styledText.getClientArea();
		Color lineBackground = getLineBackground(lineIndex, null);
		StyledTextEvent event = styledText.getLineBackgroundData(lineOffset,
				line);
		if (event != null && event.lineBackground != null)
			lineBackground = event.lineBackground;
		int height = layout.getBounds().height;
		height = getLineHeight(lineIndex);
		if (lineBackground != null) {
			gc.setBackground(lineBackground);
			gc.fillRectangle(client.x, paintY, client.width, height);
		} else {
			/*
			 * gc.setBackground(widgetBackground); //turned off; now bg is
			 * filled for all lines in ExtendedStyledText.handlePaint
			 * styledText.drawBackground(gc, client.x, paintY, client.width,
			 * height);
			 */
		}
		gc.setForeground(widgetForeground);
		if (selectionStart == selectionEnd
				|| (selectionEnd <= 0 && selectionStart > lineLength - 1)) {
			checkDraw(lineIndex, gc, paintX, paintY, -1, -1, null, null, 0);
			// layout.draw(gc, paintX, paintY);
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
			if (selectionStart <= lineLength && lineLength < selectionEnd) {
				flags |= SWT.LAST_LINE_SELECTION;
			}
			checkDraw(lineIndex, gc, paintX, paintY, start, end, selectionFg,
					selectionBg, flags);
			// layout.draw(gc, paintX, paintY, start, end - 1, selectionFg,
			// selectionBg, flags);
		}

		// draw objects
		drawObjects(lineIndex, gc, paintX, paintY, layout, lineOffset);
		disposeTextLayout(layout);

		return height;
	}

	int getHeight() {
		int defaultLineHeight = getLineHeight();
		if (styledText.isFixedLineHeight()) {
			return lineCount * defaultLineHeight;
		}
		int totalHeight = 0;
		int width = styledText.getWrapWidth();
		for (int i = 0; i < lineCount; i++) {
			int height = lineHeight[i];
			if (height == -1) {
				if (width > 0) {
					calculate(i, 1);
					height = lineHeight[i];
				} else {
					height = defaultLineHeight;
				}
			}
			totalHeight += height;
		}
		int additionalHeight = getLastPageAdditionalHeight();
		return totalHeight + additionalHeight + styledText.topMargin
				+ styledText.bottomMargin;
	}

	protected int getLastPageAdditionalHeight() {
		int last = lineCount - 1;
		int lineCountInLayoitWithoutSpacing = 0;
		if (lineCount == 1) {
			lineCountInLayoitWithoutSpacing = lineCounts[last];
		} else {
			lineCountInLayoitWithoutSpacing = lineCounts[last]
					- lineCounts[last - 1];
		}
		double extraAdjust = (ExtendedStyledTextConstants.BREAK_SIZE * getScalingFactor()) / 2;
		int backSize = (int) Math.round(PAGE_BREAK_BACKSIZE
				* getScalingFactor());
		// TODO FIX ME
		int fullLineCount = getPrevLineCountForPaging(last)
				+ lineCountInLayoitWithoutSpacing;
		int restOfLines = fullLineCount % linesPerPage == 0 ? linesPerPage
				: fullLineCount % linesPerPage;
		double a = (double) (linesPerPage - restOfLines) * getLineHeight()
				* getScalingFactor() + extraAdjust - backSize / 2;// / 2.0;
		int additionalHeight = (int) Math.round(a);
		return additionalHeight;
	}

	protected int checkDraw(int lineIndex, GC gc, int x, int y,
			int selectionStart, int selectionEnd, Color selectionForeground,
			Color selectionBackground, int flags) {

		TextLayout layout = getTextLayout(lineIndex);
		// System.out.println("Draw " + layout.getText() + " linesPerPage " +
		// linesPerPage);

		int lineCountInsideLayout = layout.getLineCount();

		int visualLineCount = getPrevLineCountForPaging(lineIndex); // =prevLineCountForPaging
		int prevLineCountForPaging = getPrevLineCountForPaging(lineIndex); // =visualLineCount
		// visualLineCount

		if (lineIndex != 0) {
			visualLineCount -= getParagraphSpacingInSymbols(lineIndex);
			prevLineCountForPaging -= getParagraphSpacingInSymbols(lineIndex);
		}

		int pageIndex = getPageIndexForLogicalLine(lineIndex); // correct
		int pageOffset = getPageOffset(pageIndex); // correct this is offset of

		if (pageFirstLineOffsets.length <= pageIndex) {
			int[] newArr = new int[pageIndex + 1];
			System.arraycopy(pageFirstLineOffsets, 0, newArr, 0,
					pageFirstLineOffsets.length);
			Arrays.fill(newArr, pageFirstLineOffsets.length - 1, pageIndex, -1);
			newArr[pageIndex] = pageOffset;
			pageFirstLineOffsets = newArr;
		} else {
			if (pageFirstLineOffsets[pageIndex] == -1) {
				pageFirstLineOffsets[pageIndex] = pageOffset;
			}
		}

		// last page
		int pI = getLineCount(lineIndex); // correct
		int layoutLineCount = lineCountInsideLayout;
		// visualLineCount -= layoutLineCount;
		pI -= layoutLineCount;
		layout.draw(gc, x, y, selectionStart, selectionEnd - 1,
				selectionForeground, selectionBackground, flags);
		double scalingFactor = getScalingFactor();
		// ExtendedStyledText extendedStyledText = (ExtendedStyledText)
		// styledText;
		IPageInformation pageInformation = extendedStyledText.pageInformation;
		// pageInformation = extendedStyledText.pageInformation;

		// int nextI=visualLineCount;

		int i = visualLineCount;
		int pageLineCountInsideLayout = 0;
		if (lineIndex == 0) {
			pageLineCountInsideLayout = lineCountInsideLayout; // pageLineCounts[lineIndex];
		} else {
			pageLineCountInsideLayout = lineCountInsideLayout; // pageLineCounts[lineIndex]
		}
		int paragraphSpacingInString = getParagraphSpacingInSymbols(lineIndex);
		pageLineCountInsideLayout += paragraphSpacingInString;
		int heightOfPageLine = (int) (getLineHeight() * getScalingFactor()); // layout.getBounds().height
		// /
		// pageLineCountInsideLayout;
		int breakShift = 0;
		for (int a = 0; a < pageLineCountInsideLayout; a++) {
			int numberOfLineInsideLayout = a - paragraphSpacingInString <= 0 ? 0
					: a - paragraphSpacingInString; // getNumberOfLineInsideLayout(a*heightOfPageLine,layout);
			i = /*
				 * prevLineCountForPaging -
				 * getParagraphSpacingInSymbols(lineIndex) + a;
				 */visualLineCount + numberOfLineInsideLayout;

			if (a == 0 && takeParagraphSpacingInAccount) {
				i = i - (lineIndex > 0 ? paragraphSpacingInString : 0);
			}
			if (pI == 0 && a == 0) {
				pageInformationRenderer.drawFirstPageHeader(gc, scalingFactor,
						pageIndex, pageOffset, extendedStyledText,
						pageInformation);
			}

			boolean lastLine = numberOfLineInsideLayout == layoutLineCount - 1
					&& lineIndex == styledText.getLineCount() - 1
					&& a == pageLineCountInsideLayout - 1;
			boolean needBreak = false;

			// if(lastLine){
			// System.out.println("Last Line: " + layout.getText());
			// }

			int lineCount = prevLineCountForPaging + a + 1;
			needBreak = isPageBreak(lineCount, lineCount);

			// if(lineIndex == lineHeight.length - 1 && needBreak){
			// System.out.println(" lineCount " + lineCount + " a " + a +
			// " numLineInsLayout " + numberOfLineInsideLayout + " linesInLay "
			// + lineCountInsideLayout);
			// }

			if (/* i > 0 && */(needBreak && pagingEnabled) || lastLine) {
				int lineY = (a) * heightOfPageLine;

				double scaledBreakSize = getScaledBreakSize(); // ExtendedStyledTextConstants.BREAK_SIZE
				// * scalingFactor;
				double extraAdjust = scaledBreakSize / 2;
				int backSize = (int) Math.round(PAGE_BREAK_BACKSIZE
						* scalingFactor);
				int yy = this.lineY[lineIndex];
				int pos = (int) (/*yy*/ y + lineY + extraAdjust - backSize / 2 + breakShift);
						//- styledText.getTopPixel();
				// - heightOfPageLine;

				if (lastLine) {
					// TODO REMOVE System.out.*
					// System.out.println("Last line " + layout.getText());

					int lastPageAdditionalHeight = getLastPageAdditionalHeight();
					int spaceForText = /*yy*/ y +  + (pageLineCountInsideLayout - 1)
							* heightOfPageLine;
					pos = (int) (spaceForText + lastPageAdditionalHeight);
							//- styledText.getTopPixel(); // (int)(lineHeight2+2*d);
					// if( lastLinePosition == 0){
					// lastLinePosition = pos + styledText.getTopPixel();
					// }
					// if(needBreak ){
					pos += breakShift;
					i++;
					// }
				}

				if (lineIndex == 0) {
					pos += extraAdjust + 3 * backSize / 2;
				}
				// System.out.
				breakShift += extraAdjust * 2;

				int hPos = pos - (int) Math.round(scaledBreakSize / 2)
						+ backSize / 2 + heightOfPageLine;// +
				// corr;
				int clearHeight = (int) Math.round(scaledBreakSize) - 2
						* heightOfPageLine;
				clearBackground(gc, scalingFactor, hPos, clearHeight,
						pageIndex, pageOffset, extendedStyledText,
						pageInformation);
				if (lastLine) {
					if (i % linesPerPage != 0) {
						i = (i / linesPerPage + 1) * linesPerPage;
					}
				}

				int pageIndexFromOffset = getPageIndex(i + 1);
				int pageOffsett1 = getPageOffset(pageIndexFromOffset);
				if (pageFirstLineOffsets.length <= pageIndexFromOffset) {
					int[] newArr = new int[pageIndexFromOffset + 1];
					System.arraycopy(pageFirstLineOffsets, 0, newArr, 0,
							pageFirstLineOffsets.length);
					Arrays.fill(newArr, pageFirstLineOffsets.length - 1,
							pageIndexFromOffset, -1);
					newArr[pageIndexFromOffset] = pageOffsett1;
					pageFirstLineOffsets = newArr;
				} else {
					if (pageFirstLineOffsets[pageIndexFromOffset] == -1) {
						pageFirstLineOffsets[pageIndexFromOffset] = pageOffsett1;
					}
				}

				if (pageInformation != null) {
					if (!lastLine) {

						// int pos1 = pageIndexFromOffset >= 0
						// && pageIndexFromOffset < pageFirstLineOffsets.length
						// ? pageIndexFromOffset
						// : pageIndexFromOffset; //0;
						// draw page header
						pageInformationRenderer.drawPageHeader(gc,
								scalingFactor, pos, pageIndexFromOffset,
								pageFirstLineOffsets[pageIndexFromOffset],
								extendedStyledText, pageInformation);

						if (pageFirstLineOffsets[pageIndexFromOffset - 1] == -1) {
							pageFirstLineOffsets[pageIndexFromOffset - 1] = getPageOffset(pageIndexFromOffset - 1);
						}
						// page footer
						pageInformationRenderer.drawPageFooter(gc,
								scalingFactor, pos, pageIndexFromOffset - 1,
								pageFirstLineOffsets[pageIndexFromOffset - 1],
								extendedStyledText, pageInformation);

						pageInformationRenderer.drawPageNumber(gc,
								scalingFactor, i + 1, pos,
								pageFirstLineOffsets[pageIndexFromOffset - 1],
								extendedStyledText, pageInformation);
					}
				}

				// System.out.println("NEEED BREAK " + needBreak + " VIS BREAK "
				// + visualizePageBreaks + " CALCULATED " + definatelyNeedBreak
				// + " PAGE SIZE " + physicalPageHeight + " Y "+ y );
				if (visualizePageBreaks) {
					// System.out.println("BREAK real: " + pos + " calculated: "
					// + realBreakPos);
					pageInformationRenderer.drawPageBreak(gc, scalingFactor,
							extendedStyledText, pos);
				}

				if (lastLine && pagingEnabled) {
					pageInformationRenderer.drawBreak(gc, scalingFactor,
							extendedStyledText, pos);
					// + (int) Math.round(pageBreakSize
					// * scalingFactor));

					if (pageFirstLineOffsets[pageIndexFromOffset - 1] == -1) {
						pageFirstLineOffsets[pageIndexFromOffset - 1] = getPageOffset(pageIndexFromOffset - 1);
					}

					// TODO: FIX ME
					pageInformationRenderer.drawPageFooter(gc, scalingFactor,
							pos, pageIndexFromOffset - 1,
							pageFirstLineOffsets[pageIndexFromOffset - 1],
							extendedStyledText, pageInformation);

					pageInformationRenderer.drawPageNumber(gc, scalingFactor,
							i + 1, pos,
							pageFirstLineOffsets[pageIndexFromOffset - 1],
							extendedStyledText, pageInformation);
				}
				// System.out.println("PB:"+lineIndex+"Pos:"+pos);

			}

			// prevVisualLineCount = i;
			int y2 = y + layout.getLineBounds(numberOfLineInsideLayout).y;

			// gc.drawRectangle(layout.getLineBounds(numberOfLineInsideLayout).x+extendedStyledText.getIndent(lineIndex),
			// y2, layout.getLineBounds(numberOfLineInsideLayout).width,
			// layout.getLineBounds(numberOfLineInsideLayout).height);

		}

		// layout.getFont().dispose();
		// layout.setFont(de);

		// Test code
		// gc.drawRectangle(layout.getBounds().x+extendedStyledText.getIndent(lineIndex),
		// y + layout.getBounds().y,
		// layout
		// .getBounds().width, layout.getBounds().height);
		//
		
		return 0;

	}

	public int getNumberOfLineInsideLayout(int offset, TextLayout layout) {
		int layoutLineCount = layout.getLineCount();
		for (int i = 0; i < layoutLineCount; i++) {
			int left = TextLayoutOps.getLineY(layout, i);
			int right = TextLayoutOps.getLineY(layout, i)
					+ TextLayoutOps.getLineHeight(layout, i);

			if (offset >= left && offset < right) {
				return i;
			}

			if (offset == left && offset == right) {
				return i;
			}

		}
		
		return 0;
	}

	public void drawTestPageBreak(GC gc, double scalingFactor,
			ExtendedStyledText extendedStyledText, int pos) {
		Color tmp = gc.getBackground();
		Color color = new Color(gc.getDevice(), new RGB(255, 0, 0));
		int additionalPageWidth = extendedStyledText.getAdditionalPageWidth();

		gc.setBackground(color);

		gc.fillRectangle(0, pos, (int) Math.round((extendedStyledText
				.getPageWidth() + additionalPageWidth)
				* scalingFactor)
				- extendedStyledText.horizontalScrollOffset, (int) Math
				.round(Renderer.PAGE_BREAK_SIZE * scalingFactor));
		color.dispose();
		gc.setBackground(tmp);
		tmp.dispose();
	}

	public void clearBackground(GC gc, double scalingFactor, int pos,
			int height, int pageIndex, int pageOffset,
			ExtendedStyledText extendedStyledText,
			IPageInformation pageInformation) {
		int additionalPageWidth = extendedStyledText.getAdditionalPageWidth();
		Color tmp = gc.getBackground();
		Color white = new Color(gc.getDevice(), new RGB(255, 255, 255));
		gc.setBackground(white);
		gc.fillRectangle(0, pos, (int) Math.round((extendedStyledText
				.getPageWidth() + additionalPageWidth)
				* scalingFactor)
				- extendedStyledText.horizontalScrollOffset, height);
		white.dispose();
		gc.setBackground(tmp);
	}

	protected void drawObjects(int lineIndex, GC gc, int paintX, int paintY,
			TextLayout layout, int lineOffset) {
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
					if (bulletIndex != -1)
						break;
				}
			}
		}
		if (bulletIndex != -1 && bullet != null) {
			FontMetrics metrics = layout.getLineMetrics(0);
			int lineAscent = metrics.getAscent() + metrics.getLeading();
			if (bullet.type == ST.BULLET_CUSTOM) {
				bullet.style.start = lineOffset;
				styledText.paintObject(gc, paintX, paintY, lineAscent, metrics
						.getDescent(), bullet.style, bullet, bulletIndex);
			} else {
				drawBullet(bullet, gc, paintX, paintY, bulletIndex, lineAscent,
						metrics.getDescent());
			}
		}
		TextStyle[] styles = layout.getStyles();
		int[] ranges = null;
		for (int i = 0; i < styles.length; i++) {
			if (styles[i].metrics != null) {
				if (ranges == null)
					ranges = layout.getRanges();
				int start = ranges[i << 1];
				int length = ranges[(i << 1) + 1] - start;
				Point point = layout.getLocation(start, false);
				FontMetrics metrics = layout.getLineMetrics(layout
						.getLineIndex(start));
				StyleRange style = (StyleRange) ((StyleRange) styles[i])
						.clone();
				style.start = start + lineOffset;
				style.length = length;
				int lineAscent = metrics.getAscent() + metrics.getLeading();
				styledText.paintObject(gc, point.x + paintX, point.y + paintY,
						lineAscent, metrics.getDescent(), style, null, 0);
			}
		}
	}

	void textChanging(TextChangingEvent event) {
		int start = event.start;
		int newCharCount = event.newCharCount, replaceCharCount = event.replaceCharCount;
		int newLineCount = event.newLineCount, replaceLineCount = event.replaceLineCount;

		updateRanges(start, replaceCharCount, newCharCount);

		int startLine = content.getLineAtOffset(start);
		if (replaceCharCount == content.getCharCount())
			lines = null;
		if (replaceLineCount == lineCount) {
			lineCount = newLineCount;
			lineWidth = new int[lineCount];
			lineHeight = new int[lineCount];
			lineY = new int[lineCount];
			originalLineHeight = new int[lineCount];
			lineCounts = new int[lineCount];
			// physLineWithBreaks = new int[lineCount];
			pageLineCounts = new int[lineCount];
			pageFirstLineOffsets = new int[lineCount];
			reset(0, lineCount);
		} else {
			lastAdequateY = startLine - 1;
			int delta = newLineCount - replaceLineCount;
			if (lineCount + delta > lineWidth.length) {
				int[] newWidths = new int[lineCount + delta + GROW];
				System.arraycopy(lineWidth, 0, newWidths, 0, lineCount);
				lineWidth = newWidths;
				int[] newHeights = new int[lineCount + delta + GROW];
				System.arraycopy(lineHeight, 0, newHeights, 0, lineCount);
				lineHeight = newHeights;
				int[] newY = new int[lineCount + delta + GROW];
				System.arraycopy(lineY, 0, newY, 0, lineCount);
				lineY = newY;
				int[] newOriginalHeight = new int[lineCount + delta + GROW];
				System.arraycopy(originalLineHeight, 0, newOriginalHeight, 0,
						lineCount);
				originalLineHeight = newOriginalHeight;
				int[] newCounts = new int[lineCount + delta + GROW];
				int[] newCounts1 = new int[lineCount + delta + GROW];
				int[] newOffsets = new int[lineCount + delta + GROW];
				int[] newBreaks = new int[lineCount + delta + GROW];
				System.arraycopy(lineCounts, 0, newCounts, 0, lineCount);
				lineCounts = newCounts;
				System.arraycopy(pageLineCounts, 0, newCounts1, 0, lineCount);
				pageLineCounts = newCounts1;
				// System.arraycopy(pageFirstLineOffsets, 0, newOffsets, 0,
				// lineCount);
				// pageFirstLineOffsets = newOffsets;
				System
						.arraycopy(physLineWithBreaks, 0, newBreaks, 0,
								lineCount);
				physLineWithBreaks = newBreaks;
			}
			if (lines != null) {
				if (lineCount + delta > lines.length) {
					LineInfo[] newLines = createLineInfoArray(lineCount + delta
							+ GROW);
					System.arraycopy(lines, 0, newLines, 0, lineCount);
					lines = newLines;
				}
			}
			int startIndex = startLine + replaceLineCount + 1;
			int endIndex = startLine + newLineCount + 1;
			System.arraycopy(lineWidth, startIndex, lineWidth, endIndex,
					lineCount - startIndex);
			System.arraycopy(lineHeight, startIndex, lineHeight, endIndex,
					lineCount - startIndex);
			System.arraycopy(originalLineHeight, startIndex,
					originalLineHeight, endIndex, lineCount - startIndex);
			System.arraycopy(lineY, startIndex, lineY, endIndex, lineCount
					- startIndex);
			System.arraycopy(lineCounts, startIndex, lineCounts, endIndex,
					lineCount - startIndex);
			System.arraycopy(pageLineCounts, startIndex, pageLineCounts,
					endIndex, lineCount - startIndex);
			// System.arraycopy(pageFirstLineOffsets, startIndex,
			// pageFirstLineOffsets, endIndex, lineCount - startIndex);
			System.arraycopy(physLineWithBreaks, startIndex,
					physLineWithBreaks, endIndex, lineCount - startIndex);
			for (int i = startLine; i < endIndex; i++) {
				physLineWithBreaks[i] = pageLineCounts[i] = lineWidth[i] = lineHeight[i] = originalLineHeight[i] = lineY[i] = lineCounts[i] = -1;
			}
			for (int i = lineCount + delta; i < lineCount; i++) {
				physLineWithBreaks[i] = pageLineCounts[i] = lineWidth[i] = lineHeight[i] = originalLineHeight[i] = lineY[i] = lineCounts[i] = -1;
			}
			for (int i = startLine; i < physLineWithBreaks.length; i++) {
				physLineWithBreaks[i] = -1;
			}
			if (layouts != null) {
				int layoutStartLine = startLine - topIndex;
				int layoutEndLine = layoutStartLine + replaceLineCount + 1;
				for (int i = layoutStartLine; i < layoutEndLine; i++) {
					if (0 <= i && i < layouts.length) {
						if (layouts[i] != null) {
							// //TEST CODE
							firsltLinesBreaks.remove(layouts[i]);
							firstLineShifts.remove(layouts[i]);
							paragraphSpacingOfFirstLine.remove(layouts[i]);
							// //
							layouts[i].dispose();
						}
						layouts[i] = null;
						if (bullets != null && bulletsIndices != null)
							bullets[i] = null;
					}
				}
				if (delta > 0) {
					for (int i = layouts.length - 1; i >= layoutEndLine; i--) {
						if (0 <= i && i < layouts.length) {
							endIndex = i + delta;
							if (0 <= endIndex && endIndex < layouts.length) {
								layouts[endIndex] = layouts[i];
								layouts[i] = null;
								if (bullets != null && bulletsIndices != null) {
									bullets[endIndex] = bullets[i];
									bulletsIndices[endIndex] = bulletsIndices[i];
									bullets[i] = null;
								}
							} else {
								if (layouts[i] != null) {
									// //TEST CODE
									firsltLinesBreaks.remove(layouts[i]);
									firstLineShifts.remove(layouts[i]);
									paragraphSpacingOfFirstLine
											.remove(layouts[i]);
									// //
									layouts[i].dispose();
								}
								layouts[i] = null;
								if (bullets != null && bulletsIndices != null)
									bullets[i] = null;
							}
						}
					}
				} else if (delta < 0) {
					for (int i = layoutEndLine; i < layouts.length; i++) {
						if (0 <= i && i < layouts.length) {
							endIndex = i + delta;
							if (0 <= endIndex && endIndex < layouts.length) {
								layouts[endIndex] = layouts[i];
								layouts[i] = null;
								if (bullets != null && bulletsIndices != null) {
									bullets[endIndex] = bullets[i];
									bulletsIndices[endIndex] = bulletsIndices[i];
									bullets[i] = null;
								}
							} else {
								if (layouts[i] != null) {
									// //TEST CODE
									firsltLinesBreaks.remove(layouts[i]);
									firstLineShifts.remove(layouts[i]);
									paragraphSpacingOfFirstLine
											.remove(layouts[i]);
									// //
									layouts[i].dispose();
								}
								layouts[i] = null;
								if (bullets != null && bulletsIndices != null)
									bullets[i] = null;
							}
						}
					}
				}
			}
			if (replaceLineCount != 0 || newLineCount != 0) {
				int startLineOffset = content.getOffsetAtLine(startLine);
				if (startLineOffset != start)
					startLine++;
				updateBullets(startLine, replaceLineCount, newLineCount, true);
				if (lines != null) {
					startIndex = startLine + replaceLineCount;
					endIndex = startLine + newLineCount;
					System.arraycopy(lines, startIndex, lines, endIndex,
							lineCount - startIndex);
					for (int i = startLine; i < endIndex; i++) {
						lines[i] = null;
					}
					for (int i = lineCount + delta; i < lineCount; i++) {
						lines[i] = null;
					}
				}
			}
			lineCount += delta;
			if (maxWidthLineIndex != -1 && startLine <= maxWidthLineIndex
					&& maxWidthLineIndex <= startLine + replaceLineCount) {
				maxWidth = 0;
				maxWidthLineIndex = -1;
				for (int i = 0; i < lineCount; i++) {
					if (lineWidth[i] > maxWidth) {
						maxWidth = lineWidth[i];
						maxWidthLineIndex = i;
					}
				}
			}
		}
	}

	void reset() {
		super.reset();
		lineY = null;
		originalLineHeight = null;
		lineCounts = null;
		physLineWithBreaks = null;
		pageLineCounts = null;
		pageFirstLineOffsets = null;

		if (firsltLinesBreaks != null)
		    firsltLinesBreaks.clear();
		if (firstLineShifts != null)
		    firstLineShifts.clear();
		if (paragraphSpacingOfFirstLine != null)
		    paragraphSpacingOfFirstLine.clear();
	}

	void partiallyReset() {
		if (layouts != null) {
			for (int i = 0; i < layouts.length; i++) {
				TextLayout layout = layouts[i];
				if (layout != null) {
					// //TEST CODE
					firsltLinesBreaks.remove(layout);
					firstLineShifts.remove(layout);
					paragraphSpacingOfFirstLine.remove(layout);
					// //
					layout.dispose();
				}
			}
			layouts = null;
		}
		topIndex = -1;
		stylesSetCount = styleCount = 0;
		ranges = null;
		styles = null;
		stylesSet = null;
	}

	void reset(int startLine, int lineCount) {
		if (lineHeight == null) {
			return;
		}
		super.reset(startLine, lineCount);
		int endLine = startLine + lineCount;
		if (startLine < 0 || endLine > lineWidth.length)
			return;
		for (int i = startLine; i < endLine; i++) {
			lineY[i] = -1;
			originalLineHeight[i] = -1;
			lineCounts[i] = -1;
			pageLineCounts[i] = -1;
		}

		for (int i = 0; i < pageFirstLineOffsets.length; i++) {
			pageFirstLineOffsets[i] = -1;
		}
		/*
		 * if (startLine != 0) { int startLineC = pageLineCounts[startLine - 1];
		 * for (int i = startLineC; i < physLineWithBreaks.length; i++) {
		 * physLineWithBreaks[i] = -1; } }
		 */

		lastAdequateY = startLine - 1;
	}

	void setContent(StyledTextContent content) {
		reset();
		this.content = content;
		lineCount = content.getLineCount();
		lineWidth = new int[lineCount];
		lineHeight = new int[lineCount];
		lineY = new int[lineCount];
		originalLineHeight = new int[lineCount];
		lineCounts = new int[lineCount];
		physLineWithBreaks = new int[lineCount];
		pageLineCounts = new int[lineCount];
		pageFirstLineOffsets = new int[lineCount];
		rightLineIndents = new ArrayList<Integer>();
		reset(0, lineCount);
		// mainFontHeight = -1;
	}

	public int getParagraphSpacing() {
		return paragraphSpacing;
	}

	public void setParagraphSpacing(int paragraphSpacing) {
		this.paragraphSpacing = paragraphSpacing;// 0;
	}

	void dispose() {
		super.dispose();
		disposeFonts();
	}

	protected void disposeFonts() {
		Collection<Font> values = scaledFonts.values();

		for (Iterator<Font> iterator = values.iterator(); iterator.hasNext();) {
			Font font = (Font) iterator.next();
			font.dispose();
		}
	}

	public int getCaretHeight(int lineAtOffset) {
		TextLayout textLayout = getTextLayout(lineAtOffset);
		TextLayout localLayout = createLayoutForVisibleLine(textLayout, 0);
		return localLayout.getBounds().height;
	}

	public int getPageIndex(int lineIndex) {
		int pageIndex = (int) Math.round(lineIndex * 1.0 / linesPerPage);
		// System.out
		// .println("pageIndex " + pageIndex + " lineIndex " + lineIndex);
		return pageIndex;// lineIndex
	}

	public int getPageIndexForLogicalLine(int lineIndex) {
		int lineNumber = 0;
		int[] array; // TODO add choise for paging mode

		lineNumber = pageLineCounts[lineIndex];
		// return (int) Math.round(lineNumber * 1.0 / linesPerPage);// lineIndex
		return lineNumber / linesPerPage;// lineIndex
	}

	public int getPageOffset(int pageIndex) {
		// System.out.println("MIN " +pageIndex * linesPerPage +" " +
		// lineY.length);
		int allLinesCount = 0;
		allLinesCount += pageLineCounts[pageLineCounts.length - 1];

		int numLinesInsidePages = Math.min(pageIndex * linesPerPage,
				allLinesCount);
		int logicalLineIndex = 0;
		int countOfPrevLines = 0;
		for (int i = 0; i < pageLineCounts.length; i++) {
			int tmp = pageLineCounts[i];
			if (numLinesInsidePages <= tmp) {
				logicalLineIndex = i;
				break;
			}
			countOfPrevLines = pageLineCounts[i];
		}
		int numberOfLinesInsideLayout = numLinesInsidePages - countOfPrevLines;

		TextLayout logicalString = getTextLayout(logicalLineIndex);
		int logicalLineOffset = content.getOffsetAtLine(logicalLineIndex);
		int delta = getParagraphSpacingInSymbols(logicalLineIndex);

		if (numberOfLinesInsideLayout > delta) {
			numberOfLinesInsideLayout -= delta;
		} else {
			numberOfLinesInsideLayout = 0;
		}

		int fullOffset = logicalString.getLineOffsets()[numberOfLinesInsideLayout]
				+ logicalLineOffset;
		return fullOffset; // FIX
	}

	public IPageInformationRenderer getPageInformationRenderer() {
		return pageInformationRenderer;
	}

	public void setPageInformationRenderer(
			IPageInformationRenderer pageInformationRenderer) {
		this.pageInformationRenderer = pageInformationRenderer;
	}

	public int getLineSpacing(int index) {
		return (int) Math.round(super.getLineSpacing(index)
				* getScalingFactor());
	}

	public boolean isVisualizePageBreaks() {
		return visualizePageBreaks;
	}

	public void setVisualizePageBreaks(boolean visualizePageBreaks) {
		this.visualizePageBreaks = visualizePageBreaks;
	}

	public boolean isPagingEnabled() {
		return pagingEnabled;
	}

	public int getPageBreakSize() {
		return pageBreakSize;
	}

	public void setPageBreakSize(int pageBreakSize) {
		this.pageBreakSize = pageBreakSize;
	}

	TextLayout getTextLayout(int lineIndex, int orientation, int width,
			int lineSpacing) {
		TextLayout layout = null;
		if (styledText != null) {
			int topIndex = styledText.topIndex > 0 ? styledText.topIndex - 1
					: 0;
			if (layouts == null || topIndex != this.topIndex) {
				TextLayout[] newLayouts = new TextLayout[CACHE_SIZE];
				if (layouts != null) {
					for (int i = 0; i < layouts.length; i++) {
						if (layouts[i] != null) {
							int layoutIndex = (i + this.topIndex) - topIndex;
							if (0 <= layoutIndex
									&& layoutIndex < newLayouts.length) {
								newLayouts[layoutIndex] = layouts[i];
							} else {
								// //TEST CODE
								firsltLinesBreaks.remove(layouts[i]);
								firstLineShifts.remove(layouts[i]);
								paragraphSpacingOfFirstLine.remove(layouts[i]);
								// //
								layouts[i].dispose();
							}
						}
					}
				}
				if (bullets != null && bulletsIndices != null
						&& topIndex != this.topIndex) {
					int delta = topIndex - this.topIndex;
					if (delta > 0) {
						if (delta < bullets.length) {
							System.arraycopy(bullets, delta, bullets, 0,
									bullets.length - delta);
							System.arraycopy(bulletsIndices, delta,
									bulletsIndices, 0, bulletsIndices.length
											- delta);
						}
						int startIndex = Math.max(0, bullets.length - delta);
						for (int i = startIndex; i < bullets.length; i++)
							bullets[i] = null;
					} else {
						if (-delta < bullets.length) {
							System.arraycopy(bullets, 0, bullets, -delta,
									bullets.length + delta);
							System.arraycopy(bulletsIndices, 0, bulletsIndices,
									-delta, bulletsIndices.length + delta);
						}
						int endIndex = Math.min(bullets.length, -delta);
						for (int i = 0; i < endIndex; i++)
							bullets[i] = null;
					}
				}
				this.topIndex = topIndex;
				layouts = newLayouts;
			}
			if (layouts != null) {
				int layoutIndex = lineIndex - topIndex;
				if (0 <= layoutIndex && layoutIndex < layouts.length) {
					layout = layouts[layoutIndex];
					if (layout != null) {
						if (lineWidth[lineIndex] != -1)
							return layout;
					} else {
						layout = layouts[layoutIndex] = new TextLayout(device);
					}
				}
			}
		}
		if (layout == null)
			layout = new TextLayout(device);
		String line = content.getLine(lineIndex);
		int lineOffset = content.getOffsetAtLine(lineIndex);
		int[] segments = null;
		int indent = 0;
		int alignment = SWT.LEFT;
		boolean justify = false;
		Bullet bullet = null;
		int[] ranges = null;
		StyleRange[] styles = null;
		int rangeStart = 0, styleCount = 0;
		StyledTextEvent event = null;
		if (styledText != null) {
			event = styledText.getLineStyleData(lineOffset, line);
			//segments = styledText.getBidiSegments(lineOffset, line);
			indent = styledText.indent;
			alignment = styledText.alignment;
			justify = styledText.justify;
		}
		if (event != null) {
			indent = event.indent;
			alignment = event.alignment;
			justify = event.justify;
			bullet = event.bullet;
			ranges = event.ranges;
			styles = event.styles;
			if (styles != null) {
				styleCount = styles.length;
				if (styledText.isFixedLineHeight()) {
					for (int i = 0; i < styleCount; i++) {
						if (styles[i].isVariableHeight()) {
							styledText.verticalScrollOffset = -1;
							styledText.setVariableLineHeight();
							styledText.redraw();
							break;
						}
					}
				}
			}
			if (bullets == null || bulletsIndices == null) {
				bullets = new Bullet[CACHE_SIZE];
				bulletsIndices = new int[CACHE_SIZE];
			}
			int index = lineIndex - topIndex;
			if (0 <= index && index < CACHE_SIZE) {
				bullets[index] = bullet;
				bulletsIndices[index] = event.bulletIndex;
			}
		} else {
			if (lines != null) {
				LineInfo info = lines[lineIndex];
				if (info != null) {
					if ((info.flags & INDENT) != 0)
						indent = info.indent;
					if ((info.flags & ALIGNMENT) != 0)
						alignment = info.alignment;
					if ((info.flags & JUSTIFY) != 0)
						justify = info.justify;
					if ((info.flags & SEGMENTS) != 0)
						segments = info.segments;
				}
			}
			if (bulletsIndices != null) {
				bullets = null;
				bulletsIndices = null;
			}
			if (bullets != null) {
				for (int i = 0; i < bullets.length; i++) {
					if (bullets[i].indexOf(lineIndex) != -1) {
						bullet = bullets[i];
						break;
					}
				}
			}
			ranges = this.ranges;
			styles = this.styles;
			styleCount = this.styleCount;
			if (ranges != null) {
				rangeStart = getRangeIndex(lineOffset, -1, styleCount << 1);
			} else {
				rangeStart = getRangeIndex(lineOffset, -1, styleCount);
			}
		}
		if (bullet != null) {
			StyleRange style = bullet.style;
			GlyphMetrics metrics = style.metrics;
			indent += metrics.width;
		}
		layout.setFont(regularFont);
		layout.setAscent(ascent);
		layout.setDescent(descent);
		layout.setText(line);
		layout.setOrientation(orientation);
		layout.setSegments(segments);
		layout.setWidth(width);
		layout.setSpacing(lineSpacing);
		layout.setTabs(new int[] { tabWidth });
		layout.setIndent(indent);
		layout.setAlignment(alignment);
		layout.setJustify(justify);

		int lastOffset = 0;
		int length = line.length();
		if (styles != null) {
			if (ranges != null) {
				int rangeCount = styleCount << 1;
				for (int i = rangeStart; i < rangeCount; i += 2) {
					int start, end;
					if (lineOffset > ranges[i]) {
						start = 0;
						end = Math.min(length, ranges[i + 1] - lineOffset
								+ ranges[i]);
					} else {
						start = ranges[i] - lineOffset;
						end = Math.min(length, start + ranges[i + 1]);
					}
					if (start >= length)
						break;
					if (lastOffset < start) {
						layout.setStyle(null, lastOffset, start - 1);
					}
					layout.setStyle(getStyleRange(styles[i >> 1]), start, end);
					lastOffset = Math.max(lastOffset, end);
				}
			} else {
				for (int i = rangeStart; i < styleCount; i++) {
					int start, end;
					if (lineOffset > styles[i].start) {
						start = 0;
						end = Math.min(length, styles[i].length - lineOffset
								+ styles[i].start);
					} else {
						start = styles[i].start - lineOffset;
						end = Math.min(length, start + styles[i].length);
					}
					if (start >= length)
						break;
					if (lastOffset < start) {
						layout.setStyle(null, lastOffset, start - 1);
					}
					layout.setStyle(getStyleRange(styles[i]), start, end);
					lastOffset = Math.max(lastOffset, end);
				}
			}
		}
		if (lastOffset < length)
			layout.setStyle(null, lastOffset, length);
		if (styledText != null && styledText.ime != null) {
			IME ime = styledText.ime;
			int compositionOffset = ime.getCompositionOffset();
			if (compositionOffset != -1) {
				int commitCount = ime.getCommitCount();
				int compositionLength = ime.getText().length();
				if (compositionLength != commitCount) {
					int compositionLine = content
							.getLineAtOffset(compositionOffset);
					if (compositionLine == lineIndex) {
						int[] imeRanges = ime.getRanges();
						TextStyle[] imeStyles = ime.getStyles();
						if (imeRanges.length > 0) {
							for (int i = 0; i < imeStyles.length; i++) {
								int start = imeRanges[i * 2] - lineOffset;
								int end = imeRanges[i * 2 + 1] - lineOffset;
								TextStyle imeStyle = imeStyles[i], userStyle;
								for (int j = start; j <= end; j++) {
									userStyle = layout.getStyle(j);
									if (userStyle == null && j > 0)
										userStyle = layout.getStyle(j - 1);
									if (userStyle == null && j + 1 < length)
										userStyle = layout.getStyle(j + 1);
									if (userStyle == null) {
										layout.setStyle(imeStyle, j, j);
									} else {
										TextStyle newStyle = new TextStyle(
												imeStyle);
										if (newStyle.font == null)
											newStyle.font = userStyle.font;
										if (newStyle.foreground == null)
											newStyle.foreground = userStyle.foreground;
										if (newStyle.background == null)
											newStyle.background = userStyle.background;
										layout.setStyle(newStyle, j, j);
									}
								}
							}
						} else {
							int start = compositionOffset - lineOffset;
							int end = start + compositionLength - 1;
							TextStyle userStyle = layout.getStyle(start);
							if (userStyle == null) {
								if (start > 0)
									userStyle = layout.getStyle(start - 1);
								if (userStyle == null && end + 1 < length)
									userStyle = layout.getStyle(end + 1);
								if (userStyle != null) {
									TextStyle newStyle = new TextStyle();
									newStyle.font = userStyle.font;
									newStyle.foreground = userStyle.foreground;
									newStyle.background = userStyle.background;
									layout.setStyle(newStyle, start, end);
								}
							}
						}
					}
				}
			}
		}

		if (styledText != null && styledText.isFixedLineHeight()) {
			int index = -1;
			int lineCount = layout.getLineCount();
			int height = getLineHeight();
			for (int i = 0; i < lineCount; i++) {
				int lineHeight = layout.getLineBounds(i).height;
				if (lineHeight > height) {
					height = lineHeight;
					index = i;
				}
			}
			if (index != -1) {
				FontMetrics metrics = layout.getLineMetrics(index);
				ascent = metrics.getAscent() + metrics.getLeading();
				descent = metrics.getDescent();
				if (layouts != null) {
					for (int i = 0; i < layouts.length; i++) {
						if (layouts[i] != null && layouts[i] != layout) {
							layouts[i].setAscent(ascent);
							layouts[i].setDescent(descent);
						}
					}
				}
				if (styledText.verticalScrollOffset != 0) {
					int topIndex = styledText.topIndex;
					int topIndexY = styledText.topIndexY;
					int lineHeight = getLineHeight();
					if (topIndexY >= 0) {
						styledText.verticalScrollOffset = (topIndex - 1)
								* lineHeight + lineHeight - topIndexY;
					} else {
						styledText.verticalScrollOffset = topIndex * lineHeight
								- topIndexY;
					}
				}
				styledText.calculateScrollBars();
				if (styledText.isBidiCaret())
					styledText.createCaretBitmaps();
				styledText.caretDirection = SWT.NULL;
				styledText.setCaretLocation();
				styledText.redraw();
			}
		}
		return layout;
	}

	void disposeTextLayout(TextLayout layout) {
		if (layouts != null) {
			for (int i = 0; i < layouts.length; i++) {
				if (layouts[i] == layout)
					return;
			}
		}

		// //TEST CODE
		firsltLinesBreaks.remove(layout);
		firstLineShifts.remove(layout);
		paragraphSpacingOfFirstLine.remove(layout);
		// //
		layout.dispose();
	}

}
