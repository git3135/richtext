/*******************************************************************************
 * Copyright (c) 2007, 2008 OnPositive Technologies (http://www.onpositive.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     OnPositive Technologies (http://www.onpositive.com/) - initial API and implementation
 *******************************************************************************/

package com.onpositive.richtext.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.onpositive.richtexteditor.model.AbstractLayerManager;
import com.onpositive.richtexteditor.model.changes.AddRemovePartitionChange;
import com.onpositive.richtexteditor.model.changes.AdjustPartitionChange;
import com.onpositive.richtexteditor.model.changes.Change;

/**
 * @author kor
 * Storage for partitions list 
 */
public class PartitionStorage {

	private ArrayList<BasePartition> partitions = new ArrayList<BasePartition>();
	private BasePartitionLayer layer;

	/**
	 * @return {@link ILineAttributeModel} associated with storage
	 */
	public ILineAttributeModel getLineAttributeModel() {
		return ((AbstractModel) layer).getLineAttributeModel();
	}

	/**
	 * Basic constructor
	 * @param layer new storage's layer
	 */
	public PartitionStorage(BasePartitionLayer layer) {
		super();
		this.layer = layer;

	}

	/**
	 * Applies a change to storage
	 * @param change change to apply
	 * @return {@link PartitionDelta} for change
	 */
	public PartitionDelta apply(Change change) {
		PartitionDelta delta = new PartitionDelta(this);
		change.apply(delta);
		Set<BasePartition> changed = delta.getChanged();
		if (delta.isOptimizeParitions()) {
			for (BasePartition p : changed) {
				tryMerge(delta, change, p);
			}
		}
		Collections.reverse(delta.getUndoChange().getParts());
		validate();
		return delta;
	}

	private void tryMerge(PartitionDelta delta, Change change, BasePartition partition) {
		int pa = partition.getIndex();
		if (pa == -1) {
			return;
		}
		while (pa > 0 && pa < partitions.size()) {
			BasePartition previouspartition = partitions.get(pa - 1);
			if (previouspartition != partition && previouspartition.equalsByStyle(partition) && 
				(previouspartition.getLength() + partition.getLength()) < AbstractLayerManager.MAX_PARTITION_SIZE) {
				AddRemovePartitionChange addRemovePartitionChange = new AddRemovePartitionChange(
						previouspartition.getIndex(), previouspartition, false);
				addRemovePartitionChange.apply(delta);
				AdjustPartitionChange adjustPartitionChange = new AdjustPartitionChange(
						partition, previouspartition.getOffset(), partition.getLength() + previouspartition.getLength());
				adjustPartitionChange.apply(delta);
				pa--;
			} else {
				break;
			}
		}
		pa = partition.getIndex();
		while (pa < partitions.size() - 1 && size() > 1) {
			BasePartition pz = partitions.get(pa + 1);
			if (pz != partition && pz.equalsByStyle(partition) && (pz.getLength() + partition.getLength()) < AbstractLayerManager.MAX_PARTITION_SIZE) {
				AddRemovePartitionChange addRemovePartitionChange = new AddRemovePartitionChange(
						pz.getIndex(), pz, false);
				addRemovePartitionChange.apply(delta);
				AdjustPartitionChange adjustPartitionChange = new AdjustPartitionChange(
						partition, partition.getOffset(), partition.getLength() + pz.getLength());
				adjustPartitionChange.apply(delta);
			} else {
				break;
			}
		}
	}

	public void insertPartition(int index, BasePartition partition) {
		
		partitions.add(index, partition);
		partition.setIndex(index);
		int size = partitions.size();
		for (int a = index + 1; a < size; a++) {
			partitions.get(a).setIndex(partitions.get(a).getIndex() + 1);
		}
	}

	/**
	 * @param partition Partition to remove
	 */
	public void removePartition(BasePartition partition) {
		if (partition.index >= 0) {
			partitions.remove(partition.index);
			int size = partitions.size();
			for (int a = partition.index; a < size; a++) {
				partitions.get(a).setIndex(partitions.get(a).getIndex() - 1);
			}
			partition.index = -1;
		}
	}

