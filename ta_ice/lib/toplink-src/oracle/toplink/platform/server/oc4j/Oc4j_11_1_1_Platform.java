// Copyright (c) 1998, 2008, Oracle. All rights reserved.
package oracle.toplink.platform.server.oc4j;

import java.lang.reflect.Field;
//import java.sql.SQLException;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
//import javax.management.MBeanServer;

import com.evermind.server.ejb.AbstractEJBHome;
import com.evermind.server.ejb.EJBContainer;
import com.evermind.server.ejb.MessageDrivenHome;
import com.evermind.server.http.HttpApplication;
import com.evermind.server.ContextContainer;
//import com.evermind.server.OC4JServer;
import com.evermind.server.ThreadState;
import com.evermind.server.ApplicationServer;

//import oracle.oc4j.connector.proxy.AbstractProxy;

//import oracle.oc4j.admin.jmx.server.Oc4jMBeanServerFactory;
import oracle.oc4j.admin.management.mbeans.Constant;
//import oracle.as.jmx.framework.PortableMBeanFactory;

//import oracle.toplink.services.oc4j.Oc4jRuntime;
//import oracle.toplink.services.oc4j.Oc4jRuntimeMXBean;
import oracle.toplink.sessions.DatabaseSession;
import oracle.toplink.tools.profiler.DMSPerformanceProfiler;
//import oracle.toplink.internal.databaseaccess.Accessor;
import oracle.toplink.internal.localization.ToStringLocalization;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.internal.sessions.DatabaseSessionImpl;
import oracle.toplink.logging.SessionLog;
//import oracle.ucp.UniversalPooledConnection;

/**
 * PUBLIC: This is the concrete subclass responsible for representing Oc4j
 * version 11.1.1 specific behaviour.
 * Implementing Runnable interface is a workaround to handle JNDI lookup issue for launching a TopLink thread.
 * It will be removed when container provide the api to launch thread associate with ContextContainer.
 *     08/07/2008- 11.1.1   Michael OBrien 
 *       7278787 : Remove OC4J11 specific functionality specific to proxy.AbstractProxy
 *     18/08/2008- 11.1.1   Michael OBrien 
 *       7278787 : Remove usage of OC4JServer
 *     20/08/2008- 11.1.1   Michael OBrien 
 *       7278787 : Remove ucp(UniversalPooledConnection) functionality
 */
public class Oc4j_11_1_1_Platform extends Oc4jPlatform implements Runnable {
    public String baseObjectName;

    /**
     * INTERNAL:
     * The following two attributes only used if a new Oc4j_11_1_1_Platform instantiated in 
     * launchContainerRunnable method.
     * This is workaround to handle a JNDI lookup issue when launching a TopLink thread.
     * The attributes will be removed when container provide the api to launch thread associate with ContextContainer.
     */
    protected ContextContainer context;
    protected Runnable runnableObject;


    /**
     * INTERNAL:
     * Default Constructor: All behaviour for the default constructor is inherited
     */
    public Oc4j_11_1_1_Platform(DatabaseSession newDatabaseSession) {
        super(newDatabaseSession);
    }

    /**
     * INTERNAL:
     * This constructor only used to create object used in launchContainerRunnable method.
     * This is workaround to handle a JNDI lookup issue when launching TopLink thread.
     * This constructor will be removed when container provide the api to launch thread associate with ContextContainer.
     */
    protected Oc4j_11_1_1_Platform(Runnable runnableObject) {
        super((DatabaseSession)null);
        this.runnableObject = runnableObject;
        this.context = ThreadState.getCurrentState().getContextContainer();
    }

    /**
     * INTERNAL:
     * set contextContainer to the thread to be launched
     * callback run() method on the thread to be launched
     */
    public void run() {
        ThreadState.getCurrentState().setContextContainer(context);
        this.runnableObject.run();
    }

    /**
       *  INTERNAL:
       *  getObjectName(): Answer the ObjectName identifying the MBean providing runtime
     *  services for the given session
     *
     *  format:
     *  "oc4j:name=mySessionName,j2eeType=TopLinkSession,J2EEServer=" + Constant.OC4JJ2eeServerName +
     *  ",J2EEApplication=myApplicationName,EJBModule=myEjbModuleName.jar"
     *
     *  @return ObjectName objectName: the JSR-77 ObjectName to associate with Oc4jRuntimeServices
       */
    protected ObjectName getObjectName() throws MalformedObjectNameException, javax.naming.NamingException, InstantiationException {
        //BUG 3867367: (NPE during logout (getContextContainer() is null)...cache the objectName)
        return new javax.management.ObjectName("oc4j" + getBaseObjectName());
    }

