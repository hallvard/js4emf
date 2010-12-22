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
		super.selectionChanged(selection);
	}
	
	protected void updateView() {
		String scriptText = null;
		boolean enabled = false;
		EObject selectedEObject = getSelectedEObject();
		if (selectedEObject != null) {
			scriptText = getScriptText(selectedEObject);
			enabled = hasScriptText(selectedEObject);
		}
		updateScriptTextControl(scriptText, enabled);
		layoutView();
	}

	private String getScriptControlText() {
		return (scriptTextControl != null ? scriptTextControl.getText() : "");
	}
	private void updateScriptTextControl(String scriptText, Boolean enabled) {
		if (scriptTextControl != null) {
			scriptTextControl.setText(scriptText != null ? scriptText : "");
			if (enabled != null) {
				scriptTextControl.setEnabled(enabled);
			}
		}
	}

	static FeatureValueProvider<String> getScriptSourceFeatureValueProvider(EObject eObject) {
		return JavascriptSupportFactory.getInstance().getJavascriptSupport(eObject).getScriptSourceFeatureValueProvider(eObject);
	}

	@Override
	protected boolean isValidSelection(Object o) {
		return o instanceof EObject && getScriptSourceFeatureValueProvider((EObject) o) != null;
	}
	
	protected FeatureValueProvider<String> getEObjectScriptSourceFeatureValueProvider(EObject eObject) {
		return getScriptSourceFeatureValueProvider(eObject);
	}
	
	private boolean hasScriptText(EObject eObject) {
		FeatureValueProvider<String> featureValueProvider = getEObjectScriptSourceFeatureValueProvider(eObject);
		return featureValueProvider != null;
	}
	private String getScriptText(EObject eObject) {
		FeatureValueProvider<String> featureValueProvider = getEObjectScriptSourceFeatureValueProvider(eObject);
		return (featureValueProvider != null ? getScriptText(eObject, featureValueProvider) : "");
	}

	private static String getScriptText(EObject eObject, FeatureValueProvider<String> featureValueProvider) {
		Object value = featureValueProvider.getFeatureValue(eObject);
		return (value != null ? value.toString().trim() : "");
	}

	protected EObject getSelectedEObject() {
		return (EObject) getSelection();
	}

	private void commitScriptText(EObject eObject) {
		if (eObject != null) {
			String scriptText = getScriptControlText();
			if (! scriptText.equals(getScriptText(eObject))) {
				try {
					setFeatureValue(eObject, scriptText, getEObjectScriptSourceFeatureValueProvider(eObject), editingDomainProvider);
				} catch (Exception e) {
					log.log(Level.WARNING, "Exception setting script text to " + scriptText + ": " + e, e);
				}
			}
		}
	}

	static void setFeatureValue(final EObject eObject, final String scriptText, final FeatureValueProvider<String> featureValueProvider, IEditingDomainProvider editingDomainProvider) {
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

	private Composite parentComposite = null;
	private Text scriptTextControl;

	public void createPartControl(Composite parent) {
		parentComposite = parent;
		super.createPartControl(parent);
		createTopControls(parent);
		scriptTextControl = createTextControl(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		scriptTextControl.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e) {
			}
			public void focusLost(FocusEvent e) {
				commitScriptText(getSelectedEObject());
			}
		});
	}
	
	protected void layoutView() {
		parentComposite.layout();
	}

	protected void createTopControls(Composite parent) {
	}

	protected Text getScriptTextControl() {
		return scriptTextControl;
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
