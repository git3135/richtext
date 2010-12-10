package com.onpositive.richtexteditor.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.BasePartitionLayer;
import com.onpositive.richtext.model.LinkPartition;
import com.onpositive.richtext.model.PartitionStorage;
import com.onpositive.richtexteditor.model.changes.Change;
import com.onpositive.richtexteditor.model.changes.CompositeChange;
import com.onpositive.richtexteditor.model.changes.SetUrlChange;
import com.onpositive.richtexteditor.model.changes.SetUserStyleChange;

public class RichtextCommandCreator
{
	protected BasePartitionLayer layer;
	
	public RichtextCommandCreator(BasePartitionLayer layer)
	{
		this.layer = layer;
	}
	
	public Change createSetPartitionStyleChange(int offset, int length, int style)
	{
		CompositeChange composite = new CompositeChange(layer.getStorage());
		Collection<BasePartition> newPartitions = getAffectedPartitionsList(offset, length);
		for (Iterator<BasePartition> iterator = newPartitions.iterator(); iterator
				.hasNext();) {
			BasePartition curPartition = (BasePartition) iterator.next();
			SetUserStyleChange change = new SetUserStyleChange(curPartition, style); 
			composite.add(change);
		}
		return composite;		
	}
	
	public Change createSetUrlChange(LinkPartition partition, String newUrl)
	{
		return new SetUrlChange(partition, newUrl);
	}
	
	protected Collection<BasePartition> getAffectedPartitionsList(int offset, int length)
	{
		ArrayList<BasePartition> result = new ArrayList<BasePartition>();
		BasePartition newPartition = (BasePartition) layer.getPartitionAtOffset(offset);
		if (newPartition == null && offset > 0)
			newPartition = (BasePartition) layer.getPartitionAtOffset(offset - 1);
		if (newPartition == null)
			throw new IllegalArgumentException("Invalid offset " + offset +". Can't get suitable partition for change");
		PartitionStorage storage = layer.getStorage();
		for (int i = newPartition.getIndex(); i < storage.size() && storage.get(i).getOffset() < offset + length; i++)
			result.add(storage.get(i));
		return result;
	}
}
