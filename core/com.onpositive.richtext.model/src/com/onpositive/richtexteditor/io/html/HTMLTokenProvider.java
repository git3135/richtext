package com.onpositive.richtexteditor.io.html;

import java.util.HashMap;
import java.util.HashSet;

import com.onpositive.richtexteditor.io.TokenProvider;


public class HTMLTokenProvider extends TokenProvider
{ 
	public static final int TYPE_END = 0;
	public static final int TYPE_UNKNOWN = 101;
	public static final int TYPE_TEXT = 100;
	
	public static final int TYPE_HTML = 1;
	public static final int TYPE_BODY = 2;
	public static final int TYPE_P = 3;
	public static final int TYPE_B = 4;
	public static final int TYPE_I = 5;
	public static final int TYPE_U = 6;
	public static final int TYPE_STRIKE = 7;
	public static final int TYPE_FONT = 8;
	public static final int TYPE_IMG = 9;
	public static final int TYPE_SPAN = 10;
	
	public static final int TYPE_BR = 11;
	public static final int TYPE_OL = 12;
	public static final int TYPE_UL = 13;
	public static final int TYPE_LI = 14;
	public static final int TYPE_STYLE = 15;
	public static final int TYPE_XML = 16;
	public static final int TYPE_H1 = 17;
	public static final int TYPE_H2 = 18;
	public static final int TYPE_H3 = 19;
	public static final int TYPE_A = 20;		
	public static final int TYPE_SCRIPT = 21;
	public static final int TYPE_TITLE = 22;
	
	public static final int TYPE_DIV = 23;
	public static final int TYPE_TR = 24;
	public static final int TYPE_TABLE = 25;
	public static final int TYPE_TH = 26;
	public static final int TYPE_HR = 27;
	public static final int TYPE_CODE = 28;
	public static final int TYPE_PRE = 29;
	public static final int TYPE_SUP = 30;
	public static final int TYPE_SUB = 31;
	
	public static final int MIN_ATTR_TYPE = 50;
	
	public static final int ATTR_TYPE_HEIGHT = 50;
	public static final int ATTR_TYPE_FACE = 51;
	public static final int ATTR_TYPE_ALIGN = 52;
	public static final int ATTR_TYPE_COLOR = 53;
	public static final int ATTR_TYPE_STYLE = 54;
	public static final int ATTR_TYPE_HREF = 55;
	public static final int ATTR_TYPE_SRC = 56;
	public static final int ATTR_TYPE_TYPE = 57;
	public static final int ATTR_TYPE_CLASS = 58;

	
	protected static final int LAST_TOKEN_CONST_INDEX = 100;
	public static final String RICH_TEXT_HTML_CLASS_NAME = "RichText";
	
	protected HashMap<String, String> colorConstants;

	static
	{
		
	}

	
	private static HTMLTokenProvider instance = null;
	
	public static HTMLTokenProvider getInstance()
	{
		if (instance == null)
		{
			instance = new HTMLTokenProvider();
		}
		return instance;
	}
	
	protected HashMap<String, Integer> mirrorKeywords; //Because in our HTML parsing/serializing scheme
													   //We need both conversions - number -> keyword and keyword -> number
	
