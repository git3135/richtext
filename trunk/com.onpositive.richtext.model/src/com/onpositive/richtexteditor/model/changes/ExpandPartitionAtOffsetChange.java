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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.FontStyle;
import com.onpositive.richtext.model.IPartition;
import com.onpositive.richtext.model.PartitionDelta;
import com.onpositive.richtext.model.PartitionStorage;
import com.onpositive.richtexteditor.model.AbstractLayerManager;

/**
 * @author kor
 * Expanding partition change
 */
public class ExpandPartitionAtOffsetChange extends Change {

	private int offset;
	private int amount;
	private BasePartition current;
	
	private BasePartition psToCreate;

	/**
	 * Basic constructor
	 * @param offset Offset of part. to expand
	 * @param amount Amount to expand to
	 * @param currentFontPartition Current Font Partition
	 */
	public ExpandPartitionAtOffsetChange(int offset, int amount,
			BasePartition currentFontPartition) {
		super();
		this.amount = amount;
		this.offset = offset;
		this.current = currentFontPartition;
	}

	
	/**
	 * @see com.onpositive.richtexteditor.model.changes.Change#apply(com.onpositive.richtext.model.PartitionDelta)
	 */
	public void apply(PartitionDelta delta) {
		final PartitionStorage storage = delta.getStorage();
		PartitionStorage partitionStorage = storage;
		BasePartition partitionAtPosition = partitionStorage
				.getPartitionAtOffset(Math.max(offset - 1, 0));
		BasePartition initial = current != null ? null : partitionAtPosition;
		
		if (initial != null)
		{
			if (!initial.isExpandable()) {
				if (offset == 0) {
					initial = null;
				} else {
					if (initial.getOffset() == offset
							|| initial.getOffset() + initial.getLength() == offset) {
						expandOtherPartition(delta, initial);
						return;
					}
				}
	
			}
		}
		
		final BasePartition original = initial;
		BasePartition partitionAtOffset = original != null ? original
				: createPartition(partitionStorage);
		if (original == null) {
			// int pos = determineInsertionOffset(partitionStorage);
			IPartition pa=null;
			pa = getPartitionToInsertIn(storage, pa);
			if (pa==null) {
				int pos = determineInsertionOffset(partitionStorage);
				partitionStorage.insertPartition(pos, partitionAtOffset);
				delta.added(partitionAtOffset);
			} else {
				CompositeChange ccomposite=new CompositeChange(storage);
				BasePartition fp = (BasePartition) pa;
				ccomposite.setLength(fp, pa.getLength()+amount);
				ccomposite.shiftOffsets(fp.getIndex()+1, amount);
				ccomposite.apply(delta);
				ccomposite=new CompositeChange(storage);
				List<IPartition> extractChangedRegion = ccomposite.extractChangedRegion(
						offset, amount);
				FontStyle fs = new FontStyle(current);
				for (IPartition p : extractChangedRegion) {
					PartitionStyleChange change = new PartitionStyleChange(
							(BasePartition) p, fs, false);
					change.setSet(true);
					ccomposite.add(change);
				}
				ccomposite.apply(delta);				
				return;
			}
		}
		ArrayList <BasePartition> parts = new ArrayList<BasePartition>();
		partitionAtOffset.setLength(partitionAtOffset.getLength() + amount);

		BasePartition oldPartition = null;
		if (!canWhitespaceExpand(partitionAtOffset, partitionAtOffset.getText().substring(offset - partitionAtOffset.getOffset() , offset - partitionAtOffset.getOffset() + amount)))
		{
			String text = partitionAtOffset.getText();
			int initialOffset = partitionAtOffset.getOffset();		
			boolean isHeader = !partitionAtOffset.getFontDataName().equals(FontStyle.NORMAL_FONT_NAME);
			for (int i = text.length()-1; i >0; i--)
			{
				boolean whitespace = isWhitespace(text, i, isHeader);
				if (!whitespace){
					break;
				}
				if (whitespace && !Character.isWhitespace(text.charAt(i-1)))
				{
					BasePartition leftPartition = partitionAtOffset.extractLeftPartitionWithStyle(i + initialOffset);					
					storage.insertPartition(partitionAtOffset.getIndex(), leftPartition);
					oldPartition = partitionAtOffset;
					BasePartition partition = new BasePartition(partitionAtOffset.getDocument(), partitionAtOffset.getOffset(), partitionAtOffset.getLength());
					//partition.applyAttributes(partitionAtOffset);
					storage.insertPartition(partitionAtOffset.getIndex(), partition);
					storage.removePartition(partitionAtOffset);					
					partitionAtOffset = partition;
					parts.add(leftPartition);
					delta.changed(leftPartition);
					delta.changed(partitionAtOffset);
					break;
				}
				else if (!isWhitespace(text, i, isHeader) && Character.isWhitespace(text.charAt(i-1)))
				{
					BasePartition leftPartition = partitionAtOffset.extractLeftPartition(i + initialOffset);
					storage.insertPartition(partitionAtOffset.getIndex(), leftPartition);
					parts.add(leftPartition);
					delta.changed(leftPartition);
					break;
				}
			}
			for (int i = 0; i <text.length()-1; i++)
			{
				boolean whitespace = isWhitespace(text, i, isHeader);
				if (!whitespace){
					break;
				}
				if (whitespace && !Character.isWhitespace(text.charAt(i+1)))
				{
					partitionAtOffset.extractLeftPartition(i + initialOffset+1);		
					oldPartition = partitionAtOffset;
					final BasePartition partition = new BasePartition(partitionAtOffset.getDocument(),initialOffset,i+1);
					//partition.applyAttributes(partitionAtOffset);
					storage.insertPartition(partitionAtOffset.getIndex(), partition);										
					//partitionAtOffset = partition;					
					delta.changed(partitionAtOffset);
					//partitionAtOffset=null;
					delta.changed(partitionAtOffset);
					final int j = partitionAtOffset.getIndex() + 1;
					for (int a = j; a < partitionStorage.size(); a++) {
						BasePartition basePartition = partitionStorage.get(a);
						basePartition.setOffset(basePartition.getOffset() + amount);
					}
					Change change = new Change() {

						
						public void apply(PartitionDelta delta) {
							int j=partition.getIndex();
							storage.removePartition(partition);
							for (int a = j; a < storage.size(); a++) {
								BasePartition basePartition = storage.get(a);
								basePartition.setOffset(basePartition.getOffset() - amount);
							}
							delta.getUndoChange().add(ExpandPartitionAtOffsetChange.this);
						}
						
					};
					delta.getUndoChange().add(change);
					return;
				}				
			}
		}
		
		if (partitionAtOffset.getLength() > AbstractLayerManager.MAX_PARTITION_SIZE)
		{
			while (partitionAtOffset.getLength() > AbstractLayerManager.MAX_PARTITION_SIZE)
			{
				int divisionOffset = partitionAtOffset.getOffset() + AbstractLayerManager.MAX_PARTITION_SIZE;
				BasePartition leftPartition = partitionAtOffset.extractLeftPartitionWithStyle(divisionOffset);
				storage.insertPartition(partitionAtOffset.getIndex(), leftPartition);
				parts.add(leftPartition);
				delta.changed(leftPartition);
			}
		}

		
		delta.changed(partitionAtOffset);
		final int i = partitionAtOffset.getIndex() + 1;
		for (int a = i; a < partitionStorage.size(); a++) {
			BasePartition basePartition = partitionStorage.get(a);
			basePartition.setOffset(basePartition.getOffset() + amount);
		}
		final ArrayList<BasePartition> finalParts = parts;
		final BasePartition finalPartAtOffset = partitionAtOffset;
		final BasePartition finalOldPart = oldPartition;
		Change change = new Change() {

			public void apply(PartitionDelta delta) {
				PartitionStorage partitionStorage = delta.getStorage();
				BasePartition partAtOffset = finalPartAtOffset;
				if (finalOldPart != null)
				{
					partitionStorage.insertPartition(partAtOffset.getIndex(), finalOldPart);
					partitionStorage.removePartition(partAtOffset);
					partAtOffset = finalOldPart;
				}
				if (original == null) {
					partitionStorage.removePartition(partAtOffset);
				}
				if (partAtOffset != null) {
					if (finalParts != null && finalParts.size() > 0)
					{
						int additionalLength = partAtOffset.getLength();
						partAtOffset.setOffset(finalParts.get(0).getOffset());
						BasePartition lastPart = finalParts.get(finalParts.size() - 1);
						partAtOffset.setLength(lastPart.getOffset() + lastPart.getLength() - 
													partAtOffset.getOffset() + additionalLength);
						for (Iterator<BasePartition> iterator = finalParts.iterator(); iterator
								.hasNext();)
						{
							BasePartition basePartition = (BasePartition) iterator
									.next();
							storage.removePartition(basePartition);
						}
					}
					partAtOffset.setLength(partAtOffset.getLength()
							- amount);
				}
				int nextPartition = i;
				if (partAtOffset != null && partAtOffset.getIndex() >= 0) //This partition wasn't deleted
					nextPartition = partAtOffset.getIndex() + 1;
				else if (original == null)
					nextPartition = i - 1;
				for (int a = nextPartition; a < partitionStorage.size(); a++) {
					BasePartition basePartition = partitionStorage.get(a);
					basePartition.setOffset(basePartition.getOffset() - amount);
				}
				delta.getUndoChange().add(ExpandPartitionAtOffsetChange.this);
			}

		};
		delta.getUndoChange().add(change);
	}


