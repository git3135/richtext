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

package com.onpositive.richtexteditor.io.html_scaner;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.BasePartitionLayer;
import com.onpositive.richtext.model.HRPartition;
import com.onpositive.richtext.model.ImagePartition;
import com.onpositive.richtext.model.LinkPartition;
import com.onpositive.richtext.model.RegionPartition;
import com.onpositive.richtext.model.meta.BulletFactory;
import com.onpositive.richtext.model.meta.IImage;
import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtext.model.meta.RGB;
import com.onpositive.richtexteditor.io.LexEvent;
import com.onpositive.richtexteditor.io.html.HTMLTokenProvider;
import com.onpositive.richtexteditor.model.Logger;
import com.onpositive.richtexteditor.model.RichTextEditorConstants;

/**
 * 
 * @author 32kda made in USSR
 */
public class HTMLLexListener implements ILexListener {
	private static final int COLOR_HEX_STR_LEN = 7;

	private static final String COLOR_ATTR_NAME = "color:";

	private static final String MONOSPACE = "Monospace";

	protected boolean isLink = false;
	protected String curHref = "";

	protected boolean isImg = false;
	protected String curSrc = "";

	protected Scanner scanner;

	protected Stack<Boolean> boldStack = new Stack<Boolean>();
	protected Stack<Boolean> italicStack = new Stack<Boolean>();
	protected Stack<Boolean> underlinedStack = new Stack<Boolean>();
	protected Stack<Boolean> strikethroughStack = new Stack<Boolean>();
	protected Stack<Integer> subsupStack = new Stack<Integer>();
	protected Stack<Integer> listStack = new Stack<Integer>();
	protected boolean isCode = false;
	protected Stack<HashMap<String, String>> styleAttrsStack = new Stack<HashMap<String, String>>();
	protected Stack<Integer> tagsStack = new Stack<Integer>(); // We need this
																// for non-xhtml
																// texts
																// processing,
	// to watch tags structure
	protected HashMap<Integer, Integer> listTypes = new HashMap<Integer, Integer>();

	protected String fontFaceString = "font-family";
	protected String fontSizeString = "font-size";
	protected String fontColorString = "color";
	protected String bkColorString = "background-color";

	protected String h1StyleString = "font-family:Times New Roman; font-size:24pt";
	protected String h2StyleString = "font-family:Times New Roman; font-size:18pt";
	protected String h3StyleString = "font-family:Times New Roman; font-size:14pt";
	protected String h4StyleString = "font-family:Times New Roman; font-size:12pt";

	protected static int SUB_CONST = 1;
	protected static int SUP_CONST = 2;

	protected boolean isParagraphAttr = false; // This attr is paragraph attr

	protected StringBuilder curTextStr = new StringBuilder();
	protected ArrayList<BasePartition> partitions = new ArrayList<BasePartition>();
	protected ArrayList<Integer> lineAligns = new ArrayList<Integer>();
	protected ArrayList<Integer> lineBullets = new ArrayList<Integer>();
	protected ArrayList<Integer> lineIndents = new ArrayList<Integer>();
	//protected BasePartitionLayer layer;
	protected ITextDocument document;
	protected StringBuilder globalBuilder = new StringBuilder();
	protected int curLineAlign = RichTextEditorConstants.LEFT_ALIGN;
	protected int curLineIndent = 0;
	protected RGB curRGB = null; // Now used to remember <hr> color

	// protected boolean isBulletedList = false;
	// protected boolean isNumberedList = false;

	protected boolean wasFontTag = false; // If was font tag before
	protected boolean wasSpanTag = false; // If was span tag before
	protected boolean appendText = true; // If we should append current text to
	// result text
	protected boolean trimWhiteSpaces = true;

	protected static final int BULLETED_LIST = 0; // Bulleted list state
	protected static final int NUMBERED_LIST = 1; // Numbered list state

	protected int listState = -1; // If was <UL> tag before

	protected int listLevel = 0; // Used to manage multi-level list (not
	// really supported yet)
	protected boolean wasLI = false; // LI tag type
	protected boolean wasDiv = false; // Was div tag. We can interpret it
										// differently

	protected int currentNumberedListValue = 1; // Current numbered list bullet
	// idx
	protected int lastGeneratedCurrentListValue = currentNumberedListValue;// For
	// new
	// values
	// generation

