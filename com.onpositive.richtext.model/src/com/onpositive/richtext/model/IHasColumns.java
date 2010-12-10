package com.onpositive.richtext.model;

/**
 * Represents some UI widget or it's wrapper, which has columns
 * @author 32kda
 *
 */
public interface IHasColumns
{
	/**
	 * Causes columns size change according to parent widget size
	 * @param parentWidth Parent widget width
	 */
	public void updateSizeOfColumns(int parentWidth);
}
