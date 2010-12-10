package com.onpositive.richtexteditor.io;

import javax.swing.text.BadLocationException;

import com.onpositive.richtext.model.meta.Point;

public interface ITextSerilizer
{

	/**
	 * Serializes all contents of document into single string
	 * 
	 * @return Serialized string
	 */
	public String serializeAllToStr();

	/**
	 * Serialized selected doc fragment into String
	 * 
	 * @param selection
	 *            selection in editor
	 * @return serialized string
	 * @throws BadLocationException
	 *             (actually never does it on correct selection)
	 */
	public String serializeToStr(Point selection) throws Exception;

	/**
	 * Serializes a single line
	 * 
	 * @param lineNum
	 *            number of line to serialize
	 * @return seralized doc line
	 */
	public String getSerializedLine(int lineNum);

}
