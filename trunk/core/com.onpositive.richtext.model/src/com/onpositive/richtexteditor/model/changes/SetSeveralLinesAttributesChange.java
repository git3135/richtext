package com.onpositive.richtexteditor.model.changes;

import java.util.ArrayList;


import com.onpositive.richtext.model.ILineAttributeModel;
import com.onpositive.richtext.model.PartitionDelta;
import com.onpositive.richtext.model.meta.BasicBullet;
import com.onpositive.richtext.model.meta.ITextDocument;


/**
 * Used for saving line attributes for undoing/redoing
 * @author [powt]32kda
 * (c) OnPositive, made in USSR
 */
public class SetSeveralLinesAttributesChange extends Change {

	protected int line;
	protected int newLinesCount;
	protected int insertionEndOffset;
	protected int removeLinesCount;
	protected ITextDocument document;
	protected ILineAttributeModel model;
	
	protected ArrayList<Integer> indents, firstLineIndents, rightIndents, aligns, spacings;
	protected ArrayList<BasicBullet> bullets;
	protected ArrayList<Integer> newIndents, newFirstLineIndents, newRightIndents, newAligns, newSpacings;
	protected ArrayList<BasicBullet> newBullets;
	
	/**
	 * It's intended, that this constructor will be called and change created BEFORE line deletion (in method like documentAboutToBeChanged)
	 * here we store old line attributes
	 * @param line first change line
	 * @param removeLinesCount Lines count to be removed
	 * @param insertionEndOffset It's insertion offset + new chars count.. When calling this constructor, we actually don't know anything about future line count/attributes,
	 * so we need to know, where insertion ends
	 * @param model {@link ILineAttributeModel} providing line attributes 
	 *	 */
	public SetSeveralLinesAttributesChange(int line, int removeLinesCount, int insertionEndOffset, ILineAttributeModel model, ITextDocument document)
	{
		this.line = line;
		this.removeLinesCount = removeLinesCount;
		this.insertionEndOffset = insertionEndOffset;
		this.model = model;
		this.document = document;
		saveOldValues();
	}
	
	protected void saveOldValues()
	{
		indents = new ArrayList<Integer>(removeLinesCount);
		firstLineIndents = new ArrayList<Integer>(removeLinesCount);
		rightIndents = new ArrayList<Integer>(removeLinesCount);
		aligns = new ArrayList<Integer>(removeLinesCount);
		bullets = new ArrayList<BasicBullet>(removeLinesCount);
		spacings = new ArrayList<Integer>(removeLinesCount);
		
		for (int i = line; i <= line + removeLinesCount; i++)
		{
			
			firstLineIndents.add(model.getFirstLineIndent(i));
			rightIndents.add(model.getRightIndent(i));
			indents.add(model.getLineIndent(i));
			bullets.add(model.getBullet(i));
			aligns.add(model.getLineAlign(i));
			spacings.add(model.getLineSpacing(i));
		}
		
	}
	
	public void apply(PartitionDelta delta) {		
		/*if (newIndents == null)
		{
			try
			{
				int inserionEndLine = document.getLineOfOffset(insertionEndOffset);
				newLinesCount = inserionEndLine - line;
			} catch (BadLocationException e)
			{
				e.printStackTrace();
			}
			newIndents = new ArrayList<Integer>(newLinesCount); //+1 is needed, because we need to remember first bullet in some cases
			newFirstLineIndents = new ArrayList<Integer>(newLinesCount);
			newRightIndents = new ArrayList<Integer>(newLinesCount);
			newAligns = new ArrayList<Integer>(newLinesCount);
			newBullets = new ArrayList<Object>(newLinesCount);
			newSpacings = new ArrayList<Integer>(newLinesCount);
			for (int i = line; i <= line + newLinesCount; i++)
			{
				newFirstLineIndents.add(model.getFirstLineIndent(i));
				newRightIndents.add(model.getRightIndent(i));
				newIndents.add(model.getLineIndent(i));
				newBullets.add(model.getBullet(i));
				newAligns.add(model.getLineAlign(i));
				newSpacings.add(model.getLineSpacing(i));
			}
		}*/
/*		else
		{
			for (int i = line + 1; i <= line + newLinesCount; i++)
			{
				model.setFirstLineIndent(i,1,newFirstLineIndents.get(i - line));
				model.setLineAlign(i,1,newAligns.get(i - line));
				model.setLineBullet(i,1,newBullets.get(i - line));
				model.setLineIndent(i,1,newIndents.get(i - line));
				model.setRightIndent(i,1,newRightIndents.get(i - line));
				model.setLineSpacing(i,1,newSpacings.get(i - line));
			}
		}*/
		
		for (int i = line; i <= line + removeLinesCount; i++)
		{
			model.setFirstLineIndent(i,1,firstLineIndents.get(i - line));
			model.setLineAlign(i,1,aligns.get(i - line));
			model.setLineBullet(i,1,bullets.get(i - line));
			model.setLineIndent(i,1,indents.get(i - line));
			model.setRightIndent(i,1,rightIndents.get(i - line));
			model.setLineSpacing(i,1,spacings.get(i - line));
		}
		
		/*delta.getUndoChange().add(new Change(){
			
			public void apply(PartitionDelta delta) {
				
				for (int i = line; i <= line + removeLinesCount; i++)
				{
					Integer indent = firstLineIndents.get(i - line);
					model.setFirstLineIndent(i,1,indent - 1);
					model.setLineAlign(i,1,aligns.get(i - line));
					model.setLineBullet(i,1,bullets.get(i - line));
					model.setLineIndent(i,1,indents.get(i - line));
					model.setRightIndent(i,1,rightIndents.get(i - line));
					model.setLineSpacing(i,1,spacings.get(i - line));
				}
			
				//delta.getUndoChange().add(SetSeveralLinesAttributesChange.this); TODO we should always add this when changing several lines, not only here
			}			
		});*/
		
	}

}
