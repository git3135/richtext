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

import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtext.model.meta.StyleRange;
import com.onpositive.richtexteditor.model.AbstractLayerManager;

/**
 * @author 32kda
 * Superclass for all objects, which isn't
 * really text partition and can contain some objects like images or hr lines.
 */
public abstract class ObjectPartition extends BasePartition
{		
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Basic constructor
	 * @param layer Layer where partition'll be situated
	 * @param offset offset of part.
	 * @param length length of part.
	 */
	public ObjectPartition(ITextDocument document, int offset, int length)
	{		
		super(document, offset, length);
		mask = 0;
	}
	
	/**
	 * @return Object
	 */
	public abstract Object getObject();
	/** 
	 * @param object Object for setting
	 */
	public abstract void setObject(Object object);

	/**
	 * Do nothing here, preserve old style
	 */	
	public void applyAttributes(BasePartition oldPartition)
	{
	}

	
	/**
	 * @return always false for this class 
	 */
	public boolean isBold()
	{
		return false;
	}
	
	
	/**
	 * @return always false for this class 
	 */	
	public boolean isItalic()
	{
		return false;
	}
	
	/**
	 * @return always false for this class 
	 */
	public boolean isUnderlined()
	{
		return false;
	}
	
	/**
	 * @return always false for this class 
	 */
	public boolean isStrikethrough()
	{
		return false;
	}
	
	/**
	 * Returns partition style-matching style range for it's displaying
	 * @param manager AbstractLayerManager responsible for partition handling
	 * @return new StyleRange
	 */
	public StyleRange getStyleRange(AbstractLayerManager manager)
	{
		StyleRange style = new StyleRange ();
		style.start = getOffset();
		style.length = 1;
		return style;
	}
	
	/** (non-Javadoc)
	 * @see com.onpositive.richtext.model.BasePartition#isExpandable()
	 */
	public boolean isExpandable()
	{
		return false;
	}
	
	/** (non-Javadoc)
	 * @see com.onpositive.richtext.model.BasePartition#isMergeable()
	 */
	public boolean isMergeable()
	{
		return false;
	}

}