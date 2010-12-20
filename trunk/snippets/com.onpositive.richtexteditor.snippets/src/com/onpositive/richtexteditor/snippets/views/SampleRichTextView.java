package com.onpositive.richtexteditor.snippets.views;


import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy;
import org.eclipse.ui.texteditor.spelling.SpellingService;

import com.onpositive.richtexteditor.snippets.SharedTextColors;
import com.onpositive.richtexteditor.snippets.SourceViewerDecorationSupport;
import com.onpositive.richtexteditor.snippets.SpellCheckConfigurator;
import com.onpositive.richtexteditor.viewer.RichTextViewer;
import com.onpositive.richtexteditor.viewer.undo.RichViewerUndoManager;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class SampleRichTextView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.onpositive.richtexteditor.snippets.views.SampleRichTextView";

	private RichTextViewer viewer;

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	 
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			return new String[] { "One", "Two", "Three" };
		}
	}
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().
					getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public SampleRichTextView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new RichTextViewer(parent, SWT.WRAP);
		//No we'll configure undo/redo & spellchecking. For spellchecking support, we need some Eclipse classes
		//some of them, like SourceViewerDecorationSupport, with little changes
		final DefaultMarkerAnnotationAccess defaultMarkerAnnotationAccess = new DefaultMarkerAnnotationAccess();
		final SharedTextColors sharedTextColors = new SharedTextColors();
		final SpellingService spellingService = new SpellingService(EditorsUI.getPreferenceStore());
		final SourceViewerDecorationSupport svs = new SourceViewerDecorationSupport(viewer, null, defaultMarkerAnnotationAccess, sharedTextColors);
		//We need to add AnnotationPrefernce for spellchecking
		svs.setAnnotationPreference(new AnnotationPreference("org.eclipse.ui.workbench.texteditor.spelling", "", AnnotationPreference.STYLE_SQUIGGLES, AnnotationPreference.STYLE_IBEAM, 0));
		//Let's crete a store for our preferences
		PreferenceStore store = new PreferenceStore();
		//Let's enable displaying for SQUIGGLES STYLE 
		store.setValue(AnnotationPreference.STYLE_SQUIGGLES, true);
		//Let'snstall our decoration support
		svs.install(store);
		//With the help of tis configuration we determine, which reconciler and UndoManager to use
		SpellCheckConfigurator.SpellingSourceViewerConfiguration configuration = new SpellCheckConfigurator.SpellingSourceViewerConfiguration(spellingService, viewer)
		{

			
			public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer)
			{
				return new DefaultAnnotationHover();
			}

			public IReconciler getReconciler(final ISourceViewer sourceViewer)
			{
				return new SpellingReconciler(sourceViewer)
				{
					final SpellingReconcileStrategy spellingReconcileStrategy = new SpellingReconcileStrategy(viewer, spellingService);
					{
						spellingReconcileStrategy.setDocument(sourceViewer.getDocument());
					}

					
					public IReconcilingStrategy getReconcilingStrategy(String contentType)
					{
						return spellingReconcileStrategy;
					}

				};
			}

			public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer)
			{
				return null;
			}

			
			public IUndoManager getUndoManager(ISourceViewer sourceViewer)
			{
				return new RichViewerUndoManager(100);
			}

		};
		viewer.configure(configuration);
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "com.onpositive.richtexteditor.snippets.viewer");
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}