/*******************************************************************************
 * Copyright (c) 2009 Hallvard Traetteberg.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Hallvard Traetteberg - initial API and implementation
 ******************************************************************************/
package org.eclipse.emf.js4emf.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.emf.js4emf.ui";

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		addLoggerHandler(getBundle());
		addLoggerHandler(org.eclipse.emf.js4emf.Activator.getDefault().getBundle());
	}

	public void addLoggerHandler(Bundle bundle) {
		Logger.getLogger(bundle.getSymbolicName()).addHandler(new PlatformLoggerHandler(bundle));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public MessageConsole getConsole(String name) {
		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		IConsole[] existing = consoleManager.getConsoles();
		for (int i = 0; i < existing.length; i++) {
			if (name.equals(existing[i].getName())) {
				return (MessageConsole) existing[i];
			}
		}
		//no console found, so create a new one
		MessageConsole console = new MessageConsole(name, null);
		consoleManager.addConsoles(new IConsole[]{console});
		return console;
	}

	public void showConsole(String name) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			IConsoleView view = (IConsoleView)page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
			view.display(getConsole(name));
		} catch (PartInitException e) {
		}
	}
	
	//

	private Map<String, IDelegatesScriptSourceFeatureValueProviders> delegatesScriptSourceFeatureValueProvidersMap = null;
	
	private String getAttribute(IConfigurationElement ces, String attributeName, String def) {
		String value = ces.getAttribute(attributeName);
		return (value != null && value.trim().length() > 0 ? value : def);
	}
	
	private void processFeatureValueProviderExtensionPoint() {
		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint(getBundle().getSymbolicName() + ".delegatesScriptSourceProvider");
		IExtension[] extensions = ep.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			for (IConfigurationElement ces: extensions[i].getConfigurationElements()) {
				String name = ces.getName();
				if ("delegatesScriptSourceProvider".equals(name)) {
					String uri = ces.getAttribute("uri");
					String providerName = getAttribute(ces, "name", uri.substring(uri.lastIndexOf('/') + 1));
					String settingDelegateKey = getAttribute(ces, "settingDelegateKey", providerName);
					String invocationDelegateKey = getAttribute(ces, "invocationDelegateKey", providerName);
					IDelegatesScriptSourceFeatureValueProviders delegatesScriptSourceFeatureValueProviders = 
						new DelegatesScriptSourceFeatureValueProviders(uri, settingDelegateKey, invocationDelegateKey);
					if (delegatesScriptSourceFeatureValueProviders != null) {
						delegatesScriptSourceFeatureValueProvidersMap.put(providerName, delegatesScriptSourceFeatureValueProviders);
					}
				}
			}
		}
	}
	
	private Map<String, IDelegatesScriptSourceFeatureValueProviders> getDelegatesScriptSourceFeatureValueProvidersMap() {
		if (delegatesScriptSourceFeatureValueProvidersMap == null) {
			delegatesScriptSourceFeatureValueProvidersMap = new HashMap<String, IDelegatesScriptSourceFeatureValueProviders>();
			processFeatureValueProviderExtensionPoint();
		}
		return delegatesScriptSourceFeatureValueProvidersMap;
	}

	public IDelegatesScriptSourceFeatureValueProviders getDelegatesScriptSourceFeatureValueProviders(String providerName) {
		return getDelegatesScriptSourceFeatureValueProvidersMap().get(providerName);
	}
	
	public String[] getDelegatesScriptSourceFeatureValueProviderNames() {
		Set<String> providerNames = getDelegatesScriptSourceFeatureValueProvidersMap().keySet();
		return providerNames.toArray(new String[providerNames.size()]);
	}
}
