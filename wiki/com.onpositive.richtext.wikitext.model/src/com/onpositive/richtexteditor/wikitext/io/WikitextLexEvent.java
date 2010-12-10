package com.onpositive.richtexteditor.wikitext.io;

import com.onpositive.richtexteditor.io.TypedLexEvent;


public class WikitextLexEvent extends TypedLexEvent
{
	/**
	 * Used to specify a number, which characterize count of symbols of prev event/text region,
	 * which should be excluded from previous event and included into this event
	 */
	protected int eventOffsetShift;
	
	public static final int BOLD = 1;
	public static final int ITALIC = 2;
	public static final int BOLD_ITALIC = 3;
	public static final int UNDERLINED = 4;
	public static final int HEADER1_OPEN = 5;
	public static final int HEADER2_OPEN = 6;
	public static final int HEADER3_OPEN = 7;
	public static final int HEADER4_OPEN = 8;
	public static final int HEADER5_OPEN = 9;
	public static final int HEADER6_OPEN = 10;
	public static final int NUMBERED_LIST = 11;
	public static final int LINK = 12;
	public static final int EXPLICIT_ID = 13;
	public static final int HEADER_CLOSE = 15;
	public static final int EOL_REACHED = 19; //EOL is used as closing tag for some wiki markup
	public static final int SIMPLE_REGION = 20; //Region partition
	public static final int IMAGE = 21;
	public static final int HR = 22;
	public static final int BR = 23;
	public static final int SUBSCRIPT = 24;
	public static final int SUPERSCRIPT = 25;
	public static final int BULLETED_LIST = 26;
	public static final int STRIKETHROUGH = 27;
	public static final int INDENT = 28;
	public static final int MONOSPACE = 29;
	public static final int RESET_STYLE_FLAGS = 30;
	/**
	 * Newline indicator for simple cases, like paragraphs
	 */
	public static final int ADDITIONAL_NEWLINE = 31;
	/**
	 * Break newline indicator, this newline should always be added
	 */
	public static final int BREAK_NEWLINE = 32;
	
	public static final int REGION_START = 42; //Region partition start
	public static final int REGION_STRING = 43; //Region partition string
	public static final int REGION_END = 44; //Region partition end
	public static final int TABLE_START = 45; //Table partition start
	public static final int TABLE_STRING = 46; //Table partition string
	public static final int TABLE_END = 47; //Table partition end
	public static final int OPTIONAL_WHITESPACE = 48; //Space, which should be added, if not after other whitespace
	public static final int MACRO = 49;

	public static final int DEFLIST_START = 60;
	public static final int DEFLIST_STRING = 61;
	public static final int DEFLIST_END = 62;

	public static final int SEPARATE_LINE = 63; //Line, which must be separated (citation etc.)

	




	public WikitextLexEvent(String l, int type)
	{
		super(l, type);
	}
	
	public WikitextLexEvent(String l, int type, int eventOffsetShift)
	{
		super(l, type);
		this.eventOffsetShift = eventOffsetShift;
	}

	
	/**
	 * @return the eventOffsetShift
	 */
	public int getEventOffsetShift()
	{
		return eventOffsetShift;
	}
	
	
	public String toString()
	{
		return "Event: " + text + " type: " + type;
	}

}
