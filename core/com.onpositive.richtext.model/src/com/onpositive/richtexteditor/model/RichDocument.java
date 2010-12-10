package com.onpositive.richtexteditor.model;

import com.onpositive.richtext.model.meta.ITextDocument;


/**
 * Document implementation for RichTextViewer
 * @author 32kda
 *
 */
public interface RichDocument extends ITextDocument
{
	public ISimpleRichTextModel getModel();	

}
