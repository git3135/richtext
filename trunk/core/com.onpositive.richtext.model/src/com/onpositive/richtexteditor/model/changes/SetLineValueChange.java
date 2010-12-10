package com.onpositive.richtexteditor.model.changes;

import com.onpositive.richtext.model.ILineAttributeModel;
import com.onpositive.richtext.model.PartitionDelta;


public abstract class SetLineValueChange extends Change
{
	protected int value;
	protected int line;
	protected int count;
	protected ILineAttributeModel model;

	/**
	 * Basic constructor
	 * @param line first line
	 * @param count line count
	 * @param value Indent in units to set
	 */
	public SetLineValueChange(int line, int count, int value) {
		super();
		this.value = value;
		this.count = count;
		this.line = line;
	}
	
	public void apply(PartitionDelta delta) {
		final ILineAttributeModel lineAttributeModel = delta.getStorage().getLineAttributeModel();
		model = lineAttributeModel;
		final int[]values=new int[count];
		for (int a=line;a<line+count;a++){
			values[a-line]=getValueForLine(a);
		}
		setValueForLines(line, count, value);
		delta.getUndoChange().add(new Change(){

			
			public void apply(PartitionDelta delta) {
				for (int a=0;a<values.length;a++){
					setValueForLines(a+line, 1, values[a]);
				}
				delta.getUndoChange().add(SetLineValueChange.this);
			}			
		});
	}
	
	public abstract int getValueForLine(int index);
	
	public abstract void setValueForLines(int index, int count, int value);
}
