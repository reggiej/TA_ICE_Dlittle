package com.icesoft.faces.webapp.http.core;

import java.io.IOException;

import java.util.Map;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.FlowExecutionResult;
import org.springframework.webflow.execution.FlowExecutionOutcome;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.context.servlet.FlowUrlHandler;
import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.registry.NoSuchFlowDefinitionException;

import com.icesoft.faces.webapp.http.servlet.SpringWebFlowInstantiationServlet;

public class SwfLifecycleExecutor extends LifecycleExecutor  {
    private static Log log = LogFactory.getLog(SwfLifecycleExecutor.class);
	private static final String SERVLET_RELATIVE_LOCATION_PREFIX = "servletRelative:";
	private static final String CONTEXT_RELATIVE_LOCATION_PREFIX = "contextRelative:";
	private static final String SERVER_RELATIVE_LOCATION_PREFIX = "serverRelative:";

	private FlowUrlHandler flowUrlHandler = new DefaultFlowUrlHandler();

    public void apply(FacesContext facesContext)  {
        FlowExecutor flowExecutor = (FlowExecutor)
                SpringWebFlowInstantiationServlet.getFlowExecutor();
        ExternalContext externalContext = facesContext.getExternalContext();

        ServletContext servletContext = (ServletContext) externalContext.getContext();
        HttpServletRequest servletRequest = (HttpServletRequest) externalContext.getRequest();
        HttpServletResponse servletResponse = (HttpServletResponse) externalContext.getResponse();
        String flowExecutionKey = servletRequest.getParameter("org.springframework.webflow.FlowExecutionKey");

        // if the ajax value is null, try the URL parameter from a possible GET.
        if (flowExecutionKey == null) {
            flowExecutionKey = servletRequest.getParameter("execution");
        }
        String flowId = firstSegment(servletRequest.getPathInfo());
        try {
            ServletExternalContext servletExternalContext =
                new ServletExternalContext(
                        servletContext, servletRequest, servletResponse );
		servletExternalContext.setAjaxRequest(true);
            FlowExecutionResult result;
            if (null != flowExecutionKey)  {
                result = flowExecutor.resumeExecution(
                        flowExecutionKey, servletExternalContext);
            } else {
                MutableAttributeMap input =
                    defaultFlowExecutionInputMap(servletRequest);
                result = flowExecutor.launchExecution(
                        flowId, input, servletExternalContext);
            }

            // pass the facesContext, as in some circumstances, it's been cleared
            // from the threadlocal.
            handleFlowExecutionResult(result, servletExternalContext,
                    servletRequest, servletResponse, facesContext);
        } catch (NoSuchFlowDefinitionException e)  {
            jsfExecutor.apply(facesContext);
        } catch (IOException e) {
            throw(new FacesException(e));
        }
    }

    public String firstSegment(String path)  {
        String path1 = path.substring(1);
        int end = path1.indexOf("/");
        if (-1 == end) {
            end = path1.length();
        }
        path1 = path1.substring(0, end);
        return path1;
    }

    private void handleFlowExecutionResult(FlowExecutionResult result, ServletExternalContext context,
			HttpServletRequest request, HttpServletResponse response, FacesContext facesContext)  throws IOException {
		if (result.isPaused()) {
			if (context.getFlowExecutionRedirectRequested()) {
				sendFlowExecutionRedirect(result, context, request, response, facesContext);
			} else if (context.getFlowDefinitionRedirectRequested()) {
				sendFlowDefinitionRedirect(result, context, request, response, facesContext);
			} else if (context.getExternalRedirectRequested()) {
				sendExternalRedirect(context.getExternalRedirectUrl(), request, response, facesContext);
			}
		} else if (result.isEnded()) {
			if (context.getFlowDefinitionRedirectRequested()) {
				sendFlowDefinitionRedirect(result, context, request, response, facesContext);
			} else if (context.getExternalRedirectRequested()) {
				sendExternalRedirect(context.getExternalRedirectUrl(), request, response, facesContext);
			} else {
            /* What is the function of the handler?
				String location = handler.handleExecutionOutcome(result.getOutcome(), request, response);
				if (location != null) {
					sendExternalRedirect(location, request, response);
				} else {
            */
                defaultHandleExecutionOutcome(result.getFlowId(), result.getOutcome(), request, response, facesContext);
			}
        } else {
			throw new IllegalStateException("Execution result should have been one of [paused] or [ended]");
		}
	}

	protected void defaultHandleExecutionOutcome(String flowId, FlowExecutionOutcome outcome,
			HttpServletRequest request, HttpServletResponse response, FacesContext facesContext ) throws IOException {

        if (!response.isCommitted()) {
            sendRedirect( flowUrlHandler.createFlowDefinitionUrl(flowId, outcome.getOutput(), request),
                          request,
                          response,
                          facesContext);
        }
    }

