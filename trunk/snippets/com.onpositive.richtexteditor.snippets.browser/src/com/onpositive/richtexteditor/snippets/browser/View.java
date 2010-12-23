package com.onpositive.richtexteditor.snippets.browser;

import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import com.onpositive.richtext.model.IRichDocumentListener;
import com.onpositive.richtext.model.RichDocumentChange;
import com.onpositive.richtext.model.meta.DocumentEvent;
import com.onpositive.richtexteditor.viewer.RichTextViewer;
import com.onpositive.richtexteditor.viewer.undo.RichViewerUndoManager;

public class View extends ViewPart {
	private final class BrowserUpdater implements Runnable
	{
		private final Browser browser;

		private BrowserUpdater(Browser browser)
		{
			this.browser = browser;
		}

		@Override
		public void run()
		{
			long current = System.currentTimeMillis();
			if (current - lastChangeTime < 1000)
				Display.getDefault().timerExec(MIN_UPDATE_INTERVAL, this);
			else
			{
				String s = richTextViewer.getLayerManager().getSerializedString();
				browser.setText(s);
				updateScheduled = false;
				lastUpdateTime = current;
			}
			
		}
	}

	private static final int UNDO_CACHE_SIZE = 100;
	public static final String ID = "com.onpositive.richtexteditor.snippets.browser.view";
	private static final int MIN_UPDATE_INTERVAL = 2000; //2 sec

	private RichTextViewer richTextViewer;
	private long lastUpdateTime = -1;
	private long lastChangeTime = -1;
	private Boolean updateScheduled = false;
	


	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		
		parent.setLayout(new GridLayout(2,true));
		richTextViewer = new RichTextViewer(parent, SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 1;
		gridData.minimumWidth = 200;
		gridData.minimumHeight = 400;
		richTextViewer.getControl().setLayoutData(gridData);		
		RichViewerUndoManager undoManager = new RichViewerUndoManager(UNDO_CACHE_SIZE);
		undoManager.connect(richTextViewer);
		richTextViewer.setUndoManager(undoManager);
		final Browser browser = new Browser(parent, SWT.BORDER );
		GridData gridData1 = new GridData(GridData.FILL_BOTH);
		gridData1.horizontalSpan = 1;
		gridData1.minimumWidth = 200;
		gridData1.minimumHeight = 400;
		browser.setLayoutData(gridData1);		
		registerHandlers(); 
		richTextViewer.addRichDocumentListener(new IRichDocumentListener(){

			public void documentAboutToBeChanged(DocumentEvent event) {				
			}

			public void documentChanged(DocumentEvent event,
					RichDocumentChange change) {
				lastChangeTime = System.currentTimeMillis();
				synchronized (updateScheduled)
				{
					if (!updateScheduled)
					{
						if (lastUpdateTime == -1 || lastChangeTime - lastUpdateTime < MIN_UPDATE_INTERVAL)
						{
							String s =richTextViewer.getLayerManager().getSerializedString();
							browser.setText(s);
							lastUpdateTime = lastChangeTime;
						}
						else
						{
							Display.getDefault().timerExec(MIN_UPDATE_INTERVAL, new BrowserUpdater(browser));
							updateScheduled = true;
						}
					}
				}
			}
		});		
	}

	protected void registerHandlers()
	{
		Action undoAction = new Action()
		{
			@Override
			public void run()
			{
				richTextViewer.doOperation(TextViewer.UNDO);
			}
		};
		undoAction.setId("com.onpositive.richtexteditor.snippets.browser.undo");
		undoAction.setActionDefinitionId("com.onpositive.richtexteditor.snippets.browser.undo");
		ActionHandler undoHandler = new ActionHandler(undoAction);
		((IHandlerService)getSite().getService(IHandlerService.class)).
		activateHandler("com.onpositive.richtexteditor.snippets.browser.undo", (IHandler)undoHandler);
		Action redoAction = new Action()
		{
			@Override
			public void run()
			{
				richTextViewer.doOperation(TextViewer.REDO);
			}
		};
		redoAction.setId("com.onpositive.richtexteditor.snippets.browser.redo");
		redoAction.setActionDefinitionId("com.onpositive.richtexteditor.snippets.browser.redo");
		ActionHandler redoHandler = new ActionHandler(redoAction);
		((IHandlerService)getSite().getService(IHandlerService.class)).
		activateHandler("com.onpositive.richtexteditor.snippets.browser.redo", (IHandler)redoHandler);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		richTextViewer.getControl().setFocus();
	}
}