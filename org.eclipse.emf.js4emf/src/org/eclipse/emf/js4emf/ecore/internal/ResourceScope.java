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
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.js4emf.ecore.IJsScope;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

@SuppressWarnings("serial")
class ResourceScope extends NativeObject implements Adapter, IJsScope {

	private static Logger log = Logger.getLogger(ResourceScope.class.getName());

	private JavascriptSupportImpl javascriptSupport;
	
	private Resource resource = null;
	private String scopeName = null;
	
	private String getScopeName() {
		URI uri = resource != null ? resource.getURI() : null;
		return (uri != null ? uri.toString() : scopeName);
	}
	
	public String toString() {
		return "[JS + " + getScopeName() + "]";
	}
	
	private ResourceScope(JavascriptSupportImpl javascriptSupport) {
		super();
		this.javascriptSupport = javascriptSupport;
		setParentScope(javascriptSupport.rootScope);
	}
	public ResourceScope(JavascriptSupportImpl javascriptSupport, Resource resource) {
		this(javascriptSupport);
		this.resource = resource;
	}
	public ResourceScope(JavascriptSupportImpl javascriptSupport, String uriString) {
		this(javascriptSupport);
		this.scopeName = uriString;
	}

	@Override
	public void setParentScope(Scriptable m) {
		if (m instanceof ResourceScope && this.javascriptSupport != ((ResourceScope) m).javascriptSupport) {
			System.out.println("Alarm!");
		}
		super.setParentScope(m);
	}

	static EObject has(Resource resource, String name, JavascriptSupportImpl javascriptSupport) {
		if (resource != null) {
			for (EObject content: resource.getContents()) {
				if (javascriptSupport.getNameSupport().hasName(content, name)) {
					return content;
				}
				EPackage ePackage = content.eClass().getEPackage();
				if (javascriptSupport.getNameSupport().hasName(ePackage, name)) {
					return ePackage;
				}
			}
		}
		return null;
	}
	
	public boolean has(String name, Scriptable start) {
		return has(resource, name, this.javascriptSupport) != null || super.has(name, start);
	}

	static Object get(EObject content, JavascriptSupportImpl javascriptSupport) {
		NameHelper nameSupport = javascriptSupport.getNameSupport();
		EStructuralFeature feature = nameSupport.getNameFeature(content);
		EmfContext.noteDependency(content, feature, nameSupport.getName(content));
		return javascriptSupport.wrap(content);
	}

	public Object get(String name, Scriptable start) {
		EObject named = has(resource, name, this.javascriptSupport);
		if (named != null) {
			return get(named, this.javascriptSupport);
		}
		return super.get(name, start);
	}

	static void addNameIds(Resource resource, List<Object> ids, JavascriptSupportImpl javascriptSupport) {
		if (resource != null) {
			javascriptSupport.getNameSupport().addNameIds(resource.getResourceSet(), resource.getContents(), true, ids);
		}
	}

	public Object[] getIds() {
		Object[] superIds = super.getIds();
		if (resource == null) {
			return superIds;
		}
		List<Object> ids = new ArrayList<Object>(Arrays.asList(superIds));
		addNameIds(resource, ids, javascriptSupport);
		return ids.toArray();
	}

	public boolean has(int index, Scriptable start) {
		if (resource != null) {
			return JsWrapper.has((List) resource.getContents(), index, start, javascriptSupport);
		}
		return super.has(index, start);
	}

	public Object get(int index, Scriptable start) {
		if (resource != null) {
			return JsWrapper.get(resource.getContents(), index, start, javascriptSupport);
		}
		return super.get(index, start);
	}

	public void put(int index, Scriptable start, Object value) {
		if (resource != null) {
			JsWrapper.put(resource.getContents(), index, start, value, javascriptSupport);
		} else {
			super.put(index, start, value);
		}
	}

	// from Adapter

	public Notifier getTarget() {
		return resource;
	}
	
	public void setTarget(Notifier newTarget) {
	}
	
	public boolean isAdapterForType(Object type) {
		return type instanceof JavascriptSupportImpl.AdapterType &&
			((JavascriptSupportImpl.AdapterType) type).isTypeFor(this.javascriptSupport, IJsScope.class);
	}
	
	public void notifyChanged(Notification notification) {
	}
	
	// from IScopeObject
	
	public Object getVariable(String name) {
		return javascriptSupport.getVariable(resource, name);
	}

	public void setVariable(String name, Object value) {
		javascriptSupport.setVariable(resource, name, value);
	}

	public Object callFunction(String methodName, Object args) {
		return javascriptSupport.callFunction(resource, methodName, args, true);
	}

	public Object evaluate(String script) {
		return javascriptSupport.evaluate(script, this, true);
	}
}
