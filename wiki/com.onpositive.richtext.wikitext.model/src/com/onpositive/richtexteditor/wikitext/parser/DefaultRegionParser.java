package com.onpositive.richtexteditor.wikitext.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultRegionParser implements IRegionParser
{
	int regionMarkupLevel = 0;
	
	public static final String REGION_START_MARKER = "{{{";
	public static final String REGION_END_MARKER = "}}}";
	
	
	public DefaultRegionParser()
	{
	}
	
	

	/*public WikitextLine tryToParseRegion(String text, int offset)
	{
		int indent = ParserUtil.countChars(text,' ',offset);
		Matcher startMatcher = startPattern.matcher(text);
		Matcher endMatcher = endPattern.matcher(text);
		Matcher innerStartMatcher = innerStartPattern.matcher(text);
		regionMarkupLevel = 0;
		if (startMatcher.find(offset + indent) && startMatcher.start() == offset + indent)
		{
			int regionContentsStart = startMatcher.end(); 
			int lastIdx = regionContentsStart;
			endMatcher.region(lastIdx, text.length());
			innerStartMatcher.region(lastIdx, text.length());
			regionMarkupLevel++;
			while (regionMarkupLevel > 0)
			{
				int idx1 = text.length();
				int idx2 = idx1;
				if (endMatcher.find(lastIdx))
					idx1 = endMatcher.start();
				if (innerStartMatcher.find(lastIdx))
					idx2 = innerStartMatcher.start();
				lastIdx = Math.min(idx1,idx2);
				if (lastIdx == idx1 && lastIdx < text.length())
				{
					regionMarkupLevel--;
					lastIdx = text.indexOf(REGION_END_MARKER,idx1) + REGION_END_MARKER.length(); //Bec. newline can be a start of second markup
					if (regionMarkupLevel == 0)
					{
						WikitextLine wikitextLine = new WikitextLine(WikitextLine.REGION, text.substring(regionContentsStart, lastIdx), indent, null);
						wikitextLine.setFullParsedLength(endMatcher.end() - offset);
						return wikitextLine;
					}
				}
				else if (lastIdx == idx2)
				{
					regionMarkupLevel++;
					lastIdx = text.indexOf(REGION_START_MARKER,idx2) + REGION_START_MARKER.length(); //Bec. newline can be a start of second markup
				}
				else
				{
					WikitextLine wikitextLine = new WikitextLine(WikitextLine.REGION, text.substring(regionContentsStart), indent, null);
					wikitextLine.setFullParsedLength(text.length() - offset);
					return wikitextLine;
				}
			}
		}
		return null;
	}*/
	
	public WikitextLine tryToParseRegion(String text, int offset)
	{
		int indent = ParserUtil.countChars(text,' ',offset);
		int startMarkupEnd = checkStart(text, offset + indent);
		if (startMarkupEnd > 0)
		{
			int regionContentsStart = startMarkupEnd;
			int regionEnd = findRegionEnd(text, offset + indent + REGION_START_MARKER.length());
			if (regionEnd - regionContentsStart > 1)
				regionContentsStart++; //-1/+1 neede for removing extra newlines
			WikitextLine wikitextLine = new WikitextLine(WikitextLine.REGION, text.substring(regionContentsStart, regionEnd - 1), indent, null);
			wikitextLine.setFullParsedLength(Math.min(text.length(), regionEnd + REGION_END_MARKER.length()) - offset);
			return wikitextLine;			
		}
		return null;
	}
	
	protected int checkStart(String text,int offset)
	{
		int lineEndOffset = text.indexOf("\n",offset);
		if (lineEndOffset == -1)
			return -1;
		String line = text.substring(offset,lineEndOffset).trim();
		if (line.trim().equals(REGION_START_MARKER))
			return lineEndOffset;
		return -1;	
	}
	
	protected int findRegionEnd(String text,int offset)
	{
		int level = 1;
		int lastIdx = offset;
		while (level > 0)
		{
			int lineEnd = text.indexOf("\n", lastIdx + 1);
			if (lineEnd == -1)
				return text.length();
			String current = text.substring(lastIdx,lineEnd).trim();
			if (current.equals(REGION_END_MARKER))
			{
				level--;
				if (level == 0)
					return text.indexOf(REGION_END_MARKER, lastIdx);
			}
			else if (current.equals(REGION_START_MARKER))
				level++;
			lastIdx = lineEnd;
		}
		return lastIdx;
	}

}
