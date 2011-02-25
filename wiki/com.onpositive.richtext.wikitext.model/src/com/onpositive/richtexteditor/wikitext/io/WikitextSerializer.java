package com.onpositive.richtexteditor.wikitext.io;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;


import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.BasePartitionLayer;
import com.onpositive.richtext.model.HRPartition;
import com.onpositive.richtext.model.ImagePartition;
import com.onpositive.richtext.model.LinkPartition;
import com.onpositive.richtext.model.RegionPartition;
import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtext.model.meta.RGB;
import com.onpositive.richtexteditor.io.ILineInformationProvider;
import com.onpositive.richtexteditor.io.html.HTMLSerializer;
import com.onpositive.richtexteditor.model.AbstractLayerManager;
import com.onpositive.richtexteditor.model.IFontStyleManager;
import com.onpositive.richtexteditor.model.Logger;
import com.onpositive.richtexteditor.wikitext.partitions.CamelCasePartition;

public class WikitextSerializer extends HTMLSerializer {
	private static final String WIKI_PREFFIX_STR = "wiki:";
	protected WikitextTokenProvider tokenProvider;
	protected ArrayList<Integer> escapingSymbolInsertionPoints;
	// protected ArrayList<Integer[]> specialEscapingSymbolInsertionPoints;
	// //Insertion points for monospace markup, as {{{ and }}}. Assumed to be
	// {x,y} at one line
	protected BasePartition linkPrototypePartition;

	public WikitextSerializer(AbstractLayerManager manager,
			ILineInformationProvider provider) {
		super(manager, provider);
		linkPrototypePartition = manager.getLinkPrototype();
		tokenProvider = WikitextTokenProvider.getInstance();
	}

	/**
	 * Basic constructor
	 * 
	 * @param layer
	 *            {@link BasePartitionLayer} instance
	 * @param doc
	 *            {@link IDocument} instance
	 * @param iLineInformationProvider
	 *            instance of {@link ILineInformationProvider} for this
	 *            serializer
	 * 
	 */
	public WikitextSerializer(BasePartitionLayer layer, ITextDocument doc,
			ILineInformationProvider iLineInformationProvider) {
		super(layer, doc, iLineInformationProvider);
		tokenProvider = WikitextTokenProvider.getInstance();
	}

	protected String getBulletedListCloseString() {
		return "";
	}

	protected String getBulletedListOpenString() {
		return "";
	}

	protected String getFileEndString() {
		return "";
	}

	protected String getFileStartString() {
		return "";
	}

	protected String getLineBreakString() {
		return "\r\n";
	}

	protected String getBulletedListElementStartString(int lineIndex) {
		return tokenProvider
				.getKeyword(WikitextTokenProvider.WIKITEXT_BULLETED_LIST_MARKER);
	}

	/**
	 * Don't truly encode here; we just need dis to remove unuseful symbols.
	 */
	protected String encodeStr(String str) {
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() > 0
				&& (sb.charAt(0) == '\r' || sb.charAt(0) == '\n'))
			sb.deleteCharAt(0);

