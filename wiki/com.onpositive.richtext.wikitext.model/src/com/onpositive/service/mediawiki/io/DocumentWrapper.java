package com.onpositive.service.mediawiki.io;

import java.util.HashMap;


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;

import com.onpositive.richtext.model.meta.IDocumentListener;
import com.onpositive.richtext.model.meta.IRegion;
import com.onpositive.richtext.model.meta.ITextDocument;

/**
 * @author kor
 *
 */
public class DocumentWrapper implements ITextDocument {

	IDocument doc;

	HashMap<IDocumentListener, org.eclipse.jface.text.IDocumentListener> lMap = new HashMap<IDocumentListener, org.eclipse.jface.text.IDocumentListener>();

	public DocumentWrapper(IDocument doc2) {
		this.doc=doc2;
	}

	/**
	 * @see com.onpositive.richtext.model.meta.ITextDocument#addDocumentListener(com.onpositive.richtext.model.meta.IDocumentListener)
	 */
	public void addDocumentListener(final IDocumentListener basePartitionLayer) {
		org.eclipse.jface.text.IDocumentListener listener = new org.eclipse.jface.text.IDocumentListener() {

			public void documentChanged(DocumentEvent event) {
				basePartitionLayer.documentChanged(convertEvent(event));
			}

			private com.onpositive.richtext.model.meta.DocumentEvent convertEvent(
					DocumentEvent event) {
				return new com.onpositive.richtext.model.meta.DocumentEvent(
						DocumentWrapper.this, event.getOffset(), event
								.getLength(), event.fText);
			}

			public void documentAboutToBeChanged(DocumentEvent event) {
				basePartitionLayer
						.documentAboutToBeChanged(convertEvent(event));
			}
		};
		lMap.put(basePartitionLayer, listener);
		doc.addDocumentListener(listener);
	}

	/**
	 * @see com.onpositive.richtext.model.meta.ITextDocument#removeDocumentListener(com.onpositive.richtext.model.meta.IDocumentListener)
	 */
	public void removeDocumentListener(IDocumentListener basePartitionLayer) {
		doc.removeDocumentListener(lMap.get(basePartitionLayer));
	}

	/**
	 * @see com.onpositive.richtext.model.meta.ITextDocument#getLineInformation(int)
	 */
	public IRegion getLineInformation(int endLine) {
		try {
			final org.eclipse.jface.text.IRegion lineInformation = doc
					.getLineInformation(endLine);
			return new IRegion() {

				public int getOffset() {
					return lineInformation.getOffset();
				}

				public int getLength() {
					return lineInformation.getLength();
				}
			};
		} catch (BadLocationException e) {

			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @see com.onpositive.richtext.model.meta.ITextDocument#get(int, int)
	 */
	public String get(int offset, int length) {
		try {
			return doc.get(offset, length);
		} catch (BadLocationException e) {
			return null;
		}
	}

	/**
	 * @see com.onpositive.richtext.model.meta.ITextDocument#get()
	 */
	public String get() {
		return doc.get();
	}
	
	public String toString()
	{
		return get();
	}

	/**
	 * @see com.onpositive.richtext.model.meta.ITextDocument#getLength()
	 */
	public int getLength() {
		return doc.getLength();
	}

	/**
	 * @see com.onpositive.richtext.model.meta.ITextDocument#getLineLength(int)
	 */
	public int getLineLength(int lineNum) {
		try {
			return doc.getLineLength(lineNum);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * @see com.onpositive.richtext.model.meta.ITextDocument#getLineOfOffset(int)
	 */
	public int getLineOfOffset(int documentOffset) {
		try {
			return doc.getLineOfOffset(documentOffset);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * @see com.onpositive.richtext.model.meta.ITextDocument#getLineOffset(int)
	 */
	public int getLineOffset(int lineOfOffset) {
		try {
			return doc.getLineOffset(lineOfOffset);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * @see com.onpositive.richtext.model.meta.ITextDocument#getModificationStamp()
	 */
	public long getModificationStamp() {
		return ((IDocumentExtension4) doc).getModificationStamp();
	}

	/**
	 * @see com.onpositive.richtext.model.meta.ITextDocument#getNumberOfLines()
	 */
	public int getNumberOfLines() {
		return doc.getNumberOfLines();
	}

	/**
	 * @see com.onpositive.richtext.model.meta.ITextDocument#replace(int, int,
	 *      java.lang.String)
	 */
	public void replace(int offset, int length, String text) {
		try {
			doc.replace(offset, length, text);
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see com.onpositive.richtext.model.meta.ITextDocument#getChar(int)
	 */
	public char getChar(int a) {
		try {
			return doc.getChar(a);
		} catch (BadLocationException e) {
			return 0;
		}
	}

	public void set(String text) {
		doc.set(text);
	}
}
