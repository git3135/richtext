package com.onpositive.richtexteditor.wikitext.parser;

/**
 * Common interface for all syntax matchers
 * Intended to be used for simple markup matching, like 
 * separating line into text and some simple marking symbols like ''' or ==
 */
public interface ISyntaxMatcher
{
	/**
	 * Tries to match corresponding token into text on pos
	 * @param text parsed text or text line itself
	 * @param pos position into text to try parsing
	 * @return corresponding {@link LineTextLexEvent}, if matching succeeded, <code>false</code> otherwise
	 */
	public LineTextLexEvent match(String text, int pos);
}
