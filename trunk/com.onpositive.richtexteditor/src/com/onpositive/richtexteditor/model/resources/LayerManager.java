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

package com.onpositive.richtexteditor.model.resources;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.text.Document;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.onpositive.richtext.model.AbstractModel;
import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.BasePartitionLayer;
import com.onpositive.richtext.model.FontStyle;
import com.onpositive.richtext.model.HRPartition;
import com.onpositive.richtext.model.IHandlesSelection;
import com.onpositive.richtext.model.IHasColumns;
import com.onpositive.richtext.model.IImageManager;
import com.onpositive.richtext.model.ILineAttributeModel;
import com.onpositive.richtext.model.ILineAttributeModelExtension;
import com.onpositive.richtext.model.IPartition;
import com.onpositive.richtext.model.IRegionCompositeWrapper;
import com.onpositive.richtext.model.IRegionCompositeWrapperListener;
import com.onpositive.richtext.model.IRichDocumentAutoStylingStrategy;
import com.onpositive.richtext.model.IRichDocumentListener;
import com.onpositive.richtext.model.ImagePartition;
import com.onpositive.richtext.model.LinkPartition;
import com.onpositive.richtext.model.ObjectPartition;
import com.onpositive.richtext.model.PartitionDelta;
import com.onpositive.richtext.model.PartitionFactory;
import com.onpositive.richtext.model.PartitionStorage;
import com.onpositive.richtext.model.RegionCompositeEvent;
import com.onpositive.richtext.model.RegionPartition;
import com.onpositive.richtext.model.RichDocumentChange;
import com.onpositive.richtext.model.meta.BasicBullet;
import com.onpositive.richtext.model.meta.BulletFactory;
import com.onpositive.richtext.model.meta.DocumentEvent;
import com.onpositive.richtext.model.meta.IImage;
import com.onpositive.richtext.model.meta.IStyledText;
import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtext.model.meta.Point;
import com.onpositive.richtext.model.meta.RGB;
import com.onpositive.richtext.model.meta.Rectangle;
import com.onpositive.richtext.model.meta.StyleRange;
import com.onpositive.richtexteditor.io.TextSerializer;
import com.onpositive.richtexteditor.io.html.DefaultHTMLLoader;
import com.onpositive.richtexteditor.io.html.HTMLSerializer;
import com.onpositive.richtexteditor.io.html.IHTMLLoader;
import com.onpositive.richtexteditor.io.html.IHTMLLoaderFactory;
import com.onpositive.richtexteditor.io.html.IHTMLSerializerFactory;
import com.onpositive.richtexteditor.model.AbstractLayerManager;
import com.onpositive.richtexteditor.model.FontStylesChangeListener;
import com.onpositive.richtexteditor.model.HyperlinksDecorator;
import com.onpositive.richtexteditor.model.IFontStyleManager;
import com.onpositive.richtexteditor.model.IPartitionListener;
import com.onpositive.richtexteditor.model.IRichtextDecorator;
import com.onpositive.richtexteditor.model.ISimpleRichTextModel;
import com.onpositive.richtexteditor.model.LayerEvent;
import com.onpositive.richtexteditor.model.Logger;
import com.onpositive.richtexteditor.model.PartitionEvent;
import com.onpositive.richtexteditor.model.RichDocument;
import com.onpositive.richtexteditor.model.RichSelectionState;
import com.onpositive.richtexteditor.model.RichTextEditorConstants;
import com.onpositive.richtexteditor.model.RichtextCommandCreator;
import com.onpositive.richtexteditor.model.changes.Change;
import com.onpositive.richtexteditor.model.changes.SetAlignChange;
import com.onpositive.richtexteditor.model.changes.SetBulletChange;
import com.onpositive.richtexteditor.model.changes.SetFirstLineIndentChange;
import com.onpositive.richtexteditor.model.changes.SetIndentChange;
import com.onpositive.richtexteditor.model.changes.SetLineSpacingChange;
import com.onpositive.richtexteditor.model.changes.SetRightIndentChange;
import com.onpositive.richtexteditor.model.changes.WorldChange;
import com.onpositive.richtexteditor.registry.InnerWidgetRegistry;
import com.onpositive.richtexteditor.util.MetaConverter;
import com.onpositive.richtexteditor.util.RomanConverter;
import com.onpositive.richtexteditor.viewer.StyledTextLineAttributeModel;
import com.onpositive.richtexteditor.viewer.StyledTextLineInformationProvider;

/**
 * @author 32kda This class contains different utility methods and encapsulates
 *         common system interaction
 */
