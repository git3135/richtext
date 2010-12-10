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

package com.onpositive.richtexteditor.model.resources;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.FontStyle;
import com.onpositive.richtext.model.meta.RGB;
import com.onpositive.richtexteditor.model.FontStylesChangeListener;
import com.onpositive.richtexteditor.model.IFontStyleManager;

/**
 * @author kor & 32kda Manages (creates and disposes) fonts
 */
public class FontStyleManager implements IFontStyleManager {

	protected DisposableFontRegistry fontRegistry;

	protected HashMap<String, FontStyle> styleMap = new HashMap<String, FontStyle>();
	protected ArrayList<FontStyle> fontStyles = new ArrayList<FontStyle>(3);

	protected static RGB ESCAPED_LINK_FOREGROUND = new RGB(50, 50, 50);
	protected static RGB ESCAPED_LINK_BACKGROUND = new RGB(255, 255, 255);

	protected static RGB INVALID_LINK_FOREGROUND = new RGB(30, 30, 30);
	protected static RGB INVALID_LINK_BACKGROUND = new RGB(200, 200, 200);

	/**
	 * @return foreground
	 */
	public RGB getEscapedLinkForeground() {
		return ESCAPED_LINK_FOREGROUND;
	}

	/**
	 * @return background
	 */
	public RGB getEscapedLinkBackground() {
		return ESCAPED_LINK_BACKGROUND;
	}

	/**
	 * @return foreground
	 */
	public RGB getInvalidLinkForeground() {
		return INVALID_LINK_FOREGROUND;
	}

	/**
	 * @return background
	 */
	public RGB getInvalidLinkBackground() {
		return INVALID_LINK_BACKGROUND;
	}

	/**
	 * Normal font data display name
	 */
	public static String FONT_NORMAL_DISPLAY_NAME = "Normal";
	/**
	 * H1 font data display name
	 */
	public static String FONT_H1_DISPLAY_NAME = "Header 1";
	/**
	 * H2 font data display name
	 */
	public static String FONT_H2_DISPLAY_NAME = "Header 2";
	/**
	 * H3 font data display name
	 */
	public static final String FONT_H3_DISPLAY_NAME = "Header 3";
	/**
	 * H4 font data display name
	 */
	public static final String FONT_H4_DISPLAY_NAME = "Header 4";
	/**
	 * H5 font data display name
	 */
	public static final String FONT_H5_DISPLAY_NAME = "Header 5";
	/**
	 * H6 font data display name
	 */
	public static final String FONT_H6_DISPLAY_NAME = "Header 6";
	/**
	 * MONOSPACE font data display name
	 */
	public static final String FONT_MONOSPACE_DISPLAY_NAME = "Monospace";

	/**
	 * Normal font data name
	 */
	public static String NORMAL_FONT_NAME = FONT_NORMAL_DISPLAY_NAME;
	/**
	 * H1 font data name
	 */
	public static String FONT_H1_NAME = FONT_H1_DISPLAY_NAME;
	/**
	 * H2 font data name
	 */
	public static String FONT_H2_NAME = FONT_H2_DISPLAY_NAME;
	/**
	 * H3 font data name
	 */
	public static final String FONT_H3_NAME = FONT_H3_DISPLAY_NAME;
	/**
	 * H4 font data name
	 */
	public static final String FONT_H4_NAME = FONT_H4_DISPLAY_NAME;
	/**
	 * H5 font data name
	 */
	public static final String FONT_H5_NAME = FONT_H5_DISPLAY_NAME;
	/**
	 * H6 font data name
	 */
	public static final String FONT_H6_NAME = FONT_H6_DISPLAY_NAME;
	/**
	 * MONOSPACE font data name
	 */
	public static final String FONT_MONOSPACE_NAME = FONT_MONOSPACE_DISPLAY_NAME;

