/*******************************************************************************
 * Copyright (c) 2009 Hallvard Traetteberg.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Hallvard Traetteberg - initial API and implementation
 ******************************************************************************/
package org.eclipse.emf.js4emf.ecore.internal.functions;

import org.eclipse.emf.js4emf.ecore.internal.JavascriptSupportImpl;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class LoadEPackageFunction extends AbstractFunction {

	private JavascriptSupportImpl javascriptSupport;
	
	public LoadEPackageFunction(JavascriptSupportImpl javascriptSupport) {
		super();
		this.javascriptSupport = javascriptSupport;
	}

	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		String packageUri = typeCheckArgument(args, 0, String.class);
		String altUri = null;
		if (args.length > 1) {
			altUri = typeCheckArgument(args, 1, String.class);
		}
		return javascriptSupport.getEPackageHelper().loadEPackage(packageUri, altUri);
	}
}