	protected boolean isWhitespace(String text, int i, boolean isHeader)
	{
		return Character.isWhitespace(text.charAt(i));
	}


	protected boolean canWhitespaceExpand(BasePartition partitionAtOffset, String addedText)
	{
		return partitionAtOffset.isWhitespaceExpandable();
	}

	private IPartition getPartitionToInsertIn(PartitionStorage storage,
			IPartition pa) {
		for (IPartition p:storage.getPartitions()){
			boolean b = p.getOffset()<offset;
			if (b)
			{
				if (p.getOffset()+p.getLength()>offset){
					pa=p;
					break;
				}
			}
			else{
				break;
			}
		}
		return pa;
	}

	private int determineInsertionOffset(PartitionStorage partitionStorage) {
		int pos = partitionStorage.size();
		for (int a = 0; a < partitionStorage.size(); a++) {
			IPartition pa = partitionStorage.get(a);
			if (pa.getOffset() >= offset) {
				pos = a;
				break;
			}
		}
		return pos;
	}

	private BasePartition createPartition(PartitionStorage partitionStorage) {
		if (psToCreate!=null){
			return psToCreate;
		}
		BasePartition internalCreatePartition = internalCreatePartition(partitionStorage);
		psToCreate=internalCreatePartition;
		return internalCreatePartition;
	}