	protected FontStyle normFontStyle = new FontStyle(0, NORMAL_FONT_NAME,
			FONT_NORMAL_DISPLAY_NAME);
	protected FontStyle h1FontStyle = new FontStyle(0, FONT_H1_NAME,
			FONT_H1_DISPLAY_NAME);
	protected FontStyle h2FontStyle = new FontStyle(0, FONT_H2_NAME,
			FONT_H2_DISPLAY_NAME);
	protected FontStyle h3FontStyle = new FontStyle(0, FONT_H3_NAME,
			FONT_H3_DISPLAY_NAME);
	protected FontStyle h4FontStyle = new FontStyle(0, FONT_H4_NAME,
			FONT_H4_DISPLAY_NAME);
	protected FontStyle h5FontStyle = new FontStyle(0, FONT_H5_NAME,
			FONT_H5_DISPLAY_NAME);
	protected FontStyle h6FontStyle = new FontStyle(0, FONT_H6_NAME,
			FONT_H6_DISPLAY_NAME);
	protected FontStyle monospaceFontStyle = new FontStyle(0,
			FONT_MONOSPACE_NAME, FONT_MONOSPACE_DISPLAY_NAME);

	protected ArrayList<FontStylesChangeListener> listeners = new ArrayList<FontStylesChangeListener>();

	/**
	 * @param style
	 *            new font style to add to this manager
	 */
	public void addFontStyle(FontStyle style) {
		String fontDataName = style.getFontDataName();
		if (styleMap.containsKey(fontDataName)) {
			throw new IllegalArgumentException(
					"Style with a given font data name already exists");
		}
		this.fontStyles.add(style);

		this.styleMap.put(fontDataName, style);

	}

	/**
	 * Disposes font resources
	 */
	public void dispose() {
		fontRegistry.dispose();
	}

	/**
	 * Basic constructor
	 * 
	 * @param display
	 *            the device on which to allocate fonts
	 */
	public FontStyleManager(Display display) {
		initializeByDefault(display);
	}

	protected void initializeByDefault(Display display) {
		this.fontRegistry = new DisposableFontRegistry(display);
		addFontStyle(normFontStyle);
		addFontStyle(h1FontStyle);
		addFontStyle(h2FontStyle);
		addFontStyle(h3FontStyle);
		addFontStyle(monospaceFontStyle);
		fontRegistry.put(FONT_H1_NAME, new FontData[] { new FontData(
				"Times new Roman", 24, SWT.NORMAL) });
		fontRegistry.put(FONT_H2_NAME, new FontData[] { new FontData(
				"Times New Roman", 18, SWT.NORMAL) });
		fontRegistry.put(FONT_H3_NAME, new FontData[] { new FontData(
				"Times New Roman", 14, SWT.NORMAL) });
		fontRegistry.put(NORMAL_FONT_NAME, new FontData[] { new FontData(
				"Times New Roman", 12, SWT.NORMAL) });
		fontRegistry.put(FONT_MONOSPACE_NAME, new FontData[] { new FontData(
				"Monospace", 10, SWT.NORMAL) });
	}

	/**
	 * @return array of Font Data Names
	 */
	public String[] getFontStyleDataNames() {
		String[] result = new String[fontStyles.size()];
		int a = 0;
		for (FontStyle s : fontStyles) {
			result[a++] = s.getFontDataName();
		}
		return result;
	}

	/**
	 * @return array of Font Displayed Names
	 */
	public String[] getFontStyleDisplayNames() {
		String[] result = new String[fontStyles.size()];
		int a = 0;
		for (FontStyle s : fontStyles) {
			result[a++] = s.getDisplayName();
		}
		return result;
	}

	/**
	 * @return array of font styles
	 */
	public FontStyle[] getFontStyles() {
		return fontStyles.toArray(new FontStyle[fontStyles.size()]);
	}

	/**
	 * @return list of font styles
	 */
	public ArrayList<FontStyle> getFontStylesList() {
		return fontStyles;
	}

	/**
	 * Get a font style by
	 * 
	 * @param fontStyleDisplayName
	 *            specified display name
	 * @return font style
	 */
	public FontStyle getFontStyle(String fontStyleDisplayName) {
		for (Iterator<FontStyle> iterator = fontStyles.iterator(); iterator
				.hasNext();) {
			FontStyle fs = (FontStyle) iterator.next();
			if (fs.getDisplayName().equals(fontStyleDisplayName))
				return fs;
		}
		return null;
	}

