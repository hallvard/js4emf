package org.eclipse.emf.js4emf.ui;

import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.js4emf.ecore.FeatureValueProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class DelegatesScriptView extends ScriptSourceView {

	private String currentProviderName = "Javascript";
	private String currentConstraintName = null;

	@Override
	protected FeatureValueProvider<String> getEObjectScriptSourceFeatureValueProvider(EObject eObject) {
		IDelegatesScriptSourceFeatureValueProviders delegatesScriptSourceFeatureValueProviders = Activator.getDefault().getDelegatesScriptSourceFeatureValueProviders(currentProviderName);
		if (delegatesScriptSourceFeatureValueProviders != null) {
			if (eObject instanceof EClassifier) {
				return delegatesScriptSourceFeatureValueProviders.getConstraintDelegateScriptSourceProvider(currentConstraintName);
			} else if (eObject instanceof EOperation) {
				EOperation eOperation = (EOperation) eObject;
				if (EcoreUtil.isInvariant(eOperation)) {
					return delegatesScriptSourceFeatureValueProviders.getInvariantDelegateScriptSourceProvider();
				}
				return delegatesScriptSourceFeatureValueProviders.getInvocationDelegateScriptSourceProvider();
			} else if (eObject instanceof EStructuralFeature) {
				return delegatesScriptSourceFeatureValueProviders.getSettingDelegateScriptSourceProvider();
			}
			return super.getEObjectScriptSourceFeatureValueProvider(eObject);
		}
		return null;
	}

	private EAnnotation ensureEClassifierConstraint(EClassifier eClassifier, String constraintName, String constraint) {
		EAnnotation eAnnotation = AnnotationFeatureValueProvider.ensureEAnnotation(eClassifier, EcorePackage.eNS_URI, "constraints", null);
		String value = eAnnotation.getDetails().get("constraints");
		if (value != null) {
			value = value.trim();
		}
		if (value == null || value.length() == 0) {
			value = constraintName;
		} else {
			int pos = value.indexOf(constraintName);
			if (pos < 0 || (pos > 0 && value.charAt(pos - 1) != ' ') || (pos < value.length() - 1 && value.charAt(pos + 1) != ' ')) {
				value = value + " " + constraintName;
			} else {
				value = null;
			}
		}
		if (value != null) {
			eAnnotation.getDetails().put("constraints", value);
		}
		if (constraint != null) {
			IDelegatesScriptSourceFeatureValueProviders delegatesScriptSourceFeatureValueProviders = Activator.getDefault().getDelegatesScriptSourceFeatureValueProviders(currentProviderName);
			if (delegatesScriptSourceFeatureValueProviders != null) {
				setFeatureValue(eClassifier, constraint, delegatesScriptSourceFeatureValueProviders.getConstraintDelegateScriptSourceProvider(constraintName), editingDomainProvider);
			}
		}
		return eAnnotation;
	}

	private EOperation ensureEOperationConstraint(EClass eClass, String name, String constraint) {
		EOperation eOperation = null;
		for (EOperation op: eClass.getEOperations()) {
			if (name.equals(op.getName())) {
				eOperation = op;
				break;
			}
		}
		if (eOperation == null) {
			eOperation = EcoreFactory.eINSTANCE.createEOperation();
			eOperation.setName(name);
			eClass.getEOperations().add(eOperation);
		}
		if (! EcoreUtil.isInvariant(eOperation)) {
			EList<EParameter> parameters = eOperation.getEParameters();
			parameters.clear();
			EParameter diagnosticsParam = EcoreFactory.eINSTANCE.createEParameter();
			diagnosticsParam.setName("diagnostics");
			diagnosticsParam.setEType(EcorePackage.eINSTANCE.getEDiagnosticChain());
			parameters.add(diagnosticsParam);
			EParameter contextParam = EcoreFactory.eINSTANCE.createEParameter();
			contextParam.setName("context");
			contextParam.setEType(EcorePackage.eINSTANCE.getEMap());
			parameters.add(contextParam);
		}
		if (constraint != null) {
			IDelegatesScriptSourceFeatureValueProviders delegatesScriptSourceFeatureValueProviders = Activator.getDefault().getDelegatesScriptSourceFeatureValueProviders(currentProviderName);
			if (delegatesScriptSourceFeatureValueProviders != null) {
				setFeatureValue(eOperation, constraint, delegatesScriptSourceFeatureValueProviders.getInvariantDelegateScriptSourceProvider(), editingDomainProvider);
			}
		}
		return eOperation;
	}

	private IInputValidator javaNameValidator = new IInputValidator() {
		public String isValid(String javaName) {
			if (javaName.length() <= 0) {
				return "The name cannot be empty";
			}
			for (int i = 0; i < javaName.length(); i++) {
				char c = javaName.charAt(i);
				if (i == 0 && (! Character.isJavaIdentifierStart(c))) {
					return "The name must start with a letter";
				} else if (i > 0 && (! Character.isJavaIdentifierStart(c))) {
					return "The name can only include letters, digits and underlines";
				}
			}
			return null;
		}
	};

	private Label delegateKind;
	private Combo constraintSelector;

	private Display display;
	
	@Override
	public void createTopControls(Composite parent) {
		this.display = parent.getDisplay();
		parent.setLayout(new GridLayout(2, false));
		delegateKind = new Label(parent, SWT.NONE);
		delegateKind.setText(defaultDelegateKind);
		constraintSelector = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		super.createTopControls(parent);
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		delegateKind.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
		constraintSelector.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		getScriptTextControl().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));
		constraintSelector.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int pos = constraintSelector.getSelectionIndex();
				if (pos >= 0 && pos < constraintSelector.getItemCount()) {
					String selection = constraintSelector.getItem(pos);
					int end = 0;
					while (end < selection.length()) {
						if (! Character.isJavaIdentifierPart(selection.charAt(end))) {
							break;
						}
						end++;
					}
					currentConstraintName = selection.substring(0, end);
				} else {
					currentConstraintName = null;
				}
				DelegatesScriptView.super.updateView();
			}
		});
	}
	
	@Override
	protected Object getSelection() {
		EObject eObject = (EObject) super.getSelection();
		while (eObject != null && (! (eObject instanceof ENamedElement))) {
			eObject = eObject.eContainer();
		}
		return eObject;
	}

	private final String defaultDelegateKind = "<no delegate>";

	@Override
	public void updateView() {
		EObject selectedEObject = getSelectedEObject();
		boolean constraintsDelegate = selectedEObject instanceof EClassifier;
		final List<String> constraints = (constraintsDelegate ? EcoreUtil.getConstraints((EClassifier) selectedEObject) : Collections.EMPTY_LIST);
		String selectedEObjectName = (selectedEObject instanceof EModelElement ? ((ENamedElement) selectedEObject).getName() : null);
		if (constraintsDelegate) {
			delegateKind.setText(constraints.size() > 0 ? ("Constraints (" + constraints.size() + ") for " + selectedEObjectName + ": ") : "No constraints");
		} else if (selectedEObject instanceof EOperation) {
			EOperation operation = (EOperation) selectedEObject;
			String operationText = "Operation body for " + selectedEObjectName + "(";
			for (EParameter param : operation.getEParameters()) {
				if (operationText.charAt(operationText.length() - 1) != '(') {
					operationText += ", ";
				}
				operationText += param.getName();
			}
			operationText += "): ";
			delegateKind.setText(operationText);
		} else if (selectedEObject instanceof EStructuralFeature) {
			delegateKind.setText("Derived value for " + selectedEObjectName + ": ");
		} else {
			delegateKind.setText(defaultDelegateKind);
		}
		final String currentSelection = currentConstraintName;
		constraintSelector.clearSelection();
		String[] items = new String[constraints.size()];
		for (int i = 0; i < items.length; i++) {
			items[i] = String.format("%s (%d/%d)", constraints.get(i), i + 1, constraints.size());
		}
		constraintSelector.setItems(items);
		int pos = constraints.indexOf(currentSelection);
		if (pos < 0 && constraints.size() > 0) {
			currentConstraintName = constraints.get(pos = constraints.size() - 1);
		}
		if (pos >= 0) {
			constraintSelector.select(pos);
			constraintSelector.setText(currentConstraintName);
		}
		constraintSelector.setVisible(items.length > 0);
		super.updateView();
		addConstraintAction.updateEnablement();
		addInvariantAction.updateEnablement();
	}

	EClassifierAction addConstraintAction, addInvariantAction;

	private abstract class EClassifierAction extends Action {
		
		protected EClassifierAction(String name) {
			super(name);
		};

		private boolean enabled = false;
		
		@Override
		public boolean isEnabled() {
			return getSelectedEObject() instanceof EClassifier;
		}
		public void updateEnablement() {
			boolean enabled = getSelectedEObject() instanceof EClassifier;
			if (enabled != this.enabled) {
				this.enabled = enabled;
				firePropertyChange(Action.ENABLED, ! enabled, enabled);
			}
		}
		
		@Override
		public void run() {
			if (isEnabled()) {
				InputDialog dialog = createInputDialog(display.getActiveShell());
				if (dialog.open() == Window.OK) {
					runWithInput((EClass) getSelectedEObject(), dialog.getValue());
					updateView();
				}
			}
		}
		protected abstract InputDialog createInputDialog(Shell shell);
		protected abstract void runWithInput(EClass eClass, String input);
	}

	@Override
	protected void createActions() {
		addConstraintAction = new EClassifierAction("Add constraint") {
			@Override
			protected InputDialog createInputDialog(Shell shell) {
				return new InputDialog(shell, "Add contraint to EClass", "Enter contraint name", "someConstraint", javaNameValidator);
			}
			@Override
			protected void runWithInput(EClass eClass, String input) {
				ensureEClassifierConstraint(eClass, input, "return true;");
				currentConstraintName = input;
			}
		};

		addInvariantAction = new EClassifierAction("Add invariant") {
			protected InputDialog createInputDialog(Shell shell) {
				return new InputDialog(shell, "Add invariant EOperation", "Enter EOperation name", "checkSomething", javaNameValidator);
			}
			protected void runWithInput(EClass eClass, String input) {
				ensureEOperationConstraint(eClass, input, "return true;");
			}
		};
	}

	@Override
	protected void createMenu(IMenuManager mgr) {
		super.createMenu(mgr);
		mgr.add(addConstraintAction);
		mgr.add(addInvariantAction);
	}

	@Override
	protected void createToolbar(IToolBarManager mgr) {
		super.createToolbar(mgr);
		mgr.add(addConstraintAction);
		mgr.add(addInvariantAction);
		mgr.add(new ControlContribution("providerName") {
			@Override
			protected Control createControl(Composite parent) {
				final Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
				combo.setItems(Activator.getDefault().getDelegatesScriptSourceFeatureValueProviderNames());
				int pos = combo.indexOf(currentProviderName);
				if (pos < 0) {
					currentProviderName = combo.getItem(pos = 0);
				}
				combo.select(pos);
				combo.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						currentProviderName = combo.getItem(combo.getSelectionIndex());
						updateView();
					}
				});
				return combo;
			}
		});
	}
}
