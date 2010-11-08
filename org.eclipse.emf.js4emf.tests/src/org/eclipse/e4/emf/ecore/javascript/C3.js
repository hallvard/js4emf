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
function getPrefixedName(string) {
	return string + "_" + this.name;
}

function getSuffixedName(string) {
	return this.name + "_" + string;
}

function getFixedName(prefix, suffix) {
	return this.getPrefixedName(prefix) + "-" + this.getSuffixedName(suffix);
}

function onSetTitle(notification) {
	var suffix = " Hacker";
	var len = suffix.length;
	var tlen = this.title.length;
	if (! (tlen >= len && this.title.substr(tlen - len, len) == suffix)) {
		this.title = this.title + suffix;
	}
}
