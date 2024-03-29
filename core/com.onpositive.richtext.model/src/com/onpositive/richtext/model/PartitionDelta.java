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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.onpositive.richtexteditor.model.changes.Change;
import com.onpositive.richtexteditor.model.changes.CompositeChange;


/**
 * @author kor
 * Contains after-change info about change
 */
public class PartitionDelta {

	private CompositeChange undoChange;
	private boolean optimizeParitions=true;
	private boolean fireChange=true;

	public boolean isFireChange() {
		return fireChange;
	}

	public void setFireChange(boolean fireChange) {
		this.fireChange = fireChange;
	}

	private LinkedHashSet<BasePartition>changed=new LinkedHashSet<BasePartition>();
	private LinkedHashSet<BasePartition>added=new LinkedHashSet<BasePartition>();
	private LinkedHashSet<BasePartition>removed=new LinkedHashSet<BasePartition>();

	/**
	 * @return true, if we should optimize partitions
	 */
	public boolean isOptimizeParitions() {
		return optimizeParitions;
	}

	/**
	 * @param optimizeParitions  true, if we should optimize partitions
	 */
	public void setOptimizeParitions(boolean optimizeParitions) {
		this.optimizeParitions = optimizeParitions;
	}
	
	/**
	 * @param storage {@link PartitionStorage} to set
	 */
	public PartitionDelta(PartitionStorage storage){
		this.undoChange=new CompositeChange(storage);
	}
	
	/**
	 * @param partition partition to put into added list
	 */
	public void added(BasePartition partition){
		removed.remove(partition);
		changed.add(partition);
		added.add(partition);
	}
	
	/**
	 * @param partition partition to put into changed list
	 */
	public void changed(BasePartition partition){
		removed.remove(partition);
		changed.add(partition);
		added.add(partition);
	}
	
	/**
	 * @param partition partition to put into removed list
	 */	
	public void removed(BasePartition partition){
		removed.add(partition);
		changed.remove(partition);
		added.remove(partition);
	}
	
	/**
	 * @return undo change for this delta
	 */
	public CompositeChange getUndoChange(){
		return undoChange;
	}
	
	/**
	 * @return added partitions list
	 */	
	public Set<BasePartition>getAdded(){
		return new LinkedHashSet<BasePartition>(added);
	}
	
	/**
	 * Clears added part. list of Delta
	 */
	public void clearAdded()
	{
		added.clear();
	}
	
	/**
	 * Clears added part. list of Delta
	 */
	public void clearChanged()
	{
		changed.clear();
	}
	
	/**
	 * @return changed partitions list
	 */		
	public Set<BasePartition>getChanged(){
		return new LinkedHashSet<BasePartition>(changed);
	}

	/**
	 * @return removed partitions list
	 */		
	public Set<BasePartition>getRemoved(){
		return new LinkedHashSet<BasePartition>(removed);
	}
	
	/**
	 * Merge deltas
	 * @param delta {@link PartitionDelta} to merge with
	 */
	public void merge(PartitionDelta delta){
		this.added.addAll(delta.added);
		this.changed.addAll(delta.changed);
		this.removed.addAll(delta.removed);
		this.added.removeAll(delta.removed);
		this.changed.removeAll(delta.removed);
		List<Change> parts = delta.getUndoChange().getParts();
		this.undoChange.addAll(0,parts);
	}

	/**
	 * @return {@link PartitionStorage} associated with this object
	 */
	public PartitionStorage getStorage() {
		return undoChange.getStorage();
	}
}
