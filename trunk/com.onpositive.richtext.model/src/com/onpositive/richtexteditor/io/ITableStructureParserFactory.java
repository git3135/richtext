package com.onpositive.richtexteditor.io;

/**
 * Used to provide table structure parser instances
 * @author 32kda
 *
 */
public interface ITableStructureParserFactory
{
	public abstract ITableStructureParser createTableStructureParser(String tableContents);
}
