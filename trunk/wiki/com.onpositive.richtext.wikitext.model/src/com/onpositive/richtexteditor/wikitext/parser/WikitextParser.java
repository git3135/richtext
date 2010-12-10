package com.onpositive.richtexteditor.wikitext.parser;

import java.util.Iterator;
import java.util.List;

import com.onpositive.richtexteditor.model.ISimpleRichTextModel;

public class WikitextParser
{
	protected ILexEventConsumer consumer;
	protected ILineParser lineParser;
	
	public WikitextParser(ILexEventConsumer consumer, ILineParser lineParser)
	{
		this.consumer = consumer;
		this.lineParser = lineParser;
	}
	
	public ISimpleRichTextModel parse(List<WikitextLine> lines)
	{
		for (Iterator<WikitextLine> iterator = lines.iterator(); iterator.hasNext();)
		{
			WikitextLine line = (WikitextLine) iterator.next();
			int type = line.getType();
			if (type == WikitextLine.SIMPLE || type == WikitextLine.HEADER || type == WikitextLine.SEPARATE_LINE)
				lineParser.parseLine(line);
			else if (type == WikitextLine.REGION || type == LineTextLexEvent.REGION)
				consumer.handleLexEvent(new LineTextLexEvent(line.getText(), LineTextLexEvent.REGION, 0));
			else if (type == LineTextLexEvent.DEFLIST)
				consumer.handleLexEvent(new LineTextLexEvent(line.getText(), LineTextLexEvent.DEFLIST, 0));
			else if (type == LineTextLexEvent.TABLE)
				consumer.handleLexEvent(new LineTextLexEvent(line.getText(), LineTextLexEvent.TABLE, 0));
			else if (type == WikitextLine.HR)
				consumer.handleLexEvent(new LineTextLexEvent(line.getText(), LineTextLexEvent.HR, 0));
			else 
				handleCustomLineType(line);
			consumer.handleLexEvent(new LineTextLexEvent("", LineTextLexEvent.EOL, line.getText().length()));
			
			//consumer.handleLexEvent(new LineTextLexEvent("", LineTextLexEvent.EOL, line.getText().length()));

		}
		return null;
	}

	/**
	 * Is called, if {@link WikitextLine#type}contains some unknown/user defined type
	 * @param line Line to be handled
	 * Default impl is empty.
	 * Intended to be overriden in subclasses
	 */
	protected void handleCustomLineType(WikitextLine line)
	{
		
	}
}
