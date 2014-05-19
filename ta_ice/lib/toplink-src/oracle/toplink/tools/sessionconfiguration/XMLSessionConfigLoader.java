// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration;

import java.util.Vector;
import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import org.xml.sax.*;
import org.w3c.dom.Document;

import oracle.toplink.ox.XMLDescriptor;
import oracle.toplink.ox.XMLContext;
import oracle.toplink.ox.XMLUnmarshaller;
import oracle.toplink.platform.xml.XMLParser;
import oracle.toplink.platform.xml.XMLPlatform;
import oracle.toplink.platform.xml.XMLPlatformFactory;
import oracle.toplink.exceptions.ValidationException;
import oracle.toplink.exceptions.SessionLoaderException;
import oracle.toplink.internal.helper.ConversionManager;
import oracle.toplink.tools.sessionmanagement.SessionManager;
import oracle.toplink.internal.localization.ExceptionLocalization;
import oracle.toplink.sessions.Project;
import oracle.toplink.sessions.Session;
import oracle.toplink.tools.sessionconfiguration.model.TopLinkSessions;

/**
 * Provide a mechanism for loading Session configuration XML files.
 * This is used by the SessionManager to define how to find and load a Session from a sessions XML file.
 * The sessions XML file is typically deployed in the applications jar (ejb-jar) and named sessions.xml in the /META-INF directory.
 * Several loading options are provided,
 * <ul>
 * <li> resourceName : The reasource path and file name to the sessions XML file,
 * default is /sessions.xml or /META-INF/sessions.xml. (enusre "/" is used, not "\").
 * A file path can also be provided, although a resource is typically used.
 * <li> shouldLogin : Define if the loaded session should be connected, default true.
 * <li> shouldRefresh : Define if the loaded session should be refreshed from the file,
 * (this old session will be disconnected) default false.
 * <li> classLoader : Define the class loader that should be used to find the resource.
 * This loader will also be used as the loaded session's class loader.  This should be the application's class loader. 
 * Default is the ConversionManager loader, which is thread-based.
 * <li> shouldCheckClassLoader : Defines if the session will be refreshed from the file if the class loader requesting the load,
 * is different than the loaded session's class loader.  This can be used to handle re-deployment.
 * </ul>
 *
 * @since TopLink 10.1.3
 * @author Guy Pelletier
 */
public class XMLSessionConfigLoader {
    protected String resourceName;
    
    /** Stores the resource path to provide a better error message if the load fails. */
    protected String resourcePath = DEFAULT_RESOURCE_NAME;
    /** Stores the name of the Session in the sessions XML file desired to be loaded. */
    protected String sessionName = "default";
    /** Define if the loaded session should be connected, default true. */
    protected boolean shouldLogin = true;
    /** Define if the loaded session should be refreshed from the file. */
    protected boolean shouldRefresh = false;
    /** Define the class loader that should be used to find the resource. */
    protected ClassLoader classLoader;
    /** Defines if the session will be refreshed from the file if the class loader requesting the load is different than the loaded session's class loader. */
    protected boolean shouldCheckClassLoader = false;
    /** Stores any exceptions that occured to provide all the exceptions up front if the load fails. */
    protected Vector exceptionStore;
    /** Used to store the entity resolver to validate the XML schema when parsing. */
    protected TopLinkEntityResolver entityResolver;    
    
    protected static final String DEFAULT_RESOURCE_NAME = "sessions.xml";
    protected static final String DEFAULT_RESOURCE_NAME_IN_META_INF = "META-INF/sessions.xml";    

    /** Cache the creation and initialization of the Session XML mapping project. */
    protected static Project project = new XMLSessionConfigProject_11_1_1();

    /** Cache the creation and initialization of the Session XML mapping project. */
    protected static Project getProject() {
        return project;
    }
    
    /**
     * PUBLIC:
     * This constructor is used when the file resource named 'sessions.xml' will
     * be parsed for configuration.
     */
    public XMLSessionConfigLoader() {
        this(DEFAULT_RESOURCE_NAME);
    }

    /**
     * PUBLIC:
     * This constructor is used when passing in the resource name of the
     * configuration file that should be parsed
     */
    public XMLSessionConfigLoader(String resourceName) {
        this.resourceName = resourceName;
        this.exceptionStore = new Vector();
        this.entityResolver = new TopLinkEntityResolver();
        this.classLoader = ConversionManager.getDefaultManager().getLoader();
    }

