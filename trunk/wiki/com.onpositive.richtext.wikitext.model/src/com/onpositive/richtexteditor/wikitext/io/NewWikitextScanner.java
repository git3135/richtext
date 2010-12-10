package com.onpositive.richtexteditor.wikitext.io;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;

import com.onpositive.richtexteditor.io.LexEvent;
import com.onpositive.richtexteditor.io.TypedLexEvent;

/**
 * (4934)
 * 
 * @author 32kda (c) Made in USSR
 */
public class NewWikitextScanner extends TextScanner {
	private static final String MACRO_OPEN_STR = "[[";

	protected static HashSet<Character> keySymbols; // Symbols, which can be the
	// beginning of some special
	// symbols of
	// wikitext markup

	protected int uk;
	protected char[] t;
	// protected HashSet<ILexListener> listeners = new HashSet<ILexListener>();
	protected int curTableIndent = -1; // -1 means, that it's no current table

	protected int currentState = 0;

	protected boolean separarateBulletedLine = false; // If true, we should scan
	// text up to \n and
	// make this text a
	// separate partition
	// with a newline after
	// it
	protected int separationIndent = 0; // Linked with prev constant. Means an
	// indent, lines with wich belongs to
	// current list
	protected int currentIndent = 0;
	protected int regionMarkupLevel = 0;
	protected int headerEndIdx = -1;

	protected int currentSourceLineIdx = 0;
	protected int sourceLineCount;
	protected boolean isDefList = false;
	protected List<Integer> indents;
	protected List<String> bullets;
	protected boolean shouldAppendNextLine = false;
	int lastAccumulatedLineNum = -1; // Number of last line, which should be
	// added to definition list

	protected static final int NONE = 0;
	protected static final int IS_HEADER1 = 1;
	protected static final int IS_HEADER2 = 2;
	protected static final int IS_HEADER3 = 3;
	protected static final int IS_HEADER4 = 4;
	protected static final int IS_HEADER5 = 5;
	protected static final int IS_HEADER6 = 6;

	protected static final int DEFAULT_DEFLIST_INDENT = 1;

	static {
		keySymbols = WikitextTokenProvider.getInstance().getKeySymbols();
	}

	/**
	 * Main func
	 * 
	 * @throws Exception
	 */
	void scaner() {
		uk = 0;
		headerEndIdx = -1;
		while (true) {
			TypedLexEvent lxm = new TypedLexEvent();
			// pass();
			if (uk >= t.length)
				return;
			StringBuilder sb = new StringBuilder();
			while (uk < t.length) {
				if (keySymbols.contains(t[uk]) || canBeListMarker(uk)) {
					final WikitextLexEvent markupTagEvent = (WikitextLexEvent) tryToScanMarkupTag();
					if (markupTagEvent != null) {
						final int deletionCount = -markupTagEvent
								.getEventOffsetShift();
						int length = sb.length();
						if (deletionCount > 0)
							sb.delete(Math.max(length - deletionCount, 0),
									length);
						lxm.text = sb.toString();
						handleLexEvent(lxm);
						handleLexEvent(markupTagEvent);

						sb.delete(0, length);
						lxm = new TypedLexEvent();
					} else
					{
						if (t[uk]=='#'){
							WikitextLexEvent camelCaseEvent = tryToScanCamelCase(sb);
							if (camelCaseEvent != null) {
								lxm.text = sb.toString();
								handleAndClearLexEvent(lxm, sb);
								handleLexEvent(camelCaseEvent);
							}
							else if (t.length>uk){
									sb.append(t[uk++]);								
							}
						}
						else{
							sb.append(t[uk++]);
						}
					}
				} else if ((uk == 0 || isPrevWhiteSpace())&&
						(Character.isUpperCase(t[uk])||t[uk]=='#')) {
					WikitextLexEvent camelCaseEvent = tryToScanCamelCase(sb);
					if (camelCaseEvent != null) {
						lxm.text = sb.toString();
						handleAndClearLexEvent(lxm, sb);
						handleLexEvent(camelCaseEvent);
					}
				}
				/*
				 * else if (t[uk] == '\n' || t[uk] == '\r') { if
				 * (separarateBulletedLine) { uk++; if (uk < t.length - 1 &&
				 * ((t[uk - 1] == '\n' && t[uk] == '\r') || (t[uk - 1] == '\r'
				 * && t[uk] == '\n'))) uk++; int spacesCount =
				 * countSpacesIncremental(uk); if (spacesCount > 0 &&
				 * spacesCount == separationIndent &&
				 * tryToMatchNumberedListMarkup(uk + spacesCount) == 0 &&
				 * tryToMatchBulletedListMarkup(uk + spacesCount) == 0) {
				 * sb.append(' '); uk += spacesCount; } else { lxm = new
				 * WikitextLexEvent
				 * (clearFromEnters(sb.toString()),WikitextLexEvent
				 * .EOL_REACHED); lxm = handleAndClearLexEvent(lxm, sb);
				 * currentIndent = 0; StringBuilder indent = new
				 * StringBuilder(""); for (int i =0 ; i < spacesCount; i++)
				 * indent.append(' '); handleLexEvent(new
				 * WikitextLexEvent(indent.toString(),
				 * WikitextLexEvent.INDENT)); separarateBulletedLine = false; }
				 * } //FIXME а что если пробелы были на линии!!!! else if
				 * ((t[uk] == '\n' && (uk > 0 && t[uk - 1] == '\n' || //Newline
				 * after another newline uk > 1 && t[uk - 1] == '\r' && t[uk -
				 * 2] == '\n')) || (t[uk] == '\r' && (uk > 0 && t[uk - 1] ==
				 * '\r' || uk > 1 && t[uk - 1] == '\n' && t[uk - 2] == '\r')) )
				 * { uk++; lxm = new
				 * WikitextLexEvent("",WikitextLexEvent.RESET_STYLE_FLAGS);
				 * handleLexEvent(lxm); String string = sb.toString(); lxm = new
				 * WikitextLexEvent
				 * (clearFromEnters(string),WikitextLexEvent.ADDITIONAL_NEWLINE
				 * ); lxm = handleAndClearLexEvent(lxm, sb); currentIndent = 0;
				 * separationIndent = 0; handleLexEvent(new WikitextLexEvent("",
				 * WikitextLexEvent.INDENT)); } else if (uk < t.length - 1 &&
				 * !Character.isWhitespace(t[uk + 1]) && currentIndent > 0) {
				 * String string = sb.toString(); lxm = new
				 * WikitextLexEvent(string,WikitextLexEvent.EOL_REACHED); lxm =
				 * handleAndClearLexEvent(lxm, sb); currentIndent = 0;
				 * separationIndent = 0; handleLexEvent(new WikitextLexEvent("",
				 * WikitextLexEvent.INDENT)); } else sb.append(t[uk++]); }
				 */
				// This case should not exists. This means wrong parsing model
				// logic !!! FIXME!!!
				else if (t[uk] == ' ' && uk == 0) {
					StringBuilder indentSb = new StringBuilder();
					while (uk < t.length && t[uk] == ' ') {
						indentSb.append(t[uk++]);
					}
					final String indent = indentSb.toString();
					currentIndent = indent.length();
					separationIndent = 0;
					handleLexEvent(new WikitextLexEvent(indent,
							WikitextLexEvent.INDENT));
				} else
					sb.append(t[uk++]);

			}
			if (sb.length() > 0)
				handleLexEvent(new LexEvent(sb.toString()));
		}
	}

