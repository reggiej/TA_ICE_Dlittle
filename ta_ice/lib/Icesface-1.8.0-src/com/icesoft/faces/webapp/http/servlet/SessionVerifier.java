package com.icesoft.faces.webapp.http.servlet;

import com.icesoft.faces.webapp.http.common.standard.ResponseHandlerServer;
import com.icesoft.faces.webapp.http.core.SessionExpiredException;
import com.icesoft.faces.webapp.http.core.SessionExpiredResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SessionVerifier implements PseudoServlet {
    private static final PseudoServlet SessionExpiredServlet = new BasicAdaptingServlet(new ResponseHandlerServer(SessionExpiredResponse.Handler));
    private PseudoServlet servlet;
    private boolean xmlResponse;

    public SessionVerifier(PseudoServlet servlet, boolean xmlResponse) {
        this.servlet = servlet;
        this.xmlResponse = xmlResponse;
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (request.isRequestedSessionIdValid()) {
            servlet.service(request, response);
        } else {
            if (xmlResponse) {
                SessionExpiredServlet.service(request, response);
            } else {
                throw new SessionExpiredException();
            }
        }
    }

    public void shutdown() {
        servlet.shutdown();
    }
}
