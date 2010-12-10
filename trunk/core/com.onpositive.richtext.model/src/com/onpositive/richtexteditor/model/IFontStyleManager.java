package com.onpositive.richtexteditor.model;

import java.util.ArrayList;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.FontStyle;
import com.onpositive.richtext.model.meta.Color;
import com.onpositive.richtext.model.meta.Font;

public interface IFontStyleManager {

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

	FontStyle normFontStyle = new FontStyle(0, NORMAL_FONT_NAME,
			FONT_NORMAL_DISPLAY_NAME);
	FontStyle h1FontStyle = new FontStyle(0, FONT_H1_NAME,
			FONT_H1_DISPLAY_NAME);
	FontStyle h2FontStyle = new FontStyle(0, FONT_H2_NAME,
			FONT_H2_DISPLAY_NAME);
	FontStyle h3FontStyle = new FontStyle(0, FONT_H3_NAME,
			FONT_H3_DISPLAY_NAME);
	FontStyle h4FontStyle = new FontStyle(0, FONT_H4_NAME,
			FONT_H4_DISPLAY_NAME);
	FontStyle h5FontStyle = new FontStyle(0, FONT_H5_NAME,
			FONT_H5_DISPLAY_NAME);
	FontStyle h6FontStyle = new FontStyle(0, FONT_H6_NAME,
			FONT_H6_DISPLAY_NAME);
	FontStyle monospaceFontStyle = new FontStyle(0,
			FONT_MONOSPACE_NAME, FONT_MONOSPACE_DISPLAY_NAME);
	
	
	Font getFont(String fontDataName);


	Font getFontForPartition(BasePartition basePartition);


	String getNameForStyleString(String fontDataName);


	Color getEscapedLinkBackground();


	Color getEscapedLinkForeground();


	Color getInvalidLinkForeground();


	Color getInvalidLinkBackground();


	FontStyle getDefaultStyle();


	String[] getFontStyleDataNames();


	FontStyle getFontStyle(String item);


	FontStyle getFontStyleByFontDataName(String fontDataName);


	String[] getFontStyleDisplayNames();


	void addFontStyleChangeListener(
			FontStylesChangeListener fontStylesChangeListener);


	FontStyle[] getFontStyles();



}
