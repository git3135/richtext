package com.onpositive.richtexteditor.wikitext.parser;

/**
 * Should be used for mathing html tag
 * @author 32kda
 *
 */
public class TagBracketMatcher extends SimpleBracketMatcher
{

	public TagBracketMatcher(String tagName,  int returnType, String name, boolean includeMarkup)
	{
		super("<" + tagName, "</" + tagName, returnType, name, includeMarkup);
	}

	public TagBracketMatcher(String tagName,  int returnType, String name)
	{
		super("<" + tagName, "</" + tagName, returnType, name);
	}

	public TagBracketMatcher(String tagName, int returnType)
	{
		super("<" + tagName, "</" + tagName, returnType);
	}
	
	protected boolean checkStart(String text, int pos)
	{
		return text.charAt(pos) == '<';
	}
	
	
	public LineTextLexEvent match(String text, int pos)
	{
		if (!checkStart(text, pos))
			return null;
		if (pos + startingMarkupLength >= text.length())
			return null;
		if (startingMarkupLength > 1 && !(text.substring(pos, pos + startingMarkupLength).equalsIgnoreCase(startingMarkup))) //check tag name
			return null;
		int idx = pos + startingMarkupLength; //let's check, that tag is properly closed with '>'
		while (idx < text.length() && Character.isWhitespace(text.charAt(idx)))
			idx++;
		if (idx == text.length() || text.charAt(idx) != '>')
			return null;
		int contentStartIdx = idx + 1; 
		int endIdx = text.indexOf(endingMarkup, pos + startingMarkupLength);
		if (endIdx == -1)
			return null;
		idx = endIdx + endingMarkup.length(); //let's check, that tag is properly closed with '>'
		while (idx < text.length() && Character.isWhitespace(text.charAt(idx)))
			idx++;
		if (idx == text.length() || text.charAt(idx) != '>')
			return null;
		int tagEnd = idx + 1; 
		String content = text.substring(contentStartIdx, endIdx);
		LineTextLexEvent event = new LineTextLexEvent(content, returnType, pos);
		if (!includeMarkup)
			event = new LineTextLexEvent(content, returnType, pos);
		else
			event = new LineTextLexEvent(text.substring(pos, tagEnd), returnType, pos);
		event.setFullLength(tagEnd - pos); //to know full length of text sucessfully parsed
		return event;
	}
	
}
