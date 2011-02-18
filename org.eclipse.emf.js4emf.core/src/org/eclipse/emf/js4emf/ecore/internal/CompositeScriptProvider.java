package org.eclipse.emf.js4emf.ecore.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.js4emf.ecore.ScriptProvider;
import org.mozilla.javascript.Scriptable;

public class CompositeScriptProvider extends JavascriptSupportHelper implements ScriptProvider {

	public CompositeScriptProvider(JavascriptSupportImpl javascriptSupport) {
		super(javascriptSupport);
	}

	private List<ScriptProvider> scriptProviders = new ArrayList<ScriptProvider>();

	public void addScriptProvider(ScriptProvider scriptProvider) {
		scriptProviders.add(scriptProvider);
	}

	public boolean loadScript(Object key, Scriptable scope) {
		for (ScriptProvider scriptProvider : scriptProviders) {
			if (scriptProvider.loadScript(key, scope)) {
				return true;
			}
		}
		return false;
	}
}
