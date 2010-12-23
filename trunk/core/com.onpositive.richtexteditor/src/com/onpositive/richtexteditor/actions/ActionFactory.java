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
package com.onpositive.richtexteditor.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.FontStyle;
import com.onpositive.richtext.model.IRichDocumentListener;
import com.onpositive.richtext.model.LinkPartition;
import com.onpositive.richtext.model.RichDocumentChange;
import com.onpositive.richtext.model.meta.DocumentEvent;
import com.onpositive.richtext.model.meta.Point;
import com.onpositive.richtexteditor.dialogs.FontConfigurationDialog;
import com.onpositive.richtexteditor.dialogs.HyperlinkDialog;
import com.onpositive.richtexteditor.model.FontStylesChangeListener;
import com.onpositive.richtexteditor.model.RichSelectionState;
import com.onpositive.richtexteditor.model.RichTextEditorConstants;
import com.onpositive.richtexteditor.model.changes.Change;
import com.onpositive.richtexteditor.model.resources.FontStyleData;
import com.onpositive.richtexteditor.model.resources.FontStyleManager;
import com.onpositive.richtexteditor.model.resources.LayerManager;
import com.onpositive.richtexteditor.util.MetaConverter;

/**
 * @author 32kda & kor Class, which manages interface actions and their states
 */
