package org.eclipse.emf.js4emf.ecore.internal.featurevalueprovider;

public class NameFeatureValueProvider extends FeatureMatcherFeatureValueProvider<String> {

	public NameFeatureValueProvider() {
		super(new NameAnnotationFeatureMatcher("name"));
	}
}
