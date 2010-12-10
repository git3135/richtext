package com.onpositive.richtext.model;

import com.onpositive.richtext.model.meta.ITextDocument;


/**
 * @author 32kda
 * Such partition can have different styles and can be 
 * displayed differently according to them
 */
public class StylePartition extends BasePartition implements IStylePartition
{
	protected int style;

	public StylePartition(ITextDocument document, int offset, int length)
	{
		super(document, offset, length);
	}

	public StylePartition(ITextDocument document, int offset, int length, int style)
	{
		super(document, offset, length);
		this.style = style;
	}

	/* (non-Javadoc)
	 * @see com.onpositive.richtexteditor.model.partitions.IStylePartition#getStyle()
	 */
	public int getStyle()
	{
		return style;
	}

	/* (non-Javadoc)
	 * @see com.onpositive.richtexteditor.model.partitions.IStylePartition#setStyle(int)
	 */
	public boolean setStyle(int style)
	{
		boolean r=this.style==style;
		this.style = style;
		return r;
	}
	
	public void applyAttributes(BasePartition oldPartition)
	{
		super.applyAttributes(oldPartition);
		if (oldPartition instanceof StylePartition)
		{
			style = ((StylePartition)oldPartition).style;
		}
	}
	
	public BasePartition extractLeftPartitionWithStyle(int divisionOffset)
	{
		StylePartition leftPartition = (StylePartition) super.extractLeftPartitionWithStyle(divisionOffset);
		leftPartition.style = style;
		return leftPartition;
	}
	

}
