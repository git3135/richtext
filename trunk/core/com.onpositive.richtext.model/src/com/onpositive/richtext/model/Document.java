package com.onpositive.richtext.model;

import java.util.ArrayList;
import java.util.List;


public class Document {

	protected ArrayList<String> lines = new ArrayList<String>();
	protected List<Integer> lineStarts = new ArrayList<Integer>();
	protected String text;

	public Document(String str) {
		this.text = str;
		int lineStart = 0;
		for (int a = 0; a < str.length(); a++) {
			char charAt = str.charAt(a);
			if (charAt == '\r' || charAt == '\n') {
				if (a > 0) {
					lines.add(str.substring(lineStart, a));
					lineStarts.add(lineStart);
					if (charAt == '\r') {
						if (str.charAt(a + 1) == '\n') {
							a++;
						}
					}
					lineStart = a+1;
				}
				
			}
		}
		lines.add(str.substring(lineStart, str.length()));
		lineStarts.add(lineStart);
	}

	public int getNumberOfLines() {
		return lines.size();
	}
	
	/**
	 * Returns line contents, <b>without</b> delimiter
	 * @param index Line index to get
	 * @return line contents, <b>without</b> delimiter
	 */
	public String get(int index) {
		return lines.get(index);
	}
	
	public int getDocumentLegth()
	{
		return text.length();
	}
	
	/**
	 * Returns line offset in text
	 * @param index Line index
	 * @return line offset
	 */
	public int getLineStart(int index)
	{
		return lineStarts.get(index);
	}
	
	public int getLineCount()
	{
		return lines.size();
	}
	
	/**
	 * Returns line length, <b>including</b> trailing delimiter
	 * @param index line index
	 * @return
	 */
	public int getLineLength(int index)
	{
		if (index < getLineCount() - 1)
			return lineStarts.get(index + 1) - lineStarts.get(index);
		return getDocumentLegth() - lineStarts.get(index);
	}
	
	/**
	 * Returns full document text
	 * @return document text
	 */
	public String get()
	{
		return text;
	}

}
