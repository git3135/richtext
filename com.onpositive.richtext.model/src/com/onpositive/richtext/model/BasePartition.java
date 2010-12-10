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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.onpositive.richtext.model.meta.Color;
import com.onpositive.richtext.model.meta.Font;
import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtext.model.meta.RGB;
import com.onpositive.richtext.model.meta.StyleRange;
import com.onpositive.richtexteditor.model.AbstractLayerManager;
import com.onpositive.richtexteditor.model.IFontStyleManager;
import com.onpositive.richtexteditor.model.Logger;

/**
 * @author 32kda
 * Basic partition class. 
 * It's instance can have font, color and style mask
 */
public class BasePartition implements IPartition,Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//transient BasePartitionLayer layer;
	transient ITextDocument document;

	int length;
	int offset;
	int index = -1;
	protected Object data;
	protected int mask = 0; // Attributes mask (bold, italic etc.)
	protected RGB colorRGB = null, bgColorRGB = null;
	//protected boolean refreshVisibleState = false;
	protected String fontDataName = FontStyle.NORMAL_FONT_NAME;
	/**
	 * This data intended to be used for storing some extra data connected with partition -
	 * some extra info like unparsed attr, which should be stored up to serialization  
	 */
	protected HashMap<String, Object> additionalData;

	private static String ADDITION = "_bold_italic";

	/**
	 * Gets a name of a "bold+italic" font by the base font name.
	 * 
	 * @param name - base font name.
	 * @return a "bold+italic" font name
	 */
	public static String getBoldItalicNameByFontName(String name)
	{
		return name + ADDITION;
	}
	
	/**
	 * @return Object's index in partition array
	 */
	public int getIndex()
	{
		return index;
	}	

	/**
	 * Basic constructor
	 * @param layer Layer where partition'll be situated
	 * @param offset offset of part.
	 * @param length length of part.
	 */
	public BasePartition(ITextDocument document, int offset, int length)
	{
		super();
		this.document = document;
		this.length = length;
		this.offset = offset;
		mask = 0;
	}

	/**
	 * @return true, if changing partition's attributes should cause partition text
	 * repainting in editor window.
	 */
/*	public boolean isRefreshVisibleState()
	{
		return refreshVisibleState;
	}*/


	/**
	 * Should changing partition's attributes cause partition text repainting in editor window?
	 * @param refreshVisibleState It should be false in cause, if partition is incorrect yet, document not changed yet,
	 * text widget not ready for repainting, or if it's too many partitions to repaint,
	 * and it's better to repaint them all later, at same time
	 */
