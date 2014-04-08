package com.onpositive.service.mediawiki.io;

import com.onpositive.richtexteditor.wikitext.parser.IRegionParser;
import com.onpositive.richtexteditor.wikitext.parser.WikitextLine;

public class PreRegionParser implements IRegionParser
{
	protected int returnType;
	protected static final String PRE_START_MARKUP = "<pre>";
	protected static final String PRE_END_MARKUP = "</pre>";

	public PreRegionParser(int returnType)
	{
		this.returnType = returnType;
	}

	
	public WikitextLine tryToParseRegion(String text, int offset)
	{
		if (text.charAt(offset) == '<')
		{
			int i = offset + 1;
			int length = text.length();
			while (i < length && text.charAt(i) != '>')
				i++;
			if (i >= text.length())
				return null;
			String tag = text.substring(offset, i + 1).toLowerCase().replaceAll(" ", "");
			if (!tag.equals(PRE_START_MARKUP))
				return null;
			int textStart = i + 1;
			int textEnd = getTagOffset("/pre",i+1,text);
			if (textEnd == -1)
				return null;
			WikitextLine region = new WikitextLine(returnType, text.substring(textStart, textEnd));
			region.setFullParsedLength(Math.min(text.length(), textEnd + PRE_END_MARKUP.length()) - offset);
			return region;
		}
		return null;
	}

	protected int getTagOffset(String tagName, int startingOffset, String text)
	{
		int i = startingOffset;
		int length = text.length();
		while (i < length)
		{
			if (text.charAt(i) == '<')
			{
				int j = i + 1;
				while (j < length && text.charAt(j) != '>')
					j++;
				if (j >= text.length())
					return -1;
				String parsedTag = text.substring(i + 1, j).toLowerCase().trim();
				if (tagName.equals(parsedTag))
					return i;
			}
			i++;
		}
		return -1;
	}

}
