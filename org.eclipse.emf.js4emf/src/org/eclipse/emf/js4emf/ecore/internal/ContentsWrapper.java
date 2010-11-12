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

import org.mozilla.javascript.Scriptable;

@SuppressWarnings("serial")
abstract class ContentsWrapper extends JsWrapper {

	public ContentsWrapper(JavascriptSupportImpl javascriptSupport, Scriptable scope, Object javaObject, Class<?> staticType) {
		super(javascriptSupport, scope, javaObject, staticType);
	}

	public abstract List<?> getContents();

	protected void addContentsIds(List<Object> result) {
		addIndexIds(getContents().size(), result);
	}

	protected List<Object> getIds(boolean addSuperIds, boolean addPrototypeIds) {
		List<Object> ids = new ArrayList<Object>();
		if (addSuperIds) {
			ids.addAll(Arrays.asList(super.getIds()));
		}
		if (addPrototypeIds) {
			ids.addAll(Arrays.asList(getPrototype().getIds()));
		}
		addContentsIds(ids);
		return ids;
	}

	public boolean has(int index, Scriptable start) {
		return has(getContents(), index, start, this.javascriptSupport);
	}

	public Object get(int index, Scriptable start) {
		return get(getContents(), index, start, this.javascriptSupport);
	}

	public void put(int index, Scriptable start, Object value) {
		put(getContents(), index, start, value, this.javascriptSupport);
	}
}
