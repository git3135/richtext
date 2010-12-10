package com.onpositive.richtext.model.meta;



/**
 * We use this event, if we need to remember text being replaced by new text.
 * We must do so when we need to make some operation (like some special partition inserting wit preceding text replacing)
 * atomic, so document is already changed when operation are going to be commited 
 * @author 32kda
 *
 */
public class ExtendedDocumentEvent extends com.onpositive.richtext.model.meta.DocumentEvent
{
	/**	event's replaced text */
	public String replacedText;
	
	/**
	 * Creates a new document event.
	 *
	 * @param doc the changed document
	 * @param offset the offset of the replaced text
	 * @param length the length of the replaced text
	 * @param text the substitution text
	 * @param replacedText event's replaced text
	 */
	public ExtendedDocumentEvent(ITextDocument doc, int offset, int length, String text, String replacedText) {
		super(doc,offset,length,text);
		this.replacedText = replacedText;
	}

	/**
	 * Used to copy some simple event data 
	 * @param event {@link DocumentEvent} to copy
	 * @param replacedText replacedText event's replaced text
	 */
	public ExtendedDocumentEvent(DocumentEvent event, String replacedText)
	{
		fDocument = event.fDocument;
		fLength = event.fLength;
		fModificationStamp = event.fModificationStamp;
		fOffset = event.fOffset;
		fText = event.fText;
		this.replacedText = replacedText;
	}
}
