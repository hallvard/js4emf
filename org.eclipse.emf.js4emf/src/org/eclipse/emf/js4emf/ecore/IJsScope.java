package org.eclipse.emf.js4emf.ecore;


/**
 * An interface declaring the capabilities of objects that are wrapped as Javascript objects
 * @author hal
 *
 */
public interface IJsScope {

	/**
	 * Get the variable in the underlying scope with the specified name
	 * @param name the variable name
	 * @return the value of the specified property
	 */
	public Object getVariable(String name);
	
	/**
	 * Set the variable in the underlying scope with the specified name, to the specified value
	 * @param name the variable name
	 * @param value the new value
	 */
	public void setVariable(String name, Object value);

	/**
	 * Calls the function with the specified name, with the specified arguments
	 * @param methodName the method name
	 * @param args the arguments
	 * @return
	 */
	public Object callFunction(String methodName, Object args);
		
	/**
	 * Evaluates script in the context of this object
	 * @param script the script to evaluate
	 * @return the resulting value
	 */
	public Object evaluate(String script);
}
