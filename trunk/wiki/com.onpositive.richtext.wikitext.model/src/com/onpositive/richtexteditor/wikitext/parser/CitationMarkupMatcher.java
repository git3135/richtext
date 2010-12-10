package com.onpositive.richtexteditor.wikitext.parser;

public class CitationMarkupMatcher extends CharCountMatcher
{

	public CitationMarkupMatcher(int returnType, char citationChar)
	{
		super(returnType, "Citation", citationChar);
	}
	
	public LineTextLexEvent match(String text, int pos)
	{
		if (pos > 0)
			return null;
		return super.match(text, pos);
	}

}
