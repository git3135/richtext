package com.onpositive.richtexteditor.wikitext.io;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Iterator;

import com.onpositive.richtexteditor.io.LexEvent;
import com.onpositive.richtexteditor.io.html_scaner.EOFEvent;
import com.onpositive.richtexteditor.io.html_scaner.ILexListener;


public abstract class TextScanner
{
	
	protected HashSet<ILexListener> listeners = new HashSet<ILexListener>();
	protected boolean textScanned;
	
	public void addLexListener(ILexListener listener)
	{
		listeners.add(listener);		
	}

	public void removeLexListener(ILexListener listener)
	{
		listeners.remove(listener);		
	}

	/**
	 * @return the textScanned
	 */
	public boolean isTextScanned()
	{
		return textScanned;
	}
	
	protected void handleLexEvent(LexEvent event)
	{
		for (Iterator<ILexListener> iterator = listeners.iterator(); iterator.hasNext();)
		{
			ILexListener listener = (ILexListener) iterator.next();
			listener.handleLexEvent(event);
		}
	}

}
