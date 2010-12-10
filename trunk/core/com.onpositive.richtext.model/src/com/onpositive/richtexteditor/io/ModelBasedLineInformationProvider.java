package com.onpositive.richtexteditor.io;

import com.onpositive.richtext.model.meta.BasicBullet;
import com.onpositive.richtexteditor.model.ISimpleRichTextModel;


public class ModelBasedLineInformationProvider implements
		ILineInformationProvider
{
	protected ISimpleRichTextModel model;
	
	public ModelBasedLineInformationProvider(ISimpleRichTextModel model)
	{
		this.model = model;
	}

	public int getBulletIdForLine(int lineIndex)
	{
		final Object bullet = model.getBullet(lineIndex);
		if (bullet != null) return bullet.hashCode();
		return 0;
	}

	public int getBulletType(int lineIndex)
	{
		if (model.getBullet(lineIndex) == null)
			return NONE_BULLET;
		final BasicBullet lineBullet = (BasicBullet)model.getBullet(lineIndex);
		if (lineBullet.type == BasicBullet.BULLET_DOT)
			return SIMPLE_BULLET;
		if (lineBullet.type == (BasicBullet.BULLET_NUMBER | BasicBullet.BULLET_TEXT))
			return NUMBER_BULLET;
		if (lineBullet.type == (BasicBullet.BULLET_CUSTOM))
		{
			if (lineBullet.getText().equals("a"))
				return LETTER_BULLET;
			if (lineBullet.getText().equals("i"))
				return ROMAN_BULLET;
			else
				return EXPLICIT_BULLET;
		}
		return 0; //TODO throw some exception here?
	}

	public int getLineAlignment(int lineIndex)
	{
		return model.getAlign(lineIndex);
	}

	public int getLineIndent(int lineIndex)
	{
		return model.getIndent(lineIndex);
	}

	public String getBulletText(int lineIndex)
	{		
		return ((BasicBullet)model.getBullet(lineIndex)).getText();
	}

	public int getLineSpacing(int lineIndex)
	{
		return 0;
	}

	public int getLineStartIndent(int lineIndex)
	{
		return 0;
	}

	public int getRightLineIndent(int lineIndex)
	{
		return 0;
	}

	public int getIndentSize()
	{
		return 0;
	}

}
