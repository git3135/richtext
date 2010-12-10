package com.onpositive.richtexteditor.model.changes;


/**
 * Encapsulates setting first visible line indent (paragraph) change
 * @author 32kda
 * (c) Made in USSR
 */
public class SetFirstLineIndentChange extends SetLineValueChange
{

	/**
	 * Basic constructor
	 * @param line first line
	 * @param count line count
	 * @param indent Indent in units to set
	 */
	public SetFirstLineIndentChange(int line, int count, int indent) {
		super(line, count, indent);
	}
	


	public int getValueForLine(int index)
	{
		return model.getFirstLineIndent(index);
	}


	public void setValueForLines(int index, int count, int value)
	{
		model.setFirstLineIndent(index, count, value);		
	}

}
