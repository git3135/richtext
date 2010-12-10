package com.onpositive.richtexteditor.model.changes;


/**
 * @author 32kda
 * Change class for indent changing
 */
public class SetIndentChange extends SetLineValueChange
{
	/**
	 * Basic constructor
	 * @param line first line
	 * @param count line count
	 * @param indent Indent in units to set
	 */
	public SetIndentChange(int line, int count, int indent) {
		super(line, count, indent);
	}

	public int getValueForLine(int index)
	{
		return model.getLineIndent(index);
	}


	public void setValueForLines(int index, int count, int value)
	{
		model.setLineIndent(index, count, value);
	}


}
