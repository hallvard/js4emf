/*******************************************************************************
 * Copyright (c) 2008 Hallvard Traetteberg.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Hallvard Traetteberg - initial API and implementation
 ******************************************************************************/
package org.eclipse.emf.js4emf.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.ChangeCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.js4emf.ecore.FeatureValueProvider;
import org.eclipse.emf.js4emf.ecore.JavascriptSupportFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class ScriptSourceView extends AbstractSelectionView {

	private Logger log = Logger.getLogger(Activator.getDefault().getBundle().getSymbolicName());

	public void dispose() {
		disposeTextControl(scriptTextControl);
		scriptTextControl = null;
		super.dispose();
	}

	protected void selectionChanged(ISelection selection) {
		commitScriptText(getSelectedEObject());
		String scriptText = null;
		super.selectionChanged(selection);
		if (getSelectedEObject() != null) {
			scriptText = getScriptText(getSelectedEObject());
		}
		setScriptControlText(scriptText);
	}

	private String getScriptControlText() {
		return (scriptTextControl != null ? scriptTextControl.getText() : "");
	}
	private void setScriptControlText(String scriptText) {
		if (scriptTextControl != null) {
			scriptTextControl.setText(scriptText != null ? scriptText : "");
		}
	}

	static FeatureValueProvider<String> getScriptSourceFeatureValueProvider(EObject eObject) {
		return JavascriptSupportFactory.getInstance().getJavascriptSupport(eObject).getScriptSourceFeatureValueProvider(eObject);
	}

	protected boolean isValidSelection(Object o) {
		return o instanceof EObject && getScriptSourceFeatureValueProvider((EObject) o) != null;
	}
	
	private String getScriptText(EObject eObject) {
		final FeatureValueProvider<String> featureValueProvider = getScriptSourceFeatureValueProvider(eObject);
//		EAttribute scriptAttr = JavascriptSupport.getScriptSourceAttribute(eObject, JavascriptSupport.JAVASCRIPT_EXTENSION);
		return (featureValueProvider != null ? getScriptText(eObject, featureValueProvider) : "");
	}
//	private static String getScriptText(EObject eObject, EAttribute scriptAttr) {
//		Object value = eObject.eGet(scriptAttr);
//		return (value != null ? value.toString().trim() : "");
//	}

	private static String getScriptText(EObject eObject, FeatureValueProvider<String> featureValueProvider) {
		Object value = featureValueProvider.getFeatureValue(eObject);
		return (value != null ? value.toString().trim() : "");
	}

	private EObject getSelectedEObject() {
		return (EObject) this.selection;
	}

	private void commitScriptText(EObject eObject) {
		if (eObject != null) {
			String scriptText = getScriptControlText();
			try {
				commitScriptText(eObject, scriptText, editingDomainProvider);
			} catch (Exception e) {
				log.log(Level.WARNING, "Exception setting script text to " + scriptText + ": " + e, e);
			}
		}
	}

	static void commitScriptText(final EObject eObject, final String scriptText, IEditingDomainProvider editingDomainProvider) {
		final FeatureValueProvider<String> featureValueProvider = getScriptSourceFeatureValueProvider(eObject);
		if (editingDomainProvider != null && featureValueProvider != null) {
			EditingDomain editingDomain = editingDomainProvider.getEditingDomain();
			if (! scriptText.equals(getScriptText(eObject, featureValueProvider))) {
				ChangeCommand command = new ChangeCommand(eObject) {
					@Override
					protected void doExecute() {
						featureValueProvider.setFeatureValue(eObject, scriptText);
					}
				};
				if (command.canExecute()) {
					editingDomain.getCommandStack().execute(command);
				}
			}
		} else if (featureValueProvider != null) {
			featureValueProvider.setFeatureValue(eObject, scriptText);
		}
	}

	private Text scriptTextControl;

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		scriptTextControl = createTextControl(parent, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		scriptTextControl.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e) {
			}
			public void focusLost(FocusEvent e) {
				commitScriptText(getSelectedEObject());
			}
		});
	}

	public void setFocus() {
		scriptTextControl.setFocus();
	}
	
	/*
	function handlePress(event) {
	java.lang.System.out.println("Event: " + event);
	this.text = "Selected!";
}
	 */
}
