package com.onpositive.richtexteditor.wikitext.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matcher for matching < hr > tag
 * @author 32kda
 *
 */
public class HtmlHrSpecialMarkupScanner implements ISpecialMarkupScanner
{
	protected static final String HR_TAG_REGEX = "<\\s*[hH][rR]\\s*>";
	
	protected static Pattern hrPattern = Pattern.compile(HR_TAG_REGEX);
	
	public WikitextLine tryToScanMarkup(String line)
	{
		int indent = ParserUtil.getLineIndent(line);
		Matcher matcher = hrPattern.matcher(line);
		if (matcher.find(indent) && matcher.start() == indent)
		{
			WikitextLine hrLine = new WikitextLine(WikitextLine.HR,line.substring(matcher.start(),matcher.end()));
			hrLine.setFullParsedLength(matcher.end());
			return hrLine;
		}
		return null;
	}

}
