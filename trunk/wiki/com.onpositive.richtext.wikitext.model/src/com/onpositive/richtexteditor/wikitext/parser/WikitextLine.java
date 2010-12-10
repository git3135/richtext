package com.onpositive.richtexteditor.wikitext.parser;

public class WikitextLine
{
	/**
	 * Simple line constant
	 */
	public static final int SIMPLE = 0;
	/**
	 * Region "line" constant
	 */
	public static final int REGION = 1;
	/**
	 * Dividing line/hr constant
	 */
	public static final int HR = 2;
	/**
	 * Header line constant
	 */
	public static final int HEADER = 3;
	/**
	 * Constant for lines, which can't be merged with another ones
	 */
	public static final int SEPARATE_LINE = 4;
	
	public static final int MAX_TYPE = 10;
	
	protected int type;
	
	protected ModelBullet bullet;
	
	protected int indent;
	
	protected int markupIndent;
	
	protected String text;
	
	protected int fullParsedLength = -1;

	public WikitextLine(int type, String text)
	{
		super();
		
		
		this.type = type;
		this.text = text;
	}
	
	public WikitextLine(int type, String text, int indent, ModelBullet bullet)
	{
		this(type,text);
		this.indent = indent;
		this.bullet = bullet;
	}

	/**
	 * @return the type
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type)
	{
		this.type = type;
	}

	/**
	 * @return the bullet
	 */
	public ModelBullet getBullet()
	{
		return bullet;
	}

	/**
	 * @param bullet the bullet to set
	 */
	public void setBullet(ModelBullet bullet)
	{
		this.bullet = bullet;
	}

	/**
	 * @return the indent
	 */
	public int getIndent()
	{
		return indent;
	}

	/**
	 * @param indent the indent to set
	 */
	public void setIndent(int indent)
	{
		this.indent = indent;
	}

	/**
	 * @return the markupIndent
	 */
	public int getMarkupIndent()
	{
		return markupIndent;
	}

	/**
	 * @param markupIndent the markupIndent to set
	 */
	public void setMarkupIndent(int markupIndent)
	{
		this.markupIndent = markupIndent;
	}

	/**
	 * @return the text
	 */
	public String getText()
	{
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text)
	{
		this.text = text;
	}
	
	/**
	 * Returns full successfully scanned text length
	 * @return
	 */
	public int getFullParsedLength()
	{
		if (fullParsedLength >= 0)
			return fullParsedLength;
		return markupIndent + text.length();
	}

	public void append(String current)
	{
		text = text + removeIndents(current);		
	}

	protected String removeIndents(String current)
	{		
		String leftTrim = current.substring(ParserUtil.getLineIndent(current));
		if (!text.endsWith(" ") && !leftTrim.startsWith(" "))
			leftTrim = " " + leftTrim;
		return leftTrim;
	}
	
	public String toString()
	{
		return text;
	}

	/**
	 * @param fullParsedLength the fullParsedLength to set
	 */
	public void setFullParsedLength(int fullParsedLength)
	{
		this.fullParsedLength = fullParsedLength;
	}
}
