package com.onpositive.richtexteditor.wikitext.io;

import java.util.HashMap;
import java.util.HashSet;

import com.onpositive.richtexteditor.io.TokenProvider;


public class WikitextTokenProvider extends TokenProvider
{
	public static final int WIKITEXT_REGION_START = 1;
	public static final int WIKITEXT_REGION_END = 2;
	public static final int WIKITEXT_UNDERLINE_TOKEN = 3;
	public static final int WIKITEXT_STRIKE_TOKEN = 4;
	public static final int WIKITEXT_ITALIC_QUOTAS = 5;
	public static final int WIKITEXT_BOLD_QUOTAS = 6;
	public static final int WIKITEXT_TABLE_DIVIDER = 7;
	public static final int WIKITEXT_DEF_LIST_DIVIDER = 8;
	public static final int WIKITEXT_HR_STR = 10;
	public static final int WIKITEXT_LINK_START = 11;
	public static final int WIKITEXT_LINK_END = 12;
	public static final int WIKITEXT_ESCAPE_NEXT = 13; //Symbol for escaping (no preprocessing) next markup tag/sequence. Here !
	public static final int WIKITEXT_ESCAPE_QUOTE = 14; //Qutas, which indicates, that all symbols inside should be escaped
	public static final int WIKITEXT_NUMBERED_LIST_MARKER = 15;
	public static final int WIKITEXT_BULLETED_LIST_MARKER = 16;
	public static final int WIKITEXT_IMG_TAG_START = 17;
	public static final int WIKITEXT_IMG_TAG_END = 18;
	public static final int WIKITEXT_SUBSCRIPT = 19;
	public static final int WIKITEXT_SUPERSCRIPT = 20;
	public static final int WIKITEXT_LETTER_LIST_MARKER = 21;
	public static final int WIKITEXT_ROMAN_STYLE_LIST_MARKER = 22;
	public static final int WIKITEXT_H1_STR = 23;
	public static final int WIKITEXT_H2_STR = 24;	
	public static final int WIKITEXT_H3_STR = 25;
	public static final int WIKITEXT_H4_STR = 26;
	public static final int WIKITEXT_H5_STR = 27;
	public static final int WIKITEXT_H6_STR = 28;
	public static final int WIKITEXT_EXPLICIT_ID_STARTER = 29;
	public static final int WIKITEXT_CITATION_MARKUP = 30;
	
	public static final int LAST_TOKEN_CONST_INDEX = 30;
	public static final int MIN_HEADER_CONST = WIKITEXT_H1_STR;
	public static final int MAX_HEADER_CONST = WIKITEXT_H6_STR;
	
	public static final int WIKI_LINK_PREFFIX = 60;
	public static final int HTTP_LINK_PREFFIX = 61;	
	public static final int ATTACHMENT_LINK_PREFFIX = 62;
	
	public static final String REGION_CONTENT_TYPE_PREFFIX = "#!";
	public static final int COMMENT_LINK_PREFFIX = 63;
	public static final int TICKET_LINK_PREFFIX = 64;
	
	protected static WikitextTokenProvider instance = null;
	 
	public static WikitextTokenProvider getInstance()
	{
		if (instance == null) 
			instance = new WikitextTokenProvider();
		return instance;
	}
	
	//------------------------------Static part end----------------------------------
	
	protected char[] specialFilenameSymbols = new char[]{'\\','/','?','.'};
	
