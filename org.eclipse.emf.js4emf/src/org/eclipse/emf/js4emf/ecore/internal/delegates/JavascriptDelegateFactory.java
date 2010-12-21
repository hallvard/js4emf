package org.eclipse.emf.js4emf.ecore.internal.delegates;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EOperation.Internal.InvocationDelegate;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EStructuralFeature.Internal.SettingDelegate;
import org.eclipse.emf.js4emf.ecore.JavascriptSupportFactory;
import org.eclipse.emf.js4emf.ecore.internal.JavascriptSupportImpl;

public class JavascriptDelegateFactory implements EStructuralFeature.Internal.SettingDelegate.Factory, EOperation.Internal.InvocationDelegate.Factory {

	public SettingDelegate createSettingDelegate(EStructuralFeature structuralFeature) {
		return new JavascriptSettingDelegate(structuralFeature);
	}

	public InvocationDelegate createInvocationDelegate(EOperation operation) {
		return new JavascriptInvocationDelegate(operation);
	}

	static JavascriptSupportImpl getJavascriptSupport(EObject target) {
		return (JavascriptSupportImpl) JavascriptSupportFactory.getInstance().getJavascriptSupport(target);
	}
}
