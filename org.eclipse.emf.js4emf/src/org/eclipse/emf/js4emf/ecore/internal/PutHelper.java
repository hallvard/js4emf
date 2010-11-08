package org.eclipse.emf.js4emf.ecore.internal;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.mozilla.javascript.NativeArray;

public class PutHelper extends JavascriptSupportHelper {

	public PutHelper(JavascriptSupport javascriptSupport) {
		super(javascriptSupport);
	}

	private Collection toCollection(Object col, Class<?> valueClass) {
		Collection<Object> result = new ArrayList<Object>();
		if (col instanceof NativeArray) {
			NativeArray array = (NativeArray) col;
			for (int i = 0; i < array.getLength(); i++) {
				addToCollection(result, array.get(i, null), valueClass);
			}
		} else if (col instanceof Collection) {
			for (Object value : (Collection) col) {
				addToCollection(result, value, valueClass);
			}
		} else {
			checkValueType(col, null, Collection.class);
		}
		return result;
	}

	private void addToCollection(Collection<Object> col, Object value, Class<?> valueClass) {
		Object unwrappedValue = this.javascriptSupport.unwrapTo(value, valueClass);
		checkValueType(value, unwrappedValue, valueClass);
		col.add(unwrappedValue);
	}

	public boolean checkValueType(Object value, Object unwrappedValue, Class<?> valueClass) {
		if (unwrappedValue != null && valueClass != null) {
			if (valueClass.isPrimitive()) {
				valueClass = getObjectClass(valueClass);
			}
			if (! valueClass.isInstance(unwrappedValue)) {
				throw new RuntimeException("Unsupported conversion from " + value + " to " + valueClass + (unwrappedValue != null ? ": " + unwrappedValue : ""));
			}
		}
		return true;
	}
	
	private Class<?> getObjectClass(Class<?> valueClass) {
		if 		(valueClass == Boolean.TYPE) 	return Boolean.class;
		else if (valueClass == Integer.TYPE) 	return Integer.class;
		else if (valueClass == Long.TYPE) 		return Long.class;
		else if (valueClass == Short.TYPE) 		return Short.class;
		else if (valueClass == Byte.TYPE) 		return Byte.class;
		else if (valueClass == Character.TYPE) 	return Character.class;
		else if (valueClass == Double.TYPE) 	return Double.class;
		else if (valueClass == Float.TYPE) 		return Float.class;
		return null;
	}

	void put(EObject eObject, EStructuralFeature feature, Object value) {
		EClassifier type = feature.getEType();
		if (feature.isMany()) {
			Object unwrappedValue = (value != null ? this.javascriptSupport.unwrapTo(value, Object.class) : null);
			EList list = (EList<?>) eObject.eGet(feature);
			list.clear();
			list.addAll(toCollection(unwrappedValue, type.getInstanceClass()));
		} else {
			Object unwrappedValue = (value != null ? this.javascriptSupport.unwrapTo(value, type.getInstanceClass()) : null);
			if (checkValueType(value, unwrappedValue, type.getInstanceClass())) {
				eObject.eSet(feature, unwrappedValue);
			}
		}
	}
}
