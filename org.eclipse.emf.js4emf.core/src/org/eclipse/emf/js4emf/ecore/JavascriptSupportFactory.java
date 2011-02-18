package org.eclipse.emf.js4emf.ecore;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.js4emf.ecore.internal.JavascriptSupportImpl;

public class JavascriptSupportFactory {

	private static JavascriptSupportFactory instance = null;
	
	public static JavascriptSupportFactory getInstance() {
		if (instance == null) {
			instance = new JavascriptSupportFactory();
		}
		return instance;
	}
	
	public JavascriptSupport getJavascriptSupport(EObject eObject) {
		while (eObject.eContainer() != null) {
			eObject = eObject.eContainer();
		}
		Resource resource = eObject.eResource();
		return getJavascriptSupport((Notifier) (resource != null ? resource : eObject));
	}

	public JavascriptSupport getJavascriptSupport(Resource resource) {
		return getJavascriptSupport((Notifier) resource);
	}
	public JavascriptSupport getJavascriptSupport(ResourceSet resourceSet) {
		return getJavascriptSupport((Notifier) resourceSet);
	}

	private AdapterFactory adapterFactory = new AdapterFactoryImpl() {
		
		@Override
		public boolean isFactoryForType(Object type) {
			return JavascriptSupport.class == type;
		}
	
		// used for notifier targets, i.e. all EMF objects
		@Override
		protected Adapter createAdapter(Notifier target) {
			return new JavascriptSupportImpl(target);
		}
	};

	private JavascriptSupport getJavascriptSupport(Notifier notifier) {
		return (JavascriptSupport) adapterFactory.adapt(notifier, JavascriptSupport.class);
	}
	
	// convenient, but not sure they are needed/should be supported
	
	public IJsObject getJsObject(EObject eObject) {
		return getJsObject((Notifier) eObject);
	}

	public IJsObject getJsObject(Resource resource) {
		return getJsObject((Notifier) resource);
	}

	private IJsObject getJsObject(Notifier notifier) {
		JavascriptSupport javascriptSupport = getJavascriptSupport(notifier);
		return (IJsObject) javascriptSupport.getJsObject(notifier);
	}
}
