// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;
import java.lang.reflect.Constructor;
import oracle.toplink.Version;
import oracle.toplink.eis.*;
import oracle.toplink.eis.adapters.xmlfile.XMLFileSequence;
import oracle.toplink.internal.helper.ConversionManager;
import oracle.toplink.ox.XMLLogin;
import oracle.toplink.remote.*;
import oracle.toplink.logging.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.remotecommand.*;
import oracle.toplink.remote.jms.JMSClusteringService;
import oracle.toplink.remote.rmi.RMIClusteringService;
import oracle.toplink.remote.rmi.wls.WLSClusteringService;
import oracle.toplink.remotecommand.rmi.RMITransportManager;
import oracle.toplink.remotecommand.jms.JMSTopicTransportManager;
import oracle.toplink.remotecommand.corba.sun.SunCORBATransportManager;
import oracle.toplink.threetier.*;
import oracle.toplink.internal.databaseaccess.DatabasePlatform;
import oracle.toplink.internal.databaseaccess.Platform;
import oracle.toplink.platform.database.converters.StructConverter;
import oracle.toplink.platform.server.ServerPlatform;
import oracle.toplink.platform.server.NoServerPlatform;
import oracle.toplink.sessions.Login;
import oracle.toplink.sessions.Project;
import oracle.toplink.sessions.DatabaseLogin;
import oracle.toplink.sessions.DatasourceLogin;
import oracle.toplink.sessions.SessionEventListener;
import oracle.toplink.sequencing.Sequence;
import oracle.toplink.sequencing.DefaultSequence;
import oracle.toplink.sequencing.NativeSequence;
import oracle.toplink.sequencing.TableSequence;
import oracle.toplink.sequencing.UnaryTableSequence;
import oracle.toplink.jndi.JNDIConnector;
import oracle.toplink.sessionbroker.SessionBroker;
import oracle.toplink.tools.workbench.XMLProjectReader;
import oracle.toplink.tools.profiler.*;
import oracle.toplink.internal.databaseaccess.DatasourcePlatform;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedGetConstructorFor;
import oracle.toplink.internal.security.PrivilegedInvokeConstructor;
import oracle.toplink.internal.security.PrivilegedNewInstanceFromClass;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.internal.sessions.DatabaseSessionImpl;
import oracle.toplink.tools.sessionconfiguration.model.*;
import oracle.toplink.tools.sessionconfiguration.model.log.*;
import oracle.toplink.tools.sessionconfiguration.model.csm.*;
import oracle.toplink.tools.sessionconfiguration.model.pool.*;
import oracle.toplink.tools.sessionconfiguration.model.rcm.*;
import oracle.toplink.tools.sessionconfiguration.model.rcm.command.*;
import oracle.toplink.tools.sessionconfiguration.model.login.*;
import oracle.toplink.tools.sessionconfiguration.model.event.*;
import oracle.toplink.tools.sessionconfiguration.model.project.*;
import oracle.toplink.tools.sessionconfiguration.model.sequencing.*;
import oracle.toplink.tools.sessionconfiguration.model.session.*;
import oracle.toplink.tools.sessionconfiguration.model.platform.*;
import oracle.toplink.tools.sessionconfiguration.model.property.*;
import oracle.toplink.tools.sessionconfiguration.model.clustering.*;
import oracle.toplink.tools.sessionconfiguration.model.transport.*;
import oracle.toplink.tools.sessionconfiguration.model.transport.naming.*;
import oracle.toplink.tools.sessionconfiguration.model.transport.discovery.*;

/**
 * INTERNAL:
 * Builds TopLink Sessions from the XML Session Config model.
 * Model classes that are not built, are processed only.
 *
 * @author Guy Pelletier
 * @version 1.0
 * @date November 18, 2003
 */
public class TopLinkSessionsFactory {
    private Map m_sessions;
    private Map m_logLevels;
    private ClassLoader m_classLoader;

    /**
     * INTERNAL:
     */
    protected TopLinkSessionsFactory() {
        m_logLevels = new HashMap();
        m_logLevels.put("off", new Integer(SessionLog.OFF));
        m_logLevels.put("severe", new Integer(SessionLog.SEVERE));
        m_logLevels.put("warning", new Integer(SessionLog.WARNING));
        m_logLevels.put("info", new Integer(SessionLog.INFO));
        m_logLevels.put("config", new Integer(SessionLog.CONFIG));
        m_logLevels.put("fine", new Integer(SessionLog.FINE));
        m_logLevels.put("finer", new Integer(SessionLog.FINER));
        m_logLevels.put("finest", new Integer(SessionLog.FINEST));
        m_logLevels.put("all", new Integer(SessionLog.FINEST));
    }

    /**
     * INTERNAL:
     * To build TopLink sessions, users must call this method with a
     * TopLinkSessions object returned from an OX read in the
     * XMLSessionsConfigLoader
     */
    public Map buildTopLinkSessions(TopLinkSessions topLinkSessions, ClassLoader classLoader) {
        m_sessions = new HashMap();
        m_classLoader = classLoader;
        Vector sessionBrokerConfigs = new Vector();
        Enumeration e = topLinkSessions.getSessionConfigs().elements();

        while (e.hasMoreElements()) {
            SessionConfig sessionConfig = (SessionConfig)e.nextElement();

            if (sessionConfig instanceof SessionBrokerConfig) {
                // Hold all the session brokers till all the sessions have been built
                sessionBrokerConfigs.add(sessionConfig);
            } else {
                AbstractSession session = buildSession(sessionConfig);
                session.getDatasourcePlatform().getConversionManager().setLoader(classLoader);
                processSessionCustomizer(sessionConfig, session);
                m_sessions.put(session.getName(), session);
            }
        }

        // All the sessions have been built now so we can process the Session Brokers
        Enumeration ee = sessionBrokerConfigs.elements();

        while (ee.hasMoreElements()) {
            SessionBrokerConfig sessionBrokerConfig = (SessionBrokerConfig)ee.nextElement();
            SessionBroker sessionBroker = buildSessionBrokerConfig(sessionBrokerConfig);
            sessionBroker.getDatasourcePlatform().getConversionManager().setLoader(classLoader);
            processSessionCustomizer(sessionBrokerConfig, sessionBroker);
            m_sessions.put(sessionBroker.getName(), sessionBroker);
        }

        return m_sessions;
    }

