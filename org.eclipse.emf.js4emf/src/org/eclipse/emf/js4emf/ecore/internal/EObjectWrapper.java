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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.mozilla.javascript.Scriptable;

@SuppressWarnings("serial")
class EObjectWrapper extends ContentsWrapper {

	private static Logger log = Logger.getLogger(EObjectWrapper.class.getName());

	public EObjectWrapper(JavascriptSupportImpl javascriptSupport, Scriptable scope, EObject eObject, Class<?> staticType) {
		super(javascriptSupport, scope, eObject, staticType);
	}

	EObject getEObject() {
		return (EObject) javaObject;
	}

	private EStructuralFeature getFeature(String name, EObject eObject) {
		EClass eClass = eObject.eClass();
		if (name.length() == 0 || NameHelper.NAME_PREFIX.equals(name)) {
			EReference eContainmentFeature = eObject.eContainmentFeature();
			return (eContainmentFeature != null ? eContainmentFeature.getEOpposite() : null);
		}
		return eClass.getEStructuralFeature(name);
	}

	public boolean has(String name, Scriptable start) {
		EObject eObject = getEObject();
		EStructuralFeature feature = getFeature(name, eObject);
		if (feature != null) {
			return true;
		}
		for (EObject content: eObject.eContents()) {
			if (this.javascriptSupport.getNameSupport().hasName(content, name)) {
				return true;
			}
		}
		return super.has(name, start);
	}

	public Object get(String name, Scriptable start) {
		EObject eObject = getEObject();
		EStructuralFeature feature = getFeature(name, eObject);
		if (feature != null) {
			Object value = eObject.eGet(feature);
			EmfContext.noteDependency(eObject, feature, value);
			return this.javascriptSupport.wrap(value);
		}
		NameHelper nameSupport = this.javascriptSupport.getNameSupport();
		for (EObject content: eObject.eContents()) {
			if (nameSupport.hasName(content, name)) {
				feature = nameSupport.getNameFeature(content);
				EmfContext.noteDependency(content, feature, nameSupport.getName(content));
				return this.javascriptSupport.wrap(content);
			}
		}
		return super.get(name, start);
	}

	public void put(String name, Scriptable start, Object value) {
		EObject eObject = getEObject();
		EStructuralFeature feature = getFeature(name, eObject);
		if (feature != null) {
			try {
				this.javascriptSupport.getPutHelper().put(eObject, feature, value);
			} catch (Exception e) {
				log.log(Level.SEVERE, e.getMessage(), e);
			}
			return;
		}
		super.put(name, start, value);
	}

	public Object[] getIds() {
		EObject eObject = getEObject();
		EClass eClass = eObject.eClass();
		List<Object> ids = getIds(delegate2Super(), true);
		ids.addAll(eClass.getEAllStructuralFeatures());
		this.javascriptSupport.getNameSupport().addNameIds(eObject.eContainer(), eObject.eContents(), false, ids);
		return ids.toArray();
	}

	public List<?> getContents() {
		return getEObject().eContents();
	}
	
	//

	private boolean supportNotifications = true; 
	
	public void notifyChanged(Notification notification) {
		if (supportNotifications) {
			javascriptSupport.getNotificationHelper().notifyChanged(notification, getEObject(), false);
		}
	}
}
