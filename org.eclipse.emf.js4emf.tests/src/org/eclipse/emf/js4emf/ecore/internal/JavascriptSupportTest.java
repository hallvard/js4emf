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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class JavascriptSupportTest extends AbstractJavascriptTest {

	protected void setUp() throws Exception {
		setUp("javascriptSupportTest.xmi");
	}

	private String NAME_PREFIX = "$";
	
	public void testNameProperty() {
		EObject c11 = resource.getContents().get(0);
		Object name = getFeatureValue(c11, "name");
		Assert.assertTrue(name instanceof String);

		String c2Name = "c2";
		Object c2 = getProperty(c11, NAME_PREFIX + c2Name);
		Assert.assertTrue(c2 instanceof EObject);
		Assert.assertEquals(c11, ((EObject) c2).eContainer());
	}

	public void testContainerProperty() {
		EObject c11 = resource.getContents().get(0);
		EObject c2 = c11.eContents().get(0);
		Assert.assertEquals(c11, getProperty(c2, NAME_PREFIX));
	}

	public void testCallMethodInModel_getPrefixedName() {
		EObject c1 = resource.getContents().get(0);
		String prefix = "Name: ";
		Object result = callMethod(c1, "getPrefixedName", new Object[]{prefix});
		Assert.assertEquals(prefix + getFeatureValue(c1, "name") , result);
	}

	public void testCallMethodInModel_getSuffixedName() {
		EObject c1 = resource.getContents().get(0);
		for (Iterator<EObject> c2s = ((List<EObject>)c1.eGet(c1.eClass().getEStructuralFeature("c2s"))).iterator(); c2s.hasNext();) {
			EObject c2 = c2s.next();
			if ("C2".equals(c2.eClass().getName())) {
				String suffix = " = name";
				Object result = callMethod(c2, "getSuffixedName", new Object[]{suffix});
				Assert.assertEquals(getFeatureValue(c2, "name") + suffix , result);
			}
		}
	}

	public void testCallMethodInModel_createC2() {
		EObject c1 = resource.getContents().get(0);
		Object result = callMethod(c1, "createC2", new Object[]{"c2Name"});
		Assert.assertTrue(result instanceof EObject);
		Assert.assertEquals("C2", ((EObject)result).eClass().getName());
		EObject c2 = (EObject)result;
		Assert.assertEquals(c1, getFeatureValue(c2, "c1"));
	}

	public void testCallFunctionInFile_C1ContainsC2() {
		EObject c1 = resource.getContents().get(0);
		Object c2 = callMethod(c1, "createC2", new Object[]{"c2Name"});
		Object result = callFunction(resource, "C1ContainsC2", new Object[]{c1, c2});
		Assert.assertEquals(Boolean.TRUE, result);
	}

	public void testGetIds() {
		EObject c1 = resource.getContents().get(0);
		callMethod(c1, "createC2", new Object[]{"c2Name"});
		List<Object> c1Ids = Arrays.asList(((Scriptable) javascriptSupport.getJsObject(c1)).getIds());
		List<Object> expectedIds = new ArrayList<Object>(Arrays.asList(new Object[]{
				"name", "string1", "int1", "c2s",
				"getPrefixedName", "createC2",
				"$c2", "$c31", "$c32", "$c2Name",
		}));
		List<Object> indices = Arrays.asList(new Object[]{
				new Integer(0),
				new Integer(1),
				new Integer(2),
				new Integer(3),
		});
		expectedIds.addAll(indices);
		Assert.assertTrue(c1Ids.containsAll(expectedIds));
		List<Object> c1ContentIds = Arrays.asList(((Scriptable) javascriptSupport.getJsObject(c1.eContents())).getIds());
		Assert.assertTrue(c1ContentIds.containsAll(indices));
	}

	public void testListMethod_findInstances() {
		EObject c1 = resource.getContents().get(0);
		Object result1 = callFunction(resource, "findInstances", new Object[]{resource, c1.eClass()});
		Assert.assertTrue(result1 instanceof List);
		List<EObject> list1 = (List<EObject>)result1;
		Assert.assertEquals(1, list1.size());
		Assert.assertEquals(c1, list1.get(0));

		List<EObject> c2s = (List<EObject>)getFeatureValue(c1, "c2s");
		EObject c2 = c2s.get(0);
		EObject c31 = c2s.get(1);
		EObject c32 = c2s.get(2);
		Object result2 = callFunction(resource, "findInstances", new Object[]{resource, c2.eClass()});
		Assert.assertTrue(result2 instanceof List);
		List<EObject> list2 = (List)result2;
		Assert.assertEquals(3, list2.size());
		Assert.assertEquals(c2, list2.get(0));
		Assert.assertEquals(c31, list2.get(1));
		Assert.assertEquals(c32, list2.get(2));
	}

	public void testCallResourceFunction_resource() {
		Object result = callFunction(resource, "resource", new Object[]{});
		Assert.assertEquals(resource, result);
	}

	public void testCallEClassMethod_isA() {
		EObject c1 = resource.getContents().get(0);
		EClass eClass1 = c1.eClass();
		Assert.assertTrue(ScriptableObject.hasProperty((Scriptable) javascriptSupport.getJsObject(eClass1), "isA"));
		Object result = callMethod(c1, "isA", new Object[]{eClass1});
		Assert.assertEquals(Boolean.TRUE, result);
	}

	public void testCallMethodsInFile() {
		EObject c1 = resource.getContents().get(0);
		for (Iterator<EObject> c2s = ((List<EObject>)c1.eGet(c1.eClass().getEStructuralFeature("c2s"))).iterator(); c2s.hasNext();) {
			EObject c2 = c2s.next();
			String suffix = " = name";
			Object result = callMethod(c2, "getSuffixedName", new Object[]{suffix});
			Object c2Name = getFeatureValue(c2, "name");
			if ("C3".equals(c2.eClass().getName())) {
				Assert.assertEquals(c2Name + "_" + suffix, result);
				String prefix = "name = ";
				result = callMethod(c2, "getFixedName", new Object[]{prefix, suffix});
				Assert.assertEquals((prefix + "_" + c2Name) + "-" + (c2Name + "_" + suffix), result);
			} else {
				Assert.assertEquals(c2Name + suffix , result);
			}
		}
	}

	private static String test11Script =
		"function getPrefixedName(string) { return string + \"+\" + this.name;}" +
		"function getSuffixedName(string) { return this.name + \"*\" + string;}";

	public void testCallInstanceMethodsInScript() {
		EObject c1 = resource.getContents().get(0);
		int n = 0;
		for (Iterator<EObject> c2s = ((List<EObject>) c1.eGet(c1.eClass().getEStructuralFeature("c2s"))).iterator(); c2s.hasNext();) {
			EObject c2 = (EObject)c2s.next();
			if ("C3".equals(c2.eClass().getName()) && (n == 0)) {
				evaluateInObject(c2, test11Script);
			}
			if ("C3".equals(c2.eClass().getName())) {
				String suffix = " = name";
				Object result = callMethod(c2, "getSuffixedName", new Object[]{suffix});
				Object c2Name = getFeatureValue(c2, "name");
				Assert.assertEquals(c2Name + (n == 0 ? "*" : "_") + suffix, result);
				String prefix = "name = ";
				result = callMethod(c2, "getFixedName", new Object[]{prefix, suffix});
				Assert.assertEquals((prefix + (n == 0 ? "+" : "_") + c2Name) + "-" + (c2Name + (n == 0 ? "*" : "_") + suffix), result);
				n++;
			}
		}
	}

	public void testChangeEventMethod_onSetTitle() {
		EObject c1 = resource.getContents().get(0);
		for (Iterator<EObject> c2s = ((List<EObject>)c1.eGet(c1.eClass().getEStructuralFeature("c2s"))).iterator(); c2s.hasNext();) {
			EObject c2 = c2s.next();
			if ("C3".equals(c2.eClass().getName())) {
				javascriptSupport.getJsObject(c2);
				setFeatureValue(c2, "title", "Mr");
				Assert.assertEquals("Mr Hacker", getFeatureValue(c2, "title"));
			}
		}
	}

	public void testPackageVariable() {
		EObject c1 = resource.getContents().get(0);
		EPackage c1ClassPackage = c1.eClass().getEPackage();
//		String packVariableName = javascriptSupport.getNamePropertyName(c1ClassPackage); // "javascriptSupportTest"
		assertEquals(c1ClassPackage, getVariable(resource, NAME_PREFIX + "javascriptSupportTest"));
	}

	public void testClassLoader(boolean ignore) {
		Object result = evaluateInScope(resource, "var test = new Packages.org.eclipse.e4.emf.ecore.javascript.JavascriptSupportTest(); test");
		assertNotNull(result);
		assertTrue(result instanceof JavascriptSupportTest);
	}
}
