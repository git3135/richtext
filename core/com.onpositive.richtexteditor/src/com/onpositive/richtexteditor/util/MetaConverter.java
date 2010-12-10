package com.onpositive.richtexteditor.util;

import java.lang.reflect.Field;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.widgets.Display;

import com.onpositive.richtext.model.meta.BasicBullet;
import com.onpositive.richtext.model.meta.BulletFactory;
import com.onpositive.richtext.model.meta.Point;
import com.onpositive.richtext.model.meta.RGB;
import com.onpositive.richtexteditor.model.resources.ColorManager;
import com.onpositive.richtexteditor.model.resources.FontStyleManager;
import com.onpositive.richtexteditor.model.resources.LayerManager;

/**
 * @author kor
 * 
 */
public class MetaConverter {

	/**
	 * @param rgb
	 * @return rgb
	 */
	public static org.eclipse.swt.graphics.RGB convertRGB(RGB rgb) {
		if (rgb == null) {
			return null;
		}
		return new org.eclipse.swt.graphics.RGB(rgb.red, rgb.green, rgb.blue);
	}

	public static RGB convertRGBBack(org.eclipse.swt.graphics.RGB rgb) {
		if (rgb == null) {
			return null;
		}
		return new RGB(rgb.red, rgb.green, rgb.blue);
	}
	static Field declaredField1;
	static Field declaredField;
	
	
	static Field bdeclaredField1;
	static Field bdeclaredField;
	static{
		try {
			declaredField1=Bullet.class.getDeclaredField("linesIndices");
			declaredField= Bullet.class.getDeclaredField("count");
			declaredField.setAccessible(true);
			declaredField1.setAccessible(true);
		} catch (SecurityException e) {
			throw new LinkageError();
		} catch (NoSuchFieldException e) {
			throw new LinkageError();
		}
	}
	
