package com.onpositive.richtexteditor.wikitext.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.BasePartitionLayer;
import com.onpositive.richtext.model.IImageManager;
import com.onpositive.richtext.model.LinkPartition;
import com.onpositive.richtext.model.meta.BasicBullet;
import com.onpositive.richtext.model.meta.BulletFactory;
import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtexteditor.io.AbstractTextLoader;
import com.onpositive.richtexteditor.io.ITextLoader;
import com.onpositive.richtexteditor.model.AbstractLayerManager;
import com.onpositive.richtexteditor.model.ISimpleRichTextModel;

public class DefaultWikitextLoader extends AbstractTextLoader {

	protected BasePartitionLayer layer;
	protected IImageManager imageManager;
	protected BasePartition linkPrototypePartition;

	public DefaultWikitextLoader(AbstractLayerManager manager) {
		this.layer = manager.getLayer();
		this.imageManager = manager.getImageManager();
		this.linkPrototypePartition = manager.getLinkPrototype();
	}

	public DefaultWikitextLoader(ITextDocument d ) {
		this.layer = new BasePartitionLayer();
		layer.connectToDocument(d);
		this.imageManager = null;
		this.linkPrototypePartition = new LinkPartition(layer.getDoc(), 0, 0);
	}

	public DefaultWikitextLoader(BasePartitionLayer layer,
			IImageManager imageManager, BasePartition linkPrototypePartition) {
		this.layer = layer;
		this.imageManager = imageManager;
		this.linkPrototypePartition = linkPrototypePartition;
	}

	public ISimpleRichTextModel parse(String text) {
		// WikitextScanner scanner = new WikitextScanner();
		WikitextLinesScanner scanner = new WikitextLinesScanner();
		final WikitextLexListener listener = new WikitextLexListener(layer.getDoc());

		scanner.addLexListener(listener);
		try {
			scanner.process(text);
			// scanner.process(new StringReader(text), text.length());
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		final ArrayList<BasePartition> partitions = listener.getPartitions();
		final ArrayList<Integer> lineAligns = listener.getLineAligns();
		final ArrayList<Integer> lineBullets = listener.getLineBullets();
		final ArrayList<Integer> lineIndents = listener.getLineIndents();
		final ArrayList<Integer> lineBulletTypes = listener
				.getLineBulletTypes();
		final HashMap<Integer, String> numberedListStarts = listener
				.getNumberedListStarts();
		setNewPartitionsFonts(partitions);

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
				int bulletType = lineBulletTypes.get(line);
				final String bulletStr = numberedListStarts.get(bulletNum);
				BasicBullet bullet = factory.getBulletForNum(bulletNum, bulletType,
						bulletStr);
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
		ITextDocument doc = layer.getDoc();
		if (doc != null) {
			layer.disconnectFromDocument();
			doc.set(listener.getText());
			layer.connectToDocument(doc);
		}

		return z;
	}

	protected void setNewPartitionsFonts(ArrayList<BasePartition> partitions) {
		for (Iterator<BasePartition> iterator = partitions.iterator(); iterator.hasNext();) {
			BasePartition partition = (BasePartition) iterator.next();
			if (partition instanceof LinkPartition) {
				partition.applyAttributes(linkPrototypePartition);
			}
		}

	}

}