	private void sendFlowExecutionRedirect(FlowExecutionResult result, ServletExternalContext context,
			HttpServletRequest request, HttpServletResponse response, FacesContext facesContext) throws IOException {
		String url = flowUrlHandler.createFlowExecutionUrl(result.getFlowId(), result.getPausedKey(), request);
		if (log.isDebugEnabled()) {
			log.debug("Sending flow execution redirect to '" + url + "'");
		}
        /* SWF Ajax popup features
		if (context.isAjaxRequest()) {
			ajaxHandler.sendAjaxRedirect(url, request, response, context.getRedirectInPopup());
		} else {
        */
        sendRedirect(url, request, response, facesContext);
	}

	private void sendFlowDefinitionRedirect(FlowExecutionResult result, ServletExternalContext context,
			HttpServletRequest request, HttpServletResponse response, FacesContext facesContext) throws IOException {
		String flowId = context.getFlowRedirectFlowId();
		MutableAttributeMap input = context.getFlowRedirectFlowInput();
		if (result.isPaused()) {
			input.put("refererExecution", result.getPausedKey());
		}
		String url = flowUrlHandler.createFlowDefinitionUrl(flowId, input, request);
		if (log.isDebugEnabled()) {
			log.debug("Sending flow definition redirect to '" + url + "'");
		}
		sendRedirect(url, request, response, facesContext);
	}

	private void sendExternalRedirect(String location, HttpServletRequest request, HttpServletResponse response,  FacesContext facesContext)
			throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("Sending external redirect to '" + location + "'");
		}
		if (location.startsWith(SERVLET_RELATIVE_LOCATION_PREFIX)) {
			sendServletRelativeRedirect(location.substring(SERVLET_RELATIVE_LOCATION_PREFIX.length()), request,
					response, facesContext);
		} else if (location.startsWith(CONTEXT_RELATIVE_LOCATION_PREFIX)) {
			StringBuffer url = new StringBuffer(request.getContextPath());
			String contextRelativeUrl = location.substring(CONTEXT_RELATIVE_LOCATION_PREFIX.length());
			if (!contextRelativeUrl.startsWith("/")) {
				url.append('/');
			}
			url.append(contextRelativeUrl);
			sendRedirect(url.toString(), request, response, facesContext);
		} else if (location.startsWith(SERVER_RELATIVE_LOCATION_PREFIX)) {
			String url = location.substring(SERVER_RELATIVE_LOCATION_PREFIX.length());
			if (!url.startsWith("/")) {
				url = "/" + url;
			}
			sendRedirect(url, request, response, facesContext);
		} else if (location.startsWith("http://") || location.startsWith("https://")) {
			sendRedirect(location, request, response, facesContext);
		} else {
			sendServletRelativeRedirect(location, request, response, facesContext);
		}
	}

	private void sendServletRelativeRedirect(String location, HttpServletRequest request, HttpServletResponse response, FacesContext facesContext)
			throws IOException {
		StringBuffer url = new StringBuffer(request.getContextPath());
		url.append(request.getServletPath());
		if (!location.startsWith("/")) {
			url.append('/');
		}
		url.append(location);
		sendRedirect(url.toString(), request, response, facesContext);
	}

	private void sendRedirect(String url, HttpServletRequest request, HttpServletResponse response, FacesContext facesContext) throws IOException {
		/* SWF Ajax popup features
        if (ajaxHandler.isAjaxRequest(request, response)) {
			ajaxHandler.sendAjaxRedirect(url, request, response, false);
		} else {
        */
        /* SWF legacy HTTP support
        if (redirectHttp10Compatible) {
            // Always send status code 302.
            response.sendRedirect(response.encodeRedirectURL(url));
        } else {
        */

        ExternalContext ec = null;
        if ( (facesContext != null) && ( (ec = facesContext.getExternalContext() ) != null)) {
            ec.redirect( response.encodeRedirectURL( url ));
        } else {
            // I'm not sure if there is a case for this legacy redirection code.
            // Correct HTTP status code is 303, in particular for POST requests.
            response.setStatus(303);
            response.setHeader("Location", response.encodeRedirectURL(url));
        }
	}

    protected MutableAttributeMap defaultFlowExecutionInputMap(HttpServletRequest request) {
        LocalAttributeMap inputMap = new LocalAttributeMap();
        Map parameterMap = request.getParameterMap();
        Iterator it = parameterMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String name = (String) entry.getKey();
            String[] values = (String[]) entry.getValue();
            if (values.length == 1) {
                inputMap.put(name, values[0]);
            } else {
                inputMap.put(name, values);
            }
        }
        return inputMap;
    }

}
