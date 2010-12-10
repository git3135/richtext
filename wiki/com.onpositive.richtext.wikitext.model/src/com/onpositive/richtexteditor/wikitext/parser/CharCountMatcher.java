package com.onpositive.richtexteditor.wikitext.parser;

/**
 * This matcher is used for scanning tokens consisting of 1+ same characters,
 * like citation markups:
 * 
 * >>>
 * >>
 * >
 * 
 * @author Dmitry "32kda" Karpenko
 *
 */
public class CharCountMatcher extends BasicSyntaxMatcher
{
	public CharCountMatcher(int returnType, char countedChar)
	{
		super(returnType);
		startingChar = countedChar;
	}
	
	public CharCountMatcher(int returnType, String name, char countedChar)
	{
		super(returnType, name);
		startingChar = countedChar;
	}

	public LineTextLexEvent match(String text, int pos)
	{
		while (pos<text.length()){
		if (text.charAt(pos) != startingChar)
			return null;
		int count = 1;
		while (pos+count<text.length()&& text.charAt(pos + count) == startingChar)
			count++;
		return new LineTextLexEvent(text.substring(pos, pos + count), returnType, pos);
		}
		return null;
	}

}
