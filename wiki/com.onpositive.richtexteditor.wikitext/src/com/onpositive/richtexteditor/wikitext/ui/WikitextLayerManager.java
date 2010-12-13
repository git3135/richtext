package com.onpositive.richtexteditor.wikitext.ui;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.custom.StyledText;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.BasePartitionLayer;
import com.onpositive.richtext.model.FontStyle;
import com.onpositive.richtext.model.ILineAttributeModel;
import com.onpositive.richtext.model.IPartition;
import com.onpositive.richtext.model.IRegionCompositeWrapper;
import com.onpositive.richtext.model.LinkPartition;
import com.onpositive.richtext.model.PartitionFactory;
import com.onpositive.richtext.model.RegionPartition;
import com.onpositive.richtexteditor.io.AbstractTextLoader;
import com.onpositive.richtexteditor.io.IFormatEnvironmentProvider;
import com.onpositive.richtexteditor.io.ITableStructureParserFactory;
import com.onpositive.richtexteditor.io.ITextLoader;
import com.onpositive.richtexteditor.io.ITextSerilizer;
import com.onpositive.richtexteditor.model.AbstractLayerManager;
import com.onpositive.richtexteditor.model.CitationDecorator;
import com.onpositive.richtexteditor.model.ISimpleRichTextModel;
import com.onpositive.richtexteditor.model.LayerEvent;
import com.onpositive.richtexteditor.model.resources.FontStyleManager;
import com.onpositive.richtexteditor.model.resources.LayerManager;
import com.onpositive.richtexteditor.region_provider.ContentTypeEditorProvider;
import com.onpositive.richtexteditor.util.DocumentWrapper;
import com.onpositive.richtexteditor.viewer.StyledTextLineInformationProvider;
import com.onpositive.richtexteditor.wikitext.MacrosDecorator;
import com.onpositive.richtexteditor.wikitext.io.DefaultWikitextLoader;
import com.onpositive.richtexteditor.wikitext.io.UpdatedWikitextLoader;
import com.onpositive.richtexteditor.wikitext.io.WikitextSerializer;
import com.onpositive.richtexteditor.wikitext.io.WikitextTokenProvider;
import com.onpositive.richtexteditor.wikitext.partitions.IPartitionsUpdater;

public class WikitextLayerManager extends LayerManager  implements IFormatEnvironmentProvider{

	protected static final int DEFAULT_MARGIN = 10;
	public static final int HEADER_INDENT = -10;
	protected Collection<IModelProcessor> modelPreprocessors;
	protected IPartitionsUpdater linkUpdater;
	protected ITextLoader textLoader;
	protected ITextSerilizer textSerilizer;
	protected ITableStructureParserFactory tableStructureParserFactory;

	public IPartitionsUpdater getLinkUpdater() {
		return linkUpdater;
	}

	public void setLinkUpdater(IPartitionsUpdater linkUpdater) {
		this.linkUpdater = linkUpdater;
	}

	public WikitextLayerManager(StyledText newEditor, IDocument newDoc,
			IPartitionsUpdater updater) {
		super(newEditor,new DocumentWrapper(newDoc));
		this.linkUpdater = updater;
		modelPreprocessors = new ArrayList<IModelProcessor>();
		configure();
	}

	protected void configure()
	{
		Method method;
		try {
			method = editor.getClass().getMethod("setDefaultMargin",
					new Class[] { int.class });
			method.invoke(editor, DEFAULT_MARGIN);
		} catch (Exception e) {
			e.printStackTrace();
		}
		addRichtextDecorator(new CitationDecorator());
		addRichtextDecorator(new MacrosDecorator());
	}

	public void strikethroughCommand(int offset, int length, boolean apply) {
		final IPartition partition = layer.getPartitionAtOffset(offset);
		if (partition instanceof LinkPartition) {
			offset = partition.getOffset();
			length = partition.getLength();
		}
		super.strikethroughCommand(offset, length, apply);
	}

	public void underlineCommand(int offset, int length, boolean apply) {
		final IPartition partition = layer.getPartitionAtOffset(offset);
		if (partition instanceof LinkPartition) {
			offset = partition.getOffset();
			length = partition.getLength();
		}
		super.underlineCommand(offset, length, apply);
	}

	public void italicCommand(int offset, int length, boolean apply) {
		final IPartition partition = layer.getPartitionAtOffset(offset);
		if (partition instanceof LinkPartition) {
			offset = partition.getOffset();
			length = partition.getLength();
		}
		super.italicCommand(offset, length, apply);
	}

