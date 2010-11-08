/*******************************************************************************
 * Copyright (c) 2010 Hallvard Traetteberg.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Hallvard Traetteberg - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.emf.javascript.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.edit.command.ChangeCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.PropertySheetPage;

public class EOperationInvocationView extends EOperationView {

	public void dispose() {
		propertySheetPage.dispose();
		super.dispose();
	}

	private static class EOperationInvocation extends ChangeCommand {

		private EOperation operation = null;
		private EObject argumentsInstance = null;
		private Object result = null;

		public EOperationInvocation(EObject operationOwner) {
			super(operationOwner);
		}

		public void dispose() {
			super.dispose();
			operation = null;
			argumentsInstance = null;
			result = null;
		}
		
		public EObject getOperationOwner() {
			return (EObject) notifier;
		}

		public void setOperation(EOperation operation, EObject argumentsInstance) {
			this.operation = operation;
			this.argumentsInstance = argumentsInstance;
		}
		
		private EList<Object> argumentList = new BasicEList<Object>();

		@Override
		protected boolean prepare() {
			if (! super.prepare() || getOperationOwner() == null || operation == null) {
				return false;
			}
			for (EStructuralFeature property : argumentsInstance.eClass().getEStructuralFeatures()) {
				Object value = argumentsInstance.eGet(property);
				argumentList.add(value);
			}
			return true;
		}

		protected void doExecute() {
			try {
				result = getOperationOwner().eInvoke(operation, argumentList);
			} catch (Exception e) {
				result = e;
			}
		}

//		public boolean didChange() {
//			ChangeDescription changes = getChangeDescription();
//			return changes.getObjectChanges().size() == 0 && changes.getResourceChanges().size() == 0;
//		}
		
		private void execute(IEditingDomainProvider editingDomainProvider, Shell shell) {
			if (! canExecute()) {
				return;
			}
			if (editingDomainProvider != null) {
				editingDomainProvider.getEditingDomain().getCommandStack().execute(this);
			} else {
				execute();
			}
			if (result != null && shell != null) {
				MessageDialog dialog = new MessageDialog(shell, "Invocation result", null, String.valueOf(result),
						(result instanceof Exception ? MessageDialog.ERROR : MessageDialog.INFORMATION), new String[]{"Close"}, 0);
				dialog.open();
			}
		}
	}
	
	private EOperationInvocation currentInvocation;

	@Override
	protected void updateView() {
		EObject selection = getSelectedEObject();
		if (selection == null) {
			currentInvocation = null;
		} else {
			currentInvocation = new EOperationInvocation(selection);
		}
		super.updateView();
	}

	@Override
	protected void operationSelected(EOperation operation) {
		EObject operationArgumentsInstance = null;
		if (currentInvocation != null) {
			if (operation != null) {
				operationArgumentsInstance = operationArgumentsInstances.get(operation);
				if (operationArgumentsInstance == null) {
					operationArgumentsInstance = createEOperationEObject(createEOperationEClass(operation));
					operationArgumentsInstances.put(operation, operationArgumentsInstance);
					initEOperationOwnerContainer(operationArgumentsInstance, currentInvocation.getOperationOwner());
				}
			}
			currentInvocation.setOperation(operation, operationArgumentsInstance);
		}
		updatePropertySheet(operationArgumentsInstance);
	}

	private EPackage operationArgumentsPackage;

	private EPackage getEOperationEPackage() {
		if (operationArgumentsPackage == null) {
			operationArgumentsPackage = EcoreFactory.eINSTANCE.createEPackage();
			operationArgumentsPackage.setEFactoryInstance(EcoreFactory.eINSTANCE.createEFactory());
		}
		return operationArgumentsPackage;
	}

	private Map<EOperation, EObject> operationArgumentsInstances = new HashMap<EOperation, EObject>();

	private EClass createEOperationEClass(EOperation operation) {
		EClass eClass = createEOperationEClass();
		for (EParameter param : operation.getEParameters()) {
			EClassifier type = param.getEType();
			EStructuralFeature property = (type instanceof EClass ? EcoreFactory.eINSTANCE.createEReference() : EcoreFactory.eINSTANCE.createEAttribute());
			property.setName(param.getName());
			property.setEType(type);
			property.setLowerBound(param.getLowerBound());
			property.setUpperBound(param.getUpperBound());
			eClass.getEStructuralFeatures().add(property);
		}
		return eClass;
	}

	private EObject operationOwnerContainer;
	private EReference operationOwnerContainerContentRef, operationOwnerContainerContextRef;

	private EReference createEObjectEReference(EClass owner, String name) {
		EReference ref = EcoreFactory.eINSTANCE.createEReference();
		ref.setName(name);
		ref.setEType(EcorePackage.eINSTANCE.getEObject());
		owner.getEStructuralFeatures().add(ref);
		return ref;
	}
	
	private EClass createEOperationEClass() {
		EClass eClass = EcoreFactory.eINSTANCE.createEClass();
		getEOperationEPackage().getEClassifiers().add(eClass);
		return eClass;
	}
	
	private EObject createEOperationEObject(EClass eClass) {
		EObject eObject = getEOperationEPackage().getEFactoryInstance().create(eClass);
		return eObject;
	}
	
	private EObject getEOperationOwnerContainer() {
		if (operationOwnerContainer == null) {
			EClass operationOwnerContainerContainerClass = createEOperationEClass();
			if (operationOwnerContainerContentRef == null) {
				operationOwnerContainerContentRef = createEObjectEReference(operationOwnerContainerContainerClass, "contentRef");
				operationOwnerContainerContentRef.setContainment(true);
			}
			if (operationOwnerContainerContextRef == null) {
				operationOwnerContainerContextRef = createEObjectEReference(operationOwnerContainerContainerClass, "contextRef");
			}
			operationOwnerContainer = createEOperationEObject(operationOwnerContainerContainerClass);
		}
		return operationOwnerContainer;
	}

	private void initEOperationOwnerContainer(EObject operationArgumentsInstance, EObject operationOwner) {
		EObject container = getEOperationOwnerContainer();
		container.eSet(operationOwnerContainerContentRef, operationArgumentsInstance);
		container.eSet(operationOwnerContainerContextRef, getSelectedEObject());
	}

	private Shell parentShell;

	private void updatePropertySheet(EObject eObject) {
		if (editingDomainProvider != null) {
			AdapterFactoryEditingDomain editingDomain = (AdapterFactoryEditingDomain) editingDomainProvider.getEditingDomain();
			propertySheetPage.setPropertySourceProvider(new AdapterFactoryContentProvider(editingDomain.getAdapterFactory()));
		}
		StructuredSelection selection = (eObject != null && editingDomainProvider != null ? new StructuredSelection(eObject) : StructuredSelection.EMPTY);
		propertySheetPage.selectionChanged(null, selection);
	}

	private PropertySheetPage propertySheetPage;

	@Override
	public void createPartControl(Composite parent) {
		parentShell = parent.getShell();
		super.createPartControl(parent);
		parent.setLayout(new GridLayout(2, false));

		Button invokeButton = new Button(parent, SWT.PUSH);
		invokeButton.setText("Invoke!");
		invokeButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		invokeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				EOperationInvocation invocation = currentInvocation;
				currentInvocation = null;
				if (invocation != null) {
					invocation.execute(editingDomainProvider, parentShell);
				}
			}
		});

		propertySheetPage = new PropertySheetPage();
		propertySheetPage.createControl(parent);
		propertySheetPage.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
	}
}
