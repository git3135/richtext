package com.onpositive.richtexteditor.io;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.Document;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.meta.BasicBullet;
import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtexteditor.model.ISimpleRichTextModel;
import com.onpositive.richtexteditor.util.DocumentWrapper;

public class SimpleTextLoader extends AbstractTextLoader
{

	/* (non-Javadoc)
	 * @see com.onpositive.richtexteditor.io.ITextLoader#parse(java.lang.String)
	 */
	public ISimpleRichTextModel parse(final String text)
	{
		return new ISimpleRichTextModel()
		{
			DefaultLineTracker tl=new DefaultLineTracker();
			ITextDocument document =  (getSharedDocument()!=null?getSharedDocument(): new DocumentWrapper(new Document(text)));
			{
				tl.set(text);
			}
			public String getText()
			{
				return text;
			}
			
			public List<BasePartition> getPartitions()
			{
				
				List<BasePartition> partitions = new ArrayList<BasePartition>();
				for (int i = 0; i < tl.getNumberOfLines(); i++)
				{
					int lineStart;
					try {
						lineStart = tl.getLineOffset(i);
						int lineLength = tl.getLineLength(i);
						BasePartition partition = new BasePartition(document, lineStart, lineLength);
						partitions.add(partition);
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
					
				}
				return partitions;
				
			}
			
			public int getLineCount()
			{
				return tl.getNumberOfLines();
			}
			
			public int getIndent(int lineIndex)
			{
				return 0;
			}
			
			public BasicBullet getBullet(int lineIndex)
			{
				return null;
			}
			
			public int getAlign(int lineIndex)
			{
				return 0;
			}
		};
	}	

}