	public void boldCommand(int offset, int length, boolean apply) {
		final IPartition partition = layer.getPartitionAtOffset(offset);
		if (partition instanceof LinkPartition) {
			offset = partition.getOffset();
			length = partition.getLength();
		}
		super.boldCommand(offset, length, apply);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.onpositive.richtexteditor.model.resources.LayerManager#changeFontCommand(java.lang.String,
	 *      int, int)
	 */
	public void changeFontCommand(String fontStyleDisplayName, int offset,
			int length) {
		final IPartition partition = layer.getPartitionAtOffset(offset);
		if (partition instanceof LinkPartition) {
			offset = partition.getOffset();
			length = partition.getLength();
		}
		if (length != 0) {
			FontStyle style = fontStyleManager
					.getFontStyle(fontStyleDisplayName);
			int shift = 0;
			char startingChar = 0, endingChar = 0;
			try {
				startingChar = doc.get(offset - 1, 1).charAt(0);
				endingChar = doc.get(offset + length, 1).charAt(0);
			} catch (Exception e) {
				if (startingChar == 0)
					startingChar = '\n';// if nothing before, we don't need to
				// add anything
			}
			final boolean isHeader = FontStyleManager
					.isHeaderFontStyle(fontStyleDisplayName);
			if (isHeader)
				shift = internalAddNewlines(offset, length, startingChar,
						endingChar);
			if (shift == 0) // No newlines added
			{
				final int lineDelimiterLength = getLineDelimiterLength();
				if (offset > 0) {
					offset = offset - lineDelimiterLength;
					length = length + lineDelimiterLength;
				}
			}
			layer.changeFontCommand(style, offset, length + shift);
		}
	}

	private int internalAddNewlines(int offset, int length, char startingChar,
			char endingChar) {
		int shift = 0;
		if (startingChar != '\r' && startingChar != '\n') {
			BasePartition caretPartition = new BasePartition(doc, offset, 2);
			layer.replacePartitions(offset, 0, "\r\n", Collections
					.singletonList(caretPartition));
			shift += 2;
		}
		if (endingChar != '\r' && endingChar != '\n') {
			BasePartition caretPartition = new BasePartition(doc, offset
					+ length + shift, 2);
			layer.replacePartitions(offset + length + shift, 0, "\r\n",
					Collections.singletonList(caretPartition));
			// shift += 2;
		}
		return shift;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onpositive.richtexteditor.model.LayerManager#addNewlinesIfNeeded(int,
	 * java.lang.String)
	 */
	
