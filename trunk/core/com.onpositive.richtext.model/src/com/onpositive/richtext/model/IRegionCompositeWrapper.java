package com.onpositive.richtext.model;

import com.onpositive.richtext.model.meta.Point;
import com.onpositive.richtext.model.meta.Rectangle;

/**
 * @author 32kda
 *
 */
public interface IRegionCompositeWrapper {
	/**
	 * Listeners is used to handle different events produced by in-wrapper controls, like text or line count change etc.
	 * @param listener - new region composite wrapper listener
	 */
	void addRegionCompositeWrapperListener(
			IRegionCompositeWrapperListener listener);

	/**
	 * Listeners is used to handle different events produced by in-wrapper controls, like text or line count change etc.
	 * @param listener region composite wrapper listener to remove
	 */
	void removeRegionCompositeWrapperListener(
			IRegionCompositeWrapperListener listener);

	/**
	 * @return size
	 */
	Point getSize();

	/**
	 * @return location
	 */
	Point getLocation();

	/**
	 * @param width
	 * @param height
	 */
	void setSize(int width, int height);

	/**
	 * @param x
	 * @param y
	 */
	void setLocation(int x, int y);

	/**
	 * @return x
	 */
	int getX();

	/**
	 * @return y
	 */
	int getY();

	/**
	 * @return width
	 */
	int getWidth();

	/**
	 * @return height
	 */
	int getHeight();

	/**
	 * @return initialHeight
	 */
	int getInitialHeight();

	/**
	 * @return bounds
	 */
	Rectangle getBounds();

	/**
	 * @return mainObject;
	 */
	Object getMainObject();

	/**
	 * @return topLevelObject;
	 */
	Object getTopLevelObject();

	/**
	 * dispose
	 */
	void dispose();

	/**
	 * @param initialText
	 */
	void setText(String initialText);

	/**
	 * @return content
	 */
	String getContent();
}
