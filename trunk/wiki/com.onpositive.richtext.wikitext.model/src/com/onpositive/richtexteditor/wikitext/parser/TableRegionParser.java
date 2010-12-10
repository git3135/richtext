package com.onpositive.richtexteditor.wikitext.parser;

public class TableRegionParser extends ByLineRegionParser
{
	public TableRegionParser(int returnType)
	{
		super(returnType);
	}

	protected static final String TABLE_DIVIDER = "||";

	protected boolean isRegionLine(String lineText)
	{
		lineText = lineText.trim();
		if (lineText.startsWith(TABLE_DIVIDER) && lineText.endsWith(TABLE_DIVIDER))
			return true;
		return false;
	}

}