	public void addNewlinesIfNeeded(int offset, String selectedFontName) {
		if (selectedFontName.equals(FontStyleManager.NORMAL_FONT_NAME))
			return;
		char prevChar = '\n', nextChar = 0; // If there's no prevChar (offset == 0), 
		//let's suppose, that we have it
		try {
			prevChar = doc.get(offset - 1, 1).charAt(0);
			nextChar = doc.get(offset, 1).charAt(0);
		} catch (Exception e) {
			prevChar = '\n';
			nextChar = 0;
		}
		int shift = internalAddNewlines(offset, 0, prevChar, nextChar);
		if (shift > 0)
			editor.setCaretOffset(offset + shift);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.onpositive.richtexteditor.model.resources.LayerManager#createBasePartitionLayer()
	 */
	protected BasePartitionLayer createBasePartitionLayer() {
		return new WikitextPartitionLayer();
	}

	public String getWikitextContent() {
		if (textSerilizer == null)
			textSerilizer = new WikitextSerializer(layer, layer
					.getDoc(), new StyledTextLineInformationProvider(this));
		final String res = textSerilizer.serializeAllToStr();
		return res;
	}

	/**
	 * Sets wikitext content to layer
	 * @param content Wikitext string
	 * @param optionsMap Some environmental options map for parsing
	 *   @see AbstractTextLoader#SEPARATE_LINES_OPTION
	 */
	public void setWikitextContent(String content, HashMap<String,String> optionsMap) {
		getTextLoader(optionsMap);

		//InputStream stream = new ByteArrayInputStream(content.getBytes());
		//try {
			final ISimpleRichTextModel model = textLoader.parse(content);
			setModel(model);
	/*	} catch (IOException e) {
			e.printStackTrace();
		}*/
	}

	public ITextLoader getTextLoader(HashMap<String, String> optionsMap) {
		if (textLoader == null)
			textLoader = new DefaultWikitextLoader(layer, imageManager, linkPrototypePartition);
		if (textLoader instanceof UpdatedWikitextLoader)
		{
			((UpdatedWikitextLoader) textLoader).setCanMergeLines(!"true".equals(optionsMap.get(AbstractTextLoader.SEPARATE_LINES_OPTION)));
		}
		return textLoader;
	}

	public void setModel(final ISimpleRichTextModel model) {
		if (model == null)
			return;
		// Document document = new Document(model.getText());
		// layer.connectToDocument(document);
		// layer.getStorage().setPartitions(model.getPartitions());
		preProcessModel(model);
		layer.setIgnoreDocumentEvents(true);
		editor.setRedraw(false);
		try {
			set(model);
		} catch (Exception e) {
			e.printStackTrace();
		}
		editor.setRedraw(true);
		layer.setIgnoreDocumentEvents(false);
//		layerChanged(new LayerEvent(layer, new ArrayList<IPartition>(model
//				.getPartitions())));
	}

	/**
	 * We may need some partitions or oter text model preprocessing before
	 * setting this model to editor. This method does such preprocessing
	 * 
	 * @param model
	 */
	protected void preProcessModel(ISimpleRichTextModel model) {
		for (Iterator<IModelProcessor> iterator = modelPreprocessors.iterator(); iterator
				.hasNext();) {
			IModelProcessor processor = iterator.next();
			processor.processModel(model);
		}

	}

	public IRegionCompositeWrapper getWrapperForContentType(String contentType,AbstractLayerManager manager,RegionPartition partition) {
		if (contentType.equals(RegionPartition.PLAIN_TEXT_CONTENT_TYPE)
				|| (contentType.length() < 3)) {
			return ContentTypeEditorProvider
					.getEditorForContentTypeWithFakeFileName("a.txt", "",
							editor,manager,partition);
		}
		if (contentType
				.startsWith(WikitextTokenProvider.REGION_CONTENT_TYPE_PREFFIX))
			contentType = contentType
					.substring(WikitextTokenProvider.REGION_CONTENT_TYPE_PREFFIX
							.length());
		else if (contentType.startsWith("!"))
			contentType = contentType.substring(1);
		int indexOf = contentType.indexOf('/');
		if (indexOf > 0) {
			contentType = contentType.substring(indexOf + 1);
		}
		if (contentType.endsWith(":")) {
			contentType = RegionPartition.PLAIN_TEXT_CONTENT_TYPE;
		}
		// if (contentType.equals(RegionPartition.JAVA_STR_CONTENT_TYPE))
		return ContentTypeEditorProvider
				.getEditorForContentTypeWithFakeFileName("a." + contentType,
						"", editor, manager,partition);
		// return super.getWidgetForContentType(contentType);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.onpositive.richtexteditor.model.resources.LayerManager#setStyleRangeByPartition(com.onpositive.richtext.model.BasePartition)
	 */
	protected void setStyleRangeByPartition(BasePartition partition) {
		checkHeaderIndent(partition);
		super.setStyleRangeByPartition(partition);
	}

	protected void setStyleRangesByPartitionList(
			ArrayList<BasePartition> partitions) {
		if (partitions.size() == 0)
			return;
		BasePartition last = partitions.get(partitions.size() - 1);
		BasePartition totalPartition = PartitionFactory.createAsSampleStyle(
				partitions.get(0), doc, partitions.get(0).getOffset(), last
						.getOffset()
						+ last.getLength() - partitions.get(0).getOffset()); // Create
		// a
		// fake
		// partition
		// for
		// header
		// check
		checkHeaderIndent(totalPartition);
		super.setStyleRangesByPartitionList(partitions);
	}

	protected void checkHeaderIndent(BasePartition partition) {
		String text = partition.getText();
		int offset = partition.getOffset();
		int end = offset + partition.getLength() - 1;
		if (text != null) {
			if (text.trim().equals(""))
				return;
			int initialOffset = offset;
			while (text.charAt(offset - initialOffset) == '\r'
					|| text.charAt(offset - initialOffset) == '\n')
				offset++;
			while (text.charAt(end - initialOffset) == '\r'
					|| text.charAt(end - initialOffset) == '\n')
				end--;
		}
		int startLine = editor.getLineAtOffset(offset);
		int endLine = editor.getLineAtOffset(end);

		final ILineAttributeModel lineAttributeModel = layer
				.getLineAttributeModel();

		if (isHeader(partition)) {
			lineAttributeModel.setLineIndentDirectly(startLine, endLine
					- startLine + 1, HEADER_INDENT);
		} else if (lineAttributeModel.getLineIndentDirectly(startLine) == HEADER_INDENT) {
			lineAttributeModel.setLineIndentDirectly(startLine, endLine
					- startLine + 1, 0);
			/*
			 * String str = partition.getText(); final int offsetAtLine =
			 * editor.getOffsetAtLine(startLine + 1); if (startLine + 1 <
			 * editor.getLineCount() && partition.getOffset() +
			 * partition.getLength() > offsetAtLine) { str =
			 * partition.getTextUpToOffset(offsetAtLine); } if
			 * (str.trim().length() != 0)
			 * lineAttributeModel.setLineIndentDirectly(startLine,1,0);
			 */
		}
	}

	protected boolean isHeader(BasePartition partition) {
		if (partition.getFontDataName().toLowerCase().indexOf("header") > -1)
			return true;
		return false;
	}

	public LinkPartition insertLinkPartititon(com.onpositive.richtext.model.meta.Point point, String name,
			String url) {
		if (url.startsWith("#")) {
			url = WikitextTokenProvider.getInstance().getKeyword(
					WikitextTokenProvider.TICKET_LINK_PREFFIX)
					+ url.substring(1);
			LinkPartition insertLinkPartititon = super.insertLinkPartititon(
					point, name, url);
			if (linkUpdater != null) {
				boolean updatePartition = linkUpdater.updatePartition(insertLinkPartititon);
				if (updatePartition){
					layer.handleEvent(new LayerEvent(layer, (Collection)Collections.singleton(insertLinkPartititon)));	
				}
			}
			return insertLinkPartititon;
		} else {
			if (!url.startsWith(WikitextTokenProvider.getInstance().getKeyword(
					WikitextTokenProvider.HTTP_LINK_PREFFIX))
					&& !url
							.startsWith(WikitextTokenProvider
									.getInstance()
									.getKeyword(
											WikitextTokenProvider.WIKI_LINK_PREFFIX))
					&& !url
							.startsWith(WikitextTokenProvider
									.getInstance()
									.getKeyword(
											WikitextTokenProvider.ATTACHMENT_LINK_PREFFIX)))

				if (url.indexOf("://") == -1) {
					url = WikitextTokenProvider.getInstance().getKeyword(
							WikitextTokenProvider.WIKI_LINK_PREFFIX)
							+ url;
					// super.insertLinkPartititon(point, name, url);
				}
			// url = WikitextTokenProvider.getInstance().getKeyword(
			// WikitextTokenProvider.HTTP_LINK_PREFFIX)
			// + url;
			LinkPartition insertLinkPartititon = super.insertLinkPartititon(
					point, name, url);
			if (linkUpdater != null) {
				boolean updatePartition = linkUpdater.updatePartition(insertLinkPartititon);
				if (updatePartition){
					layer.handleEvent(new LayerEvent(layer, (Collection)Collections.singleton(insertLinkPartititon)));	
				}
			}
			return insertLinkPartititon;
		}
	}

	/**
	 * Model preprocessors is used to make some changes to
	 * {@link ISimpleRichTextModel}before it's displaying in editor
	 * 
	 * @param processor
	 *            {@link IModelProcessor} to add
	 * @return
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	public boolean addModelPreProcessor(IModelProcessor processor) {
		return modelPreprocessors.add(processor);
	}

	/**
	 * Model preprocessors is used to make some changes to
	 * {@link ISimpleRichTextModel}before it's displaying in editor
	 * 
	 * @param processor
	 *            {@link IModelProcessor} to remove
	 * @return
	 * @see java.util.ArrayList#remove(java.lang.Object)
	 */
	public boolean removeModelPreProcessor(IModelProcessor processor) {
		return modelPreprocessors.remove(processor);
	}

	public void setTextLoader(ITextLoader textLoader)
	{
		this.textLoader = textLoader;		
	}

	/**
	 * @return the textSerilizer
	 */
	public ITextSerilizer getTextSerilizer()
	{
		return getSerializer();
	}

	/**
	 * @param textSerilizer Text Serilizer to set
	 */
	public void setTextSerilizer(ITextSerilizer textSerilizer)
	{
		this.textSerilizer = textSerilizer;
	}

	public ITextLoader getLoader()
	{
		return textLoader;
	}

	public ITextSerilizer getSerializer()
	{
		return textSerilizer;
	}

	public ITableStructureParserFactory getTableStructureParserFactory()
	{
		return tableStructureParserFactory;
	}

	public void setTableStructureParserFactory(ITableStructureParserFactory factory)
	{
		tableStructureParserFactory = factory;
	}

}
