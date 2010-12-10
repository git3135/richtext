package org.eclipse.swt.custom;

import java.awt.Rectangle;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;

public class PrintingPageInformationRenderer extends PageInformationRenderer {

	protected double ratioX; 
	protected double ratioY;
	protected org.eclipse.swt.graphics.Rectangle clientArea;
	protected Font defaultFont;
	protected IPageInformationRenderer userRenderer;
	
	public PrintingPageInformationRenderer(PrinterRenderer renderer, IPageInformationRenderer userRenderer) {
		super(renderer);
		defaultFont = renderer.styledText.getFont();
		ratioX = renderer.getRatioX();
		ratioY = renderer.getRatioY();
		this.userRenderer = userRenderer;
	}
	
		
	public void drawPageHeader(GC gc, double scalingFactor, int pos,
			int pageIndex,
			int pageOffset, ExtendedStyledText extendedStyledText, IPageInformation pageInformation) {
		if (pageInformation == null) {
			return;
		}
		String pageHeader = getPageHeader(pageInformation,pageIndex, pageOffset);
		if (pageHeader != null) {
			Font pageHeaderFont = getPageHeaderFont(pageInformation,pageIndex, pageOffset);
			if (pageHeaderFont == null) {
				pageHeaderFont = defaultFont;
			}
			pageHeaderFont = renderer.scaleFont(pageHeaderFont);
			int pageX = getPageHeaderXOffset(pageInformation,pageIndex, pageOffset)
					+ clientArea.x;
			int pageY = getPageHeaderYOffset(pageInformation,pageIndex, pageOffset);
			int headerX = (int) (pageX * ratioX);
			gc.setFont(pageHeaderFont);
			int headerY = (int) (pos + (pageY * ratioY));
			gc.drawText(pageHeader, headerX, headerY);
		}
	}
	
	public void drawPageFooter(GC gc, double scalingFactor, int pos,
			int pageIndex,
			int pageOffset, ExtendedStyledText extendedStyledText, IPageInformation pageInformation) {
		String pageFooter = getPageFooter(pageInformation,pageIndex, pageOffset);
		if (pageFooter != null) {
			Font pageFooterFont = getPageFooterFont(pageInformation,pageIndex, pageOffset);
			if (pageFooterFont == null) {
				pageFooterFont = defaultFont;
			}
			Font pageFont = renderer.scaleFont(pageFooterFont);

			int pageX = getPageFooterXOffset(pageInformation,pageIndex, pageOffset)
					+ clientArea.x;
			int pageY = getPageFooterYOffset(pageInformation,pageIndex, pageOffset);
			int footerX = (int) (pageX * ratioX);
			gc.setFont(pageFont);
			int footerY = (int) (pos - ((int) pageY * ratioY) - gc
					.getFontMetrics().getHeight());

			gc.drawText(pageFooter, footerX, footerY);
		}
	}
	
	protected String getPageHeader(IPageInformation pageInformation, int pageIndex, int pageOffset){
		return ((PageInformationRenderer) userRenderer).getPageHeader(pageInformation,pageIndex, pageOffset);
	}
	
	protected String getPageFooter(IPageInformation pageInformation, int pageIndex, int pageOffset){
		
		return ((PageInformationRenderer) userRenderer).getPageFooter(pageInformation,pageIndex, pageOffset);
	}
	
	public int drawPageNumber(GC gc, double scalingFactor, int lineIndex, int pos,
			int pageOffset,
			ExtendedStyledText extendedStyledText, IPageInformation pageInformation) {
		if (pageInformation == null) {
			return 0;
		}
		int pageIndex = renderer.getPageIndex(lineIndex);
		boolean showPageNumbers = showPageNumbers(pageInformation, pageIndex, pageOffset);
		//int additionalPageWidth = extendedStyledText.getAdditionalPageWidth();
		if (showPageNumbers) {
			Font font = getPageNumberFont(pageInformation, pageIndex, pageOffset);
			if (font == null) {
				font = defaultFont;
			}
			Font scaleFont = renderer.scaleFont(font);
			gc.setFont(scaleFont);

			int chWidth = gc.getFontMetrics().getAverageCharWidth();
			
//			if (i%linesPerPage!=0){
//				j++;
//			}
			String string = "" + pageIndex;
			gc.drawText(string, (int) ((clientArea.width - string.length() * chWidth * ratioX) / 2),
							(int) (pos - gc.getFontMetrics().getHeight() - renderer.PAGE_NUMBER_OFFSET
									* ratioY));
		}
		return 0;
	}

	public org.eclipse.swt.graphics.Rectangle getClientArea() {
		return clientArea;
	}

	public void setClientArea(org.eclipse.swt.graphics.Rectangle rectangle) {
		this.clientArea = rectangle;
	}

	public int getFullHeaderHeight(GC gc, double scalingFactor, 
			int pageIndex, int pageOffset,
			ExtendedStyledText extendedStyledText,
			IPageInformation pageInformation) {
		Font pageHeaderFont = getPageHeaderFont(pageInformation,pageIndex, pageOffset);
		if (pageHeaderFont == null) {
			pageHeaderFont = defaultFont;
		}
		pageHeaderFont = renderer.scaleFont(pageHeaderFont);
		int pageY = getPageHeaderYOffset(pageInformation,pageIndex, pageOffset);
		return (int) (pageY * ratioY) + pageHeaderFont.getFontData()[0].getHeight();	
	}


	public double getRatioX() {
		return ratioX;
	}


	public void setRatioX(double ratioX) {
		this.ratioX = ratioX;
	}


	public double getRatioY() {
		return ratioY;
	}


	public void setRatioY(double ratioY) {
		this.ratioY = ratioY;
	}
	
}
