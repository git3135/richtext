package com.onpositive.wiki.ui.table;


public class TableCell  {

	protected String text;
	protected String additionalInfo;
	
	public TableCell(String s) {
		this.text=s;
	}

	public void setAdditionalInfo(String additionalInfo)
	{
		this.additionalInfo = additionalInfo;
	}

	public String getAdditionalInfo()
	{
		return additionalInfo;
	}

}
