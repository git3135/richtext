package com.onpositive.richtexteditor.wikitext.parser;

public class OrderedListBullet extends ModelBullet
{
	public OrderedListBullet(int type, String bulletText)
	{
		super(type, bulletText);
	}

	/**
	 * 1. 2. 3. - markered list type constant
	 */
	public static final int NUMBER = 1;
	/**
	 * i. ii. iii. - markered list type constant
	 */	
	public static final int ROMAN = 2;
	/**
	 * a. b. c. - markered list type constant
	 */
	public static final int ABC = 3;
	
	/**
	 * initial value for list
	 */
	protected String initialValue;

	/**
	 * @return the initialValue
	 */
	public String getInitialValue()
	{
		return initialValue;
	}

	/**
	 * @param initialValue the initialValue to set
	 */
	public void setInitialValue(String initialValue)
	{
		this.initialValue = initialValue;
	}
}
