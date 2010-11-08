/*******************************************************************************
 * Copyright (c) 2008 Hallvard Traetteberg.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Hallvard Traetteberg - initial API and implementation
 ******************************************************************************/
package org.eclipse.emf.js4emf.ecore.internal;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.js4emf.Activator;
import org.eclipse.emf.js4emf.Util;
import org.eclipse.emf.js4emf.ecore.IJsObject;
import org.eclipse.emf.js4emf.ecore.PrototypeProvider;
import org.eclipse.emf.js4emf.ecore.ScriptProvider;
import org.eclipse.emf.js4emf.ecore.internal.functions.AdaptTo;
import org.eclipse.emf.js4emf.ecore.internal.functions.BindingApply;
import org.eclipse.emf.js4emf.ecore.internal.functions.LoadEPackageFunction;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrapFactory;

public class JavascriptSupport {

	private Logger log = Logger.getLogger(Activator.PLUGIN_ID);

	public final static String JAVASCRIPT_EXTENSION = "js";

	private final URI ecoreJsUri = URI.createURI(String.valueOf(getClass().getResource("Ecore.js")));

	private URIConverter uriConverter;

	public void setUriConverter(URIConverter uriConverter) {
		initUriMap(uriConverter.getURIMap());
		this.uriConverter = uriConverter;
	}

	private void initUriMap(Map<URI, URI> uriMap) {
		if (Activator.getDefault() != null) {
			Activator.getDefault().addRegisteredPackages(uriMap);
		}
		uriMap.put(Util.createParentFolderUri(JavascriptSupport.ECORE_SCRIPT_URI), Util.createParentFolderUri(ecoreJsUri));
	}

	public URIConverter getURIConverter() {
		if (uriConverter == null) {
			setUriConverter(new ExtensibleURIConverterImpl());
		}
		return uriConverter;
	}

	final static String JS4EMF_URI_PREFIX = "http://www.eclipse.org/emf/js4emf";

	public final static String SCRIPTING_SOURCE_URI = JS4EMF_URI_PREFIX + "/source";
	public final static String SCRIPTING_SOURCE_FEATURE_URI = JS4EMF_URI_PREFIX + "/sourceFeature";
	public final static String SCRIPTING_EXTERNAL_SOURCE_URI = JS4EMF_URI_PREFIX + "/externalSource";

	static String getAnnotation(EModelElement modelElement, String uri, String key, String def) {
		String annotation = EcoreUtil.getAnnotation(modelElement, uri, key);
		return annotation != null && annotation.trim().length() > 0 ? annotation : def;
	}

	public final static String SCRIPT_SOURCE_FEATURE_NAME = "scriptSource";

	private Context context;
	private ScriptableObject rootScope;

	public JavascriptSupport() {
		// force loading of EmfContextFactory class, to ensure it is set as
		// global ContextFactory
		this.context = EmfContextFactory.getEmfContextFactory().makeContext();
//		Context context = EmfContext.enter();
		// don't wrap String objects as JavaNativeObject, but use their JS equivalent
		this.getWrapFactory().setJavaPrimitiveWrap(false);
		context.setWrapFactory(this.getWrapFactory());
		URI ecoreJsUri = URI.createURI(String.valueOf(getClass().getResource("Ecore.js")));
		rootScope = createScope(ecoreJsUri);
		context.initStandardObjects(rootScope);
		initStandardObjects(rootScope);
	}

	private Context enterContext() {
		Context context = Context.enter(this.context);
		context.setWrapFactory(this.getWrapFactory());
		return context;
	}
	private void exitContext() {
		Context.exit();
	}

	public void setApplicationClassLoader(ClassLoader classLoader) {
		Context context = enterContext();
		context.setApplicationClassLoader(classLoader);
		exitContext();
	}
	
	private void initStandardObjects(Scriptable scope) {
		ScriptableObject.putProperty(scope, "out", System.out);
		ScriptableObject.putProperty(scope, "err", System.err);
		ScriptableObject.putProperty(scope, "loadEPackage", new LoadEPackageFunction(this));
		ScriptableObject.putProperty(scope, "adaptTo", new AdaptTo());
		ScriptableObject.putProperty(scope, "applyAsBinding", new BindingApply(null));
	}

