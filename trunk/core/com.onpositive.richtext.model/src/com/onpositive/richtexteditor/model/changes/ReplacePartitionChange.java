package com.onpositive.richtexteditor.model.changes;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.BasePartitionLayer;
import com.onpositive.richtext.model.PartitionDelta;
import com.onpositive.richtext.model.PartitionStorage;

public class ReplacePartitionChange extends AbstractPartitionChange
{
	protected BasePartition oldPartition;

	public ReplacePartitionChange(BasePartition partition)
	{
		super(partition);		
	}

	public void apply(PartitionDelta delta)
	{
		PartitionStorage storage=delta.getStorage();
		
		oldPartition = (BasePartition) storage.getPartitionAtOffset(partition.getOffset());
		if (!(oldPartition.getOffset() == partition.getOffset() && oldPartition.getLength() == partition.getLength()))
			throw new IllegalArgumentException("Old partition offset/length must be equal to new partition offset/length!");
		
		int position = oldPartition.getIndex();
		storage.insertPartition(position, partition);		
		storage.removePartition(position + 1);
		delta.changed(partition);
		delta.getUndoChange().add(new ReplacePartitionChange(oldPartition));
	}
	
	/** (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{	
		if (oldPartition != null)
			return "Replace " + oldPartition + " with " + partition;
		return "Replace smth. with " + partition;
	}

}
