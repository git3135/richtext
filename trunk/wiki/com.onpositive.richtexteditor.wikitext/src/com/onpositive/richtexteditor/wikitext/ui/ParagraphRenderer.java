package com.onpositive.richtexteditor.wikitext.ui;

import java.util.Collection;

import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.ExtendedRenderer;
import org.eclipse.swt.custom.ExtendedStyledText;
import org.eclipse.swt.custom.ITextLayoutDecorator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TextLayoutOpsProvider;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.TextLayout;

public class ParagraphRenderer extends ExtendedRenderer
{
	private int PAR_SPACING = 0;
	protected int paragraphSpacing = 0;	
	protected StyledText styledText;

	public ParagraphRenderer(Device device, StyledText styledText)
	{
		super(device, styledText);
		this.styledText = styledText;
		PAR_SPACING=TextLayoutOpsProvider.getInstance().canShiftParagraphs()?15:0;
	}
	
	protected void checkParagraphSpacing(int lineIndex, TextLayout textLayout,
			int a, TextLayout localLayout, int practicalHeight,
			int height, int i) {
		if (TextLayoutOpsProvider.getInstance().needsSpacing(textLayout, localLayout, height,
				practicalHeight)) {
			if (i >= 0) {
				boolean empty = textLayout.getText().trim().isEmpty();
				int paragraphSpacing2 = getParagraphSpacing(lineIndex,empty);				
				TextLayoutOpsProvider.getInstance().setFirstLineSpacing(textLayout, localLayout,
						paragraphSpacing2);
			}
		}
	}
	
	protected int getParagraphSpacing(int a, boolean empty) {
		int dlt = PAR_SPACING;
		Bullet lineBullet = styledText.getLineBullet(a);
		if (lineBullet != null)
		{
			Bullet lineBullet2 = styledText.getLineBullet(a-1);
			if (a!=0&&lineBullet2!=null){
				if (lineBullet2.type==lineBullet.type){
					return paragraphSpacing;
				}
				else{
					if (((ExtendedStyledText)styledText).getLineIndent(a)==((ExtendedStyledText)styledText).getLineIndent(a-1)){
						return paragraphSpacing;
					}
				}
			}
		}
		if (empty){
				
			return 0;
		}
		if (a>0&&styledText.getContent().getLine(a-1).trim().length()==0){
			return 0;
		}
		return dlt + paragraphSpacing;
	}
	
	protected void drawBullet(Bullet bullet, GC gc, int paintX, int paintY, int index, int lineAscent, int lineDescent,int lineIndex)
	{
		Bullet lineBullet = lineIndex!=0?styledText.getLineBullet(lineIndex-1):null;
		if (lineIndex!=0&&lineBullet!=bullet){
			ExtendedStyledText s=(ExtendedStyledText) styledText;
			if (lineBullet==null|| s.getLineIndent(lineIndex)==s.getLineIndent(lineIndex)-1){
				if (s.getContent().getLine(lineIndex).trim().length()!=0){
					if (lineIndex>0&&styledText.getContent().getLine(lineIndex-1).trim().length()==0){
						
					}
					else{
						paintY+=PAR_SPACING;
					}
				}					
			}
			
		}		
		super.drawBullet(bullet, gc, paintX, paintY, index, lineAscent, lineDescent,lineIndex);
	}
	
	
	protected TextLayout adjustPagingForVisibleLine(int logicalLineIndex,
			TextLayout textLayout, 	int linesInLayout, int lineInLayout) {
		TextLayout localLayout = createLayoutForVisibleLine(textLayout,
				lineInLayout);
		int practicalHeight = getPracticalLayoutHeight(localLayout);
		//int practicalHeight = 0;
		int actualHeight = TextLayoutOpsProvider.getInstance().getLineHeight(textLayout, lineInLayout);
		boolean isFirstLineInLayout = lineInLayout == 0;
		if (isFirstLineInLayout)
			checkParagraphSpacing(logicalLineIndex, textLayout,
						lineInLayout, localLayout, practicalHeight,
						actualHeight, lineInLayout);
		return localLayout;
	}
	

	protected TextLayout createLayoutForVisibleLine(TextLayout originalLayout,
			int visibleLineIdx) {
		TextLayout localLayout = new TextLayout(styledText.getDisplay());
		int[] lineOffsets = originalLayout.getLineOffsets();

		String contents = originalLayout.getText();
		int start = lineOffsets[visibleLineIdx];
		int end = lineOffsets[visibleLineIdx + 1];
		String lineContents = contents.substring(start, end);
		if (lineContents.length() == 0) {
			lineContents = " ";
		}
		localLayout.setText(lineContents);
		int length = originalLayout.getText().length();
		if (length > 0 && length < start) {

			localLayout
					.setStyle(originalLayout.getStyle(start), 0, end - start);
		}

		localLayout.setAlignment(originalLayout.getAlignment());
		localLayout.setAscent(originalLayout.getAscent());
		localLayout.setDescent(originalLayout.getDescent());
		localLayout.setFont(originalLayout.getFont());
		localLayout.setIndent(originalLayout.getIndent());
		localLayout.setJustify(originalLayout.getJustify());
		localLayout.setOrientation(originalLayout.getOrientation());
		localLayout.setSegments(originalLayout.getSegments());
		localLayout.setSpacing(originalLayout.getSpacing());
		localLayout.setTabs(originalLayout.getTabs());
		return localLayout;
	}
	
	private int getPracticalLayoutHeight(TextLayout localLayout) {
		int practicalHeight = localLayout.getBounds().height;
		if (localLayout.getText().length() == 0) {
			practicalHeight += localLayout.getSpacing() / 2;
		}
		return practicalHeight;
	}
	
	public TextLayout getTextLayout(int lineIndex) {		
		TextLayout textLayout = super.getTextLayout(lineIndex);
		adjustLayout(textLayout);
		int linesInLayout = textLayout.getLineCount();
		if (lineIndex > 0)
		{
			TextLayout localLayout = adjustPagingForVisibleLine(lineIndex,
					textLayout, linesInLayout ,0);
			localLayout.dispose();
		}		
		return textLayout;
	}

	private void adjustLayout(TextLayout textLayout) {
		if (decs!=null){
			for (ITextLayoutDecorator d:decs){
				d.decorateTextLayout(textLayout);
			}
		}
	}
	Collection<ITextLayoutDecorator>decs;

	public void setAdditionDecorators(
			Collection<ITextLayoutDecorator> textLayoutDecorators) {
		decs=textLayoutDecorators;
	}
	
	
}