	private boolean isPrevWhiteSpace() {
		return (Character.isWhitespace(t[uk - 1]));
	}

	/*
	 * private boolean spacesOnly(char[] t, int uk2) { int a=uk2; for
	 * (;a<t.length;a++){ if (t[a]=='\r'||t[a]=='\n'){ break; } } for
	 * (;a<t.length;a++){ if (t[a]=='\r'||t[a]=='\n'){ return true; } if
	 * (!Character.isWhitespace(t[a])){ return false; } } return false; }
	 */

	private boolean canBeListMarker(int uk2) {
		if (uk2 > 0
				&& t[uk2 - 1] == ' '
				&& (Character.isDigit(t[uk2]) || (t[uk2] >= 'a' && t[uk2] <= 'z')))
			return true;
		return false;
	}

	protected WikitextLexEvent tryToScanCamelCase(StringBuilder sb) {
		if (uk >= t.length)
			return null;
		StringBuilder curWordSb = new StringBuilder("" + t[uk]);
		uk++;
		while (uk < t.length
				&& ((Character.isJavaIdentifierStart(t[uk]) || t[uk] == ':' || t[uk] == '#')||Character.isDigit(t[uk])))
			curWordSb.append(t[uk++]);
		if (WikitextTokenProvider.isCamelCaseWord(curWordSb.toString()))
			return new WikitextLexEvent(curWordSb.toString(),
					WikitextLexEvent.LINK, 0);
		sb.append(curWordSb); // In case curWord isn't camelcase, simply append
		// it to global sb
		return null;
	}

	protected TypedLexEvent handleAndClearLexEvent(TypedLexEvent lxm,
			StringBuilder sb) {
		handleLexEvent(lxm);

		lxm = new TypedLexEvent();
		sb.delete(0, sb.length());
		return lxm;
	}

