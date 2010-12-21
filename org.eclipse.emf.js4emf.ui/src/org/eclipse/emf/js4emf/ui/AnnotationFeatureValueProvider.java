package org.eclipse.emf.js4emf.ui;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.js4emf.ecore.FeatureValueProvider;

public class AnnotationFeatureValueProvider implements FeatureValueProvider<String> {

	protected final String uri, key;
	
	public AnnotationFeatureValueProvider(String uri, String key) {
		super();
		this.uri = uri;
		this.key = key;
	}

	public String getFeatureValue(Notifier notifier) {
		if (notifier instanceof EModelElement) {
			EModelElement annotatedElement = (EModelElement) notifier;
			return EcoreUtil.getAnnotation(annotatedElement, uri, key);
		}
		return null;
	}

	public void setFeatureValue(Notifier notifier, String value) {
		if (notifier instanceof EModelElement) {
			EModelElement annotatedElement = (EModelElement) notifier;
			setFeatureValue(annotatedElement, value);
		}
	}

	protected void setFeatureValue(EModelElement annotatedElement, String value) {
		EcoreUtil.setAnnotation(annotatedElement, uri, key, value);
	}

	public void addListener(Listener listener) {
	}

	public void removeListener(Listener listener) {
	}
	
	public static EAnnotation ensureEAnnotation(EModelElement owner, String uri, String key, String value) {
		EAnnotation ownerAnnotation = null;
		for (EAnnotation eAnnotation: owner.getEAnnotations()) {
			if (uri.equals(eAnnotation.getSource())) {
				if (value != null && value.equals(eAnnotation.getDetails().get(key))) {
					return eAnnotation;
				}
				ownerAnnotation = eAnnotation;
			}
		}
		if (ownerAnnotation == null) {
			ownerAnnotation = EcoreFactory.eINSTANCE.createEAnnotation();
			ownerAnnotation.setSource(uri);
			if (value != null) {
				ownerAnnotation.getDetails().put(key, value);
			}
		}
		return ownerAnnotation;
	}
}
