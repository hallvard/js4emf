package org.eclipse.emf.js4emf.ecore;

import org.mozilla.javascript.Scriptable;

public interface PrototypeProvider {

	public Scriptable getPrototype(Object key);
}
