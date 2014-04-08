package com.onpositive.service.mediawiki.io;

import com.onpositive.richtexteditor.wikitext.parser.ILexEventConsumer;
import com.onpositive.richtexteditor.wikitext.parser.ILineParser;
import com.onpositive.richtexteditor.wikitext.parser.WikitextLexEventConsumer;
import com.onpositive.richtexteditor.wikitext.parser.WikitextLine;
import com.onpositive.richtexteditor.wikitext.parser.WikitextParser;

public class MediaWikiTextParser extends WikitextParser
{

	public MediaWikiTextParser(ILexEventConsumer consumer, ILineParser lineParser)
	{
		super(consumer, lineParser);
	}
	
	@Override
	protected void handleCustomLineType(WikitextLine line)
	{
		int type = line.getType();
		if (type == MediaWikiParserConstants.PREFORMATTED_LINE_TYPE)
		{
			lineParser.parseLine(line);
			return;
		}
		if (type==33){
			WikitextLexEventConsumer lc=(WikitextLexEventConsumer) consumer;
			lc.handleRegion(line.getText());
			return;
		}
		
		throw new IllegalArgumentException(line.getType() + " is unknown line type.");
	}
	
}