	Object evaluate(String script, String name, Scriptable scope, boolean rethrowException) {
		Context context = enterContext();
		Object result = null;
		try {
			result = context.evaluateString(scope, script, name, -1, null);
		} catch (RuntimeException re) {
			log.log(Level.SEVERE, "Exception when evaluating " + script + " in " + scope + ": " + re, re);
			if (rethrowException) {
				throw re;
			}
		} finally {
			exitContext();
		}
		return unwrap(result);
	}

	Object evaluate(String script, Object scope, boolean rethrowException) {
		return evaluate(script, null, getScope(scope), rethrowException);
	}

	public Scriptable getScope(Object object) {
		if (object instanceof Scriptable) {
			return (Scriptable) object;
		} else if (object instanceof Resource) {
			return getResourceScope((Resource)object);
		} else if (object instanceof EObject) {
			return getResourceScope((EObject)object);
		} else {
			return rootScope;
		}
	}

	public Object wrap(Object object) {
		enterContext();
		try {
			return Context.javaToJS(object, getScope(object));
		} finally {
			exitContext();
		}
	}

	public Object unwrap(Object value) {
		return unwrapTo(value, Object.class);
	}

	public Object unwrapTo(Object value, Class<?> c) {
		enterContext();
		try {
			return Context.jsToJava(value, c != null ? c : Object.class);
		} finally {
			exitContext();
		}
	}

	public Scriptable newObject(String constructorName, Object args[]) {
		Context context = enterContext();
		Scriptable result = null;
		try {
			result = context.newObject(getScope(null), constructorName, args);
		} catch (RuntimeException e) {
			log.log(Level.SEVERE, "Exception when invoking newObject: " + e, e);
		} finally {
			exitContext();
		}
		return result;
	}

	Object getVariable(Resource res, String name) {
		return unwrap(getVariable(name, getResourceScope(res)));
	}
	public Object getProperty(Object object, String name) {
		return unwrap(getProperty(name, (Scriptable) wrap(object)));
	}
	void setVariable(Resource res, String name, Object value) {
		setProperty(name, getResourceScope(res), wrap(value));
	}
	void setProperty(Object object, String name, Object value) {
		setProperty(name, (Scriptable) wrap(object), wrap(value));
	}

	private Object getProperty(String name, Scriptable scope) {
		return ScriptableObject.getProperty(scope, name);
	}
	private void setProperty(String name, Scriptable scope, Object value) {
		ScriptableObject.putProperty(scope, name, value);
	}
	private Object getVariable(String name, Scriptable scope) {
		Object result = (scope != null ? scope.get(name, scope) : null);
		if (result == Scriptable.NOT_FOUND) {
			result = getVariable(name, scope.getParentScope());
		}
		return result;
	}

	public Object callMethod(Object object, String methodName, Object args, boolean rethrowException) {
		return call(object, methodName, args, object, rethrowException);
	}	
	public Object callMethod(Object object, Function method, Object args, boolean rethrowException) {
		return call(object, method, args, object, rethrowException);
	}

	public Object callFunction(Resource res, String funName, Object args, boolean rethrowException) {
		return call(res, funName, args, null, rethrowException);
	}
	public Object callFunction(Resource res, Function method, Object args, boolean rethrowException) {
		return call(res, method, args, null, rethrowException);
	}

