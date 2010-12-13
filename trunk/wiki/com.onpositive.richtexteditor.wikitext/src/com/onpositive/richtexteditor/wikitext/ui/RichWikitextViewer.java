package com.onpositive.richtexteditor.wikitext.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedRenderer;
import org.eclipse.swt.custom.ExtendedStyledText;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.onpositive.richtext.model.IPartition;
import com.onpositive.richtext.model.LinkPartition;
import com.onpositive.richtext.model.meta.RGB;
import com.onpositive.richtexteditor.actions.ActionFactory;
import com.onpositive.richtexteditor.io.AbstractTextLoader;
import com.onpositive.richtexteditor.io.ITextLoader;
import com.onpositive.richtexteditor.io.ITextSerilizer;
import com.onpositive.richtexteditor.model.HyperlinksDecorator;
import com.onpositive.richtexteditor.model.IRichtextDecorator;
import com.onpositive.richtexteditor.model.resources.FontStyleManager;
import com.onpositive.richtexteditor.model.resources.ImageManager;
import com.onpositive.richtexteditor.model.resources.LayerManager;
import com.onpositive.richtexteditor.viewer.RichTextViewer;
import com.onpositive.richtexteditor.wikitext.partitions.IPartitionsUpdater;

public class RichWikitextViewer extends RichTextViewer
{

	private IPartitionsUpdater linkUpdater;

	public RichWikitextViewer(Composite parent, int style, IPartitionsUpdater linkUpdater)
	{
		super(parent, style);
		this.linkUpdater = linkUpdater;
	}

	/**
	 * 
	 * @param parent
	 * @param ruler
	 * @param style
	 */
	public RichWikitextViewer(Composite parent, IVerticalRuler ruler, int style, IPartitionsUpdater linkUpdater)
	{
		super(parent, ruler, style);
		this.linkUpdater = linkUpdater;
	}

	protected void postInit(IDocument doc, StyledText textWidget)
	{
		super.postInit(doc, textWidget);
		setLinkPartitionUpdater(linkUpdater);
		getLayerManager().getLinkPrototype().setColorRGB(new RGB(200, 0, 50));
	}

	protected ActionFactory createActionFactory()
	{
		return new CustomActionFactory(manager, this);
	}

	boolean isArrow;
	protected WikitextLayerManager wikitextLayerManager;

