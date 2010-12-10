package com.onpositive.richtexteditor.model;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.BasePartitionLayer;
import com.onpositive.richtext.model.IImageManager;
import com.onpositive.richtext.model.ILineAttributeModel;
import com.onpositive.richtext.model.IRegionCompositeWrapper;
import com.onpositive.richtext.model.IRegionCompositeWrapperListener;
import com.onpositive.richtext.model.IRichDocumentAutoStylingStrategy;
import com.onpositive.richtext.model.PartitionDelta;
import com.onpositive.richtext.model.RegionPartition;
import com.onpositive.richtext.model.meta.DocumentEvent;
import com.onpositive.richtext.model.meta.IStyledText;
import com.onpositive.richtext.model.meta.ITextDocument;

public abstract class AbstractLayerManager {

	public static final int MAX_PARTITION_SIZE = 300; // While paste
	public static final int PARTITION_DIVIDING_THRESHOLD = 400; // While normal
	/**
	 * Should we paste special region controls or use some textual represenatation for regions
	 * (e.g. in quick views)?
	 */
	protected boolean useRegionControls = true;

	public abstract void fireRichDocumentEvent(DocumentEvent object, PartitionDelta apply2);

	public abstract int getCaretOffset() ;

	public abstract IStyledText getTextWidget() ;
	
	public abstract void fireDocumentGoingToChange(DocumentEvent event);

	public abstract void setRefreshVisibleState(boolean b);

	public abstract IRichDocumentAutoStylingStrategy[] getAutoStylingStrategies();

	public abstract ITextDocument getDocument();

	public abstract IFontStyleManager getFontStyleManager();

	public abstract IImageManager getImageManager();

	public abstract void layerChanged(LayerEvent layerEvent);

	public abstract void syncExec(Runnable runnable);

	public abstract IRegionCompositeWrapper getWrapperForContentType(String contentType, AbstractLayerManager manager,RegionPartition partition);

	public abstract IRegionCompositeWrapperListener createRegionCompositeWrapperListener(RegionPartition regionPartition);

	public abstract BasePartitionLayer getLayer();

	public abstract BasePartition getLinkPrototype();

	public abstract ILineAttributeModel createModel();

	public abstract int getLineDelimiterLength();

	/**
	 * @return should we paste special region controls or use some textual represenatation for regions
	 * (e.g. in quick views)?
	 */
	public boolean isUseRegionControls()
	{
		return useRegionControls;
	}

	/**
	 * @param useRegionControls Should we paste special region controls or use some textual represenatation for regions
	 * (e.g. in quick views)?
	 * <code>true</code>, if we should use such controls, <code>false</code> otherwise 
	 */
	public void setUseRegionControls(boolean useRegionControls)
	{
		this.useRegionControls = useRegionControls;
	}
}
