package org.eclipse.emf.js4emf.ecore.internal;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.js4emf.ecore.ScriptProvider;
import org.mozilla.javascript.Scriptable;

public class EClassifierScriptProvider implements ScriptProvider {

	public boolean loadScript(Object key, Scriptable scope, JavascriptSupport javascriptSupport) {
		if (key instanceof EClassifier) {
			EClassifier classifier = (EClassifier) key;
			URI scriptUri = null;
			String sourceUri = JavascriptSupport.getAnnotation(classifier, JavascriptSupport.SCRIPTING_EXTERNAL_SOURCE_URI, JavascriptSupport.JAVASCRIPT_EXTENSION, null);
			if (sourceUri != null) {
				scriptUri = URI.createURI(sourceUri);
			}
			if (scriptUri == null) {
				scriptUri = getEClassifierUri(classifier);
			}
			if (javascriptSupport.loadScript(scriptUri, scope)) {
				return true;
			}
		}
		return false;
	}

	private URI getEClassifierUri(EClassifier prototypeClass) {
		URI uri = URI.createURI(prototypeClass.getEPackage().getNsURI());
		if (JavascriptSupport.ECORE_URI.equals(uri)) {
			uri = JavascriptSupport.ECORE_SCRIPT_URI;
		}
		return uri.trimSegments(1).appendSegment(prototypeClass.getName());
	}
}