    /**
     * INTERNAL:
     * Process the user inputed session customizer class. Will be run at the
     * end of the session build process
     */
    private void processSessionCustomizer(SessionConfig sessionConfig, AbstractSession session) {
        // Session customizer - MUST BE THE LAST THING TO PROCESS
        String sessionCustomizerClassName = sessionConfig.getSessionCustomizerClass();
        if (sessionCustomizerClassName != null) {
            try {
                Class sessionCustomizerClass = m_classLoader.loadClass(sessionCustomizerClassName);
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    ((SessionCustomizer)AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(sessionCustomizerClass))).customize(session);
                }else{
                    ((SessionCustomizer)PrivilegedAccessHelper.newInstanceFromClass(sessionCustomizerClass)).customize(session);
                }
            } catch (Throwable exception) {
                throw SessionLoaderException.failedToLoadTag("session-customizer-class", sessionCustomizerClassName, exception);
            }
        }
    }

    /**
     * INTERNAL:
     * Build the correct session based on the session config type
     */
    private AbstractSession buildSession(SessionConfig sessionConfig) {
        if (sessionConfig instanceof ServerSessionConfig) {
            return buildServerSessionConfig((ServerSessionConfig)sessionConfig);
        } else {// if (sessionConfig instanceof DatabaseSessionConfig) {
            return buildDatabaseSessionConfig((DatabaseSessionConfig)sessionConfig);
        }
    }

    /**
     * INTERNAL:
     * Wrapped by the getSession() call, therefore, config can't be null at this
     * point.
     */
    private AbstractSession buildDatabaseSessionConfig(DatabaseSessionConfig databaseSessionConfig) {
        // Create a new database session (null means use login from deployment xml if there is one)
        DatabaseSessionImpl databaseSession = createSession(databaseSessionConfig, null);

        // Login - may overwrite the previous login (expected behavior)
        Login login = buildLogin(databaseSessionConfig.getLoginConfig());
        if (login != null) {
            databaseSession.setLogin(login);
        }

        // Common processing since ServerSessions inherit from DatabaseSession
        processDatabaseSessionConfig(databaseSessionConfig, databaseSession);

        // Process the common elements in SessionConfig
        processSessionConfig(databaseSessionConfig, databaseSession);

        return databaseSession;
    }

    /**
     * INTERNAL
     * Process a DatabaseSessionConfig object.
     */
    private void processDatabaseSessionConfig(DatabaseSessionConfig sessionConfig, AbstractSession session) {
    }

    /**
     * INTERNAL:
     * Builds a server server from the given ServerSessionConfig.
     */
    private AbstractSession buildServerSessionConfig(ServerSessionConfig serverSessionConfig) {
        // For server sessions we should build the login first, that way we can 
        // initialize the server session with the login (if there is one)
        Login login = buildLogin(serverSessionConfig.getLoginConfig());

        // Create a new server session
        ServerSession serverSession = (ServerSession)createSession(serverSessionConfig, login);

        // Common processing since ServerSessions inherit from DatabaseSession
        processDatabaseSessionConfig(serverSessionConfig, serverSession);

        // Process pools config
        processPoolsConfig(serverSessionConfig.getPoolsConfig(), serverSession);

        // Process connection policy config
        processConnectionPolicyConfig(serverSessionConfig.getConnectionPolicyConfig(), serverSession);

        // Process the common elements in SessionConfig
        processSessionConfig(serverSessionConfig, serverSession);

        return serverSession;
    }

    /**
     * INTERNAL:
     * Return a DatabaseSession object from it's config object using either
     * the project classes or project XML files.
     */
    private DatabaseSessionImpl createSession(DatabaseSessionConfig sessionConfig, Login login) {
        Project primaryProject;

        if (sessionConfig.getPrimaryProject() != null) {
            primaryProject = loadProjectConfig(sessionConfig.getPrimaryProject());
        } else {
            primaryProject = new Project();// Build a session from an empty project
        }

        prepareProjectLogin(primaryProject, login);
        DatabaseSessionImpl sessionToReturn = getSession(sessionConfig, primaryProject);

        // Append descriptors from all subsequent project.xml and project classes 
        // to the mainProject  
        if (sessionConfig.getAdditionalProjects() != null) {
            Enumeration additionalProjects = sessionConfig.getAdditionalProjects().elements();

            while (additionalProjects.hasMoreElements()) {
                Project subProject = loadProjectConfig((ProjectConfig)additionalProjects.nextElement());
                primaryProject.addDescriptors(subProject, sessionToReturn);
            }
        }

        return sessionToReturn;
    }

    /**
     * INTERNAL:
     * Return the correct session type from the sessionConfig
     */
    private void prepareProjectLogin(Project project, Login login) {
        if (login != null) {
            project.setLogin(login);
        } else if (project.getDatasourceLogin() == null) {
            // dummy login that needs to be set, otherwise session creation will fail
            project.setLogin(new DatabaseLogin());
        } else {
            // we read a login from the deployment xml of java, don't overwrite
        }
    }

    /**
     * INTERNAL:
     * Return the correct session type from the sessionConfig
     */
    private DatabaseSessionImpl getSession(SessionConfig sessionConfig, Project project) {
        if (sessionConfig instanceof ServerSessionConfig) {
            return (ServerSession)project.createServerSession();
        } else {
            return (DatabaseSessionImpl)project.createDatabaseSession();
        }
    }

    /**
     * INTERNAL:
     * Load a projectConfig from the session.xml file. This method will determine
     * the proper loading scheme, that is, for a class or xml project.
     */
    private Project loadProjectConfig(ProjectConfig projectConfig) {
        Project project = null;
        String projectString = projectConfig.getProjectString();

        if (projectConfig.isProjectClassConfig()) {
            try {
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    project = (Project) AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(m_classLoader.loadClass(projectString)));
                }else{
                    project = (Project) PrivilegedAccessHelper.newInstanceFromClass(m_classLoader.loadClass(projectString));
                }
            } catch (Throwable exception) {
                throw SessionLoaderException.failedToLoadProjectClass(projectString, exception);
            }
        } else {
            try {
                project = XMLProjectReader.read(projectString, m_classLoader);
            } catch (ValidationException validationException) {
                if (validationException.getErrorCode() == ValidationException.PROJECT_XML_NOT_FOUND) {
                    try {
                        project = XMLProjectReader.read(projectString);
                    } catch (Exception e) {
                        throw SessionLoaderException.failedToLoadProjectXml(projectString, validationException);
                    }
                } else {
                    throw SessionLoaderException.failedToLoadProjectXml(projectString, validationException);
                }
            }
        }

        return project;
    }

    /**
     * INTERNAL:
     * Build the correct login based on the login config type
     */
    private Login buildLogin(LoginConfig loginConfig) {
        if (loginConfig instanceof EISLoginConfig) {
            return buildEISLoginConfig((EISLoginConfig)loginConfig);
        } else if (loginConfig instanceof XMLLoginConfig) {
            return buildXMLLoginConfig((XMLLoginConfig)loginConfig);
        } else if (loginConfig instanceof DatabaseLoginConfig) {
            return buildDatabaseLoginConfig((DatabaseLoginConfig)loginConfig);
        } else {
            return null;
        }
    }

    /**
     * INTERNAL:
     * Wrapped by the getLogin() call, therefore, config can't be null at this
     * point.
     */
    private Login buildEISLoginConfig(EISLoginConfig eisLoginConfig) {
        EISLogin eisLogin = new EISLogin();

        // Connection Spec
        String specClassName = eisLoginConfig.getConnectionSpecClass();
        if (specClassName != null) {
            try {
                Class specClass = m_classLoader.loadClass(specClassName);
                EISConnectionSpec spec = null;
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    spec = (EISConnectionSpec)AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(specClass));
                }else{
                    spec = (EISConnectionSpec)PrivilegedAccessHelper.newInstanceFromClass(specClass);
                }
                eisLogin.setConnectionSpec(spec);
            } catch (Exception exception) {
                throw SessionLoaderException.failedToLoadTag("connection-spec-class", specClassName, exception);
            }
        }

        // Connection Factory URL, setConnectionFactoryURL checks for null
        eisLogin.setConnectionFactoryURL(eisLoginConfig.getConnectionFactoryURL());

        // Process the common elements in LoginConfig
        processLoginConfig(eisLoginConfig, eisLogin);

        // Finally, return the newly created EISLogin
        return eisLogin;
    }

    /**
      * INTERNAL:
      * Wrapped by the getLogin() call, therefore, config can't be null at this
      * point.
      */
    private Login buildXMLLoginConfig(XMLLoginConfig xmlLoginConfig) {
        XMLLogin xmlLogin = new XMLLogin();

        // Process the common elements in LoginConfig
        processLoginConfig(xmlLoginConfig, xmlLogin);

        // Finally, return the newly created XMLLogin
        return xmlLogin;
    }

    /**
     * INTERNAL:
     * Build a DatabaseLogin for the given Session
     * Wrapped by the getLogin() call, therefore, config can't be null at this
     * point.
     */
    private Login buildDatabaseLoginConfig(DatabaseLoginConfig databaseLoginConfig) {
        DatabaseLogin databaseLogin = new DatabaseLogin();

        // Driver class
        String driverClassName = databaseLoginConfig.getDriverClass();
        if (driverClassName != null) {
            try {
                Class driverClass = m_classLoader.loadClass(driverClassName);
                databaseLogin.setDriverClass(driverClass);
            } catch (Exception exception) {
                throw SessionLoaderException.failedToLoadTag("driver-class", driverClassName, exception);
            }
        }

        // Connection URL
        String connectionString = databaseLoginConfig.getConnectionURL();
        if (connectionString != null) {
            databaseLogin.setConnectionString(connectionString);
        }

        // Datasource
        String datasourceName = databaseLoginConfig.getDatasource();
        if (datasourceName != null) {
            try {
                JNDIConnector jndiConnector = new JNDIConnector(new javax.naming.InitialContext(), datasourceName);
                jndiConnector.setLookupType(databaseLoginConfig.getLookupType().intValue());
                databaseLogin.setConnector(jndiConnector);
            } catch (Exception exception) {
                throw SessionLoaderException.failedToLoadTag("datasource", datasourceName, exception);
            }
        }

        // Bind all parameters - XML Schema default is false
        databaseLogin.setShouldBindAllParameters(databaseLoginConfig.getBindAllParameters());

        // Cache all statements - XML Schema default is false
        databaseLogin.setShouldCacheAllStatements(databaseLoginConfig.getCacheAllStatements());

        // Byte array binding - XML Schema default is true
        databaseLogin.setUsesByteArrayBinding(databaseLoginConfig.getByteArrayBinding());

        // String binding - XML Schema default is false
        databaseLogin.setUsesStringBinding(databaseLoginConfig.getStringBinding());

        // Stream binding - XML Schema default is false
        databaseLogin.setUsesStreamsForBinding(databaseLoginConfig.getStreamsForBinding());

        // Force field to uppper case - XML Schema default is false
        databaseLogin.setShouldForceFieldNamesToUpperCase(databaseLoginConfig.getForceFieldNamesToUppercase());

        // Optimize data conversion - XML Schema default is true
        databaseLogin.setShouldOptimizeDataConversion(databaseLoginConfig.getOptimizeDataConversion());

        // Trim strings - XML Schema default is true
        databaseLogin.setShouldTrimStrings(databaseLoginConfig.getTrimStrings());

        // Batch writing - XML Schema default is false
        databaseLogin.setUsesBatchWriting(databaseLoginConfig.getBatchWriting());

        // JDBC 2.0 batch writing - XML Schema default is true
        databaseLogin.setUsesJDBCBatchWriting(databaseLoginConfig.getJdbcBatchWriting());

        // Max batch writing size - XML Schema default is 32000
        Integer maxBatchWritingSize = databaseLoginConfig.getMaxBatchWritingSize();
        if (maxBatchWritingSize != null) {
            databaseLogin.setMaxBatchWritingSize(maxBatchWritingSize.intValue());
        }

        // Native SQL - XML Schema default is false
        databaseLogin.setUsesNativeSQL(databaseLoginConfig.getNativeSQL());

        // Process the common elements in LoginConfig
        processLoginConfig(databaseLoginConfig, databaseLogin);

        processStructConverterConfig(databaseLoginConfig.getStructConverterConfig(), databaseLogin);
        
        if (databaseLoginConfig.isConnectionHealthValidatedOnError() != null){
            databaseLogin.setConnectionHealthValidatedOnError(databaseLoginConfig.isConnectionHealthValidatedOnError());
        }
        if (databaseLoginConfig.getQueryRetryAttemptCount() != null){
            databaseLogin.setQueryRetryAttemptCount(databaseLoginConfig.getQueryRetryAttemptCount());
        }
        if (databaseLoginConfig.getDelayBetweenConnectionAttempts() != null){
            databaseLogin.setDelayBetweenConnectionAttempts(databaseLoginConfig.getDelayBetweenConnectionAttempts());
        }
        if (databaseLoginConfig.getPingSQL() != null){
            databaseLogin.setPingSQL(databaseLoginConfig.getPingSQL());
        }
        
        // Finally, return the newly created DatabaseLogin
        return databaseLogin;
    }

    /**
     * INTERNAL:
     */
    private void processStructConverterConfig(StructConverterConfig converterClassConfig, DatabaseLogin login) {
        if (converterClassConfig != null) {
            Platform platform = login.getDatasourcePlatform();
            if (platform instanceof DatabasePlatform){
                Iterator i = converterClassConfig.getStructConverterClasses().iterator();
    
                while (i.hasNext()) {
                    String converterClassName = (String)i.next();
                    try {
                        Class converterClass = m_classLoader.loadClass(converterClassName);
                        StructConverter converter = null;
                        if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                            try{
                                converter = (StructConverter)AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(converterClass));
                            }catch (PrivilegedActionException ex){
                                throw (Exception)ex.getCause();
                            }
                        }else{
                            converter = (StructConverter)PrivilegedAccessHelper.newInstanceFromClass(converterClass);
                        }
                        ((DatabasePlatform)platform).addStructConverter(converter);
                    } catch (Exception exception) {
                        throw SessionLoaderException.failedToLoadTag("struct-converter", converterClassName, exception);
                    }
                }
            }
        }
    }
    
    /**
     * INTERNAL:
     * Process the common elements of a Login.
     */
    private void processLoginConfig(LoginConfig loginConfig, DatasourceLogin login) {
        // Platform class
        String platformClassName = loginConfig.getPlatformClass();
        if (platformClassName != null) {
            try {
                Class platformClass = m_classLoader.loadClass(platformClassName);
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    login.usePlatform((DatasourcePlatform)AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(platformClass)));
                }else{
                    login.usePlatform((DatasourcePlatform)PrivilegedAccessHelper.newInstanceFromClass(platformClass));
                }
            } catch (Exception exception) {
                throw SessionLoaderException.failedToLoadTag("platform-class", platformClassName, exception);
            }
        }

        // Table qualifier
        String tableQualifier = loginConfig.getTableQualifier();
        if (tableQualifier != null) {
            login.setTableQualifier(tableQualifier);
        }

        // Username - setUserName checks for null
        login.setUserName(loginConfig.getUsername());

        // Encryption class (must be set before the password)
        // XML Schema default is oracle.toplink.internal.security.JCEEncryptor
        login.setEncryptionClassName(loginConfig.getEncryptionClass());

        // Password is encrypted on the model - setEncryptedPassword checks for null
        login.setEncryptedPassword(loginConfig.getEncryptedPassword());

        // External connection pool - XML Schema default is false
        login.setUsesExternalConnectionPooling(loginConfig.getExternalConnectionPooling());

        // External transaction controller - XML Schema default is false
        login.setUsesExternalTransactionController(loginConfig.getExternalTransactionController());

        // Sequencing - XML Schema default is null
        if (loginConfig.getSequencingConfig() != null) {
            if (loginConfig.getSequencingConfig().getDefaultSequenceConfig() != null) {
                Sequence sequence = buildSequence(loginConfig.getSequencingConfig().getDefaultSequenceConfig());
                login.setDefaultSequence(sequence);
            }

            if ((loginConfig.getSequencingConfig().getSequenceConfigs() != null) && !loginConfig.getSequencingConfig().getSequenceConfigs().isEmpty()) {
                Enumeration eSequenceConfigs = loginConfig.getSequencingConfig().getSequenceConfigs().elements();

                while (eSequenceConfigs.hasMoreElements()) {
                    Sequence sequence = buildSequence((SequenceConfig)eSequenceConfigs.nextElement());
                    login.addSequence(sequence);
                }
            }
        }

        // Properties (assumes they are all valid) 
        if (loginConfig.getPropertyConfigs() != null) {
            Enumeration e = loginConfig.getPropertyConfigs().elements();

            while (e.hasMoreElements()) {
                PropertyConfig propertyConfig = (PropertyConfig)e.nextElement();
                login.getProperties().put(propertyConfig.getName(), propertyConfig.getValue());
            }
        }
    }

    /**
     * INTERNAL:
     * Process the PoolsConfig object.
     */
    private void processPoolsConfig(PoolsConfig poolsConfig, ServerSession serverSession) {
        if (poolsConfig != null) {
            // Read connection pool
            ReadConnectionPoolConfig readConnectionPoolConfig = poolsConfig.getReadConnectionPoolConfig();
            if (readConnectionPoolConfig != null) {
                serverSession.setReadConnectionPool(buildReadConnectionPoolConfig(readConnectionPoolConfig, serverSession));
            }

            // Write connection pool
            ConnectionPoolConfig writeConnectionPoolConfig = poolsConfig.getWriteConnectionPoolConfig();
            if (writeConnectionPoolConfig != null) {
                serverSession.addConnectionPool(buildConnectionPoolConfig(writeConnectionPoolConfig, serverSession));
            }

            // Sequence connection pool
            ConnectionPoolConfig sequenceConnectionPoolConfig = poolsConfig.getSequenceConnectionPoolConfig();
            if (sequenceConnectionPoolConfig != null) {
                processSequenceConnectionPoolConfig(sequenceConnectionPoolConfig, serverSession);
            }

            // Connection pools
            Enumeration e = poolsConfig.getConnectionPoolConfigs().elements();
            while (e.hasMoreElements()) {
                ConnectionPoolConfig connectionPoolConfig = (ConnectionPoolConfig)e.nextElement();
                serverSession.addConnectionPool(buildConnectionPoolConfig(connectionPoolConfig, serverSession));
            }
        }
    }

    /**
     * INTERNAL:
     * Process a SequenceConnectionPoolConfig object.
     */
    private void processSequenceConnectionPoolConfig(ConnectionPoolConfig poolConfig, ServerSession serverSession) {
        // Set the Sequence connection pool flag to true
        serverSession.getSequencingControl().setShouldUseSeparateConnection(true);

        // Max connections
        Integer maxConnections = poolConfig.getMaxConnections();
        if (maxConnections != null) {
            serverSession.getSequencingControl().setMaxPoolSize(maxConnections.intValue());
        }

        // Min connections
        Integer minConnections = poolConfig.getMinConnections();
        if (minConnections != null) {
            serverSession.getSequencingControl().setMinPoolSize(minConnections.intValue());
        }

        // Name - no need to process
    }

    /**
     * INTERNAL:
     * Process a ServerPlatformConfig object.
     */
    private void processServerPlatformConfig(ServerPlatformConfig platformConfig, ServerPlatform platform) {
        // Enable runtime services - XML schema default is true
        if (!platformConfig.getEnableRuntimeServices()) {
            platform.disableRuntimeServices();
        }

        // Enable JTA - XML schema default is true
        if (!platformConfig.getEnableJTA()) {
            platform.disableJTA();
        }
    }

    /**
     * INTERNAL:
     * Build a connection pool from the config to store on the server session.
     */
    private ConnectionPool buildConnectionPoolConfig(ConnectionPoolConfig poolConfig, ServerSession serverSession) {
        ConnectionPool connectionPool = new ConnectionPool();

        // Process the common elements in ConnectionPool
        processConnectionPoolConfig(poolConfig, connectionPool, serverSession);

        return connectionPool;
    }

    /**
     * INTERNAL:
     */
    private ServerPlatform buildCustomServerPlatformConfig(CustomServerPlatformConfig platformConfig, DatabaseSessionImpl session) {
        ServerPlatform platform;

        // Server class - XML schema default is oracle.toplink.platform.server.CustomServerPlatform
        String serverClassName = platformConfig.getServerClassName();
        try {
            Class serverClass = m_classLoader.loadClass(serverClassName);
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                Constructor constructor = (Constructor)AccessController.doPrivileged(new PrivilegedGetConstructorFor(serverClass, new Class[] { oracle.toplink.sessions.DatabaseSession.class }, false));
                platform = (ServerPlatform)AccessController.doPrivileged(new PrivilegedInvokeConstructor(constructor, new Object[] { session }));
            }else{
                Constructor constructor = PrivilegedAccessHelper.getConstructorFor(serverClass, new Class[] { oracle.toplink.sessions.DatabaseSession.class }, false);
                platform = (ServerPlatform)PrivilegedAccessHelper.invokeConstructor(constructor, new Object[] { session });
            }
        } catch (Throwable e) {
            throw SessionLoaderException.failedToLoadTag("server-class", serverClassName, e);
        }

        // External transaction controller class
        String externalTransactionControllerClass = platformConfig.getExternalTransactionControllerClass();
        if (externalTransactionControllerClass != null) {
            try {
                platform.setExternalTransactionControllerClass(m_classLoader.loadClass(externalTransactionControllerClass));
            } catch (Exception exception) {
                throw SessionLoaderException.failedToLoadTag("external-transaction-controller-class", externalTransactionControllerClass, exception);
            }
        }

        return platform;
    }

    /**
     * INTERNAL:
     * Build a read connection pool from the config to store on the server session.
     */
    private ConnectionPool buildReadConnectionPoolConfig(ReadConnectionPoolConfig poolConfig, ServerSession serverSession) {
        // Exclusive tag - XML Schema default is false
        ConnectionPool connectionPool = (poolConfig.getExclusive()) ? new ConnectionPool() : new ReadConnectionPool();

        // Process the common elements in ConnectionPool
        processConnectionPoolConfig(poolConfig, connectionPool, serverSession);

        return connectionPool;
    }

    /**
     * INTERNAL:
     * Process the common elements from a ConnectionPoolConfig
     */
    private void processConnectionPolicyConfig(ConnectionPolicyConfig connectionPolicyConfig, ServerSession serverSession) {
        if (connectionPolicyConfig != null) {
            ConnectionPolicy connectionPolicy = serverSession.getDefaultConnectionPolicy();
            connectionPolicy.setShouldUseExclusiveConnection(connectionPolicyConfig.getUseExclusiveConnection());
            connectionPolicy.setIsLazy(connectionPolicyConfig.getLazy());
        }
    }

    /**
     * INTERNAL:
     * Process the common elements from a ConnectionPoolConfig
     */
    private void processConnectionPoolConfig(ConnectionPoolConfig poolConfig, ConnectionPool connectionPool, AbstractSession session) {
        // Login - if null, set it to the same as the session login
        Login login = buildLogin(poolConfig.getLoginConfig());
        if (login != null) {
            connectionPool.setLogin(login);
        } else {
            connectionPool.setLogin(session.getDatasourceLogin());
        }

        // Name
        connectionPool.setName(poolConfig.getName());

        // Max connections
        Integer maxConnections = poolConfig.getMaxConnections();
        if (maxConnections != null) {
            connectionPool.setMaxNumberOfConnections(maxConnections.intValue());
        }

        // Min connections
        Integer minConnections = poolConfig.getMinConnections();
        if (minConnections != null) {
            connectionPool.setMinNumberOfConnections(minConnections.intValue());
        }
    }

    /**
     * INTERNAL:
     * Process the common elements from a SessionConfig.
     */
    private void processSessionConfig(SessionConfig sessionConfig, AbstractSession session) {
        // Session Event Manager
        processSessionEventManagerConfig(sessionConfig.getSessionEventManagerConfig(), session);

        //server platform
        ((DatabaseSessionImpl)session).setServerPlatform(buildServerPlatformConfig(sessionConfig.getServerPlatformConfig(), (DatabaseSessionImpl)session));

        // Session Log - BUG# 3442865, don't set the log if it is null
        SessionLog log = buildSessionLog(sessionConfig.getLogConfig(), session);
        if (log != null) {
            session.setSessionLog(log);
        }

        // Cache synchronization
        session.setCacheSynchronizationManager(buildCacheSynchronizationManagerConfig(sessionConfig.getCacheSynchronizationManagerConfig(), session));

        // Remote command manager    
        buildRemoteCommandManagerConfig(sessionConfig.getRemoteCommandManagerConfig(), session);

        // Name
        session.setName(sessionConfig.getName());

        // Profiler - XML Schema default is null
        if (sessionConfig.getProfiler() != null) {
            if (sessionConfig.getProfiler().equals("toplink")) {
                session.setProfiler(new PerformanceProfiler());
            } else if (sessionConfig.getProfiler().equals("dms")) {
                session.setProfiler(new DMSPerformanceProfiler(session));
            }
        }

        // Exception handler
        String exceptionHandlerClassName = sessionConfig.getExceptionHandlerClass();
        if (exceptionHandlerClassName != null) {
            try {
                Class exceptionHandlerClass = m_classLoader.loadClass(exceptionHandlerClassName);
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    session.setExceptionHandler((ExceptionHandler)AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(exceptionHandlerClass)));
                }else{
                    session.setExceptionHandler((ExceptionHandler)PrivilegedAccessHelper.newInstanceFromClass(exceptionHandlerClass));
                }
            } catch (Throwable e) {
                throw SessionLoaderException.failedToLoadTag("exception-handler-class", exceptionHandlerClassName, e);
            }
        }

        // Session customizer will be processed in the buildSessions method.
        // Ensures it is run last.
    }

    /**
     * INTERNAL: Build the appropriate server platform
     */
    private ServerPlatform buildServerPlatformConfig(ServerPlatformConfig platformConfig, DatabaseSessionImpl session) {
        if (platformConfig == null) {
            return new NoServerPlatform(session);
        }

        // Build the server platform, the config model knows which to build.
        ServerPlatform platform;

        if (platformConfig instanceof CustomServerPlatformConfig) {
            platform = buildCustomServerPlatformConfig((CustomServerPlatformConfig)platformConfig, session);
        } else {
            // A supported platform so instantiate an object of its type.
            String serverClassName = platformConfig.getServerClassName();
            if(platformConfig.isSupported()) {
                try {
                    Class serverClass = m_classLoader.loadClass(serverClassName);
                    if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                        Constructor constructor = (Constructor) AccessController.doPrivileged(new PrivilegedGetConstructorFor(serverClass, new Class[] { oracle.toplink.sessions.DatabaseSession.class }, false));
                        platform = (ServerPlatform)AccessController.doPrivileged(new PrivilegedInvokeConstructor(constructor, new Object[] { (oracle.toplink.sessions.DatabaseSession)session }));
                    }else{
                        Constructor constructor = PrivilegedAccessHelper.getConstructorFor(serverClass, new Class[] { oracle.toplink.sessions.DatabaseSession.class }, false);
                        platform = (ServerPlatform)PrivilegedAccessHelper.invokeConstructor(constructor, new Object[] { (oracle.toplink.sessions.DatabaseSession)session });
                    }
                } catch (Throwable e) {
                    throw SessionLoaderException.failedToParseXML("Server platform class is invalid: " + serverClassName, e);
                }
            } else {
                throw SessionLoaderException.serverPlatformNoLongerSupported(serverClassName);
            }
        }

        // Process the common elements in ServerPlatformConfig 
        processServerPlatformConfig(platformConfig, platform);
        return platform;
    }

    /**
     * INTERNAL:
     */
    private void buildRemoteCommandManagerConfig(RemoteCommandManagerConfig rcmConfig, AbstractSession session) {
        if (rcmConfig != null) {
            RemoteCommandManager rcm = new RemoteCommandManager(session);

            // Commands
            processCommandsConfig(rcmConfig.getCommandsConfig(), rcm);

            // Transport Manager - will set the built TransportManager on the given 
            // Remote command manager that is passed in
            buildTransportManager(rcmConfig.getTransportManagerConfig(), rcm);

            // Channel - XML Schema default is TopLinkCommandChannel
            rcm.setChannel(rcmConfig.getChannel());
        }
    }

    /**
     * INTERNAL:
     */
    private void buildTransportManager(TransportManagerConfig tmConfig, RemoteCommandManager rcm) {
        if (tmConfig instanceof RMITransportManagerConfig) {
            buildRMITransportManagerConfig((RMITransportManagerConfig)tmConfig, rcm);
        } else if (tmConfig instanceof RMIIIOPTransportManagerConfig) {
            buildRMIIIOPTransportManagerConfig((RMIIIOPTransportManagerConfig)tmConfig, rcm);
        } else if (tmConfig instanceof JMSTopicTransportManagerConfig) {
            buildJMSTopicTransportManagerConfig((JMSTopicTransportManagerConfig)tmConfig, rcm);
        } else if (tmConfig instanceof Oc4jJGroupsTransportManagerConfig) {
            buildOc4jJGroupsTransportManagerConfig((Oc4jJGroupsTransportManagerConfig)tmConfig, rcm);
        } else if (tmConfig instanceof SunCORBATransportManagerConfig) {
            buildSunCORBATransportManagerConfig((SunCORBATransportManagerConfig)tmConfig, rcm);
        } else if (tmConfig instanceof UserDefinedTransportManagerConfig) {
            buildUserDefinedTransportManagerConfig((UserDefinedTransportManagerConfig)tmConfig, rcm);
        }
    }

    /**
     * INTERNAL:
     */
    private void buildRMITransportManagerConfig(RMITransportManagerConfig tmConfig, RemoteCommandManager rcm) {
        RMITransportManager tm = new RMITransportManager(rcm);

        // Set the transport manager. This will initialize the DiscoveryManager
        // This needs to be done before we process the DiscoveryConfig.
        rcm.setTransportManager(tm);

        // Discovery
        DiscoveryConfig discoveryConfig = tmConfig.getDiscoveryConfig();
        if (discoveryConfig != null) {
            processDiscoveryConfig(discoveryConfig, rcm.getDiscoveryManager());
        }

        if (tmConfig.getJNDINamingServiceConfig() != null) {
            // JNDI naming service
            tm.setNamingServiceType(TransportManager.JNDI_NAMING_SERVICE);
            processJNDINamingServiceConfig(tmConfig.getJNDINamingServiceConfig(), tm);
        } else if (tmConfig.getRMIRegistryNamingServiceConfig() != null) {
            // RMI registry naming service
            tm.setNamingServiceType(TransportManager.REGISTRY_NAMING_SERVICE);
            processRMIRegistryNamingServiceConfig(tmConfig.getRMIRegistryNamingServiceConfig(), tm);
        }

        tm.setIsRMIOverIIOP(tmConfig instanceof RMIIIOPTransportManagerConfig);

        // Send mode - Can only be Asynchronous (true) or Synchronous (false), validated by the schema
        // XML Schema default is Asynchronous
        rcm.setShouldPropagateAsynchronously(tmConfig.getSendMode().equals("Asynchronous"));

        // Process the common elements in TransportManagerConfig
        processTransportManagerConfig(tmConfig, tm);
    }

    /**
     * INTERNAL:
     * Builds a Sequence from the given SequenceConfig.
     */
    private Sequence buildSequence(SequenceConfig sequenceConfig) {
        if (sequenceConfig == null) {
            return null;
        }

        String name = sequenceConfig.getName();
        int size = sequenceConfig.getPreallocationSize().intValue();

        if (sequenceConfig instanceof DefaultSequenceConfig) {
            return new DefaultSequence(name, size);
        } else if (sequenceConfig instanceof NativeSequenceConfig) {
            return new NativeSequence(name, size);
        } else if (sequenceConfig instanceof TableSequenceConfig) {
            TableSequenceConfig tsc = (TableSequenceConfig)sequenceConfig;
            return new TableSequence(name, size, tsc.getTable(), tsc.getNameField(), tsc.getCounterField());
        } else if (sequenceConfig instanceof UnaryTableSequenceConfig) {
            UnaryTableSequenceConfig utsc = (UnaryTableSequenceConfig)sequenceConfig;
            return new UnaryTableSequence(name, size, utsc.getCounterField());
        } else if (sequenceConfig instanceof XMLFileSequenceConfig) {
            return new XMLFileSequence(name, size);
        } else {
            // Unknow SequenceConfig subclass - should never happen
            return null;
        }
    }

    /**
     * INTERNAL:
     * Left this in for now since in the future we may add more IIOP specific
     * configurations?
     */
    private void buildRMIIIOPTransportManagerConfig(RMIIIOPTransportManagerConfig tmConfig, RemoteCommandManager rcm) {
        buildRMITransportManagerConfig(tmConfig, rcm);
    }

    /**
     * INTERNAL:
     */
    private void buildJMSTopicTransportManagerConfig(JMSTopicTransportManagerConfig tmConfig, RemoteCommandManager rcm) {
        JMSTopicTransportManager tm = new JMSTopicTransportManager(rcm);

        // Set the transport manager. This will initialize the DiscoveryManager
        rcm.setTransportManager(tm);

        // JNDI naming service
        if (tmConfig.getJNDINamingServiceConfig() != null) {
            tm.setNamingServiceType(TransportManager.JNDI_NAMING_SERVICE);
            processJNDINamingServiceConfig(tmConfig.getJNDINamingServiceConfig(), tm);
        }

        // Topic host URL
        String topicHostURL = tmConfig.getTopicHostURL();
        if (topicHostURL != null) {
            tm.setTopicHostUrl(topicHostURL);
        }

        // Topic connection factory name - XML Schema default is jms/TopLinkTopicConnectionFactory
        tm.setTopicConnectionFactoryName(tmConfig.getTopicConnectionFactoryName());

        // Topic name - XML Schema default is jms/TopLinkTopic
        tm.setTopicName(tmConfig.getTopicName());

        // Process the common elements in TransportManagerConfig
        processTransportManagerConfig(tmConfig, tm);
    }

    /**
     * INTERNAL:
     */
    private void buildOc4jJGroupsTransportManagerConfig(Oc4jJGroupsTransportManagerConfig tmConfig, RemoteCommandManager rcm) {
        TransportManager tm = null;
        try {
            Class tmClass = m_classLoader.loadClass(tmConfig.getTransportManagerClassName());
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                Constructor constructor = (Constructor)AccessController.doPrivileged(new PrivilegedGetConstructorFor(tmClass, new Class[] { RemoteCommandManager.class, boolean.class, String.class }, false));
                tm = (TransportManager)AccessController.doPrivileged(new PrivilegedInvokeConstructor(constructor, new Object[] { rcm, tmConfig.useSingleThreadedNotification(), tmConfig.getTopicName() }));
            }else{
                Constructor constructor = PrivilegedAccessHelper.getConstructorFor(tmClass, new Class[] { RemoteCommandManager.class, boolean.class, String.class }, false);
                tm = (TransportManager)PrivilegedAccessHelper.invokeConstructor(constructor, new Object[] { rcm, tmConfig.useSingleThreadedNotification(), tmConfig.getTopicName() });
            }
        } catch (Throwable e) {
            throw SessionLoaderException.failedToParseXML("Oc4jJGroupsTransportManager class is invalid: " + tmConfig.getTransportManagerClassName(), e);
        }

        // Set the transport manager. This will initialize the DiscoveryManager
        rcm.setTransportManager(tm);

        // Process the common elements in TransportManagerConfig
        processTransportManagerConfig(tmConfig, tm);
    }

    /**
     * INTERNAL:
     */
    private void buildUserDefinedTransportManagerConfig(UserDefinedTransportManagerConfig tmConfig, RemoteCommandManager rcm) {
        TransportManager tm = null;

        // Transport class
        String transportManagerClassName = tmConfig.getTransportClass();
        if (transportManagerClassName != null) {
            try {
                Class transportManagerClass = m_classLoader.loadClass(transportManagerClassName);
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    tm = (TransportManager)AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(transportManagerClass));
                }else{
                    tm = (TransportManager)PrivilegedAccessHelper.newInstanceFromClass(transportManagerClass);
                }
            } catch (Throwable exception) {
                throw SessionLoaderException.failedToLoadTag("transport-class", transportManagerClassName, exception);
            }

            // Set the transport manager. This will initialize the DiscoveryManager
            rcm.setTransportManager(tm);

            // Process the common elements in TransportManagerConfig
            processTransportManagerConfig(tmConfig, tm);
        }
    }

    /**
     * INTERNAL:
     */
    private void processJNDINamingServiceConfig(JNDINamingServiceConfig namingConfig, TransportManager tm) {
        // URL
        String url = namingConfig.getURL();
        if (url != null) {
            tm.getRemoteCommandManager().setUrl(url);
        }

        // Username - XML Schema default is admin
        tm.setUserName(namingConfig.getUsername());

        // Encryption class - XML Schema default is oracle.toplink.internal.security.JCEEncryptor
        tm.setEncryptionClassName(namingConfig.getEncryptionClass());

        // Password - XML Schema default is password
        tm.setEncryptedPassword(namingConfig.getEncryptedPassword());

        // Initial context factory name - XML Schema is oracle.j2ee.rmi.RMIInitialContextFactory
        tm.setInitialContextFactoryName(namingConfig.getInitialContextFactoryName());

        // Properties (assumes they are all valid)
        Enumeration e = namingConfig.getPropertyConfigs().elements();

        while (e.hasMoreElements()) {
            PropertyConfig propertyConfig = (PropertyConfig)e.nextElement();
            tm.getRemoteContextProperties().put(propertyConfig.getName(), propertyConfig.getValue());
        }
    }

    /**
     * INTERNAL:
     */
    private void processRMIRegistryNamingServiceConfig(RMIRegistryNamingServiceConfig namingConfig, TransportManager tm) {
        // URL
        tm.getRemoteCommandManager().setUrl(namingConfig.getURL());
    }

    /**
     * INTERNAL:
     */
    private void processDiscoveryConfig(DiscoveryConfig discoveryConfig, DiscoveryManager discoveryManager) {
        // Multicast group address - XML Schema default is 226.10.12.64
        discoveryManager.setMulticastGroupAddress(discoveryConfig.getMulticastGroupAddress());

        // Mutlicast port - XML Schema default is 3121
        discoveryManager.setMulticastPort(discoveryConfig.getMulticastPort());

        // Announcement delay - XML Schema default is 1000
        discoveryManager.setAnnouncementDelay(discoveryConfig.getAnnouncementDelay());

        //Packet time-to-live - XML Schema default is 2
        discoveryManager.setPacketTimeToLive(discoveryConfig.getPacketTimeToLive());
    }

    /**
     * INTERNAL:
     */
    private void processTransportManagerConfig(TransportManagerConfig tmConfig, TransportManager tm) {
        // On connection error - Can only be DiscardConnection (true) or 
        // KeepConnection (false), validated by the schema
        // XML Schema default is DiscardConnection
        tm.setShouldRemoveConnectionOnError(tmConfig.getOnConnectionError().equals("DiscardConnection"));
    }

    /**
     * INTERNAL:
     */
    private void processSessionEventManagerConfig(SessionEventManagerConfig sessionEventManagerConfig, AbstractSession session) {
        if (sessionEventManagerConfig != null) {
            Enumeration e = sessionEventManagerConfig.getSessionEventListeners().elements();

            while (e.hasMoreElements()) {
                String listenerClassName = (String)e.nextElement();

                try {
                    Class listenerClass = m_classLoader.loadClass(listenerClassName);
                    if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                        session.getEventManager().addListener((SessionEventListener)AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(listenerClass)));
                    }else{
                        
                    }
                    session.getEventManager().addListener((SessionEventListener)PrivilegedAccessHelper.newInstanceFromClass(listenerClass));
                } catch (Exception exception) {
                    throw SessionLoaderException.failedToLoadTag("event-listener-class", listenerClassName, exception);
                }
            }
        }
    }

    /**
     * INTERNAL:
     */
    private SessionLog buildSessionLog(LogConfig logConfig, AbstractSession session) {
        if (logConfig instanceof JavaLogConfig) {
            return buildJavaLogConfig((JavaLogConfig)logConfig, session);
        } else if (logConfig instanceof DefaultSessionLogConfig) {
            return buildDefaultSessionLogConfig((DefaultSessionLogConfig)logConfig);
        } else if (logConfig instanceof ServerLogConfig) {
            return buildServerLogConfig((ServerLogConfig)logConfig, session);
        } else {
            return null;
        }
    }

    /**
     * INTERNAL:
     */
    private SessionLog buildJavaLogConfig(JavaLogConfig javaLogConfig, AbstractSession session) {
        SessionLog javaLog = null;
        if (!Version.isJDK13()) {
            try {
                // use ConversionManager to avoid loading the JDK 1.4 class unless it is needed.
                javaLog = (SessionLog)((Class)ConversionManager.getDefaultManager().convertObject("oracle.toplink.logging.JavaLog", Class.class)).newInstance();
                javaLog.setSession(session);
            } catch (Exception exception) {
                throw ValidationException.unableToLoadClass("oracle.toplink.logging.JavaLog", exception);
            }
        } else {
            throw ValidationException.featureIsNotAvailableInRunningJDKVersion("Java Log");
        }

        // Process the common elements from LogConfig
        processLogConfig(javaLogConfig, javaLog);

        return javaLog;
    }

    /**
     * INTERNAL:
     * Wrapped by the getSessionLog() call, therefore, config can't be null at
     * this point.
     */
    private SessionLog buildDefaultSessionLogConfig(DefaultSessionLogConfig defaultSessionLogConfig) {
        DefaultSessionLog defaultSessionLog = new DefaultSessionLog();

        // Log level - XML Schema default is info
        defaultSessionLog.setLevel(((Integer)m_logLevels.get(defaultSessionLogConfig.getLogLevel())).intValue());

        // Filename - setWriter will handle nulls
        defaultSessionLog.setWriter(defaultSessionLogConfig.getFilename());

        // Process the common elements from LogConfig
        processLogConfig(defaultSessionLogConfig, defaultSessionLog);

        return defaultSessionLog;
    }

    /**
     * INTERNAL:
     */
    private SessionLog buildServerLogConfig(ServerLogConfig serverLogConfig, AbstractSession session) {
        SessionLog serverLog = ((DatabaseSessionImpl)session).getServerPlatform().getServerLog();

        return serverLog;
    }

    /**
     * INTERNAL:
     */
    private void processLogConfig(LogConfig logConfig, SessionLog log) {
        if (logConfig.getLoggingOptions() != null) {
            if (logConfig.getLoggingOptions().getShouldLogExceptionStackTrace() != null) {
                log.setShouldLogExceptionStackTrace(logConfig.getLoggingOptions().getShouldLogExceptionStackTrace().booleanValue());
            }
            if (logConfig.getLoggingOptions().getShouldPrintConnection() != null) {
                log.setShouldPrintConnection(logConfig.getLoggingOptions().getShouldPrintConnection().booleanValue());
            }
            if (logConfig.getLoggingOptions().getShouldPrintDate() != null) {
                log.setShouldPrintDate(logConfig.getLoggingOptions().getShouldPrintDate().booleanValue());
            }
            if (logConfig.getLoggingOptions().getShouldPrintSession() != null) {
                log.setShouldPrintSession(logConfig.getLoggingOptions().getShouldPrintSession().booleanValue());
            }
            if (logConfig.getLoggingOptions().getShouldPrintThread() != null) {
                log.setShouldPrintThread(logConfig.getLoggingOptions().getShouldPrintThread().booleanValue());
            }
        }
    }

    /**
     * INTERNAL:
     */
    private CacheSynchronizationManager buildCacheSynchronizationManagerConfig(CacheSynchronizationManagerConfig csmConfig, AbstractSession session) {
        CacheSynchronizationManager csm = null;

        if (csmConfig != null) {
            csm = new CacheSynchronizationManager();

            // Is asynchronous - XML Schema default is true
            csm.setIsAsynchronous(csmConfig.getIsAsynchronous());

            // Remove connection on error - XML Schema default is true
            csm.setShouldRemoveConnectionOnError(csmConfig.getRemoveConnectionOnError());

            // Clustering service
            AbstractClusteringService clusteringService = buildClusteringService(csmConfig.getClusteringServiceConfig(), session);
            if (clusteringService != null) {
                csm.setClusteringService(clusteringService);
            }
        }

        return csm;
    }

    /**
     * INTERNAL:
     * Build the correct clustering service type based on the clustering config type.
     */
    private AbstractClusteringService buildClusteringService(ClusteringServiceConfig csConfig, AbstractSession session) {
        if (csConfig instanceof WLSClusteringConfig) {
            return buildWLSClusteringConfig((WLSClusteringConfig)csConfig, session);
        } else if (csConfig instanceof RMIClusteringConfig) {
            return buildRMIClusteringConfig((RMIClusteringConfig)csConfig, session);
        } else if (csConfig instanceof JMSClusteringConfig) {
            return buildJMSClusteringConfig((JMSClusteringConfig)csConfig, session);
        } else if (csConfig instanceof RMIJNDIClusteringConfig) {
            return buildRMIJNDIClusteringConfig((RMIJNDIClusteringConfig)csConfig, session);
        } else if (csConfig instanceof RMIIIOPJNDIClusteringConfig) {
            return buildRMIIIOPJNDIClusteringConfig((RMIIIOPJNDIClusteringConfig)csConfig, session);
        } else if (csConfig instanceof SunCORBAJNDIClusteringConfig) {
            return buildSunCORBAJNDIClusteringConfig((SunCORBAJNDIClusteringConfig)csConfig, session);
        } else {
            return null;
        }
    }

    /**
     * INTERNAL:
     * Builds a WLS Clustering service from the given ClusteringConfig.
     */
    private AbstractClusteringService buildWLSClusteringConfig(WLSClusteringConfig csConfig, AbstractSession session) {
        WLSClusteringService cs = new WLSClusteringService(session);

        // Process the common elements in ClusteringServiceConfig
        processClusteringServiceConfig(csConfig, cs);

        return cs;
    }

    /**
     * INTERNAL:
     * Builds an RMI Clustering service from the given ClusteringConfig.
     */
    private AbstractClusteringService buildRMIClusteringConfig(RMIClusteringConfig csConfig, AbstractSession session) {
        RMIClusteringService cs = new RMIClusteringService(session);

        // Process the common elements in ClusteringServiceConfig
        processClusteringServiceConfig(csConfig, cs);

        return cs;
    }

    /**
     * INTERNAL:
     * Builds a JMS Clustering service from the given ClusteringConfig.
     */
    private AbstractClusteringService buildJMSClusteringConfig(JMSClusteringConfig csConfig, AbstractSession session) {
        JMSClusteringService cs = new JMSClusteringService(session);

        // Topic connection factory name - null is handled in the clustering service
        cs.setTopicConnectionFactoryName(csConfig.getJMSTopicConnectionFactoryName());

        // Topic name - null is handled in the clustering service
        cs.setTopicName(csConfig.getJMSTopicName());

        // Process the common elements in JNDIClusteringServiceConfig
        processJNDIClusteringServiceConfig(csConfig, cs);

        return cs;
    }

    /**
     * INTERNAL:
     * Builds an RMI JNDI Clustering service from the given ClusteringConfig.
     */
    private AbstractClusteringService buildRMIJNDIClusteringConfig(RMIJNDIClusteringConfig csConfig, AbstractSession session) {
        oracle.toplink.remote.rmi.RMIJNDIClusteringService cs = new oracle.toplink.remote.rmi.RMIJNDIClusteringService(session);

        // Process the common elements in JNDIClusteringServiceConfig
        processJNDIClusteringServiceConfig(csConfig, cs);

        return cs;
    }

    /**
     * INTERNAL:
     * Builds an RMI-IIOP JNDI Clustering service from the given ClusteringConfig.
     */
    private AbstractClusteringService buildRMIIIOPJNDIClusteringConfig(RMIIIOPJNDIClusteringConfig csConfig, AbstractSession session) {
        oracle.toplink.remote.rmi.iiop.RMIJNDIClusteringService cs = new oracle.toplink.remote.rmi.iiop.RMIJNDIClusteringService(session);

        // Process the common elements in JNDIClusteringServiceConfig
        processJNDIClusteringServiceConfig(csConfig, cs);

        return cs;
    }

    /**
     * INTERNAL:
     * Builds a Sun CORBA JNDI Clustering service from the given ClusteringConfig.
     */
    private AbstractClusteringService buildSunCORBAJNDIClusteringConfig(SunCORBAJNDIClusteringConfig csConfig, AbstractSession session) {
        oracle.toplink.remote.corba.sun.CORBAJNDIClusteringService cs = new oracle.toplink.remote.corba.sun.CORBAJNDIClusteringService(session);

        // Process the common elements in JNDIClusteringServiceConfig
        processJNDIClusteringServiceConfig(csConfig, cs);

        return cs;
    }

    /**
     * INTERNAL:
     * Builds a Sun CORBA transport manager with the given remote command manager
     */
    private void buildSunCORBATransportManagerConfig(SunCORBATransportManagerConfig tmConfig, RemoteCommandManager rcm) {
        SunCORBATransportManager tm = new SunCORBATransportManager(rcm);

        // Set the transport manager. This will initialize the DiscoveryManager
        rcm.setTransportManager(tm);

        // Process the common elements in TransportManagerConfig
        processTransportManagerConfig(tmConfig, tm);
    }

    /**
     * INTERNAL:
     * Process the common elements of a ClusteringServiceConfig
     */
    private void processClusteringServiceConfig(ClusteringServiceConfig csConfig, AbstractClusteringService cs) {
        // Multicast port - XML Schema default is 6018
        Integer multicastPort = csConfig.getMulticastPort();
        if (multicastPort != null) {
            cs.setMulticastPort(multicastPort.intValue());
        }

        // Multicast group address - XML Schema default is 226.18.6.18
        cs.setMulticastGroupAddress(csConfig.getMulticastGroupAddress());

        // Packet time to live - XML Schema default is 2
        Integer timeToLive = csConfig.getPacketTimeToLive();
        if (timeToLive != null) {
            cs.setTimeToLive(timeToLive.intValue());
        }

        // Naming Service URL
        String localHostURL = csConfig.getNamingServiceURL();
        if (localHostURL != null) {
            cs.setLocalHostURL(localHostURL);
        }
    }

    /**
     * INTERNAL:
     * Process the common elements of a ClusteringServiceConfig
     */
    private void processJNDIClusteringServiceConfig(JNDIClusteringServiceConfig csConfig, AbstractJNDIClusteringService cs) {
        // JNDI username
        String jndiUsername = csConfig.getJNDIUsername();
        if (jndiUsername != null) {
            cs.setUserName(jndiUsername);
        }

        // JNDI password
        String jndiPassword = csConfig.getJNDIPassword();
        if (jndiPassword != null) {
            cs.setPassword(jndiPassword);
        }

        // Naming service initial context factory
        String initialContextFactoryName = csConfig.getNamingServiceInitialContextFactoryName();
        if (initialContextFactoryName != null) {
            cs.setInitialContextFactoryName(initialContextFactoryName);
        }

        // Process the common elements in ClusteringServiceConfig
        processClusteringServiceConfig(csConfig, cs);
    }

    /**
     * INTERNAL:
     */
    private void processCommandsConfig(CommandsConfig commandsConfig, RemoteCommandManager rcm) {
        if (commandsConfig != null) {
            // cache-sync - XML Schema default is false
            ((AbstractSession)rcm.getCommandProcessor()).setShouldPropagateChanges(commandsConfig.getCacheSync());
        }
    }

    /**
     * INTERNAL:
     * Builds a session broker from the given SessionBrokerConfig.
     */
    private SessionBroker buildSessionBrokerConfig(SessionBrokerConfig sessionBrokerConfig) {
        SessionBroker sessionBroker = new SessionBroker();

        // Session names
        Enumeration sessionNames = sessionBrokerConfig.getSessionNames().elements();

        while (sessionNames.hasMoreElements()) {
            // Register the sessions
            String sessionName = (String)sessionNames.nextElement();
            sessionBroker.registerSession(sessionName, (AbstractSession)m_sessions.get(sessionName));
        }

        // Process the common elements in SessionConfig
        processSessionConfig(sessionBrokerConfig, sessionBroker);

        return sessionBroker;
    }
}
