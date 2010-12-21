package org.eclipse.emf.js4emf.ui;

import java.util.Map;

@SuppressWarnings("rawtypes")
public class MapEntryImpl implements Map.Entry {

	private final Object key, value;
	
	public MapEntryImpl(Object key, Object value) {
		this.key = key;
		this.value = value;
	}

	public Object getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	public Object setValue(Object value) {
		throw new UnsupportedOperationException("Cannot change value of this implementation");
	}
}
