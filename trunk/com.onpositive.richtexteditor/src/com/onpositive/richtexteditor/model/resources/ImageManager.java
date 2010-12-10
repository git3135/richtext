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

package com.onpositive.richtexteditor.model.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Observer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.onpositive.richtext.model.IImageManager;
import com.onpositive.richtext.model.meta.IImage;
import com.onpositive.richtexteditor.model.RichTextEditorConstants;

/**
 * @author kor Image managing class
 */
public class ImageManager implements IImageManager {

	private final class LocalImage implements IImage {
		private final Image vImage;
		private final String imagePath;

		private LocalImage(Image vImage, String imagePath) {
			this.vImage = vImage;
			this.imagePath = imagePath;
		}

		public String url() {
			return imagePath;
		}

		
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((imagePath == null) ? 0 : imagePath.hashCode());
			result = prime * result
					+ ((vImage == null) ? 0 : vImage.hashCode());
			return result;
		}

		
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LocalImage other = (LocalImage) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (imagePath == null) {
				if (other.imagePath != null)
					return false;
			} else if (!imagePath.equals(other.imagePath))
				return false;
			if (vImage == null) {
				if (other.vImage != null)
					return false;
			} else if (!vImage.equals(other.vImage))
				return false;
			return true;
		}

		public com.onpositive.richtext.model.meta.Rectangle getBounds() {
			Rectangle bounds = vImage.getBounds();
			return new com.onpositive.richtext.model.meta.Rectangle(bounds.x,
					bounds.x, bounds.width, bounds.height);
		}

		private ImageManager getOuterType() {
			return ImageManager.this;
		}
	}

	protected File tempDirFile = null;

	protected HashMap<String, Image> imageMap = new HashMap<String, Image>();
	protected Display display;

	protected static final String SERVICE_IMAGE_KEY = "#";
	/**
	 * Text for temp image loading image
	 */
	protected static final String IMG_LOAD_STRING = " Loading ";
	/**
	 * Text for 'error loading servive image
	 */
	protected static final String IMG_LOAD_ERROR_STRING = " Error loading ";

	/**
	 * Basic constructor
	 * 
	 * @param display
	 *            display to allocate images at
	 */
	public ImageManager(Display display) {
		this.display = display;
		String tempDir = System.getProperty("java.io.tmpdir");
		if (tempDir == null) {
			tempDirFile = new File(".");
		}

	}

	/**
	 * Asynchronous image loading method
	 * 
	 * @param string
	 *            image resource string
	 * @param observer
	 *            Observer instance
	 */
	/*
	 * public void sheduleLoad(final String string, final Observer observer) {
	 * Thread loader = new Thread() { public void run() {
	 * 
	 * Image image = imageMap.get(string); if (image != null) {
	 * observer.update(null, image); return; } try { InputStream openStream =
	 * getStream(string); if (openStream != null) { try { Image loadedImage =
	 * new Image(display, openStream); imageMap.put(string, loadedImage);
	 * observer.update(null, loadedImage); } finally { openStream.close(); } }
	 * else{ handleNotFound(string,observer); } } catch (IOException e) {
	 * handleIOException(string,observer,e); } } };
	 * loader.setPriority(Thread.MAX_PRIORITY); loader.start(); }
	 */

	protected void handleNotFound(String string, Observer observer) {

	}

	protected void handleIOException(String string, Observer observer,
			IOException e) {

	}

	/**
	 * Get image by it's string
	 * 
	 * @param resourceKey
	 *            image resource string
	 * @param observer
	 *            Observer instance
	 * @return Image object for loaded image
	 */
	public synchronized Image getVImage(String resourceKey, Observer observer) {
		Image image = imageMap.get(resourceKey);

		if (image != null) {
			return image;
		}
		sheduleLoad(resourceKey, observer);
		return getTempImage(resourceKey, IMG_LOAD_STRING,
				RichTextEditorConstants.IMAGE_LOAD_ICON_PATH, new RGB(50, 50,
						50));
	}

	protected Image getTempImage(String fileName, String imageText,
			String iconPath, RGB foreground) {
		String key = SERVICE_IMAGE_KEY + imageText;
		Image image = imageMap.get(key);
		if (image == null) {
			ImageDescriptor loadImgDescriptor = ImageDescriptor
					.createFromURL(ImageManager.class.getResource(iconPath));
			image = generateTextImage(fileName, loadImgDescriptor, imageText,
					foreground);
			imageMap.put(key, image);
		}
		return image;
	}

	/**
	 * Disposes all loaded images
	 */
	public void dispose() {
		for (Image i : imageMap.values()) {
			i.dispose();
		}
	}

	/**
	 * Checks, have we loaded specified image file or not
	 * 
	 * @param filename
	 *            Image File Name
	 * @return not null, if we have loaded an image file, null otherwise
	 */
	public Image checkVImage(String filename) {
		Image image = imageMap.get(filename);
		return image;
	}

	/**
	 * Registers image
	 * 
	 * @param filename
	 *            image file name
	 * @param image
	 *            image object to register
	 * @param tempFile TODO
	 * @return New filename - if, for example, images are being cached to some
	 *         location, we need to change it
	 */
	public String registerImage(String filename, Image image, boolean tempFile) {
		imageMap.put(filename, image);
		return filename;
	}

	protected InputStream getStream(final String string) throws IOException,
			MalformedURLException {
		return new URL(string).openStream();
	}

	/**
	 * Asynchronous image loading method
	 * 
	 * @param string
	 *            image resource string
	 * @param observer
	 *            Observer instance
	 */
	public void sheduleLoad(final String string, final Observer observer) {
		Thread loader = new Thread() {
			public void run() {

				try {
					Image image = imageMap.get(string);
					if (image != null) {
						observer.update(null, image);
						return;
					}
					InputStream openStream = getStream(string);
					if (openStream != null) {
						try {
							Image loadedImage = new Image(display, openStream);
							imageMap.put(string, loadedImage);
							observer.update(null, loadedImage);

						} catch (Exception e) {
							generateErrorImg(string, observer);
						} finally {
							openStream.close();
						}
					}
				} catch (MalformedURLException e) {
					generateErrorImg(string, observer);
				} catch (IOException e) {
					generateErrorImg(string, observer);
				}

			}

			private void generateErrorImg(final String string,
					final Observer observer) {
				try {
					// ImageDescriptor errImgDescriptor=
					// ImageDescriptor.createFromURL(ImageManager.class.getResource(RichTextEditorConstants.IMAGE_LOAD_ERROR_ICON_PATH));
					observer.update(null, getTempImage(string,
							IMG_LOAD_ERROR_STRING,
							RichTextEditorConstants.IMAGE_LOAD_ERROR_ICON_PATH,
							new RGB(255, 0, 0)));
				} catch (Exception e1) {
					System.err
							.println("Error loading icon "
									+ RichTextEditorConstants.IMAGE_LOAD_ERROR_ICON_PATH);
				}
			}
		};
		loader.setPriority(Thread.MAX_PRIORITY);
		loader.start();
	}

	protected Image generateTextImage(String curSrc,
			ImageDescriptor imgDescriptor, String text, RGB forground) {
		int fontHeight = 10;
		int margin = 2;
		Display defaultDisplay = Display.getDefault();
		Image errImage = imgDescriptor.createImage(defaultDisplay);
		int errWidth = errImage.getBounds().width;
		int errHeight = errImage.getBounds().height;
		Font font = new Font(defaultDisplay, new FontData("Arial", fontHeight,
				SWT.NONE));
		Color fgColor = new Color(defaultDisplay, forground);
		Color black = new Color(defaultDisplay, new RGB(50, 50, 50));
		int width = (int) (margin + fontHeight / 1.5
				* (curSrc.length() + text.length()) + errWidth);
		int height = Math.max(errHeight + margin, fontHeight * 2);
		Image res = new Image(defaultDisplay,
				new Rectangle(0, 0, width, height));
		GC gc = new GC(res);
		gc.setFont(font);
		gc.setForeground(fgColor);
		gc.drawImage(errImage, margin, margin);
		gc.drawText(text + curSrc, errWidth + margin, 0);
		gc.setForeground(black);
		gc.drawRectangle(0, 0, width - 1, height - 1);
		gc.dispose();
		font.dispose();
		fgColor.dispose();
		black.dispose();
		errImage.dispose();
		return res;
	}

	public IImage checkImage(String imageFileName) {
		boolean containsKey = imageMap.containsKey(imageFileName);
		if (containsKey){
			return new LocalImage(imageMap.get(imageFileName), imageFileName);
		}
		return null;
	}

	public IImage getImage(final String imagePath, Observer observer) {
		final Image vImage = getVImage(imagePath, observer);
		if (vImage != null) {
			return new LocalImage(vImage, imagePath);
		}
		return null;
	}

	public void registerImage(String imageFileName, IImage image) {
		if (image instanceof LocalImage){
			
		}
	}

	public Image mapImage(IImage image) {
		if (image == null) {
			return null;
		}
		LocalImage m = (LocalImage) image;
		return m.vImage;
	}
}
