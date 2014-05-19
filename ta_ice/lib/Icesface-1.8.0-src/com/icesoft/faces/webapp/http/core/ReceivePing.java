package com.icesoft.faces.webapp.http.core;

import com.icesoft.faces.webapp.command.CommandQueue;
import com.icesoft.faces.webapp.command.Pong;
import com.icesoft.faces.webapp.http.common.Request;
import com.icesoft.faces.webapp.http.common.Server;
import com.icesoft.faces.webapp.http.common.standard.EmptyResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

public class ReceivePing implements Server {
    private static Log log = LogFactory.getLog(ReceivePing.class);
    private static final Pong PONG = new Pong();
    private Map commandQueues;

    public ReceivePing(Map commandQueues) {
        this.commandQueues = commandQueues;
    }

    public void service(Request request) throws Exception {
        String viewIdentifier = request.getParameter("ice.view");
        CommandQueue queue = (CommandQueue) commandQueues.get(viewIdentifier);
        if (queue != null) {
            queue.put(PONG);
        } else {
            if (log.isWarnEnabled()) {
                log.warn("could not get a valid queue for " + viewIdentifier);
            }
        }
        request.respondWith(EmptyResponse.Handler);
    }

    public void shutdown() {
    }
}
