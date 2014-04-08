package com.onpositive.wiki.ui.table;


import com.onpositive.richtexteditor.io.ITableStructureParser;


public class DefaultTableStructureParser implements ITableStructureParser
{
	protected String contents;
	protected String[] lines;
	protected int curLine = 0;
	protected int lineCount = -1;
	
	public DefaultTableStructureParser(String contents)
	{
		this.contents = contents;
		this.lineCount = getRowCount();
	}

	public String[] parseTableRow(String conents)
	{
		String[] split = conents.split("\\|\\|");
		return split;
	}

	public String getRow(int index)
	{		
		return lines[index];
	}

	public int getRowCount()
	{
		if (lineCount == -1)
		{
			contents = contents.trim();
			if (contents.length() == 0)
			{
				lines = new String[0];
				lineCount = 0;
			}
			else
			{
				lines = contents.split("\n");
				lineCount = lines.length;
			}
		}
		return lineCount;
	}

	public int getColumnCount()
	{
		if (lineCount > 0)
		{
			String line = lines[0].trim();
			int count = 0;
			int pos = 0;
			while (pos > -1)
			{
				pos = line.indexOf("||",pos);
				if (pos > 0)
					count++;
			}
			return count;
		}
		return 0;
	}

}
