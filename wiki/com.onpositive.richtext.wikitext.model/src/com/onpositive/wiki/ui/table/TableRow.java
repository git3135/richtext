package com.onpositive.wiki.ui.table;

import java.util.ArrayList;

import com.onpositive.richtexteditor.io.ITableStructureParser;

public class TableRow {

	ArrayList<TableCell>cells=new ArrayList<TableCell>();
	public TableRow(String readLine) {
		String[] split = readLine.split("\\|\\|");
		for (String s:split){
			if (s.length()>0){
			cells.add(new TableCell(s));
			}
		}
	}
	
	public TableRow(){
		if(cells == null){
			ArrayList<TableCell>cells=new ArrayList<TableCell>();
		}
	}

	public TableRow(String readLine, ITableStructureParser tableStructureParser)
	{
		String[] cellsText = tableStructureParser.parseTableRow(readLine.trim());
		for (int i = 0; i < cellsText.length; i++)
		{
			if (cellsText[i].length() > 0)
				cells.add(new TableCell(cellsText[i]));
		}
	}
	
}
