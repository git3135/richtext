package com.onpositive.richtexteditor.region_provider;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.INavigationHistory;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.MultiPartInitException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.EditorActionBars;
import org.eclipse.ui.internal.KeyBindingService;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.services.ServiceLocator;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.MultiPageSelectionProvider;

public class DummyEditorSite implements IEditorSite {
	protected IEditorPart editor;
	protected String id;
	protected Composite editorParent;
	protected KeyBindingService instance;
	protected ISelectionProvider selectionProvider;
	private Shell shell;
	protected IWorkbenchPage lastPage;

	public DummyEditorSite(IEditorPart editor, String id, Composite editorParent) {
		this.editor = editor;
		this.id = id;
		this.editorParent = editorParent;
		shell = editorParent.getShell();
		lastPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
		.getActivePage();
	}

	
	public IEditorActionBarContributor getActionBarContributor() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public IActionBars getActionBars() {
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages().length == 0)
		{
			return new DummyActionBars();
		}
		WorkbenchPage activePage = (WorkbenchPage) PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		if (activePage == null )
			activePage = (WorkbenchPage) PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow().getPages()[0];
		return new EditorActionBars(activePage,
				new ServiceLocator(), "simple") {
			
			public void setGlobalActionHandler(String actionID, IAction handler) {

			}
		};
	}

	
	public void registerContextMenu(MenuManager menuManager,
			ISelectionProvider selectionProvider, boolean includeEditorInput) {
		// TODO Auto-generated method stub

	}

	
	public void registerContextMenu(String menuId, MenuManager menuManager,
			ISelectionProvider selectionProvider, boolean includeEditorInput) {
		// TODO Auto-generated method stub

	}

	
	public String getId() {
		return id;
	}

	
	public IKeyBindingService getKeyBindingService() {
		if (instance == null)
			instance = new KeyBindingService(this);
		return instance;
	}

	
	public IWorkbenchPart getPart() {
		return editor;
	}

	
	public String getPluginId() {
		return Activator.PLUGIN_ID;
	}

	
	public String getRegisteredName() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void registerContextMenu(MenuManager menuManager,
			ISelectionProvider selectionProvider) {
		// TODO Auto-generated method stub

	}

	
	public void registerContextMenu(String menuId, MenuManager menuManager,
			ISelectionProvider selectionProvider) {
		// TODO Auto-generated method stub

	}

	
	public IWorkbenchPage getPage() {
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
		.getActivePage();
		if (activePage == null)
			activePage = lastPage;
		if (activePage == null)
		{
			activePage = new IWorkbenchPage()
			{
				
				public void removeSelectionListener(String partId, ISelectionListener listener)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void removeSelectionListener(ISelectionListener listener)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void removePostSelectionListener(String partId, ISelectionListener listener)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void removePostSelectionListener(ISelectionListener listener)
				{
					// TODO Auto-generated method stub
					
				}
				
				public ISelection getSelection(String partId)
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public ISelection getSelection()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public void addSelectionListener(String partId, ISelectionListener listener)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void addSelectionListener(ISelectionListener listener)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void addPostSelectionListener(String partId, ISelectionListener listener)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void addPostSelectionListener(ISelectionListener listener)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void removePartListener(IPartListener2 listener)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void removePartListener(IPartListener listener)
				{
					// TODO Auto-generated method stub
					
				}
				
				public IWorkbenchPartReference getActivePartReference()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IWorkbenchPart getActivePart()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public void addPartListener(IPartListener2 listener)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void addPartListener(IPartListener listener)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void zoomOut()
				{
					// TODO Auto-generated method stub
					
				}
				
				public void toggleZoom(IWorkbenchPartReference ref)
				{
					// TODO Auto-generated method stub
					
				}
				
				public IViewPart showView(String viewId, String secondaryId, int mode) throws PartInitException
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IViewPart showView(String viewId) throws PartInitException
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public void showEditor(IEditorReference ref)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void showActionSet(String actionSetID)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void setWorkingSets(IWorkingSet[] sets)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void setPerspective(IPerspectiveDescriptor perspective)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void setPartState(IWorkbenchPartReference ref, int state)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void setEditorReuseThreshold(int openEditors)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void setEditorAreaVisible(boolean showEditorArea)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void savePerspectiveAs(IPerspectiveDescriptor perspective)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void savePerspective()
				{
					// TODO Auto-generated method stub
					
				}
				
				public boolean saveEditor(IEditorPart editor, boolean confirm)
				{
					// TODO Auto-generated method stub
					return false;
				}
				
				public boolean saveAllEditors(boolean confirm)
				{
					// TODO Auto-generated method stub
					return false;
				}
				
				public void reuseEditor(IReusableEditor editor, IEditorInput input)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void resetPerspective()
				{
					// TODO Auto-generated method stub
					
				}
				
				public void removePropertyChangeListener(IPropertyChangeListener listener)
				{
					// TODO Auto-generated method stub
					
				}
				
				public IEditorReference[] openEditors(IEditorInput[] inputs, String[] editorIDs, int matchFlags) throws MultiPartInitException
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IEditorPart openEditor(IEditorInput input, String editorId, boolean activate, int matchFlags) throws PartInitException
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IEditorPart openEditor(IEditorInput input, String editorId, boolean activate) throws PartInitException
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IEditorPart openEditor(IEditorInput input, String editorId) throws PartInitException
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public boolean isPartVisible(IWorkbenchPart part)
				{
					// TODO Auto-generated method stub
					return false;
				}
				
				public boolean isPageZoomed()
				{
					// TODO Auto-generated method stub
					return false;
				}
				
				public boolean isEditorPinned(IEditorPart editor)
				{
					// TODO Auto-generated method stub
					return false;
				}
				
				public boolean isEditorAreaVisible()
				{
					// TODO Auto-generated method stub
					return false;
				}
				
				public void hideView(IViewReference view)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void hideView(IViewPart view)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void hideEditor(IEditorReference ref)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void hideActionSet(String actionSetID)
				{
					// TODO Auto-generated method stub
					
				}
				
				public IWorkingSet[] getWorkingSets()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IWorkingSet getWorkingSet()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IWorkbenchWindow getWorkbenchWindow()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IViewPart[] getViews()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IViewPart[] getViewStack(IViewPart part)
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IViewReference[] getViewReferences()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IPerspectiveDescriptor[] getSortedPerspectives()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public String[] getShowViewShortcuts()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IWorkbenchPartReference getReference(IWorkbenchPart part)
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public String[] getPerspectiveShortcuts()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IPerspectiveDescriptor getPerspective()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public int getPartState(IWorkbenchPartReference ref)
				{
					// TODO Auto-generated method stub
					return 0;
				}
				
				public IPerspectiveDescriptor[] getOpenPerspectives()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public String[] getNewWizardShortcuts()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public INavigationHistory getNavigationHistory()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public String getLabel()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IAdaptable getInput()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IExtensionTracker getExtensionTracker()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IEditorPart[] getEditors()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public int getEditorReuseThreshold()
				{
					// TODO Auto-generated method stub
					return 0;
				}
				
				public IEditorReference[] getEditorReferences()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IEditorPart[] getDirtyEditors()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IWorkingSet getAggregateWorkingSet()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IEditorPart getActiveEditor()
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IViewReference findViewReference(String viewId, String secondaryId)
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IViewReference findViewReference(String viewId)
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IViewPart findView(String viewId)
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IEditorReference[] findEditors(IEditorInput input, String editorId, int matchFlags)
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public IEditorPart findEditor(IEditorInput input)
				{
					// TODO Auto-generated method stub
					return null;
				}
				
				public void closePerspective(IPerspectiveDescriptor desc, boolean saveParts, boolean closePage)
				{
					// TODO Auto-generated method stub
					
				}
				
				public boolean closeEditors(IEditorReference[] editorRefs, boolean save)
				{
					// TODO Auto-generated method stub
					return false;
				}
				
				public boolean closeEditor(IEditorPart editor, boolean save)
				{
					// TODO Auto-generated method stub
					return false;
				}
				
				public void closeAllPerspectives(boolean saveEditors, boolean closePage)
				{
					// TODO Auto-generated method stub
					
				}
				
				public boolean closeAllEditors(boolean save)
				{
					// TODO Auto-generated method stub
					return false;
				}
				
				public boolean close()
				{
					// TODO Auto-generated method stub
					return false;
				}
				
				public void bringToTop(IWorkbenchPart part)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void addPropertyChangeListener(IPropertyChangeListener listener)
				{
					// TODO Auto-generated method stub
					
				}
				
				public void activate(IWorkbenchPart part)
				{
					// TODO Auto-generated method stub
					
				}
			};
		}
		return activePage;
	}

	
	public ISelectionProvider getSelectionProvider() {

		if (selectionProvider == null) {
			if (editor instanceof MultiPageEditorPart) {
				selectionProvider = new MultiPageSelectionProvider(
						(MultiPageEditorPart) editor);
			} else {
				selectionProvider = new SimpleSelectionProvider();
			}
		}
		return selectionProvider;
	}

	
	public Shell getShell() {
		return shell;
	}

	
	public IWorkbenchWindow getWorkbenchWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	
	public void setSelectionProvider(ISelectionProvider provider) {
		// TODO Auto-generated method stub

	}

	
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Object getService(Class api) {
		if (api == IPartService.class) {
			return new IPartService() {

				
				public void addPartListener(IPartListener listener) {

				}

				
				public void addPartListener(IPartListener2 listener) {

				}

				
				public IWorkbenchPart getActivePart() {
					return null;
				}

				
				public IWorkbenchPartReference getActivePartReference() {
					return null;
				}

				
				public void removePartListener(IPartListener listener) {

				}

				
				public void removePartListener(IPartListener2 listener) {
				}

			};
		}
		return PlatformUI.getWorkbench().getService(api);
	}

	
	public boolean hasService(Class api) {
		return PlatformUI.getWorkbench().hasService(api);
	}

}
