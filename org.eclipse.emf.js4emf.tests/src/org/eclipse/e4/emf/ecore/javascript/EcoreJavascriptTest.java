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
package org.eclipse.e4.emf.ecore.javascript;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.mozilla.javascript.Scriptable;

public class EcoreJavascriptTest extends AbstractJavascriptTest {

	protected void setUp() throws Exception {
		setUp("ecoreJavascriptTest.xmi");
	}
	
	private Object evalScript(Object scopePrototype, String script) {
		return evalScript((Scriptable)javascriptSupport.wrap(scopePrototype), script);
	}
	private Object evalScript(Scriptable scopePrototype, String script) {
		Scriptable scope = (Scriptable)javascriptSupport.wrap(scopePrototype);
		scope.setParentScope(javascriptSupport.getScope(scopePrototype));
		if (! script.startsWith("return")) {
			script = "return " + script;
		}
		script = "function ScriptScrapBookView() { " + script + "}";
		Object result = javascriptSupport.evaluate(script, scope, true);
		result = javascriptSupport.callMethod(scope, "ScriptScrapBookView", (Object[])null, true);
		return result;
	}

	/*
	 * Test methods EObject.js
	 */
	
	public void testEcoreIsA() {
		EObject c1 = id2EObject("c1");
		String isAScript = "return this.isA($ecoreJavascriptTest.$C1);";
		assertEquals(Boolean.TRUE, evalScript(c1, isAScript));
	}

//	public void testEcoreCopy() {
//		EObject c1 = id2EObject("c1");
//		String copyScript = "return this.copy();";
//		Object copy = evalScript(c1, copyScript);
//		assertTrue(copy instanceof EObject);
//		assertNotSame(c1, copy);
//		assertTrue(EcoreUtil.equals(c1, (EObject)copy));
//	}

	public void testEcoreFindContainer() {
		EObject c1 = id2EObject("c1"), c11 = id2EObject("c11"), c211 = id2EObject("c211");
		String isAScript = "return this.findContainer($ecoreJavascriptTest.$C1);";
		assertEquals(c11, evalScript(c211, isAScript));
		assertEquals(c1, evalScript(c11, isAScript));
		assertEquals(null, evalScript(c1, isAScript));
	}

	/*
	 * Test methods in EEList.js
	 */
	
	public void testEcoreContains() {
		String resourceContainsScript = "return this.__().contains($(this, 'c1'));";
		assertEquals(Boolean.TRUE, evalScript(resource, resourceContainsScript));
		String eObjectContainsScript = "return this.__().contains($(this, 'c21'));";
		assertEquals(Boolean.TRUE, evalScript(resource.getContents().get(0), eObjectContainsScript));
		String resourceSetContainsScript = "return this.resourceSet.__().contains(this);";
		assertEquals(Boolean.TRUE, evalScript(resource, resourceSetContainsScript));
	}

	public void testFilter() {
		assertEquals(Boolean.FALSE, evalScript(resource, "this.__().filter(false);"));
		assertEquals(Boolean.FALSE, evalScript(resource, "this.__().filter(null);"));
		assertEquals(Boolean.FALSE, evalScript(resource, "this.__().filter(0.0);"));
		assertEquals(Boolean.FALSE, evalScript(resource, "this.__().filter(undefined);"));
		
		assertEquals(Boolean.TRUE, evalScript(resource, "this.__().filter(true);"));
		assertEquals(Boolean.TRUE, evalScript(resource, "this.__().filter(1.0);"));
		assertEquals(Boolean.TRUE, evalScript(resource, "this.__().filter('string');"));
	}
	
	private EObject id2EObject(String id) {
		return resource.getEObject(id);
	}
	
	public void testFindOne() {
		EObject c1 = id2EObject("c1");
		Object undefined = evalScript(c1, "undefined");
		assertEquals(id2EObject("c22"), evalScript(c1, "this.__().findOne(function (arg) { return arg.name == 'c22';});"));
		assertEquals(id2EObject("c22"), evalScript(c1, "this.__().findOne(function (arg) { return arg.name == 'c22';}, 1);"));
		assertEquals(undefined, evalScript(c1, "this.__().findOne(function (arg) { return arg.name == 'c212';}, 0);"));
		assertEquals(id2EObject("c212"), evalScript(c1, "this.__().findOne(function (arg) { return arg.name == 'c212';}, 1);"));
	}

	private void testContainsSame(List<?> l1, List<?> l2) {
		assertTrue("The sizes, " + l1.size() + " and " + l2.size() + ", are not the same", l1.size() == l2.size());
		assertTrue(l1 + " and " + l2 + " are not the same", l1.containsAll(l2) && l2.containsAll(l1));
	}
	private void testContainsSame(List<?> l1, Object o) {
		assertTrue(o instanceof List<?>);
		testContainsSame(l1, (List<?>)o);
	}
	private List<?> asList(String... ids) {
		List<EObject> result = new ArrayList<EObject>(ids.length);
		for (int i = 0; i < ids.length; i++) {
			result.add(id2EObject(ids[i]));
		}
		return result;
	}
	
