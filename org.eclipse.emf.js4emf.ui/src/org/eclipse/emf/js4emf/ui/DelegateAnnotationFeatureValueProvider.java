package org.eclipse.emf.js4emf.ui;

import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;

public class DelegateAnnotationFeatureValueProvider extends AnnotationFeatureValueProvider {

	private String delegateKind;

	public DelegateAnnotationFeatureValueProvider(String uri, String key, String delegateKind) {
		super(uri, key);
		this.delegateKind = delegateKind;
	}

	@Override
	protected void setFeatureValue(EModelElement annotatedElement, String value) {
		super.setFeatureValue(annotatedElement, value);
		ensurePackageAnnotation(annotatedElement, delegateKind, uri);
	}

	public static void ensurePackageAnnotation(EObject eObject, String delegateKind, String providerUri) {
		while (eObject != null && (! (eObject instanceof EPackage))) {
			eObject = eObject.eContainer();
		}
		if (eObject instanceof EPackage) {
			EPackage ePackage = (EPackage) eObject;
			ensureEAnnotation(ePackage, EcorePackage.eNS_URI, delegateKind, providerUri);
		}
	}
}
