package com.onpositive.service.mediawiki.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.BasePartitionLayer;
import com.onpositive.richtext.model.LinkPartition;
import com.onpositive.richtext.model.meta.BasicBullet;
import com.onpositive.richtext.model.meta.BulletFactory;
import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtexteditor.io.AbstractTextLoader;
import com.onpositive.richtexteditor.model.AbstractLayerManager;
import com.onpositive.richtexteditor.model.ISimpleRichTextModel;
import com.onpositive.richtexteditor.wikitext.parser.DefaultLinesProvider;
import com.onpositive.richtexteditor.wikitext.parser.ILineMarkupParser;
import com.onpositive.richtexteditor.wikitext.parser.ILineParser;
import com.onpositive.richtexteditor.wikitext.parser.ModelBullet;
import com.onpositive.richtexteditor.wikitext.parser.OrderedListBullet;
import com.onpositive.richtexteditor.wikitext.parser.WikitextLexEventConsumer;
import com.onpositive.richtexteditor.wikitext.parser.WikitextLine;
import com.onpositive.richtexteditor.wikitext.parser.WikitextParser;

public class MediaWikiTextLoader extends AbstractTextLoader
{
	protected BasePartition linkPrototypePartition;
	protected HashMap<Integer, Integer> lists = new HashMap<Integer, Integer>();
	protected HashMap<Integer, Integer> listTypes = new HashMap<Integer, Integer>();
	protected HashMap<Integer, String> listStartBullets = new HashMap<Integer, String>();
	protected ArrayList<Integer> lineBullets = new ArrayList<Integer>();
	protected BasePartitionLayer layer;
	protected ITextDocument document;
	protected int nextListIdx = 0;
	protected ILineMarkupParser lineMarkupParser;
	
	public MediaWikiTextLoader(AbstractLayerManager manager)
	{
		this.layer = manager.getLayer();
		this.linkPrototypePartition = manager.getLinkPrototype();
	}
	
	public MediaWikiTextLoader(ITextDocument document)
	{
		this.document = document;
	}

	public ISimpleRichTextModel parse(String text)
	{
		text = text.replaceAll("<br(\\s)*>","\n\n");
		//text = text.replaceAll("<br/>","\n\n");
		text = text.replaceAll("<br(\\s)*/>","\n\n");
		DefaultLinesProvider provider = new MediaWikiLinesProvider(text,new MediaWikiLineMarkupParser());
		final List<WikitextLine> linesList = provider.getLinesList();		
		final WikitextLexEventConsumer consumer = new MediaWikiLexEventConsumer(layer != null?layer.getDoc():document);
		ILineParser parser = new WikiLineParserFactory().createParser(consumer);
		WikitextParser wikitextParser = new MediaWikiTextParser(consumer, parser);
		wikitextParser.parse(linesList);		
		final ArrayList<BasePartition> partitions = consumer.getPartitions();
		
		final ArrayList<Integer> lineAligns = createEmptyAligns(linesList.size());
		setNewPartitionsFonts(partitions);
		createBulletNumbers(linesList);
		
		ISimpleRichTextModel z = new ISimpleRichTextModel() {

			protected BulletFactory factory;

			{
				factory = new BulletFactory();
			}

			public int getAlign(int line) {
				return lineAligns.get(line).intValue();
			}

			public BasicBullet getBullet(int line) {
				if (linesList.get(line).getBullet() == null)
					return null;
				int bulletNum = lineBullets.get(line);
				int bulletType = convertBulletType(line, linesList);
				//final String bulletStr = linesList.get(line).getBullet().getBulletText();
				BasicBullet bullet = factory.getBulletForNum(bulletNum, bulletType,
						listStartBullets.get(bulletNum));//	bulletStr);
				return bullet;
			}

			public int getLineCount() {
				return lineAligns.size();
			}

			public List<BasePartition> getPartitions() {
				return partitions;
			}

			public String getText() {
				return consumer.getText();
			}

			public int getIndent(int lineIndex) {
				return linesList.get(lineIndex).getIndent();
			}
			
		};
		return z;

}

