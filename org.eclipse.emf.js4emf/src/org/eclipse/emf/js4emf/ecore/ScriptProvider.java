package org.eclipse.emf.js4emf.ecore;

import org.eclipse.emf.js4emf.ecore.internal.JavascriptSupport;
import org.mozilla.javascript.Scriptable;

public interface ScriptProvider {

	public boolean loadScript(Object key, Scriptable scope, JavascriptSupport javascriptSupport);
}
