package com.onpositive.richtexteditor.wikitext.parser;

/**
 * Behaves like {@link SimpleBracketMatcher}, but does no trimming to inside-bracket content
 * @author 32kda
 */
public class BracedEscapeMatcher extends SimpleBracketMatcher
{
	
	
	public BracedEscapeMatcher(String startingMarkup, String endingMarkup, int returnType, String name, boolean includeMarkup)
	{
		super(startingMarkup, endingMarkup, returnType, name, includeMarkup);
	}

	public BracedEscapeMatcher(String startingMarkup, String endingMarkup, int returnType, String name)
	{
		super(startingMarkup, endingMarkup, returnType, name);
	}

	public BracedEscapeMatcher(String startingMarkup, String endingMarkup, int returnType)
	{
		super(startingMarkup, endingMarkup, returnType);
	}

	protected String getEventString(String text, int startIdx, int endIdx)
	{	
		return text.substring(startIdx + startingMarkupLength, endIdx);
	}
}