    /**
     * INTERNAL:
     * Will return the the resource name with full path of the resource file.
     */
    public String getResourcePath() {
        return this.resourcePath;
    }

    /**
     * INTERNAL:
     */
    public Vector getExceptionStore() {
        return this.exceptionStore;
    }

    /**
     * PUBLIC:
     * Returns the resource name we are trying to load.
     */
    public String getResourceName() {
        return this.resourceName;
    }

    /**
     * PUBLIC:
     * Set the resource name we are trying to load.
     */
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    
    /**
     * PUBLIC:
     * Returns the name of the Session in the sessions XML file desired to be loaded.
     */
    public String getSessionName() {
        return this.sessionName;
    }

    /**
     * PUBLIC:
     * Set the name of the Session in the sessions XML file desired to be loaded.
     */
    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }
    
    /**
     * PUBLIC:
     * Return if the loaded session should be connected.
     */
    public boolean shouldLogin() {
        return shouldLogin;
    }
    
    /**
     * PUBLIC:
     * Set if the loaded session should be connected.
     */
    public void setShouldLogin(boolean shouldLogin) {
        this.shouldLogin = shouldLogin;
    }
    
    /**
     * PUBLIC:
     * Return if the loaded session should be refreshed from the file.
     */
    public boolean shouldRefresh() {
        return shouldRefresh;
    }
    
    /**
     * PUBLIC:
     * Set if the loaded session should be refreshed from the file.
     */
    public void setShouldRefresh(boolean shouldRefresh) {
        this.shouldRefresh = shouldRefresh;
    }
    
    /**
     * PUBLIC:
     * Return if the session will be refreshed from the file if the class loader requesting the load is different than the loaded session's class loader.
     */
    public boolean shouldCheckClassLoader() {
        return shouldCheckClassLoader;
    }
    
    /**
     * PUBLIC:
     * Set if the session will be refreshed from the file if the class loader requesting the load is different than the loaded session's class loader.
     */
    public void setShouldCheckClassLoader(boolean shouldCheckClassLoader) {
        this.shouldCheckClassLoader = shouldCheckClassLoader;
    }
    
    /**
     * PUBLIC:
     * Return the class loader that should be used to find the resource.
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }
    
    /**
     * PUBLIC:
     * Set the class loader that should be used to find the resource.
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    
    /**
     * INTERNAL:
     * This method instantiates the parser and builds the document based on the
     * schema. If the document is loaded without errors, then the configs are
     * converted to sessions and stored on the session manager and true is
     * returned to indicate success.
     */
    public boolean load(SessionManager sessionManager, ClassLoader loader) {
        Document document = loadDocument(loader);

        if (document.getDocumentElement().getTagName().equals("toplink-sessions")) {
            String version = document.getDocumentElement().getAttribute("version");
            Project project = null;
            if ((version != null) && (version.indexOf("10.1.3") != -1)) {
                project = new XMLSessionConfigProject();
            } else if (getExceptionStore().isEmpty()) {
                project = getProject();
            }
            if (project != null) {
                // Either no errors occured or it's 10.1.3:
                // unmarshal the document which will return a 
                // TopLinkSessions containing 0 or more SessionConfigs and send
                // them through the factory to create actual Sessions
                XMLContext context = new XMLContext(project);
                XMLUnmarshaller unmarshaller = context.createUnmarshaller();
                TopLinkSessions configs = (TopLinkSessions)unmarshaller.unmarshal(document);
                TopLinkSessionsFactory factory = new TopLinkSessionsFactory();
                Map sessions = factory.buildTopLinkSessions(configs, loader);
                for (Iterator iterator = sessions.entrySet().iterator(); iterator.hasNext(); ) {
                    Map.Entry entry = (Map.Entry)iterator.next();
                    // Only add the session if missing.
                    if (!sessionManager.getSessions().containsKey(entry.getKey())) {
                        sessionManager.addSession((String)entry.getKey(), (Session)entry.getValue());
                    }
                }
                return true;
            } else {
                // Throw the exceptions we encountered
                throw SessionLoaderException.finalException(getExceptionStore());
            }
        } else {
            // 9.0.4 session.xml, return false to indicate we should load with the XMLLoader
            return false;
        }
    }

    /**
     * INTERNAL:
     * This method is to be used to load config objects for the Mapping Workbench
     * only. Do not call this method.
     */
    public TopLinkSessions loadConfigsForMappingWorkbench(ClassLoader loader) {
        return loadConfigsForMappingWorkbench(loader, true);
    }

    /**
     * INTERNAL:
     * This method is to be used to load config objects for the Mapping Workbench
     * only. Do not call this method.
     */
    public TopLinkSessions loadConfigsForMappingWorkbench(ClassLoader loader, boolean validate) {
        Document document = loadDocument(loader, validate);

        if (document.getDocumentElement().getTagName().equals("toplink-sessions")) {
            if (getExceptionStore().isEmpty()) {
                // No errors occured, unmarshal the document which will return a 
                // TopLinkSessions containing 0 or more SessionConfigs
                XMLContext context = new XMLContext(getProject());
                XMLUnmarshaller unmarshaller = context.createUnmarshaller();
                return (TopLinkSessions)unmarshaller.unmarshal(document);
            } else {
                // Throw the exceptions we encountered
                throw SessionLoaderException.finalException(getExceptionStore());
            }
        } else {
            // 9.0.4 session.xml, send it through the DTD2SessionConfigLoader
            return new DTD2SessionConfigLoader().load(document);
        }
    }

    /**
     * INTERNAL:
     * Load a session.xml document. The error handler will capture all the
     * errors and allow for a document to be returned.
     */
    protected Document loadDocument(ClassLoader loader) {
        return loadDocument(loader, SessionManager.shouldUseSchemaValidation());
    }

    /**
     * INTERNAL:
     * Load a session.xml document. The error handler will capture all the
     * errors and allow for a document to be returned.
     */
    protected Document loadDocument(ClassLoader loader, boolean validate) {
        URL inURL = loader.getResource(this.resourceName);
        // Also allow a file reference.
        File inFile = new File(this.resourceName);

        if (inURL == null) {
            // If loading the default resource name and it is not found,
            // look in the META-INF directory.
            if (this.resourceName.equals(DEFAULT_RESOURCE_NAME)) {
                inURL = loader.getResource(DEFAULT_RESOURCE_NAME_IN_META_INF);
            }
            
            if ((inURL == null) && (!inFile.exists())) {
                throw ValidationException.noSessionsXMLFound(this.resourceName);    
            }
        }
        
        if (inURL == null) {
            this.resourcePath = inFile.getAbsolutePath();
        } else {
            // Stored the loaded resource path, used in exception string if the
            // resource is not found or if there are errors in the resource.
            this.resourcePath = inURL.getPath();
        }

        XMLPlatform xmlPlatform = XMLPlatformFactory.getInstance().getXMLPlatform();
        XMLDescriptor projectDescriptor = (XMLDescriptor)getProject().getDescriptors().get(TopLinkSessions.class);

        XMLParser parser = xmlPlatform.newXMLParser();

        if (validate) {
            parser.setValidationMode(XMLParser.SCHEMA_VALIDATION);
        } else {
            parser.setValidationMode(XMLParser.NONVALIDATING);
        }

        parser.setWhitespacePreserving(false);
        parser.setXMLSchema(projectDescriptor.getSchemaReference().getURL());
        parser.setEntityResolver(this.entityResolver);
        parser.setErrorHandler(new XMLSessionConfigLoaderErrorHandler());

        if (inURL == null) {
            return parser.parse(inFile);
        } else {
            return parser.parse(inURL);
        }
    }

    /**
     * INTERNAL:
     * <p><b>Purpose</b>: Provide a mechanism to swallow all parsing errors
     *
     * @see ErrorHandler
     * @since TopLink 10.1.3
     * @author Guy Pelletier
     */
    public class XMLSessionConfigLoaderErrorHandler implements ErrorHandler {
        public void warning(SAXParseException e) throws SAXException {
            getExceptionStore().add(SessionLoaderException.failedToParseXML(ExceptionLocalization.buildMessage("parsing_warning"), e.getLineNumber(), e.getColumnNumber(), e));
        }

        public void error(SAXParseException e) throws SAXException {
            getExceptionStore().add(e);
        }

        public void fatalError(SAXParseException e) throws SAXException {
            getExceptionStore().add(e);
        }
    }
}