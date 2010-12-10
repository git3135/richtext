package com.onpositive.richtexteditor.wikitext.parser;

public interface ISpecialMarkupScanner
{
	/**
	 * Basic method for scanning some spec. markup from line	  
	 * @param line {@link String} to scan markup from
	 * @return if scan was successfull, {@link WikitextLine} representing info scanned
	 * if not, should return <code>null</code>
	 */
	public WikitextLine tryToScanMarkup(String line);
}
