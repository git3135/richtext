package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.GC;

public interface IPageInformationRenderer {

	public abstract void drawFirstPageHeader(GC gc, double scalingFactor,
			int pageIndex,
			int pageOffset, ExtendedStyledText extendedStyledText, IPageInformation pageInformation);

	public abstract void drawBreak(GC gc, double scalingFactor,
			ExtendedStyledText extendedStyledText, int pos);

	public abstract void drawPageBreak(GC gc, double scalingFactor,
			ExtendedStyledText extendedStyledText, int pos);

	public abstract void drawPageHeader(GC gc, double scalingFactor, int pos,
			int pageIndex,
			int pageOffset, ExtendedStyledText extendedStyledText, IPageInformation pageInformation);

	public abstract void drawPageFooter(GC gc, double scalingFactor, int pos,
			int pageIndex,
			int pageOffset, ExtendedStyledText extendedStyledText, IPageInformation pageInformation);

	public abstract int drawPageNumber(GC gc, double scalingFactor, int lineIndex,
			int pos, int pageOffset,
			ExtendedStyledText extendedStyledText, IPageInformation pageInformation);
	
	public abstract int getFullHeaderHeight(GC gc, double scalingFactor, 
			int pageIndex,
			int pageOffset, ExtendedStyledText extendedStyledText, IPageInformation pageInformation);

}