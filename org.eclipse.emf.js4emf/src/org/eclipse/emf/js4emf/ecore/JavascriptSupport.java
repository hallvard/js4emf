package org.eclipse.emf.js4emf.ecore;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.URIConverter;

public interface JavascriptSupport {

	public final static String JAVASCRIPT_NAME = "Javascript";
	public final static String JAVASCRIPT_EXTENSION = "js";

	public final static String SCRIPTING_SOURCE_URI = 			EcorePackage.eNS_URI + "/" + JAVASCRIPT_NAME;
	public final static String SCRIPTING_SOURCE_FEATURE_URI = 	SCRIPTING_SOURCE_URI + "/sourceFeature";
	public final static String SCRIPTING_EXTERNAL_SOURCE_URI = 	SCRIPTING_SOURCE_URI + "/externalSource";

	public final static String SCRIPT_SOURCE_FEATURE_NAME = "scriptSource";

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
