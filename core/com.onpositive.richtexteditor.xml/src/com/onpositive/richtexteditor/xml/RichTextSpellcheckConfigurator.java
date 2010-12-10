package com.onpositive.richtexteditor.xml;

import com.onpositive.richtexteditor.viewer.RichTextViewer;
import com.onpositive.semantic.model.ui.property.editors.IViewerTextElement;
import com.onpositive.semantic.ui.text.SpellCheckConfigurator;


public class RichTextSpellcheckConfigurator extends RichtextConfigurator
{
	
	public void configure(IViewerTextElement element)
	{
		super.configure(element);
		RichTextViewer viewer = (RichTextViewer) element.getSourceViewer(); 
		SpellCheckConfigurator configurator = new SpellCheckConfigurator();
		configurator.configure(viewer);
	}
}
