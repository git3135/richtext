package com.onpositive.richtexteditor.wikitext.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.onpositive.richtext.model.Document;
import com.onpositive.richtexteditor.io.html_scaner.EOFEvent;
import com.onpositive.richtexteditor.io.html_scaner.ILexListener;

public class WikitextLinesScanner extends TextScanner {
	protected NewWikitextScanner scanner;
	protected ArrayList<Integer> lineIndents;
	protected ArrayList<String> lineBullets;
	protected int regionMarkupLevel = 0;

	public WikitextLinesScanner() {
		scanner = new NewWikitextScanner();
		lineIndents = new ArrayList<Integer>();
		lineBullets = new ArrayList<String>();
	}

	public void addLexListener(ILexListener listener) {
		scanner.addLexListener(listener);
		super.addLexListener(listener);
	}

	public void process(String str) throws IOException {
		Document doc = new Document(str!=null?str:"");
		
		final int numberOfLines = doc.getNumberOfLines();
		try {
			for (int i = 0; i < numberOfLines; i++) {
				
				String line = doc.get(i);
				final String trim = line.trim();
				if (trim.equals(WikitextTokenProvider.getInstance().getKeyword(
						WikitextTokenProvider.WIKITEXT_REGION_START))) {
					lineIndents.add(calculateLineIndent(line));
					lineBullets.add("");
					regionMarkupLevel++;
				} else if (regionMarkupLevel > 0) {
					if (trim.equals(WikitextTokenProvider.getInstance()
							.getKeyword(
									WikitextTokenProvider.WIKITEXT_REGION_END))) {
						regionMarkupLevel--;
					}
					lineIndents.add(0);
					lineBullets.add("");
				} else {
					int indent = 0;
					if (!trim.equals(""))
						indent = calculateLineIndent(line);
					lineIndents.add(indent);
					final String lineTail = line.substring(indent);
					if (indent > 0) {
						final String bullet = tryToMatchBullet(lineTail);
						lineBullets.add(bullet);
					} else {
						/*
						 * if (lineTail.startsWith(">")) lineBullets.add(">");
						 * //For citation else
						 */
						lineBullets.add("");
					}
				}
			}
			scanner.setIndents(lineIndents);
			scanner.setBullets(lineBullets);
			scanner.setSourceLineCount(doc.getNumberOfLines());

			for (int i = 0; i < numberOfLines; i++) {
				String line = doc.get(i);
				/*
				 * scanner.handleLexEvent(new WikitextLexEvent("",
				 * WikitextLexEvent.INDENT)); if
				 * (!lineBullets.get(i).equals("")) {
				 * handleBulletEvent(lineBullets.get(i)); }
				 */
				scanner.process(line.substring(lineIndents.get(i)
						+ lineBullets.get(i).length()), i);
			}
			handleLexEvent(new EOFEvent());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void handleBulletEvent(String marker) {
		if (marker.equals("* "))
			handleLexEvent(new WikitextLexEvent("* ",
					WikitextLexEvent.BULLETED_LIST));
		else
			handleLexEvent(new WikitextLexEvent(marker,
					WikitextLexEvent.NUMBERED_LIST));
	}

	public static String tryToMatchBullet(String substring) {
		if (substring.startsWith("* "))
			return "* ";
		final Pattern pattern1 = Pattern.compile("\\d+\\. ");
		final Pattern pattern2 = Pattern.compile("[a-z]\\. ");
		final Pattern pattern3 = Pattern.compile("[ixv]+\\. ");
		String marker = tryToFind(substring, pattern1);
		if (marker != null)
			return marker;
		marker = tryToFind(substring, pattern2);
		if (marker != null)
			return marker;
		marker = tryToFind(substring, pattern3);
		if (marker != null)
			return marker;
		return "";
	}

	protected static String tryToFind(String substring, Pattern pattern) {
		Matcher matcher = pattern.matcher(substring);
		if (matcher.find()) {
			int start = matcher.start();
			if (start == 0) {
				int end = matcher.end();
				return substring.substring(start, end);
			}
		}
		return null;
	}

	protected int calculateLineIndent(String line) {
		int i = 0;
		for (; i < line.length() && line.charAt(i) == ' '; i++)
			;
		return i;
	}

}
