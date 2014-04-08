package com.onpositive.wiki.ui.table;

public interface TableListener {
	public void addColumn(int pos);

	public void addRow(int pos);

	public void removeRow(int pos, TableRow row);

	public void removeColumn(int pos);

	public void removeColumns(int[] pos);

	public void removeRows(TableRow[] pos);
	
	public void mergeColumns(int[] pos);
	
	public void mergeCells(int[] pos);
}
