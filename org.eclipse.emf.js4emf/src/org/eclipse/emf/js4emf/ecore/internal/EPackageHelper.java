package org.eclipse.emf.js4emf.ecore.internal;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

public class EPackageHelper extends JavascriptSupportHelper {

	public EPackageHelper(JavascriptSupportImpl javascriptSupport) {
		super(javascriptSupport);
	}

	private URIConverter getURIConverter() {
		return getJavascriptSupport().getURIConverter();
	}

	void addEPackageVariable(EClassifier prototypeClass) {
		EPackage ePack = prototypeClass.getEPackage();
		addEPackageVariable(ePack);
	}

	public EPackage loadEPackage(String packageUri, String schemaUri) {
		EPackage ePack = getEPackage(packageUri, schemaUri);
		if (ePack == null) {
			throw new IllegalArgumentException("No package with URI " + packageUri + (schemaUri != null ? " @ " + schemaUri : "") + " found");
		}
		registerEPackage(ePack, schemaUri);
		return ePack;
	}

	public void registerEPackage(EPackage ePack, String schemaUri) {
		if (schemaUri != null) {
			registerSchemaUri(ePack, schemaUri);
		}
		addEPackageVariable(ePack);
	}

	private void registerSchemaUri(EPackage ePack, String schemaUriString) {
		URI packageUri = URI.createURI(ePack.getNsURI());
		URI schemaUri = getURIConverter().normalize(schemaUriString != null ? URI.createURI(schemaUriString) : packageUri);
		if (schemaUri != null && (! schemaUri.equals(packageUri))) {
			getURIConverter().getURIMap().put(Util.createParentFolderUri(packageUri), Util.createParentFolderUri(schemaUri));
		}
	}

	private ResourceSet packagesResourceSet = null;
	
	private EPackage getEPackage(String packageUriString, String schemaUriString) {
		Registry ePackageRegistry = EPackage.Registry.INSTANCE;
		EPackage ePack = ePackageRegistry.getEPackage(packageUriString);
		if (ePack != null) {
			return ePack;
		}
		if (packagesResourceSet == null) {
			packagesResourceSet = new ResourceSetImpl();
			packagesResourceSet.setURIConverter(getURIConverter());
		}
		for (Resource packageResource: packagesResourceSet.getResources()) {
			ePack = getResourcePackage(packageResource, packageUriString);
			if (ePack != null) {
				return ePack;
			}
		}
		URI packageUri = URI.createURI(packageUriString);
		URI schemaUri = getURIConverter().normalize(schemaUriString != null ? URI.createURI(schemaUriString) : packageUri);
		Resource packageResource = packagesResourceSet.getResource(schemaUri, true);
		ePack = getResourcePackage(packageResource, packageUriString);
		if (ePack != null) {
			ePackageRegistry.put(ePack.getNsURI(), ePack);
			registerSchemaUri(ePack, schemaUriString);
		}
		return ePack;
	}

	private EPackage getResourcePackage(Resource packageResource, String packageUri) {
		if (packageResource != null && packageResource.getContents().size() > 0) {
			EObject eObject = packageResource.getContents().get(0);
			if (eObject instanceof EPackage) {
				EPackage ePack = (EPackage)eObject;
				if (packageUri == null || packageUri.equals(ePack.getNsURI())) {
					return ePack;
				}
			}
		}
		return null;
	}
	
	private void addEPackageVariable(EPackage ePack) {
		String packVariableName = getJavascriptSupport().getNameSupport().getNamePropertyName(ePack);
		if (packVariableName != null && getJavascriptSupport().getVariable(null, packVariableName) == null) {
			// indicate loading state to prevent recursive call
			getJavascriptSupport().setVariable(null, packVariableName, ePack.getNsURI());
			// start loading
			getJavascriptSupport().setVariable(null, packVariableName, ePack);
		}
	}
}
