package com.onpositive.richtexteditor.xml;

import java.util.ArrayList;

import org.eclipse.jface.text.source.SourceViewerConfiguration;

import com.onpositive.richtext.model.IRichDocumentAutoStylingStrategy;
import com.onpositive.richtexteditor.viewer.RichTextViewer;
import com.onpositive.semantic.model.ui.property.editors.IViewerConfigurator;
import com.onpositive.semantic.model.ui.property.editors.IViewerTextElement;


public class RichtextConfigurator implements IViewerConfigurator
{
	SourceViewerConfiguration configuration = null;
	protected ArrayList<IRichDocumentAutoStylingStrategy> autoStylingStrategies = new ArrayList<IRichDocumentAutoStylingStrategy>();
	
	
	/**
	 * @return the autoStylingStrtegies
	 */
	public ArrayList<IRichDocumentAutoStylingStrategy> getAutoStylingStrtegies()
	{
		return autoStylingStrategies;
	}

	
	/**
	 * @param autoStylingStrtegies the autoStylingStrtegies to set
	 */
	public void setAutoStylingStrtegies(
			ArrayList<IRichDocumentAutoStylingStrategy> autoStylingStrtegies)
	{
		this.autoStylingStrategies = autoStylingStrtegies;
	}
	
	/**
	 * @param strategy {@link IRichDocumentAutoStylingStrategy} to add
	 */
	public void addRichDocumentAutoStylingStrategy(
			IRichDocumentAutoStylingStrategy strategy) {
		autoStylingStrategies.add(strategy);
	}

	/**
	 * @param strategy {@link IRichDocumentAutoStylingStrategy} to remove
	 */
	public void removeRichDocumentAutoStylingStrategy(
			IRichDocumentAutoStylingStrategy strategy) {
		autoStylingStrategies.remove(strategy);
	}

	public RichtextConfigurator()
	{
		// TODO Auto-generated constructor stub
	}
	
	public RichtextConfigurator(SourceViewerConfiguration configuration)
	{
		this.configuration = configuration;		
	}
	
	
	
	public void configure(IViewerTextElement element)
	{
		if (!(element.getSourceViewer() instanceof RichTextViewer)) throw new IllegalArgumentException("element should be RichTextViewer instance!");
		RichTextViewer viewer = (RichTextViewer) element.getSourceViewer();
		if (configuration != null) viewer.configure(configuration);
		if (autoStylingStrategies.size() > 0) viewer.getAutoStylingStrategies().addAll(autoStylingStrategies);
		
	}

}
