/**
 * 
 */
package com.demo.app.web.validators;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

public class PasswordValidator implements Validator {
	
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		if(true) {
			
		}
		else {
			FacesMessage message = new FacesMessage();
            message.setDetail("not in valid format");
            message.setSummary("not in valid format");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
		}
		
		// TODO 
		
//    	if(currentUser.getPassword().equals(encryptedPassword)) {
////    		System.out.println("encryptedNewPassword: " + encryptedNewPassword);
////    		if(!(encryptedNewPassword.equals(currentPassword)) ) {
//		    	if(newPassword.equals(confirmPassword)) {
//		    		currentUser.setPassword(newPassword);
//		    		currentUser.setChangePassword(Boolean.FALSE);
//		    		service.resetPasswordByUsername(currentUser);
//		    	}
//		    	else {
//		    		super.addErrorMessage("New passwords do not match.");
//		    	}
////    		}
////    		else {
////    			super.addErrorMessage("New password cannot be the same as the previous password.");
////    		}
//    	}
//	    else {
//	    	super.addErrorMessage("Current password is incorrect.");
//	    }

    }
}
