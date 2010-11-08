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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.js4emf.ecore.IJsScope;
import org.mozilla.javascript.Scriptable;

class ResourceWrapper extends ContentsWrapper implements IJsScope {

	protected ResourceWrapper(JavascriptSupport javascriptSupport, Scriptable scope, Resource res, Class<?> staticType) {
		super(javascriptSupport, scope, res, staticType);
	}

	Resource getResource() {
		return (Resource) javaObject;
	}
	
	public boolean has(String name, Scriptable start) {
		return ResourceScope.has(getResource(), name, this.javascriptSupport) || super.has(name, start);
	}

	public Object get(String name, Scriptable start) {
		if (has(name, start)) {
			ResourceScope.get(getResource(), name, javascriptSupport);
		}
		return super.get(name, start);
	}

	public List<EObject> getContents() {
		return ((Resource) javaObject).getContents();
	}

	public Object[] getIds() {
		List<Object> ids = getIds(! delegate2Super(), true);
		ResourceScope.addNameIds(getResource(), ids, javascriptSupport);
		return ids.toArray();
	}
	
	// from Adapter
	
	public boolean isAdapterForType(Object type) {
		return IJsScope.class.equals(type) || super.isAdapterForType(type);
	}

	// from IJsObject
	
	public Object getProperty(String name) {
		return javascriptSupport.getProperty((Resource) javaObject, name);
	}

	public void setProperty(String name, Object value) {
		javascriptSupport.setProperty((Resource) javaObject, name, value);
	}

	// from IScopeObject
	
	public Object getVariable(String name) {
		return javascriptSupport.getVariable((Resource) javaObject, name);
	}

	public void setVariable(String name, Object value) {
		javascriptSupport.setVariable((Resource) javaObject, name, value);
	}

	public Object callFunction(String methodName, Object args) {
		return javascriptSupport.callFunction((Resource) javaObject, methodName, args, true);
	}
}
