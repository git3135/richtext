/*******************************************************************************
 * Copyright (c) 2007, 2008 OnPositive Technologies (http://www.onpositive.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     OnPositive Technologies (http://www.onpositive.com/) - initial API and implementation
 *******************************************************************************/

package com.onpositive.richtext.model;

import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtext.model.meta.StyleRange;
import com.onpositive.richtexteditor.model.AbstractLayerManager;
import com.onpositive.richtexteditor.model.ILink;


/**
 * Represents a BasePartition, which is a link
 * 
 * @author 32kda Made in USSR
 */
public class LinkPartition extends BasePartition implements ILink {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected String url;

	protected int style;

	/**
	 * Healthy link
	 */
	public static final int LINK = 1;
	/**
	 * Escaped link
	 */
	public static final int ESCAPED = 2;
	/**
	 * Invalid link
	 */
	public static final int INVALID_LINK = 3;

	/**
	 * Stroken link
	 */
	public static final int STRIKE_THROUGH_LINK = 4;

	/**
	 * Basic constructor
	 * 
	 * @param layer
	 *            Layer where partition'll be situated
	 * @param offset
	 *            offset of part.
	 * @param length
	 *            length of part.
	 */
	public LinkPartition(ITextDocument document, int offset, int length) {
		super(document, offset, length);
	}

	/**
	 * Complex constructor
	 * 
	 * @param layer
	 *            Layer where partition'll be situated
	 * @param offset
	 *            offset of part.
	 * @param length
	 *            length of part.
	 * @param url
	 *            url string
	 * @param mask
	 *            style bitmask
	 * @param stylePrototype
	 *            link style prototype partition
	 */
	public LinkPartition(ITextDocument document, int offset, int length,
			String url, int mask, BasePartition stylePrototype) {
		super(document, offset, length);
		if (url == null)
			throw new IllegalArgumentException("Link partition url can't be null!");
		this.url = url;
		this.mask = mask;
		applyAttributes(stylePrototype);
	}

	/**
	 * Complex constructor
	 * 
	 * @param layer
	 *            Layer where partition'll be situated
	 * @param offset
	 *            offset of part.
	 * @param length
	 *            length of part.
	 * @param url
	 *            url string
	 * @param stylePrototype
	 *            link style prototype partition
	 */
	public LinkPartition(ITextDocument document, int offset, int length,
			String url, BasePartition stylePrototype) {
		super(document, offset, length);
		if (url == null)
			throw new IllegalArgumentException("Link partition url can't be null!");
		this.url = url;
		applyAttributes(stylePrototype);
	}

	/**
	 * @return url string
	 * 
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            url string
	 * 
	 */
	public void setUrl(String url) {
		if (url == null)
			throw new IllegalArgumentException("Link partition url can't be null!");
		this.url = url;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.onpositive.richtext.model.BasePartition#toString()
	 */
	public String toString() {
		return "{ (" + offset + "," + length + ") " + " bold: " + isBold()
				+ " italic: " + isItalic() + "   " + getText() + " url: " + url
				+ "}";
	}

	/**
	 * @see com.onpositive.richtext.model.BasePartition#isExpandable()
	 */
	public boolean isExpandable() {
		return true;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.onpositive.richtext.model.BasePartition#isWhitespaceExpandable()
	 */
	public boolean isWhitespaceExpandable() {
		return false;
	}

	/**
	 * @see com.onpositive.richtext.model.BasePartition#isMergeable()
	 */
	public boolean isMergeable() {
		return false;
	}

	/**
	 * @return Whether partition's url can be set. <code>false</code> means that
	 *         url is determined somehow by partition's text, e.g. CamelCase
	 */
	public boolean isUrlEditable() {
		return true;
	}

	/**
	 * @return Whether this link can be "clicked" and it's url can be opened
	 *         Actually, this method should be fast and "optimistic", it should
	 *         return false only for escaped / other obviously invalid links
	 */
	public boolean isValidLink() {
		return true;
	}

	public int getStyle() {
		return style;
	}

	public boolean setStyle(int style) {
		boolean res=this.style!=style;
		this.style = style;
		return res;
	}

	/**
	 * @see com.onpositive.richtext.model.BasePartition#getStyleRange(com.onpositive.richtexteditor.model.resources.AbstractLayerManager)
	 */
	
	public StyleRange getStyleRange(AbstractLayerManager manager) {
		StyleRange styleRange = super.getStyleRange(manager);
		switch (style) {
		case STRIKE_THROUGH_LINK:
			styleRange.strikeout = true;
			styleRange.underline = false;
			break;
		case ESCAPED:
			styleRange.foreground = manager.getFontStyleManager().getEscapedLinkForeground();
			styleRange.background =
					manager.getFontStyleManager().getEscapedLinkBackground();
			styleRange.underline = false;
			styleRange.strikeout = false;
			styleRange.underline = false;
			break;
		case INVALID_LINK:
			styleRange.strikeout = false;
			styleRange.underlineStyle = StyleRange.UNDERLINE_ERROR;
			styleRange.rise=2;
			styleRange.underline = true;
			styleRange.foreground = manager.getFontStyleManager().getInvalidLinkForeground();
			styleRange.background = manager.getFontStyleManager().getInvalidLinkBackground();
			break;
		default:
			//styleRange.underline = true;
			styleRange.strikeout = false;
			styleRange.underlineStyle = StyleRange.UNDERLINE_LINK;
		}
		return styleRange;
	}
}
