package com.onpositive.service.mediawiki.io;

import com.onpositive.richtexteditor.wikitext.parser.IRegionParser;
import com.onpositive.richtexteditor.wikitext.parser.ParserUtil;
import com.onpositive.richtexteditor.wikitext.parser.WikitextLine;

public class MediaWikiTableRegionParser implements IRegionParser
{
	
	public static final String REGION_START_MARKER = "{|";
	public static final String REGION_END_MARKER = "|}";
	
	protected int returnType;
	
	public MediaWikiTableRegionParser(int returnType)
	{
		this.returnType = returnType;
	}

	
	public WikitextLine tryToParseRegion(String text, int offset)
	{
		int indent = ParserUtil.countChars(text,' ',offset);
		int startMarkupEnd = checkStart(text, offset + indent);
		if (startMarkupEnd > 0)
		{
			int regionContentsStart = startMarkupEnd;
			int regionEnd = findRegionEnd(text, offset + indent + REGION_START_MARKER.length());
		//	if (regionEnd - regionContentsStart > 1)
		//		regionContentsStart++; //-1/+1 needed for removing extra newlines
			if (regionEnd<regionContentsStart){
				regionEnd=regionContentsStart+1;
			}
			WikitextLine wikitextLine = new WikitextLine(returnType, text.substring(regionContentsStart, regionEnd - 1), indent, null);
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
		if (line.trim().startsWith(REGION_START_MARKER)) //Starting markup in MW can have some attributes after it
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
