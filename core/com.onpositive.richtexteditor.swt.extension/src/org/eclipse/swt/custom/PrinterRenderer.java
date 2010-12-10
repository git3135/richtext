package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextLayoutOps;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.printing.Printer;

public class PrinterRenderer extends Renderer {
	
	protected double ratioX;
	protected double ratioY;
	protected Renderer screenRenderer;

	PrinterRenderer(Device device, StyledText styledText, Device originalDevice, IPageInformation pageInformation) {
		super(device, styledText);
		if (styledText.renderer instanceof Renderer)
			screenRenderer = (Renderer) styledText.renderer;
		int original = ((ExtendedStyledText) styledText).getScreenPPI().x;
		int accepting;
		int acceptingY;
		IPPIResolver printerResolver = ((ExtendedStyledText) styledText).getPrinterResolver();
		if (printerResolver != null)
		{
			accepting = printerResolver.getPPI().x;
			acceptingY = printerResolver.getPPI().y;	
		}
		else
		{
			accepting = device.getDPI().x;
			acceptingY = device.getDPI().y;
		}
		ratioX = ((double)accepting) / original;
		int originalY = ((ExtendedStyledText) styledText).getScreenPPI().y;		
		ratioY = ((double)acceptingY) / originalY;
		pagingEnabled = true;
		if (device instanceof Printer && pageInformationRenderer instanceof PrintingPageInformationRenderer)		
			((PrintingPageInformationRenderer) pageInformationRenderer).setClientArea(device.getBounds());
		((PrintingPageInformationRenderer)pageInformationRenderer).setRatioX(ratioX);
		((PrintingPageInformationRenderer)pageInformationRenderer).setRatioY(ratioY);
	}
	
	protected void createPageInformationRenderer() {
		IPageInformationRenderer pir = null;
		if( styledText instanceof ExtendedStyledText){
			ExtendedStyledText ext = (ExtendedStyledText)styledText;
			StyledTextRenderer str = ext.renderer;
			if( str instanceof Renderer){
				Renderer r = (Renderer)str;
				pir =  r.pageInformationRenderer;
			}
		}
		pageInformationRenderer = new PrintingPageInformationRenderer(this,pir);
	}
	
	protected void shiftLines(TextLayout textLayout, int lineInLayout, int delta) {
		super.shiftLines(textLayout, lineInLayout, delta);
	}
	
	void calculateCounts(int startLine, int lineCount) {
		int endLine = startLine + lineCount;
		for (int i = startLine; i < endLine; i++) {
			TextLayout layout;
			if (screenRenderer != null)
				layout = screenRenderer.getTextLayout(i); //Because we need original, "screen", by-line division here
			else
				layout = getTextLayout(i);
			int prevCount = 0;
			int prev = 0;
			if (i > 0) {
				prevCount = lineCounts[i - 1];
				prev = pageLineCounts[i - 1];
			}
			int lineCount2 = layout.getLineCount();
			lineCounts[i] = lineCount2 + prevCount;
			StyledTextContent content2 = styledText.getContent();
			if (content2 instanceof IMarginedStyledTextContent) {
				IMarginedStyledTextContent mc = (IMarginedStyledTextContent) content2;
				pageLineCounts[i] = (int) (prev + mc.getParagraphSpacing(i))
						+ lineCount2;
			} else {
				pageLineCounts[i] = lineCount2 + prev;
			}
			if (screenRenderer == null)
				disposeTextLayout(layout);
			
		}
		for (int i = startLine; i < physLineWithBreaks.length; i++) {
			//physLineWithBreaks[i] = -4;
		}
	/*	int startPhysLine = -1;
		if(startLine == 0){
			startPhysLine = 0;
		}else{
			startPhysLine = getPrevLineCountForPaging(startLine) - getParagraphSpacingInSymbols(startLine);
		}
		
		for(int i = startPhysLine; i < physLineWithBreaks.length; i++){
			physLineWithBreaks[i] = -1;
		}*/
	}
	
