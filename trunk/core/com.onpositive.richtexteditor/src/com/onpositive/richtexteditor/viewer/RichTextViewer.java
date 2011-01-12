/*******************************************************************************
 * Copyright (c) 2007, 2008 OnPositive Technologies (http://www.onpositive.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     OnPositive Technologies (http://www.onpositive.com/) - initial API and implementation
 *******************************************************************************/

package com.onpositive.richtexteditor.viewer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentAdapter;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.VerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.HTMLTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.IPartition;
import com.onpositive.richtext.model.IRichDocumentAutoStylingStrategy;
import com.onpositive.richtext.model.IRichDocumentListener;
import com.onpositive.richtext.model.RegionPartition;
import com.onpositive.richtexteditor.actions.ActionFactory;
import com.onpositive.richtexteditor.model.ISimpleRichTextModel;
import com.onpositive.richtexteditor.model.Logger;
import com.onpositive.richtexteditor.model.RichDocument;
import com.onpositive.richtexteditor.model.RichTextEditorConstants;
import com.onpositive.richtexteditor.model.resources.LayerManager;
import com.onpositive.richtexteditor.registry.InnerWidgetRegistry;
import com.onpositive.richtexteditor.util.DocumentWrapper;

/**
 * @author kor Source viewer subclass with some features like toolbar auto
 *         styling strategy ant etc.
 */
public class RichTextViewer extends SourceViewer {

	private static final int DEFAULT_WIDTH = 400;

	/** Default layout margin width. */
	private static final int DEFAULT_LAYOUT_MARGIN_WIDTH = 0;

	/** Default layout margin height. */
	private static final int DEFAULT_LAYOUT_MARGIN_HEIGHT = 2;

	protected IContributionManager toolbarManager;

	private MenuManager menuManager;// = new MenuManager();

	private ArrayList<IRichDocumentListener> listeners = new ArrayList<IRichDocumentListener>();

	private ArrayList<IRichDocumentAutoStylingStrategy> autoStyling = new ArrayList<IRichDocumentAutoStylingStrategy>();

	protected RichTextViewerControlConfiguration configuration;

	protected Composite viewerArea;
	protected LayerManager manager;
	protected ActionFactory factory;
	/**
	 * Should we use region controls or replace them with text (e.g., for preview)
	 */
	protected boolean useRegionControls = true;

	private boolean initToolbarManager = true; // Should we fill toolbar with

	
	

	// initial contents?

	// We need this, if we want to use external toolbar manager, for example,
	// from some configuration

	/**
	 * @return menu manager object
	 */
	public final MenuManager getMenuManager() {
		return menuManager;
	}

	public void setInput(Object input) {
		if (input instanceof ISimpleRichTextModel) {
			changeDocument((ISimpleRichTextModel) input);
		}
		if (input instanceof RichDocument) {
			changeDocument(((RichDocument) input).getModel());
		}
	}

	/**
	 * Simple constructor
	 * 
	 * @param parent
	 *            parent component
	 * @param style
	 *            style bitmask
	 */
	public RichTextViewer(Composite parent, int style) {
		this(parent, new VerticalRuler(16), style | SWT.WRAP | SWT.V_SCROLL
				| SWT.H_SCROLL);
	}

