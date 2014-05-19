package com.icesoft.faces.webapp.http.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;

import com.icesoft.faces.webapp.http.servlet.SpringWebFlowInstantiationServlet;

public abstract class LifecycleExecutor {
    private static Log log = LogFactory.getLog(LifecycleExecutor.class);
    static LifecycleExecutor jsfExecutor = null;
    private static LifecycleExecutor swfExecutor = null;

    public static LifecycleExecutor getLifecycleExecutor(FacesContext context)  {
        init();
        if (null != swfExecutor)  {
            //Spring Web Flow URLs do not typically contain file extensions
            //this is not the correct way to determine whether to delegate
            //these requests
           if (!isExtensionMapped(context))  {
                return swfExecutor;
            }
        }
        return jsfExecutor;
    }

    public abstract void apply(FacesContext facesContext);

    private static void init()  {
        if (null != jsfExecutor)  {
            return;
        }
        Object flowExecutor = null;
        try {
            flowExecutor = SpringWebFlowInstantiationServlet.getFlowExecutor();
        } catch (Throwable t)  {
            if (log.isDebugEnabled()) {
                log.debug("SpringWebFlow unavailable ");
            }
        }

        if (null != flowExecutor)  {
            swfExecutor = new SwfLifecycleExecutor();
        }
        
        jsfExecutor = new JsfLifecycleExecutor();
    }
    
    static boolean isExtensionMapped(FacesContext facesContext)  {
        Object request = facesContext.getExternalContext().getRequest();
        if (request instanceof HttpServletRequest)  {
            String requestURI = ((HttpServletRequest) request).getRequestURI();
            int slashIndex = requestURI.lastIndexOf("/");
            int dotIndex = requestURI.lastIndexOf(".");
            if (slashIndex < dotIndex) {
                return true;
            }
        }

        return false;
    }

}