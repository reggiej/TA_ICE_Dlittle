/*
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * "The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations under
 * the License.
 *
 * The Original Code is ICEfaces 1.5 open source software code, released
 * November 5, 2006. The Initial Developer of the Original Code is ICEsoft
 * Technologies Canada, Corp. Portions created by ICEsoft are Copyright (C)
 * 2004-2006 ICEsoft Technologies Canada, Corp. All Rights Reserved.
 *
 * Contributor(s): _____________________.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"
 * License), in which case the provisions of the LGPL License are
 * applicable instead of those above. If you wish to allow use of your
 * version of this file only under the terms of the LGPL License and not to
 * allow others to use your version of this file under the MPL, indicate
 * your decision by deleting the provisions above and replace them with
 * the notice and other provisions required by the LGPL License. If you do
 * not delete the provisions above, a recipient may use your version of
 * this file under either the MPL or the LGPL License."
 *
 */

package com.icesoft.faces.util.event.servlet;

import com.icesoft.faces.webapp.http.common.Configuration;
import com.icesoft.faces.webapp.http.servlet.SessionDispatcher;
import com.icesoft.faces.webapp.http.servlet.ServletContextConfiguration;
import com.icesoft.net.messaging.Message;
import com.icesoft.net.messaging.MessageServiceClient;
import com.icesoft.net.messaging.MessageServiceException;
import com.icesoft.util.Properties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * The ContextEventRepeater was designed to forward servlet events to different
 * parts of the ICEfaces framework. These events are typically of interest for
 * gracefully and/or proactively keeping track of valid sessions and allowing
 * for orderly shut down.
 *
 * This was deemed necessary since the Servlet specification does not allow a
 * programmatic way of adding and removing listeners for these events. So rather
 * than have the various ICEfaces pieces register listeners individually, we can
 * register this class and then add and remove listeners as required.
 *
 * The implementation is currently simple and broad. The class maintains a
 * static collection of listeners in a WeakHashMap and forwards all events to
 * all registered listeners.
 *
 * Future improvements might include:
 * - adapter implementations
 * - event filtering
 *
 * For now, anything that is interested in receiving events from the repeater
 * should simply implement the ContextEventListener interface and then add
 * itself to the ContextEventRepeater using the static addListener method.
 *
 * The limitation of adding a listener programmatically is that certain creation
 * events can occur before the class has a chance to add itself as a listener.
 * To mitigate this, the ContextEventRepeater buffers the creation events
 * temporarily.  When a ContextEventListener is added, the receiveBufferedEvents
 * method is called and, if it returns true, any buffered creation events are
 * sent to the listener after it has been added to the listener collection. The
 * timing of the events is off but the session information can still be useful.
 * Events are removed from the buffer when the corresponding destroy events are
 * received.  This means that that sessions that have already been created AND
 * destroyed are NOT in the buffer.
 */

/**
 *
 */
