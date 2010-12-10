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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;


import com.onpositive.richtext.model.meta.BasicBullet;
import com.onpositive.richtext.model.meta.DocumentEvent;
import com.onpositive.richtext.model.meta.ExtendedDocumentEvent;
import com.onpositive.richtext.model.meta.IDocumentListener;
import com.onpositive.richtext.model.meta.IStyledText;
import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtexteditor.model.AbstractLayerManager;
import com.onpositive.richtexteditor.model.BasePartitionEvent;
import com.onpositive.richtexteditor.model.ILink;
import com.onpositive.richtexteditor.model.IPartitionLayer;
import com.onpositive.richtexteditor.model.IPartitionListener;
import com.onpositive.richtexteditor.model.LayerEvent;
import com.onpositive.richtexteditor.model.Logger;
import com.onpositive.richtexteditor.model.changes.Change;
import com.onpositive.richtexteditor.model.changes.CompositeChange;
import com.onpositive.richtexteditor.model.changes.ExpandPartitionAtOffsetChange;
import com.onpositive.richtexteditor.model.changes.PartitionStyleChange;
import com.onpositive.richtexteditor.model.changes.SetSeveralLinesAttributesChange;
import com.onpositive.richtexteditor.model.changes.ShiftPartitionAtPositionChange;
import com.onpositive.richtexteditor.model.changes.WorldChange;


/**
 * @author 32kda Partition Layer Manages Partitions
 */