	private BasePartition internalCreatePartition(
			PartitionStorage partitionStorage) {
		if (current != null) {
			return partitionStorage.newPartition(offset, 0, current);
		}
		return partitionStorage.newPartition();
	}

	private void expandOtherPartition(PartitionDelta delta, BasePartition ps) {
		PartitionStorage partitionStorage = delta.getStorage();
		final BasePartition original = getNextUsualPartition(partitionStorage);
		final BasePartition partitionAtOffset = original != null ? partitionStorage
				.newPartition(offset, amount, original)
				: createPartition(partitionStorage);
		partitionStorage.insertPartition(ps.getIndex() + 1,
				partitionAtOffset);
		partitionAtOffset.setOffset(offset);
		partitionAtOffset.setLength(amount);
		delta.added(partitionAtOffset);

		final int i = partitionAtOffset.getIndex() + 1;
		for (int a = i; a < partitionStorage.size(); a++) {
			BasePartition basePartition = partitionStorage.get(a);
			basePartition.setOffset(basePartition.getOffset() + amount);
		}
		Change change = new Change() {

			
			public void apply(PartitionDelta delta) {
				PartitionStorage partitionStorage = delta.getStorage();
				partitionStorage.removePartition(partitionAtOffset); //We delete such partition, because we always create new one
				/*if (original == null) {
					partitionStorage.removePartition(partitionAtOffset);
				} else if (partitionAtOffset != null) {
					partitionAtOffset.setOffset(partitionAtOffset.getOffset()
							- amount);
				}*/
				for (int a = i - 1; a >= 0 && a < partitionStorage.size(); a++) { //i-1, because we deleted ith partition before
					BasePartition basePartition = partitionStorage.get(a);
					basePartition.setOffset(basePartition.getOffset() - amount);
				}
				delta.getUndoChange().add(ExpandPartitionAtOffsetChange.this);
			}

		};
		delta.getUndoChange().add(change);
	}

	private BasePartition getNextUsualPartition(
			PartitionStorage partitionStorage) {
		int pos = offset + 1;
		while (true) {
			BasePartition partitionAtOffset = partitionStorage
					.getPartitionAtOffset(pos);
			if (partitionAtOffset == null)
				return null;
			if (!partitionAtOffset.isExpandable()) {
				pos = partitionAtOffset.getOffset()
						+ partitionAtOffset.getLength();
			} else {
				return partitionAtOffset;
			}
		}
	}
	
	public String toString()
	{
		return "Expand " + current + " at " + offset + " amount: " + amount;
	}
}
