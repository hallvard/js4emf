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

import org.mozilla.javascript.Scriptable;

class ListWrapper extends ContentsWrapper {

	public ListWrapper(JavascriptSupportImpl javascriptSupport, Scriptable scope, List<?> list, Class<?> staticType) {
		super(javascriptSupport, scope, list, staticType);
	}

	public List<?> getContents() {
		return (List<?>)javaObject;
	}

	public Object[] getIds() {
		return getIds(true, false).toArray();
	}
}
