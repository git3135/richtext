package com.onpositive.richtexteditor.wikitext.partitions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.BasePartitionLayer;
import com.onpositive.richtext.model.IPartition;
import com.onpositive.richtext.model.LinkPartition;
import com.onpositive.richtexteditor.model.LayerEvent;

public class LinkPartitionsStyleUpdater {

	protected BasePartitionLayer layer;
	protected IPartitionsUpdater updater;

	public LinkPartitionsStyleUpdater(BasePartitionLayer layer,
			IPartitionsUpdater updater) {
		this.layer = layer;
		this.updater = updater;
	}

	public void updatePartitions() {
		List<BasePartition> partitions = layer.getStorage().getPartitions();
		ArrayList<BasePartition> linkPartitions = new ArrayList<BasePartition>();
		for (Iterator<BasePartition> iterator = partitions.iterator(); iterator
				.hasNext();) {
			BasePartition basePartition = (BasePartition) iterator.next();
			if (basePartition instanceof LinkPartition) {
				linkPartitions.add( basePartition);
			}
		}
		updater.updatePartitions(linkPartitions, new IUpdatedCallback()
		{
			
			public void partitionsUpdated(Collection<IPartition> updated)
			{
				layer.handleEvent(new LayerEvent(layer, updated));				
			}
		});

	}
	
	public void processNewLink(LinkPartition partition){
		updater.updatePartition(partition);
	}

}
