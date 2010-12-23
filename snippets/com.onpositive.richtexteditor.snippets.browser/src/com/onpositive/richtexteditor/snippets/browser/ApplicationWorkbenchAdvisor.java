package com.onpositive.richtexteditor.snippets.browser;

import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private static final String PERSPECTIVE_ID = "com.onpositive.richtexteditor.snippets.browser.perspective";

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}
	
	@Override
	protected IWorkbenchConfigurer getWorkbenchConfigurer()
	{
		IWorkbenchConfigurer workbenchConfigurer = super.getWorkbenchConfigurer();
		workbenchConfigurer.setSaveAndRestore(true);
		return workbenchConfigurer;
	}

}
