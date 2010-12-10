package com.onpositive.richtexteditor.wikitext.parser;

import java.util.Iterator;
import java.util.List;

public class EscapedTokenMatcher extends BasicSyntaxMatcher
{
	protected List<ISyntaxMatcher> escapeables;
	protected char escapeChar;

	public EscapedTokenMatcher(int returnType, char escapeChar, List<ISyntaxMatcher> escapeables)
	{
		super(returnType);
		this.escapeables = escapeables;
		this.escapeChar = escapeChar;
	}
	
	

	public EscapedTokenMatcher(int returnType, String name, char escapeChar, List<ISyntaxMatcher> escapeables)
	{
		super(returnType, name);
		this.escapeables = escapeables;
		this.escapeChar = escapeChar;
	}



	public LineTextLexEvent match(String text, int pos)
	{
		if (text.charAt(pos) != escapeChar)
			return null;
		int idx = pos + 1;
		if (idx >= text.length())
			return null;
		LineTextLexEvent event = tryToMatchEscaped(text, idx);
		if (event != null)
		{	
			int fullLength = event.getFullLength();
			//event.text = text.substring(idx, idx + fullLength); //For re			
			event.setFullLength(fullLength + 1);
			event.setType(returnType);
			return event;
		}
		return null;
	}



	protected LineTextLexEvent tryToMatchEscaped(String text, int idx)
	{
		for (Iterator<ISyntaxMatcher> iterator = escapeables.iterator(); iterator.hasNext();)
		{
			ISyntaxMatcher matcher = (ISyntaxMatcher) iterator.next();
			LineTextLexEvent match = matcher.match(text, idx);
			if (match != null)
	 			return match;
		}
		return null;
	}

}
