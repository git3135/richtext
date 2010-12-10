package com.onpositive.richtexteditor.wikitext.parser;

public abstract class BasicSyntaxMatcher implements ISyntaxMatcher
{

	/**
	 * Human-readable markup name. Useful only for debug/displaying purposes
	 */
	protected String name;
	/**
	 * Starting char. Not intended to be directly set, this should be always a first char from constructor
	 */
	protected char startingChar;
	/**
	 * int type for event/token to return, if match succeeded
	 */
	protected int returnType;

	public abstract LineTextLexEvent match(String text, int pos);

	protected void acquireStartingChar(String str)
	{
		this.startingChar = str.charAt(0);	
	}

	protected boolean checkStart(String text, int pos)
	{
		return (text.charAt(pos) == startingChar);
	}
	
	public BasicSyntaxMatcher(int returnType)
	{
		this.returnType = returnType;
	}
	
	public BasicSyntaxMatcher(int returnType, String name)
	{
		this(returnType);
		this.name = name;
	}
	
	
	public String toString()
	{	
		return "{Matcher: "  + ((name != null)?name:"unnamed") + " ret.type " + returnType + "}";
	}

}
