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

package com.onpositive.richtexteditor.io.html;

import java.util.HashMap;
import java.util.Stack;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.BasePartitionLayer;
import com.onpositive.richtext.model.FontStyle;
import com.onpositive.richtext.model.HRPartition;
import com.onpositive.richtext.model.ImagePartition;
import com.onpositive.richtext.model.LinkPartition;
import com.onpositive.richtext.model.RegionPartition;
import com.onpositive.richtext.model.meta.Font;
import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtext.model.meta.RGB;
import com.onpositive.richtexteditor.io.ILineInformationProvider;
import com.onpositive.richtexteditor.io.TextSerializer;
import com.onpositive.richtexteditor.model.AbstractLayerManager;
import com.onpositive.richtexteditor.model.RichTextEditorConstants;

/**
 * @author 32kda {@link TextSerializer} impl for serializing into HTML format
 */
public class HTMLSerializer extends TextSerializer {
	
	private static final String HTML_INDENT_MEASUREMENT = "em;";
	protected static final String BOLD_CLOSE_TAG = "</b>";
	protected static final String ITALIC_CLOSE_TAG = "</i>";
	protected static final String UNDERLINE_CLOSE_TAG = "</u>";
	protected static final String STRIKE_CLOSE_TAG = "</STRIKE>";
	protected static final String STRIKE_OPEN_TAG = "<STRIKE>";
	protected static final String UNDERLINE_OPEN_TAG = "<u>";
	protected static final String ITALIC_OPEN_TAG = "<i>";
	protected static final String BOLD_OPEN_TAG = "<b>";
	
	protected static final String REGION_TAG_START_STRING = "<div style=\"background: #fff; border: 1px solid #36c;\" class=\"" + HTMLTokenProvider.RICH_TEXT_HTML_CLASS_NAME + "\"";
	protected static final String REGION_CLOSE_TAG_STRING = "</div>";
	
	protected Stack<Integer> openedTags = new Stack<Integer>();
		
	protected ISerializationHelper helper;
	
	/**
	 * @return returns a helper
	 */
	public ISerializationHelper getHelper() {
		return helper;
	}

	/**
	 * @param helper
	 *            ISerializationHelper to set
	 */
	public void setHelper(ISerializationHelper helper) {
		this.helper = helper;
	}

	/**
	 * Basic constructor
	 * 
	 * @param manager
	 *            {@link AbstractLayerManager} instance
	 */
	public HTMLSerializer(AbstractLayerManager manager,
			ILineInformationProvider iLineInformationProvider) {
		listsEnds = new HashMap<Integer, Integer>();
		listsIndents = new HashMap<Integer, Integer>();
		curLayer = manager.getLayer();
		provider = iLineInformationProvider;
		doc = manager.getDocument();
	}

	/**
	 * Basic constructor
	 * 
	 * @param layer
	 *            {@link BasePartitionLayer} instance
	 * @param fontRegistry
	 *            {@link FontRegistry} instance
	 * @param doc
	 *            {@link IDocument} instance
	 * @param iLineInformationProvider
	 *            instance of {@link ILineInformationProvider} for this
	 *            serializer
	 * 
	 */
	public HTMLSerializer(BasePartitionLayer layer, ITextDocument doc, ILineInformationProvider iLineInformationProvider) {
		listsEnds = new HashMap<Integer, Integer>();
		listsIndents = new HashMap<Integer, Integer>();
		curLayer = layer;
		provider = iLineInformationProvider;
		this.doc = doc;
	}

