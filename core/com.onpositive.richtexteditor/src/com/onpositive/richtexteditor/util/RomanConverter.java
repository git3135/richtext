package com.onpositive.richtexteditor.util;


public class RomanConverter
{
	public static String converts(int n)
	{
		return (convert1000s(n) + convert100s(n) + convert10s(n) + convert1s(n));
	}

	private static String convert1000s(int n)
	{
		return ("MMMM".substring(0, n / 1000));
	}

	private static String convert100s(int h)
	{
		String m = "";
		h = (h % 1000) / 100;
		if (h == 9)
		{
			m = "CM";
		} else if (h > 4)
		{
			m = "DCCC".substring(0, h - 4);
		} else if (h == 4)
		{
			m = "CD";
		} else
		{
			m = "CCC".substring(0, h);
		};
		return m;
	}

	private static String convert100a(int h)
	{
		String m = "";
		h = (h % 1000) / 100;
		if (h > 4)
		{
			m = "DCCCC".substring(0, h - 4);
		} else
		{
			m = "CCCC".substring(0, h);
		};
		return m;
	}

	private static String convert10s(int t)
	{
		String m = "";
		t = (t % 100) / 10;
		if (t == 9)
		{
			m = "XC";
		} else if (t > 4)
		{
			m = "LXXX".substring(0, t - 4);
		} else if (t == 4)
			m = "XL";
		else
			m = "XXX".substring(0, t);
		return m;
	}

	private static String convert10a(int t)
	{
		String m = "";
		t = (t % 100) / 10;
		if (t > 4)
		{
			m = "LXXXX".substring(0, t - 4);
		} else
		{
			m = "XXXX".substring(0, t);
		};
		return m;
	}

	private static String convert1s(int u)
	{
		String m = "";
		u = u % 10;
		if (u == 9)
		{
			m = "IX";
		} else if (u > 4)
		{
			m = "VIII".substring(0, u - 4);
		} else if (u == 4)
		{
			m = "IV";
		} else
		{
			m = "III".substring(0, u);
		};
		return m;
	}

	private static String convert1a(int u)
	{
		String m = "";
		u = u % 10;
		if (u > 4)
		{
			m = "VIIII".substring(0, u - 4);
		} else
		{
			m = "IIII".substring(0, u);
		};
		return m;
	}

}
