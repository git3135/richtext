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

package com.onpositive.richtexteditor.io.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.BasePartitionLayer;
import com.onpositive.richtext.model.IImageManager;
import com.onpositive.richtext.model.ImagePartition;
import com.onpositive.richtext.model.LinkPartition;
import com.onpositive.richtext.model.meta.BasicBullet;
import com.onpositive.richtext.model.meta.BulletFactory;
import com.onpositive.richtext.model.meta.IImage;
import com.onpositive.richtexteditor.io.html_scaner.HTMLLexListener;
import com.onpositive.richtexteditor.io.html_scaner.Scanner;
import com.onpositive.richtexteditor.model.AbstractLayerManager;
import com.onpositive.richtexteditor.model.IFontStyleManager;
import com.onpositive.richtexteditor.model.ISimpleRichTextModel;

/**
 * @author kor Default implementation of IHTMLLoader Class encapsulates
 *         converting HTML into {@link ISimpleRichTextModel}
 */
public class DefaultHTMLLoader implements IHTMLLoader {

	private BasePartitionLayer layer;
	private IFontStyleManager fontStyleManager;
	private IImageManager imageManager;
	private BasePartition linkPrototypePartition;

	/**
	 * Basic constructor
	 * 
	 * @param manager
	 *            {@link AbstractLayerManager} instance
	 */
	public DefaultHTMLLoader(AbstractLayerManager manager) {
		this.layer = manager.getLayer();
		this.fontStyleManager = manager.getFontStyleManager();
		this.imageManager = manager.getImageManager();
		this.linkPrototypePartition = manager.getLinkPrototype();
	}

	/**
	 * Wraps parse process
	 * 
	 * @param html
	 *            String with html content to parse
	 * @return ISimpleRichTextModel
	 */
	public ISimpleRichTextModel parse(String html) {
		Scanner scanner = new Scanner();
		final HTMLLexListener listener = createLexingListener(scanner);
		scanner.addLexListener(listener);
		try {
			scanner.process(new StringReader(html), html.length());
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		final ArrayList<BasePartition> partitions = listener.getPartitions();
		optimizePartitionsSize(partitions);
		setNewPartitionsFonts(partitions);
		final ArrayList<Integer> lineAligns = listener.getLineAligns();
		final ArrayList<Integer> lineBullets = listener.getLineBullets();
		final ArrayList<Integer> lineIndents = listener.getLineIndents();
		final HashMap<Integer, Integer> listTypes = listener.getListTypes();
		ISimpleRichTextModel z = new ISimpleRichTextModel() {

			protected BulletFactory factory;

			{
				factory = new BulletFactory();
			}

			public int getAlign(int line) {
				return lineAligns.get(line).intValue();
			}

			public BasicBullet getBullet(int line) {
				int bulletNum = lineBullets.get(line);
				if (bulletNum == -1)
					return null;
				BasicBullet bullet = factory.getBulletForNum(bulletNum, listTypes
						.get(bulletNum), null);
				return bullet;
			}

			public int getLineCount() {
				return lineAligns.size();
			}

			public List<BasePartition> getPartitions() {
				return partitions;
			}

			public String getText() {
				return listener.getText();
			}

			public int getIndent(int lineIndex) {
				return lineIndents.get(lineIndex);
			}

		};
		return z;
	}

	protected void optimizePartitionsSize(ArrayList<BasePartition> partitions) {
		for (int i = 0; i < partitions.size(); i++) {
			BasePartition partition = partitions.get(i);
			if (partition.getLength() > AbstractLayerManager.MAX_PARTITION_SIZE) {
				BasePartition leftPartition = partition
						.extractLeftPartitionWithStyle(partition.getOffset()
								+ AbstractLayerManager.MAX_PARTITION_SIZE);
				partitions.add(i, leftPartition);
			}

		}

	}

	private HTMLLexListener createLexingListener(Scanner scanner) {
		return new HTMLLexListener(layer.getDoc(), scanner);
	}

	protected void setNewPartitionsFonts(ArrayList<BasePartition> partitions) {
		for (Iterator<BasePartition> iterator = partitions.iterator(); iterator
				.hasNext();) {
			BasePartition basePartition = (BasePartition) iterator.next();
			if (!basePartition.getFontDataName().equals("")) {
				String innerName = fontStyleManager.getNameForStyleString(basePartition.getFontDataName());
				basePartition.setFontDataName(innerName);
			}
			if (basePartition instanceof LinkPartition)
				basePartition.applyAttributes(linkPrototypePartition);
			if (basePartition instanceof ImagePartition) {
				ImagePartition imagePartition = (ImagePartition) basePartition;
				if (imageManager.checkImage(imagePartition.getImageFileName()) == null) {
					IImage image = ((ImagePartition) basePartition).getImage();
					if (image != null) {
						imageManager.registerImage(imagePartition
								.getImageFileName(), image);
					}
				}
			}
		}
	}

	/**
	 * Wraps parse process
	 * 
	 * @param reader
	 *            Contents reader
	 * @return ISimpleRichTextModel
	 * @throws IOException
	 *             in case of reading error
	 */
	public ISimpleRichTextModel parse(Reader reader) throws IOException {
		StringBuilder bld = new StringBuilder();
		while (true) {
			int read = reader.read();
			if (read != -1) {
				bld.append((char) read);
			} else {
				break;
			}
		}
		return parse(bld.toString());
	}

	/**
	 * Wraps parse process
	 * 
	 * @param stream
	 *            Contents input stream
	 * @return ISimpleRichTextModel
	 * @throws IOException
	 *             in case of reading error
	 */
	public ISimpleRichTextModel parse(InputStream stream) throws IOException {
		return parse(new InputStreamReader(stream));
	}

}
