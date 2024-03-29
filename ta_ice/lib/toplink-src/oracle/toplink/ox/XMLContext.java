// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.xml.namespace.QName;
import oracle.toplink.exceptions.XMLMarshalException;
import oracle.toplink.internal.ox.XPathFragment;
import oracle.toplink.internal.ox.documentpreservation.DescriptorLevelDocumentPreservationPolicy;
import oracle.toplink.internal.ox.documentpreservation.NoDocumentPreservationPolicy;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.logging.SessionLog;
import oracle.toplink.ox.documentpreservation.DocumentPreservationPolicy;
import oracle.toplink.ox.platform.SAXPlatform;
import oracle.toplink.ox.platform.XMLPlatform;
import oracle.toplink.ox.schema.XMLSchemaReference;
import oracle.toplink.sessions.DatabaseSession;
import oracle.toplink.sessions.Project;
import oracle.toplink.sessions.SessionEventListener;
import oracle.toplink.tools.sessionconfiguration.XMLSessionConfigLoader;
import oracle.toplink.tools.sessionmanagement.SessionManager;

/**
 * <p>
 * An XMLContext is created based on TopLink sessions or projects and can then
 * used to create instances of XMLMarshaller, XMLUnmarshaller and XMLValidator.
 *
 * <p>
 * There are constructors to create an XMLContext with a single TopLink project
 * or with a String which is a single TopLink session name or a ':' separated
 * list of TopLink session names.
 *
 * <p>
 * <em>Code Sample</em><br>
 * <code>
 *  XMLContext context = new XMLContext("mySessionName");<br>
 *  XMLMarshaller marshaller = context.createMarshaller();<br>
 *  XMLUnmarshaller unmarshaller = context.createUnmarshaller();<br>
 *  XMLValidator validator = context.createValidator();<br>
 *  <code>
 *
 *  <p>The XMLContext is thread-safe.  If multiple threads accessing the same XMLContext object
 *  request an XMLMarshaller, each will receive their own instance of XMLMarshaller, so any
 *  state that the XMLMarshaller maintains will be unique to that process.  The same is true
 *  of instances of XMLUnmarshaller and XMLValidator.
 *
 *  @see oracle.toplink.ox.XMLMarshaller
 *  @see oracle.toplink.ox.XMLUnmarshaller
 *  @see oracle.toplink.ox.XMLValidator
 *
 */
public class XMLContext {
    private List sessions;
    private Map descriptorsByQName;
    private Map descriptorsByGlobalType;
    private boolean hasDocumentPreservation = false;

    /**
     * Create a new XMLContext based on the specified session name or list of
     * session names
     *
     * @param sessionNames
     *            A single session name or multiple session names separated by a :
     */
    public XMLContext(String sessionNames) {
        this(sessionNames, PrivilegedAccessHelper.privilegedGetClassLoaderForClass(XMLContext.class));
    }

    /**
     * Create a new XMLContext based on the specified session name or list of
     * session names
     *
     * @param sessionNames
     *            A single session name or multiple session names separated by a :
     * @param classLoader
     *            classloader for loading sessions.xml
     */
    public XMLContext(String sessionNames, ClassLoader classLoader) {
        this(sessionNames, classLoader, null);
    }

    /**
     * Create a new XMLContext based on passed in session names and session meta
     * XML.
     *
     * @param sessionNames
     *            A single session name or multiple session names separated by
     *            a:
     * @param xmlResource
     *            path to XML file containing session meta data to initialize
     *            and load sessions.
     */
    public XMLContext(String sessionNames, String xmlResource) {
        this(sessionNames, PrivilegedAccessHelper.privilegedGetClassLoaderForClass(XMLContext.class), xmlResource);
    }