	public void testNewList() {
		EObject c1 = id2EObject("c1");
		Object original = evalScript(c1, "this.__();"), copy = evalScript(c1, "this.__().newList(this.__());");
		assertTrue(original instanceof List<?>);
		assertTrue(copy instanceof List<?>);
		assertNotSame(original, copy);
		assertEquals(original, copy);
		Object list = evalScript(c1, "this.__().newList(this.__().size());");
		assertTrue(list instanceof List<?>);
		assertEquals(0, ((List<?>)list).size());
	}
	
	public void testFindMany() {
		EObject c1 = id2EObject("c1");
		testContainsSame(asList("c21", "c22"), evalScript(c1, "this.__().findMany(function (arg) { return arg.name.substring(0,2) == 'c2';});"));
		testContainsSame(asList("c21", "c22", "c211", "c212", "c221", "c222"), evalScript(c1, "this.__().findMany(function (arg) { return arg.name.substring(0,2) == 'c2';}, 1);"));
		testContainsSame(asList("c21", "c22", "c211", "c212", "c221", "c222"), evalScript(c1, "this.__().findMany(function (arg) { return arg.name.substring(0,2) == 'c2';}, -1);"));
	}

	public void testFindInstances() {
		EObject c1 = id2EObject("c1");
		assertSame(c1.eClass(), evalScript(resource, "$ecoreJavascriptTest.$C1;"));
		testContainsSame(asList("c1", "c11", "c12"), evalScript(resource, "this.__().findInstances($ecoreJavascriptTest.$C1);"));
		testContainsSame(asList("c21", "c22", "c211", "c212", "c221", "c222", "c31", "c311", "c321"), evalScript(resource, "this.__().findInstances($ecoreJavascriptTest.$C2);"));
		testContainsSame(asList("c31", "c311", "c321"), evalScript(resource, "this.__().findInstances($ecoreJavascriptTest.$C3);"));

		testContainsSame(asList("c11", "c12"), evalScript(c1, "this.__().findInstances($ecoreJavascriptTest.$C1);"));
		testContainsSame(asList("c21", "c22", "c211", "c212", "c221", "c222", "c31", "c311", "c321"), evalScript(c1, "this.__().findInstances($ecoreJavascriptTest.$C2);"));
		testContainsSame(asList("c31", "c311", "c321"), evalScript(c1, "this.__().findInstances($ecoreJavascriptTest.$C3);"));
	}
	public void testFindInstancesGen() {
		EObject c1 = id2EObject("c1");
		assertSame(c1.eClass(), evalScript(resource, "$ecoreJavascriptTest.$C1;"));
		testContainsSame(asList("c1", "c11", "c12"), evalScript(resource, "this.__().findInstancesGen($ecoreJavascriptTest.$C1);"));
		testContainsSame(asList("c21", "c22", "c211", "c212", "c221", "c222", "c31", "c311", "c321"), evalScript(resource, "this.__().findInstancesGen($ecoreJavascriptTest.$C2);"));
		testContainsSame(asList("c31", "c311", "c321"), evalScript(resource, "this.__().findInstancesGen($ecoreJavascriptTest.$C3);"));
		
		testContainsSame(asList("c11", "c12"), evalScript(c1, "this.__().findInstancesGen($ecoreJavascriptTest.$C1);"));
		testContainsSame(asList("c21", "c22", "c211", "c212", "c221", "c222", "c31", "c311", "c321"), evalScript(c1, "this.__().findInstancesGen($ecoreJavascriptTest.$C2);"));
		testContainsSame(asList("c31", "c311", "c321"), evalScript(c1, "this.__().findInstancesGen($ecoreJavascriptTest.$C3);"));
	}

	public void testFindInstance() {
		EObject c1 = id2EObject("c1");
		assertSame(c1.eClass(), evalScript(resource, "$ecoreJavascriptTest.$C1;"));		
		assertEquals(id2EObject("c11"), evalScript(c1, "this.__().findInstance($ecoreJavascriptTest.$C1);"));
		assertEquals(id2EObject("c21"), evalScript(c1, "this.__().findInstance($ecoreJavascriptTest.$C2);"));
		assertEquals(id2EObject("c31"), evalScript(c1, "this.__().findInstance($ecoreJavascriptTest.$C3);"));
	}
	public void testFindInstanceGen() {
		EObject c1 = id2EObject("c1");
		assertSame(c1.eClass(), evalScript(resource, "$ecoreJavascriptTest.$C1;"));		
		assertEquals(id2EObject("c11"), evalScript(c1, "this.__().findInstanceGen($ecoreJavascriptTest.$C1);"));
		assertEquals(id2EObject("c21"), evalScript(c1, "this.__().findInstanceGen($ecoreJavascriptTest.$C2);"));
		assertEquals(id2EObject("c31"), evalScript(c1, "this.__().findInstanceGen($ecoreJavascriptTest.$C3);"));
	}

