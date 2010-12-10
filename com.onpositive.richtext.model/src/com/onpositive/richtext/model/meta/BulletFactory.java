/*******************************************************************************
 * Copyright (c) 2007, 2008 OnPositive Technologies (http://www.onpositive.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     OnPositive Technologies (http://www.onpositive.com/) - initial API and implementation
 *******************************************************************************/

package com.onpositive.richtext.model.meta;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.WeakHashMap;


/**
 * Factory for getting and controlling instances of line bullets for StyledText
 * 
 * @author 32kda Made in USSR
 */
public class BulletFactory
{
	protected static StyleRange bulletStyle;
	WeakHashMap<Object, BasicBullet>platformBullets=new WeakHashMap<Object, BasicBullet>();

	/**
	 * Means no list at spec. line
	 */
	public static final int NONE_LIST_CONST = -1;
	/**
	 * Means bulleted list at spec. line
	 */
	public static final int BULLETED_LIST = 0;

	/**
	 * Simple numbered list - 1. 2. etc.
	 */
	public static final int SIMPLE_NUMBERED_LIST = 1;
	/**
	 * ABC numbered list - a. b. etc.
	 */
	public static final int LETTERS_NUMBERED_LIST = 2;
	/**
	 * Roman-like numbered list - i. ii. etc.
	 */
	public static final int ROMAN_NUMBERED_LIST = 3;

	static
	{
		bulletStyle = new StyleRange();
		bulletStyle.font = new Font("Arial", 10, 0);
		bulletStyle.length = 1;
		bulletStyle.start = 0;
		bulletStyle.metrics = new GlyphMetrics(10, 10, 25);
	}

	public static void setBulletStyleFont(Font newFont)
	{
		bulletStyle.font = newFont;
	}

	/**
	 * @return instance of bullet for bulleted list
	 */
	public BasicBullet getNewBulletedListBulletInstance()
	{
		return new BasicBullet(bulletStyle,this);
	}

	/**
	 * @param bulletType
	 * @param bulletText
	 *            TODO
	 * @return bullet for new numbered list
	 */
	public BasicBullet getNewNumberedListBulletInstance(int bulletType, String bulletText)
	{
		BasicBullet bullet = null;
		if (bulletText != null)
		{
			if (bulletText.endsWith(".")) 
				bulletText = bulletText.substring(0, bulletText.length() - 1);
			bullet = new BasicBullet(BasicBullet.BULLET_CUSTOM, bulletStyle,this);
			bullet.text = bulletText;
		} else if (bulletType == SIMPLE_NUMBERED_LIST)
		{
			bullet = new BasicBullet(BasicBullet.BULLET_NUMBER | BasicBullet.BULLET_TEXT, bulletStyle,this);
			bullet.text = ".";
		} else if (bulletType == LETTERS_NUMBERED_LIST)
		{
			bullet = new BasicBullet(BasicBullet.BULLET_CUSTOM, bulletStyle,this);
			bullet.text = "a";
		} else if (bulletType == ROMAN_NUMBERED_LIST)
		{
			bullet = new BasicBullet(BasicBullet.BULLET_CUSTOM, bulletStyle,this);
			bullet.text = "i";
		} else
			throw new IllegalArgumentException("Bullet type " + bulletType + " is not supported!");
		try
		{
			Field declaredField = BasicBullet.class.getDeclaredField("linesIndices");
			declaredField.setAccessible(true);
			declaredField.set(bullet, new int[] { -1 });
			declaredField = BasicBullet.class.getDeclaredField("count");
			declaredField.setAccessible(true);
			
			declaredField.set(bullet, Integer.valueOf(1));
		} catch (Throwable e)
		{
			e.printStackTrace();
		}
		return bullet;

	}

	// -----------------------Static block
	// ends-----------------------------------------------------------------------
	protected HashMap<Integer, BasicBullet> listBullets = new HashMap<Integer, BasicBullet>();
	

	public BulletFactory()
	{
		listBullets = new HashMap<Integer, BasicBullet>();
		listBullets.put(BULLETED_LIST, new BasicBullet(bulletStyle,this));
	}

	/**
	 * Returns bullet for some numbered list, determined by num, We need this
	 * for correct numbered lists loading from html
	 * 
	 * @param num
	 *            characterize, which list we read and process now. e.g. list 1
	 *            may have 5 points with 2 unnumbered between them, and we
	 *            should correctly display such list
	 * @param bulletType
	 *            Used for specify, how numbered list bullets should look (e.g.
	 *            1. , a., i. etc)
	 * @param bulletText
	 *            TODO
	 * @return Bullet for list with specified index
	 */
	public BasicBullet getBulletForNum(int num, int bulletType, String bulletText)
	{
		if (num == NONE_LIST_CONST)
			return null;
		if (!listBullets.containsKey(num)) // If it isn't a bullet for this num,
											// create new one
		{
			if (bulletType == BULLETED_LIST)
				listBullets.put(num, new BasicBullet(BasicBullet.BULLET_DOT, bulletStyle,this));
			else
				listBullets.put(num, getNewNumberedListBulletInstance(bulletType, bulletText));
		}
		return listBullets.get(num);
	}

	public BasicBullet getNewNumberedListBulletInstanceForIndent(int indent)
	{
		if (indent == 1)
			return getNewNumberedListBulletInstance(LETTERS_NUMBERED_LIST, null);
		if (indent == 2)
			return getNewNumberedListBulletInstance(ROMAN_NUMBERED_LIST, null);
		return getNewNumberedListBulletInstance(SIMPLE_NUMBERED_LIST, null);
	}	
	
	void registerPlatformBullet(BasicBullet b){
		
	}

	public Object getBulletForPlarform(Object lineBullet) {
		return platformBullets.get(lineBullet);
	}

	public void registerPlatformData(BasicBullet basicBullet,
			Object platformData) {
		platformBullets.put(platformData, basicBullet);
	}

	/*
	 * public Bullet getNewExplictNumberBulletInstance(int num, int bulletType,
	 * String bulletText) { if (!listBullets.containsKey(num)) //If it isn't a
	 * bullet for this num, create new one listBullets.put(num,
	 * getNewNumberedListBulletInstance(bulletType, bulletText)); return
	 * listBullets.get(num); }
	 */
}