	protected LexEvent tryToScanMarkupTag() {
		if (uk >= t.length)
			return null;
		LexEvent lexEvent = null;
		if (uk < t.length - 1 && t[uk] == '_' && t[uk + 1] == '_')
			lexEvent = createLexEvent(2, WikitextLexEvent.UNDERLINED);
		else if (uk < t.length - 1 && t[uk] == '~' && t[uk + 1] == '~')
			lexEvent = createLexEvent(2, WikitextLexEvent.STRIKETHROUGH);
		else if (uk < t.length - 1 && t[uk] == ',' && t[uk + 1] == ',')
			lexEvent = createLexEvent(2, WikitextLexEvent.SUBSCRIPT);
		else if (t[uk] == '^')
			lexEvent = createLexEvent(1, WikitextLexEvent.SUPERSCRIPT);
		else if (t[uk] == '[') {
			if (uk < t.length - 1 && t[uk + 1] == '[') {
				final LexEvent macroTag = tryToScanMacroTag();
				return macroTag;
			}
			return tryToScanHyperlink();
		} else if (t[uk] == '-') {
			if (uk < t.length - 3 && t[uk + 1] == '-' && t[uk + 2] == '-'
					&& t[uk + 3] == '-') {
				int pos = uk + 4;
				while (pos < t.length && t[pos] == '-')
					pos++;
				return createLexEvent(pos - uk, WikitextLexEvent.HR);
			}
		} else if (t[uk] == '\'') {
			int lastIdx = uk + 1;
			while (lastIdx < t.length && t[lastIdx] == '\'')
				lastIdx++;
			int count = lastIdx - uk;
			if (count >= 5)
				lexEvent = createLexEvent(5, WikitextLexEvent.BOLD_ITALIC);
			else if (count >= 3)
				lexEvent = createLexEvent(3, WikitextLexEvent.BOLD);
			else if (count == 2)
				lexEvent = createLexEvent(2, WikitextLexEvent.ITALIC);
		} else if (t[uk] == '=') {
			int lastIdx = uk;
			while (lastIdx < t.length && t[lastIdx] == '=')
				lastIdx++;
			int count = lastIdx - uk;
			if (currentState != IS_HEADER1 && currentState != IS_HEADER2
					&& currentState != IS_HEADER3 && currentState != IS_HEADER4
					&& currentState != IS_HEADER5 && currentState != IS_HEADER6) {
				if (lastIdx == t.length || t[lastIdx] != ' ' || count > 6)
					return null;
				int pos = tryToMatchHeaderMarkup(lastIdx + 1, count);
				if (pos > 0) {
					if (atLineStart(uk)) {
						if (count == 1) {
							lexEvent = createLexEvent(count + 1,
									WikitextLexEvent.HEADER1_OPEN); // 1 for
							// after-markup
							// space
							currentState = IS_HEADER1;
						} else if (count == 2) {
							lexEvent = createLexEvent(count + 1,
									WikitextLexEvent.HEADER2_OPEN);
							currentState = IS_HEADER2;
						} else if (count == 3) {
							lexEvent = createLexEvent(count + 1,
									WikitextLexEvent.HEADER3_OPEN);
							currentState = IS_HEADER3;
						} else if (count == 4) {
							lexEvent = createLexEvent(count + 1,
									WikitextLexEvent.HEADER4_OPEN);
							currentState = IS_HEADER4;
						} else if (count == 5) {
							lexEvent = createLexEvent(count + 1,
									WikitextLexEvent.HEADER5_OPEN);
							currentState = IS_HEADER5;
						} else if (count == 6) {
							lexEvent = createLexEvent(count + 1,
									WikitextLexEvent.HEADER6_OPEN);
							currentState = IS_HEADER6;
						}
					}

				}
			} else if (((currentState == IS_HEADER1 && count == 1)
					|| (currentState == IS_HEADER2 && count == 2)
					|| (currentState == IS_HEADER3 && count == 3)
					|| (currentState == IS_HEADER4 && count == 4)
					|| (currentState == IS_HEADER5 && count == 5) || (currentState == IS_HEADER6 && count == 6))
					&& uk > 0 && t[uk - 1] == ' ') {
				currentState = NONE;
				lexEvent = createLexEvent(count + 1,
						WikitextLexEvent.HEADER_CLOSE, -1);
				headerEndIdx = uk + count;
			}

		} else if (t[uk] == '*' && isSpaceBefore() && isSpaceAfter(uk)
				&& atLineStart(uk)) {
			int spacesCount = countSpaces(uk - 1);
			lexEvent = createLexEvent(spacesCount + 2,
					WikitextLexEvent.BULLETED_LIST, -spacesCount);
			separarateBulletedLine = true;
			separationIndent = lexEvent.text.length();
		} else if (t[uk] == '#' && headerEndIdx > 0) {
			if (checkOnlySpacesBetween(headerEndIdx, uk)) {
				StringBuilder idBuilder = new StringBuilder("#");
				int pos = uk + 1;
				if (Character.isJavaIdentifierStart(t[pos])) {
					idBuilder.append(t[pos]);
					pos++;
					while (pos < t.length
							&& (Character.isJavaIdentifierPart(t[pos]) || t[pos] == '-'))
						idBuilder.append(t[pos++]);
					int shift = uk - headerEndIdx + 1;
					uk = pos;
					final WikitextLexEvent wikitextLexEvent = new WikitextLexEvent(
							idBuilder.toString(), WikitextLexEvent.EXPLICIT_ID);
					wikitextLexEvent.eventOffsetShift = shift;
					headerEndIdx = -1;
					return wikitextLexEvent;
				}
			}
			return null;
		} else if (t[uk] == '>' && uk == 0) {
			String markup = scanCitationMarkup(0);
			lexEvent = new WikitextLexEvent(markup,
					WikitextLexEvent.SEPARATE_LINE);
			uk += markup.length();
		} else if (uk < t.length - 1
				&& (Character.isDigit(t[uk]) || (t[uk] >= 'a' && t[uk] <= 'z'))
				&& isSpaceBefore() && atLineStart(uk)) {
			int spacesCount = countSpaces(uk - 1);
			int k = tryToMatchNumberedListMarkup(uk);
			if (k > uk && isSpaceAfter(k)) {
				if (Character.isDigit(t[uk]))
					lexEvent = createLexEventWithExplictText(spacesCount + k
							+ 2 - uk, WikitextLexEvent.NUMBERED_LIST,
							-spacesCount, createMarkerWithSpaces("1. ",
									spacesCount));
				else if ((t[uk] == 'i')
						|| ((t[uk] == 'x' || t[uk] == 'v') && k - uk > 1))
					lexEvent = createLexEventWithExplictText(spacesCount + k
							+ 2 - uk, WikitextLexEvent.NUMBERED_LIST,
							-spacesCount, createMarkerWithSpaces("i. ",
									spacesCount));
				else
					lexEvent = createLexEventWithExplictText(spacesCount + k
							+ 2 - uk, WikitextLexEvent.NUMBERED_LIST,
							-spacesCount, createMarkerWithSpaces("a. ",
									spacesCount));
				separarateBulletedLine = true;
				separationIndent = lexEvent.text.length();
			}

		} else if (t[uk] == '{' && uk < t.length - 2 && t[uk + 1] == '{'
				&& t[uk + 2] == '{') {
			int pos = uk + 3;
			int level = 0;
			while (pos < t.length
					&& (!(t[pos] == '}' && t[pos - 1] == '}' && t[pos - 2] == '}') || level > 0)) {
				if (pos < t.length - 3
						&& (t[pos] == '{' && t[pos + 1] == '{' && t[pos + 2] == '{')) {
					level++;
					pos += 2;
				} else if (t[pos] == '}' && t[pos - 1] == '}'
						&& t[pos - 2] == '}') {
					level--;
				}
				pos++;
			}
			lexEvent = createLexEventByCoords(uk + 3, pos - 2 - uk - 3,
					WikitextLexEvent.MONOSPACE);
			uk = pos + 1;
		} else if (t[uk] == '!') {
			uk++;
			LexEvent escapedTag = tryToScanMarkupTag();
			if (escapedTag == null && uk < t.length
					&& Character.isJavaIdentifierStart(t[uk]))
				escapedTag = tryToScanCamelCase(new StringBuilder());
			if (escapedTag != null) {
				if (escapedTag instanceof WikitextLexEvent
						&& ((WikitextLexEvent) escapedTag).getType() == WikitextLexEvent.MACRO)
					return new WikitextLexEvent("!" + escapedTag.text,
							WikitextLexEvent.NONE_TYPE);
				if (escapedTag instanceof WikitextLexEvent
						&& ((WikitextLexEvent) escapedTag).getType() == WikitextLexEvent.LINK) // Escaped
					// CamelCase
					return new WikitextLexEvent("!" + escapedTag.text,
							WikitextLexEvent.LINK);
				return new WikitextLexEvent(escapedTag.text,
						WikitextLexEvent.NONE_TYPE);
			} else {
				uk--;
				return null;
			}
			/*
			 * int pos = uk + 1; if (pos < t.length &&
			 * keySymbols.contains(t[pos])) { char initChar = t[pos]; if
			 * (initChar == '!' || Character.isLetter(initChar)) { //uk++;
			 * return null; } else if (initChar == '`') { lexEvent =
			 * tryToScanEscapedText(pos); if (lexEvent != null) { lexEvent.l =
			 * "`" + lexEvent.l + "`"; uk++; //For passing trailing '`' } } else
			 * { while (pos < t.length && t[pos] == initChar) pos++; if (pos >
			 * uk + 1) lexEvent = createLexEvent(pos - uk - 1,
			 * WikitextLexEvent.NONE_TYPE, 1); } }
			 */
		} else if (t[uk] == '`') {
			lexEvent = tryToScanEscapedText(uk);
			if (lexEvent != null)
				uk++; // For passing trailing '`'
		}
		return lexEvent;
	}

