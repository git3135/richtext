package com.onpositive.richtexteditor.wikitext.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.BadLocationException;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.BasePartitionLayer;
import com.onpositive.richtext.model.FontStyle;
import com.onpositive.richtext.model.IPartition;
import com.onpositive.richtext.model.PartitionDelta;
import com.onpositive.richtext.model.meta.DocumentEvent;
import com.onpositive.richtexteditor.model.LayerEvent;
import com.onpositive.richtexteditor.model.changes.CompositeChange;
import com.onpositive.richtexteditor.model.changes.DirectSetIndentChange;
import com.onpositive.richtexteditor.model.changes.ExpandPartitionAtOffsetChange;
import com.onpositive.richtexteditor.model.changes.PartitionStyleChange;
import com.onpositive.richtexteditor.model.changes.SetBulletChange;
import com.onpositive.richtexteditor.model.resources.FontStyleManager;


public class WikitextPartitionLayer extends BasePartitionLayer
{


	/* (non-Javadoc)
	 * @see com.onpositive.richtexteditor.model.BasePartitionLayer#preserveDocumentStructureIfNeeded(com.onpositive.richtexteditor.model.partitions.BasePartition, com.onpositive.richtexteditor.model.partitions.BasePartition, com.onpositive.richtexteditor.model.partitions.CompositeChange, int, int)
	 */
	protected CompositeChange preserveDocumentStructureIfNeeded(int offset,
			int length) throws BadLocationException
	{			
		if (length == 0) return null;
		CompositeChange change = new CompositeChange(this.getStorage());
		BasePartition startPartition = (BasePartition) getPartitionAtOffset(offset);
		//int startIdx = startPartition.getPosition();
		BasePartition endPartition = (BasePartition) getPartitionAtOffset(offset
				+ length);
		if (endPartition != null && endPartition.getOffset() == offset + length && endPartition != startPartition)
			endPartition = storage.get(endPartition.getIndex() - 1);
		boolean fontChangedAtTheBeginning = false, fontChangedAtTheEnd = false;
		
		String startPartitionFontDataName = startPartition.getFontDataName();
		int startPartitionPos = startPartition.getIndex();
		//final FontStyle normalFontStyle = manager.getFontStyleManager().getFontStyleByFontDataName(FontStyleManager.NORMAL_FONT_NAME);
		if (offset == startPartition.getOffset() && //If we delete from the beginning of partition
			isCarriageReturn(startPartition.getText().charAt(0)))
		{									
			if (!startPartitionFontDataName.equalsIgnoreCase(FontStyleManager.NORMAL_FONT_NAME))										
			{
				if (startPartitionPos > 0 && !get(startPartitionPos - 1).getFontDataName().equals(startPartitionFontDataName))
				{ //Then wee need to change all header's style
					makePartitionAtTheEnd(offset,length, change);
					fontChangedAtTheEnd = true;
				}
			}
			else if (startPartitionPos > 0)
			{
				final BasePartition prevPartition = getStorage().get(startPartitionPos - 1);
				final String prevFontDataName = prevPartition.getFontDataName();
				if (!prevFontDataName.equals(FontStyleManager.NORMAL_FONT_NAME))
				{
//					startPartition = prevPartition;
//					startPartitionPos--;
					startPartitionFontDataName = prevFontDataName;
				}
			}
		}
		if (endPartition != null)
		{
			int endPartitionEnd = endPartition.getOffset() + endPartition.getLength();
			String endPartitionFontDataName = endPartition.getFontDataName();
			int endPartitionPos = endPartition.getIndex();
			
			if (!startPartitionFontDataName.equals(endPartitionFontDataName))
			{
				
				int normalStyleSpreadingStartPosition = endPartitionPos; //This means, partition at which position has "wrong" style
				//if both, start and end partitions has wrong style, will also be working correctly
				if (!startPartitionFontDataName.equals(FontStyleManager.NORMAL_FONT_NAME))
				{
					makePartitionAtTheBeginning(offset, length, change);
					fontChangedAtTheBeginning = true;
				}
				if (!endPartitionFontDataName.equals(FontStyleManager.NORMAL_FONT_NAME)  && !fontChangedAtTheEnd)
					makePartitionAtTheEnd(offset, length, change);
				
			}
			else if (offset + length == endPartitionEnd && isCarriageReturn(endPartition.getText().charAt(endPartition.getText().length() - 1))) 
			{
				if (endPartitionPos < size() && !get(endPartitionPos + 1).getFontDataName().equals(endPartitionFontDataName) && !fontChangedAtTheBeginning)
					makePartitionAtTheBeginning(offset,length,change);
			}
		}
		if (change.getParts().size() == 0)
			return null;
		return change;
		
	}

