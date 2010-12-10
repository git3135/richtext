package com.onpositive.richtexteditor.wikitext.parser;

import java.util.List;

public interface ILineMarkupParser
{
	
	/**
	 * Scans given line for starting markup, like indents, bullets or header type  
	 * @param line text line to scan
	 * @return value indicating, that line is need to be breaked into 2 parts
	 */
	public WikitextLine parseLine(String line);
	
/*	*//**
	 * Checks, whether given line should be merged with last line scanned;
	 * We need such a merging, than some new line comes without paragraph break after bulleted/indented line, and has
	 * the same indent as a previous line, for example lines
	 * 
	 *  * item 1
	 *    rest of item 1 description
	 *    
	 * in TracWiki should be merged due to having same indent. 
	 * @param line line to check
	 * @return <code>true</code>, if lines should ne merged 
	 *//*
	public boolean isSameLineWithLast(String line);*/
	
	/**
	 * Checks, whether given line has bullet markup in front of it
	 * @param line Line to check for bullet present
	 * @return <code>true</code>, if it has, <code>false</code> otherwise
	 */
	public boolean hasBullet(String line);
	
	/**
	 * Calculates line indent by it's markup
	 * @param line line to calculate indent for
	 * @return indent level of given line
	 */
	public int getLineIndent(String line);
}
