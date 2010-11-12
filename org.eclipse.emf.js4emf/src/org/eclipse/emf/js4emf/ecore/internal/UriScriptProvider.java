package org.eclipse.emf.js4emf.ecore.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.js4emf.ecore.ScriptProvider;
import org.mozilla.javascript.Scriptable;

public class UriScriptProvider extends JavascriptSupportHelper implements ScriptProvider {

	public UriScriptProvider(JavascriptSupportImpl javascriptSupport) {
		super(javascriptSupport);
	}

	private Map<URI, Script> resourceScripts = new HashMap<URI, Script>();

	public boolean loadScript(Object key, Scriptable scope) {
		if (key instanceof URI) {
			URI uri = (URI) key;
			// only reuse scripts that provided their own scope
			Script script = (scope == null ? (Script) resourceScripts.get(uri) : null);
			if (script == null) {
				URI scriptUri = uri.trimFileExtension().appendFileExtension(JavascriptSupportImpl.JAVASCRIPT_EXTENSION);
				script = new Script(scriptUri, scope);
				script.loadScript(getJavascriptSupport().getURIConverter(), getJavascriptSupport().getScriptClassLoader());
				resourceScripts.put(uri, script);
				resourceScripts.put(scriptUri, script);
			} else if (script.shouldLoadScript()) {
				script.loadScript(getJavascriptSupport().getURIConverter(), getJavascriptSupport().getScriptClassLoader());
			}
			return true;
		}
		return false;
	}
}
