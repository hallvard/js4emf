package org.eclipse.emf.js4emf.ecore.internal.featurevalueprovider;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.js4emf.ecore.internal.featurevalueprovider.AbstractFeatureValueProvider.FeatureMatcher;

public class NameAnnotationFeatureMatcher implements FeatureMatcher {

	private String featureName, annotationSource, annotationKey;
	
	public NameAnnotationFeatureMatcher(String featureName, String annotationSource, String annotationKey) {
		this.featureName = featureName;
		this.annotationSource = annotationSource;
		this.annotationKey = annotationKey;
	}
	public NameAnnotationFeatureMatcher(String featureName) {
		this.featureName = featureName;
	}
	public NameAnnotationFeatureMatcher(String annotationSource, String annotationKey) {
		this.annotationSource = annotationSource;
		this.annotationKey = annotationKey;
	}
	
	public boolean matches(EStructuralFeature feature) {
		if (featureName != null && (! featureName.equals(feature.getName()))) {
			return false;
		}
		if (annotationSource != null && annotationKey != null) {
			String annotation = EcoreUtil.getAnnotation(feature, annotationSource, annotationKey);
			if (annotation == null) {
				return false;
			}
		}
		return true;
	}
}