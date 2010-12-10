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

package com.onpositive.richtexteditor.io;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.HashMap;


import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.BasePartitionLayer;
import com.onpositive.richtext.model.HRPartition;
import com.onpositive.richtext.model.ImagePartition;
import com.onpositive.richtext.model.LinkPartition;
import com.onpositive.richtext.model.meta.IRegion;
import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtext.model.meta.Point;
import com.onpositive.richtext.model.meta.RGB;
import com.onpositive.richtexteditor.model.Logger;

/**
 * @author 32kda Basic abstract class for all classes, which serializes editor's
 *         content into text-based formats like html
 */
public abstract class TextSerializer implements ITextSerilizer{

	protected BasePartitionLayer curLayer = null;
	protected int currentLineIdx;
	// protected StyledText editor;
	protected ILineInformationProvider provider;
	protected ITextDocument doc;
	boolean optimizeParagraphs = false;
	boolean isBulletedList = false;
	protected boolean isNumberedList = false;
	protected HashMap<Integer, Integer> listsEnds;
	protected HashMap<Integer, Integer> listsIndents;
	protected static RGB black = new RGB(0, 0, 0);
	protected static RGB white = new RGB(255, 255, 255);
	protected int startLine;
	protected int endLine;
	protected boolean appendParagraphs = true;

	/**
	 * Serializes all contents of document into single string
	 * 
	 * @return Serialized string
	 */
	public String serializeAllToStr() {
		CharArrayWriter out = new CharArrayWriter();
		PrintWriter writer = new PrintWriter(out);
		serializeAll(writer);
		return out.toString();
	}

	/**
	 * Serialized selected doc fragment into String
	 * 
	 * @param selection
	 *            selection in editor
	 * @return serialized string
	 * @throws BadLocationException
	 *             (actually never does it on correct selection)
	 */
	public String serializeToStr(Point selection) throws Exception {

		defineNumberedListsEnds();
		StringBuilder str = new StringBuilder();
		int startingPosition = selection.x;
		int endingPosition = selection.y;
		startLine = doc.getLineOfOffset(startingPosition);
		endLine = doc.getLineOfOffset(endingPosition);
		IRegion lineInformation = doc.getLineInformation(startLine);
		int length = lineInformation.getOffset() + lineInformation.getLength()
				- startingPosition;
		if (startLine == endLine) {
			str.append(getSerializedPartOfLine(startLine, selection.x, selection.y
					- selection.x, appendParagraphs));
		} else {
			if (length > 0) {
				str.append(getSerializedPartOfLine(startLine, startingPosition,
						length, appendParagraphs));
			}
			for (int i = startLine + 1; i < endLine; i++) {
				
				str.append(getSerializedLine(i));
				str.append("\n");
			}
			lineInformation = doc.getLineInformation(endLine);
			length = endingPosition - lineInformation.getOffset();
			if (length > 0) {
				str.append(getSerializedPartOfLine(endLine, lineInformation
						.getOffset(), length, appendParagraphs));
			}
		}
		return getFileStartString() + str + getFileEndString();
	}

	protected void defineNumberedListsEnds() {
		final int numberOfLines = doc.getNumberOfLines();
		for (int i = 0; i < numberOfLines; i++) {
			final int bulletType = provider.getBulletType(i);
			if (bulletType != ILineInformationProvider.NONE_BULLET)
			/*if (bulletType == ILineInformationProvider.NUMBER_BULLET
					|| bulletType == ILineInformationProvider.LETTER_BULLET
					|| bulletType == ILineInformationProvider.ROMAN_BULLET
					|| bulletType == ILineInformationProvider.EXPLICIT_BULLET)*/ // (was
																				// ST.BULLET_TEXT
																				// |
																				// ST.BULLET_NUMBER)
			{
				int curBulletId = provider.getBulletIdForLine(i);
				listsIndents.put(curBulletId, provider.getLineIndent(i));
				int j = i;
				int last = j;
				while (++j < numberOfLines) {
					if (provider.getBulletIdForLine(j) == curBulletId)
						last = j;
				}
				int k = last + 1;
				for (;k < numberOfLines && provider.getLineIndent(k) > provider.getLineIndent(i) && provider.getBulletType(k) != ILineInformationProvider.NONE_BULLET; k++);
				last = k--;
				if (!listsEnds.containsKey(last))
					listsEnds.put(last, curBulletId);
			}
		}
	}

