package org.eclipse.emf.js4emf.ecore;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.URIConverter;

public interface JavascriptSupport {

	public String JAVASCRIPT_EXTENSION = "js";

	public String SCRIPTING_SOURCE_URI = 			"http://www.eclipse.org/emf/js4emf/source";
	public String SCRIPTING_SOURCE_FEATURE_URI = 	"http://www.eclipse.org/emf/js4emf/sourceFeature";
	public String SCRIPTING_EXTERNAL_SOURCE_URI = 	"http://www.eclipse.org/emf/js4emf/externalSource";

	public String SCRIPT_SOURCE_FEATURE_NAME = "scriptSource";

	/**
	 * Sets the URIConverter used by this JavascriptSupport
	 * @param uriConverter the URIConverter
	 */
	public void setUriConverter(URIConverter uriConverter);

	/**
	 * Returns the IJsObject corresponding to the specified object, i.e. the Javascript wrapper
	 * @param object the object
	 * @return the corresponding IJsObject
	 */
	public IJsObject getJsObject(Object object);

	/**
	 * Returns the IJsScope corresponding to the specified object, i.e. the Javascript wrapper of its resource
	 * @param object the object
	 * @return the corresponding IJsObject
	 */
	public IJsScope getJsScope(Object object);
	
	/**
	 * Gets the FeatureValueProvider for editing eObject's instance scripts
	 * @param eObject
	 * @return
	 */
	public FeatureValueProvider<String> getScriptSourceFeatureValueProvider(EObject eObject);
}
