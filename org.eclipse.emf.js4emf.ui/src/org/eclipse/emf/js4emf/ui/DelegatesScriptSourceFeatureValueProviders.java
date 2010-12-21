package org.eclipse.emf.js4emf.ui;

import org.eclipse.emf.js4emf.ecore.FeatureValueProvider;

class DelegatesScriptSourceFeatureValueProviders implements IDelegatesScriptSourceFeatureValueProviders {
	
	private String delegatesUri, settingDelegateKey, invocationDelegateKey;

	public DelegatesScriptSourceFeatureValueProviders(String uri, String settingDelegateKey, String invocationDelegateKey) {
		super();
		this.delegatesUri = uri;
		this.settingDelegateKey = settingDelegateKey;
		this.invocationDelegateKey = invocationDelegateKey;
	}

	public String getScriptSourceProviderUri() {
		return delegatesUri;
	}

	public FeatureValueProvider<String> getSettingDelegateScriptSourceProvider() {
		return new DelegateAnnotationFeatureValueProvider(delegatesUri, settingDelegateKey, "settingDelegates");
	}
	
	public FeatureValueProvider<String> getInvocationDelegateScriptSourceProvider() {
		return new DelegateAnnotationFeatureValueProvider(delegatesUri, invocationDelegateKey, "invocationDelegates");
	}
	
	public FeatureValueProvider<String> getConstraintDelegateScriptSourceProvider(String constraint) {
		return new DelegateAnnotationFeatureValueProvider(delegatesUri, constraint, "validationDelegates");
	}
	
	public FeatureValueProvider<String> getInvariantDelegateScriptSourceProvider() {
		return new DelegateAnnotationFeatureValueProvider(delegatesUri, "body", "validationDelegates");
	}
}