	/**
	 * Return an html string representing some partition wiith all needed style
	 * tags etc.
	 * 
	 * @param partition
	 *            partition to serialize
	 * @param partitionText
	 *            text of such partition. Can differ from partition text, when
	 *            we want to get the style for the part of the partition
	 * @return html string for partition with styles
	 */
	public String applyPartitionStyleToString(BasePartition partition,
			String partitionText) {
		if (partition instanceof ImagePartition)
			return getImageStr((ImagePartition) partition);
		if (partition instanceof HRPartition
				&& partitionText.trim().length() > 0)
			return getHRString((HRPartition) partition);
		if (partition instanceof RegionPartition
				&& partitionText.trim().length() > 0)
			return getRegionString((RegionPartition) partition);

		StringBuilder sb = new StringBuilder();

		openFontFlagTagIfNeeded(partition, sb);

		boolean fontTag = false, spanTag = false;
		spanTag = openFontStyleTagIfNeeded(partition, sb);
		if (partition.getColorRGB() != null
				&& !partition.getColorRGB().equals(black)) {
			if (!fontTag) {
				sb.append(getFontColorTagOpenString());
				fontTag = true;
			}
			sb.append(getColorStr(partition.getColorRGB()));
		}
		if (partition.getBgColorRGB() != null
				&& !partition.getBgColorRGB().equals(white)) {
			if (!fontTag) {
				sb.append(getFontColorTagOpenString());
				fontTag = true;
			}
			sb.append(getBgColorStr(partition.getBgColorRGB()));
		}
		if (fontTag) {
			sb.append(getTagCloseString());
		}

		if (partition instanceof LinkPartition) {
			sb.append(getLinkStartString((LinkPartition) partition));
		}
		if (helper != null) {
			String prefix = helper.getAdditionPartitionPrefix(partition);
			if (prefix != null) {
				sb.append(prefix);
			}
		}
		final String encodedStr = encodeStr(partitionText);
		if (skipString(encodedStr))
			return "";
		sb.append(encodedStr);
		if (helper != null) {
			String prefix = helper.getAdditionPartitionSuffix(partition);
			if (prefix != null) {
				sb.append(prefix);
			}
		}

		StringBuilder carriageReturn = new StringBuilder();
		if (!appendParagraphs)
		{
			
			int n = sb.length() - 1;
			while (n > 0 && (sb.charAt(n) == '\n' || sb.charAt(n) == '\r')) {
				carriageReturn.append(sb.charAt(n));
				sb.deleteCharAt(n);
				n--;
			}
			carriageReturn.reverse();
		}

		if (partition instanceof LinkPartition)
			sb.append(getLinkEndString());

		if (fontTag)
			sb.append(getFontColorTagCloseString());
		if (spanTag)
			closeFontStyleTag(partition, sb);
		closeFontFlagTagIfNeeded(partition, sb);
		if (carriageReturn.length() > 0)
			sb.append(carriageReturn);

		return sb.toString();
	}

	protected boolean skipString(final String encodedStr) {
		return encodedStr.length() == 0;
	}

	protected String openFontFlagTagIfNeeded(BasePartition partition,
			StringBuilder sb) {
		final int position = partition.getIndex();
		if (position > 0) {
			BasePartition prevPartition = curLayer.get(position - 1);
			if (!prevPartition.isBold() && partition.isBold())
				appendBoldStartString(sb);
			if (!prevPartition.isItalic() && partition.isItalic())
				appendItalicStartString(sb);
			if (!prevPartition.isUnderlined() && partition.isUnderlined())
				appendUnderlinedStartString(sb);
			if (!prevPartition.isStrikethrough() && partition.isStrikethrough())
				appendStrikethroughStartString(sb);
			if (!prevPartition.isSub() && partition.isSub())
				appendSubStartString(sb);
			if (!prevPartition.isSup() && partition.isSup())
				appendSupStartString(sb);
			return sb.toString();
		}
		if (partition.isBold())
			appendBoldStartString(sb);
		if (partition.isItalic())
			appendItalicStartString(sb);
		if (partition.isUnderlined())
			appendUnderlinedStartString(sb);
		if (partition.isStrikethrough())
			appendStrikethroughStartString(sb);
		if (partition.isSub())
			appendSubStartString(sb);
		if (partition.isSup())
			appendSupStartString(sb);
		return sb.toString();
	}

