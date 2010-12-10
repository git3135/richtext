package com.onpositive.richtext.model.meta;



public interface ITextDocument {

	/**
	 * Returns this document's text for the specified range.
	 *
	 * @param offset the document offset
	 * @param length the length of the specified range
	 * @return the document's text for the specified range
	 */
	String get(int offset, int length);

	int getLineOfOffset(int documentOffset);

	int getLineOffset(int lineOfOffset);

	long getModificationStamp();

	void removeDocumentListener(IDocumentListener basePartitionLayer);
	void addDocumentListener(IDocumentListener basePartitionLayer);

	IRegion getLineInformation(int endLine);

	int getNumberOfLines();

	int getLineLength(int lineNum);

	String get();

	int getLength();

	void replace(int offset, int length, String text);

	char getChar(int a);

	void set(String text);

	

}
