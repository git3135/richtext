package com.onpositive.service.mediawiki.io;

import com.onpositive.richtexteditor.wikitext.parser.DefaultLinesProvider;
import com.onpositive.richtexteditor.wikitext.parser.ILineMarkupParser;
import com.onpositive.richtexteditor.wikitext.parser.LineTextLexEvent;
import com.onpositive.richtexteditor.wikitext.parser.WikitextLine;

public class MediaWikiLinesProvider extends DefaultLinesProvider
{

	public MediaWikiLinesProvider(String initialText, ILineMarkupParser lineMarkupParser)
	{
		super(initialText, lineMarkupParser, true);
	}

	public MediaWikiLinesProvider(String initialText)
	{
		super(initialText,true);
	}

	/* (non-Javadoc)
	 * @see com.onpositive.richtexteditor.wikitext.parser.DefaultLinesProvider#createRegionParsers()
	 */
	@Override
	protected void createRegionParsers()
	{
		addRegionParser(new PreRegionParser(LineTextLexEvent.REGION));
		addRegionParser(new SourceRegionParser(WikitextLine.REGION));
		addRegionParser(new MediaWikiTableRegionParser(LineTextLexEvent.TABLE));
	}
	
	

}
