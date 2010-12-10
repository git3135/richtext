package com.onpositive.richtexteditor.model;

import com.onpositive.richtext.model.meta.Point;

public class SimpleLinkSearcher {

	String[] prefixes;

	public SimpleLinkSearcher(String... pp) {
		prefixes = pp;
	}

	public Point next(String text, int from) {
		int min = -1;
		for (String s : prefixes) {
			int indexOf = text.indexOf(s, from);
			if (indexOf != -1) {
				if (!(indexOf == 0 || Character.isWhitespace(text
						.charAt(indexOf - 1)))) {
					indexOf = -1;
				}
			}
			if (min == -1) {
				min = indexOf;
			} else {
				if (indexOf != -1) {
					min = Math.min(indexOf, min);
				}
			}
		}
		if (min > -1) {
			for (int a = min; a < text.length(); a++) {
				if (Character.isWhitespace(text.charAt(a))) {
					return new Point(min, a);
				}
			}
			return new Point(min, text.length());
		}
		return null;
	}
}