public class ActionFactory implements ISelectionChangedListener,
		IRichDocumentListener, IActionListProvider {

	public final class StyleCombo extends CCombo {
		public StyleCombo(Composite parent, int style) {
			super(parent, style);
		}
		protected void checkSubclass () 
		{
			
		}

		
		public boolean setFocus() {
			return super.setFocus();
		}
		
		
		public boolean forceFocus() {
			return super.forceFocus();
		}
		
		
		public void select(int index) {		
			super.select(index);
		}
		public void doStyleAction(){
			if (combo.getSelectionIndex() > -1) {
				if (viewer.getSelectedRange().y > 0) {
					manager.changeFontCommand(
							combo.getItem(combo
									.getSelectionIndex()),
							viewer.getSelectedRange().x, viewer
									.getSelectedRange().y);
					viewer.getTextWidget().setFocus();
				} else {
					setCurrentBasePartitionState();
					viewer.getTextWidget().setFocus();
				}
			}
		}
	}

	private final class AlignAction extends Action {

		private int style;

		public AlignAction(int style) {
			super("", Action.AS_RADIO_BUTTON);
			this.style = style;
		}

		public void run() {
			disableAllOther();
			org.eclipse.swt.graphics.Point selectedRange = viewer.getSelectedRange();
			manager.setIntervalAlign(selectedRange.x, selectedRange.y, style);
			// setLineIdentation(viewer.getSelectedRange().x,
			// viewer.getSelectedRange().y);
		}

		protected void disableAllOther() {
			if (centerAlignAction != null && centerAlignAction != this)
				centerAlignAction.setChecked(false);
			if (leftAlignAction != null && leftAlignAction != this)
				leftAlignAction.setChecked(false);
			if (rightAlignAction != null && rightAlignAction != this)
				rightAlignAction.setChecked(false);
			if (fillAlignAction != null && fillAlignAction != this)
				fillAlignAction.setChecked(false);
		}
	}

	private final class ListAction extends Action {

		protected int style;

		public ListAction(int style) {
			super("", Action.AS_CHECK_BOX);
			this.style = style;
		}

		public void run() {
			disableAllOtherListButtons();
			if (isChecked()) {
				manager.setIntervalList(viewer.getSelectedRange().x, viewer
						.getSelectedRange().y, style);
				enableProhibitedAligns(false);
			} else {
				manager.setIntervalList(viewer.getSelectedRange().x, viewer
						.getSelectedRange().y,
						RichTextEditorConstants.NONE_LIST);
				enableProhibitedAligns(true);
			}
		}

		protected void disableAllOtherListButtons() {
			if (bulletedListAction != null && bulletedListAction != this)
				bulletedListAction.setChecked(false);
			if (numberedListAction != null && numberedListAction != this)
				numberedListAction.setChecked(false);
		}
	}

	protected static final String FULL_JUSTIFY_IMAGE = "full-justify";

	protected static final String RIGHT_JUSTIFY_IMAGE = "right-justify";

	protected static final String CENTER_JUSTIFY_IMAGE = "center-justify";

	protected static final String LEFT_JUSTIFY_IMAGE = "left-justify";

	protected static final String LINK_IMAGE = "link";

	protected static final String UNDERLINE_IMAGE = "underline";

	protected static final String ITALIC_IMAGE = "italic";

	protected static final String BOLD_IMAGE = "bold";

	protected static final String BULLETS_IMAGE = "bullets";

	protected static final String NUMBERS_IMAGE = "numbers";

	protected static final String LETTERS_LIST_IMAGE = "letters";

	protected static final String ROMAN_LIST_IMAGE = "roman";

	protected static final String STRIKE_IMAGE = "strike";

	protected static final String ADD_IMAGE_IMAGE = "image";

	protected static final String ADD_REGION_IMAGE = "region";

	protected static final String ADD_JAVA_REGION_IMAGE = "java_region";

	protected static final String OPEN_IMAGE_IMAGE = "open";

	protected static final String ADD_HR_IMAGE = "hr";

	protected static final String CUSTOMIZE_FONT_STYLES_IMAGE = "customize_font";

	protected static final String SUB_IMAGE = "sub";

	protected static final String SUP_IMAGE = "sup";

	protected static final String INCREASE_INDENT_IMAGE = "indent";

	protected static final String DECREASE_INDENT_IMAGE = "outdent";

	protected static final String INCREASE_RIGHT_INDENT_IMAGE = "right_indent";

	protected static final String DECREASE_RIGHT_INDENT_IMAGE = "right_outdent";

	protected static final String SINGLE_SPACING = "single_spacing";

	protected static final String ONE_AND_HALF_SPACING = "one_and_half_spacing";

	protected static final String DOUBLE_SPACING = "double_spacing";

	protected static final String TABLE = "table";

	private IAction boldAction;

	private IAction italicAction;

	private CCombo combo;

	protected LayerManager manager;

	protected ITextViewer viewer;

	public ITextViewer getViewer() {
		return viewer;
	}

	public void setViewer(ITextViewer viewer) {
		this.viewer = viewer;
	}

	protected ToolBar toolBar;

	private IAction underlineAction;

	private IAction strikeThroughAction;

	private IAction linkAction;

	private IAction leftAlignAction;

	private AlignAction rightAlignAction;

	private IAction centerAlignAction;

	private IAction fillAlignAction;

	private IAction bulletedListAction;

	private IAction numberedListAction;

	private IAction lettersNumberedListAction;

	private IAction romanNumberedListAction;

	private IAction foregroundColorAction;

	private IAction backgroundColorAction;

	protected IAction addImageAction;

	protected IAction addRegionAction;

	private IAction openFileAction;

	private IAction addHRAction;

	private IAction customizeFontStyleAction;

	private IAction supAction;

	private IAction subAction;

	private IAction increaseIndentAction;

	private IAction decreaseIndentAction;

	private IAction increaseFirstLineIndentAction;

	private IAction decreaseFirstLineIndentAction;

	private IAction increaseLineSpacingAction;

	private IAction decreaseLineSpacingAction;

	private IAction increaseRightIndentAction;

	private IAction decreaseRightIndentAction;

	private IAction singleSpacingAction;

	private IAction oneAndHalfSpacingAction;

	private IAction doubleSpacingAction;

	
	/**
	 * @return Foreground Color Action
	 */
	public IAction getForegroundColorAction() {
		if (foregroundColorAction == null)
			foregroundColorAction = new ForeGroundColorAction(manager);
		return foregroundColorAction;
	}

	/**
	 * @return Backround Color Action
	 */
	public IAction getBackgroundColorAction() {
		if (backgroundColorAction == null)
			backgroundColorAction = new BackGroundColorAction(manager);
		return backgroundColorAction;
	}

	private ControlContribution controlContribution;

	protected static ImageRegistry images = new ImageRegistry();

	protected ArrayList<IContributionItem> resList;
	protected ArrayList<EnabledAction> enablementList;

	{
		images.put(ITALIC_IMAGE, ImageDescriptor
				.createFromURL(ActionFactory.class
						.getResource("text_italic.png")));
		images.put(BOLD_IMAGE,
				ImageDescriptor.createFromURL(ActionFactory.class
						.getResource("text_bold.png")));
		images.put(UNDERLINE_IMAGE, ImageDescriptor
				.createFromURL(ActionFactory.class
						.getResource("text_underline.png")));
		images.put(LINK_IMAGE, ImageDescriptor
				.createFromURL(ActionFactory.class.getResource("link.png")));
		images.put(LEFT_JUSTIFY_IMAGE, ImageDescriptor
				.createFromURL(ActionFactory.class
						.getResource("JustifyLeft.gif")));
		images.put(CENTER_JUSTIFY_IMAGE, ImageDescriptor
				.createFromURL(ActionFactory.class
						.getResource("JustifyCenter.gif")));
		images.put(RIGHT_JUSTIFY_IMAGE, ImageDescriptor
				.createFromURL(ActionFactory.class
						.getResource("JustifyRight.gif")));
		images.put(FULL_JUSTIFY_IMAGE, ImageDescriptor
				.createFromURL(ActionFactory.class
						.getResource("JustifyFull.gif")));
		images.put(BULLETS_IMAGE, ImageDescriptor
				.createFromURL(ActionFactory.class
						.getResource("text_list_bullets.png")));
		images.put(NUMBERS_IMAGE, ImageDescriptor
				.createFromURL(ActionFactory.class
						.getResource("text_list_numbers.png")));
		images.put(LETTERS_LIST_IMAGE, ImageDescriptor
				.createFromURL(ActionFactory.class
						.getResource("letter_list_numbers.png")));
		images.put(ROMAN_LIST_IMAGE, ImageDescriptor
				.createFromURL(ActionFactory.class
						.getResource("roman_list_numbers.png")));
		images.put(STRIKE_IMAGE, ImageDescriptor
				.createFromURL(ActionFactory.class
						.getResource("StrikeThrough.gif")));
		images.put(ADD_IMAGE_IMAGE, ImageDescriptor
				.createFromURL(ActionFactory.class.getResource("image.gif")));
		images.put(ADD_REGION_IMAGE, ImageDescriptor
				.createFromURL(ActionFactory.class
						.getResource("textarea_obj.gif")));
		images
				.put(ADD_JAVA_REGION_IMAGE, ImageDescriptor
						.createFromURL(ActionFactory.class
								.getResource("java_app.gif")));
		images.put(OPEN_IMAGE_IMAGE, ImageDescriptor
				.createFromURL(ActionFactory.class
						.getResource("html_tag_obj.png")));
		images.put(ADD_HR_IMAGE, ImageDescriptor
				.createFromURL(ActionFactory.class.getResource("hr.gif")));
		images.put(CUSTOMIZE_FONT_STYLES_IMAGE, ImageDescriptor
				.createFromURL(ActionFactory.class
						.getResource("alphab_sort_co.gif")));
		images.put(SUB_IMAGE, ImageDescriptor.createFromURL(ActionFactory.class
				.getResource("text_sub.png")));
		images.put(SUP_IMAGE, ImageDescriptor.createFromURL(ActionFactory.class
				.getResource("text_sup.png")));
		images.put(SUP_IMAGE, ImageDescriptor.createFromURL(ActionFactory.class
				.getResource("text_sup.png")));
		images.put(INCREASE_INDENT_IMAGE, ImageDescriptor
				.createFromURL(ActionFactory.class.getResource("Indent.gif")));
		images.put(DECREASE_INDENT_IMAGE, ImageDescriptor
				.createFromURL(ActionFactory.class.getResource("Outdent.gif")));
		images.put(INCREASE_RIGHT_INDENT_IMAGE, ImageDescriptor
				.createFromURL(ActionFactory.class
						.getResource("Right_Indent.GIF")));
		images.put(DECREASE_RIGHT_INDENT_IMAGE, ImageDescriptor
				.createFromURL(ActionFactory.class
						.getResource("Right_Outdent.GIF")));
		images.put(SINGLE_SPACING, ImageDescriptor
				.createFromURL(ActionFactory.class
						.getResource("single_spacing.GIF")));
		images.put(ONE_AND_HALF_SPACING, ImageDescriptor
				.createFromURL(ActionFactory.class
						.getResource("one_and_half_spacing.GIF")));
		images.put(DOUBLE_SPACING, ImageDescriptor
				.createFromURL(ActionFactory.class
						.getResource("double_spacing.GIF")));
		images.put(TABLE, ImageDescriptor.createFromURL(ActionFactory.class
				.getResource("table_obj.gif")));
	}

	/**
	 * Basic constructor
	 * 
	 * @param manager
	 *            LayerManager instance
	 * @param viewer
	 *            ITextViewer instance
	 */
	public ActionFactory(LayerManager manager, ITextViewer viewer) {
		this.manager = manager;
		this.viewer = viewer;
		FocusListener insideRegionListener = new FocusListener() {

			public void focusLost(FocusEvent e) {
				enableActionBar(true);
			}

			public void focusGained(FocusEvent e) {
				enableActionBar(false);
			}
		};
		manager.setInsideRegionListener(insideRegionListener);
		TextViewer tv = (TextViewer) viewer;
		tv.addPostSelectionChangedListener(this);
	}
	public void dispose(){
		((TextViewer)this.viewer).removePostSelectionChangedListener(this);
		manager.setInsideRegionListener(null);
	}

	protected void enableActionBar(boolean enabled) {
		if (toolBar != null) {
			toolBar.setEnabled(enabled);
			return;
		}
		List<IContributionItem> actionsList = getActionsList();
		for (Iterator iterator = actionsList.iterator(); iterator.hasNext();) {
			IContributionItem contributionItem = (IContributionItem) iterator
					.next();
			if (contributionItem instanceof ActionContributionItem)
				((ActionContributionItem) contributionItem).getAction()
						.setEnabled(enabled);
		}

	}

	/**
	 * This must be used <b>only</b> when we need to configure action bar in
	 * custom style, and later, with the help of <i>delayedConfigure</i> method,
	 * add all needed links.
	 */
	public ActionFactory() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Used for delayed basic links configuring - for customized ActionFactory
	 * 
	 * @param manager
	 *            LayerManager instance
	 * @param viewer
	 *            ITextViewer instance
	 */
	public void delayedConfigure(LayerManager manager, ITextViewer viewer) {
		this.manager = manager;
		this.viewer = viewer;
		TextViewer tv = (TextViewer) viewer;
		tv.addPostSelectionChangedListener(this);
	}

	/**
	 * @return LayerManager instance
	 */
	public LayerManager getManager() {
		return manager;
	}

	/**
	 * @param manager
	 *            LayerManager instance
	 */
	public void setManager(LayerManager manager) {
		this.manager = manager;
	}

	/**
	 * @return bold style action
	 */
	public IAction getBoldAction() {
		if (boldAction == null) {
			boldAction = new Action("", Action.AS_CHECK_BOX) {
				public void run() {
					if (viewer.getSelectedRange().y > 0)
						manager.boldCommand(viewer.getSelectedRange().x, viewer
								.getSelectedRange().y, isChecked());
					else
						setCurrentBasePartitionState();
				}
			};
			boldAction.setText("Bold");
			boldAction.setImageDescriptor(images.getDescriptor(BOLD_IMAGE));
		}
		return boldAction;
	}

	/**
	 * @return italic style action
	 */
	public IAction getItalicAction() {
		if (italicAction == null) {
			italicAction = new Action("", Action.AS_CHECK_BOX) {
				public void run() {
					if (viewer.getSelectedRange().y > 0)
						manager.italicCommand(viewer.getSelectedRange().x,
								viewer.getSelectedRange().y, isChecked());
					else
						setCurrentBasePartitionState();
				}
			};
			italicAction.setText("Italic");
			italicAction.setImageDescriptor(images.getDescriptor(ITALIC_IMAGE));
		}
		return italicAction;
	}

	/**
	 * @return underline style action
	 */
	public IAction getUnderlineAction() {
		if (underlineAction == null) {
			underlineAction = new Action("", Action.AS_CHECK_BOX) {
				public void run() {
					if (viewer.getSelectedRange().y > 0)
						manager.underlineCommand(viewer.getSelectedRange().x,
								viewer.getSelectedRange().y, isChecked());
					else
						setCurrentBasePartitionState();
				}
			};
			underlineAction.setText("Underline");
			underlineAction.setImageDescriptor(images
					.getDescriptor(UNDERLINE_IMAGE));
		}
		return underlineAction;
	}

	/**
	 * @return Sup style action
	 */
	public IAction getSupAction() {
		if (supAction == null) {
			supAction = new Action("", Action.AS_CHECK_BOX) {
				public void run() {
					if (viewer.getSelectedRange().y > 0)
						manager.supCommand(viewer.getSelectedRange().x, viewer
								.getSelectedRange().y, isChecked());
					else
						setCurrentBasePartitionState();
				}
			};
			supAction.setText("Superscript");
			supAction.setImageDescriptor(images.getDescriptor(SUP_IMAGE));
		}
		return supAction;
	}

	/**
	 * @return Sub style action
	 */
	public IAction getSubAction() {
		if (subAction == null) {
			subAction = new Action("", Action.AS_CHECK_BOX) {
				public void run() {
					if (viewer.getSelectedRange().y > 0)
						manager.subCommand(viewer.getSelectedRange().x, viewer
								.getSelectedRange().y, isChecked());
					else
						setCurrentBasePartitionState();
				}
			};
			subAction.setText("Subscript");
			subAction.setImageDescriptor(images.getDescriptor(SUB_IMAGE));
		}
		return subAction;
	}

	/**
	 * @return insert image action
	 */
	public IAction getAddImageAction() {
		if (addImageAction == null) {
			addImageAction = new Action() {
				public void run() {
					FileDialog dialog = new FileDialog(viewer.getTextWidget()
							.getShell());
					dialog.setFilterExtensions(new String[] { "*.gif", "*.jpg",
							"*.png", "*.bmp" });
					String filename = dialog.open();
					if (filename != null)
						manager.addNewImage(filename);
				}
			};
			addImageAction.setText("Image");
			addImageAction.setImageDescriptor(images
					.getDescriptor(ADD_IMAGE_IMAGE));
		}
		return addImageAction;
	}

	/**
	 * @return insert range action
	 */
	public IAction getAddRegionAction() {
		if (addRegionAction == null) {
			addRegionAction = new Action() {
				public void run() {
					manager.addNewRegion();
				}
			};
			addRegionAction.setText("Custom region");
			addRegionAction.setImageDescriptor(images
					.getDescriptor(ADD_REGION_IMAGE));
		}
		return addRegionAction;
	}

	// /**
	// * @return insert java source code region action
	// */
	// public IAction getAddJavaRegionAction()
	// {
	// if (addJavaRegionAction == null)
	// {
	// addJavaRegionAction = new Action()
	// {
	// public void run()
	// {
	//
	// FileDialog dialog = new FileDialog(viewer
	// .getTextWidget().getShell());
	// String filename = dialog.open();
	// if (filename != null) manager.addNewSourceCodeRegion(filename);
	// }
	// };
	// addJavaRegionAction.setText("insert source code region");
	// addJavaRegionAction.setImageDescriptor(images.getDescriptor(ADD_JAVA_REGION_IMAGE));
	// }
	// return addJavaRegionAction;
	// }

	/**
	 * @return insert hr line action
	 */
	public IAction getAddHRAction() {
		if (addHRAction == null) {
			addHRAction = new Action() {
				public void run() {
					manager.addNewHR();
				}
			};
			addHRAction.setText("Horizontal line");
			addHRAction.setImageDescriptor(images.getDescriptor(ADD_HR_IMAGE));
		}
		return addHRAction;
	}

	/**
	 * @return load contents from html file action
	 */
	public IAction getOpenFileAction() {
		if (openFileAction == null) {
			openFileAction = new Action() {
				public void run() {
					FileDialog dialog = new FileDialog(viewer.getTextWidget()
							.getShell());
					dialog
							.setFilterExtensions(new String[] { "*.html",
									"*.htm" });
					String filename = dialog.open();
					if (filename != null)
						manager.openHTMLFile(filename);
				}
			};
			openFileAction.setImageDescriptor(images
					.getDescriptor(OPEN_IMAGE_IMAGE));
			openFileAction.setText("Open");
		}
		return openFileAction;
	}

	/**
	 * @return insert new hyperlink action
	 */
	public IAction getNewLinkAction() {
		if (linkAction == null) {
			linkAction = new Action("Hyperlink", Action.AS_PUSH_BUTTON) {
				public void run() {

					//actionActivated = true;
					int offset = viewer.getSelectedRange().x;
					int length = viewer.getSelectedRange().y;
					String url = null;
					HyperlinkDialog hyperlinkDialog;
					String name = viewer.getTextWidget().getSelectionText();
					int selOffset = viewer.getTextWidget().getSelection().x;
					RichSelectionState state = manager.defineSumStylePartition(
							offset, length);
					if (state.isHasLinks()) {
						BasePartition sumPartition = state.getSumPartition();
						if (sumPartition instanceof LinkPartition) {
							LinkPartition linkPartition = (LinkPartition) sumPartition;
							if (!linkPartition.isUrlEditable()&&(linkPartition.getStyle()!=LinkPartition.INVALID_LINK))
								return;
							else
								url = linkPartition.getUrl();
						}
					}
					if (url == null)
						url = manager.getLayer().getSummaryUrl(
								viewer.getSelectedRange().x,
								viewer.getSelectedRange().y);
					boolean editName = (length > 0);
					Shell shell = viewer
							.getTextWidget().getShell();
					hyperlinkDialog = createHyperlinkDialog(editName, shell);
					hyperlinkDialog.create();
					if (editName)
						hyperlinkDialog.setName(name);
					hyperlinkDialog.setUrl(url);
					setChecked(false);
					int retCode = hyperlinkDialog.open();
					if (retCode == Window.OK) {
						if (!editName) {
							if (!url.equals(hyperlinkDialog.getUrl())) {
								Change change = manager.getCommandCreator()
										.createSetUrlChange(
												((LinkPartition) state
														.getAllPartitions()
														.get(0)),
												hyperlinkDialog.getUrl());
								manager.getLayer()
										.executeExternalChange(change);
							}
							return;
						}
						if (!name.equals(hyperlinkDialog.getName()))
							viewer.getTextWidget().setFocus();
						org.eclipse.swt.graphics.Point selectedRange = viewer.getSelectedRange();
						manager.insertLinkPartititon(new Point(selectedRange.x, selectedRange.y),
								hyperlinkDialog.getName(), hyperlinkDialog
										.getUrl());
						viewer.setSelectedRange(selOffset, hyperlinkDialog
								.getName().length()); // Selection fixing
						// neede for correct newlink adding
					}
				}
			};
			linkAction.setText("New Hyperlink");
			linkAction.setImageDescriptor(images.getDescriptor(LINK_IMAGE));
		}
		return linkAction;
	}

	/**
	 * @return insert new customize font style dialog action
	 */
	public IAction getCustomizeFontStyleAction() {
		if (customizeFontStyleAction == null) {
			customizeFontStyleAction = new Action("", Action.AS_PUSH_BUTTON) {
				public void run() {
					FontConfigurationDialog configurationDialog;
					configurationDialog = new FontConfigurationDialog(viewer
							.getTextWidget().getShell(),(FontStyleManager) manager
							.getFontStyleManager());
					configurationDialog.create();
					int retCode = configurationDialog.open();
					if (retCode == Window.OK) {
						FontStyleData data = configurationDialog.getData();
						if (data.getDeletedStyles().size() > 0) {
							ArrayList<FontStyle> usedStyles = new ArrayList<FontStyle>();
							for (Iterator<FontStyle> iterator = data
									.getDeletedStyles().iterator(); iterator
									.hasNext();) {
								FontStyle deletedStyle = (FontStyle) iterator
										.next();
								if (manager.isStyleUsed(deletedStyle))
									usedStyles.add(deletedStyle);
							}
							if (usedStyles.size() > 0) {
								String namesList = "";
								StringBuilder b = new StringBuilder();
								for (Iterator<FontStyle> iterator = usedStyles
										.iterator(); iterator.hasNext();) {
									b.append(((FontStyle) iterator.next())
											.getDisplayName());
									b.append(',');
								}
								namesList = b.toString();
								boolean res = MessageDialog
										.openQuestion(
												null,
												"Styles is in use",
												"Foolowing styles is in use: "
														+ namesList
														+ ". Delete them and replace with default style in text?");
								if (res) {
									for (Iterator<FontStyle> iterator = usedStyles
											.iterator(); iterator.hasNext();)
										manager
												.removeStyleFromAllPartitions((FontStyle) iterator
														.next());
								} else
									data.getFontStyles().addAll(usedStyles);
							}
						}
						ArrayList<FontStyle> changedStylesList = data
								.validateChangedStyles(manager
										.getFontStyleManager());
						if (changedStylesList.size() > 0
								|| data.getAddedStyles().size() > 0
								|| data.getDeletedStyles().size() > 0) {
							manager.getActualTextWidget().setFont(
									data.getResultFontRegistry().get(
											FontStyleManager.NORMAL_FONT_NAME));
							((FontStyleManager) manager.getFontStyleManager()).reinit(
									data.getFontStyles(),
									data.getResultFontRegistry(),
									changedStylesList);
							/*
							 * combo.setItems(manager.getFontStyleManager().getFontStyleDisplayNames
							 * ());combo.setText(manager.getFontStyleManager().
							 * getDefaultStyle().getDisplayName());
							 */
						}
					}
				}
			};
			manager.getFontStyleManager().addFontStyleChangeListener(
					new FontStylesChangeListener() {
						public void stylesChanged(
								ArrayList<FontStyle> changedStyles) {
							if (combo == null)
								return;
							combo.setItems(manager.getFontStyleManager()
									.getFontStyleDisplayNames());
							combo.setText(manager.getFontStyleManager()
									.getDefaultStyle().getDisplayName());
						}
					});

			customizeFontStyleAction.setText("Customize Font Styles");
			customizeFontStyleAction.setImageDescriptor(images
					.getDescriptor(CUSTOMIZE_FONT_STYLES_IMAGE));

		}

		return customizeFontStyleAction;
	}

	/**
	 * @return font style change action
	 */
	public IContributionItem getStyleContributionItem() {
		if (controlContribution == null) {
			controlContribution = new ControlContribution("style") {

				protected Control createControl(Composite parent) {
					combo = new StyleCombo(parent, SWT.READ_ONLY | SWT.BORDER);
					combo.setItems(manager.getFontStyleManager()
							.getFontStyleDisplayNames());
					combo.setText(manager.getFontStyleManager()
							.getDefaultStyle().getDisplayName());
					combo.addSelectionListener(new SelectionListener() {

						public void widgetDefaultSelected(SelectionEvent e) {

						}

						public void widgetSelected(SelectionEvent e) {
							if (combo.getSelectionIndex() > -1) {
								if (viewer.getSelectedRange().y > 0) {
									manager.changeFontCommand(
											combo.getItem(combo
													.getSelectionIndex()),
											viewer.getSelectedRange().x, viewer
													.getSelectedRange().y);
									viewer.getTextWidget().setFocus();
								} else {
									setCurrentBasePartitionState();
									viewer.getTextWidget().setFocus();
								}
							}
						}

					});
					return combo;
				}

			};
		}
		return controlContribution;
	}

	// alignment actions
	/**
	 * @return left text align action
	 */
	public IAction getAlignLeftAction() {
		if (leftAlignAction == null) {
			leftAlignAction = new AlignAction(SWT.LEFT);
			leftAlignAction.setText("Align Left");
			leftAlignAction.setImageDescriptor(images
					.getDescriptor(LEFT_JUSTIFY_IMAGE));
		}
		return leftAlignAction;
	}

	/**
	 * @return right text align action
	 */
	public IAction getAlignRightAction() {
		if (rightAlignAction == null) {
			rightAlignAction = new AlignAction(SWT.RIGHT);
			rightAlignAction.setText("Align Right");
			rightAlignAction.setImageDescriptor(images
					.getDescriptor(RIGHT_JUSTIFY_IMAGE));
		}
		return rightAlignAction;
	}

	/**
	 * @return center text align action
	 */
	public IAction getAlignCenterAction() {
		if (centerAlignAction == null) {
			centerAlignAction = new AlignAction(SWT.CENTER);
			centerAlignAction.setText("Align Center");
			centerAlignAction.setImageDescriptor(images
					.getDescriptor(CENTER_JUSTIFY_IMAGE));
		}
		return centerAlignAction;
	}

	/**
	 * @return fit text align/justify action
	 */
	public IAction getAlignJustifyAction() {
		if (fillAlignAction == null) {
			fillAlignAction = new AlignAction(RichTextEditorConstants.FIT_ALIGN);
			fillAlignAction.setText("Align Justify");
			fillAlignAction.setImageDescriptor(images
					.getDescriptor(FULL_JUSTIFY_IMAGE));
		}
		return fillAlignAction;
	}

	/**
	 * @return bulleted list action
	 */
	public IAction getBulletedListAction() {
		if (bulletedListAction == null) {
			bulletedListAction = new ListAction(
					RichTextEditorConstants.BULLETED_LIST);
			bulletedListAction.setText("Bulleted list");
			bulletedListAction.setImageDescriptor(images
					.getDescriptor(BULLETS_IMAGE));
		}
		return bulletedListAction;
	}

	/**
	 * @return numbered list action
	 */
	public IAction getNumberedListAction() {
		if (numberedListAction == null) {
			numberedListAction = new ListAction(
					RichTextEditorConstants.NUMBERED_LIST);
			numberedListAction.setText("Numbered list");
			numberedListAction.setImageDescriptor(images
					.getDescriptor(NUMBERS_IMAGE));
		}
		return numberedListAction;
	}

	/**
	 * @return numbered list action (with letter markers)
	 */
	public IAction getLettersNumberedListAction() {
		if (lettersNumberedListAction == null) {
			lettersNumberedListAction = new ListAction(
					RichTextEditorConstants.LETTERS_NUMBERED_LIST);
			lettersNumberedListAction
					.setText("Latin markers");
			lettersNumberedListAction.setImageDescriptor(images
					.getDescriptor(LETTERS_LIST_IMAGE));
		}
		return lettersNumberedListAction;
	}

	/**
	 * @return numbered list action (with roman-like markers)
	 */
	public IAction getRomanNumberedListAction() {
		if (romanNumberedListAction == null) {
			romanNumberedListAction = new ListAction(
					RichTextEditorConstants.ROMAN_NUMBERED_LIST);
			romanNumberedListAction
					.setText("Roman markers");
			romanNumberedListAction.setImageDescriptor(images
					.getDescriptor(ROMAN_LIST_IMAGE));
		}
		return romanNumberedListAction;
	}

	/**
	 * @return increase first line indent action
	 */
	public IAction getIncreaseFirstLineIndentAction() {
		if (increaseFirstLineIndentAction == null) {
			increaseFirstLineIndentAction = new Action() {
				public void run() {
					manager.increaseIntervalFirstLineIndent(viewer
							.getSelectedRange().x, viewer.getSelectedRange().y);
				}
			};
			increaseFirstLineIndentAction.setText("Increase first line indent");
			increaseFirstLineIndentAction.setImageDescriptor(images
					.getDescriptor(INCREASE_INDENT_IMAGE));
		}
		return increaseFirstLineIndentAction;
	}

	/**
	 * @return decrease first line indent action
	 */
	public IAction getDecreaseFirstLineIndentAction() {
		if (decreaseFirstLineIndentAction == null) {
			decreaseFirstLineIndentAction = new Action() {
				public void run() {
					manager.decreaseIntervalFirstLineIndent(viewer
							.getSelectedRange().x, viewer.getSelectedRange().y);
				}
			};
			decreaseFirstLineIndentAction.setText("Decrease first line indent");
			decreaseFirstLineIndentAction.setImageDescriptor(images
					.getDescriptor(DECREASE_INDENT_IMAGE));
		}
		return decreaseFirstLineIndentAction;
	}

	/**
	 * @return single line spacing action
	 */
	public IAction getSingleSpacingAction() {
		if (singleSpacingAction == null) {
			singleSpacingAction = new Action() {
				public void run() {
					manager.setIntervalLineSpacing(viewer.getSelectedRange().x,
							viewer.getSelectedRange().y,
							RichTextEditorConstants.SINGLE_SPACING_CONST);
				}
			};
			singleSpacingAction.setText("Single line spacing");
			singleSpacingAction.setImageDescriptor(images
					.getDescriptor(SINGLE_SPACING));
		}
		return singleSpacingAction;
	}

	/**
	 * @return one and half line spacing action
	 */
	public IAction getOneAndHalfSpacingAction() {
		if (oneAndHalfSpacingAction == null) {
			oneAndHalfSpacingAction = new Action() {
				public void run() {
					manager.setIntervalLineSpacing(viewer.getSelectedRange().x,
							viewer.getSelectedRange().y,
							RichTextEditorConstants.ONE_AND_HALF_SPACING_CONST);
				}
			};
			oneAndHalfSpacingAction.setText("oneAndHalf line spacing");
			oneAndHalfSpacingAction.setImageDescriptor(images
					.getDescriptor(ONE_AND_HALF_SPACING));
		}
		return oneAndHalfSpacingAction;
	}

	/**
	 * @return double line spacing action
	 */
	public IAction getDoubleSpacingAction() {
		if (doubleSpacingAction == null) {
			doubleSpacingAction = new Action() {
				public void run() {
					manager.setIntervalLineSpacing(viewer.getSelectedRange().x,
							viewer.getSelectedRange().y,
							RichTextEditorConstants.DOUBLE_SPACING_CONST);
				}
			};
			doubleSpacingAction.setText("double line spacing");
			doubleSpacingAction.setImageDescriptor(images
					.getDescriptor(DOUBLE_SPACING));
		}
		return doubleSpacingAction;
	}

	/**
	 * @return increase first line indent action
	 */
	public IAction getIncreaseLineSpacingAction() {
		if (increaseLineSpacingAction == null) {
			increaseLineSpacingAction = new Action() {
				public void run() {
					manager.increaseIntervalLineSpacing(viewer
							.getSelectedRange().x, viewer.getSelectedRange().y);
				}
			};
			increaseLineSpacingAction.setText("Increase spacing");
			increaseLineSpacingAction.setImageDescriptor(images
					.getDescriptor(INCREASE_INDENT_IMAGE));
		}
		return increaseLineSpacingAction;
	}

	/**
	 * @return decrease first line indent action
	 */
	public IAction getDecreaseLineSpacingAction() {
		if (decreaseLineSpacingAction == null) {
			decreaseLineSpacingAction = new Action() {
				public void run() {
					manager.decreaseIntervalLineSpacing(viewer
							.getSelectedRange().x, viewer.getSelectedRange().y);
				}
			};
			decreaseLineSpacingAction.setText("decrease spacing");
			decreaseLineSpacingAction.setImageDescriptor(images
					.getDescriptor(DECREASE_INDENT_IMAGE));
		}
		return decreaseLineSpacingAction;
	}

	/**
	 * @return increase indent action
	 */
	public IAction getIncreaseIndentAction() {
		if (increaseIndentAction == null) {
			increaseIndentAction = new Action() {
				public void run() {
					manager.increaseIntervalIndent(viewer.getSelectedRange().x,
							viewer.getSelectedRange().y);
				}
			};
			increaseIndentAction.setText("Increase indent");
			increaseIndentAction.setImageDescriptor(images
					.getDescriptor(INCREASE_INDENT_IMAGE));
		}
		return increaseIndentAction;
	}

	/**
	 * @return decrease indent action
	 */
	public IAction getDecreaseIndentAction() {
		if (decreaseIndentAction == null) {
			decreaseIndentAction = new Action() {
				public void run() {
					manager.decreaseIntervalIndent(viewer.getSelectedRange().x,
							viewer.getSelectedRange().y);
				}
			};
			decreaseIndentAction.setText("Decrease indent");
			decreaseIndentAction.setImageDescriptor(images
					.getDescriptor(DECREASE_INDENT_IMAGE));
		}
		return decreaseIndentAction;
	}

	/**
	 * @return increase right indent action
	 */
	public IAction getIncreaseRightIndentAction() {
		if (increaseRightIndentAction == null) {
			increaseRightIndentAction = new Action() {
				public void run() {
					manager.increaseIntervalRightIndent(viewer
							.getSelectedRange().x, viewer.getSelectedRange().y);
				}
			};
			increaseRightIndentAction.setText("Increase right indent");
			increaseRightIndentAction.setImageDescriptor(images
					.getDescriptor(INCREASE_RIGHT_INDENT_IMAGE));
		}
		return increaseRightIndentAction;
	}

	/**
	 * @return decrease right indent action
	 */
	public IAction getDecreaseRightIndentAction() {
		if (decreaseRightIndentAction == null) {
			decreaseRightIndentAction = new Action() {
				public void run() {
					manager.decreaseIntervalRightIndent(viewer
							.getSelectedRange().x, viewer.getSelectedRange().y);
				}
			};
			decreaseRightIndentAction.setText("Decrease right indent");
			decreaseRightIndentAction.setImageDescriptor(images
					.getDescriptor(DECREASE_RIGHT_INDENT_IMAGE));
		}
		return decreaseRightIndentAction;
	}

	/**
	 * @param toolbarManager
	 *            ToolbarManager to fill with action buttons
	 */
	public void fillToolbarManager(IContributionManager toolbarManager) {
		if (toolbarManager==null){
			return;
		}
		List<IContributionItem> itemsList = getActionsList();
		for (Iterator<IContributionItem> iterator = itemsList.iterator(); iterator.hasNext();) {
			IContributionItem contributionItem = (IContributionItem) iterator
					.next();
			toolbarManager.add(contributionItem);
		}
		if (toolbarManager instanceof ToolBarManager)
			toolBar = ((ToolBarManager) toolbarManager).getControl();
		/*
		 * toolbarManager.add(this.getBoldAction());
		 * 
		 * toolbarManager.add(this.getItalicAction());
		 * toolbarManager.add(this.getUnderlineAction()); //TODO IMPLEMENT IT
		 * toolbarManager.add(this.getStrikeThroughAction());
		 * toolbarManager.add(new Separator());
		 * toolbarManager.add(this.getStyleContributionItem());
		 * toolbarManager.add(new Separator());
		 * toolbarManager.add(this.getAlignLeftAction());
		 * toolbarManager.add(this.getAlignRightAction());
		 * toolbarManager.add(this.getAlignCenterAction());
		 * toolbarManager.add(this.getAlignJustifyAction());
		 * toolbarManager.add(new Separator());
		 * toolbarManager.add(this.getNewLinkAction()); toolbarManager.add(new
		 * Separator()); toolbarManager.add(this.getBulletedListAction());
		 * toolbarManager.add(this.getNumberedListAction());
		 * toolbarManager.add(new Separator()); //fix for Windows toolbar
		 * redrawing issue toolbarManager.add(new ControlContribution(""){
		 * 
		 * 
		 * protected Control createControl(Composite parent) {
		 * parent.setLayout(new FillLayout()); ToolBar toolbar = new
		 * ToolBar(parent,SWT.NONE); ToolBarManager ma=new
		 * ToolBarManager(toolbar); ma.add(getForegroundColorAction());
		 * ma.add(new Separator()); ma.add(getBackgroundColorAction());
		 * ma.update(true); return ma.getControl(); }
		 * 
		 * }); toolbarManager.add(new Separator());
		 * toolbarManager.add(this.getAddImageAction()); toolbarManager.add(new
		 * Separator()); toolbarManager.add(this.getOpenFileAction());
		 * toolbarManager.add(new Separator());
		 * toolbarManager.add(this.getAddHRAction());
		 * toolbarManager.add(this.getCustomizeFontStyleAction());
		 */}

	/**
	 * Returns ActioFactory's actions list
	 * 
	 * @return actions list, consisting from {@link IContributionItem}'s
	 */
	public ArrayList<IContributionItem> getActionsList() {
		if (resList == null)
			resList = createActionsList();
		return resList;
	}

	public ArrayList<IContributionItem> createActionsList() {
		initContainers();
//		addActionToList(new ControlContribution("Zoom") {
//
//			
//			protected Control createControl(Composite parent) {
//				final CCombo cmd = new CCombo(parent, SWT.NONE);
//				final float[] zooms = new float[] { 0.5f, 0.75f, 1f, 2f, 3f };
//				cmd.setItems(new String[] { "50%", "75%", "100%", "200%",
//						"300%" });
//				cmd.select(2);
//				cmd.addSelectionListener(new SelectionListener() {
//
//					public void widgetDefaultSelected(SelectionEvent e) {
//
//					}
//
//					public void widgetSelected(SelectionEvent e) {
//						int selectionIndex = cmd.getSelectionIndex();
//						manager.setScale(zooms[selectionIndex]);
//					}
//
//				});
//				return cmd;
//			}
//
//		});
		addActionToList(new ActionContributionItem(this.getBoldAction()));

		addActionToList(new ActionContributionItem(this.getItalicAction()));
		addActionToList(new ActionContributionItem(this.getUnderlineAction()));
		addActionToList(new ActionContributionItem(this.getSupAction()));
		addActionToList(new ActionContributionItem(this.getSubAction()));
		// TODO IMPLEMENT IT
		addActionToList(new ActionContributionItem(this
				.getStrikeThroughAction()));

		addActionToList(new Separator());
	    addActionToList(this.getStyleContributionItem());
		addActionToList(new Separator());
		addActionToList(new ActionContributionItem(this.getAlignLeftAction()));
		addActionToList(new ActionContributionItem(this.getAlignRightAction()));
		addActionToList(new ActionContributionItem(this.getAlignCenterAction()));
		addActionToList(new ActionContributionItem(this.getAlignJustifyAction()));
		addActionToList(new ActionContributionItem(this
				.getIncreaseFirstLineIndentAction()));
		addActionToList(new ActionContributionItem(this
				.getDecreaseFirstLineIndentAction()));
		addActionToList(new ActionContributionItem(this
				.getSingleSpacingAction()));
		addActionToList(new ActionContributionItem(this
				.getOneAndHalfSpacingAction()));
		addActionToList(new ActionContributionItem(this
				.getDoubleSpacingAction()));
		addActionToList(new Separator());
		addActionToList(new ActionContributionItem(this
				.getIncreaseRightIndentAction()));
		addActionToList(new ActionContributionItem(this
				.getDecreaseRightIndentAction()));
		addActionToList(new Separator());
		addActionToList(new ActionContributionItem(this.getNewLinkAction()));
		addActionToList(new Separator());
		addActionToList(new ActionContributionItem(this.getBulletedListAction()));
		addActionToList(new ActionContributionItem(this.getNumberedListAction()));
		addActionToList(new Separator());
		// fix for Windows toolbar redrawing issue
		addActionToList(new ControlContribution("") {

			protected Control createControl(Composite parent) {
				// parent.setLayout(new FillLayout());
				ToolBar toolbar = new ToolBar(parent, SWT.NONE);
				ToolBarManager ma = new ToolBarManager(toolbar);
				ma.add(getForegroundColorAction());
				ma.add(new Separator());
				ma.add(getBackgroundColorAction());
				ma.update(true);
				return ma.getControl();
			}

		});
		addActionToList(new Separator());
		addActionToList(new ActionContributionItem(this.getAddImageAction()));
		addActionToList(new ActionContributionItem(this.getAddRegionAction()));
		// TODO OPTIMIZE IT
		// addActionToList(new
		// ActionContributionItem(this.getAddJavaRegionAction()));
		addActionToList(new Separator());
		addActionToList(new ActionContributionItem(this.getOpenFileAction()));
		addActionToList(new Separator());
		addActionToList(new ActionContributionItem(this.getAddHRAction()));
		addActionToList(new ActionContributionItem(this
				.getCustomizeFontStyleAction()));
		return resList;
	}

	protected void initContainers() {
		resList = new ArrayList<IContributionItem>();
		enablementList = new ArrayList<EnabledAction>();
	}

	protected void addActionToList(IContributionItem iContributionItem) {
		if (iContributionItem instanceof ActionContributionItem
				&& ((ActionContributionItem) iContributionItem).getAction() instanceof EnabledAction) {
			enablementList
					.add((EnabledAction) ((ActionContributionItem) iContributionItem)
							.getAction());
		}
		resList.add(iContributionItem);
	}

	/**
	 * Used to fill toolbar manager with custom actions
	 * 
	 * @param toolbarManager
	 *            manager to fill
	 * @param items
	 *            list of actions to fill with
	 */
	public void customFillToolbarManager(IContributionManager toolbarManager,
			List<IContributionItem> items) {
		for (Iterator<IContributionItem> iterator = items.iterator(); iterator
				.hasNext();) {
			IContributionItem item = (IContributionItem) iterator.next();
			toolbarManager.add(item);
		}
	}

	/**
	 * @return strikethrough style action
	 */
	public IAction getStrikeThroughAction() {
		if (strikeThroughAction == null) {
			strikeThroughAction = new Action("", Action.AS_CHECK_BOX) {
				public void run() {
					if (viewer.getSelectedRange().y > 0)
						manager.strikethroughCommand(
								viewer.getSelectedRange().x, viewer
										.getSelectedRange().y, isChecked());
					else
						setCurrentBasePartitionState();
				}
			};
			strikeThroughAction.setText("Strike through");
			// boldAction.setAccelerator(SWT.CTRL | 'I');
			strikeThroughAction.setImageDescriptor(images
					.getDescriptor(STRIKE_IMAGE));
		}
		return strikeThroughAction;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		int offset = manager.getTextWidget().getCaretOffset();
		BasePartition curPartition = (BasePartition) manager.getLayer()
				.getPartitionAtOffset(offset);
		if (curPartition != null && curPartition.requiresSingleLine()
				&& offset > curPartition.getOffset())
			manager.getTextWidget().setCaretOffset(
					curPartition.getOffset() + curPartition.getLength());
		manager.setCurrentFontPartition(null);
		update();
	}

	public void update() {
		if (viewer.getTextWidget().getText().length() < 1)
			return;
		int offset = viewer.getSelectedRange().x;
		int length = viewer.getSelectedRange().y;

		int firstLine = viewer.getTextWidget().getLineAtOffset(offset);
		int lastLine;
		if (length == 0)
			lastLine = firstLine;
		else
			lastLine = viewer.getTextWidget().getLineAtOffset(
					offset + length - 1);

		// if (length == 0)
		// setLineIdentation(offset, length);
		RichSelectionState sumPartition = manager.defineSumStylePartition(
				offset, length);
		setFontStyleControlsStates(sumPartition);

		int align = manager.defineSumAlignStyle(firstLine, lastLine);
		setAlignButtonsStates(align);
		setListButtonsStates(manager.defineSumListStyle(firstLine, lastLine));
		if (enablementList == null) {
			enablementList = new ArrayList<EnabledAction>();
		}
		for (Iterator<EnabledAction> iterator = enablementList.iterator(); iterator
				.hasNext();) {
			EnabledAction action = (EnabledAction) iterator.next();
			int enablement = action.getEnabled(manager.getLayer(), offset,
					length);
			if (enablement == EnabledAction.DISABLED) {
				action.setEnabled(false);
				action.setChecked(false);
			} else if (enablement == EnabledAction.ENABLED) {
				action.setEnabled(true);
				action.setChecked(false);
			} else if (enablement == EnabledAction.CHECKED) {
				action.setEnabled(true);
				action.setChecked(true);
			}

		}
	}

	protected void setCurrentBasePartitionState() {
		BasePartition partition = manager.getCurrentFontPartition();
		partition.setBold(getBoldAction().isChecked());
		partition.setItalic(getItalicAction().isChecked());
		partition.setUnderlined(getUnderlineAction().isChecked());
		partition.setStrikethrough(getStrikeThroughAction().isChecked());
		partition.setSub(getSubAction().isChecked());
		partition.setSup(getSupAction().isChecked());
		// String fontDataName = FontStyle.NORMAL_FONT_NAME; // by default
		if (combo != null) {
			final int selectionIndex = combo.getSelectionIndex();
			if (selectionIndex > -1) {
				final String item = combo.getItem(selectionIndex);
				manager.addNewlinesIfNeeded(viewer.getTextWidget()
						.getCaretOffset(), item);
				manager.getFontStyleManager().getFontStyleByFontDataName(
						partition.getFontDataName()).removeStyle(partition);
				manager.getFontStyleManager().getFontStyle(item).applyStyle(
						partition);
				combo.select(selectionIndex);
			}
		}

		// update();
	}

	protected void setAlignButtonsStates(int align) {
		if (this.centerAlignAction != null)
			this.getAlignCenterAction().setChecked(
					align == RichTextEditorConstants.CENTER_ALIGN);
		if (this.leftAlignAction != null)
			this.getAlignLeftAction().setChecked(
					align == RichTextEditorConstants.LEFT_ALIGN);
		if (this.rightAlignAction != null)
			this.getAlignRightAction().setChecked(
					align == RichTextEditorConstants.RIGHT_ALIGN);
		if (this.fillAlignAction != null)
			this.getAlignJustifyAction().setChecked(
					align == RichTextEditorConstants.FIT_ALIGN);
	}

	protected void setListButtonsStates(int style) {
		boolean wasChecked = false;
		if (this.bulletedListAction != null) {
			this.bulletedListAction
					.setChecked(style == RichTextEditorConstants.BULLETED_LIST);
			if (this.bulletedListAction.isChecked())
				wasChecked = true;
		}
		if (this.numberedListAction != null) {
			this.numberedListAction
					.setChecked(style == RichTextEditorConstants.NUMBERED_LIST);
			if (this.numberedListAction.isChecked())
				wasChecked = true;
		}
		if (this.lettersNumberedListAction != null) {
			this.lettersNumberedListAction
					.setChecked(style == RichTextEditorConstants.LETTERS_NUMBERED_LIST);
			if (this.lettersNumberedListAction.isChecked())
				wasChecked = true;
		}
		if (this.romanNumberedListAction != null) {
			this.romanNumberedListAction
					.setChecked(style == RichTextEditorConstants.ROMAN_NUMBERED_LIST);
			if (this.romanNumberedListAction.isChecked())
				wasChecked = true;
		}
		enableProhibitedAligns(!wasChecked);
	}

	protected void setFontStyleControlsStates(RichSelectionState state) {
		BasePartition sumPartition = state.getSumPartition();
		if (this.boldAction != null)
			this.getBoldAction().setChecked(sumPartition.isBold());
		if (this.italicAction != null)
			this.getItalicAction().setChecked(sumPartition.isItalic());
		if (this.underlineAction != null)
			this.getUnderlineAction().setChecked(sumPartition.isUnderlined());
		if (this.supAction != null)
			this.getSupAction().setChecked(sumPartition.isSup());
		if (this.subAction != null)
			this.getSubAction().setChecked(sumPartition.isSub());
		if (this.strikeThroughAction != null)
			this.getStrikeThroughAction().setChecked(
					sumPartition.isStrikethrough());
		if (this.foregroundColorAction != null)
			((ForeGroundColorAction) this.getForegroundColorAction())
					.setColor(MetaConverter.convertRGB(sumPartition.getColorRGB()));
		if (this.backgroundColorAction != null)
			((BackGroundColorAction) this.getBackgroundColorAction())
					.setColor(MetaConverter.convertRGB(sumPartition.getBgColorRGB()));
		if (this.linkAction != null) {
			linkAction.setChecked(state.isHasLinks());
		}
		if (this.controlContribution != null&&combo!=null&&!combo.isDisposed()) {
			if (sumPartition.getFontDataName().length() > 0) {
				String[] fontStyleNames = manager.getFontStyleManager().getFontStyleDataNames();
				boolean wasSelected = false;
				for (int i = 0; i < fontStyleNames.length; i++) {
					if (fontStyleNames[i]
							.equals(sumPartition.getFontDataName())) {
						combo.select(i);
						i = fontStyleNames.length; // For loop interrupting
						wasSelected = true;
					}
				}
				if (!wasSelected)
					combo.select(-1);
			} else
				combo.select(-1);
		}
	}

	/**
	 * enables or disable special aligns< like right or center Needed when
	 * endling/disabling lists, to display list bullets correctly
	 * 
	 * @param enable
	 *            <b>true</b> enable this actions, <b>false</b> - disable them
	 */
	protected void enableProhibitedAligns(boolean enable) {

		if (this.centerAlignAction != null)
			this.centerAlignAction.setEnabled(enable);
		if (this.rightAlignAction != null)
			this.rightAlignAction.setEnabled(enable);
	}

	public void documentAboutToBeChanged(DocumentEvent event) {

	}

	/**
	 * only for updating
	 */
	public void documentChanged(DocumentEvent event, RichDocumentChange change) {
		update();
	}

	protected HyperlinkDialog createHyperlinkDialog(boolean editName, Shell shell) {
		return new HyperlinkDialog(shell, editName);
	}
	HashSet<IStyleEnablementListener>slisteners=new HashSet<IStyleEnablementListener>();

	private boolean senabled=true;

	/**
	 * @param wikiEditorRibbonContribution
	 */
	public void addStyleEnablementListener(
			IStyleEnablementListener wikiEditorRibbonContribution) {
		slisteners.add(wikiEditorRibbonContribution);
	}
	/**
	 * @param wikiEditorRibbonContribution
	 */
	public void removeStyleEnablementListener(
			IStyleEnablementListener wikiEditorRibbonContribution) {
		slisteners.remove(wikiEditorRibbonContribution);
	}

	protected void setStyleEnabled(boolean enabled){
		if(combo!=null){
			combo.setEnabled(enabled);
		}
		senabled=enabled;
		for (IStyleEnablementListener s:slisteners){
			s.changed(enabled);
		}
		
	}
	public boolean isStylesEnabled(){
		return senabled;
	}
}
