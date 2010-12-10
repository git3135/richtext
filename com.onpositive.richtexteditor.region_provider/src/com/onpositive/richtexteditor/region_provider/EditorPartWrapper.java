package com.onpositive.richtexteditor.region_provider;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.EditorPart;

import com.onpositive.richtext.model.IHasText;
import com.onpositive.richtext.model.RegionCompositeEvent;
import com.onpositive.richtexteditor.model.resources.CompositeEditorWrapper;

public class EditorPartWrapper extends CompositeEditorWrapper {

	protected IWorkbenchPart editorPart;
	
	public EditorPartWrapper(Composite widget, final IWorkbenchPart editorPart) {
		super(widget);
		this.editorPart = editorPart;
		editorPart.addPropertyListener(new IPropertyListener(){
		
			
			public void propertyChanged(Object source, int propId)
			{
				if (propId == EditorPart.PROP_DIRTY)
				{
					if (editorPart instanceof EditorPart && !((EditorPart) editorPart).isDirty())
						notifyListeners(new RegionCompositeEvent(EditorPartWrapper.this, RegionCompositeEvent.REMOVE_DIRTY));
					else
						notifyListeners(new RegionCompositeEvent(EditorPartWrapper.this, RegionCompositeEvent.SET_DIRTY));
				}
			}
		});
	}
	
	public void dispose() {
		editorPart.dispose();
		//super.dispose();
	}

	public String getContent() {
		if (editorPart instanceof IHasText){
			IHasText t=(IHasText) editorPart;
			return t.getText();
		}
		return super.getContent();
	}

	public void setText(String initialText) {
		if (editorPart instanceof IHasText){
			IHasText t=(IHasText) editorPart;
			t.setText(initialText);
		}
		else{
		super.setText(initialText);
		}
	}

	public int getInitialHeight() {
		if (editorPart instanceof IHasText){
			IHasText t=(IHasText) editorPart;
			return t.getInitialHeight();
		}
		return super.getInitialHeight();
	}

}
