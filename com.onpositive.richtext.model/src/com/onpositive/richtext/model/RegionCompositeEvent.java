package com.onpositive.richtext.model;



public class RegionCompositeEvent
{
	protected Object sourceComposite;
	protected IRegionCompositeWrapper source;
	protected int type;
	protected Object object;
	
	public static final int LINE_COUNT_CHANGE = 1;
	public static final int CARET_MOVE = 2;
	public static final int SET_DIRTY = 3;
	public static final int REMOVE_DIRTY = 4;
	
	public RegionCompositeEvent(Object sourceComposite,
			IRegionCompositeWrapper source, int type)
	{
		super();
		this.sourceComposite = sourceComposite;
		this.source = source;
		this.type = type;
	}

	public RegionCompositeEvent(IRegionCompositeWrapper source, int type)
	{
		super();
		this.source = source;
		this.type = type;
	}

	
	public RegionCompositeEvent(IRegionCompositeWrapper source,
			int type, Object object)
	{
		this(source, type);
		this.object = object;
	}

	/**
	 * @return the sourceComposite
	 */
	public Object getSourceComposite()
	{
		return sourceComposite;
	}

	
	/**
	 * @param sourceComposite the sourceComposite to set
	 */
	public void setSourceComposite(Object sourceComposite)
	{
		this.sourceComposite = sourceComposite;
	}

	
	/**
	 * @return the source
	 */
	public IRegionCompositeWrapper getSource()
	{
		return source;
	}

	
	/**
	 * @param source the source to set
	 */
	public void setSource(IRegionCompositeWrapper source)
	{
		this.source = source;
	}

	
	/**
	 * @return the type
	 */
	public int getType()
	{
		return type;
	}

	
	/**
	 * @param type the type to set
	 */
	public void setType(int type)
	{
		this.type = type;
	}

	
	/**
	 * @return the object
	 */
	public Object getObject()
	{
		return object;
	}
	
}
