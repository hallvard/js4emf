package org.eclipse.emf.js4emf.ecore;


/**
 * An interface declaring the capabilities of objects that are wrapped as Javascript objects
 * @author hal
 *
 */
public interface IJsObject {

	/**
	 * Get the property of the underlying object with the speficied name
	 * @param name the property name
	 * @return the value of the specified property
	 */
	public Object getProperty(String name);
	
	/**
	 * Set the property of the underlying object with the speficied name, to the specified value
	 * @param name the property name
	 * @param value the new value
	 */
	public void setProperty(String name, Object value);

	/**
	 * Calls the method with the specified name, with the specified arguments
	 * @param methodName the method name
	 * @param args the arguments
	 * @return
	 */
	public Object callMethod(String methodName, Object args);
	
	/**
	 * Evaluates script in the context of this object
	 * @param script the script to evaluate
	 * @return the resulting value
	 */
	public Object evaluate(String script);
}
