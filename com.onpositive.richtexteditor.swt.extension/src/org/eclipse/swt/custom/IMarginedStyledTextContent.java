package org.eclipse.swt.custom;

public interface IMarginedStyledTextContent extends StyledTextContent, Cloneable {
    /**
     * Returns the wrap width in number of characters.
     * 
     * @param lineIndex
     *            The line we want the wrap width for
     * @return The maximum number of characters in that line (after those, soft wrap should happen).
     */
    int getWrapWidthAt(int lineIndex);

    /**
     * Returns the left margin size in number of characters.
     * 
     * @param lineIndex
     *            The line we want the left margin for
     * @return The number of characters used to create the margin
     */
    int getMarginAt(int lineIndex);

    /**
     * @return The number of characters used to create the base margin (used by all contents)
     */
    int getBaseMargin();

    /**
     * Returns the visible line spacing in character heights.
     * 
     * @param lineIndex
     *            The line we want the line spacing for
     * @return the line spacing (for wrapped, screen-visible lines) in character heights
     */
    double getLineSpacing(int lineIndex);

    /**
     * Returns the paragraph (logical, Document line) spacing in character heights.
     * 
     * @param lineIndex
     *            The line we want the line spacing for
     * @return the paragraph (logical, Document line) spacing in character heights
     */
    double getParagraphSpacing(int lineIndex);

    public Object clone() throws CloneNotSupportedException;

    /**
     * Returns the alignment (SWT.LEFT, SWT.CENTER or SWT.RIGHT) of the specified line.
     * 
     * @param lineIndex
     *            The line we want the alignment for
     * @return the alignment (SWT.LEFT, SWT.CENTER or SWT.RIGHT) of the specified line
     */
    int getParagraphAlignment(int lineIndex);
}
