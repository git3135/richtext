package com.onpositive.richtexteditor.io;

import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtext.model.meta.Point;
import com.onpositive.richtexteditor.model.AbstractLayerManager;

public class SimpleTextSerializer implements ITextSerilizer
{
	AbstractLayerManager manager;
	
	public SimpleTextSerializer(AbstractLayerManager manager)
	{
		this.manager = manager;
	}

	public String getSerializedLine(int lineNum)
	{
		ITextDocument document = manager.getDocument();
		if (lineNum < 0 || lineNum >= document.getNumberOfLines())
			throw new IndexOutOfBoundsException(lineNum + "is not a valid line number");
		int lineOffset = document.getLineOffset(lineNum);
		int lineLength = document.getLineLength(lineNum);
		return document.get(lineOffset, lineLength);
	}

	public String serializeAllToStr()
	{
		ITextDocument document = manager.getDocument();
		return document.get();
	}

	public String serializeToStr(Point selection) throws Exception
	{
		ITextDocument document = manager.getDocument();
		if (selection.x < 0 || selection.x > document.getLength() || selection.y < selection.x || selection.y > document.getLength())
			throw new IndexOutOfBoundsException("invalid selection: " + selection.toString());
		return document.get(selection.x, selection.y);
	}

	
}