	protected String closeFontFlagTagIfNeeded(BasePartition partition,
			StringBuilder sb) {
		final int position = partition.getIndex();
		if (position < curLayer.size() - 1) {
			BasePartition nextPartition = curLayer.get(position + 1);
			if (partition.isSup() && !nextPartition.isSup())
				appendSupEndString(sb);
			if (partition.isSub() && !nextPartition.isSub())
				appendSubEndString(sb);
			if (partition.isStrikethrough() && !nextPartition.isStrikethrough())
				appendStrikethroughEndString(sb);
			if (partition.isUnderlined() && !nextPartition.isUnderlined())
				appendUnderlinedEndString(sb);
			if (partition.isItalic() && !nextPartition.isItalic())
				appendItalicEndString(sb);
			if (partition.isBold() && !nextPartition.isBold())
				appendBoldEndString(sb);
			return sb.toString();
		}
		if (partition.isSup())
			appendSupEndString(sb);
		if (partition.isSub())
			appendSubEndString(sb);
		if (partition.isStrikethrough())
			appendStrikethroughEndString(sb);
		if (partition.isUnderlined())
			appendUnderlinedEndString(sb);
		if (partition.isItalic())
			appendItalicEndString(sb);
		if (partition.isBold())
			appendBoldEndString(sb);
		return sb.toString();
	}

	// Actions here and below appendSmth-like methods is very simple;
	// This methods is made for applying some additional logics to them in
	// subclasses
	protected void appendBoldStartString(StringBuilder sb) {
		sb.append(getBoldStartString());
	}

	protected void appendItalicStartString(StringBuilder sb) {
		sb.append(getItalicStartString());
	}

	protected void appendSupStartString(StringBuilder sb) {
		sb.append(getSupStartString());
	}

	protected void appendSubStartString(StringBuilder sb) {
		sb.append(getSubStartString());
	}

	protected void appendSubEndString(StringBuilder sb) {
		sb.append(getSubEndString());
	}

	protected void appendSupEndString(StringBuilder sb) {
		sb.append(getSupEndString());
	}

	protected void appendStrikethroughStartString(StringBuilder sb) {
		sb.append(getStrikeStartString());
	}

	protected void appendUnderlinedStartString(StringBuilder sb) {
		sb.append(getUnderlinedStartString());
	}

	protected void appendBoldEndString(StringBuilder sb) {
		sb.append(getBoldEndString());
	}

	protected void appendItalicEndString(StringBuilder sb) {
		sb.append(getItalicEndString());
	}

	protected void appendStrikethroughEndString(StringBuilder sb) {
		sb.append(getStrikeEndString());
	}

	protected void appendUnderlinedEndString(StringBuilder sb) {
		sb.append(getUnderlinedEndString());
	}

	protected void closeFontStyleTag(BasePartition partition, StringBuilder sb) {
		sb.append(getFontStyleClosingTag(partition));
	}

	protected boolean openFontStyleTagIfNeeded(BasePartition partition,
			StringBuilder sb) {
		if (!partition.getFontDataName().equals(FontStyle.NORMAL_FONT_NAME)) {
			sb.append(getFontStyleOpeningTag(partition));
			return true;
		}
		return false;
	}

	protected String getRegionString(RegionPartition partition) {
		StringBuilder sb = new StringBuilder();
		// sb.append(indent);
		sb.append(getRegionStartString());
		String contentType = partition.getContentType();
/*		if (contentType != null	&& contentType.length() > 0)
				sb.append(contentType);*/
		sb.append("\"");
		sb.append(getTagCloseString());
		sb.append("\n");
		String content = partition.getContent();
		sb.append(content);
		sb.append("\n");
		// sb.append(indent);
		sb.append(getRegionEndString() + "\n");
		return sb.toString();
	}

	protected String getHRString(HRPartition partition) {
		String str = "<hr";
		if (partition.getColorRGB() != null)
			str = str + getColorStr(partition.getColorRGB());
		str = str + ">";
		return str;
	}

