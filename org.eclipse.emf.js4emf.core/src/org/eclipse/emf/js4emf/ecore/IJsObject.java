package org.eclipse.emf.js4emf.ecore;


/**
 * An interface declaring the capabilities of objects that are wrapped as Javascript objects
 * @author hal
 *
 */
public interface IJsObject {

	/**
	 * Get the property of the underlying object with the specified name
	 * @param name the property name
	 * @return the value of the specified property
	 */
	public Object getProperty(String name);
	/**
	 * Get the element of the underlying object at the specified index
	 * @param i the index
	 * @return the element at the specified index
	 */
	public Object getElement(int i);
	
	/**
	 * Set the property of the underlying object with the specified name, to the specified value
	 * @param name the property name
	 * @param value the new value
	 */
	public void setProperty(String name, Object value);
	/**
	 * Set the element of the underlying object at the specified index, to the specified value
	 * @param i the index
	 * @param value the new value
	 */
	public void setElement(int i, Object value);

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
