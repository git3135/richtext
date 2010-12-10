package org.eclipse.swt.custom;

import org.eclipse.swt.widgets.Display;

/**
 * Simple wrapper for IMarginedStyledTextContext
 * Use it, if you want widget to interpret values given by getMarginAt, getWrapWidth etc as values in mm, not in pixels
 * @author 32kda
 *
 */
public class MetricStyledTextContent implements IMarginedStyledTextContent{
	
	protected IMarginedStyledTextContent provider;
	protected final static double mmpi = 25.4;
	protected IPPIResolver ppiResolver;
	
	public MetricStyledTextContent(IMarginedStyledTextContent informationProvider, IPPIResolver ppiResolver) {
		provider = informationProvider;	
		this.ppiResolver = ppiResolver;
	}
	
	int convertX(int mmValue)
	{
		int dpiX;
		if (ppiResolver != null)
			dpiX = ppiResolver.getPPI().x;
		else
			dpiX = Display.getDefault().getDPI().x;
		double inches = mmValue / mmpi;
		return (int) Math.round(inches * dpiX);
	}
	
	double convertY(int mmValue)
	{
		int dpiY;
		if (ppiResolver != null)
			dpiY = ppiResolver.getPPI().y;
		else
			dpiY = Display.getDefault().getDPI().y;
		double inches = mmValue / mmpi;
		return inches * dpiY;
	}

	public int getBaseMargin() {		
		return convertX(provider.getBaseMargin());
	}

	public double getLineSpacing(int lineIndex) {
		return provider.getLineSpacing(lineIndex);
	}

	public int getMarginAt(int lineIndex) {
		return convertX(provider.getMarginAt(lineIndex));
	}

	public double getParagraphSpacing(int lineIndex) {
		return provider.getParagraphSpacing(lineIndex);
	}

	public int getWrapWidthAt(int lineIndex) {
		return convertX(provider.getWrapWidthAt(lineIndex));	
	}

	public void addTextChangeListener(TextChangeListener listener) {
		provider.addTextChangeListener(listener);
	}

	public int getCharCount() {
		return provider.getCharCount();
	}

	public String getLine(int lineIndex) {
		return provider.getLine(lineIndex);
	}

	public int getLineAtOffset(int offset) {
		return provider.getLineAtOffset(offset);
	}

	public int getLineCount() {
		return provider.getLineCount();
	}

	public String getLineDelimiter() {
		return provider.getLineDelimiter();
	}

	public int getOffsetAtLine(int lineIndex) {
		return provider.getOffsetAtLine(lineIndex);
	}

	public String getTextRange(int start, int length) {
		return provider.getTextRange(start, length);
	}

	public void removeTextChangeListener(TextChangeListener listener) {
		provider.removeTextChangeListener(listener);
	}

	public void replaceTextRange(int start, int replaceLength, String text) {
		provider.replaceTextRange(start, replaceLength, text);
	}

	public void setText(String text) {
		provider.setText(text);
	}
	
	public Object clone() throws CloneNotSupportedException {
        MetricStyledTextContent clone = (MetricStyledTextContent) super.clone();        
        IMarginedStyledTextContent newProvider = (IMarginedStyledTextContent) provider.clone();
        clone.provider = newProvider;
        return super.clone();
    }
	
    public int getParagraphAlignment(int lineIndex) {
        return provider.getParagraphAlignment(lineIndex);
    }
}
