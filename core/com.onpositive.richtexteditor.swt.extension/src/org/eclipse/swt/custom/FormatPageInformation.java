package org.eclipse.swt.custom;

import org.eclipse.swt.widgets.Display;

public class FormatPageInformation extends PageInformation {

	protected double width;
	protected double height = 11.7 / 2;
	
	protected StyledText styledText;
	
	public FormatPageInformation(StyledText styledText) {
		this.styledText = styledText;
	}

	public int getLinesPerPage() {
		int dpiY;
		if (styledText instanceof ExtendedStyledText)
			dpiY = ((ExtendedStyledText) styledText).getScreenPPI().y;
		else
			dpiY = Display.getDefault().getDPI().y;
		
		double dpmY = dpiY / 25.4;
		double deltaY = 5* dpmY;
		
		int lineHeight = styledText.getLineHeight();
/*		double scale = 1.0;
		if (styledText instanceof ExtendedStyledText)
			scale = ((ExtendedStyledText) styledText).getScalingFactor();*/
		int totalHeight = (int) Math.round(dpiY * height - deltaY);
	    //int count = (int) (Math.round((totalHeight - ExtendedStyledTextConstants.BREAK_SIZE) / lineHeight));
		int i = totalHeight;
		int count = (int) (Math.round((i - ExtendedStyledTextConstants.BREAK_SIZE/* + Renderer.PAGE_BREAK_SIZE*/) / (double)lineHeight));
		return count;
	}

	public int getPageWidth() {
		int dpiX;
		if (styledText instanceof ExtendedStyledText)
			dpiX = ((ExtendedStyledText) styledText).getScreenPPI().x;
		else
			dpiX = Display.getDefault().getDPI().x;
		
		double dpmX = dpiX / 25.4;
		double deltaX = 5* dpmX;
		
		return (int) Math.round(dpiX * width - deltaX) + dpiX / 2 ;// - ((ExtendedStyledText) styledText).getAdditionalPageWidth();
	}

}
