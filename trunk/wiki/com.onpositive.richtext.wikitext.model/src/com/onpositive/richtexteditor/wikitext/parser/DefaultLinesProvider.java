package com.onpositive.richtexteditor.wikitext.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DefaultLinesProvider implements ILinesProvider
{
	protected String text;
	protected ILineMarkupParser lineMarkupParser;
	protected IRegionParser regionParser;
	protected List <WikitextLine> lines;
	protected List <ModelBullet> bullets;
	protected List <Integer> indents;
	protected List <ISyntaxMatcher> specialMarkupMatchers;
	protected List <IRegionParser> regionParsers = new ArrayList<IRegionParser>();
	protected boolean canMergeLines = true;
	
	public DefaultLinesProvider(String initialText, ILineMarkupParser lineMarkupParser, boolean canMergeLines)
	{
		text = initialText.replaceAll("\r\n","\n").replaceAll("\n\r","\n");
		this.lineMarkupParser = lineMarkupParser;
		this.canMergeLines = canMergeLines;
		createLists();
		createRegionParsers();
		createSpecialMarkupMatchers();
		process();
	} 
	
	public DefaultLinesProvider(String initialText, boolean canMergeLines)
	{
		this(initialText, new DefaultLineMarkupParser(),canMergeLines);
	}
	
	
	

	protected void createSpecialMarkupMatchers()
	{
		specialMarkupMatchers.add(new CitationMarkupMatcher(LineTextLexEvent.CITATION,'>'));		
	}


	protected void createLists()
	{
		lines = new ArrayList<WikitextLine>();
		bullets = new ArrayList<ModelBullet>();
		indents = new ArrayList<Integer>();
		specialMarkupMatchers = new ArrayList<ISyntaxMatcher>();
	}


	protected void createRegionParsers()
	{
		addRegionParser(new DefaultRegionParser());
		addRegionParser(new DeflistRegionParser(LineTextLexEvent.DEFLIST));
		addRegionParser(new TableRegionParser(LineTextLexEvent.TABLE));
	}
	
	public void addRegionParser(IRegionParser parser)
	{
		regionParsers.add(parser);
	}
	
	public void removeRegionParser(IRegionParser parser)
	{
		regionParsers.remove(parser);
	}


	protected void process()
	{
		int pos = 0; 
		int next;
		boolean wasBreak = true;
		while(pos < text.length())
		{
			next = text.indexOf('\n',pos + 1);
			if (next == -1)
				next = text.length();
			if (next > pos)
			{
				String current;
				if (pos == 0)
					current = text.substring(pos, next);
				else
					current = text.substring(pos + 1, next);
				if (current.trim().length() == 0) //empty line
					wasBreak = true;
				else
				{
					int possibleOffset = (pos == 0)?pos:pos + 1; 
					WikitextLine region = tryToParseRegion(possibleOffset);				
					if (region != null)
					{
						addLine(region);
						next = possibleOffset + region.getFullParsedLength();
						if (text.charAt(next - 1) == '\n')
							next--; //For beginning from next line
					}					
					else
					{
						WikitextLine parsedLine = lineMarkupParser.parseLine(current);
						if (!wasBreak && isSameLineWithLast(parsedLine))
						{
							appendToPreviousLine(parsedLine);
						}
						else
							addLine(parsedLine);
						if (parsedLine.getFullParsedLength() != current.length())
							addLineRest(current, parsedLine.getFullParsedLength());
					}
					wasBreak = false;
				}
			}
			pos = next;
		}
		
	}


	private void appendToPreviousLine(WikitextLine parsedLine)
	{
		WikitextLine previousLine = lines.get(lines.size() - 1);
		previousLine.append(parsedLine.getText());
	}
	
	protected WikitextLine tryToParseRegion(int pos)
	{
		for (Iterator<IRegionParser> iterator = regionParsers.iterator(); iterator.hasNext();)
		{
			IRegionParser parser = (IRegionParser) iterator.next();
			WikitextLine parsed = parser.tryToParseRegion(text, pos);
			if (parsed != null)
				return parsed;
		}
		return null;
	}


	protected void addLine(WikitextLine parsedLine)
	{
		lines.add(parsedLine);
		bullets.add(parsedLine.getBullet());
		indents.add(parsedLine.getIndent());
	}


	public boolean isSameLineWithLast(WikitextLine line)
	{
		if (!canMergeLines || lines.size() == 0) //First line
			return false;
		WikitextLine previousLine = lines.get(lines.size() - 1);
		if (line.getBullet() != null)
			return false;	
		if (line.getClass() != WikitextLine.class || previousLine.getClass() != WikitextLine.class)
			return false;
		if (line.getType() != WikitextLine.SIMPLE || previousLine.getType() != WikitextLine.SIMPLE)
			return false;
		if (line.getIndent() != previousLine.getMarkupIndent())
			return false;
		if (hasSpecialMarkup(line) || hasSpecialMarkup(previousLine))
			return false;
		return true;
	}


	protected boolean hasSpecialMarkup(WikitextLine line)
	{
		for (Iterator<ISyntaxMatcher> iterator = specialMarkupMatchers.iterator(); iterator.hasNext();)
		{
			ISyntaxMatcher matcher = (ISyntaxMatcher) iterator.next();
			if (matcher.match(line.getText(),0) != null)
				return true;
		}
		return false;
	}


	protected void addLineRest(String current, int firstUnparserCharIdx)
	{
		try{
			String rest = current.substring(firstUnparserCharIdx);
			if (rest.trim().length() == 0) //Ignore spaces
				return;
			lines.add(new WikitextLine(WikitextLine.SIMPLE,rest));
			bullets.add(null);
			indents.add(0);
		}catch( StringIndexOutOfBoundsException e ){
			
		}
	}


	public List<WikitextLine> getLinesList()
	{
		return lines;
	}

}
