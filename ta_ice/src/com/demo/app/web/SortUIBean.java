package com.demo.app.web;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

public abstract class SortUIBean {
	
	protected String sortColumnName;
	protected boolean ascending;
	protected String oldSort;
	protected boolean oldAscending;
	
	protected SortUIBean(String defaultSortColumn) {
		sortColumnName = defaultSortColumn;
		ascending = isDefaultAscending(defaultSortColumn);
		oldSort = sortColumnName;
		oldAscending = !ascending;
	}
	
	/**
	 *
	 * @return
	 */
	public FacesContext getFacesContext (){
		return FacesContext.getCurrentInstance();
	}

	public void addInfoMessage (String message){
		FacesContext context = FacesContext.getCurrentInstance();
       FacesMessage fmessage = new FacesMessage();
       fmessage.setSeverity(FacesMessage.SEVERITY_INFO);
       fmessage.setSummary(message);
       context.addMessage(getBeanName(), fmessage);
	}

	public void addWarningMessage (String message){
		FacesContext context = FacesContext.getCurrentInstance();
       FacesMessage fmessage = new FacesMessage();
       fmessage.setSeverity(FacesMessage.SEVERITY_WARN);
       fmessage.setSummary(message);
       context.addMessage(getBeanName(), fmessage);
	}

	public void addErrorMessage (String message){
       FacesContext context = FacesContext.getCurrentInstance();
       FacesMessage fmessage = new FacesMessage();
       fmessage.setSeverity(FacesMessage.SEVERITY_ERROR);
       fmessage.setSummary(message);
       context.addMessage(getBeanName(), fmessage);
   }

	public Object getService (String serviceName){
		return ServiceLocator.getInstance().getService(serviceName);
	}

	public Object getBeanReference(String beanName){
		if (beanName != null)
			return resolveExpression ("#{" +beanName+ "}");
		else
			return null;
	}

	public void resetToValue(Object beanValue) {
		if (getBeanName() != null) {
			setValueFromExpression("#{" + getBeanName() + "}", beanValue);

		} else
			throw new WebException(
					"Bean name was not set properly, getBeanName cannot returned null");

	}

	/**
	 * Method for taking a reference to a JSF binding expression and returning
	 * the matching object (or creating it).
	 *
	 * @param expression
	 *            EL expression
	 * @return Managed object
	 */
	protected static Object resolveExpression(String expression) {
		if (expression != null) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			Application app = facesContext.getApplication();
			ExpressionFactory elFactory = app.getExpressionFactory();
			ELContext elContext = facesContext.getELContext();
			ValueExpression valueExp = elFactory.createValueExpression(
					elContext, expression, Object.class);
			return valueExp.getValue(elContext);
		} else
			throw new WebException(
					"Parameter expression cannot be null");
	}


	/**
	 * Method for setting a value to a reference to a JSF binding expression and
	 * returning the matching object (or creating it).
	 *
	 * @param expression
	 *            EL expression
	 * @param value
	 *            value of the object
	 * @return Managed object
	 * @throws WebException
	 *             if <code>expression</code> is <code>null</code>
	 */
	protected static void setValueFromExpression(String expression, Object value) {
		if (expression != null) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			Application app = facesContext.getApplication();
			ExpressionFactory elFactory = app.getExpressionFactory();
			ELContext elContext = facesContext.getELContext();
			ValueExpression valueExp = elFactory.createValueExpression(
					elContext, expression, Object.class);
			valueExp.setValue(elContext, value);
		} else
			throw new WebException(
					"Parameter expression cannot be null");
	}

	public abstract String getBeanName ();

	/*
	 *	Method for returning current logged-in username.
	 *
	 * @return User username
	 */
	/*public User getCurrentUser() {*/
//		SiteAdminService service = 
//			(SiteAdminService)getService(ASDWConstants.SERVICE_SITEADMIN_REFERENCE);
		
//		return service.getCurrentUser();
		
		/*User currentUser = new User();
		currentUser.setUsername(getCurrentUserID());
		
		return currentUser;
	}*/
	
	/*
	 *	Method for returning current logged-in User object.
	 *
	 * @return User user object
	 */
//	public User getCurrentUserObject() {
//		SiteAdminService service = 
//			(SiteAdminService)getService(ASDWConstants.SERVICE_SITEADMIN_REFERENCE);
//		
//		return service.getCurrentUserByUserId(getCurrentUserID());
//	}
	
	/*
	 *	Method for returning current logged-in userID as a String. 
	 * 
	 * 	 @return String userID
	 */
	public String getCurrentUserID() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();
		String userID = externalContext.getRemoteUser();
		
		return userID;
	}
	
	protected abstract void sort();
	
	protected abstract boolean isDefaultAscending(String sortColumn);

	public String getSortColumnName() {
		return sortColumnName;
	}

	public void setSortColumnName(String sortColumnName) {
		oldSort = this.sortColumnName;
		this.sortColumnName = sortColumnName;
	}
	
	public boolean isAscending() {
		return ascending;
	}
	
	public void setAscending(boolean ascending) {
		oldAscending = this.ascending;
		this.ascending = ascending;
	}


}
