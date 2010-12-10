package com.onpositive.richtexteditor.wikitext.parser;
/**
 * This matcher is intended to be used for matching some tokens, which have start and end markers,
 * like some markup enclosed into [..], {...} and etc.
 * @author Dmitry "32kda" Karpenko
 *
 */
public class SimpleBracketMatcher extends BasicSyntaxMatcher
{
	
	protected String startingMarkup;
	protected String endingMarkup;
	
	protected int startingMarkupLength;
	protected boolean includeMarkup = false;
	
	public SimpleBracketMatcher(String startingMarkup, String endingMarkup, int returnType)
	{
		super(returnType);
		this.startingMarkup = startingMarkup;
		this.endingMarkup = endingMarkup;
		startingMarkupLength = startingMarkup.length();
		acquireStartingChar(startingMarkup);
	}
	
	public SimpleBracketMatcher(String startingMarkup, String endingMarkup, int returnType, String name)
	{
		this(startingMarkup, endingMarkup, returnType);
		this.name = name;		
	}
	
	public SimpleBracketMatcher(String startingMarkup, String endingMarkup, int returnType, String name, boolean includeMarkup)
	{
		this(startingMarkup, endingMarkup, returnType, name);
		this.includeMarkup = includeMarkup;
	}

	public LineTextLexEvent match(String text, int pos)
	{
		if (!checkStart(text, pos))
			return null;
		if (pos + startingMarkupLength >= text.length())
			return null;
		if (startingMarkupLength > 1 && !(text.substring(pos, pos + startingMarkupLength).equals(startingMarkup)))
			return null;
		int endIdx = text.indexOf(endingMarkup, pos + startingMarkupLength);
		if (endIdx == -1)
			return null;
		String eventString = getEventString(text, pos, endIdx);
		if (checkSyntax(eventString))
		{
			LineTextLexEvent event = new LineTextLexEvent(eventString, returnType, pos);
			if (!includeMarkup)
				event = new LineTextLexEvent(eventString, returnType, pos);
			else
				event = new LineTextLexEvent(text.substring(pos, endIdx + endingMarkup.length()), returnType, pos);
			event.setFullLength(endIdx + endingMarkup.length() - pos); //to know full length of text sucessfully parsed
			return event;
		}
		return null;
	}

	protected String getEventString(String text, int startIdx, int endIdx)
	{
		return text.substring(startIdx + startingMarkupLength, endIdx).trim();
	}
	
	/**
	 * Used to check, whether "extracted" content between brackets really matches
	 * @param linkString String to check
	 * @return <code>true</code> if it matches, <code>false</code> otherwise
	 * This method is intended to be overriden if needed
	 */
	protected  boolean checkSyntax(String linkString)
	{
		return true;		
	}


}
