package com.onpositive.richtexteditor.wikitext.partitions;


import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.BasePartitionLayer;
import com.onpositive.richtext.model.IStylePartition;
import com.onpositive.richtext.model.LinkPartition;
import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtext.model.meta.StyleRange;
import com.onpositive.richtexteditor.model.AbstractLayerManager;

public class CamelCasePartition extends LinkPartition implements
		IStylePartition {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CamelCasePartition(ITextDocument document, int offset, int length) {
		super(document, offset, length);
		style = LINK;
	}

	public CamelCasePartition(ITextDocument document, int offset, int length,
			int style) {
		super(document, offset, length);
		this.style = style;
	}

	public String getUrl() {
		String text = getText();
		if (text != null)
			return text;
		return url; // We can need pre-initialized url, if we are disconnected
		// from ddocument at some moment
	}

	public boolean equalsByStyle(BasePartition partition2) {
		if (!super.equalsByStyle(partition2))
			return false;
		if (partition2 instanceof CamelCasePartition
				&& ((CamelCasePartition) partition2).getStyle() == style)
			return true;
		return false;
	}

	public StyleRange getStyleRange(AbstractLayerManager manager) {
		if (style == LINK) {
			StyleRange styleRange = manager.getLinkPrototype().getStyleRange(
					manager);
			StyleRange origRange = super.getStyleRange(manager);
			styleRange.font = origRange.font;
			styleRange.start = this.getOffset();
			styleRange.length = this.getLength();
			return styleRange;
		} else if (style == ESCAPED) {
			StyleRange styleRange = super.getStyleRange(manager);
			styleRange.foreground = 
					manager.getFontStyleManager().getEscapedLinkForeground();
			styleRange.background = 
					manager.getFontStyleManager().getEscapedLinkBackground();
			styleRange.underline = false;
			return styleRange;
		} else if (style == INVALID_LINK) {
			StyleRange styleRange = super.getStyleRange(manager);
			styleRange.foreground = 
					manager.getFontStyleManager().getInvalidLinkForeground();
			styleRange.background = 
					manager.getFontStyleManager().getInvalidLinkBackground();
			return styleRange;
		}
		return super.getStyleRange(manager);
	}

	public boolean isExpandable() {
		return true;
	}

	public boolean isWhitespaceExpandable() {
		return false;
	}

	public boolean isValidLink() {
		if (style == ESCAPED)
			return false;
		return true;
	}

	
	public boolean isUrlEditable() {
		return false;
	}
}
