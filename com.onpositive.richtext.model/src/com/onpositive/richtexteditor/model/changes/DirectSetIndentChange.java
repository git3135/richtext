package com.onpositive.richtexteditor.model.changes;

import com.onpositive.richtext.model.ILineAttributeModel;
import com.onpositive.richtext.model.PartitionDelta;

/**
 * Differs from previous by setting an indent directly, in pixels, not in some relational units
 * @author 32kda
 *
 */
public class DirectSetIndentChange extends SetIndentChange
{
	
	/**
	 * Basic constructor
	 * @param indent Indent in units to set
	 * @param count line count
	 * @param line first line
	 */
	public DirectSetIndentChange(int indent, int count, int line) {
		super(line, count, indent);
	}
	
	public void apply(PartitionDelta delta) {
		final ILineAttributeModel lineAttributeModel = delta.getStorage().getLineAttributeModel();
		final int[]indents=new int[count+1];
		for (int a=line;a<line+count;a++){
			indents[a-line]=lineAttributeModel.getLineIndentDirectly(a);
		}
		lineAttributeModel.setLineIndentDirectly(line, count, value);
		delta.getUndoChange().add(new Change(){

			
			public void apply(PartitionDelta delta) {
				for (int a=0;a<indents.length;a++){
					lineAttributeModel.setLineIndentDirectly(a+line, 1, indents[a]);
				}
				delta.getUndoChange().add(DirectSetIndentChange.this);
			}			
		});
	}
}
