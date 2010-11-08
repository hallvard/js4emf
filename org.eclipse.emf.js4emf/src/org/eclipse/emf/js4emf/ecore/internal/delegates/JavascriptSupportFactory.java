package org.eclipse.emf.js4emf.ecore.internal.delegates;

import org.eclipse.emf.js4emf.ecore.internal.JavascriptSupport;

public class JavascriptSupportFactory {

	private static JavascriptSupportFactory singleton;
	
	public static JavascriptSupportFactory getJavascriptSupportFactory() {
		if (singleton == null) {
			singleton = new JavascriptSupportFactory();
		}
		return singleton;
	}
	
	private JavascriptSupport javascriptSupport;
	
	public JavascriptSupport getJavascriptSupport() {
		if (javascriptSupport == null) {
			javascriptSupport = new JavascriptSupport();
		}
		return javascriptSupport;
	}
}
