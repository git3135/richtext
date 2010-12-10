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

/**
 * @author kor
 * Change class incapsulating partition position change command
 */
public class AdjustPartitionChange extends AbstractPartitionChange{

	private int offset;
	private int length;

	/**
	 * Basic constructor
	 * @param partition Partition to perform change at
	 * @param offset new offset for it
	 * @param length new length for it
	 */
	public AdjustPartitionChange(BasePartition partition, int offset, int length) {
		super(partition);
		this.offset=offset;
		this.length=length;
	}

	

	
	
	public void apply(PartitionDelta delta) {
		if (partition.getIndex() == -1) //Removed by another partition
		{
			BasePartition candidate = delta.getStorage().getPartitionAtOffset(partition.getOffset());
			if (candidate.getOffset() == partition.getOffset() && candidate.getLength() == partition.getLength()) //Maybe we replaced old partition by new, with same coords
				partition = candidate;
			else 
				System.err.println("Partition " + partition + " index == -1, cannot adjust it.");
		}
		if (partition.getOffset() == offset && partition.getLength() == length)
			return; //Nothing to do
		int oldOffset=partition.getOffset();
		int oldLength=partition.getLength();
		partition.setOffset(offset);
		partition.setLength(length);
		delta.changed(partition);
		delta.getUndoChange().add(new AdjustPartitionChange(partition, oldOffset,oldLength));
	}
	
	
	public String toString()
	{
		return "Adjust " + partition + " to offset " + offset + " ,length " + length;
	}

}
