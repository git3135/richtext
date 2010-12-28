package com.onpositive.richtexteditor.region_provider;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.EditorDescriptor;

import com.onpositive.richtext.model.IRegionCompositeWrapper;
import com.onpositive.richtext.model.RegionPartition;
import com.onpositive.richtexteditor.model.AbstractLayerManager;



public class ContentTypeEditorProvider
{
	/**
	 * Returns an inner editor for some content.
	 * In the filename, we really need only extension - it's fake 
	 * @param fakeFileName Fake file name, which must have content type corresponding extension, e.g.
	 * "a.java" for java contents
	 * @param contents Real content to create an editor for
	 * @param parent Parent composite for an editor
	 * @return Editor composite
	 */
	public static IRegionCompositeWrapper getEditorForContentTypeWithFakeFileName(String fakeFileName, String contents, Composite parent,AbstractLayerManager manager,RegionPartition partition)
	{
		fakeFileName = fakeFileName.toLowerCase();
		if (PlatformUI.getWorkbench() == null)
			throw new RuntimeException("No workbench found! You must create a workbench for using ContentTypeEditorProvider!");
		try
		{
			int lastIdx = fakeFileName.lastIndexOf(".");
			if (lastIdx == -1) return null;
			String substring = fakeFileName.substring(0,lastIdx);
			StringBuilder sb = new StringBuilder();
			for (int i = substring.length() - 1; i >= 0; i--)
			{
				if (Character.isJavaIdentifierPart(substring.charAt(i)))
					sb.append(substring.charAt(i));
				else 
					break;
			}
			sb.reverse();
			if (sb.length() < 3) substring = "aaa";
			else 
				substring = sb.toString();
			File tmpFile = File.createTempFile(substring, fakeFileName.substring(lastIdx, fakeFileName.length()));
			
			tmpFile.deleteOnExit();
			IEditorDescriptor defaultEditor = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(tmpFile.getAbsolutePath());
			if (defaultEditor == null){
				defaultEditor = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor("a.txt");
			}
			
			IEditorPart newEditor; 
			try{
				if (defaultEditor.getId().equals("org.eclipse.ui.browser.editorSupport")){
					newEditor=((EditorDescriptor)PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor("a.txt")).createEditor();	
				}
				else{
				newEditor=((EditorDescriptor)defaultEditor).createEditor();
				}
			}catch (Exception e) {
				e.printStackTrace();
				newEditor=((EditorDescriptor)PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor("a.txt")).createEditor();
			}
			if (defaultEditor instanceof EditorDescriptor)
			{
				StringEditorInput input = new StringEditorInput( tmpFile.getName(),contents);
				newEditor.init(new DummyEditorSite(newEditor, "editor", parent), input);
				final Composite con=new Composite(parent,SWT.BORDER);// | SWT.DOUBLE_BUFFERED);
				//con.setToolTipText("TABLEEDITORR!!!!!!");
				con.setLayout(new FillLayout());	
				final IEditorPart editor = newEditor;
				newEditor.createPartControl(con);
				/*if (UIUtil.getActivePage() != null)
					newEditor.createPartControl(con);
				else
				{
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPageListener( new IPageListener()
					{
						
						public void pageOpened(IWorkbenchPage page)
						{
						}
						
						public void pageClosed(IWorkbenchPage page)
						{
							
						}
						
						public void pageActivated(IWorkbenchPage page)
						{
							editor.createPartControl(con);
							
						}
					});
					return null;
				}*/
				if(newEditor instanceof ILayerConnector){
				//	con.setLayout(new GridLayout(1,false));
				//	con.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					return ((ILayerConnector)newEditor).connect(manager,partition);								
				}
				return new EditorPartWrapper(con, newEditor);
			}
		} catch (CoreException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}
	
	public static IRegionCompositeWrapper getEditorForContentType(String fileName, Composite parent)
	{		
			try
			{				
				File inputFile = new File(fileName);
				byte[] arr = new byte[(int) inputFile.length()];
				FileInputStream stream = new FileInputStream(inputFile);
				try{
				stream.read(arr);
				}finally{
					stream.close();
				}
				return getEditorForContentTypeWithFakeFileName(fileName, new String(arr), parent, null,null);
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return null;
	}
}
