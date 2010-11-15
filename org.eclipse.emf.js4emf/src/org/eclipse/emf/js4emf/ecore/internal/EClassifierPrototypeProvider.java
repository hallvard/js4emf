package org.eclipse.emf.js4emf.ecore.internal;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.js4emf.ecore.PrototypeProvider;
import org.mozilla.javascript.Scriptable;

public class EClassifierPrototypeProvider extends JavascriptSupportHelper implements PrototypeProvider {

	public EClassifierPrototypeProvider(JavascriptSupportImpl javascriptSupport) {
		super(javascriptSupport);
	}

	private Map<Object, Scriptable> classPrototypes = new IdentityHashMap<Object, Scriptable>();

	public Scriptable getPrototype(Object key) {
		if (key instanceof EClassifier) {
			EClassifier prototypeClass = (EClassifier) key;
			Scriptable prototype = (Scriptable) classPrototypes.get(prototypeClass);
			if (prototype == null) {
				prototype = getJavascriptSupport().createScope(prototypeClass.getName());
				classPrototypes.put(prototypeClass, prototype);
				if (prototypeClass instanceof EClass) {
					EClass prototypeEClass = (EClass)prototypeClass;
					initEClassPrototype(prototype, prototypeEClass);
				}
			}
			return prototype;
		} else if (key instanceof List) {
			List prototypeClasses = (List) key;
			if (prototypeClasses.size() == 1 && prototypeClasses.get(0) instanceof EClass) {
				return getJavascriptSupport().getClassifierPrototype((EClass) prototypeClasses.get(0));
			}
			Scriptable prototype = (Scriptable)classPrototypes.get(prototypeClasses);
			if (prototype == null) {
				// create a new scope for this set of classes
				prototype = getJavascriptSupport().createScope(prototypeClasses.toString());
				for (Object prototypeClass: prototypeClasses) {
					if (prototypeClass instanceof EClass && prototypeClass == prototypeClasses.get(0)) {
						initEClassPrototype(prototype, (EClass) prototypeClass);
					}
				}
				classPrototypes.put(prototypeClasses, prototype);
			}
			return prototype;
		}
		return null;
	}

	private String getScriptSourceCodeAnnotation(EModelElement modelElement) {
		return JavascriptSupportImpl.getAnnotation(modelElement, JavascriptSupportImpl.SCRIPTING_SOURCE_URI, JavascriptSupportImpl.JAVASCRIPT_EXTENSION, null);
	}

	public static void setScriptSourceCodeAnnotation(EModelElement modelElement, String script) {
		 EcoreUtil.setAnnotation(modelElement,
				JavascriptSupportImpl.SCRIPTING_SOURCE_URI, JavascriptSupportImpl.JAVASCRIPT_EXTENSION,
				script);
	}

	protected void initEClassPrototype(Scriptable prototype, EClass prototypeClass) {
		addEOperationFunctions(prototype, prototypeClass);
		if (prototypeClass != EcorePackage.eINSTANCE.getEObject()) {
			List<EClass> superClasses = prototypeClass.getESuperTypes();
			if (superClasses.size() == 0) {
				superClasses = Collections.singletonList(EcorePackage.eINSTANCE.getEObject()); // EcorePackage.eINSTANCE.getEClass()
			}
			Scriptable prototype2 = getJavascriptSupport().getClassifierPrototype(superClasses);
			if (prototype2 == prototype) {
				System.err.println("Circular prototype chain!!!");
			}
			prototype.setPrototype(prototype2);
		}
		prototype.setParentScope(getJavascriptSupport().getScope(prototypeClass.eResource()));
	}

	private void addEOperationFunctions(Scriptable prototype, EClass prototypeClass) {
		for (EOperation op: prototypeClass.getEOperations()) {
			String source = getScriptSourceCodeAnnotation(op);
			if (source != null) {
				getJavascriptSupport().defineEClassifierOwnedFunction(op.getEContainingClass(), op.getName(), op.getEParameters().iterator(), source, prototype);
			}
		}
	}

}
