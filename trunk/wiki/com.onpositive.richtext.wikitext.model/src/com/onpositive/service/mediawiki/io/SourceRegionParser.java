package com.onpositive.service.mediawiki.io;

import com.onpositive.richtexteditor.wikitext.parser.IRegionParser;
import com.onpositive.richtexteditor.wikitext.parser.WikitextLine;

public class SourceRegionParser implements IRegionParser
{
	protected int returnType;
	protected static final String SOURCE_START_MARKUP = "<source";
	protected static final String SOURCE_END_MARKUP = "</source>";

	public SourceRegionParser(int returnType)
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
			if (!tag.startsWith(SOURCE_START_MARKUP))
				return null;
			tag=tag.substring(SOURCE_START_MARKUP.length());
			int textStart = i + 1;
			//textStart=text.indexOf('>',textStart);
			
			int textEnd = getTagOffset("/source",i+1,text);
			if (textEnd == -1)
				return null;
			int indexOf = tag.indexOf("lang=");
			String tq="";
			if (indexOf!=-1){
				String t=tag.substring(6);
				int iq=t.indexOf('"');
				if (iq!=-1){
					String lang=t.substring(0,iq);
					//System.out.println(lang);
					tq="#text/"+lang;
				}
			}
			String substring = tq+text.substring(textStart, textEnd);
			WikitextLine region = new WikitextLine(returnType, substring);
			
			region.setFullParsedLength(Math.min(text.length(), textEnd + SOURCE_END_MARKUP.length()) - offset);
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
