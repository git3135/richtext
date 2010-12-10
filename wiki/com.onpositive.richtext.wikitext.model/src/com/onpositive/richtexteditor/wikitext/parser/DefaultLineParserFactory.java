package com.onpositive.richtexteditor.wikitext.parser;

import java.util.ArrayList;
import java.util.List;

public class DefaultLineParserFactory
{
	protected List<ISyntaxMatcher> matchers;
	protected List<ISyntaxMatcher> hyperlinkMatchers;
	
		
	public ILineParser createParser(ILexEventConsumer consumer)
	{
		matchers = createMatcherList();
		return new DefaultLineParser(matchers, consumer);
	}
	
	protected List<ISyntaxMatcher> createHyperlinkMatcherList()
	{
		ArrayList<ISyntaxMatcher> matchers = new ArrayList<ISyntaxMatcher>();
		matchers.add(new TracBracedLinkMatcher("[","]",LineTextLexEvent.LINK));
		matchers.add(new CamelCaseMatcher(LineTextLexEvent.LINK));
		matchers.add(new TicketRefMatcher(LineTextLexEvent.LINK, "TICKET REF"));
		matchers.add(new TracLinksMatcher(LineTextLexEvent.LINK, "TRAC LINK"));
		matchers.add(new NumberedLinkMatcher("{","}",LineTextLexEvent.LINK, "TRAC REPORT"));
		matchers.add(new NumberedLinkMatcher("[","]",LineTextLexEvent.LINK, "TRAC CHANGESET"));
		return matchers;
	}
	
	protected List<ISyntaxMatcher> createMatcherList()
	{
		ArrayList<ISyntaxMatcher> matchers = new ArrayList<ISyntaxMatcher>();
		matchers.add(new DefaultSyntaxMatcher(LineTextLexEvent.BOLD,"'''","BOLD"));
		matchers.add(new DefaultSyntaxMatcher(LineTextLexEvent.ITALIC,"''","ITALIC"));
		matchers.add(new DefaultSyntaxMatcher(LineTextLexEvent.BOLD_ITALIC,"'''''","BOLD_ITALIC"));
		matchers.add(new DefaultSyntaxMatcher(LineTextLexEvent.STRIKETHROUGH,"~~","STRIKETHROUGH"));
		matchers.add(new DefaultSyntaxMatcher(LineTextLexEvent.SUBSCRIPT,",,","SUBSCRIPT"));
		matchers.add(new DefaultSyntaxMatcher(LineTextLexEvent.SUPERSCRIPT,"^","SUPERSCRIPT"));
		matchers.add(new DefaultSyntaxMatcher(LineTextLexEvent.UNDERLINED,"__","UNDERLINED"));
		matchers.add(new CitationMarkupMatcher(LineTextLexEvent.CITATION,'>'));
		matchers.add(new TracImageMatcher(LineTextLexEvent.IMAGE));
		matchers.add(new SimpleBracketMatcher("[[","]]",LineTextLexEvent.MACRO,"macro",true));
		matchers.add(new BracedEscapeMatcher("{{{","}}}",LineTextLexEvent.MONOSPACE,"monospace"));
		matchers.add(new BracedEscapeMatcher("`","`",LineTextLexEvent.MONOSPACE,"monospace"));
		matchers.add(new EscapedTokenMatcher(LineTextLexEvent.TEXT, '!', new ArrayList<ISyntaxMatcher>(matchers)));
		
		hyperlinkMatchers = createHyperlinkMatcherList();		
		matchers.addAll(hyperlinkMatchers);		
		matchers.add(new EscapedTokenMatcher(LineTextLexEvent.ESCAPED_LINK, '!', hyperlinkMatchers));
		hyperlinkMatchers.add(new SimpleHttpLinkMatcher(LineTextLexEvent.LINK, "HTTP: link matcher"));
		return matchers;
	}
}
