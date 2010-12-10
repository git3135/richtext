package com.onpositive.richtexteditor.actions;

import org.eclipse.jface.action.Action;

import com.onpositive.richtext.model.BasePartitionLayer;

/**
 * @author 32kda
 * This action can determine, is it enabled or disabled at moment
 */
public abstract class EnabledAction extends Action
{
	/**
	 * Action should be disabled
	 */
	public static final int DISABLED = 0;
	/**
	 * Action should be enabled (but not checked)
	 */
	public static final int ENABLED = 1;
	/**
	 * Action should be checked 
	 */
	public static final int CHECKED = 2;

	public EnabledAction(String text, int style)
	{
		super(text, style);
	}
	
	/**
	 * Method checks, whether this action should be enabled or disabled at this moment
	 * @param layer Layer, containing information about document, it's partitions and line attributes
	 * @param offset Selection offset
	 * @param length TODO
	 * @return EnabledAction.DISABLED, if an action should be disabled
	 * 		   EnabledAction.ENABLED, if an action should be enabled (but not selected, if selectable)
	 * 	       EnabledAction.CHECKED, if an action should be checked
	 */
	public abstract int getEnabled(BasePartitionLayer layer, int offset, int length);
}
