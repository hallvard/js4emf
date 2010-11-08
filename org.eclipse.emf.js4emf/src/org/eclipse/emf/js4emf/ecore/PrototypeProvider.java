package org.eclipse.emf.js4emf.ecore;

import org.eclipse.emf.js4emf.ecore.internal.JavascriptSupport;
import org.mozilla.javascript.Scriptable;

public interface PrototypeProvider {

	public Scriptable getPrototype(Object key, JavascriptSupport javascriptSupport);
}
