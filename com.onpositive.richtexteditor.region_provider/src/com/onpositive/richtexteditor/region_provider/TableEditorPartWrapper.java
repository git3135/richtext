package com.onpositive.richtexteditor.region_provider;

import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;

import com.onpositive.richtext.model.IHasColumns;
import com.onpositive.richtext.model.RegionPartition;

public class TableEditorPartWrapper extends EditorPartWrapper implements IHasColumns{

	int hWidth;
	int hHeight;

	int rWidth = 0;
	int rHeight = 0;

	public TableEditorPartWrapper(Composite widget, IWorkbenchPart editorPart) {
		super(widget, editorPart);
	}

	
	public com.onpositive.richtext.model.meta.Point getSize() {
		return new com.onpositive.richtext.model.meta.Point(hWidth, hHeight);
	}

	
	public void setSize(int width, int height) {
		hWidth = width;
		hHeight = height;
		
		/*
		 * if(rWidth <= 0){ rWidth = width; }
		 * 
		 * if(rHeight <= 0){ rHeight = height; }
		 */

		System.out.println("||" + rWidth + " " +rHeight);
		if (rWidth < width) {
			super.setSize(rWidth, rHeight);
		} else {
			if (width >= 0) {
				super.setSize(width, rHeight);
			}
		}
		

	}

	public void updateSize(int width, int height) {
		rWidth = width;
		rHeight = height;
	}


	public void updateSizeOfColumns(int parentWidth)
	{
		if (editorPart instanceof IHasColumns)
			((IHasColumns)editorPart).updateSizeOfColumns(parentWidth);		
	}
}
