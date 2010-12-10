package com.onpositive.richtexteditor.region_provider;

import com.onpositive.richtext.model.RegionPartition;
import com.onpositive.richtexteditor.model.AbstractLayerManager;
import com.onpositive.richtexteditor.model.resources.CompositeEditorWrapper;


public interface ILayerConnector {

	public CompositeEditorWrapper connect(AbstractLayerManager manager,RegionPartition partition);
}