	protected boolean isHR = false;

	protected boolean ignoreMarkup = false; // Yet, in-region html markup is not
	// supported.
	// Initially, regions was created for Wikitext, and there
	// is no in-region markup

	protected static RGB RED = new RGB(255, 0, 0);
	protected static RGB GREEN = new RGB(0, 255, 0);
	protected static RGB BLUE = new RGB(0, 0, 255);
	protected static RGB YELLOW = new RGB(255, 255, 0);
	protected static RGB GRAY = new RGB(128, 128, 128);
	protected static RGB BLACK = new RGB(0, 0, 0);

	/**
	 * Basic constructor
	 * 
	 * @param layer
	 *            layer for filling with this listener
	 * @param scanner
	 *            scanner, which we should listen
	 */
	public HTMLLexListener(ITextDocument document, Scanner scanner) {
		this.document = document;
		this.scanner = scanner;
	}

	/**
	 * Main function for parser events handling
	 * 
	 * @param event
	 *            LexEvent
	 */
	public void handleLexEvent(LexEvent event) {
		if (event instanceof TagLexEvent) {
			TagLexEvent tagEvent = (TagLexEvent) event;
			int type = tagEvent.getType();
			if (type == -1 || type == HTMLTokenProvider.TYPE_UNKNOWN)
				return;
			HashMap<String, String> currentFontStyleMap;

			if (ignoreMarkup) {
				if (isMarkupEndTag(tagEvent)) {
					ignoreMarkup = false;
					scanner.setClearEnters(true);
					BasePartition partition = getNewRegionPartition();
					partitions.add(partition);
				} else
					curTextStr.append(event.text);
				return;
			}

			if (tagEvent.isOpen()) {
				currentFontStyleMap = new HashMap<String, String>();
				styleAttrsStack.push(currentFontStyleMap);
				tagsStack.push(tagEvent.getType());
			}

			if ((type == HTMLTokenProvider.TYPE_HTML || type == HTMLTokenProvider.TYPE_BODY)) {
				if (resolveStr(curTextStr).length() > 0) {
					BasePartition partition = getNewFontPartition();
					if (partition.getLength() > 0)
						partitions.add(partition);
				}
			} else if (type == HTMLTokenProvider.TYPE_P) {
				if (tagEvent.isOpen()) {
					insertNewLineIfNeeded(); // We must be sure, that list
					// always begins at newline
					isParagraphAttr = true;
					curLineAlign = RichTextEditorConstants.LEFT_ALIGN;
					curLineIndent = 0;
				} else
					insertNewLineIfNeeded();
			} else if (type == HTMLTokenProvider.TYPE_BR) {
				styleAttrsStack.pop(); // Because br don't have closing tag to
				// pop there
				tagsStack.pop();
				endOfLineReached();
			} else if (type == HTMLTokenProvider.TYPE_PRE) {
				BasePartition partition = getNewFontPartition(); // we call this
				// here,
				// because
				// text is
				// being
				// trimmed
				// in
				// getNewFontPartition
				if (partition.getLength() > 0) // or it's submethods
					partitions.add(partition);
				trimWhiteSpaces = !tagEvent.isOpen();
				scanner.setClearEnters(trimWhiteSpaces);
			} else if (type == HTMLTokenProvider.TYPE_OL
					|| type == HTMLTokenProvider.TYPE_UL) {
				if (tagEvent.isOpen()) {
					if (listLevel > 0) {
						listStack.push(currentNumberedListValue);
						currentNumberedListValue = getNextCurrentListValue();
					}
					listLevel++;
					if (type == HTMLTokenProvider.TYPE_OL)
						listTypes.put(currentNumberedListValue,
								BulletFactory.SIMPLE_NUMBERED_LIST);
					else
						listTypes.put(currentNumberedListValue,
								BulletFactory.BULLETED_LIST);
				} else if (listLevel > 0) {
					listLevel--;
					if (listLevel == 0) {
						if (listStack.size() > 0)
							listStack.pop(); // TODO Think about this once again
						currentNumberedListValue = getNextCurrentListValue();// Means,
						// that
						// list
						// ended,
						// and
						// we
						// must
						// increase
						// this
						// value
						// for
						// possible
						// next
						// list
					} else
						currentNumberedListValue = listStack.pop();
				}

			} else if (type == HTMLTokenProvider.TYPE_UL) {

			} else if (type == HTMLTokenProvider.TYPE_LI) {
				if (tagEvent.isOpen()) {
					insertNewLineIfNeeded();
					wasLI = true;
				} else
					endOfLineReached();
			} else if (type == HTMLTokenProvider.TYPE_DIV && tagEvent.isOpen()) {
				wasDiv = true;
			} else if (type == HTMLTokenProvider.TYPE_TABLE
					|| type == HTMLTokenProvider.TYPE_TR) {
				if (tagEvent.isOpen()) {
					insertNewLineIfNeeded();
				} else {
					insertNewLineIfNeeded();
				}
			} else if (type == HTMLTokenProvider.TYPE_STYLE
					|| type == HTMLTokenProvider.TYPE_XML // Skip contents of
					// following
					|| type == HTMLTokenProvider.TYPE_SCRIPT
					|| type == HTMLTokenProvider.TYPE_TITLE) {
				appendText = !tagEvent.isOpen();
			} else if (appendText) // “екст не в служ теге.
			{
				if (type == HTMLTokenProvider.TYPE_IMG && tagEvent.isOpen()) {
					styleAttrsStack.pop(); // Because img don't have closing tag
					// to pop there
					tagsStack.pop();
					isImg = true;
				}

				if (type == HTMLTokenProvider.TYPE_HR && tagEvent.isOpen()) {

					styleAttrsStack.pop(); // Because hr don't have closing tag
					// to pop there
					tagsStack.pop();
					isHR = true;
				}

				BasePartition partition = getNewFontPartition();
				if (partition.getLength() > 0)
					partitions.add(partition);

				if (type == HTMLTokenProvider.TYPE_B)
					manageBold(tagEvent.isOpen());
				if (type == HTMLTokenProvider.TYPE_CODE)
					isCode = tagEvent.isOpen();
				if (type == HTMLTokenProvider.TYPE_I)
					manageItalic(tagEvent.isOpen());
				if (type == HTMLTokenProvider.TYPE_U)
					manageUnderlined(tagEvent.isOpen());
				if (type == HTMLTokenProvider.TYPE_STRIKE)
					manageStrikethrough(tagEvent.isOpen());
				if (type == HTMLTokenProvider.TYPE_SUB)
					manageSub(tagEvent.isOpen());
				if (type == HTMLTokenProvider.TYPE_SUP)
					manageSup(tagEvent.isOpen());
				if (type == HTMLTokenProvider.TYPE_FONT)
					wasFontTag = (tagEvent.isOpen());
				if (type == HTMLTokenProvider.TYPE_SPAN)
					wasSpanTag = (tagEvent.isOpen());
				if (type == HTMLTokenProvider.TYPE_H1) {
					endOfLineReached();
					manageBold(tagEvent.isOpen());
				}
				if (type == HTMLTokenProvider.TYPE_H2) {
					endOfLineReached();
					manageBold(tagEvent.isOpen());
				}
				if (type == HTMLTokenProvider.TYPE_H3) {
					endOfLineReached();
					manageBold(tagEvent.isOpen());
				}
				if (type == HTMLTokenProvider.TYPE_A) {
					if (tagEvent.isOpen())
						isLink = true;
					else {
						isLink = false;
						curHref = "";
					}
				}
			}

			if (!tagEvent.isOpen()) {
				int lastIdx = tagsStack.lastIndexOf(tagEvent.getType());
				if (lastIdx != -1) {
					tagsStack.remove(lastIdx);
					styleAttrsStack.remove(lastIdx);
				}
			}
		} else if (event instanceof AttrValueLexEvent) {
			if (ignoreMarkup)
				return;
			AttrValueLexEvent event2 = (AttrValueLexEvent) event;
			if (isParagraphAttr
					&& event2.type == HTMLTokenProvider.ATTR_TYPE_ALIGN) {
				if (event2.text.equalsIgnoreCase("left"))
					curLineAlign = RichTextEditorConstants.LEFT_ALIGN;
				else if (event2.text.equalsIgnoreCase("right"))
					curLineAlign = RichTextEditorConstants.RIGHT_ALIGN;
				else if (event2.text.equalsIgnoreCase("justify"))
					curLineAlign = RichTextEditorConstants.FIT_ALIGN;
				else if (event2.text.equalsIgnoreCase("center"))
					curLineAlign = RichTextEditorConstants.CENTER_ALIGN;
				isParagraphAttr = false;
			} else if (isParagraphAttr
					&& event2.type == HTMLTokenProvider.ATTR_TYPE_STYLE) {
				String MARGIN_LEFT_KEYWORD = "margin-left:";
				int idx = event2.text.indexOf(MARGIN_LEFT_KEYWORD);
				if (idx > 0) {
					int idx2 = event2.text.indexOf(';', idx);
					if (idx2 == -1)
						idx2 = event2.text.length();
					String substr = event2.text.substring(
							idx + MARGIN_LEFT_KEYWORD.length(), idx2).trim();
					int measurementIdx = substr.indexOf("em");
					if (measurementIdx > 0) {
						String value = substr.substring(0, measurementIdx)
								.trim();
						curLineIndent = Integer.parseInt(value) * 2
								/ RichTextEditorConstants.TAB_WIDTH;
					}
				}
			} else if (wasLI && event2.type == HTMLTokenProvider.ATTR_TYPE_TYPE) {
				if (event2.text.equalsIgnoreCase("a")) {
					listTypes.put(currentNumberedListValue,
							BulletFactory.LETTERS_NUMBERED_LIST);
				} else if (event2.text.equalsIgnoreCase("i")) {
					listTypes.put(currentNumberedListValue,
							BulletFactory.ROMAN_NUMBERED_LIST);
				}
			} else if (wasDiv
					&& event2.type == HTMLTokenProvider.ATTR_TYPE_CLASS) {
				if (event2.text.equals("RichText")) {
					tagsStack.pop();
					BasePartition partition = getNewFontPartition();
					if (partition.getLength() > 0)
						partitions.add(partition);
					insertNewLineIfNeeded();
					ignoreMarkup = true;
					scanner.setClearEnters(false);
				}
			} else {
				if (!styleAttrsStack.isEmpty()) {
					HashMap<String, String> peek = styleAttrsStack.peek();

					if (event2.type == HTMLTokenProvider.ATTR_TYPE_COLOR) {
						if (wasFontTag || wasSpanTag) {
							if (event2.text.indexOf(':') > -1)
								event2.text = manageStyleString(event2.text, peek);
							else
								peek.put(fontColorString, event2.text);
						} else if (isHR)
							curRGB = getRGBfromHexRGBString(event2.text);
					} else if (event2.type == HTMLTokenProvider.ATTR_TYPE_STYLE) {
						event2.text = manageStyleString(event2.text, peek);
					}

				}
				if (event2.type == HTMLTokenProvider.ATTR_TYPE_HREF) {
					if (isLink)
						curHref = convertLinkURL(event2.text);
				} else if (event2.type == HTMLTokenProvider.ATTR_TYPE_SRC) {
					if (isImg)
						curSrc = convertImageSrc(event2.text);
				}
			}
		} else if (event instanceof TagEndEvent) // Here we should clear all
													// tag-specific variables
		{
			if (ignoreMarkup)
				return;
			wasFontTag = false;
			wasSpanTag = false;
			isImg = false;
			isHR = false;
			if (((TagEndEvent) event).isOpen()) {
				if (((TagEndEvent) event).getType() == HTMLTokenProvider.TYPE_H1)
					inheritStyleFromString(h1StyleString);
				if (((TagEndEvent) event).getType() == HTMLTokenProvider.TYPE_H2)
					inheritStyleFromString(h2StyleString);
				if (((TagEndEvent) event).getType() == HTMLTokenProvider.TYPE_H3)
					inheritStyleFromString(h3StyleString);
				if (((TagEndEvent) event).getType() == HTMLTokenProvider.TYPE_IMG
						&& !curSrc.trim().equals(""))
					appendImage(curSrc);
				if (((TagEndEvent) event).getType() == HTMLTokenProvider.TYPE_HR) {
					appendHR(curRGB);
					curRGB = null;
				}
			}
		} else if (event instanceof EOFEvent) {
			BasePartition partition = getNewFontPartition();
			if (partition.getLength() > 0)
				partitions.add(partition);
			if (wasLI) {
				lineAligns.add(curLineAlign);
				lineIndents.add(curLineIndent);
				if (listLevel > 0)
					lineBullets.add(currentNumberedListValue);
				else
					lineBullets.add(BulletFactory.NONE_LIST_CONST);
			}
		} else if (appendText) {
			if (!event.text.trim().equals("")) {
				curTextStr.append(event.text);
			}
		}
		// ѕереводы строки зачищаем заранее, т.к. позже нам надо вставить
		// "значащие" переводы строки

		if (getLineCount(globalBuilder.toString())
				+ getLineCount(curTextStr.toString()) != lineAligns.size()) {
			Logger.log("(!) unsync");
		}
	}

