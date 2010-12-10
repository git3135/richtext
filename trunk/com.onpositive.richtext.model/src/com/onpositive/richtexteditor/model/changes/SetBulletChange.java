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
import com.onpositive.richtext.model.ILineAttributeModel;
import com.onpositive.richtext.model.PartitionDelta;
import com.onpositive.richtext.model.meta.BasicBullet;
import com.onpositive.richtexteditor.model.RichTextEditorConstants;

/**
 * @author kor
 * Change Bullet command
 */
public class SetBulletChange extends Change{

	private BasicBullet bullet;
	private int line;
	private int count;
	
	/**
	 * Basic constructor
	 * @param line first line 
	 * @param count line count
	 * @param bullet Bullet to set
	 */
	public SetBulletChange(int line, int count, BasicBullet bullet) {
		super();
		this.bullet = bullet;
		this.count = count;
		this.line = line;
	}
	
	protected int defineSumAlignStyle(ILineAttributeModel editor,int startLineNum, int endLineNum) {
		int align = editor.getLineAlign(startLineNum);		
		return align;		
	}

	
	/* (non-Javadoc)
	 * @see com.onpositive.richtexteditor.model.changes.Change#apply(com.onpositive.richtexteditor.model.partitions.PartitionDelta)
	 */
	public void apply(PartitionDelta delta) {
		int firstLineNum=line;
		int lastLineNum=line+count;
		final ILineAttributeModel lineAttributeModel = delta.getStorage().getLineAttributeModel();
		int defineSumAlignStyle = defineSumAlignStyle(lineAttributeModel,firstLineNum, lastLineNum);
		if (defineSumAlignStyle != RichTextEditorConstants.LEFT_ALIGN
				&& defineSumAlignStyle != RichTextEditorConstants.FIT_ALIGN)
		{
			new SetAlignChange(line,count-1,RichTextEditorConstants.LEFT_ALIGN).apply(delta);			
		}		
		final BasicBullet[]bullets=new BasicBullet[count];
		for (int a=line;a<line+count;a++){
			bullets[a-line]=lineAttributeModel.getBullet(a);
		}
		lineAttributeModel.setLineBullet(line, count, null);
		if (bullet != null)
		{
			for (int i = line; i < line + count; i++)
			{
				BasePartition partitionAtOffset = delta.getStorage().getPartitionAtOffset(lineAttributeModel.getOffsetAtLine(i));
				if (partitionAtOffset == null || !partitionAtOffset.requiresSingleLine())
					lineAttributeModel.setLineBullet(i, 1, bullet);
			}
		}
		delta.getUndoChange().add(new Change(){

			
			public void apply(PartitionDelta delta) {
				lineAttributeModel.setLineBullet(line, count, null);
				for (int a=0;a<bullets.length;a++){
					lineAttributeModel.setLineBullet(a+line, 1, bullets[a]);
				}
				delta.getUndoChange().add(SetBulletChange.this);
			}			
		});
	}

}
