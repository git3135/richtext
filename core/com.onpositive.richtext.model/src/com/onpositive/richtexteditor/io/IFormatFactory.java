package com.onpositive.richtexteditor.io;

import com.onpositive.richtexteditor.model.AbstractLayerManager;


public interface IFormatFactory
{
	public abstract ITextLoader getLoader(AbstractLayerManager manager);
	public abstract TextSerializer getSerializer(AbstractLayerManager manager);

}
