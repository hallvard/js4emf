package org.eclipse.emf.js4emf.ecore.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EClass;

public class ESuperTypesIterator implements Iterator<EClass> {

	private List<EClass> superTypes = new ArrayList<EClass>();
	private int pos = 0;
	
	public ESuperTypesIterator(EClass eClass) {
		superTypes.add(eClass);
	}
	public ESuperTypesIterator(Collection<EClass> eClasses) {
		superTypes.addAll(eClasses);
	}
	
	public boolean hasNext() {
		return pos < superTypes.size();
	}

	public EClass next() {
		EClass eClass = superTypes.get(pos);
		for (EClass superType : eClass.getESuperTypes()) {
			if (! superTypes.contains(superType)) {
				superTypes.add(superType);
			}
		}
//		superTypes.addAll(eClass.getESuperTypes());
		pos++;
		return eClass;
	}

	public void remove() {
		throw new UnsupportedOperationException(this.getClass() + " does not support remove");
	}
}
