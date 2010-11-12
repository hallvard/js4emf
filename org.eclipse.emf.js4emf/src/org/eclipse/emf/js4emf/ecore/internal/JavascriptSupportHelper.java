package org.eclipse.emf.js4emf.ecore.internal;

public abstract class JavascriptSupportHelper {

	private final JavascriptSupportImpl javascriptSupport;

	protected JavascriptSupportImpl getJavascriptSupport() {
		return javascriptSupport;
	}

	public JavascriptSupportHelper(JavascriptSupportImpl javascriptSupport) {
		this.javascriptSupport = javascriptSupport;
	}
}