/*	public void setRefreshVisibleState(boolean refreshVisibleState)
	{
		//this.refreshVisibleState = refreshVisibleState;
		this.refreshVisibleState = false;
	}*/

	/**
	 * @param length new partition length
	 */
	public void setLength(int length)
	{
		this.length = length;
	}

	/**
	 * @return True, if partition must be single partition on some line
	 */
	public boolean requiresSingleLine()
	{
		return false;
	}

	/**
	 * If one of the partition's symbols should be deleted, should the whole
	 * partition be deleted
	 * 
	 * @return true if yes, false otherwise
	 */
	public boolean requiresFullDeletion()
	{
		return false;
	}

	/**
	 * Can some symbols be type in the center of the partition
	 * 
	 * @return true if yes, false otherwise
	 */
	public boolean allowsInnerTyping()
	{
		return true;
	}

	/**
	 * @return has this partition bold attribute?
	 */
	public boolean isBold()
	{
		if ((mask & FontStyle.BOLD) > 0)
			return true;
		return false;
	}
	
	/**
	 * @param bold Should this partition have bold attribute?
	 */
	public void setBold(boolean bold)
	{
		if (bold)
			mask = mask | FontStyle.BOLD;
		else
			mask = mask & ~FontStyle.BOLD;
		/*if (refreshVisibleState)
			layer.handlePartitionEvent(new BasePartitionEvent(this)); // TODO May be slow
*/	}

	
	/**
	 * @return has this partition italic attribute?
	 */
	public boolean isItalic()
	{
		if ((mask & FontStyle.ITALIC) > 0)
			return true;
		return false;
	}
	
	/**
	 * @param italic Should this partition have italic attribute?
	 */
	public void setItalic(boolean italic)
	{
		if (italic)
			mask = mask | FontStyle.ITALIC;
		else
			mask = mask & ~FontStyle.ITALIC;
		/*if (refreshVisibleState)
			layer.handlePartitionEvent(new BasePartitionEvent(this));*/
	}
	
	/**
	 * @return Should this partition be expanded, when typing text in the middle or in the end of it? 
	 */
	public boolean isExpandable()
	{
		return true;		
	}
	
	/**
	 * @return Should this partition be expanded, when typing whitespace text in the end of it? 
	 */
	public boolean isWhitespaceExpandable()
	{
		return true;		
	}
	
	
	/**
	 * @return Can this partition have some style equal to another partitions and can it be merged with them?
	 */
	public boolean isMergeable()
	{
		return true;		
	}
	 
	
	/**
	 * @return has this partition sub attribute?
	 */
	public boolean isSub()
	{
		if ((mask & FontStyle.SUB) > 0)
			return true;
		return false;
	}
	
	/**
	 * @param sub Should this partition have sub attribute?
	 */
	public void setSub(boolean sub)
	{
		if (sub)
		{
			mask = mask & ~FontStyle.SUP;
			mask = mask | FontStyle.SUB;
		}
		else
			mask = mask & ~FontStyle.SUB;
/*		if (refreshVisibleState)
			layer.handlePartitionEvent(new BasePartitionEvent(this));*/
	}
	
	/**
	 * @return has this partition Sup attribute?
	 */
	public boolean isSup()
	{
		if ((mask & FontStyle.SUP) > 0)
			return true;
		return false;
	}
	
	/**
	 * @param sup Should this partition have sup attribute?
	 */
	public void setSup(boolean sup)
	{
		if (sup)
		{
			mask = mask & ~FontStyle.SUB;
			mask = mask | FontStyle.SUP;
		}
		else
			mask = mask & ~FontStyle.SUP;
/*		if (refreshVisibleState)
			layer.handlePartitionEvent(new BasePartitionEvent(this));*/
	}

	/**
	 * @param underlined Should this partition have underlined attribute?
	 */
	public void setUnderlined(boolean underlined)
	{
		if (underlined)
			mask = mask | FontStyle.UNDERLINED;
		else
			mask = mask & ~FontStyle.UNDERLINED;
/*		if (refreshVisibleState)
			layer.handlePartitionEvent(new BasePartitionEvent(this));*/
	}

	/**
	 * @param strikeThrough Should this partition have strikethrough attribute?
	 */
	public void setStrikethrough(boolean strikeThrough)
	{
		if (strikeThrough)
			mask = mask | FontStyle.STRIKETHROUGH;
		else
			mask = mask & ~FontStyle.STRIKETHROUGH;
		/*if (refreshVisibleState)
			layer.handlePartitionEvent(new BasePartitionEvent(this));*/
	}

	/** (non-Javadoc)
	 * @see com.onpositive.richtext.model.IPartition#getLength()
	 */
	public final int getLength()
	{
		return length;
	}

	/** (non-Javadoc)
	 * @see com.onpositive.richtext.model.IPartition#getOffset()
	 */
	public final int getOffset()
	{
		return offset;
	}

	/** (non-Javadoc)
	 * @see com.onpositive.richtext.model.IPartition#getText()
	 */
	public String getText()
	{
		if (document == null)
			return null;
		return document.get(offset, length);		
	}

	/**
	 * Used for inheriting attributes 
	 * @param oldPartition  - source partition for inheritance
	 */
	public void applyAttributes(BasePartition oldPartition)
	{
		mask = mask | oldPartition.mask;
		if (oldPartition.fontDataName != FontStyle.NORMAL_FONT_NAME)
			fontDataName = oldPartition.fontDataName;
		if (oldPartition.colorRGB != null)
			colorRGB = oldPartition.colorRGB;
		if (oldPartition.bgColorRGB != null)
			bgColorRGB = oldPartition.bgColorRGB;
	}

	/**
	 * @param offset new offset
	 */
	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	/** (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "{ (" + offset + "," + length + ") " + " bold: " + isBold()
				+ " italic: " + isItalic() + "   " + getText() + "}";
	}


	/**
	 * Breaks this partition into two parts, returns left part
	 * with no style
	 * @param divisionOffset offset to break
	 * @return new partition - left part of older
	 */
	public BasePartition extractLeftPartition(int divisionOffset)
	{
		if (divisionOffset <= offset)
			throw new RuntimeException("Wrong extractLeftPartition argument ("
					+ divisionOffset + " < " + offset + ")");
		if (divisionOffset == offset + length)
			return this;
		int oldOffset = offset;
		offset = divisionOffset;
		length = length - (divisionOffset - oldOffset);
		return PartitionFactory.createAsSample(this, oldOffset,
				divisionOffset - oldOffset);
	}

	/**
	 * Breaks this partition into two parts, returns left part,
	 * with style of old partition
	 * @param divisionOffset offset to break
	 * @return new partition - left part of older
	 */
	public BasePartition extractLeftPartitionWithStyle(int divisionOffset)
	{
		if (divisionOffset <= offset)
			throw new RuntimeException("Wrong extractLeftPartition argument ("
					+ divisionOffset + " < " + offset + ")");
		if (divisionOffset == offset + length)
			return this;
		int oldOffset = offset;
		offset = divisionOffset;
		length = length - (divisionOffset - oldOffset);
		return PartitionFactory.createAsSampleStyle(this, document, oldOffset,
				divisionOffset - oldOffset);
	}

	/**
	 * @return partition style mask
	 */
	public int getStyleMask()
	{
		return mask;
	}

	/**
	 * @param newMask new style mask for partition
	 */
	public void setStyleMask(int newMask)
	{
		mask = newMask;
/*		if (refreshVisibleState)
			layer.handlePartitionEvent(new BasePartitionEvent(this));*/
	}

	boolean equalsByCoords(BasePartition partition2)
	{
		if (partition2.offset == offset && partition2.length == length)
			return true;
		return false;
	}

	/**
	 * Compares two partitions by style
	 * @param partition2 second partition for comparison
	 * @return true, if 2 partition have equal style
	 */
	public boolean equalsByStyle(BasePartition partition2)
	{
		if (!partition2.isMergeable()
				|| !this.isMergeable())
		{
			return false;
		}
		
		if (mask == partition2.mask
				&& fontDataName.equals(partition2.fontDataName)
				&& colorRGB == partition2.colorRGB
				&& bgColorRGB == partition2.bgColorRGB)
			return true;
		return false;
	}
	
	public BasePartition getCommonStyle(BasePartition partition2)
	{
		if (partition2 instanceof ObjectPartition)
			return new BasePartition(document, 0, 0);
		if (partition2.getClass() != getClass())
		{
			BasePartition res = new BasePartition(document, 0, 0);
			res.applyAttributes(partition2);
			partition2 = res;
		}
		partition2.mask = mask & partition2.mask;
		if (partition2.colorRGB != colorRGB) partition2.setColorRGB(null);
		if (partition2.bgColorRGB != bgColorRGB) partition2.setBgColorRGB(null);
		if (!partition2.getFontDataName().equals(fontDataName))
			partition2.setFontDataName(FontStyle.NORMAL_FONT_NAME);
		return partition2;
	}

	/**
	 * @return FontData name for partition
	 */
	public String getFontDataName()
	{
		return fontDataName;
	}
	
