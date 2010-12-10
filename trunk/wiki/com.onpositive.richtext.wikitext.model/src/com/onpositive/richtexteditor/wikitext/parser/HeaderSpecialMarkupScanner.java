package com.onpositive.richtexteditor.wikitext.parser;

import com.onpositive.richtexteditor.wikitext.io.WikitextLexEvent;

public class HeaderSpecialMarkupScanner implements ISpecialMarkupScanner
{

	/**
	 * Header marker
	 */	
	protected static char HEADER_MARKER_CHAR = '=';
	
	public WikitextLine tryToScanMarkup(String line)
	{
 		int fullLength = line.length();
		int lineIndent = ParserUtil.getLineIndent(line);
		String leftTrim = line.substring(lineIndent);
		int count = ParserUtil.countChars(leftTrim,HEADER_MARKER_CHAR,0);
		if (count > 0 && leftTrim.length() >= (count + 1) * 2 && leftTrim.charAt(count) == ' ') //Header markup should be followed by ' ' char
		{
			String headerString = leftTrim.substring(0,count);
			int lastIdx = leftTrim.lastIndexOf(headerString);
			if (lastIdx > 0 && leftTrim.charAt(lastIdx - 1) == ' ')
			{
				HeaderLine headerLine = new HeaderLine(WikitextLine.HEADER,leftTrim.substring(count + 1, lastIdx - 1),count);
				String explicitId = tryToParseExplicitId(line, lastIdx + headerString.length());
				if (explicitId != null)
				{
					headerLine.setFullParsedLength(lineIndent + lastIdx + headerString.length() + explicitId.length());
					headerLine.setId(explicitId.trim());
				}
				else
					headerLine.setFullParsedLength(lineIndent + lastIdx + headerString.length());
				
				return headerLine;
			}
		}
		return null;
	}

	protected String tryToParseExplicitId(String line, int offset)
	{
		int length = line.length();
		int pos = offset;
		StringBuilder idBuilder = new StringBuilder();
		while (pos < length && line.charAt(pos) == ' ')
		{
			pos++;
			idBuilder.append(' ');
		}
		if (length <= pos || line.charAt(pos) != '#')
			return null;
		idBuilder.append('#');
		pos++;
		if (pos < length && Character.isJavaIdentifierStart(line.charAt(pos))) {
			idBuilder.append(line.charAt(pos));
			pos++;
			while (pos < length
					&& (Character.isJavaIdentifierPart(line.charAt(pos)) || line.charAt(pos) == '-'))
				idBuilder.append(line.charAt(pos++));
			return idBuilder.toString();
		}
		return null;
	}

}