		return sb.toString();
	}

	protected String getNumberedListCloseString() {
		return "";
	}

	protected String getNumberedListOpenString() {
		return "";
	}

	protected String getTagCloseString() {
		return "]";
	}

	public void serializeAll(PrintWriter pw) {
		for (int i = 0; i < doc.getNumberOfLines(); i++) {
			pw.println(getSerializedLine(i));
		}
	}

	protected String getNumberedListElementStartString(int bulletType) {
		if (bulletType == ILineInformationProvider.NUMBER_BULLET)
			return tokenProvider
					.getKeyword(WikitextTokenProvider.WIKITEXT_NUMBERED_LIST_MARKER);
		else if (bulletType == ILineInformationProvider.LETTER_BULLET)
			return tokenProvider
					.getKeyword(WikitextTokenProvider.WIKITEXT_LETTER_LIST_MARKER);
		else if (bulletType == ILineInformationProvider.ROMAN_BULLET)
			return tokenProvider
					.getKeyword(WikitextTokenProvider.WIKITEXT_ROMAN_STYLE_LIST_MARKER);
		return null;
	}

	protected String getParagraphEndString() {
		return "";
	}

	protected String getParagraphStartString() {
		return "";
	}

	protected String getHRString(HRPartition partition) {
		return tokenProvider.getKeyword(WikitextTokenProvider.WIKITEXT_HR_STR);
	}

	protected String getImageStr(ImagePartition partition) {
		String imageFileName = partition.getImageFileName();
		if (helper != null) {
			imageFileName = helper.getImageLocation(partition);
		}
		return tokenProvider
				.getKeyword(WikitextTokenProvider.WIKITEXT_IMG_TAG_START)
				+ "Image("
				+ imageFileName
				+ ")"
				+ tokenProvider
						.getKeyword(WikitextTokenProvider.WIKITEXT_IMG_TAG_END);
	}

	protected String getLinkStartString(LinkPartition partition) {
		LinkPartition linkPartition = (LinkPartition) partition;
		String url = linkPartition.getUrl();
		if (helper != null) {
			url = helper.getLinkURL(linkPartition);
		} else {
			final String text = partition.getText();
			if ((url.equals(text) && !WikitextTokenProvider
					.isCamelCaseWord(text))
					|| (url.startsWith(WIKI_PREFFIX_STR) && (WIKI_PREFFIX_STR + text)
							.equals(url)) /* || url.equals(partition.getText()) */)
				return tokenProvider
						.getKeyword(WikitextTokenProvider.WIKITEXT_LINK_START);
		}
		return tokenProvider
				.getKeyword(WikitextTokenProvider.WIKITEXT_LINK_START)
				+ url + " ";
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

		} catch (Exception e) {
			Logger.log(e);
		}

		if (length > 0) {
			try {
				doLinePreprocessing(offset, length);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			String startAdditionalNewline = "";
			String additionalNewline = "";
			final String serialized = getSerializedPartOfLine(lineNum, offset,
					length, false);
			if (!tokenProvider.isSpecialString(serialized)
					&& !beforeRegion(lineNum))
				additionalNewline = "\n";
			if (needsNewlineAfterList(lineNum))
				startAdditionalNewline = "\n";
			return startAdditionalNewline + serialized + additionalNewline;// TODO
																			// changed
																			// to
																			// "false"
																			// because
																			// we
																			// don.t
																			// need
																			// to
																			// see
																			// <P>
																			// tags
																			// in
																			// wikitext
		}

		return "";
	}

	protected boolean beforeRegion(int lineNum) {
		try {
			if (lineNum < doc.getNumberOfLines() - 1
					&& curLayer.getPartitionAtOffset(doc
							.getLineOffset(lineNum + 1)) instanceof RegionPartition)
				return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	protected boolean needsNewlineAfterList(int lineIndex) {
		if (lineIndex == 0)
			return false;
		int prevLineBulletType = provider.getBulletType(lineIndex - 1);
		if (provider.getBulletType(lineIndex) == ILineInformationProvider.NONE_BULLET
				&& prevLineBulletType != ILineInformationProvider.NONE_BULLET) {
			String bulletStr;
			if (prevLineBulletType == ILineInformationProvider.SIMPLE_BULLET)
				bulletStr = getBulletedListElementStartString(prevLineBulletType);
			else
				bulletStr = getNumberedListElementStartString(prevLineBulletType);
			if (bulletStr!=null){
			if (provider.getLineIndent(lineIndex) == provider
					.getLineIndent(lineIndex - 1)
					
					+ bulletStr.length())
				return true;
			}
		}
		return false;
	}

	protected String getLinkEndString() {
		return tokenProvider
				.getKeyword(WikitextTokenProvider.WIKITEXT_LINK_END);
	}

	protected String getBoldEndString() {
		return tokenProvider
				.getKeyword(WikitextTokenProvider.WIKITEXT_BOLD_QUOTAS);
	}

	protected String getBoldStartString() {
		return tokenProvider
				.getKeyword(WikitextTokenProvider.WIKITEXT_BOLD_QUOTAS);
	}

	protected String getItalicEndString() {
		return tokenProvider
				.getKeyword(WikitextTokenProvider.WIKITEXT_ITALIC_QUOTAS);
	}

	protected String getItalicStartString() {
		return tokenProvider
				.getKeyword(WikitextTokenProvider.WIKITEXT_ITALIC_QUOTAS);
	}

	protected String getStrikeEndString() {
		return tokenProvider
				.getKeyword(WikitextTokenProvider.WIKITEXT_STRIKE_TOKEN);
	}

	protected String getStrikeStartString() {
		return tokenProvider
				.getKeyword(WikitextTokenProvider.WIKITEXT_STRIKE_TOKEN);
	}

	protected String getUnderlinedEndString() {
		return tokenProvider
				.getKeyword(WikitextTokenProvider.WIKITEXT_UNDERLINE_TOKEN);
	}

	protected String getUnderlinedStartString() {
		return tokenProvider
				.getKeyword(WikitextTokenProvider.WIKITEXT_UNDERLINE_TOKEN);
	}

	protected boolean deleteExtraNewlinesBeforeListItem() {
		return true;
	}

	protected boolean insertNewlinesAfterListStart() {
		return false;
	}

	protected void appendBoldStartString(StringBuilder sb) {
		appendWithEscapedCheck(sb, getBoldStartString());
	}

	protected void appendItalicStartString(StringBuilder sb) {
		appendWithEscapedCheck(sb, getItalicStartString());
	}

	protected boolean skipString(final String encodedStr) {
		return false;
	}

	protected void appendStrikethroughStartString(StringBuilder sb) {
		appendWithEscapedCheck(sb, getStrikeStartString());
	}

	protected void appendUnderlinedStartString(StringBuilder sb) {
		appendWithEscapedCheck(sb, getUnderlinedStartString());
	}

	protected void appendBoldEndString(StringBuilder sb) {
		appendWithEscapedCheck(sb, getBoldEndString());
	}

	protected void appendItalicEndString(StringBuilder sb) {
		appendWithEscapedCheck(sb, getItalicEndString());
	}

	protected void appendStrikethroughEndString(StringBuilder sb) {
		appendWithEscapedCheck(sb, getStrikeEndString());
	}

	protected void appendUnderlinedEndString(StringBuilder sb) {
		appendWithEscapedCheck(sb, getUnderlinedEndString());
	}

	protected void appendWithEscapedCheck(StringBuilder sb, String str) {
		if (checkIsEscaped(sb, str))
			sb.append(" ");
		sb.append(str);
	}

	/**
	 * @see com.onpositive.richtexteditor.io.html.HTMLSerializer#appendSubEndString(java.lang.StringBuilder)
	 */
	protected void appendSubEndString(StringBuilder sb) {
		appendWithEscapedCheck(sb, getSubEndString());
	}

	/**
	 * @see com.onpositive.richtexteditor.io.html.HTMLSerializer#appendSubStartString(java.lang.StringBuilder)
	 */
	protected void appendSubStartString(StringBuilder sb) {
		appendWithEscapedCheck(sb, getSubStartString());
	}

	/**
	 * @see com.onpositive.richtexteditor.io.html.HTMLSerializer#appendSupEndString(java.lang.StringBuilder)
	 */
	protected void appendSupEndString(StringBuilder sb) {
		appendWithEscapedCheck(sb, getSupEndString());
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.onpositive.richtexteditor.io.html.HTMLSerializer#appendSupStartString(java.lang.StringBuilder)
	 */
	protected void appendSupStartString(StringBuilder sb) {
		appendWithEscapedCheck(sb, getSupStartString());
	}

	protected boolean checkIsEscaped(StringBuilder sb, String appendedString) {
		if (sb.length() == 0 || appendedString.length() == 0)
			return false;
		int idx = sb.length() - 1;
		final char firstCharAt = appendedString.charAt(0);
		if (sb.charAt(idx) == firstCharAt) {
			idx--;
			while (idx > 0 && sb.charAt(idx) == firstCharAt)
				idx--;
			if (sb.charAt(idx) == '!')
				return true;
		}
		return false;
	}

	protected String encodeStrToHTML(String str) {
		/*
		 * int k = str.length() - 1; //Looks not so good while (str.charAt(k) ==
		 * '\r' || str.charAt(k) == '\n') k--; str = str.substring(0,k + 1); if
		 * (k != str.length() - 1) needToAddCarriageReturn = true;
		 */
		return str;
	}

	/**
	 * Returns a string for current font style: "=" for h1 "==" for h2 "===" for
	 * h3 "====" for h4 "=====" for h5 "======" for h6 "`" for monospace
	 */
	protected String getFontStyleClosingTag(BasePartition partition) {
		if (partition.getFontDataName().equals(
				IFontStyleManager.FONT_MONOSPACE_NAME)) {
			return tokenProvider.getKeyword(
					WikitextTokenProvider.WIKITEXT_ESCAPE_QUOTE).trim();
		} else {
			String tag = getFontStyleOpeningTag(partition);
			final Object data = partition.getData();
			if (data != null && data instanceof String) {
				String str = (String) data;
				if (!str.startsWith("#"))
					str = "#" + str;
				tag = tag + " " + str;
			}
			return tag;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onpositive.richtexteditor.io.html.HTMLSerializer#getBgColorStr(org
	 * .eclipse.swt.graphics.RGB)
	 */
	protected String getBgColorStr(RGB color) {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onpositive.richtexteditor.io.html.HTMLSerializer#getColorStr(org.
	 * eclipse.swt.graphics.RGB)
	 */
	protected String getColorStr(RGB color) {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.onpositive.richtexteditor.io.html.HTMLSerializer#
	 * getFontColorTagCloseString()
	 */
	
	protected String getFontColorTagCloseString() {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.onpositive.richtexteditor.io.html.HTMLSerializer#
	 * getFontColorTagOpenString()
	 */
	protected String getFontColorTagOpenString() {
		return "";
	}

	/**
	 * Do nothing here; Font styles is line attribute in wiki, so they are
	 * handled at another level
	 */
	protected void closeFontStyleTag(BasePartition partition, StringBuilder sb) {
		if (partition.getFontDataName().equals(
				IFontStyleManager.FONT_MONOSPACE_NAME)) // Need to do this only
														// for monospace font
		{
			final String escapeQuote = tokenProvider
					.getKeyword(WikitextTokenProvider.WIKITEXT_ESCAPE_QUOTE);
			if (partition.getText().trim().startsWith(escapeQuote))
				sb.append(tokenProvider
						.getKeyword(WikitextTokenProvider.WIKITEXT_REGION_END));
			else
				sb.append(escapeQuote);
		}
	}

	/**
	 * Do nothing here; Font styles is line attribute in wiki, so they are
	 * handled at another level
	 * 
	 * @return always false
	 */
	protected boolean openFontStyleTagIfNeeded(BasePartition partition,
			StringBuilder sb) {
		if (partition.getFontDataName().equals(
				IFontStyleManager.FONT_MONOSPACE_NAME)) // Need to do this only
														// for monospace font
		{
			final String escapeQuote = tokenProvider
					.getKeyword(WikitextTokenProvider.WIKITEXT_ESCAPE_QUOTE);
			if (partition.getText().trim().startsWith(escapeQuote))
				sb
						.append(tokenProvider
								.getKeyword(WikitextTokenProvider.WIKITEXT_REGION_START));
			else
				sb.append(escapeQuote);
			return true;
		}
		return false;
	}

	/**
	 * Returns a string for current font style: "=" for h1 "==" for h2 "===" for
	 * h3
	 */
	protected String getFontStyleOpeningTag(BasePartition partition) {
		String name = partition.getFontDataName();
		if (name.equalsIgnoreCase(IFontStyleManager.FONT_H1_NAME))
			return tokenProvider.getKeyword(
					WikitextTokenProvider.WIKITEXT_H1_STR).trim();
		else if (name.equalsIgnoreCase(IFontStyleManager.FONT_H2_NAME))
			return tokenProvider.getKeyword(
					WikitextTokenProvider.WIKITEXT_H2_STR).trim();
		else if (name.equalsIgnoreCase(IFontStyleManager.FONT_H3_NAME))
			return tokenProvider.getKeyword(
					WikitextTokenProvider.WIKITEXT_H3_STR).trim();
		else if (name.equalsIgnoreCase(IFontStyleManager.FONT_H4_NAME))
			return tokenProvider.getKeyword(
					WikitextTokenProvider.WIKITEXT_H4_STR).trim();
		else if (name.equalsIgnoreCase(IFontStyleManager.FONT_H5_NAME))
			return tokenProvider.getKeyword(
					WikitextTokenProvider.WIKITEXT_H5_STR).trim();
		else if (name.equalsIgnoreCase(IFontStyleManager.FONT_H6_NAME))
			return tokenProvider.getKeyword(
					WikitextTokenProvider.WIKITEXT_H6_STR).trim();
		else if (name.equalsIgnoreCase(IFontStyleManager.FONT_MONOSPACE_NAME))
			return tokenProvider.getKeyword(
					WikitextTokenProvider.WIKITEXT_ESCAPE_QUOTE).trim();
		return "";
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
				.getPartitionAtOffset(offset + length);
		if (endPartition==null|| endPartition.getOffset()>=offset+length){
			endPartition=(BasePartition) curLayer
			.getPartitionAtOffset(offset + length-1);
		}
		boolean wasFontStyleMarkupTag = false;

		if (!startPartition.getFontDataName().equals(
				IFontStyleManager.NORMAL_FONT_NAME)
				&& !startPartition.getFontDataName().equals(
						IFontStyleManager.FONT_MONOSPACE_NAME)) {
			builder.append(getFontStyleOpeningTag(startPartition).trim());
			builder.append(" ");
			wasFontStyleMarkupTag = true;
		}
		serializeSelectedPartitions(offset, length, builder, startPartition,
				endPartition);
		if (endPartition!=null){
		int position = endPartition.getIndex();
		int size = curLayer.getPartitions().size() - 1;
		while (position < size) {
			position++;
			BasePartition basePartition = curLayer.get(position);
			// preserving empty partitions
			if (basePartition.getLength() == 0) {
				String applyPartitionStyleToString = applyPartitionStyleToString(
						basePartition, preprocessString(offset, "",
								basePartition));
				if (applyPartitionStyleToString.length() > 0) {
					builder.append(applyPartitionStyleToString);
				}
			} else {
				break;
			}
		}
		}

		// Let's delete trailing CR's. We'll add them manually later //TODO Can
		// we delete some useful?
		deleteWhitespace(builder);

		if (wasFontStyleMarkupTag) {
			builder.append(" ");
			final String fontStyleClosingTag = getFontStyleClosingTag(startPartition);
			if (startPartition.getFontDataName().equals("Header 1")) {
				// String fontStyleClosingTag1 =
				// getFontStyleClosingTag(startPartition);
				// System.out.println(fontStyleClosingTag1);
			}
			builder.append(fontStyleClosingTag);
		}

	}

	protected void deleteWhitespace(StringBuilder builder) {
		int pos = builder.length() - 1;
		while (pos > 0
				&& (builder.charAt(pos) == '\n' || builder.charAt(pos) == '\r')) {
			builder.deleteCharAt(pos);
			pos--;
		}
	}

	protected String serializeAsTable(RegionPartition partition) {
		final String indent = getIndentStrForLine(currentLineIdx);
		String content = partition.getContent();
		content = content.replaceAll("\\r\\n", "\n");
		content = content.replaceAll("\\n\\r", "\n");
		content = content.replaceAll("\\n", "\n" + indent);
		return content;
	}

	protected String getRegionStartString() {
		return tokenProvider
				.getKeyword(WikitextTokenProvider.WIKITEXT_REGION_START);
	}

	protected String getRegionEndString() {
		return tokenProvider
				.getKeyword(WikitextTokenProvider.WIKITEXT_REGION_END);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @throws BadLocationException
	 * @see com.onpositive.richtexteditor.io.TextSerializer#doLinePreprocessing(int,
	 *      int)
	 */
	protected void doLinePreprocessing(int offset, int length)
			throws BadLocationException {
		escapingSymbolInsertionPoints = new ArrayList<Integer>();
		// specialEscapingSymbolInsertionPoints = new ArrayList<Integer[]>();
		String txt = doc.get(offset, length);
		String trim = trim(txt);
		if (trim.length() == 0)
			return;
		int firstValuableCharIdx = 0;
		while (firstValuableCharIdx<txt.length()&&Character.isWhitespace(txt.charAt(firstValuableCharIdx)))
			firstValuableCharIdx++;
		final String regionStartKeyword = tokenProvider
				.getKeyword(WikitextTokenProvider.WIKITEXT_REGION_START);
		final String regionEndKeyword = tokenProvider
				.getKeyword(WikitextTokenProvider.WIKITEXT_REGION_END);
		if (trim.equals(regionStartKeyword) || trim.equals(regionEndKeyword)) {
			int insertionPoint = txt.indexOf(regionStartKeyword.charAt(0));
			if (insertionPoint == -1)
				insertionPoint = txt.indexOf(regionStartKeyword.charAt(1));
			escapingSymbolInsertionPoints.add(insertionPoint + offset);
		} else {
			String headerMarkup = tokenProvider
					.getKeyword(WikitextTokenProvider.WIKITEXT_H4_STR);
			int insertionPoint = tryToGetBeforeHeaderMarkupInsertionPoint(txt,
					trim, headerMarkup);
			if (insertionPoint < 0)
				insertionPoint = tryToGetBeforeHeaderMarkupInsertionPoint(
						txt,
						trim,
						tokenProvider
								.getKeyword(WikitextTokenProvider.WIKITEXT_H3_STR));
			if (insertionPoint < 0)
				insertionPoint = tryToGetBeforeHeaderMarkupInsertionPoint(
						txt,
						trim,
						tokenProvider
								.getKeyword(WikitextTokenProvider.WIKITEXT_H2_STR));
			if (insertionPoint < 0)
				insertionPoint = tryToGetBeforeHeaderMarkupInsertionPoint(
						txt,
						trim,
						tokenProvider
								.getKeyword(WikitextTokenProvider.WIKITEXT_H1_STR));
			if (insertionPoint >= 0)
				escapingSymbolInsertionPoints.add(insertionPoint + offset);
			else if (trim.startsWith(tokenProvider
					.getKeyword(WikitextTokenProvider.WIKITEXT_HR_STR))
					|| trim
							.startsWith(tokenProvider
									.getKeyword(WikitextTokenProvider.WIKITEXT_BULLETED_LIST_MARKER))
					|| trim
							.startsWith(tokenProvider
									.getKeyword(WikitextTokenProvider.WIKITEXT_NUMBERED_LIST_MARKER))) {
				escapingSymbolInsertionPoints
						.add(firstValuableCharIdx + offset);
			}
			for (int i = firstValuableCharIdx; i < txt.length(); i++) {
				final char charAt = txt.charAt(i);
				if ((charAt == '_' && i < txt.length() - 1 && txt.charAt(i + 1) == '_')
						|| (charAt == '~' && i < txt.length() - 1 && txt
								.charAt(i + 1) == '~'))
					escapingSymbolInsertionPoints.add(i + offset);
				else if (charAt == '\'' && i < txt.length() - 1
						&& txt.charAt(i + 1) == '\'') {
					escapingSymbolInsertionPoints.add(i + offset);
					while (txt.charAt(i) == '\'')
						i++;
					i--;
				} else if (charAt == '[' || charAt == '{' || charAt == '`') {
					int endOffset = getMarkupEndingOffset(charAt, i, txt);
					if (endOffset > 0) {
						escapingSymbolInsertionPoints.add(i + offset);
						if (charAt == '[')
							i = endOffset;
					}
				}
				/*
				 * else if (charAt == '`') { int endOffset =
				 * getMarkupEndingOffset(charAt,i,txt); if (endOffset > 0) {
				 * specialEscapingSymbolInsertionPoints.add(new Integer[]{i +
				 * offset, endOffset + 1 + offset}); } }
				 */
			}
			/*
			 * int idx = txt.indexOf(ATTACHMENT); while (idx > -1) {
			 * escapingSymbolInsertionPoints.add(idx + offset); idx =
			 * txt.indexOf(ATTACHMENT, idx + ATTACHMENT.length() + 1); }
			 */
		}

	}

	protected String trim(String txt) {
		return txt.trim();
	}

	protected int getMarkupEndingOffset(char charAt, int offset, String text) {
		int markupEndOffset = offset + 1;
		if (charAt == '`') {
			while (markupEndOffset < text.length()
					&& text.charAt(markupEndOffset) != '`')
				markupEndOffset++;
			if (markupEndOffset < text.length())
				return markupEndOffset;
		} else if (charAt == '[') {
			if (offset < text.length() - 1 && text.charAt(offset + 1) == '[') {
				markupEndOffset = text.indexOf("]]", offset + 2);
				if (markupEndOffset > 0) {
					String substr = text.substring(offset + 2, markupEndOffset);
					Pattern p = Pattern
							.compile("[a-zA-Z][\\p{Alnum}]* ([(]{1}[^)]*[)]{1}){0,1}");
					final Matcher matcher = p.matcher(substr);
					if (matcher.matches())
						return markupEndOffset;
				}
			} else {
				markupEndOffset = text.indexOf("]", offset + 1);
				if (markupEndOffset > 0) {
					String substr = text.substring(offset + 1, markupEndOffset);
					if (substr.startsWith(WIKI_PREFFIX_STR)
							|| substr.startsWith("http:\\")
					// || substr.matches("[1-9]+/[^\\n]+") ||
					// substr.matches("[1-9]+:[1-9]+") ||
					// substr.matches("[1-9]+")
					)
						return markupEndOffset;
				}
			}

		}
		/*
		 * else if (charAt == '{') { markupEndOffset = text.indexOf("}", offset
		 * + 1); if (markupEndOffset > 0) { String substr =
		 * text.substring(offset + 1, markupEndOffset); if
		 * (substr.matches("[1-9]+")) return markupEndOffset; } }
		 */

		return -1;
	}

	protected int tryToGetBeforeHeaderMarkupInsertionPoint(String text,
			String trimmedText, String headerMarkup) {
		if (trimmedText.startsWith(headerMarkup + " ")
				&& trimmedText.endsWith(" " + headerMarkup))
			return text.indexOf(headerMarkup.charAt(0));
		return -1;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.onpositive.richtexteditor.io.TextSerializer#preprocessString(int,
	 *      java.lang.String, BasePartition)
	 */
	protected String preprocessString(int offset, String textRegion,
			BasePartition partition) {
		if (partition instanceof LinkPartition)
			return textRegion;
		if (partition.getFontDataName() == IFontStyleManager.FONT_MONOSPACE_NAME)
			return textRegion;
		StringBuilder sb = new StringBuilder(textRegion);
		int i = 0;
		int length = textRegion.length();
		int shift = 0;
		for (; i < escapingSymbolInsertionPoints.size()
				&& escapingSymbolInsertionPoints.get(i) < offset; i++)
			;
		for (; i < escapingSymbolInsertionPoints.size()
				&& escapingSymbolInsertionPoints.get(i) < offset + length; i++) {
			sb.insert(escapingSymbolInsertionPoints.get(i) - offset + shift,
					'!');
			shift++;
		}

		/*
		 * String monospaceOpenString =
		 * WikitextTokenProvider.getInstance().getKeyword
		 * (WikitextTokenProvider.WIKITEXT_REGION_START); String
		 * monospaceCloseString =
		 * WikitextTokenProvider.getInstance().getKeyword(
		 * WikitextTokenProvider.WIKITEXT_REGION_END); int j = 0; for (; j <
		 * specialEscapingSymbolInsertionPoints.size() &&
		 * specialEscapingSymbolInsertionPoints.get(j)[1] < offset; j++); for (;
		 * j < specialEscapingSymbolInsertionPoints.size() &&
		 * specialEscapingSymbolInsertionPoints.get(j)[0] < offset + length;
		 * j++) { int idx0 = specialEscapingSymbolInsertionPoints.get(j)[0]; int
		 * idx1 = specialEscapingSymbolInsertionPoints.get(j)[1]; if (idx0 >=
		 * offset) { sb.insert(specialEscapingSymbolInsertionPoints.get(j)[0] -
		 * offset + shift,monospaceOpenString); shift = shift +
		 * monospaceOpenString.length(); } if (idx1 < offset + length) {
		 * sb.insert(specialEscapingSymbolInsertionPoints.get(j)[1] - offset +
		 * shift,monospaceCloseString); shift = shift +
		 * monospaceCloseString.length(); } }
		 */

		return sb.toString();
	}

	protected String getSubEndString() {
		return tokenProvider
				.getKeyword(WikitextTokenProvider.WIKITEXT_SUBSCRIPT);
	}

	protected String getSubStartString() {
		return tokenProvider
				.getKeyword(WikitextTokenProvider.WIKITEXT_SUBSCRIPT);
	}

	protected String getSupEndString() {
		return tokenProvider
				.getKeyword(WikitextTokenProvider.WIKITEXT_SUPERSCRIPT);
	}

	protected String getSupStartString() {
		return tokenProvider
				.getKeyword(WikitextTokenProvider.WIKITEXT_SUPERSCRIPT);
	}

	protected String getIndentStrForLine(int lineNum) {
		StringBuilder sb = new StringBuilder("");
		int lineIndent = provider.getLineIndent(lineNum);
		/*
		 * if (provider.getBulletType(lineNum) !=
		 * ILineInformationProvider.NONE_BULLET) lineIndent--;
		 */
		for (int i = 0; i < lineIndent; i++)
			sb.append(getIndentStr());
		return sb.toString();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.onpositive.richtexteditor.io.html.HTMLSerializer#applyPartitionStyleToString(com.onpositive.richtext.model.BasePartition,
	 *      java.lang.String)
	 */
	public String applyPartitionStyleToString(BasePartition partition,
			String partitionText) {
		/*
		 * if (partition instanceof LinkPartition) { if
		 * (partitionText.trim().length() == 0) return partitionText;
		 * StringBuilder sb = new StringBuilder();
		 * sb.append(getLinkStartString((LinkPartition) partition)); final
		 * String encodedStr = encodeStr(partitionText); if (encodedStr.length()
		 * == 0) return ""; sb.append(encodedStr); if (partition instanceof
		 * LinkPartition) sb.append(getLinkEndString()); return sb.toString(); }
		 */
		if (partition instanceof LinkPartition) {
			if (partition instanceof CamelCasePartition) {
				if (((CamelCasePartition) partition).getStyle() == CamelCasePartition.ESCAPED)
					return "!" + partition.getText();
				return partition.getText();
			}
			final String url = ((LinkPartition) partition).getUrl();
			if (url.equals(partition.getText())
					&& WikitextTokenProvider.isCamelCaseWord(url))
				return url;
			if (url.startsWith(WIKI_PREFFIX_STR)
					&& (WIKI_PREFFIX_STR + partitionText).equals(url))
				partitionText = WIKI_PREFFIX_STR + partitionText;
			partition.setUnderlined(false); // TODO ???

			StringBuilder sb = new StringBuilder();

			sb.append(getLinkStartString((LinkPartition) partition));
			final String encodedStr = encodeStr(partitionText);
			if (skipString(encodedStr))
				return "";
			sb.append(encodedStr);
			StringBuilder carriageReturn = new StringBuilder();
			int n = sb.length() - 1;
			while (n > 0 && (sb.charAt(n) == '\n' || sb.charAt(n) == '\r')) {
				carriageReturn.append(sb.charAt(n));
				sb.deleteCharAt(n);
				n--;
			}
			carriageReturn.reverse();

			sb.append(getLinkEndString());
			return sb.toString();
		}
		return super.applyPartitionStyleToString(partition, partitionText);
	}

	protected String getIndentStr() {
		return " ";
	}

	public String serializeAllToStr() {
		defineNumberedListsEnds();
		StringBuilder str = new StringBuilder();
		str.append(getFileStartString());
		for (int i = 0; i < doc.getNumberOfLines(); i++) {
			String serializedLine = getSerializedLine(i);
			String endingNewline = "\n";
			if (trim(serializedLine).length() == 0) {
				serializedLine = "";
				if (str.length() > 2) {
					if (str.charAt(str.length() - 1) == '\n') {
						if (str.charAt(str.length() - 2) == '\n') {
							endingNewline = "";
						}
					}
				}
				// if (str.endsWith("\n\n"))
			}
			str = str.append(serializedLine);
			str = str.append(endingNewline);
		}
		str.append(getFileEndString());
		return str.toString();
	}

	protected String getCustomNumberedListElementStartString(String bulletText) {
		return " " + bulletText + ". ";
	}

	protected String getRegionString(RegionPartition partition) {

		String contentType = partition.getContentType();
		if (contentType != null
				&& (contentType.equals(RegionPartition.TABLE_CONTENT_TYPE) || contentType
						.equals(RegionPartition.DEFLIST_CONTENT_TYPE)))
			return serializeAsTable(partition);
		StringBuilder sb = new StringBuilder();
		final String indent = getIndentStrForLine(currentLineIdx);
		// sb.append(indent);
		sb.append(getRegionStartString());
		sb.append("\n");
		if (contentType != null
				&& contentType.length() > 0
				&& !partition.getContentType().equals(
						RegionPartition.PLAIN_TEXT_CONTENT_TYPE)) {
			if (!contentType
					.startsWith(WikitextTokenProvider.REGION_CONTENT_TYPE_PREFFIX)) {
				if (contentType.startsWith("!"))
					contentType = contentType.substring(1);
				contentType = WikitextTokenProvider.REGION_CONTENT_TYPE_PREFFIX
						+ contentType;
			}
			sb.append(contentType);
			sb.append("\n");
		}
		// Content is already indented sb.append(indent);
		String content = partition.getContent();
		// org.eclipse.jface.text.Document d=new
		// org.eclipse.jface.text.Document(content);
		// int numberOfLines = d.getNumberOfLines();
		// try{
		// StringBuilder bs=new StringBuilder();
		// for (int a=0;a<numberOfLines;a++){
		// IRegion lineInformation = d.getLineInformation(a);
		// String
		// text=d.get(lineInformation.getOffset(),lineInformation.getLength());
		// bs.append(indent);
		// bs.append(text);
		// bs.append('\n');
		// }
		// content=bs.toString();
		// }catch(BadLocationException e){
		//			
		// }
		if (indent.length() > 0 && !content.startsWith(" "))
			sb.append(indent);
		sb.append(content);
		if (content.length() > 0) {
			for (int a = content.length() - 1; a >= 0; a--) {
				char charAt = content.charAt(a);
				if (!Character.isWhitespace(charAt)) {
					sb.append('\n');
					break;
				} else {
					if (charAt == '\r' || charAt == '\n') {
						break;
					}
				}
			}
		}
		if (indent.length() > 0 && !content.endsWith(" "))
			sb.append(indent);
		// sb.append(indent);
		sb.append(getRegionEndString() + "\n");
		return sb.toString();
	}

	protected String closeLists(int depth) {
		for (int i = 0; i < depth && i < openedTags.size(); i++)
			openedTags.pop();
		return "";
	}

	protected void closeListsIfNeeded(int lineIndex, StringBuilder sb,
			boolean lastLine) {

	}

}
