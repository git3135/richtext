package com.onpositive.richtexteditor.wikitext.ui;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.onpositive.commons.ui.dialogs.TitledDialog;
import com.onpositive.commons.xml.language.DOMEvaluator;
import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.BasePartitionLayer;
import com.onpositive.richtext.model.IRegionCompositeWrapper;
import com.onpositive.richtext.model.IStylePartition;
import com.onpositive.richtext.model.PartitionStorage;
import com.onpositive.richtext.model.RegionPartition;
import com.onpositive.richtexteditor.actions.ActionFactory;
import com.onpositive.richtexteditor.actions.EnabledAction;
import com.onpositive.richtexteditor.model.changes.Change;
import com.onpositive.richtexteditor.model.resources.LayerManager;
import com.onpositive.richtexteditor.region_provider.ContentTypeEditorProvider;
import com.onpositive.richtexteditor.wikitext.partitions.CamelCasePartition;
import com.onpositive.richtexteditor.wikitext.regions.IWikitextRegionTypesProvider;
import com.onpositive.richtexteditor.wikitext.regions.RegionType;
import com.onpositive.semantic.model.binding.Binding;
import com.onpositive.semantic.model.ui.generic.IPropertyEditor;

public class CustomActionFactory extends ActionFactory {
	
	/**
	 * Default row count for table creating action
	 */
	protected static final int DEFAULT_TABLE_ROW_COUNT = 4;
	/**
	 * Default column count for table creating action
	 */
	protected static final int DEFAULT_TABLE_COLUMN_COUNT = 4;

	public class TableParamsDialog extends TitleAreaDialog {

		
		private Point value;

		private Spinner numCols;
		private Spinner numRows;

		public TableParamsDialog(Shell parentShell) {
			super(parentShell);
			open();
			// setTitle("Please enter parameters of the table");
		}

		
		public void create() {
			super.create();
			setTitle("Create table");
			setMessage("Please enter parameters of the table");
		}

		
		protected Control createDialogArea(Composite parent) {
			getShell().setText("Create table");
			Composite parent1 = (Composite) super.createDialogArea(parent);
			parent1.setLayoutData(GridDataFactory.fillDefaults().grab(true,
					true).create());

			Composite result = new Composite(parent1, SWT.NONE);
			result.setLayoutData(GridDataFactory.fillDefaults()
					.grab(true, true).create());

			GridLayout layout = new GridLayout();

			layout.numColumns = 2;
			result.setLayout(layout);

			Label numColLabel = new Label(result, SWT.NONE);
			numColLabel.setText("Number of columns");

			numCols = new Spinner(result, SWT.BORDER);
			numCols.setMaximum(30);
			numCols.setMinimum(1);
			numCols.setSelection(2);
			numCols.setIncrement(1);
			numCols.setLayoutData(GridDataFactory.fillDefaults().grab(true,
					false).create());

			Label numRowLabel = new Label(result, SWT.NONE);
			numRowLabel.setText("Number of rows");

			numRows = new Spinner(result, SWT.BORDER);
			numRows.setMaximum(30);
			numRows.setMinimum(1);
			numRows.setSelection(2);
			numRows.setIncrement(1);
			numRows.setLayoutData(GridDataFactory.fillDefaults().grab(true,
					false).create());

			return parent1;
		}

		
		protected void okPressed() {
			value = new Point(numCols.getSelection(), numRows.getSelection());
			super.okPressed();
		}

		public Point getValue() {
			return value;
		}
	}

	protected IWikitextRegionTypesProvider wikitextRegionTypesProvider;

	private IAction createTableAction;

	public CustomActionFactory(LayerManager manager, ITextViewer viewer) {
		super(manager, viewer);
		wikitextRegionTypesProvider = getRegionTypesProvider();
	}

