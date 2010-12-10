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

import com.onpositive.richtext.model.meta.BasicBullet;

/**
 * @author kor
 * Encapsulates model, that contain line attributes like aligns and bullets. 
 */
public interface ILineAttributeModel 
{

	/**
	 * Sets selected interval align
	 * @param startLine first line
	 * @param count line count
	 * @param align Align constant to set
	 */
	public void    setLineAlign(int startLine,int count,int align);
	/**
	 * Gets line align
	 * @param line line index
	 * @return align of that line
	 */
	public int     getLineAlign(int line);
	/**
	 * Gets line bullet
	 * @param line line index
	 * @return line bullet
	 */
	public BasicBullet  getBullet(int line);
	/**
	 * Sets selected interval bullet
	 * @param startLine first line
	 * @param count line count
	 * @param bullet Bullet to set
	 */
	public void    setLineBullet(int startLine,int count,BasicBullet bullet);	
	/**
	 * Sets selected interval bullet without checking, whether list needs to be expanded etc.
	 * @param startLine first line
	 * @param count line count
	 * @param bullet Bullet to set
	 */
	public void    setLineBulletWithoutCheck(int startLine,int count,BasicBullet bullet);
	
	/**
	 * @return line count
	 */
	public int     lineCount();
	
	/**
	 * Sets selected interval indent
	 * @param startLine first line
	 * @param count line count
	 * @param indent in units to set
	 */
	public void setLineIndent(int startLine,int count,int indent);
	
	/**
	 * Sets selected interval first line indent
	 * Logical lines can be wrapped into several parts (visible lines), behaving like paragraph in 
	 * MS Word/OOO Writer. This indent is an indent of first visible line (line in paragraph)
	 * @param startLine first line to set indent to
	 * @param count line count
	 * @param indent in units to set
	 */
	public void setFirstLineIndent(int startLine,int count,int indent);
	
	/**
	 * Sets selected interval line spacing
	 * Line spacing is vertical interval between lines
	 * @param startLine first line to set indent to
	 * @param count line count
	 * @param spacing in units to set
	 */
	public void setLineSpacing(int startLine,int count,int spacing);
	
	/**
	 * Used for setting indent directly, without checking that it suits some range etc
	 * Intended to be used for setting content change, when we can't be sure, that all
	 * line's indent values is set etc.
	 * @param startLine first line to set indent to
	 * @param count line count
	 * @param indent indent to set
	 */
	public void setLineIndentWithoutCheck(int startLine, int count, int indent);
	
	/**
	 * Sets selected interval indent, <b>but without multiplying it by font size</b>
	 * @param startLine first line
	 * @param count line count
	 * @param indent in units to set
	 */
	public void    setLineIndentDirectly(int startLine,int count,int indent);
	
	/**
	 * Gets line indent
	 * @param line line index
	 * @return indent in units of that line
	 */
	public int getLineIndent(int line);
	
	/**
	 * Gets line spacing
	 * @param line line index
	 * @return spacing in units of that line
	 */
	public int getLineSpacing(int line);
	
	/**
	 * Gets first line indent
	 * Logical lines can be wrapped into several parts (visible lines), behaving like paragraph in 
	 * MS Word/OOO Writer. This indent is an indent of first visible line (line in paragraph)
	 * @param line line index
	 * @return first visible line indent in units of that line
	 */
	public int getFirstLineIndent(int line);
	
	
	/**
	 * Gets right line indent
	 * @param line line index
	 * @return Left margin size
	 */
	public int getRightIndent(int line);
	
	/**
	 * Sets selected interval right indent, <b>but without multiplying it by font size</b>
	 * @param startLine first line
	 * @param count line count
	 * @param indent in units to set
	 */
	public void setRightIndent(int startLine, int count, int indent);
	
	/**
	 * Gets line indent, <b>but without dividing it by font size</b>
	 * @param line line index
	 * @return indent in units of that line
	 */
	public int getLineIndentDirectly(int line);
	
	/**
	 * Returns global indent size. It's multiply factor for getting indent in pixels for some indent level
	 * indentInPixels = indentSize * indentLevel;	 
	 * @return global indent size
	 */
	public int getIndentSize();
	
	
	/**
	 * Returns offset at specified line
	 * @param lineIndex line index
	 * @return offset at specified line
	 */
	public int getOffsetAtLine(int lineIndex);
	
}