    /**
     * Create a new XMLContext based on passed in session names, classloader and
     * session meta XML.
     *
     * @param sessionNames
     *            A single session name or multiple session names separated by a :
     * @param classLoader
     *            classloader for loading sessions.xml
     * @param xmlResource
     *            path to XML file containing session meta data to initialize
     *            and load sessions.
     */
    public XMLContext(String sessionNames, ClassLoader classLoader, String xmlResource) {
        XMLSessionConfigLoader loader = null;
        if (xmlResource != null) {
            loader = new XMLSessionConfigLoader(xmlResource);

        } else {
            loader = new XMLSessionConfigLoader();
        }
        descriptorsByQName = new HashMap();
        descriptorsByGlobalType = new HashMap();
        StringTokenizer st = new StringTokenizer(sessionNames, ":");
        sessions = new ArrayList(st.countTokens());
        int index = 0;
        while (st.hasMoreTokens()) {
            sessions.add(buildSession(st.nextToken(), classLoader, loader));
            index++;
        }
        for (int x = index - 1; x >= 0; x--) {
            storeXMLDescriptorsByQName((DatabaseSession)sessions.get(x));
        }
    }

    /**
     * Create a new XMLContext based on the specified project
     *
     * @param project
     *            A TopLink project
     */
    public XMLContext(Project project) {
        if ((project.getDatasourceLogin() == null) || !(project.getDatasourceLogin().getDatasourcePlatform() instanceof XMLPlatform)) {
            XMLPlatform platform = new SAXPlatform();
            project.setLogin(new XMLLogin(platform));
        }
        sessions = new ArrayList(1);
        DatabaseSession session = project.createDatabaseSession();

        // turn logging for this session off and leave the global session up
        // Note: setting level to SEVERE or WARNING will printout stacktraces for expected exceptions
        session.setLogLevel(SessionLog.OFF);
        // dont turn off global static logging
        //AbstractSessionLog.getLog().log(AbstractSessionLog.INFO, "ox_turn_global_logging_off", getClass());        			
        //AbstractSessionLog.getLog().setLevel(AbstractSessionLog.OFF);
        setupDocumentPreservationPolicy(session);
        session.login();
        sessions.add(session);
        descriptorsByQName = new HashMap();
        descriptorsByGlobalType = new HashMap();
        storeXMLDescriptorsByQName(session);
    }

    private DatabaseSession buildSession(String sessionName, ClassLoader classLoader, XMLSessionConfigLoader sessionLoader) throws XMLMarshalException {
        DatabaseSession dbSession;
        if (classLoader != null) {
            dbSession = (DatabaseSession)SessionManager.getManager().getSession(sessionLoader, sessionName, classLoader, false, true);
        } else {
            dbSession = (DatabaseSession)SessionManager.getManager().getSession(sessionLoader, sessionName, PrivilegedAccessHelper.privilegedGetClassLoaderForClass(this.getClass()), false, false, false);
        }
        if ((dbSession.getDatasourceLogin() == null) || !(dbSession.getDatasourceLogin().getDatasourcePlatform() instanceof XMLPlatform)) {
            XMLPlatform platform = new SAXPlatform();
            dbSession.setLogin(new XMLLogin(platform));
        }
        DatabaseSession session = dbSession.getProject().createDatabaseSession();
        if (dbSession.getEventManager().hasListeners()) {
            List listeners = dbSession.getEventManager().getListeners();
            int listenersSize = listeners.size();
            for (int x = 0; x < listenersSize; x++) {
                session.getEventManager().addListener((SessionEventListener)listeners.get(x));
            }
        }
        session.setExceptionHandler(dbSession.getExceptionHandler());
        session.setLogLevel(SessionLog.OFF);
        setupDocumentPreservationPolicy(session);
        session.login();
        return session;
    }

    /**
     * INTERNAL: Add and initialize a new session to the list of sessions
     * associated with this XMLContext.
     */
    public void addSession(DatabaseSession sessionToAdd) {
        if ((sessionToAdd.getDatasourceLogin() == null) || !(sessionToAdd.getDatasourceLogin().getDatasourcePlatform() instanceof XMLPlatform)) {
            XMLPlatform platform = new SAXPlatform();
            sessionToAdd.setLogin(new XMLLogin(platform));
        }
        DatabaseSession session = sessionToAdd.getProject().createDatabaseSession();
        if (sessionToAdd.getEventManager().hasListeners()) {
            List listeners = sessionToAdd.getEventManager().getListeners();
            int listenersSize = listeners.size();
            for (int x = 0; x < listenersSize; x++) {
                session.getEventManager().addListener((SessionEventListener)listeners.get(x));
            }
        }
        session.setExceptionHandler(sessionToAdd.getExceptionHandler());
        session.setLogLevel(SessionLog.OFF);
        this.setupDocumentPreservationPolicy(session);
        session.login();
        sessions.add(session);

        storeXMLDescriptorsByQName(session);

    }

