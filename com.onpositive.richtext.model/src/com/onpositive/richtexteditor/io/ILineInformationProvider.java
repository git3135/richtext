package com.onpositive.richtexteditor.io;


public interface ILineInformationProvider
{
	public static final int NONE_BULLET = 0;
	public static final int NUMBER_BULLET = 6;
	public static final int SIMPLE_BULLET = 5;
	public static final int LETTER_BULLET = 7;
	public static final int ROMAN_BULLET = 8;
	public static final int EXPLICIT_BULLET = 9; //Bullets starting from some explicit number
	
	public static final int LEFT_ALIGN = 16384; //Really taken from SWT
	public static final int RIGHT_ALIGN = 131072;
	public static final int CENTER_ALIGN = 16777216;
	public static final int JUSTIFY_ALIGN = 4;
	
	/**
	 * Returns bullet type for some line
	 * @param lineIndex Line index
	 * @return NONE_BULLET if none assigned, 
	 * {@link ILineInformationProvider#NUMBER_BULLET} - for numbered list line,
	 * {@link ILineInformationProvider#LETTER_BULLET} - for numbered list line with letter markers,
	 * {@link ILineInformationProvider#ROMAN_BULLET} - for numbered list line with roman markers,
	 * {@link ILineInformationProvider#SIMPLE_BULLET} - for bulleted list
	 * {@link ILineInformationProvider#EXPLICIT_BULLET} - for bullets starting from some explicit number
	 */
	public int getBulletType(int lineIndex);
	
	/**
	 * Needed for getting bullet text, if bulletType == EXPLICIT_BULLET
	 * @param lineIndex Line index
	 * @return bullet text
	 */
	public String getBulletText(int lineIndex);
	/**
	 * Returns unique id for bullet assigned to a specified line
	 * For real GUI objects-based realizations should be hashCode,
	 * for "stub" realizations - may be some other unique number
	 * @param index Line index
	 * @return unique id for bullet 
	 */	
	public int getBulletIdForLine(int index);
	/**
	 * Returns line alignment for some line
	 * @param lineIndex Line index
	 * @return NONE_BULLET if none assigned, 
	 * NUMBER_BULLET - for numbered list line,
	 * SIMPLE_BULLET - for bulleted list
	 */
	public int getLineAlignment(int lineIndex);
	
	/**
	 * Returns line indent for some line
	 * @param lineIndex Line index
	 * @return line indent
	 */
	public int getLineIndent(int lineIndex);
	
	/**
	 * Returns first visible line indent for some line(paragraph)
	 * @param lineIndex Line index
	 * @return first visible line indent 
	 */
	public int getLineStartIndent(int lineIndex);
	
	/**
	 * Returns right line indent (right margin)
	 * @param lineIndex Line index
	 * @return right line indent (right margin) 
	 */
	public int getRightLineIndent(int lineIndex);
	
	/**
	 * Returns line spacing
	 * @param lineIndex Line index
	 * @return line spacing 
	 */
	public int getLineSpacing(int lineIndex);
	
	/**
	 * Returns global indent size. It's multiply factor for getting indent in pixels for some indent level
	 * indentInPixels = indentSize * indentLevel;	 
	 * @return global indent size
	 */
	public int getIndentSize();
	

}
