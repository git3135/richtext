package com.onpositive.service.mediawiki.io;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.ImagePartition;
import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtexteditor.wikitext.parser.LineTextLexEvent;
import com.onpositive.richtexteditor.wikitext.parser.WikitextLexEventConsumer;
import com.onpositive.richtexteditor.wikitext.parser.WikitextLine;

public class MediaWikiLexEventConsumer extends WikitextLexEventConsumer
{

	public MediaWikiLexEventConsumer(ITextDocument document)
	{
		super(document);
	}
	
	@Override
	protected void appendImage(String curSrc)
	{
		int startIdx = curSrc.indexOf(':'); //: should come after some image macro id, like image:
		if (startIdx > 0)
		{
			int width = -1;
			int endIdx = curSrc.indexOf('|',startIdx); //'|' precedes some img params, like width or height
			if (endIdx == -1)
				endIdx = curSrc.length();
			else
			{
				int nextDividerIdx = curSrc.indexOf('|',endIdx + 1);
				if (nextDividerIdx == -1)
					nextDividerIdx = curSrc.length();
				String widthStr = curSrc.substring(endIdx + 1, nextDividerIdx).trim().toLowerCase();
				if (widthStr.endsWith("px"))
				{
					widthStr = widthStr.substring(0, widthStr.length() - "px".length());
					try
					{
						width = Integer.parseInt(widthStr);
					}
					catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}
			String newSrc = curSrc.substring(startIdx + 1,endIdx).trim();
			int newOffset = getDocEndOffset();
			
			globalBuilder.append("?");		
			ImagePartition newPartition = new ImagePartition(document, newOffset, 1, null, newSrc);
			if (width > 0)
				newPartition.setWidth(width);
			partitions.add(newPartition);
			return;
		}
		else
			super.appendImage(curSrc);
	}
	
	protected void handleMediaWikiLink(LineTextLexEvent tagEvent, WikitextLine line)
	{
		curHref = getMediaWikiReference(tagEvent.text);
		int startIdx = tagEvent.text.indexOf('|') + 1; //Will be 0, if no '|' present, first after-'|' idx otherwise
		int endIdx = tagEvent.text.lastIndexOf(']');
		if (endIdx == -1 || endIdx <= startIdx)
			endIdx = tagEvent.text.length();
		if (startIdx >= endIdx)
			return; //Syntax error
		String content = tagEvent.text.substring(startIdx, endIdx)
				.trim();
		if (content.trim().length() == 0)
			content = removePreffix(curHref);
		curTextStr.append(content);
		BasePartition partition = getNewFontPartition(line);
		addIfCorrect(partition);
	}
	
	protected String getMediaWikiReference(String text)
	{
		int idx = text.indexOf('|');
		if (idx > 0)
		{
			String href = text.substring(0,idx).trim();
			return href;
		}
		return text;
	}

	@Override
	protected boolean extraHandle(LineTextLexEvent event, WikitextLine line)
	{
		if (event.getType() == MediaWikiParserConstants.MEDIAWIKI_LINK)
		{
			handleMediaWikiLink(event,line);
			return true;
		}
		return false;
	}

}
