package com.onpositive.richtexteditor.wikitext.tests;

import org.eclipse.swt.widgets.Shell;


public class ShellProvider
{
	protected static ShellProvider instance = null;	
	
	public static ShellProvider getInstance()
	{
		if (instance == null)
		{
			instance = new ShellProvider();
		}
		return instance;
	}
	
	//--------------------------------------------------------------
	
	protected Shell shell = null;
	
	protected ShellProvider()
	{		
		
	}
	
	
	 
}
