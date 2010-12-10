package com.onpositive.richtexteditor.wikitext.parser;

public class SimpleHttpLinkMatcher extends DefaultSyntaxMatcher implements ISyntaxMatcher
{
	protected static final String HTTP_MARKUP = "http://";

	public SimpleHttpLinkMatcher(int returnType, String name)
	{
		super(returnType, HTTP_MARKUP, name);
		// TODO Auto-generated constructor stub
	}	
	
	public LineTextLexEvent match(String text, int pos)
	{
		int endIndex = pos + HTTP_MARKUP.length();
		if (endIndex + 1 >= text.length()) //+1 for one extra symbol after link mark
			return null;
		if (!text.subSequence(pos, endIndex).equals(HTTP_MARKUP) || Character.isWhitespace(endIndex))
			return null;
		int idx = endIndex;
		while (idx < text.length() && !Character.isWhitespace(text.charAt(idx)) && (text.charAt(idx) != '(' && text.charAt(idx) != ')'))
			idx++;		
		return new LineTextLexEvent(text.substring(pos,idx),returnType, 0);
	}

}
