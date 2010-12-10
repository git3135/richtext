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

import com.onpositive.richtext.model.PartitionDelta;

/**
 * @author kor
 * Superclass for all change classes
 */
public abstract class Change {
	
	protected int afterChangeCaretPos = -1;

	/**
	 * Apply change for partition storage of given {@link PartitionDelta} 
	 * @param delta {@link PartitionDelta} to apply change to
	 */
	public abstract void apply(PartitionDelta delta);

	/**
	 * @return the afterChangeCaretPos
	 */
	public int getAfterChangeCaretPos()
	{
		return afterChangeCaretPos;
	}

	/**
	 * @param afterChangeCaretPos the afterChangeCaretPos to set
	 */
	public void setAfterChangeCaretPos(int afterChangeCaretPos)
	{
		this.afterChangeCaretPos = afterChangeCaretPos;
	}
		
}
