package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

public class PageInformation implements IPageInformation {

	public String pageFooter;
	public String pageHeader;
	public int pageFooterX;
	public int pageFooterY;
	public int pageHeaderX;
	public int pageHeaderY;
	public Font pageFooterFont;
	public Font pageHeaderFont;
	public Font pageNumberFont;
	public boolean needsPageNumber;
	public int linesPerPage;

	private int pageBreakSize = 10;

	public IBreakSize getBreakSize(int type) {
		if (type == IBreakSize.BREAK_SIZE_IN_LINES) {
			return new InLIneBreakSize();
		} else if (type == IBreakSize.BREAK_SIZE_IN_PIXELS) {
			return new InPixelsBreakSize();
		} else
			return null;
	}

	public int pageWidth;
	private IPageInformationRenderer renderer;

	public String getPageFooter() {
		return pageFooter;
	}

	public Font getPageFooterFont() {
		return pageFooterFont;
	}

	public int getPageFooterXOffset() {
		return pageFooterX;
	}

	public int getPageFooterYOffset() {
		return pageFooterY;
	}

	public String getPageHeader() {
		return pageHeader;
	}

	public Font getPageHeaderFont() {
		return pageHeaderFont;
	}

	public int getPageHeaderXOffset() {
		return pageHeaderX;
	}

	public int getPageHeaderYOffset() {
		return pageHeaderY;
	}

	public Font getPageNumberFont() {
		return pageNumberFont;
	}

	public boolean showPageNumbers() {
		return needsPageNumber;
	}

	public int getLinesPerPage() {
		return linesPerPage;
	}

	public int getPageBreakSize() {
		return pageBreakSize;
	}

	public int getPageWidth() {
		return pageWidth;
	}

	public IPageInformationRenderer getPageInformationRenderer() {
		return renderer;
	}

	public void setPageInformationRenderer(IPageInformationRenderer renderer) {
		this.renderer = renderer;
	}

	public Point getPPI() {
		return Display.getDefault().getDPI();
	}

}
