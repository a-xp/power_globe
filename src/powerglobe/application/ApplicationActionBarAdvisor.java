/*!
 * @file
 * Copyright (c) jdknight. All rights reserved.
 *
 * The MIT License (MIT).
 */

package powerglobe.application;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;

/**
 * Advisor (configurer) for the action bar of the workbench window.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor
{
	/**
	 * Initializes a new instance of ApplicationActionBarAdvisor.
	 *  
	 * @param configurer The action bar configurer.
	 */
	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer)
	{
		super(configurer);
	}
	
	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
		removeUnusedActions();
	}
	
	protected void removeUnusedActions() {
		
	     /*Remove actions tip from: http://random-eclipse-tips.blogspot.de/2009/02/eclipse-rcp-removing-unwanted_02.html*/
			final ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();
			final IActionSetDescriptor[] actionSets = reg.getActionSets();
			final String[] removeActionSets = new String[] { "org.eclipse.search.searchActionSet",
					"org.eclipse.ui.cheatsheets.actionSet", "org.eclipse.ui.actionSet.keyBindings",
					"org.eclipse.ui.edit.text.actionSet.navigation", "org.eclipse.ui.edit.text.actionSet.annotationNavigation",
					"org.eclipse.ui.edit.text.actionSet.convertLineDelimitersTo", "org.eclipse.ui.edit.text.actionSet.openExternalFile",
					"org.eclipse.ui.externaltools.ExternalToolsSet", "org.eclipse.ui.WorkingSetActionSet",
					"org.eclipse.update.ui.softwareUpdates", "org.eclipse.ui.actionSet.openFiles",
					"org.eclipse.mylyn.tasks.ui.navigation", };

			for (int i = 0; i < actionSets.length; i++) {
				boolean found = false;
				for (int j = 0; j < removeActionSets.length; j++) {
					if (removeActionSets[j].equals(actionSets[i].getId())) {
						found = true;
					}
				}
				if (!found) {
					continue;
				}
				final IExtension ext = actionSets[i].getConfigurationElement().getDeclaringExtension();
				reg.removeExtension(ext, new Object[] { actionSets[i] });
			}
	}
}
