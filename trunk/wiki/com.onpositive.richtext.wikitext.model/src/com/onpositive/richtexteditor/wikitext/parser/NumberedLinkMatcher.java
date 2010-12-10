package com.onpositive.richtexteditor.wikitext.parser;

public class NumberedLinkMatcher extends SimpleBracketMatcher
{

	public NumberedLinkMatcher(String startingMarkup, String endingMarkup, int returnType, String name)
	{
		super(startingMarkup, endingMarkup, returnType, name, true);
	}

	/* (non-Javadoc)
	 * @see com.onpositive.richtexteditor.wikitext.parser.SimpleBracketMatcher#checkSyntax(java.lang.String)
	 */
	
	protected boolean checkSyntax(String linkString)
	{
		if (linkString.length() > 0 && Character.isDigit(linkString.charAt(0)))
			return true;
		return false;		
	}
	
	

}