	private Object call(Object scopeObject, Object funObject, Object args, Object thisEObject, boolean rethrowException) {
		Context context = enterContext();
		Scriptable scope = getScope(scopeObject);
		Object result = null;
		try {
			Scriptable thisObject = scope;
			if (thisEObject instanceof Scriptable) {
				thisObject = (Scriptable)thisEObject;
			} else if (thisEObject != null) {
				thisObject = (Scriptable)wrap(thisEObject);
			}
			Object fun = funObject;
			if (fun instanceof String) {
				fun = (thisEObject != null ? getProperty((String)fun, thisObject) : getVariable((String)fun, scope));
			}
			if (fun instanceof Function) {
				result = ((Function) fun).call(context, scope, thisObject, wrap(args, context, scope));
			} else {
				log.log(Level.SEVERE, funObject + " not found for " + thisObject);
				result = Scriptable.NOT_FOUND;
			}
		} catch (RuntimeException re) {
			String objectRef = toString(thisEObject);
			log.log(Level.SEVERE, "Exception when calling " + funObject + " on " + objectRef + ": " + re, re);
			if (rethrowException) {
				throw re;
			}
		} finally {
			exitContext();
		}
		return unwrap(result);
	}
	
	String toString(Object thisObject) {
		String objectRef = "";
		if (thisObject instanceof EObject) {
			EObject thisEObject = (EObject)thisObject;
			objectRef = "a " + thisEObject.eClass().getName();
			EStructuralFeature nameFeature = thisEObject.eClass().getEStructuralFeature("name");
			if (nameFeature != null) {
				objectRef += " named " + thisEObject.eGet(nameFeature);
			}
		} else {
			String.valueOf(objectRef);
		}
		return objectRef;
	}

	private Object[] wrap(Object args, Context context, Scriptable scope) {
		if (args == null) {
			return new Object[0];
		}
		Object[] wrappedArgs = null;
		if (args.getClass().isArray()) {
			wrappedArgs = new Object[Array.getLength(args)];
			for (int i = 0; i < wrappedArgs.length; i++) {
				wrappedArgs[i] = wrap(context, scope, Array.get(args, i), Object.class);
			}
		} else if (args instanceof Collection<?>) {
			Collection<?> col = (Collection<?>)args;
			wrappedArgs = new Object[col.size()];
			int i = 0;
			for (Object element: col) {
				wrappedArgs[i++] = wrap(context, scope, element, Object.class);
			}
		} else if (args instanceof Object) {
			wrappedArgs = new Object[1];
			wrappedArgs[0] = wrap(context, scope, args, Object.class);
		}
		if (wrappedArgs == null) {
			throw new IllegalArgumentException("Cannot wrap args passed as " + args);
		}
		return wrappedArgs;
	}

	//

	public final static URI ECORE_URI = URI.createURI(EcorePackage.eINSTANCE.getNsURI());
	public final static URI ECORE_SCRIPT_URI = ECORE_URI.appendFileExtension(JavascriptSupport.JAVASCRIPT_EXTENSION);

	public NativeObject createScope(URI uri) {
		return createScope(uri.toString());
	}

	@SuppressWarnings("serial")
	public NativeObject createScope(final String name) {
		return new NativeObject() {
			// useful when debugging
			private String scopeName = name;

			public String toString() {
				return "[JS + " + scopeName + "]";
			}
		};
	}

	//

	private NameHelper nameSupport = new NameHelper(this);

	NameHelper getNameSupport() {
		return nameSupport;
	}
	
	//

	private Scriptable getResourceScope(EObject eObject) {
		return getResourceScope(eObject.eResource());
	}

	public Scriptable getResourceScope(Resource res) {
		if (res == null) {
			return rootScope;
		}
		Scriptable scope = getResourceScope(res.getURI());
		if (! (scope.get("resource", scope) instanceof Function)) {
			defineConstantFunction("resource", res, null, scope);
//			nameSupport.handleResource(res);
		}
		// scope.put("contents", scope, createConstantFunction("contents", res.getContents()));
		return scope;
	}

	public void defineConstantFunction(String name, Object constant, String property, Scriptable scope) {
		scope.put(name, scope, createConstantFunction(name, constant, property));
	}

	private Scriptable getResourceScope(URI uri) {
		if (uri == null) {
			return rootScope;
		}
		Scriptable scope = uri.equals(JavascriptSupport.ECORE_SCRIPT_URI) ? rootScope : createScope(uri);
		loadScript(uri, scope);
		if (scope != rootScope) {
			scope.setParentScope(getResourceScope(ECORE_URI));
		}
		return scope;
	}

