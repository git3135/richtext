package com.onpositive.richtexteditor.wikitext.parser;

public class ParserUtil
{
	protected static char indentChar = ' ';

	public static int getLineIndent(String line)
	{
		if (line.length() == 0)
			return 0;
		int count = 0;		
		while (line.charAt(count) == indentChar)
			count++;
		return count;
		
	}
	
	public static int countChars (String line, char chr, int from)
	{
		int pos = from;
		int length = line.length();
		while (pos < length && line.charAt(pos) == chr)
			pos++;
		return pos - from;
	}
}
