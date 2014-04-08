package com.onpositive.service.mediawiki.io;

import com.onpositive.richtexteditor.wikitext.parser.SimpleBracketMatcher;

public class BracedLinkMatcher extends SimpleBracketMatcher
{
	protected static final String START_BRACKET = "[";
	protected static final String END_BRACKET = "]";
	
	protected static final String  WIKI_LINK_PREFFIX = "wiki:";	
	protected static final String  HTTP_LINK_PREFFIX = "http://";
	protected static final String  HTTPS_LINK_PREFFIX = "https://";
	protected static final String  FTP_LINK_PREFFIX = "ftp://";
	protected static final String  MAIL_LINK_PREFFIX = "mailto:";
	protected static final String  IRC_LINK_PREFFIX = "irc://";
	protected static final String  PARENT_PREFFIX = "..";
	protected static final char RELATIVE_LINK_START_CHAR = '#';
	
	
	
	public BracedLinkMatcher(int returnType)
	{
		super(START_BRACKET, END_BRACKET, returnType);
	}
	
	public BracedLinkMatcher(int returnType, String name, boolean includeMarkup)
	{
		super(START_BRACKET, END_BRACKET, returnType, name, includeMarkup);
	}

	public BracedLinkMatcher(int returnType, String name)
	{
		super(START_BRACKET, END_BRACKET, returnType, name);
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
		if (linkString.startsWith(WIKI_LINK_PREFFIX) || 
				linkString.startsWith(MAIL_LINK_PREFFIX) ||linkString.startsWith(IRC_LINK_PREFFIX) ||linkString.startsWith(HTTP_LINK_PREFFIX) ||	linkString.startsWith(PARENT_PREFFIX) ||
			linkString.startsWith(HTTPS_LINK_PREFFIX) || linkString.startsWith(FTP_LINK_PREFFIX))
				return true;
		else if (linkString.length() > 1 && linkString.charAt(0) == RELATIVE_LINK_START_CHAR &&
				Character.isJavaIdentifierPart(linkString.charAt(1)))
			return true;
		return false;
	}

}
