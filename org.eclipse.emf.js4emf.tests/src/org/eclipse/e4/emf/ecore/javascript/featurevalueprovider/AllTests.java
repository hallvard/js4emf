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
package org.eclipse.e4.emf.ecore.javascript.featurevalueprovider;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.e4.emf.ecore.javascript.featurevalueprovider");
		//$JUnit-BEGIN$
		suite.addTestSuite(NameAnnotationFeatureMatcherTest.class);
		suite.addTestSuite(FeatureMatcherFeatureValueProviderTest.class);
		//$JUnit-END$
		return suite;
	}

}
