package com.onpositive.richtext.model.meta;


/**
 * Instances of this class represent bullets in the <code>StyledText</code>.
 * <p>
 * The hashCode() method in this class uses the values of the public fields to
 * compute the hash value. When storing instances of the class in hashed
 * collections, do not modify these fields after the object has been inserted.
 * </p>
 * <p>
 * Application code does <em>not</em> need to explicitly release the resources
 * managed by each instance when those instances are no longer required, and
 * thus no <code>dispose()</code> method is provided.
 * </p>
 * 
 * @see StyledText#setLineBullet(int, int, BasicBullet)
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 * 
 * @since 3.2
 */
public class BasicBullet {

	/**
	 * Bullet style dot.
	 * 
	 * @see BasicBullet
	 * 
	 * @since 3.2
	 */
	public static final int BULLET_DOT = 1 << 0;

	/**
	 * Bullet style number.
	 * 
	 * @see BasicBullet
	 * 
	 * @since 3.2
	 */
	public static final int BULLET_NUMBER = 1 << 1;

	/**
	 * Bullet style lower case letter.
	 * 
	 * @see BasicBullet
	 * 
	 * @since 3.2
	 */
	public static final int BULLET_LETTER_LOWER = 1 << 2;

	/**
	 * Bullet style text.
	 * 
	 * @see BasicBullet
	 * 
	 * @since 3.2
	 */
	public static final int BULLET_TEXT = 1 << 4;

	/**
	 * Bullet style custom draw.
	 * 
	 * @see StyledText#addPaintObjectListener(PaintObjectListener)
	 * @see StyledText#removePaintObjectListener(PaintObjectListener)
	 * @see BasicBullet
	 * 
	 * @since 3.2
	 */
	public static final int BULLET_CUSTOM = 1 << 5;

	/**
	 * The bullet type. Possible values are:
	 * <ul>
	 * <li><code>ST.BULLET_DOT</code></li>
	 * <li><code>ST.BULLET_NUMBER</code></li>
	 * <li><code>ST.BULLET_LETTER_LOWER</code></li>
	 * <li><code>ST.BULLET_LETTER_UPPER</code></li>
	 * <li><code>ST.BULLET_TEXT</code></li>
	 * <li><code>ST.BULLET_CUSTOM</code></li>
	 * </ul>
	 */
	public final int type;

	/**
	 * The bullet style.
	 */
	public final StyleRange style;

	/**
	 * The bullet text.
	 */
	protected String text;

	int[] linesIndices;
	int count;

	private BulletFactory factory;

	/**
	 * Create a new bullet with the specified style, and type
	 * <code>ST.BULLET_DOT</code>. The style must have a glyph metrics set.
	 * 
	 * @param style
	 *            the style
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT when the style or the glyph
	 *                metrics are null</li>
	 *                </ul>
	 */
	public BasicBullet(StyleRange style,BulletFactory b) {
		this(BULLET_DOT, style,b);
	}

	/**
	 * Create a new bullet the specified style and type. The style must have a
	 * glyph metrics set.
	 * 
	 * @param type
	 *            the bullet type
	 * @param style
	 *            the style
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT when the style or the glyph
	 *                metrics are null</li>
	 *                </ul>
	 */
	public BasicBullet(int type, StyleRange style,BulletFactory f) {
		this.type = type;
		this.style = style;
		this.text = "";
		this.factory=f;
	}

	void addIndices(int startLine, int lineCount) {
		if (linesIndices == null) {
			linesIndices = new int[lineCount];
			count = lineCount;
			for (int i = 0; i < lineCount; i++)
				linesIndices[i] = startLine + i;
		} else {
			int modifyStart = 0;
			while (modifyStart < count) {
				if (startLine <= linesIndices[modifyStart])
					break;
				modifyStart++;
			}
			int modifyEnd = modifyStart;
			while (modifyEnd < count) {
				if (startLine + lineCount <= linesIndices[modifyEnd])
					break;
				modifyEnd++;
			}
			int newSize = modifyStart + lineCount + count - modifyEnd;
			if (newSize > linesIndices.length) {
				int[] newLinesIndices = new int[newSize];
				System.arraycopy(linesIndices, 0, newLinesIndices, 0, count);
				linesIndices = newLinesIndices;
			}
			System.arraycopy(linesIndices, modifyEnd, linesIndices, modifyStart
					+ lineCount, count - modifyEnd);
			for (int i = 0; i < lineCount; i++)
				linesIndices[modifyStart + i] = startLine + i;
			count = newSize;
		}
	}

	int indexOf(int lineIndex) {
		for (int i = 0; i < count; i++) {
			if (linesIndices[i] == lineIndex)
				return i;
		}
		return -1;
	}

	public int hashCode() {
		return style.hashCode() ^ type;
	}

	int[] removeIndices(int startLine, int replaceLineCount, int newLineCount,
			boolean update) {
		if (count == 0)
			return null;
		if (startLine > linesIndices[count - 1])
			return null;
		int endLine = startLine + replaceLineCount;
		int delta = newLineCount - replaceLineCount;
		for (int i = 0; i < count; i++) {
			int index = linesIndices[i];
			if (startLine <= index) {
				int j = i;
				while (j < count) {
					if (linesIndices[j] >= endLine)
						break;
					j++;
				}
				if (update) {
					for (int k = j; k < count; k++)
						linesIndices[k] += delta;
				}
				int[] redrawLines = new int[count - j];
				System.arraycopy(linesIndices, j, redrawLines, 0, count - j);
				System.arraycopy(linesIndices, j, linesIndices, i, count - j);
				count -= (j - i);
				return redrawLines;
			}
		}
		for (int i = 0; i < count; i++)
			linesIndices[i] += delta;
		return null;
	}

	int size() {
		return count;
	}

	protected Object platformData;

	public Object getPlatformData() {
		return platformData;
	}

	public void setPlatformData(Object platformData) {
		this.platformData = platformData;
		factory.registerPlatformData(this,platformData);		
	}

	public String getText() {
		return text;
	}

	public void setText(String text2) {
		this.text=text2;
	}

	public int getCount() {
		return count;
	}

	public int[] getIndexes() {
		return linesIndices;
	}
}
