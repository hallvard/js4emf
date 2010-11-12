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

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.js4emf.ecore.IJsObject;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

@SuppressWarnings("serial")
public abstract class JsWrapper extends NativeJavaObject implements Adapter, IJsObject {

	protected final JavascriptSupportImpl javascriptSupport;

	JsWrapper(JavascriptSupportImpl javascriptSupport, Scriptable scope, Object object, Class<?> staticType) {
		super(scope, object, staticType);
		this.javascriptSupport = javascriptSupport;
	}

	static String toString(String wrapperName, Object o) {
		boolean isEObject = (o instanceof EObject);
		return "[" + wrapperName + " for " + (isEObject ? "eO" : "o") + "bject of " + (isEObject ? "eClass " + ((EObject)o).eClass().getName() : o.getClass()) + ": " + o + "]";
	}

	public String toString() {
		return toString("JSWrapper", javaObject);
	}
	
	protected void addIndexIds(int n, List<Object> l) {
		for (int i = 0; i < n; i++) {
			l.add(i);
		}
	}

	//
	
	public boolean delegate2Super() {
		return false;
	}

	public boolean has(String name, Scriptable start) {
		return (delegate2Super() ? false : super.has(name, start));
	}

	public Object get(String name, Scriptable start) {
		return (delegate2Super() ? Scriptable.NOT_FOUND : super.get(name, start));
	}

	public void put(String name, Scriptable start, Object value) {
		if (! delegate2Super()) {
			super.put(name, start, value);
		}
	}

	static boolean has(List<?> list, int index, Scriptable start, JavascriptSupportImpl javascriptSupport) {
		return list.size() > index;
	}

	static Object get(List<?> list, int index, Scriptable start, JavascriptSupportImpl javascriptSupport) {
		return javascriptSupport.wrap(list.get(index));
	}

	static void put(List list, int index, Scriptable start, Object value, JavascriptSupportImpl javascriptSupport) {
		value = javascriptSupport.unwrap(value);
		list.set(index, value);
	}

	// from Adapter

	public Notifier getTarget() {
		return (javaObject instanceof Notifier ? (Notifier) javaObject : null);
	}
	
	public void setTarget(Notifier newTarget) {
	}
	
	public boolean isAdapterForType(Object type) {
		return IJsObject.class.equals(type);
	}

	public void notifyChanged(Notification notification) {
	}
	
	// from IJsObject

	public Object getProperty(String name) {
		return javascriptSupport.unwrap(ScriptableObject.getProperty(this, name));
	}
	public Object getElement(int i) {
		return javascriptSupport.unwrap(ScriptableObject.getProperty(this, i));
	}

	public void setProperty(String name, Object value) {
		ScriptableObject.putProperty(this, name, value);
	}
	public void setElement(int i, Object value) {
		ScriptableObject.putProperty(this, i, value);
	}

	public Object callMethod(String methodName, Object args) {
		return javascriptSupport.callMethod((EObject) javaObject, methodName, args, true);
	}

	public Object evaluate(String script) {
		return javascriptSupport.evaluate(script, this, true);
	}
}