    /**
     * Create a new XMLUnmarshaller
     *
     * @return An XMLUnmarshaller based on this XMLContext
     */
    public XMLUnmarshaller createUnmarshaller() {
        XMLUnmarshaller unmarshaller = new XMLUnmarshaller(this);
        return unmarshaller;
    }

    /**
     * Create a new XMLBinder
     * @return an XMLBinder based on this XMLContext
     */
    public XMLBinder createBinder() {
        return new XMLBinder(this);
    }

    /**
     * Create a new XMLMarshaller
     *
     * @return An XMLMarshaller based on this XMLContext
     */
    public XMLMarshaller createMarshaller() {
        XMLMarshaller marshaller = new XMLMarshaller(this);
        return marshaller;
    }

    /**
     * Create a new XMLValidator
     *
     * @return An XMLValidator based on this XMLContext
     */
    public XMLValidator createValidator() {
        XMLValidator validator = new XMLValidator(this);
        return validator;
    }

    /**
     * INTERNAL: Return the session corresponding to this object. Since the
     * object may be mapped by more that one of the projects used to create the
     * XML Context, this method will return the first match.
     *
     * The session will be a unit of work if document preservation is not
     * enabled.  This method will typically  be used for unmarshalling
     * when a non-shared cache is desired.
     */
    public AbstractSession getReadSession(Object object) {
        if (null == object) {
            return null;
        }
        int numberOfSessions = sessions.size();
        for (int x = 0; x < numberOfSessions; x++) {
            AbstractSession next = ((AbstractSession)sessions.get(x));
            XMLDescriptor xmlDescriptor = (XMLDescriptor)next.getDescriptor(object);
            if (xmlDescriptor != null) {
                // we don't currently support document preservation
                // and non-shared cache (via unit of work)
                //if (!documentPreservationPolicy.shouldPreserveDocument()) {
                next = next.acquireUnitOfWork();
                //}
                return next;
            }
        }
        throw XMLMarshalException.descriptorNotFoundInProject(object.getClass().getName());
    }

    /**
     * INTERNAL:
     * Return the session corresponding to this class. Since the class
     * may be mapped by more that one of the projects used to create the XML
     * Context, this method will return the first match.
     *
     * The session will be a unit of work if document preservation is not
     * enabled.  This method will typically  be used for unmarshalling
     * when a non-shared cache is desired.
     */
    public AbstractSession getReadSession(Class clazz) {
        if (null == clazz) {
            return null;
        }
        int numberOfSessions = sessions.size();
        for (int x = 0; x < numberOfSessions; x++) {
            AbstractSession next = ((AbstractSession)sessions.get(x));
            XMLDescriptor xmlDescriptor = (XMLDescriptor)next.getDescriptor(clazz);
            if (xmlDescriptor != null) {
                // we don't currently support document preservation
                // and non-shared cache (via unit of work)
                //if (!documentPreservationPolicy.shouldPreserveDocument()) {
                next = next.acquireUnitOfWork();
                //}
                return next;
            }
        }
        throw XMLMarshalException.descriptorNotFoundInProject(clazz.getName());
    }

    /**
     * INTERNAL:
     * Return the session corresponding to this XMLDescriptor. Since
     * the class may be mapped by more that one of the projects used to create
     * the XML Context, this method will return the first match.
     *
     * The session will be a unit of work if document preservation is not
     * enabled.  This method will typically  be used for unmarshalling
     * when a non-shared cache is desired.
     */
    public AbstractSession getReadSession(XMLDescriptor xmlDescriptor) {
        if (null == xmlDescriptor) {
            return null;
        }
        int numberOfSessions = sessions.size();
        for (int x = 0; x < numberOfSessions; x++) {
            AbstractSession next = ((AbstractSession)sessions.get(x));
            if (next.getProject().getOrderedDescriptors().contains(xmlDescriptor)) {
                // we don't currently support document preservation
                // and non-shared cache (via unit of work)
                //if (!documentPreservationPolicy.shouldPreserveDocument()) {
                next = next.acquireUnitOfWork();
                //}
                return next;
            }
        }
        throw XMLMarshalException.descriptorNotFoundInProject(xmlDescriptor.getJavaClass().getName());
    }

