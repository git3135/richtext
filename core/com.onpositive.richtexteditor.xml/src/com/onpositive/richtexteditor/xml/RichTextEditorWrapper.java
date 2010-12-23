package com.onpositive.richtexteditor.xml;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedStyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.onpositive.commons.elements.AbstractUIElement;
import com.onpositive.commons.elements.IProvidesToolbarManager;
import com.onpositive.richtext.model.IRichDocumentListener;
import com.onpositive.richtext.model.RichDocumentChange;
import com.onpositive.richtext.model.meta.DocumentEvent;
import com.onpositive.richtexteditor.actions.ActionFactory;
import com.onpositive.richtexteditor.io.IFormatFactory;
import com.onpositive.richtexteditor.io.ITextLoader;
import com.onpositive.richtexteditor.io.TextSerializer;
import com.onpositive.richtexteditor.model.ISimpleRichTextModel;
import com.onpositive.richtexteditor.model.resources.LayerManager;
import com.onpositive.richtexteditor.viewer.DefaultFormatFactory;
import com.onpositive.richtexteditor.viewer.RichTextViewer;
import com.onpositive.richtexteditor.viewer.RichTextViewerControlConfiguration;
import com.onpositive.richtexteditor.viewer.undo.RichViewerUndoManager;
import com.onpositive.semantic.model.binding.IBinding;
import com.onpositive.semantic.model.realm.ISetDelta;
import com.onpositive.semantic.model.ui.property.editors.AbstractEditor;
import com.onpositive.semantic.model.ui.property.editors.IViewerTextElement;

