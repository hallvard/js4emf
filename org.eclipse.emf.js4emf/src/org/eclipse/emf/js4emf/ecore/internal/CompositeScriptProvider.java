package org.eclipse.emf.js4emf.ecore.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.js4emf.ecore.ScriptProvider;
import org.mozilla.javascript.Scriptable;

public class CompositeScriptProvider implements ScriptProvider {

	private List<ScriptProvider> scriptProviders = new ArrayList<ScriptProvider>();

	public void addScriptProvider(ScriptProvider scriptProvider) {
		scriptProviders.add(scriptProvider);
	}

	public boolean loadScript(Object key, Scriptable scope, JavascriptSupport javascriptSupport) {
		for (ScriptProvider scriptProvider : scriptProviders) {
			if (scriptProvider.loadScript(key, scope, javascriptSupport)) {
				return true;
			}
		}
		return false;
	}
}