/*	public Font getFont(){
		return layer.getManager().getFontStyleManager().getFont(fontDataName);
	}*/

	/**
	 * @param fontDataName new fontData name for partition
	 */
	public void setFontDataName(String fontDataName)
	{
		this.fontDataName = fontDataName;
	}

	/**
	 * returns partition's text from <b>offset</b> till patition's end
	 * @param offset Offset to return text from
	 * @return text 
	 */
	public String getTextFromOffset(int offset)
	{
		if (offset < this.offset || offset >= this.offset + this.length)
			throw new RuntimeException("Invalid getTextFromOffset argument - "
					+ offset);
		try
		{
			return document.get(offset,
					this.length - (offset - this.offset));
		} catch (Exception e)
		{
			Logger.log(e);
		}
		return null;
	}

	/**
	 * returns partition's text from it's beginning till <b>offset</b>
	 * @param offset Offset to return text before
	 * @return text
	 */
	public String getTextUpToOffset(int offset)
	{
		if (offset < this.offset || offset > this.offset + this.length)
			throw new RuntimeException("Invalid getTextFromOffset argument - "
					+ offset);
		try
		{
			return document.get(this.offset, offset - this.offset);
		} catch (Exception e)
		{
			Logger.log(e);
		}
		return null;
	}

	/**
	 * Return partition text from specific region
	 * @param offset region offset
	 * @param length region length
	 * @return text
	 */
	public String getTextRegion(int offset, int length)
	{
		if (offset < this.offset || offset >= this.offset + this.length
				|| length <= 0) // TODO Maybe = here
			throw new RuntimeException("Invalid getTextFromOffset argument - "
					+ offset);
		try
		{
			return document.get(offset, length);
		} catch (Exception e)
		{
			Logger.log(e);
		}
		return null;

	}

	/**
	 * @return partition color RGB
	 */
	public RGB getColorRGB()
	{
		return colorRGB;
	}

	/**
	 * @return partition bg color RGB
	 */
	public RGB getBgColorRGB()
	{
		return bgColorRGB;
	}

	/**
	 * @return has this partition Underlined attribute?
	 */
	public boolean isUnderlined()
	{
		if ((mask & FontStyle.UNDERLINED) > 0)
			return true;
		return false;
	}

	/**
	 * @return has this partition Strikethrough attribute?
	 */
	public boolean isStrikethrough()
	{
		if ((mask & FontStyle.STRIKETHROUGH) > 0)
			return true;
		return false;
	}

	
	/**
	 * Returns partition style-matching style range for it's displaying
	 * @param manager AbstractLayerManager responsible for partition handling
	 * @return new StyleRange
	 */
	public StyleRange getStyleRange(AbstractLayerManager manager)
	{

		StyleRange styleRange = new StyleRange();
		styleRange.start = this.getOffset();
		styleRange.length = this.getLength();
		styleRange.underline = this.isUnderlined();
		styleRange.strikeout = this.isStrikethrough();
		final IFontStyleManager fontStyleManager = manager.getFontStyleManager();
		styleRange.font = fontStyleManager.getFontForPartition(this);
		if ((mask&FontStyle.ITALIC)!=0){
			styleRange.fontStyle|=FontStyle.ITALIC;
		}
		if ((mask&FontStyle.BOLD)!=0){
			styleRange.fontStyle|=FontStyle.BOLD;
		}
		int height = (int) styleRange.font.getHeight();
		if (this.isSub()) styleRange.rise = -height/2;
		else if (this.isSup()) styleRange.rise = height/2;
		
		if (this.colorRGB != null)
		{
			styleRange.foreground =
					this.colorRGB;
		}
		if (this.bgColorRGB != null)
		{
			styleRange.background =bgColorRGB;
		}
		//refreshVisibleState = true;
		return styleRange;
	}

	/**
	 * Returns new Color object (meaning system color, with handle) for this partition's color
	 * @param manager AbstractLayerManager responsible for partition handling
	 * @return Color object
	 */
	public Color getColor(AbstractLayerManager manager)
	{
		
		return colorRGB;
	}
	
	/**
	 * Returns new Color object (meaning system color, with handle) for this partition's bgcolor
	 * @param manager AbstractLayerManager responsible for partition handling
	 * @return Color object
	 */
	public Color getBgColor(AbstractLayerManager manager)
	{
		return bgColorRGB;
	}
	
	/**
	 * @param color new fg color RGB
	 */
	public void setColorRGB(RGB color)
	{
		this.colorRGB = color;
	}

	/**
	 * @param bgColor new bg color RGB
	 */
	public void setBgColorRGB(RGB bgColor)
	{
		this.bgColorRGB = bgColor;
	}
	
	
	/** 
	 * Creates and returns a copy of this object. 
	 * @return object's clone
	 */
	public BasePartition clone(){
		try {
			BasePartition clone = (BasePartition) super.clone();
			if (clone.colorRGB!=null){
				clone.colorRGB=new RGB(clone.colorRGB.red,clone.colorRGB.green,clone.colorRGB.blue);
			}
			if (clone.bgColorRGB!=null){
				clone.bgColorRGB=new RGB(clone.bgColorRGB.red,clone.bgColorRGB.green,clone.bgColorRGB.blue);
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("Should never happen");
		}		
	}

	/**
	 * @see com.onpositive.richtext.model.IPartition#getData()
	 */
	public Object getData() {
		return data;
	}

	/**
	 * @see com.onpositive.richtext.model.IPartition#setData(java.lang.Object)
	 */
	public void setData(Object data) {
		this.data=data;
	}

	public ITextDocument getDocument() {
		return document;
	}
	
	public synchronized void setAdditionalData(String key, Object value)
	{
		if (additionalData == null)
			additionalData = new HashMap<String, Object>();
		additionalData.put(key,value);
	}

	public Map<String,Object> getAdditionalData()
	{
		return additionalData;
	}
	
	public Object getAdditionalData(String key)
	{
		if (additionalData == null)
			return null;
		return (additionalData.get(key));
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	/**
	 * @param document the document to set
	 */
	public void setDocument(ITextDocument document)
	{
		this.document = document;
	}
	
}
