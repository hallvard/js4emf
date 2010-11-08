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

import java.util.Map;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.ChangeCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class EOperationBodyView extends EOperationView {

	public void dispose() {
		disposeTextControl(scriptTextControl);
		super.dispose();
	}
	
	private EOperation selectedOperation;
	
	@Override
	protected void operationSelected(EOperation operation) {
		selectedOperation = operation;
		setScriptControlText(operation != null ? getScriptText(operation) : null);
	}

	private void commitScriptText(EOperation operation) {
		if (operation != null) {
			String scriptText = getScriptControlText();
			try {
				commitScriptText(operation, scriptText, editingDomainProvider);
			} catch (Exception e) {
//				log.log(Level.WARNING, "Exception setting script text to " + scriptText + ": " + e, e);
			}
		}
	}

	private String getScriptText(EOperation operation) {
		Map.Entry<String, String> scriptEntry = getScriptEntry(operation);
		return scriptEntry != null ? scriptEntry.getValue() : null;
	}
	
	private void setScriptText(EOperation operation, String scriptText) {
		Map.Entry<String, String> scriptEntry = getScriptEntry(operation);
		if (scriptEntry != null) {
			scriptEntry.setValue(scriptText);
		}
	}
	
	private Map.Entry<String, String> getScriptEntry(EOperation operation) {
		EPackage ePackage = operation.getEContainingClass().getEPackage();
		String invocationDelegatesAnnotation = EcoreUtil.getAnnotation(ePackage, EcorePackage.eNS_URI, "invocationDelegates");
		if (invocationDelegatesAnnotation != null) {
			String[] invocationDelegates = invocationDelegatesAnnotation.split(" ");
			for (int i = 0; i < invocationDelegates.length; i++) {
				String invocationDelegate = invocationDelegates[i];
				Map.Entry<String, String> scriptEntry = getAnnotationEntry(operation, invocationDelegate, "body");
				if (scriptEntry == null) scriptEntry = getAnnotationEntry(operation, invocationDelegate, "source");
				if (scriptEntry == null) scriptEntry = getAnnotationEntry(operation, invocationDelegate, "script");
				if (scriptEntry == null) scriptEntry = getAnnotationEntry(operation, invocationDelegate, null);
				if (scriptEntry != null) {
					return scriptEntry;
				}
			}
		}
		return null;
	}

	private Map.Entry<String, String> getAnnotationEntry(EOperation operation, String source, String key) {
		EAnnotation operationAnnotation = operation.getEAnnotation(source);
		for (Map.Entry<String, String> entry : operationAnnotation.getDetails().entrySet()) {
			if (key == null || key.equals(entry.getKey())) {
				return entry;
			}
		}
		return null;
	}

	private void commitScriptText(final EOperation operation, final String scriptText, IEditingDomainProvider editingDomainProvider) {
		if (editingDomainProvider != null) {
			EditingDomain editingDomain = editingDomainProvider.getEditingDomain();
			if (! scriptText.equals(getScriptText(operation))) {
				ChangeCommand command = new ChangeCommand(operation) {
					@Override
					protected void doExecute() {
						setScriptText(operation, scriptText);
					}
				};
				if (command.canExecute()) {
					editingDomain.getCommandStack().execute(command);
				}
			}
		} else {
			setScriptText(operation, scriptText);
		}
	}

	private Text scriptTextControl;

	private String getScriptControlText() {
		return scriptTextControl.getText();
	}

	private void setScriptControlText(String scriptText) {
		boolean hasScript = scriptText != null;
		scriptTextControl.setEnabled(hasScript);
		scriptTextControl.setEditable(hasScript);
		scriptTextControl.setText(hasScript ? scriptText : "");
	}

	public void setFocus() {
		scriptTextControl.setFocus();
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
		parent.setLayout(new GridLayout(2, false));

		Button updateSourceButton = new Button(parent, SWT.PUSH);
		updateSourceButton.setText("Update");
		updateSourceButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		updateSourceButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				commitScriptText(selectedOperation);
			}
		});

		scriptTextControl = createTextControl(parent, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		scriptTextControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		scriptTextControl.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e) {
			}
			public void focusLost(FocusEvent e) {
//				commitScriptText(selectedOperation);
			}
		});
	}
}
