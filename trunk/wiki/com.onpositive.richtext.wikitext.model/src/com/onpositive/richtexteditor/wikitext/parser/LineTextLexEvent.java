package com.onpositive.richtexteditor.wikitext.parser;

import com.onpositive.richtexteditor.io.TypedLexEvent;

/**
 * This event type should be used for some common "marker" events indication.
 * Sample for this are indicating bold, italic, underline and some other style markers,
 * hyperlinks, etc.  
 * @author Dmitry [32kda] Karpenko
 *
 */
public class LineTextLexEvent extends TypedLexEvent
{
	
	/**
	 * Type constants
	 */
	/**
	 * Bold font style marker
	 */
	public static final int BOLD = 1;
	/**
	 * Italic font style marker
	 */
	public static final int ITALIC = 2;
	/**
	 * Bold-italic font style
	 */
	public static final int BOLD_ITALIC = 3;
	/**
	 * Underlined text marker
	 */
	public static final int UNDERLINED = 4;
	/**
	 * Hyperlink text
	 */
	public static final int LINK = 12;
	/**
	 * Escaped hyperlink 
	 */
	public static final int ESCAPED_LINK = 13;
	/**
	 * Image insertion
	 */
	public static final int IMAGE = 21;
	/**
	 * Subscript-styled text
	 */
	public static final int SUBSCRIPT = 24;
	/**
	 * Superscript-styled text
	 */
	public static final int SUPERSCRIPT = 25;
	/**
	 * Striked text
	 */
	public static final int STRIKETHROUGH = 27;
	/**
	 * Monospace font style marker
	 */
	public static final int MONOSPACE = 29;
	/**
	 * Citation marker
	 */
	public static final int CITATION = 30;
	/**
	 * Macro
	 */
	public static final int MACRO = 31;
	/**
	 * Simple text
	 */
	public static final int TEXT = 32;
	/**
	 * Text region
	 */
	public static final int REGION = 33;
	/**
	 * Horizontal line
	 */
	public static final int HR = 34;
	/**
	 * Definition list
	 */
	public static final int DEFLIST = 35;
	/**
	 * Table
	 */
	public static final int TABLE = 36;
	/**
	 * Comment
	 */
	public static final int COMMENT = 37;
	
	public static final int EOL = 50;
	
	public static final int MAX_TYPE = 100;
	
	
	protected int fullLength = -1;




	
	/**
	 * Line describes this event's text fragment, or some text fragment including this event area
	 */
	protected WikitextLine contextLine;
	
	/**
	 * Offset in line
	 */
	protected int offsetInLine = 0;
	
	public LineTextLexEvent(String text, int type, int offsetInLine)
	{
		this.text = text;
		this.type = type;
		this.offsetInLine = offsetInLine;
	}
	
	public LineTextLexEvent(String text, int type, int offsetInLine, int fullParsedLength)
	{
		this(text, type, offsetInLine);
		fullLength = fullParsedLength;
	}
	
	public LineTextLexEvent(String text, int type, int offsetInLine, WikitextLine contextLine)
	{
		this(text,type,offsetInLine);
		this.contextLine = contextLine;
		
	}
	
	boolean doNotInverse;
	
	public boolean isDoNotInverse() {
		return doNotInverse;
	}

	public void setDoNotInverse(boolean doNotInverse) {
		this.doNotInverse = doNotInverse;
	}

	public LineTextLexEvent(String text, int type, int offsetInLine, WikitextLine contextLine, int fullParsedLength)
	{
		this(text,type,offsetInLine,contextLine);
		fullLength = fullParsedLength;
	}

	/**
	 * @return the line
	 */
	public WikitextLine getContextLine()
	{
		return contextLine;
	}

	/**
	 * @return the offsetInLine
	 */
	public int getOffsetInLine()
	{
		return offsetInLine;
	}
	
	/**
	 * Returns full parsed length.
	 * Full parsed length means full text length parsed by matcher or scanner which created this event
	 * However, event text can contain only a valuable part of successfully parsed text, excluding
	 * some whitespaces, braces and other char, which are no longer interesting to us in underlying logics 
	 * @return value describing full length, that was parsed 
	 */
	public int getFullLength()
	{
		if (fullLength == -1)
			return text.length();
		return fullLength;
	}

	/**
	 * @param fullLength the fullLength to set
	 */
	public void setFullLength(int fullLength)
	{
		this.fullLength = fullLength;
	}
	
	
}
