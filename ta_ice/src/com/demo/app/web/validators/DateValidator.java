package com.demo.app.web.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

public class DateValidator implements Validator {
	
	private static final String DATE_FMT = "[0-3]{1}[0-9]{1}[-]{1}[A-Za-z]{3}[-]{1}[1-2]{1}[0-9]{3}";

	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		
		Pattern mask = Pattern.compile(DATE_FMT);
		String date = (String)value;
		Matcher matcher = mask.matcher(date);
		
		if(!matcher.matches()) {
			FacesMessage msg = new FacesMessage();
			msg.setDetail("Not a valid format for Artifact Date (dd-MMM-yyyy)");
			msg.setSummary("Not a valid format for Artifact Date (dd-MMM-yyyy)");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}
	}
}
