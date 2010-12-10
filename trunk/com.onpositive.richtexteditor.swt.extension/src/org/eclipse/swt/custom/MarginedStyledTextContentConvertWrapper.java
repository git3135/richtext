/**
 * 
 */
package org.eclipse.swt.custom;


/**
 * This wrapper is used, if we want to set content's margins, wrap width etc not in pixels, but in millimeters, char counts etc.
 * There are 3 dimensional converterss - for base margin, line margin and wrap width.
 * They implement {@link IDimensionConverter}, and you can pass them to constructor or set independently by special method
 * @author 32kda
 *
 */
public class MarginedStyledTextContentConvertWrapper implements
		IMarginedStyledTextContent {

	protected IMarginedStyledTextContent provider;
	protected IDimensionConverter baseMarginConverter;
	protected IDimensionConverter marginConverter;
	protected IDimensionConverter wrapWidthConverter;
	
	public MarginedStyledTextContentConvertWrapper(IMarginedStyledTextContent provider) {
		this.provider = provider;
	}
	
	public MarginedStyledTextContentConvertWrapper(IMarginedStyledTextContent provider, IDimensionConverter baseMarginConverter,
													IDimensionConverter marginConverter, IDimensionConverter wrapWidthConverter) 
	{
		this.provider = provider;
		this.baseMarginConverter = baseMarginConverter;
		this.marginConverter = marginConverter;
		this.wrapWidthConverter = wrapWidthConverter;
	}
	
	public int getBaseMargin() {
		return (int) Math.round(baseMarginConverter.convert(provider.getBaseMargin()));
	}

	public double getLineSpacing(int lineIndex) {
		return provider.getLineSpacing(lineIndex);
	}

	public int getMarginAt(int lineIndex) {
		return (int) Math.round(marginConverter.convert(provider.getMarginAt(lineIndex)));
	}

	public double getParagraphSpacing(int lineIndex) {
		return provider.getParagraphSpacing(lineIndex);
	}

	public int getWrapWidthAt(int lineIndex) {
		return (int) Math.round(wrapWidthConverter.convert(provider.getWrapWidthAt(lineIndex)));
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

	public IDimensionConverter getBaseMarginConverter() {
		return baseMarginConverter;
	}

	public void setBaseMarginConverter(IDimensionConverter baseMarginConverter) {
		this.baseMarginConverter = baseMarginConverter;
	}

	public IDimensionConverter getMarginConverter() {
		return marginConverter;
	}

	public void setMarginConverter(IDimensionConverter marginConverter) {
		this.marginConverter = marginConverter;
	}

	public IDimensionConverter getWrapWidthConverter() {
		return wrapWidthConverter;
	}

	public void setWrapWidthConverter(IDimensionConverter wrapWidthConverter) {
		this.wrapWidthConverter = wrapWidthConverter;
	}
	    
    public Object clone() throws CloneNotSupportedException {
        MarginedStyledTextContentConvertWrapper clone = (MarginedStyledTextContentConvertWrapper) super.clone();        
        IMarginedStyledTextContent newProvider = (IMarginedStyledTextContent) provider.clone();
        clone.provider = newProvider;
        return clone;
    }

    public int getParagraphAlignment(int lineIndex) {
        return provider.getParagraphAlignment(lineIndex);
    }
}