public class ContextEventRepeater
implements HttpSessionListener, ServletContextListener {
    private static final Log LOG =
        LogFactory.getLog(ContextEventRepeater.class);

    private static final String BUFFERED_CONTEXT_EVENTS_MESSAGE_TYPE =
        "BufferedContextEvents";
    private static final String CONTEXT_EVENT_MESSAGE_TYPE =
        "ContextEvent";

    //todo: fix it... this is just a temporary solution
    private static SessionDispatcher.Listener SessionDispatcherListener;

    static {
        SessionDispatcherListener = new SessionDispatcher.Listener();
    }

    private static List bufferedContextEvents = new ArrayList();
    private static Map listeners = new WeakHashMap();
    private static Configuration servletContextConfiguration;
    private static String blockingRequestHandlerContext;
    private static MessageServiceClient messageServiceClient;

    private static AnnouncementMessageHandler announcementMessageHandler =
        new AnnouncementMessageHandler() {
            public void publishBufferedContextEvents() {
                ContextEvent[] _contextEvents =
                    (ContextEvent[])
                        bufferedContextEvents.toArray(
                            new ContextEvent[bufferedContextEvents.size()]);
                if (_contextEvents.length != 0) {
                    StringBuffer _message = new StringBuffer();
                    for (int i = 0; i < _contextEvents.length; i++) {
                        if (i != 0) {
                            _message.append("\r\n");
                        }
                        _message.append(createMessage(_contextEvents[i]));
                    }
                    Properties _messageProperties = new Properties();
                    _messageProperties.
                        setStringProperty(
                            Message.DESTINATION_SERVLET_CONTEXT_PATH,
                            blockingRequestHandlerContext);
                    messageServiceClient.publish(
                        _message.toString(),
                        _messageProperties,
                        BUFFERED_CONTEXT_EVENTS_MESSAGE_TYPE,
                        MessageServiceClient.PUSH_TOPIC_NAME);
                }
            }
        };

    /**
     * Adds the specified <code>listener</code> to this
     * <code>ContextEventRepeater</code>. </p>
     *
     * @param contextEventListener the listener to be added.
     */
    public synchronized static void addListener(
        final ContextEventListener contextEventListener) {

        if (contextEventListener == null ||
            listeners.containsKey(contextEventListener)) {

            return;
        }
        listeners.put(contextEventListener, null);
        if (contextEventListener.receiveBufferedEvents()) {
            sendBufferedEvents(contextEventListener);
        }
    }

    /**
     * Fires a new <code>ContextDestroyedEvent</code>, based on the received
     * <code>event</code>, to all registered listeners, and cleans itself
     * up. </p>
     *
     * @param event the servlet context event.
     */
    public synchronized void contextDestroyed(final ServletContextEvent event) {
        SessionDispatcherListener.contextDestroyed(event);

        ContextDestroyedEvent contextDestroyedEvent =
            new ContextDestroyedEvent(event);
        Iterator it = listeners.keySet().iterator();
        while (it.hasNext()) {
            ((ContextEventListener) it.next()).
                contextDestroyed(contextDestroyedEvent);
        }
        listeners.clear();
        bufferedContextEvents.clear();
        if (LOG.isInfoEnabled()) {
            ServletContext servletContext =
                contextDestroyedEvent.getServletContext();
            LOG.info(
                "Servlet Context Name: " +
                    servletContext.getServletContextName() + ", " +
                "Server Info: " + servletContext.getServerInfo());
        }
    }

    public synchronized void contextInitialized(
        final ServletContextEvent event) {

        servletContextConfiguration =
            new ServletContextConfiguration(
                "com.icesoft.faces", event.getServletContext());
        SessionDispatcherListener.contextInitialized(event);
    }

    public synchronized static void iceFacesIdDisposed(
        final Object source, final String iceFacesId) {

        ICEfacesIDDisposedEvent iceFacesIdDisposedEvent =
            new ICEfacesIDDisposedEvent(source, iceFacesId);
        Iterator _listeners = listeners.keySet().iterator();
        while (_listeners.hasNext()) {
            ((ContextEventListener) _listeners.next()).
                iceFacesIdDisposed(iceFacesIdDisposedEvent);
        }
        removeBufferedEvents(iceFacesId);
        if (messageServiceClient != null) {
            Properties _messageProperties = new Properties();
            _messageProperties.
                setStringProperty(
                    Message.DESTINATION_SERVLET_CONTEXT_PATH,
                    blockingRequestHandlerContext);
            messageServiceClient.publish(
                createMessage(iceFacesIdDisposedEvent),
                _messageProperties,
                CONTEXT_EVENT_MESSAGE_TYPE,
                MessageServiceClient.PUSH_TOPIC_NAME);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace(
                "ICEfaces ID disposed: " +
                    iceFacesIdDisposedEvent.getICEfacesID());
        }
    }

    /**
     * Fires a new <code>ICEfacesIDRetrievedEvent</code>, with the specified
     * <code>source</code> and </code>iceFacesId</code>, to all registered
     * listeners. </p>
     *
     * @param source     the source of the event.
     * @param iceFacesId the ICEfaces ID.
     */
    public synchronized static void iceFacesIdRetrieved(
        final Object source, final String iceFacesId) {

        ICEfacesIDRetrievedEvent iceFacesIdRetrievedEvent =
            new ICEfacesIDRetrievedEvent(source, iceFacesId);
        bufferedContextEvents.add(iceFacesIdRetrievedEvent);
        Iterator _listeners = listeners.keySet().iterator();
        while (_listeners.hasNext()) {
            ((ContextEventListener) _listeners.next()).
                iceFacesIdRetrieved(iceFacesIdRetrievedEvent);
        }
        if (messageServiceClient != null) {
            Properties _messageProperties = new Properties();
            _messageProperties.
                setStringProperty(
                    Message.DESTINATION_SERVLET_CONTEXT_PATH,
                    blockingRequestHandlerContext);
            messageServiceClient.publish(
                createMessage(iceFacesIdRetrievedEvent),
                _messageProperties,
                CONTEXT_EVENT_MESSAGE_TYPE,
                MessageServiceClient.PUSH_TOPIC_NAME);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace(
                "ICEfaces ID retrieved: " +
                    iceFacesIdRetrievedEvent.getICEfacesID());
        }
    }

    /**
     * Removes the specified <code>listener</code> from this
     * <code>ContextEventRepeater</code>. </p>
     *
     * @param contextEventListener the listener to be removed.
     */
    public synchronized static void removeListener(
        final ContextEventListener contextEventListener) {

        if (contextEventListener == null) {
            return;
        }
        listeners.remove(contextEventListener);
    }

    public synchronized void sessionCreated(final HttpSessionEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Session Created event: " + event.getSession().getId());
        }
    }

    /**
     * Fires a new <code>SessionDestroyedEvent</code>, based on the received
     * <code>event</code>, to all registered listeners. </p>
     *
     * @param event the HTTP session event.
     */
    public synchronized void sessionDestroyed(final HttpSessionEvent event) {
        if (LOG.isDebugEnabled() ) {
            LOG.debug("Session Destroyed event: " + event.getSession().getId());
        }
        // #3073 directly invoke method on SessionDispatcher for all sessions
        // (Not just wrapped ones)
        SessionDispatcherListener.sessionDestroyed(event);
        SessionDestroyedEvent sessionDestroyedEvent =
            new SessionDestroyedEvent(event);
        Iterator _listeners = listeners.keySet().iterator();
        while (_listeners.hasNext()) {
            ((ContextEventListener) _listeners.next()).
                sessionDestroyed(sessionDestroyedEvent);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("ICEfaces ID: " + sessionDestroyedEvent.getICEfacesID());
        }
    }

    public synchronized static void setMessageServiceClient(
        final MessageServiceClient client) {

        if (client != null) {
            blockingRequestHandlerContext =
                servletContextConfiguration.getAttribute(
                    "blockingRequestHandlerContext", "push-server");
            messageServiceClient = client;
            try {
                messageServiceClient.subscribe(
                    MessageServiceClient.PUSH_TOPIC_NAME,
                    announcementMessageHandler.getMessageSelector());
            } catch (MessageServiceException exception) {
                if (LOG.isFatalEnabled()) {
                    LOG.fatal(
                        "\r\n" +
                        "\r\n" +
                        "Failed to subscribe to topic: " +
                            MessageServiceClient.PUSH_TOPIC_NAME + "\r\n" +
                        "    Exception message: " +
                            exception.getMessage() + "\r\n" +
                        "    Exception cause: " +
                            exception.getCause() + "\r\n\r\n");
                }
                blockingRequestHandlerContext = null;
                messageServiceClient = null;
                return;
            }
            messageServiceClient.addMessageHandler(
                announcementMessageHandler,
                MessageServiceClient.PUSH_TOPIC_NAME);
        }
    }

    public synchronized static void viewNumberDisposed(
        final Object source, final String iceFacesId,
        final int viewNumber) {

        ViewNumberDisposedEvent viewNumberDisposedEvent =
            new ViewNumberDisposedEvent(source, iceFacesId, viewNumber);
        Iterator _listeners = listeners.keySet().iterator();
        while (_listeners.hasNext()) {
            ((ContextEventListener)_listeners.next()).
                viewNumberDisposed(viewNumberDisposedEvent);
        }
        removeBufferedEvents(iceFacesId, viewNumber);
        if (messageServiceClient != null) {
            Properties _messageProperties = new Properties();
            _messageProperties.
                setStringProperty(
                    Message.DESTINATION_SERVLET_CONTEXT_PATH,
                    blockingRequestHandlerContext);
            messageServiceClient.publish(
                createMessage(viewNumberDisposedEvent),
                _messageProperties,
                CONTEXT_EVENT_MESSAGE_TYPE,
                MessageServiceClient.PUSH_TOPIC_NAME);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace(
                "View Number disposed: " +
                    viewNumberDisposedEvent.getViewNumber() + " " +
                        "[ICEfaces ID: " +
                            viewNumberDisposedEvent.getICEfacesID() +
                        "]");
        }
    }
    /**
     * Fires a new <code>ViewNumberRetrievedEvent</code>, with the specified
     * <code>source</code> and </code>viewNumber</code>, to all registered
     * listeners. </p>
     *
     * @param source     the source of the event.
     * @param iceFacesId the ICEfaces ID.
     * @param viewNumber the view number.
     */
    public synchronized static void viewNumberRetrieved(
        final Object source, final String iceFacesId,
        final int viewNumber) {

        ViewNumberRetrievedEvent viewNumberRetrievedEvent =
            new ViewNumberRetrievedEvent(source, iceFacesId, viewNumber);
        bufferedContextEvents.add(viewNumberRetrievedEvent);
        Iterator _listeners = listeners.keySet().iterator();
        while (_listeners.hasNext()) {
            ((ContextEventListener) _listeners.next()).
                viewNumberRetrieved(viewNumberRetrievedEvent);
        }
        if (messageServiceClient != null) {
            Properties _messageProperties = new Properties();
            _messageProperties.
                setStringProperty(
                    Message.DESTINATION_SERVLET_CONTEXT_PATH,
                    blockingRequestHandlerContext);
            messageServiceClient.publish(
                createMessage(viewNumberRetrievedEvent),
                _messageProperties,
                CONTEXT_EVENT_MESSAGE_TYPE,
                MessageServiceClient.PUSH_TOPIC_NAME);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace(
                "View Number retrieved: " +
                    viewNumberRetrievedEvent.getViewNumber() + " " +
                        "[ICEfaces ID: " +
                            viewNumberRetrievedEvent.getICEfacesID() +
                        "]");
        }
    }

    ContextEvent[] getBufferedContextEvents() {
        return
            (ContextEvent[])
                bufferedContextEvents.toArray(
                    new ContextEvent[bufferedContextEvents.size()]);
    }

    private static String createMessage(final ContextEvent event) {
        if (event instanceof ICEfacesIDDisposedEvent) {
            return
                "ICEfacesIDDisposed;" +
                    ((ICEfacesIDDisposedEvent)event).getICEfacesID();
        } else if (event instanceof ICEfacesIDRetrievedEvent) {
            return
                "ICEfacesIDRetrieved;" +
                    ((ICEfacesIDRetrievedEvent)event).getICEfacesID();
        } else if (event instanceof ViewNumberDisposedEvent) {
            return
                "ViewNumberDisposed;" +
                    ((ViewNumberDisposedEvent)event).getICEfacesID() + ";" +
                    ((ViewNumberDisposedEvent)event).getViewNumber();
        } else if (event instanceof ViewNumberRetrievedEvent) {
            return
                "ViewNumberRetrieved;" +
                    ((ViewNumberRetrievedEvent)event).getICEfacesID() + ";" +
                    ((ViewNumberRetrievedEvent)event).getViewNumber();
        } else {
            return null;
        }
    }

    private synchronized static void removeBufferedEvents(
        final String iceFacesId) {

        Iterator it = bufferedContextEvents.iterator();
        while (it.hasNext()) {
            Object event = it.next();
            if ((event instanceof ICEfacesIDRetrievedEvent &&
                ((ICEfacesIDRetrievedEvent)event).
                    getICEfacesID().equals(iceFacesId)) ||
                (event instanceof ViewNumberRetrievedEvent &&
                ((ViewNumberRetrievedEvent)event).
                    getICEfacesID().equals(iceFacesId))) {

                it.remove();
            }
        }
    }

    private synchronized static void removeBufferedEvents(
        final String iceFacesId, final int viewNumber) {

        Iterator it = bufferedContextEvents.iterator();
        while (it.hasNext()) {
            Object event = it.next();
            if (event instanceof ViewNumberRetrievedEvent &&
                ((ViewNumberRetrievedEvent)event).
                    getICEfacesID().equals(iceFacesId) &&
                ((ViewNumberRetrievedEvent)event).
                    getViewNumber() == viewNumber) {

                it.remove();
            }
        }
    }

    private synchronized static void sendBufferedEvents(
        final ContextEventListener contextEventListener) {

        Iterator it = bufferedContextEvents.iterator();
        while (it.hasNext()) {
            Object event = it.next();
            if (event instanceof ICEfacesIDRetrievedEvent) {
                contextEventListener.iceFacesIdRetrieved(
                    (ICEfacesIDRetrievedEvent)event);
            } else if (event instanceof ViewNumberRetrievedEvent) {
                contextEventListener.viewNumberRetrieved(
                    (ViewNumberRetrievedEvent)event);
            }
        }
    }
}