	/**
	 * Serializes a single line
	 * 
	 * @param lineNum
	 *            number of line to serialize
	 * @return seralized doc line
	 */
	public String getSerializedLine(int lineNum) {
		currentLineIdx = lineNum;
		int offset = 0, length = 0;
		try {
			length = doc.getLineLength(lineNum);
			offset = doc.getLineOffset(lineNum);
			doLinePreprocessing(offset, length);
		} catch (Exception e) {
			Logger.log(e);
		}

		if (length > 0) { // TODO was patched; Looks allright, but maybe we
							// should check this once more
			return getSerializedPartOfLine(lineNum, offset, length, appendParagraphs);
		}

		return "";
	}

	/**
	 * This method is intended to be overridden in subclasses for performing
	 * some actions needed for preprocessing of whole line
	 * 
	 * @param offset
	 *            line offset
	 * @param length
	 *            line length
	 * @throws BadLocationException
	 */
	protected void doLinePreprocessing(int offset, int length)
			throws Exception {

	}

	protected String getSerializedPartOfLine(int lineIndex, int offset,
			int length, boolean appendParagraphs) {

		boolean wasBullet = false;
		StringBuilder sb = new StringBuilder();
		sb.append(getIndentStrForLine(lineIndex));
		final int bulletType = provider.getBulletType(lineIndex);
		if (bulletType != ILineInformationProvider.NONE_BULLET) {
			if (bulletType == ILineInformationProvider.SIMPLE_BULLET) {
				sb.append(checkInnerLists(lineIndex, ILineInformationProvider.SIMPLE_BULLET));
				if (!isBulletedList) {
					isNumberedList = false;
					startBulletedList(sb);
				}
				sb.append(getBulletedListElementStartString(lineIndex));
				wasBullet = true;
			} else if (bulletType == ILineInformationProvider.NUMBER_BULLET
					|| bulletType == ILineInformationProvider.LETTER_BULLET
					|| bulletType == ILineInformationProvider.ROMAN_BULLET
					|| bulletType == ILineInformationProvider.EXPLICIT_BULLET) {
				sb.append(checkInnerLists(lineIndex, ILineInformationProvider.NUMBER_BULLET));
				if (!isNumberedList || (lineIndex > 0 && provider.getBulletIdForLine(lineIndex - 1) != provider.getBulletIdForLine(lineIndex) &&
						provider.getLineIndent(lineIndex - 1) == provider.getLineIndent(lineIndex))) {
					isBulletedList = false;
					startNumberedList(sb);
				}
				if (bulletType == ILineInformationProvider.EXPLICIT_BULLET)
					sb.append(getCustomNumberedListElementStartString(provider
							.getBulletText(lineIndex)));
				else
				{
					
					sb.append(getNumberedListElementStartString(bulletType));
				}
				wasBullet = true;
			}
		} else {			
			closeListsIfNeeded(lineIndex, sb, false);
		}
		try {
			if (doc.get(doc.getLineOffset(lineIndex), doc.getLineLength(lineIndex))
					.trim().length() == 0)
				return sb.append(getLineBreakString()).toString();

			if (appendParagraphs
					&& !(optimizeParagraphs && lineIndex > 0 && linesEqualsByParagraphStyle(
							lineIndex, lineIndex - 1))) {
				int align = provider.getLineAlignment(lineIndex);				
				sb.append(getParagraphStartString(lineIndex));
				if (align == ILineInformationProvider.LEFT_ALIGN)
					sb.append(getLeftAlignAttributeString());
				else if (align == ILineInformationProvider.RIGHT_ALIGN)
					sb.append(getRightAlignAttributeString());
				else if (align == ILineInformationProvider.CENTER_ALIGN)
					sb.append(getCenterAlignAttributeString());
				else if (align == ILineInformationProvider.JUSTIFY_ALIGN)
					sb.append(getJustifyAlignAttributeString());
				//sb.append(getParagraphStyleStr(lineIndex));
				sb.append(getTagCloseString());
			}
		} catch (Exception e) {
			Logger.log(e);
		}

		if (curLayer.getPartitionAtOffset(offset) == null)
			return sb.toString();
		serializePartitionsFromTo(offset, length, sb);

		if (optimizeParagraphs
				&& lineIndex < doc.getNumberOfLines() - 1
				&& provider.getLineAlignment(lineIndex) == provider
						.getLineAlignment(lineIndex + 1))
			sb.append(getLineBreakString());
		else if (appendParagraphs)
		{
			int idx = sb.length() - 2;
			if (sb.indexOf("\n\r") == idx || sb.indexOf("\r\n") == idx) //We don't need newline, if we are using paragraphs
				sb.delete(idx,idx + 2);
			else if (sb.charAt(idx + 1) == '\n')
				sb.deleteCharAt(idx + 1);
			sb.append(getParagraphEndString());
		}
		if (deleteExtraNewlinesBeforeListItem() && wasBullet)
			deleteExtraNewlines(sb);
		if (lineIndex == endLine)
			closeListsIfNeeded(lineIndex, sb, true);
		return sb.toString();
	}

