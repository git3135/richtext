package com.onpositive.richtexteditor.model;

import java.util.List;


import com.onpositive.richtext.model.meta.GlyphMetrics;
import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtext.model.meta.StyleRange;

/**
 * This decorator is used for autodecorating citation markers (">") situated in the beginning of the line
 * Also, it provides decoration sample
 * @author 32kda
 *
 */
public class CitationDecorator extends SimpleTextDecorator {
	
	protected static final String CITATION_MARKER = ">"; 
	protected ITextDocument doc;
	protected int documentOffset;


	/* (non-Javadoc)
	 * @see com.onpositive.richtexteditor.model.SimpleTextDecorator#decorateStyleRange(java.util.List, org.eclipse.jface.text.IDocument)
	 */
	public List<StyleRange> decorateStyleRange(List<StyleRange> ranges,
			ITextDocument doc) {		
		this.doc = doc;
		if (ranges!=null){
		documentOffset = ranges.get(0).start;
		}
		return super.decorateStyleRange(ranges, doc);
	}

	protected void decorateStyleRange(StyleRange decoratedRange) {
		decoratedRange.data = RichTextEditorConstants.DECORATION_SIGNATURE;
		float height = decoratedRange.font.getHeight();
		decoratedRange.metrics = new GlyphMetrics(Math.round(height), 0, (int) Math.round(height / 1.5));
	}

	protected int getMinimumDecorationLength() {
		return CITATION_MARKER.length();
	}

	protected int getNextDecorationOffset(String text) {		
		int indexOf = text.indexOf(CITATION_MARKER, lastDecorationOffset);
		while (indexOf > -1 && indexOf < text.length() && !checkIndex(indexOf, text))
		{
			lastDecorationOffset = indexOf + CITATION_MARKER.length();
			indexOf = text.indexOf(CITATION_MARKER, lastDecorationOffset);
		}
		if (indexOf > -1)
			lastDecorationOffset = indexOf + CITATION_MARKER.length(); 
		return indexOf;
	}

	private boolean checkIndex(int indexOf, String text) {
		indexOf = text.indexOf(CITATION_MARKER, lastDecorationOffset);
		if (indexOf == 0)
		{
			try
			{
				int lineOfOffset = doc.getLineOfOffset(documentOffset);
				int offsetOfLine = doc.getLineOffset(lineOfOffset);
				if (documentOffset == offsetOfLine)
				{
					String prev = doc.get(offsetOfLine, 1);
					return checkPrevCitation(indexOf, prev);
				}
				else
					return false;
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		else
		{
			char c = text.charAt(indexOf - 1);
			if (c == '\n' || c == '\r')
				return true;
			else
			{
				StringBuilder sb = new StringBuilder();
				for (int i = indexOf + documentOffset - 1; i > 0 && doc.getChar(i) != '\r' && doc.getChar(i) != '\n'; i--)
					sb.append(doc.getChar(i));
				String str = sb.reverse().toString();
				return checkPrevCitation(indexOf - str.length(), str);
			}
			
		}
	}

	private boolean checkPrevCitation(int indexOf, String prev) {
		if (prev.length() == 0)
			return false;
		for (int i = 0; i < prev.length();)
		{
			int idx = prev.indexOf(CITATION_MARKER, i);
			if (idx == -1 || idx != i)
				return false;
			i += CITATION_MARKER.length();
		}
		if (indexOf == 0)
		{
			try
			{
				int lineOfOffset = doc.getLineOfOffset(documentOffset);
				int offsetOfLine = doc.getLineOffset(lineOfOffset);
				if (offsetOfLine == documentOffset)
					return true;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
		else
			return true;
	}

	protected int getDecoratedAreaTailLength(int base, String text,
			StyleRange styleRange, int basePartEndOffset) {
		return basePartEndOffset;
	}

}
