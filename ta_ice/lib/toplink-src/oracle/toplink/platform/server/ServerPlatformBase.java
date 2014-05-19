// Copyright (c) 1998, 2008, Oracle. All rights reserved.
package oracle.toplink.platform.server;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.sql.SQLException;

import oracle.toplink.exceptions.ValidationException;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.internal.sessions.DatabaseSessionImpl;
import oracle.toplink.sessions.DatabaseSession;
import oracle.toplink.sessions.ExternalTransactionController;
import oracle.toplink.internal.localization.ToStringLocalization;
import oracle.toplink.logging.AbstractSessionLog;
import oracle.toplink.logging.SessionLog;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedNewInstanceFromClass;
import oracle.toplink.internal.databaseaccess.Accessor;

/**
 * PUBLIC:
 * Implementation of oracle.toplink.platform.server.ServerPlatform
 *
 * This is the abstract superclass of all platforms for all servers. Each DatabaseSession
 * contains an instance of the receiver, to help the DatabaseSession determine:
 *
 * - Which external transaction controller to use
 * - Whether or not to enable JTA (external transaction control)
 * - How to register/unregister for runtime services (JMX/MBean)
 * - Whether or not to enable runtime services
 * - How to launch container Threads
 *
 * Subclasses already exist to provide configurations for Oc4J, WebLogic, and WebSphere.
 *
 * If the user wants a different external transaction controller class or
 * to provide some different behavior than the provided ServerPlatform(s), we recommend
 * subclassing oracle.toplink.platform.server.ServerPlatformBase (or a subclass),
 * and overriding:
 *
 * ServerPlatformBase.getExternalTransactionControllerClass()
 * ServerPlatformBase.registerMBean()
 * ServerPlatformBase.unregisterMBean()
 *
 * for the desired behavior.
 *
 * @see oracle.toplink.platform.server.ServerPlatformBase
 *
 * public API:
 *
 * String getServerNameAndVersion()
 */
public abstract class ServerPlatformBase implements ServerPlatform {

    /**
     * externalTransactionControllerClass: This is a user-specifiable class defining the class
     * of external transaction controller to be set into the DatabaseSession
     */
    protected Class externalTransactionControllerClass;
	
    /**
     * INTERNAL:
     * isRuntimeServicesEnabled: Determines if the JMX Runtime Services will be deployed at runtime
     */
    private boolean isRuntimeServicesEnabled;

    /**
     * INTERNAL:
     * isJTAEnabled: Determines if the external transaction controller will be populated into the DatabaseSession
     * at runtime
     */
    private boolean isJTAEnabled;

    /**
     * INTERNAL:
     * isCMP: true if the container created the server platform, because we're configured
     * for CMP.
     */
    private boolean isCMP;
    
    /**
     * INTERNAL:
     * databaseSession: The instance of DatabaseSession that I am helping.
     */
    private DatabaseSession databaseSession;
    
    /**
     * INTERNAL:
     * Server name and version.
     */
    protected String serverNameAndVersion;

    /**
     * INTERNAL:
     * Default Constructor: Initialize so that runtime services and JTA are enabled. Set the DatabaseSession that I
     * will be helping.
     */
    public ServerPlatformBase(DatabaseSession newDatabaseSession) {
        this.isRuntimeServicesEnabled = true;
        this.isJTAEnabled = true;
        this.databaseSession = newDatabaseSession;
        this.setIsCMP(false);
    }

    /**
     * INTERNAL: configureProfiler(): set default performance profiler used in this server.
     */
    public void configureProfiler(oracle.toplink.sessions.Session session) {
        return;
    }
    
    /**
     * INTERNAL: getDatabaseSession(): Answer the instance of DatabaseSession the receiver is helping.
     *
     * @return DatabaseSession databaseSession
     */
    public DatabaseSession getDatabaseSession() {
        return this.databaseSession;
    }

