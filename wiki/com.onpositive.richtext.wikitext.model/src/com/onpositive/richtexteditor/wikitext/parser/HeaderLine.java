package com.onpositive.richtexteditor.wikitext.parser;

public class HeaderLine extends WikitextLine
{
	/**
	 * Header level, usually 1-6(or 0-5)
	 */
	protected int headerLevel;
	/**
	 * Header explicid id for different TOC's
	 */
	protected String id; 

	public HeaderLine(int type, String text, int headerLevel)
	{
		super(type, text);
		this.headerLevel = headerLevel;
	}

	/**
	 * @return the headerLevel
	 */
	public int getHeaderLevel()
	{
		return headerLevel;
	}

	/**
	 * @param headerLevel the headerLevel to set
	 */
	public void setHeaderLevel(int headerLevel)
	{
		this.headerLevel = headerLevel;
	}

	/**
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id)
	{
		this.id = id;
	}

}