	protected int getNextCurrentListValue() {
		return ++lastGeneratedCurrentListValue;
	}

	protected boolean isMarkupEndTag(TagLexEvent tagEvent) {
		if (tagEvent.getType() == HTMLTokenProvider.TYPE_DIV
				&& !tagEvent.isOpen())
			return true;
		return false;
	}

	protected int getLineCount(String s) {
		int cnt = 0;
		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) == '\n')
				cnt++;
		return cnt;
	}

	protected String convertImageSrc(String src) {
		return src;
	}

	protected String convertLinkURL(String url) {
		return url;
	}

	protected void insertNewLineIfNeeded() {
		if (!checkEolPresence())
			endOfLineReached();
	}

	protected void appendImage(String curSrc) {
		int newOffset = getDocEndOffset();

		globalBuilder.append("?");
		IImage img = null;
//		try {
//			img = new Image(curSrc);
//		} catch (Exception e) {
//			try {
//				URL resourceAsStream = this.getClass().getResource(
//						"/com/onpositive/richtexteditor/img/notFound.gif");
//				img = new Image(resourceAsStream);
//			} catch (Exception e1) {
//				System.err.println("Can't find notFound.gif");
//			}
//		}
		partitions.add(new ImagePartition(document, newOffset, 1, img, curSrc));

	}

	protected void appendHR(RGB rgb) {
		int newOffset = getDocEndOffset();

		/*
		 * partitions.add(new BasePartition(layer, newOffset, 1));
		 * globalBuilder.append("\n"); newOffset++;
		 */

		String hrStr = "\n?\n"; // And here add new HR partition
		HRPartition hrp = new HRPartition(document, newOffset, hrStr.length());
		if (rgb != null)
			hrp.setColorRGB(rgb);
		partitions.add(hrp);
		globalBuilder.append(hrStr);
	}

	/**
	 * used to get doc index to add partitions to doc's end 0 if doc contains no
	 * partitions, offset + length of last partition otherwise
	 */
	protected int getDocEndOffset() {
		int newOffset = 0; // TODO Maybe, possible to replace with
		// globalBuilder.length
		if (partitions.size() > 0)
			newOffset = partitions.get(partitions.size() - 1).getOffset()
					+ partitions.get(partitions.size() - 1).getLength();
		return newOffset;
	}

	/**
	 * Used to check, is it an eol in the end of current unresolved line or
	 * global StringBuilder
	 * 
	 * @return true, if \n or \r presented false otherwise
	 */
	protected boolean checkEolPresence() {
		String resolveStr = resolveStr(curTextStr);
		if (resolveStr.length() > 0 && !resolveStr.equals(" ")
				&& !resolveStr.equals("\t")) {
			if (resolveStr.charAt(resolveStr.length() - 1) == '\n'
					|| resolveStr.charAt(resolveStr.length() - 1) == '\r') {
				return true;
			}
		} else if (globalBuilder.length() > 0
				&& (globalBuilder.charAt(globalBuilder.length() - 1) == '\n' || globalBuilder
						.charAt(globalBuilder.length() - 1) == '\r'))
			return true;
		if (globalBuilder.toString().trim().equals("")
				&& resolveStr.toString().trim().equals(""))
			return true;
		return globalBuilder.length() == 0 && resolveStr.length() == 0;
	}

	/**
	 * This func is used to inherit style from other attributes and to form
	 * style string with inheritance
	 * 
	 * @param l
	 * @return
	 */
	protected String manageStyleString(String l,
			HashMap<String, String> currentStyleMap) {
		StringTokenizer st = new StringTokenizer(l, ";");
		while (st.hasMoreTokens()) {
			String str = st.nextToken().trim();
			int divPoint = str.indexOf(':');
			if (divPoint != -1) {
				String name = str.substring(0, divPoint).trim();
				String value = str.substring(divPoint + 1).trim();
				currentStyleMap.put(name, value);
			}
		}

		searchUndefinedValues(currentStyleMap);

		// styleAttrsStack.push(currentStyleMap); //Here we push current formed
		// style hashmap

		return getStyleStringForStyleMap(currentStyleMap);
	}

	protected void searchUndefinedValues(HashMap<String, String> currentStyleMap) {
		if (!currentStyleMap.containsKey(fontFaceString))
			searchValueFor(currentStyleMap, fontFaceString);
		if (!currentStyleMap.containsKey(fontSizeString))
			searchValueFor(currentStyleMap, fontSizeString);
		if (!currentStyleMap.containsKey(fontColorString))
			searchValueFor(currentStyleMap, fontColorString);
		if (!currentStyleMap.containsKey(bkColorString))
			searchValueFor(currentStyleMap, bkColorString);
	}

	/**
	 * Used to inherit missing attributes for style in a top of style stack from
	 * line l. Used for inheriting style attr for smth like h1
	 * 
	 * @param l
	 * @return attributes string with inheritance
	 */
	protected String inheritStyleFromString(String l) {
		HashMap<String, String> currentStyleMap = styleAttrsStack.peek();
		StringTokenizer st = new StringTokenizer(l, ";");
		while (st.hasMoreTokens()) {
			String str = st.nextToken().trim();
			int divPoint = str.indexOf(':');
			try {
				String name = str.substring(0, divPoint).trim().toLowerCase();
				String value = str.substring(divPoint + 1).trim().toLowerCase();
				if (!currentStyleMap.containsKey(name))
					currentStyleMap.put(name, value);
			} catch (Exception e) {
			}
		}

		return getStyleStringForStyleMap(currentStyleMap);
	}

	/**
	 * Converts styleMap into String
	 * 
	 * @param styleMap
	 * @return
	 */
	protected String getStyleStringForStyleMap(HashMap<String, String> styleMap) {
		StringBuilder resString = new StringBuilder();
		for (Iterator<Entry<String, String>> iterator = styleMap.entrySet()
				.iterator(); iterator.hasNext();) {
			Entry<String, String> next = iterator.next();
			String key = (String) next.getKey();
			resString.append(key);
			resString.append(':');
			resString.append(next.getValue());
			resString.append(';');
		}
		return resString.toString();
	}

	protected void searchValueFor(HashMap<String, String> currentStyleMap,
			String key) {
		String foundValue = null;
		for (Iterator<HashMap<String, String>> iterator = styleAttrsStack
				.iterator(); iterator.hasNext();) {
			HashMap<String, String> map = (HashMap<String, String>) iterator
					.next();
			if (map.containsKey(key)) {
				foundValue = map.get(key);
				break;
			}
		}
		if (foundValue != null)
			currentStyleMap.put(key, foundValue);
	}

	// 4 following Used to manage states stack
	protected void manageBold(boolean open) {
		if (open)
			boldStack.push(true);
		else if (boldStack.size() > 0)
			boldStack.pop();
	}

	protected void manageItalic(boolean open) {
		if (open)
			italicStack.push(true);
		else if (italicStack.size() > 0)
			italicStack.pop();
	}

	protected void manageUnderlined(boolean open) {
		if (open)
			underlinedStack.push(true);
		else if (underlinedStack.size() > 0)
			underlinedStack.pop();
	}

	protected void manageStrikethrough(boolean open) {
		if (open)
			strikethroughStack.push(true);
		else if (strikethroughStack.size() > 0)
			strikethroughStack.pop();

	}

	protected void manageSup(boolean open) {
		if (open)
			subsupStack.push(SUP_CONST);
		else if (subsupStack.size() > 0 && subsupStack.peek() == SUP_CONST)
			subsupStack.pop();
	}

	protected void manageSub(boolean open) {
		if (open)
			subsupStack.push(SUB_CONST);
		else if (subsupStack.size() > 0 && subsupStack.peek() == SUB_CONST)
			subsupStack.pop();
	}

	protected static RGB parseBgColorStyleString(String l) {
		int pos = l.indexOf("background-color:");
		if (pos >= 0) {
			int colorValueStartPos = pos
					+ String.valueOf("background-color:").length();
			String colorString = l.substring(colorValueStartPos,
					colorValueStartPos + 7);
			return getRGBfromHexRGBString(colorString);
		}
		return null;
	}

	protected static RGB parseColorString(String colorString) {
		String newString = colorString;
		RGB res = null;
		int pos = newString.indexOf(COLOR_ATTR_NAME);
		if (pos >= 0) {
			int colorValueStartPos = pos
					+ String.valueOf(COLOR_ATTR_NAME).length();
			if (newString.length() < colorValueStartPos + COLOR_HEX_STR_LEN)
				res = getRGBfromHexRGBString(tryToDecodeColorStr(newString
						.substring(colorValueStartPos)));
			else {
				newString = newString.substring(colorValueStartPos,
						colorValueStartPos + COLOR_HEX_STR_LEN);
				res = getRGBfromHexRGBString(newString);
				if (res == null)
					res = getRGBfromHexRGBString(tryToDecodeColorStr(newString
							.substring(colorValueStartPos)));
			}
			if (res == null)
				res = new RGB(0, 0, 0);
		}
		return res;
	}

	protected static String tryToDecodeColorStr(String str) {
		int i = str.length() - 1;
		while (!Character.isLetter(str.charAt(i)) && i > 0)
			i--;
		if (i != str.length() - 1)
			str = str.substring(0, i + 1);
		return HTMLTokenProvider.getInstance().getColorConstantHexStr(
				str.toLowerCase());
	}

	protected static RGB getRGBfromHexRGBString(String hexRGBString) {
		hexRGBString = hexRGBString.trim();
		if (hexRGBString.charAt(0) == '#')
			hexRGBString = hexRGBString.substring(1);
		if (hexRGBString.length() < 6)
			return new RGB(0, 0, 0);
		RGB rgb;
		try {
			rgb = new RGB(Integer.parseInt(hexRGBString.substring(0, 2), 16),
					Integer.parseInt(hexRGBString.substring(2, 4), 16), Integer
							.parseInt(hexRGBString.substring(4, 6), 16));
		} catch (Exception e) {
			rgb = null;
		}
		return rgb;
	}

	protected BasePartition getNewRegionPartition() {
		String addAfterPartitionStr = ""; // used for adding some symbols after
		// current partition
		int newOffset = 0;
		if (partitions.size() > 0)
			newOffset = partitions.get(partitions.size() - 1).getOffset()
					+ partitions.get(partitions.size() - 1).getLength();
		String resStr = "?\r\n";
		BasePartition newPartition = new RegionPartition(document, newOffset,
				resStr.length(), "", curTextStr.toString());
		globalBuilder.append(resStr);
		lineAligns.add(RichTextEditorConstants.LEFT_ALIGN);
		lineIndents.add(0);
		lineBullets.add(BulletFactory.NONE_LIST_CONST);
		curTextStr = new StringBuilder(addAfterPartitionStr);
		return newPartition;
	}

	protected BasePartition getNewFontPartition() {
		String addAfterPartitionStr = ""; // used for adding some symbols after
		// current partition
		int newOffset = 0;
		if (partitions.size() > 0)
			newOffset = partitions.get(partitions.size() - 1).getOffset()
					+ partitions.get(partitions.size() - 1).getLength();
		BasePartition newPartition;
		if (!curHref.equals("")) {
			if (curTextStr.length() > 0
					&& !Character.isWhitespace(curTextStr.charAt(curTextStr
							.length() - 1)))
				addAfterPartitionStr = " ";
			newPartition = new LinkPartition(document, newOffset, curTextStr
					.length());
			((LinkPartition) newPartition).setUrl(curHref);
		}

		else
			newPartition = new BasePartition(document, newOffset, curTextStr
					.length());
		// newPartition.setRefreshVisibleState(false); // Don't update; text &
		// partition is still under
		// construction

		newPartition.setBold(boldStack.size() > 0);
		newPartition.setItalic(italicStack.size() > 0);
		newPartition.setUnderlined(underlinedStack.size() > 0);
		newPartition.setStrikethrough(strikethroughStack.size() > 0);
		if (subsupStack.size() > 0) {
			newPartition.setSub(subsupStack.peek() == SUB_CONST);
			newPartition.setSup(subsupStack.peek() == SUP_CONST);
		}

		if (styleAttrsStack.size() > 0) {
			String curFontStyleString = manageStyleString("", styleAttrsStack
					.peek());
			if (curFontStyleString.trim() != "") {
				RGB curBackgroundRGB = parseBgColorStyleString(curFontStyleString);
				RGB curForegroundRGB = parseColorString(curFontStyleString);

				if (curForegroundRGB != null)
					newPartition.setColorRGB(curForegroundRGB);
				if (curBackgroundRGB != null)
					newPartition.setBgColorRGB(curBackgroundRGB);

				if (!curFontStyleString.equals("") && !isCode)
					newPartition.setFontDataName(curFontStyleString);
			}
		}
		if (isCode)
			newPartition.setFontDataName(MONOSPACE);

		String resStr = resolveStr(curTextStr);
		if (globalBuilder.length() > 0
				&& trimWhiteSpaces
				&& Character.isWhitespace(globalBuilder.charAt(globalBuilder
						.length() - 1)) && resStr.length() > 0
				&& (resStr.charAt(0) == ' ' || resStr.charAt(0) == '\t'))
			resStr = resStr.substring(1);

		/*
		 * if (!trimWhiteSpaces && str.charAt(i) == '\n') endOfLineReached();
		 */

		globalBuilder.append(resStr);
		newPartition.setLength(resStr.length());
		curTextStr = new StringBuilder(addAfterPartitionStr);
		return newPartition;
	}

	/**
	 * Inserts \n\r at the end of paragraph && does align & list style calcs, if
	 * needed
	 */
	protected void endOfLineReached() {
		if (trimWhiteSpaces)
			curTextStr.append("\r\n"); // Otherwise (between <pre> tags) neede
		// enters already exists
		lineAligns.add(curLineAlign);
		lineIndents.add(curLineIndent);
		if (listLevel > 0 && wasLI)
			lineBullets.add(currentNumberedListValue);
		else
			lineBullets.add(BulletFactory.NONE_LIST_CONST);
		wasLI = false;
	}

	protected String resolveStr(StringBuilder str) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			if (builder.length() != 0)
				if (trimWhiteSpaces
						&& Character.isWhitespace(builder.charAt(builder
								.length() - 1))) {
					if (str.charAt(i) == ' ' || str.charAt(i) == '\t')
						continue;
				}
			if (!trimWhiteSpaces && str.charAt(i) == '\n')
				endOfLineReached();
			builder.append(str.charAt(i));
		}

		str = builder;

		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '&') {
				int j = str.indexOf(";", i);
				if (j > i + 1) {
					String link = str.substring(i + 1, j);
					char chr = CharacterMapper.getCodePointFromName(link);
					String l = str.substring(0, i);
					String r = "";
					if (j < str.length() - 1)
						r = str.substring(j + 1);
					str = new StringBuilder();
					str.append(l);
					str.append(chr);
					str.append(r);
				}
			}
		}
		return str.toString();
	}

	public ArrayList<BasePartition> getPartitions() {
		return partitions;
	}

	public String getText() {
		return globalBuilder.toString();
	}

	/**
	 * Returns scanned text line bullets list.
	 * 
	 * @return line bullets list
	 */
	public ArrayList<Integer> getLineBullets() {
		return lineBullets;
	}

	/**
	 * Returns scanned text line aligns list.
	 * 
	 * @return line aligns list
	 */
	public ArrayList<Integer> getLineAligns() {
		return lineAligns;
	}

	/**
	 * Returns scanned text line indents list.
	 * 
	 * @return line indents list
	 */
	public ArrayList<Integer> getLineIndents() {
		return lineIndents;
	}

	public HashMap<Integer, Integer> getListTypes() {
		return listTypes;
	}

	protected String clearFromEnters(String l) {
		if (l.trim().length() == 0)
			return "";
		l = l.replace('\r', ' ');
		l = l.replace('\n', ' ');
		return l;
	}
}
