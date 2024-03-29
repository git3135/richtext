package com.onpositive.richtext.model.meta;



/**
 * Specification of changes applied to documents. All changes are represented as
 * replace commands, i.e. specifying a document range whose text gets replaced
 * with different text. In addition to this information, the event also contains
 * the changed document.
 *
 * @see org.eclipse.jface.text.IDocument
 */
public class DocumentEvent {

	/**
	 * Debug option for asserting that text is not null.
	 * If the <code>org.eclipse.text/debug/DocumentEvent/assertTextNotNull</code>
	 * system property is <code>true</code>
	 *
	 * @since 3.3
	 */
	private static final boolean ASSERT_TEXT_NOT_NULL= Boolean.getBoolean("org.eclipse.text/debug/DocumentEvent/assertTextNotNull"); //$NON-NLS-1$

	/** The changed document */
	public ITextDocument fDocument;
	/** The document offset */
	public int fOffset;
	/** Length of the replaced document text */
	public int fLength;
	/** Text inserted into the document */
	public String fText= ""; //$NON-NLS-1$
	/**
	 * The modification stamp of the document when firing this event.
	 * @since 3.1 and public since 3.3
	 */
	public long fModificationStamp;

	/**
	 * Creates a new document event.
	 *
	 * @param doc the changed document
	 * @param offset the offset of the replaced text
	 * @param length the length of the replaced text
	 * @param text the substitution text
	 */
	public DocumentEvent(ITextDocument doc, int offset, int length, String text) {

	
		fDocument= doc;
		fOffset= offset;
		fLength= length;
		fText= text;

		fModificationStamp= (fDocument).getModificationStamp();
		
	}

	/**
	 * Creates a new, not initialized document event.
	 */
	public DocumentEvent() {
	}

	/**
	 * Returns the changed document.
	 *
	 * @return the changed document
	 */
	public ITextDocument getDocument() {
		return fDocument;
	}

	/**
	 * Returns the offset of the change.
	 *
	 * @return the offset of the change
	 */
	public int getOffset() {
		return fOffset;
	}

	/**
	 * Returns the length of the replaced text.
	 *
	 * @return the length of the replaced text
	 */
	public int getLength() {
		return fLength;
	}

	/**
	 * Returns the text that has been inserted.
	 *
	 * @return the text that has been inserted
	 */
	public String getText() {
		return fText;
	}

	/**
	 * Returns the document's modification stamp at the
	 * time when this event was sent.
	 *
	 * @return the modification stamp or {@link IDocumentExtension4#UNKNOWN_MODIFICATION_STAMP}.
	 * @see IDocumentExtension4#getModificationStamp()
	 * @since 3.1
	 */
	public long getModificationStamp() {
		return fModificationStamp;
	}

	/*
	 * @see java.lang.Object#toString()
	 * @since 3.4
	 */
	public String toString() {
		StringBuffer buffer= new StringBuffer();
		buffer.append("offset: " ); //$NON-NLS-1$
		buffer.append(fOffset);
		buffer.append(", length: " ); //$NON-NLS-1$
		buffer.append(fLength);
		buffer.append(", timestamp: " ); //$NON-NLS-1$
		buffer.append(fModificationStamp);
		buffer.append("\ntext:>" ); //$NON-NLS-1$
		buffer.append(fText);
		buffer.append("<\n" ); //$NON-NLS-1$
		return buffer.toString();
	}
}