	protected String scanCitationMarkup(int pos) {
		StringBuilder resBuilder = new StringBuilder();
		int i = pos;
		for (; i < t.length && t[i] == '>'; i++) {
			resBuilder.append(t[i]);
		}
		return resBuilder.toString();
	}

	protected boolean checkOnlySpacesBetween(int start, int end) {
		for (int i = start; i <= end; i++) {
			if (t[i] != ' ')
				return false;
		}
		return true;
	}

	protected boolean atLineStart(int pos) {
		pos--;
		while (pos >= 0 && t[pos] == ' ')
			pos--;
		if (pos == -1)
			return true;
		return false;
	}

	protected String createMarkerWithSpaces(String string, int spacesCount) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < spacesCount; i++)
			sb.append(" ");
		sb.append(string);
		return sb.toString();
	}

	protected boolean atFirstLine(int pos) {
		pos = pos - 1;
		while (pos >= 0 && t[pos] == ' ')
			pos--;
		if (pos == -1)
			return true;
		return false;
	}

	protected int countSpaces(int pos) {
		int startingPos = pos;
		while (pos >= 0 && t[pos] == ' ')
			pos--;
		return startingPos - pos;
	}

	protected int countSpacesIncremental(int pos) {
		int startingPos = pos;
		while (pos < t.length && t[pos] == ' ')
			pos++;
		return pos - startingPos;
	}

	protected LexEvent tryToScanEscapedText(int pos) {
		StringBuilder sb = new StringBuilder();
		pos++;
		while (pos < t.length && t[pos] != '`') {
			sb.append(t[pos]);
			pos++;
		}
		if (pos < t.length && t[pos] == '`') {
			return directCreateLexEvent(sb.toString(),
					WikitextLexEvent.MONOSPACE, 1);
		}
		return null;
	}

	protected LexEvent tryToScanMacroTag() {
		StringBuilder sb = new StringBuilder(MACRO_OPEN_STR);
		int pos = uk + 2;
		while (pos < t.length && t[pos] == ' ')
			pos++;
		while (pos < t.length && t[pos] != ' ' && t[pos] != ']')
			sb.append(t[pos++]);
		while (pos < t.length && !(t[pos] == '\r' || t[pos] == '\n')
				&& !(t[pos] == ']' && t[pos - 1] == ']'))
			sb.append(t[pos++]);
		if (pos >= t.length || t[pos] == '\r' || t[pos] == '\n')
			return null;
		if (sb.charAt(sb.length() - 1) == ']')
			sb.deleteCharAt(sb.length() - 1);
		uk = pos + 1;
		if (sb.indexOf("image") == MACRO_OPEN_STR.length()
				|| sb.indexOf("Image") == MACRO_OPEN_STR.length()) {
			int idx1 = sb.indexOf("(");
			if (idx1 == -1 || sb.indexOf(")") < idx1) {
				sb.append("]]");
				return new WikitextLexEvent(sb.toString(),
						WikitextLexEvent.MACRO);
			}
			return new WikitextLexEvent(sb.substring(2).toString(),
					WikitextLexEvent.IMAGE);
		} else {
			sb.append("]]");
			return new WikitextLexEvent(sb.toString(), WikitextLexEvent.MACRO);
		}
		/*
		 * else if (sb.indexOf("BR") == 0) //TODO Too much copypaste here;
		 * Should refactor, if add one more { while (pos < t.length && !(t[pos]
		 * == ']' && t[pos - 1] == ']')) sb.append(t[pos++]); if
		 * (sb.charAt(sb.length() - 1) == ']') sb.deleteCharAt(sb.length() - 1);
		 * uk = pos + 1; separarateBulletedLine = false; return new
		 * WikitextLexEvent(sb.toString(),WikitextLexEvent.BR); }
		 */

	}

	protected LexEvent tryToScanHyperlink() {
		StringBuilder sb = new StringBuilder();
		int pos = uk + 1;
		while (pos < t.length && t[pos] == ' ')
			pos++;
		while (pos < t.length && t[pos] != ' ' && t[pos] != ']')
			sb.append(t[pos++]);
		if ((sb.indexOf(WikitextTokenProvider.getInstance().getKeyword(
				WikitextTokenProvider.HTTP_LINK_PREFFIX)) == 0)
				|| (sb.indexOf(WikitextTokenProvider.getInstance().getKeyword(
						WikitextTokenProvider.WIKI_LINK_PREFFIX)) == 0)
				|| (sb.indexOf(WikitextTokenProvider.getInstance().getKeyword(
						WikitextTokenProvider.ATTACHMENT_LINK_PREFFIX)) == 0)
				|| (sb.indexOf(WikitextTokenProvider.getInstance().getKeyword(
						WikitextTokenProvider.COMMENT_LINK_PREFFIX)) == 0)
			    || (sb.indexOf(WikitextTokenProvider.getInstance().getKeyword(
						WikitextTokenProvider.TICKET_LINK_PREFFIX)) == 0)
				|| WikitextTokenProvider.isCamelCaseWord(sb.toString())||isHyperLink(sb)) {
			while (pos < t.length && t[pos] != ']')
				sb.append(t[pos++]);
			uk = pos;
			if (pos < t.length && t[pos] == ']')
				uk++;
			return new WikitextLexEvent(sb.toString(), WikitextLexEvent.LINK);
		}
		return null;
	}

	private boolean isHyperLink(StringBuilder sb) {
		for (int a=0;a<sb.length();a++){
			char c=sb.charAt(a);
			if (c==':'){
				if (sb.length()>a+2){
					if (sb.charAt(a+1)=='/'){
						if (sb.charAt(a+2)=='/'){
							return true;
						}
					}
				}
			}
			if (!Character.isJavaIdentifierStart(c)){
				return false;
			}
		}
		return false;
	}

	private boolean isUrl(StringBuilder sb) {
		for (int a=0;a<sb.length();a++){
			char charAt = sb.charAt(a);
			if (!Character.isJavaIdentifierPart(charAt)){
				if (charAt==':'){
					if (sb.length()>a+2){
						if (sb.charAt(a+1)=='/'){
							if (sb.charAt(a+2)=='/'){
								return true;
							}
						}
					}
				}
				return false;
			}
		}
		return false;
	}

	/**
	 * Used to find header markup closing tag (if it's any)
	 * 
	 * @param pos
	 *            searching start position
	 * @param neededCount
	 *            Used to check, that found tag closing symbols ('=' here) count
	 *            matches it's count in the markup start tag
	 * @return markup tag end position, or -1 if ii isn't found
	 */
	protected int tryToMatchHeaderMarkup(int pos, int neededCount) {
		int lastFoundSuitableMarkupEnd = -1; // Because we take into account
		// only the last markup tag
		while (pos < t.length) {
			if (t[pos] == '=') {
				int tmp = pos + 1;
				while (tmp < t.length && t[tmp] == '=')
					tmp++;
				int count = tmp - pos;
				if (count == neededCount && t[pos - 1] == ' ') {
					lastFoundSuitableMarkupEnd = tmp;
					pos = tmp;
				}
			}
			pos++;
		}
		return lastFoundSuitableMarkupEnd;
	}

	/**
	 * Creates text event starting at current position, with the length
	 * eventTextLength Automatically increments position counter
	 * 
	 * @param eventTextLength
	 *            - event length. E.g. for __ must be 2
	 * @return new event
	 */
	protected LexEvent createLexEvent(int eventTextLength, int eventType) {
		StringBuilder sb = new StringBuilder();
		int eventFinish = uk + eventTextLength;
		for (; uk < eventFinish; uk++) {
			sb.append(t[uk]);
		}
		WikitextLexEvent event = new WikitextLexEvent(sb.toString(), eventType);
		return event;
	}

	protected LexEvent createLexEventByCoords(int offset, int length,
			int eventType) {
		StringBuilder sb = new StringBuilder();
		int eventFinish = offset + length;
		for (; offset < eventFinish; offset++) {
			sb.append(t[offset]);
		}
		WikitextLexEvent event = new WikitextLexEvent(sb.toString(), eventType);
		return event;
	}

	/**
	 * Creates text event starting at current position, with the length
	 * eventTextLength Automatically increments position counter
	 * 
	 * @param eventTextLength
	 *            - event length. E.g. for __ must be 2
	 * @param eventOffsetShift
	 *            - event offset shift. Should be negative number or 0. We need
	 *            this, if our event should "capture" some symbols (whitespaces
	 *            oftenly) from previous event
	 * @return new event
	 */
	protected LexEvent createLexEvent(int eventTextLength, int eventType,
			int eventOffsetShift) {
		StringBuilder sb = new StringBuilder();
		int pos = uk + eventOffsetShift;
		int end = pos + eventTextLength;
		for (; pos < end; pos++) {
			sb.append(t[pos]);
		}
		uk = pos;
		WikitextLexEvent event = new WikitextLexEvent(sb.toString(), eventType,
				eventOffsetShift);
		return event;
	}

	/**
	 * @return the sourceLineCount
	 */
	public int getSourceLineCount() {
		return sourceLineCount;
	}

	/**
	 * @param sourceLineCount
	 *            the sourceLineCount to set
	 */
	public void setSourceLineCount(int sourceLineCount) {
		this.sourceLineCount = sourceLineCount;
	}

	/**
	 * Creates text event starting at current position, with the length
	 * eventTextLength Automatically increments position counter
	 * 
	 * @param eventTextLength
	 *            - event length. E.g. for __ must be 2
	 * @param eventOffsetShift
	 *            - event offset shift. Should be negative number or 0. We need
	 *            this, if our event should "capture" some symbols (whitespaces
	 *            oftenly) from previous event
	 * @return new event
	 */
	protected LexEvent createLexEventWithExplictText(int eventTextLength,
			int eventType, int eventOffsetShift, String text) {
		int pos = uk + eventOffsetShift;
		int end = pos + eventTextLength;
		uk = end;
		WikitextLexEvent event = new WikitextLexEvent(text, eventType,
				eventOffsetShift);
		return event;
	}

	/**
	 * Creates text event starting at uk + eventOffsetShift, with the event
	 * string eventText Automatically increments position counter
	 * 
	 * @param eventText
	 *            - event text string. Will be directly used for event
	 * @param eventOffsetShift
	 *            - event offset shift. Specifies, where this event begins
	 *            relatively to uk We need this, if our event should "capture"
	 *            some symbols (whitespaces oftenly) from previous event, or,
	 *            contrariwise, skip some symbols
	 * @return new event
	 */
	protected LexEvent directCreateLexEvent(String eventText, int eventType,
			int eventOffsetShift) {
		uk = uk + eventOffsetShift + eventText.length();
		WikitextLexEvent event = new WikitextLexEvent(eventText, eventType,
				eventOffsetShift);
		return event;
	}

	/**
	 * @return the indents
	 */
	public List<Integer> getIndents() {
		return indents;
	}

	/**
	 * @return the bullets
	 */
	public List<String> getBullets() {
		return bullets;
	}

	/**
	 * @param bullets
	 *            the bullets to set
	 */
	public void setBullets(List<String> bullets) {
		this.bullets = bullets;
	}

	/**
	 * @param indents
	 *            the indents to set
	 */
	public void setIndents(List<Integer> indents) {
		this.indents = indents;
	}

	protected boolean isSpaceBefore() {
		if (uk > 0 && t[uk - 1] == ' ')
			return true;
		return false;
	}

	protected boolean isSpaceAfter(int pos) {
		if (pos < t.length - 1 && t[pos + 1] == ' ')
			return true;
		return false;
	}

	/**
	 * Method for processing Reader provided contents
	 * 
	 * @param line
	 *            String to process
	 * @param i
	 * @param charCount
	 *            char count to read
	 * @throws IOException
	 *             in case of i/o error
	 */
	public void process(String line, int i) throws IOException {
		currentSourceLineIdx = i;
		final String trim = line.trim();
		if (trim.equals(WikitextTokenProvider.getInstance().getKeyword(
				WikitextTokenProvider.WIKITEXT_REGION_START))) {
			if (curTableIndent > -1)
				handleTableEnd(i);
			if (isDefList)
				handleDeflistEnd(i);
			if (regionMarkupLevel == 0) {
				handleIndent(indents.get(i));
				handleLexEvent(new WikitextLexEvent(trim,
						WikitextLexEvent.REGION_START));
			} else
				handleLexEvent(new WikitextLexEvent(line,
						WikitextLexEvent.REGION_STRING));
			regionMarkupLevel++;
		} else if (regionMarkupLevel > 0) {
			if (trim.equals(WikitextTokenProvider.getInstance().getKeyword(
					WikitextTokenProvider.WIKITEXT_REGION_END))) {
				regionMarkupLevel--;
				if (regionMarkupLevel == 0) {
					handleIndent(indents.get(i));
					handleLexEvent(new WikitextLexEvent(trim,
							WikitextLexEvent.REGION_END));
				} else
					handleLexEvent(new WikitextLexEvent(line,
							WikitextLexEvent.REGION_STRING));
			} else
				handleLexEvent(new WikitextLexEvent(line,
						WikitextLexEvent.REGION_STRING));
		} else if (trim.startsWith(WikitextTokenProvider.getInstance()
				.getKeyword(WikitextTokenProvider.WIKITEXT_TABLE_DIVIDER))
				&& trim.endsWith(WikitextTokenProvider.getInstance()
						.getKeyword(
								WikitextTokenProvider.WIKITEXT_TABLE_DIVIDER))) {
			if (isDefList)
				handleDeflistEnd(i);
			int indent = indents.get(i);
			if (indent != curTableIndent) {
				if (curTableIndent > -1)
					handleTableEnd(i - 1);
				handleLexEvent(new WikitextLexEvent("",
						WikitextLexEvent.REGION_START));
				curTableIndent = indent;
			}
			handleLexEvent(new WikitextLexEvent(line,
					WikitextLexEvent.REGION_STRING));
			if (i == indents.size() - 1)
				handleTableEnd(i);
		} else {
			final String keyword = WikitextTokenProvider
					.getInstance()
					.getKeyword(WikitextTokenProvider.WIKITEXT_DEF_LIST_DIVIDER);
			final int indexOfDivider = trim.indexOf(keyword);
			// shouldAppendNextLine = trim.endsWith(keyword.trim());
			if (((indexOfDivider > 1
					&& indexOfDivider < trim.length() - keyword.length() || trim
					.endsWith(keyword.trim())) && indents.get(i) > 0)
					|| i <= lastAccumulatedLineNum) {
				if (curTableIndent > -1) {
					handleTableEnd(i - 1);
				}
				if (!isDefList) {
					handleLexEvent(new WikitextLexEvent("",
							WikitextLexEvent.REGION_START));
					isDefList = true;
				}
				if (trim.endsWith(keyword.trim()) && i < indents.size() - 1) // need
				// to
				// append
				// aftergoing
				// lines
				// to
				// this
				// definition
				{
					int defaultIndent = indents.get(i + 1);
					for (lastAccumulatedLineNum = i + 1; lastAccumulatedLineNum < indents
							.size()
							&& indents.get(lastAccumulatedLineNum) == defaultIndent; lastAccumulatedLineNum++)
						;
				}
				handleLexEvent(new WikitextLexEvent(emulateIndentStr(indents
						.get(i))
						+ line, WikitextLexEvent.REGION_STRING));
				if (i == indents.size() - 1)
					handleDeflistEnd(i);

			} else {
				if (curTableIndent > -1)
					handleTableEnd(i - 1);
				if (isDefList) {
					handleDeflistEnd(i - 1);
				}
				if (line.trim().length() == 0) {
					if (i > 0 && i < sourceLineCount - 1
							&& bullets.get(i - 1).length() > 0
							&& bullets.get(i + 1).length() > 0)
						handleLexEvent(new WikitextLexEvent("",
								WikitextLexEvent.BREAK_NEWLINE));
					else
						handleLexEvent(new WikitextLexEvent("",
								WikitextLexEvent.ADDITIONAL_NEWLINE));
				} else
					process(new StringReader(line), line.length());
			}
		}

	}

	private void handleTableEnd(int i) {
		curTableIndent = -1;
		handleIndent(indents.get(i));
		handleLexEvent(new WikitextLexEvent("", WikitextLexEvent.TABLE_END));
	}

	private void handleDeflistEnd(int i) {
		isDefList = false;
		// handleIndent(indents.get(i));
		handleIndent(DEFAULT_DEFLIST_INDENT); // Because this indent at
		// displayed wikipage seems to
		// be always 1, despite
		// indenting spaces count
		handleLexEvent(new WikitextLexEvent("", WikitextLexEvent.DEFLIST_END));
	}

	/**
	 * Method for processing Reader provided contents
	 * 
	 * @param reader
	 *            Reader
	 * @param charCount
	 *            char count to read
	 * @throws IOException
	 *             in case of i/o error
	 */
	public void process(Reader reader, int charCount) throws IOException {
		t = new char[(int) charCount];
		int read = reader.read(t, 0, (int) charCount);
		while (read != charCount && read != -1) {
			read += reader.read(t, read, charCount - read);
		}
		int indent = indents.get(currentSourceLineIdx);
		handleIndent(indent);
		final String bullet = bullets.get(currentSourceLineIdx);
		if (bullet.startsWith("* "))
			handleLexEvent(new WikitextLexEvent(bullet,
					WikitextLexEvent.BULLETED_LIST));
		else if (bullet.length() > 0/* && !bullet.equals(">") */)
			handleLexEvent(new WikitextLexEvent(bullet,
					WikitextLexEvent.NUMBERED_LIST));
		scaner();

		if (currentSourceLineIdx == sourceLineCount - 1
				|| (bullets.get(currentSourceLineIdx + 1).length() > 0)
				|| (indents.get(currentSourceLineIdx + 1) < indent || indents
						.get(currentSourceLineIdx + 1) > indent
						+ bullet.length())) {
			handleLexEvent(new WikitextLexEvent("",
					WikitextLexEvent.EOL_REACHED));
		} else
			handleLexEvent(new WikitextLexEvent(" ",
					WikitextLexEvent.OPTIONAL_WHITESPACE));
		textScanned = true;
	}

	public NewWikitextScanner() {
	}

	protected int tryToMatchNumberedListMarkup(int pos) {
		if (Character.isDigit(t[pos])) {
			pos++;
			while (Character.isDigit(t[pos]))
				pos++;
		} else if (t[pos] >= 'a' && t[pos] <= 'z') {
			pos++;
			if (t[pos] == 'i' || t[pos] == 'x' || t[pos] == 'v')
				while (t[pos] == 'i' || t[pos] == 'x' || t[pos] == 'v')
					pos++;
		}
		if (pos > 0 && t[pos] == '.')
			return pos;
		return 0;
	}

	protected int tryToMatchBulletedListMarkup(int pos) {
		if (t[pos] == '*') {
			pos++;
		}
		if (pos > 0 && t[pos] == ' ')
			return pos;
		return 0;
	}

	protected void handleIndent(int indent) {
		handleLexEvent(new WikitextLexEvent(emulateIndentStr(indent),
				WikitextLexEvent.INDENT));
	}

	protected String emulateIndentStr(int indent) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indent; i++)
			sb.append(' ');
		return sb.toString();
	}

}