	protected StyledText createTextWidget(Composite parent, int styles)
	{
		final ExtendedStyledText textWidget = (ExtendedStyledText) super.createTextWidget(parent, styles | SWT.WRAP);
		textWidget.setRenderer(createRenderer(textWidget));
		// textWidget.setPagingEnabled(false);
		textWidget.setPageWidth(-1);
		textWidget.setAllowParagraphSpacing(false);
		textWidget.addMouseMoveListener(new MouseMoveListener()
		{

			public void mouseMove(MouseEvent e)
			{
				if (textWidget.getCharCount() == 0)
					return;
				int offsetAtLocation = 0;
				try
				{
					offsetAtLocation = textWidget.getOffsetAtLocation(new Point(e.x, e.y));
				} catch (IllegalArgumentException e1)
				{
					return;
				}
				IPartition partitionAtOffset = getLayerManager().getLayer().getPartitionAtOffset(offsetAtLocation);
				try
				{
					if (partitionAtOffset==null){
						return;
					}
					StyleRange styleRangeAtOffset = getTextWidget().getStyleRangeAtOffset(offsetAtLocation);
					if (isLink(partitionAtOffset, styleRangeAtOffset))
					{
						if (!isArrow)
						{
							textWidget.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND));
							isArrow = true;
						}
					} else
					{
						if (isArrow)
						{
							textWidget.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_IBEAM));
							isArrow = false;
						}
					}
				} catch (IllegalArgumentException e1)
				{
					// ignore it
				}
			}

			private boolean isLink(IPartition partitionAtOffset, StyleRange styleRangeAtOffset)
			{
				boolean b = partitionAtOffset instanceof LinkPartition;
				if (!b)
				{
					return (styleRangeAtOffset != null && styleRangeAtOffset.data != null && styleRangeAtOffset.data.equals(HyperlinksDecorator.HYPERLINK_MARKER));
				}
				return ((LinkPartition) partitionAtOffset).isValidLink();

			}

		});
		textWidget.addMouseListener(new MouseListener()
		{

			public void mouseDoubleClick(MouseEvent e)
			{

			}

			public void mouseDown(MouseEvent e)
			{

			}

			public void mouseUp(MouseEvent e)
			{
				if ((e.stateMask & SWT.CTRL) == 0)
					return;
				Object pointedObject = getPointedObject(e.x,e.y);
				if (pointedObject != null && (pointedObject instanceof String))
					onHyperLinkClicked((String) pointedObject,e);
			}

		});

		textWidget.setPageInformation(null);
		return textWidget;
	}

	/**
	 * Returns some object at (x,y), like link, image etc.
	 * 
	 * @param x
	 *            - x coordinate
	 * @param y
	 *            - y coordinate
	 * @return {@link Object} representing pointed object
	 */
	public Object getPointedObject(int x, int y)
	{
		StyledText textWidget = getTextWidget();
		int offsetAtLocation = textWidget.getOffsetAtLocation(new Point(x, y));
		return getPointedObject(offsetAtLocation);
	}
	
	public Object getPointedObject(final int offsetInText)
	{
		IPartition partitionAtOffset = getLayerManager().getLayer().getPartitionAtOffset(offsetInText);
		if (partitionAtOffset == null)
		{
			return null;
		}
		String text = partitionAtOffset.getText();

		if (partitionAtOffset instanceof LinkPartition)
		{
			LinkPartition linkPartition = (LinkPartition) partitionAtOffset;
			if (linkPartition.isValidLink())
				return linkPartition.getUrl();
		} else
		{
			if (text == null)
			{
				text = getDocument().get();
				partitionAtOffset = null;
			}
			final StyleRange[] styleRanges = new StyleRange[1];
			Display.getDefault().syncExec(new Runnable()
			{
				
				public void run()
				{
					StyleRange styleRange = getTextWidget().getStyleRangeAtOffset(offsetInText);
					styleRanges[0] = styleRange;
				}
			});
			if (styleRanges[0] == null)
				return null;
			StyleRange styleRange = styleRanges[0];
			
			if (styleRange.data != null && styleRange.data.equals(HyperlinksDecorator.HYPERLINK_MARKER))
			{
				int i = styleRange.start - (partitionAtOffset != null ? partitionAtOffset.getOffset() : 0);
				String decoratedUrl = determineUrl(text, i);
				return decoratedUrl.toString();
			}
		}
		return null;
	}

	protected ParagraphRenderer createRenderer(final ExtendedStyledText textWidget)
	{
		return new ParagraphRenderer(textWidget.getDisplay(), textWidget);
	}

	
	protected void createControl(Composite parent, int styles)
	{
		super.createControl(parent, styles);
	}

	protected void onHyperLinkClicked(String url, MouseEvent event)
	{

	}

	public void setLinkPartitionUpdater(IPartitionsUpdater updater)
	{
		wikitextLayerManager.setLinkUpdater(updater);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.onpositive.richtexteditor.viewer.RichTextViewer#createLayerManager(org.eclipse.swt.custom.StyledText,
	 *      org.eclipse.jface.text.IDocument)
	 */
	protected LayerManager createLayerManager(StyledText textWidget, IDocument doc)
	{
		
		wikitextLayerManager = new WikitextLayerManager(textWidget, doc, null)
		{

			protected ImageManager createImageManager()
			{
				return RichWikitextViewer.this.createImageManager();
			}

			protected FontStyleManager createFontStyleManager()
			{
				return new FontStyleManager(editor.getDisplay())
				{

					protected void initializeByDefault(Display display)
					{
						super.initializeByDefault(display);
						addFontStyle(h4FontStyle);
						addFontStyle(h5FontStyle);
						addFontStyle(h6FontStyle);
						fontRegistry.put(FONT_H1_NAME, new FontData[] { new FontData("Verdana", 18, SWT.BOLD) });
						fontRegistry.put(FONT_H2_NAME, new FontData[] { new FontData("Tahoma", 16, SWT.NORMAL) });
						fontRegistry.put(FONT_H3_NAME, new FontData[] { new FontData("Tahoma", 13, SWT.NORMAL) });
						fontRegistry.put(FONT_H4_NAME, new FontData[] { new FontData("Tahoma", 12, SWT.NORMAL) });
						fontRegistry.put(FONT_H5_NAME, new FontData[] { new FontData("Tahoma", 11, SWT.NORMAL) });
						fontRegistry.put(FONT_H6_NAME, new FontData[] { new FontData("Tahoma", 10, SWT.NORMAL) });
						fontRegistry.put(NORMAL_FONT_NAME, new FontData[] { new FontData("Verdana", 9, SWT.NORMAL) });
					}

				};
			}

			protected int getLineSpacing()
			{				
				return 5;
			}
		};
		return wikitextLayerManager;
	}

	/**
	 * This is method for caching images inserted into editor, and should return
	 * changed file name Does really nothing here, intended to be overriden in
	 * subclasses
	 * 
	 * @param filename
	 *            Filename to cache
	 * @param image
	 *            Image object
	 * @return Path to cached image file
	 */
	protected String cacheImage(String filename, Image image)
	{
		return filename;
	}

	protected InputStream getImageStream(String string)
	{
		return null;
	}
	
	/**
	 * Sets wikitext content to layer
	 * @param content Wikitext string
	 * @param optionsMap Some environmental options map for parsing
	 *   @see AbstractTextLoader#SEPARATE_LINES_OPTION
	 */
	public void setWikitextContent(String wikitext, HashMap<String,String> optionsMap)
	{
		((WikitextLayerManager) manager).setWikitextContent(wikitext, optionsMap);
	}

	public WikitextLayerManager getWikitextLayerManager()
	{
		return wikitextLayerManager;
	}

	protected ImageManager createImageManager()
	{
		return new ImageManager(getTextWidget().getDisplay())
		{

			protected InputStream getStream(String string) throws IOException, MalformedURLException
			{
				return getImageStream(string);
			}

		/*	public String registerImage(String filename, Image image)
			{
				filename = cacheImage(filename, image);
				super.registerImage(filename, image, false);
				return filename;
			}*/
			
			public String registerImage(String filename, Image image,
					boolean tempFile) {
				filename = cacheImage(filename, image);				
				return super.registerImage(filename, image, tempFile);
			}
		};
	}

	public void setTextLoader(ITextLoader textLoader)
	{
		if (wikitextLayerManager != null)
			wikitextLayerManager.setTextLoader(textLoader);
	}

	public void setTextSerializer(ITextSerilizer textSerilizer)
	{
		if (wikitextLayerManager != null)
			wikitextLayerManager.setTextSerilizer(textSerilizer);
	}

	public void addDecorators(Collection<IRichtextDecorator> additionalDecoratorsList)
	{
		for (Iterator<IRichtextDecorator> iterator = additionalDecoratorsList.iterator(); iterator.hasNext();)
		{
			IRichtextDecorator decorator = iterator.next();
			manager.addRichtextDecorator(decorator);
		}
	}

	/**
	 * @return
	 * @see com.onpositive.richtexteditor.wikitext.ui.WikitextLayerManager#getTextSerilizer()
	 */
	public ITextSerilizer getTextSerilizer()
	{
		return wikitextLayerManager.getTextSerilizer();
	}

	public void setUseParagraphRenderer(boolean useParagraphRenderer)
	{
		ExtendedStyledText extendedStyledText = (ExtendedStyledText) getTextWidget();
		if (useParagraphRenderer)
		{
			if (!(extendedStyledText.getRenderer() instanceof ParagraphRenderer))
			{
				extendedStyledText.setRenderer(new ParagraphRenderer(extendedStyledText.getDisplay(), extendedStyledText));
				extendedStyledText.setContent(extendedStyledText.getContent());
			}
		} else
		{
			if ((extendedStyledText.getRenderer() instanceof ParagraphRenderer))
			{
				extendedStyledText.setRenderer(new ExtendedRenderer(extendedStyledText.getDisplay(), extendedStyledText));
				extendedStyledText.setContent(extendedStyledText.getContent());
			}
		}
	}

	protected String determineUrl(String text, int i)
	{
		int j = i;
		while (i >= 0 && !Character.isWhitespace(text.charAt(i)))
			i--;
		i++;
		while (j < text.length() && !Character.isWhitespace(text.charAt(j)))
			j++;
		while (text.charAt(j - 1) == '.' || text.charAt(j - 1) == ',')
			j--;
		String decoratedUrl = text.substring(i, j);
		return decoratedUrl;
	}

}
