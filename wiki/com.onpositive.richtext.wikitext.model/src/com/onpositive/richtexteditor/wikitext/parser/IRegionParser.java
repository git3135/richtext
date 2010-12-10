package com.onpositive.richtexteditor.wikitext.parser;

/**
 * Wikitext has line-based hierarcy, and lines means paragraph
 * "Region" in such terms means some piece of text, which can occupy more than one line, 
 * like regions/text frames itself, tables etc. 
 * @author Dmitry "32kda" Karpenko
 */
public interface IRegionParser
{
	public WikitextLine tryToParseRegion(String text, int offset);

}
