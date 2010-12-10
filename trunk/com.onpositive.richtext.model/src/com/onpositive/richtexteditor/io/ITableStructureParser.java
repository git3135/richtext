package com.onpositive.richtexteditor.io;

public interface ITableStructureParser
{
	/**
	 * Parses Wiki table row, returning an array, containing text for each of cells
	 * @param conents Row contents to parse 
	 * @return array of row cells text
	 */
	public abstract String[] parseTableRow(String conents);
	/**
	 * Returns next table row text
	 * @param index TODO
	 * @return next table row text
	 */
	public abstract String getRow(int index);
	/**
	 * Returns table row count	
	 * @return row count
	 */
	public abstract int getRowCount();
	
	/**
	 * Returns table column count
	 * @return table column count
	 */
	public abstract int getColumnCount();

}
