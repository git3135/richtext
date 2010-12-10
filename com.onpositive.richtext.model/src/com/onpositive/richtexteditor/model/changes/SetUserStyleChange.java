package com.onpositive.richtexteditor.model.changes;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.IStylePartition;
import com.onpositive.richtext.model.PartitionDelta;
import com.onpositive.richtext.model.StylePartition;

/**
 * @author 32kda
 * This class represents style change commands for special {@link StylePartition} having 
 * int field representing some special "user" style for partition
 * This style can affect partition rendering, serializing etc.
 */
public class SetUserStyleChange extends AbstractPartitionChange
{
	protected int style;

	public SetUserStyleChange(BasePartition partition, int style)
	{
		super(partition);
		this.style = style;
	}

	public void apply(PartitionDelta delta)
	{
		if (!(partition instanceof IStylePartition))
			throw new IllegalArgumentException(partition + " is not an instance of StylePartition. Can't set style to it.");
		IStylePartition stylePartition = (IStylePartition) partition;
		int oldStyle = stylePartition.getStyle();
		stylePartition.setStyle(style);
		delta.changed(partition);
		delta.getUndoChange().add(new SetUserStyleChange(partition, oldStyle));
	}

}