	/**
	 * Get a font style by
	 * 
	 * @param fontDataName
	 *            inner fontDataName name
	 * @return font style
	 */
	public FontStyle getFontStyleByFontDataName(String fontDataName) {
		for (Iterator<FontStyle> iterator = fontStyles.iterator(); iterator
				.hasNext();) {
			FontStyle fs = (FontStyle) iterator.next();
			if (fs.getFontDataName().equals(fontDataName))
				return fs;
		}
		return null;
	}

	/**
	 * @param fontDataName
	 *            Font Data Name - really unparsed html attr here
	 * @return when it will be parsed, we'll return generated name string for
	 *         this style
	 */
	public String getNameForStyleString(String fontDataName) {
		if (fontDataName.toLowerCase().equals(FONT_MONOSPACE_NAME))
			return FONT_MONOSPACE_NAME;
		if (fontDataName.indexOf("font-family:") == -1
				&& fontDataName.indexOf("font-size:") == -1)
			return FontStyle.NORMAL_FONT_NAME;
		if (fontDataName.indexOf("font-family:") == -1)
			fontRegistry.get(FontStyle.NORMAL_FONT_NAME).getFontData()[0]
					.getName(); // By default
		String faceTail = fontDataName.substring(fontDataName
				.indexOf("font-family:")
				+ String.valueOf("font-family:").length());
		String faceName = faceTail.substring(0, faceTail.indexOf(";"))
				.toLowerCase().trim();

		String heightTail = fontDataName.substring(
				fontDataName.indexOf("font-size:")
						+ String.valueOf("font-size:").length()).trim();
		StringBuilder h = new StringBuilder();
		for (int i = 0; i < heightTail.length() && heightTail.charAt(i) >= '0'
				&& heightTail.charAt(i) <= '9'; i++) {
			h.append(heightTail.charAt(i));
		}
		String heightValue = h.toString();
		int height = fontRegistry.get(FontStyle.NORMAL_FONT_NAME).getFontData()[0]
				.getHeight(); // By default
		try {
			height = Integer.parseInt(heightValue);
		} catch (Exception e) {
		}

		for (Iterator<?> iterator = fontRegistry.getKeySet().iterator(); iterator
				.hasNext();) {
			String name = (String) iterator.next();
			FontData fd = fontRegistry.get(name).getFontData()[0];
			if (fd.getName().toLowerCase().equals(faceName)
					&& fd.getHeight() == height)
				return name;
		}

		String newFontName = faceName + "_" + heightValue;
		fontRegistry.put(newFontName, new FontData[] { new FontData(faceName,
				height, SWT.NORMAL) });

		return newFontName;
	}

	public int getFontHeightForPartition(BasePartition partition) {
		Font basicFont = fontRegistry.get(partition.getFontDataName());
		final FontData fontData = basicFont.getFontData()[0];
		return fontData.getHeight();
	}

	/**
	 * Return Font object for specified partition
	 * 
	 * @param partition
	 *            partition
	 * @return font for it
	 */
	public Font getFontForPartition0(BasePartition partition) {
		String preffix = "";
		Font basicFont = fontRegistry.get(partition.getFontDataName());
		final FontData oldFontData = basicFont.getFontData()[0];
		final int oldHeight = oldFontData.getHeight();
		int height = oldHeight;
		if (partition.isBold())
			preffix += FontStyle.BOLD_PREFFIX;
		if (partition.isItalic())
			preffix += FontStyle.ITALIC_PREFFIX;
		if (partition.isSub()) {
			preffix += FontStyle.SUB_PREFFIX;
			height = (int) (oldHeight / 1.5);
		}
		if (partition.isSup()) {
			preffix += FontStyle.SUP_PREFFIX;
			height = (int) (oldHeight / 1.5);
		}
		if (preffix.equals(""))
			return basicFont;
		String preffixName = preffix + partition.getFontDataName();
		Font newFont = fontRegistry.get(preffixName);
		if (newFont == null || newFont == fontRegistry.defaultFont()) {
			int fontStyle = 0;
			if (partition.isBold())
				fontStyle = SWT.BOLD;
			if (partition.isItalic())
				fontStyle = fontStyle | SWT.ITALIC;

			fontRegistry.put(preffixName, new FontData[] { new FontData(
					oldFontData.getName(), height, fontStyle) });
			newFont = fontRegistry.get(preffixName);
		}
		return newFont;
	}

