package com.onpositive.richtexteditor.wikitext.parser;

public class DefaultSyntaxMatcher extends BasicSyntaxMatcher
{
	/**
	 * Markup string to match. E.g. \<b\> for html bold tag
	 */
	protected String markupString;
	
	public LineTextLexEvent match(String text, int pos)
	{
		if (!checkStart(text, pos))
			return null;
		if (pos + markupString.length() > text.length())
			return null;
		if (text.substring(pos, pos + markupString.length()).equals(markupString))
			return new LineTextLexEvent(markupString, returnType, pos);
		return null;
	}
	
	protected boolean checkStart(String text, int pos)
	{	
		return super.checkStart(text, pos) && (pos + markupString.length() <= text.length());
	}
	
	/**
	 * Creates a {@link DefaultSyntaxMatcher} using given params
	 * @param returnType int constant corresponding to this token/marker type. 
	 * It should be returned in events for matched markup
	 * @param markupString String, identifying marker itself
	 */
	public DefaultSyntaxMatcher(int returnType, String markupString)
	{
		super(returnType);
		if (markupString == null || markupString.length() == 0)
			throw new IllegalArgumentException("markupString should be non-empty string");
		this.markupString = markupString;
		acquireStartingChar(markupString);
	}
	
	/**
	 * Creates a {@link DefaultSyntaxMatcher} using given params
	 * @param returnType int constant corresponding to this token/marker type. 
	 * @param markupString String, identifying marker itself
	 * @param name Human-readable name for matcher - mostly for debugging purposes
	 * It should be returned in events for matched markup
	 */
	public DefaultSyntaxMatcher(int returnType, String markupString, String name)
	{
		super(returnType,name);
		if (markupString == null || markupString.length() == 0)
			throw new IllegalArgumentException("markupString should be non-empty string");
		this.markupString = markupString;
		acquireStartingChar(markupString);
	}
		

}
