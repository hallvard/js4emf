package org.eclipse.emf.js4emf.ecore.internal.featurevalueprovider;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.js4emf.ecore.AbstractFeatureValueProvider;

public class FeatureMatcherFeatureValueProvider<T> extends AbstractFeatureValueProvider<T> {

	private FeatureMatcher featureMatcher;
	
	public FeatureMatcherFeatureValueProvider(FeatureMatcher featureMatcher) {
		this.featureMatcher = featureMatcher;
	}
	
	@Override
	protected EStructuralFeature getStructuralFeature(EObject eObject) {
		return getStructuralFeature(eObject, featureMatcher);
	}
	
	protected EContentAdapter adapter = new EContentAdapter() {
		@Override
		public void notifyChanged(Notification notification) {
			fireFeatureValueChanged(notification);
		}
	};

	public void setTarget(Notifier target) {
		target.eAdapters().add(adapter);
		adapter.setTarget(target);
	}
}