public class RichTextEditorWrapper extends AbstractEditor<Control> implements
		IViewerTextElement {

	protected RichTextViewer richTextViewer = null;
	protected ActionFactory customActionFactory = null;
	protected TextSerializer customSerializer;
	private boolean isCreateToolbar = true;

	public static abstract class ActionFactoryCreator {

		public abstract ActionFactory getActionFactory(LayerManager manager,
				ISourceViewer viewer);
	}

	protected ActionFactoryCreator actionFactoryCreator;
	protected IFormatFactory formatFactory = null;
	protected RichtextConfigurator configurator = null;

	/**
	 * @return the actionFactorycreator
	 */
	public ActionFactoryCreator getActionFactorycreator() {
		return actionFactoryCreator;
	}

	/**
	 * @param actionFactorycreator
	 *            the actionFactorycreator to set
	 */
	public void setActionFactorycreator(
			ActionFactoryCreator actionFactorycreator) {
		this.actionFactoryCreator = actionFactorycreator;
	}

	protected void internalSetBinding(IBinding binding) {
		setValue(binding, binding.getValue());
	}

	protected void processValueChange(ISetDelta valueElements) {
		if (!valueElements.getAddedElements().isEmpty()) {
			this.setValue(this.getBinding(), valueElements.getAddedElements()
					.iterator().next());
		} else {
			if (!valueElements.getChangedElements().isEmpty()) {
				this.setValue(this.getBinding(), valueElements
						.getChangedElements().iterator().next());
			} else {
				if (!valueElements.getRemovedElements().isEmpty()) {
					this.setValue(this.getBinding(), null);
				}
			}
		}
	}

	protected void setValue(IBinding binding, Object value) {

		if (value == null) {
			value = "";
		}
		setIgnoreChanges(true);
		try {
			if (isCreated()) {
				if (value instanceof String) {

					ISimpleRichTextModel model = getTextLoader().parse(
							(String) value);
					richTextViewer.changeDocument(model);
				}
			}
		} finally {
			setIgnoreChanges(false);
		}
	}

	protected ITextLoader getTextLoader() {
		return getFormatFactory().getLoader(richTextViewer.getLayerManager());
	}

	protected IFormatFactory getFormatFactory() {
		if (formatFactory == null)
			formatFactory = new DefaultFormatFactory();
		return formatFactory;
	}

	public boolean isBordered() {
		return true;
	}

	protected Control createControl(Composite conComposite) {
		richTextViewer = new RichTextViewer(conComposite, SWT.NONE) {

			public RichTextViewerControlConfiguration getConfiguration() {
				RichTextViewerControlConfiguration conf = new RichTextViewerControlConfiguration() {

					protected void configureLayerManager(LayerManager manager) {
						super.configureLayerManager(manager);
					}
				};
				conf.setCreateToolbar(isCreateToolbar);
				return conf;
			}

			/**
			 * (non-Javadoc)
			 * 
			 * @see com.onpositive.richtexteditor.viewer.RichTextViewer#createActionFactory()
			 */

			protected ActionFactory createActionFactory() {
				if (actionFactoryCreator != null) {
					ActionFactory actionFactory2 = actionFactoryCreator.getActionFactory(
							getLayerManager(), this);
					return actionFactory2;
				}
				return super.createActionFactory();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.onpositive.richtexteditor.viewer.RichTextViewer#initActions()
			 */

			protected void initActions() {

				if (getConfiguration().isCreateToolbar())
					super.initActions();
				else {
					IProvidesToolbarManager toolbarProvider = (IProvidesToolbarManager) getService(IProvidesToolbarManager.class);
					if (toolbarProvider != null) {
						List<IContributionItem> itemsList = factory
								.createActionsList();
						for (Iterator<IContributionItem> iterator = itemsList.iterator(); iterator
								.hasNext();) {
							IContributionItem contributionItem = (IContributionItem) iterator
									.next();
							toolbarProvider.addToToolbar(contributionItem);
						}
					}

				}

			}

		};

		// richTextViewer.getConfiguration().setCreateToolbar(isCreateToolbar);
		
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.minimumHeight = 300;
		richTextViewer.getControl().setLayoutData(layoutData);
		((ExtendedStyledText)richTextViewer.getTextWidget()).setMargins(0, 0, 0, 0);
		RichViewerUndoManager undoManager = new RichViewerUndoManager(40);
		undoManager.connect(richTextViewer);
		richTextViewer.setUndoManager(undoManager);
		richTextViewer.addRichDocumentListener(new IRichDocumentListener() {

			public void documentAboutToBeChanged(DocumentEvent event) {
				// TODO Auto-generated method stub

			}

			public void documentChanged(DocumentEvent event,
					RichDocumentChange change) {
				if (!shouldIgnoreChanges()) {
					try{
					commitToBinding(getCurrentSerializer().serializeAllToStr());
					}catch (Exception e){
						e.printStackTrace();
					}
					
				}
			}

		});
		if (configurator != null)
			configurator.configure(this); // Because we need richTextViewer to
											// be created when wrapper being
											// configured
		IBinding binding = getBinding();
		if (binding != null) {
			Object value=binding.getValue();
			if (value == null) {
				value = "";
			}
			setIgnoreChanges(true);
			try {
				if (value instanceof String) {

					ISimpleRichTextModel model = getTextLoader().parse(
							(String) value);
					richTextViewer.changeDocument(model);
				}

			} finally {
				setIgnoreChanges(false);
			}
		}
		return richTextViewer.getControl();
	}

	public boolean needsLabel() {
		return false;
	}

	public AbstractUIElement<?> getElement() {
		return this;
	}

	/**
	 * @return the richTextViewer
	 */

	public SourceViewer getSourceViewer() {
		return richTextViewer;
	}

	public void setInitToolbarManager(boolean init) {
		richTextViewer.setInitToolbarManager(init);
	}

	protected TextSerializer getCurrentSerializer() {
		return getFormatFactory().getSerializer(
				richTextViewer.getLayerManager());
	}

	public void setFormatFactory(IFormatFactory factory) {
		formatFactory = factory;
	}

	public void setConfigurator(RichtextConfigurator configurator) {
		this.configurator = configurator;

	}

	public void setCreateToolbar(boolean isCreateToolbar) {
		this.isCreateToolbar = isCreateToolbar;
	}

	public boolean isCreateToolbar() {
		return isCreateToolbar;
	}

}
