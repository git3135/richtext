package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.internal.Platform;

public class PageInformationRenderer implements IPageInformationRenderer {
	
	protected Renderer renderer;
	
	public PageInformationRenderer(Renderer renderer) {
		this.renderer = renderer;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.custom.IPageInformationRenderer#drawFirstPageHeader(org.eclipse.swt.graphics.GC, double, org.eclipse.swt.custom.ExtendedStyledText, org.eclipse.swt.custom.IPageInformation)
	 */
	public void drawFirstPageHeader(GC gc, double scalingFactor,
			int pageIndex,
			int pageOffset, ExtendedStyledText extendedStyledText, IPageInformation pageInformation) {
		int pos = extendedStyledText.topMargin - extendedStyledText.getTopPixel();
		if (pos >= 0 && renderer.pagingEnabled) {
			drawPageBreak(gc, scalingFactor, extendedStyledText, pos);
			drawPageHeader(gc, scalingFactor, pos, pageIndex,
					pageOffset, extendedStyledText, pageInformation);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.custom.IPageInformationRenderer#drawBreak(org.eclipse.swt.graphics.GC, double, org.eclipse.swt.custom.ExtendedStyledText, int)
	 */
	public void drawBreak(GC gc, double scalingFactor,
			ExtendedStyledText extendedStyledText, int pos) {
		Color tmp = gc.getBackground();
		Color color =  new Color(gc.getDevice(),// new RGB(255,0,0));
				extendedStyledText.fieldColor);
		int additionalPageWidth = extendedStyledText.getAdditionalPageWidth();
		
		gc.setBackground(color);

		int x = 0;
		int y = pos;// >= extendedStyledText.clientAreaHeight ? extendedStyledText.clientAreaHeight - 10 : pos;
		int w = (int) Math.round((extendedStyledText.getPageWidth()+ additionalPageWidth)
				* scalingFactor);
		if( extendedStyledText.clientAreaHeight - pos > 0){
	//		y = pos = extendedStyledText.clientAreaHeight - (int)(extendedStyledText.averageCharHeight*scalingFactor);			
		}
		int h =  extendedStyledText.clientAreaHeight - pos > 0 ? extendedStyledText.clientAreaHeight - pos:  -extendedStyledText.clientAreaHeight +pos ;
		//Color tmp1 = gc.getForeground();
		//gc.setForeground(color);
		//System.out.println("POS " + pos + " CLIENTAREAHEIGHT " +extendedStyledText.clientAreaHeight);
		if(extendedStyledText.clientAreaHeight - pos < 0){
			int a = 0;
			a = 2;
		}
		gc.fillRectangle(x, y, w, h); //fillRectangle(x, y, w, h);
		//gc.setForeground(tmp1);
		//tmp1.dispose();
		color.dispose();
		gc.setBackground(tmp);
		tmp.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.custom.IPageInformationRenderer#drawPageBreak(org.eclipse.swt.graphics.GC, double, org.eclipse.swt.custom.ExtendedStyledText, int)
	 */
	public void drawPageBreak(GC gc, double scalingFactor,
			ExtendedStyledText extendedStyledText, int pos) {
		if(false){
			return;
		}
		

		Color tmp = gc.getBackground();
		Color color = new Color(gc.getDevice(),	extendedStyledText.fieldColor);
		int additionalPageWidth = extendedStyledText.getAdditionalPageWidth();

		gc.setBackground(color);

		gc.fillRectangle(0, pos, (int) Math.round((extendedStyledText.getPageWidth()+additionalPageWidth)
				* scalingFactor)
				-extendedStyledText.horizontalScrollOffset, (int) Math.round(Renderer.PAGE_BREAK_SIZE
				* scalingFactor));
		color.dispose();
		gc.setBackground(tmp);
		tmp.dispose();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.swt.custom.IPageInformationRenderer#drawPageHeader(org.eclipse.swt.graphics.GC, double, int, org.eclipse.swt.custom.ExtendedStyledText, org.eclipse.swt.custom.IPageInformation)
	 */
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
				pageHeaderFont = extendedStyledText.getFont();
			}
			pageHeaderFont = renderer.scaleFont(pageHeaderFont);
			int pageX = getPageHeaderXOffset(pageInformation,pageIndex, pageOffset)
					+ extendedStyledText.leftFieldSize;
			int pageY = getPageHeaderYOffset(pageInformation,pageIndex, pageOffset);
			int headerX = (int) (pageX * scalingFactor - extendedStyledText.horizontalScrollOffset);
			gc.setFont(pageHeaderFont);
			int headerY = (int) (pos + (pageY * scalingFactor) + Renderer.PAGE_BREAK_SIZE
					* scalingFactor);
			gc.drawText(pageHeader, headerX, headerY);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.custom.IPageInformationRenderer#drawPageFooter(org.eclipse.swt.graphics.GC, double, int, org.eclipse.swt.custom.ExtendedStyledText, org.eclipse.swt.custom.IPageInformation)
	 */
	public void drawPageFooter(GC gc, double scalingFactor, int pos,
			int pageIndex,
			int pageOffset, ExtendedStyledText extendedStyledText, IPageInformation pageInformation) {	    
		String pageFooter = getPageFooter(pageInformation,pageIndex, pageOffset);
		if (pageFooter != null) {
			Font pageFooterFont = getPageFooterFont(pageInformation,pageIndex, pageOffset);
			if (pageFooterFont == null) {
				pageFooterFont = extendedStyledText.getFont();
			}
			Font pageFont = renderer.scaleFont(pageFooterFont);

			int pageX = getPageFooterXOffset(pageInformation,pageIndex, pageOffset)
					+ extendedStyledText.leftFieldSize;
			int pageY = getPageFooterYOffset(pageInformation,pageIndex, pageOffset);
			int footerX = (int) (pageX * scalingFactor - extendedStyledText.horizontalScrollOffset);
			gc.setFont(pageFont);
			int footerY = (int) (pos - ((int) pageY * scalingFactor) - gc
					.getFontMetrics().getHeight());

			gc.drawText(pageFooter, footerX, footerY);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.custom.IPageInformationRenderer#drawPageNumber(org.eclipse.swt.graphics.GC, double, int, int, org.eclipse.swt.custom.ExtendedStyledText, org.eclipse.swt.custom.IPageInformation)
	 */
	public int drawPageNumber(GC gc, double scalingFactor, int lineIndex, int pos,
			int pageOffset,
			ExtendedStyledText extendedStyledText, IPageInformation pageInformation) {
		if (pageInformation == null) {
			return extendedStyledText.getAdditionalPageWidth();
		}
		int pageIndex = renderer.getPageIndex(lineIndex);
		boolean showPageNumbers = showPageNumbers(pageInformation, pageIndex, pageOffset);
		int additionalPageWidth = extendedStyledText.getAdditionalPageWidth();
		if (showPageNumbers) {
			Font font = getPageNumberFont(pageInformation, pageIndex, pageOffset);
			if (font == null) {
				font = extendedStyledText.getFont();
			}
			Font scaleFont = renderer.scaleFont(font);
			gc.setFont(scaleFont);

			int chWidth = gc.getFontMetrics().getAverageCharWidth();
			
//			if (i%linesPerPage!=0){
//				j++;
//			}
			//TODO FIX ME
			String string = "" + pageIndex;
			gc.drawText(string,	(int) (((extendedStyledText.getPageWidth()
									+ additionalPageWidth - string.length()
									* chWidth) / 2.0)
									* scalingFactor - extendedStyledText.horizontalScrollOffset),
							(int) (pos - gc.getFontMetrics().getHeight() - renderer.PAGE_NUMBER_OFFSET
									* scalingFactor));
		}
		return additionalPageWidth;
	}
	
	protected String getPageHeader(IPageInformation pageInformation, int pageIndex, int pageOffset)
	{
		return pageInformation.getPageHeader();	
	}
	
	protected String getPageFooter(IPageInformation pageInformation, int pageIndex, int pageOffset)
	{
		return pageInformation.getPageFooter();
	}

	protected Font getPageHeaderFont(IPageInformation pageInformation, int pageIndex, int pageOffset)
	{
		return pageInformation.getPageHeaderFont();
	}
	
	protected Font getPageFooterFont(IPageInformation pageInformation, int pageIndex, int pageOffset)
	{
		return pageInformation.getPageFooterFont();	
	}
	
	protected Font getPageNumberFont(IPageInformation pageInformation, int pageIndex, int pageOffset)
	{
		return pageInformation.getPageNumberFont();	
	}
	
	protected boolean showPageNumbers(IPageInformation pageInformation, int pageIndex, int pageOffset)
	{
		return pageInformation.showPageNumbers();	
	}
	
	protected int getPageFooterXOffset(IPageInformation pageInformation, int pageIndex, int pageOffset)
	{
		return pageInformation.getPageFooterXOffset();	
	}
	
	protected int getPageFooterYOffset(IPageInformation pageInformation, int pageIndex, int pageOffset)
	{
		return pageInformation.getPageFooterYOffset();	
	}
	
	protected int getPageHeaderXOffset(IPageInformation pageInformation, int pageIndex, int pageOffset)
	{
		return pageInformation.getPageHeaderXOffset();
	}
	
	public int getPageHeaderYOffset(IPageInformation pageInformation, int pageIndex, int pageOffset)
	{
		return pageInformation.getPageHeaderYOffset();
	}

	public int getFullHeaderHeight(GC gc, double scalingFactor,
			int pageIndex, int pageOffset,
			ExtendedStyledText extendedStyledText,
			IPageInformation pageInformation) {
		Font pageHeaderFont = getPageHeaderFont(pageInformation,pageIndex, pageOffset);
		if (pageHeaderFont == null) {
			pageHeaderFont = extendedStyledText.getFont();
		}
		pageHeaderFont = renderer.scaleFont(pageHeaderFont);
		int pageY = getPageHeaderYOffset(pageInformation,pageIndex, pageOffset);
		return (int) (pageY * scalingFactor) + pageHeaderFont.getFontData()[0].getHeight();
	}
	
}
