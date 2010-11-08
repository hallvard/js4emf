package org.eclipse.emf.js4emf.ecore.internal.delegates;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.EStructuralFeature.Internal.SettingDelegate;
import org.eclipse.emf.ecore.util.BasicSettingDelegate;
import org.eclipse.emf.js4emf.ecore.internal.JavascriptSupport;

public class JavascriptSettingDelegate extends BasicSettingDelegate.Stateless implements SettingDelegate {

	private String opName;

	public JavascriptSettingDelegate(EStructuralFeature structuralFeature) {
		super(structuralFeature);
		opName = "_get_" + eStructuralFeature.getName();
	}

	private JavascriptSupport getJavascriptSupport() {
		return JavascriptDelegateFactory.getJavascriptSupportFactory().getJavascriptSupport();
	}

	protected Object get(InternalEObject owner, boolean resolve, boolean coreType) {
		try {
			return JavascriptInvocationDelegate.dynamicInvoke(eStructuralFeature.getEContainingClass(), eStructuralFeature, null, opName, null, owner, null, getJavascriptSupport());
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Couldn't get value of " + eStructuralFeature + " feature using " + opName, e);
		}
	}

	protected boolean isSet(InternalEObject owner) {
		return false;
	}

}
