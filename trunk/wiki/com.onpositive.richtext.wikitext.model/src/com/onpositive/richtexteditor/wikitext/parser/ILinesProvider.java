package com.onpositive.richtexteditor.wikitext.parser;

import java.util.List;

public interface ILinesProvider
{
	/**
	 * Should provide wikitext lines list after removing all unuseful lines and defining all meanable line types,
	 * like hr or region(should appear as single line) 
	 * @return list, containing from {@link WikitextLine} type-text pairs
	 */
	public List<WikitextLine> getLinesList();	
	
}
