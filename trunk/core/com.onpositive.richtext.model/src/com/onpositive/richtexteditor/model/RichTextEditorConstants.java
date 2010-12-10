package com.onpositive.richtexteditor.model;



/**
 * This class contains system-scope constants. Mostly - some marker constans for settings and flags
 * @author 32kda
 *
 */
public class RichTextEditorConstants
{
	/**
	 * Spacings
	 */
	
	/**
	 * Single spacing
	 */
	public static final int SINGLE_SPACING_CONST = 0;
	/**
	 * One-and-half spacing
	 */	
	public static final int ONE_AND_HALF_SPACING_CONST = 1;
	/**
	 * Double spacing
	 */
	public static final int DOUBLE_SPACING_CONST = 2;
	
	/**
	 * Left Text Align constant
	 */
	public static final int LEFT_ALIGN =1 << 14;
	/**
	 * Right Text Align constant
	 */
	public static final int RIGHT_ALIGN = 1 << 17;

	/**
	 * Center Text Align constant
	 */
	public static final int CENTER_ALIGN =1 << 24;
	/**
	 * Fit Left Text Align/Justify constant
	 */
	public static final int FIT_ALIGN = 4;
	/**
	 * No list
	 */
	public static final int NONE_LIST = 0;
	/**
	 * Simple bulleted list
	 */
	public static final int BULLETED_LIST = 5;
	/**
	 * Numbered list
	 */
	public static final int NUMBERED_LIST = 6;
	/**
	 * Numbered list with letters
	 */
	public static final int LETTERS_NUMBERED_LIST = 7;
	/**
	 * Numbered list with roman-like numeration
	 */
	public static final int ROMAN_NUMBERED_LIST = 8;
	/**
	 * Default line indent
	 */
	public static final int DEFAULT_INDENT = 0;
	/**
	 * Maximum line indent
	 */
	public static final int MAX_INDENT = 10;
	/**
	 * Maximum line right indent
	 */
	public static final int MAX_RIGHT_INDENT = 5;
	/**
	 * Maximum line spacing
	 */
	public static final int MAX_SPACING = 3; //3 * 0.5 + 0.5 = 2, double spacing
	
	/**
	 * Min line spacing
	 */
	public static final int MIN_SPACING = 1; //0.5 + 0.5 = 1, single spacing
	
	/**
	 * Tab width
	 */
	public static final int TAB_WIDTH = 4;
	
	/**
	 * Citation decoration sign
	 */
	public static final String DECORATION_SIGNATURE = "DECORATION";
	
	/**	 
	 * ExtendedStyledText class name, for creating it's instance using Reflection and checking some functions availability
	 *  This is class name of the extended styled text in rich text editor extension plugin.
	 */
	public static final String EXTENDED_STYLED_TEXT_CLASS = "org.eclipse.swt.custom.ExtendedStyledText";
	
	/**
	 * Top margin setting string constant for {@link ExtendedStyledText}
	 */
	public static final String TOP_MARGIN = "TOP_MARGIN";
	/**
	 * Bottom margin setting string constant for {@link ExtendedStyledText}
	 */
	public static final String BOTTOM_MARGIN = "BOTTOM_MARGIN";
	/**
	 * Left field size setting string constant for {@link ExtendedStyledText}
	 */
	public static final String LEFT_FIELD_SIZE = "LEFT_FIELD_SIZE";
	/**
	 * Right field size setting string constant for {@link ExtendedStyledText}
	 */
	public static final String RIGHT_FIELD_SIZE = "RIGHT_FIELD_SIZE";
	/**
	 * Renderer class name setting string constant for {@link ExtendedStyledText}
	 */
	public static final String RENDERER_CLASS = "RENDERER_CLASS";
	/**
	 * Main plug-in containing images
	 */
	public static final String MAIN_ICON_PLUGIN = "com.onpositive.richtexteditor";
	/**
	 * Path for 'image loading' icon
	 */	
	public static final String IMAGE_LOAD_ICON_PATH = "image_load.png";
	
	/**
	 * Path for 'image load error' icon
	 */	
	public static final String IMAGE_LOAD_ERROR_ICON_PATH = "error.gif";
}

