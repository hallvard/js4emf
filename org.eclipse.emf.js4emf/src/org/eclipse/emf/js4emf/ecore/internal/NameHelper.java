/*******************************************************************************
 * Copyright (c) 2009 Hallvard Traetteberg.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Hallvard Traetteberg - initial API and implementation
 ******************************************************************************/
package org.eclipse.emf.js4emf.ecore.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class NameHelper extends JavascriptSupportHelper {

	public NameHelper(JavascriptSupportImpl javascriptSupport) {
		super(javascriptSupport);
	}

	final static String NAME_PREFIX = "$";

	private boolean hasName(EObject eObject, String name, boolean requireAnnotation) {
		if (! name.startsWith(NameHelper.NAME_PREFIX)) {
			return false;
		}
		String eName = getName(eObject, requireAnnotation);
		return (eName != null && name.endsWith(eName) && (name.length() == NameHelper.NAME_PREFIX.length() + eName.length()));
	}
	boolean hasName(EObject eObject, String name) {
		return hasName(eObject, name, false);
	}

	private String getName(EObject eObject, boolean requireAnnotation) {
		EStructuralFeature feature = getNameFeature(eObject, requireAnnotation);
		return (feature != null ? (String)eObject.eGet(feature) : null);
	}
	String getName(EObject eObject) {
		return getName(eObject, false);
	}

	String getNamePropertyName(EObject eObject) {
		return (NameHelper.NAME_PREFIX + getName(eObject)).intern();
	}

	public final static String NAME_FEATURE_NAME = "name";
	public final static String NAME_FEATURE_URI = "http://www.eclipse.org/emf/js4emf/nameFeature";

	private Map<EClass, EStructuralFeature> nameFeatures = new HashMap<EClass, EStructuralFeature>();

	private EStructuralFeature getNameFeature(EObject eObject, boolean requireAnnotation) {
		EClass eClass = eObject.eClass();
		EStructuralFeature feature = nameFeatures.get(eClass);
		if (feature != null) {
			return feature;
		}
		String featureName = EcoreUtil.getAnnotation(eClass, NAME_FEATURE_URI, NAME_FEATURE_NAME);
		if (featureName == null && (! requireAnnotation)) {
			featureName = NameHelper.NAME_FEATURE_NAME;
		}
		if (featureName != null && featureName.length() > 0) {
			feature = eClass.getEStructuralFeature(featureName);
			if (feature == null) {
				feature = eClass.getEStructuralFeature(NameHelper.NAME_FEATURE_NAME);
			}
			if (feature != null && feature.getEType() == EcorePackage.eINSTANCE.getEString()) {
				nameFeatures.put(eClass, feature);
			} else {
				feature = null;
			}
		}
		return feature;
	}
	EStructuralFeature getNameFeature(EObject eObject) {
		return getNameFeature(eObject, false);
	}

	/*
	private class NameAdapter extends EContentAdapter {

		private Scriptable scope;

		public NameAdapter(Resource resource) {
			scope = javascriptSupport.getResourceScope(resource);
			setTarget(resource.getResourceSet() != null ? resource.getResourceSet() : resource);
		}

		protected void setTarget(EObject target) {
			super.setTarget(target);
			addName(target);
		}
		protected void unsetTarget(EObject target) {
			super.unsetTarget(target);
			removeName(target);
		}

		private boolean isEcoreModelObject(EObject eObject) {
			while (eObject != null) {
				if (eObject == EcorePackage.eINSTANCE) {
					return true;
				}
				eObject = eObject.eContainer();
			}
			return false;
		}

		protected void addName(EObject eObject) {
			if (isEcoreModelObject(eObject)) {
				return;
			}
			String name = getName(eObject, true);
			if (name != null) {
				addName(eObject, name);
			}
		}
		private void addName(EObject eObject, String name) {
			name = getNamePropertyName(name);
			Object value = eObject, oldValue = scope.get(name, scope);
			if (oldValue instanceof EObject[]) {
				EObject[] oldEObjects = (EObject[])oldValue;
				EObject[] newEObjects = new EObject[oldEObjects.length + 1];
				System.arraycopy(oldEObjects, 0, newEObjects, 0, oldEObjects.length);
				newEObjects[oldEObjects.length] = eObject;
				value = newEObjects;
			} else if (oldValue instanceof EObject) {
				value = new EObject[]{(EObject)oldValue, eObject};
			}
			scope.put(name, scope,  value);
		}

		protected void removeName(EObject eObject) {
			if (isEcoreModelObject(eObject)) {
				return;
			}
			String name = getName(eObject, true);
			if (name != null) {
				removeName(eObject, name);
			}
		}
		private void removeName(EObject eObject, String name) {
			name = getNamePropertyName(name);
			Object value = null, oldValue = scope.get(name, scope);
			if (oldValue instanceof EObject[]) {
				EObject[] oldEObjects = (EObject[])oldValue;
				EObject[] newEObjects = new EObject[oldEObjects.length - 1];
				for (int d = 0, i = 0; i < oldEObjects.length; i++) {
					if (oldEObjects[i] == eObject) {
						d++;
					} else {
						newEObjects[i - d] = oldEObjects[i];
					}
				}
				value = (newEObjects.length == 1 ? newEObjects[0] : newEObjects);
			}
			scope.put(name, scope,  value);
		}

		public void notifyChanged(Notification notification) {
			super.notifyChanged(notification);
			if (notification.getNotifier() instanceof EObject) {
				EObject eObject = (EObject)notification.getNotifier();
				if (notification.getFeature() == getNameFeature(eObject, true)) {
					if (notification.getOldValue() instanceof String) {
						removeName(eObject, (String)notification.getOldValue());
					}
					if (notification.getNewValue() instanceof String) {
						addName(eObject, (String)notification.getNewValue());
					}
				}
			}
		}
	};

	void handleResource(Resource res) {
		Scriptable scope = javascriptSupport.getScope(res);
		ResourceSet resSet = res.getResourceSet();
		if (resSet != null) {
			for (Resource siblingRes: resSet.getResources()) {
				if (siblingRes != res) {
					javascriptSupport.getScript(siblingRes.getURI(), scope);
				}
			}
		}
		new NameAdapter(res);
	}
	 */

	//	public void addNamedEObjects(Iterator eObjects, Scriptable scope) {
	//		while (eObjects.hasNext()) {
	//			EObject contained = (EObject)eObjects.next();
	//			String name = getName(contained);
	//			if (name != null) {
	//				scope.put(NameSupport.NAME_PREFIX + name, scope, contained);
	//			}
	//			if (contained instanceof EPackage) {
	//				ResourceSet resourceSet = contained.eResource().getResourceSet();
	//				EAnnotation ann = ((EPackage)contained).getEAnnotation(JavascriptSupport.SCRIPTING_IMPORT_URI);
	//				if (ann != null) {
	//					EMap details = ann.getDetails();
	//					for (Iterator dependsOn = details.keySet().iterator(); dependsOn.hasNext();) {
	//						String key = (String)dependsOn.next();
	//						String value = (String)details.get(key);
	//						Resource res = resourceSet.getResource(URI.createURI(value), true);
	//						log.info("Loaded " + res.getContents().size() + " objects from " + value + " on demand");
	//						addNamedEObjects(res.getContents().iterator(), scope);
	//					}
	//				}
	//			}
	//		}
	//	}
	
	public void addNameIds(Object parent, List<EObject> contents, List<Object> result) {
		if (parent != null) {
			result.add(NameHelper.NAME_PREFIX);
		}
		for (EObject content: contents) {
			String eName = getName(content);
			if (eName != null) {
				result.add(NameHelper.NAME_PREFIX + eName);
			}
		}
		for (int i = 0; i < result.size(); i++) {
			Object id = result.get(i);
			if (id instanceof ENamedElement) {
				result.set(i, ((ENamedElement)id).getName());
			}
		}
	}
}
