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
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.js4emf.Activator;
import org.eclipse.emf.js4emf.ecore.FeatureValueProvider;
import org.eclipse.emf.js4emf.ecore.IJsObject;
import org.eclipse.emf.js4emf.ecore.IJsScope;
import org.eclipse.emf.js4emf.ecore.JavascriptSupport;
import org.eclipse.emf.js4emf.ecore.PrototypeProvider;
import org.eclipse.emf.js4emf.ecore.ScriptClassLoader;
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

public class JavascriptSupportImpl extends AdapterImpl implements JavascriptSupport {

	private Logger log = Logger.getLogger(Activator.PLUGIN_ID);

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
		uriMap.put(Util.createParentFolderUri(JavascriptSupportImpl.ECORE_SCRIPT_URI), Util.createParentFolderUri(ecoreJsUri));
	}

	public URIConverter getURIConverter() {
		if (uriConverter == null) {
			setUriConverter(new ExtensibleURIConverterImpl());
		}
		return uriConverter;
	}

	static String getAnnotation(EModelElement modelElement, String uri, String key, String def) {
		String annotation = EcoreUtil.getAnnotation(modelElement, uri, key);
		return annotation != null && annotation.trim().length() > 0 ? annotation : def;
	}

	public final static String SCRIPT_SOURCE_FEATURE_NAME = "scriptSource";

	private Context context;
	final ScriptableObject rootScope;

	private JavascriptSupportImpl() {
		// force loading of EmfContextFactory class, to ensure it is set as
		// global ContextFactory
		this.context = EmfContextFactory.getEmfContextFactory().makeContext();
//		Context context = EmfContext.enter();
		// don't wrap String objects as JavaNativeObject, but use their JS equivalent
		this.getWrapFactory().setJavaPrimitiveWrap(false);
		context.setWrapFactory(this.getWrapFactory());
		URI ecoreJsUri = URI.createURI(String.valueOf(getClass().getResource("Ecore.js")));
		rootScope = createScope(ecoreJsUri.toString());
		context.initStandardObjects(rootScope);
		initStandardObjects(rootScope);
	}

	public JavascriptSupportImpl(Notifier notifier) {
		this();
		setTarget(notifier);
	}

	//

	public boolean isAdapterForType(Object type) {
		return JavascriptSupport.class == type;
	}
	
	public void setTarget(Notifier notifier) {
		if (notifier instanceof Resource) {
			registerEPackages(((Resource) notifier).getContents());
		} else if (notifier instanceof ResourceSet) {
			registerEPackages((ResourceSet) notifier);
		} else if (notifier instanceof EObject) {
			registerEPackages(((EObject) notifier).eContents());
		} else {
			throw new IllegalArgumentException("Target must be a Resource or ResourceSet, but was " + notifier);
		}
		super.setTarget(notifier);
	}
	
	public void notifyChanged(Notification notification) {
		boolean isAdd = notification.getEventType() == Notification.ADD || notification.getEventType() == Notification.ADD_MANY;
		if (notification.getNotifier() instanceof Resource) {
			int featureId = notification.getFeatureID(Resource.class);
			if (featureId == Resource.RESOURCE__CONTENTS && isAdd) {
				registerEPackages(((ResourceSet) notification.getNotifier()));
			}
		} else if (notification.getNotifier() instanceof ResourceSet) {
			int featureId = notification.getFeatureID(ResourceSet.class);
			if (isAdd && featureId == ResourceSet.RESOURCE_SET__RESOURCES) {
				registerEPackages(((Resource) notification.getNotifier()).getContents());
			}
		} else if (notification.getNotifier() instanceof EObject) {
			if (isAdd && notification.getFeature() instanceof EReference && ((EReference) notification.getFeature()).isContainment()) {
				registerEPackages(((EObject) notification.getNotifier()).eContents());
			}
		}
	}

	private void registerEPackages(EList<EObject> contents) {
		for (EObject eObject : contents) {
			getEPackageHelper().registerEPackage(eObject.eClass().getEPackage(), null);
		}
	}

	private void registerEPackages(ResourceSet resourceSet) {
		for (Resource resource : resourceSet.getResources()) {
			registerEPackages(resource.getContents());
		}
	}
	
	//
	
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
		return unwrap(getVariable(name, getScope(res)));
	}
	private Object getVariable(String name, Scriptable scope) {
		Object result = (scope != null ? scope.get(name, scope) : null);
		if (result == Scriptable.NOT_FOUND) {
			result = getVariable(name, scope.getParentScope());
		}
		return result;
	}
	void setVariable(Resource res, String name, Object value) {
		ScriptableObject.putProperty(getScope(res), name, wrap(value));
	}

	//
	
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
				fun = (thisEObject != null ? getJsObject(thisObject).getProperty((String)fun) : getVariable((String)fun, scope));
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
	public final static URI ECORE_SCRIPT_URI = ECORE_URI.appendFileExtension(JavascriptSupportImpl.JAVASCRIPT_EXTENSION);

	public NativeObject createScope(String scopeName) {
		return new ResourceScope(this, scopeName);
	}

	//

	private NameHelper nameSupport = new NameHelper(this);

	NameHelper getNameSupport() {
		return nameSupport;
	}
	
	//

	public Scriptable getScope(Object object) {
		if (object instanceof Scriptable) {
			return (Scriptable) object;
		}
		Resource res = null;
		if (object instanceof EObject) {
			object = ((EObject) object).eResource();
		}
		if (object instanceof Resource) {
			res = (Resource) object;
		}
		if (res != null) {
			return (ResourceScope) getAdapterFactory().adapt(res, IJsScope.class);
		}
		return rootScope;
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

	private Scriptable getPrototype(PrototypeProvider prototypeProvider, Object object, Map<Object, Scriptable> prototypes, ScriptProvider scriptProvider) {
		Scriptable prototype = prototypes.get(object);
		if (prototype == null && prototypeProvider != null) {
			prototype = prototypeProvider.getPrototype(object);
			if (prototype != null) {
				prototypes.put(object, prototype);
				if (scriptProvider != null) {
					loadScript(scriptProvider, object, prototype);
				}
			}
		}
		return prototype;
	}
	
	private PrototypeProvider
		instancePrototypeProvider = new InstancePrototypeProvider(this),
		classifierPrototypeProvider = new EClassifierPrototypeProvider(this);
	private Map<Object, Scriptable>
		instancePrototypes = new IdentityHashMap<Object, Scriptable>(),
		classifierPrototypes = new IdentityHashMap<Object, Scriptable>();
	
	private ScriptProvider
		instanceScriptProvider = new InstanceScriptProvider(this),
		classifierScriptProvider = new EClassifierScriptProvider(this);

	protected void setInstancePrototypeProvider(PrototypeProvider instancePrototypeProvider) {
		this.instancePrototypeProvider = instancePrototypeProvider;
	}
	protected void setClassifierPrototypeProvider(PrototypeProvider classifierPrototypeProvider) {
		this.classifierPrototypeProvider = classifierPrototypeProvider;
	}

	private Scriptable getInstancePrototype(Object instance) {
		return getPrototype(instancePrototypeProvider, instance, instancePrototypes, instanceScriptProvider);
	}

	public Scriptable getClassifierPrototype(Object classifier) {
		return getPrototype(classifierPrototypeProvider, classifier, classifierPrototypes, classifierScriptProvider);
	}
	
	private boolean loadScript(ScriptProvider scriptProvider, Object key, Scriptable scope) {
		if (scriptProvider != null) {
			if (scope == null) {
				scope = createScope(key.toString());
			}
			return scriptProvider.loadScript(key, scope);
		}
		return false;
	}

	private ScriptProvider uriScriptProvider = new UriScriptProvider(this);
	
	public boolean loadScript(URI uri, Scriptable scope) {
		return loadScript(uriScriptProvider, uri, scope);
	}

	@SuppressWarnings("serial")
	void initWrapper(JsWrapper wrapper, Scriptable scope, final Object wrappedObject, EClassifier prototypeClass) {
		// must make sure we reuse the wrapper and avoid infinite recursion
		Scriptable prototype = getInstancePrototype(wrappedObject), prototype2 = getClassifierPrototype(prototypeClass);
		if (prototype == null) {
			prototype = prototype2;
			prototype2 = null;
		}
		if (prototype != null) {
			prototype.setPrototype(prototype2);
			prototype.setParentScope(scope);
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
			scope = getClassifierPrototype(owner);
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
			return IJsObject.class == type || IJsScope.class == type;
		}

		// used for notifier targets, i.e. all EMF objects
		@Override
		protected Adapter createAdapter(Notifier target, Object type) {
			if (type == IJsObject.class) {
				return createJsObject(cx, scope, target, staticType);
			} else if (type == IJsScope.class && target instanceof Resource) {
				Resource res = (Resource) target;
				ResourceScope scope = new ResourceScope(JavascriptSupportImpl.this, res);
				loadScript(res.getURI(), scope);
				if (! (scope.get("resource", scope) instanceof Function)) {
					scope.put("resource", scope, createConstantFunction("resource", res, null));
				}
				return scope;
			}
			return null;
		}

		// used for non-notifier targets, e.g. Lists and Maps
		@Override
		protected Object resolve(Object object, Object type) {
			return createJsObject(cx, scope, object, staticType);
		}
	};

	private JsWrapperFactory adapterFactory = new JsWrapperFactory();
	
	private JsWrapperFactory getAdapterFactory() {
		return adapterFactory;
	}
	
	protected JsWrapper createJsObject(Context cx, Scriptable scope, Object javaObject, @SuppressWarnings("rawtypes") Class staticType) {
		JsWrapper wrapper = null;
		EClassifier prototypeClass = null;
		if (javaObject instanceof EObject) {
			prototypeClass = (EClassifier) ((EObject) javaObject).eClass();
			wrapper = new EObjectWrapper(JavascriptSupportImpl.this, scope, (EObject) javaObject, staticType);
		} else if (javaObject instanceof Resource) {
			prototypeClass = EcorePackage.eINSTANCE.getEResource();
			wrapper = new ResourceWrapper(JavascriptSupportImpl.this, scope, (Resource) javaObject, staticType);
		} else if (javaObject instanceof ResourceSet) {
			prototypeClass = EcorePackage.eINSTANCE.getEResourceSet();
			wrapper = new ResourceSetWrapper(JavascriptSupportImpl.this, scope, (ResourceSet) javaObject, staticType);
		} else if (javaObject instanceof List<?>) {
			prototypeClass = EcorePackage.eINSTANCE.getEEList();
			wrapper = new ListWrapper(JavascriptSupportImpl.this, scope, (List<?>) javaObject, staticType);
		} else if (javaObject instanceof Map<?, ?>) {
			prototypeClass = EcorePackage.eINSTANCE.getEMap();
			wrapper = new MapWrapper(JavascriptSupportImpl.this, scope, (Map<?, ?>) javaObject, staticType);
		} else if (javaObject instanceof EMap<?, ?>) {
			prototypeClass = EcorePackage.eINSTANCE.getEMap();
			wrapper = new MapWrapper(JavascriptSupportImpl.this, scope, (EMap<?, ?>) javaObject, staticType);
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
	
	private WrapFactory getWrapFactory() {
		return wrapFactory;
	}

	private Object wrap(Context cx, Scriptable scope, Object object, Class<Object> c) {
		return getWrapFactory().wrap(cx, scope, object, c);
	}

	public IJsObject getJsObject(Object object) {
		return (IJsObject) getAdapterFactory().adapt(object, IJsObject.class);
	}
	public IJsScope getJsScope(Object object) {
		return (IJsScope) getAdapterFactory().adapt(object, IJsScope.class);
	}
	
	//
	
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
	
	//
	
	public FeatureValueProvider<String> getScriptSourceFeatureValueProvider(EObject eObject) {
		return InstanceScriptProvider.getScriptSourceFeatureValueProvider(eObject);
	}
}