	protected WikitextTokenProvider()
	{
		keywords = new HashMap<Integer, String>(LAST_TOKEN_CONST_INDEX * 2);
		keySymbols = new HashSet<Character>(LAST_TOKEN_CONST_INDEX * 2);
		addKeyword(WIKITEXT_REGION_START, "{{{");
		addKeyword(WIKITEXT_REGION_END, "}}}");
		addKeyword(WIKITEXT_UNDERLINE_TOKEN, "__");
		addKeyword(WIKITEXT_STRIKE_TOKEN, "~~");
		addKeyword(WIKITEXT_BOLD_QUOTAS, "'''");
		addKeyword(WIKITEXT_ITALIC_QUOTAS, "''");
		addKeyword(WIKITEXT_H1_STR, " =");
		addKeyword(WIKITEXT_H2_STR, " ==");
		addKeyword(WIKITEXT_H3_STR, " ===");
		addKeyword(WIKITEXT_H4_STR, " ====");
		addKeyword(WIKITEXT_H5_STR, " =====");
		addKeyword(WIKITEXT_H6_STR, " ======");
		addKeyword(WIKITEXT_HR_STR, "----");
		addKeyword(WIKITEXT_LINK_START, "[");
		addKeyword(WIKITEXT_LINK_END, "]");
		addKeyword(WIKITEXT_ESCAPE_NEXT, "!");
		addKeyword(WIKITEXT_ESCAPE_QUOTE, "`");
		addKeyword(WIKITEXT_BULLETED_LIST_MARKER, " * ");
		addKeyword(WIKITEXT_NUMBERED_LIST_MARKER, " 1. ");
		addKeyword(WIKITEXT_LETTER_LIST_MARKER, " a. ");
		addKeyword(WIKITEXT_ROMAN_STYLE_LIST_MARKER, " i. ");
		addKeyword(WIKITEXT_IMG_TAG_START, "[[");
		addKeyword(WIKITEXT_IMG_TAG_END, "]]");
		addKeyword(WIKITEXT_SUBSCRIPT, ",,");
		addKeyword(WIKITEXT_SUPERSCRIPT, "^");
		addKeyword(WIKITEXT_TABLE_DIVIDER, "||");
		addKeyword(WIKITEXT_DEF_LIST_DIVIDER, ":: ");
		addKeyword(WIKITEXT_EXPLICIT_ID_STARTER, "#");
		addKeyword(WIKITEXT_CITATION_MARKUP, ">");
		addKeyword(WIKI_LINK_PREFFIX, "wiki:");
		addKeyword(COMMENT_LINK_PREFFIX, "comment:");
		addKeyword(TICKET_LINK_PREFFIX, "ticket:");
		addKeyword(HTTP_LINK_PREFFIX, "http://");
		addKeyword(ATTACHMENT_LINK_PREFFIX, "attachment:");
	}


	public boolean isSpecialString(String serialized)
	{
		final String trim = serialized.trim();
		serialized = partiallyDeleteLeadingSpaces(serialized);
		if (!WikitextLinesScanner.tryToMatchBullet(serialized.trim()).equals(""))
			return true;
		if (trim.equals(getKeyword(WIKITEXT_HR_STR)))
			return true;
		if (trim.startsWith(getKeyword(WIKITEXT_REGION_START)) && trim.endsWith(getKeyword(WIKITEXT_REGION_END)) && trim.indexOf('\n') > 0)
			return true;
		if (trim.startsWith(getKeyword(WIKITEXT_CITATION_MARKUP)))
			return true;
		if (trim.length() == 0)
			return true;
		StringBuilder headerToken = new StringBuilder(getKeyword(WIKITEXT_H1_STR).trim());
		for (int i = MIN_HEADER_CONST; i <= MAX_HEADER_CONST; i++)
		{
			final String curHeaderToken = headerToken.toString();
			if (trim.startsWith(curHeaderToken + ' ') && trim.endsWith(' ' + curHeaderToken))
				return true;
			headerToken.append('=');
		}		
		return false;
	}

	/**
	 * Deletes all leading spaces, except one
	 * @param serialized String to process
	 * @return processed str
	 */
	protected String partiallyDeleteLeadingSpaces(String serialized)
	{
		int  i = 0;
		while (i<serialized.length()&&serialized.charAt(i) == ' ') i++;
		if (i > 0)
		{
			i--;
			return serialized.substring(i);
		}
		return serialized;
	}
	
	public static boolean isCamelCaseWord(String word)	
	{
		if (word.length()==0){
			return false;
		}
		if (word.charAt(0)=='#'){
			String substring = word.substring(1);
			try{
				Integer.parseInt(substring);
				return true;
			}catch (NumberFormatException e) {
				return false;
			}
		}
		final int length = word.length();
		int upperCaseLettersCount = 0;
		int dividerCount = 0;
		boolean isCamelCase = true;
		for (int i = 0; i < length && isCamelCase; i++)
		{
			final char charAt = word.charAt(i);
			if (i == 0 && !Character.isUpperCase(charAt))
				return false;
			if (charAt == ':' || charAt == '#')
			{
				if (dividerCount == 1)
				{
					return false;
				}
				if (i >= length - 1 || !Character.isUpperCase(word.charAt(i + 1)))				
					isCamelCase = false;				
				else
					dividerCount++;
			}
			else if (Character.isUpperCase(charAt))
			{
				if (i >= length - 1 || !Character.isLowerCase(word.charAt(i + 1)))
					isCamelCase = false;
				else 
					upperCaseLettersCount++;
			}
			else if (!Character.isLetter(charAt) && !Character.isDigit(charAt))
				return false;
		}
		if (isCamelCase && upperCaseLettersCount >= 2 && dividerCount < 2)
			return true;
		return false;
	}
	
	public char[] getFilenameSpecialSymbols()
	{
		return specialFilenameSymbols;
	}
	
}
