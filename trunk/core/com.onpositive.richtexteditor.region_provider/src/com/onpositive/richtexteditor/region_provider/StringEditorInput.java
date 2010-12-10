package com.onpositive.richtexteditor.region_provider;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;


public class StringEditorInput extends PlatformObject implements IStorageEditorInput
{
	StringStorage storage;
	
	
	public StringEditorInput(String name,String initialStr)
	{
		storage = new StringStorage(name,initialStr);
	}
	
	public IStorage getStorage() throws CoreException
	{		
		return storage;
	}

	public boolean exists()
	{
		if (storage != null) return true;
		return false;
	}

	public ImageDescriptor getImageDescriptor()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getName()
	{
		return storage.getName();
	}

	public IPersistableElement getPersistable()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getToolTipText()
	{
		return "String input";
	}

	/*public Object getAdapter(Class adapter)
	{
		// TODO Auto-generated method stub
		return null;
	}*/

}
