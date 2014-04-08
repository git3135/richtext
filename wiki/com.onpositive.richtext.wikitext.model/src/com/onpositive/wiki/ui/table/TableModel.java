package com.onpositive.wiki.ui.table;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;

import com.onpositive.richtexteditor.io.ITableStructureParser;

public class TableModel 
{

	ArrayList<TableRow> rows = new ArrayList<TableRow>();
	ArrayList<TableListener> listeners = new ArrayList<TableListener>();
	ITableStructureParser tableStructureParser;
	protected String additionalInfo;

	int minWidth = 0;

	final static int ADD_ROW = 0;
	final static int ADD_COLUMN = 2;
	final static int REMOVE_ROW = 1;
	final static int REMOVE_ROWS = 6;
	final static int REMOVE_COLUMN = 4;
	final static int REMOVE_COLUMNS = 5;
	final static int MERGE_COLUMNS = 7;
	final static int MERGE_CELLS = 8;

	public TableModel()
	{
	}

	public void setRawContent(String content)
	{

		try
		{
			BufferedReader bufferedReader = new BufferedReader(new StringReader(content));
			try
			{

				while (true)
				{
					String readLine = bufferedReader.readLine();
					if (readLine == null)
					{
						break;
					}
					addRow(readLine);
				}
			} finally
			{
				bufferedReader.close();
			}
		} catch (MalformedURLException e)
		{
			
		} catch (IOException e)
		{
			//Activator.error(e.getMessage());
		}

	}

	public TableModel(ITableStructureParser tableStructureParser)
	{
		this.tableStructureParser = tableStructureParser;
		int rowCount = tableStructureParser.getRowCount();
		for (int i = 0; i < rowCount; i++)
		{
			String row = tableStructureParser.getRow(i);
			addRow(row);
		}
	}

	public void addRow(int pos)
	{
		TableRow currentRow = new TableRow("");
		addCells(currentRow);
		rows.add(pos, currentRow);
		updateWidth();
		invokeListeners(null, null, pos, null, TableModel.ADD_ROW);
	}

	public void addRow(String readLine)
	{
		TableRow currentRow = new TableRow(readLine,tableStructureParser);
		int columns = numColumns();
		if (currentRow.cells.size() < columns)
		{
			addCells(currentRow);
		}
		rows.add(currentRow);
		updateWidth();
		invokeListeners(null, null, rows.size() - 1, null, TableModel.ADD_ROW);
	}

	private void addCells(TableRow current)
	{
		int diff = numColumns() - current.cells.size();
		if (diff > 0)
		{
			for (int i = 0; i < diff; i++)
			{
				current.cells.add(new TableCell("     "));
			}
		} else if (numColumns() == 0)
		{
			current.cells.add(new TableCell("      "));
		}
		updateWidth();
	}

	public void removeRow(int pos)
	{
		TableRow remove = rows.remove(pos);
		updateWidth();
		invokeListeners(null, remove, pos, null, TableModel.REMOVE_ROW);
	}

	public void removeRows(int[] pos)
	{
		ArrayList<TableRow> rowsForDelete = getListOfElements(rows, pos);
		rows.removeAll(rowsForDelete);
		invokeListeners(rowsForDelete.toArray(new TableRow[0]), null, -1, pos, TableModel.REMOVE_ROWS);
		updateWidth();
	}

	public void addColumn(int pos)
	{
		for (int i = 0; i < rows.size(); i++)
		{
			rows.get(i).cells.add(pos, new TableCell("empty"));
		}
		if (rows.size() == 0 && pos == 0 && numColumns() == 0)
		{
			TableRow row = new TableRow("");
			row.cells.add(pos, new TableCell("empty"));
			rows.add(0, row);
		}
		updateWidth();
		invokeListeners(null, null, pos, null, TableModel.ADD_COLUMN);
	}

	public void removeColumn(int pos)
	{
		for (int i = 0; i < rows.size(); i++)
		{
			rows.get(i).cells.remove(pos);
		}
		updateWidth();
		invokeListeners(null, null, pos, null, TableModel.REMOVE_COLUMN);
	}

	public void removeColumns(int[] pos)
	{
		ArrayList<TableRow> emptyList = new ArrayList<TableRow>();
		for (TableRow row : rows)
		{
			ArrayList<TableCell> cellsForDeleting = getListOfElements(row.cells, pos);
			row.cells.removeAll(cellsForDeleting);

			if (row.cells.size() == 0)
			{
				emptyList.add(row);
			}
		}
		rows.removeAll(emptyList);
		updateWidth();
		invokeListeners(null, null, -1, pos, TableModel.REMOVE_COLUMNS);
	}

