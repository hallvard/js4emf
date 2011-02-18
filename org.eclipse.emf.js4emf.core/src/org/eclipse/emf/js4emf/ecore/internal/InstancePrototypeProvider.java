package org.eclipse.emf.js4emf.ecore.internal;

import java.util.IdentityHashMap;
import java.util.Map;

import org.eclipse.emf.js4emf.ecore.PrototypeProvider;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class InstancePrototypeProvider extends JavascriptSupportHelper implements PrototypeProvider {

	public InstancePrototypeProvider(JavascriptSupportImpl javascriptSupport) {
		super(javascriptSupport);
	}

	private Map<Object, Scriptable> instancePrototypes = new IdentityHashMap<Object, Scriptable>();

	public Scriptable getPrototype(final Object key) {
		Scriptable prototype = instancePrototypes.get(key);
		if (prototype == null) {
			prototype = new InstancePrototype() {
				public String toString() {
					return JsWrapper.toString(
							"JSPrototypeWrapper", key);
				}
				public String getClassName() {
					return "JSPrototypeWrapper";
				}
			};
			instancePrototypes.put(key, prototype);
		}
		return prototype;
	}
	
	@SuppressWarnings("serial")
	private abstract class InstancePrototype extends ScriptableObject {
		public Object[] getIds() {
			Object[] ids1 = super.getIds();
			Object[] ids2 = getPrototype().getIds();
			Object[] ids = new Object[ids1.length + ids2.length];
			System.arraycopy(ids1, 0, ids, 0, ids1.length);
			System.arraycopy(ids2, 0, ids, ids1.length, ids2.length);
			return ids;
		}
	}
}