    /**
     * INTERNAL: getRegistrationName(): Return the name used to register Oc4jRuntimeServices in OC4J.
     *
     * @return String
     */
    public String getBaseObjectName() throws javax.naming.NamingException {
        if (baseObjectName == null) {
            //BUG 4002127: Check for colon in session name, enclose in "'s
            String mySessionName = getDatabaseSession().getName();
            boolean sessionNameHasAColon = mySessionName.indexOf(":") != -1;
            if (sessionNameHasAColon) {
                mySessionName = "\"" + mySessionName + "\"";
            }

            String moduleName = getModuleName();

            //If there is no module name, then we're using POJO. EJBModule is not required.
            if (moduleName == null) {
                baseObjectName = ":name=" + mySessionName + ",j2eeType=TopLinkSession" + ",J2EEServer=" + Constant.OC4JJ2eeServerName + ",J2EEApplication=" + com.evermind.server.ThreadState.getCurrentState().getContextContainer().getApplication().getName();
            } else {
                baseObjectName = ":name=" + mySessionName + ",j2eeType=TopLinkSession" + ",J2EEServer=" + Constant.OC4JJ2eeServerName + ",J2EEApplication=" + com.evermind.server.ThreadState.getCurrentState().getContextContainer().getApplication().getName() + "," + getModuleType() + "=" + moduleName;
            }
        }
        return baseObjectName;
    }

    /**
     * INTERNAL: getServerLog(): Return the correct ServerLog for this platform
     *
     * Return a OjdlLog
     *
     * @return oracle.toplink.logging.SessionLog
     */
    public oracle.toplink.logging.SessionLog getServerLog() {
        return new OjdlLog();
    }

    /**
     * INTERNAL: serverSpecificRegisterMBean(): Talk to the Oc4jMBeanServerFactory, and
     * register Oc4jRuntimeServices against the ObjectName for it.
     *
     * @return void
     * @see getObjectName()
     * @see registerMBean()
     */
/*    public void serverSpecificRegisterMBean() {
        try {
            PortableMBeanFactory portableMBeanFactory = new PortableMBeanFactory();
            MBeanServer platformMbeanServer= portableMBeanFactory.getMBeanServer();
            Oc4jRuntime managementObject= new Oc4jRuntime((oracle.toplink.internal.sessions.DatabaseSessionImpl)getDatabaseSession());
            Object mbean= portableMBeanFactory.createMBean(managementObject, Oc4jRuntimeMXBean.class);
            ObjectName name= getObjectName();
            platformMbeanServer.registerMBean(mbean, name);
        } catch (Exception generalException) {
            //need better exception handling i.e. a specific exception for this
            generalException.printStackTrace();
        }
    }*/

    /**
     * INTERNAL: serverSpecificUnregisterMBean(): Talk to the Oc4jMBeanServerFactory, and
     * unregister Oc4jRuntimeServices from the ObjectName.
     *
     * ObjectName format: Unregister the JMX MBean that was providing runtime services for my
     * databaseSession.
     *
     * @return void
     * @see getObjectName()
     * @see registerMBean()
     */
/*    public void serverSpecificUnregisterMBean() {
        try {
            Oc4jMBeanServerFactory.unregisterMBean(getObjectName());
        } catch (Exception generalException) {
            //need better exception handling i.e. a specific exception for this
            generalException.printStackTrace();
        }
    }*/

    /**
       *  INTERNAL:
       *  getModuleName(): Answer the name of the module pre-set in ProjectDeployment.deployEJB()
       *  If we are using POJO, then return "unknown".
       */
    public String getModuleName() {
        String cmpModuleName = (String)this.getDatabaseSession().getProperty("oc4j.moduleName");
        if (cmpModuleName != null) {
            return ObjectName.quote(cmpModuleName);
        } else {
            ContextContainer ctxContainer = ThreadState.getCurrentState().getContextContainer();
            if (ctxContainer instanceof HttpApplication) {
                return ((HttpApplication)ctxContainer).getName();
            } else if (ctxContainer instanceof AbstractEJBHome) {
                 return EJBContainer.getEJBModuleName(((AbstractEJBHome)ctxContainer).getEJBPackage().getModule());
            } else if (ctxContainer instanceof MessageDrivenHome) {
                return EJBContainer.getEJBModuleName(((MessageDrivenHome)ctxContainer).getEJBPackage().getDeployment().getModule());
            }
            return null;
        }
    }

