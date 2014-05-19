package com.icesoft.faces.webapp.http.servlet;

import com.icesoft.faces.async.render.RenderManager;
import com.icesoft.faces.env.Authorization;
import com.icesoft.faces.webapp.http.common.Configuration;
import com.icesoft.faces.webapp.http.common.FileLocator;
import com.icesoft.faces.webapp.http.common.MimeTypeMatcher;
import com.icesoft.faces.webapp.http.common.Request;
import com.icesoft.faces.webapp.http.common.Server;
import com.icesoft.faces.webapp.http.common.standard.NotFoundHandler;
import com.icesoft.faces.webapp.http.core.DisposeBeans;
import com.icesoft.faces.webapp.http.core.ResourceServer;
import com.icesoft.faces.util.event.servlet.ContextEventRepeater;
import com.icesoft.faces.application.ProductInfo;
import com.icesoft.net.messaging.AbstractMessageHandler;
import com.icesoft.net.messaging.Message;
import com.icesoft.net.messaging.MessageHandler;
import com.icesoft.net.messaging.MessageSelector;
import com.icesoft.net.messaging.MessageServiceClient;
import com.icesoft.net.messaging.MessageServiceException;
import com.icesoft.net.messaging.TextMessage;
import com.icesoft.net.messaging.expression.Equal;
import com.icesoft.net.messaging.expression.Identifier;
import com.icesoft.net.messaging.expression.StringLiteral;
import com.icesoft.net.messaging.http.HttpAdapter;
import com.icesoft.net.messaging.jms.JMSAdapter;
import com.icesoft.util.IdGenerator;
import com.icesoft.util.MonitorRunner;
import com.icesoft.util.SeamUtilities;
import com.icesoft.util.Properties;
import com.icesoft.util.ServerUtility;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MainServlet extends HttpServlet {
    private static final Log LOG = LogFactory.getLog(MainServlet.class);
    private static final CurrentContextPath currentContextPath = new CurrentContextPath();

    static {
        final String headless = "java.awt.headless";
        if (null == System.getProperty(headless)) {
            System.setProperty(headless, "true");
        }
    }

    private PathDispatcher dispatcher = new PathDispatcher();
    private ServletContext context;
    private MonitorRunner monitorRunner;
    private MessageServiceClient messageServiceClient;
    private String localAddress;
    private int localPort;
    private String blockingRequestHandlerContext;
    private boolean detectionDone = false;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        context = servletConfig.getServletContext();
        try {
            final Configuration configuration = new ServletContextConfiguration("com.icesoft.faces", context);
            final IdGenerator idGenerator = new IdGenerator(context.getResource("/WEB-INF/web.xml").getPath());
            final MimeTypeMatcher mimeTypeMatcher = new MimeTypeMatcher() {
                public String mimeTypeFor(String extension) {
                    return context.getMimeType(extension);
                }
            };
            final FileLocator localFileLocator = new FileLocator() {
                public File locate(String path) {
                    URI contextURI = URI.create(currentContextPath.lookup());
                    URI pathURI = URI.create(path);
                    String result = contextURI.relativize(pathURI).getPath();
                    String fileLocation = context.getRealPath(result);
                    return new File(fileLocation);
                }
            };
            monitorRunner = new MonitorRunner(configuration.getAttributeAsLong("monitorRunnerInterval", 10000));
            RenderManager.setServletConfig(servletConfig);
            PseudoServlet resourceServer = new BasicAdaptingServlet(new ResourceServer(configuration, mimeTypeMatcher, localFileLocator));
            PseudoServlet sessionDispatcher = new SessionDispatcher(configuration, context) {
                protected Server newServer(HttpSession session, Monitor sessionMonitor, Authorization authorization) {
                    return new MainSessionBoundServlet(session, sessionMonitor, idGenerator, mimeTypeMatcher, monitorRunner, configuration, getMessageServiceClient(configuration), blockingRequestHandlerContext, authorization);
                }
            };
            if (SeamUtilities.isSpringEnvironment()) {
                //Need to dispatch to the Spring resource server
                dispatcher.dispatchOn("/spring/resources/", resourceServer);
            }
            //don't create new session for resources belonging to expired user sessions
            dispatcher.dispatchOn(".*(block\\/resource\\/)", new SessionVerifier(sessionDispatcher, false));
            if (!configuration.getAttributeAsBoolean("synchronousUpdate", false)) {
                dispatcher.dispatchOn(
                    ".*(block\\/message)",
                    new PseudoServlet() {
                        private PseudoServlet notFound =
                            new EnvironmentAdaptingServlet(
                                new Server() {
                                    public void service(final Request request) throws Exception {
                                        request.respondWith(new NotFoundHandler(""));
                                    }

                                    public void shutdown() {
                                        // do nothing.
                                    }
                                },
                                configuration,
                                context);

                        public void service(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
                            if (messageServiceClient != null &&
                                messageServiceClient.getMessageServiceAdapter() instanceof HttpAdapter) {

                                ((HttpAdapter)messageServiceClient.getMessageServiceAdapter()).getHttpMessagingDispatcher().service(request, response);
                            } else {
                                notFound.service(request, response);
                            }
                        }

                        public void shutdown() {
                            // do nothing.
                        }
                    });
            }
            //don't create new session for XMLHTTPRequests identified by "block/*" prefixed paths
            dispatcher.dispatchOn(".*(block\\/)", new SessionVerifier(sessionDispatcher, true));
            dispatcher.dispatchOn(".*(\\/$|\\.iface$|\\.jsf|\\.faces$|\\.jsp$|\\.jspx$|\\.html$|\\.xhtml$|\\.seam$|uploadHtml$|/spring/)", sessionDispatcher);
            dispatcher.dispatchOn(".*", resourceServer);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        if (localAddress == null) {
            localAddress = ServerUtility.getLocalAddr(request, context);
            localPort = ServerUtility.getLocalPort(request, context);
        }
        try {
            currentContextPath.attach(request.getContextPath());
            dispatcher.service(request, response);
        } catch (SocketException e) {
            if ("Broken pipe".equals(e.getMessage())) {
                // client left the page
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Connection broken by client.", e);
                } else if (LOG.isDebugEnabled()) {
                    LOG.debug("Connection broken by client: " + e.getMessage());
                }
            } else {
                throw new ServletException(e);
            }
        } catch (RuntimeException e) {
            //ICE-4261: We cannot wrap RuntimeExceptions as ServletExceptions because of support for Jetty
            //Continuations.  However, if the message of a RuntimeException is null, Tomcat won't
            //properly redirect to the configured error-page.  So we need a new RuntimeException
            //that actually includes a message.
            if( e.getMessage() != null ){
                throw e;
            }
            throw new RuntimeException("no message available",e);
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            currentContextPath.detach();
        }
    }

    public void destroy() {
        monitorRunner.stop();
        DisposeBeans.in(context);
        dispatcher.shutdown();
        tearDownMessageServiceClient();
    }

    private synchronized MessageServiceClient getMessageServiceClient(
        final Configuration configuration) {

        if (!detectionDone) {
            if (!configuration.
                    getAttributeAsBoolean("synchronousUpdate", false)) {

                setUpMessageServiceClient(configuration);
            }
            detectionDone = true;
        }
        return messageServiceClient;
    }

    private boolean isJMSAvailable() {
        try {
            this.getClass().getClassLoader().
                loadClass("javax.jms.TopicConnectionFactory");
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }

    private void setUpMessageServiceClient(final Configuration configuration) {
        String blockingRequestHandler =
            configuration.getAttribute(
                "blockingRequestHandler", "auto-detect");
        if (blockingRequestHandler.equalsIgnoreCase("icefaces")) {
            // Adapt to Push environment.
            if (LOG.isInfoEnabled()) {
                LOG.info(
                    "Blocking Request Handler: " +
                        "\"" + blockingRequestHandler + "\"");
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("Adapting to Push environment.");
            }
        } else if (blockingRequestHandler.equalsIgnoreCase("push-server")) {
            // Adapt to Server Push environment.
            if (LOG.isInfoEnabled()) {
                LOG.info(
                    "Blocking Request Handler: " +
                        "\"" + blockingRequestHandler + "\"");
            }
            messageServiceClient =
                new MessageServiceClient(
                    new HttpAdapter(localAddress, localPort, context),
                    currentContextPath.lookup());
            testMessageService(configuration);
            if (messageServiceClient == null) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(
                        "Push Server not found - the Push Server must be " +
                            "deployed to support multiple asynchronous " +
                            "applications.");
                }
                if (LOG.isInfoEnabled()) {
                    LOG.info("Adapting to Push environment.");
                }
            }
        } else {
            if (blockingRequestHandler.equalsIgnoreCase("auto-detect")) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(
                        "Blocking Request Handler: " +
                            "\"" + blockingRequestHandler + "\"");
                }
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(
                        "INVALID Blocking Request Handler: " +
                            "\"" + blockingRequestHandler + "\" - " +
                                "Using \"auto-detect\"");
                }
            }
            // Auto-detect environment.
            boolean isJMSAvailable = isJMSAvailable();
            if (LOG.isDebugEnabled()) {
                LOG.debug("JMS API Available: " + isJMSAvailable);
            }
            if (isJMSAvailable) {
                messageServiceClient =
                    new MessageServiceClient(
                        new JMSAdapter(context),
                        currentContextPath.lookup());
                testMessageService(configuration);
            }
            if (messageServiceClient == null) {
                messageServiceClient =
                    new MessageServiceClient(
                        new HttpAdapter(localAddress, localPort, context),
                        currentContextPath.lookup());
                testMessageService(configuration);
            }
            if (messageServiceClient == null) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(
                        "Push Server not found - the Push Server must be " +
                            "deployed to support multiple asynchronous " +
                            "applications.");
                }
                if (LOG.isInfoEnabled()) {
                    LOG.info("Adapting to Push environment.");
                }
            }
        }
        if (messageServiceClient != null) {
            try {
                // todo: make message selector static to avoid instantiating
                //       the message handler
                messageServiceClient.
                    subscribe(
                        MessageServiceClient.PUSH_TOPIC_NAME,
                        new DisposeViewsHandler().getMessageSelector());
                messageServiceClient.start();
                ContextEventRepeater.
                    setMessageServiceClient(messageServiceClient);
            } catch (Exception exception) {
                // todo: log some message
                messageServiceClient = null;
                blockingRequestHandlerContext = null;
            }
        }
    }

    private void tearDownMessageServiceClient() {
        if (messageServiceClient != null) {
            try {
                messageServiceClient.stop();
            } catch (MessageServiceException exception) {
                LOG.error(
                    "Failed to close connection due to some internal error!",
                    exception);
            }
        }
    }

    private void testMessageService(final Configuration configuration) {
        String blockingRequestHandlerContext =
            configuration.getAttribute(
                "blockingRequestHandlerContext", "push-server");
        MessageHandler acknowledgeMessageHandler =
            new AbstractMessageHandler(
                new MessageSelector(
                    new Equal(
                        new Identifier(Message.MESSAGE_TYPE),
                        new StringLiteral("Presence")))) {

                public void handle(final Message message) {
                    if (message instanceof TextMessage) {
                        String text = ((TextMessage)message).getText();
                        int begin, end;
                        begin = 0;
                        end = text.indexOf(";");
                        if (end != -1 &&
                            text.substring(begin, end).
                                equals("Acknowledge")) {

                            String product =
                                text.substring(
                                    begin = end + 1,
                                    end = text.indexOf(";", begin));
                            String primary =
                                text.substring(
                                    begin = end + 1,
                                    end = text.indexOf(";", begin));
                            String secondary =
                                text.substring(
                                    begin = end + 1,
                                    end = text.indexOf(";", begin));
                            String tertiary =
                                text.substring(
                                    begin = end + 1,
                                    end = text.indexOf(";", begin));
                            String releaseType =
                                text.substring(
                                    begin = end + 1,
                                    end = text.indexOf(";", begin));
                            if (LOG.isInfoEnabled()) {
                                LOG.info(
                                    "Push Server detected: \"" +
                                        product + " " +
                                        primary + "." +
                                        secondary + "." +
                                        tertiary +
                                        (releaseType.equals("") ?
                                            "" :
                                            " " + releaseType) + "\"");
                            }
                            if (!primary.equals("x") &&
                                !ProductInfo.PRIMARY.equals("x")) {

                                if (!primary.equals(ProductInfo.PRIMARY) ||
                                    !secondary.equals(ProductInfo.SECONDARY)) {

                                    if (LOG.isWarnEnabled()) {
                                        LOG.warn(
                                            "ICEfaces / Push Server version " +
                                                "mismatch! - " +
                                                "Using \"" +
                                                    ProductInfo.PRODUCT + " " +
                                                    ProductInfo.PRIMARY + "." +
                                                    ProductInfo.SECONDARY + "." +
                                                    "x\" " +
                                                "with \"" +
                                                    product + " " +
                                                    primary + "." +
                                                    secondary + ".x\" " +
                                                "is not recommended.");
                                    }
                                }
                            }
                            messageServiceClient.removeMessageHandler(
                                this, MessageServiceClient.PUSH_TOPIC_NAME);
                            if (LOG.isInfoEnabled()) {
                                LOG.info(
                                    "Using Push Server " +
                                        "Blocking Request Handler");
                            }
                        }
                    }
                }
            };
        try {
            // throws MessageServiceException
            messageServiceClient.subscribe(
                MessageServiceClient.PUSH_TOPIC_NAME,
                acknowledgeMessageHandler.getMessageSelector());
            messageServiceClient.addMessageHandler(
                acknowledgeMessageHandler,
                MessageServiceClient.PUSH_TOPIC_NAME);
            // throws MessageServiceException
            messageServiceClient.start();
            Properties messageProperties = new Properties();
            messageProperties.setStringProperty(
                Message.DESTINATION_SERVLET_CONTEXT_PATH,
                blockingRequestHandlerContext);
            // throws MessageServiceException
            messageServiceClient.publishNow(
                "Hello",
                messageProperties,
                "Presence",
                MessageServiceClient.PUSH_TOPIC_NAME);
            this.blockingRequestHandlerContext =
                URI.create("/").resolve(blockingRequestHandlerContext + "/").
                    toString();
        } catch (MessageServiceException exception) {
            // todo: log some message
            messageServiceClient.removeMessageHandler(
                acknowledgeMessageHandler,
                MessageServiceClient.PUSH_TOPIC_NAME);
            try {
                // throws MessageServiceException
                messageServiceClient.unsubscribe(
                    MessageServiceClient.PUSH_TOPIC_NAME);
            } catch (MessageServiceException e) {
                // do nothing.
            }
            messageServiceClient = null;
        }
    }

    //todo: factor out into a ServletContextDispatcher
    private static class CurrentContextPath extends ThreadLocal {
        public String lookup() {
            return (String) get();
        }

        public void attach(String path) {
            set(path);
        }

        public void detach() {
            set(null);
        }
    }
}