	public TextLayout createLayoutForVisibleLine(TextLayout originalLayout,
			int visibleLineIdx, int[] lineOffsets) {
		TextLayout localLayout = new TextLayout(styledText.getDisplay());
		String contents = originalLayout.getText();
		int start = lineOffsets[visibleLineIdx];
		int end = lineOffsets[visibleLineIdx + 1];
		String lineContents = contents.substring(start, end);
		if (lineContents.length() == 0) {
			lineContents = " ";
		}
		localLayout.setText(lineContents);
		localLayout.setFont(originalLayout.getFont());
		int[] originalRanges = originalLayout.getRanges();
		TextStyle[] originalStyles = originalLayout.getStyles();
		boolean process = true;
		for (int i = 0; i < originalRanges.length && process; i+=2)
		{
			if (originalRanges[i] >=end)
			{
				process = false;
				continue;
			}				
			if (originalRanges[i+1] <= start)
				continue;
			localLayout.setStyle(originalStyles[i / 2], Math.max(0, originalRanges[i] - start), Math.min(originalRanges[i + 1] - start, end - start));
		}
		localLayout.setAlignment(originalLayout.getAlignment());
		localLayout.setAscent((int)Math.round(originalLayout.getAscent() ));
		localLayout.setDescent((int)Math.round(originalLayout.getDescent()));
		if (visibleLineIdx == 0)
			localLayout.setIndent((int)Math.round(originalLayout.getIndent() * ratioX));
		localLayout.setJustify(originalLayout.getJustify());
		localLayout.setOrientation(originalLayout.getOrientation());
		localLayout.setSegments(originalLayout.getSegments());
		localLayout.setSpacing(originalLayout.getSpacing());
		localLayout.setTabs(originalLayout.getTabs());
		return localLayout;
	}
	
	protected boolean checkParagraphSpacing(int lineIndex, TextLayout textLayout,
			int prevTotalLineCount, int a, TextLayout localLayout,
			int practicalHeight, int height, int scaledBreakSize, int i) {
		if (TextLayoutOps.needsSpacing(textLayout, localLayout, height,
				practicalHeight)) {
			if (i >= 0) {
				int paragraphSpacing2 = (int) Math.round(getParagraphSpacing(lineIndex) * ratioY);
				if (a == 0 && prevTotalLineCount == 0&&pagingEnabled) {
					paragraphSpacing2 += scaledBreakSize / 2;
				}
				TextLayoutOps.setFirstLineSpacing(textLayout, localLayout,
						paragraphSpacing2);
				firstLineShifts.put(textLayout, paragraphSpacing2);
				return true;
			}
		}
		return false;
	}
	
	protected int getParagraphSpacing(int a) {
		int dlt = 0;
		StyledTextContent content2 = styledText.getContent();
		if (content2 instanceof IMarginedStyledTextContent) {
			IMarginedStyledTextContent mc = (IMarginedStyledTextContent) content2;
			ExtendedStyledText extendedStyledText = (ExtendedStyledText) styledText;
			dlt += (mc.getParagraphSpacing(a)
					* extendedStyledText.averageCharHeight);
		}
		return dlt + paragraphSpacing; // 0
	}
	
	protected double getScalingFactor() {
		return 1.0;
	}
	
	protected int getScaledBreakSize() {
		return (int) Math.round(ExtendedStyledTextConstants.BREAK_SIZE * ratioY);
	}
	
	protected void addPageSpacingForLastLineIfNeeded(int logicalLineIndex,
			TextLayout textLayout, int scaledBreakSize, double scalingFactor,
			int lineInLayout, int practicalHeight, int actualHeight, int i) {
		if (pagingEnabled) {
			int i1 = i;
			int is = i1 % linesPerPage == 0 ? 0 : 1;
			int nextPage = (i1 / linesPerPage) + is;
			int offset = nextPage * linesPerPage - i1;
			int ps = offset * practicalHeight;
			// ps=150;
			int delta = practicalHeight + ps - actualHeight;
			shiftLines(textLayout, lineInLayout, delta);
			if (lineHeight[logicalLineIndex] != -1) {
				lineHeight[logicalLineIndex] += delta;
			}
			originalLineHeight[logicalLineIndex] += Math.round(delta
					/ scalingFactor);
		}
	}
	
	protected int getWrapWidth(int lineIndex, StyledTextContent content2) {
		return (int) Math.round(super.getWrapWidth(lineIndex, content2) * ratioX);
	}

	public double getRatioY() {
		return ratioY;
	}

	public double getRatioX() {
		return ratioX;
	}
	
	public Font scaleFont(Font font) {
		Font scaled = scaledFonts.get(font);
		if (scaled != null)
			return scaled;
		FontData fd = font.getFontData()[0];
		FontData fd2 = new FontData(fd.getName(),
				(int) (fd.getHeight() * ratioY), fd.getStyle());
		fd2.height = (int) (fd.getHeight() * ratioY);
		scaled = new Font(styledText.getDisplay(), fd2);
		scaledFonts.put(font, scaled);
		originalFonts.put(scaled, font);
		return scaled;
	}	
	
	

}