	static{
		try {
			bdeclaredField1=BasicBullet.class.getDeclaredField("linesIndices");
			bdeclaredField= BasicBullet.class.getDeclaredField("count");
			bdeclaredField.setAccessible(true);
			bdeclaredField1.setAccessible(true);
		} catch (SecurityException e) {
			throw new LinkageError();
		} catch (NoSuchFieldException e) {
			throw new LinkageError();
		}
	}
	
	
	public static Bullet convertBullet(
			com.onpositive.richtext.model.meta.BasicBullet bullet) {
		if (bullet == null) {
			return null;
		}
		if (bullet.getPlatformData() != null) {
			return (Bullet) bullet.getPlatformData();
		}
		Bullet bullet2 = new Bullet(bullet.type, convertRange(bullet.style,
				null));
		bullet2.text = bullet.getText();
		try {
			declaredField1.set(bullet2, bullet.getIndexes());
			declaredField.set(bullet2, bullet.getCount());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		// bullet2.
		bullet.setPlatformData(bullet2);
		return bullet2;
		// bullet.
	}

	/**
	 * @param font
	 * @param manager
	 * @return
	 */
	public static Font convertFont(
			com.onpositive.richtext.model.meta.Font font, LayerManager manager) {
		if (font == null) {
			return null;
		}
		FontRegistry fontRegistry = manager.getFontRegistry();
		String string = font.toString();
		if (fontRegistry.hasValueFor(string)) {
			return fontRegistry.get(font.toString());
		}
		Font t = new Font(Display.getCurrent(), font.getName(), (int) font
				.getHeight(), font.getStyle());
		fontRegistry.put(font.toString(), t.getFontData());
		t.dispose();
		return fontRegistry.get(font.toString());
	}

	/**
	 * @param styleRange
	 * @param manager
	 * @return converted range
	 */
	public static StyleRange convertRange(
			com.onpositive.richtext.model.meta.StyleRange styleRange,
			LayerManager manager) {
		if (styleRange == null) {
			return null;
		}
		StyleRange styleRange2 = new StyleRange();
		styleRange2.start = styleRange.start;
		styleRange2.length = styleRange.length;
		styleRange2.borderStyle = styleRange.borderStyle;
		styleRange2.data = styleRange.data;
		styleRange2.fontStyle = styleRange.fontStyle;
		styleRange2.rise = styleRange.rise;
		styleRange2.strikeout = styleRange.strikeout;
		styleRange2.underline = styleRange.underline;
		styleRange2.underlineStyle = styleRange.underlineStyle;
		styleRange2.borderColor = null;
		if (manager != null) {
			ColorManager colorRegistry = manager.getColorRegistry();
			if (colorRegistry != null) {
				if (styleRange.background != null) {
					styleRange2.background = colorRegistry
							.getColor(convertRGB((RGB) styleRange.background));
				}
				if (styleRange.foreground != null) {
					styleRange2.foreground = colorRegistry
							.getColor(convertRGB((RGB) styleRange.foreground));
				}
			}
			styleRange2.font = convertFont(styleRange.font, manager);
		}
		styleRange2.metrics = convertMetrics(styleRange.metrics);

		styleRange2.underlineColor = null;
		return styleRange2;
	}

	/**
	 * @param styleRange
	 * @param manager
	 * @return converted range
	 */
	public static com.onpositive.richtext.model.meta.StyleRange convertRangeBack(
			StyleRange styleRange, LayerManager manager) {
		if (styleRange == null) {
			return null;
		}
		com.onpositive.richtext.model.meta.StyleRange styleRange2 = new com.onpositive.richtext.model.meta.StyleRange();
		styleRange2.start = styleRange.start;
		styleRange2.length = styleRange.length;
		styleRange2.borderStyle = styleRange.borderStyle;
		styleRange2.data = styleRange.data;
		styleRange2.fontStyle = styleRange.fontStyle;
		styleRange2.rise = styleRange.rise;
		styleRange2.strikeout = styleRange.strikeout;
		styleRange2.underline = styleRange.underline;
		styleRange2.underlineStyle = styleRange.underlineStyle;
		styleRange2.borderColor = null;
		if (manager != null) {
			ColorManager colorRegistry = manager.getColorRegistry();
			if (colorRegistry != null) {
				if (styleRange.background != null) {
					styleRange2.background = convertRGBBack(styleRange.background
							.getRGB());
				}
				if (styleRange.foreground != null) {
					styleRange2.foreground = convertRGBBack(styleRange.foreground
							.getRGB());
				}
			}
			styleRange2.font = convertFontBack(styleRange.font, manager);
		}
		styleRange2.metrics = convertMetricsBack(styleRange.metrics);

		styleRange2.underlineColor = null;
		return styleRange2;
	}

	private static com.onpositive.richtext.model.meta.Font convertFontBack(
			Font font, LayerManager manager) {
		FontData fontData = font.getFontData()[0];
		return new com.onpositive.richtext.model.meta.Font(fontData.getName(),
				fontData.getHeight(), fontData.getStyle());
	}

	private static GlyphMetrics convertMetrics(
			com.onpositive.richtext.model.meta.GlyphMetrics metrics) {
		if (metrics == null) {
			return null;
		}
		return new GlyphMetrics(metrics.ascent, metrics.descent, metrics.width);
	}

	private static com.onpositive.richtext.model.meta.GlyphMetrics convertMetricsBack(
			GlyphMetrics metrics) {
		if (metrics == null) {
			return null;
		}
		return new com.onpositive.richtext.model.meta.GlyphMetrics(
				metrics.ascent, metrics.descent, metrics.width);
	}

	public static Point convertBack(org.eclipse.swt.graphics.Point selection) {
		if (selection == null) {
			return null;
		}
		return new Point(selection.x, selection.y);
	}

	public static com.onpositive.richtext.model.meta.BasicBullet convertBulletBack(
			Bullet lineBullet, BulletFactory bulletFactory) {
		if (lineBullet == null) {
			return null;
		}
		Object bulletForPlarform = bulletFactory.getBulletForPlarform(lineBullet);
		if (bulletForPlarform!=null){
			return (BasicBullet) bulletForPlarform;
		}
		BasicBullet basicBullet = new BasicBullet(lineBullet.type,convertRangeBack(lineBullet.style, null),bulletFactory);
		basicBullet.setText(lineBullet.text);
		
		try {
			
			bdeclaredField1.set(basicBullet,declaredField1.get(lineBullet));
			bdeclaredField.set(basicBullet, declaredField.get(lineBullet));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		bulletFactory.registerPlatformData(basicBullet, lineBullet);
		basicBullet.setPlatformData(lineBullet);
		return basicBullet;
	}
}