	protected ArrayList<Integer> createEmptyAligns(int size)
	{		
		ArrayList<Integer> list = new ArrayList<Integer>(size);
		for (int i = 0; i < size; i++)
		{
			list.add(0);
		}
		return list;
	}

	protected void createBulletNumbers(List<WikitextLine> lines)
	{
		int currentList = -1;
		int currentType = -1;
		initNextListIdx();
		for (int i = 0; i < lines.size(); i++)
		{
			WikitextLine line = lines.get(i);
			ModelBullet bullet = line.getBullet();
			int indent = line.getIndent();
			
			if (bullet == null && currentType != -1)
			{
				currentList = getNextListIdx();
				currentType = -1;
				listTypes.put(indent, -1);
			}
			
			if (bullet != null && (bullet.getType() != currentType || 
				(i > 0 && line.getIndent() != lines.get(i-1).getIndent())))
			{
				if ((bullet != null && currentType > -1) && 
					i > 0 && indent < lines.get(i - 1).getIndent())
				{
					if (lists.containsKey(indent)) //Can be not containing due to some syntax error
					{
						currentList = lists.get(indent);
						int listType = listTypes.get(indent);
						clearAllGreater(indent);
						if (listType != bullet.getType())
							currentList = getNextListIdx();
					}
				}
				else if (currentType != -1)
				{
					currentList = getNextListIdx();
					/*if (bullet == null)
					{
						currentType = -1;
						currentList = BulletFactory.NONE_LIST_CONST;
						listTypes.put(indent, -1);
					}*/
				}			
				currentType = bullet.getType();
				if (listTypes.get(indent) == null || listTypes.get(indent) != currentType)
				{
					currentList = getNextListIdx();
					if (isExplicitBullet(bullet, currentList))
						listStartBullets.put(currentList, bullet.getBulletText().trim());

				}
				listTypes.put(indent, bullet.getType());
				lists.put(indent, currentList);	
			}
			if (bullet != null)
				lineBullets.add(currentList);
			else
				lineBullets.add(BulletFactory.NONE_LIST_CONST);
		}
	}

	protected boolean isExplicitBullet(ModelBullet bullet, int currentList)
	{
		return false;
	}

	protected void initNextListIdx()
	{
		nextListIdx  = 0;
	}
	
	protected int getNextListIdx()
	{
		return nextListIdx++;
	}

	protected void clearAllGreater(int currentIndent) {
		currentIndent++;
		while (lists.get(currentIndent) != null) {
			lists.remove(currentIndent);
			listTypes.remove(currentIndent);
			currentIndent++;
		}
	}

	protected void setNewPartitionsFonts(ArrayList<BasePartition> partitions) {
		for (Iterator<BasePartition> iterator = partitions.iterator(); iterator.hasNext();) {
			BasePartition partition = (BasePartition) iterator.next();
			if (partition instanceof LinkPartition && linkPrototypePartition != null) {
				partition.applyAttributes(linkPrototypePartition);
			}
		}
	
	}

	protected int getDiffIndex(String trim, String trim2)
	{
		int n = Math.min(trim.length(), trim2.length());
		for (int i = 0; i < n; i++)
		{
			if (trim.charAt(i) != trim2.charAt(i))
				return i;
		}
		return n - 1;
	}

	protected int convertBulletType(int idx, List<WikitextLine> linesList)
	{
		WikitextLine line = linesList.get(idx);
		if (line.getBullet().getType() == OrderedListBullet.SIMPLE)
			return BulletFactory.BULLETED_LIST;
		if (line.getBullet().getType() == OrderedListBullet.ABC)
			return BulletFactory.LETTERS_NUMBERED_LIST;
		if (line.getBullet().getType() == OrderedListBullet.NUMBER)
			return BulletFactory.SIMPLE_NUMBERED_LIST;
		if (line.getBullet().getType() == OrderedListBullet.ROMAN)
			return BulletFactory.ROMAN_NUMBERED_LIST;
		return 0;
	}
}
