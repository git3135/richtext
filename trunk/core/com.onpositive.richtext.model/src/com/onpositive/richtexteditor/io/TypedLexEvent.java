package com.onpositive.richtexteditor.io;

/**
 * 
 * @author 32kda
 * LexEvent with String and type of event
 */
public class TypedLexEvent extends LexEvent
{
	protected int type;
	
	public static final int NONE_TYPE = 0;
	
	/**
	 * @return tag type constant
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * @param type tag type constant to set
	 */
	public void setType(int type)
	{
		this.type = type;
	}

	
	
	/**
	 * Constructor
	 * @param l lex word
	 * @param type tag type constant
	 */
	public TypedLexEvent(String text, int type)
	{
		super(text);
		this.type = type;
	}

	public TypedLexEvent()
	{
		type = NONE_TYPE;
	}
	
	public String toString()
	{
		return "Event: " + text + " Type: " + type;
	}
	

}