	protected abstract String checkInnerLists(int lineIndex, int numberBullet);

	protected void startNumberedList(StringBuilder sb)
	{
		isNumberedList = true;
		sb.append(getNumberedListOpenString());
		if (insertNewlinesAfterListStart())
			sb.append("\n");
	}

	protected void startBulletedList(StringBuilder sb)
	{
		isBulletedList = true;
		sb.append(getBulletedListOpenString());
		if (insertNewlinesAfterListStart())
			sb.append("\n");
	}

	protected void closeListsIfNeeded(int lineIndex, StringBuilder sb, boolean lastLine)
	{
		if (isBulletedList) {
			if (insertNewlinesAfterListStart())
				sb.append("\n");
			closeLists(Integer.MAX_VALUE);
			sb.append(getBulletedListCloseString());
			isBulletedList = false;
		} else {
			final Integer curEnd = listsEnds.get(lineIndex - 1);
			if (isNumberedList
					&& curEnd != null
					&& (curEnd == ILineInformationProvider.NUMBER_BULLET
							|| curEnd == ILineInformationProvider.LETTER_BULLET || curEnd == ILineInformationProvider.ROMAN_BULLET)) {
				if (insertNewlinesAfterListStart())
					sb.append("\n");
				sb.append(closeLists(Integer.MAX_VALUE));
				sb.append(getNumberedListCloseString());
				isNumberedList = false;
			}
		}
	}

	/**
	 * Used for serializing some line Should be overriden in subclasses, if they
	 * have some special logics (e.g. special font style is whole line (not a
	 * particular partition) attribute
	 * 
	 * @param offset
	 *            offset of line in doc
	 * @param length
	 *            length of line in doc
	 * @param builder
	 *            String builder, which accept the changes
	 */
	protected void serializePartitionsFromTo(int offset, int length,
			StringBuilder builder) {
		BasePartition startPartition = (BasePartition) curLayer
				.getPartitionAtOffset(offset);
		BasePartition endPartition = (BasePartition) curLayer
				.getPartitionAtOffset(offset + length - 1);

		serializeSelectedPartitions(offset, length, builder, startPartition,
				endPartition);

	}

	protected void serializeSelectedPartitions(int offset, int length,
			StringBuilder builder, BasePartition startPartition,
			BasePartition endPartition) {
		if (startPartition == endPartition) {
			builder.append(applyPartitionStyleToString(startPartition,
					preprocessString(offset, startPartition.getTextRegion(
							offset, length), startPartition)));
		} else {
			builder.append(applyPartitionStyleToString(startPartition,
					preprocessString(offset, startPartition
							.getTextFromOffset(offset), startPartition)));
			for (int i = startPartition.getIndex() + 1; endPartition!=null?i < endPartition
					.getIndex():i<curLayer.size(); i++) {
				BasePartition curPartition = (BasePartition) curLayer.get(i);
				builder.append(applyPartitionStyleToString(curPartition,
						preprocessString(curPartition.getOffset(), curPartition
								.getText(), curPartition)));
			}
			if (endPartition!=null){
			builder
					.append(applyPartitionStyleToString(endPartition,
							preprocessString(endPartition.getOffset(),
									endPartition.getTextUpToOffset(offset
											+ length), endPartition)));
			}			
		}
	}