	public void testMapList() {
		EObject c1 = id2EObject("c1");
		Object undefined = evalScript(c1, "undefined");
		assertEquals(Arrays.asList("c21", "c22", "c31", "c11", "c12"), evalScript(c1, "this.__().mapList('name');"));
		assertEquals(Arrays.asList(21.0, 22.0, 31.0, undefined, undefined), evalScript(c1, "this.__().mapList('value');"));
		assertEquals(Arrays.asList(42.0, 44.0, 62.0, 0.0, 0.0), evalScript(c1, "this.__().mapList(function (c) { var value = c.value; return value == undefined ? 0.0 : value * 2;});"));
	}
	public void testMapListGen() {
		EObject c1 = id2EObject("c1");
		Object undefined = evalScript(c1, "undefined");
		assertEquals(Arrays.asList("c21", "c22", "c31", "c11", "c12"), evalScript(c1, "this.__().mapListGen('name');"));
		assertEquals(Arrays.asList(21.0, 22.0, 31.0, undefined, undefined), evalScript(c1, "this.__().mapListGen('value');"));
		assertEquals(Arrays.asList(42.0, 44.0, 62.0, 0.0, 0.0), evalScript(c1, "this.__().mapListGen(function (c) { var value = c.value; return value == undefined ? 0.0 : value * 2;});"));
	}
	public void testMap() {
		EObject c1 = id2EObject("c1");
		Object undefined = evalScript(c1, "undefined");
		assertEquals(Arrays.asList("c21", "c22", "c31", "c11", "c12"), evalScript(c1, "this.__().map('name');"));
		assertEquals(Arrays.asList(21.0, 22.0, 31.0, undefined, undefined), evalScript(c1, "this.__().map('value');"));
		assertEquals(Arrays.asList(42.0, 44.0, 62.0, 0.0, 0.0), evalScript(c1, "this.__().mapList(function (c) { var value = c.value; return value == undefined ? 0.0 : value * 2;});"));
	}
	public void testMapProperty() {
		EObject c1 = id2EObject("c1");
		Object undefined = evalScript(c1, "undefined");
		assertEquals(Arrays.asList("c21", "c22", "c31", "c11", "c12"), evalScript(c1, "this.__().mapProperty('name');"));
		assertEquals(Arrays.asList(21.0, 22.0, 31.0, undefined, undefined), evalScript(c1, "this.__().mapProperty('value');"));
	}
	
	public void testReduce() {
		EObject c1 = id2EObject("c1");
		assertEquals(42.0 + 44.0 + 62.0 + 0.0 + 0.0, evalScript(c1, "this.__().reduce(function (res,c) { var value = c.value; return res + (value == undefined ? 0.0 : value * 2);}, 0.0);"));
		assertEquals(Arrays.asList("c21", "c22", "c31", "c11", "c12"), evalScript(c1, "this.__().reduce(function (res,c) { res.add(c.name); return res;}, new java.util.ArrayList());"));
	}

	public void testFilterList() {
		EObject c1 = id2EObject("c1");
		testContainsSame(asList("c11", "c12"), evalScript(c1, "this.__().filterList(function (c) { return c.isA($ecoreJavascriptTest.$C1);})"));
		testContainsSame(asList("c21", "c22", "c31"), evalScript(c1, "this.__().filterList(function (c) { return c.isA($ecoreJavascriptTest.$C1);}, false)"));
	}
	public void testFilterListGen() {
		EObject c1 = id2EObject("c1");
		testContainsSame(asList("c11", "c12"), evalScript(c1, "this.__().filterListGen(function (c) { return c.isA($ecoreJavascriptTest.$C1);})"));
		testContainsSame(asList("c21", "c22", "c31"), evalScript(c1, "this.__().filterListGen(function (c) { return c.isA($ecoreJavascriptTest.$C1);}, false)"));
	}
	public void testSelect() {
		EObject c1 = id2EObject("c1");
		testContainsSame(asList("c11", "c12"), evalScript(c1, "this.__().select(function (c) { return c.isA($ecoreJavascriptTest.$C1);})"));
	}
	public void testReject() {
		EObject c1 = id2EObject("c1");
		testContainsSame(asList("c21", "c22", "c31"), evalScript(c1, "this.__().reject(function (c) { return c.isA($ecoreJavascriptTest.$C1);})"));
	}
	
