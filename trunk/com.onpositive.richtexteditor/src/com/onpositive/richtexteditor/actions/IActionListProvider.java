package com.onpositive.richtexteditor.actions;

import java.util.ArrayList;

import org.eclipse.jface.action.IContributionItem;

/**
 * Iterface for action list providers
 * @author 32kda
 *
 */
public interface IActionListProvider {
	public ArrayList<IContributionItem> getActionsList();
}