    /**
     * INTERNAL: Return the TopLink session used to marshall.
     */
    public List getSessions() {
        return sessions;
    }

    /**
     * INTERNAL: <code>
     * XMLContext xmlContext = new XMLContext("path0:path1");<br>
     * DatabaseSession session = xmlContext.getSession(0);  // returns session for path0<br>
     * </code>
     */
    public DatabaseSession getSession(int index) {
        if (null == sessions) {
            return null;
        }
        return (DatabaseSession)sessions.get(index);
    }

    /**
     * INTERNAL: Return the session corresponding to this object. Since the
     * object may be mapped by more that one of the projects used to create the
     * XML Context, this method will return the first match.
     */
    public AbstractSession getSession(Object object) {
        if (null == object) {
            return null;
        }
        int numberOfSessions = sessions.size();
        for (int x = 0; x < numberOfSessions; x++) {
            AbstractSession next = ((AbstractSession)sessions.get(x));
            if (next.getDescriptor(object) != null) {
                return next;
            }
        }
        throw XMLMarshalException.descriptorNotFoundInProject(object.getClass().getName());
    }

    /**
     * INTERNAL: Return the session corresponding to this class. Since the class
     * may be mapped by more that one of the projects used to create the XML
     * Context, this method will return the first match.
     */
    public AbstractSession getSession(Class clazz) {
        if (null == clazz) {
            return null;
        }
        int numberOfSessions = sessions.size();
        for (int x = 0; x < numberOfSessions; x++) {
            AbstractSession next = ((AbstractSession)sessions.get(x));
            if (next.getDescriptor(clazz) != null) {
                return next;
            }
        }
        throw XMLMarshalException.descriptorNotFoundInProject(clazz.getName());
    }

    /**
     * INTERNAL: Return the session corresponding to this XMLDescriptor. Since
     * the class may be mapped by more that one of the projects used to create
     * the XML Context, this method will return the first match.
     */
    public AbstractSession getSession(XMLDescriptor xmlDescriptor) {
        if (null == xmlDescriptor) {
            return null;
        }
        int numberOfSessions = sessions.size();
        for (int x = 0; x < numberOfSessions; x++) {
            AbstractSession next = ((AbstractSession)sessions.get(x));
            if (next.getProject().getOrderedDescriptors().contains(xmlDescriptor)) {
                return next;
            }
        }
        throw XMLMarshalException.descriptorNotFoundInProject(xmlDescriptor.getJavaClass().getName());
    }

    private void storeXMLDescriptorsByQName(DatabaseSession session) {
        Iterator iterator = session.getProject().getOrderedDescriptors().iterator();
        while (iterator.hasNext()) {
            XMLDescriptor xmlDescriptor = (XMLDescriptor)iterator.next();
            storeXMLDescriptorByQName(xmlDescriptor);
        }
    }