    /**
     * PUBLIC: getServerNameAndVersion(): Talk to the relevant server class library, and get the server name
     * and version
     * 
     * @return String serverNameAndVersion
     */
    public String getServerNameAndVersion() {
        if(this.serverNameAndVersion == null) {
            this.initializeServerNameAndVersion();
        }
        return this.serverNameAndVersion;
    }

    /**
     * INTERNAL: initializeServerNameAndVersion(): Talk to the relevant server class library, and get the server name
     * and version
     *
     * Default is "unknown"
     */
    protected void initializeServerNameAndVersion() {
        this.serverNameAndVersion = ToStringLocalization.buildMessage("unknown");
    }

    /**
     * INTERNAL: getModuleName(): Answer the name of the module (jar name) that my session
       * is associated with.
       * Answer "unknown" if there is no module name available.
       *
       * Default behaviour is to return "unknown".
     *
     * @return String moduleName
     */
    public String getModuleName() {
        return "unknown";
    }

    /**
     * INTERNAL: getExternalTransactionControllerClass(): Answer the class of external transaction controller to use
     * For this server platform. This is read-only.
     *
       * If the user wants a different external transaction controller class than the provided ServerPlatform(s),
       * we recommend subclassing oracle.toplink.platform.server.ServerPlatformBase (or a subclass),
       * and overriding:
       *
       * ServerPlatformBase.getExternalTransactionControllerClass()
       *
       * for the desired behavior.
     *
     * @return Class externalTransactionControllerClass
     *
     * @see oracle.toplink.transaction.JTATransactionController
     * @see #isJTAEnabled()
     * @see #disableJTA()
     */
    public abstract Class getExternalTransactionControllerClass();

    /**
     * INTERNAL: setExternalTransactionControllerClass(Class newClass): Set the class of external
     * transaction controller to use in the DatabaseSession.
     * This is defined by the user via the sessions.xml.
     *
     * @see oracle.toplink.transaction.JTATransactionController
     * @see #isJTAEnabled()
     * @see #disableJTA()
     * @see #initializeExternalTransactionController()
     */
    public void setExternalTransactionControllerClass(Class newClass) {
        this.externalTransactionControllerClass = newClass;
    }
    
