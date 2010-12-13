package com.onpositive.richtexteditor.wikitext.ui;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.FontStyle;
import com.onpositive.richtexteditor.model.changes.ExpandPartitionAtOffsetChange;

public class WikiExpandPartitionAtOffsetChange extends ExpandPartitionAtOffsetChange
{

	public WikiExpandPartitionAtOffsetChange(int offset, int amount, BasePartition currentFontPartition)
	{
		super(offset, amount, currentFontPartition);
	}
	
	
	protected boolean isWhitespace(String text, int i, boolean isHeader)
	{
		return super.isWhitespace(text, i, isHeader) || (isHeader && (text.charAt(i) == '\n' || text.charAt(i) == '\r'));
	}
	
	
	protected boolean canWhitespaceExpand(BasePartition partitionAtOffset, String text)
	{
		return super.canWhitespaceExpand(partitionAtOffset, text) && (partitionAtOffset.getFontDataName().equals(FontStyle.NORMAL_FONT_NAME) || text.indexOf('\n') == -1);
	}

}