public class LayerManager extends AbstractLayerManager implements
		IPartitionListener, FontStylesChangeListener, PaintObjectListener {


	/**
	 * @author kor Incapsulates change of whole content of model
	 */
	public final class SetContentChange extends WorldChange implements
			IHandlesSelection {

		private String oldContent;
		private ITextDocument document;
		private int oldOffset;

		private SetContentChange(List<BasePartition> clonePartitions,
				List<Integer> oldAligns, List<BasicBullet> oldBullets,
				ArrayList<Integer> oldIndents, String oldContent, int oldOffset) {
			super(clonePartitions, oldAligns, oldBullets, oldIndents);
			this.oldContent = oldContent;
			this.oldOffset = oldOffset;
		}

		private SetContentChange(List<BasePartition> clonePartitions,
				List<Integer> oldAligns, List<BasicBullet> oldBullets,
				ArrayList<Integer> oldIndents, ITextDocument oldDocument,
				int oldOffset) {
			super(clonePartitions, oldAligns, oldBullets, oldIndents);
			this.document = oldDocument;
			this.oldOffset = oldOffset;
		}

		/**
		 * @param model
		 *            Model to set to
		 * @param caretOffset
		 *            pre-change caret offset
		 */
		public SetContentChange(ISimpleRichTextModel model, int caretOffset) {
			super(model);
			this.oldContent = model.getText();
			this.oldOffset = caretOffset;
		}

		/**
		 * @param model
		 *            Model to set to
		 * @param caretOffset
		 *            pre-change caret offset
		 */
		public SetContentChange(RichDocument document, int caretOffset) {
			super(document.getModel());
			this.document = document;
			this.oldOffset = caretOffset;
		}

		public void apply(PartitionDelta delta) {
			try {
				editor.setRedraw(false);
				delta.clearAdded();
				delta.clearChanged(); // We don't need this any more, if we
				// change whole content
				delta.setOptimizeParitions(false);
				delta.setFireChange(false);
				layer.disconnectFromDocument();
				ILineAttributeModel lineAttributeModel = delta.getStorage()
						.getLineAttributeModel();
				
				int lineCount = lineAttributeModel.lineCount();
				ArrayList<Integer> aligns = new ArrayList<Integer>();
				ArrayList<Integer> indents = new ArrayList<Integer>();
				ArrayList<BasicBullet> bullets = new ArrayList<BasicBullet>();
				for (int a = 0; a < lineCount; a++) {
					aligns.add(lineAttributeModel.getLineAlign(a));
					bullets.add(lineAttributeModel.getBullet(a));
					indents.add(lineAttributeModel.getLineIndent(a));
				}
				int pos = editor.getCaretOffset();
				String old = doc.get();
				ITextDocument oldDoc = null;
				if (document == null) {
					try {
						doc.replace(0, doc.getLength(), oldContent);
						layer.connectToDocument(doc);

					} catch (Exception e) {
						throw new IllegalStateException(e);
					}
				} else {
					oldDoc = doc;
					doc = document;
					layer.connectToDocument(doc);
					layer.setIgnoreDocumentEvents(true);
				}
				if (lineAttributeModel instanceof ILineAttributeModelExtension){
					ILineAttributeModelExtension ext=(ILineAttributeModelExtension) lineAttributeModel;
					ext.setBulk(this.aligns,this.bullets,this.indents);
				}
				else{
				for (int a = 0; a < this.aligns.length; a++) {
					try {
						lineAttributeModel.setLineAlign(a, 0, this.aligns[a]);
						lineAttributeModel.setLineBulletWithoutCheck(a, 1,
								this.bullets[a]);
						lineAttributeModel.setLineIndentWithoutCheck(a, 1,
								this.indents[a]);
					} catch (Exception e) {
						System.err.println("Wrong number of lines!");
					}
				}
				}
				PartitionStorage storage = delta.getStorage();
				final List<BasePartition> clonePartitions = storage
						.clonePartitions();

				int lengthAddition = 0;
				int partCount = newPartitions.size();
				for (int i = 0; i < partCount; i++)
				{
					BasePartition part = newPartitions.get(i);
					part.setDocument(doc); //If partition was created with one, but after content setting should have another one
					part.setOffset(part.getOffset() + lengthAddition);
					if (part instanceof RegionPartition) {
						if (useRegionControls)
							setRegionPartitionProps(part);
						else
						{
							int lengthDelta = REGION_REPLACE_STR.length() - part.getLength(); 							
							doc.replace(part.getOffset(), part.getLength(), REGION_REPLACE_STR);
							part = new BasePartition(doc, part.getOffset(), REGION_REPLACE_STR.length());
							newPartitions.remove(i);
							newPartitions.add(i,part);
							lengthAddition += lengthDelta;
						}
						
						// 						innerWidgetRegistry.addRedrawedControl((Control)
						// partition.getObject());

					}
					delta.added(part);
				}
				storage.setPartitions(newPartitions);
				partitionChange((ArrayList)newPartitions);
				if (oldDoc == null) {
					delta.getUndoChange().add(
							new SetContentChange(clonePartitions, aligns,
									bullets, indents, old, pos));
				} else {
					delta.getUndoChange().add(
							new SetContentChange(clonePartitions, aligns,
									bullets, indents, oldDoc, pos));
				}
				layer.setIgnoreDocumentEvents(false);
				editor.setSelection(oldOffset);
			} catch (Exception e) {
				Logger.log(e);

			}
			editor.setRedraw(true);
			editor.redraw();
		}
	}

	/**
	 * @author kor Incapsulates paste change
	 */
	public final class PasteChange extends Change implements IHandlesSelection {

		private final ISimpleRichTextModel model;
		private final int offset;
		private Point selectedRange;

		private PasteChange(ISimpleRichTextModel listener, int offset) {
			this.model = listener;
			this.offset = offset;
		}

		private PasteChange(ISimpleRichTextModel listener, Point selectedRange) {
			this.model = listener;
			this.offset = selectedRange.x;
			this.selectedRange = selectedRange;
		}

		public void apply(PartitionDelta delta) {
			try {
				// delta.setOptimizeParitions(false);
				int lineNumber = doc.getLineOfOffset(offset);
				String insertedText = model.getText();
				List<BasePartition> partitions = layer.getPartitions();
				List<Integer> oldAligns = new ArrayList<Integer>();
				List<BasicBullet> oldBullets = new ArrayList<BasicBullet>();
				ArrayList<Integer> oldIndents = new ArrayList<Integer>();
				int lineCount = editor.getLineCount();
				for (int a = 0; a < lineCount; a++) {
					oldAligns.add(editor.getLineAlignment(a));
					oldBullets.add(MetaConverter.convertBulletBack(editor
							.getLineBullet(a), getBulletFactory()));
					oldIndents.add(layer.getLineAttributeModel().getLineIndent(
							a));
				}
				List<BasePartition> clonePartitions = layer.getStorage()
						.clonePartitions();
				final String oldContent = doc.get();
				delta.getUndoChange().add(
						new SetContentChange(clonePartitions, oldAligns,
								oldBullets, oldIndents, oldContent, editor
										.getCaretOffset()));
				if (selectedRange != null) {
					doc.replace(selectedRange.x, selectedRange.y, "");
				}
				layer.disconnectFromDocument();

				doc.replace(offset, 0, insertedText);

				layer.connectToDocument(doc);

				List<BasePartition> newPartitions = model.getPartitions();

				for (BasePartition partition : newPartitions) {
					partition.setOffset(partition.getOffset() + offset);
					if (partition instanceof RegionPartition) {
						setRegionPartitionProps(partition);
					}
				}
				ArrayList<BasePartition> resultPartitions = new ArrayList<BasePartition>(
						partitions.size() + newPartitions.size());

				int insertionIndex;
				BasePartition partitionTail = null;
				for (insertionIndex = 0; insertionIndex < partitions.size(); insertionIndex++) {
					BasePartition curPartition = (BasePartition) partitions
							.get(insertionIndex);
					if (curPartition.getOffset() < offset) {
						resultPartitions.add(curPartition);
						if (curPartition.getOffset() + curPartition.getLength() > offset) {
							int oldLength = curPartition.getLength();
							curPartition.setLength(offset
									- curPartition.getOffset());
							partitionTail = new BasePartition(doc,
									curPartition.getOffset()
											+ curPartition.getLength()
											+ insertedText.length(), oldLength
											- curPartition.getLength());
							partitionTail.applyAttributes(curPartition);
						}
					} else {
						break;
					}
				}
				for (BasePartition p : newPartitions) {
					resultPartitions.add(p);
				}

				if (partitionTail != null)
					resultPartitions.add(partitionTail);

				for (; insertionIndex < partitions.size(); insertionIndex++) {
					BasePartition p = (BasePartition) partitions
							.get(insertionIndex);
					p.setOffset(p.getOffset() + insertedText.length());
					resultPartitions.add(p);
				}
				layer.getStorage().setPartitions(resultPartitions);
				for (int i = 0; i < model.getLineCount(); i++) {
					int align = model.getAlign(i);
					if (align != RichTextEditorConstants.FIT_ALIGN)
						editor.setLineAlignment(i + lineNumber, 1, align);
					else {
						editor.setLineAlignment(i + lineNumber, 1,
								RichTextEditorConstants.LEFT_ALIGN);
						editor.setLineJustify(i + lineNumber, 1, true);
					}
					editor.setLineBullet(i + lineNumber, 1, MetaConverter
							.convertBullet((BasicBullet) model.getBullet(i)));
					layer.getLineAttributeModel().setLineIndentWithoutCheck(
							i + lineNumber, 1, model.getIndent(i));
					// editor.setLineIndent(i + lineNumber, 1,
					// model.getIndent(i));
				}
				editor.setStyleRanges(new org.eclipse.swt.custom.StyleRange[0]);
				for (BasePartition p : resultPartitions) {
					delta.added(p);
				}
				editor.setCaretOffset(offset + insertedText.length());
				editor.setSelection(offset + insertedText.length());
			} catch (Exception e) {
				throw new IllegalStateException();
			}
		}
	}

	protected class RegionListener implements IRegionCompositeWrapperListener {

		protected RegionPartition partition;

		public RegionListener(RegionPartition partition) {
			this.partition = partition;
		}

		public void handleEvent(RegionCompositeEvent event) {
			if (event.getType() == RegionCompositeEvent.LINE_COUNT_CHANGE)
				setStyleRangeByPartition(partition);
			else if (event.getType() == RegionCompositeEvent.CARET_MOVE) {
				int y = ((Caret) event.getObject()).getLocation().y;
				int globalY = partition.getWrapper().getLocation().y + y;
				if (globalY < 0) {
					editor.setTopPixel(editor.getTopPixel() + globalY);
				} else if (globalY > editor.getSize().y) {
					editor.setTopPixel(editor.getTopPixel() + globalY
							- editor.getSize().y + editor.getLineHeight()
							+ MARGIN);
				}

			}
		}

		public boolean equals(Object obj) {
			if (obj instanceof RegionListener) {
				if (((RegionListener) obj).partition == partition)
					return true;
			}
			return false;
		}

		public int hashCode() {
			return partition.hashCode();
		}
	}

	/**
	 * Margin for contents displaying in editor window
	 */
	public static final int MARGIN = 30;

	private static final String REGION_STR = "?\n";
	private static final String REGION_REPLACE_STR = "[]\n";

	private ArrayList<IRichDocumentAutoStylingStrategy> autoStylers = new ArrayList<IRichDocumentAutoStylingStrategy>();

	protected ColorManager colorManager;
	private HashSet<IRichDocumentListener> richDocumentlisteners = new HashSet<IRichDocumentListener>();
	protected HashSet<IRichtextDecorator> rangeDecorators = new HashSet<IRichtextDecorator>();
	protected FontStyleManager fontStyleManager;
	protected ImageManager imageManager;

	protected StyledText editor;
	protected BasePartitionLayer layer;
	protected int currentAlign = -1;
	protected int currentIndent = 0;
	protected int currentRightIndent = 0;
	protected int currentFirstLineIndent = 0;
	protected int currentLineSpacing = 1;

	// resize

	protected ITextDocument doc;

	protected BasePartition linkPrototypePartition;

	protected FontStyle boldFontStyle = new FontStyle(FontStyle.BOLD);
	protected FontStyle italicFontStyle = new FontStyle(FontStyle.ITALIC);
	protected FontStyle underlineFontStyle = new FontStyle(FontStyle.UNDERLINED);
	protected FontStyle strikethroughFontStyle = new FontStyle(
			FontStyle.STRIKETHROUGH);
	protected FontStyle subFontStyle = new FontStyle(FontStyle.SUB);
	protected FontStyle supFontStyle = new FontStyle(FontStyle.SUP);

	protected int hrWidth = 2;
	protected int indentSize = -1;

	private IHTMLSerializerFactory serializerFactory;
	private IHTMLLoaderFactory loaderFactory;

	protected Listener paintListener, repaintListener;
	protected KeyListener editorKeyListener;
	protected MouseListener editorMouseListener;
	protected PaintObjectListener bulletPaintObjectListener;
	protected InnerWidgetRegistry innerWidgetRegistry;
	protected RichtextCommandCreator commandCreator;
	/**
	 * Listens for inner editor controls (like regions-representing StyledTexts)
	 * getting focus
	 */
	protected FocusListener insideRegionListener;

	/**
	 * Listens to region text modification
	 */
	protected List<IRegionCompositeWrapperListener> regionCompositeListeners;
	
	/**
	 * @return Auto Stylers List
	 */
	public IRichDocumentAutoStylingStrategy[] getAutoStylingStrategies() {
		return autoStylers
				.toArray(new IRichDocumentAutoStylingStrategy[autoStylers
						.size()]);
	}

	/**
	 * @param str
	 *            new auto styling strategy
	 */
	public void addAutoStylingStrategy(IRichDocumentAutoStylingStrategy str) {
		autoStylers.add(str);
	}

	/**
	 * @param str
	 *            auto styling strategy to remove
	 */
	public void removeAutoStylingStrategy(IRichDocumentAutoStylingStrategy str) {
		autoStylers.add(str);
	}

	/**
	 * @return Loader Factory
	 */
	public final IHTMLLoaderFactory getLoaderFactory() {
		return loaderFactory;
	}

	/**
	 * @param loaderFactory
	 *            Loader Factory to set
	 */
	public final void setLoaderFactory(IHTMLLoaderFactory loaderFactory) {
		this.loaderFactory = loaderFactory;
	}

	/**
	 * @return Serializer factory
	 */
	public IHTMLSerializerFactory getSerializerFactory() {
		return serializerFactory;
	}

	/**
	 * @param serializerFactory
	 *            Serializer factory to set
	 */
	public void setSerializerFactory(IHTMLSerializerFactory serializerFactory) {
		this.serializerFactory = serializerFactory;
	}

	protected void setLineIdentation(int offset, int length) {
		if (length > 0)
			return;
		int firstLine = editor.getLineAtOffset(offset);
		int lineIndent = editor.getLineIndent(firstLine);
		int alignment = editor.getLineAlignment(firstLine);
		if (editor.getCharCount() == 0) {
			alignment = currentAlign;
		}

		String str = editor.getContent().getLine(firstLine);
		if (str.equals("")) {
			if (alignment == RichTextEditorConstants.CENTER_ALIGN) {
				int indent = editor.getClientArea().width / 2 - MARGIN + 1;
				if (lineIndent != indent)
					editor.setLineIndent(firstLine, 1, indent);
			} else if (alignment == RichTextEditorConstants.RIGHT_ALIGN) {
				int indent = editor.getClientArea().width - MARGIN - 1;
				if (lineIndent != indent)
					editor.setLineIndent(firstLine, 1, indent);
			} else if (lineIndent < RichTextEditorConstants.DEFAULT_INDENT)
				editor.setLineIndent(firstLine, 1,
						RichTextEditorConstants.DEFAULT_INDENT);

		} else if (lineIndent < RichTextEditorConstants.DEFAULT_INDENT)
			editor.setLineIndent(firstLine, 1,
					RichTextEditorConstants.DEFAULT_INDENT);
	}

	/**
	 * Basic constructor
	 * 
	 * @param newEditor
	 *            StyledText widget
	 * @param newDoc
	 *            Document to associate with
	 */
	public LayerManager(final StyledText newEditor, ITextDocument newDoc) {
		editor = newEditor;
		innerWidgetRegistry = InnerWidgetRegistry.getInstanceFor(editor);
		doc = newDoc;
		regionCompositeListeners = new ArrayList<IRegionCompositeWrapperListener>();
		paintListener = new Listener() {

			public void handleEvent(Event event) {
				setLineIdentation(editor.getCaretOffset(), 0);
			}

		};
		newEditor.addListener(SWT.Paint, paintListener);
		newEditor.addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent e) {
				validateCaretPos(e.keyCode);
			}

			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
			}
		});
		newEditor.addMouseListener(new MouseListener() {

			public void mouseUp(MouseEvent e) {

			}

			public void mouseDown(MouseEvent e) {
				validateCaretPos(0);
			}

			public void mouseDoubleClick(MouseEvent e) {

			}
		});

		bulletPaintObjectListener = new PaintObjectListener() {

			public void paintObject(PaintObjectEvent event) {
				if (event.bullet == null
						|| event.bullet.type != ST.BULLET_CUSTOM) {
					return;
				}
				Display display = event.display;
				org.eclipse.swt.custom.StyleRange style = event.style;
				Font font = style.font;
				if (font == null)
					font = newEditor.getFont();
				TextLayout layout = new TextLayout(display);
				layout.setAscent(event.ascent);
				layout.setDescent(event.descent);
				layout.setFont(font);
				StringBuilder bulletTxt = new StringBuilder();
				if (event.bullet.text != null && event.bullet.text.equals("a")) {
					if (event.bulletIndex <= 26)
						bulletTxt.append((char) ('a' + event.bulletIndex - 1));
					else
						bulletTxt.append('a');
					bulletTxt.append(".");
				} else if (event.bullet.text != null
						&& event.bullet.text.equals("i")) {
					bulletTxt.append(RomanConverter.converts(event.bulletIndex)
							.toLowerCase());
					bulletTxt.append(".");
				} else {
					final char charAt = event.bullet.text.charAt(0);
					if (Character.isDigit(charAt)) {
						int idx = Integer.parseInt(event.bullet.text);
						bulletTxt
								.append((char) ('1' + idx + event.bulletIndex - 2));
					} else if (Character.isLetter(charAt)) {
						// int idx = Integer.parseInt(event.bullet.text);
						bulletTxt
								.append((char) (charAt + event.bulletIndex - 1));
					}
					bulletTxt.append(".");
				}
				layout.setText(bulletTxt.toString());
				int paintY = event.y;
				// if
				// (lineIndex==0||styledText.getLineBullet(lineIndex-1)==null){
				// paintY+=15;
				// }
				GlyphMetrics metrics = event.bullet.style.metrics;
				int paintX = event.x;
				int x = paintX
						+ Math.max(0, metrics.width - layout.getBounds().width
								- 8);
				layout.draw(event.gc, x - 5, paintY);
				layout.dispose();

			}

		};
		newEditor.addPaintObjectListener(bulletPaintObjectListener);
		newEditor.addExtendedModifyListener(new ExtendedModifyListener() {

			public void modifyText(ExtendedModifyEvent event) {
				int offset = event.start + event.length;
				BasePartition curPartition = (BasePartition) getLayer()
						.getPartitionAtOffset(offset);
				if (curPartition != null && curPartition.requiresSingleLine()) {
					String text = curPartition.getText();
					if (!text.startsWith("\n") && !text.startsWith("\r")) {
						editor.setCaretOffset(offset - 1);
					}
				}

			}
		});

		repaintListener = new Listener() {

			public void handleEvent(Event event) {
				editor.redraw();
			}

		};
		newEditor.addListener(SWT.Selection, repaintListener);
		if (newEditor.getVerticalBar() != null) {
			newEditor.getVerticalBar().addListener(SWT.Selection,
					repaintListener);
		}
		newEditor.addVerifyListener(new VerifyListener() {

			public void verifyText(VerifyEvent e) {
				int offset = e.end;
				BasePartition curPartition = (BasePartition) getLayer()
						.getPartitionAtOffset(offset);
				if (curPartition != null && curPartition.requiresSingleLine()) {
					String text = curPartition.getText();
					if (!text.startsWith("\n") && !text.startsWith("\r")
							&& !e.text.endsWith("\n") && !e.text.endsWith("\r")) {
						e.text = e.text + "\n";
					}
				}

			}
		});
		/*
		 * newEditor.addVerifyListener(new VerifyListener(){
		 * 
		 * public void verifyText(VerifyEvent e) {
		 * 
		 * } });
		 */

		// newEditor.addKeyListener(editorKeyListener);
		fontStyleManager = createFontStyleManager();
		// if (!debug)

		Font defaultFont = getDefaultFont();
		editor.setFont(defaultFont);
		editor.setLineSpacing(getLineSpacing());
		// editor.setLineSpacing(50);
		newEditor.addPaintObjectListener(this);
		layer = createBasePartitionLayer();
		layer.setManager(this);
		layer.connectToDocument(doc);
		commandCreator = new RichtextCommandCreator(layer);

		layer.addPartitionListener(this);
		colorManager = new ColorManager(editor.getDisplay());
		imageManager = createImageManager();
		fontStyleManager.addFontStyleChangeListener(this);

		try {
			Method declaredMethod = StyledText.class.getDeclaredMethod(
					"setMargins", new Class[] { int.class, int.class,
							int.class, int.class, });
			declaredMethod.setAccessible(true);
			declaredMethod.invoke(editor, 10, 10, 10, 10);
		} catch (Exception e) {
			e.printStackTrace();
		}
		linkPrototypePartition = new BasePartition(doc, 0, 0);
		linkPrototypePartition.setStyleMask(FontStyle.UNDERLINED);
		linkPrototypePartition.setColorRGB(MetaConverter.convertRGBBack(editor
				.getDisplay().getSystemColor(SWT.COLOR_BLUE).getRGB()));
		addRichtextDecorator(new HyperlinksDecorator());
	}

	protected Font getDefaultFont() {
		return fontStyleManager.getDefaultFont();
	}

	protected int getLineSpacing() {
		fontStyleManager = createFontStyleManager();
		return fontStyleManager.getDefaultFontHeight() / 4; // 2 because we can
		// have
		// one-with-half
		// interval and more
		// *2 because
		// lowercase letters
		// are 2 times
		// smaller than
		// standart font
		// height
	}

	protected ImageManager createImageManager() {
		return new ImageManager(editor.getDisplay());
	}

	protected BasePartitionLayer createBasePartitionLayer() {
		return new BasePartitionLayer();
	}

	protected FontStyleManager createFontStyleManager() {
		if (fontStyleManager == null)
			fontStyleManager = new FontStyleManager(editor.getDisplay());
		return fontStyleManager;
	}

	/**
	 * Disposes all used resources
	 */
	public void dispose() {
		if (paintListener != null)
			editor.removeListener(SWT.Paint, paintListener);
		if (repaintListener != null) {
			editor.removeListener(SWT.Selection, repaintListener);
			if (repaintListener != null && editor != null) {
				if(editor.getVerticalBar() != null){
				editor.getVerticalBar().removeListener(SWT.Selection,
						repaintListener);
				}
			}
		}
		if (bulletPaintObjectListener != null)
			editor.removePaintObjectListener(bulletPaintObjectListener);
		if (editorKeyListener != null)
			editor.removeKeyListener(editorKeyListener);
		if (editorMouseListener != null)
			editor.removeMouseListener(editorMouseListener);
		layer.disposeSpecialPartitionsData();
		layer.disconnectFromDocument();

		imageManager.dispose();
		colorManager.dispose();
		fontStyleManager.dispose();
	}

	/**
	 * @return {@link FontRegistry} of {@link FontStyleManager} used by this
	 *         class
	 */
	public FontRegistry getFontRegistry() {
		return fontStyleManager.getFontRegistry();
	}

	/**
	 * Current Font Partition is used for "delayed" applying of some partition
	 * style. This is needed in case, when user has selected some font style
	 * without current selection and expects newly typed text will have selected
	 * style Current Font Partition is a dummy partition representing selected
	 * style
	 * 
	 * @return Current Font Partition
	 */
	public BasePartition getCurrentFontPartition() {
		return layer.getCurrentFontPartition();
	}

	/**
	 * Current Font Partition is used for "delayed" applying of some partition
	 * style. This is needed in case, when user has selected some font style
	 * without current selection and expects newly typed text will have selected
	 * style Current Font Partition is a dummy partition representing selected
	 * style
	 * 
	 * @param currentFontPartition
	 *            Current Font Partition
	 */
	public void setCurrentFontPartition(BasePartition currentFontPartition) {
		layer.setCurrentFontPartition(currentFontPartition);
	}

	/**
	 * Make some text sub/not sub
	 * 
	 * @param offset
	 *            Text offset
	 * @param length
	 *            Text length
	 * @param apply
	 *            Apply or remove sub attribute
	 */
	public void subCommand(int offset, int length, boolean apply) {
		layer.fontStyleCommand(subFontStyle, offset, length, apply);
	}

	/**
	 * Make some text sup/not sup
	 * 
	 * @param offset
	 *            Text offset
	 * @param length
	 *            Text length
	 * @param apply
	 *            Apply or remove sup attribute
	 */
	public void supCommand(int offset, int length, boolean apply) {
		layer.fontStyleCommand(supFontStyle, offset, length, apply);
	}

	/**
	 * Make some text bold/not bold
	 * 
	 * @param offset
	 *            Text offset
	 * @param length
	 *            Text length
	 * @param apply
	 *            Apply or remove bold attribute
	 */
	public void boldCommand(int offset, int length, boolean apply) {
		layer.fontStyleCommand(boldFontStyle, offset, length, apply);
	}

	/**
	 * Make some text italic/not italic
	 * 
	 * @param offset
	 *            Text offset
	 * @param length
	 *            Text length
	 * @param apply
	 *            Apply or remove italic attribute
	 */
	public void italicCommand(int offset, int length, boolean apply) {
		layer.fontStyleCommand(italicFontStyle, offset, length, apply);
	}

	/**
	 * Make some text strikethrough/not strikethrough
	 * 
	 * @param offset
	 *            Text offset
	 * @param length
	 *            Text length
	 * @param apply
	 *            Apply or remove strikethrough attribute
	 */
	public void strikethroughCommand(int offset, int length, boolean apply) {
		layer.fontStyleCommand(strikethroughFontStyle, offset, length, apply);
	}

	/**
	 * Make some text underlined/not underlined
	 * 
	 * @param offset
	 *            Text offset
	 * @param length
	 *            Text length
	 * @param apply
	 *            Apply or remove underlined attribute
	 */
	public void underlineCommand(int offset, int length, boolean apply) {
		layer.fontStyleCommand(underlineFontStyle, offset, length, apply);
	}

	/**
	 * Change some text's font
	 * 
	 * @param fontStyleDisplayName
	 *            name of font style selected in combo
	 * @param offset
	 *            Text offset
	 * @param length
	 *            Text length
	 */
	public void changeFontCommand(String fontStyleDisplayName, int offset,
			int length) {
		if (length != 0) {
			FontStyle style = fontStyleManager
					.getFontStyle(fontStyleDisplayName);
			layer.changeFontCommand(style, offset, length);
		}
	}

	/**
	 * Indicates, that data layer changed, and it's visualization nedd also be
	 * updated
	 * 
	 * @param event
	 *            Layer Event holding data about changes performed
	 */
	@SuppressWarnings("unchecked")
	public void layerChanged(LayerEvent event) {
		if (editor.isDisposed()) {
			return;
		}
		
		ArrayList<IPartition> sortedPartitions = new ArrayList<IPartition>(
				event.getChangedPartitions());
		Collections.sort(sortedPartitions, new Comparator() {

			public int compare(Object o1, Object o2) {
				if (o1 instanceof BasePartition && o2 instanceof BasePartition) {
					if (((BasePartition) o1).getIndex() > ((BasePartition) o2)
							.getIndex())
						return 1;
					if (((BasePartition) o1).getIndex() < ((BasePartition) o2)
							.getIndex())
						return -1;
				}
				return 0;
			}
		});
		ArrayList<BasePartition> sameStylePartitions = new ArrayList<BasePartition>();
		for (Iterator<IPartition> iterator = sortedPartitions.iterator(); iterator
				.hasNext();) {

			IPartition part = iterator.next();
			// Logger.log(part);
			if (part instanceof BasePartition) {
				BasePartition fpartition = (BasePartition) part;
				if (fpartition.getIndex() == -1) {
					continue;
				}
				if (sameStylePartitions.size() == 0)
					sameStylePartitions.add(fpartition);
				BasePartition prevPartition = sameStylePartitions
						.get(sameStylePartitions.size() - 1);
				if (fpartition.equalsByStyle(prevPartition)
						&& fpartition.getOffset() == prevPartition.getOffset()
								+ prevPartition.getLength())
					sameStylePartitions.add(fpartition);
				else {
					try {
						setStyleRangesByPartitionList(sameStylePartitions);
					} catch (Exception e) {
						Logger.log(e);
					}
					sameStylePartitions.clear();
					sameStylePartitions.add(fpartition);
				}
			}
		}
		try {
			setStyleRangesByPartitionList(sameStylePartitions);
		} catch (Exception e) {
			Logger.log(e);
		}
		if (currentAlign > -1) {
			editor.setLineAlignment(0, 1, currentAlign);
			currentAlign = -1;
		}
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				editor.redraw();
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	protected void partitionChange(ArrayList<IPartition>sortedPartitions){
		
		Collections.sort(sortedPartitions, new Comparator() {

			public int compare(Object o1, Object o2) {
				if (o1 instanceof BasePartition && o2 instanceof BasePartition) {
					if (((BasePartition) o1).getIndex() > ((BasePartition) o2)
							.getIndex())
						return 1;
					if (((BasePartition) o1).getIndex() < ((BasePartition) o2)
							.getIndex())
						return -1;
				}
				return 0;
			}
		});
		ArrayList<BasePartition> sameStylePartitions = new ArrayList<BasePartition>();
		ArrayList<org.eclipse.swt.custom.StyleRange>ranges=new ArrayList<org.eclipse.swt.custom.StyleRange>();
		for (Iterator<IPartition> iterator = sortedPartitions.iterator(); iterator
				.hasNext();) {

			IPartition part = iterator.next();
			// Logger.log(part);
			if (part instanceof BasePartition) {
				BasePartition fpartition = (BasePartition) part;
				if (fpartition.getIndex() == -1) {
					continue;
				}
				if (sameStylePartitions.size() == 0)
					sameStylePartitions.add(fpartition);
				BasePartition prevPartition = sameStylePartitions
						.get(sameStylePartitions.size() - 1);
//				if (fpartition.equalsByStyle(prevPartition)
//						&& fpartition.getOffset() == prevPartition.getOffset()
//								+ prevPartition.getLength())
//					sameStylePartitions.add(fpartition);
//				else {
					try {
						setStyleRangesByPartitionList(sameStylePartitions,ranges);
					} catch (Exception e) {
						Logger.log(e);
					}
					sameStylePartitions.clear();					
				//}
			}
		}
		try {
			setStyleRangesByPartitionList(sameStylePartitions,ranges);
		} catch (Exception e) {
			Logger.log(e);
		}
		if (currentAlign > -1) {
			editor.setLineAlignment(0, 1, currentAlign);
			currentAlign = -1;
		}
		
		org.eclipse.swt.custom.StyleRange[] array =ranges.toArray(new org.eclipse.swt.custom.StyleRange[ranges.size()]);
		editor.setStyleRanges(array);
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				if (!editor.isDisposed())
					editor.redraw();
			}
		});
		
	}

	private void setStyleRangesByPartitionList(
			ArrayList<BasePartition> partitions,
			ArrayList<org.eclipse.swt.custom.StyleRange> r) {
		int size = partitions.size();
		if (size == 0)
			return;
		StyleRange range = partitions.get(0).getStyleRange(this);
		if (size > 1) {
			range.length = partitions.get(size - 1).getOffset()
					+ partitions.get(size - 1).getLength() - range.start;
		}
		List<StyleRange> ranges = decorateRanges(Collections
				.singletonList(range));
		for (Iterator<StyleRange> iterator = ranges.iterator(); iterator
				.hasNext();) {
			final StyleRange styleRange = (StyleRange) iterator.next();
			if (styleRange.length <= 0) {
				continue;
			}
			r.add(MetaConverter.convertRange(styleRange,
					LayerManager.this));
		}
	}

	/**
	 * Indicates, that partition changed, and it's visualization nedd also be
	 * updated
	 * 
	 * @param event
	 *            Layer Event holding data about changes performed
	 */
	public void partitionChanged(PartitionEvent event) {
		BasePartition partition = (BasePartition) event.getPartition();
		try {
			setStyleRangeByPartition(partition);
		} catch (Exception e) {
			Logger.log(e);
		}
		editor.redraw();
	}

	protected int getSelectedLine(int offset) {
		try {
			return doc.getLineOfOffset(offset);
		} catch (Exception e) {
			Logger.log(e);
		}
		return -1;
	}

	/**
	 * Returns Point object, where x is first selected line index and y - last
	 * selected line index
	 * 
	 * @param offset
	 *            selection offset
	 * @param length
	 * @return
	 */

	protected Point getSelectedLines(int offset, int length) {
		int firstLineNum = getSelectedLine(offset);
		int lastLineNum;
		if (length == 0)
			lastLineNum = firstLineNum;
		else
			try {
				lastLineNum = doc.getLineOfOffset(offset + length - 1);
			} catch (Exception e) {
				Logger.log(e);
				return null;
			}
		return new Point(firstLineNum, lastLineNum);
	}

	/**
	 * Increases interval indent by 1
	 * 
	 * @param offset
	 *            first selected range char idx
	 * @param length
	 *            selected char range length
	 */
	public void increaseIntervalIndent(int offset, int length) {
		if (editor.getCharCount() == 0) {
			if (currentIndent < RichTextEditorConstants.MAX_INDENT)
				currentIndent++;
			editor.redraw();
			return;
		}
		int indent = ((AbstractModel) layer).getLineAttributeModel()
				.getLineIndent(getSelectedLine(offset));
		if (indent < RichTextEditorConstants.MAX_INDENT)
			setIntervalIndent(offset, length, indent + 1);
	}

	/**
	 * Decreases interval indent by 1
	 * 
	 * @param offset
	 *            first selected range char idx
	 * @param length
	 *            selected char range length
	 */
	public void decreaseIntervalIndent(int offset, int length) {
		if (editor.getCharCount() == 0) {
			if (currentIndent > 0)
				currentIndent--;
			editor.redraw();
			return;
		}
		int indent = ((AbstractModel) layer).getLineAttributeModel()
				.getLineIndent(getSelectedLine(offset));
		if (indent > 0)
			setIntervalIndent(offset, length, indent - 1);
	}

	/**
	 * Increases interval right indent by 1
	 * 
	 * @param offset
	 *            first selected range char idx
	 * @param length
	 *            selected char range length
	 */
	public void increaseIntervalRightIndent(int offset, int length) {
		if (editor.getCharCount() == 0) {
			if (currentRightIndent < RichTextEditorConstants.MAX_RIGHT_INDENT)
				currentRightIndent++;
			editor.redraw();
			return;
		}
		int indent = ((AbstractModel) layer).getLineAttributeModel()
				.getRightIndent(getSelectedLine(offset));
		if (indent < RichTextEditorConstants.MAX_INDENT)
			setIntervalRightIndent(offset, length, indent + 1);
	}

	/**
	 * Decreases interval right indent by 1
	 * 
	 * @param offset
	 *            first selected range char idx
	 * @param length
	 *            selected char range length
	 */
	public void decreaseIntervalRightIndent(int offset, int length) {
		if (editor.getCharCount() == 0) {
			if (currentRightIndent > 0)
				currentRightIndent--;
			editor.redraw();
			return;
		}
		int indent = ((AbstractModel) layer).getLineAttributeModel()
				.getRightIndent(getSelectedLine(offset));
		if (indent > 0)
			setIntervalRightIndent(offset, length, indent - 1);
	}

	/**
	 * Increases interval first line indent by 1
	 * 
	 * @param offset
	 *            first selected range char idx
	 * @param length
	 *            selected char range length
	 */
	public void increaseIntervalFirstLineIndent(int offset, int length) {
		if (editor.getCharCount() == 0) {
			if (currentFirstLineIndent < RichTextEditorConstants.MAX_INDENT)
				currentFirstLineIndent++;
			editor.redraw();
			return;
		}
		int indent = ((AbstractModel) layer).getLineAttributeModel()
				.getFirstLineIndent(getSelectedLine(offset));
		if (indent < RichTextEditorConstants.MAX_INDENT)
			setIntervalFirstLineIndent(offset, length, indent + 1);
	}

	/**
	 * Decreases interval first line indent by 1
	 * 
	 * @param offset
	 *            first selected range char idx
	 * @param length
	 *            selected char range length
	 */
	public void decreaseIntervalFirstLineIndent(int offset, int length) {
		if (editor.getCharCount() == 0) {
			if (currentFirstLineIndent > 0)
				currentFirstLineIndent--;
			editor.redraw();
			return;
		}
		int indent = ((AbstractModel) layer).getLineAttributeModel()
				.getFirstLineIndent(getSelectedLine(offset));
		if (indent > 0)
			setIntervalFirstLineIndent(offset, length, indent - 1);
	}

	/**
	 * Increases interval spacing by 0.5, from 1 to 2 units
	 * 
	 * @param offset
	 *            first selected range char idx
	 * @param length
	 *            selected char range length
	 */
	public void increaseIntervalLineSpacing(int offset, int length) {
		if (editor.getCharCount() == 0) {
			if (currentLineSpacing < RichTextEditorConstants.MAX_SPACING)
				currentLineSpacing++;
			editor.redraw();
			return;
		}
		int spacing = ((AbstractModel) layer).getLineAttributeModel()
				.getLineSpacing(getSelectedLine(offset));
		if (spacing < RichTextEditorConstants.MAX_SPACING)
			setIntervalLineSpacing(offset, length, spacing + 1);
	}

	/**
	 * Decreases interval spacing by 0.5, from 1 to 2 units
	 * 
	 * @param offset
	 *            first selected range char idx
	 * @param length
	 *            selected char range length
	 */
	public void decreaseIntervalLineSpacing(int offset, int length) {
		if (editor.getCharCount() == 0) {
			if (currentLineSpacing > RichTextEditorConstants.MIN_SPACING)
				currentLineSpacing--;
			editor.redraw();
			return;
		}
		int spacing = ((AbstractModel) layer).getLineAttributeModel()
				.getLineSpacing(getSelectedLine(offset));
		if (spacing > RichTextEditorConstants.MIN_SPACING)
			setIntervalLineSpacing(offset, length, spacing - 1);
	}

	/**
	 * Sets selected interval indent
	 * 
	 * @param offset
	 *            Text offset
	 * @param length
	 *            Text indent
	 * @param indent
	 *            Indent in units to set
	 */
	public void setIntervalIndent(int offset, int length, int indent) {
		if (editor.getCharCount() == 0) {
			currentIndent = indent;
			editor.redraw();
			return;
		}
		Point selectedLines = getSelectedLines(offset, length);
		int firstLineNum = selectedLines.x;
		int lastLineNum = selectedLines.y;
		int count = lastLineNum - firstLineNum + 1;
		layer.execute(new SetIndentChange(firstLineNum, count, indent));
	}

	/**
	 * Sets selected interval first line indent
	 * 
	 * @param offset
	 *            Text offset
	 * @param length
	 *            Text indent
	 * @param indent
	 *            First line indent in units to set
	 */
	public void setIntervalFirstLineIndent(int offset, int length, int indent) {
		if (editor.getCharCount() == 0) {
			currentFirstLineIndent = indent;
			editor.redraw();
			return;
		}
		Point selectedLines = getSelectedLines(offset, length);
		int firstLineNum = selectedLines.x;
		int lastLineNum = selectedLines.y;
		int count = lastLineNum - firstLineNum + 1;
		layer
				.execute(new SetFirstLineIndentChange(firstLineNum, count,
						indent));
	}

	/**
	 * Sets selected interval right indent
	 * 
	 * @param offset
	 *            Text offset
	 * @param length
	 *            Text indent
	 * @param indent
	 *            Indent in units to set
	 */
	public void setIntervalRightIndent(int offset, int length, int indent) {
		if (editor.getCharCount() == 0) {
			currentIndent = indent;
			editor.redraw();
			return;
		}
		Point selectedLines = getSelectedLines(offset, length);
		int firstLineNum = selectedLines.x;
		int lastLineNum = selectedLines.y;
		int count = lastLineNum - firstLineNum + 1;
		layer.execute(new SetRightIndentChange(firstLineNum, count, indent));
	}

	/**
	 * Sets selected interval line spacing
	 * 
	 * @param offset
	 *            Text offset
	 * @param length
	 *            Text indent
	 * @param spacing
	 *            Line spacing to set
	 */
	public void setIntervalLineSpacing(int offset, int length, int spacing) {
		if (editor.getCharCount() == 0) {
			currentFirstLineIndent = spacing;
			editor.redraw();
			return;
		}
		Point selectedLines = getSelectedLines(offset, length);
		int firstLineNum = selectedLines.x;
		int lastLineNum = selectedLines.y;
		int count = lastLineNum - firstLineNum + 1;
		layer.execute(new SetLineSpacingChange(spacing, count, firstLineNum));
	}

	/**
	 * Sets selected interval align
	 * 
	 * @param offset
	 *            Text offset
	 * @param length
	 *            Text align
	 * @param align
	 *            Align constant to set
	 */
	public void setIntervalAlign(int offset, int length, int align) {
		if (editor.getCharCount() == 0) {
			currentAlign = align;
			editor.redraw();
			return;
		}
		Point selectedLines = getSelectedLines(offset, length);
		int firstLineNum = selectedLines.x;
		int lastLineNum = selectedLines.y;
		int count = lastLineNum - firstLineNum;
		layer.execute(new SetAlignChange(firstLineNum, count, align));
	}

	BulletFactory factory = new BulletFactory();

	/**
	 * Sets selected interval list style
	 * 
	 * @param offset
	 *            Text offset
	 * @param length
	 *            Text align
	 * @param listStyle
	 *            List style constant to set
	 */
	public void setIntervalList(int offset, int length, int listStyle) {
		BasicBullet bullet = null;

		if (listStyle == RichTextEditorConstants.BULLETED_LIST)
			bullet = factory.getNewBulletedListBulletInstance();
		else if (listStyle == RichTextEditorConstants.NUMBERED_LIST)
			bullet = factory.getNewNumberedListBulletInstance(
					BulletFactory.SIMPLE_NUMBERED_LIST, null);
		else if (listStyle == RichTextEditorConstants.LETTERS_NUMBERED_LIST)
			bullet = factory.getNewNumberedListBulletInstance(
					BulletFactory.LETTERS_NUMBERED_LIST, null);
		else if (listStyle == RichTextEditorConstants.ROMAN_NUMBERED_LIST)
			bullet = factory.getNewNumberedListBulletInstance(
					BulletFactory.ROMAN_NUMBERED_LIST, null);
		Point selectedLines = getSelectedLines(offset, length);
		int firstLineNum = selectedLines.x;
		int lastLineNum = selectedLines.y;
		layer.execute(new SetBulletChange(firstLineNum, lastLineNum
				- firstLineNum + 1, bullet));

	}

	/**
	 * Sets selected interval foreground color
	 * 
	 * @param color
	 *            RGB color to set
	 */
	public void setSelectedIntervalForegroundColor(RGB color) {
		int offset = editor.getSelectionRange().x;
		int length = editor.getSelectionRange().y;

		FontStyle colorStyle = new FontStyle(0, color);
		if (length == 0)
			colorStyle.applyStyle(layer.getCurrentFontPartition());
		else
			layer.fontStyleCommand(colorStyle, offset, length, true);
	}

	/**
	 * Sets selected interval background color
	 * 
	 * @param color
	 *            RGB color to set
	 */
	public void setSelectedIntervalBackgroundColor(RGB color) {
		int offset = editor.getSelectionRange().x;
		int length = editor.getSelectionRange().y;
		FontStyle colorStyle = new FontStyle(0);
		colorStyle.setBgColor(color);
		if (length == 0)
			colorStyle.applyStyle(layer.getCurrentFontPartition());
		else
			layer.fontStyleCommand(colorStyle, offset, length, true);
	}

	/**
	 * @return Partition associated with manager
	 */
	public BasePartitionLayer getLayer() {
		return layer;
	}

	/**
	 * Inserts new link partition
	 * 
	 * @param point
	 *            point to insert this partition at
	 * @param name
	 *            name of link
	 * @param url
	 *            URL of link
	 * @return result
	 */
	public LinkPartition insertLinkPartititon(Point point, String name,
			String url) {
		LinkPartition partition = new LinkPartition(doc, point.x, name
				.length(), url, linkPrototypePartition);
		layer.replacePartition(point.x, point.y, name, partition);
		return partition;
	}

	/**
	 * Used to manage control states and get information about selected text
	 * fragment
	 * 
	 * @param offset
	 *            Text offset
	 * @param length
	 *            Text align
	 * @return Selection state
	 */
	public RichSelectionState defineSumStylePartition(int offset, int length) {
		List<BasePartition> parts = layer.getPartitions();
		BasePartition startPartition = (BasePartition) layer
				.getPartitionAtOffset(offset);
		if (startPartition == null)
			startPartition = (BasePartition) layer
					.getPartitionAtOffset(offset - 1);
		if (startPartition == null) {
			startPartition = layer.getStorage().newPartition();
		}
		if (length == 0) {
			if (offset == startPartition.getOffset() && offset > 0) {
				BasePartition basePartition = (BasePartition) layer
						.getPartitions().get(startPartition.getIndex() - 1);
				return new RichSelectionState(Collections
						.singletonList(basePartition), basePartition);
			} else
				return new RichSelectionState(Collections
						.singletonList(startPartition), startPartition);
		}
		BasePartition endPartition = (BasePartition) layer
				.getPartitionAtOffset(offset + length - 1);
		List<BasePartition> ps = new ArrayList<BasePartition>();
		while (startPartition instanceof ObjectPartition
				&& startPartition != endPartition) {
			offset++;
			length--;
			startPartition = (BasePartition) layer.getPartitionAtOffset(offset);
		}
		if (startPartition == endPartition) {
			return new RichSelectionState(Collections
					.singletonList(startPartition), startPartition);
		}
		// String sumFontName = startPartition.getFontDataName();
		BasePartition res = PartitionFactory.createAsSampleStyle(
				startPartition, doc, 0, 0);
		int startIdx = layer.getPartitions().indexOf(startPartition);
		int endIdx = layer.getPartitions().indexOf(endPartition);
		for (int i = startIdx + 1; i <= endIdx; i++) {
			BasePartition partition = (BasePartition) parts.get(i);
			ps.add(partition);
			res = partition.getCommonStyle(res);
		}
		/*
		 * boolean isBold = startPartition.isBold(); boolean isItalic =
		 * startPartition.isItalic(); boolean isUnderlined =
		 * startPartition.isUnderlined(); boolean isStrikeThrough =
		 * startPartition.isStrikethrough(); RGB color =
		 * startPartition.getColorRGB(); RGB bgColor =
		 * startPartition.getColorRGB();
		 * 
		 * int startIdx = layer.getPartitions().indexOf(startPartition); int
		 * endIdx = layer.getPartitions().indexOf(endPartition); for (int i =
		 * startIdx + 1; i <= endIdx; i++) { BasePartition partition =
		 * (BasePartition) parts.get(i); ps.add(partition); if
		 * (!sumFontName.equals(partition.getFontDataName())) sumFontName = "";
		 * if (!(partition instanceof ObjectPartition)) { if
		 * (!partition.isBold()) isBold = isBold & partition.isBold(); if
		 * (!partition.isItalic()) isItalic = false; if
		 * (!partition.isUnderlined()) isUnderlined = false; if
		 * (!partition.isStrikethrough()) isStrikeThrough = false; } boolean b =
		 * color != null && partition.getColorRGB() != null &&
		 * color.equals(partition.getColorRGB()); if (!b) { color = null; } b =
		 * bgColor != null && partition.getBgColorRGB() != null &&
		 * bgColor.equals(partition.getBgColorRGB()); if (!b) { bgColor = null;
		 * } } if (bgColor == null) { bgColor = new RGB(255, 255, 255); }
		 * BasePartition res = new BasePartition(layer, 0, 0);
		 * res.setFontDataName(sumFontName); res.setBold(isBold);
		 * res.setItalic(isItalic); res.setUnderlined(isUnderlined);
		 * res.setStrikethrough(isStrikeThrough); res.setColorRGB(color);
		 * res.setBgColorRGB(bgColor);
		 */
		return new RichSelectionState(ps, res);
	}

	/**
	 * Used to define several lines align style
	 * 
	 * @param startLineNum
	 *            starting line index
	 * @param endLineNum
	 *            finishing line index
	 * @return one of align constant values, if all lines of interval have same
	 *         align, 0 otherwise
	 */
	public int defineSumAlignStyle(int startLineNum, int endLineNum) {
		int align = editor.getLineAlignment(startLineNum);
		boolean justify = editor.getLineJustify(startLineNum);
		for (int i = startLineNum + 1; i <= endLineNum; i++) {
			if (editor.getLineAlignment(i) != align
					|| editor.getLineJustify(i) != justify)
				return 0;
		}
		if (justify)
			return RichTextEditorConstants.FIT_ALIGN;
		return align;
	}

	/**
	 * Used to define several lines list style
	 * 
	 * @param startLineNum
	 *            starting line index
	 * @param endLineNum
	 *            finishing line index
	 * @return one of list constant values, if all lines of interval have same
	 *         list style, NONE_LIST otherwise
	 */
	public int defineSumListStyle(int startLineNum, int endLineNum) {
		org.eclipse.swt.custom.Bullet bullet = editor
				.getLineBullet(startLineNum);
		for (int i = startLineNum + 1; i <= endLineNum; i++) {
			if (editor.getLineBullet(i) != bullet
					&& !(editor.getLineBullet(i) == null && ((BasePartition) layer
							.getPartitionAtOffset(editor.getOffsetAtLine(i)))
							.requiresSingleLine()))
				return RichTextEditorConstants.NONE_LIST;
		}
		if (bullet != null) {
			if (bullet.type == ST.BULLET_DOT)
				return RichTextEditorConstants.BULLETED_LIST;
			if ((bullet.type & ST.BULLET_NUMBER) != 0)
				return RichTextEditorConstants.NUMBERED_LIST;
			if ((bullet.type & ST.BULLET_CUSTOM) != 0) // TODO Seems to be quite
			// ugly; Will crash
			// after one more adding
			{
				if (bullet.text.equals("a"))
					return RichTextEditorConstants.LETTERS_NUMBERED_LIST;
				if (bullet.text.equals("i"))
					return RichTextEditorConstants.ROMAN_NUMBERED_LIST;
			}
		}
		return RichTextEditorConstants.NONE_LIST;
	}

	IStyledText wrappedWidget = new IStyledText() {

		public void setTopPixel(int i) {
			editor.setTopPixel(i);
		}

		public void setStyleRanges(StyleRange[] styleRanges) {
			org.eclipse.swt.custom.StyleRange[] r = new org.eclipse.swt.custom.StyleRange[styleRanges.length];
			for (int a = 0; a < styleRanges.length; a++) {
				r[a] = MetaConverter.convertRange(styleRanges[a],
						LayerManager.this);
			}
			editor.setStyleRanges(r);
		}

		public void setLineBullet(int i, int j, BasicBullet bullet) {
			editor.setLineBullet(i, j, MetaConverter.convertBullet(bullet));
		}

		public BasicBullet getLineBullet(int a) {
			return MetaConverter.convertBulletBack(editor.getLineBullet(a),
					getBulletFactory());
		}

		public void setSelection(int oldOffset) {
			editor.setSelection(oldOffset);
		}

		public void setRedraw(boolean b) {
			editor.setRedraw(b);
		}

		public void setLineJustify(int i, int j, boolean b) {
			editor.setLineJustify(i, j, b);
		}

		public void setLineAlignment(int i, int j, int align) {
			editor.setLineAlignment(i, j, align);
		}

		public void setCaretOffset(int i) {
			editor.setCaretOffset(i);
		}

		public void redraw() {
			editor.redraw();
		}

		public int getTopPixel() {
			return editor.getTopPixel();
		}

		public Point getSize() {
			org.eclipse.swt.graphics.Point size = editor.getSize();
			return new Point(size.x, size.y);
		}

		public boolean getLineJustify(int lastLine) {
			return editor.getLineJustify(lastLine);
		}

		public int getLineHeight() {
			return editor.getLineHeight();
		}

		public int getLineCount() {
			return editor.getLineCount();
		}

		public int getLineAlignment(int a) {
			return editor.getLineAlignment(a);
		}

		public String getLine(int inheritAlignLineNum) {
			return editor.getLine(inheritAlignLineNum);
		}

		public int getCaretOffset() {
			return editor.getCaretOffset();
		}

		public int getLineAtOffset(int offset) {
			return editor.getLineAtOffset(offset);
		}
	};

	/**
	 * @return {@link StyledText} associated with LayerManager
	 */
	public IStyledText getTextWidget() {
		return wrappedWidget;
	}

	/**
	 * @return {@link StyledText} associated with LayerManager
	 */
	public StyledText getActualTextWidget() {
		return editor;
	}

	public void addNewImage(String filename, int offset, int replacementLength) {
		// check if key already exists
		IImage checkImage = imageManager.checkImage(filename);
		Image image2 = imageManager.mapImage(checkImage);
		if (image2 == null) {
			try {
				image2 = new Image(editor.getDisplay(), filename);
				filename = imageManager.registerImage(filename, image2, false);
			} catch (Exception e) {
				MessageDialog.openError(Display.getCurrent().getActiveShell(),
						"Error during attempt to insert image", e.getMessage());
				return;
			}
		}
		ImagePartition pa = new ImagePartition(doc, offset, 1, checkImage,
				filename);
		layer.replacePartition(offset, replacementLength, "?", pa);
	}

	/**
	 * Loads image from file and inserts it into text
	 * 
	 * @param filename
	 *            image file name
	 */
	public void addNewImage(String filename) {
		int offset = editor.getCaretOffset();
		addNewImage(filename, offset, 0);
	}

	/**
	 * Adds new inner StyledText control, representing inner region
	 */
	public void addNewRegion() {
		StyledText widget = new StyledText(editor, SWT.BORDER);
		addWidgetPartition(new CompositeEditorWrapper(widget));
	}

	public void addWidgetPartition(IRegionCompositeWrapper editorForContentType) {
		layer.setInheritPrevLineBullets(false);
		int offset = editor.getCaretOffset();
		final RegionPartition partition = new RegionPartition(doc, offset,
				REGION_STR.length(), editorForContentType.getMainObject());
		partition.setWrapper(editorForContentType); // For initing component
		// size listners
		// etc.
		addWidgetPartition(partition);
		layer.setInheritPrevLineBullets(true);
	}

	public void addWidgetPartition(String initialText, String contentType) {
		int offset = editor.getCaretOffset();
		RegionPartition regionPartition = new RegionPartition(doc, offset,
				REGION_STR.length(), contentType, initialText);
		addWidgetPartition(regionPartition);
	}

	public void addWidgetPartition(RegionPartition partition) {
		int offset = editor.getCaretOffset();
		char prevChar = 0;
		if (offset > 0 && offset < doc.getLength())
			prevChar = doc.get(offset - 1, 1).charAt(0);
		String additionalStr = null;
		BasePartition additionalPart = null;
		if (offset > 0 && prevChar != '\r' && prevChar != '\n') {
			additionalStr = "\n";
			additionalPart = new BasePartition(doc, offset, 1); // Add a new
			// line
			// first
			partition.setOffset(partition.getOffset() + 1);
			offset++;
		}
		if (useRegionControls)
		{	
			editor.setRedraw(false);
	
			if (additionalPart == null)
				layer.replacePartition(offset, 0, REGION_STR, partition);
			else {
				layer.replacePartitions(offset - additionalPart.getLength(), 0,
						additionalStr + REGION_STR, Arrays
								.asList(new BasePartition[] { additionalPart,
										partition }));
			}
			partition.setSize(editor.getClientArea().width - MARGIN * 2, partition
					.getInitialHeight()
					+ MARGIN);
			setStyleRangeByPartition(partition);
			innerWidgetRegistry.addRedrawedControl((Control) partition.getObject());
			if (insideRegionListener != null)
				((Control) partition.getObject())
						.addFocusListener(insideRegionListener);
			for (Iterator<IRegionCompositeWrapperListener> iterator = regionCompositeListeners
					.iterator(); iterator.hasNext();) {
				IRegionCompositeWrapperListener listener = (IRegionCompositeWrapperListener) iterator
						.next();
				partition.getWrapper().addRegionCompositeWrapperListener(listener);
			}
			int lineAtOffset = editor.getLineAtOffset(partition.getOffset());
			editor.setLineBullet(lineAtOffset, 1, null);
			editor.setRedraw(true);
			editor.redraw();
		}
		else
		{
			BasePartition replacePartition = new BasePartition(doc, partition.getOffset(), REGION_REPLACE_STR.length());
			if (additionalPart == null)
				layer.replacePartition(offset, 0, REGION_REPLACE_STR, replacePartition);
			else {
				layer.replacePartitions(offset - additionalPart.getLength(), 0,
						additionalStr + REGION_REPLACE_STR, Arrays
								.asList(new BasePartition[] { additionalPart,
										replacePartition }));
			}
		}
	}

	public RegionListener createRegionCompositeWrapperListener(
			RegionPartition partition) {
		return new RegionListener(partition);
	}

	// public void addNewSourceCodeRegion(String fileName)
	// {
	// /*TextEditor textEditor = new TextEditor(){
	//			
	// public boolean isEditable()
	// {
	// return true;
	// }
	// 
	// public IWorkbenchPartSite getSite()
	// {
	//				
	// return
	// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite();
	// }
	//			
	// };
	// Document document = new Document("System.out.println(\"preved!\");");
	//		
	// JavaTextTools tools= JavaPlugin.getDefault().getJavaTextTools();
	// IPreferenceStore store=
	// JavaPlugin.getDefault().getCombinedPreferenceStore();
	// JavaSourceViewer viewer= new JavaSourceViewer(editor, null, null, false,
	// SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL, store);
	// viewer.setDocument(document);
	// tools.setupJavaDocumentPartitioner(document,
	// IJavaPartitions.JAVA_PARTITIONING);
	//		
	// SimpleJavaSourceViewerConfiguration configuration= new
	// SimpleJavaSourceViewerConfiguration(tools.getColorManager(), store, null,
	// IJavaPartitions.JAVA_PARTITIONING, false);
	//
	// viewer.configure(configuration);*/
	//		
	// Composite editorForContentType =
	// ContentTypeEditorProvider.getEditorForContentType(fileName, editor);
	// addWidgetPartition(editorForContentType);
	// }

	/**
	 * Inserts new text breaking line (hr)
	 */
	public void addNewHR() {
		layer.setInheritPrevLineBullets(false);
		int offset = editor.getCaretOffset(); // Add a new line first
		/*
		 * BasePartition part = new BasePartition(layer, offset, 1);
		 * layer.replacePartitions(offset, 0, "\n", part); offset++;
		 */

		String hrStr = "\n?\n"; // And here add new HR partition
		HRPartition hrp = new HRPartition(doc, offset, hrStr.length());
		layer.replacePartition(offset, 0, hrStr, hrp);
		layer.setInheritPrevLineBullets(true);
	}

	/**
	 * Is it a hr line at offset?
	 * 
	 * @param offset
	 *            Offset to check
	 * @return true, if partition at that offset is HRPartition
	 */
	public boolean isHRLine(int offset) {
		if (layer.getPartitionAtOffset(offset) instanceof HRPartition)
			return true;
		return false;

	}

	/**
	 * responsible for painting "special" objects in editor window, like images
	 * or hr's
	 * 
	 * @param event
	 *            Paint Object Event
	 */
	public void paintObject(PaintObjectEvent event) {
		GC gc = event.gc;
		org.eclipse.swt.custom.StyleRange style = event.style;
		int start = style.start;
		IPartition partitionAtOffset = layer.getPartitionAtOffset(start);
		if (partitionAtOffset instanceof ImagePartition) {
			ImagePartition partition = (ImagePartition) partitionAtOffset;
			IImage image = partition.getImage();
			Image im = imageManager.mapImage(image);
			if (im == null) {
				return;
			}
			int x = event.x;
			int y = event.y + event.ascent - style.metrics.ascent;
			if (im != null) {
				Rectangle size = partition.calculateResizedBounds();
				gc.drawImage(im, 0, 0, im.getBounds().width, im.getBounds().height,x, y,size.width, size.height);
			} else {
				gc.drawText("?", x, y);
			}
		} else if (partitionAtOffset instanceof RegionPartition) {
			int leftMargin = ((AbstractModel) layer).getLineAttributeModel()
					.getLineIndent(
							editor.getLineAtOffset(partitionAtOffset
									.getOffset()))
					* indentSize;
			int rightMargin = ((AbstractModel) layer).getLineAttributeModel()
					.getRightIndent(
							editor.getLineAtOffset(partitionAtOffset
									.getOffset()))
					* indentSize;
			RegionPartition partition = (RegionPartition) partitionAtOffset;
			Point pt = partition.getSize();
			int x = event.x + 5;
			int y = event.y;// + event.ascent - 2 * pt.y / 3;
			int width = editor.getClientArea().width - MARGIN * 2 - leftMargin
					- rightMargin;
			int height = partition.getInitialHeight() + MARGIN;
			if (pt.x != width || pt.y != height) {
				partition.setSize(width, height);
				if (partition.getWrapper() instanceof IHasColumns)
					((IHasColumns)partition.getWrapper()).updateSizeOfColumns(width);
				setStyleRangeByPartition(partition);
			}
			partition.setLocation(x, y);
			innerWidgetRegistry.addRedrawedControl((Control) partition
					.getTopLevelObject());
		} else if (partitionAtOffset instanceof HRPartition) {
			gc.setLineWidth(hrWidth);
			if (((HRPartition) partitionAtOffset).getColor(this) != null) {
				com.onpositive.richtext.model.meta.Color color = ((HRPartition) partitionAtOffset)
						.getColor(this);
				Color clr = colorManager.getColor(MetaConverter
						.convertRGB((RGB) color));
				gc.setForeground(clr);
			}
			if (((HRPartition) partitionAtOffset).getBgColor(this) != null) {
				com.onpositive.richtext.model.meta.Color bgColor = ((HRPartition) partitionAtOffset)
						.getBgColor(this);
				Color color = colorManager.getColor(MetaConverter
						.convertRGB((RGB) bgColor));
				gc.setForeground(color);
			}
			gc.setLineStyle(SWT.LINE_SOLID);
			gc.drawLine(event.x, event.y,
					editor.getClientArea().width - MARGIN, event.y);

			/*
			 * BasePartition prevPartition, nextPartition; //TODO This doesn't
			 * worx well, having ugly artifacts int position = ((HRPartition)
			 * partitionAtOffset).getPosition(); if (position > 0) {
			 * prevPartition = layer.get(position - 1); if (!(prevPartition
			 * instanceof ObjectPartition))
			 * editor.redrawRange(prevPartition.getOffset
			 * (),prevPartition.getLength(),false); } if (position <
			 * layer.size() - 1) { nextPartition = layer.get(position + 1); if
			 * (!(nextPartition instanceof ObjectPartition))
			 * editor.redrawRange(nextPartition
			 * .getOffset(),nextPartition.getLength(),false);
			 * 
			 * }
			 */

		} else if (style.data != null
				&& style.data
						.equals(RichTextEditorConstants.DECORATION_SIGNATURE)) {
			int line = editor.getLineAtOffset(style.start);
			int lineStart = editor.getOffsetAtLine(line);
			int level = start - lineStart;

			int red = level % 2 * 255;
			int green = level / 2 % 2 * 255;
			int blue = level / 4 % 2 * 255;
			org.eclipse.swt.graphics.RGB color = new org.eclipse.swt.graphics.RGB(
					red, green, blue);
			if (editor.getBackground().getRGB().equals(color))
				color = editor.getForeground().getRGB();
			Color tmp = editor.getForeground();
			int oldStyle = gc.getLineStyle();
			int oldWidth = gc.getLineWidth();
			Color color2 = new Color(editor.getDisplay(), color);
			gc.setForeground(color2);
			gc.setLineStyle(SWT.LINE_SOLID);
			gc.setLineWidth(3);
			gc.drawLine(event.x, event.y, event.x, event.y
					+ editor.getLineHeight(style.start));
			gc.setForeground(tmp);
			gc.setLineStyle(oldStyle);
			gc.setLineWidth(oldWidth);
			color2.dispose();
		}
	}

	/**
	 * Opens HTML file and loads it's contents into editor
	 * 
	 * @param filename
	 *            HTML file name
	 */
	public void openHTMLFile(String filename) {
		ISimpleRichTextModel loadedModel = null;
		try {
			loadedModel = getTextHTMLLoader().parse(
					new FileInputStream(filename));
		} catch (Exception e) {
			MessageDialog.openError(editor.getShell(), "Unable to open file", e
					.getLocalizedMessage());
			return;
		}
		int oldLength;
		editor.setRedraw(false);
		SetContentChange change = new SetContentChange(loadedModel, 0);
		oldLength = doc.getLength();
		/*
		 * try { layer.setIgnoreDocumentEvents(true); oldLength =
		 * doc.getLength(); doc.set(loadedModel.getText());
		 * layer.setIgnoreDocumentEvents(false); List<BasePartition> partitions
		 * = loadedModel.getPartitions();
		 * layer.storage.setPartitions(partitions); int lineCount =
		 * loadedModel.getLineCount(); for (int i = 0; i < lineCount; i++) { int
		 * align = loadedModel.getAlign(i); if (align !=
		 * RichTextEditorConstants.FIT_ALIGN) editor.setLineAlignment(i, 1,
		 * align); else { editor.setLineAlignment(i, 1,
		 * RichTextEditorConstants.LEFT_ALIGN); editor.setLineJustify(i, 1,
		 * true); } Bullet bullet = (Bullet) loadedModel.getBullet(i);
		 * editor.setLineBullet(i, 1, bullet); } layerChanged(new
		 * LayerEvent(layer, new ArrayList<IPartition>( partitions))); } finally
		 * { editor.setRedraw(true); }
		 */
		layer.execute(change);
		editor.setRedraw(true);
		fireRichDocumentEvent(new DocumentEvent(doc, 0, oldLength, loadedModel
				.getText()), new PartitionDelta(layer.getStorage()));
	}

	/**
	 * @param selectedRange
	 *            selected range to replace
	 * @param model
	 *            -model to paste
	 * @throws BadLocationException
	 *             is something goes wrong
	 */
	public void paste(final Point selectedRange,
			final ISimpleRichTextModel model) throws Exception {
		Change change = new PasteChange(model, selectedRange);
		layer.execute(change);
	}

	/**
	 * Used to paste HTML contained in the clipboard
	 * 
	 * @param contents
	 *            HTML string
	 * @param offset
	 *            offset, where to paste
	 */
	public void pasteHTML(String contents, int offset) {
		editor.setRedraw(false);
		try {
			ISimpleRichTextModel model = getTextHTMLLoader().parse(contents);
			paste(offset, model);
		} catch (Exception e) {
			throw new RuntimeException();
		} finally {
			editor.setRedraw(true);
		}
	}

	/**
	 * @param contents
	 *            content
	 * @param selectedRange
	 *            selected range
	 */
	public void pasteHTML(String contents, Point selectedRange) {
		editor.setRedraw(false);
		try {
			ISimpleRichTextModel model = getTextHTMLLoader().parse(contents);
			paste(selectedRange, model);
		} catch (Exception e) {
			throw new RuntimeException();
		} finally {
			editor.setRedraw(true);
		}
	}

	/**
	 * Used to paste some ISimpleRichTextModel contents
	 * 
	 * @param offset
	 *            where to paste
	 * @param model
	 *            model containing, what to paste
	 * @throws BadLocationException
	 *             never thrown normally
	 */
	public void paste(final int offset, final ISimpleRichTextModel model)
			throws Exception {
		Change change = new PasteChange(model, offset);
		layer.execute(change);
	}

	/**
	 * Used to paste some ISimpleRichTextModel contents
	 * 
	 * @param model
	 *            model containing, what to paste
	 * @throws BadLocationException
	 *             never thrown normally
	 */
	public void set(final ISimpleRichTextModel model) throws Exception {
		Change change = new SetContentChange(model, getTextWidget()
				.getCaretOffset());
		layer.execute(change);
	}

	/**
	 * Used to paste some ISimpleRichTextModel contents
	 * 
	 * @param input
	 *            with RichDocument containing information to paste
	 * @throws BadLocationException
	 *             never thrown normally
	 */
	public void set(RichDocument input) {
		editor.setRedraw(false);
		Change change = new SetContentChange(input, getTextWidget()
				.getCaretOffset());
		layer.execute(change);
		editor.setRedraw(true);
	}

	/**
	 * Used for content serializing into HTML file
	 * 
	 * @param fileName
	 *            fileName, where to serialize
	 */
	public void serializeToFile(String fileName) {
		TextSerializer serializer = getTextSerializer();
		try {
			PrintWriter pw = new PrintWriter(fileName);
			serializer.serializeAll(pw);
			pw.close();
		} catch (Exception e) {
			MessageDialog.openError(editor.getShell(),
					"Error during HTML serialization", e.getMessage());
		}
	}

	/**
	 * Used for content serializing into single string
	 * 
	 * @return HTML string
	 */
	public String getSerializedString() {
		TextSerializer serializer = getTextSerializer();
		return serializer.serializeAllToStr();
	}

	/**
	 * Used for content serializing into single string
	 * 
	 * @return HTML string
	 */
	public String getSerializedHTMLString() {
		TextSerializer serializer = new HTMLSerializer(this,
				new StyledTextLineInformationProvider(this));
		return serializer.serializeAllToStr();
	}

	protected TextSerializer getTextSerializer() {
		if (serializerFactory != null) {
			return serializerFactory.getNewSerializer(this);
		}
		TextSerializer serializer = new HTMLSerializer(this,
				new StyledTextLineInformationProvider(this));
		return serializer;
	}

	protected IHTMLLoader getTextHTMLLoader() {
		if (loaderFactory == null) {
			return new DefaultHTMLLoader(this);
		}
		return loaderFactory.getHTMLLoader(this);
	}

	/**
	 * Serializes selected text interval into HTML
	 * 
	 * @return String with HTML
	 */
	public String getSelectedHTML() {
		TextSerializer serializer = getTextSerializer();

		Point selection = MetaConverter.convertBack(editor.getSelection());
		int start = editor.getLineAtOffset(selection.x);
		int end = editor.getLineAtOffset(selection.y);
		boolean semiLine = (start != end);
		((TextSerializer) serializer).setAppendParagraphs(semiLine);
		if (selection.y - selection.x > 0) {
			try {
				String serialized = serializer.serializeToStr(selection);
				((TextSerializer) serializer).setAppendParagraphs(true);
				return serialized;
			} catch (Exception e) {

				Logger.log(e);
			}
		}
		return null;
	}

	public String getContentsHTML() {
		return getTextSerializer().serializeAllToStr();
	}

	/**
	 * @return {@link ImageManager} of this {@link LayerManager}
	 */
	public IImageManager getImageManager() {
		return imageManager;
	}

	/**
	 * @param documentListener
	 *            new documentListener to add
	 */
	public void addRichDocumentListener(IRichDocumentListener documentListener) {
		richDocumentlisteners.add(documentListener);
	}

	/**
	 * @param documentListener
	 *            documentListener to remove
	 */
	public void removeRichDocumentListener(
			IRichDocumentListener documentListener) {
		richDocumentlisteners.remove(documentListener);
	}

	/**
	 * Method for event handling
	 * 
	 * @param event
	 *            DocumentEvent instance
	 */
	public void fireDocumentGoingToChange(DocumentEvent event) {
		for (IRichDocumentListener l : richDocumentlisteners) {
			l.documentAboutToBeChanged(event);
		}
	}

	/**
	 * Method for event handling
	 * 
	 * @param event
	 *            DocumentEvent instance
	 * @param processChange
	 *            change to perform
	 */
	public void fireRichDocumentEvent(DocumentEvent event,
			PartitionDelta processChange) {
		RichDocumentChange change = new RichDocumentChange(processChange);
		for (IRichDocumentListener l : richDocumentlisteners) {
			l.documentChanged(event, change);
		}
	}

	/**
	 * @return {@link FontStyleManager} of this {@link LayerManager}
	 */
	public IFontStyleManager getFontStyleManager() {
		return fontStyleManager;
	}

	/**
	 * @return {@link ColorRegistry} of this {@link LayerManager}
	 */
	public ColorManager getColorRegistry() {
		return colorManager;
	}

	/**
	 * Used to shift caret, if it's situated in "wrong" place, like hr line
	 * 
	 * @param keyCode
	 *            Key pressed code
	 */
	protected void validateCaretPos(int keyCode) {
		int offset = getTextWidget().getCaretOffset();
		BasePartition curPartition = (BasePartition) getLayer()
				.getPartitionAtOffset(offset);
		if (curPartition != null && curPartition.requiresSingleLine()
				&& !(curPartition instanceof RegionPartition)) {
			if (keyCode == SWT.ARROW_UP || keyCode == SWT.ARROW_LEFT)
				if (curPartition.getText().startsWith("\n")
						|| curPartition.getText().startsWith("\r")) // Because
					// special
					// partitions
					// can look
					// like
					// \n?\n (as
					// one
					// partition),
					// but also
					// can be
					// only ?\n,
					// with the
					// simple
					// text \n
					// before
					getTextWidget()
							.setCaretOffset(curPartition.getOffset() - 1);
				// else
				// {
				// //searchSuitableOffsetBefore(curPartition.getOffset() - 1);
				// }
				else {
					char charAt = 'a'; // Because 'a' is'nt a whitespace char
					try {
						charAt = doc.get(offset, 1).charAt(0);
					} catch (Exception e) {
					}
					if (curPartition.getOffset() != offset
							|| !Character.isWhitespace(charAt))
						getTextWidget().setCaretOffset(
								curPartition.getOffset()
										+ curPartition.getLength());
				}
		}
	}

	protected int searchSuitableOffsetBefore(int offset) {
		while (offset > 0) {
			BasePartition curPartition = (BasePartition) getLayer()
					.getPartitionAtOffset(offset);
			if (curPartition != null && curPartition.requiresSingleLine()) {
				if (curPartition.getText().startsWith("\n")
						|| curPartition.getText().startsWith("\r"))
					return curPartition.getOffset();
				else if (offset != curPartition.getOffset())
					offset = curPartition.getOffset();
				else
					offset--;

			} else
				return offset;
		}
		return -1;
	}

	/**
	 * @return document associated with this {@link LayerManager}
	 */
	public ITextDocument getDocument() {
		return doc;
	}

	/**
	 * @return dummy {@link BasePartition}, which have a style same as all
	 *         {@link LinkPartition}s must have
	 */
	public BasePartition getLinkPrototype() {
		return linkPrototypePartition;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.onpositive.richtexteditor.model.FontStylesChangeListener#stylesChanged(java.util.ArrayList)
	 */
	public void stylesChanged(ArrayList<FontStyle> changedStyles) {
		LayerEvent event = new LayerEvent(layer);
		List<BasePartition> partitions = layer.getStorage().getPartitions();
		for (Iterator<FontStyle> iterator = changedStyles.iterator(); iterator
				.hasNext();) {
			FontStyle fontStyle = (FontStyle) iterator.next();
			for (Iterator<BasePartition> iterator2 = partitions.iterator(); iterator2
					.hasNext();) {
				BasePartition partition = (BasePartition) iterator2.next();
				// partition.setRefreshVisibleState(false);
				if (partition.getFontDataName().equals(
						fontStyle.getFontDataName())) {
					fontStyle.applyStyle(partition);
					event.addChangedPartition(partition);
				}
				// partition.setRefreshVisibleState(true);
			}
		}
		layerChanged(new LayerEvent(layer, (Collection) partitions));

	}

	/**
	 * True, if some style is in use by some partition
	 * 
	 * @param style
	 *            Style to check
	 * @return True, if style is in use by some partition, false otherwise
	 */
	public boolean isStyleUsed(FontStyle style) {
		for (int i = 0; i < layer.getStorage().getPartitions().size(); i++) {
			if (layer.getStorage().getPartitions().get(i).getFontDataName()
					.equals(style.getFontDataName()))
				return true;
		}
		return false;
	}

	/**
	 * Used to remove specified style from all partitions and replace it by
	 * default style
	 * 
	 * @param style
	 *            Styl to remove
	 */
	public void removeStyleFromAllPartitions(FontStyle style) {
		LayerEvent event = new LayerEvent(layer);
		for (int i = 0; i < layer.getStorage().getPartitions().size(); i++) {
			if (layer.getStorage().getPartitions().get(i).getFontDataName()
					.equals(style.getFontDataName())) {
				fontStyleManager.getDefaultStyle().setStyle(
						layer.getStorage().getPartitions().get(i));
				event.addChangedPartition(layer.getStorage().getPartitions()
						.get(i));
			}
		}
		layerChanged(event);
	}

	public void setRefreshVisibleState(boolean refresh) {
		editor.setRedraw(refresh);
	}

	/**
	 * It's a stub, intended to be overrided in subclasses
	 * 
	 * @param offset
	 * @param item
	 */
	public void addNewlinesIfNeeded(int offset, String selectedFontName) {

	}

	public int getLineDelimiterLength() {
		// TODO Auto-generated method stub
		return 2;
	}

	public IRegionCompositeWrapper getWrapperForContentType(String contentType,
			AbstractLayerManager manager, RegionPartition partition) {
		return new CompositeEditorWrapper(new StyledText(editor, SWT.BORDER));
	}

	public int getIndentSize() {
		if (indentSize > 0)
			return indentSize;
		if (Display.getCurrent() != null) {
			GC gc = new GC(editor);
			indentSize = gc.getCharWidth('a')
					* RichTextEditorConstants.TAB_WIDTH;
			gc.dispose();
		} else {
			editor.getDisplay().syncExec(new Runnable() {

				public void run() {
					getIndentSize();
				}
			});
		}
		return indentSize;
	}

	protected void setStyleRangeByPartition(BasePartition partition) {
		editor.setStyleRange(MetaConverter.convertRange(partition
				.getStyleRange(LayerManager.this), LayerManager.this));
	}

	public void setStyleRangesByPartition(BasePartition partition) {
		setStyleRangeByPartition(partition);
	}

	protected void setStyleRangesByPartitionList(
			ArrayList<BasePartition> partitions) {
		int size = partitions.size();
		if (size == 0)
			return;
		StyleRange range = partitions.get(0).getStyleRange(this);
		if (size > 1) {
			range.length = partitions.get(size - 1).getOffset()
					+ partitions.get(size - 1).getLength() - range.start;
		}
		List<StyleRange> ranges = decorateRanges(Collections
				.singletonList(range));
		Display defaultDisplay = Display.getDefault();
		for (Iterator<StyleRange> iterator = ranges.iterator(); iterator
				.hasNext();) {
			final StyleRange styleRange = (StyleRange) iterator.next();
			if (styleRange.length <= 0) {
				continue;
			}
			defaultDisplay.syncExec(new Runnable() {

				public void run() {
					editor.setStyleRange(MetaConverter.convertRange(styleRange,
							LayerManager.this));
				}
			});
		}
		defaultDisplay.syncExec(new Runnable() {

			public void run() {
				editor.redraw();
			}
		});
	}

	protected List<StyleRange> decorateRanges(List<StyleRange> rangesList) {
		if (rangesList != null) {
			for (Iterator<IRichtextDecorator> iterator = rangeDecorators
					.iterator(); iterator.hasNext();) {
				IRichtextDecorator decorator = (IRichtextDecorator) iterator
						.next();
				rangesList = decorator.decorateStyleRange(rangesList, doc);
			}
		}
		return rangesList;
	}

	/**
	 * Used for getting bullets list from some other thread using syncExec
	 * 
	 * @param list
	 *            List to put bullet info into
	 */
	public void getBulletsList(List<BasicBullet> list) {

		// ((ExtendedStyledText)editor).getBulletList(list);

	}

	public void setScale(float f) {
		try {
			Method declaredMethod = editor.getClass().getDeclaredMethod(
					"setScale", new Class[] { double.class });
			declaredMethod.invoke(editor, (double) f);
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof InvocationTargetException
					|| e instanceof NoSuchMethodException)
				throw new UnsupportedOperationException("Not supported");
			else
				e.printStackTrace();
		}

	}

	public void addRichtextDecorator(IRichtextDecorator decorator) {
		rangeDecorators.add(decorator);
	}

	public void removeRichtextDecorator(IRichtextDecorator decorator) {
		rangeDecorators.remove(decorator);
	}

	public RichtextCommandCreator getCommandCreator() {
		return commandCreator;
	}

	public void setCommandCreator(RichtextCommandCreator commandCreator) {
		this.commandCreator = commandCreator;
	}

	/**
	 * @return Returns the caret position relative to the start of the text.
	 */
	public int getSelectionOffset() {
		return editor.getCaretOffset();
	}

	/**
	 * @return the insideRegionListener
	 */
	public FocusListener getInsideRegionListener() {
		return insideRegionListener;
	}

	/**
	 * @param insideRegionListener
	 *            the insideRegionListener to set
	 */
	public void setInsideRegionListener(FocusListener insideRegionListener) {
		this.insideRegionListener = insideRegionListener;
	}

	protected void setRegionPartitionProps(BasePartition part) {
		final RegionPartition partition = (RegionPartition) part;
		if (partition.getWrapper() == null)
			partition.createWrapper(LayerManager.this);
		for (Iterator<IRegionCompositeWrapperListener> iterator = regionCompositeListeners
				.iterator(); iterator.hasNext();) {
			IRegionCompositeWrapperListener listener = (IRegionCompositeWrapperListener) iterator
					.next();
			partition.getWrapper().addRegionCompositeWrapperListener(listener);
		}
		if (insideRegionListener != null)
		{
			Control control = (Control) partition.getObject();
			if (control!=null)
			{
				control.addFocusListener(insideRegionListener);
			}
		}
		partition.getWrapper().addRegionCompositeWrapperListener(
				new RegionListener(partition));
		partition.setSize(editor.getClientArea().width - MARGIN * 2, partition
				.getInitialHeight()
				+ MARGIN);
		setStyleRangeByPartition(partition);
	}

	/**
	 * This listener will be added to all regions being added to this
	 * {@link LayerManager}'s document Currently, all listeners should be added
	 * during the initialization, before opening document/adding regions to it
	 * If added later, listener should be added only for regions being added
	 * after adding this listener Listeners is used to handle different events
	 * produced by in-wrapper controls, like text or line count change etc.
	 * 
	 * @param listener
	 *            {@link IRegionCompositeWrapperListener} to add
	 */
	public void addRegionCompositeWrapperListener(
			IRegionCompositeWrapperListener listener) {
		regionCompositeListeners.add(listener);
	}

	/**
	 * Listeners is used to handle different events produced by in-wrapper
	 * controls, like text or line count change etc.
	 * 
	 * @param listener
	 *            {@link IRegionCompositeWrapperListener} to remove
	 */
	public void removeRegionCompositeWrapperListener(
			IRegionCompositeWrapperListener listener) {
		regionCompositeListeners.remove(listener);
	}

	public int getCaretOffset() {
		return 0;
	}

	
	public ILineAttributeModel createModel() {
		// TODO Auto-generated method stub
		return new StyledTextLineAttributeModel(this);
	}

	
	public void syncExec(Runnable runnable) {
		editor.getDisplay().syncExec(runnable);
	}

	public BulletFactory getBulletFactory() {
		return factory;
	}

}
