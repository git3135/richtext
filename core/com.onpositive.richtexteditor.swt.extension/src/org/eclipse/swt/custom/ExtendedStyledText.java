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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
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
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.internal.BidiUtil;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * 
 * @author kor
 * 
 */
public class ExtendedStyledText extends StyledText implements
		IMarginInfoProvider {

	static class ExtendedPrinting extends Printing {
		protected static final int tempDeshift = 200;
		protected StyledTextContent sourceContent;
		protected StyledTextRenderer screenRenderer;
		protected int fullScreenBreakSize;
		protected int breakLayoutLine;
		protected int deshiftDelta;
		protected ArrayList<Integer> indents;
		protected IPageInformation pageInformation;
		protected IPageInformationRenderer pageInformationRenderer;
		protected int page;
		protected int fullPageHeight;

		private int[] ancents;
		private int headerOffset = 50;
		double correctedRatioX = 0;
		double correctedRatioY = 0;

		// private int[] offsets;

		private void calculateRatios() {

			int lPHeight = ((PrintingPageInformationRenderer) pageInformationRenderer).clientArea.height; // printer.getBounds().height;
			int pMargins = pageInformation.getPageHeaderYOffset()
					+ pageInformation.getPageFooterYOffset()
					+ pageInformation.getPageBreakSize() + 40;//45
			int breakLineNumber = pageInformation.getBreakSize(
					IBreakSize.BREAK_SIZE_IN_LINES).getValue();
			int linesPerPage = pageInformation.getLinesPerPage();
			double pHeight = (lPHeight - pMargins)
					/ (linesPerPage + breakLineNumber);

			double breakSize = pHeight * (double) breakLineNumber;// /2.0;
			headerOffset = (int) (breakSize / 2.0);// - 50;

			double hOfOneLineP = pHeight;
			double hOfOneLineS = screenRenderer.getLineHeight();

			correctedRatioX = hOfOneLineP / hOfOneLineS;
			correctedRatioY = hOfOneLineP / hOfOneLineS; // ((PrinterRenderer)printerRenderer).ratioY*
															// 0.9;\
			((PrinterRenderer) printerRenderer).ratioX = correctedRatioX;
			((PrinterRenderer) printerRenderer).ratioY = correctedRatioY;
			return;
		}

		// private Font correctedFont = null;

		ExtendedPrinting(StyledText styledText, Printer printer,
				StyledTextPrintOptions printOptions) {
			super(styledText, printer, printOptions);
			screenRenderer = styledText.renderer;
			pageInformation = ((ExtendedStyledText) styledText).pageInformation;
			printerRenderer = new PrinterRenderer(printer, styledText,
					styledText.getDisplay(), pageInformation) {

				TextLayout getTextLayout(int lineIndex, int orientation,
						int width, int lineSpacing) {
					return getTextLayout(lineIndex);
				}
			};
			StyledTextRenderer renderer = styledText.renderer;
			((Renderer) printerRenderer).setVisualizePageBreaks(false);
			((Renderer) printerRenderer)
					.setLinesPerPage(((ExtendedStyledText) styledText).pageInformation
							.getLinesPerPage());
			sourceContent = copyContent(styledText.getContent());
			printerRenderer.setContent(sourceContent);
			int lineCount = styledText.getLineCount();
			Point screenPPI = ((ExtendedStyledText) styledText).getScreenPPI();
			Point printerDPI;
			if (((ExtendedStyledText) styledText).getPrinterResolver() != null)
				printerDPI = ((ExtendedStyledText) styledText)
						.getPrinterResolver().getPPI();
			else
				printerDPI = printer.getDPI();
			fullPageHeight = printer.getBounds().height;
			fullScreenBreakSize = ExtendedStyledTextConstants.BREAK_SIZE
					* printerDPI.y / screenPPI.y;
			indents = new ArrayList<Integer>(lineCount);
			for (int i = 0; i < lineCount; i++) {
				printerRenderer.setLineAlignment(i, 1, styledText
						.getLineAlignment(i));
				printerRenderer
						.setLineBullet(i, 1, styledText.getLineBullet(i));
				int indent = ((ExtendedStyledText) styledText).getIndent(i);
				indents.add(indent * printerDPI.x / screenPPI.x);
				/*
				 * if (indent > 0) printerRenderer.setLineIndent(i,1, indent *
				 * printerDPI.x / screenDPI.x);
				 */}
			renderer.copyInto(printerRenderer);
			cacheLineData(styledText);
		}

		StyledTextContent copyContent(StyledTextContent original) {
			StyledTextContent printerContent = null;
			if (original instanceof IMarginedStyledTextContent)
				try {
					printerContent = (IMarginedStyledTextContent) ((IMarginedStyledTextContent) original)
							.clone();
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if (printerContent == null) {
				printerContent = new DefaultContent();
				int insertOffset = 0;
				for (int i = 0; i < original.getLineCount(); i++) {
					int insertEndOffset;
					if (i < original.getLineCount() - 1) {
						insertEndOffset = original.getOffsetAtLine(i + 1);
					} else {
						insertEndOffset = original.getCharCount();
					}
					printerContent.replaceTextRange(insertOffset, 0, original
							.getTextRange(insertOffset, insertEndOffset
									- insertOffset));
					insertOffset = insertEndOffset;
				}
			}
			return printerContent;
		}

		void printLine(int x, int y, GC gc, Color foreground, Color background,
				TextLayout layout, TextLayout printLayout, int index) {

			if (background != null) {
				Rectangle rect = layout.getBounds();
				// gc.setBackground(background);
				// gc.drawRectangle(x, y, rect.width, rect.height);
				// gc.drawText(index+"", x+rect.width-20,y);
			}
			if (printOptions.printLineNumbers) {
				FontMetrics metrics = layout.getLineMetrics(0);
				printLayout.setAscent(metrics.getAscent()
						+ metrics.getLeading());
				printLayout.setDescent(metrics.getDescent());
				String[] lineLabels = printOptions.lineLabels;
				if (lineLabels != null) {
					if (0 <= index && index < lineLabels.length
							&& lineLabels[index] != null) {
						printLayout.setText(lineLabels[index]);
					} else {
						printLayout.setText("");
					}
				} else {
					printLayout.setText(String.valueOf(index));
				}
				int paintX = x - printMargin - printLayout.getBounds().width;
				printLayout.draw(gc, paintX, y);
				printLayout.setAscent(-1);
				printLayout.setDescent(-1);
			}
			TextLayout originalLayout = screenRenderer.getTextLayout(index);

			int[] lineOffsets = originalLayout.getLineOffsets();
			int initialY = y;

			//ArrayList<TextLayout> localLayouts = new ArrayList<TextLayout>(
			//		originalLayout.getLineCount());
			ArrayList<Font> fonts = new ArrayList<Font>();
			int[] ss = new int[originalLayout.getLineCount()];
			double ratioY = ((PrinterRenderer) printerRenderer).getRatioY();

			int spacing = ((Renderer) printerRenderer)
					.getParagraphSpacingInSymbols(index);
			int totalLines = spacing + originalLayout.getLineCount();
			int correctLineHeight = layout.getBounds().height / totalLines;

			double rx = correctedRatioX; // ((PrinterRenderer)printerRenderer).ratioX;
			double ry = correctedRatioY;// ((PrinterRenderer)printerRenderer).ratioY;

			double spaccing = 0.0;
			if (breakLayoutLine == 0) {
				int spacingG = ((Renderer) screenRenderer)
						.getParagraphSpacing(index);
				TextLayout local = ((Renderer) screenRenderer)
						.createLayoutForVisibleLine(originalLayout, 0);
				TextLayoutOpsProvider.getInstance()
						.shiftLines(
								originalLayout,
								0,
								-TextLayoutOpsProvider.getInstance().getLineBounds(originalLayout, 0).height 	+ (int) (screenRenderer.getLineHeight() * ((Renderer) screenRenderer)
												.getScalingFactor()));
				TextLayoutOpsProvider.getInstance().setFirstLineSpacing(originalLayout, local,
						spacingG);// +
									// (int)(screenRenderer.getLineHeight()*((Renderer)screenRenderer).getScalingFactor()));
				// spaccing+=spacingG*ry;
				local.dispose();
			}

			Transform transform = new Transform(gc.getDevice(), new float[] {
					(float) rx, 0, 0, (float) ry, 0, 0 });
			gc.setTransform(transform);
			originalLayout.draw(gc, (int) ((x + indents.get(index)) / rx),
					(int) (y / ry));// (int)(y/ry));
		
			//Test code
			//gc.drawRectangle((int)(x /rx),(int)(y /
			//ry),originalLayout.getBounds().width,originalLayout.getBounds().height);//(int)(y/ry)
			//
			
			gc.setTransform(null);
			ancents[index] = (int) (ry * originalLayout.getBounds().height + spaccing);
			transform.dispose();
			/*
			 * Font pf = layout.getFont(); float ps = pf.size; double r =
			 * ((PrinterRenderer)printerRenderer).ratioY; int fh = (int)
			 * (r*originalLayout.getFont().size);
			 * //(int)(((float)originalLayout.getBounds().height /
			 * (float)layout.getBounds().height) *
			 * originalLayout.getFont().size); Font nf = new
			 * Font(pf.getDevice(),pf.toString(),(int)fh,pf.style); nf.size =
			 * fh; fonts.add(nf);
			 * 
			 * for (int i = 0; i < originalLayout.getLineCount(); i++) {
			 * TextLayout localLayout = ((PrinterRenderer)
			 * printerRenderer).createLayoutForVisibleLine(layout, i,
			 * lineOffsets); int h = localLayout.getBounds().height;
			 * 
			 * //localLayout.setFont(nf);//layout.getFont());
			 * //localLayout.setAscent(0); //
			 * localLayout.setDescent(layout.getDescent()); //
			 * localLayout.setSpacing(layout.getSpacing());
			 * TextLayoutOpsProvider.getInstance().forceComputeRuns(localLayout, gc);
			 * 
			 * int shiftAmount = 0; /*if (i < layout.getLineCount()) {
			 * shiftAmount = (int) ((TextLayoutOpsProvider.getInstance() .getLineHeight(layout, i)));
			 * // System.out.println(shiftAmount+":"+i); } else { int shift =
			 * (int) Math.round(TextLayoutOpsProvider.getInstance().getLineHeight( originalLayout,
			 * i)); shiftAmount = shift; if (breakLayoutLine == i) shiftAmount
			 * -= deshiftDelta; }
			 */
			/*
			 * if (i == 0) { ancents[index] = (spacing/*+1) * correctLineHeight
			 * + localLayout.getBounds().height;//shiftAmount -
			 * localLayout.getBounds().height + 1;
			 * localLayout.setAscent((spacing/*+1) * correctLineHeight
			 * +localLayout
			 * .getBounds().height);//localLayout.getBounds().height)
			 * ;//shiftAmount - 1); }else{ //localLayout.set ancents[index] +=
			 * localLayout.getBounds().height;//correctLineHeight;//
			 * localLayout.getBounds().height; } ss[i] = shiftAmount;
			 * localLayouts.add(localLayout); } gc.setForeground(foreground);
			 * Transform transform = new Transform(gc.getDevice()); float scaleY
			 * = (float) (ratioY * layout.getBounds().height / (y - initialY));
			 * // System.out.println(scaleY); // transform.scale(1,
			 * (float)(1/ratioY)); // gc.setTransform(transform); y = initialY;
			 * // y*=scaleY; for (int i = 0; i < localLayouts.size(); i++) {
			 * TextLayout localLayout = localLayouts.get(i);
			 * localLayout.draw(gc, x + indents.get(index)/* - clientArea.x ,
			 * y); //code gc.drawRectangle(x ,
			 * y,localLayout.getBounds().width,localLayout.getBounds().height);
			 * //// // gc.drawLine(0,y,100,y); // gc.drawText(""+y,10,y,true);
			 * 
			 * // System.out.println("line:"+i+"y:"+ //
			 * y+":x:"+x+"Text:"+localLayout.getText()); y +=
			 * localLayout.getBounds().height; } for(Font f : fonts){
			 * f.dispose(); } gc.setTransform(null);
			 */
		}

		int printLinesFromTo(int x, int y, GC gc, Color foreground,
				Color background, TextLayout layout, TextLayout printLayout,
				int index, int fromIndex, int toIndex) {
			ArrayList<Font> fonts = new ArrayList<Font>();

			if (background != null) {
				Rectangle rect = layout.getBounds();
				gc.setBackground(background);
				gc.fillRectangle(x, y, rect.width, rect.height);
			}
			if (printOptions.printLineNumbers) {
				FontMetrics metrics = layout.getLineMetrics(0);
				printLayout.setAscent(metrics.getAscent()
						+ metrics.getLeading());
				printLayout.setDescent(metrics.getDescent());
				String[] lineLabels = printOptions.lineLabels;
				if (lineLabels != null) {
					if (0 <= index && index < lineLabels.length
							&& lineLabels[index] != null) {
						printLayout.setText(lineLabels[index]);
					} else {
						printLayout.setText("");
					}
				} else {
					printLayout.setText(String.valueOf(index));
				}
				int paintX = x - printMargin - printLayout.getBounds().width;
				printLayout.draw(gc, paintX, y);
				printLayout.setAscent(-1);
				printLayout.setDescent(-1);
			}
			TextLayout originalLayout = screenRenderer.getTextLayout(index);
			/* int[] lineOffsets = originalLayout.getLineOffsets(); */
			int initialY = y;
			/*
			 * int lineCount = originalLayout.getLineCount(); if (toIndex == -1
			 * || toIndex > lineCount) toIndex = lineCount; int headerOffset =
			 * 0;
			 */

			/*
			 * if (fromIndex == breakLayoutLine) TODO { if (pageInformation !=
			 * null) headerOffset =
			 * pageInformationRenderer.getFullHeaderHeight(gc
			 * ,1,0,layout.getLineOffsets
			 * ()[breakLayoutLine],null,pageInformation); //TODO page index }
			 */
			/*
			 * Font pf = layout.getFont(); float ps = pf.size; double r =
			 * ((PrinterRenderer)printerRenderer).ratioY; float layoutRate =
			 * ((float)layout.getBounds().height /
			 * (float)originalLayout.getBounds().height); int fh = (int)
			 * (r*originalLayout.getFont().size);
			 * //(int)(((float)originalLayout.getBounds().height /
			 * (float)layout.getBounds().height) *
			 * originalLayout.getFont().size); Font nf = new
			 * Font(pf.getDevice(),pf.toString(),(int)fh,pf.style); nf.size =
			 * fh; fonts.add(nf);
			 */

			double rx = correctedRatioX; // ((PrinterRenderer)printerRenderer).ratioX;
			double ry = correctedRatioY;// ((PrinterRenderer)printerRenderer).ratioY;

			Transform transform = new Transform(gc.getDevice(), new float[] {
					(float) rx, 0, 0, (float) ry, 0, 0 });
			gc.setTransform(transform);
			// originalLayout.draw(gc,(int)((x + indents.get(index))
			//rx),(int)(y / ry));//(int)(y/ry));
			//gc.drawRectangle((int)(x /rx),(int) (y / ry),originalLayout.getBounds().width,originalLayout.getBounds().height);//(int)(y/ry)
			int pSpacingg= ((Renderer)screenRenderer).getParagraphSpacingInSymbols(index);
			for (int i = fromIndex; i < toIndex; i++) {
				 int localY = (int) (y / ry);
				TextLayout localLayout = ((Renderer) screenRenderer)
						.createLayoutForVisibleLine(originalLayout, i);
				
				Rectangle rForFun = localLayout.getBounds(); //Do not delete
				if(pSpacingg !=0 && i == 0){					
					TextLayoutOpsProvider.getInstance().setFirstLineSpacing(localLayout,localLayout,pSpacingg *(screenRenderer.getLineHeight()));
					//Test code
					//gc.drawRectangle((int)((x + indents.get(index))/rx) ,
					//		 (int)(y/ry),localLayout.getBounds().width,localLayout.getBounds().height);
				}				
				
				
				localLayout.draw(gc, (int) ((x + indents.get(index)) / rx),	localY);

				//Test code
				// gc.drawRectangle((int)((x + indents.get(index))/rx) ,
				// (int)(y/ry),localLayout.getBounds().width,localLayout.getBounds().height);
				// //

				int height = TextLayoutOpsProvider.getInstance().getLineHeight(originalLayout,i);// localLayout.getBounds().height;
				y += height * ry;
				/*
				 * TextLayout localLayout = ((PrinterRenderer) printerRenderer)
				 * .createLayoutForVisibleLine(layout, i, lineOffsets);
				 * 
				 * localLayout.setFont(nf);//layout.getFont());
				 * 
				 * localLayout.setFont(layout.getFont());
				 * localLayout.setAscent(layout.getAscent());
				 * localLayout.setDescent(layout.getDescent());
				 * TextLayoutOpsProvider.getInstance().forceComputeRuns(localLayout, gc); int shift =
				 * (int) Math.round(TextLayoutOpsProvider.getInstance().getLineY( originalLayout, i)
				 * ((PrinterRenderer) printerRenderer).getRatioY()); int
				 * shiftAmount; if (fromIndex == 0 && i < layout.getLineCount())
				 * shiftAmount = TextLayoutOpsProvider.getInstance().getLineY(layout, i) - (y -
				 * initialY); else if (fromIndex > 0) { shiftAmount =
				 * Math.max(fullScreenBreakSize / 2 + headerOffset -
				 * clientArea.y, headerOffset); } else { shiftAmount = shift -
				 * (y - initialY); if (breakLayoutLine == i) shiftAmount -=
				 * deshiftDelta; } if (shiftAmount > 0){
				 * TextLayoutOpsProvider.getInstance().shiftLines(localLayout, 0, shiftAmount); //
				 * delta = shiftAmount; }else{ // delta = 0; }
				 */
				/*
				 * gc.setForeground(foreground); localLayout.draw(gc, x +
				 * indents.get(index)/* - clientArea.x, y);
				 * 
				 * //code //gc.drawRectangle(x ,
				 * y,localLayout.getBounds().width,
				 * localLayout.getBounds().height); //// /* int height =
				 * localLayout.getBounds().height; y += height;
				 */
				localLayout.dispose();
			}
			/*
			 * for(Font f : fonts){ f.dispose(); }
			 */

			gc.setTransform(null);
			transform.dispose();
			return y - initialY;
		}

		void printDecorationEx(int page, boolean header, TextLayout layout,
				int pageOffset) {
			if (printerRenderer instanceof PrinterRenderer) {
				if (header) {
					((PrinterRenderer) printerRenderer)
							.getPageInformationRenderer().drawPageHeader(
									gc,
									((PrinterRenderer) printerRenderer)
											.getRatioX(), 0, page, pageOffset,
									null, pageInformation);
				} else {
					((PrinterRenderer) printerRenderer)
							.getPageInformationRenderer()
							.drawPageFooter(
									gc,
									((PrinterRenderer) printerRenderer)
											.getRatioX(),
									(int) (fullPageHeight - 40 * ((PrinterRenderer) printerRenderer)
											.getRatioY()), page, pageOffset,
									null, pageInformation);
					((PrinterRenderer) printerRenderer)
							.getPageInformationRenderer()
							.drawPageNumber(
									gc,
									((PrinterRenderer) printerRenderer)
											.getRatioX(),
									page
											* ((Renderer) printerRenderer)
													.getLinesPerPage(),
									(int) (fullPageHeight - 40 * ((PrinterRenderer) printerRenderer)
											.getRatioY()), pageOffset, null,
									pageInformation);
					/*
					 * ((PrinterRenderer)
					 * printerRenderer).getPageInformationRenderer
					 * ().drawPageFooter(gc, ((PrinterRenderer)
					 * printerRenderer).getRatioX(), clientArea.y +
					 * clientArea.height, page, pageOffset, null,
					 * pageInformation); ((PrinterRenderer)
					 * printerRenderer).getPageInformationRenderer
					 * ().drawPageNumber(gc, ((PrinterRenderer)
					 * printerRenderer).getRatioX(), page * ((Renderer)
					 * printerRenderer).getLinesPerPage(), clientArea.y +
					 * clientArea.height, pageOffset, null, pageInformation);
					 */
				}
			}
		}

		void printDecoration(int page, boolean header, TextLayout layout,
				TextLayout sourceLayout, int breakLayoutLine) {
			if (!(printerRenderer instanceof PrinterRenderer))
				super.printDecoration(page, header, layout);
			else {
				int pageOffset = ((Renderer) screenRenderer)
						.getPageOffset(page - 1);
				/*
				 * if (sourceLayout.getLineCount() > breakLayoutLine) pageOffset
				 * = sourceLayout.getLineOffsets()[breakLayoutLine]; else
				 * pageOffset = sourceLayout.getLineOffsets()[sourceLayout
				 * .getLineOffsets().length - 1];
				 */
				printDecorationEx(page - 1, header, layout, pageOffset);
			}
		}

		void init() {
			Rectangle trim = printer.computeTrim(0, 0, 0, 0);
			Point dpi = printer.getDPI();
			Point fakeScreenDPI = Display.getDefault().getDPI();
			Point realDPI = ((ExtendedStyledText) screenRenderer.styledText)
					.getScreenPPI();
			double rel = 1.0 * fakeScreenDPI.x / realDPI.x;

			printerFont = new Font(printer, fontData.getName(), (int) (fontData
					.getHeight() * rel), SWT.NORMAL);
			clientArea = printer.getClientArea();
			pageWidth = clientArea.width;
			// one inch margin around text
			clientArea.x = dpi.x + trim.x;
			clientArea.y = dpi.y + trim.y;
			clientArea.width -= (clientArea.x + trim.width);
			clientArea.height -= (clientArea.y + trim.height);

			int style = mirrored ? SWT.RIGHT_TO_LEFT : SWT.LEFT_TO_RIGHT;
			gc = new GC(printer, style);
			gc.setFont(printerFont);
			printerRenderer.setFont(printerFont, tabLength);
			int lineHeight = printerRenderer.getLineHeight();
			if (printOptions.header != null) {
				clientArea.y += lineHeight * 2;
				clientArea.height -= lineHeight * 2;
			}
			if (printOptions.footer != null) {
				clientArea.height -= lineHeight * 2;
			}

			// TODO not wrapped
			StyledTextContent content = printerRenderer.content;
			startLine = 0;
			endLine = singleLine ? 0 : content.getLineCount() - 1;
			PrinterData data = printer.getPrinterData();
			if (data.scope == PrinterData.PAGE_RANGE) {
				int pageSize = clientArea.height / lineHeight;// WRONG
				startLine = (startPage - 1) * pageSize;
			} else if (data.scope == PrinterData.SELECTION) {
				startLine = content.getLineAtOffset(selection.x);
				if (selection.y > 0) {
					endLine = content.getLineAtOffset(selection.x + selection.y
							- 1);
				} else {
					endLine = startLine - 1;
				}
			}
		}

		/**
		 * Starts a print job and prints the pages specified in the constructor.
		 */
		public void run() {
			String jobName = printOptions.jobName;
			if (jobName == null) {
				jobName = "Printing";
			}
			if (printer.startJob(jobName)) {
				init();
				print();
				dispose();
				printer.endJob();
			}
		}

		void print() {
			if (!(printerRenderer instanceof PrinterRenderer)
					|| !((Renderer) printerRenderer).isPagingEnabled()) {
				super.print();
				return;
			}
			ArrayList<Integer> additionalBreakLines = new ArrayList<Integer>();
			Color background = gc.getBackground();
			Color foreground = gc.getForeground();
			if (fullScreenBreakSize / 2 > clientArea.y)
				deshiftDelta = fullScreenBreakSize - clientArea.y;
			else
				deshiftDelta = fullScreenBreakSize;
			int paintY = 0;//clientArea.y; // 0
			int paintX = 0;// clientArea.x;
			int width = clientArea.width;
			page = startPage;
			int pageBottom = clientArea.y + clientArea.height;
			int orientation = gc.getStyle()
					& (SWT.RIGHT_TO_LEFT | SWT.LEFT_TO_RIGHT);
			TextLayout printLayout = null;
			pageInformationRenderer = ((PrinterRenderer) printerRenderer)
					.getPageInformationRenderer();
			if (printOptions.printLineNumbers || printOptions.header != null
					|| printOptions.footer != null) {
				printLayout = new TextLayout(printer);
				printLayout.setFont(printerFont);
			}
			if (printOptions.printLineNumbers) {
				int numberingWidth = 0;
				int count = endLine - startLine + 1;
				String[] lineLabels = printOptions.lineLabels;
				if (lineLabels != null) {
					for (int i = startLine; i < Math.min(count,
							lineLabels.length); i++) {
						if (lineLabels[i] != null) {
							printLayout.setText(lineLabels[i]);
							int lineWidth = printLayout.getBounds().width;
							numberingWidth = Math
									.max(numberingWidth, lineWidth);
						}
					}
				} else {
					StringBuffer buffer = new StringBuffer("0");
					while ((count /= 10) > 0)
						buffer.append("0");
					printLayout.setText(buffer.toString());
					numberingWidth = printLayout.getBounds().width;
				}
				numberingWidth += printMargin;
				if (numberingWidth > width)
					numberingWidth = width;
				paintX += numberingWidth;
				width -= numberingWidth;
			}
			int lastPageOffset = -1;
			ancents = new int[endLine + 1];
			Arrays.fill(ancents, 0);
			calculateRatios();
			for (int i = startLine; i <= endLine && page <= endPage; i++) {
				TextLayout layout = screenRenderer.getTextLayout(i);// printerRenderer.getTextLayout(i,
																	// orientation,
																	// width,
																	// lineSpacing);
				if (paintY == 0 /* clientArea.y */) {
					printer.startPage();
					/*
					 * printDecorationEx(page-1, true, printLayout, layout
					 * .getLineOffsets()[0]);
					 */
					printDecoration(page, true, printLayout, layout, layout
							.getLineOffsets()[0]);
				}
				Color lineBackground = printerRenderer.getLineBackground(i,
						background);
				int paragraphBottom = paintY
						+ (int) (layout.getBounds().height * ((PrinterRenderer) printerRenderer).ratioY);
				boolean needBreak = false;
				boolean needBreakFinally = false;
				breakLayoutLine = -1;
				if (((Renderer) /* printerRenderer */screenRenderer)
						.isPagingEnabled()) {
					int layoutLineCount = layout.getLineCount();
					int prevLineCountForPaging = ((Renderer) /* printerRenderer */screenRenderer)
							.getPrevLineCountForPaging(i);
					for (int a = 0; a < layoutLineCount; a++) {
						if (a == 0
								&& sourceContent instanceof IMarginedStyledTextContent) {
							needBreak = ((Renderer) /* printerRenderer */screenRenderer)
									.isPageBreak(
											prevLineCountForPaging
													- ((Renderer) /* printerRenderer */screenRenderer)
															.getParagraphSpacingInSymbols(i),
											prevLineCountForPaging);
							needBreak &= prevLineCountForPaging + a > 0;
							if (needBreak) {
								needBreakFinally = true;
								if (breakLayoutLine == -1)
									breakLayoutLine = a;
								else
									additionalBreakLines.add(a);
							}
						} else {
							needBreak = ((Renderer) /* printerRenderer */screenRenderer)
									.isPageBreak(prevLineCountForPaging + a,
											prevLineCountForPaging + a);
							needBreak &= prevLineCountForPaging + a > 0;
							if (needBreak) {
								needBreakFinally = true;
								if (breakLayoutLine == -1)
									breakLayoutLine = a;
								else
									additionalBreakLines.add(a);
							}
						}
					}
				} else {
					needBreak = paragraphBottom > pageBottom;
				}
				for (int j = 0; j < layout.getLineCount(); j++) {
					if (TextLayoutOpsProvider.getInstance().getLineHeight(layout, j) > fullScreenBreakSize && false)
						TextLayoutOpsProvider.getInstance().shiftLines(layout, j,
								-fullScreenBreakSize); // Remove illegal
					// pagebreaks
				}
				TextLayout originalLayout = screenRenderer.getTextLayout(i);
				if (!needBreakFinally
						|| breakLayoutLine >= originalLayout.getLineCount()) {
					int firstLineBreak = ((Renderer) printerRenderer)
							.getFirstLayoutLineShift(layout);
					if (i == 0 && false) {
						if (clientArea.y < firstLineBreak)
							TextLayoutOpsProvider.getInstance().setFirstLineSpacing(layout, layout,
									-clientArea.y);
						else
							TextLayoutOpsProvider.getInstance().setFirstLineSpacing(layout, layout,
									-firstLineBreak);
					}

					// normal case, the whole paragraph fits in the current page
					printLine(paintX, paintY, gc, foreground, lineBackground,
							layout, printLayout, i);
					paintY = paintY/* + layout.getBounds().height; */
							+ ancents[i];
				} else {
					if (breakLayoutLine == -1) {
						int lineCount = layout.getLineCount();
						while (paragraphBottom > pageBottom && lineCount > 0) {
							lineCount--;
							paragraphBottom -= TextLayoutOpsProvider.getInstance().getLineBounds(
									layout, lineCount).height// layout.getLineBounds(lineCount).height
									+ layout.getSpacing();
						}
						breakLayoutLine = lineCount - 1;
					}
					if (breakLayoutLine == 0
							&& additionalBreakLines.size() == 0) { // We need
						// second
						// condition,
						// if line
						// count in
						// print
						// layout
						// and
						// breakLayoutLine
						// is
						// grater,
						// than in
						// original
						// layout
						// the whole paragraph goes to the next page
						printDecoration(page, false, printLayout, layout,
								breakLayoutLine); // print footer
						printer.endPage();
						page++;
						if (page <= endPage) {
							printer.startPage();
							printDecoration(page, true, printLayout, layout,
									breakLayoutLine); // print header
							paintY = this.headerOffset;// clientArea.y -
														// tempDeshift;
							// TextLayoutOpsProvider.getInstance().shiftLines(layout, 0,
							// -Math.min(deshiftDelta,
							// TextLayoutOpsProvider.getInstance().getLineAscent(layout,0)));
							//int headerOffset = 0;
							/*
							 * if (pageInformation != null) headerOffset =
							 * pageInformationRenderer
							 * .getFullHeaderHeight(gc,1,page
							 * ,layout.getLineOffsets
							 * ()[breakLayoutLine],null,pageInformation);
							 */
							if (headerOffset > 0 && false) {
								TextLayoutOpsProvider.getInstance().shiftLines(layout,
										breakLayoutLine, headerOffset);
							}
							printLine(paintX, paintY, gc, foreground,
									lineBackground, layout, printLayout, i);
							paintY += /* layout.getBounds().height; */ancents[i];
						}
					} else {
						// draw paragraph top in the current page and paragraph
						// bottom in the next
						int height = paragraphBottom - paintY;
						// gc.setClipping(clientArea.x, paintY,
						// clientArea.width,
						// height);
						// TextLayoutOpsProvider.getInstance().shiftLines(layout, breakLayoutLine,
						// -Math.min(deshiftDelta,
						// TextLayoutOpsProvider.getInstance().getLineAscent(layout,breakLayoutLine)));
						int headerOffset = 0;
						/*
						 * if (pageInformation != null) headerOffset =
						 * pageInformationRenderer
						 * .getFullHeaderHeight(gc,1,page,
						 * layout.getLineOffsets()
						 * [breakLayoutLine],null,pageInformation);
						 */
						if(false){
							TextLayoutOpsProvider.getInstance().shiftLines(layout, breakLayoutLine,
								headerOffset);
						}
						printLinesFromTo(paintX, paintY, gc, foreground,
								lineBackground, layout, printLayout, i, 0,
								breakLayoutLine);
						gc.setClipping((Rectangle) null);
						printDecoration(page, false, printLayout, layout,
								breakLayoutLine); // draw footer
						printer.endPage();
						page++;
						if (page <= endPage) {
							printer.startPage();
							printDecoration(page, true, printLayout, layout,
									breakLayoutLine); // draw header
							// paintY = clientArea.y - height;

							paintY = this.headerOffset;// clientArea.y;

							// int layoutHeight = layout.getBounds().height;
							// int clipHeight = TextLayoutOpsProvider.getInstance().getLineY(layout,
							// breakLayoutLine);
							// gc.setClipping(clientArea.x, clientArea.y,
							// clientArea.width, layoutHeight - height);
							// gc.setClipping(clientArea.x, clientArea.y,
							// clientArea.width, clipHeight);
							int paintHeight = 0;
							int size = additionalBreakLines.size();
							if (size == 0) {
								paintHeight = printLinesFromTo(paintX, paintY,
										gc, foreground, lineBackground, layout,
										printLayout, i, breakLayoutLine, layout
												.getLineCount());
							} else {
								int firstLine = breakLayoutLine;
								int lastLine = additionalBreakLines.get(0);
								for (int j = 0; j <= size; j++) {
									paintHeight = printLinesFromTo(paintX,
											paintY, gc, foreground,
											lineBackground, layout,
											printLayout, i, firstLine, lastLine);
									firstLine = lastLine;
									if (j < size - 1)
										lastLine = additionalBreakLines
												.get(j + 1);
									else
										lastLine = layout.getLineCount();
									if (j < size) {
										printDecoration(page, false,
												printLayout, layout, firstLine /* breakLayoutLine */);
										printer.endPage();
										page++;
										printer.startPage();
										printDecoration(page, true,
												printLayout, layout, firstLine /* breakLayoutLine */);
									}
								}
								additionalBreakLines.clear();
							}
							gc.setClipping((Rectangle) null);
							paintY += paintHeight;
						}
					}
				}
				if (i == endLine/* && needBreakFinally */) // we need to save //
															// endPage
				// last page
				// starting offset
				// here
				{

					if (layout.getLineCount() > breakLayoutLine) {
						int pppos = breakLayoutLine > 0 ? breakLayoutLine
								: (layout.getLineCount() - 1);
						lastPageOffset = layout.getLineOffsets()[pppos];
					} else
						lastPageOffset = layout.getLineOffsets()[layout
								.getLineOffsets().length - 1];
				}
				// printerRenderer.disposeTextLayout(layout);
			}
			if (page <= endPage && paintY > clientArea.y) {
				// close partial page

				// printDecorationEx(page-1, false, printLayout,
				// lastPageOffset);
				printDecoration(page, false, printLayout, printLayout,
						lastPageOffset);
				printer.endPage();
			}
			if (printLayout != null)
				printLayout.dispose();
		}

		@SuppressWarnings("unchecked")
		void cacheLineData(StyledText styledText) {
			StyledTextRenderer renderer = styledText.renderer;
			renderer.copyInto(printerRenderer);
			fontData = styledText.getFont().getFontData()[0];
			tabLength = styledText.tabLength;
			int lineCount = printerRenderer.lineCount;
			if (styledText.isListening(LineGetBackground)
					|| (styledText.isBidi() && styledText
							.isListening(LineGetSegments))
					|| styledText.isListening(LineGetStyle)) {
				StyledTextContent content = printerRenderer.content;
				for (int i = 0; i < lineCount; i++) {
					String line = content.getLine(i);
					int lineOffset = content.getOffsetAtLine(i);
					StyledTextEvent event = styledText.getLineBackgroundData(
							lineOffset, line);
					if (event != null && event.lineBackground != null) {
						printerRenderer.setLineBackground(i, 1,
								event.lineBackground);
					}
					if (styledText.isBidi()) {
						// TODO FIX IT IN A WAY COMPATIBLE BETWEEN DIFFERENT
						// IMPLEMENTATIONS
						// int[] segments =
						// styledText.getBidiSegments(lineOffset,
						// line);
						// printerRenderer.setLineSegments(i, 1, segments);
					}
					event = styledText.getLineStyleData(lineOffset, line);
					if (event != null) {
						printerRenderer.setLineIndent(i, 1, event.indent);
						printerRenderer.setLineAlignment(i, 1, event.alignment);
						printerRenderer.setLineJustify(i, 1, event.justify);
						printerRenderer.setLineBullet(i, 1, event.bullet);
						StyleRange[] styles = event.styles;
						if (styles != null && styles.length > 0) {
							printerRenderer
									.setStyleRanges(event.ranges, styles);
						}
					}
				}
			}
			Point screenDPI = ((ExtendedStyledText) styledText).getScreenPPI();
			Point printerDPI = printer.getDPI();
			resources = new Hashtable();
			for (int i = 0; i < lineCount; i++) {
				Color color = printerRenderer.getLineBackground(i, null);
				if (color != null) {
					if (printOptions.printLineBackground) {
						Color printerColor = (Color) resources.get(color);
						if (printerColor == null) {
							printerColor = new Color(printer, color.getRGB());
							resources.put(color, printerColor);
						}
						printerRenderer.setLineBackground(i, 1, printerColor);
					} else {
						printerRenderer.setLineBackground(i, 1, null);
					}
				}
				int indent = printerRenderer.getLineIndent(i, 0);
				if (indent != 0) {
					printerRenderer.setLineIndent(i, 1, indent * printerDPI.x
							/ screenDPI.x);
				}
			}
			StyleRange[] styles = printerRenderer.styles;
			for (int i = 0; i < printerRenderer.styleCount; i++) {
				StyleRange style = styles[i];
				Font font = style.font;
				if (style.font != null) {
					Font printerFont = (Font) resources.get(font);
					if (printerFont == null) {
						printerFont = new Font(printer, font.getFontData());
						resources.put(font, printerFont);
					}
					style.font = printerFont;
				}
				Color color = style.foreground;
				if (color != null) {
					Color printerColor = (Color) resources.get(color);
					if (printOptions.printTextForeground) {
						if (printerColor == null) {
							printerColor = new Color(printer, color.getRGB());
							resources.put(color, printerColor);
						}
						style.foreground = printerColor;
					} else {
						style.foreground = null;
					}
				}
				color = style.background;
				if (color != null) {
					Color printerColor = (Color) resources.get(color);
					if (printOptions.printTextBackground) {
						if (printerColor == null) {
							printerColor = new Color(printer, color.getRGB());
							resources.put(color, printerColor);
						}
						style.background = printerColor;
					} else {
						style.background = null;
					}
				}
				if (!printOptions.printTextFontStyle) {
					style.fontStyle = SWT.NORMAL;
				}
				style.rise = style.rise * printerDPI.y / screenDPI.y;
				GlyphMetrics metrics = style.metrics;
				if (metrics != null) {
					metrics.ascent = metrics.ascent * printerDPI.y
							/ screenDPI.y;
					metrics.descent = metrics.descent * printerDPI.y
							/ screenDPI.y;
					metrics.width = metrics.width * printerDPI.x / screenDPI.x;
				}
			}
			lineSpacing = styledText.lineSpacing * printerDPI.y / screenDPI.y;
			if (printOptions.printLineNumbers) {
				printMargin = 3 * printerDPI.x / screenDPI.x;
			}
		}

	}

	private HashSet<IPageListener> pageListeners = new HashSet<IPageListener>();

	/**
	 * Indent of a line marker bullet
	 */
	private static final int BULLET_INDENT = 20;
	/**
	 * Top field size
	 */
	private static final int FIELD_SIZE = 10;

	public static final int BULLET_SPACE = 25;

	private static final String TOP_MARGIN = "TOP_MARGIN";
	private static final String BOTTOM_MARGIN = "BOTTOM_MARGIN";
	private static final String LEFT_FIELD_SIZE = "LEFT_FIELD_SIZE";
	private static final String RIGHT_FIELD_SIZE = "RIGHT_FIELD_SIZE";
	private static final String RENDERER_CLASS = "RENDERER_CLASS";
	protected static final int EXTRA_PAGE_SIZE = 20;

	protected IPageInformation pageInformation;

	protected int oldLineCount;
	protected int firstLineIdx;
	protected int defaultMargin = 0;

	// protected int pageWidth = -1;
	protected int pageWidth = 800;
	/**
	 * Size of left page field
	 */
	protected int leftFieldSize = 10;

	protected int caretHeight = 10;
	/**
	 * Size of right page field
	 */
	protected int rightFieldSize = 10;
	protected RGB fieldColor = new RGB(128, 128, 128);
	protected double scalingFactor = 1.0;
	protected Cursor defaultCursor, arrowCursor;
	protected IPPIResolver screenResolver, printerResolver;

	final TextChangeListener textListener = new TextChangeListener() {

		public void textChanged(TextChangedEvent event) {
			final int lineCount = getLineCount();
			if (oldLineCount != lineCount) {
				int delta = lineCount - oldLineCount;
				if (delta > 0) {
					int firstIndent = 0;
					if (lineIndents.size() > firstLineIdx)
						firstIndent = lineIndents.get(firstLineIdx);
					else {
						for (int i = lineIndents.size(); i < firstLineIdx + 1; i++)
							lineIndents.add(firstIndent);
					}
					for (int i = firstLineIdx + 1; i < firstLineIdx + 1 + delta; i++) {
						lineIndents.add(firstLineIdx + 1, firstIndent);
					}
				} else if (delta < 0) {
					for (int i = firstLineIdx + 1; i < firstLineIdx + 1
							+ (-delta)
							&& lineIndents.size() > firstLineIdx + 1; i++) {
						lineIndents.remove(firstLineIdx + 1);
					}
				}
				if (renderer instanceof ExtendedRenderer) {
					((ExtendedRenderer) renderer).textChanged(firstLineIdx,
							delta);
				}
			}
		}

		public void textChanging(TextChangingEvent event) {
			oldLineCount = getLineCount();
			firstLineIdx = getLineAtOffset(event.start);
		}

		public void textSet(TextChangedEvent event) {
			lineIndents = new ArrayList<Integer>(getLineCount());
		}
	};

	protected ArrayList<Integer> lineIndents;
	protected int averageCharWidth;
	protected int averageCharHeight;

	private boolean allowParagraphSpacing = true;

	{
		lineIndents = new ArrayList<Integer>(getLineCount());

		/*
		 * addVerifyListener(new VerifyListener(){
		 * 
		 * public void verifyText(VerifyEvent e) { oldLineCount =
		 * getLineCount(); firstLineIdx = getLineAtOffset(e.start); }
		 * 
		 * });
		 * 
		 * addModifyListener(new ModifyListener(){
		 * 
		 *  public void modifyText(ModifyEvent e) { final int lineCount
		 * = getLineCount(); if (oldLineCount != lineCount) { int delta =
		 * lineCount - oldLineCount; if (delta > 0) { for (int i = firstLineIdx
		 * + 1; i < firstLineIdx + 1 + delta; i++) lineIndents.add(firstLineIdx
		 * + 1,0); } else if (delta < 0) { for (int i = firstLineIdx + 1; i <
		 * firstLineIdx + 1 + (-delta); i++) lineIndents.remove(firstLineIdx +
		 * 1); } } } });
		 */

		getContent().addTextChangeListener(textListener);
	}

	public void setPageInformation(IPageInformation pageInformation) {

		Point offset = getSelection();

		this.pageInformation = pageInformation;
		if (pageInformation != null) {
			int pageWidth2 = pageInformation.getPageWidth();
			if (pageWidth2 > 0) {
				pageWidth = pageWidth2;
			}
			int linePerPage = pageInformation.getLinesPerPage();

			if (renderer instanceof Renderer) {
				Renderer r = (Renderer) renderer;
				r.pagingEnabled = true;
				if (linePerPage > 0) {
					r.linesPerPage = linePerPage; // 10;
				}
				// int pageBreakSize = pageInformation.getPageBreakSize();
				// if (pageBreakSize > 0) {
				// //r.breakSize = pageBreakSize; TODO
				// }
				IPageInformationRenderer pageInformationRenderer = pageInformation
						.getPageInformationRenderer();
				if (pageInformationRenderer != null) {
					r.pageInformationRenderer = pageInformationRenderer;
				}
				if (r.pageInformationRenderer == null) {
					r.pageInformationRenderer = new PageInformationRenderer(r);
				}
			}
		} else {
			if (renderer instanceof Renderer) {
				Renderer r = (Renderer) renderer;
				r.pagingEnabled = false;
				r.pageInformationRenderer = new PageInformationRenderer(r);
			}
		}

		// setSelection(offset);
		reset();
		redraw();
	}

	boolean updateScheduled = false;

	private Runnable updateRunnable = new Runnable() {

		public void run() {
			updateScheduled = false;
			if (!ExtendedStyledText.this.isDisposed()) {
				redraw();
				update();
			}
		}
	};

	ILineBackgroundPainter lineBackgroundPainter;

	public ILineBackgroundPainter getLineBackgroundPainter() {
		return lineBackgroundPainter;
	}

	public void setLineBackgroundPainter(
			ILineBackgroundPainter lineBackgroundPainter) {
		this.lineBackgroundPainter = lineBackgroundPainter;
	}

	public void scroll(int destX, int destY, int x, int y, int width,
			int height, boolean all) {
		super.scroll(destX, destY, x, y, width, height, all);
		deferredRepaint();
	}

	private void deferredRepaint() {
		if (!updateScheduled) {
			updateScheduled = true;
			Display.getCurrent().asyncExec(updateRunnable);
		}
	}

	public void setContent(StyledTextContent newContent) {
		if (newContent instanceof IMarginedStyledTextContent) {
			IMarginedStyledTextContent mc = (IMarginedStyledTextContent) newContent;
			int baseMargin = mc.getBaseMargin();
			topMargin = baseMargin;
			leftMargin = baseMargin;
			rightMargin = baseMargin;
			bottomMargin = baseMargin;
		}
		getContent().removeTextChangeListener(textListener);
		super.setContent(newContent);
		newContent.addTextChangeListener(textListener);
		final TextChangedEvent event = new TextChangedEvent(newContent);
		textListener.textSet(event);

		redraw();
		Display.getCurrent().asyncExec(new Runnable() {

			public void run() {
				if (!ExtendedStyledText.this.isDisposed())
					redraw();
				/*
				 * if ((ExtendedStyledText.this.getStyle() & SWT.NO_FOCUS) == 0)
				 * UIUtils.setFocus(handle);
				 */
				ExtendedStyledText extendedStyledText = ExtendedStyledText.this;
				if (!extendedStyledText.isDisposed() && (extendedStyledText.getStyle() & SWT.NO_FOCUS) == 0)
					UIUtils.setFocus(extendedStyledText);
			}
		});
	}

	public void putIndent(int line, int indent) {
		if (line == lineIndents.size())
			lineIndents.add(indent);
		else
			lineIndents.set(line, indent);
		showCaret();
	}

	public void putRightIndent(int line, int indent) {
		if (renderer instanceof ExtendedRenderer) {
			((ExtendedRenderer) renderer).putRightIndent(line, indent);
			showCaret();
		} else
			throw new UnsupportedOperationException(
					"Only special page-renderer supports right indenting.");
	}

	public int getIndent(int line) {
		if (content instanceof IMarginedStyledTextContent) {
			IMarginedStyledTextContent marginedContent = (IMarginedStyledTextContent) content;
			return (int) Math.round(marginedContent.getMarginAt(line)
					* scalingFactor);
		}
		if (line >= lineIndents.size())
			return 0;
		return lineIndents.get(line);
	}

	public int getRightIndent(int line) {
		if (renderer instanceof ExtendedRenderer)
			return ((ExtendedRenderer) renderer).getRightIndent(line);
		else
			return 0;
	}

	int getVisualLineIndex(TextLayout layout, int offsetInLine) {
		int lineIndex = layout.getLineIndex(offsetInLine);
		int[] offsets = layout.getLineOffsets();
		if (lineIndex != 0 && offsetInLine == offsets[lineIndex]) {
			int lineY = TextLayoutOpsProvider.getInstance().getLineBounds(layout, lineIndex).y;// layout.getLineBounds(lineIndex).y;
			int caretY = getCaret().getLocation().y
					- getLinePixel(getCaretLine());
			if (lineY > caretY)
				lineIndex--;
		}
		return lineIndex;
	}

	void setCaretLocation() {
		if (!(renderer instanceof Renderer)) {
			super.setCaretLocation();
			return;
		}
		Point newCaretPos = getPointAtOffset(caretOffset);
		caretHeight = ((Renderer) renderer).getCaretHeight(content
				.getLineAtOffset(caretOffset));
		setCaretLocation(newCaretPos, getCaretDirection());
	}

	public ExtendedStyledText(Composite parent, int style,
			HashMap<String, String> settings) {
		super(parent, (style | SWT.DOUBLE_BUFFERED | SWT.H_SCROLL) & ~SWT.WRAP);
		setWordWrap((style & SWT.WRAP) != 0);
		if (settings != null) {
			configure(settings);
		} else {
			setUseBasicRenderer(false);
			topMargin = FIELD_SIZE;
			bottomMargin = FIELD_SIZE;
			leftFieldSize = FIELD_SIZE;
			rightFieldSize = FIELD_SIZE;
		}
		setVariableLineHeight();
		// setPagingEnabled(true);
		checkHorizontalScroll();
		defaultCursor = getCursor();
		arrowCursor = new Cursor(getDisplay(), SWT.CURSOR_ARROW);
		addMouseMoveListener(new MouseMoveListener() {
			protected boolean isArrow = false;

			public void mouseMove(MouseEvent e) {
				if (leftFieldSize > 0) {
					int offset = horizontalScrollOffset;
					int realFieldSize = (int) Math.round(leftFieldSize
							* scalingFactor);
					if (e.x < realFieldSize - offset)
						if (!isArrow) {
							isArrow = true;
							ExtendedStyledText.this.setCursor(arrowCursor);
						} else if (isArrow) {
							isArrow = false;
							ExtendedStyledText.this.setCursor(defaultCursor);
						}
				} else if (isArrow) {
					isArrow = false;
					ExtendedStyledText.this.setCursor(defaultCursor);
				}
				if (rightFieldSize > 0) {
					double offset = (pageWidth == -1 ? clientAreaWidth
							- rightFieldSize : pageWidth)
							* scalingFactor - horizontalScrollOffset;
					if (offset < clientAreaWidth && e.x > offset)
						if (!isArrow) {
							isArrow = true;
							ExtendedStyledText.this.setCursor(arrowCursor);
						} else if (isArrow) {
							isArrow = false;
							ExtendedStyledText.this.setCursor(defaultCursor);
						}
				} else if (isArrow)
					ExtendedStyledText.this.setCursor(defaultCursor);
			}
		});
		final Point screenSize = TextLayoutOpsProvider.getInstance().getScreenSize();

		if (screenSize != null) {
			screenResolver = new IPPIResolver() {

				public Point getPPI() {
					Rectangle bounds = Display.getDefault().getBounds();
					int x = (int) Math.round(bounds.width * 1.0 / screenSize.x
							* 25.4);
					int y = (int) Math.round(bounds.height * 1.0 / screenSize.y
							* 25.4);
					return new Point(x, y);
				}
			};
		}
		// TODO FOR TESTS
		//FontData fontData = new FontData("Courier", 75, SWT.NONE); //$NON-NLS-1$ // 12, SWT.NONE
		// Font defaultFont = new Font(parent.getDisplay(), fontData);
		// setFont(defaultFont);
	}

	public ExtendedStyledText(Composite parent, int style) {
		this(parent, style, null);
	}

	private void configure(HashMap<String, String> settings) {
		String tmp;
		tmp = settings.get(TOP_MARGIN);
		if (tmp != null)
			topMargin = Integer.parseInt(tmp);
		else
			topMargin = FIELD_SIZE;
		tmp = settings.get(BOTTOM_MARGIN);
		if (tmp != null)
			bottomMargin = Integer.parseInt(tmp);
		else
			bottomMargin = FIELD_SIZE;
		tmp = settings.get(RENDERER_CLASS);
		if (tmp != null)
			createRenderer(tmp);
		else
			setUseBasicRenderer(true);
		tmp = settings.get(LEFT_FIELD_SIZE);
		if (tmp != null)
			leftFieldSize = Integer.parseInt(tmp);
		else
			leftFieldSize = 0;
		tmp = settings.get(RIGHT_FIELD_SIZE);
		if (tmp != null)
			rightFieldSize = Integer.parseInt(tmp);
		else
			rightFieldSize = 0;
		tmp = settings.get(BOTTOM_MARGIN);
		if (tmp != null)
			bottomMargin = Integer.parseInt(tmp);
	}

	/**
	 * Sets whether the widget wraps lines.
	 * <p>
	 * This overrides the creation style bit SWT.WRAP.
	 * </p>
	 * 
	 * @param wrap
	 *            true=widget wraps lines, false=widget does not wrap lines
	 * @since 2.0
	 */
	public void setWordWrap(boolean wrap) {
		checkWidget();
		if ((getStyle() & SWT.SINGLE) != 0)
			return;
		if (wordWrap == wrap)
			return;
		wordWrap = wrap;
		setVariableLineHeight();
		resetCache(0, content.getLineCount());
		horizontalScrollOffset = 0;

		setScrollBars(true);
		setCaretLocation();
		super.redraw();
	}

	public void createRenderer(String rendererClassName) {
		Class<?> forName;
		try {
			forName = Class.forName(rendererClassName);
			Constructor<?> constructor = forName.getConstructor(new Class[] {
					Device.class, StyledText.class });
			renderer = (StyledTextRenderer) constructor.newInstance(this
					.getDisplay(), this);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		renderer.setContent(content);
		renderer.setFont(getFont(), tabLength);
	}

	public void setUseBasicRenderer(boolean basic) {
		if (basic) {
			renderer = new BasicRenderer(getDisplay(), this);
		} else {
			renderer = new Renderer(getDisplay(), this);
		}
		renderer.setContent(content);
		renderer.setFont(getFont(), tabLength);
	}

	/**
	 * Used for specifying custom {@link StyledText} renderer Must be called on
	 * initialization, before actual paintin or setting content
	 * 
	 * @param renderer
	 *            {@link StyledTextRenderer} indtance
	 */
	public void setRenderer(StyledTextRenderer renderer) {
		this.renderer = renderer;
	}

	public StyledTextRenderer getRenderer() {
		return renderer;
	}

	/**
	 * Scrolls text to the right to use new space made available by a resize.
	 */
	void claimRightFreeSpace() {
		int newHorizontalOffset = Math.max(0, renderer.getWidth()
				- (clientAreaWidth - leftMargin(-1) - rightMargin(-1)));
		if (newHorizontalOffset < horizontalScrollOffset) {
			// item is no longer drawn past the right border of the client area
			// align the right end of the item with the right border of the
			// client area (window is scrolled right).
			scrollHorizontal(newHorizontalOffset - horizontalScrollOffset, true);
			setScrollBars(true);
		}
	}

	protected void checkHorizontalScroll() {
		if (pageWidth == -1) {
			return;
		}
		if (getHorizontalBar() == null)
			return;
		long realWidth = Math.round(pageWidth * scalingFactor)
				+ getAdditionalPageWidth();
		if (realWidth > clientAreaWidth) {
			getHorizontalBar().setVisible(true);
			// getHorizontalBar().setMaximum((int) realWidth);
			getHorizontalBar().setMaximum((int) ( Math.round(pageWidth * scalingFactor + leftFieldSize)));//realWidth / 2));
		} else{
			getHorizontalBar().setVisible(false);
		}
	}

	boolean scrollHorizontal(int pixels, boolean adjustScrollBar) {
		if (pixels == 0) {
			return false;
		}
		ScrollBar horizontalBar = getHorizontalBar();

		if (horizontalBar != null && adjustScrollBar) {

			horizontalBar.setSelection(horizontalScrollOffset + pixels);
		}
		int scrollHeight = clientAreaHeight - topMargin - bottomMargin;
		if (pixels > 0) {
			int leftMargin = leftMargin(-1);
			int sourceX = leftMargin + pixels;
			int scrollWidth = clientAreaWidth - sourceX - rightMargin;
			if (scrollWidth > 0) {
				scroll(leftMargin, topMargin, sourceX, topMargin, scrollWidth,
						scrollHeight, true);
			}
			if (sourceX > scrollWidth) {
				// super.redraw(leftMargin + scrollWidth, topMargin, pixels -
				// scrollWidth, scrollHeight, true);
				super.redraw();
			}
		} else {
			int destinationX = leftMargin - pixels;
			int scrollWidth = clientAreaWidth - destinationX - rightMargin;
			if (scrollWidth > 0) {
				scroll(destinationX, topMargin, leftMargin, topMargin,
						scrollWidth, scrollHeight, true);
			}
			if (destinationX > scrollWidth) {
				super.redraw(leftMargin + scrollWidth, topMargin, -pixels
						- scrollWidth, scrollHeight, true);
			}
		}
		horizontalScrollOffset += pixels;
		setCaretLocation();
		if (horizontalBar != null && !horizontalBar.isVisible()) {
			calculateScrollBars();
		}
		return true;
	}

	protected int rightMargin(int i) {
		// return rightFieldSize + rightMargin;
		return rightMargin;
	}

	protected int leftMargin(int i) {

		if (i == -1) {
			return (int) ((leftFieldSize + leftMargin) * scalingFactor);
		}
		int k = 0;
		if (content instanceof IMarginedStyledTextContent) {
			IMarginedStyledTextContent styledTextContent = (IMarginedStyledTextContent) content;
			k += styledTextContent.getMarginAt(i);// * averageCharWidth;TODO
			// Should be implemented on
			// another level
		} else if (lineIndents != null) {
			if (i >= lineIndents.size()) {
				for (int l = lineIndents.size(); l <= i; l++)
					lineIndents.add(0);
			}
			int value = lineIndents.get(i);
			k += value;
		}

		Bullet lineBullet = getLineBullet(i);
		if (lineBullet != null) {
			return (int) (scalingFactor * (leftFieldSize + leftMargin
					+ BULLET_INDENT + k + defaultMargin));
		}
		return (int) (scalingFactor * (leftFieldSize + leftMargin + k + defaultMargin));
	}

	/**
	 * @return the defaultMargin
	 */
	public int getDefaultMargin() {
		return defaultMargin;
	}

	/**
	 * @param defaultMargin
	 *            the defaultMargin to set
	 */
	public void setDefaultMargin(int defaultMargin) {
		this.defaultMargin = defaultMargin;
	}

	public void setLineSpacing(int startLine, int lineCount, int spacing) {
		checkWidget();
		// if (isListening(LineGetStyle)) return; //TODO Why do we need this???
		if (!(renderer instanceof ExtendedRenderer)) {
			throw new UnsupportedOperationException(
					"Only ExtendedRenderer supports setLineSpacing.");
		}
		if (startLine < 0 || startLine + lineCount > content.getLineCount()) {
			SWT
					.error(
							SWT.ERROR_INVALID_ARGUMENT,
							new IllegalArgumentException(
									"Error in text range: startLine < 0 or startLine + count is greater than total line count"));
		}
		resetCache(startLine, lineCount);
		((ExtendedRenderer) renderer).setLineSpacing(startLine, lineCount,
				spacing);
		redrawLines(startLine, lineCount);
		int caretLine = getCaretLine();
		if (startLine <= caretLine && caretLine < startLine + lineCount) {
			setCaretLocation();
		}
	}

	public int getLineSpacing(int index) {
		checkWidget();
		if (index < 0 || index > content.getLineCount()) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
		return renderer.getTextLayout(index).getSpacing();
	}

	public Point computeSize(int wHint, int hHint, boolean changed) {
		checkWidget();
		int lineCount = (getStyle() & SWT.SINGLE) != 0 ? 1 : content
				.getLineCount();
		int width = 0;
		int height = 0;
		if (wHint == SWT.DEFAULT || hHint == SWT.DEFAULT) {
			Display display = getDisplay();
			int maxHeight = display.getClientArea().height;
			for (int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
				TextLayout layout = renderer.getTextLayout(lineIndex);
				int wrapWidth = layout.getWidth();
				if (wordWrap)
					layout.setWidth(wHint == 0 ? 1 : wHint);
				Rectangle rect = layout.getBounds();
				height += rect.height;
				width = Math.max(width, rect.width);
				layout.setWidth(wrapWidth);
				renderer.disposeTextLayout(layout);
				if (isFixedLineHeight() && height > maxHeight)
					break;
			}
			if (isFixedLineHeight()) {
				height = lineCount * renderer.getLineHeight();
			}
		}
		// Use default values if no text is defined.
		if (width == 0)
			width = DEFAULT_WIDTH;
		if (height == 0)
			height = DEFAULT_HEIGHT;
		if (wHint != SWT.DEFAULT)
			width = wHint;
		if (hHint != SWT.DEFAULT)
			height = hHint;
		int wTrim = leftMargin(-1) + rightMargin + getCaretWidth();
		int hTrim = topMargin + bottomMargin;
		Rectangle rect = computeTrim(0, 0, width + wTrim, height + hTrim);
		return new Point(rect.width, rect.height);
	}

	/**
	 * A mouse move event has occurred. See if we should start autoscrolling. If
	 * the move position is outside of the client area, initiate autoscrolling.
	 * Otherwise, we've moved back into the widget so end autoscrolling.
	 */
	void doAutoScroll(Event event) {
		if (event.y > clientAreaHeight) {
			doAutoScroll(SWT.DOWN, event.y - clientAreaHeight);
		} else if (event.y < 0) {
			doAutoScroll(SWT.UP, -event.y);
		} else if (event.x < leftMargin(-1) && !wordWrap) {
			doAutoScroll(ST.COLUMN_PREVIOUS, leftMargin(-1) - event.x);
		} else if (event.x > clientAreaWidth - leftMargin(-1) - rightMargin(-1)
				&& !wordWrap) {
			doAutoScroll(ST.COLUMN_NEXT, event.x
					- (clientAreaWidth - leftMargin(-1) - rightMargin(-1)));
		} else {
			endAutoScroll();
		}
	}

	Rectangle getBoundsAtOffset(int offset) {
		int lineIndex = content.getLineAtOffset(offset);
		int lineOffset = content.getOffsetAtLine(lineIndex);
		String line = content.getLine(lineIndex);
		Rectangle bounds;
		if (line.length() != 0) {
			int offsetInLine = offset - lineOffset;
			TextLayout layout = renderer.getTextLayout(lineIndex);
			bounds = TextLayoutOpsProvider.getInstance()
					.getBounds(layout, offsetInLine, offsetInLine);// layout.getBounds(offsetInLine,
																	// offsetInLine);
			renderer.disposeTextLayout(layout);
		} else {
			bounds = new Rectangle(0, 0, 0, renderer.getLineHeight());
		}
		if (offset == caretOffset) {
			int lineEnd = lineOffset + line.length();
			if (offset == lineEnd && caretAlignment == PREVIOUS_OFFSET_TRAILING) {
				bounds.width += getCaretWidth();
			}
		}
		bounds.x += leftMargin(lineIndex) - horizontalScrollOffset;
		bounds.y += super.getLinePixel(lineIndex); // Because we should apply
		// scale later
		// applyScale(bounds);
		return bounds;
	}

	/*
	 * boolean scrollHorizontal(int pixels, boolean adjustScrollBar) { if
	 * (horizontalScrollOffset + pixels < 0) return false; return
	 * super.scrollHorizontal(pixels, adjustScrollBar); }
	 */

	void doLineUp(boolean select) {
		int caretLine = getCaretLine(), y = 0;
		boolean firstLine = false;
		if (wordWrap) {
			int lineOffset = content.getOffsetAtLine(caretLine);
			int offsetInLine = caretOffset - lineOffset;
			TextLayout layout = renderer.getTextLayout(caretLine);
			int lineIndex = getVisualLineIndex(layout, offsetInLine);
			if (lineIndex == 0) {
				firstLine = caretLine == 0;
				if (!firstLine) {
					caretLine--;
					y = renderer.getLineHeight(caretLine) - 1;
				}
			} else {
				y = TextLayoutOpsProvider.getInstance().getLineBounds(layout, lineIndex - 1).y;// layout.getLineBounds(lineIndex
																			// -
																			// 1).y;
			}
			renderer.disposeTextLayout(layout);
		} else {
			firstLine = caretLine == 0;
			caretLine--;
		}
		if (firstLine) {
			if (select)
				setCaretOffset(0);
		} else {
			int[] alignment = new int[1];
			int offset = getOffsetAtPoint(columnX, y, caretLine, alignment);
			setCaretOffset(offset, alignment[0]);
			// setCaretOffset(getOffsetAtPoint(columnX, y, caretLine));
		}
		int oldColumnX = columnX;
		int oldHScrollOffset = horizontalScrollOffset;
		if (select)
			setMouseWordSelectionAnchor();
		showCaret();
		if (select)
			doSelection(ST.COLUMN_PREVIOUS);
		int hScrollChange = oldHScrollOffset - horizontalScrollOffset;
		columnX = oldColumnX + hScrollChange;
		redraw();
	}

	void handleResize(Event event) {
		int oldHeight = clientAreaHeight;
		int oldWidth = clientAreaWidth;
		Rectangle clientArea = getClientArea();
		clientAreaHeight = clientArea.height;
		clientAreaWidth = clientArea.width;
		/* Redraw the old or new right/bottom margin if needed */
		if (oldWidth != clientAreaWidth) {
			if (rightMargin > 0) {
				int x = (oldWidth < clientAreaWidth ? oldWidth
						: clientAreaWidth)
						- rightMargin;
				super.redraw(x, 0, rightMargin, oldHeight, false);
			}
		}
		if (oldHeight != clientAreaHeight) {
			if (bottomMargin > 0) {
				int y = (oldHeight < clientAreaHeight ? oldHeight
						: clientAreaHeight)
						- bottomMargin;
				super.redraw(0, y, oldWidth, bottomMargin, false);
			}
		}
		if (wordWrap) {
			if (oldWidth != clientAreaWidth) {
				if (pageWidth == -1) {
					renderer.reset(0, content.getLineCount());
					verticalScrollOffset = -1;
					renderer.calculateIdle();
				}
				super.redraw();
			}
			if (oldHeight != clientAreaHeight) {
				if (oldHeight == 0)
					topIndexY = 0;
				setScrollBars(true);
			}
			setCaretLocation();
		}
		{
			renderer.calculateClientArea();
			setScrollBars(true);
			claimRightFreeSpace();
			// StyledText allows any value for horizontalScrollOffset when
			// clientArea is zero
			// in setHorizontalPixel() and setHorisontalOffset(). Fixes bug
			// 168429.
			if (clientAreaWidth != 0) {
				ScrollBar horizontalBar = getHorizontalBar();
				if (horizontalBar != null && horizontalBar.getVisible()) {
					if (horizontalScrollOffset != horizontalBar.getSelection()) {
						horizontalBar.setSelection(horizontalScrollOffset);
						horizontalScrollOffset = horizontalBar.getSelection();
					}
				}
			}
		}
		updateCaretVisibility();
		claimBottomFreeSpace();
		setAlignment();
		checkHorizontalScroll();
		// TODO FIX TOP INDEX DURING RESIZE
		// if (oldHeight != clientAreaHeight || wordWrap) {
		// calculateTopIndex(0);
		// }
	}

	/**
	 * Returns the offset at the specified x location in the specified line.
	 * 
	 * @param x
	 *            x location of the mouse location
	 * @param line
	 *            line the mouse location is in
	 * @return the offset at the specified x location in the specified line,
	 *         relative to the beginning of the document
	 */
	int getOffsetAtPoint(int x, int y, int lineIndex) {
		TextLayout layout = renderer.getTextLayout(lineIndex);
		x += horizontalScrollOffset - leftMargin(lineIndex);
		int[] trailing = new int[1];
		int offsetInLine = layout.getOffset(x, y, trailing);
		caretAlignment = OFFSET_LEADING;
		if (trailing[0] != 0) {
			int lineInParagraph = layout.getLineIndex(offsetInLine
					+ trailing[0]);
			int lineStart = layout.getLineOffsets()[lineInParagraph];
			if (offsetInLine + trailing[0] == lineStart) {
				offsetInLine += trailing[0];
				caretAlignment = PREVIOUS_OFFSET_TRAILING;
			} else {
				String line = content.getLine(lineIndex);
				int level;
				int offset = offsetInLine;
				while (offset > 0 && Character.isDigit(line.charAt(offset)))
					offset--;
				if (offset == 0 && Character.isDigit(line.charAt(offset))) {
					level = isMirrored() ? 1 : 0;
				} else {
					level = layout.getLevel(offset) & 0x1;
				}
				offsetInLine += trailing[0];
				int trailingLevel = layout.getLevel(offsetInLine) & 0x1;
				if ((level ^ trailingLevel) != 0) {
					caretAlignment = PREVIOUS_OFFSET_TRAILING;
				} else {
					caretAlignment = OFFSET_LEADING;
				}
			}
		}
		renderer.disposeTextLayout(layout);
		return offsetInLine + content.getOffsetAtLine(lineIndex);
	}

	int getOffsetAtPoint(int x, int y) {
		int lineIndex = getLineIndex(y);
		y -= getLinePixel(lineIndex);

		return getOffsetAtPoint(x, y, lineIndex);
	}

	int getOffsetAtPoint(int x, int y, int[] trailing, boolean inTextOnly) {
		if (inTextOnly && y + getVerticalScrollOffset() < 0
				|| x + horizontalScrollOffset < 0) {
			return -1;
		}
		// if (inTextOnly && y > height) {
		// return -1;
		// }
		int lineIndex = getLineIndex(y);
		int lineOffset = content.getOffsetAtLine(lineIndex);
		TextLayout layout = renderer.getTextLayout(lineIndex);
		x += horizontalScrollOffset - leftMargin(lineIndex);
		y -= getLinePixel(lineIndex);
		int offset = layout.getOffset(x, y, trailing);
		renderer.disposeTextLayout(layout);
		// if (inTextOnly && !(rect.x <= x && x <= (rect.x + rect.width))) {
		// return -1;
		// }
		return offset + lineOffset;
	}

	int getOffsetAtPoint(int x, int y, int lineIndex, int[] alignment) {
		TextLayout layout = renderer.getTextLayout(lineIndex);
		x += horizontalScrollOffset - leftMargin(lineIndex);
		int[] trailing = new int[1];
		int offsetInLine = layout.getOffset(x, y, trailing);
		if (alignment != null)
			alignment[0] = OFFSET_LEADING;
		if (trailing[0] != 0) {
			int lineInParagraph = layout.getLineIndex(offsetInLine
					+ trailing[0]);
			int lineStart = layout.getLineOffsets()[lineInParagraph];
			if (offsetInLine + trailing[0] == lineStart) {
				offsetInLine += trailing[0];
				if (alignment != null)
					alignment[0] = PREVIOUS_OFFSET_TRAILING;
			} else {
				String line = content.getLine(lineIndex);
				int level = 0;
				if (alignment != null) {
					int offset = offsetInLine;
					while (offset > 0 && Character.isDigit(line.charAt(offset)))
						offset--;
					if (offset == 0 && Character.isDigit(line.charAt(offset))) {
						level = isMirrored() ? 1 : 0;
					} else {
						level = layout.getLevel(offset) & 0x1;
					}
				}
				offsetInLine += trailing[0];
				if (alignment != null) {
					int trailingLevel = layout.getLevel(offsetInLine) & 0x1;
					if ((level ^ trailingLevel) != 0) {
						alignment[0] = PREVIOUS_OFFSET_TRAILING;
					} else {
						alignment[0] = OFFSET_LEADING;
					}
				}
			}
		}
		renderer.disposeTextLayout(layout);
		return offsetInLine + content.getOffsetAtLine(lineIndex);
	}

	Point getPointAtOffset(int offset) {
		int lineIndex = content.getLineAtOffset(offset);
		String line = content.getLine(lineIndex);
		int lineOffset = content.getOffsetAtLine(lineIndex);
		int offsetInLine = offset - lineOffset;
		int lineLength = line.length();
		if (lineIndex < content.getLineCount() - 1) {
			int endLineOffset = content.getOffsetAtLine(lineIndex + 1) - 1;
			if (lineLength < offsetInLine && offsetInLine <= endLineOffset) {
				offsetInLine = lineLength;
			}
		}
		Point point = null;

		TextLayout layout = renderer.getTextLayout(lineIndex);

		if (lineLength != 0 && offsetInLine <= lineLength) {
			if (offsetInLine == lineLength) {
				point = TextLayoutOpsProvider.getInstance().getLocation(layout, offsetInLine - 1,
						true);// layout.getLocation(offsetInLine - 1, true);
			} else {
				switch (caretAlignment) {
				case OFFSET_LEADING:
					point = TextLayoutOpsProvider.getInstance().getLocation(layout, offsetInLine,
							false);// layout.getLocation(offsetInLine, false);
					break;
				case PREVIOUS_OFFSET_TRAILING:
				default:
					int[] lineOffsets = layout.getLineOffsets();
					boolean found = false;
					for (int a = 0; a < lineOffsets.length; a++) {
						if (lineOffsets[a] == offsetInLine) {
							point = TextLayoutOpsProvider.getInstance().getLocation(layout,
									offsetInLine, false);// layout.getLocation(offsetInLine,
															// false);
							found = true;
							break;
						}
					}
					if (!found) {

						point = TextLayoutOpsProvider.getInstance().getLocation(layout,
								offsetInLine - 1, true);// layout.getLocation(offsetInLine
														// - 1, true);

					}
					break;
				}
			}
		} else {
			point = new Point(layout.getIndent(), 0);
		}
		int lineAscent = TextLayoutOpsProvider.getInstance().getLineAscent(layout, 0);
		if (lineAscent > 0) {
			if (layout.getLineIndex(offsetInLine) == 0) {
				point.y += lineAscent - layout.getAscent();
			}
		}
		renderer.disposeTextLayout(layout);
		point.x += leftMargin(lineIndex) - horizontalScrollOffset;

		point.y += super.getLinePixel(lineIndex); // Beacause we apply transform
		// later
		// if (renderer instanceof Renderer)
		// point.y += (point.y + getTopPixel()) /
		// ((Renderer)renderer).pageHeight *
		// ((Renderer)renderer).PAGE_BREAK_ASCENT;

		return point;
	}

	public static boolean doPrint(int x, int y, int width, int height,
			boolean all) {
		//System.out.println("X:" + x + " Y:" + y + " W:" + width + "H:" + height
		//		+ "A:" + all);
		return false;
	}

	/**
	 * Renders the invalidated area specified in the paint event.
	 * 
	 * @param event
	 *            paint event
	 */
	void handlePaint(Event event) {
		// getCaret().setVisible(false);
		//long free1 =Runtime.getRuntime().freeMemory();
		topMargin = 0;
		if (event.width == 0 || event.height == 0)
			return;
		if (clientAreaWidth == 0 || clientAreaHeight == 0)
			return;

		// System.out.println(event.x + " " + event.y + " " + event.width + " "
		// + event.height);

		int startLine = getLineIndex(event.y);

		// System.out.println("Height:"+event.height);

		int sz = -1;
		int top = getTopPixel();
		if (startLine == 0) {
			sz = (int) (FIELD_SIZE * scalingFactor - top);
		}
		int y = super.getLinePixel(startLine);
		int endY = event.y + event.height;

		GC gc = event.gc;

		Color background = getBackground();
		Color foreground = getForeground();

		// setBackground(background);
		// drawBackground(gc, 0, 0, clientAreaWidth, clientAreaHeight);
		int lm = leftMargin(-1);
		if (lm > 0) {
			drawBackground(gc, 0, 0, lm, clientAreaHeight);
		}

		if (topMargin > 0) {
			drawBackground(gc, 0, 0, clientAreaWidth, topMargin);
		}
		if (bottomMargin > 0) {
			drawBackground(gc, 0, clientAreaHeight - bottomMargin,
					clientAreaWidth, bottomMargin);
		}
		int rightMargin2 = rightMargin(-1);
		if (rightMargin2 > 0) {
			drawBackground(gc, clientAreaWidth - rightMargin2, 0, rightMargin2,
					clientAreaHeight);
		}
		if (endY > 0) {
			int contentLineCount = content.getLineCount();
			int lineCount = isSingleLine() ? 1 : contentLineCount;
			// if (y < endY) {
			gc.setBackground(background); // (1) Used for filling whole paint
			// event area with bgcolor,
			// instead of drawing every line bg and space rest bg separately
			drawBackground(gc, 0, y, clientAreaWidth, endY - y);
			// }
			int i = 0;
			// int prevY = 0;
			for (i = startLine; y < endY && i < lineCount; i++) {
				int x = leftMargin(i) - horizontalScrollOffset;
				/*
				 * System.out.println("Y:"+y); TODO debug if (y - prevY > 18)
				 * System.out.println("prevY:"+prevY);
				 */
				// prevY = y;
				y += renderer.drawLine(i, x, y, gc, background, foreground);

			}
			if (i < lineCount && TextLayoutOpsProvider.getInstance().needToDrawFirstInvivsibleLine()
					&& pageInformation != null) {
				int x = leftMargin(i) - horizontalScrollOffset;
				y += renderer.drawLine(i, x, y, gc, background, foreground);
			}
			// System.out.println("Y:"+y);
			// if (renderer instanceof Renderer){
			// Renderer r=(Renderer) renderer;
			//				
			// int p=r.getLineY(lineCount-1);
			// int h=r.getLineHeight(lineCount-1);
			// p+=h;
			// if (TextLayoutOpsProvider.getInstance().getExtra()==1){
			// int visutalPos=r.getLineCount(lineCount-1)-1;
			// while (visutalPos%r.pageLineCount!=0){
			// p+=h;
			// visutalPos++;
			// };
			// p+=(r.breakSize*scalingFactor);
			// }
			//
			// int k=p-verticalScrollOffset;
			// if (k<endY){
			// gc.setBackground(background);
			//					
			// //drawField(gc, 0, k, clientAreaWidth, clientAreaHeight);
			// }
			//				
			// }

			// Little description for this. It's connected with (1)^. This if
			// was filling the rest of
			// paint area with bg color, others was filled in Renderer,
			// line-by-line.
			// Now corrected to single call filling of all event paint area

		}
		// fill the margin background
		gc.setBackground(background);

		Color bg = new Color(getDisplay(), 128, 128, 128);
		if (sz > 0) {
			Color tmp = gc.getBackground();
			gc.setBackground(bg);
			// gc.fillRectangle(0, 0, clientAreaWidth, sz);
			gc.setBackground(tmp);
		}
		if (leftFieldSize > 0) {
			int offset = horizontalScrollOffset;
			int realFieldSize = (int) Math.round(leftFieldSize * scalingFactor);
			if (realFieldSize > offset)
				drawField(gc, 0, 0, realFieldSize - offset, clientAreaHeight);
		}
		if (rightFieldSize > 0) {
			double offset = (pageWidth == -1 ? clientAreaWidth - rightFieldSize
					: pageWidth)
					* scalingFactor - horizontalScrollOffset;
			if (offset < clientAreaWidth)
				drawField(gc, (int) Math.round(offset), 0, (int) Math
						.round(clientAreaWidth - offset) + 1, clientAreaHeight);
		}
		bg.dispose();
		getCaret().setVisible(true);
		
		//long free2 =Runtime.getRuntime().freeMemory();
		//long dlt = free2-free1;
		//System.out.println("DELTA " + dlt);
	}

	void setSelection(int start, int length, boolean sendEvent, boolean doBlock) {
		super.setSelection(start, length, sendEvent, doBlock);
		super.redraw();
	}

	void sendSelectionEvent() {
		super.sendSelectionEvent();
		super.redraw();
	}

	public int getAdditionalPageWidth() {
		return (int) (rightFieldSize + Math.round(EXTRA_PAGE_SIZE
				* scalingFactor));
	}

	protected void drawField(GC gc, int x1, int y1, int x2, int y2) {
		Color tmp = gc.getBackground();
		Color color = new Color(gc.getDevice(), fieldColor);
		gc.setBackground(color);
		gc.fillRectangle(x1, y1, x2, y2);
		color.dispose();
		gc.setBackground(tmp);
	}

	/**
	 * Returns the smallest bounding rectangle that includes the characters
	 * between two offsets.
	 * 
	 * @param start
	 *            offset of the first character included in the bounding box
	 * @param end
	 *            offset of the last character included in the bounding box
	 * @return bounding box of the text between start and end
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_RANGE when start and/or end are outside
	 *                the widget content</li>
	 *                </ul>
	 * @since 3.1
	 */
	public Rectangle getTextBounds(int start, int end) {
		checkWidget();
		int contentLength = getCharCount();
		if (start < 0 || start >= contentLength || end < 0
				|| end >= contentLength || start > end) {
			SWT.error(SWT.ERROR_INVALID_RANGE);
		}
		int lineStart = content.getLineAtOffset(start);
		int lineEnd = content.getLineAtOffset(end);
		Rectangle rect;
		int y = getLinePixel(lineStart);
		int height = 0;
		int left = 0x7fffffff, right = 0;
		for (int i = lineStart; i <= lineEnd; i++) {
			int lineOffset = content.getOffsetAtLine(i);
			TextLayout layout = renderer.getTextLayout(i);
			int length = layout.getText().length();
			if (length > 0) {
				if (i == lineStart) {
					if (i == lineEnd) {
						rect = TextLayoutOpsProvider.getInstance().getBounds(layout, start
								- lineOffset, end - lineOffset);// layout.getBounds(start
																// - lineOffset,
																// end -
																// lineOffset);
					} else {
						rect = TextLayoutOpsProvider.getInstance().getBounds(layout, start
								- lineOffset, length);// layout.getBounds(start
														// - lineOffset,
														// length);
					}
					y += rect.y;
				} else if (i == lineEnd) {
					rect = TextLayoutOpsProvider.getInstance().getBounds(layout, 0, end - lineOffset); // layout.getBounds(0,
																					// end
																					// -
																					// lineOffset);
				} else {
					rect = layout.getBounds();
				}
				left = Math.min(left, rect.x);
				right = Math.max(right, rect.x + rect.width);
				height += rect.height;
			} else {
				height += renderer.getLineHeight();
			}
			renderer.disposeTextLayout(layout);
		}
		rect = new Rectangle(left, y, right - left, height);
		rect.x += leftMargin(lineStart) - horizontalScrollOffset;
		return rect;
	}

	int getWrapWidth(int i) {
		if (wordWrap && !isSingleLine()) {
			int width = clientAreaWidth - leftMargin(i) - 5
					- getRightMarginAt(i) - getCaretWidth();
			return width > 0 ? width : 1;
		}
		return -1;
	}

	void internalRedrawRange(int start, int length) {
		if (length <= 0)
			return;
		int end = start + length;
		int startLine = content.getLineAtOffset(start);
		int endLine = content.getLineAtOffset(end);
		int partialBottomIndex = getPartialBottomIndex();
		int partialTopIndex = getPartialTopIndex();
		if (startLine > partialBottomIndex || endLine < partialTopIndex) {
			return;
		}
		if (partialTopIndex > startLine) {
			startLine = partialTopIndex;
			start = 0;
		} else {
			start -= content.getOffsetAtLine(startLine);
		}
		if (partialBottomIndex < endLine) {
			endLine = partialBottomIndex + 1;
			end = 0;
		} else {
			end -= content.getOffsetAtLine(endLine);
		}

		TextLayout layout = renderer.getTextLayout(startLine);
		int lineX = leftMargin(startLine) - horizontalScrollOffset, startLineY = getLinePixel(startLine);
		int[] offsets = layout.getLineOffsets();
		int startIndex = layout.getLineIndex(Math.min(start, layout.getText()
				.length()));

		/*
		 * Redraw end of line before start line if wrapped and start offset is
		 * first char
		 */
		if (wordWrap && startIndex > 0 && offsets[startIndex] == start) {
			Rectangle rect = TextLayoutOpsProvider.getInstance()
					.getLineBounds(layout, startIndex - 1);// layout.getLineBounds(startIndex
															// - 1);
			rect.x = rect.width;
			rect.width = clientAreaWidth - rightMargin(startIndex - 1) - rect.x;
			rect.x += lineX;
			rect.y += startLineY;
			super.redraw(rect.x, rect.y, rect.width + BULLET_INDENT,
					rect.height, false);
		}

		if (startLine == endLine) {
			int endIndex = layout.getLineIndex(Math.min(end, layout.getText()
					.length()));
			if (startIndex == endIndex) {
				/*
				 * Redraw rect between start and end offset if start and end
				 * offsets are in same wrapped line
				 */
				Rectangle rect = TextLayoutOpsProvider.getInstance()
						.getBounds(layout, start, end - 1);// layout.getBounds(start,
															// end - 1);
				rect.x += lineX;
				rect.y += startLineY;
				super.redraw(rect.x, rect.y, rect.width + BULLET_INDENT,
						rect.height, false);
				renderer.disposeTextLayout(layout);
				return;
			}
		}

		/* Redraw start line from the start offset to the end of client area */
		Rectangle startRect = TextLayoutOpsProvider.getInstance().getBounds(layout, start,
				offsets[startIndex + 1] - 1);// layout.getBounds(start,offsets[startIndex
												// + 1] - 1);
		if (startRect.height == 0) {
			Rectangle bounds = TextLayoutOpsProvider.getInstance().getLineBounds(layout, startIndex);// layout.getLineBounds(startIndex);
			startRect.x = bounds.width;
			startRect.y = bounds.y;
			startRect.height = bounds.height;
		}
		startRect.x += lineX;
		startRect.y += startLineY;
		startRect.width = clientAreaWidth - rightMargin(startIndex)
				- startRect.x;
		super.redraw(startRect.x, startRect.y, startRect.width + BULLET_INDENT,
				startRect.height, false);

		/* Redraw end line from the beginning of the line to the end offset */
		if (startLine != endLine) {
			renderer.disposeTextLayout(layout);
			layout = renderer.getTextLayout(endLine);
			offsets = layout.getLineOffsets();
		}
		int endIndex = layout.getLineIndex(Math.min(end, layout.getText()
				.length()));
		Rectangle endRect = TextLayoutOpsProvider.getInstance().getBounds(layout, offsets[endIndex],
				end - 1);// layout.getBounds(offsets[endIndex], end - 1);
		if (endRect.height == 0) {
			Rectangle bounds = TextLayoutOpsProvider.getInstance().getLineBounds(layout, endIndex);// layout.getLineBounds(endIndex);
			endRect.y = bounds.y;
			endRect.height = bounds.height;
		}
		endRect.x += lineX;
		endRect.y += getLinePixel(endLine);
		super.redraw(endRect.x, endRect.y, endRect.width + BULLET_INDENT,
				endRect.height, false);
		renderer.disposeTextLayout(layout);

		/* Redraw all lines in between start and end line */
		int y = startRect.y + startRect.height;
		if (endRect.y > y) {
			super.redraw(leftMargin(-1), y, clientAreaWidth - rightMargin(-1)
					- leftMargin(-1) + BULLET_INDENT, endRect.y - y, false);
		}
	}

	/**
	 * Scrolls down the text to use new space made available by a resize or by
	 * deleted lines.
	 */
	void claimBottomFreeSpace() {
		if (isFixedLineHeight()) {
			int newVerticalOffset = Math.max(0, renderer.getHeight()
					- clientAreaHeight);
			if (newVerticalOffset < getVerticalScrollOffset()) {
				scrollVertical(newVerticalOffset - getVerticalScrollOffset(),
						true);
			}
		} else {
			int bottomIndex = getPartialBottomIndex();
			int height = getLinePixel(bottomIndex + 1);
			if (TextLayoutOpsProvider.getInstance().getExtra() == 0) {
				if (clientAreaHeight - bottomMargin > height) {
					scrollVertical(-getAvailableHeightAbove(clientAreaHeight
							- height), true);
				}
			} else {
				if (clientAreaHeight - bottomMargin > height) {
					scrollVertical(-getAvailableHeightAbove(clientAreaHeight
							- height), true);
				}
			}
		}
	}

	/*
	 * private int adjustMargin(int startLine, int lm) { return lm +
	 * BULLET_INDENT; }
	 */

	/*
	 * public float getScalingFactor() { return scalingFactor; }
	 */

	public void setFont(Font font) {
		//	FontData fontData = new FontData("Courier", 25, SWT.NONE); //$NON-NLS-1$ // 12, SWT.NONE
		// font = new Font(this.getParent().getDisplay(), fontData);

		Point offset = getSelection();
		int caretOffset = getCaretOffset();

		if (font == null)
			font = new Font(getDisplay(), new FontData("Arial", 18, SWT.NONE)); // TODO
		// debug
		if (font.isDisposed()) {
			return;
		}
		super.setFont(font);
		GC gc = new GC(getDisplay());
		gc.setFont(getFont());
		FontMetrics metrics = gc.getFontMetrics();
		averageCharWidth = metrics.getAverageCharWidth();
		averageCharHeight = metrics.getHeight();
		setPageInformation(pageInformation); // Because something, like page
		// line count, can change

		setCaretOffset(caretOffset);
		setSelection(offset);

		gc.dispose();
	}

	public void setScale(double newScale) {
		Point s = getSelection();
		int offset = getCaretOffset();

		if (renderer instanceof Renderer) {
			((Renderer) renderer).changeScale(newScale);
		} else
			throw new UnsupportedOperationException(
					"Current StyledTextRenderer doesn't support such operation.");
		this.scalingFactor = newScale;
		setRedraw(false);
		// Resetting of all neat calculations. We need this to prevent some
		// magic bugs
		topIndex = 0;
		topIndexY = 0;
		verticalScrollOffset = 0;
		horizontalScrollOffset = 0;
		resetSelection();

		((Renderer) renderer).partiallyReset();
		// renderer.setContent(content);

		if (getVerticalBar() != null) {
			getVerticalBar().setSelection(0);
		}
		if (getHorizontalBar() != null) {
			getHorizontalBar().setSelection(0);
		}

		setFocus();

		claimBottomFreeSpace();

		calculateScrollBars();

		if (isBidiCaret())
			createCaretBitmaps();
		caretDirection = SWT.NULL;
		// End of resetting; Let's restore cursor pos here
		setRedraw(true);
		checkHorizontalScroll();
		super.redraw();
		// UIUtils.setFocus(handle);
		/* from pach check it */
		UIUtils.setFocus(this);

		setCaretOffset(offset);
		setSelection(s);

		showCaret();
	}

	public void setLineSpacing(int lineSpacing) {
		super.setLineSpacing(lineSpacing);
		if (renderer instanceof ExtendedRenderer) {
			((ExtendedRenderer) renderer).setDefaultLineSpacing(lineSpacing);
		}
	}

	public int getParagraphSpacing() {
		if (renderer instanceof Renderer) {
			return ((Renderer) renderer).getParagraphSpacing();
		} else
			return 0;
	}

	public void setParagraphSpacing(int paragraphSpacing) {
		if (renderer instanceof Renderer) {
			((Renderer) renderer).setParagraphSpacing(paragraphSpacing);
		} else
			throw new UnsupportedOperationException(
					"Paragraph spacing is supported only by special Renderer class!");
	}

	/*
	 * private boolean hasBullet(int startLine) { return
	 * getLineBullet(startLine) != null; }
	 */

	public int getBaseMargin() {
		return leftMargin;
	}

	public int getBaseRightMargin() {
		return rightMargin;
	}

	public int getMarginAt(int lineIndex) {
		return leftMargin(lineIndex);
	}

	public int getRightMarginAt(int lineIndex) {
		int baseMargin = rightMargin(lineIndex);
		if (renderer instanceof ExtendedRenderer)
			baseMargin += ((ExtendedRenderer) renderer)
					.getRightIndent(lineIndex);
		return baseMargin;
	}

	public int getWrapWidthAt(int lineIndex) {
		return getWrapWidth() / averageCharWidth;
	}

	public double getScalingFactor() {
		return scalingFactor;
	}

	public int getPageWidth() {
		return pageWidth;
	}

	public void setPageWidth(int pageWidth) {
		this.pageWidth = pageWidth;
	}

	public void setPagingEnabled(boolean pagingEnabled) {
		if (renderer instanceof Renderer) {
			Renderer pageRenderer = (Renderer) renderer;
			pageRenderer.setPagingEnabled(pagingEnabled);

		} else
			throw new UnsupportedOperationException(
					"Operation is suppored only by special PageRenderer.");
	}

	
	public void redraw(int x, int y, int width, int height, boolean all) {

		super.redraw(x, y, width, height, all);
	}

	
	public void redrawRange(int start, int length, boolean clearBackground) {
		// TODO Auto-generated method stub
		super.redrawRange(start, length, clearBackground);
	}

	void redrawLines(int startLine, int lineCount) {
		// do nothing if redraw range is completely invisible
		int partialBottomIndex = getPartialBottomIndex();
		if (startLine > partialBottomIndex
				|| startLine + lineCount - 1 < topIndex) {
			return;
		}
		// only redraw visible lines
		if (startLine < topIndex) {
			lineCount -= topIndex - startLine;
			startLine = topIndex;
		}
		if (startLine + lineCount - 1 > partialBottomIndex) {
			lineCount = partialBottomIndex - startLine + 1;
		}
		startLine -= topIndex;
		int redrawTop = getLinePixel(startLine);
		int redrawBottom = getLinePixel(startLine + lineCount);
		int redrawWidth = clientAreaWidth - leftMargin(-1) - rightMargin;
		super.redraw(leftMargin(-1), redrawTop, redrawWidth, redrawBottom
				- redrawTop, true);
	}

	void scrollText(int srcY, int destY) {
		int leftMargin = leftMargin(-1);
		if (srcY == destY)
			return;
		int deltaY = destY - srcY;
		int scrollWidth = clientAreaWidth - leftMargin - rightMargin, scrollHeight;
		if (deltaY > 0) {
			scrollHeight = clientAreaHeight - srcY - bottomMargin;
		} else {
			scrollHeight = clientAreaHeight - destY - bottomMargin;
		}

		scroll(leftMargin, destY, leftMargin, srcY, scrollWidth, scrollHeight,
				true);
		if ((0 < srcY + scrollHeight) && (topMargin > srcY)) {
			super.redraw(leftMargin, deltaY, scrollWidth, topMargin, false);
		}
		if ((0 < destY + scrollHeight) && (topMargin > destY)) {
			super.redraw(leftMargin, 0, scrollWidth, topMargin, false);
		}
		if ((clientAreaHeight - bottomMargin < srcY + scrollHeight)
				&& (clientAreaHeight > srcY)) {
			super.redraw(leftMargin, clientAreaHeight - bottomMargin + deltaY,
					scrollWidth, bottomMargin, false);
		}
		if ((clientAreaHeight - bottomMargin < destY + scrollHeight)
				&& (clientAreaHeight > destY)) {
			super.redraw(leftMargin, clientAreaHeight - bottomMargin,
					scrollWidth, bottomMargin, false);
		}
	}

	/**
	 * Scrolls the widget vertically.
	 * 
	 * @param pixel
	 *            the new vertical scroll offset
	 * @param adjustScrollBar
	 *            true= the scroll thumb will be moved to reflect the new scroll
	 *            offset. false = the scroll thumb will not be moved
	 * @return true=the widget was scrolled false=the widget was not scrolled
	 */
	boolean scrollVertical(int pixels, boolean adjustScrollBar) {
		if (pixels == 0) {
			return false;
		}
		int leftMargin = leftMargin(-1);
		if (verticalScrollOffset != -1) {
			ScrollBar verticalBar = getVerticalBar();
			if (verticalBar != null && adjustScrollBar) {
				verticalBar.setSelection(verticalScrollOffset + pixels);
			}
			int oldOffset = verticalScrollOffset;
			int scrollWidth = clientAreaWidth - leftMargin - rightMargin;
			if (pixels > 0) {
				int sourceY = topMargin + pixels;
				int scrollHeight = clientAreaHeight - sourceY - bottomMargin;
				if (scrollHeight > 0) {
					scroll(leftMargin, topMargin, leftMargin, sourceY,
							scrollWidth, scrollHeight, true);
				}
				if (sourceY > scrollHeight) {
					int redrawY = Math.max(0, topMargin + scrollHeight);
					int redrawHeight = Math.min(clientAreaHeight, pixels
							- scrollHeight);
					super.redraw(0, redrawY, clientAreaWidth, redrawHeight,
							true);
				}
			} else {
				int destinationY = topMargin - pixels;
				int scrollHeight = clientAreaHeight - destinationY
						- bottomMargin;
				if (scrollHeight > 0) {
					scroll(leftMargin, destinationY, leftMargin, topMargin,
							scrollWidth, scrollHeight, true);
				}
				if (destinationY > scrollHeight) {
					int redrawY = Math.max(0, topMargin + scrollHeight);
					int redrawHeight = Math.min(clientAreaHeight, -pixels
							- scrollHeight);
					super.redraw(0, redrawY, clientAreaWidth, redrawHeight,
							true);
				}
			}
			verticalScrollOffset = oldOffset + pixels;
			calculateTopIndex(pixels);
		} else {
			calculateTopIndex(pixels);
			super.redraw();
		}
		setCaretLocation();
		deferredRepaint();
		return true;
	}

	void setScrollBars(boolean vertical) {
		if (isDisposed())
			return;
		int inactive = 1;
		if (vertical || !isFixedLineHeight()) {
			ScrollBar verticalBar = getVerticalBar();
			if (verticalBar != null) {
				int maximum = renderer.getHeight();
				// only set the real values if the scroll bar can be used
				// (ie. because the thumb size is less than the scroll maximum)
				// avoids flashing on Motif, fixes 1G7RE1J and 1G5SE92
				if (clientAreaHeight < maximum) {
					verticalBar.setMaximum(maximum);
					verticalBar.setThumb(clientAreaHeight);
					verticalBar.setPageIncrement(clientAreaHeight);
				} else if (verticalBar.getThumb() != inactive
						|| verticalBar.getMaximum() != inactive) {
					verticalBar.setValues(verticalBar.getSelection(),
							verticalBar.getMinimum(), inactive, inactive,
							verticalBar.getIncrement(), inactive);
				}
			}
		}
		ScrollBar horizontalBar = getHorizontalBar();
		if (horizontalBar != null && horizontalBar.getVisible()) {
			int maximum = (int) (Math.round(pageWidth * scalingFactor) + getAdditionalPageWidth());
			// int maximum = (int) Math.round(renderer.getWidth() *
			// scalingFactor);
			// only set the real values if the scroll bar can be used
			// (ie. because the thumb size is less than the scroll maximum)
			// avoids flashing on Motif, fixes 1G7RE1J and 1G5SE92
			if (clientAreaWidth < maximum) {
				horizontalBar.setMaximum(maximum);
				// horizontalBar.setThumb(800);
				horizontalBar.setThumb((int) clientAreaWidth
						+ getAdditionalPageWidth() - rightFieldSize/*
																	 * -
																	 * leftMargin
																	 * (-1) -
																	 * rightMargin
																	 * +
																	 * rightFieldSize
																	 * *
																	 * scalingFactor
																	 * +
																	 * getAdditionalPageWidth
																	 * () *
																	 * scalingFactor
																	 * )
																	 */);
				horizontalBar.setPageIncrement(clientAreaWidth - leftMargin(-1)
						- rightMargin);
			} else if (horizontalBar.getThumb() != inactive
					|| horizontalBar.getMaximum() != inactive) {
				horizontalBar.setValues(horizontalBar.getSelection(),
						horizontalBar.getMinimum(), inactive, inactive,
						horizontalBar.getIncrement(), inactive);
			}
		}
	}

	boolean showLocation(Rectangle rect, boolean scrollPage) {
		int leftMargin = 0;

		int clientAreaWidth = Math.max(this.clientAreaWidth - leftMargin
				- rightMargin, 0);
		int clientAreaHeight = Math.max(this.clientAreaHeight - topMargin
				- bottomMargin, 0);
		boolean scrolled = false;
		if (rect.y <= topMargin) {
			scrolled = scrollVertical(rect.y - topMargin, true);
		} else if (rect.y + rect.height > clientAreaHeight) {
			if (clientAreaHeight == 0) {
				scrolled = scrollVertical(rect.y, true);
			} else {
				int vertScrollOffset = rect.y + rect.height - clientAreaHeight;
				// vertScrollOffset = this.verticalScrollOffset;
				scrolled = scrollVertical(vertScrollOffset, true);
			}
		}
		if (clientAreaWidth > 0) {
			int minScroll = scrollPage ? clientAreaWidth / 4 : 0;
			if (rect.x < leftMargin) {
				int scrollWidth = Math.max(leftMargin - rect.x, minScroll);
				int maxScroll = horizontalScrollOffset;
				scrolled = scrollHorizontal(-Math.min(maxScroll, scrollWidth),
						true);
			} else if (rect.x + rect.width > clientAreaWidth) {
				int scrollWidth = Math.max(rect.x + rect.width
						- clientAreaWidth, minScroll);

				int pw = (int) (pageInformation == null ? pageWidth * scalingFactor : pageInformation.getPageWidth());
				int rw = renderer.getWidth() + rightMargin;
				int mw = Math.max(pw, rw);
				int maxScroll = mw - horizontalScrollOffset
						- this.clientAreaWidth;
				scrolled = scrollHorizontal(Math.min(maxScroll, scrollWidth),
						true);
			}
		}
		return scrolled;
	}

	void setCaretLocation(Point location, int direction) {
		if (!(renderer instanceof Renderer)) {
			super.setCaretLocation(location, direction);
			return;
		}
		Caret caret = getCaret();
		if (caret != null) {

			boolean isDefaultCaret = caret == defaultCaret;
			int lineHeight = renderer.getLineHeight();
			int caretHeight = lineHeight;
			if (!isFixedLineHeight() && isDefaultCaret) {

				if (this.caretHeight != 0)
					caretHeight = this.caretHeight;
				if (caretHeight != lineHeight) {
					direction = SWT.DEFAULT;
				}
			}
			int imageDirection = direction;
			if (isMirrored()) {
				if (imageDirection == SWT.LEFT) {
					imageDirection = SWT.RIGHT;
				} else if (imageDirection == SWT.RIGHT) {
					imageDirection = SWT.LEFT;
				}
			}
			if (isDefaultCaret && imageDirection == SWT.RIGHT) {
				location.x -= (caret.getSize().x - 1);
			}
			if (isDefaultCaret) {
				caret
						.setBounds(location.x, location.y, caretWidth,
								caretHeight);
			} else {
				Rectangle bounds = caret.getBounds();
				if (this.caretHeight != 0) {
					bounds.height = this.caretHeight;
				}
				caret.setBounds(bounds);
				caret.setLocation(location);
			}
			getAccessible().textCaretMoved(getCaretOffset());
			if (direction != caretDirection) {
				caretDirection = direction;
				if (isDefaultCaret) {
					if (imageDirection == SWT.DEFAULT) {
						defaultCaret.setImage(null);
					} else if (imageDirection == SWT.LEFT) {
						defaultCaret.setImage(leftCaretBitmap);
					} else if (imageDirection == SWT.RIGHT) {
						defaultCaret.setImage(rightCaretBitmap);
					}
				}
				if (caretDirection == SWT.LEFT) {
					BidiUtil.setKeyboardLanguage(BidiUtil.KEYBOARD_NON_BIDI);
				} else if (caretDirection == SWT.RIGHT) {
					BidiUtil.setKeyboardLanguage(BidiUtil.KEYBOARD_BIDI);
				}
			}
		}
		columnX = location.x;
	}

	public boolean isAllowParagraphSpacing() {
		return allowParagraphSpacing;
	}

	public void setAllowParagraphSpacing(boolean allowPSpacing) {
		this.allowParagraphSpacing = allowPSpacing;
	}

	public int getVisibleLineCount() {
		if (renderer instanceof Renderer) {
			Renderer r = (Renderer) renderer;
			return r.getPrevLineCountForPaging(getLineCount());
		}
		return -1;
	}

	public int getLinesToParagraphStart(int number) {
		if (renderer instanceof Renderer) {
			Renderer r = (Renderer) renderer;
			return r.getPrevLineCountForPaging(number);
		}
		return 1;
	}

	public int getLinesInParagraph(int number) {
		if (renderer instanceof Renderer) {
			Renderer r = (Renderer) renderer;
			if (number > 0) {

				return r.getLineCount(number) - r.getLineCount(number - 1);
			}
			return r.getLineCount(number);
		}
		return 1;
	}

	public int getTotalPageNumber() {
		return getPageNumber(getLineCount() - 1);
	}

	private int getPageNumber(int line) {
		if (renderer instanceof Renderer) {
			Renderer r = (Renderer) renderer;
			if (r.pagingEnabled) {
				int t = getLinesToParagraphStart(line);
				int i = (t / r.linesPerPage)
						+ (t % r.linesPerPage == 0 ? 0 : 1);
				return i;
			}
		}
		return 0;
	}

	void handleTextChanging(TextChangingEvent event) {
		if (event.replaceCharCount < 0) {
			event.start += event.replaceCharCount;
			event.replaceCharCount *= -1;
		}
		lastTextChangeStart = event.start;
		lastTextChangeNewLineCount = event.newLineCount;
		lastTextChangeNewCharCount = event.newCharCount;
		lastTextChangeReplaceLineCount = event.replaceLineCount;
		lastTextChangeReplaceCharCount = event.replaceCharCount;
		int lineIndex = content.getLineAtOffset(event.start);
		int srcY = getLinePixel(lineIndex + event.replaceLineCount + 1);
		int destY = getLinePixel(lineIndex + 1) + event.newLineCount
				* renderer.getLineHeight();
		lastLineBottom = destY;

		if (srcY < 0 && destY < 0) {
			lastLineBottom += srcY - destY;
			verticalScrollOffset += destY - srcY;
			calculateTopIndex(destY - srcY);
			setScrollBars(true);
		} else {
			scrollText(srcY, destY);
		}

		renderer.textChanging(event);

		// Update the caret offset if it is greater than the length of the
		// content.
		// This is necessary since style range API may be called between the
		// handleTextChanging and handleTextChanged events and this API sets the
		// caretOffset.
		int newEndOfText = content.getCharCount() - event.replaceCharCount
				+ event.newCharCount;
		if (caretOffset > newEndOfText) {
			setCaretOffset(newEndOfText, SWT.DEFAULT);
		}
	}

	
	void handleTextChanged(TextChangedEvent event) {
		int firstLine = content.getLineAtOffset(lastTextChangeStart);
		super.handleTextChanged(event);
		if (renderer instanceof Renderer) {
			Renderer r = (Renderer) renderer;
			if (r.pagingEnabled) {
				for (IPageListener l : pageListeners) {
					l.pagingChanged(getPageNumber(firstLine),
							getTotalPageNumber());
				}
			}
		}
	}

	public void addPageListener(IPageListener listener) {
		pageListeners.add(listener);
	}

	public boolean removePageListener(IPageListener listener) {
		return pageListeners.remove(listener);
	}

	public IPageListener[] getPageListeners() {
		return pageListeners.toArray(new IPageListener[pageListeners.size()]);
	}

	public void getBulletList(List<Bullet> list) {
		for (int i = 0; i < content.getLineCount(); i++) {
			list.add(renderer.getLineBullet(i, null));
		}
	}

	public int getOffsetAtLocation(Point point) {
		checkWidget();
		if (point == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		int[] trailing = new int[1];
		int offset = getOffsetAtPoint(point.x, point.y, trailing, true);
		if (offset == -1) {
			return trailing[0];
		}
		return offset + trailing[0];
	}

	public void dispose() {
		super.dispose();
		arrowCursor.dispose();
	}

	public int getAverageCharWidth() {
		return averageCharWidth;
	}

	/**
	 * Prints the widget's text to the default printer.
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void print() {
		checkWidget();
		Printer printer = new Printer();
		StyledTextPrintOptions options = new StyledTextPrintOptions();
		options.printTextForeground = true;
		options.printTextBackground = true;
		options.printTextFontStyle = true;
		options.printLineBackground = true;
		new ExtendedPrinting(this, printer, options).run();
		printer.dispose();
	}

	/**
	 * Returns a runnable that will print the widget's text to the specified
	 * printer.
	 * <p>
	 * The runnable may be run in a non-UI thread.
	 * </p>
	 * 
	 * @param printer
	 *            the printer to print to
	 * @param options
	 *            print options to use during printing
	 * 
	 * @return a <code>Runnable</code> for printing the receiver's text
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT when printer or options is null</li>
	 *                </ul>
	 * @since 2.1
	 */
	public Runnable print(Printer printer, StyledTextPrintOptions options) {
		checkWidget();
		if (printer == null || options == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		return new ExtendedPrinting(this, printer, options);
	}

	public int getAverageCharHeight() {
		return averageCharHeight;
	}

	void paintObject(GC gc, int x, int y, int ascent, int descent,
			StyleRange style, Bullet bullet, int bulletIndex) {
		if (bullet != null && renderer instanceof ExtendedRenderer
				&& bullet.type == ST.BULLET_CUSTOM) {
			x -= ExtendedStyledText.BULLET_SPACE;
		}
		super
				.paintObject(gc, x, y, ascent, descent, style, bullet,
						bulletIndex);
	}

	public IPPIResolver getScreenResolver() {
		return screenResolver;
	}

	public void setScreenResolver(IPPIResolver screenResolver) {
		this.screenResolver = screenResolver;
	}

	public IPPIResolver getPrinterResolver() {
		return printerResolver;
	}

	public void setPrinterResolver(IPPIResolver printerResolver) {
		this.printerResolver = printerResolver;
	}

	public Point getScreenPPI() {
		if (screenResolver == null)
			return Display.getDefault().getDPI();
		else
			return screenResolver.getPPI();
	}

	
	public boolean setFocus() {
		// TODO Auto-generated method stub
		return super.setFocus();
	}

	public int getLeftFieldSize() {
		return leftFieldSize;
	}

	public void bulkSet(int[] aligns, int[] indents, Bullet[] bl) {
		StyledTextRenderer renderer2 = getRenderer();
		if (renderer2 instanceof ExtendedRenderer) {
			ExtendedRenderer r = (ExtendedRenderer) renderer2;
			r.bulkSet(aligns, indents, bl);
		}
		for (int a = 0; a < aligns.length; a++) {
			putIndent(a, indents[a]);
		}
	}
	
	@Override
	public int getOffsetAtLine(int lineIndex)
	{
		if (lineIndex < 0 || 
				(lineIndex > 0 && lineIndex >= content.getLineCount())) {
				return 0;
			}
		return super.getOffsetAtLine(lineIndex);
	}
}
