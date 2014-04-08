package com.onpositive.service.mediawiki.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MediaWikiTokenProvider
{
	public static final String IMAGE = "image";
	public static final String CATEGORY = "category";
	public static final String BULLET = "*";
	public static final String NUMBER= "#";
	
	private static HashMap<String,List<String>> tokenMap = new HashMap<String, List<String>>();
	
	static
	{
		register(IMAGE, "image");
		register(IMAGE, "изображение");
		register(CATEGORY, "категория");
		register(BULLET, "*");
		register(NUMBER, "#");
	}
	
	protected static void register (String tokenType, String value)
	{
		List<String> list = tokenMap.get(tokenType);
		if (list == null)
		{
			list = new ArrayList<String>();
			tokenMap.put(tokenType,list);
		}
		list.add(value);
	}
	
	public static List<String> getTokens(String tokenType)
	{
		List<String> list = tokenMap.get(tokenType);		
		return list;
	}

}
