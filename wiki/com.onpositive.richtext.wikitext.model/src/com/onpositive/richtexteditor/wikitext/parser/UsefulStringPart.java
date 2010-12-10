package com.onpositive.richtexteditor.wikitext.parser;

/**
 * Describes string part, which was processed by lines parser, and shold be saved for futher token scanning
 * @author Dmitry "32kda" Karpenko
 *
 */
public class UsefulStringPart
{
	/**
	 * Describes, where useful part starts; This is a position after all string-describing markup, like indents or list bullets
	 */
	public int start;
	/**
	 * Describes useful part length
	 */
	public int length;
	/**
	 * Describes, where useful part trailing markup ends. We need this,
	 * if parsed string should be broken into 2 or more parts. This value 
	 * determines next line start
	 */
	public int end;
}
