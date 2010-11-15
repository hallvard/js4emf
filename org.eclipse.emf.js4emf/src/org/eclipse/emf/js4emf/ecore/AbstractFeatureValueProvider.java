package org.eclipse.emf.js4emf.ecore;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

public abstract class AbstractFeatureValueProvider<T> implements FeatureValueProvider<T> {

	public EObject getEObject(Notifier notifier) {
		return (notifier instanceof EObject ? (EObject)notifier : null);
	}

	protected abstract EStructuralFeature getStructuralFeature(EObject eObject);

	public EStructuralFeature getFeature(Notifier notifier) {
		EObject eObject = getEObject(notifier);
		if (eObject != null) {
			return getStructuralFeature(eObject);
		}
		return null;
	}

	public T getFeatureValue(Notifier notifier) {
		EObject eObject = getEObject(notifier);
		if (eObject != null) {
			EStructuralFeature feature = getStructuralFeature(eObject);
			if (feature != null) {
				return (T)eObject.eGet(feature);
			}
		}
		return null;
	}

	protected EObject createEObject(Notifier notifier) {
		return null;
	}
	
	public void setFeatureValue(Notifier notifier, T value) {
		EObject eObject = getEObject(notifier);
		if (eObject == null) {
			eObject = createEObject(notifier);
		}
		if (eObject != null) {
			EStructuralFeature feature = getStructuralFeature(eObject);
			if (feature != null) {
				eObject.eSet(feature, value);
			}
		}
	}

	private List<Listener> listeners;

	public void addListener(Listener listener) {
		if (listeners == null) {
			listeners = new ArrayList<Listener>();
		}
		listeners.add(listener);
	}

	public void removeListener(Listener listener) {
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

	protected void fireFeatureValueChanged(Notification notification) {
		if (notification.getNotifier() instanceof Notifier) {
			Notifier notifier = (Notifier)notification.getNotifier();
			if (notification.getFeature() == getFeature(notifier)) {
				fireFeatureValueChanged(notifier);
			}
		}
	}

	protected void fireFeatureValueChanged(Notifier notifier) {
		if (listeners != null) {
			for (Listener listener: listeners) {
				listener.featureValueChanged(notifier, this);
			}
		}
	}
	
	//
	
	public interface FeatureMatcher {
		public boolean matches(EStructuralFeature feature);
	}
	
	public static EStructuralFeature getStructuralFeature(EObject eObject, FeatureMatcher featureMatcher) {
		for (EStructuralFeature feature: eObject.eClass().getEAllStructuralFeatures()) {
			if (featureMatcher.matches(feature)) {
				return feature;
			}
		}
		return null;
	}
}
