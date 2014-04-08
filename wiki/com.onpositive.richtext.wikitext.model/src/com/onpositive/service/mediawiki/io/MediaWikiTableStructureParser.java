package com.onpositive.service.mediawiki.io;

import java.util.ArrayList;

import com.onpositive.wiki.ui.table.DefaultTableStructureParser;

/**
 * MediaWiki table structure parser
 * For language specification please visit http://www.mediawiki.org/wiki/Help:Tables
 * @author 32kda
 *
 */

public class MediaWikiTableStructureParser extends DefaultTableStructureParser
{
	protected static final String CELL_SEPARATOR_REGEX = "\\|\\|";
	protected static final String ROW_START_MARKUP_REGEX = "\\|-"; 

	public MediaWikiTableStructureParser(String tableContents)
	{
		super(tableContents);
	}

	@Override
	public String[] parseTableRow(String contents)
	{
		contents = removeComments(contents).trim();
		String[] cellLines = contents.split("\n");		
		if (cellLines.length > 1) //Row cells is located at several lines and should be started by "|" or "!"
		{
			ArrayList<String> result = new ArrayList<String>();
			for (int i = 0; i < cellLines.length; i++)
			{
				String cellTxt = cellLines[i].trim();
				if (cellTxt.startsWith("|") || cellTxt.startsWith("!") ) //| - data cell, ! - header cell
				{
					cellTxt = cellTxt.substring(1);					
					result.add(cellTxt);
				}				
			}
			return result.toArray(new String[0]);
		} else if (contents.startsWith("|") || contents.startsWith("!")) //Single lined row - separated by ||
		{
			contents = contents.substring(1);
			String[] rawCells = contents.split(CELL_SEPARATOR_REGEX);
			return rawCells;
		}
		
		return new String[0];
	}

	protected String removeComments(String content)
	{		
		return content.replaceAll("<!--.*-->","");
	}

	public int getRowCount()
	{
		if (lineCount == -1)
		{
			contents = contents.trim().replaceAll("\\s+\\|-\\s+",ROW_START_MARKUP_REGEX);
			if (contents.length() == 0)
			{
				lines = new String[0];
				lineCount = 0;
			}
			else
			{
				String[] rawLines = contents.split(ROW_START_MARKUP_REGEX);
				ArrayList<String> result = new ArrayList<String>();
				for (int i = 0; i < rawLines.length; i++)
				{
					if (rawLines[i].length() > 0)
						result.add(rawLines[i]);
				}
				lines = result.toArray(new String[0]);
				lineCount = lines.length;
			}
		}
		return lineCount;
	}

}