	/**
	 * Constructs a new rich text viewer. The vertical ruler is initially
	 * visible. The viewer has not yet been initialized with a source viewer
	 * configuration.
	 * 
	 * @param parent
	 *            the parent of the viewer's control
	 * @param ruler
	 *            the vertical ruler used by this source viewer
	 * @param style
	 *            the SWT style bits for the viewer's control
	 */
	public RichTextViewer(Composite parent, IVerticalRuler ruler, int style) {
		super(parent, ruler, style | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
	}

	/**
	 * Tells this viewer whether the registered auto edit strategies should be
	 * ignored.
	 * 
	 * @param ignore
	 *            <code>true</code> if the strategies should be ignored.
	 * @since 2.1
	 */
	public void ignoreAutoEditStrategies(boolean ignore) {
		super.ignoreAutoEditStrategies(ignore);
	}

	protected void createControl(Composite parent, int styles) {
		configuration = getConfiguration();
		viewerArea = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		adjustLayout(layout);
		viewerArea.setLayout(layout);

		createTopControl(styles);
		super.createControl(viewerArea, styles);
		Control control = super.getControl();
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.widthHint = DEFAULT_WIDTH;
		control.setLayoutData(layoutData);

		IDocument doc = new Document();
		setDocument(doc, new AnnotationModel());
		final StyledText textWidget = getTextWidget();

		textWidget.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				final InnerWidgetRegistry registry = InnerWidgetRegistry
						.getInstanceFor(textWidget);
				final Control[] children = textWidget.getChildren();
				final Point globalWidgetSize = textWidget.getSize();
				// textWidget.setRedraw(false);
				boolean updates = false;
				for (int i = 0; i < children.length; i++) {
					if (!registry.wasRedrawed(children[i])) {
						Control control2 = children[i];
						Point location = control2.getLocation();
						if (location.x != globalWidgetSize.x + 10
								|| location.y != globalWidgetSize.y + 10) {
							control2.setLocation(globalWidgetSize.x + 10,
									globalWidgetSize.y + 10);
							updates = true;
						}
					}
				}
				registry.clear();
				if (updates) {
					//textWidget.redraw();

				}
				// textWidget.setRedraw(true);
			}

		});
		textWidget.addListener(SWT.Resize, new Listener() {

			public void handleEvent(Event event) {
				final Control[] children = textWidget.getChildren();
				int width = textWidget.getClientArea().width;
				for (Control c : children) {
					if (c.getData() instanceof RegionPartition) {
						Point size = c.getSize();

						size.x = width - LayerManager.MARGIN * 2;
						c.setSize(size);
					}
				}
			}

		});
		hookListeners();
		
		postInit(doc, textWidget);
	}

	protected void postInit(IDocument doc, final StyledText textWidget) {
		manager = createLayerManager(textWidget, doc);
		initToolbarAndMenu(textWidget);
	}

	protected void adjustLayout(GridLayout layout) {
		layout.marginWidth = DEFAULT_LAYOUT_MARGIN_WIDTH;
		layout.marginHeight = DEFAULT_LAYOUT_MARGIN_HEIGHT;
	}

	protected void initToolbarAndMenu(final StyledText textWidget) {
		if ((textWidget.getStyle() & SWT.READ_ONLY) != 0) {
			return;
		}
		if (factory==null){
			factory = createActionFactory();
			manager.addRichDocumentListener(factory);
		}
		initActions();
		if (toolbarManager != null) {
			toolbarManager.update(true);
		}
		menuManager = new MenuManager();

		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				fillPopupMenu(manager);
				manager.update(true);
			}

		});
		if (factory!=null){
		fillPopupMenu(menuManager);
		menuManager.update(true);
		}
		Menu contextMenu = menuManager.createContextMenu(textWidget);
		getTextWidget().setMenu(contextMenu);
	}
	
	protected void setActionFactory(ActionFactory factory){
		if (factory!=null){
			manager.removeRichDocumentListener(factory);
		}
		this.factory=factory;
		initActions();
		if (toolbarManager != null) {
			toolbarManager.update(true);
		}
		if (factory!=null){
			if (menuManager!=null){
			fillPopupMenu(menuManager);
			menuManager.update(true);
			}
		}
	}

	protected void createTopControl(int styles) {
		if ((styles & SWT.READ_ONLY) != 0) {
			return;
		}
		if (configuration.isCreateToolbar()) {
			toolbarManager = createToolbarManager();
			if (needToolbarSeparatorLine()) {
				createLabel();
			}
		}
	}

	protected void createLabel() {
		Label la = new Label(viewerArea, SWT.SEPARATOR | SWT.HORIZONTAL);
		la.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	protected boolean needToolbarSeparatorLine() {
		return true;
	}

	protected LayerManager createLayerManager(StyledText textWidget,
			IDocument doc) {
		LayerManager layerManager = new LayerManager(textWidget, new DocumentWrapper(doc));
		layerManager.setUseRegionControls(useRegionControls);
		return layerManager;
	}

	protected void fillPopupMenu(IMenuManager menuManager) {
		if (factory == null) {
			throw new IllegalArgumentException(
					"Action factory is null, please create it before fill PopupMenu!");
		}

		ArrayList<IContributionItem> actionsList = factory.getActionsList();
		for (Iterator<IContributionItem> iterator = actionsList.iterator(); iterator
				.hasNext();) {
			IContributionItem item = (IContributionItem) iterator.next();
			if (item instanceof ActionContributionItem) {
				menuManager.add(((ActionContributionItem) item).getAction());
			}
		}
	}

	/**
	 * Creates a action factory.
	 * 
	 * @return the action factory.
	 */
	protected ActionFactory createActionFactory() {
		ActionFactory result = new ActionFactory(manager, this);
		return result;
	}

	protected void delayedConfigureActionFactory(ActionFactory factory) {
		factory.delayedConfigure(manager, this);
	}

	protected IContributionManager createToolbarManager() {
		ToolBar toolBar = new ToolBar(viewerArea, SWT.HORIZONTAL | SWT.FLAT);
		toolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return new ToolBarManager(toolBar);
	}

	/**
	 * @param model
	 */
	public void changeDocument(ISimpleRichTextModel model) {

		manager.getLayer().setFireEvents(false);
		try {
			manager.set(model);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		manager.getLayer().setFireEvents(true);
	}

	/**
	 * @return the initToolbarManager
	 */
	public boolean isInitToolbarManager() {
		return initToolbarManager;
	}

	/**
	 * @param initToolbarManager
	 *            the initToolbarManager to set
	 */
	public void setInitToolbarManager(boolean initToolbarManager) {
		this.initToolbarManager = initToolbarManager;
	}

	public String getContentsHTML() {
		if (manager != null)
			return manager.getContentsHTML();
		return "";
	}

	private void hookListeners() {
		addTextInputListener(new ITextInputListener() {
			public void inputDocumentAboutToBeChanged(IDocument oldInput,
					IDocument newInput) {
			}

			public void inputDocumentChanged(IDocument oldInput,
					IDocument newInput) {
				manager.dispose();
				if (newInput != null) {
					manager = new LayerManager(getTextWidget(), new DocumentWrapper(newInput));
					configureManager();
					factory.setManager(manager);
					manager.addRichDocumentListener(factory);
				}
			}
		});
		getTextWidget().addVerifyKeyListener(new VerifyKeyListener() {

			public void verifyKey(VerifyEvent e) {
				handleKey(e);
			}

		});
	}

	protected void initActions() {
		if (configuration.isCreateToolbar() && factory != null) {
			factory.fillToolbarManager(toolbarManager);
		}
	}

	protected void customizeDocumentCommand(DocumentCommand command) {
		IDocument document = getDocument();
		int offset = command.offset;
		int length = command.length;
		if (length == 0) {
			super.customizeDocumentCommand(command);
			return;
		}
		IPartition part1 = manager.getLayer().getPartitionAtOffset(offset);
		IPartition part2 = manager.getLayer().getPartitionAtOffset(
				offset + length - 1);
		if (part1 instanceof BasePartition
				&& ((BasePartition) part1).requiresFullDeletion()) {
			int diff = part1.getOffset() - command.offset;
			command.offset = part1.getOffset();
			command.length += diff;
		}
		if (part2 instanceof BasePartition
				&& ((BasePartition) part2).requiresFullDeletion())
			command.length = part2.getLength() + part2.getOffset()
					- command.offset;
		else {
			IPartition endPart = manager.getLayer().getPartitionAtOffset(
					offset + length);
			if (endPart instanceof BasePartition
					&& ((BasePartition) endPart).requiresSingleLine()
					&& endPart.getOffset() == command.offset + command.length) {
				try {
					String s = document.get(endPart.getOffset(), 1);
					if (!s.equals("\n") && !s.equals("\r"))
						command.length = endPart.getLength()
								+ endPart.getOffset() - command.offset;

				} catch (BadLocationException e) {
				}
			}
		}
		super.customizeDocumentCommand(command);
	}

	protected StyledText createTextWidget(Composite parent, int styles) {
		StyledText result = null;
		HashMap<String, String> extendedStyledTextSettings = new HashMap<String, String>();
		extendedStyledTextSettings.put(RichTextEditorConstants.RENDERER_CLASS,
				"org.eclipse.swt.custom.BasicRenderer");
		extendedStyledTextSettings.put(RichTextEditorConstants.LEFT_FIELD_SIZE,
				"0");
		extendedStyledTextSettings.put(
				RichTextEditorConstants.RIGHT_FIELD_SIZE, "0");
		try {
			Class<?> forName = Class
					.forName(RichTextEditorConstants.EXTENDED_STYLED_TEXT_CLASS);
			Constructor<?> constructor = forName.getConstructor(new Class[] {
					Composite.class, int.class, HashMap.class });
			result = (StyledText) constructor.newInstance(parent, styles,
					extendedStyledTextSettings);
		} catch (ClassNotFoundException e) {
			// extended class is not available, use default
			result = super.createTextWidget(parent, styles);
		} catch (SecurityException e) {
			// extended class is not available, use default
			result = super.createTextWidget(parent, styles);
		} catch (NoSuchMethodException e) {
			Logger.log(e);
		} catch (IllegalArgumentException e) {
			Logger.log(e);
		} catch (InstantiationException e) {
			Logger.log(e);
		} catch (IllegalAccessException e) {
			Logger.log(e);
		} catch (InvocationTargetException e) {
			Logger.log(e);
		}

		return result;
	}

	/**
	 * @see org.eclipse.jface.text.source.SourceViewer#getControl()
	 */
	public Control getControl() {
		return viewerArea;
	}

	/**
	 * @return Toolbar manager
	 */
	public IContributionManager getToolbarManager() {
		return toolbarManager;
	}

	/**
	 * @return LayerManager instance
	 */
	public LayerManager getLayerManager() {
		return manager;
	}

	// TODO Refactor it later
	protected void handleKey(VerifyEvent e) {
		System.out.println(e.keyCode);
		if ((e.keyCode == 'v' || e.keyCode == 'V')
				&& ((e.stateMask & SWT.MOD1) != 0)
				|| ((e.keyCode == SWT.INSERT) && (e.stateMask & SWT.MOD2) != 0)) {
			pasteOperation(e);
		} else if ((e.keyCode == SWT.DEL && (e.stateMask & SWT.MOD2) != 0)) {
			cutOperation();
		} else if ((e.keyCode == 'a' || e.keyCode == 'A')
				&& (((e.stateMask & SWT.MOD1) != 0))) {
			selectAllOperation(e);
		} else if ((e.keyCode == 'c' || e.keyCode == 'C')
				&& (((e.stateMask & SWT.MOD1) != 0))) {
			copyOperation(e);
		} else if ((e.keyCode == 'b' || e.keyCode == 'B')
				&& (((e.stateMask & SWT.MOD1) != 0))) {
			boldOperation(e);
		} else if ((e.keyCode == 'i' || e.keyCode == 'I')
				&& (((e.stateMask & SWT.MOD1) != 0))) {
			italicOperation(e);
		} else if ((e.keyCode == 'u' || e.keyCode == 'U')
				&& (((e.stateMask & SWT.MOD1) != 0))) {
			underlineOperation(e);
		} else if ((e.keyCode == 'z' || e.keyCode == 'Z')
				&& (((e.stateMask & SWT.MOD1) != 0))) {
			doOperation(TextViewer.UNDO);
		} else if ((e.keyCode == 'y' || e.keyCode == 'Y')
				&& (((e.stateMask & SWT.MOD1) != 0))) {
			doOperation(TextViewer.REDO);
		}
	}

	private void pasteOperation(VerifyEvent e) {
		if (paste())
			e.doit = false;
	}

	private void cutOperation() {
		cut();
	}

	public void cut() {
		copy();
		Point selectedRange = getSelectedRange();
		try {
			getDocument().replace(selectedRange.x, selectedRange.y, "");
		} catch (BadLocationException e1) {
			Logger.log(e1);
		}
	}

	private void selectAllOperation(VerifyEvent e) {
		manager.getActualTextWidget().setSelection(0,
				manager.getActualTextWidget().getCharCount());
		e.doit = false;
	}

	private void copyOperation(VerifyEvent e) {
		copy();
		e.doit = false;
	}

	public boolean paste() {
		Clipboard clipboard = new Clipboard(Display.getCurrent());
		HTMLTransfer htmlTransfer = HTMLTransfer.getInstance();
		TransferData[] availableTypes = clipboard.getAvailableTypes();
		for (TransferData d : availableTypes) {
			if (htmlTransfer.isSupportedType(d)) {
				String contents = (String) clipboard.getContents(htmlTransfer);
				Point selectedRange = getSelectedRange();
				manager.pasteHTML(contents,new com.onpositive.richtext.model.meta.Point(selectedRange.x,selectedRange.y));
				clipboard.dispose();
				return true;
			}
		}
		clipboard.dispose();
		return false;
	}

	public void copy() {
		Clipboard clipboard = new Clipboard(Display.getCurrent());
		HTMLTransfer instance = HTMLTransfer.getInstance();
		String selectedHTML = manager.getSelectedHTML();
		TextTransfer textTransfer = TextTransfer.getInstance();
		if (selectedHTML != null) {
			clipboard.setContents(new Object[] { selectedHTML,
					manager.getActualTextWidget().getSelectionText() },
					new Transfer[] { instance, textTransfer });
		}
	}

	private void boldOperation(VerifyEvent e) {
		IAction boldAction = factory.getBoldAction();
		if (boldAction!=null&&boldAction.isEnabled()){
		boolean checked = boldAction.isChecked();
		boldAction.setChecked(!checked);
		boldAction.run();
		boldAction.setChecked(!checked);
		if (toolbarManager != null) {
			toolbarManager.update(true);
		}
		
		}
		e.doit = false;
	}

	private void italicOperation(VerifyEvent e) {
		IAction italicAction = factory.getItalicAction();
		if (italicAction!=null&&italicAction.isEnabled()){
		boolean checked = italicAction.isChecked();
		italicAction.setChecked(!checked);
		italicAction.run();
		italicAction.setChecked(!checked);
		if (toolbarManager != null) {
			toolbarManager.update(true);
		}
		
		}
		e.doit = false;
	}

	private void underlineOperation(VerifyEvent e) {
		IAction underlineAction = factory.getUnderlineAction();
		if (underlineAction!=null&&underlineAction.isEnabled()){
		boolean checked = underlineAction.isChecked();
		underlineAction.setChecked(!checked);
		underlineAction.run();
		underlineAction.setChecked(!checked);
		if (toolbarManager != null) {
			toolbarManager.update(true);
		}
		
		}
		e.doit = false;
	}

	/**
	 * @param richDocumentListener
	 *            {@link IRichDocumentListener} to add
	 */
	public void addRichDocumentListener(
			IRichDocumentListener richDocumentListener) {
		listeners.add(richDocumentListener);
		getLayerManager().addRichDocumentListener(richDocumentListener);
	}

	/**
	 * @param strategy
	 *            {@link IRichDocumentAutoStylingStrategy} to remove
	 */
	public void removeRichDocumentAutoStylingStrategy(
			IRichDocumentAutoStylingStrategy strategy) {
		autoStyling.remove(strategy);
		getLayerManager().removeAutoStylingStrategy(strategy);
	}

	/**
	 * @param strategy
	 *            {@link IRichDocumentAutoStylingStrategy} to add
	 */
	public void addRichDocumentAutoStylingStrategy(
			IRichDocumentAutoStylingStrategy strategy) {
		autoStyling.add(strategy);
		getLayerManager().addAutoStylingStrategy(strategy);
	}

	/**
	 * @param richDocumentListener
	 *            {@link IRichDocumentListener} to remove
	 */
	public void removeRichDocumentListener(
			IRichDocumentListener richDocumentListener) {
		listeners.remove(richDocumentListener);
		getLayerManager().removeRichDocumentListener(richDocumentListener);
	}

	protected void configureManager() {
		for (IRichDocumentListener l : listeners) {
			manager.addRichDocumentListener(l);
		}
		for (IRichDocumentAutoStylingStrategy l : autoStyling) {
			manager.addAutoStylingStrategy(l);
		}
		configuration.configureLayerManager(manager);
	}

	/**
	 * @return the autoStylingStrtegies
	 */
	public ArrayList<IRichDocumentAutoStylingStrategy> getAutoStylingStrategies() {
		return autoStyling;
	}

	/**
	 * @param autoStyling
	 *            the autoStylingStrtegies to set
	 */
	public void setAutoStylingStrategies(
			ArrayList<IRichDocumentAutoStylingStrategy> autoStyling) {
		this.autoStyling = autoStyling;
	}

	/**
	 * @param toolbarManager
	 *            the toolbarManager to set
	 */
	public void setToolbarManager(IContributionManager toolbarManager) {
		this.toolbarManager = toolbarManager;
	}

	public RichTextViewerControlConfiguration getConfiguration() {
		if (configuration == null)
			configuration = new RichTextViewerControlConfiguration();
		return configuration;
	}

	
	protected IDocumentAdapter createDocumentAdapter() {
		// TODO Auto-generated method stub
		return super.createDocumentAdapter();
	}

	/**
	 * Sets this viewer's visible document. The visible document represents the
	 * visible region of the viewer's input document.
	 * 
	 * @param document
	 *            the visible document
	 */
	protected void setVisibleDocument(IDocument document) {
		if (document != null)
			document.addPrenotifiedDocumentListener(new IDocumentListener() { // We
																				// need
																				// to
																				// do
																				// this,
																				// because
																				// we
																				// need
																				// to
																				// add
																				// this
																				// listener
																				// before
																				// inner
																				// document
																				// listener

						public void documentChanged(DocumentEvent event) {

						}

						public void documentAboutToBeChanged(DocumentEvent event) {
							manager.getLayer().createLineAttributesUndoChange(
									event.fOffset, event.fLength,
									event.fText.length()); // Because at this
															// moment all
															// bullets at
															// deleted lines is
															// still "alive"
						}
					});
		super.setVisibleDocument(document);
	}
	
	public void setUseRegionControls(boolean use)
	{
		useRegionControls = use;	
		manager.setUseRegionControls(use);
	}

	/**
	 * @return the useRegionControls
	 */
	public boolean isUseRegionControls()
	{
		return useRegionControls;
	}

}
