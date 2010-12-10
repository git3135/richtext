package com.onpositive.richtexteditor.io;

import java.util.HashMap;
import java.util.HashSet;


/**
 * Provider for some textual format tokens to be used in parsers/serializers
 * @author 32kda
 * (c) Made in USSR
 */
public class TokenProvider
{

	protected HashMap<Integer, String> keywords;
	protected HashSet<Character> keySymbols;

	public void addKeyword(int keyConstant, String string)
	{
		keywords.put(keyConstant, string);
		string = string.trim();
		if (string.length() > 0)
		{
			keySymbols.add(string.trim().charAt(0));
		}
	}

	/**
	 * @return the keywords
	 */
	public HashMap<Integer, String> getKeywords()
	{
		return keywords;
	}

	public String getKeyword(int keyConstant)
	{
		return keywords.get(keyConstant);
	}

	/**
	 * @return the keySymbols
	 */
	public HashSet<Character> getKeySymbols()
	{
		return keySymbols;
	}
	
	public char[] getFilenameSpecialSymbols()
	{
		return null;
		
	}

}
