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
package org.eclipse.emf.js4emf.ecore.internal;

import java.util.Date;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.js4emf.ecore.IJsObject;

public class JsWrapperTest extends AbstractJavascriptTest {

	protected void setUp() throws Exception {
		setUp("jsWrapperTest.xmi");
	}

	public void testResourceWrapper() {
		IJsObject resourceWrapper = javascriptSupport.getJsObject(resource);
		Object population = resourceWrapper.getElement(0);
		checkEObjectClass(population, "Population");
	}

	public void testEObjectWrapper() {
		Object population = javascriptSupport.getJsObject(resource).getElement(0);
		checkEObjectClass(population, "Population");
		
		IJsObject populationWrapper = javascriptSupport.getJsObject(population);
		
		Object hallvard = populationWrapper.getElement(0); 	checkEObjectClass(hallvard, "Person");
		Object marit = populationWrapper.getElement(1);		checkEObjectClass(marit, "Person");
		checkPerson(hallvard, "Hallvard", new Date("1966/11/16"), "male");
		checkPerson(marit, "Marit", new Date("1964/04/17"), "female");
		
		Object family1 = populationWrapper.getElement(2); checkEObjectClass(family1, "Family");
	
		IJsObject family1Wrapper = javascriptSupport.getJsObject(family1);
		assertSame(hallvard, family1Wrapper.getProperty("person1"));
		assertSame(marit, family1Wrapper.getProperty("person2"));

		Object jens = family1Wrapper.getElement(0); checkEObjectClass(jens, "Person");
		Object anne = family1Wrapper.getElement(1);	checkEObjectClass(anne, "Person");
		checkPerson(jens, "Jens", new Date("1997/01/14"), "male");
		checkPerson(anne, "Anne", new Date("1998/07/13"), "female");
	}

	public void testListWrapper() {
		Object population = javascriptSupport.getJsObject(resource).getElement(0);
		checkEObjectClass(population, "Population");
		
		IJsObject populationWrapper = javascriptSupport.getJsObject(population);
		Object populationPersons = populationWrapper.getProperty("persons");
		assertTrue(populationPersons instanceof EList);
		
		IJsObject populationPersonsWrapper = javascriptSupport.getJsObject(populationPersons);
		assertSame(populationWrapper.getElement(0), populationPersonsWrapper.getElement(0));
		assertSame(populationWrapper.getElement(1), populationPersonsWrapper.getElement(1));
		
		Object populationFamilies = populationWrapper.getProperty("families");
		assertTrue(populationFamilies instanceof EList);
		
		Object family1 = populationWrapper.getElement(2);
		
		IJsObject populationFamiliesWrapper = javascriptSupport.getJsObject(populationFamilies);
		assertSame(populationFamiliesWrapper.getElement(0), family1);
	
		IJsObject family1Wrapper = javascriptSupport.getJsObject(family1);
		
		Object family1Children = family1Wrapper.getProperty("children");
		assertTrue(family1Children instanceof EList);
		IJsObject family1ChildrenWrapper = javascriptSupport.getJsObject(family1Children);
		assertSame(family1Wrapper.getElement(0), family1ChildrenWrapper.getElement(0));
		assertSame(family1Wrapper.getElement(1), family1ChildrenWrapper.getElement(1));
	}

	private void checkPerson(Object person, String name, Date date, String gender) {
		IJsObject wrapper = javascriptSupport.getJsObject(person);
		assertEquals(name, wrapper.getProperty("name"));
		Object birthday = wrapper.getProperty("birthday");
		assertTrue(birthday instanceof Date);
		assertEquals(date.getYear(), ((Date) birthday).getYear());
		assertEquals(date.getMonth(), ((Date) birthday).getMonth());
		assertEquals(date.getDay(), ((Date) birthday).getDay());
		assertTrue(wrapper.getProperty("gender") instanceof EEnumLiteral);
		assertEquals(gender, ((EEnumLiteral) wrapper.getProperty("gender")).getName());
	}
	
	private void checkEObjectClass(Object population, String eClassName) {
		assertTrue(population instanceof EObject);
		assertEquals(eClassName, ((EObject) population).eClass().getName());
	}
}
