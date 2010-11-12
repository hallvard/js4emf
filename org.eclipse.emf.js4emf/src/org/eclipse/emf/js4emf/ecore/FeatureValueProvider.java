package org.eclipse.emf.js4emf.ecore;

import org.eclipse.emf.common.notify.Notifier;

/**
 * This interface is implemented by classes that store/accesses features values in a model-independent manner.
 * By implementing this interface, clients are made less dependent on how features are stored.
 * Use cases:
 * - eClass scripts in files
 * - eObject scripts in annotated features
 * - eObject names in annotated features
 * 
 * @author hal
 *
 * @param <T> the feature value type
 */
public interface FeatureValueProvider<T> {
	
	/**
	 * Listener interface, so clients can be notified if a feature value changes.
	 * Listeners are attached to the provider, not the individual Notifiers
	 * 
	 * @author hal
	 *
	 */
	public interface Listener {
		public void featureValueChanged(Notifier notifier, FeatureValueProvider<?> featureValueProvider);
	}
	
	/**
	 * Get's the feature value of the provided Notifier
	 * @param notifier the Notifier
	 * @return the feature value or null if the Notifier does not have a value for this feature
	 */
	public T getFeatureValue(Notifier notifier);
	
	/**
	 * Sets the feature value of the provided Notifier
	 * @param notifier the Notifier
	 * @param value the new value
	 * @throws IllegalArgumentException, if the feature couldn't be set
	 */
	public void setFeatureValue(Notifier notifier, T value);
	
	public void addListener(Listener listener);
	public void removeListener(Listener listener);
}
