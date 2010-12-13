package com.onpositive.richtexteditor.wikitext.ui;

import com.onpositive.richtexteditor.model.ISimpleRichTextModel;

/**
 * This class is used for processing specified model 
 * @author 32kda
 */
public interface IModelProcessor
{
	/**
	 * This method should do some processing to model specified
	 * This can be line alignment change, partition style change etc.
	 * @param model
	 */
	public void processModel(ISimpleRichTextModel model);
}
