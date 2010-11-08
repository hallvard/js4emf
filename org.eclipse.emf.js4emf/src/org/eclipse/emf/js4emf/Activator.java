/*******************************************************************************
 * Copyright (c) 2008 Hallvard Traetteberg.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Hallvard Traetteberg - initial API and implementation
 ******************************************************************************/
package org.eclipse.emf.js4emf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.js4emf.ecore.internal.ESuperTypesIterator;
import org.eclipse.emf.js4emf.ecore.internal.featurevalueprovider.FeatureMatcherFeatureValueProvider;
import org.eclipse.emf.js4emf.ecore.internal.featurevalueprovider.FeatureValueProvider;
import org.eclipse.emf.js4emf.ecore.internal.featurevalueprovider.NameAnnotationFeatureMatcher;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.emf.javascript";

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

	private Map<URI, URI> packageUriMap = null;

	public void addRegisteredPackages(Map<URI, URI> uriMap) {
		if (packageUriMap == null) {
			initPackageUriMap();
		}
		for (Map.Entry<URI, URI> entry : packageUriMap.entrySet()) {
			URI key = Util.createParentFolderUri(entry.getKey());
			URI value = Util.createParentFolderUri(entry.getValue());
			URI oldValue = uriMap.get(key);
			if (oldValue == null) {
				uriMap.put(key, value);
			} else if (! value.equals(oldValue)) {
				getLog().log(new Status(Status.WARNING, PLUGIN_ID, "Tried to map " + key + " to " + value + ", but it already maps to " + oldValue));
			}
		}
	}

	private void initPackageUriMap() {
		packageUriMap = new HashMap<URI, URI>();
		registerGeneratedPackages();
		registerDynamicPackages();
	}

	private void registerPackage(URI packageUri, URI location) {
		packageUriMap.put(packageUri, location);
	}
	
	private void registerGeneratedPackages() {
		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.emf.ecore.generated_package");
		IExtension[] extensions = ep.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			for (IConfigurationElement ces: extensions[i].getConfigurationElements()) {
				String name = ces.getName();
				if ("package".equals(name)) {
					
//		            uri="http://www.eclipse.org/e4/tm/widgets.ecore" 
//		            class="org.eclipse.e4.tm.widgets.impl.WidgetsPackageImpl"
//		            genModel="model/tm.genmodel"/>

					URI uri =  URI.createURI(ces.getAttribute("uri"));
					String genModel = ces.getAttribute("genModel");
					URI pluginUri = URI.createPlatformPluginURI("/" + extensions[i].getNamespaceIdentifier() + "/" + genModel, true);

					registerPackage(uri, pluginUri);
				}
			}
		}
	}

	private void registerDynamicPackages() {
		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.emf.ecore.dynamic_package");
		IExtension[] extensions = ep.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			for (IConfigurationElement ces: extensions[i].getConfigurationElements()) {
				String name = ces.getName();
				if ("resource".equals(name)) {

//		           uri="http://www.eclipse.org/gmt/2005/ATL">
//			       location="platform:/plugin/org.eclipse.m2m.atl.common/src/org/eclipse/m2m/atl/common/resources/ATL.ecore"
					
					URI uri =  URI.createURI(ces.getAttribute("uri"));
					URI pluginUri = URI.createURI(ces.getAttribute("location"));
					
					registerPackage(uri, pluginUri);
				}
			}
		}
	}
	
	private Map<String, FeatureValueProvider<?>> featureValueProviderMap = null;
	
	private void processFeatureValueProviderExtensionPoint() {
		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint(getBundle().getSymbolicName() + ".featureValueProvider");
		IExtension[] extensions = ep.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			for (IConfigurationElement ces: extensions[i].getConfigurationElements()) {
				String name = ces.getName();
				if ("package".equals(name)) {
					String uri = ces.getAttribute("nsUri");
					String featureName = ces.getAttribute("featureName");
					FeatureValueProvider<?> featureValueProvider = null;
					String className = ces.getAttribute("featureValueProvider");
					if (className != null) {
						try {
							featureValueProvider = (FeatureValueProvider<?>) ces.createExecutableExtension("featureValueProvider");
						} catch (CoreException e) {
						}
					} else {
						featureValueProvider = new FeatureMatcherFeatureValueProvider(new NameAnnotationFeatureMatcher(featureName));
					}
					if (featureValueProvider != null) {
						featureValueProviderMap.put(getFeatureValueProviderKey(featureName, uri), featureValueProvider);
					}
				}
			}
		}
	}
	
	private String getFeatureValueProviderKey(String featureName, String uri) {
		return uri + "?featureName=" + featureName;
	}
	
	public FeatureValueProvider<?> getFeatureValueProvider(String featureName, String uri) {
		if (featureValueProviderMap == null) {
			featureValueProviderMap = new HashMap<String, FeatureValueProvider<?>>();
			processFeatureValueProviderExtensionPoint();
		}
		return featureValueProviderMap.get(getFeatureValueProviderKey(featureName, uri));
	}

	public <T> FeatureValueProvider<T> getFeatureValueProvider(String featureName, EObject eObject) {
		Iterator<EClass> superTypes = new ESuperTypesIterator(eObject instanceof EClass ? (EClass) eObject : eObject.eClass());
		while (superTypes.hasNext()) { 
			EClass eClass = superTypes.next();
			String packageUri = eClass.getEPackage().getNsURI();
			String classUri = packageUri + "#" + eClass.getName();
			FeatureValueProvider<?> featureValueProvider = getFeatureValueProvider(featureName, classUri);
			if (featureValueProvider == null) {
				featureValueProvider = getFeatureValueProvider(featureName, packageUri);
			}
			if (featureValueProvider != null) {
				return (FeatureValueProvider<T>)featureValueProvider;
			}
		}
		return null;
	}
}
