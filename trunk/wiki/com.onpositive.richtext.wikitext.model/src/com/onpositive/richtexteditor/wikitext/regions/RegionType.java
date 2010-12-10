package com.onpositive.richtexteditor.wikitext.regions;

public class RegionType
{
	protected String displayName;
	protected String typeString;
	
	public RegionType(String displayName, String typeString)
	{
		super();
		this.displayName = displayName;
		this.typeString = typeString;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public String getTypeString()
	{
		return typeString;
	}
	
	
}
