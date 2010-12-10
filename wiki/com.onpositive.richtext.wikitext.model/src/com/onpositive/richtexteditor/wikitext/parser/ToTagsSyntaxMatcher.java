package com.onpositive.richtexteditor.wikitext.parser;

public class ToTagsSyntaxMatcher extends BasicSyntaxMatcher {
	/**
	 * Markup string to match. E.g. \<b\> for html bold tag
	 */
	protected String markupString;
	protected String markupString1;

	public LineTextLexEvent match(String text, int pos) {
		if (!checkStart(text, pos))
			return null;

		if (pos + markupString.length() <= text.length()) {
			if (text.substring(pos, pos + markupString.length()).equalsIgnoreCase(
					markupString))
				return new LineTextLexEvent(markupString, returnType, pos);
		}
		if (pos + markupString1.length() <= text.length()) {
			if (text.substring(pos, pos + markupString1.length()).equalsIgnoreCase(
					markupString1))
				return new LineTextLexEvent(markupString1, returnType, pos);
		}
		return null;
	}

	protected boolean checkStart(String text, int pos) {
		return super.checkStart(text, pos)
				&& (pos + markupString.length() <= text.length());
	}

	/**
	 * Creates a {@link ToTagsSyntaxMatcher} using given params
	 * 
	 * @param returnType
	 *            int constant corresponding to this token/marker type. It
	 *            should be returned in events for matched markup
	 * @param markupString
	 *            String, identifying marker itself
	 */
	public ToTagsSyntaxMatcher(int returnType, String markupString,
			String markupString1, String name) {
		super(returnType, name);
		if (markupString == null || markupString.length() == 0)
			throw new IllegalArgumentException(
					"markupString should be non-empty string");
		this.markupString = markupString;
		this.markupString1 = markupString1;
		acquireStartingChar(markupString);
	}

}
