package com.onpositive.richtexteditor.wikitext.regions;

public interface IWikitextRegionTypesProvider
{
	public RegionType[] getSupportedRegionTypes();
	
	public String[] getSupportedRegionTypesDisplayNames();
}
