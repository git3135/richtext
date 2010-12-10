package com.onpositive.richtexteditor.viewer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.ExtendedStyledText;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;

import com.onpositive.richtext.model.ILineAttributeModel;
import com.onpositive.richtext.model.ILineAttributeModelExtension;
import com.onpositive.richtext.model.meta.BasicBullet;
import com.onpositive.richtext.model.meta.BulletFactory;
import com.onpositive.richtexteditor.model.RichTextEditorConstants;
import com.onpositive.richtexteditor.model.resources.LayerManager;
import com.onpositive.richtexteditor.util.MetaConverter;

/**
 * The StyledTextLineAttributeModel - {@link ILineAttributeModel} implementation
 * for {@link StyledText}
 */
public class StyledTextLineAttributeModel implements ILineAttributeModel,
		ILineAttributeModelExtension {

	protected LayerManager manager;
	protected int indentSize;
	protected int defaultSpacing;
	protected StyledText widget;

	public StyledTextLineAttributeModel(LayerManager manager) {
		this.manager = manager;
		widget = manager.getActualTextWidget();
		indentSize = manager.getIndentSize();
		defaultSpacing = getDefaultLineSpacing();
	}

	public int getLineAlign(int line) {
		int lineAlignment = widget.getLineAlignment(line);
		boolean lineJustify = widget.getLineJustify(line);
		if (lineJustify) {
			return RichTextEditorConstants.FIT_ALIGN;
		}
		return lineAlignment;
	}

	public void setLineAlign(final int startLine, final int count, int align) {
		int firstLineNum = startLine;

		int lineCount = count;
		int maxLineCount = widget.getLineCount();
		if (startLine + lineCount < maxLineCount - 1)
			lineCount++;
		if (align == RichTextEditorConstants.FIT_ALIGN) {
			widget.setLineAlignment(firstLineNum, lineCount, SWT.LEFT);
			widget.setLineJustify(firstLineNum, lineCount, true);

		} else {
			widget.setLineJustify(firstLineNum, lineCount, false);
			// if (align == RichTextEditorConstants.LEFT_ALIGN){
			// widget.setLineAlignment(firstLineNum, lineCount, align);
			// }
			// else {
			int lastLine = startLine + count;
			try
			{
				for (int i = firstLineNum; i <= lastLine; i++) {
					// if (widget.getLineBullet(i) != null){
					// widget.setLineBullet(startLine, maxLineCount, bullet)
					// }
					widget.setLineAlignment(i, 1, align);
				}
			}
			catch (IllegalArgumentException e) {
			}

			// }
		}
		manager.getTextWidget().setLineAlignment(startLine, count, align);
	}

	Field renderer;
	Method mn;

	public com.onpositive.richtext.model.meta.BasicBullet getBullet(int line) {
		if (renderer == null) {
			try {
				renderer = StyledText.class.getDeclaredField("renderer");
				renderer.setAccessible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Object object = null;
		try {
			object = renderer.get(manager.getActualTextWidget());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (object != null) {
			if (mn == null) {
				Class<? extends Object> class1 = object.getClass();
				while (class1 != null) {
					try {
						mn = class1.getDeclaredMethod("getLineBullet",
								new Class[] { int.class, Bullet.class });
						mn.setAccessible(true);
						break;
					} catch (NoSuchMethodException e) {
						class1 = class1.getSuperclass();
					}
				}
			}
			try {
				return MetaConverter.convertBulletBack(
						(Bullet) mn.invoke(object, line, null),
						manager.getBulletFactory());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public void setLineBullet(int startLine, int count,
			com.onpositive.richtext.model.meta.BasicBullet bl) {
		if (count == 0)
			return;
		int oldStartLine = startLine;
		int oldCount = count;
		Bullet bullet = MetaConverter.convertBullet(bl);
		if (bullet != null && ((Bullet) bullet).type != ST.BULLET_DOT) {
			bullet = checkBeforeList(startLine, (Bullet) bullet);
			count = checkAfterList(oldStartLine + count, (Bullet) bullet)
					- oldStartLine;
			if (count != oldCount)
				setBulletsWithIndentChecking(getLineIndent(startLine + oldCount
						- 1), oldStartLine + oldCount, count - oldCount,
						(Bullet) bullet);
		}
		manager.getActualTextWidget().setLineBullet(oldStartLine, oldCount,
				(Bullet) null);
		if (bullet != null)
			manager.getActualTextWidget().setLineBullet(oldStartLine, oldCount,
					(Bullet) bullet);
	}

	protected void setBulletsWithIndentChecking(int lineIndent, int startLine,
			int count, Bullet bullet) {
		for (int i = startLine; i < startLine + count; i++) {
			int indent = getLineIndent(i);
			if (indent == lineIndent) {
				setBullet0(i, 1, (Bullet) null);
				setBullet0(i, 1, (Bullet) bullet);
			}

		}

	}

	private int checkAfterList(int firstAfterListLine, Bullet bullet) {
		if (firstAfterListLine == lineCount())
			return firstAfterListLine;
		int end = firstAfterListLine;
		Bullet oldBullet = ((Bullet) getBullet0(end));
		int oldIndent = getLineIndent(end);
		int indent = getLineIndent(end - 1);
		while (end < lineCount() - 1
				&& oldBullet != null
				&& (oldIndent > indent || (oldBullet.type == bullet.type && oldIndent == indent))) { // Go
			// forward,
			// search
			// bullets,
			// which
			// was
			// in
			// this
			// list
			// earlier
			end++;
			oldBullet = (Bullet) getBullet0(end);
			oldIndent = getLineIndent(end);
		}
		if (oldBullet == null
				|| (oldBullet.type != bullet.type && oldIndent == indent)) {
			end--;
			// oldBullet = (Bullet) getBullet(end);
			// oldIndent = getLineIndent(end);
		}
		/*
		 * if (end < lineCount() && oldBullet != null && oldBullet != bullet &&
		 * oldBullet.type == bullet.type && oldIndent == indent) { if
		 * (bullet.type == ST.BULLET_CUSTOM &&
		 * !oldBullet.text.equals(bullet.text)) return firstAfterListLine; while
		 * (end < lineCount() && getBullet(end) != null &&
		 * ((Bullet)getBullet(end)) == oldBullet && getLineIndent(end) ==
		 * oldIndent) end++; return end; }
		 */
		return end;
	}

	private Bullet checkBeforeList(int startLine, Bullet bullet) {
		if (startLine <= 0)
			return bullet;
		int top = startLine - 1;
		Bullet oldBullet = (Bullet) getBullet0(top);
		int indent = getLineIndent(startLine);
		int oldIndent = getLineIndent(top);
		while (top > 0 && oldBullet != null && oldIndent > indent) {// Go
			// to
			// the
			// top
			// of
			// list,
			// search
			// bullets,
			// which
			// was
			// in
			// this
			// list
			// earlier
			top--;
			oldBullet = (Bullet) getBullet0(top);
			oldIndent = getLineIndent(top);
		}
		if (oldBullet == null || oldIndent > indent) // rollback, if
		// list ended
		{
			top++;
			oldBullet = (Bullet) getBullet0(top);
			oldIndent = getLineIndent(top);
		}
		if (oldBullet != null
				&& oldBullet.type == bullet.type
				&& oldIndent == indent
				&& ((oldBullet.text == null && bullet.text == null) || oldBullet.text
						.equals(bullet.text)))
			return oldBullet;
		return bullet;
	}

	public int lineCount() {
		// TODO Auto-generated method stub
		return manager.getTextWidget().getLineCount();
	}

	public void resetBulletData() {
		int count = lineCount();
		for (int i = 0; i < count; i++)
			setLineBullet(i, 1, null);
	}

	public int getLineIndent(int line) {
		return getLineIndentDirectly(line) / indentSize;
	}

	/**
	 * Used for setting line indent, usually from GUI "Increase indent" and
	 * "decrease indent" commands *
	 * 
	 * @param startLine
	 *            first line to set indent to
	 * @param count
	 *            line count
	 * @param indent
	 *            indent to set; Really, indent on some line is changed by a
	 *            value, that calculated, as delta (indent - currentIndent)
	 */
	public void setLineIndent(int startLine, int count, int indent) {
		int change = indent - getLineIndent(startLine);
		setLineIndentDirectly(startLine, 1, indent * indentSize);
		for (int i = startLine + 1; i < startLine + count; i++) {
			int oldIndent = getLineIndent(i);
			int newIndent = oldIndent + change;
			if (newIndent < 0)
				newIndent = 0;
			if (newIndent > RichTextEditorConstants.MAX_INDENT)
				newIndent = RichTextEditorConstants.MAX_INDENT;
			setLineIndentDirectly(i, 1, newIndent * indentSize);
		}
		validateAfterListLineBullets(startLine, count);
		validateLineBullets(startLine, count);

	}

	/**
	 * Used for setting indent directly, without checking that it suits some
	 * range etc Intended to be used for setting content change, when we can't
	 * be sure, that all line's indent values is set etc.
	 * 
	 * @param startLine
	 *            first line to set indent to
	 * @param count
	 *            line count
	 * @param indent
	 *            indent to set
	 */
	public void setLineIndentWithoutCheck(int startLine, int count, int indent) {
		setLineIndentDirectly(startLine, count, indent * indentSize);
	}

	protected void validateAfterListLineBullets(int startLine, int count) {
		final int lineCount = lineCount();
		if (startLine + count == lineCount)
			return;
		int i = startLine + count - 1;
		Bullet lastBullet = (Bullet) getBullet0(i);
		int lastIndent = getLineIndent(i);
		for (; lastBullet == null && i >= startLine; i--)
			lastBullet = (Bullet) getBullet0(i);
		if (lastBullet != null) {
			Bullet newBullet = null;
			for (int j = startLine + count; j < lineCount; j++) {
				final Bullet bullet = (Bullet) getBullet0(j);
				int indent = getLineIndent(j);
				if (bullet != null && bullet != lastBullet
						&& bullet.type == lastBullet.type
						&& indent != lastIndent) {
					if (newBullet == null) {
						if (lastBullet.type == ST.BULLET_DOT) {
							BasicBullet newBulletedListBulletInstance = manager
									.getBulletFactory()
									.getNewBulletedListBulletInstance();
							newBullet = MetaConverter
									.convertBullet(newBulletedListBulletInstance);
						} else if (lastBullet.type == (ST.BULLET_NUMBER | ST.BULLET_TEXT)
								|| lastBullet.type == ST.BULLET_CUSTOM) {
							BasicBullet newNumberedListBulletInstanceForIndent = manager
									.getBulletFactory()
									.getNewNumberedListBulletInstanceForIndent(
											indent);
							newBullet = MetaConverter
									.convertBullet(newNumberedListBulletInstanceForIndent);
						}
					}
					setBullet0(j, 1, newBullet);
				}
			}
		}

	}

	Bullet getBullet0(int index) {
		return manager.getActualTextWidget().getLineBullet(index);
	}

	void setBullet0(int i, int j, Bullet bl) {
		manager.getActualTextWidget().setLineBullet(i, j, bl);
	}

	protected void validateLineBullets(int startLine, int count) {
		if (startLine > 0) {
			Bullet newLevelBullet = null;
			int prevIndent = getLineIndent(startLine - 1);
			Bullet prevBullet = (Bullet) getBullet0(startLine - 1);
			if (prevBullet == null)
				return;
			for (int i = startLine; i < startLine + count; i++) {
				int indent = getLineIndent(i);
				Bullet curBullet = (Bullet) getBullet0(i);
				if (curBullet == null)
					continue;
				if (indent <= prevIndent) {
					Bullet newBullet = searchBullet(indent, i - 1);
					if (newBullet != null)
						setBullet0(i, 1, newBullet);
				} else if (indent == prevIndent + 1) {
					if (newLevelBullet == null) {
						if (prevBullet.type == ST.BULLET_DOT) {
							BasicBullet newBulletedListBulletInstance = manager
									.getBulletFactory()
									.getNewBulletedListBulletInstance();
							newLevelBullet = MetaConverter
									.convertBullet(newBulletedListBulletInstance);
						} else if (prevBullet.type == (ST.BULLET_NUMBER | ST.BULLET_TEXT)
								|| prevBullet.type == ST.BULLET_CUSTOM) {
							BasicBullet newNumberedListBulletInstanceForIndent = manager
									.getBulletFactory()
									.getNewNumberedListBulletInstanceForIndent(
											indent);
							newLevelBullet = MetaConverter
									.convertBullet(newNumberedListBulletInstanceForIndent);
						}
					}
					setBullet0(i, 1, newLevelBullet);
				}
			}
		}
	}

	protected Bullet searchBullet(int indent, int lineIdx) {
		Bullet bullet = (Bullet) manager.getActualTextWidget().getLineBullet(
				lineIdx);
		while (lineIdx > 0 && getLineIndent(lineIdx) != indent) {
			lineIdx--;
			bullet = (Bullet) manager.getActualTextWidget().getLineBullet(
					lineIdx);
		}
		if (getLineIndent(lineIdx) != indent)
			return null;
		return bullet;
	}

	public int getLineIndentDirectly(int line) {
		if (!isExtendedStyledTextWidget()) // Not supported
			return 0;
		try {
			final Method method = widget.getClass().getMethod("getIndent",
					new Class[] { int.class });
			Integer res = (Integer) method.invoke(widget, line);
			return res;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public void setLineIndentDirectly(int startLine, int count, int indent) {
		if (!isExtendedStyledTextWidget())
			return;
		try {
			final StyledText editor = manager.getActualTextWidget();
			final Method method = editor.getClass().getMethod("putIndent",
					new Class[] { int.class, int.class });
			for (int a = startLine; a < startLine + count; a++) {
				method.invoke(editor, a, indent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see com.onpositive.richtext.model.ILineAttributeModel#setLineBulletWithoutCheck(int,
	 *      int, java.lang.Object)
	 */
	public void setLineBulletWithoutCheck(int startLine, int count,
			com.onpositive.richtext.model.meta.BasicBullet bullet) {
		manager.getActualTextWidget().setLineBullet(startLine, count,
				(Bullet) null);
		manager.getActualTextWidget().setLineBullet(startLine, count,
				MetaConverter.convertBullet(bullet));
	}

	/**
	 * @see com.onpositive.richtext.model.ILineAttributeModel#getFirstLineIndent(int)
	 *      Really, default StyledText supports only first line indent. This
	 *      method will return it. For getting whole paragraph("logical" line)
	 *      indent, use {@link StyledTextLineAttributeModel#getLineIndent(int)}
	 *      method
	 * @param line
	 *            Logical line/paragraph idx for getting first line indent
	 * @return first "visible" line idx in characters (depends on
	 *         {@link StyledTextLineAttributeModel#indentSize})
	 */
	public int getFirstLineIndent(int line) {
		final StyledText editor = manager.getActualTextWidget();
		try {
			final Method method = editor.getClass().getMethod("getLineIndent",
					new Class[] { int.class });
			Integer res = (Integer) method.invoke(editor, line);
			return res.intValue() / indentSize;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	public void setFirstLineIndent(int startLine, int count, int indent) {
		try {
			final StyledText editor = manager.getActualTextWidget();
			final Method method = editor.getClass().getMethod("setLineIndent",
					new Class[] { int.class, int.class, int.class });
			method.invoke(editor, startLine, count, indent * indentSize);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public int getDefaultLineSpacing() {
		final StyledText editor = manager.getActualTextWidget();
		try {
			final Method method = editor.getClass().getMethod("getLineSpacing",
					new Class[] {});
			Integer res = (Integer) method.invoke(editor);
			return res.intValue();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	public int getLineSpacing(int line) {
		if (!isExtendedStyledTextWidget()) // Not supported
			return 0;
		final StyledText editor = manager.getActualTextWidget();
		try {
			final Method method = editor.getClass().getMethod("getLineSpacing",
					new Class[] { int.class });
			Integer res = (Integer) method.invoke(editor, line);
			return (res.intValue() - defaultSpacing) / defaultSpacing / 2;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	public void setLineSpacing(int startLine, int count, int spacing) {
		if (!isExtendedStyledTextWidget()) // Not supported
			return;
		try {
			final StyledText editor = manager.getActualTextWidget();
			final Method method = editor.getClass().getMethod("setLineSpacing",
					new Class[] { int.class, int.class, int.class });
			int resultSpacing = spacing * defaultSpacing * 2 + defaultSpacing;
			method.invoke(editor, startLine, count, resultSpacing);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public int getRightIndent(int line) {
		if (!isExtendedStyledTextWidget()) // Not supported
			return 0;
		try {
			final StyledText editor = manager.getActualTextWidget();
			final Method method = editor.getClass().getMethod("getRightIndent",
					new Class[] { int.class });
			Integer res = (Integer) method.invoke(editor, line);
			return res / indentSize;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public void setRightIndent(int startLine, int count, int indent) {
		if (!isExtendedStyledTextWidget()) // Not supported
			return;
		indent = indent * indentSize;
		try {
			final StyledText editor = manager.getActualTextWidget();
			final Method method = editor.getClass().getMethod("putRightIndent",
					new Class[] { int.class, int.class });
			for (int a = startLine; a < startLine + count; a++) {
				method.invoke(editor, a, indent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.onpositive.richtexteditor.model.partitions.ILineAttributeModel#
	 * getIndentSize()
	 */
	public int getIndentSize() {
		return indentSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.onpositive.richtexteditor.model.partitions.ILineAttributeModel#
	 * getOffsetAtLine(int)
	 */
	public int getOffsetAtLine(int lineIndex) {
		return widget.getOffsetAtLine(lineIndex);
	}

	/**
	 * Checks if widgets an extended styled text widget.
	 * 
	 * @return true if widget an extended styled text widget.
	 */
	private boolean isExtendedStyledTextWidget() {
		boolean result = false;
		if (widget != null
				&& widget
						.getClass()
						.getName()
						.equals(RichTextEditorConstants.EXTENDED_STYLED_TEXT_CLASS)) {
			result = true;
		}
		return result;
	}

	public void setBulk(int[] aligns, BasicBullet[] bullets, int[] indents) {
		Bullet[] bl = new Bullet[bullets.length];
		for (int a = 0; a < bullets.length; a++) {
			Bullet bullet = MetaConverter.convertBullet(bullets[a]);
			bl[a]=bullet;
			
//			if (bullet != null && ((Bullet) bullet).type != ST.BULLET_DOT) {
//				bullet = checkBeforeList(startLine, (Bullet) bullet);
//				count = checkAfterList(oldStartLine + count, (Bullet) bullet)
//						- oldStartLine;
//				if (count != oldCount)
//					setBulletsWithIndentChecking(getLineIndent(startLine
//							+ oldCount - 1), oldStartLine + oldCount, count
//							- oldCount, (Bullet) bullet);
//			}
		}
	 	ExtendedStyledText m=(ExtendedStyledText) widget;
	 	for (int a=0;a<indents.length;a++){
	 		indents[a]*=indentSize;
	 	}
	 	m.bulkSet(aligns,indents,bl);
	}

}
