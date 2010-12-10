package com.onpositive.richtexteditor.wikitext.parser;

public class ModelBullet
{
	public static final int SIMPLE = 0;
	
	protected int type = SIMPLE;
	
	protected String bulletText;
	
	
	public ModelBullet(int type, String bulletText)
	{
		this.type = type;
		this.bulletText = bulletText;
	}
	
	public ModelBullet(String bulletText)
	{
		this(SIMPLE, bulletText);
	}

	/**
	 * @return Bullet type
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * @param type Bullet type to set
	 */
	public void setType(int type)
	{
		this.type = type;
	}

	/**
	 * @return the bulletText
	 */
	public String getBulletText()
	{
		return bulletText;
	}

	/**
	 * @param bulletText the bulletText to set
	 */
	public void setBulletText(String bulletText)
	{
		this.bulletText = bulletText;
	}
	
	
}
