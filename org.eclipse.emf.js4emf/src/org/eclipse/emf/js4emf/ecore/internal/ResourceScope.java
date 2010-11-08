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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

@SuppressWarnings("serial")
class ResourceScope extends NativeObject {

	private static Logger log = Logger.getLogger(ResourceScope.class.getName());

	private JavascriptSupport javascriptSupport;
	
	private Resource resource = null;
	
	public ResourceScope(JavascriptSupport javascriptSupport, Resource resource) {
		this.javascriptSupport = javascriptSupport;
		this.resource = resource;
	}

	private Resource getResource() {
		return resource;
	}

	static boolean has(Resource resource, String name, JavascriptSupport javascriptSupport) {
		if (resource != null) {
			for (EObject content: resource.getContents()) {
				if (javascriptSupport.getNameSupport().hasName(content, name)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean has(String name, Scriptable start) {
		return has(resource, name, this.javascriptSupport) || super.has(name, start);
	}

	static Object get(Resource resource, String name, JavascriptSupport javascriptSupport) {
		if (resource != null) {
			NameHelper nameSupport = javascriptSupport.getNameSupport();
			for (EObject content: resource.getContents()) {
				if (nameSupport.hasName(content, name)) {
					EStructuralFeature feature = nameSupport.getNameFeature(content);
					EmfContext.noteDependency(content, feature, nameSupport.getName(content));
					return javascriptSupport.wrap(content);
				}
			}
		}
		return null;
	}

	public Object get(String name, Scriptable start) {
		if (has(name, start)) {
			return get(resource, name, this.javascriptSupport);
		}
		return super.get(name, start);
	}

	static void addNameIds(Resource resource, List<Object> ids, JavascriptSupport javascriptSupport) {
		if (resource != null) {
			javascriptSupport.getNameSupport().addNameIds(resource.getContents(), ids);
		}
	}

	public Object[] getIds() {
		List<Object> ids = new ArrayList<Object>();
		addNameIds(resource, ids, javascriptSupport);
		return ids.toArray();
	}

	public boolean has(int index, Scriptable start) {
		return JsWrapper.has((List) resource.getContents(), index, start, javascriptSupport);
	}

	public Object get(int index, Scriptable start) {
		return JsWrapper.get(resource.getContents(), index, start, javascriptSupport);
	}

	public void put(int index, Scriptable start, Object value) {
		JsWrapper.put(resource.getContents(), index, start, value, javascriptSupport);
	}
}
