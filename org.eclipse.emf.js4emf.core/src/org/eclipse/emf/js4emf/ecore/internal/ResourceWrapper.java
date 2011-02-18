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
import org.eclipse.emf.ecore.resource.Resource;
import org.mozilla.javascript.Scriptable;

@SuppressWarnings("serial")
class ResourceWrapper extends ContentsWrapper {

	protected ResourceWrapper(JavascriptSupportImpl javascriptSupport, Scriptable scope, Resource res, Class<?> staticType) {
		super(javascriptSupport, scope, res, staticType);
	}

	Resource getResource() {
		return (Resource) javaObject;
	}
	
	public boolean has(String name, Scriptable start) {
		return ResourceScope.has(getResource(), name, this.javascriptSupport) != null || super.has(name, start);
	}

	public Object get(String name, Scriptable start) {
		EObject named = ResourceScope.has(getResource(), name, this.javascriptSupport);
		if (named != null) {
			return ResourceScope.get(named, this.javascriptSupport);
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
}
