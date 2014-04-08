package com.onpositive.service.mediawiki.io;

import com.onpositive.richtext.model.ImagePartition;
import com.onpositive.richtexteditor.io.ILineInformationProvider;
import com.onpositive.richtexteditor.io.ITextSerilizer;
import com.onpositive.richtexteditor.model.AbstractLayerManager;
import com.onpositive.richtexteditor.wikitext.io.WikitextSerializer;

public class MediaWikiSerializer extends WikitextSerializer implements ITextSerilizer
{

	public MediaWikiSerializer(AbstractLayerManager manager, ILineInformationProvider provider)
	{
		super(manager, provider);
	}
	
	@Override
	protected String getUnderlinedStartString()
	{
		return "<u>";
	}
	
	@Override
	protected String getUnderlinedEndString()
	{
		return "</u>";
	}
	
	@Override
	protected String getStrikeStartString()
	{
		return "<s>";
	}
	
	@Override
	protected String getStrikeEndString()
	{
		return "</s>";
	}
	
	@Override
	protected String getImageStr(ImagePartition partition)
	{
		String name = partition.getImageFileName();
		if (helper != null) {
			name = helper.getImageLocation(partition);
		}
		
		return "[[image:" + name + "]]";
	}
	
	protected String buildBulletMarker(int lineIndex, String markup)
	{
		int indent = provider.getLineIndent(lineIndex);
		StringBuilder builder = new StringBuilder(indent);
		for (int i = 0; i < indent; i++)
		{
			builder.append(markup);
		}
		return builder.toString();
	}
	
	@Override
	protected String getIndentStrForLine(int lineIndex)
	{
		int bulletType = provider.getBulletType(lineIndex);
		if (bulletType == ILineInformationProvider.SIMPLE_BULLET)
			return buildBulletMarker(lineIndex, "*");
		else if (bulletType != ILineInformationProvider.NONE_BULLET)
			return buildBulletMarker(lineIndex, "#");
		return "";
	}
	
	@Override
	protected String getIndentStr()
	{
		return "";
	}
	
	
	
	
}
