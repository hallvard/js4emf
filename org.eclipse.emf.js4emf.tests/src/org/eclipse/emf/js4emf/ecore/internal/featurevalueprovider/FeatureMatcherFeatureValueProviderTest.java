package org.eclipse.emf.js4emf.ecore.internal.featurevalueprovider;

import junit.framework.TestCase;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.js4emf.ecore.FeatureValueProvider;

public class FeatureMatcherFeatureValueProviderTest extends TestCase {

	private static class FeatureMatcherFeatureValueProviderSubclass extends FeatureMatcherFeatureValueProvider<String> {
		public FeatureMatcherFeatureValueProviderSubclass(FeatureMatcher featureMatcher) {
			super(featureMatcher);
		}
		
		@Override
		public EStructuralFeature getStructuralFeature(EObject eObject) {
			return super.getStructuralFeature(eObject);
		}
	}
	
	private FeatureMatcherFeatureValueProviderSubclass featureValueProvider;
	private EClass eClass;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		featureValueProvider = new FeatureMatcherFeatureValueProviderSubclass(new NameAnnotationFeatureMatcher("name"));
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
