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

package com.onpositive.richtext.model;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import com.onpositive.richtext.model.meta.Color;
import com.onpositive.richtext.model.meta.GlyphMetrics;
import com.onpositive.richtext.model.meta.IImage;
import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtext.model.meta.Rectangle;
import com.onpositive.richtext.model.meta.StyleRange;
import com.onpositive.richtexteditor.model.AbstractLayerManager;
import com.onpositive.richtexteditor.model.LayerEvent;

/**
 * @author 32kda Made in USSR
 * Encapsulates image containing partition
 */

public class ImagePartition extends ObjectPartition {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected transient IImage image=null;
	protected String imagePath;
	protected boolean loadSheduled;
	
	protected int width = -1;
	protected int height = -1;
		
	
	/**
	 * Basic constructor
	 * @param layer Layer where partition'll be situated
	 * @param offset offset of part. 
	 * @param length length of part.Now - always 1.
	 * @param image Image object associated with the partition
	 * @param imageFileName Image file name associated with the partition
	 */
	public ImagePartition(ITextDocument document, int offset, int length,
			IImage image, String imageFileName) {
		super(document, offset, length);
		this.image = image;
		this.imagePath = imageFileName;
	}

	/**
	 * Basic constructor
	 * @param layer Layer where partition'll be situated
	 * @param offset offset of part. 
	 * @param length length of part.Now - always 1.
	 * @param image Image object associated with the partition
	 */
	public ImagePartition(ITextDocument document, int offset, int length,
			IImage image) {
		super(document, offset, length);
		this.image = image;
	}

	/**
	 * @return Image object associated with the partition
	 */
	public IImage getImage() {
		return image;
	}

	/**
	 * @param image Image object associated with the partition
	 */
	public void setImage(IImage image) {
		this.image = image;
	}

	/** (non-Javadoc)
	 * @see com.onpositive.richtext.model.ObjectPartition#getObject()
	 */
	public Object getObject() {
		return image;
	}

	/** (non-Javadoc)
	 * @see com.onpositive.richtext.model.ObjectPartition#setObject(java.lang.Object)
	 */
	public void setObject(Object object) {
		this.image = (IImage) object;
	}

	/**
	 * Returns partition style-matching style range for it's displaying
	 * @param manager AbstractLayerManager responsible for partition handling
	 * @return new StyleRange
	 */
	public StyleRange getStyleRange(final AbstractLayerManager manager) {

		if (!loadSheduled && image == null) {
			loadSheduled = true;
			final IImageManager imageCache = manager.getImageManager();
			image=imageCache.getImage(imagePath, new Observer() {

				public void update(Observable o, final Object arg) {
					manager.syncExec(new Runnable() {

						public void run() {
							image=imageCache.checkImage(imagePath);
							if (ImagePartition.this.getIndex()!=-1) {
								ArrayList<IPartition> changedPartitions = new ArrayList<IPartition>();
								changedPartitions.add(ImagePartition.this);
								manager.layerChanged(new LayerEvent(manager.getLayer(),
										changedPartitions));
							}
						}

					});
				}

			});
		}
		StyleRange style = new StyleRange();
		style.start = getOffset();
		style.length = 1;
		Rectangle rect = new Rectangle(0,0,20,20);
		if (image!=null)
		{
			rect = calculateResizedBounds();
		}
		style.metrics = new GlyphMetrics(rect.height, 0, rect.width);
		return style;
	}

	/**
	 * Calculates image bounds, if some width or height were set 
	 * @return calculated bounds {@link Rectangle}
	 */
	public Rectangle calculateResizedBounds()
	{
		Rectangle rect = new Rectangle(0,0,20,20);
		Rectangle baseBounds = image.getBounds();
		if (width > 0 && height > 0)
		{
			rect.width = width;
			rect.height = height;
		}
		else if (width > 0)
		{
			double relation = baseBounds.width * 1.0 / baseBounds.height;
			rect.width = width;
			rect.height = (int) (width / relation);
		}
		else if (height > 0)
		{
			double relation  = baseBounds.width * 1.0 / baseBounds.height;
			rect.height = height;
			rect.width = (int) (height * relation);
		}
		else 
			rect = baseBounds;
		return rect;
	}

	
	/**
	 * @param manager AbstractLayerManager
	 * @return always null
	 */
	public Color getColor(AbstractLayerManager manager)
	{
		return null;
	}
	
	/**
	 * @param manager AbstractLayerManager
	 * @return always null
	 */
	public Color getBgColor(AbstractLayerManager manager)
	{
		return null;
	}
	
	/**
	 * @param partition2 part. to compare
	 * @return always false
	 */
	public boolean equalsByStyle(BasePartition partition2) {
		return false;
	}

	/**
	 * @return image file name
	 */
	public String getImageFileName() {
		return imagePath;
	}

	/**
	 * @param imageFileName image file name
	 */
	public void setImageFileName(String imageFileName) {
		this.imagePath = imageFileName;
	}

	/**
	 * @return Image width
	 */
	public int getWidth()
	{
		return width;
	}

	/**
	 * @param width Image width to set
	 */
	public void setWidth(int width)
	{
		this.width = width;
	}

	/**
	 * @return Image height
	 */
	public int getHeight()
	{
		return height;
	}

	/**
	 * @param height Image height to set
	 */
	public void setHeight(int height)
	{
		this.height = height;
	}

}