	private ScriptClassLoader scriptClassLoader = null;

	public ScriptClassLoader getScriptClassLoader() {
		if (scriptClassLoader == null) {
			Context context = enterContext();
			scriptClassLoader = new ScriptClassLoader(context.getApplicationClassLoader());
			exitContext();
		}
		return scriptClassLoader;
	}
	
	@SuppressWarnings("serial")
	private Function createConstantFunction(final String name, final Object constant, final String property) {
		return new BaseFunction() {
			public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
				Object result = wrap(constant);
				if (property != null && result instanceof Scriptable) {
					result = ((Scriptable) result).get(property, (Scriptable)result);
				}
				return result;
			}

			public int getArity() {
				return 0;
			}

			public String getFunctionName() {
				return name;
			}
		};
	}

	private PrototypeProvider prototypeProvider = null;
	
	public Scriptable getPrototype(Object key) {
		return prototypeProvider.getPrototype(key, this);
	}
	
	private ScriptProvider scriptProvider = null;

	public boolean loadScript(Object key, Scriptable scope) {
		if (scriptProvider != null) {
			if (scope == null) {
				scope = createScope(key.toString());
			}
			return scriptProvider.loadScript(key, scope, this);
		}
		return false;
	}

	@SuppressWarnings("serial")
	void initWrapper(JsWrapper wrapper, Scriptable scope, final Object wrappedObject, EClassifier prototypeClass) {
		// must make sure we reuse the wrapper and avoid infinite recursion
		Scriptable prototype = getPrototype(wrappedObject);
		
		prototype.setPrototype(getPrototype(prototypeClass));
		prototype.setParentScope(scope);
		if (wrappedObject instanceof EObject) {
			EObject eObject = (EObject) wrappedObject;
			if (scriptProvider != null) {
				scriptProvider.loadScript(eObject, prototype, this);
			}
		}
		wrapper.setPrototype(prototype);
//		Object initFun = ScriptableObject.getProperty(wrapper, "init");
//		if (initFun instanceof Function) {
//			Context context = enterContext();
//			try {
//				Object[] initFunArgs = {};
//				((Function)initFun).call(context, scope, wrapper, initFunArgs);
//			} catch (RuntimeException e) {
//				log.log(Level.SEVERE, "Exception when calling init() on " + wrappedObject + ": " + e, e);
//			} finally {
//				exitContext();
//			}
//		}
	}

	private EPackageHelper ePackageSupport;
	
	public EPackageHelper getEPackageHelper() {
		if (ePackageSupport == null) {
			ePackageSupport = new EPackageHelper(this);
		}
		return ePackageSupport;
	}

	public String functionSource(String name, Iterator<?> params, String source) {
		if (!source.startsWith("function ")) {
			if (!source.startsWith("{")) {
				source = "{\n" + source + "\n}";
			}
			String argList = "";
			int i = 0;
			while (params != null && params.hasNext()) {
				Object param = params.next();
				String paramName = null;
				if (param instanceof String) {
					paramName = (String)param;
				} else if (param instanceof ENamedElement) {
					paramName = ((ENamedElement)param).getName();
				} else {
					param = "p" + i;
				}
				if (argList.length() == 0) {
					argList = paramName;
				} else {
					argList += "," + paramName;
				}
				i++;
			}
			source = "function " + name + "(" + argList + ")" + source;
		}
		return source;
	}

	public BaseFunction defineEClassifierOwnedFunction(EClassifier owner, String name, Iterator<?> params, String source, Scriptable scope) {
		if (scope == null) {
			scope = getPrototype(owner);
		}
		try {
			Context context = enterContext();
			String def = functionSource(name, params, source);
			context.evaluateString(scope, def, name, -1, null);
			Object fun = scope.get(name, scope);
			if (fun instanceof BaseFunction) {
				return (BaseFunction) fun;
			} else {
				throw new IllegalArgumentException(source + " evaluated to "
						+ fun + ", which is not a BaseFunction");
			}
		} finally {
			exitContext();
		}
	}