	protected String encodeStr(String str) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			if (builder.length() != 0) {
				if (Character
						.isWhitespace(builder.charAt(builder.length() - 1))) {
					if (str.charAt(i) == ' ') {
						builder.append("&nbsp;");
						continue;
					}
				}
			}
			builder.append(str.charAt(i));
		}
		String str2 = builder.toString();
		str2 = str2.replace("<", "&lt;");
		str2 = str2.replace(">", "&gt;");
		str2 = str2.replace("\t", "&nbsp; &nbsp; ");
		return str2;
	}

	protected String getImageStr(ImagePartition partition) {
		String imageFileName = partition.getImageFileName();
		if (helper != null) {
			imageFileName = helper.getImageLocation(partition);
		}
		return "<IMG src=\"" + imageFileName + "\">";
	}
//
//	/**
//	 * <b>Unused</b>
//	 * 
//	 * @param fontDataName
//	 * @return
//	 */
//	protected String getFontFaceStr(String fontDataName) {
//		return " face = \""
//				+ fontRegistry.get(fontDataName).getFontData()[0].getName()
//				+ "\" height = \""
//				+ fontRegistry.get(fontDataName).getFontData()[0].getHeight()
//				+ "px\"";
//	}
//
//	/**
//	 * <b>Unused</b>
//	 * 
//	 * @param fontDataName
//	 * @return
//	 */
//	protected String getFontCSSStr(String fontDataName) {
//		return " font-family : "
//				+ fontRegistry.get(fontDataName).getFontData()[0].getName()
//				+ ";\n" + " font-size :"
//				+ fontRegistry.get(fontDataName).getFontData()[0].getHeight()
//				+ "px;";
//	}
//
//	/**
//	 * <b>Unused</b>
//	 * 
//	 * @param fontDataName
//	 * @return
//	 */
//	protected String getBodyFontParametersStr() {
//		String str = "<style type=\"text/css\">\n";
//		str = str + "body\n{\n";
//		str = str + getFontCSSStr(FontStyle.NORMAL_FONT_NAME);
//		str = str + "}\n</style>\n";
//		return str;
//	}

	protected String getColorStr(RGB color) {
		return " color = \"" + getRGBColorHexStr(color) + "\"";
	}

	protected String getBgColorStr(RGB color) {
		return " style=\"background-color:" + getRGBColorHexStr(color) + "\"";
	}

	protected String getRGBColorHexStr(RGB color) {
		String r = Integer.toHexString(color.red);
		if (r.length() == 1)
			r = "0" + r;
		String g = Integer.toHexString(color.green);
		if (g.length() == 1)
			g = "0" + g;
		String b = Integer.toHexString(color.blue);
		if (b.length() == 1)
			b = "0" + b;
		return "#" + r + g + b;
	}

	protected String getCenterAlignAttributeString() {
		return "align = \"center\"";
	}

	protected String getFileEndString() {
		return "</body></html>";
	}

	protected String getFileStartString() {
		return "<html>\n<body>\n";// + getBodyFontParametersStr();
	}

	protected String getJustifyAlignAttributeString() {
		return "align = \"justify\"";
	}

	protected String getLeftAlignAttributeString() {
		return "align = \"left\"";
	}

	protected String getLineBreakString() {
		return "<br>";
	}

	protected String getParagraphEndString() {
		return "</P>";
	}

	protected String getParagraphStartString(int lineIdx) {
		return "<P  style=\"margin: 4;" + getParagraphStyleStr(lineIdx);
	}

	protected String getRightAlignAttributeString() {
		return "align = \"right\"";
	}

	protected String getTagCloseString() {
		return ">";
	}

	protected String getBulletedListCloseString() {
		return "</ul>";
	}

	protected String getBulletedListOpenString() {
		return "<ul>";
	}

	protected String getBulletedListElementStartString(int lineIndex) {
		StringBuilder sb = new StringBuilder();
		if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '\n')
			sb.append('\n');
		sb.append("<li>");
		return sb.toString();
	}

	protected String getNumberedListCloseString() {		
		return "</ol>";
	}

	protected String getNumberedListOpenString() {
		return "<ol>";
	}

	protected String getNumberedListElementStartString(int bulletType) {
		StringBuilder sb = new StringBuilder();
		sb.append("<li "); 
		sb.append(getTypedBulletStr(bulletType));
		sb.append(">");
		return sb.toString();
	}
	
	protected String checkInnerLists(int lineIndex, int newBulletType)
	{
		StringBuilder sb = new StringBuilder();
		if (lineIndex > 0)
		{
			//int prevLineBulletType = provider.getBulletType(lineIndex - 1);
			int prevLineBulletId = provider.getBulletIdForLine(lineIndex - 1);
			if (prevLineBulletId != 0) {
				int prevIndent = provider.getLineIndent(lineIndex - 1);
				int indent = provider.getLineIndent(lineIndex);
				if (prevIndent < indent)
				{
					if (newBulletType == ILineInformationProvider.SIMPLE_BULLET)
						sb.append(getBulletedListOpenString());
					else
						sb.append(getNumberedListOpenString());
					openedTags.push(newBulletType);
				}
				else if (prevIndent > indent)
				{
					sb.append(closeLists(prevIndent - indent));
					int idx = lineIndex - 2;
					while (idx >= 0 && provider.getLineIndent(idx) > indent)
						idx--;
					if (idx < 0 || provider.getBulletIdForLine(idx) != provider.getBulletIdForLine(lineIndex))
					{
						sb.append(closeLists(1));						
					}
						
				}
				else if (prevLineBulletId != provider.getBulletIdForLine(lineIndex))
				{
					sb.append(closeLists(1));		
				}
			}
		}
		return sb.toString();
	}
	
	protected void closeListsIfNeeded(int lineIndex, StringBuilder sb, boolean lastLine)
	{
		if (!lastLine)
		{
			final Integer curEnd = listsEnds.get(lineIndex - 1);
			if (curEnd != null)
			{
				int level = listsIndents.get(curEnd);
				sb.append(closeLists(level));			
			}
		}
		else
			while(openedTags.size() > 0)
			{
				Integer tag = openedTags.pop();
				if (tag == ILineInformationProvider.SIMPLE_BULLET)
					sb.append(getBulletedListCloseString());
				else if (tag == ILineInformationProvider.LETTER_BULLET ||
						 tag == ILineInformationProvider.NUMBER_BULLET ||
						 tag == ILineInformationProvider.ROMAN_BULLET)
					sb.append(getNumberedListCloseString());
			}
	}

	protected String getTypedBulletStr(int bulletType) {
		if (bulletType == ILineInformationProvider.LETTER_BULLET)
			return "type=\"a\"";
		else if (bulletType == ILineInformationProvider.ROMAN_BULLET)
			return "type=\"i\"";
		if (bulletType == ILineInformationProvider.NUMBER_BULLET)
			return "";
		return "";
	}

	protected String getLinkEndString() {
		return "</A>";
	}

	protected String getLinkStartString(LinkPartition partition) {
		LinkPartition linkPartition = (LinkPartition) partition;
		String url = linkPartition.getUrl();
		if (helper != null) {
			url = helper.getLinkURL(linkPartition);
		}
		return "<A href = \"" + url + "\">";

	}

	protected String getBoldEndString() {
		return BOLD_CLOSE_TAG;
	}

	protected String getBoldStartString() {
		return BOLD_OPEN_TAG;
	}

	protected String getItalicEndString() {
		return ITALIC_CLOSE_TAG;
	}

	protected String getItalicStartString() {
		return ITALIC_OPEN_TAG;
	}

	protected String getStrikeEndString() {
		return STRIKE_CLOSE_TAG;
	}

	protected String getStrikeStartString() {
		return STRIKE_OPEN_TAG;
	}

	protected String getUnderlinedEndString() {
		return UNDERLINE_CLOSE_TAG;
	}

	protected String getUnderlinedStartString() {
		return UNDERLINE_OPEN_TAG;
	}

	protected String getFontStyleClosingTag(BasePartition partition) {
		return "</span>";
	}

	protected String getFontStyleOpeningTag(BasePartition partition) {
		Font fd=curLayer.getManager().getFontStyleManager().getFont(partition.getFontDataName());
		return "<span style='font-family:" + fd.getName()
				+ "; font-weight: normal; font-size: " + fd.getHeight()
				+ "pt;' >";
	}

	protected String getFontColorTagOpenString() {
		return "<font ";
	}

	protected String getFontColorTagCloseString() {
		return "</font>";
	}

	protected String getRegionEndString() {
		// TODO It's not a really good way to represent regions in HTML;
		// We should make this customizable from the outside
		return REGION_CLOSE_TAG_STRING;
	}

	protected String getRegionStartString() {
		return REGION_TAG_START_STRING;
	}

	protected String getSubEndString() {
		return "</sub>";
	}

	protected String getSubStartString() {
		return "<sub>";
	}

	protected String getSupEndString() {
		return "</sup>";
	}

	protected String getSupStartString() {
		return "<sup>";
	}

	protected String getIndentStrForLine(int lineNum) {
		StringBuilder sb = new StringBuilder("");
		final int lineIndent = provider.getLineIndent(lineNum);
		for (int i = 0; i < lineIndent; i++)
			sb.append("&nbsp;");
		//return sb.toString(); TODO left indent implemented by another way
		return "";
	}

	protected String getIndentStr() {
		//return "&nbsp;";
		return "";
	}

	protected String getCustomNumberedListElementStartString(String bulletText) {
		// TODO Auto-generated method stub
		return "";
	}

	protected String getParagraphStyleStr(int lineIndex)
	{
		StringBuilder sb = new StringBuilder();
		int firstLineIndent = provider.getLineStartIndent(lineIndex);
		int lineIndent = provider.getLineIndent(lineIndex);
		int lineSpacing = provider.getLineSpacing(lineIndex);
		int rightLineIndent = provider.getRightLineIndent(lineIndex);
		if (lineIndent > 0)
		{
			sb.append("margin-left: ");
			sb.append(lineIndent * RichTextEditorConstants.TAB_WIDTH / 2);
			sb.append(HTML_INDENT_MEASUREMENT);
		}
		if (rightLineIndent > 0)
		{
			sb.append("margin-right: ");
			sb.append(rightLineIndent * RichTextEditorConstants.TAB_WIDTH / 2);
			sb.append(HTML_INDENT_MEASUREMENT);
		}
		if (firstLineIndent > 0)
		{
			sb.append("text-indent: ");
			sb.append(firstLineIndent * RichTextEditorConstants.TAB_WIDTH / 2);
			sb.append(HTML_INDENT_MEASUREMENT);
		}
		if (lineSpacing != RichTextEditorConstants.SINGLE_SPACING_CONST)
		{
			sb.append("line-height: ");
			if (lineSpacing == RichTextEditorConstants.ONE_AND_HALF_SPACING_CONST)
				sb.append("1.5");
			else if (lineSpacing == RichTextEditorConstants.DOUBLE_SPACING_CONST)
				sb.append("2.0");
			else
				throw new IllegalArgumentException(lineSpacing + " is not valid line spacing value.");
			sb.append(";");
		}
		sb.append("\"");
		return sb.toString();
	}

	protected String closeLists(int depth) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < depth && i < openedTags.size(); i++)
		{
			Integer tag = openedTags.pop();
			if (tag == ILineInformationProvider.SIMPLE_BULLET)
				sb.append(getBulletedListCloseString() + "\n");
			else if (tag == ILineInformationProvider.NUMBER_BULLET || tag == ILineInformationProvider.ROMAN_BULLET ||
					tag == ILineInformationProvider.LETTER_BULLET)
				sb.append(getNumberedListCloseString() + "\n");
		}
		return sb.toString();
	}
	
	protected void startBulletedList(StringBuilder sb)
	{	
		super.startBulletedList(sb);
		openedTags.push(ILineInformationProvider.SIMPLE_BULLET);
	}
	
	protected void startNumberedList(StringBuilder sb)
	{
		super.startNumberedList(sb);
		openedTags.push(ILineInformationProvider.NUMBER_BULLET);
	}
	

}
