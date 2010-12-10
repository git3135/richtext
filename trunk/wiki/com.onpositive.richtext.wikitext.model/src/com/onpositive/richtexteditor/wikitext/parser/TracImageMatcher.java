package com.onpositive.richtexteditor.wikitext.parser;

public class TracImageMatcher extends SimpleBracketMatcher
{
	protected static final String startingMarkup = "[[";
	protected static final String endingMarkup = "]]";
	protected static final String imageStartMarkup = "image(";
	protected static final String imageEndMarkup = ")";

	public TracImageMatcher(int returnType)
	{
		super(startingMarkup, endingMarkup, returnType, "image");
	}

	
	public LineTextLexEvent match(String text, int pos)
	{
		if (!checkStart(text, pos))
			return null;
		int endIdx = text.indexOf(endingMarkup, pos + startingMarkupLength);
		if (endIdx == -1)
			return null;
		String imageString = text.substring(pos + startingMarkupLength, endIdx).trim();
		if (imageString.toLowerCase().startsWith(imageStartMarkup) && imageString.endsWith(imageEndMarkup))
		{
			String imageRef = imageString.substring(imageStartMarkup.length(), imageString.length() - imageEndMarkup.length());
			LineTextLexEvent event = new LineTextLexEvent(imageRef, returnType, pos);
			event.setFullLength(endIdx + endingMarkup.length() -  pos);
			return event;
		}
		return null;
	}


}
