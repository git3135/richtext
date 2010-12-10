package org.eclipse.swt.graphics;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.internal.Platform;
import org.osgi.framework.Bundle;

public class TextLayoutOps {

	public static void shiftLines(TextLayout layout, int line, int delta) {
		
		layout.lineHeight[line] += delta;
	}

	public static int getLineY(TextLayout layout, int line) {
		int y = 0;
		for (int a = 0; a < line; a++) {
			y += layout.lineHeight[a];
		}
		return y;
	}

	public static int getHeight(TextLayout t) {
		if (t.lineHeight != null) {
			int result = 0;
			for (int a = 0; a < t.lineHeight.length; a++) {
				result += t.lineHeight[a];
			}
			return result;
		}
		return t.getBounds().height >= 0 ? t.getBounds().height : 0;
	}

	public static int getLineHeight(TextLayout textLayout, int a) {
		return textLayout.lineHeight[a];

	}

	public static int getAdditionalHeight(TextLayout layout) {
		int sum = 0;
		for (int i = 0; i < layout.lineHeight.length; i++)
			sum += layout.lineAscent[i];
		return sum;
	}

	public static int getLineAscent(TextLayout layout, int a) {
		if (layout.lineAscent == null) {
			return layout.ascent;
		}
		return layout.lineAscent[a];
	}

	public static void setLineAscent(TextLayout textLayout, int a, int i) {
		textLayout.lineAscent[a] = i;

	}

	public static boolean needsSpacing(TextLayout layout,
			TextLayout localLayout, int height, int practicalHeight) {
		return height == practicalHeight;
	}

	public static void setFirstLineSpacing(TextLayout textLayout,
			TextLayout localLayout, int paragraphSpacing) {
		TextLayoutOps.setLineAscent(textLayout, 0, paragraphSpacing
				+ textLayout.ascent);
		TextLayoutOps.shiftLines(textLayout, 0, paragraphSpacing);
	}

	public static int getExtra() {
		return 1;
	}

	public static void forceComputeRuns(TextLayout localLayout, GC gc) {
		localLayout.computeRuns();

	}

	public static boolean needToDrawFirstInvivsibleLine() {
		return true;
	}

	public static Point getScreenSize() {
		try {
			Bundle bundle = org.eclipse.core.runtime.Platform
					.getBundle("com.onpositive.swt.extension.macosx");
			File bundleFile = FileLocator.getBundleFile(bundle);
			String path = "/test";
			if (bundleFile.isDirectory())
				path = bundleFile.getAbsolutePath() + path;
			else
				path = bundleFile.getParent() + path;
			ProcessBuilder builder = new ProcessBuilder(path);
			Process start = builder.start();
			InputStream inputStream = start.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			ArrayList<String> strings = new ArrayList<String>();
			start.waitFor();
			while (reader.ready()) {
				String line = reader.readLine();
				strings.add(line);
			}
			if (strings.size() > 0) {
				String output = strings.get(0);
				String str1 = output.substring(0, output.indexOf('.'));
				int lastIndexOf = output.lastIndexOf('.');
				String str2 = output.substring(output.indexOf(' ') + 1,
						lastIndexOf);
				return new Point((Integer.parseInt(str1)), Integer
						.parseInt(str2));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static Point getLocation(TextLayout layout, int offset,
			boolean trailing) {
		Point p = layout.getLocation(offset, trailing);
		return p;
	}
	public static Rectangle getBounds(TextLayout layout, int start, int end){
		Rectangle r = layout.getBounds( start,end);
		return r;
	}
	
	public static Rectangle getLineBounds(TextLayout layout, int index){
		Rectangle r = layout.getLineBounds(index);
		return r;
	}
	
	public static void main(String[] args) {
		new TextLayoutOps().getScreenSize();
	}

}