	/**
	 * @return font registry associated with this object
	 */
	public FontRegistry getFontRegistry() {
		return fontRegistry;
	}

	/**
	 * @return default font style
	 */
	public FontStyle getDefaultStyle() {
		return styleMap.get(NORMAL_FONT_NAME);
	}

	/**
	 * @return default font height
	 */
	public int getDefaultFontHeight() {
		return fontRegistry.get(NORMAL_FONT_NAME).getFontData()[0].getHeight();
	}

	/**
	 * @return default font
	 */
	public Font getDefaultFont() {
		return fontRegistry.get(NORMAL_FONT_NAME);
	}

	/**
	 * @return the styleMap
	 */
	public HashMap<String, FontStyle> getStyleMap() {
		return styleMap;
	}

	public ArrayList<SaveableFontStyle> getSavebaleFontStylesList() {
		ArrayList<SaveableFontStyle> styles = new ArrayList<SaveableFontStyle>();
		for (Iterator iterator = fontStyles.iterator(); iterator.hasNext();) {
			FontStyle style = (FontStyle) iterator.next();
			SaveableFontStyle saveableFontStyle = new SaveableFontStyle(this,
					style);
			styles.add(saveableFontStyle);
		}
		return styles;
	}

	public void setStylesFromSaveableFontStylesList(
			Collection<SaveableFontStyle> list, Display display) {
		DisposableFontRegistry newRegistry = new DisposableFontRegistry(display);
		ArrayList<FontStyle> newFontStyles = new ArrayList<FontStyle>();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			SaveableFontStyle style = (SaveableFontStyle) iterator.next();
			String fontDataName = style.getFontDataName();
			String fdString = style.getFontDataString();
			FontData[] fontData = new FontData[] { new FontData(fdString) };
			newRegistry.put(fontDataName, fontData);
			FontStyle newStyle = new FontStyle(fontData[0].getStyle(),
					fontDataName, style.getDisplayName());
			newStyle.setColor(style.getColor());
			newStyle.setBgColor(style.getBgColor());
			newFontStyles.add(newStyle);
		}
		reinit(newFontStyles, newRegistry, newFontStyles);
	}

	/**
	 * Used to reinitialize contents of this manager, then they were changed by
	 * dialog
	 * 
	 * @param newStyles
	 *            new styles list
	 * @param newRegistry
	 *            new font registry, containing modified FontData
	 * @param changedStyles
	 *            styles, which has been changed
	 */
	public void reinit(ArrayList<FontStyle> newStyles,
			DisposableFontRegistry newRegistry,
			ArrayList<FontStyle> changedStyles) {
		fontStyles = newStyles;
		styleMap.clear();
		for (Iterator<FontStyle> iterator = newStyles.iterator(); iterator
				.hasNext();) {
			FontStyle fontStyle = (FontStyle) iterator.next();
			styleMap.put(fontStyle.getFontDataName(), fontStyle);
		}
		fontRegistry.dispose();
		fontRegistry = newRegistry;
		stylesChanged(changedStyles);
	}

	/**
	 * Font style change listener to add
	 * 
	 * @param listener
	 *            Listener to ad to listeners list
	 */
	public void addFontStyleChangeListener(FontStylesChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Font style change listener to remove
	 * 
	 * @param listener
	 *            Listener to ad to listeners list
	 */
	public void removeFontStyleChangeListener(FontStylesChangeListener listener) {
		listeners.remove(listener);
	}

	protected void stylesChanged(ArrayList<FontStyle> changedStyles) {
		for (Iterator<FontStylesChangeListener> iterator = listeners.iterator(); iterator
				.hasNext();) {
			FontStylesChangeListener listener = (FontStylesChangeListener) iterator
					.next();
			listener.stylesChanged(changedStyles);
		}
	}

	/**
	 * Determines, whether some font style display name is header font display
	 * name
	 * 
	 * @param fontStyleDisplayName
	 *            font style display name
	 * @return true, if it's header font style display name, false otherwise
	 */
	public static boolean isHeaderFontStyle(String fontStyleDisplayName) {
		if (fontStyleDisplayName.equals(FONT_H1_DISPLAY_NAME)
				|| fontStyleDisplayName.equals(FONT_H2_DISPLAY_NAME)
				|| fontStyleDisplayName.equals(FONT_H3_DISPLAY_NAME)
				|| fontStyleDisplayName.equals(FONT_H4_DISPLAY_NAME)
				|| fontStyleDisplayName.equals(FONT_H5_DISPLAY_NAME)
				|| fontStyleDisplayName.equals(FONT_H6_DISPLAY_NAME))
			return true;
		return false;
	}

	/**
	 * @param fontStyleDisplayName
	 * @return
	 */
	public static int getHeaderLevel(String fontStyleDisplayName) {
		if (fontStyleDisplayName.equals(FONT_H1_DISPLAY_NAME))
			return 1;
		if (fontStyleDisplayName.equals(FONT_H2_DISPLAY_NAME))
			return 2;
		if (fontStyleDisplayName.equals(FONT_H3_DISPLAY_NAME))
			return 3;
		if (fontStyleDisplayName.equals(FONT_H4_DISPLAY_NAME))
			return 4;
		if (fontStyleDisplayName.equals(FONT_H5_DISPLAY_NAME))
			return 5;
		if (fontStyleDisplayName.equals(FONT_H6_DISPLAY_NAME))
			return 6;
		return -1;
	}

	/**
	 * @return font
	 * 
	 */
	public com.onpositive.richtext.model.meta.Font getFont(String fontDataName) {
		// fontRegistry.get(fontDataName);
		FontData[] fontData = fontRegistry.getFontData(fontDataName);
		com.onpositive.richtext.model.meta.Font font = new com.onpositive.richtext.model.meta.Font(
				fontData[0].getName(), fontData[0].getHeight(), fontData[0]
						.getStyle());
		fontRegistry.put(font.toString(), fontData);
		return font;
	}

	/**
	 * @see com.onpositive.richtexteditor.model.IFontStyleManager#getFontForPartition(com.onpositive.richtext.model.BasePartition)
	 */
	public com.onpositive.richtext.model.meta.Font getFontForPartition(
			BasePartition partition) {
		String preffix = "";
		Font basicFont = fontRegistry.get(partition.getFontDataName());
		final FontData oldFontData = basicFont.getFontData()[0];
		final int oldHeight = oldFontData.getHeight();
		int height = oldHeight;
		 if (partition.isBold())
		 preffix += FontStyle.BOLD_PREFFIX;
		 if (partition.isItalic())
		 preffix += FontStyle.ITALIC_PREFFIX;
		if (partition.isSub()) {
			preffix += FontStyle.SUB_PREFFIX;
			height = (int) (oldHeight / 1.5);
		}
		if (partition.isSup()) {
			preffix += FontStyle.SUP_PREFFIX;
			height = (int) (oldHeight / 1.5);
		}
		if (preffix.equals("")) {
			com.onpositive.richtext.model.meta.Font font = new com.onpositive.richtext.model.meta.Font(oldFontData
					.getName(), oldFontData.getHeight(), oldFontData.getStyle());
			
			//fontRegistry.put(font.toString(), new Font(Display.getCurrent(),font.getName(),font.g));
			return font;
		}
		String preffixName = preffix + partition.getFontDataName();
		Font newFont = fontRegistry.get(preffixName);
		if (newFont == null || newFont == fontRegistry.defaultFont()) {
			int fontStyle = 0;
			if (partition.isBold())
				fontStyle = SWT.BOLD;
			if (partition.isItalic())
				fontStyle = fontStyle | SWT.ITALIC;

			FontData fontData = new FontData(oldFontData.getName(), height,
					fontStyle);
			fontRegistry.put(preffixName, new FontData[] { fontData });
			newFont = fontRegistry.get(preffixName);
		}
		FontData fontData = newFont.getFontData()[0];
		return new com.onpositive.richtext.model.meta.Font(fontData.getName(),
				fontData.getHeight(), fontData.getStyle());
		// return getFont(partition.getFontDataName());
	}

}
