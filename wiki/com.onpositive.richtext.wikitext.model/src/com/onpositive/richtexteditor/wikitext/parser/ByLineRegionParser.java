package com.onpositive.richtexteditor.wikitext.parser;

public abstract class ByLineRegionParser implements IRegionParser
{
	
	protected int returnType;
	

	public ByLineRegionParser(int returnType)
	{
		super();
		this.returnType = returnType;
	}

	public WikitextLine tryToParseRegion(String text, int offset)
	{
		int firstNewline = text.indexOf('\n', offset);
		if (firstNewline == -1)
			firstNewline = text.length();
		int firstLineIndent = ParserUtil.countChars(text, ' ', offset);
		if (!isRegionLine(text.substring(offset, firstNewline)))
			return null;

		int pos = firstNewline;
		int next;
		while(pos < text.length())
		{
			next = text.indexOf('\n',pos + 1);
			if (next > -1)
			{
				String current = text.substring(pos + 1, next);
				if (!isRegionLine(current))
					return new WikitextLine(returnType, text.substring(offset, pos));
				pos = next;
			}
		}
		return null;
	}

	protected abstract boolean isRegionLine(String lineText);

}
