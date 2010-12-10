package com.onpositive.richtexteditor.wikitext.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HrSpecialMarkupScanner implements ISpecialMarkupScanner
{
	/**
	 * Horizontal line list marker regex
	 */	
	protected static String HR_MARKER = "-{4,}";

	public WikitextLine tryToScanMarkup(String line)
	{
		Matcher matcher = Pattern.compile(HR_MARKER).matcher(line);
		if (matcher.find() && matcher.start() == 0)
		{
			return new WikitextLine(WikitextLine.HR,line.substring(matcher.start(),matcher.end()).trim(),0,null);
		}
		return null;
	}

}
