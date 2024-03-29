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

package com.onpositive.richtexteditor.model;

import java.util.List;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.meta.BasicBullet;


/**
 * @author kor
 * Abstract text model
 * Determines, that text is divided into partitions,
 * and has lines, which can have aligns and bullets
 */
public interface ISimpleRichTextModel 
{
	
	/**
	 * @return string representation of text
	 */
	String getText();
	
	/**
	 * @return partitions
	 */
	List<BasePartition> getPartitions();	
	
	/**
	 * @return text line count
	 */
	int     getLineCount();
	
	/**
	 * @param lineIndex line index
	 * @return align of that line
	 */
	int     getAlign(int lineIndex);
	
	/**
	 * @param lineIndex line index
	 * @return bullet of that line
	 */
	BasicBullet  getBullet(int lineIndex);
	
	/**
	 * @param lineIndex line index
	 * @return indent of that line (in units, not pixels)
	 */
	int getIndent(int lineIndex);
}
