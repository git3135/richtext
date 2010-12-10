package com.onpositive.richtexteditor.wikitext.parser;

public class DeflistRegionParser extends ByLineRegionParser
{
	public DeflistRegionParser(int returnType)
	{
		super(returnType);
	}

	protected static final String TRAC_DEFILST_DIVIDER = "::";
	int firstLineIndent = -1;

	/*public WikitextLine tryToParseRegion(String text, int offset)
	{
		//
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
				if (current.indexOf(TRAC_DEFILST_DIVIDER) > 0)
				{
					firstLineIndent = ParserUtil.getLineIndent(current);
				}
				else
				{
					int indent = ParserUtil.getLineIndent(current);
					if (indent <= firstLineIndent)
						return new WikitextLine(LineTextLexEvent.DEFLIST, text.substring(offset, pos));
						
				}
				pos = next;
			}
		}
		return null;
	}*/

	protected boolean isRegionLine(String text)
	{
		int dividerIdx = text.indexOf(TRAC_DEFILST_DIVIDER);
		if (firstLineIndent == -1)
		{
			if (dividerIdx == -1) 
				return false;
			firstLineIndent = ParserUtil.getLineIndent(text);
			if (firstLineIndent == 0)
			{
				firstLineIndent = -1;
				return false;
			}
			return true;
		}
		else 
		{
			int currentIndent = ParserUtil.getLineIndent(text);
			if (currentIndent == 0)
			{
				firstLineIndent = -1;
				return false;
			}
			if (dividerIdx > currentIndent) //There is a divider in the correct location
			{
				firstLineIndent = currentIndent;
				return true;
			}
			else if (currentIndent >= firstLineIndent)
				return true;
		}
		firstLineIndent = -1;
		return false;
		
	}

}
