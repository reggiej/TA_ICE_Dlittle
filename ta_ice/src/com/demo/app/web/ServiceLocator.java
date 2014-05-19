/**
 *
 */
package com.demo.app.web;

import javax.faces.context.FacesContext;

import org.springframework.context.ApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * @author Kunta l.
 *
 */
public class ServiceLocator {

	private static ApplicationContext applicationContext;

	private static ServiceLocator instance;

	private ServiceLocator (){
		FacesContext facesContext = FacesContext.getCurrentInstance();
		applicationContext = FacesContextUtils.getRequiredWebApplicationContext(facesContext);
	}

	/**
	 *
	 * @param serviceName Name of the Spring service
	 * @return Instance of the Spring service that is designated by <code>serviceName</code>
	 */
	public Object getService (String serviceName){
		if (applicationContext != null){
			return applicationContext.getBean(serviceName);
		}
		else
			return null;
	}

	/**
	 *
	 * @return Singleton instance of <code>ServiceLocator</code>
	 */
	public static ServiceLocator getInstance (){
		if (instance == null)
			instance = new ServiceLocator ();
		return instance;
	}




}
