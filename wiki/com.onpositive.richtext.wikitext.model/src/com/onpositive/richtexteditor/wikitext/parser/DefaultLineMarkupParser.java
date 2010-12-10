package com.onpositive.richtexteditor.wikitext.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultLineMarkupParser implements ILineMarkupParser
{

	/**
	 * Bulleted(unordered) list marker
	 */
	protected static String BULLETED_LIST_MARKER = "* ";
	/**
	 * Different ordered list markers
	 */
	protected static String NUMBERED_ORDERED_LIST_MARKER = "[0-9]+\\u002E "; //\u002E means '.'
	protected static String ABC_ORDERED_LIST_MARKER = "[a-z]\\u002E ";
	protected static String ROMAN_ORDERED_LIST_MARKER = "[xiv]+\\u002E ";
	
	protected int lastScannedBulletType; 
	
	protected List<ISpecialMarkupScanner> specialMarkupScanners = new ArrayList<ISpecialMarkupScanner>();
	
	/**
	 * Constructs parser instance. Valid bullet and indents list should be passed
	 * @param bullets shared bullets list instance
	 * @param indents shared indents list instance
	 */
	public DefaultLineMarkupParser()
	{
		createSpecialMarkupScanners();
	}
	
	protected void createSpecialMarkupScanners()
	{
		addSpecialMarkupScanner(new HeaderSpecialMarkupScanner());
		addSpecialMarkupScanner(new HrSpecialMarkupScanner());
	}

	/**
	 * Tries to scan bullet text at the beginning of the line specified
	 * @param line Line to scan for bullet
	 * @return {@link ModelBullet} if some bullet sucessfully scanned, <code>null</code> otherwise
	 */
	protected ModelBullet tryToScanBulletText(String line)
	{
		int indent = getLineIndent(line);
		if (indent > 0)
		{
			if (line.indexOf(BULLETED_LIST_MARKER) == indent)
				return new ModelBullet(line.substring(indent - 1, indent - 1 + BULLETED_LIST_MARKER.length() + 1));
			Pattern pattern = Pattern.compile(NUMBERED_ORDERED_LIST_MARKER);
			Matcher matcher = pattern.matcher(line);
			if (matcher.find(indent) && matcher.start() == indent)
				return new OrderedListBullet(OrderedListBullet.NUMBER, line.substring(indent - 1, matcher.end())); //-1 for first, marking space, which doesn't mean indenting actually
			pattern = Pattern.compile(ROMAN_ORDERED_LIST_MARKER);
			matcher = pattern.matcher(line);
			if (matcher.find(indent) && matcher.start() == indent)
				return new OrderedListBullet(OrderedListBullet.ROMAN, line.substring(indent - 1, matcher.end()));
			pattern = Pattern.compile(ABC_ORDERED_LIST_MARKER);
			matcher = pattern.matcher(line);
			if (matcher.find(indent) && matcher.start() == indent)
				return new OrderedListBullet(OrderedListBullet.ABC, line.substring(indent - 1, matcher.end()));
			
		}
		return null;
		
	}

	public WikitextLine parseLine(String line)
	{
		WikitextLine specialMarkedLine = tryToScanSpecialMarkup(line);
		if (specialMarkedLine != null)
			return specialMarkedLine;
		int indent = getLineIndent(line);
		ModelBullet bullet = tryToScanBulletText(line);
		indent = doIndentCorrection(bullet, indent, line);
		int idx = Math.max(indent,0);
		if (bullet != null)
			idx += bullet.getBulletText().length();
		WikitextLine wikitextLine = new WikitextLine(WikitextLine.SIMPLE, line.substring(idx),indent,bullet);
		wikitextLine.setMarkupIndent(idx);
		return wikitextLine;
	}

	/**
	 * Sometimes, we need to perform some indent correction depending on bullet we have.
	 * E.g. MediaWiki markup '###' gives us 3rd indent level
	 * @param bullet Bullet for the line being corrected
	 * @param indent Indent for corecting
	 * @param line Line text
	 * @return corrected inent
	 * Can be overriden in subclasses
	 */
	protected int doIndentCorrection(ModelBullet bullet, int indent, String line)
	{
		if (bullet != null) //bec. bullet has 1 indent in WikiText
			indent--;
		return indent;
	}

	protected WikitextLine tryToScanSpecialMarkup(String line)
	{
		for (Iterator<ISpecialMarkupScanner> iterator = specialMarkupScanners.iterator(); iterator.hasNext();)
		{
			ISpecialMarkupScanner scanner = (ISpecialMarkupScanner) iterator.next();
			WikitextLine markedLine = scanner.tryToScanMarkup(line);
			if (markedLine != null)
				return markedLine;
		}
		return null;		
	}

	public int getLineIndent(String line)
	{
		return ParserUtil.getLineIndent(line);
	}

	public boolean hasBullet(String line)
	{
		return tryToScanBulletText(line) != null;
	}

	/**
	 * @param scanner
	 * @return
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean addSpecialMarkupScanner(ISpecialMarkupScanner scanner)
	{
		return specialMarkupScanners.add(scanner);
	}

	/**
	 * @param scanner
	 * @return
	 * @see java.util.List#remove(java.lang.Object)
	 */
	public boolean removeSpecialMarkupScanner(ISpecialMarkupScanner scanner)
	{
		return specialMarkupScanners.remove(scanner);
	}

}