	/**
	 * Used for preprocessing some region of text, e.g. inserting/deleting some
	 * special symbols etc. Intended to be overriden in subclasses
	 * 
	 * @param offset
	 *            offset of text fragment
	 * @param textRegion
	 *            real text fragment. Offset is used for determining it's
	 *            position is inner document representation
	 * @param partition
	 *            TODO
	 * @return preprocessed text fragment
	 */
	protected String preprocessString(int offset, String textRegion,
			BasePartition partition) {
		return textRegion;
	}

	protected void deleteExtraNewlines(StringBuilder sb) {
		int lastCharIdx = sb.length() - 1;
		for (int i = lastCharIdx; i > lastCharIdx - 2 && i > 0
				&& (sb.charAt(i) == '\n' || sb.charAt(i) == '\r'); i--) {
			sb.deleteCharAt(i);
		}
	}

	protected boolean insertNewlinesAfterListStart() {
		return true;
	}

	protected boolean deleteExtraNewlinesBeforeListItem() {
		return false;
	}

	protected abstract String applyPartitionStyleToString(
			BasePartition startPartition, String textRegion);

	protected abstract String getFileStartString();

	protected abstract String getFileEndString();

	protected abstract String getLineBreakString();

	protected abstract String getParagraphStartString(int lineIdx);

	protected abstract String getParagraphEndString();

	protected abstract String getLeftAlignAttributeString();

	protected abstract String getRightAlignAttributeString();

	protected abstract String getCenterAlignAttributeString();

	protected abstract String getJustifyAlignAttributeString();

	protected abstract String getTagCloseString();

	protected abstract String getBulletedListOpenString();

	protected abstract String getBulletedListCloseString();

	protected abstract String getNumberedListOpenString();

	protected abstract String getNumberedListCloseString();
	
	protected abstract String closeLists(int depth);

	protected abstract String getBulletedListElementStartString(int lineIndex);

	protected abstract String getNumberedListElementStartString(int bulletType);

	protected abstract String getLinkStartString(LinkPartition partition);

	protected abstract String getLinkEndString();

	protected abstract String getBoldStartString();

	protected abstract String getBoldEndString();

	protected abstract String getRegionStartString();

	protected abstract String getRegionEndString();

	protected abstract String getItalicStartString();

	protected abstract String getItalicEndString();

	protected abstract String getUnderlinedStartString();

	protected abstract String getUnderlinedEndString();

	protected abstract String getStrikeStartString();

	protected abstract String getStrikeEndString();

	protected abstract String getFontColorTagOpenString();

	protected abstract String getFontColorTagCloseString();

	protected abstract String getFontStyleOpeningTag(BasePartition partition);

	protected abstract String getFontStyleClosingTag(BasePartition partition);

	protected abstract String getSubStartString();

	protected abstract String getSubEndString();

	protected abstract String getSupStartString();

	protected abstract String getSupEndString();

	protected abstract String getIndentStr();

	protected abstract String getIndentStrForLine(int lineNum);

	protected abstract String getCustomNumberedListElementStartString(
			String bulletText);
	
	protected abstract String getParagraphStyleStr(int lineIndex);

	/**
	 * Checks, that line1 and line2 have equal paragraph style
	 * 
	 * @param line1
	 *            first line to compare
	 * @param line2
	 *            second line to compare
	 * @return true if equal, false otherwise
	 */
	public boolean linesEqualsByParagraphStyle(int line1, int line2) {
		if (provider.getLineAlignment(line1) == provider
				.getLineAlignment(line2))
			return true;
		return false;
	}

	public void serializeAll(PrintWriter pw) {
		defineNumberedListsEnds();
		pw.println(getFileStartString());
		for (int i = 0; i < doc.getNumberOfLines(); i++) {
			pw.println(getSerializedLine(i));
		}
		// Check if Bullet List is open
		if (isBulletedList)
			pw.println(getBulletedListCloseString() + getIndentStr());
		if (isNumberedList)
			pw.println(getNumberedListCloseString() + getIndentStr());
		pw.println(getFileEndString());
	}

	protected abstract String getHRString(HRPartition partition);

	protected abstract String getImageStr(ImagePartition partition);

	/**
	 * @return the appendParagraphs
	 */
	public boolean isAppendParagraphs()
	{
		return appendParagraphs;
	}

	/**
	 * @param appendParagraphs the appendParagraphs to set
	 */
	public void setAppendParagraphs(boolean appendParagraphs)
	{
		this.appendParagraphs = appendParagraphs;
	}

}
