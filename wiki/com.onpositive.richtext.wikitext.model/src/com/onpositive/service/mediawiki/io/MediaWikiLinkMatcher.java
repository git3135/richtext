package com.onpositive.service.mediawiki.io;

import java.util.Iterator;
import java.util.List;

import com.onpositive.richtexteditor.wikitext.parser.SimpleBracketMatcher;

public class MediaWikiLinkMatcher extends SimpleBracketMatcher
{
	public static final String STARTING_BRACE = "[[";
	public static final String ENDING_BRACE = "]]";

	public MediaWikiLinkMatcher(int returnType)
	{
		super(STARTING_BRACE, ENDING_BRACE, returnType);
	}

	public MediaWikiLinkMatcher(int returnType, String name, boolean includeMarkup)
	{
		super(STARTING_BRACE, ENDING_BRACE, returnType, name, includeMarkup);
		// TODO Auto-generated constructor stub
	}

	public MediaWikiLinkMatcher(int returnType, String name)
	{
		super(STARTING_BRACE, ENDING_BRACE, returnType, name);
		// TODO Auto-generated constructor stub
	}
	
	/** (non-Javadoc)
	 * @see com.onpositive.richtexteditor.wikitext.parser.SimpleBracketMatcher#checkSyntax(java.lang.String)
	 */
	@Override
	protected boolean checkSyntax(String linkString)
	{
		linkString = linkString.toLowerCase().trim();
		int idx = linkString.indexOf(':');
		if (idx >= 0)
		{
			String startStr = linkString.substring(0,idx);
			List<String> tokens = MediaWikiTokenProvider.getTokens(MediaWikiTokenProvider.IMAGE);
			for (Iterator<String> iterator = tokens.iterator(); iterator.hasNext();)
			{
				String token = (String) iterator.next();
				if (startStr.equals(token))
					return false;
			}
			return true;
		}
		return true;
	}
	
	
	
	

}
