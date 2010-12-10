package com.onpositive.richtexteditor.model;


import com.onpositive.richtext.model.meta.RGB;
import com.onpositive.richtext.model.meta.StyleRange;

/**
 * This decorator is used for autodecorating hyperlinks, which begins with http:// and also provides decorating sample
 * @author 32kda
 * TODO Add more hyperlink preffixes - ftp://, https:// etc
 */
public class HyperlinksDecorator extends SimpleTextDecorator {

	/**
	 * Hyperlink data marker - we nned it for understanding, e.g., that mouse hovers over a hyperlink
	 */
	public static final String HYPERLINK_MARKER = "HYPERLINK";
	protected static final String HTTP_START_STR = "http:\\\\";
	protected static final String HTTP_START_STR2 = "http://";

	public static final String[]PREFS=new String[]{"http://","https://","ftp://","file://","ftps://"};
	
	protected int getNextDecorationOffset(String text)
	{
		int min=-1;
		for (String s:PREFS){
			int indexOf = text.indexOf(s, lastDecorationOffset);
			if (min==-1){
				min=indexOf;
			}
			else{
				if (indexOf!=-1){
					min=Math.min(indexOf, min);
				}
			}
		}
		if (min > -1)
			lastDecorationOffset = min + 3;
		return min;
	}

	protected void decorateStyleRange(StyleRange styleRange) {
		styleRange.underline = true;
		styleRange.foreground = new RGB(255,0,0);
		styleRange.data = HYPERLINK_MARKER;
	}

	protected int getMinimumDecorationLength() {
		return 3;
	}

	protected int getDecoratedAreaTailLength(int base, String text,
			StyleRange styleRange, int basePartEndOffset) {
		int k = basePartEndOffset;
		for (; k < styleRange.start + styleRange.length && !Character.isWhitespace(text.charAt(k - base));k++);
		//Check, whether link is terminated by ',' or '.'. If it's so, we'll need to exclude them from link's text  
		int end = k - 1;
		while(end > basePartEndOffset && (text.charAt(end - base) == '.' || text.charAt(end - base) == ','))
			end--;
		return end+1;
	}



}
