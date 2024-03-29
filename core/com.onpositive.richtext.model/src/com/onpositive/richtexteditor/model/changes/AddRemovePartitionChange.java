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

package com.onpositive.richtexteditor.model.changes;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.PartitionDelta;
import com.onpositive.richtext.model.PartitionStorage;
import com.onpositive.richtext.model.RegionPartition;

/**
 * @author kor
 * Change class incapsulating adding/removing partition command
 * IMPORTAINT:
 * For replacing one partition on (x,y) with another having different type/style,
 * use {@link ReplacePartitionChange} instead of Add+Remove pair 
 */
public class AddRemovePartitionChange extends AbstractPartitionChange{

	boolean add;
	int index;
	
	/**
	 * Basic constructor
	 * @param index Change index 
	 * @param partition partition to add/remove
	 * @param add true if we should add partition, false - if remove
	 */
	public AddRemovePartitionChange(int index,BasePartition partition,boolean add) {
		super(partition);
/*		if (!add){
			if (partition.index ==-1){
				throw new IllegalArgumentException();
			}
		}*/
		this.index=index;
		this.add=add;
	}

	

	
	public void apply(PartitionDelta delta) {
		PartitionStorage storage=delta.getStorage();
		if (add){
			storage.insertPartition(index, partition);
			delta.added(partition);
		}
		else{			
			if (partition.getIndex()==-1){ //TODO Made duplicate finding, but can't be shure, whether we don't use this method for replacing partitions
				BasePartition duplicate = storage.findDuplicate(partition);
				if (duplicate == null)
					return; //Already removed
				else
					partition = duplicate;
				/*BasePartition removePartition = storage.removePartition(index);
				if (removePartition instanceof RegionPartition)
					((RegionPartition) removePartition).dispose();
				delta.removed(removePartition);*/
			}
				if (partition instanceof RegionPartition)
					((RegionPartition) partition).dispose();
				storage.removePartition(partition);
				delta.removed(partition);
		}
		delta.getUndoChange().add(new AddRemovePartitionChange(index,partition,!add));
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		String res;
		if (add)
			res = "Add ";
		else
			res = "Remove ";
		res = res + partition.toString() + " index " + index; 
		return res;
	}

}
