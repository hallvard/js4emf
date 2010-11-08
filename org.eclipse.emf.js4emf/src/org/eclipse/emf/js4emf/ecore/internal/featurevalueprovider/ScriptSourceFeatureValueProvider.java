package org.eclipse.emf.js4emf.ecore.internal.featurevalueprovider;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.js4emf.ecore.internal.JavascriptSupport;

public class ScriptSourceFeatureValueProvider implements FeatureValueProvider<String> {

	public ScriptSourceFeatureValueProvider() {
	}
	
	public String getFeatureValue(Notifier notifier) {
		if (notifier instanceof EModelElement) {
			EModelElement annotatedElement = (EModelElement) notifier;
			if (annotatedElement instanceof EOperation || annotatedElement instanceof EStructuralFeature) {
				return EcoreUtil.getAnnotation(annotatedElement, JavascriptSupport.SCRIPTING_SOURCE_URI, JavascriptSupport.JAVASCRIPT_EXTENSION);
			}
		}
		return null;
	}

	public void setFeatureValue(Notifier notifier, String value) {
		if (notifier instanceof EModelElement) {
			EModelElement annotatedElement = (EModelElement) notifier;
			if (annotatedElement instanceof EOperation || annotatedElement instanceof EStructuralFeature) {
				EcoreUtil.setAnnotation(annotatedElement, JavascriptSupport.SCRIPTING_SOURCE_URI, JavascriptSupport.JAVASCRIPT_EXTENSION, value);
				EPackage ePackage = ((EClass) annotatedElement.eContainer()).getEPackage();
				EcoreUtil.setAnnotation(ePackage, EcorePackage.eNS_URI, "settingDelegates", JavascriptSupport.SCRIPTING_SOURCE_URI);
				EcoreUtil.setAnnotation(ePackage, EcorePackage.eNS_URI, "invocationDelegates", JavascriptSupport.SCRIPTING_SOURCE_URI);
				EcoreUtil.setAnnotation(ePackage, EcorePackage.eNS_URI, "validationDelegates", JavascriptSupport.SCRIPTING_SOURCE_URI);
			}
		}
	}

	public void addListener(Listener listener) {
	}

	public void removeListener(Listener listener) {
	}
}
