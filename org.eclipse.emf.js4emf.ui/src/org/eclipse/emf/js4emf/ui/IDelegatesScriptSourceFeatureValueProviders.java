package org.eclipse.emf.js4emf.ui;

import org.eclipse.emf.js4emf.ecore.FeatureValueProvider;

public interface IDelegatesScriptSourceFeatureValueProviders {
	public String getScriptSourceProviderUri();
	public FeatureValueProvider<String> getSettingDelegateScriptSourceProvider();
	public FeatureValueProvider<String> getInvocationDelegateScriptSourceProvider();
	public FeatureValueProvider<String> getConstraintDelegateScriptSourceProvider(String constraint);
	public FeatureValueProvider<String> getInvariantDelegateScriptSourceProvider();
}
