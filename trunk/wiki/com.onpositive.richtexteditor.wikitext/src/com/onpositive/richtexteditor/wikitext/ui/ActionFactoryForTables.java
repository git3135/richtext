package com.onpositive.richtexteditor.wikitext.ui;

import java.util.ArrayList;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.text.ITextViewer;

import com.onpositive.richtexteditor.model.resources.LayerManager;

public class ActionFactoryForTables extends CustomActionFactory{

	public ActionFactoryForTables(LayerManager manager, ITextViewer viewer) {
		super(manager, viewer);
	}

	
	
	public ArrayList<IContributionItem> createActionsList() // Overriding this
	// metod, we can get
	// custom action
	// list(and tollbar)
	// contents
	{
		initContainers();
		/*addActionToList(new ActionContributionItem(getBoldAction()));
		addActionToList(new ActionContributionItem(getItalicAction()));
		addActionToList(new ActionContributionItem(getUnderlineAction()));
		*//*addActionToList(new ActionContributionItem(getSupAction()));
		addActionToList(new ActionContributionItem(getSubAction()));
		//addActionToList(getStyleContributionItem());
		addActionToList(new ActionContributionItem(getNewLinkAction()));
		addActionToList(new Separator());
		addActionToList(new ActionContributionItem(getAddHRAction()));
		addActionToList(new ActionContributionItem(getAddImageAction()));
	//	addActionToList(new Separator());
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
		addActionToList(createOpenPageInBrowserContributionItem());*/
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
		//resList.add(createAddRegionContributionItem());
		//addActionToList(new Separator());
		//addActionToList(new ActionContributionItem(getCreateTableAction()));

		return resList;
	}
}
