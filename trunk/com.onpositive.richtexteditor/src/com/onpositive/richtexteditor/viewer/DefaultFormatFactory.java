package com.onpositive.richtexteditor.viewer;

import com.onpositive.richtexteditor.io.IFormatFactory;
import com.onpositive.richtexteditor.io.ITextLoader;
import com.onpositive.richtexteditor.io.TextSerializer;
import com.onpositive.richtexteditor.io.html.DefaultHTMLLoader;
import com.onpositive.richtexteditor.io.html.HTMLSerializer;
import com.onpositive.richtexteditor.model.AbstractLayerManager;
import com.onpositive.richtexteditor.model.resources.LayerManager;

/**
 * @author kor
 *
 */
public class DefaultFormatFactory implements IFormatFactory {
	
	protected ITextLoader loader = null;
	protected TextSerializer serializer = null;
	protected LayerManager lastLoaderManager = null, lastSerializerManager = null;

	/**
	 * @see com.onpositive.richtexteditor.io.IFormatFactory#getLoader(com.onpositive.richtexteditor.model.AbstractLayerManager)
	 */
	public ITextLoader getLoader(AbstractLayerManager manager) {
		if (loader == null || lastLoaderManager != manager) {
			loader = new DefaultHTMLLoader(manager);
			lastLoaderManager = (LayerManager) manager;
		}
		return loader;
	}

	/**
	 * @see com.onpositive.richtexteditor.io.IFormatFactory#getSerializer(com.onpositive.richtexteditor.model.AbstractLayerManager)
	 */
	public TextSerializer getSerializer(AbstractLayerManager manager) {
		if (serializer == null || lastSerializerManager != manager) {
			serializer = new HTMLSerializer(manager,
					new StyledTextLineInformationProvider(
							(LayerManager) manager));
			lastSerializerManager = (LayerManager) manager;
		}
		return serializer;
	}

}
