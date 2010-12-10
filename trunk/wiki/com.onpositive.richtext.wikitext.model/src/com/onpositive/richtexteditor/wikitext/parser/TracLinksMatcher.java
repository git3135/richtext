package com.onpositive.richtexteditor.wikitext.parser;

public class TracLinksMatcher extends BasicSyntaxMatcher
{
	protected static final String TICKET = "ticket";
	protected static final String COMMENT = "comment";
	protected static final String CHANGESET = "changeset";
	protected static final String MILESTONE = "milestone";
	protected static final String DIFF = "diff";
	protected static final String SOURCE = "source";
	protected static final String REPORT = "report";
	protected static final String LOG = "log";
	protected static final String WIKI = "wiki";
	protected static final String ATTACHMENT = "attachment";
	protected static final String REVISION = "r";
	protected static final char separator = ':';
	
	/**
	 * Needed for avoiding reparsing already  matched/unmatched words from some tail idx
	 */
	protected String lastString;
	protected int firstUnparsedIdx = 0;
	
	public TracLinksMatcher(int returnType)
	{
		super(returnType);
	}
	

	public TracLinksMatcher(int returnType, String name)
	{
		super(returnType, name);
	}

	public LineTextLexEvent match(String text, int pos)
	{
		if (!text.equals(lastString))
		{
			firstUnparsedIdx = 0;
			lastString = text;
		}
		else if (pos < firstUnparsedIdx)
			return null;
		else if (!Character.isLetter(text.charAt(pos)))
			return null;
		int idx = pos + 1;
		while (idx < text.length() && Character.isLetter(text.charAt(idx)))
			idx++;
		if (idx >= text.length())
			return null;
		String id = text.substring(pos, idx);
		if (text.charAt(idx) != separator && !id.equals(REVISION))
			return null;
		firstUnparsedIdx = idx;
		if (id.equals(TICKET) || id.equals(CHANGESET) || id.equals(MILESTONE) 
			|| id.equals(ATTACHMENT) || id.equals(SOURCE) || id.equals(DIFF)
			|| id.equals(REPORT) || id.equals(LOG) || id.equals(WIKI))
		{
			int start = idx;
			idx++;
			while (idx < text.length() && (Character.isDigit(text.charAt(idx)) || Character.isLetter(text.charAt(idx))
					|| text.charAt(idx) == separator || text.charAt(idx) == '@' || text.charAt(idx) == '/' 
					|| text.charAt(idx) == '.' || text.charAt(idx) == '-' || text.charAt(idx) == '#'))
				idx++;
			if (start + 1 == idx)
				return null;
			else
			{
				firstUnparsedIdx  = idx;
				return new LineTextLexEvent(text.substring(pos,idx), LineTextLexEvent.LINK,pos);
			}
		}
		else if (id.equals(REVISION))
		{
			int start = idx;
			while (idx < text.length() && (Character.isDigit(text.charAt(idx)) || text.charAt(idx) == separator))
				idx++;
			if (start == idx)
				return null;
			else
			{
				firstUnparsedIdx = idx;
				return new LineTextLexEvent(text.substring(pos,idx), LineTextLexEvent.LINK,pos);
			}
		}
		else if (id.equals(COMMENT))
		{
			int start = idx;
			idx++;
			while (idx < text.length() && Character.isLetter(text.charAt(idx))) 
					idx++;
			if (start + 1 == idx)
				return null;
			if (idx >= text.length() || text.charAt(idx) != separator)
				return null;
			String secondId = text.substring(start + 1, idx);			
			if (!secondId.equals(TICKET) )
				return null;
			start = idx;
			firstUnparsedIdx = idx;
			while (idx < text.length() && (Character.isDigit(text.charAt(idx)) || text.charAt(idx) == separator))
				idx++;
			if (start + 1 == idx)
				return null;
			firstUnparsedIdx = idx;
			return new LineTextLexEvent(text.substring(pos,idx), LineTextLexEvent.LINK,pos);
		}
		return null;
	}

}
