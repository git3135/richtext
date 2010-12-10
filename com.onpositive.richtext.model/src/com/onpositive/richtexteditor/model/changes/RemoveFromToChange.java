package com.onpositive.richtexteditor.model.changes;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.PartitionDelta;
import com.onpositive.richtext.model.PartitionStorage;

public class RemoveFromToChange extends Change
{

	protected final int offset;
	protected final int length;

	public RemoveFromToChange(int offset, int length)
	{
		this.offset = offset;
		this.length = length;
	}

	
	public void apply(PartitionDelta delta)
	{
		PartitionStorage storage = delta.getStorage();
		int end = offset + length;
		CompositeChange change = new CompositeChange(storage);
		for (BasePartition partition : storage.getPartitions())
		{
			int poffset = partition.getOffset();
			int plength = partition.getLength();
			int pend = poffset + plength;
			if (pend < offset)
			{
				continue;
			} else if (poffset >= end)
			{
				break;
			}
			// this partition is subject of interest;
			if (poffset >= offset && pend <= end)
			{
				// just remove it
				change.add(new AddRemovePartitionChange(partition.getIndex(), partition, false));
				//remove(p);
			} else if (poffset < offset)
			{
				int newLength = poffset - offset;
				{
					if (pend < end)
					{
						if (plength != -newLength)
							change.add(new AdjustPartitionChange(partition, partition.getOffset(), -newLength));
					} else
					{
						BasePartition newPartition = storage.newPartition(poffset, offset - poffset, partition);
						BasePartition newPartition2 = storage.newPartition(end, pend - end, partition);
						if (newPartition.getLength() > 0)
						{
							change.add(new AddRemovePartitionChange(partition.getIndex(), newPartition, true));
						}
						if (newPartition2.getLength() > 0)
						{
							change.add(new AddRemovePartitionChange(partition.getIndex() + 2, newPartition2, true));
						}
						change.add(new AddRemovePartitionChange(partition.getIndex(), partition, false));
						//change.remove(p);
					}
				}
			} else if (pend > end)
			{
				int newLength = pend - end;
				change.add(new AdjustPartitionChange(partition, end, newLength));
				//change.adjustPartition(p, end, newLength);
			}
		}
		change.apply(delta);
	}

	
	public String toString()
	{
		return "Remove partitions from " + offset + " length " +  length;
	}
}
