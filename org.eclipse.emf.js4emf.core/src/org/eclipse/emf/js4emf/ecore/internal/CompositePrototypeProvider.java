package org.eclipse.emf.js4emf.ecore.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.js4emf.ecore.PrototypeProvider;
import org.mozilla.javascript.Scriptable;

public class CompositePrototypeProvider extends JavascriptSupportHelper implements PrototypeProvider {

	public CompositePrototypeProvider(JavascriptSupportImpl javascriptSupport) {
		super(javascriptSupport);
	}

	private List<PrototypeProvider> prototypeProviders = new ArrayList<PrototypeProvider>();

	public void addPrototypeProvider(PrototypeProvider prototypeProvider) {
		prototypeProviders.add(prototypeProvider);
	}
	
	public Scriptable getPrototype(Object key) {
		for (PrototypeProvider prototypeProvider : prototypeProviders) {
			Scriptable prototype = prototypeProvider.getPrototype(key);
			if (prototype != null) {
				return prototype;
			}
		}
		return null;
	}
}
