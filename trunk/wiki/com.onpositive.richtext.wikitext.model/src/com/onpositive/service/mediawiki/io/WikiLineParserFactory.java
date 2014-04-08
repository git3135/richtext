package com.onpositive.service.mediawiki.io;

import java.util.ArrayList;
import java.util.List;

import com.onpositive.richtexteditor.wikitext.parser.CitationMarkupMatcher;
import com.onpositive.richtexteditor.wikitext.parser.DefaultLineParserFactory;
import com.onpositive.richtexteditor.wikitext.parser.DefaultSyntaxMatcher;
import com.onpositive.richtexteditor.wikitext.parser.ISyntaxMatcher;
import com.onpositive.richtexteditor.wikitext.parser.LineTextLexEvent;
import com.onpositive.richtexteditor.wikitext.parser.SimpleHttpLinkMatcher;
import com.onpositive.richtexteditor.wikitext.parser.TagBracketMatcher;
import com.onpositive.richtexteditor.wikitext.parser.ToTagsSyntaxMatcher;

public class WikiLineParserFactory extends DefaultLineParserFactory
{
	protected List<ISyntaxMatcher> createHyperlinkMatcherList()
	{
		ArrayList<ISyntaxMatcher> matchers = new ArrayList<ISyntaxMatcher>();
		matchers.add(new MediaWikiLinkMatcher(MediaWikiParserConstants.MEDIAWIKI_LINK, "MediaWiki link"));
		matchers.add(new BracedLinkMatcher(LineTextLexEvent.LINK, "MediaWiki braced link"));
		//matchers.add(new CamelCaseMatcher(LineTextLexEvent.LINK));
		return matchers;
	}
	
	protected List<ISyntaxMatcher> createMatcherList()
	{
		ArrayList<ISyntaxMatcher> matchers = new ArrayList<ISyntaxMatcher>();
		matchers.add(new DefaultSyntaxMatcher(LineTextLexEvent.BOLD,"'''","BOLD"));
		matchers.add(new ToTagsSyntaxMatcher(LineTextLexEvent.BOLD,"<b>","</b>","BOLD"));
		matchers.add(new ToTagsSyntaxMatcher(LineTextLexEvent.ITALIC,"<i>","</i>","ITALIC"));
		//matchers.add(new ToTagsSyntaxMatcher(LineTextLexEvent.MONOSPACE,"<tt>","</tt>","TELETYPE"));
		//matchers.add(new ToTagsSyntaxMatcher(LineTextLexEvent.MONOSPACE,"<code>","</code>","TELETYPE"));
		matchers.add(new ToTagsSyntaxMatcher(LineTextLexEvent.SUPERSCRIPT,"<sup>","</sup>","SUPERSCRIPT"));
		matchers.add(new ToTagsSyntaxMatcher(LineTextLexEvent.SUBSCRIPT,"<sub>","</sub>","SUBSCRIPT"));
		matchers.add(new ToTagsSyntaxMatcher(LineTextLexEvent.STRIKETHROUGH,"<strike>","</strike>","STRIKETHROUGH"));
		matchers.add(new ToTagsSyntaxMatcher(LineTextLexEvent.STRIKETHROUGH,"<s>","</s>","STRIKETHROUGH"));
		matchers.add(new ToTagsSyntaxMatcher(LineTextLexEvent.UNDERLINED,"<u>","</u>","STRIKETHROUGH"));
		matchers.add(new ToTagsSyntaxMatcher(LineTextLexEvent.BOLD,"<strong>","</strong>","BOLD"));
		matchers.add(new TagBracketMatcher("tt",LineTextLexEvent.MONOSPACE,"TT"));
		matchers.add(new TagBracketMatcher("code",LineTextLexEvent.MONOSPACE,"CODE"));
		matchers.add(new DefaultSyntaxMatcher(LineTextLexEvent.ITALIC,"''","ITALIC"));
		matchers.add(new DefaultSyntaxMatcher(LineTextLexEvent.BOLD_ITALIC,"'''''","BOLD_ITALIC"));
		matchers.add(new DefaultSyntaxMatcher(LineTextLexEvent.STRIKETHROUGH,"~~","STRIKETHROUGH"));
		//matchers.add(new DefaultSyntaxMatcher(LineTextLexEvent.SUBSCRIPT,",,","SUBSCRIPT"));
		//matchers.add(new DefaultSyntaxMatcher(LineTextLexEvent.SUPERSCRIPT,"^","SUPERSCRIPT"));
		//matchers.add(new DefaultSyntaxMatcher(LineTextLexEvent.UNDERLINED,"__","UNDERLINED"));
		matchers.add(new MediaWikiImageMatcher(LineTextLexEvent.IMAGE,"IMAGE"));
		matchers.add(new CitationMarkupMatcher(LineTextLexEvent.CITATION,'>'));
		//matchers.add(new TracImageMatcher(LineTextLexEvent.IMAGE));
		//matchers.add(new SimpleBracketMatcher("[[","]]",LineTextLexEvent.MACRO,"macro",true));
		//matchers.add(new BracedEscapeMatcher("{{{","}}}",LineTextLexEvent.MONOSPACE,"monospace"));
		//matchers.add(new BracedEscapeMatcher("`","`",LineTextLexEvent.MONOSPACE,"monospace"));
		//matchers.add(new EscapedTokenMatcher(LineTextLexEvent.TEXT, '!', new ArrayList<ISyntaxMatcher>(matchers))); //TODO TracWiki escaping isn't functioning for MediaWiki
		
		hyperlinkMatchers = createHyperlinkMatcherList();		
		matchers.addAll(hyperlinkMatchers);		
		//matchers.add(new EscapedTokenMatcher(LineTextLexEvent.ESCAPED_LINK, '!', hyperlinkMatchers));
		hyperlinkMatchers.add(new SimpleHttpLinkMatcher(LineTextLexEvent.LINK, "HTTP: link matcher"));
		return matchers;
	}
	
	
}
