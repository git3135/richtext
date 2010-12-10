package com.onpositive.richtexteditor.region_provider;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.ui.editors.text.ILocationProvider;


public class StringStorage extends PlatformObject implements IStorage
{
	protected String contents = null;
	protected File tempFile;
	private String name;

	
	public StringStorage(String name,String contents)
	{
		this.name=name;
		this.contents = contents;
		try
		{
			tempFile = File.createTempFile("contents",null); //TODO If it's not one file?
			FileWriter fw = new FileWriter(tempFile);
			try{
			fw.write(contents);
			}finally{
				fw.close();
			}
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public InputStream getContents() throws CoreException
	{
		if (contents == null) return null;
		byte[] b = contents.getBytes();				
		return new ByteArrayInputStream(b);
	}

	public IPath getFullPath()
	{
		return new Path(tempFile.getAbsolutePath());
	}

	public String getName()
	{
		return name;
	}

	public boolean isReadOnly()
	{
		return false;
	}

	public Object getAdapter(Class adapter)
	{
		if (adapter == ILocationProvider.class)
			return new ILocationProvider()
					{

						
						public IPath getPath(Object element)
						{
							return new Path(tempFile.getAbsolutePath());
						}
			
					};
		return null;
	}

}
