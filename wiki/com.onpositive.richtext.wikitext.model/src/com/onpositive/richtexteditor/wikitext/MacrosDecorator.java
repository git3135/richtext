package com.onpositive.richtexteditor.wikitext;

import com.onpositive.richtext.model.meta.Color;
import com.onpositive.richtext.model.meta.RGB;
import com.onpositive.richtext.model.meta.StyleRange;
import com.onpositive.richtexteditor.model.SimpleTextDecorator;

public class MacrosDecorator extends SimpleTextDecorator
{
	protected static final String MACROS_START_MARKER = "[[";
	protected static final String MACROS_END_MARKER = "]]";
	protected Color green;
	
	public MacrosDecorator()
	{
		green =  new RGB(70,70,120);
	}

	protected void decorateStyleRange(StyleRange decoratedRange)
	{
		
		decoratedRange.foreground = green;
	}

	protected int getDecoratedAreaTailLength(int base, String text, StyleRange styleRange, int basePartEndOffset)
	{		
		return base + lastDecorationOffset;
	}

	protected int getMinimumDecorationLength()
	{
		return MACROS_START_MARKER.length() + MACROS_END_MARKER.length();
	}

	protected int getNextDecorationOffset(String text)
	{
		int indexOf = text.indexOf(MACROS_START_MARKER, lastDecorationOffset);	
		int endIndex = text.indexOf(MACROS_END_MARKER, indexOf);
		if (indexOf > -1 && endIndex > -1)
		{
			lastDecorationOffset = endIndex + MACROS_END_MARKER.length();
			return indexOf;
		}
		return -1;
		
	}

}