    /**
     *  INTERNAL:
     *  getModuleType(): Answer the type of the module toplink is deployed in.  If moduleName
     *  is preset in the properties, we know it's cmp so, an EJBModule.  Otherwise, look at the
     *  ThreadState's contextContainer to try and figure it out.  Default to EJBModule
     */
    private String getModuleType() {
        if (ThreadState.getCurrentState().getContextContainer() instanceof HttpApplication) {
            return "WebModule";
        }
        return "EJBModule";
    }

    /**
     * INTERNAL: initializeServerNameAndVersion(): Talk to the relevant server class library, and get the server name
     * and version
     */
    protected void initializeServerNameAndVersion() {
        // From 10.1.3.4 platform class
        this.serverNameAndVersion = ToStringLocalization.buildMessage("unknown");
        try {
            Class cls = Class.forName("com.evermind.server.OC4JServer", true, this.getClass().getClassLoader());
            Field field = cls.getField("INFO");
            this.serverNameAndVersion = (String)field.get(null);
        } catch (Exception ex) {
            ((DatabaseSessionImpl)getDatabaseSession()).log(SessionLog.WARNING, SessionLog.SERVER, "cannot_get_server_name_and_version", ex);
            super.initializeServerNameAndVersion();
        }
    }
    

    /**
     * INTERNAL: launchContainerRunnable(Runnable runnable): Use the container library to
     * start the provided Runnable.
	 *
     * Using a new instance of Oc4j_11_1_1_Platform is workaround to handle JNDI lookup issue for launching TopLink thread.
     * It will be removed when container provide the api to launch thread associate with ContextContainer.
     *
     * @param Runnable runnable: the instance of runnable to be "started"
     * @return void
     */
    public void launchContainerRunnable(Runnable runnable) {
        ApplicationServer.getInstance().getConnectionThreadPool().launch(new Oc4j_11_1_1_Platform(runnable));
    }

    /**
     * INTERNAL:  This method is used to unwrap the oracle connection wrapped by
     * the application server.  TopLink needs this unwrapped connection for certain
     * database vendor specific support. (ie TIMESTAMPTZ)
     */
    public java.sql.Connection unwrapConnection(java.sql.Connection connection){
        // 7278787: Disable JPA/EJB3 and OC4J 11 dependencies - following if statement was disabled
        return connection;
    //Bug#5032693  AbstractProxy instead of
        // ((oracle.jdbc.internal.OracleConnection)connection).getPhysicalConnection();
        // to eliminate dependency on Oracle jdbc
/*        if(connection instanceof AbstractProxy) {
            AbstractProxy proxy = (AbstractProxy)connection;
            return (java.sql.Connection)proxy.oc4j_getTarget();
        } else {
            return connection;
        }*/
    }

    /**
     * INTERNAL: shouldUseDriverManager(): Indicates whether DriverManager should be used while connecting DefaultConnector.
     *
     * @return boolean 
     */
    public boolean shouldUseDriverManager() {
        return false;
    }
    
    
    /**
     * INTERNAL:  This method is used to set session profiler configured by application server.
     */
    public void configureProfiler(oracle.toplink.sessions.Session session) {
        if (!(System.getProperty("oracle.dms.sensors").equals("none"))) {
                ((AbstractSession)session).setProfiler(new DMSPerformanceProfiler());
        }
    }

    
    /**
     * INTERNAL:
     * A call to this method will perform a platform based check on the connection and exception
     * error code to determine if the connection is still valid or if a communication error has occurred.
     * If a communication error has occurred then the query may be retried.
     * If this platform is unable to determine if the error was communication based it will return
     * false forcing the error to be thrown to the user.
     */
    
/*    public boolean wasFailureCommunicationBased(SQLException exception, Accessor connection, AbstractSession sessionForProfile){
    	if (connection != null && connection instanceof UniversalPooledConnection){
    		if (((UniversalPooledConnection)connection).isValid()){
    			return false;
    		}else{
    			return true;
    		}
    	} else{
         return getDatabaseSession().getPlatform().wasFailureCommunicationBased(exception, connection.getConnection(), sessionForProfile);
   	}
    }*/
}
