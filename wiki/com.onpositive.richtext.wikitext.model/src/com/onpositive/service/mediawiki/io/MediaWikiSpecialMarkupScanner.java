package com.onpositive.service.mediawiki.io;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.onpositive.richtexteditor.wikitext.parser.HeaderLine;
import com.onpositive.richtexteditor.wikitext.parser.ISpecialMarkupScanner;
import com.onpositive.richtexteditor.wikitext.parser.ParserUtil;
import com.onpositive.richtexteditor.wikitext.parser.WikitextLine;

public class MediaWikiSpecialMarkupScanner implements ISpecialMarkupScanner
{

	protected static final char HEADER_MARKER_CHAR = '=';
	protected static final String COMMENT_START = "<!--";
	protected static final String COMMENT_END= "-->";
	protected static final String HEADER_TAG_REGEX = "<\\s*[hH]\\d\\s*>";
	protected static final String HEADER_CLOSE_TAG_REGEX = "<\\s*[hH]\\d\\s*/>";
	
	protected static Pattern headerStartPattern = Pattern.compile(HEADER_TAG_REGEX);
	protected static Pattern headerEndPattern = Pattern.compile(HEADER_CLOSE_TAG_REGEX);

	
	public WikitextLine tryToScanMarkup(String line)
	{
		//int fullLength = line.length();
		int lineIndent = ParserUtil.getLineIndent(line);
		String leftTrim = line.substring(lineIndent);
		//Try to match header
		int count = ParserUtil.countChars(leftTrim,HEADER_MARKER_CHAR,0);
		if (count > 0 && leftTrim.length() >= count * 2) 
		{
			String headerString = leftTrim.substring(0,count);
			int lastIdx = leftTrim.lastIndexOf(headerString);
			if (lastIdx > 0)
			{
				HeaderLine headerLine = new HeaderLine(WikitextLine.HEADER,leftTrim.substring(count, lastIdx).trim(),count);
				headerLine.setFullParsedLength(lineIndent + lastIdx + headerString.length());
				return headerLine;
			}
		}
		//Try to match tagged header, like <h1> Header 1 </h1>
		Matcher startMatcher = headerStartPattern.matcher(leftTrim);
		if (startMatcher.find() && startMatcher.start() == 0)
		{			
			String tag = leftTrim.substring(0, startMatcher.end());
			int headerLevel = extractHeaderLevel(tag);
			Matcher endMatcher = headerEndPattern.matcher(leftTrim);
			if (headerLevel > -1 && endMatcher.find(tag.length()))
			{
				String endTag = leftTrim.substring(endMatcher.start(), endMatcher.end());
				if (extractHeaderLevel(endTag) == headerLevel)
				{
					HeaderLine headerLine = new HeaderLine(WikitextLine.HEADER,leftTrim.substring(startMatcher.end(), endMatcher.start()).trim(),headerLevel);
					headerLine.setFullParsedLength(lineIndent + endMatcher.end());
					return headerLine;

				}
			}
		}
		
		//Try to match comment
		if (leftTrim.startsWith(COMMENT_START))
		{
			int lastIdx = leftTrim.indexOf(COMMENT_END);
			if (lastIdx > -1)
			{
				WikitextLine parsedLine = new WikitextLine(WikitextLine.SEPARATE_LINE, line);
				return parsedLine;
			}
		}
		return null;
	}

	protected int extractHeaderLevel(String tag)
	{
		int headerLevel = -1;
		for (int i = 2; i < tag.length(); i++) //2 because header level digit can be after at least 2 chars, <h
		{
			if (Character.isDigit(tag.charAt(i)))
			{
				headerLevel = Character.digit(tag.charAt(i),10);
				break;
			}
		}
		return headerLevel;
	}

}