	/**
	 * Get partition from storage
	 * @param i index f partition we want to get
	 * @return partition
	 */
	public BasePartition get(int i) {
		return partitions.get(i);
	}

	/**
	 * @return storage size
	 */
	public int size() {
		return partitions.size();
	}

	/**
	 * @return last partition from the storage
	 */
	public BasePartition getLastPartition() {
		return partitions.get(partitions.size() - 1);
	}

	/**
	 * Gets partition containing specified offset
	 * @param offset Offset
	 * @return partition containing specified offset
	 */
	public BasePartition getPartitionAtOffset(int offset) {
		int size = partitions.size();
		if (offset == 0 && size == 1 && partitions.get(0).getOffset() == 0 && partitions.get(0).getLength() == 0)
			return partitions.get(0);
		for (int a = 0; a < size; a++) {
			BasePartition partition = (BasePartition) partitions.get(a);
			if (partition.getOffset() + partition.getLength() > offset)
				return partition;
		}
		return null;
	}

	/**
	 * @return all partitions of storage
	 */
	public List<BasePartition> getPartitions() {
		return partitions;
	}

	/**
	 * @return new partition for this storage
	 */
	public BasePartition newPartition() {
		return new BasePartition(layer.getDoc(), 0, 0);
	}

	/**
	 * Create a new partition for this store
	 * @param offset new partition's offset
	 * @param length new partition's length
	 * @param p partition to copy attrs from
	 * @return new partition
	 */
	public BasePartition newPartition(int offset, int length, BasePartition p) {
		return PartitionFactory.createAsSampleStyle(p, layer.getDoc(), offset, length);
	}

	/**
	 * @param newPartitions partitions list to set to this storage
	 */
	public void setPartitions(List<BasePartition> newPartitions) {
		this.partitions.clear();
		int size = newPartitions.size();
		for (int i = 0; i < size; i++) {
			BasePartition partition = newPartitions.get(i);
			this.partitions.add(partition);
			partition.setIndex(i);
		}
	}

	/**
	 * Removes specified partition
	 * @param index Index of partition to remove
	 * @return removed partition
	 */
	public BasePartition removePartition(int index) {
		BasePartition remove = partitions.remove(index);
		int size = partitions.size();
		for (int a = index; a < size; a++) {
			partitions.get(a).setIndex(partitions.get(a).getIndex() - 1);
		}
		remove.index = -1;
		return remove;
	}

	/**
	 * @return copy of storages partitions list
	 */
	public List<BasePartition> clonePartitions() {
		ArrayList<BasePartition> partition = new ArrayList<BasePartition>();
		for (BasePartition p : this.partitions) {
			partition.add(p.clone());
		}
		return partition;
	}
	
	protected void validate()
	{
		
		int size = partitions.size();
		for (int i = 1; i < size; i++)
		{
			BasePartition partition = partitions.get(i);
			BasePartition basePartition = partitions.get(i-1);
			if (partition.getOffset() != basePartition.getOffset() +  partitions.get(i-1).getLength())
			{
				String message = "Partitions list failure: partitions \n" +
						partitions.get(i-1) + "\n" +
						partition;
				throw new RuntimeException(message);
			}			
		}
	}
	
	
	public String toString()
	{
		return partitions.toString();
	}

	/**
	 * Should be used for finding offset-length duplicate partitions
	 * We need this in case of trying to remove some partition, which is created by custom logics after every "normal" change,
	 * so because of this recreation we can't expect whether some command will have "correct" partition to delete
	 * @param partition Partition to search coordinate duplicate for
	 * @return duplicate or <code>null</code> if none found
	 */
	public BasePartition findDuplicate(BasePartition partition)
	{
		for (Iterator<BasePartition> iterator = partitions.iterator(); iterator.hasNext();)
		{
			BasePartition current = (BasePartition) iterator.next();
			if (current.getOffset() == partition.getOffset() && current.getLength() == partition.getLength())
				return current;
		}
		return null;
	}
}
