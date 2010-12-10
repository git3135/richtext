package com.onpositive.richtext.model;

/**
 * @author kor
 *
 */
public interface IHasText {

	/**
	 * @return text
	 */
	public String getText();
	/**
	 * @param content some text
	 */
	public void setText(String content);
	
	public int getInitialHeight();
	
}