//	private Map<Object, Scriptable> wrappers = new IdentityHashMap<Object, Scriptable>();

	private class JsWrapperFactory extends AdapterFactoryImpl {

		private Context cx;
		private Scriptable scope;
		private Class<?> staticType;
		
		JsWrapperFactory setContext(Context cx, Scriptable scope, @SuppressWarnings("rawtypes") Class staticType) {
			this.cx = cx;
			this.scope = scope;
			this.staticType = staticType;
			return this;
		}
		
		@Override
		public boolean isFactoryForType(Object type) {
			return IJsObject.class == type;
		}

		// used for notifier targets, i.e. all EMF objects
		@Override
		protected Adapter createAdapter(Notifier target) {
			return createJsObject(cx, scope, target, staticType);
		}

		// used for non-notifier targets, e.g. Lists and Maps
		@Override
		protected Object resolve(Object object, Object type) {
			return createJsObject(cx, scope, object, staticType);
		}
	};

	private JsWrapperFactory adapterFactory = new JsWrapperFactory();
	
	public JsWrapperFactory getAdapterFactory() {
		return adapterFactory;
	}
	
	protected JsWrapper createJsObject(Context cx, Scriptable scope, Object javaObject, @SuppressWarnings("rawtypes") Class staticType) {
		JsWrapper wrapper = null;
		EClassifier prototypeClass = null;
		if (javaObject instanceof EObject) {
			prototypeClass = (EClassifier) ((EObject) javaObject).eClass();
			wrapper = new EObjectWrapper(JavascriptSupport.this, scope, (EObject) javaObject, staticType);
		} else if (javaObject instanceof Resource) {
			prototypeClass = EcorePackage.eINSTANCE.getEResource();
			wrapper = new ResourceWrapper(JavascriptSupport.this, scope, (Resource) javaObject, staticType);
		} else if (javaObject instanceof ResourceSet) {
			prototypeClass = EcorePackage.eINSTANCE.getEResourceSet();
			wrapper = new ResourceSetWrapper(JavascriptSupport.this, scope, (ResourceSet) javaObject, staticType);
		} else if (javaObject instanceof List<?>) {
			prototypeClass = EcorePackage.eINSTANCE.getEEList();
			wrapper = new ListWrapper(JavascriptSupport.this, scope, (List<?>) javaObject, staticType);
		} else if (javaObject instanceof Map<?, ?>) {
			prototypeClass = EcorePackage.eINSTANCE.getEMap();
			wrapper = new MapWrapper(JavascriptSupport.this, scope, (Map<?, ?>) javaObject, staticType);
		} else if (javaObject instanceof EMap<?, ?>) {
			prototypeClass = EcorePackage.eINSTANCE.getEMap();
			wrapper = new MapWrapper(JavascriptSupport.this, scope, (EMap<?, ?>) javaObject, staticType);
		}
		if (wrapper != null) {
			initWrapper(wrapper, scope, javaObject, prototypeClass);
		}
		return wrapper;
	}
	
	private WrapFactory wrapFactory = new WrapFactory() {	
		@Override
		public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, @SuppressWarnings("rawtypes") Class staticType) {
			IJsObject wrapper = (IJsObject) getAdapterFactory().setContext(cx, scope, staticType).adapt(javaObject, IJsObject.class);
			return (wrapper instanceof Scriptable ? (Scriptable) wrapper : super.wrapAsJavaObject(cx, scope, javaObject, staticType));
		}
	};
	
	public WrapFactory getWrapFactory() {
		return wrapFactory;
	}

	private Object wrap(Context cx, Scriptable scope, Object object, Class<Object> c) {
		return getWrapFactory().wrap(cx, scope, object, c);
	}

	private PutHelper putHelper;
	
	PutHelper getPutHelper() {
		if (putHelper == null) {
			putHelper = new PutHelper(this);
		}
		return putHelper; 
	}

	// notification support
	
	private NotificationHelper notificationHelper;
	
	NotificationHelper getNotificationHelper() {
		if (notificationHelper == null) {
			notificationHelper = new NotificationHelper(this);
		}
		return notificationHelper; 
	}
}
