package com.onpositive.richtexteditor.model.changes;


public class SetLineSpacingChange extends SetLineValueChange
{

	public SetLineSpacingChange(int indent, int count, int line)
	{
		super(line, count, indent);
	}

	public int getValueForLine(int index)
	{
		return model.getLineSpacing(index);
	}

	public void setValueForLines(int index, int count, int value)
	{
		model.setLineSpacing(index, count, value);		
	}

}
