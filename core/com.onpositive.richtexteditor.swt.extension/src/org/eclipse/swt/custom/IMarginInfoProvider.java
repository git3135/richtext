package org.eclipse.swt.custom;


public interface  IMarginInfoProvider
{
	public int getBaseMargin();

	public int getMarginAt(int lineIndex);
	
	public int getBaseRightMargin();

	public int getRightMarginAt(int lineIndex);

	public int getWrapWidthAt(int lineIndex);

}
