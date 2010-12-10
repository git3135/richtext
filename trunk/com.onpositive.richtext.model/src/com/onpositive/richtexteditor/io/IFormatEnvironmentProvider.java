package com.onpositive.richtexteditor.io;

/**
 * This class should be used as an adapter 
 * for getting IO environment by some editor-connected services
 * @author 32kda
 */
public interface IFormatEnvironmentProvider
{
	ITextLoader getLoader();
	ITextSerilizer getSerializer();
	ITableStructureParserFactory getTableStructureParserFactory();
	void setTableStructureParserFactory(ITableStructureParserFactory factory);
}
