package com.onpositive.richtexteditor.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;


import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtexteditor.model.ISimpleRichTextModel;

public abstract class AbstractTextLoader implements ITextLoader
{
	/**
	 * Option for analyzing all text lines separately and disable any line merging logics while parsing
	 * Intended that string value "true" for it will be treated as <code>true</code>, all other values will be treated as <code>false</code>  
	 */
	public static final String SEPARATE_LINES_OPTION = "SEPARATE_LINES_OPTION";
	
	protected ITextDocument sharedDocument;
	
	public ITextDocument getSharedDocument() {
		return sharedDocument;
	}

	public void setSharedDocument(ITextDocument sharedDocument) {
		this.sharedDocument = sharedDocument;
	}

	/**
	 * Wraps parse process
	 * 
	 * @param reader
	 *            Contents reader
	 * @return ISimpleRichTextModel
	 * @throws IOException
	 *             in case of reading error
	 */
	public ISimpleRichTextModel parse(Reader reader) throws IOException {
		StringBuilder bld = new StringBuilder();
		while (true) {
			int read = reader.read();
			if (read != -1) {
				bld.append((char) read);
			} else {
				break;
			}
		}
		return parse(bld.toString());
	}

	/**
	 * Wraps parse process
	 * 
	 * @param stream
	 *            Contents input stream
	 * @return ISimpleRichTextModel
	 * @throws IOException
	 *             in case of reading error
	 */
	public ISimpleRichTextModel parse(InputStream stream) throws IOException {
		return parse(new InputStreamReader(stream));
	}

}
