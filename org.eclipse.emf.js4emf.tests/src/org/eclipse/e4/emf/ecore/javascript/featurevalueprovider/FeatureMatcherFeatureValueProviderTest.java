package org.eclipse.e4.emf.ecore.javascript.featurevalueprovider;

import junit.framework.TestCase;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;

public class FeatureMatcherFeatureValueProviderTest extends TestCase {

	private FeatureMatcherFeatureValueProvider<String> featureValueProvider;
	private EClass eClass;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		featureValueProvider = new FeatureMatcherFeatureValueProvider<String>(new NameAnnotationFeatureMatcher("name"));
		eClass = EcoreFactory.eINSTANCE.createEClass();
		featureValueProvider.setTarget(eClass);
	}

	public void testGetEObject() {
		assertEquals(eClass, featureValueProvider.getEObject(eClass));
	}
	
	public void testGetStructuralFeature() {
		assertEquals(EcorePackage.eINSTANCE.getENamedElement_Name(), featureValueProvider.getStructuralFeature(eClass));
	}

	private Notifier featureValueChangedNotifier;
	private FeatureValueProvider<?> featureValueChangedFeatureValueProvider;
	
	public void testGetFeatureValue() {
		String nameValue = "A different name";
		eClass.setName(nameValue);
		assertEquals(nameValue, featureValueProvider.getFeatureValue(eClass));
	}
	
	public void testSetFeatureValue() {
		String nameValue = "A different name";
		featureValueProvider.setFeatureValue(eClass, nameValue);
		assertEquals(nameValue, eClass.getName());
	}
	
	public void testFeatureValueChanged() {
		featureValueProvider.addListener(new FeatureValueProvider.Listener() {
			public void featureValueChanged(Notifier notifier, FeatureValueProvider<?> featureValueProvider) {
				featureValueChangedNotifier = notifier;
				featureValueChangedFeatureValueProvider = featureValueProvider;
			}
		});
		eClass.setName("A different name");
		assertEquals(eClass, featureValueChangedNotifier);
		assertEquals(featureValueProvider, featureValueChangedFeatureValueProvider);
	}
}
