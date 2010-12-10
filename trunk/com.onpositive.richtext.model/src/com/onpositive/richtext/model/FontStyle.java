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

import com.onpositive.richtext.model.meta.RGB;
import com.onpositive.richtexteditor.model.IFontStyleManager;
import com.onpositive.richtexteditor.model.Logger;


/**
 * @author 32kda
 * Class for applyable font style information and handling
 */
public class FontStyle implements Cloneable
{
	
	/**
	 * 
	 */
	public static final int BOLD =1 << 0;
	/**
	 * 
	 */
	public static final int ITALIC = 1<<1;
	
	/**
	 * 
	 */
	public static final int UNDERLINED = 4;
	/**
	 * 
	 */
	public static final int STRIKETHROUGH = 8;
	/**
	 * 
	 */
	public static final int SUB = 16;
	/**
	 * 
	 */
	public static final int SUP = 32;
	/**
	 * 
	 */
	public static final String NORMAL_FONT_NAME = IFontStyleManager.NORMAL_FONT_NAME;
	/**
	 * 
	 */
	public static final String BOLD_PREFFIX = "bold_";
	/**
	 * 
	 */
	public static final String ITALIC_PREFFIX = "italic_";
	/**
	 * 
	 */
	public static final String UNDERLINED_PREFFIX = "underlined_";
	/**
	 * 
	 */
	public static final String BOLD_ITALIC_PREFFIX = "bold_italic_";	
	/**
	 * 
	 */
	public static final String SUB_PREFFIX = "sub_";
	/**
	 * 
	 */
	public static final String SUP_PREFFIX = "sup_";
	
	public static final int NORMAL = 0;
	public static final int NONE = 0;

	protected int mask;
	protected String fontDataName = null;
	protected String displayName = null;
	protected RGB color = null;
	protected RGB bgColor = null;
		
	
	/**
	 * Constructor by style bitmask
	 * @param mask bitmask
	 */
	public FontStyle(int mask)
	{
		this.mask=mask;
	}
	
	/**
	 * Constructor by style bitmask, Font Data Name and font display name
	 * @param mask bitmask
	 * @param fontDataName Font Data Name
	 * @param displayName font display name
	 */
	public FontStyle(int mask, String fontDataName, String displayName)
	{		
		
		this.mask=mask;
		this.fontDataName = fontDataName;
		this.displayName = displayName;
	}
	
	/**
	 * Constructor by style bitmask and color's RGB
	 * @param mask bitmask
	 * @param RGBColor foreground color
	 * 
	 */
	public FontStyle(int mask, RGB RGBColor)
	{
		this.color = RGBColor;
		this.mask=mask;
	}

	
	/**
	 * @return the color
	 */
	public RGB getColor()
	{
		return color;
	}

	
	/**
	 * @param color the color to set
	 */
	public void setColor(RGB color)
	{
		this.color = color;
	}

	/**
	 * Constructor for copying style from some partition
	 * @param partition Partition to copy style from
	 */
	public FontStyle(BasePartition partition) {
		this.mask=partition.getStyleMask();
		this.color=partition.getColorRGB();
		this.bgColor=partition.getBgColorRGB();
		this.fontDataName=partition.getFontDataName();
		
	}

	
	/**
	 * @param partition where to apply this style to 
	 */
	public void applyStyle(BasePartition partition) 
	{
		if ((mask & FontStyle.SUB) != 0) partition.setSup(false);
		if ((mask & FontStyle.SUP) != 0) partition.setSub(false);
		partition.setStyleMask(partition.getStyleMask() | mask);
		if (color != null) partition.setColorRGB(color);
		if (bgColor != null) partition.setBgColorRGB(bgColor);
		if (fontDataName != null) partition.setFontDataName(fontDataName);
	}

	/**
	 * @param partition where to set this style to 
	 */
	public void setStyle(BasePartition partition) 
	{
		partition.setStyleMask(mask);
		partition.setColorRGB(color);
		partition.setBgColorRGB(bgColor);
		partition.setFontDataName(fontDataName);
	}

	
	/**
	 * @param partition where to remove this style from 
	 */
	public void removeStyle(BasePartition partition) 
	{
		partition.setStyleMask(partition.getStyleMask() & ~mask);
		if (color != null) partition.setColorRGB(null);
		if (bgColor != null) partition.setBgColorRGB(null);
		if (fontDataName != null && fontDataName != NORMAL_FONT_NAME) partition.setFontDataName(NORMAL_FONT_NAME);
	}

	
	/**
	 * @param partition where to remove this style from 
	 * @return true, if this style is applied to partition
	 */
	public boolean isApplied(BasePartition partition)
	{
		if ((partition.getStyleMask() & mask) > 0) return true;
		return false;
	}

	/**
	 * @return Style display name
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	/**
	 * @param displayName display name of style
	 */
	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}
	
	/**
	 * @return font name
	 */
	public String getFontDataName()
	{
		return fontDataName;
	}

	/**
	 * @param fontDataName font name
	 */
	public void setFontDataName(String fontDataName)
	{
		this.fontDataName = fontDataName;
	}
	
	/**
	 * @return bg color
	 */
	public RGB getBgColor()
	{
		return bgColor;
	}
	
	/**
	 * @param bgColor background color
	 */
	public void setBgColor(RGB bgColor)
	{
		this.bgColor = bgColor;
	}	
	
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bgColor == null) ? 0 : bgColor.hashCode());
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result
				+ ((displayName == null) ? 0 : displayName.hashCode());
		result = prime * result
				+ ((fontDataName == null) ? 0 : fontDataName.hashCode());
		result = prime * result + mask;
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FontStyle other = (FontStyle) obj;
		if (bgColor == null) {
			if (other.bgColor != null)
				return false;
		} else if (!bgColor.equals(other.bgColor))
			return false;
		if (color == null) {
			if (other.color != null)
				return false;
		} else if (!color.equals(other.color))
			return false;
		if (displayName == null) {
			if (other.displayName != null)
				return false;
		} else if (!displayName.equals(other.displayName))
			return false;
		if (fontDataName == null) {
			if (other.fontDataName != null)
				return false;
		} else if (!fontDataName.equals(other.fontDataName))
			return false;
		if (mask != other.mask)
			return false;
		return true;
	}
	
	/** (non-Javadoc)
	 * @return cloned styles
	 * 
	 */
	
	public Object clone()
	{
		try
		{
			return super.clone();
		}
		catch (CloneNotSupportedException e) {
			Logger.log(e);
		}
		return null;
	}
	
	/**
	 * @return displayable name of style
	 */
	
	public String toString()
	{
		return displayName;
	}
	
	
}
