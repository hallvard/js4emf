package org.eclipse.emf.js4emf;

import org.eclipse.emf.common.util.URI;

public class Util {

	public static URI createParentFolderUri(URI uri) {
		String lastSegment = uri.lastSegment();
		return (lastSegment == null || lastSegment.length() == 0 ? uri : uri.trimSegments(1).appendSegment(""));
	}


}
