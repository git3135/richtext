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

package com.onpositive.richtexteditor.snippets;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.onpositive.richtext.model.FontStyle;
import com.onpositive.richtext.model.IRichDocumentAutoStylingStrategy;
import com.onpositive.richtext.model.PartitionDelta;
import com.onpositive.richtext.model.meta.DocumentEvent;
import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtext.model.meta.RGB;
import com.onpositive.richtexteditor.model.Logger;
import com.onpositive.richtexteditor.model.changes.CompositeChange;
import com.onpositive.richtexteditor.model.resources.LayerManager;
import com.onpositive.richtexteditor.viewer.RichTextViewer;
import com.onpositive.richtexteditor.viewer.RichTextViewerControlConfiguration;
import com.onpositive.richtexteditor.viewer.undo.RichViewerUndoManager;

/**
 * @author kor
 *
 */
public class StylingSample {

	private final static class NumberStylingStrategy implements
			IRichDocumentAutoStylingStrategy {
		FontStyle numberStyle = new FontStyle(FontStyle.BOLD, new RGB(0, 100, 200));

		public void customizeStyleChanges(DocumentEvent event,
				CompositeChange change,PartitionDelta existingDelta) {
			if (event.fLength == 0
					&& event.fText.trim().length() == 0) {
				int offset = event.getOffset()
						- event.fText.length();
				boolean hasDigits = false;
				int a = offset;
				for (; a > 0; a--) {
					ITextDocument document = event.getDocument();
					char c = document.getChar(a);
					if (Character.isDigit(c)) {
						hasDigits = true;
					} else {
						if (Character.isWhitespace(c)) {
							break;
						} else {
							hasDigits = false;
						}
					}
				}
				if (hasDigits) {
					change.applyStyleTo(a, event.getOffset()-a, numberStyle);						
				}
			}
		}
	}

	protected static StylingSample instance;

	/**
	 * @param args arguments
	 */
	public static void main(String[] args) {
		instance = new StylingSample();
		instance.run();
	}

	void run() {
		// create the widget's shell
		Shell shell = new Shell();
		shell.setLayout(new GridLayout(1, true));
		RichTextViewer richTextViewer = new RichTextViewer(shell, SWT.NONE){

			
			public RichTextViewerControlConfiguration getConfiguration() {
				RichTextViewerControlConfiguration config = new RichTextViewerControlConfiguration(){

					
					protected void configureLayerManager(LayerManager manager) {
						manager.addAutoStylingStrategy(new NumberStylingStrategy());
						super.configureLayerManager(manager);
					}
					
				};
				return config;				
			}
			
			
		};
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.minimumHeight = 300;
		richTextViewer.getControl().setLayoutData(layoutData);
		Display display = shell.getDisplay();
		
		RichViewerUndoManager undoManager = new RichViewerUndoManager(40);
		undoManager.connect(richTextViewer);
		richTextViewer.setUndoManager(undoManager);
		shell.pack();
		shell.open();
		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
	}

}
