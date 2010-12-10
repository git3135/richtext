package com.onpositive.richtexteditor.wikitext.parser;

public class TicketRefMatcher extends BasicSyntaxMatcher
{
	protected char refStartingChar = '#';

	public TicketRefMatcher(int returnType)
	{
		super(returnType);
	}

	public TicketRefMatcher(int returnType, String name)
	{
		super(returnType, name);
	}

	
	public LineTextLexEvent match(String text, int pos)
	{
		if (text.charAt(pos) != refStartingChar)
			return null;
		int idx = pos + 1;		
		while (idx<text.length()&&Character.isDigit(text.charAt(idx)))
			idx++;
		if (idx == pos + 1) //No digits after '#'
			return null;
		String value = text.substring(pos + 1, idx);		
		return new LineTextLexEvent("#" + value,LineTextLexEvent.LINK,pos);
	}

}
