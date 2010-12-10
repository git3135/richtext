// Jericho HTML Parser - Java based library for analysing and manipulating HTML
// Version 2.6
// Copyright (C) 2007 Martin Jericho
// http://jerichohtml.sourceforge.net/
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of either one of the following licences:
//
// 1. The Eclipse Public License (EPL) version 1.0,
// included in this distribution in the file licence-epl-1.0.html
// or available at http://www.eclipse.org/legal/epl-v10.html
//
// 2. The GNU Lesser General Public License (LGPL) version 2.1 or later,
// included in this distribution in the file licence-lgpl-2.1.txt
// or available at http://www.gnu.org/licenses/lgpl.txt
//
// This library is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the individual licence texts for more details.
/*******************************************************************************
 * This class is based on corresponding class  from Jericho HTML parser
 * so we do not insert our license here
 *******************************************************************************/

package com.onpositive.richtexteditor.io.html_scaner;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kor
 * 
 */
public class CharacterMapper {

	private static Map<String, Integer> NAME_TO_CODE_POINT_MAP;

	static {
		try {
			NAME_TO_CODE_POINT_MAP = loadMap(CharacterMapper.class
					.getResourceAsStream("point.map"));
		} catch (IOException e) {
			throw new LinkageError();
		}
	}

	private static HashMap<String, Integer> loadMap(InputStream stream)
			throws IOException {

		DataInputStream s = new DataInputStream(new BufferedInputStream(stream));
		try {
			HashMap<String, Integer> res = new HashMap<String, Integer>();
			int readInt = s.readInt();
			for (int a = 0; a < readInt; a++) {
				String key = s.readUTF();
				int value = s.readInt();
				res.put(key, value);
			}
			return res;
		} finally {
			s.close();
		}
	}

	// /**
	// * @param args
	// */
	// public static void main(String[] args) {
	// try {
	// FileOutputStream c = new FileOutputStream("D:/ch.map");
	// DataOutputStream m = new DataOutputStream(c);
	// m.writeInt(m.size());
	// for (String s : NAME_TO_CODE_POINT_MAP.keySet()) {
	// m.writeUTF(s);
	// m.writeInt(NAME_TO_CODE_POINT_MAP.get(s));
	// }
	// m.close();
	// DataInputStream s = new DataInputStream(new BufferedInputStream(
	// new FileInputStream("D:/ch.map")));
	// HashMap<String, Integer> res = new HashMap<String, Integer>();
	// int readInt = s.readInt();
	// for (int a = 0; a < readInt; a++) {
	// String key = s.readUTF();
	// int value = s.readInt();
	// res.put(key, value);
	// }
	// if (!NAME_TO_CODE_POINT_MAP.equals(res)){
	// System.out.println("EE");
	// }
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	/**
	 * @param name
	 *            name of character
	 * @return code point
	 */
	public static char getCodePointFromName(final String name) {
		Integer codePoint = (Integer) NAME_TO_CODE_POINT_MAP.get(name
				.toLowerCase());
		if (codePoint == null) {
			codePoint = (Integer) NAME_TO_CODE_POINT_MAP
					.get(name.toLowerCase());
		}
		return (char) ((codePoint != null) ? codePoint.intValue() : '#');
	}

}
