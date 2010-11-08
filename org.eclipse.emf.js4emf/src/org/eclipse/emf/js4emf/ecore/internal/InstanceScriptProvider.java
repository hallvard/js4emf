package org.eclipse.emf.js4emf.ecore.internal;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.js4emf.Activator;
import org.eclipse.emf.js4emf.ecore.ScriptProvider;
import org.eclipse.emf.js4emf.ecore.internal.featurevalueprovider.FeatureMatcherFeatureValueProvider;
import org.eclipse.emf.js4emf.ecore.internal.featurevalueprovider.FeatureValueProvider;
import org.eclipse.emf.js4emf.ecore.internal.featurevalueprovider.NameAnnotationFeatureMatcher;
import org.mozilla.javascript.Scriptable;

public class InstanceScriptProvider implements ScriptProvider {

	public boolean loadScript(final Object key, final Scriptable scope, final JavascriptSupport javascriptSupport) {
		if (key instanceof EObject) {
			final EObject eObject = (EObject) key;
			final FeatureValueProvider<String> scriptSourceFeatureValueProvider = getScriptSourceFeatureValueProvider(eObject);
			if (scriptSourceFeatureValueProvider != null) {
				evaluateInstanceScript(eObject, scriptSourceFeatureValueProvider, scope, javascriptSupport);
				scriptSourceFeatureValueProvider.addListener(new FeatureValueProvider.Listener() {
					public void featureValueChanged(Notifier notifier, FeatureValueProvider<?> featureValueProvider) {
						evaluateInstanceScript(eObject, scriptSourceFeatureValueProvider, scope, javascriptSupport);
					}
				});
				return true;
			}
		}
		return false;
	}

	private void evaluateInstanceScript(final EObject eObject, FeatureValueProvider<String> featureValueProvider, Scriptable scope, JavascriptSupport javascriptSupport) {
		String scriptSource = featureValueProvider.getFeatureValue(eObject);
		if (scriptSource != null) {
			javascriptSupport.evaluate(String.valueOf(scriptSource), JavascriptSupport.SCRIPT_SOURCE_FEATURE_NAME, scope, true);
		}
	}

	public static FeatureValueProvider<String> defaultScriptSourceFeatureValueProvider = new FeatureMatcherFeatureValueProvider<String>(new NameAnnotationFeatureMatcher(JavascriptSupport.SCRIPTING_SOURCE_FEATURE_URI, JavascriptSupport.JAVASCRIPT_EXTENSION));
	
	public static FeatureValueProvider<String> getScriptSourceFeatureValueProvider(EObject eObject) {
		FeatureValueProvider<String> scriptSourceFeatureValueProvider = null;
		if (Activator.getDefault() != null) {
			scriptSourceFeatureValueProvider = Activator.getDefault().getFeatureValueProvider(JavascriptSupport.SCRIPT_SOURCE_FEATURE_NAME, eObject);
		}
		if (scriptSourceFeatureValueProvider == null) {
			scriptSourceFeatureValueProvider = defaultScriptSourceFeatureValueProvider;
		}
		return scriptSourceFeatureValueProvider;
	}
}
