package com.onpositive.richtexteditor.wikitext.parser;

/**
 * Used to match CamelCase words
 * @author 32kda
 */
public class CamelCaseMatcher implements ISyntaxMatcher
{
	
	protected static final char RELATIONAL_LINK_PREFFIX = '#';
	protected int returnType;
	
	public CamelCaseMatcher(int returnType)
	{
		this.returnType = returnType;
	}

	public LineTextLexEvent match(String text, int pos)
	{
		if (!checkPrevChar(text,pos))
			return null;
		char startingChar = text.charAt(pos);
		if (!Character.isUpperCase(startingChar) && startingChar != RELATIONAL_LINK_PREFFIX)
			return null;
		int idx = pos + 1;
		int dividerCount = 0;
		while (idx < text.length() && (Character.isJavaIdentifierPart(text.charAt(idx)) || text.charAt(idx) == '#'))
		{
			if (text.charAt(idx) == '#')
				dividerCount++;
			if (dividerCount > 1)
				break;
			idx++;
		}		
		String word = text.substring(pos, idx);
		
		if (isCamelCaseWord(word))
			return new LineTextLexEvent(word, returnType, pos);
		return null;
	}
	
	/**
	 * Used to check previous char for availability. We should do this to prevent "matching"
	 * CamelCase in the middle of some words, like reStructuredText, which is not actually CamelCase
	 * @param text Text to check in, usually - current line text
	 * @param pos pos mathching starts from. Ususally, pos-1'th symbol should be checked to be non-letter and non-digit
	 * @return <code>true</code> it we have suitable prev character, <code>false</code> otherwise
	 */
	protected boolean checkPrevChar(String text, int pos)
	{
		if (pos == 0)
			return true;
		else
		{
			char prev = text.charAt(pos - 1);
			if (Character.isLetterOrDigit(prev) || prev == RELATIONAL_LINK_PREFFIX)
				return false;
		}
		return true;
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

}