    /**
     * INTERNAL:
     */
    public void storeXMLDescriptorByQName(XMLDescriptor xmlDescriptor) {
        QName descriptorQName;
        String defaultRootName;

        Vector tableNames = xmlDescriptor.getTableNames();
        for (int i = 0; i < tableNames.size(); i++) {
            defaultRootName = (String)tableNames.get(i);

            if (null != defaultRootName) {
                int index = defaultRootName.indexOf(':');
                String defaultRootLocalName = defaultRootName.substring(index + 1);
                if (index > -1) {
                    String defaultRootPrefix = defaultRootName.substring(0, index);
                    String defaultRootNamespaceURI = xmlDescriptor.getNamespaceResolver().resolveNamespacePrefix(defaultRootPrefix);
                    descriptorQName = new QName(defaultRootNamespaceURI, defaultRootLocalName);
                } else {
                    if(xmlDescriptor.getNamespaceResolver() != null) {
                        descriptorQName = new QName(xmlDescriptor.getNamespaceResolver().getDefaultNamespaceURI(), defaultRootLocalName);
                    } else {
                        descriptorQName = new QName(defaultRootLocalName);
                    }
                }
                if (!xmlDescriptor.hasInheritance() || xmlDescriptor.getInheritancePolicy().isRootParentDescriptor()) {
                    descriptorsByQName.put(descriptorQName, xmlDescriptor);
                } else {
                    //this means we have a descriptor that is a child in an inheritance hierarchy
                    XMLDescriptor existingDescriptor = (XMLDescriptor)descriptorsByQName.get(descriptorQName);
                    if (existingDescriptor == null) {
                        descriptorsByQName.put(descriptorQName, xmlDescriptor);
                    }
                }
            }
        }

        XMLSchemaReference xmlSchemaReference = xmlDescriptor.getSchemaReference();
        if (null != xmlSchemaReference) {
            String schemaContext = xmlSchemaReference.getSchemaContext();
            if ((xmlSchemaReference.getType() == XMLSchemaReference.COMPLEX_TYPE) || (xmlSchemaReference.getType() == XMLSchemaReference.SIMPLE_TYPE)) {
                if ((null != schemaContext) && (schemaContext.lastIndexOf('/') == 0)) {
                    schemaContext = schemaContext.substring(1, schemaContext.length());
                    XPathFragment typeFragment = new XPathFragment(schemaContext);
                    if (null != xmlDescriptor.getNamespaceResolver()) {
                        typeFragment.setNamespaceURI(xmlDescriptor.getNamespaceResolver().resolveNamespacePrefix(typeFragment.getPrefix()));
                    }
                    this.descriptorsByGlobalType.put(typeFragment, xmlDescriptor);
                }
            }
        }
    }

    /**
     * INTERNAL: Return the XMLDescriptor with the default root mapping matchin
     * the QName paramater.
     */
    public XMLDescriptor getDescriptor(QName qName) {
        return (XMLDescriptor)descriptorsByQName.get(qName);
    }

    public void addDescriptorByQName(QName qName, XMLDescriptor descriptor) {
    	descriptorsByQName.put(qName, descriptor);
    }
    /**
     * INTERNAL: Return the XMLDescriptor mapped to the global type matching the
     * XPathFragment parameter.
     */
    public XMLDescriptor getDescriptorByGlobalType(XPathFragment xPathFragment) {
        return (XMLDescriptor)this.descriptorsByGlobalType.get(xPathFragment);
    }

    /**
     * INTERNAL:
     * Return the DocumentPreservationPolicy associated with this session
     * @param session
     * @return
     */
    public DocumentPreservationPolicy getDocumentPreservationPolicy(AbstractSession session) {
        XMLLogin login = (XMLLogin)session.getDatasourceLogin();
        return login.getDocumentPreservationPolicy();
    }

    public void setupDocumentPreservationPolicy(DatabaseSession session) {
        XMLLogin login = (XMLLogin)session.getDatasourceLogin();
        if (login.getDocumentPreservationPolicy() == null) {
            Iterator iterator = session.getProject().getOrderedDescriptors().iterator();
            while (iterator.hasNext()) {
                XMLDescriptor xmlDescriptor = (XMLDescriptor)iterator.next();
                if (xmlDescriptor.shouldPreserveDocument()) {
                    login.setDocumentPreservationPolicy(new DescriptorLevelDocumentPreservationPolicy(this));
                    break;
                }
            }
        }
        if (login.getDocumentPreservationPolicy() == null) {
            login.setDocumentPreservationPolicy(new NoDocumentPreservationPolicy());
        }

        if (login.getDocumentPreservationPolicy().shouldPreserveDocument() && !hasDocumentPreservation) {
            hasDocumentPreservation = true;
        }
    }

    /**
     * INTERNAL:
     * Return true if any session held onto by this context has a document preservation
     * policy that requires unmarshalling from a Node.
     */
    public boolean hasDocumentPreservation() {
        return this.hasDocumentPreservation;
    }
}