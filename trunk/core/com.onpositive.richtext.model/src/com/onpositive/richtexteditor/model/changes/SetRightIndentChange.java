package com.onpositive.richtexteditor.model.changes;


public class SetRightIndentChange extends SetLineValueChange
{

	public SetRightIndentChange(int line, int count, int value)
	{
		super(line, count, value);
	}

	public int getValueForLine(int index)
	{
		return model.getRightIndent(index);
	}

	public void setValueForLines(int index, int count, int value)
	{
		model.setRightIndent(index, count, value);
	}

}
