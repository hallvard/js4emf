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
package org.eclipse.e4.emf.ecore.javascript;

import junit.framework.TestCase;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;

public class AbstractJavascriptTest extends TestCase {

	protected JavascriptSupport javascriptSupport;
	protected XMIResource resource;

	protected boolean rethrowException = true;
	
	// must match the path of the one in test1.ecore 
	public final static URI TESTS_URI = URI.createURI("http://www.eclipse.org/e4/emf/ecore/javascript/tests/");

	protected void setUp(String testData) throws Exception {
		super.setUp();
		
		javascriptSupport = new JavascriptSupport();
		URI testUri = URI.createURI(String.valueOf(getClass().getResource(testData)));

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xml", new XMLResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());

		ResourceSetImpl resourceSet = new ResourceSetImpl();
		resourceSet.getLoadOptions().put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);

		// make tests independent of location, by using a logical uri
		URI testFolderUri = JavascriptSupport.createParentFolderUri(testUri);
		resourceSet.getURIConverter().getURIMap().put(TESTS_URI, testFolderUri);
		
		javascriptSupport.setUriConverter(resourceSet.getURIConverter());
		String packageUriString = TESTS_URI.trimSegments(1).appendSegment(testUri.lastSegment()).trimFileExtension().appendFileExtension("ecore").toString();
		javascriptSupport.loadEPackage(packageUriString, null);
		
		resource = (XMIResource)resourceSet.getResource(testUri, true);
	}

	protected Object getFeatureValue(EObject eObject, String featureName) {
		return eObject.eGet(eObject.eClass().getEStructuralFeature(featureName));
	}

	protected void setFeatureValue(EObject eObject, String featureName, Object value) {
		eObject.eSet(eObject.eClass().getEStructuralFeature(featureName), value);
	}
}
