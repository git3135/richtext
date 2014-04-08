package com.onpositive.service.mediawiki.io;

import com.onpositive.richtexteditor.wikitext.parser.LineTextLexEvent;
import com.onpositive.richtexteditor.wikitext.parser.WikitextLine;

public class MediaWikiParserConstants
{
	public static final int MEDIAWIKI_LINK = LineTextLexEvent.MAX_TYPE + 1;
	
	public static final int PREFORMATTED_LINE_TYPE = WikitextLine.MAX_TYPE + 1;
}
