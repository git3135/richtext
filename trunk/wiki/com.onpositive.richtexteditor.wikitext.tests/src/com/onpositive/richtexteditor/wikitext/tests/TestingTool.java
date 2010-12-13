package com.onpositive.richtexteditor.wikitext.tests;


public class TestingTool
{
	public static String normalizeSrcStr(String str)
	{
		str = str.replaceAll("\\r\\n", "\n");
		str = str.replaceAll(" \\n", "\n");
		str = str.replaceAll("[\\n]+\\{\\{\\{", "\n{{{");
		str = str.replaceAll("=[\\n]+", "=\n");
		int len2 = str.length();
		int i = len2 - 1;
		for (; i >= 0 && Character.isWhitespace(str.charAt(i)); i--)
		str = str.substring(0,i + 1);
		return str;
	}
	
	public static String normalizeStr(String str)
	{
		str = str.replaceAll("[\\p{Blank}]+\\n","\n");
		str = str.replaceAll("[\\p{Blank}]+\\r","\r");
		str = str.replaceAll("\\r\\n","\n");
		str = str.replaceAll("<i>[\\w]*</i>","");
		str = str.replaceAll("<tt></tt>","");
		str = str.replaceAll("</tt>\\s+", "</tt>");
		str = str.replaceAll("</tt>\\n", "</tt> ");
		str = str.replaceAll("tration </a>", "tration</a>");
		str = str.replaceAll("<span class=\"\">\\n</span>","");
		str = str.replaceAll("\\n<a"," <a");
		str = str.replaceAll("\\n<i"," <i");
		str = str.replaceAll("\\n<tt"," <tt");
		str = str.replaceAll("/i>\\n","/i> ");		
		str = str.replaceAll(" <i>\\n</i>","");
		str = str.replaceAll("</a>\\n","</a> ");
		str = str.replaceAll("</p>\\n</dd>","</dd>");
		str = str.replaceAll("</p>\\n<p>","");
		str = str.replaceAll("> \\{\\{\\{",">{{{");
		str = str.replaceAll("<br />\\n","<br /> ");
		str = str.replaceAll("<i>[\\s]+</i>","");
		str = str.replaceAll("</i> </p>","</i></p>");
		str = str.replaceAll("/strong>\\n</p","/strong></p");
		str = str.replaceAll("strong> --", "strong>--");
		str = str.replaceAll("</p>\\n<dl>","<dl>");
		str = str.replaceAll("[.]\\s</p",".</p");
		str = str.replaceAll(".\\n<s",". <s");
		str = str.replaceAll("logging[.]</p>","logging.");
		str = str.replaceAll("etc[.]</p>","etc.");
		str = str.replaceAll("\\s\\s"," ");
		
		StringBuilder sb = new StringBuilder();
		final int length = str.length();
		for (int i = 0; i < length; i++)
		{			
			if (i > 0 && i < length - 1 && str.charAt(i) == '\n' 
				&& str.charAt(i - 1) != '>' && str.charAt(i - 1) != '\n' && str.charAt(i + 1) != '<' && str.charAt(i + 1) != '\n')
				sb.append(" ");
			else
				sb.append(str.charAt(i));
		}		
		return sb.toString();
	}
}
