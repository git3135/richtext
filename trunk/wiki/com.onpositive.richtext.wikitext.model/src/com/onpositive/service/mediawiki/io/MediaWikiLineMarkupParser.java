package com.onpositive.service.mediawiki.io;

import com.onpositive.richtexteditor.wikitext.parser.DefaultLineMarkupParser;
import com.onpositive.richtexteditor.wikitext.parser.HrSpecialMarkupScanner;
import com.onpositive.richtexteditor.wikitext.parser.HtmlHrSpecialMarkupScanner;
import com.onpositive.richtexteditor.wikitext.parser.ModelBullet;
import com.onpositive.richtexteditor.wikitext.parser.OrderedListBullet;
import com.onpositive.richtexteditor.wikitext.parser.WikitextLine;

public class MediaWikiLineMarkupParser extends DefaultLineMarkupParser
{
	protected static final char MEDIAWIKI_BULLET_MARKER = '*';
	protected static final char MEDIAWIKI_NUMBER_MARKER = '#';

	protected void createSpecialMarkupScanners()
	{
		addSpecialMarkupScanner(new MediaWikiSpecialMarkupScanner());
		addSpecialMarkupScanner(new HrSpecialMarkupScanner());
		addSpecialMarkupScanner(new HtmlHrSpecialMarkupScanner());
	}

	protected ModelBullet tryToScanBulletText(String line)
	{
		if (line.charAt(0) == MEDIAWIKI_BULLET_MARKER || line.charAt(0) == MEDIAWIKI_NUMBER_MARKER)
		{
			if (getLastMarkerChar(line) == MEDIAWIKI_BULLET_MARKER)
				return new ModelBullet(line.substring(0, getBulletTailLength(line, MEDIAWIKI_BULLET_MARKER)));
			if (getLastMarkerChar(line) == MEDIAWIKI_NUMBER_MARKER)
				return new OrderedListBullet(OrderedListBullet.NUMBER, line.substring(0, getBulletTailLength(line, MEDIAWIKI_NUMBER_MARKER))); // -1
		}
		return null;
	}
	
	protected char getLastMarkerChar(String line)
	{
		int idx = getLineIndent(line) - 1;
		return line.charAt(idx);
	}

	@Override
	public int getLineIndent(String line)
	{
		int count = 0;
		while (count<line.length()&& (line.charAt(count) == MEDIAWIKI_BULLET_MARKER || line.charAt(count) == MEDIAWIKI_NUMBER_MARKER))
		{			
			count++;
		}
		return count;
	}

	@Override
	public WikitextLine parseLine(String line)
	{
		WikitextLine specialMarkedLine = tryToScanSpecialMarkup(line);
		if (specialMarkedLine != null)
			return specialMarkedLine;
		if (line.startsWith(" "))
			return new WikitextLine(MediaWikiParserConstants.PREFORMATTED_LINE_TYPE, line);
		int indent = getLineIndent(line);
		int indent1=0;
		int a;
		for (a=0;a<line.length();a++){
			char c=line.charAt(a);
			if (Character.isWhitespace(c)){
				continue;
			}
			else if (c==':'){
				indent1++;
			}
			else{
				if (a>0){
					line=line.substring(a,line.length());
				}
				break;
			}
		}
		indent=Math.max(indent, indent1);
		ModelBullet bullet = tryToScanBulletText(line);
		indent = doIndentCorrection(bullet, indent, line);
		int idx = 0;
		if (bullet != null)
			idx = bullet.getBulletText().length();
		WikitextLine wikitextLine = new WikitextLine(WikitextLine.SIMPLE, line.substring(idx), indent, bullet);
		if (a>0){
		wikitextLine.setFullParsedLength(line.length()+a);
		}
		wikitextLine.setMarkupIndent(idx);
		return wikitextLine;
	}

	@Override
	protected int doIndentCorrection(ModelBullet bullet, int indent, String line)
	{
		return indent;
	}

	protected int getBulletTailLength(String line, char marker)
	{
		int count = 0;
		while (count<line.length()&&line.charAt(count) == marker)
			count++;
		while (count<line.length()&& Character.isWhitespace(line.charAt(count)))
			count++;
		return count;
	}

}
