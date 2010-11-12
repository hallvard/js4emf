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
package org.eclipse.emf.js4emf.ecore.internal;

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
import org.eclipse.emf.js4emf.ecore.JavascriptSupport;
import org.eclipse.emf.js4emf.ecore.JavascriptSupportFactory;

public class AbstractJavascriptTest extends TestCase {

	protected JavascriptSupport javascriptSupport;
	protected XMIResource resource;

	// must match the path of the one in test1.ecore 
	public final static URI TESTS_URI = URI.createURI("http://www.eclipse.org/e4/emf/ecore/javascript/tests/");

	private URI createParentFolderUri(URI uri) {
		String lastSegment = uri.lastSegment();
		return (lastSegment == null || lastSegment.length() == 0 ? uri : uri.trimSegments(1).appendSegment(""));
	}
	
	// evaluate in both IJsObject and IJsScope
	protected Object evaluateInObject(Object object, String script) {
		return javascriptSupport.getJsObject(object).evaluate(script);
	}
	protected Object evaluateInScope(Object object, String script) {
		return javascriptSupport.getJsObject(object).evaluate(script);
	}
	
	// IJsObject methods
	protected Object getProperty(Object object, String name) {
		return javascriptSupport.getJsObject(object).getProperty(name);
	}
	protected Object callMethod(Object object, String name, Object args) {
		return javascriptSupport.getJsObject(object).callMethod(name, args);
	}
	
	// IJsScope methods
	protected Object callFunction(Object object, String name, Object args) {
		return javascriptSupport.getJsScope(object).callFunction(name, args);
	}
	protected Object getVariable(Object object, String name) {
		return javascriptSupport.getJsScope(object).getVariable(name);
	}
	
	protected void setUp(String testData) throws Exception {
		super.setUp();
		
		URI testUri = URI.createURI(String.valueOf(getClass().getResource(testData)));

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xml", new XMLResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());

		ResourceSetImpl resourceSet = new ResourceSetImpl();
		resourceSet.getLoadOptions().put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
		// make tests independent of location, by using a logical uri
		URI testFolderUri = createParentFolderUri(testUri);
		resourceSet.getURIConverter().getURIMap().put(TESTS_URI, testFolderUri);

		resource = (XMIResource)resourceSet.getResource(testUri, true);
		javascriptSupport = JavascriptSupportFactory.getInstance().getJavascriptSupport(resource);

		javascriptSupport.setUriConverter(resourceSet.getURIConverter());
		
//		String packageUriString = TESTS_URI.trimSegments(1).appendSegment(testUri.lastSegment()).trimFileExtension().appendFileExtension("ecore").toString();
	}

	protected Object getFeatureValue(EObject eObject, String featureName) {
		return eObject.eGet(eObject.eClass().getEStructuralFeature(featureName));
	}

	protected void setFeatureValue(EObject eObject, String featureName, Object value) {
		eObject.eSet(eObject.eClass().getEStructuralFeature(featureName), value);
	}
}
