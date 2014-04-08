package com.onpositive.service.mediawiki.io;

import com.onpositive.richtexteditor.io.ITableStructureParser;
import com.onpositive.richtexteditor.io.ITableStructureParserFactory;

public class MedaiWikiTableStructureParserFactory implements ITableStructureParserFactory
{

	public ITableStructureParser createTableStructureParser(String tableContents)
	{
		return new MediaWikiTableStructureParser(tableContents);
	}

}
