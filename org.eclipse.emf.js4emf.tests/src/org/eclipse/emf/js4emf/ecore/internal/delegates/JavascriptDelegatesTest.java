package org.eclipse.emf.js4emf.ecore.internal.delegates;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.util.EcoreValidator;
import org.eclipse.emf.js4emf.ecore.JavascriptSupport;
import org.eclipse.emf.js4emf.ecore.internal.AbstractJavascriptTest;

public class JavascriptDelegatesTest extends AbstractJavascriptTest {

	private EPackage companyPackage;
	private EFactory companyFactory;

	private EAttribute companyName;
	private EReference companyEmployees;
	private EAttribute companySize;

	private EClass employeeClass;
	private EAttribute employeeName;
	private EReference employeeManager;
	private EReference employeeDirectReports;
	private EReference employeeAllReports;
	private EOperation employeeReportsTo;

	private Enumerator sizeSmall;
	private Enumerator sizeMedium;
	private Enumerator sizeLarge;

	private EObject acme;

	private Map<String, EObject> employees;

	//
	// Test framework
	//

	protected void setUp() throws Exception {
		setUp("ecoreJavascriptDelegatesTest.xmi");

		String registryId = JavascriptSupport.SCRIPTING_SOURCE_URI;
		JavascriptDelegateFactory javascriptDelegateFactory = new JavascriptDelegateFactory();
		EStructuralFeature.Internal.SettingDelegate.Factory.Registry.INSTANCE.put(registryId, javascriptDelegateFactory);
		EOperation.Internal.InvocationDelegate.Factory.Registry.INSTANCE.put(registryId, javascriptDelegateFactory);
		EValidator.ValidationDelegate.Registry.INSTANCE.put(registryId, new JavascriptValidationDelegate());
		
		acme = resource.getContents().get(0);

		EClass companyClass = acme.eClass();
		companyPackage = companyClass.getEPackage();
		companyFactory = companyPackage.getEFactoryInstance();

		companyName = (EAttribute) companyClass.getEStructuralFeature("name");
		companyEmployees = (EReference) companyClass.getEStructuralFeature("employees");
		companySize = (EAttribute) companyClass.getEStructuralFeature("size");

		employeeClass = companyEmployees.getEReferenceType();
		employeeName = (EAttribute) employeeClass.getEStructuralFeature("name");
		employeeManager = (EReference) employeeClass.getEStructuralFeature("manager");
		employeeDirectReports = (EReference) employeeClass
		.getEStructuralFeature("directReports");
		employeeAllReports = (EReference) employeeClass.getEStructuralFeature("allReports");
		employeeReportsTo = employeeClass.getEOperations().get(0);

		EEnum sizeKind = (EEnum) companySize.getEAttributeType();
		sizeSmall = sizeKind.getEEnumLiteral("small");
		sizeMedium = sizeKind.getEEnumLiteral("medium");
		sizeLarge = sizeKind.getEEnumLiteral("large");

		employees = new java.util.HashMap<String, EObject>();
	}
	
	protected void tearDown() throws Exception {
		companyPackage = null;
		companyFactory = null;
		employees = null;

		super.tearDown();
	}

	public void testEAttributeDerivation() {
		assertSame(sizeSmall, size(acme));

		// add a load of employees
		EList<EObject> emps = employees(acme);
		for (int i = 0; i < 60; i++) {
			emps.add(companyFactory.create(employeeClass));
		}

		assertSame(sizeMedium, size(acme));

		// and another bunch
		for (int i = 0; i < 1000; i++) {
			emps.add(companyFactory.create(employeeClass));
		}
		assertSame(sizeLarge, size(acme));
	}

	public void testEReferenceDerivation() {
		EList<EObject> amyReports = directReports(employee("Amy"));
		assertEquals(3, amyReports.size());
		assertTrue(amyReports.contains(employee("Bob")));
		assertTrue(amyReports.contains(employee("Jane")));
		assertTrue(amyReports.contains(employee("Fred")));

		EList<EObject> bobReports = directReports(employee("Bob"));
		assertEquals(2, bobReports.size());
		assertTrue(bobReports.contains(employee("norbert")));
		assertTrue(bobReports.contains(employee("Sally")));

		EList<EObject> sallyReports = directReports(employee("Sally"));
		assertEquals(0, sallyReports.size());
	}

