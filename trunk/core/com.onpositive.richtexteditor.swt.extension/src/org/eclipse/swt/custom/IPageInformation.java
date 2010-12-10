package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;

public interface IPageInformation {

	String getPageHeader();
	
	String getPageFooter();

	Font getPageHeaderFont();
	
	Font getPageFooterFont();
	
	Font getPageNumberFont();
	
	boolean showPageNumbers();
	
	int getPageFooterXOffset();
	
	int getPageFooterYOffset();
	
	int getPageHeaderXOffset();
	
	int getPageHeaderYOffset();
	
	/**
	 * 
	 * @return number of text lines per page
	 */
	int getLinesPerPage();
	
	/**
	 * 
	 * @return page width in pixels
	 */
	int getPageWidth();
	
	
	
	/**
	 * 
	 * @return page break size in pixels.
	 */
	int getPageBreakSize();
	
	/**
	 * 
	 * @return custom page information renderer or null.
	 */
	IPageInformationRenderer getPageInformationRenderer();
	/**
	 * @return break size in lines
	 * */
	IBreakSize getBreakSize(int type);
	
}
