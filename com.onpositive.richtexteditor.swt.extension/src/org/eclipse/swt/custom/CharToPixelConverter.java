package org.eclipse.swt.custom;

/**
 * Use it, if you want to set convert e.g. margin/wrap width values from char amount to pixel amounr
 * @author 32kda
 *
 */
public class CharToPixelConverter implements IDimensionConverter{

	protected ExtendedStyledText styledText;
	
	public CharToPixelConverter() {
		// TODO Auto-generated constructor stub
	}
	
	public CharToPixelConverter(ExtendedStyledText styledText) {
		this.styledText = styledText;
	}
	
	public double convert(double value) {
		int averageCharWidth = styledText.getAverageCharWidth();
		return value * averageCharWidth;
	}

	public ExtendedStyledText getStyledText() {
		return styledText;
	}

	public void setStyledText(ExtendedStyledText styledText) {
		this.styledText = styledText;
	}
	
	

}
