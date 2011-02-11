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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.emf.js4emf.ecore.internal.delegates.JavascriptDelegatesTest;
import org.eclipse.emf.js4emf.ecore.internal.featurevalueprovider.FeatureMatcherFeatureValueProviderTest;
import org.eclipse.emf.js4emf.ecore.internal.featurevalueprovider.NameAnnotationFeatureMatcherTest;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.e4.emf.ecore.javascript");
		//$JUnit-BEGIN$
		suite.addTestSuite(JavascriptSupportTest.class);
		suite.addTestSuite(EcoreJavascriptTest.class);
		suite.addTestSuite(JsWrapperTest.class);
		suite.addTestSuite(JavascriptDelegatesTest.class);
		suite.addTestSuite(FeatureMatcherFeatureValueProviderTest.class);
		suite.addTestSuite(NameAnnotationFeatureMatcherTest.class);
		//$JUnit-END$
		return suite;
	}

}
