package com.onpositive.service.mediawiki.io;

import java.util.Iterator;
import java.util.List;

import com.onpositive.richtexteditor.wikitext.parser.ISyntaxMatcher;
import com.onpositive.richtexteditor.wikitext.parser.SimpleBracketMatcher;

public class MediaWikiImageMatcher extends SimpleBracketMatcher implements ISyntaxMatcher
{

	public MediaWikiImageMatcher(int returnType, String name, boolean includeMarkup)
	{
		super(MediaWikiLinkMatcher.STARTING_BRACE, MediaWikiLinkMatcher.ENDING_BRACE, returnType, name, includeMarkup);
	}

	public MediaWikiImageMatcher(int returnType, String name)
	{
		super(MediaWikiLinkMatcher.STARTING_BRACE, MediaWikiLinkMatcher.ENDING_BRACE, returnType, name);
	}

	public MediaWikiImageMatcher(int returnType)
	{
		super(MediaWikiLinkMatcher.STARTING_BRACE, MediaWikiLinkMatcher.ENDING_BRACE, returnType);
	}
	
	protected boolean checkSyntax(String linkString)
	{
		linkString = linkString.trim().toLowerCase();
		List<String> tokens = MediaWikiTokenProvider.getTokens(MediaWikiTokenProvider.IMAGE);
		for (Iterator<String> iterator = tokens.iterator(); iterator.hasNext();)
		{
			String token = (String) iterator.next();
			if (linkString.startsWith(token) && linkString.length() > token.length() + 1 && 
				linkString.charAt(token.length())==':')
				return true;
		}
		return false;
	}

}