	public void testAllInstances() {
		EList<EObject> amyAllReports = allReports(employee("Amy"));
		assertEquals(5, amyAllReports.size());
		assertTrue(amyAllReports.contains(employee("Bob")));
		assertTrue(amyAllReports.contains(employee("Jane")));
		assertTrue(amyAllReports.contains(employee("Fred")));
		assertTrue(amyAllReports.contains(employee("norbert")));
		assertTrue(amyAllReports.contains(employee("Sally")));

		// change the set of all instances of Employee
		set(create(acme, companyEmployees, employeeClass, "Manuel"),
				employeeManager, employee("Bob"));

		amyAllReports = allReports(employee("Amy"));
		assertEquals(6, amyAllReports.size());
		assertTrue(amyAllReports.contains(employee("Manuel")));
	}

	public void testOperationCall() {
		EObject amy = employee("Amy");

		// allReports is implemented using reportsTo()
		EList<EObject> amyAllReports = allReports(amy);
		assertEquals(5, amyAllReports.size());

		for (EObject next : amyAllReports) {
			assertTrue(this.<Boolean> call(next, employeeReportsTo, amy));
		}
	}

	public void testValidation() {
		EValidator validator = new EcoreValidator();
		DiagnosticChain diagnostics = null;
		Map<Object, Object> context = null;
		validateEmployee("Amy", validator, diagnostics, context);
		validateEmployee("Bob", validator, diagnostics, context);
		validateEmployee("Jane", validator, diagnostics, context);
		validateEmployee("Fred", validator, diagnostics, context);
		validateEmployee("Sally", validator, diagnostics, context);
		validateEmployee("norbert", validator, diagnostics, context);
	}

	private void validateEmployee(String employeeName, EValidator validator, DiagnosticChain diagnostics, Map<Object, Object> context) {
		boolean expected =
			employeeName != null &&
			// constraint capitalizedName
			employeeName.length() > 0 &&
			Character.isUpperCase(employeeName.charAt(0)) &&
			// invariant checkNameLength
			employeeName.length() != 5;
		assertEquals(expected, validator.validate(employee(employeeName), diagnostics, context));
	}

	
	EObject employee(String name) {
		EObject result = employees.get(name);
		if (result == null) {
			EList<EObject> emps = get(acme, companyEmployees);
			for (EObject next : emps) {
				if (name.equals(name(next))) {
					result = next;
					employees.put(name, result);
					break;
				}
			}
		}

		return result;
	}

	String name(EObject employeeOrCompany) {
		EAttribute name = employeeClass.isInstance(employeeOrCompany)
		? employeeName
				: companyName;
		return get(employeeOrCompany, name);
	}

	EObject manager(EObject employee) {
		return get(employee, employeeManager);
	}

	EList<EObject> directReports(EObject employee) {
		return get(employee, employeeDirectReports);
	}

	EList<EObject> allReports(EObject employee) {
		return get(employee, employeeAllReports);
	}

	EList<EObject> employees(EObject company) {
		return get(company, companyEmployees);
	}

	Enumerator size(EObject company) {
		return get(company, companySize);
	}

	@SuppressWarnings("unchecked")
	<T> T get(EObject owner, EStructuralFeature feature) {
		return (T) owner.eGet(feature);
	}

	void set(EObject owner, EStructuralFeature feature, Object value) {
		owner.eSet(feature, value);
	}

	void add(EObject owner, EStructuralFeature feature, Object value) {
		this.<EList<Object>> get(owner, feature).add(value);
	}

	EObject create(EObject owner, EReference containment, EClass type,
			String name) {
		EObject result = companyFactory.create(type);

		if (containment.isMany()) {
			add(owner, containment, result);
		} else {
			set(owner, containment, result);
		}

		if (name != null) {
			set(result, type.getEStructuralFeature("name"), name);
		}

		return result;
	}

	<T> EList<T> list(T... element) {
		return new BasicEList<T>(Arrays.asList(element));
	}

	@SuppressWarnings("unchecked")
	<T> T call(EObject target, EOperation operation, Object... arguments) {
		try {
			return (T) target.eInvoke(operation, (arguments.length == 0)
					? ECollections.<Object> emptyEList()
							: new BasicEList.UnmodifiableEList<Object>(arguments.length,
									arguments));
		} catch (InvocationTargetException e) {
			fail("Failed to call operation: " + e.getLocalizedMessage());
			return null;
		}
	}
}