	/*
	 * public Menu createPopupMenu(Control parent) { MenuManager manager = new
	 * MenuManager(); for (Iterator<IContributionItem> iterator =
	 * resList.iterator(); iterator.hasNext();) { IContributionItem item =
	 * (IContributionItem) iterator.next(); manager.add(item); } return
	 * manager.createContextMenu(parent); }
	 */

	
	public ArrayList<IContributionItem> createActionsList() // Overriding this
	// metod, we can get
	// custom action
	// list(and tollbar)
	// contents
	{
		initContainers();
		addActionToList(new ActionContributionItem(getBoldAction()));
		addActionToList(new ActionContributionItem(getItalicAction()));
		addActionToList(new ActionContributionItem(getUnderlineAction()));
		addActionToList(new ActionContributionItem(getSupAction()));
		addActionToList(new ActionContributionItem(getSubAction()));
		addActionToList(getStyleContributionItem());
		addActionToList(new ActionContributionItem(getNewLinkAction()));
		addActionToList(new Separator());
		addActionToList(new ActionContributionItem(getAddHRAction()));
		addActionToList(new ActionContributionItem(getAddImageAction()));
		addActionToList(new Separator());
		addActionToList(new ActionContributionItem(getBulletedListAction()));
		addActionToList(new ActionContributionItem(getNumberedListAction()));
		addActionToList(new ActionContributionItem(
				getLettersNumberedListAction()));
		addActionToList(new ActionContributionItem(getRomanNumberedListAction()));
		addActionToList(new ActionContributionItem(getIncreaseIndentAction()));
		addActionToList(new ActionContributionItem(getDecreaseIndentAction()));
		// addActionToList(new ActionContributionItem(getAddRegionAction()));
		addActionToList(new Separator());
		addActionToList(createEscapeCamelCaseContributionItem());
		addActionToList(createOpenPageInBrowserContributionItem());
		/*
		 * resList.add(new ActionContributionItem(new Action() {
		 *//**
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.Action#run()
		 */
		/*
		 *  public void run() { FileDialog dialog = new
		 * FileDialog(viewer.getTextWidget() .getShell()); String fileName =
		 * dialog.open();
		 * 
		 * IRegionCompositeWrapper editorForContentType =
		 * ContentTypeEditorProvider .getEditorForContentType(fileName,
		 * manager.getEditor());
		 * manager.addWidgetPartition(editorForContentType); super.run(); }
		 * 
		 * 
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.Action#getImageDescriptor()
		 * 
		 *  public ImageDescriptor getImageDescriptor() { return
		 * AbstractUIPlugin.imageDescriptorFromPlugin(
		 * "com.onpositive.richtexteditor.wikitext", "java_app.gif"); }
		 * 
		 * }));
		 */
		resList.add(createAddRegionContributionItem());
		addActionToList(new Separator());
		addActionToList(new ActionContributionItem(getCreateTableAction()));

		return resList;
	}

	protected IContributionItem createOpenPageInBrowserContributionItem() {
		return new ActionContributionItem(new Action("Open page in browser",
				Action.AS_PUSH_BUTTON) {
			public void run() {

			}

			public ImageDescriptor getImageDescriptor() {
				return AbstractUIPlugin.imageDescriptorFromPlugin(
						"com.onpositive.wiki.ui", "icons/external_browser.gif");
			}

		});
	}

	protected ActionContributionItem createEscapeCamelCaseContributionItem() {
		EnabledAction action = getEscapeAction();
		return new ActionContributionItem(action);

	}

	public EnabledAction getEscapeAction() {
		EnabledAction action = new EnabledAction("Escape link",
				Action.AS_CHECK_BOX) {
			public void run() {
				int style = CamelCasePartition.LINK;
				if (this.isChecked())
					style = CamelCasePartition.ESCAPED;
				Change change = manager.getCommandCreator()
						.createSetPartitionStyleChange(
								viewer.getSelectedRange().x,
								viewer.getSelectedRange().y, style);
				manager.getLayer().executeExternalChange(change);
			}

			public ImageDescriptor getImageDescriptor() {
				return AbstractUIPlugin.imageDescriptorFromPlugin(
						"com.onpositive.richtexteditor.wikitext",
						"icons/error_ov.gif");
			}

			public int getEnabled(BasePartitionLayer layer, int offset,
					int length) {
				BasePartition newPartition = (BasePartition) layer
						.getPartitionAtOffset(offset);
				if (newPartition == null && offset > 0)
					newPartition = (BasePartition) layer
							.getPartitionAtOffset(offset - 1);
				if (newPartition == null)
					return EnabledAction.DISABLED;
				PartitionStorage storage = layer.getStorage();
				if (!(newPartition instanceof IStylePartition))
					return EnabledAction.DISABLED;
				IStylePartition stylePartition = (IStylePartition) newPartition;
				int style = stylePartition.getStyle();
				for (int i = newPartition.getIndex(); i < storage.size()
						&& storage.get(i).getOffset() < offset + length; i++) {
					BasePartition basePartition = storage.get(i);
					if (!(basePartition instanceof IStylePartition))
						return EnabledAction.DISABLED;
					int style2 = ((IStylePartition) basePartition).getStyle();
					if (style != style2)
						style = Math.min(style, style2);
				}
				return style;
			}
		};
		return action;
	}

