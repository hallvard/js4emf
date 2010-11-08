package org.eclipse.emf.js4emf.ecore.internal;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.mozilla.javascript.Function;

public class NotificationHelper {

	private JavascriptSupport javascriptSupport;

	public NotificationHelper(JavascriptSupport javascriptSupport) {
		super();
		this.javascriptSupport = javascriptSupport;
	}
	
	private StringBuilder methodName = new StringBuilder();

	private String getEventName(int eventType) {
		String eventName = "Change";
		switch (eventType) {
		case Notification.SET: 			eventName = "Set"; 			break;
		case Notification.UNSET: 		eventName = "Unset"; 		break;
		case Notification.ADD: 			eventName = "Add"; 			break;
		case Notification.REMOVE: 		eventName = "Remove"; 		break;
		case Notification.ADD_MANY: 	eventName = "AddMany"; 		break;
		case Notification.REMOVE_MANY: 	eventName = "RemoveMany"; 	break;
		case Notification.MOVE: 		eventName = "Move"; 		break;
		}
		return eventName;
	}

	private String getMethodName(EStructuralFeature feature, int eventType) {
		methodName.setLength(0);
		String eventName = getEventName(eventType);
		String featureName = feature.getName();
		methodName.append("on");
		methodName.append(eventName);
		char first = featureName.charAt(0);
		if (Character.isUpperCase(first)) {
			methodName.append(featureName);
		} else {
			methodName.append(Character.toUpperCase(first));
			methodName.append(featureName, 1, featureName.length());
		}
		return methodName.toString().intern();
	}

	synchronized void notifyChanged(Notification notification, EObject handler, boolean rethrowException) {
		Object feature = notification.getFeature();
		if (feature instanceof EStructuralFeature) {
			String specificMethodName = getMethodName((EStructuralFeature)feature, notification.getEventType());
			String genericMethodName = getMethodName((EStructuralFeature)feature, -1);
			Object[] methodArgs = new Object[]{notification};
			Object funObject = null;
			if ((funObject = this.javascriptSupport.getProperty(handler, specificMethodName)) instanceof Function) {
//			System.out.println("Notifying " + javascriptSupport.toString(handler) + " with " + specificMethodName);
				this.javascriptSupport.callMethod(handler, (Function) funObject, methodArgs, rethrowException);
			} else if ((funObject = this.javascriptSupport.getProperty(handler, genericMethodName)) instanceof Function) {
//			System.out.println("Notifying " + javascriptSupport.toString(handler) + " with " + genericMethodName);
				this.javascriptSupport.callMethod(handler, (Function) funObject, methodArgs, rethrowException);
			} else if ((funObject = this.javascriptSupport.getVariable(handler.eResource(), specificMethodName)) instanceof Function) {
//				System.out.println("Notifying change of " + javascriptSupport.toString(handler) + " with " + specificMethodName);
				this.javascriptSupport.callFunction(handler.eResource(), (Function) funObject, methodArgs, rethrowException);
			} else if ((funObject = this.javascriptSupport.getVariable(handler.eResource(), genericMethodName)) instanceof Function) {
//			System.out.println("Notifying change of " + javascriptSupport.toString(handler) + " with " + genericMethodName);
				this.javascriptSupport.callFunction(handler.eResource(), (Function) funObject, methodArgs, rethrowException);
			}
		} else {
//			System.out.println("Notified by " + javascriptSupport.toString(handler) + ", but feature is " + notification.getFeature());
		}
	}
}
