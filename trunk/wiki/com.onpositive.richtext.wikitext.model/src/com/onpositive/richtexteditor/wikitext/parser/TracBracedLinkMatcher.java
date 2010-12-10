package com.onpositive.richtexteditor.wikitext.parser;

public class TracBracedLinkMatcher extends SimpleBracketMatcher
{
	
	protected static final String  WIKI_LINK_PREFFIX = "wiki:";
	protected static final String  TRAC_LINK_PREFFIX = "trac:";
	protected static final String  COMMENT_LINK_PREFFIX = "comment:";
	protected static final String  TICKET_LINK_PREFFIX = "ticket:";
	protected static final String  HTTP_LINK_PREFFIX = "http://";
	protected static final String  HTTPS_LINK_PREFFIX = "https://";
	protected static final String  FTP_LINK_PREFFIX = "ftp://";
	protected static final String  ATTACHMENT_LINK_PREFFIX = "attachment:";
	protected static final String  PARENT_PREFFIX = "..";
	protected static final char RELATIVE_LINK_START_CHAR = '#';
	
	public TracBracedLinkMatcher(String startingMarkup, String endingMarkup, int returnType)
	{
		super(startingMarkup, endingMarkup, returnType);
	}
	
	protected boolean checkSyntax(String linkString)
	{
		String checkString = linkString;
		int firstSpace = linkString.indexOf(' ');
		if (firstSpace > 0)
			checkString = linkString.substring(0,firstSpace);
		return checkHyperlinkSyntax(checkString);
	}
	
	protected boolean checkHyperlinkSyntax(String linkString)
	{
		if (CamelCaseMatcher.isCamelCaseWord(linkString) || linkString.startsWith(WIKI_LINK_PREFFIX) || 
			linkString.startsWith(HTTP_LINK_PREFFIX) ||	linkString.startsWith(TICKET_LINK_PREFFIX) || 
			linkString.startsWith(COMMENT_LINK_PREFFIX) || linkString.startsWith(TRAC_LINK_PREFFIX) ||
			linkString.startsWith(ATTACHMENT_LINK_PREFFIX) || linkString.startsWith(PARENT_PREFFIX) ||
			linkString.startsWith(HTTPS_LINK_PREFFIX) || linkString.startsWith(FTP_LINK_PREFFIX))
				return true;
		else if (linkString.length() > 1 && linkString.charAt(0) == RELATIVE_LINK_START_CHAR &&
				Character.isJavaIdentifierPart(linkString.charAt(1)))
			return true;
		return false;
	}

}