	public void testCountList() {
		EObject c1 = id2EObject("c1");
		assertEquals(2.0, evalScript(c1, "this.__().countList(function (c) { return c.isA($ecoreJavascriptTest.$C1);})"));
		assertEquals(3.0, evalScript(c1, "this.__().countList(function (c) { return c.isA($ecoreJavascriptTest.$C1);}, false)"));
	}
	public void testCountListGen() {
		EObject c1 = id2EObject("c1");
		assertEquals(2.0, evalScript(c1, "this.__().countListGen(function (c) { return c.isA($ecoreJavascriptTest.$C1);})"));
		assertEquals(3.0, evalScript(c1, "this.__().countListGen(function (c) { return c.isA($ecoreJavascriptTest.$C1);}, false)"));
	}
	public void testCount() {
		EObject c1 = id2EObject("c1");
		assertEquals(2.0, evalScript(c1, "this.__().count(function (c) { return c.isA($ecoreJavascriptTest.$C1);})"));
		assertEquals(3.0, evalScript(c1, "this.__().count(function (c) { return c.isA($ecoreJavascriptTest.$C2);})"));
		assertEquals(1.0, evalScript(c1, "this.__().count(function (c) { return c.isA($ecoreJavascriptTest.$C3);})"));
	}

	public void testSingle() {
		EObject c1 = id2EObject("c1");
		assertEquals(Boolean.FALSE, evalScript(c1, "this.__().single(function (c) { return c.isA($ecoreJavascriptTest.$C1);})"));
		assertEquals(Boolean.FALSE, evalScript(c1, "this.__().single(function (c) { return c.isA($ecoreJavascriptTest.$C2);})"));
		assertEquals(Boolean.TRUE, evalScript(c1, "this.__().single(function (c) { return c.isA($ecoreJavascriptTest.$C3);})"));
	}
	public void testExists() {
		EObject c1 = id2EObject("c1");
		assertEquals(Boolean.TRUE, evalScript(c1, "this.__().exists(function (c) { return c.isA($ecoreJavascriptTest.$C1);})"));
		assertEquals(Boolean.TRUE, evalScript(c1, "this.__().exists(function (c) { return c.isA($ecoreJavascriptTest.$C2);})"));
		assertEquals(Boolean.TRUE, evalScript(c1, "this.__().exists(function (c) { return c.isA($ecoreJavascriptTest.$C3);})"));
		assertEquals(Boolean.FALSE, evalScript(c1, "this.__().exists(function (c) { return c.name == 'c';})"));
	}
	public void testEvery() {
		EObject c1 = id2EObject("c1");
		assertEquals(Boolean.FALSE, evalScript(c1, "this.__().every(function (c) { return c.isA($ecoreJavascriptTest.$C1);})"));
		assertEquals(Boolean.FALSE, evalScript(c1, "this.__().every(function (c) { return c.isA($ecoreJavascriptTest.$C2);})"));
		assertEquals(Boolean.FALSE, evalScript(c1, "this.__().every(function (c) { return c.isA($ecoreJavascriptTest.$C3);})"));
		assertEquals(Boolean.TRUE, evalScript(c1, "this.__().every(function (c) { return c.name != undefined;})"));
	}
	public void testNone() {
		EObject c1 = id2EObject("c1");
		assertEquals(Boolean.FALSE, evalScript(c1, "this.__().none(function (c) { return c.isA($ecoreJavascriptTest.$C1);})"));
		assertEquals(Boolean.FALSE, evalScript(c1, "this.__().none(function (c) { return c.isA($ecoreJavascriptTest.$C2);})"));
		assertEquals(Boolean.FALSE, evalScript(c1, "this.__().none(function (c) { return c.isA($ecoreJavascriptTest.$C3);})"));
		assertEquals(Boolean.TRUE, evalScript(c1, "this.__().none(function (c) { return c.name == undefined;})"));
	}

	public void testListWith() {
		List<?> list1 = Arrays.asList(1.0, 2.0, 3.0);
		List<?> list2 = Arrays.asList(1.0, 2.0, 3.0, 4.0);
		Object result1 = evalScript(list1, "this.listWith(1.0);");
		assertSame(list1, result1);
		Object result2 = evalScript(list1, "this.listWith(4.0);");
		assertEquals(list2, result2);
	}
	public void testListWithout() {
		List<?> list1 = Arrays.asList(1.0, 2.0, 3.0);
		List<?> list2 = Arrays.asList(1.0, 2.0, 4.0, 3.0, 4.0);
		Object result1 = evalScript(list1, "this.listWithout(4.0);");
		assertSame(list1, result1);
		Object result2 = evalScript(list2, "this.listWithout(4.0);");
		assertEquals(list1, result2);
	}
}
