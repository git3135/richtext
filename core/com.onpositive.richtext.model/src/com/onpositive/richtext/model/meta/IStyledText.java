package com.onpositive.richtext.model.meta;

public interface IStyledText {

	void setRedraw(boolean b);

	int getCaretOffset();

	void setSelection(int oldOffset);

	void redraw();

	int getLineCount();

	int getLineAlignment(int a);

	BasicBullet getLineBullet(int a);

	void setLineAlignment(int i, int j, int align);

	void setLineJustify(int i, int j, boolean b);

	void setLineBullet(int i, int j, BasicBullet bullet);

	void setStyleRanges(StyleRange[] styleRanges);

	int getTopPixel();

	Point getSize();

	int getLineHeight();

	void setTopPixel(int i);

	boolean getLineJustify(int lastLine);

	String getLine(int inheritAlignLineNum);

	void setCaretOffset(int i);

	int getLineAtOffset(int offset);

}