    /**
     * INTERNAL: initializeExternalTransactionController(): Populate the DatabaseSession's
     * external transaction controller with an instance of my transaction controller class.
     *
     * To change the external transaction controller class, we recommend creating a subclass of
       * ServerPlatformBase, and overriding getExternalTransactionControllerClass().
       *
       * @see ServerPlatformBase
     *
     * @return void
     *
     */
    public void initializeExternalTransactionController() {
        this.ensureNotLoggedIn();

        //BUG 3975114: Even if JTA is disabled, override if we're in CMP
        //JTA must never be disable during CMP (WLS/Oc4j)
        if (!isJTAEnabled() && !isCMP()) {
            return;
        }
        //BUG 3975114: display a warning if JTA is disabled and we're in CMP
        if (!isJTAEnabled() && isCMP()) {
            AbstractSessionLog.getLog().warning("jta_cannot_be_disabled_in_cmp");
        }

        //check if the transaction controller class is overridden by a preLogin or equivalent,
        //or if the transaction controller was already defined, in which case they should have written 
        //a subclass. Show a warning
        try {
            if (getDatabaseSession().getExternalTransactionController() != null) {
                this.externalTransactionControllerNotNullWarning();
                return;
            }
            ExternalTransactionController controller = null;
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    controller = (ExternalTransactionController)AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(this.getExternalTransactionControllerClass()));
                } catch (PrivilegedActionException exception) {
                    Exception throwableException = exception.getException();
                    if (throwableException instanceof InstantiationException) {
                        throw ValidationException.cannotCreateExternalTransactionController(getExternalTransactionControllerClass().getName());
                    } else {
                        throw ValidationException.cannotCreateExternalTransactionController(getExternalTransactionControllerClass().getName());
                    }
                }
            } else {
                controller = (ExternalTransactionController)PrivilegedAccessHelper.newInstanceFromClass(this.getExternalTransactionControllerClass());
            }
            getDatabaseSession().setExternalTransactionController(controller);
        } catch (InstantiationException instantiationException) {
            throw ValidationException.cannotCreateExternalTransactionController(getExternalTransactionControllerClass().getName());
        } catch (IllegalAccessException illegalAccessException) {
            throw ValidationException.cannotCreateExternalTransactionController(getExternalTransactionControllerClass().getName());
        }
    }

    /**
     * INTERNAL: externalTransactionControllerNotNullWarning():
     * When the external transaction controller is being initialized, we warn the developer
     * if they have already defined the external transaction controller in some way other
     * than subclassing ServerPlatformBase.
     *
     * @see #getExternalTransactionControllerClass()
     */
    protected void externalTransactionControllerNotNullWarning() {
        ((DatabaseSessionImpl)getDatabaseSession()).warning("External_transaction_controller_not_defined_by_server_platform", SessionLog.EJB);
    }

    /**
     * INTERNAL: isJTAEnabled(): Answer true if the DatabaseSession's external transaction controller class will
     * be populated with my transaction controller class at runtime. If the transaction controller class is
     * overridden in the DatabaseSession, my transaction controller class will be ignored.
     *
     * Answer true if TopLink will be configured to register for callbacks for beforeCompletion and afterCompletion.
     *
     * @return boolean isJTAEnabled
     * @see #getExternalTransactionControllerClass()
     * @see #disableJTA()
     */
    public boolean isJTAEnabled() {
        return this.isJTAEnabled;
    }

    /**
     * INTERNAL: disableJTA(): Configure the receiver such that my external transaction controller class will
     * be ignored, and will NOT be used to populate DatabaseSession's external transaction controller class
     * at runtime.
       *
       * TopLink will NOT be configured to register for callbacks for beforeCompletion and afterCompletion.
     *
     * @return void
     * @see #getExternalTransactionControllerClass()
     * @see #isJTAEnabled()
     */
    public void disableJTA() {
        this.ensureNotLoggedIn();
        this.isJTAEnabled = false;
    }

    /**
     * INTERNAL: isRuntimeServicesEnabled(): Answer true if the JMX/MBean providing runtime services for
     * the receiver's DatabaseSession will be deployed at runtime.
     *
     * @return boolean isRuntimeServicesEnabled
     * @see #disableRuntimeServices()
     */
    public boolean isRuntimeServicesEnabled() {
        return this.isRuntimeServicesEnabled;
    }

    /**
     * INTERNAL: disableRuntimeServices(): Configure the receiver such that no JMX/MBean will be registered
     * to provide runtime services for my DatabaseSession at runtime.
     *
     * @return void
     * @see #isRuntimeServicesEnabled()
     */
    public void disableRuntimeServices() {
        this.ensureNotLoggedIn();
        this.isRuntimeServicesEnabled = false;
    }

    /**
     * INTERNAL: registerMBean(): Create and deploy the JMX MBean to provide runtime services for my
     * databaseSession.
       *
       * Default is to do nothing.
     *
     * @return void
     * @see #isRuntimeServicesEnabled()
     * @see #disableRuntimeServices()
     * @see #unregisterMBean()
     */
    public void registerMBean() {
        if (!this.isRuntimeServicesEnabled()) {
            return;
        }
        this.serverSpecificRegisterMBean();
    }

    /**
     * INTERNAL: serverSpecificRegisterMBean(): Server specific implementation of the
     * creation and deployment of the JMX MBean to provide runtime services for my
     * databaseSession.
     *
     * Default is to do nothing. This should be subclassed if required.
     *
     * @return void
     * @see #isRuntimeServicesEnabled()
     * @see #disableRuntimeServices()
     * @see #registerMBean()
     */
    public void serverSpecificRegisterMBean() {
    }

    /**
     * INTERNAL: unregisterMBean(): Unregister the JMX MBean that was providing runtime services for my
     * databaseSession.
     *
     * @return void
     * @see #isRuntimeServicesEnabled()
     * @see #disableRuntimeServices()
     * @see #registerMBean()
     */
    public void unregisterMBean() {
        if (!this.isRuntimeServicesEnabled()) {
            return;
        }
        this.serverSpecificUnregisterMBean();
    }

    /**
     * INTERNAL:  This method is used to unwrap the connection wrapped by
     * the application server.  TopLink needs this unwrapped connection for certain
     * database vendor specific support. (ie TIMESTAMPTZ,NCHAR,XMLTYPE)
     * 
     * Be default we will use the connection's metadata to try to get the connection
     */
    public java.sql.Connection unwrapConnection(java.sql.Connection connection){
        try {
            return connection.getMetaData().getConnection();
        } catch (java.sql.SQLException e){
            ((DatabaseSessionImpl)getDatabaseSession()).log(SessionLog.WARNING, SessionLog.CONNECTION, "cannot_unwrap_connection", e);
            return connection;            
        }
    }  

    /**
     * INTERNAL: serverSpecificUnregisterMBean(): Server specific implementation of the
     * unregistration of the JMX MBean from its server.
     *
     * Default is to do nothing. This should be subclassed if required.
     *
     * @return void
     * @see #isRuntimeServicesEnabled()
     * @see #disableRuntimeServices()
     */
    public void serverSpecificUnregisterMBean() {
    }

    /**
     * INTERNAL: launchContainerRunnable(Runnable runnable): Use the container library to
     * start the provided Runnable.
     *
     * Default behavior is to use Thread(runnable).start()
     *
     * @param Runnable runnable: the instance of runnable to be "started"
     * @return void
     */
    public void launchContainerRunnable(Runnable runnable) {
        new Thread(runnable).start();
    }

    /**
     * INTERNAL: Make sure that the DatabaseSession has not logged in yet.
       * Throw a ValidationException if we have.
     *
     */
    protected void ensureNotLoggedIn() {
        //RCM: Allow for a null database session
        if (getDatabaseSession() == null) {
            return;
        }
    }

    /**
     * INTERNAL: getServerLog(): Return the ServerLog for this platform
     *
     * Return the default ServerLog in the base
     *
     * @return oracle.toplink.logging.SessionLog
     */
    public oracle.toplink.logging.SessionLog getServerLog() {
        return new ServerLog();
    }
    
    /**
     * INTERNAL: isCMP(): Answer true if we're in the context of CMP (i.e. the container created me)
     *
     * @return boolean 
     */
    public boolean isCMP() {
        return isCMP;
    }

    /**
     * INTERNAL: setIsCMP(boolean): Define whether or not we're in the context of CMP (i.e. the container created me)
     *
     * @return void 
     */
    public void setIsCMP(boolean isThisCMP) {
        isCMP = isThisCMP;
    }

    /**
     * INTERNAL: shouldUseDriverManager(): Indicates whether DriverManager should be used while connecting DefaultConnector.
     *
     * @return boolean 
     */
    public boolean shouldUseDriverManager() {
        return true;
    }

    /**
     * INTERNAL:
     * A call to this method will perform a platform based check on the connection and exception
     * error code to determine if the connection is still valid or if a communication error has occurred.
     * If a communication error has occurred then the query may be retried.
     * If this platform is unable to determine if the error was communication based it will return
     * false forcing the error to be thrown to the user.
     */
    
    public boolean wasFailureCommunicationBased(SQLException exception, Accessor connection, AbstractSession sessionForProfile){
        return getDatabaseSession().getPlatform().wasFailureCommunicationBased(exception, connection.getConnection(), sessionForProfile);
    }

}
