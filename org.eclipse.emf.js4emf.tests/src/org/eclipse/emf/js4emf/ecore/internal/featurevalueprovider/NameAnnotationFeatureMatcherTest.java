package org.eclipse.emf.js4emf.ecore.internal.featurevalueprovider;

import junit.framework.TestCase;

import org.eclipse.emf.ecore.EcorePackage;

public class NameAnnotationFeatureMatcherTest extends TestCase {

	private NameAnnotationFeatureMatcher featureMatcher;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		featureMatcher = new NameAnnotationFeatureMatcher("name", null, null);
	}

	public void testFeatureMatcher() {
		assertTrue(featureMatcher.matches(EcorePackage.eINSTANCE.getENamedElement_Name()));
	}
}