	public ActionContributionItem createAddRegionContributionItem() {
		return new ActionContributionItem(new Action() {

			/**
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.Action#run()
			 */
			
			public void run() {
				FileDialog dialog = new FileDialog(viewer.getTextWidget()
						.getShell());
				String fileName = dialog.open();

				IRegionCompositeWrapper editorForContentType = ContentTypeEditorProvider
						.getEditorForContentType(fileName, manager
								.getActualTextWidget());
				manager.addWidgetPartition(editorForContentType);
				super.run();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.Action#getImageDescriptor()
			 */
			
			public ImageDescriptor getImageDescriptor() {
				return AbstractUIPlugin.imageDescriptorFromPlugin(
						"com.onpositive.richtexteditor",
						"actions/textarea_obj.gif");
			}

		});
	}

	
	public IAction getAddRegionAction() {
		if (addRegionAction == null) {
			addRegionAction = new Action("Add region") {
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

	protected IWikitextRegionTypesProvider getRegionTypesProvider() {
		if (wikitextRegionTypesProvider == null)
			wikitextRegionTypesProvider = new IWikitextRegionTypesProvider() {

				public RegionType[] getSupportedRegionTypes() {
					return new RegionType[] {
							new RegionType("Simple",
									RegionPartition.PLAIN_TEXT_CONTENT_TYPE),
							new RegionType("Java", "java"),
							new RegionType("XML", "xml"),
							new RegionType("Python", "python"),
							new RegionType("html", "html") };
				}

				public String[] getSupportedRegionTypesDisplayNames() {
					RegionType[] supportedRegionTypes = getSupportedRegionTypes();
					String[] res = new String[supportedRegionTypes.length];
					for (int i = 0; i < supportedRegionTypes.length; i++) {
						res[i] = supportedRegionTypes[i].getDisplayName();
					}
					return res;
				}
			};
		return wikitextRegionTypesProvider;
	}

	/**
	 * @return create table action
	 */
	public IAction getCreateTableAction() {
		if (createTableAction == null) {
			createTableAction = new Action() {
				public void run() {
					Point defaultTableSize = new Point(DEFAULT_TABLE_COLUMN_COUNT, DEFAULT_TABLE_ROW_COUNT);
					try {
						Binding pContext = new Binding(defaultTableSize);
						pContext.setName("Table");
						Object evaluateLocalPluginResource = DOMEvaluator
								.getInstance().evaluateLocalPluginResource(
										CustomActionFactory.class,
										"tableDialog.dlf", pContext);
						TitledDialog d = new TitledDialog(
								(IPropertyEditor) evaluateLocalPluginResource);
						int open = d.open();
						if (open == Dialog.OK) {
							
							StringBuilder initTableStr = new StringBuilder();
							for (int i = 0; i < defaultTableSize.y; i++) {
								for (int j = 0; j < defaultTableSize.x; j++) {
									initTableStr.append("||empty");
								}
								if (i != defaultTableSize.y - 1) {
									initTableStr.append("||\n");
								} else {
									initTableStr.append("||");
									manager.addWidgetPartition(initTableStr.toString(),
											RegionPartition.TABLE_CONTENT_TYPE);

								}
							}
						}
					} catch (Exception e) {

						e.printStackTrace();
					}

					// TableParamsDialog dialog = new TableParamsDialog(viewer
					// .getTextWidget().getDisplay().getActiveShell());

					// Point result = dialog.value;
					// manager.addWidgetPartition("",
					// RegionPartition.TABLE_CONTENT_TYPE);
					return;
				}
			};
			createTableAction.setText("Create table");
			createTableAction.setImageDescriptor(images.getDescriptor(TABLE));
		}
		return createTableAction;
	}

	
}
