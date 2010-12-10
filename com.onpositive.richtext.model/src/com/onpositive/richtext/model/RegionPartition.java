package com.onpositive.richtext.model;

import com.onpositive.richtext.model.meta.GlyphMetrics;
import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtext.model.meta.Point;
import com.onpositive.richtext.model.meta.Rectangle;
import com.onpositive.richtext.model.meta.StyleRange;
import com.onpositive.richtexteditor.model.AbstractLayerManager;


/**
 *  
 *
 */
public class RegionPartition extends ObjectPartition implements IDisposablePartion
{
	/**
	 * PLAIN TEXT
	 */
	public static final String PLAIN_TEXT_CONTENT_TYPE = "plainText";
	/**
	 * Table. Not really implemented yet. We need this for preventing data loss
	 */
	public static final String TABLE_CONTENT_TYPE = "table";
	/**
	 * Definition list. Not really implemented yet. We need this for preventing data loss
	 */
	public static final String DEFLIST_CONTENT_TYPE = "defList";
	protected static final int MARGIN = 5;
	protected IRegionCompositeWrapper wrapper;	
	protected String contentType;
	protected String initialText;
	
	
	/**
	 * @param layer Partition layer
	 * @param offset Offset of partition
	 * @param length Length of partition
	 * @param widget Widget, that this partition represent
	 */
	public RegionPartition(ITextDocument document, int offset, int length, Object widget)
	{		
		super(document, offset, length);
		//wrapper = new CompositeEditorWrapper(widget,this);		
	}

	/**
	 * @param layer Partition layer
	 * @param offset Offset of partition
	 * @param length Length of partition
	 * @param contentType Content type (plain text, source code etc. There is no some hardly determined constants, 
	 * really we need such values in parser/serializer and in AbstractLayerManager.getWidgetForContentType() )
	 * @param initialText Initial text, to initialize control later
	 */
	public RegionPartition(ITextDocument document, int offset, int length, String contentType, String initialText)
	{		
		super(document, offset, length);
		this.contentType = contentType;		
		this.initialText = initialText;
	}
	
	public Object getTopLevelObject()
	{
		return wrapper.getTopLevelObject();
	}
	
	public Object getObject()
	{
		return wrapper.getMainObject();
	}

	
	public void setObject(Object object)
	{
		throw new RuntimeException("setObject() should not be called for instances of " + this.getClass().getCanonicalName() + " class.");
	}
	
	
	
	public StyleRange getStyleRange(AbstractLayerManager manager)
	{	
		if (manager.isUseRegionControls())
		{
			createWrapper(manager);
			StyleRange style = new StyleRange ();
			style.start = offset;
			style.length = 1;
	//		widget.pack(); //TODO We don't call pack() anywhere. It may result in some side effects
			Rectangle rect = wrapper.getBounds();
			int ascent =  2*rect.height/3;
			int descent = rect.height - ascent;
			style.metrics = new GlyphMetrics(ascent + MARGIN, descent + MARGIN, rect.width + 2*MARGIN);		
			return style;
		}
		else
			return super.getStyleRange(manager);
	}
	
	public int getComponentHeight()
	{
		return wrapper.getHeight();
	}
	
	public void dispose()
	{
		initialText = wrapper.getContent();	
		wrapper.dispose();
		wrapper = null;
	}
	
	/**
	 * @return True, if partition must be single partition on some line
	 */
	
	public boolean requiresSingleLine()
	{
		return true;
	}
	
	public int getInitialHeight()
	{
		return wrapper.getInitialHeight();
	}
	
	public Point getSize()
	{
		return wrapper.getSize();
	}
	
	/**
	 * If one of the partition's symbols should be deleted, should the whole
	 * partition be deleted
	 * 
	 * @return true if yes, false otherwise
	 */
	
	public boolean requiresFullDeletion()
	{
		return true;
	}
	
	/**
	 * Can some symbols be type in the center of the partition
	 * 
	 * @return true if yes, false otherwise
	 */
	
	public boolean allowsInnerTyping()
	{
		return false;
	}

	
	/**
	 * Returns a wrapper, which wraps inner complex Composite-based editors behaviour.
	 * Usually needed to add listeners to them 
	 * @return the wrapper
	 */
	public IRegionCompositeWrapper getWrapper()
	{
		return wrapper;
	}

	public void setSize(int width, int height)
	{
		wrapper.setSize(width,height);		
	}

	public void setLocation(int x, int y)
	{
		//System.out.println("Setting:"+x+":"+y);
		Point location = wrapper.getLocation();
		if (location.x!=x||location.y!=y){
			wrapper.setLocation(x,y);
		}
	}

	
	/**
	 * @return the contentType
	 */
	public String getContentType()
	{
		return contentType;
	}

	
	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}
	
	public String getContent()
	{
		if (wrapper != null)
			return wrapper.getContent();
		return initialText;		
	}

	public IRegionCompositeWrapper createWrapper(AbstractLayerManager manager)
	{
		if (wrapper == null)
		{
			wrapper = manager.getWrapperForContentType(contentType,manager,this);
			if (wrapper!=null){
			wrapper.setText(initialText);
			
			wrapper.addRegionCompositeWrapperListener(manager.createRegionCompositeWrapperListener(this));
			}
		}		
		return wrapper;
	}

	public void setWrapper(IRegionCompositeWrapper wrapper)
	{
		this.wrapper = wrapper;		
	}


}
