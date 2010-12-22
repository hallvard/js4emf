package org.eclipse.emf.js4emf.ecore.internal;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.js4emf.ecore.ScriptProvider;
import org.mozilla.javascript.Scriptable;

public class EClassifierScriptProvider extends JavascriptSupportHelper implements ScriptProvider {

	public EClassifierScriptProvider(JavascriptSupportImpl javascriptSupport) {
		super(javascriptSupport);
	}

	public boolean loadScript(Object key, Scriptable scope) {
		if (key instanceof EClassifier) {
			EClassifier classifier = (EClassifier) key;
			URI scriptUri = null;
			String sourceUri = JavascriptSupportImpl.getAnnotation(classifier, JavascriptSupportImpl.SCRIPTING_EXTERNAL_SOURCE_URI, JavascriptSupportImpl.JAVASCRIPT_NAME, null);
			if (sourceUri != null) {
				scriptUri = URI.createURI(sourceUri);
			}
			if (scriptUri == null) {
				scriptUri = getEClassifierUri(classifier);
			}
			if (getJavascriptSupport().loadScript(scriptUri, scope)) {
				return true;
			}
		}
		return false;
	}

	private URI getEClassifierUri(EClassifier prototypeClass) {
		URI uri = URI.createURI(prototypeClass.getEPackage().getNsURI());
		if (JavascriptSupportImpl.ECORE_URI.equals(uri)) {
			uri = JavascriptSupportImpl.ECORE_SCRIPT_URI;
		}
		return uri.trimSegments(1).appendSegment(prototypeClass.getName());
	}
}
