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
import com.onpositive.richtext.model.LinkPartition;
import com.onpositive.richtext.model.meta.BasicBullet;
import com.onpositive.richtext.model.meta.BulletFactory;
import com.onpositive.richtexteditor.io.ITextLoader;
import com.onpositive.richtexteditor.model.ISimpleRichTextModel;
import com.onpositive.richtexteditor.wikitext.parser.DefaultLineParserFactory;
import com.onpositive.richtexteditor.wikitext.parser.DefaultLinesProvider;
import com.onpositive.richtexteditor.wikitext.parser.ILineParser;
import com.onpositive.richtexteditor.wikitext.parser.ModelBullet;
import com.onpositive.richtexteditor.wikitext.parser.OrderedListBullet;
import com.onpositive.richtexteditor.wikitext.parser.WikitextLexEventConsumer;
import com.onpositive.richtexteditor.wikitext.parser.WikitextLine;
import com.onpositive.richtexteditor.wikitext.parser.WikitextParser;

public class ParserTestWikitextLoader implements ITextLoader
{
	protected BasePartition linkPrototypePartition;
	protected HashMap<Integer, Integer> lists = new HashMap<Integer, Integer>();
	protected HashMap<Integer, Integer> listTypes = new HashMap<Integer, Integer>();
	protected ArrayList<Integer> lineBullets = new ArrayList<Integer>();

	public ParserTestWikitextLoader(BasePartitionLayer layer, Object imageManager, BasePartition linkPrototypePartition)
	{
		this.linkPrototypePartition = linkPrototypePartition;
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

	public ISimpleRichTextModel parse(String text)
	{
		DefaultLinesProvider provider = new DefaultLinesProvider(text,true);
		final List<WikitextLine> linesList = provider.getLinesList();		
		final WikitextLexEventConsumer consumer = new WikitextLexEventConsumer(null);
		ILineParser parser = new DefaultLineParserFactory().createParser(consumer);
		WikitextParser wikitextParser = new WikitextParser(consumer, parser);
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
				final String bulletStr = linesList.get(line).getBullet().getBulletText();
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
				return consumer.getText();
			}

			public int getIndent(int lineIndex) {
				return linesList.get(lineIndex).getIndent();
			}
			
			
			public boolean equals(Object obj) //TODO for testing purposes
			{
				if (!(obj instanceof ISimpleRichTextModel))
					return false;
				ISimpleRichTextModel model = ((ISimpleRichTextModel)obj);
				String trim = getText().trim();
				trim = trim.replaceAll("\\s+\\n","\n");
				trim = trim.replaceAll("\\n\\s+","\n");
				String modelText = model.getText().trim();
				modelText = modelText.replaceAll("\\s+\\n","\n");
				modelText = modelText.replaceAll("\\n\\s+","\n");
				if (!trim.equals(modelText))
				{
					int index = getDiffIndex(trim, modelText);
					System.out.println("Parsed text differs from idx " + index + ":");
					System.out.println("/------------------------------------1-------------------------------------/");
					System.out.println(getText().substring(index));
					System.out.println("/------------------------------------2------------------------------------/");
					System.out.println(model.getText().substring(index));
					System.out.println("/-------------------------------------------------------------------------/");
					throw new RuntimeException("Parsed text differs from idx " + index);
				}
/*				if (!comparePartitions(getPartitions(), model.getPartitions()))
					return false;*/
				
				return true;
			}


		};
		return z;
	}
	
	protected boolean comparePartitions(List<BasePartition> partitions, List<BasePartition> partitions2)
	{
		int size = partitions.size();
		int size2 = partitions2.size();
		if (size != size2)
			System.out.println("Partition lists size masmatch: " + size + " for first, " + size2 + " for second.");
		int n = Math.min(size, size2);		
		for (int i = 0; i < n; i++)
		{
			BasePartition part1 = partitions.get(i);
			BasePartition part2 = partitions2.get(i);
			if ((part1.getOffset() != part2.getOffset() || part1.getLength() != part2.getLength()) ||
				part1.getClass() != part2.getClass() ||
				!part1.equalsByStyle(part2))
			{
				System.out.println("Part. list mismatch: ");
				System.out.println("1: " + part1.toString());
				System.out.println("2: " + part2.toString());
				throw new RuntimeException("Part list differs from idx " + i);
			}
		}
		return true;
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

	protected ArrayList<Integer> createEmptyAligns(int size)
	{		
		ArrayList<Integer> list = new ArrayList<Integer>(size);
		for (int i = 0; i < size; i++)
		{
			list.add(0);
		}
		return list;
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

	protected void setNewPartitionsFonts(ArrayList<BasePartition> partitions) {
		for (Iterator<BasePartition> iterator = partitions.iterator(); iterator.hasNext();) {
			BasePartition partition = (BasePartition) iterator.next();
			if (partition instanceof LinkPartition) {
				partition.applyAttributes(linkPrototypePartition);
			}
		}

	}
	
	protected void createBulletNumbers(List<WikitextLine> lines)
	{
		int currentList = -1;
		int currentType = -1;
		for (int i = 0; i < lines.size(); i++)
		{
			WikitextLine line = lines.get(i);
			ModelBullet bullet = line.getBullet();
			if ((bullet == null && currentType != -1) || (bullet != null && bullet.getType() != currentType) || 
				(i > 0 && line.getIndent() != lines.get(i-1).getIndent()))
			{
				int indent = line.getIndent();
				if ((bullet != null && currentType > -1) && 
					i > 0 && indent < lines.get(i - 1).getIndent())
				{
					currentList = lists.get(indent);
					int listType = listTypes.get(indent);
					clearAllGreater(indent);
					if (listType != bullet.getType())
						currentList++;
				}
				else if (currentType != -1)
				{
					currentList++;
					if (bullet == null)
					{
						currentType = -1;
						currentList = BulletFactory.NONE_LIST_CONST;
					}
				}
				lists.put(indent, currentList);				
				if (bullet != null)
				{
					currentType = bullet.getType();
					listTypes.put(indent, bullet.getType());
				}
			}
			lineBullets.add(currentList);			
		}
	}
	
	protected void clearAllGreater(int currentIndent) {
		currentIndent++;
		while (lists.get(currentIndent) != null) {
			lists.remove(currentIndent);
			listTypes.remove(currentIndent);
			currentIndent++;
		}
	}

}
