package com.onpositive.richtexteditor.viewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;

import com.onpositive.richtext.model.ILineAttributeModel;
import com.onpositive.richtexteditor.io.ILineInformationProvider;
import com.onpositive.richtexteditor.model.resources.LayerManager;


public class StyledTextLineInformationProvider implements
		ILineInformationProvider
{
	protected StyledText editor;
	protected ILineAttributeModel model;
	
	public StyledTextLineInformationProvider(LayerManager manager)
	{
		this.editor = manager.getActualTextWidget();
		this.model = manager.getLayer().getLineAttributeModel();
	}

	public int getBulletIdForLine(int lineIndex)
	{
		final Bullet lineBullet = editor.getLineBullet(lineIndex);
		if (lineBullet == null)
			return 0;
		return lineBullet.hashCode(); //TODO is hashcode really unique for the bullets from some group?
	}

	public int getBulletType(int lineIndex)
	{		
		final Bullet lineBullet = editor.getLineBullet(lineIndex);
		if (lineBullet == null)
			return NONE_BULLET;
		if (lineBullet.type == ST.BULLET_DOT)
			return SIMPLE_BULLET;
		if (lineBullet.type == (ST.BULLET_NUMBER | ST.BULLET_TEXT))
			return NUMBER_BULLET;
		if (lineBullet.type == (ST.BULLET_CUSTOM))
		{
			if (lineBullet.text.equals("a"))
				return LETTER_BULLET;
			if (lineBullet.text.equals("i"))
				return ROMAN_BULLET;
			else
				return EXPLICIT_BULLET;
		}
		return 0; //TODO throw some exception here?
	}

	public int getLineAlignment(int lineIndex)
	{
		final int lineAlignment = editor.getLineAlignment(lineIndex);
		if (lineAlignment == SWT.LEFT)
		{
			if (editor.getLineJustify(lineIndex))
				return ILineInformationProvider.JUSTIFY_ALIGN;
			return ILineInformationProvider.LEFT_ALIGN;
		}
		if (lineAlignment == SWT.RIGHT)
			return ILineInformationProvider.RIGHT_ALIGN;
		if (lineAlignment == SWT.CENTER)
			return ILineInformationProvider.CENTER_ALIGN;
		return 0;
	}

	public int getLineIndent(int lineIndex)
	{
		return model.getLineIndent(lineIndex);
	}

	public String getBulletText(int lineIndex)
	{
		return editor.getLineBullet(lineIndex).text;
	}

	public int getLineSpacing(int lineIndex)
	{
		return model.getLineSpacing(lineIndex);
	}

	public int getLineStartIndent(int lineIndex)
	{
		return model.getFirstLineIndent(lineIndex);
	}

	public int getRightLineIndent(int lineIndex)
	{
		return model.getRightIndent(lineIndex);
	}

	public int getIndentSize()
	{
		return model.getIndentSize();
	}

}