	private void makePartitionAtTheBeginning(
			int offset, int length,
			CompositeChange change) throws BadLocationException
	{
			int num = doc.getLineOfOffset(offset);
			int lineOffset = doc.getLineOffset(num);
			int lineLength = doc.getLineLength(num);
			final int lineDelimiterLength = manager.getLineDelimiterLength();
			if (num < doc.getNumberOfLines()) lineLength -= lineDelimiterLength;
			if (lineOffset > 0)
			{
				lineOffset = lineOffset - manager.getLineDelimiterLength();
				lineLength += manager.getLineDelimiterLength();
			}
			final List<IPartition> extractedLinePartitions = change.extractChangedRegion(lineOffset, lineLength);
			for (Iterator iterator = extractedLinePartitions.iterator(); iterator.hasNext();)
			{
				BasePartition partition = (BasePartition) iterator.next();
				change.applyStyleToPartition(partition, manager.getFontStyleManager().getFontStyleByFontDataName(FontStyleManager.NORMAL_FONT_NAME));
			}
		
	}

	private void makePartitionAtTheEnd(int offset,
			int length, CompositeChange change)
			throws BadLocationException
	{
		int deletionEndOffset = offset + length;
		int num = doc.getLineOfOffset(deletionEndOffset);
		int lineOffset = doc.getLineOffset(num);
		int lineLength = doc.getLineLength(num);
		final int lineDelimiterLength = manager.getLineDelimiterLength();
		if (num < doc.getNumberOfLines()) lineLength -= lineDelimiterLength;
		if (lineOffset > 0)
		{
			lineOffset = lineOffset - lineDelimiterLength;
			lineLength += lineDelimiterLength;
		}
		final List<IPartition> extractedLinePartitions = change.extractChangedRegion(lineOffset, lineLength);
		for (Iterator<IPartition> iterator = extractedLinePartitions.iterator(); iterator.hasNext();)
		{
			BasePartition partition = (BasePartition) iterator.next();
			change.applyStyleToPartition(partition, manager.getFontStyleManager().getFontStyleByFontDataName(FontStyleManager.NORMAL_FONT_NAME));
		}
	}

	protected boolean isCarriageReturn(char charAt)
	{
		if (charAt == '\r' || charAt == '\n') return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see com.onpositive.richtexteditor.model.BasePartitionLayer#changeFontCommand(com.onpositive.richtexteditor.model.FontStyle, int, int)
	 */
	public void changeFontCommand(FontStyle style, int offset,
			int length)
	{
		boolean apply = true; // Apply or remove mask?
		CompositeChange change = new CompositeChange(this.storage);
		List<IPartition> newPartitions = change.extractChangedRegion(offset,
				length);
		String sumFontDataName = defineSumFontName(newPartitions);
		if (style.getFontDataName().equals(sumFontDataName))
			apply = false; // Remove, only then all partitions in list have a such font 
		for (Iterator<IPartition> iterator = newPartitions.iterator(); iterator
				.hasNext();) {
			BasePartition curPartition = (BasePartition) iterator.next();
			change.add(new PartitionStyleChange(curPartition, style, apply));
		}
		if (FontStyleManager.isHeaderFontStyle(style.getDisplayName()))
		{
			int lineIdx = manager.getTextWidget().getLineAtOffset(offset) + 1;
			change.add(new SetBulletChange(lineIdx, 1, null));
			change.add(new DirectSetIndentChange(WikitextLayerManager.HEADER_INDENT, 1, lineIdx));
		}
		
		//storage.apply(change);
		PartitionDelta apply2 = apply(change);
		Set<BasePartition> changed = apply2.getChanged();
		for (Iterator<BasePartition> iterator = changed.iterator(); iterator.hasNext();)
		{
			BasePartition changedPartition = iterator.next();
			newPartitions.add(changedPartition);
		}
		manager.fireRichDocumentEvent(null,apply2);
		handleEvent(new LayerEvent(this, new ArrayList<IPartition>(
				newPartitions)));
	}
	
	protected PartitionDelta processChange(DocumentEvent event, int offset,
			int length, int removeLength) {
		CompositeChange change = new CompositeChange(storage);
		manager.getTextWidget().setRedraw(false);
		PartitionDelta preservingApply = null;
		if (preservingChange != null) {
			preservingApply = storage.apply(preservingChange);
			preservingChange = null;
		}

		
		manager.getTextWidget().setRedraw(true);
		
		BasePartition firstPartition = deleteInterval(change, offset,
				removeLength);
		if (firstPartition == null)
			firstPartition = currentFontPartition;
		else if (firstPartition.getOffset() == offset)
		{
			if (currentFontPartition != null)
				firstPartition = currentFontPartition;
			else
			{
				int index = firstPartition.getIndex();
				firstPartition = (index > 0) ? storage.get(index - 1):null;
			}
		}
		if (doc.getLength() > 0 && length > 0) {
			change.add(new WikiExpandPartitionAtOffsetChange(offset, length,
					firstPartition));
		}
		PartitionDelta apply = processChange(offset, removeLength, change,
				event);
		debugCheck();
		if (preservingApply != null) {
			preservingApply.merge(apply);
			return preservingApply;
		}
		return apply;
	}
	
	
}
