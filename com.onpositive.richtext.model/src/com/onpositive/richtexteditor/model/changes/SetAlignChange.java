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

import com.onpositive.richtext.model.ILineAttributeModel;
import com.onpositive.richtext.model.PartitionDelta;

/**
 * @author kor
 * Change Align command
 */
public class SetAlignChange extends Change{

	private int align;
	private int line;
	private int count;
	
	/**
	 * Basic constructor
	 * @param line first line
	 * @param count line count
	 * @param align Align constant to set
	 */
	public SetAlignChange(int line, int count, int align) {
		super();
		this.align = align;
		this.count = count;
		this.line = line;
	}

	
	public void apply(PartitionDelta delta) {
		final ILineAttributeModel lineAttributeModel = delta.getStorage().getLineAttributeModel();
		final int[]aligns=new int[count+1];
		for (int a=line;a<=line+count;a++){
			aligns[a-line]=lineAttributeModel.getLineAlign(a);
		}
		lineAttributeModel.setLineAlign(line, count, align);
		Change change = new Change(){

			
			public void apply(PartitionDelta delta) {
				for (int a=0;a<aligns.length;a++){
					lineAttributeModel.setLineAlign(a+line, 0, aligns[a]);
				}
				delta.getUndoChange().add(SetAlignChange.this);
			}			
		};
		delta.getUndoChange().add(change);
	}

}