public class BasePartitionLayer extends AbstractModel implements
		IPartitionLayer, IDocumentListener {

	private static final boolean debug = false;
	protected PartitionStorage storage = new PartitionStorage(this);
	ArrayList<IPartitionListener> listeners;
	protected ITextDocument doc = null;
	protected BasePartition linkPrototypePartition;
	protected AbstractLayerManager manager;

	protected PartitionDelta delayedPartitionDelta = null;
	protected DocumentEvent delayedEvent = null;
	protected boolean fireEvents = true;
	/**
	 * Flag, indicating whether we should continue list, when adding new lines 
	 */
	protected boolean inheritPrevLineBullets = true;
	private ILineAttributeModel model = null;

	protected BasePartition currentFontPartition = null;

	/**
	 * Change text interval style
	 * 
	 * @param fontStyle
	 *            style to apply/remove
	 * @param offset
	 *            interval's offset
	 * @param length
	 *            interval's length
	 * @param apply
	 *            apply or remove this style
	 */
	public void fontStyleCommand(FontStyle fontStyle, int offset, int length,
			boolean apply) {
		
		CompositeChange ch = new CompositeChange(storage);
		List<IPartition> newPartitions = ch
				.extractChangedRegion(offset, length);
		for (Iterator<IPartition> iterator = newPartitions.iterator(); iterator
				.hasNext();) {
			BasePartition curPartition = (BasePartition) iterator.next();
			PartitionStyleChange sta = new PartitionStyleChange(curPartition,
					fontStyle, apply);
			ch.add(sta);
		}
		PartitionDelta apply2 = apply(ch);
		manager.fireRichDocumentEvent(null, apply2);
		handleEvent(new LayerEvent(this, new ArrayList<IPartition>(
				newPartitions)));
	}
	
	/**
	 * Used for processing some external-made change
	 * @param change External change
	 */
	public void executeExternalChange(Change change)
	{
		PartitionDelta apply2 = apply(change);
		manager.fireRichDocumentEvent(null, apply2);
		ArrayList<IPartition> changed = new ArrayList<IPartition>(apply2.getChanged());
		changed.addAll(apply2.getAdded());
		handleEvent(new LayerEvent(this,changed));
	}

	/**
	 * Current Font Partition is used for "delayed" applying of some partition
	 * style. This is needed in case, when user has selected some font style
	 * without current selection and expects newly typed text will have selected
	 * style Current Font Partition is a dummy partition representing selected
	 * style
	 * 
	 * @return Current Font Partition
	 */
	public BasePartition getCurrentFontPartition() {
		if (currentFontPartition == null) {
			int caretOffset = manager.getCaretOffset();
			IPartition partitionAtOffset = getPartitionAtOffset(caretOffset);
			if (partitionAtOffset == null && caretOffset > 0) {
				partitionAtOffset = getPartitionAtOffset(caretOffset - 1);
			}
			if (partitionAtOffset != null
					&& !(partitionAtOffset instanceof ObjectPartition)) { // TODO
				// instanceof
				// looks
				// not
				// so
				// good
				// here
				currentFontPartition = PartitionFactory.createAsSampleStyle(
						(BasePartition) partitionAtOffset, doc, 0, 0);
			} else {
				currentFontPartition = new BasePartition(doc, 0, 0);
			}
		}
		return currentFontPartition;
	}

	/**
	 * Current Font Partition is used for "delayed" applying of some partition
	 * style. This is needed in case, when user has selected some font style
	 * without current selection and expects newly typed text will have selected
	 * style Current Font Partition is a dummy partition representing selected
	 * style
	 * 
	 * @param currentFontPartition
	 *            Current Font Partition to set
	 */
	public void setCurrentFontPartition(BasePartition currentFontPartition) {
		this.currentFontPartition = currentFontPartition;
	}

	/**
	 * @return doc, this layer associated with
	 */
	public ITextDocument getDoc() {
		return doc;
	}

	/**
	 * Default constructor
	 */
	public BasePartitionLayer() {
		listeners = new ArrayList<IPartitionListener>();		
	}

	/**
	 * {@link LayerEvent} handling procedure
	 * 
	 * @param event
	 *            event to handle
	 */
	public void handleEvent(LayerEvent event) {
		debugCheck();
		for (Iterator<IPartitionListener> iterator = listeners.iterator(); iterator
				.hasNext();) {
			IPartitionListener listener = (IPartitionListener) iterator.next();
			listener.layerChanged(event);
		}
	}

	/**
	 * Partition event handling procedure
	 * 
	 * @param event
	 *            event to handle
	 */
	public void handlePartitionEvent(BasePartitionEvent event) {
		for (Iterator<IPartitionListener> iterator = listeners.iterator(); iterator
				.hasNext();) {
			IPartitionListener listener = (IPartitionListener) iterator.next();
			listener.partitionChanged(event);
		}

	}

	/**
	 * Debug func
	 */
	public void printPartitions() {
		if (debug) {
			Logger
					.log("//-----Partitions Print-----------------------------------------------------------");
			for (int a = 0; a < storage.size(); a++) {
				BasePartition partition = (BasePartition) storage.get(a);
				Logger.log(partition.toString());
			}
			Logger.log("//-----Partitions Print End-");
		}

	}

	/**
	 * @param listener
	 *            {@link IPartitionListener} to add
	 */
	public void addPartitionListener(IPartitionListener listener) {
		listeners.add(listener);
	}

	/**
	 * @param doc
	 *            document for connecting
	 */
	public void connectToDocument(ITextDocument doc) {
		this.doc = doc;
		if (doc != null) {
			doc.addDocumentListener(this);
		}
	}

	/**
	 * disconnect from document
	 */
	public void disconnectFromDocument() {
		if (doc != null)
			doc.removeDocumentListener(this);
		this.doc = null;
	}

	public void disposeSpecialPartitionsData() {
		List<BasePartition> partitions = storage.getPartitions();
		for (Iterator iterator = partitions.iterator(); iterator.hasNext();) {
			BasePartition basePartition = (BasePartition) iterator.next();
			if (basePartition instanceof IDisposablePartion)
				((IDisposablePartion) basePartition).dispose();
		}
	}

	/**
	 * Return partition containing symbol at specified offset
	 * 
	 * @param offset
	 *            Offset to search at
	 * @return partition
	 */
	public IPartition getPartitionAtOffset(int offset) {
		for (int a = 0; a < storage.size(); a++) {
			BasePartition partition = (BasePartition) storage.get(a);
			if (partition.getOffset() + partition.getLength() > offset)
				return partition;
		}
		return null;
	}

	/**
	 * @param listener
	 *            Listener to remove
	 */
	public void removePartitionListener(IPartitionListener listener) {
		listeners.remove(listener);
	}

	ArrayList<Runnable> hooks = new ArrayList<Runnable>();
	private boolean ignoreEvents;
	protected CompositeChange preservingChange;
	protected Change lineAttributesChange;

	/**
	 * Needed to fix bullet in some ugly situations Bullet-fixing run is
	 * executed from hooks later, when real changes ended
	 * 
	 * @param event
	 *            Event, which will happen
	 */
	public void documentAboutToBeChanged(DocumentEvent event) {
		if (ignoreEvents) {
			return;
		}
		if (event.getOffset() == 0) {
			final BasicBullet lineBullet = manager.getTextWidget().getLineBullet(0);
			if (lineBullet != null) {
				hooks.add(new Runnable() {

					public void run() {
						manager.getTextWidget().setLineBullet(0, 1, lineBullet);
					}

				});
			}
		}
		try {
			preservingChange = preserveDocumentStructureIfNeeded(event.fOffset,
					event.fLength);
			int removeLength = event.getLength();
			delayedEvent = new ExtendedDocumentEvent(event, doc.get(event.fOffset, removeLength));
			/*lineAttributesChange = createLineAttributesUndoChange(event.fOffset,
					event.fLength, event.fText.length());*/
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (fireEvents)
			manager.fireDocumentGoingToChange(event);
	}

	/**
	 * Deletes text interval
	 * 
	 * @param change
	 *            Change to adjust
	 * @param offset
	 *            Offset of text deletion
	 * @param length
	 *            Length of text deletion
	 * @return res Partition, which we'll need in some cases
	 */
	public BasePartition deleteInterval(CompositeChange change, int offset,
			int length) {
		if (length == 0) {
			return null; // Nothing to delete
		}
		BasePartition resPartition = null;

		BasePartition startPartition = (BasePartition) getPartitionAtOffset(offset);
		int startIdx = startPartition.getIndex();
		BasePartition endPartition = (BasePartition) getPartitionAtOffset(offset
				+ length);

		if (startPartition.getOffset() == offset) {
			if (length >= startPartition.getLength())
				startIdx--; // Simply put this partition to deletion list by
			// this
			else {

				change.setLength(startPartition, startPartition.getLength()
						- length);
				resPartition = startPartition;
			}

		} else {
			if (startPartition == endPartition) // In case of deletion in the
			// mid of partition
			{
				change.setLength(startPartition, startPartition.getLength()
						- length);

			} else {
				int startLength = offset - startPartition.getOffset();
				change.setLength(startPartition, startLength);
			}
		}
		if (endPartition != null) {
			int endIdx = endPartition.getIndex();
			if (endPartition != startPartition) // All done otherwise
			{
				int endOffset = offset; // Calculate new length/offset of end
				// partition
				int endLength = endPartition.getOffset()
						+ endPartition.getLength() - endOffset - length;
				change.adjustPartition(endPartition, endOffset, endLength);
			}
			int p = endIdx;
			for (int i = p - 1; i >= startIdx + 1; i--) {
				change.remove(i);
				endIdx--;

			}
			if (endIdx + 1 < storage.size() && length != 0)
				change.add(new ShiftPartitionAtPositionChange(endIdx + 1, -length));

		} else {
			for (int a = storage.size() - 1; a >= startIdx + 1; a--) {
				change.remove(a);
			}
		}
		return resPartition;
	}

	protected CompositeChange preserveDocumentStructureIfNeeded(int offset,
			int length) throws Exception {
		return null;
	}
	
	public Change createLineAttributesUndoChange(int offset,
			int length, int newLength) {
		int startLine;
		if (doc == null)
			return null;
		try {
			
		startLine = doc.getLineOfOffset(offset);
			int endLine = doc.getLineOfOffset(offset + length);
			//int newEndLine = doc.getLineOfOffset(offset + newLength);
			if (endLine > startLine)
			{
				SetSeveralLinesAttributesChange change = new SetSeveralLinesAttributesChange(startLine, endLine - startLine, offset + newLength, getLineAttributeModel(), doc);
				lineAttributesChange = change;
				return change;
			}		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * Partition consistency check
	 */
	public void checkConsistency() {

		for (int i = 1; i < storage.size(); i++) {
			BasePartition basePartition = storage.get(i);
			if (basePartition.getOffset() != storage.get(i - 1).getOffset()
					+ storage.get(i - 1).getLength())
				throw new RuntimeException("Consistency error - partition "
						+ basePartition.toString());
			if (basePartition.getIndex() != i) {
				throw new RuntimeException("Consistency error - partition "
						+ basePartition.toString());
			}
		}
		if (storage.size() > 0) {
			BasePartition basePartition = storage.get(storage.size() - 1);
			if (basePartition.getOffset() + basePartition.getLength() != doc
					.getLength()) {
				throw new RuntimeException(
						"Consistency error -missed partitions ");
			}
		}
	}

	/**
	 * Process text inserting/deletion-based doocument changes
	 * 
	 * @param event
	 *            representing this changes
	 */
	public void documentChanged(DocumentEvent event) {
		if (ignoreEvents) {
			return;
		}
		int offset = event.getOffset();
		int length = event.getText().length();
		int removeLength = event.getLength();
		try {
			for (Runnable r : hooks) {
				r.run();
			}
		} finally {
			hooks.clear();
		}
		copyLineAttributes(offset, length);
		PartitionDelta processChange = processChange(event, offset, length,
				removeLength);
		
		if (fireEvents) {
			manager.fireRichDocumentEvent(event, processChange);
			handleEvent(new LayerEvent(this, (Collection) processChange
					.getChanged()));
			delayedEvent = null; //We don't this further, if we can fire events here 
		} else {
			delayedEvent.fModificationStamp = event.fModificationStamp;
			delayedPartitionDelta = processChange;			
		}
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
			change.add(new ExpandPartitionAtOffsetChange(offset, length,
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

	/**
	 * Used to make some post-deletion actions Stub; Intended to be overriden in
	 * subclasses TODO Seems to be unuseful
	 * 
	 * @param offset
	 *            offset of change
	 * @param length
	 *            length of change
	 */
	protected void checkAfterDeletion(int offset, int length) {
	}

	/**
	 * Process document changes
	 * 
	 * @param offset
	 *            offset of changes
	 * @param removeLength
	 *            removed text length
	 * @param change
	 *            change to perform
	 * @param event
	 *            needed for customized auto style changes
	 * @return calculated {@link PartitionDelta}
	 */
	public PartitionDelta processChange(int offset, int removeLength,
			CompositeChange change, DocumentEvent event) {
		manager.setRefreshVisibleState(false);
		PartitionDelta apply = storage.apply(change);
		IRichDocumentAutoStylingStrategy[] aytoStylingStrategies = manager
				.getAutoStylingStrategies();
		CompositeChange cm = new CompositeChange(storage);
		if (fireEvents){
			for (IRichDocumentAutoStylingStrategy s : aytoStylingStrategies) {
				s.customizeStyleChanges(event, cm, apply);
			}
		}
		PartitionDelta apply2 = storage.apply(cm);
		apply.merge(apply2);
		LinkedHashSet<IPartition> changedPartitions = new LinkedHashSet<IPartition>(
				apply.getChanged());
		// adding changed to refresh styles
		if (storage.size() > 0) {
			BasePartition partitionAtOffset = storage
					.getPartitionAtOffset(offset);
			BasePartition lastDeletionPartition = storage
					.getPartitionAtOffset(offset + removeLength);
			if (lastDeletionPartition == null) {
				lastDeletionPartition = storage.getLastPartition();
			}
			if (partitionAtOffset == null) {
				partitionAtOffset = storage.getLastPartition();
			}
			for (int a = partitionAtOffset.getIndex(); a <= lastDeletionPartition
					.getIndex(); a++) {
				changedPartitions.add(storage.get(a));
			}
		}
		if (lineAttributesChange != null)
		{
			apply.getUndoChange().add(lineAttributesChange);
			lineAttributesChange = null;
		}
		manager.setRefreshVisibleState(true);
		handleEvent(new LayerEvent(this, changedPartitions));
		return apply;
	}

	/**
	 * dummy yet
	 * 
	 * @param offset
	 * @param length
	 */
	public void undoRedoHook(int offset, int length) {
		// copyLineAttributes(offset, length);
	}

	/**
	 * Process document changes
	 * 
	 * @param offset
	 *            offset of changes
	 * @param removeLength
	 *            removed text length
	 * @param changes
	 *            changes list to perform
	 * @param toUndo
	 *            undo buffer
	 */
	public void processChanges(int offset, int removeLength,
			ArrayList<CompositeChange> changes,
			ArrayList<CompositeChange> toUndo) {
		int caretPos = manager.getCaretOffset();
		ArrayList<IPartition> changedPartitions = new ArrayList<IPartition>();
		PartitionDelta merge = new PartitionDelta(storage);
		for (CompositeChange change : changes) {
			PartitionDelta apply = storage.apply(change);
			merge.merge(apply);
			changedPartitions.addAll(apply.getChanged());
			toUndo.add(apply.getUndoChange());
		}
		if (storage.size() > 0) {
			BasePartition partitionAtOffset = storage
					.getPartitionAtOffset(offset);
			BasePartition partitionAtOffset1 = storage
					.getPartitionAtOffset(offset + removeLength);
			if (partitionAtOffset1 == null) {
				partitionAtOffset1 = storage.getLastPartition();
			}
			if (partitionAtOffset == null) {
				partitionAtOffset = storage.getLastPartition();
			}
			for (int a = partitionAtOffset.getIndex(); a <= partitionAtOffset1
					.getIndex(); a++) {
				if (changedPartitions.size() > 0 && storage.get(a).getIndex() < ((BasePartition) changedPartitions.get(0)).getIndex())
					changedPartitions.add(0,storage.get(a));
				changedPartitions.add(storage.get(a));
			}
		}
		debugCheck();
		if (lineAttributesChange != null)
		{
			toUndo.get(0).add(lineAttributesChange);
			lineAttributesChange = null;
		}
		merge.getUndoChange().setAfterChangeCaretPos(caretPos);
		toUndo.get(0).setAfterChangeCaretPos(caretPos);
		manager.fireRichDocumentEvent(null, merge);
		handleEvent(new LayerEvent(this, changedPartitions));
		Change lastChange = changes.get(changes.size() - 1);
		/*if (lastChange.getAfterChangeCaretPos() > -1)
			manager.getEditor().setCaretOffset(lastChange.getAfterChangeCaretPos());*/
	}

	protected BasePartition addAsCurrentFontPartition(CompositeChange change,
			int offset, int length) {

		BasePartition partition = PartitionFactory.createAsSampleStyle(
				currentFontPartition, doc, offset, length);

		BasePartition addedPartition = insertNewPartition(change, offset,
				length, partition);
		// ---------------------------------------------------------
		debugCheck();
		return addedPartition;
	}

	protected BasePartition insertNewPartition(CompositeChange change,
			int offset, int length, BasePartition partition) {

		change.addPartitionAtOffset(partition, 0);
		return partition;
	}

	protected void debugCheck() {
		if (debug) {
			try {
				checkConsistency();
			} catch (RuntimeException e) {
				System.out
						.println("//---------------------------Îøèáêà!-------------------------------");
			}
			printPartitions();
		}
	}

	protected void copyLineAttributes(int offset, int length) {
		if (length == 0)
			return;
		int firstLine = 0;
		int lastLine = 0;
		boolean inheritFromLast = false;
		try {
			firstLine = doc.getLineOfOffset(offset);
			lastLine = doc.getLineOfOffset(offset + length);
			if (isLineDelimiter(doc.get(offset, length))
					&& offset == doc.getLineOffset(firstLine)
					&& length == doc.getLineLength(firstLine))
				inheritFromLast = true;
		} catch (Exception e) {
			Logger.log(e);
		}

		int align;
		boolean justify;

		IStyledText editor = manager.getTextWidget();
		if (inheritFromLast) {
			align = editor.getLineAlignment(lastLine);
			justify = editor.getLineJustify(lastLine);
		} else {
			align = editor.getLineAlignment(firstLine);
			justify = editor.getLineJustify(firstLine);

			int inheritAlignLineNum = firstLine; // Search for first upper not
			// empty line
			while ((inheritAlignLineNum > 0)
					&& editor.getLine(inheritAlignLineNum).equals(
							"")) {
				align = editor.getLineAlignment(inheritAlignLineNum - 1);
				justify = editor.getLineJustify(inheritAlignLineNum - 1);
				inheritAlignLineNum--;
			}
		}
		if (firstLine < lastLine && manager.getDocument().getNumberOfLines() > 1) {
			editor.setLineAlignment(firstLine + 1, lastLine - firstLine, align);
			editor.setLineJustify(firstLine + 1, lastLine - firstLine, justify);
			if (inheritPrevLineBullets)
				editor.setLineBullet(firstLine + 1, lastLine - firstLine, editor
						.getLineBullet(firstLine));
		}
	}

	protected BasePartition getNextPartititon(BasePartition partition) {
		int n = partition.getIndex();
		if (n < storage.size() - 1)
			return storage.get(n + 1);
		return null;
	}

	/**
	 * Shifts offset for all partitions situated after startIndex in list.
	 * Needed for correcting offset after character insertion/deletion
	 * 
	 * @param startIndex
	 *            first partition index
	 * @param amount
	 *            amount to shift
	 */
	protected void changeOffset(CompositeChange change, int startIndex,
			int amount) {
		change.add(new ShiftPartitionAtPositionChange(startIndex, amount));
	}

	/**
	 * Determines, is partition at some offset bold or not
	 * 
	 * @param offset
	 *            position to check
	 * @return true if it's bold, false otherwise
	 */
	public boolean isBoldAtOffset(int offset) {
		if (((BasePartition) getPartitionAtOffset(offset)).isBold())
			return true;
		return false;
	}

	/**
	 * Determines, is partition at some offset italic or not
	 * 
	 * @param offset
	 *            position to check
	 * @return true if it's italic, false otherwise
	 */
	public boolean isItalicAtOffset(int offset) {
		if (((BasePartition) getPartitionAtOffset(offset)).isItalic())
			return true;
		return false;
	}

	protected void mergeWithNext(CompositeChange change, int partitionIdx) {
		change.addMergeChange(partitionIdx);
	}

	/**
	 * Determines all bits set up in all partitions of list
	 * 
	 * @param parts
	 *            partitions list
	 * @return bitmask
	 */

	public int defineSumStyle(List<IPartition> parts) {
		int sumStyle = 0xFFFFFFFF;
		for (Iterator<IPartition> iterator = parts.iterator(); iterator
				.hasNext();) {
			BasePartition partition = (BasePartition) iterator.next();
			sumStyle = sumStyle & partition.getStyleMask();
		}
		return sumStyle;
	}

	/**
	 * Calculates common FontDataName for several partitions is not "" only if
	 * all partitions have same font
	 * 
	 * @param parts
	 *            partitions list
	 * @return common FontDataName
	 */
	public String defineSumFontName(List<IPartition> parts) {
		if (parts.isEmpty()) {
			return "";
		}
		String sumFontName = ((BasePartition) parts.get(0)).getFontDataName();
		for (int i = 1; i < parts.size(); i++) {
			BasePartition partition = (BasePartition) parts.get(i);
			if (!sumFontName.equals(partition.getFontDataName()))
				sumFontName = "";
		}
		return sumFontName;
	}

	/**
	 * returns the summary hyperlink url of several partitions If one of them
	 * isn't hyperlink, returns "" (empty string) If sme of them have different
	 * urls, also returns 0
	 * 
	 * @param offset
	 *            -|
	 * @param length
	 *            -|-> text range
	 * @return common hyperlink url
	 */
	public String getSummaryUrl(int offset, int length) {
		BasePartition begPartition = (BasePartition) getPartitionAtOffset(offset);
		BasePartition endPartition = (BasePartition) getPartitionAtOffset(offset
				+ length - 1);
		if (!(begPartition instanceof ILink)
				|| !(endPartition instanceof ILink))
			return "";
		if (begPartition == endPartition)
			return ((ILink) begPartition).getUrl();

		int begIdx = begPartition.getIndex();
		int endIdx = endPartition.getIndex();

		String curUrl = ((ILink) begPartition).getUrl();
		for (int i = begIdx + 1; i <= endIdx; i++) {
			BasePartition fp = storage.get(i);
			if (!(fp instanceof ILink))
				return "";
			if (!curUrl.equals(((ILink) fp).getUrl()))
				return "";
		}
		return curUrl;
	}

	protected boolean isLineDelimiter(String string) {
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) != '\r' && string.charAt(i) != '\n')
				return false;
		}
		return true;
	}

	/**
	 * Returns ith partition of this layer
	 * 
	 * @param i
	 *            of wanted partition
	 * @return partition
	 */
	public BasePartition get(int i) {
		return storage.get(i);
	}

	/**
	 * @return all partitions of this layer
	 */
	public List<BasePartition> getPartitions() {
		return storage.getPartitions();
	}

	/**
	 * Applies CompositeChange to layer's partitions
	 * 
	 * @param ch
	 *            CompositeChange
	 * @return PartitionDelta of change
	 */
	public PartitionDelta apply(Change ch) {
		int pos = manager.getCaretOffset();
		PartitionDelta result = storage.apply(ch);
		result.getUndoChange().setAfterChangeCaretPos(pos);
		return result;
	}

	/**
	 * Change text interval font
	 * 
	 * @param style
	 *            style to apply/remove
	 * @param offset
	 *            interval's offset
	 * @param length
	 *            interval's length
	 */
	public void changeFontCommand(FontStyle style, int offset, int length) {
		boolean apply = true; // Apply or remove mask?
		CompositeChange change = new CompositeChange(this.storage);
		List<IPartition> newPartitions = change.extractChangedRegion(offset,
				length);
		String sumFontDataName = defineSumFontName(newPartitions);
		if (style.getFontDataName().equals(sumFontDataName))
			apply = false; // Remove, only then all partitions in list have a
		// such font
		for (Iterator<IPartition> iterator = newPartitions.iterator(); iterator
				.hasNext();) {
			BasePartition curPartition = (BasePartition) iterator.next();
			change.add(new PartitionStyleChange(curPartition, style, apply));
		}
		// storage.apply(change);
		PartitionDelta apply2 = apply(change);
		manager.fireRichDocumentEvent(null, apply2);
		handleEvent(new LayerEvent(this, new ArrayList<IPartition>(
				newPartitions)));
	}
	
	public void replacePartition(int offset, int length, String name,
			BasePartition partition)
	{
		replacePartitions(offset, length, name, Collections.singletonList(partition));
	}

	/**
	 * Used to replace partitions from <b>offset</b> through <b>length</b> by
	 * <b>partitions</b> contains text <b>name</b>
	 */
	public void replacePartitions(int offset, int length, String name,
			List<BasePartition> partitions) {
		ArrayList<IPartition> changedPartitions = new ArrayList<IPartition>();
		changedPartitions.addAll(partitions);
		PartitionDelta secondDelta = storage.apply(delayedReplacePartitions(
				offset, length, name, partitions));
		debugCheck();
		if (delayedPartitionDelta != null) {
			delayedPartitionDelta.merge(secondDelta);
			secondDelta = delayedPartitionDelta;
			// secondDelta.merge(delayedPartitionDelta);
			// delayedPartitionDelta = null;
			delayedEvent.fModificationStamp--; //We are "emulating", that document changing hasn't started yet
			manager.fireDocumentGoingToChange(delayedEvent);
			delayedEvent.fModificationStamp++;
			manager.fireRichDocumentEvent(delayedEvent, secondDelta);
			delayedEvent = null;
		}

		handleEvent(new LayerEvent(this, changedPartitions));
		// Logger.log(storage.getPartitions().toString());

		fireEvents = true;
	}
	
	public CompositeChange delayedReplacePartition(int offset, int length,
			String name, BasePartition partition)
	{
		return delayedReplacePartitions(offset,length,name,Collections.singletonList(partition));
	}

	public CompositeChange delayedReplacePartitions(int offset, int length,
			String text, List<BasePartition> partitions) {
		fireEvents = false;
		manager.getDocument().getNumberOfLines();
		try {
			doc.replace(offset, length, text);
		} catch (Exception e) {
			e.printStackTrace();
		}

		fireEvents = true;
		CompositeChange change = new CompositeChange(this.storage);
		//change.removePartitionsFromTo(offset, Math.max(text.length(), length));
		change.removePartitionsFromTo(offset, text.length()); //TODO was a bug here; logics can be incorrect
		for (Iterator<BasePartition> iterator = partitions.iterator(); iterator.hasNext();) {
			BasePartition partition = (BasePartition) iterator.next();
		//	change.addPartitionAtOffset(partition, text.length());
			change.addPartitionAtOffset(partition, offset);
			offset += partition.getLength();
		}
		
		return change;
	}

	/**
	 * @param AbstractLayerManager
	 *            {@link AbstractLayerManager} instance to set
	 */
	public void setManager(AbstractLayerManager AbstractLayerManager) {
		this.manager = AbstractLayerManager;
	}

	/**
	 * @return PartitionStorage of layer
	 */
	public PartitionStorage getStorage() {
		return storage;
	}

	public ILineAttributeModel getLineAttributeModel() {
		if (model == null) {
			model = manager.createModel();
		}
		return model;
	}

	/**
	 * Executes align change
	 * 
	 * @param setContentChange
	 *            Align Set Change to apply
	 */
	public void execute(Change setContentChange) {
		int beforeCaretPos = -1;
		if (!(setContentChange instanceof WorldChange))
			beforeCaretPos = manager.getCaretOffset();
		PartitionDelta processChange = storage.apply(setContentChange);
		processChange.getUndoChange().setAfterChangeCaretPos(beforeCaretPos);
		if (fireEvents) {
			manager.fireRichDocumentEvent(null, processChange);
		}
		if (processChange.isFireChange()){
		LayerEvent event = new LayerEvent(this, new LinkedHashSet<IPartition>(
				processChange.getChanged()));
		handleEvent(event);
		}
	}

	/**
	 * @return the fireEvents
	 */
	public boolean isFireEvents() {
		return fireEvents;
	}

	/**
	 * @param fireEvents
	 *            the fireEvents to set
	 */
	public void setFireEvents(boolean fireEvents) {
		this.fireEvents = fireEvents;
	}

	/**
	 * @param b
	 *            true, if it's no need to throw doc change events through some
	 *            time
	 */
	public void setIgnoreDocumentEvents(boolean b) {
		this.ignoreEvents = b;
	}

	/**
	 * Used to get layer's partitions count
	 * 
	 * @return layer's partitions count
	 */
	public int size() {
		return storage.size();
	}

	
	public String toString() {
		return storage.toString();
	}

	/**
	 * @return the manager
	 */
	public AbstractLayerManager getManager() {
		return manager;
	}

	/**
	 * @return the inheritPrevLineBullets
	 */
	public boolean isInheritPrevLineBullets()
	{
		return inheritPrevLineBullets;
	}

	/**
	 * @param inheritPrevLineBullets the inheritPrevLineBullets to set
	 */
	public void setInheritPrevLineBullets(boolean inheritPrevLineBullets)
	{
		this.inheritPrevLineBullets = inheritPrevLineBullets;
	}

}