	private <T> ArrayList<T> getListOfElements(ArrayList<T> lst, int[] pos)
	{
		ArrayList<T> result = new ArrayList<T>();
		for (int i = 0; i < pos.length; i++)
		{
			if (i < lst.size())
			{
				result.add(lst.get(pos[i]));
			}
		}
		updateWidth();
		return result;
	}

	public void addTableListener(TableListener listener)
	{
		listeners.add(listener);
	}

	public void removeListener(int pos)
	{
		if (pos < listeners.size())
		{
			listeners.remove(pos);
		}
	}

	private void invokeListeners(TableRow[] rows, TableRow row, int pos, int[] positions, int typeOfEvent)
	{
		for (TableListener l : listeners)
		{
			if (typeOfEvent == TableModel.ADD_COLUMN)
			{
				l.addColumn(pos);
			} else if (typeOfEvent == TableModel.ADD_ROW)
			{
				l.addRow(pos);
			} else if (typeOfEvent == TableModel.REMOVE_COLUMN)
			{
				l.removeColumn(pos);
			} else if (typeOfEvent == TableModel.REMOVE_ROW)
			{
				l.removeRow(pos, row);
			} else if (typeOfEvent == TableModel.REMOVE_COLUMNS)
			{
				l.removeColumns(positions);
			} else if (typeOfEvent == TableModel.REMOVE_ROWS)
			{
				l.removeRows(rows);
			} else if (typeOfEvent == TableModel.MERGE_COLUMNS)
			{
				l.mergeColumns(positions);
			} else if (typeOfEvent == TableModel.MERGE_CELLS)
			{
				l.mergeCells(positions);
			}
		}
	}

	public int numColumns()
	{
		int columns = 0;
		for (TableRow r : rows)
		{
			columns = Math.max(columns, r.cells.size());
		}
		return columns;
	}

	public void mergeColumns(int[] pos)
	{
		if (pos.length <= 1)
		{
			return;
		}
		Arrays.sort(pos);
		int min = pos[0];
		for (TableRow row : rows)
		{
			ArrayList<TableCell> cells = row.cells;
			ArrayList<TableCell> forMerge = new ArrayList<TableCell>();
			for (int index : pos)
			{
				forMerge.add(cells.get(index));
			}
			StringBuilder sb = new StringBuilder();
			for (TableCell tc : forMerge)
			{
				sb.append(tc.text + " ");
			}

			cells.removeAll(forMerge);
			cells.add(min, new TableCell(sb.toString()));
		}
		updateWidth();
		invokeListeners(null, null, -1, pos, TableModel.MERGE_COLUMNS);
	}

//	public void mergeCells(Point[] positions)
//	{
//		if (positions.length <= 1)
//		{
//			return;
//		}
//
//		Point min = findMin(positions);
//		Point max = findMax(positions);
//
//		for (int i = min.y; i <= max.y; i++)
//		{
//			StringBuilder sb = new StringBuilder();
//			TableRow tmp = rows.get(i);
//			for (int j = min.x; j <= max.x; j++)
//			{
//				TableCell cell = tmp.cells.get(j);
//				sb.append(cell.text + " ");
//				cell.text = "empty";
//			}
//			tmp.cells.get(min.x).text = sb.toString();
//		}
//
//		updateWidth();
//		invokeListeners(null, null, -1, new int[] { min.x, min.y, max.x, max.y }, TableModel.MERGE_CELLS);
//		return;
//	}
//
//	private Point findMax(Point[] pos)
//	{
//		Point max = pos[0];
//		for (Point p : pos)
//		{
//			if (p.x >= max.x && p.y >= max.y)
//			{
//				max = p;
//			}
//		}
//		return max;
//	}
//
//	private Point findMin(Point[] pos)
//	{
//		Point min = pos[0];
//		for (Point p : pos)
//		{
//			if (p.x <= min.x && p.y <= min.y)
//			{
//				min = p;
//			}
//		}
//		return min;
//	}

	private void updateWidth()
	{
		for (TableRow row : rows)
		{
			for (TableCell cell : row.cells)
			{
				if (cell.text.length() > minWidth)
				{
					minWidth = cell.text.length();
				}
			}
		}
	}

	public void setAdditionalInfo(String additionalInfo)
	{
		this.additionalInfo = additionalInfo;  
	}

	public String getAdditionalInfo()
	{
		return additionalInfo;
	}
}