	protected HTMLTokenProvider()
	{
		keywords = new HashMap<Integer, String>(LAST_TOKEN_CONST_INDEX * 2);
		mirrorKeywords = new HashMap<String, Integer>(LAST_TOKEN_CONST_INDEX * 2);
		keySymbols = new HashSet<Character>(LAST_TOKEN_CONST_INDEX * 2);
		addKeyword(TYPE_HTML, "html");
		addKeyword(TYPE_BODY, "body");
		addKeyword(TYPE_P, "p");
		addKeyword(TYPE_B, "b");
		addKeyword(TYPE_I, "i");
		addKeyword(TYPE_U, "u");
		addKeyword(TYPE_I, "em" );
		addKeyword(TYPE_STRIKE,"strike");
		addKeyword(TYPE_FONT,"font");
		addKeyword(TYPE_IMG,"img");
		addKeyword(TYPE_SPAN,"span");
		addKeyword(TYPE_BR,"br");
		addKeyword(TYPE_OL,"ol");
		addKeyword(TYPE_UL,"ul");
		addKeyword(TYPE_LI,"li");
		addKeyword(TYPE_STYLE,"style");
		addKeyword(TYPE_XML,"xml");
		addKeyword(TYPE_H1,"h1");
		addKeyword(TYPE_H2,"h2");
		addKeyword(TYPE_H3,"h3");
		addKeyword(TYPE_HR,"hr");
		addKeyword(TYPE_A,"a");
		addKeyword(TYPE_SCRIPT,"script");
		addKeyword(TYPE_TITLE,"title");
		addKeyword(TYPE_DIV,"div");
		addKeyword(TYPE_TR,"tr");
		addKeyword(TYPE_TABLE,"table");
		addKeyword(TYPE_CODE,"code");
		addKeyword(TYPE_PRE,"pre");
		addKeyword(TYPE_SUB,"sub");
		addKeyword(TYPE_SUP,"sup");
		
		
		
		addKeyword(ATTR_TYPE_HEIGHT, "height");
		addKeyword(ATTR_TYPE_FACE, "face");
		addKeyword(ATTR_TYPE_ALIGN, "align");
		addKeyword(ATTR_TYPE_COLOR, "color");
		addKeyword(ATTR_TYPE_STYLE, "style");
		addKeyword(ATTR_TYPE_HREF, "href");
		addKeyword(ATTR_TYPE_SRC, "src");
		addKeyword(ATTR_TYPE_TYPE, "type");
		addKeyword(ATTR_TYPE_CLASS, "class");
		
		
		colorConstants = new HashMap<String, String>();
		colorConstants.put(String.valueOf("AliceBlue").toLowerCase(), "#F0F8FF");
		colorConstants.put(String.valueOf("AntiqueWhite").toLowerCase(), "#FAEBD7");
		colorConstants.put(String.valueOf("Aqua").toLowerCase(), "#00FFFF");
		colorConstants.put(String.valueOf("Aquamarine").toLowerCase(), "#7FFFD4");
		colorConstants.put(String.valueOf("Azure").toLowerCase(), "#F0FFFF");
		colorConstants.put(String.valueOf("Beige").toLowerCase(), "#F5F5DC");
		colorConstants.put(String.valueOf("Bisque").toLowerCase(), "#FFE4C4");
		colorConstants.put(String.valueOf("Black").toLowerCase(), "#000000");
		colorConstants.put(String.valueOf("BlanchedAlmond").toLowerCase(), "#FFEBCD");
		colorConstants.put(String.valueOf("Blue").toLowerCase(), "#0000FF");
		colorConstants.put(String.valueOf("BlueViolet").toLowerCase(), "#8A2BE2");
		colorConstants.put(String.valueOf("Brown").toLowerCase(), "#A52A2A");
		colorConstants.put(String.valueOf("BurlyWood").toLowerCase(), "#DEB887");
		colorConstants.put(String.valueOf("CadetBlue").toLowerCase(), "#5F9EA0");
		colorConstants.put(String.valueOf("Chartreuse").toLowerCase(), "#7FFF00");
		colorConstants.put(String.valueOf("Chocolate").toLowerCase(), "#D2691E");
		colorConstants.put(String.valueOf("Coral").toLowerCase(), "#FF7F50");
		colorConstants.put(String.valueOf("CornflowerBlue").toLowerCase(), "#6495ED");
		colorConstants.put(String.valueOf("Cornsilk").toLowerCase(), "#FFF8DC");
		colorConstants.put(String.valueOf("Crimson").toLowerCase(), "#DC143C");
		colorConstants.put(String.valueOf("Cyan").toLowerCase(), "#00FFFF");
		colorConstants.put(String.valueOf("DarkBlue").toLowerCase(), "#00008B");
		colorConstants.put(String.valueOf("DarkCyan").toLowerCase(), "#008B8B");
		colorConstants.put(String.valueOf("DarkGoldenRod").toLowerCase(), "#B8860B");
		colorConstants.put(String.valueOf("DarkGray").toLowerCase(), "#A9A9A9");
		colorConstants.put(String.valueOf("DarkGreen").toLowerCase(), "#006400");
		colorConstants.put(String.valueOf("DarkKhaki").toLowerCase(), "#BDB76B");
		colorConstants.put(String.valueOf("DarkMagenta").toLowerCase(), "#8B008B");
		colorConstants.put(String.valueOf("DarkOliveGreen").toLowerCase(), "#556B2F");
		colorConstants.put(String.valueOf("Darkorange").toLowerCase(), "#FF8C00");
		colorConstants.put(String.valueOf("DarkOrchid").toLowerCase(), "#9932CC");
		colorConstants.put(String.valueOf("DarkRed").toLowerCase(), "#8B0000");
		colorConstants.put(String.valueOf("DarkSalmon").toLowerCase(), "#E9967A");
		colorConstants.put(String.valueOf("DarkSeaGreen").toLowerCase(), "#8FBC8F");
		colorConstants.put(String.valueOf("DarkSlateBlue").toLowerCase(), "#483D8B");
		colorConstants.put(String.valueOf("DarkSlateGray").toLowerCase(), "#2F4F4F");
		colorConstants.put(String.valueOf("DarkTurquoise").toLowerCase(), "#00CED1");
		colorConstants.put(String.valueOf("DarkViolet").toLowerCase(), "#9400D3");
		colorConstants.put(String.valueOf("DeepPink").toLowerCase(), "#FF1493");
		colorConstants.put(String.valueOf("DeepSkyBlue").toLowerCase(), "#00BFFF");
		colorConstants.put(String.valueOf("DimGray").toLowerCase(), "#696969");
		colorConstants.put(String.valueOf("DodgerBlue").toLowerCase(), "#1E90FF");
		colorConstants.put(String.valueOf("FireBrick").toLowerCase(), "#B22222");
		colorConstants.put(String.valueOf("FloralWhite").toLowerCase(), "#FFFAF0");
		colorConstants.put(String.valueOf("ForestGreen").toLowerCase(), "#228B22");
		colorConstants.put(String.valueOf("Fuchsia").toLowerCase(), "#FF00FF");
		colorConstants.put(String.valueOf("Gainsboro").toLowerCase(), "#DCDCDC");
		colorConstants.put(String.valueOf("GhostWhite").toLowerCase(), "#F8F8FF");
		colorConstants.put(String.valueOf("Gold").toLowerCase(), "#FFD700");
		colorConstants.put(String.valueOf("GoldenRod").toLowerCase(), "#DAA520");
		colorConstants.put(String.valueOf("Gray").toLowerCase(), "#808080");
		colorConstants.put(String.valueOf("Green").toLowerCase(), "#008000");
		colorConstants.put(String.valueOf("GreenYellow").toLowerCase(), "#ADFF2F");
		colorConstants.put(String.valueOf("HoneyDew").toLowerCase(), "#F0FFF0");
		colorConstants.put(String.valueOf("HotPink").toLowerCase(), "#FF69B4");
		colorConstants.put(String.valueOf("IndianRed").toLowerCase(), "#CD5C5C");
		colorConstants.put(String.valueOf("Indigo").toLowerCase(), "#4B0082");
		colorConstants.put(String.valueOf("Ivory").toLowerCase(), "#FFFFF0");
		colorConstants.put(String.valueOf("Khaki").toLowerCase(), "#F0E68C");
		colorConstants.put(String.valueOf("Lavender").toLowerCase(), "#E6E6FA");
		colorConstants.put(String.valueOf("LavenderBlush").toLowerCase(), "#FFF0F5");
		colorConstants.put(String.valueOf("LawnGreen").toLowerCase(), "#7CFC00");
		colorConstants.put(String.valueOf("LemonChiffon").toLowerCase(), "#FFFACD");
		colorConstants.put(String.valueOf("LightBlue").toLowerCase(), "#ADD8E6");
		colorConstants.put(String.valueOf("LightCoral").toLowerCase(), "#F08080");
		colorConstants.put(String.valueOf("LightCyan").toLowerCase(), "#E0FFFF");
		colorConstants.put(String.valueOf("LightGoldenRodYellow").toLowerCase(), "#FAFAD2");
		colorConstants.put(String.valueOf("LightGrey").toLowerCase(), "#D3D3D3");
		colorConstants.put(String.valueOf("LightGreen").toLowerCase(), "#90EE90");
		colorConstants.put(String.valueOf("LightPink").toLowerCase(), "#FFB6C1");
		colorConstants.put(String.valueOf("LightSalmon").toLowerCase(), "#FFA07A");
		colorConstants.put(String.valueOf("LightSeaGreen").toLowerCase(), "#20B2AA");
		colorConstants.put(String.valueOf("LightSkyBlue").toLowerCase(), "#87CEFA");
		colorConstants.put(String.valueOf("LightSlateGray").toLowerCase(), "#778899");
		colorConstants.put(String.valueOf("LightSteelBlue").toLowerCase(), "#B0C4DE");
		colorConstants.put(String.valueOf("LightYellow").toLowerCase(), "#FFFFE0");
		colorConstants.put(String.valueOf("Lime").toLowerCase(), "#00FF00");
		colorConstants.put(String.valueOf("LimeGreen").toLowerCase(), "#32CD32");
		colorConstants.put(String.valueOf("Linen").toLowerCase(), "#FAF0E6");
		colorConstants.put(String.valueOf("Magenta").toLowerCase(), "#FF00FF");
		colorConstants.put(String.valueOf("Maroon").toLowerCase(), "#800000");
		colorConstants.put(String.valueOf("MediumAquaMarine").toLowerCase(), "#66CDAA");
		colorConstants.put(String.valueOf("MediumBlue").toLowerCase(), "#0000CD");
		colorConstants.put(String.valueOf("MediumOrchid").toLowerCase(), "#BA55D3");
		colorConstants.put(String.valueOf("MediumPurple").toLowerCase(), "#9370D8");
		colorConstants.put(String.valueOf("MediumSeaGreen").toLowerCase(), "#3CB371");
		colorConstants.put(String.valueOf("MediumSlateBlue").toLowerCase(), "#7B68EE");
		colorConstants.put(String.valueOf("MediumSpringGreen").toLowerCase(), "#00FA9A");
		colorConstants.put(String.valueOf("MediumTurquoise").toLowerCase(), "#48D1CC");
		colorConstants.put(String.valueOf("MediumVioletRed").toLowerCase(), "#C71585");
		colorConstants.put(String.valueOf("MidnightBlue").toLowerCase(), "#191970");
		colorConstants.put(String.valueOf("MintCream").toLowerCase(), "#F5FFFA");
		colorConstants.put(String.valueOf("MistyRose").toLowerCase(), "#FFE4E1");
		colorConstants.put(String.valueOf("Moccasin").toLowerCase(), "#FFE4B5");
		colorConstants.put(String.valueOf("NavajoWhite").toLowerCase(), "#FFDEAD");
		colorConstants.put(String.valueOf("Navy").toLowerCase(), "#000080");
		colorConstants.put(String.valueOf("OldLace").toLowerCase(), "#FDF5E6");
		colorConstants.put(String.valueOf("Olive").toLowerCase(), "#808000");
		colorConstants.put(String.valueOf("OliveDrab").toLowerCase(), "#6B8E23");
		colorConstants.put(String.valueOf("Orange").toLowerCase(), "#FFA500");
		colorConstants.put(String.valueOf("OrangeRed").toLowerCase(), "#FF4500");
		colorConstants.put(String.valueOf("Orchid").toLowerCase(), "#DA70D6");
		colorConstants.put(String.valueOf("PaleGoldenRod").toLowerCase(), "#EEE8AA");
		colorConstants.put(String.valueOf("PaleGreen").toLowerCase(), "#98FB98");
		colorConstants.put(String.valueOf("PaleTurquoise").toLowerCase(), "#AFEEEE");
		colorConstants.put(String.valueOf("PaleVioletRed").toLowerCase(), "#D87093");
		colorConstants.put(String.valueOf("PapayaWhip").toLowerCase(), "#FFEFD5");
		colorConstants.put(String.valueOf("PeachPuff").toLowerCase(), "#FFDAB9");
		colorConstants.put(String.valueOf("Peru").toLowerCase(), "#CD853F");
		colorConstants.put(String.valueOf("Pink").toLowerCase(), "#FFC0CB");
		colorConstants.put(String.valueOf("Plum").toLowerCase(), "#DDA0DD");
		colorConstants.put(String.valueOf("PowderBlue").toLowerCase(), "#B0E0E6");
		colorConstants.put(String.valueOf("Purple").toLowerCase(), "#800080");
		colorConstants.put(String.valueOf("Red").toLowerCase(), "#FF0000");
		colorConstants.put(String.valueOf("RosyBrown").toLowerCase(), "#BC8F8F");
		colorConstants.put(String.valueOf("RoyalBlue").toLowerCase(), "#4169E1");
		colorConstants.put(String.valueOf("SaddleBrown").toLowerCase(), "#8B4513");
		colorConstants.put(String.valueOf("Salmon").toLowerCase(), "#FA8072");
		colorConstants.put(String.valueOf("SandyBrown").toLowerCase(), "#F4A460");
		colorConstants.put(String.valueOf("SeaGreen").toLowerCase(), "#2E8B57");
		colorConstants.put(String.valueOf("SeaShell").toLowerCase(), "#FFF5EE");
		colorConstants.put(String.valueOf("Sienna").toLowerCase(), "#A0522D");
		colorConstants.put(String.valueOf("Silver").toLowerCase(), "#C0C0C0");
		colorConstants.put(String.valueOf("SkyBlue").toLowerCase(), "#87CEEB");
		colorConstants.put(String.valueOf("SlateBlue").toLowerCase(), "#6A5ACD");
		colorConstants.put(String.valueOf("SlateGray").toLowerCase(), "#708090");
		colorConstants.put(String.valueOf("Snow").toLowerCase(), "#FFFAFA");
		colorConstants.put(String.valueOf("SpringGreen").toLowerCase(), "#00FF7F");
		colorConstants.put(String.valueOf("SteelBlue").toLowerCase(), "#4682B4");
		colorConstants.put(String.valueOf("Tan").toLowerCase(), "#D2B48C");
		colorConstants.put(String.valueOf("Teal").toLowerCase(), "#008080");
		colorConstants.put(String.valueOf("Thistle").toLowerCase(), "#D8BFD8");
		colorConstants.put(String.valueOf("Tomato").toLowerCase(), "#FF6347");
		colorConstants.put(String.valueOf("Turquoise").toLowerCase(), "#40E0D0");
		colorConstants.put(String.valueOf("Violet").toLowerCase(), "#EE82EE");
		colorConstants.put(String.valueOf("Wheat").toLowerCase(), "#F5DEB3");
		colorConstants.put(String.valueOf("White").toLowerCase(), "#FFFFFF");
		colorConstants.put(String.valueOf("WhiteSmoke").toLowerCase(), "#F5F5F5");
		colorConstants.put(String.valueOf("Yellow").toLowerCase(), "#FFFF00");
		colorConstants.put(String.valueOf("YellowGreen").toLowerCase(), "#9ACD32");
	}
	
	
	public void addKeyword(int keyConstant, String string)
	{		
		string = string.toLowerCase();
		super.addKeyword(keyConstant, string);
		mirrorKeywords.put(string, keyConstant);
	}
	
	public int getKeywordConstant(String word)
	{
		word = word.toLowerCase().trim();
		Integer constant = mirrorKeywords.get(word);
		if (constant != null)
			return constant;
		return TYPE_UNKNOWN;
	}
	
	public String getColorConstantHexStr(String constant)
	{
		return colorConstants.get(constant);
	}
	

}
