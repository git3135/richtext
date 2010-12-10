package com.onpositive.richtexteditor.wikitext.parser;

import java.util.Iterator;
import java.util.List;

import com.onpositive.richtexteditor.io.LexEvent;

public class DefaultLineParser implements ILineParser
{
	
	List<ISyntaxMatcher> matchers;
	ILexEventConsumer consumer;
	
	public DefaultLineParser(List<ISyntaxMatcher> matchers, ILexEventConsumer consumer)
	{
		this.matchers = matchers;
		this.consumer = consumer;
	}

	public void parseLine(WikitextLine line)
	{
		StringBuilder textBuilder = new StringBuilder();
		String text = line.getText();
		int length = text.length();
		for (int i = 0; i < length; i++)
		{
			LineTextLexEvent event = tryToMatch(line, i);
			
			if (event != null)
			{
				event.contextLine=line;
				LexEvent textEvent = new LineTextLexEvent(textBuilder.toString(), LineTextLexEvent.NONE_TYPE,0,line);
				
				textBuilder = new StringBuilder();
				consumer.handleLexEvent(textEvent);
				consumer.handleLexEvent(event);
				i += event.getFullLength() - 1;
			}
			else
				textBuilder.append(text.charAt(i));
		}
		if (textBuilder.length() > 0)
			consumer.handleLexEvent(new LineTextLexEvent(textBuilder.toString(), LineTextLexEvent.NONE_TYPE,0,line));
	}

	protected LineTextLexEvent tryToMatch(WikitextLine line, int i)
	{
		String text = line.getText();
		for (Iterator<ISyntaxMatcher> iterator = matchers.iterator(); iterator.hasNext();)
		{
			ISyntaxMatcher matcher = (ISyntaxMatcher) iterator.next();
			LineTextLexEvent event = matcher.match(text,i);
			if (event != null)
				return event;			
		}
		return null;
	}
	

}
