package org.eclipse.emf.js4emf.ecore.internal.delegates;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EValidator.ValidationDelegate;
import org.eclipse.emf.js4emf.ecore.internal.JavascriptSupportImpl;

public class JavascriptValidationDelegate implements ValidationDelegate {

	private JavascriptSupportImpl getJavascriptSupport(EObject eObject) {
		return JavascriptDelegateFactory.getJavascriptSupport(eObject);
	}

	protected boolean booleanValue(Object value) {
		return Boolean.TRUE.equals(value);
	}

	/*
	 * from https://bugs.eclipse.org/bugs/show_bug.cgi?id=255786#c19
EPackage : ?
  EAnnotation : Ecore
    validationDelegates-><uri1> <uri2>

EClass : ?
  EAnnotation : Ecore
    constraints-><c1> <c2>
  EAnnotation : <uri1>
    <c1>-><body1>
    <c2>-><body2>

EOperation : ?
  EAnnotation : <uri2>
    body-><body>
	 */

	private RuntimeException validationException(String constraint, Throwable t) {
		return new RuntimeException("Couldn't validate invariant " + constraint, t);
	}
	
	// invariant, defined by means of an annotated EOperation
	public boolean validate(EClass eClass, EObject eObject, Map<Object, Object> context, EOperation invariant, String expression) {
		try {
			Object result = JavascriptInvocationDelegate.dynamicInvoke(eClass, invariant.getName(), invariant.getEParameters().iterator(), expression, eObject, null, getJavascriptSupport(eObject));
			return booleanValue(result);
		} catch (InvocationTargetException e) {
			throw validationException(invariant.getName(), e);
		}
	}

	private final String CONSTRAINT_METHOD_NAME_PREFIX = "_check_";

	// constraint, defined by means of annotation on eClass
	public boolean validate(EClass eClass, EObject eObject, Map<Object, Object> context, String constraint, String expression) {
		try {
			Object result = JavascriptInvocationDelegate.dynamicInvoke(eClass, eClass, constraint, CONSTRAINT_METHOD_NAME_PREFIX + constraint, null, eObject, null, getJavascriptSupport(eObject));
			return booleanValue(result);
		} catch (InvocationTargetException e) {
			throw validationException(constraint, e);
		}
	}

	// constraint, defined by means of annotation on eDatatype
	public boolean validate(EDataType eDataType, Object value, Map<Object, Object> context, String constraint, String expression) {
		List<?> params = Collections.singletonList(eDataType.getName());
		try {
			Object result = JavascriptInvocationDelegate.dynamicInvoke(eDataType, CONSTRAINT_METHOD_NAME_PREFIX + constraint, params.iterator(), expression, Collections.singletonList(value), getJavascriptSupport(eDataType));
			return booleanValue(result);		
		} catch (InvocationTargetException e) {
			throw validationException(constraint, e);
		}
//		return booleanValue(getJavascriptSupport().evaluate(expression, value, true));
	}
}
