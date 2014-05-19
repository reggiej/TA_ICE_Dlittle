// Copyright (c) 1998, 2008, Oracle. All rights reserved.
package oracle.toplink.services.oc4j;

import java.util.*;
import java.util.regex.*;
import javax.management.openmbean.*;

/*import oracle.as.jmx.framework.InternalStateManagementFull;
import oracle.as.jmx.framework.InternalStateManagementLight;
import oracle.as.jmx.framework.InternalStateManagementLightSupport;
import oracle.as.jmx.framework.StateManagement;
import oracle.as.jmx.framework.annotations.Inject;
import oracle.as.jmx.framework.services.EventBroadcaster;
import oracle.as.jmx.framework.services.JMXSupport;
*/
import oracle.dms.instrument.DMSConsole;
import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.internal.helper.*;
import oracle.toplink.sessions.DatabaseSession;
import oracle.toplink.sessions.DefaultConnector;
import oracle.toplink.sessions.Session;
import oracle.toplink.tools.profiler.*;
import oracle.toplink.sessions.DatabaseLogin;
import oracle.toplink.threetier.*;
import oracle.toplink.internal.databaseaccess.DatabaseAccessor;
import oracle.toplink.logging.*;
import oracle.toplink.internal.identitymaps.*;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.internal.sessions.DatabaseSessionImpl;
import oracle.toplink.platform.server.oc4j.OjdlLog;

/**
 * <p>
 * <b>Purpose</b>: Provide a dynamic interface into the TopLink Session.
 * <p>
 * <b>Description</b>: This class is meant to provide facilities for managing a TopLink session external
 * to TopLink over JMX.
 */
public class Oc4jRuntime {//implements Oc4jRuntimeMXBean {

    /** stores access to the session object that we are controlling */
    protected AbstractSession session;

    /** This is the profile weight at server startup time. This is read-only */
    private int deployedSessionProfileWeight;

    /** This contains the session log from server startup time. This is read-only. */
    private SessionLog deployedSessionLog;

    /**
     *  PUBLIC:
     *  Create an instance of Oc4jRuntimeServices to be associated with the provided session
     *
     *  @param session The session to be used with these RuntimeServices
     *  @param String myBaseObjectName: "oc4j:....." (The JMX object name before it's wrapped in a ObjectName)
     */
    public Oc4jRuntime(Session session) {
        this.session = (AbstractSession)session;
        this.updateDeploymentTimeData();
    }

    /**
     *  INTERNAL:
     *  Define the session that this instance is providing runtime services for
     *
     *  @param Session session The session to be used with these RuntimeServices
     */
    protected void setSession(Session newSession) {
        this.session = (AbstractSession)newSession;

        //update the deploymentTimeData
        this.updateDeploymentTimeData();
    }

    /**
     *  INTERNAL:
     *  Answer the session that this instance is providing runtime services for
    *
     *  @return session The session to be used with these RuntimeServices
     */
    protected AbstractSession getSession() {
        return this.session;
    }

    /**
     *  INTERNAL:
     *  Define the deployment time data associated with logging and profiling
     *
     */
    protected void updateDeploymentTimeData() {
        this.deployedSessionLog = (SessionLog)((AbstractSessionLog)session.getSessionLog()).clone();
        if (session.getProfiler() == null) {
            this.deployedSessionProfileWeight = -1;//there is no profiler
        } else {
            this.deployedSessionProfileWeight = session.getProfiler().getProfileWeight();
        }
    }

    /**
     * PUBLIC: Answer the name of the TopLink session this MBean represents.
     */
    public String getSessionName() {
        return getSession().getName();
    }

    /**
     * PUBLIC: Answer the type of the TopLink session this MBean represents.
     * Types include: "ServerSession", "DatabaseSession", "SessionBroker"
     */
    public String getSessionType() {
        return Helper.getShortClassName(getSession().getClass());
    }

    /**
     * PUBLIC: Provide a list of instance of ClassSummaryDetail containing information about the
     * classes in the session whose class names match the provided filter.
     *
     * ClassSummaryDetail is a model specific class that can be used internally by the Portable JMX Framework to
     * convert class attribute to JMX required open type, it has:-
     *    1. model specific type that needs to be converted : ["Class Name", "Parent Class Name",  "Cache Type", "Configured Size", "Current Size"]
     *    2. convert methods.  
     *
     * @parm filter A comma separated list of strings to match against.
     * @return A ArrayList of instance of ClassSummaryDetail containing class information for the class names that match the filter.
     */
    public ArrayList <ClassSummaryDetail>  getClassSummaryDetailsUsingFilter(String filter) {
        //if the filter is null, return all the details
        if (filter == null) {
            return getClassSummaryDetails();
        }

        try {
            Vector mappedClassNames = getMappedClassNamesUsingFilter(filter);
            String mappedClassName;

            
            ArrayList classSummaryDetails = new ArrayList<ClassSummaryDetail>();

            //Check if there aren't any classes mapped
            if (mappedClassNames.size() == 0) {
                return null;
            }

            //get details for each class, and add the details to the summary
            for (int index = 0; index < mappedClassNames.size(); index++) {
                mappedClassName = (String)mappedClassNames.elementAt(index);
                String[] key = new String[] { mappedClassName };
                classSummaryDetails.add(buildLowlevelDetailsFor(mappedClassName));
            }

            return classSummaryDetails;
        } catch (Exception openTypeException) {
            openTypeException.printStackTrace();
        }

        //wait to get requirements from EM
        return null;
    }

    /**
     * PUBLIC: Provide a list of instance of ClassSummaryDetail containing information about all
     * classes in the session.
     *
     * ClassSummaryDetail is a model specific class that can be used internally by the Portable JMX Framework to
     * convert class attribute to JMX required open type, it has:-
     *    1. model specific type that needs to be converted : ["Class Name", "Parent Class Name",  "Cache Type", "Configured Size", "Current Size"]
     *    2. convert methods.  
     *
     * @return A ArrayList of instance of ClassSummaryDetail containing class information for the class names that match the filter.
     */
    public ArrayList <ClassSummaryDetail> getClassSummaryDetails() {
        try {
            
            Vector mappedClassNames = getMappedClassNames();

            ArrayList classSummaryDetails = new ArrayList<ClassSummaryDetail>();
            
            //Check if there aren't any classes mapped
            if (mappedClassNames.size() == 0) {
                return null;
            }

            //get details for each class, and add the details to the summary
            for (int index = 0; index < mappedClassNames.size(); index++) {
                String mappedClassName = (String)mappedClassNames.elementAt(index);
                classSummaryDetails.add(buildLowlevelDetailsFor(mappedClassName));
            }

            return classSummaryDetails;
        } catch (Exception openTypeException) {
            openTypeException.printStackTrace();
        }

        //wait to get requirements from EM
        return null;
    }

    /**
     * INTERNAL:
     * Answer the fully qualified names of the classes mapped in the session.
     * This uses the mappedClass from the CMPPolicy.
     *
     * @return java.util.Vector
     */
    private Vector getMappedClassNames() {
        Hashtable alreadyAdded = new Hashtable();
        Vector mappedClassNames = new Vector();
        String mappedClassName = null;

        Iterator descriptorsIterator = getSession().getProject().getDescriptors().values().iterator();
        while (descriptorsIterator.hasNext()) {
            ClassDescriptor nextDescriptor = (ClassDescriptor)descriptorsIterator.next();

            //differentiate between a generated class and not, by comparing the descriptor's Java class
            if (nextDescriptor.getCMPPolicy() != null) {
                if (nextDescriptor.getCMPPolicy().getMappedClass() != null) {
                    mappedClassName = nextDescriptor.getCMPPolicy().getMappedClass().getName();
                }
            }

            if (mappedClassName == null) {
                mappedClassName = nextDescriptor.getJavaClassName();
            }
            if (alreadyAdded.get(mappedClassName) == null) {
                alreadyAdded.put(mappedClassName, Boolean.TRUE);
                mappedClassNames.addElement(mappedClassName);
            }
            mappedClassName = null;
        }
        return mappedClassNames;
    }

    /**
    *  INTERNAL:
    *  This method traverses the TopLink descriptors and returns a Vector of the descriptor's
    *   reference class names that match the provided filter. The filter is a comma separated
    *   list of strings to match against.
    *
    *   @parm filter A comma separated list of strings to match against.
    *   @return A Vector of class names that match the filter.
    */
    private Vector getMappedClassNamesUsingFilter(String filter) {
        //Output Vector
        Vector outputVector = new Vector();

        //Input mapped class names
        Vector mappedClassNames = getMappedClassNames();

        //Input filter values
        ArrayList filters = new ArrayList();
        StringTokenizer lineTokens = new StringTokenizer(filter, ",");
        while (lineTokens.hasMoreTokens()) {
            filters.add(lineTokens.nextToken());
        }
        for (int i = 0; i < mappedClassNames.size(); i++) {
            String className = (String)mappedClassNames.get(i);
            String classNameLowerCase = ((String)mappedClassNames.get(i)).toLowerCase();
            for (int j = 0; j < filters.size(); j++) {
                String filterValue = (Helper.rightTrimString((String)filters.get(j)).trim()).toLowerCase();
                if (filterValue.indexOf('*') == 0) {
                    filterValue = filterValue.substring(1);
                }
                try {
                    //Note: String.matches(String regex) since jdk1.4
                    if (classNameLowerCase.matches(new StringBuffer().append("^.*").append(filterValue).append(".*$").toString())) {
                        if (!outputVector.contains(className)) {
                            outputVector.add(className);
                        }
                    }
                } catch (PatternSyntaxException exception) {
                    //regular expression syntax error
                    AbstractSessionLog.getLog().log(SessionLog.FINEST, "pattern_syntax_error", exception);
                }
            }
        }
        Collections.sort(outputVector);
        return outputVector;
    }

    /**
     * INTERNAL:
     * Helper to build a HashMap to help in the construction of a CompositeData
     *
     * @param String mappedClassName: fullyQualified class name of the class

     * @return HashMap
     */
    private ClassSummaryDetail buildLowlevelDetailsFor(String mappedClassName) {
        Class mappedClass = (Class)getSession().getDatasourcePlatform().getConversionManager().convertObject(mappedClassName, ClassConstants.CLASS);
        IdentityMap identityMap = getSession().getIdentityMapAccessorInstance().getIdentityMap(mappedClass);
        ClassDescriptor descriptor = getSession().getProject().getDescriptor(mappedClass);

        String cacheType = getCacheTypeFor(identityMap.getClass());
        String configuredSize = "" + identityMap.getMaxSize();
        String currentSize = "";

        //show the current size, including subclasses 
        currentSize = "" + identityMap.getSize(mappedClass, true);

        String parentClassName = "";

        boolean isChildDescriptor = descriptor.isChildDescriptor();

        ClassSummaryDetail details = new ClassSummaryDetail(
                mappedClassName,
                (isChildDescriptor ? "" : cacheType),
                (isChildDescriptor ? "" : configuredSize),
                currentSize,
                parentClassName);

        return details;
    }

    /**
     * INTERNAL:
     * getCacheTypeFor: Give a more UI-friendly version of the cache tye
     */
    private String getCacheTypeFor(Class identityMapClass) {
        if (identityMapClass == CacheIdentityMap.class) {
            return "Cache";
        } else if (identityMapClass == FullIdentityMap.class) {
            return "Full";
        } else if (identityMapClass == HardCacheWeakIdentityMap.class) {
            return "HardWeak";
        } else if (identityMapClass == NoIdentityMap.class) {
            return "None";
        } else if (identityMapClass == SoftCacheWeakIdentityMap.class) {
            return "SoftWeak";
        } else if (identityMapClass == WeakIdentityMap.class) {
            return "Weak";
        } else if (identityMapClass == SoftIdentityMap.class) {
            return "Soft";
        }

        return "N/A";
    }

    /**
     * PUBLIC: getModuleName: Answer the EJB-Module I belong to. This is the name of the jar
       * the session is contained in.
     */
    public String getModuleName() {
        return ((DatabaseSession)this.getSession()).getServerPlatform().getModuleName();
    }

    /**
     * PUBLIC: Answer the TopLink log level at deployment time. This is read-only.
     */
    public String getDeployedTopLinkLogLevel() {
        return getNameForLogLevel(this.deployedSessionLog.getLevel());
    }

    /**
     * PUBLIC: Answer the TopLink log level at deployment time for the given category.
     * This is read-only.
     *
     * @param String category: category of log level desired
     */
    public String getDeployedTopLinkLogLevel(String category) {
        return getNameForLogLevel(this.deployedSessionLog.getLevel(category));
    }

    /**
     * PUBLIC: Answer the TopLink log level that is changeable.
     * This does not affect the log level in the project (i.e. The next
     * time the application is deployed, changes are forgotten)
     */
    public String getCurrentTopLinkLogLevel() {
        return getNameForLogLevel(this.getSession().getSessionLog().getLevel());
    }

    /**
     * PUBLIC: Answer the TopLink log level that is changeable, given the passed
     * category.
     *
     * This does not affect the log level in the project (i.e. The next
     * time the application is deployed, changes are forgotten)
     *
     * @param String category: category for level
     */
    public String getCurrentTopLinkLogLevel(String category) {
        return getNameForLogLevel(this.getSession().getSessionLog().getLevel(category));
    }

    /**
     * PUBLIC: Set the TopLink log level to be used at runtime.
     *
     * This does not affect the log level in the project (i.e. The next
     * time the application is deployed, changes are forgotten)
     *
     * @param String newLevel: new log level
     */
    public synchronized void setCurrentTopLinkLogLevel(String newLevel) {
        this.getSession().setLogLevel(this.getLogLevelForName(newLevel));
    }

    /**
     * INTERNAL: Answer the name for the log level given.
     *
     * @return String (one of OFF, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL)
     */
    private String getNameForLogLevel(int logLevel) {
        switch (logLevel) {
        case SessionLog.ALL:
            return "ALL";
        case SessionLog.SEVERE:
            return "SEVERE";
        case SessionLog.WARNING:
            return "WARNING";
        case SessionLog.INFO:
            return "INFO";
        case SessionLog.CONFIG:
            return "CONFIG";
        case SessionLog.FINE:
            return "FINE";
        case SessionLog.FINER:
            return "FINER";
        case SessionLog.FINEST:
            return "FINEST";
        case SessionLog.OFF:
            return "OFF";
        }

        //otherwise, return "N/A"
        return "N/A";
    }

    /**
     * INTERNAL: Answer the log level for the given name.
     *
     * @return int for OFF, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL.
     */
    private int getLogLevelForName(String levelName) {
        if (levelName.equals("ALL")) {
            return SessionLog.ALL;
        }
        if (levelName.equals("SEVERE")) {
            return SessionLog.SEVERE;
        }
        if (levelName.equals("WARNING")) {
            return SessionLog.WARNING;
        }
        if (levelName.equals("INFO")) {
            return SessionLog.INFO;
        }
        if (levelName.equals("CONFIG")) {
            return SessionLog.CONFIG;
        }
        if (levelName.equals("FINE")) {
            return SessionLog.FINE;
        }
        if (levelName.equals("FINER")) {
            return SessionLog.FINER;
        }
        if (levelName.equals("FINEST")) {
            return SessionLog.FINEST;
        }
        if (levelName.equals("ALL")) {
            return SessionLog.ALL;
        }
        return SessionLog.OFF;
    }

    /**
    *     PUBLIC:
    *        This method is used to get the type of profiling.
    *   Possible values are: "TopLink", "DMS" or "None".
    */
    public synchronized String getProfilingType() {
        if (getUsesTopLinkProfiling().booleanValue()) {
            return "TopLink";
        } else if (getUsesDMSProfiling().booleanValue()) {
            return "DMS";
        } else {
            return "None";
        }
    }

    /**
    *     PUBLIC:
    *        This method is used to select the type of profiling.
    *   Valid values are: "TopLink", "DMS" or "None". These values are not case sensitive.
    *   null is considered  to be "None".
    */
    public synchronized void setProfilingType(String profileType) {
        if ((profileType == null) || (profileType.compareToIgnoreCase("None") == 0)) {
            this.setuseNoProfiling();
        } else if (profileType.compareToIgnoreCase("TopLink") == 0) {
            this.setuseTopLinkProfiling();
        } else if (profileType.compareToIgnoreCase("DMS") == 0) {
            this.setuseDMSProfiling();
        }
    }

    /**
    *     PUBLIC:
    *        This method is used to turn on TopLink Performance Profiling
    */
    public void setuseTopLinkProfiling() {
        if (getUsesTopLinkProfiling().booleanValue()) {
            return;
        }
        getSession().setProfiler(new PerformanceProfiler());
    }

    /**
    *     PUBLIC:
    *        This method is used to turn on DMS Performance Profiling
    */
    public void setuseDMSProfiling() {
        if (getUsesDMSProfiling().booleanValue()) {
            return;
        }
        getSession().setProfiler(new DMSPerformanceProfiler(getSession()));
    }

    /**
    *     PUBLIC:
    *        This method answers true if TopLink Performance Profiling is on.
    */
    public Boolean getUsesTopLinkProfiling() {
        return Boolean.valueOf(getSession().getProfiler() instanceof PerformanceProfiler);
    }

    /**
    *     PUBLIC:
    *        This method answers true if DMS Performance Profiling is on.
    */
    public Boolean getUsesDMSProfiling() {
        return Boolean.valueOf(getSession().getProfiler() instanceof DMSPerformanceProfiler);
    }

    /**
    *     PUBLIC:
    *        This method is used to turn off all Performance Profiling, DMS or TopLink.
    */
    public void setuseNoProfiling() {
        getSession().setProfiler(null);
    }

    /**
    * PUBLIC:
    *     This method is used to turn on Profile logging when using the Performance Profiler
    */
    public synchronized void setShouldLogPerformanceProfiler(Boolean shouldLogPerformanceProfiler) {
        if ((getSession().getProfiler() != null) && ClassConstants.PerformanceProfiler_Class.isAssignableFrom(getSession().getProfiler().getClass())) {
            ((PerformanceProfiler)getSession().getProfiler()).setShouldLogProfile(shouldLogPerformanceProfiler.booleanValue());
        }
    }

    /**
    * PUBLIC:
    *     Method indicates if Performace profile should be logged
    */
    public Boolean getShouldLogPerformanceProfiler() {
        if ((getSession().getProfiler() != null) && ClassConstants.PerformanceProfiler_Class.isAssignableFrom(getSession().getProfiler().getClass())) {
            return Boolean.valueOf(((PerformanceProfiler)getSession().getProfiler()).shouldLogProfile());
        }
        return Boolean.FALSE;
    }

    /**
     * PUBLIC:
     *     Method returns if all Parameters should be bound or not
     */
    public Boolean getShouldBindAllParameters() {
        if (!(getSession().getDatasourceLogin() instanceof DatabaseLogin)) {
            return Boolean.FALSE;
        }
        return Boolean.valueOf(((DatabaseLogin)getSession().getDatasourceLogin()).shouldBindAllParameters());
    }

    /**
      * PUBLIC:
      *     Return the size of strings after which will be bound into the statement
      *     If we are not using a DatabaseLogin, or we're not using string binding,
      *     answer 0 (zero).
      */
    public Integer getStringBindingSize() {
        if (!(getSession().getDatasourceLogin() instanceof DatabaseLogin)) {
            return new Integer(0);
        }
        if (!((DatabaseLogin)getSession().getDatasourceLogin()).getPlatform().usesStringBinding()) {
            return new Integer(0);
        }
        return new Integer(((DatabaseLogin)getSession().getDatasourceLogin()).getStringBindingSize());
    }

    /**
      * PUBLIC:
      *        This method will return if batchWriting is in use or not.
      */
    public Boolean getUsesBatchWriting() {
        return Boolean.valueOf(getSession().getDatasourceLogin().getPlatform().usesBatchWriting());
    }

    /**
      * PUBLIC:
      *        This method will return a long indicating the exact time in Milliseconds that the
    *   session connected to the database.
      */
    public Long getTimeConnectionEstablished() {
        return new Long(((DatabaseSessionImpl)getSession()).getConnectedTime());
    }

    /**
      * PUBLIC:
      *        This method will return if batchWriting is in use or not.
      */
    public Boolean getUsesJDBCBatchWriting() {
        return Boolean.valueOf(getSession().getDatasourceLogin().getPlatform().usesJDBCBatchWriting());
    }

    /**
      * PUBLIC:
      *     Shows if Byte Array Binding is turned on or not
      */
    public Boolean getUsesByteArrayBinding() {
        return Boolean.valueOf(getSession().getDatasourceLogin().getPlatform().usesByteArrayBinding());
    }

    /**
      * PUBLIC:
      *     Shows if native SQL is being used
      */
    public Boolean getUsesNativeSQL() {
        return Boolean.valueOf(getSession().getDatasourceLogin().getPlatform().usesNativeSQL());
    }

    /**
      * PUBLIC:
      *     This method indicates if streams are being used for binding
      */
    public Boolean getUsesStreamsForBinding() {
        return Boolean.valueOf(getSession().getDatasourceLogin().getPlatform().usesStreamsForBinding());
    }

    /**
      * PUBLIC:
      *     This method indicates if Strings are being bound
      */
    public Boolean getUsesStringBinding() {
        if (!(getSession().getDatasourceLogin() instanceof DatabaseLogin)) {
            return Boolean.FALSE;
        }
        return Boolean.valueOf(((DatabaseLogin)getSession().getDatasourceLogin()).getPlatform().usesStringBinding());
    }

    /**
    * PUBLIC:
    *     Returns if statements should be cached or not
    */
    public Boolean getShouldCacheAllStatements() {
        if (!(getSession().getDatasourceLogin() instanceof DatabaseLogin)) {
            return Boolean.FALSE;
        }
        return Boolean.valueOf(((DatabaseLogin)getSession().getDatasourceLogin()).shouldCacheAllStatements());
    }

    /**
    * PUBLIC:
    *        Returns the statement cache size.  Only valid if statements are being cached
    */
    public Integer getStatementCacheSize() {
        if (!(getSession().getDatasourceLogin() instanceof DatabaseLogin)) {
            return new Integer(0);
        }
        return new Integer(((DatabaseLogin)getSession().getDatasourceLogin()).getStatementCacheSize());
    }

    /**
    * PUBLIC:
    *     Used to clear the statement cache. Only valid if statements are being cached
    */
    public synchronized void clearStatementCache() {
        if (!(getSession().getDatasourceLogin() instanceof DatabaseLogin)) {
            return;
        }
        ((DatabaseAccessor)getSession().getAccessor()).clearStatementCache(getSession());
        getSession().getSessionLog().finer("statement_cache_cleared");
    }

    /**
    * PUBLIC:
    *        Method returns the value of the Sequence Preallocation size
    */
    public Integer getSequencePreallocationSize() {
        if (!(getSession().getDatasourceLogin() instanceof DatabaseLogin)) {
            return new Integer(0);
        }
        return new Integer(((DatabaseLogin)getSession().getDatasourceLogin()).getSequencePreallocationSize());
    }

    /**
    * PUBLIC:
    *     This method will print the available Connection pools to the SessionLog.
    * @return void
    */
    public void printAvailableConnectionPools() {
        if (ClassConstants.ServerSession_Class.isAssignableFrom(getSession().getClass())) {
            Map pools = ((ServerSession)getSession()).getConnectionPools();
            Iterator poolNames = pools.keySet().iterator();
            while (poolNames.hasNext()) {
                getSession().getSessionLog().log(SessionLog.FINER, "pool_name", poolNames.next());
            }
        } else {
            getSession().getSessionLog().finer("no_connection_pools_available");
        }
    }

    /**
    * PUBLIC:
    *     This method will retrieve the max size of a particular connection pool
    * @param poolName the name of the pool to get the max size for
    * @return Integer for the max size of the pool. Return -1 if pool doesn't exist.
    */
    public Integer getMaxSizeForPool(String poolName) {
        if (ClassConstants.ServerSession_Class.isAssignableFrom(getSession().getClass())) {
            ConnectionPool connectionPool = ((ServerSession)getSession()).getConnectionPool(poolName);
            if (connectionPool != null) {
                return new Integer(connectionPool.getMaxNumberOfConnections());
            }
        }
        return new Integer(-1);
    }

    /**
    * PUBLIC:
    *     This method will retrieve the min size of a particular connection pool
    * @param poolName the name of the pool to get the min size for
    * @return Integer for the min size of the pool. Return -1 if pool doesn't exist.
    */
    public Integer getMinSizeForPool(String poolName) {
        if (ClassConstants.ServerSession_Class.isAssignableFrom(getSession().getClass())) {
            ConnectionPool connectionPool = ((ServerSession)getSession()).getConnectionPool(poolName);
            if (connectionPool != null) {
                return new Integer(connectionPool.getMinNumberOfConnections());
            }
        }
        return new Integer(-1);
    }

    /**
    * PUBLIC:
    * This method is used to reset connections from the session to the database.  Please
    * Note that this will not work with a SessionBroker at this time
    */
    public synchronized void resetAllConnections() {
        if (ClassConstants.ServerSession_Class.isAssignableFrom(getSession().getClass())) {
            Iterator enumtr = ((ServerSession)getSession()).getConnectionPools().values().iterator();
            while (enumtr.hasNext()) {
                ConnectionPool pool = (ConnectionPool)enumtr.next();
                pool.shutDown();
                pool.startUp();
            }
        } else if (ClassConstants.PublicInterfaceDatabaseSession_Class.isAssignableFrom(getSession().getClass())) {
            getSession().getAccessor().reestablishConnection(getSession());
        }
    }

    /**
    * PUBLIC:
    *        This method is used to output those Class Names that have identity Maps in the Session.
    * Please note that SubClasses and aggregates will be missing form this list as they do not have
    * separate identity maps.
    * @return void
    */
    public void printClassesInSession() {
        Vector classes = getSession().getIdentityMapAccessorInstance().getIdentityMapManager().getClassesRegistered();
        int index;
        if (classes.isEmpty()) {
            getSession().getSessionLog().finest("no_classes_in_session");
            return;
        }

        for (index = 0; index < classes.size(); index++) {
            getSession().getSessionLog().log(SessionLog.FINEST, (String)classes.elementAt(index));
        }
    }

    /**
    * PUBLIC:
    *        This method will log the objects in the Identity Map.
    * There is no particular order to these objects.
    * @param className the fully qualified classname identifying the identity map
    * @exception  thrown then the IdentityMap for that class name could not be found
    */
    public void printObjectsInIdentityMap(String className) throws ClassNotFoundException {
        Class classWithMap = (Class)getSession().getDatasourcePlatform().getConversionManager().convertObject(className, ClassConstants.CLASS);
        IdentityMap map = getSession().getIdentityMapAccessorInstance().getIdentityMap(classWithMap);

        //check if the identity map exists
        if (map == null) {
            getSession().getSessionLog().log(SessionLog.FINER, "identity_map_does_not_exist", className);
            return;
        }

        //check if there are any objects in the identity map. Print if so.
        Enumeration objects = map.keys();
        if (!objects.hasMoreElements()) {
            getSession().getSessionLog().log(SessionLog.FINER, "identity_map_is_empty", className);
        }

        CacheKey cacheKey;
        while (objects.hasMoreElements()) {
            cacheKey = (CacheKey)objects.nextElement();
            getSession().getSessionLog().log(SessionLog.FINER, "key_value", cacheKey.getKey(), cacheKey.getObject());
        }
    }

    /**
    * PUBLIC:
    *        This method will log the types of Identity Maps in the session.
    */
    public void printAllIdentityMapTypes() {
        Vector classesRegistered = getSession().getIdentityMapAccessorInstance().getIdentityMapManager().getClassesRegistered();
        String registeredClassName;
        Class registeredClass;

        //Check if there aren't any classes registered
        if (classesRegistered.size() == 0) {
            getSession().getSessionLog().finer("no_identity_maps_in_this_session");
            return;
        }

        //get each identity map, and log the type
        for (int index = 0; index < classesRegistered.size(); index++) {
            registeredClassName = (String)classesRegistered.elementAt(index);
            registeredClass = (Class)getSession().getDatasourcePlatform().getConversionManager().convertObject(registeredClassName, ClassConstants.CLASS);
            IdentityMap map = getSession().getIdentityMapAccessorInstance().getIdentityMap(registeredClass);
            getSession().getSessionLog().log(SessionLog.FINER, "identity_map_class", registeredClassName, map.getClass());
        }
    }

    /**
    * PUBLIC:
    *        This method will log all objects in all Identity Maps in the session.
    */
    public void printObjectsInIdentityMaps() {
        Vector classesRegistered = getSession().getIdentityMapAccessorInstance().getIdentityMapManager().getClassesRegistered();
        String registeredClassName;

        //Check if there aren't any classes registered
        if (classesRegistered.size() == 0) {
            getSession().getSessionLog().finer("no_identity_maps_in_this_session");
            return;
        }

        //get each identity map, and log the type
        for (int index = 0; index < classesRegistered.size(); index++) {
            registeredClassName = (String)classesRegistered.elementAt(index);
            try {
                this.printObjectsInIdentityMap(registeredClassName);
            } catch (ClassNotFoundException classNotFound) {
                //we are enumerating registered classes, so this shouldn't happen. Print anyway
                classNotFound.printStackTrace();
            }
        }
    }

    /**
    * PUBLIC:
    *        This method is used to return the number of objects in a particular Identity Map
    * @param className the fully qualified name of the class to get number of instances of.
    * @exception  thrown then the IdentityMap for that class name could not be found
    */
    public Integer getNumberOfObjectsInIdentityMap(String className) throws ClassNotFoundException {
        //BUG 3982060: Always use the root class in combination with the identity map's getSize(class, true) to get an accurate count
        Class classWithIdentityMap = (Class)getSession().getDatasourcePlatform().getConversionManager().convertObject(className, ClassConstants.CLASS);
        Class rootClass = null;

        ClassDescriptor descriptor = getSession().getDescriptor(classWithIdentityMap);
        ClassDescriptor rootDescriptor;

        if (descriptor.hasInheritance()) {
            rootDescriptor = descriptor.getInheritancePolicy().getRootParentDescriptor();
        } else {
            rootDescriptor = descriptor;
        }
        if (rootDescriptor.getCMPPolicy() != null) {
            if (rootDescriptor.getCMPPolicy().getMappedClass() != null) {
                rootClass = rootDescriptor.getCMPPolicy().getMappedClass();
            }
        }

        if (rootClass == null) {
            rootClass = rootDescriptor.getJavaClass();
        }

        return new Integer(getSession().getIdentityMapAccessorInstance().getIdentityMap(rootClass).getSize(rootClass, true));
    }

    /**
    * PUBLIC:
    *        This method will SUM and return the number of objects in all Identity Maps in the session.
    */
    public Integer getNumberOfObjectsInAllIdentityMaps() {
        Vector classesRegistered = getSession().getIdentityMapAccessorInstance().getIdentityMapManager().getClassesRegistered();
        String registeredClassName;
        int sum = 0;

        //Check if there aren't any classes registered
        if (classesRegistered.size() == 0) {
            getSession().getSessionLog().finer("no_identity_maps_in_this_session");
            return new Integer(0);
        }

        //get each identity map, and log the size
        for (int index = 0; index < classesRegistered.size(); index++) {
            registeredClassName = (String)classesRegistered.elementAt(index);
            try {
                sum += this.getNumberOfObjectsInIdentityMap(registeredClassName).intValue();
            } catch (ClassNotFoundException classNotFound) {
                //we are enumerating registered classes, so this shouldn't happen. Print anyway
                classNotFound.printStackTrace();
            }
        }

        return new Integer(sum);
    }

    /**
    * PUBLIC:
    *        This method will answer the number of persistent classes contained in the session.
    *   This does not include aggregates.
    */
    public Integer getNumberOfPersistentClasses() {
        int count = 0;
        Hashtable classesTable = new Hashtable();
        ClassDescriptor currentDescriptor;

        //use a table to eliminate duplicate classes. Ignore Aggregates
        Iterator descriptors = getSession().getProject().getDescriptors().values().iterator();
        while (descriptors.hasNext()) {
            currentDescriptor = (ClassDescriptor)descriptors.next();
            if (!currentDescriptor.isAggregateDescriptor()) {
                classesTable.put(currentDescriptor.getJavaClassName(), Boolean.TRUE);
            }
        }

        return new Integer(classesTable.size());
    }

    /**
    * PUBLIC:
    *        This method will log the instance level locks in all Identity Maps in the session.
    */
    public void printIdentityMapLocks() {
        getSession().getIdentityMapAccessorInstance().getIdentityMapManager().printLocks();
    }

    /**
    * PUBLIC:
    *        This method will log the instance level locks in the Identity Map for the given class in the session.
    */
    public void printIdentityMapLocks(String registeredClassName) {
        Class registeredClass = (Class)getSession().getDatasourcePlatform().getConversionManager().convertObject(registeredClassName, ClassConstants.CLASS);
        getSession().getIdentityMapAccessorInstance().getIdentityMapManager().printLocks(registeredClass);
    }

    /**
    * PUBLIC:
    *        This method assumes TopLink Profiling (as opposed to Java profiling).
    *        This will log at the FINER level a summary of all elements in the profile.
    */
    public void printProfileSummary() {
        if (!this.getUsesTopLinkProfiling().booleanValue()) {
            return;
        }
        PerformanceProfiler performanceProfiler = (PerformanceProfiler)getSession().getProfiler();
        getSession().getSessionLog().finer(performanceProfiler.buildProfileSummary().toString());
    }

    /**
     * INTERNAL:
     * utility method to get rid of leading and trailing {}'s
     */
    private String trimProfileString(String originalProfileString) {
        String trimmedString;

        if (originalProfileString.length() > 1) {
            trimmedString = originalProfileString.substring(0, originalProfileString.length());
            if ((trimmedString.charAt(0) == '{') && (trimmedString.charAt(trimmedString.length() - 1) == '}')) {
                trimmedString = trimmedString.substring(1, trimmedString.length() - 1);
            }
            return trimmedString;
        } else {
            return originalProfileString;
        }
    }

    /**
    * PUBLIC:
    *        This method assumes TopLink Profiling (as opposed to Java profiling).
    *        This will log at the FIENR level a summary of all elements in the profile, categorized
    *        by Class.
    */
    public void printProfileSummaryByClass() {
        if (!this.getUsesTopLinkProfiling().booleanValue()) {
            return;
        }
        PerformanceProfiler performanceProfiler = (PerformanceProfiler)getSession().getProfiler();

        //trim the { and } from the beginning at end, because they cause problems for the logger
        getSession().getSessionLog().finer(trimProfileString(performanceProfiler.buildProfileSummaryByClass().toString()));
    }

    /**
    * PUBLIC:
    *        This method assumes TopLink Profiling (as opposed to Java profiling).
    *        This will log at the FINER level a summary of all elements in the profile, categorized
    *        by Query.
    */
    public void printProfileSummaryByQuery() {
        if (!this.getUsesTopLinkProfiling().booleanValue()) {
            return;
        }
        PerformanceProfiler performanceProfiler = (PerformanceProfiler)getSession().getProfiler();
        getSession().getSessionLog().finer(trimProfileString(performanceProfiler.buildProfileSummaryByQuery().toString()));
    }

    /**
    * PUBLIC:
    * Return the log type, either "TopLink",  "Java" or "Server"
    *
    * @return the log type
    */
    public String getLogType() {
        if (this.getSession().getSessionLog().getClass() == JavaLog.class) {
            return "Java";
        //bug5376989  ServerLog is a possiblity too
        } else if (this.getSession().getSessionLog().getClass() == OjdlLog.class) {
            return "Server";
        } else {
            return "TopLink";
        }
    }

    /**
    * PUBLIC:
    * Return the database platform used by the DatabaseSession.
    *
    * @return String databasePlatform
    */
    public String getDatabasePlatform() {
        return getSession().getDatasourcePlatform().getClass().getName();
    }

    /**
    *     PUBLIC:
    *        Return JDBCConnection detail information. This includes URL and datasource information.
    */
    public synchronized String getJdbcConnectionDetails() {
        return getSession().getLogin().getConnector().getConnectionDetails();
    }

    /**
    *     PUBLIC:
    *        Return connection pool type. Values include: "Internal", "External" and "N/A".
    */
    public synchronized String getConnectionPoolType() {
        if (getSession().getLogin().shouldUseExternalConnectionPooling()) {
            return "External";
        } else {
            return "N/A";
        }
    }

    /**
    *     PUBLIC:
    *        Return db driver class name. This only applies to DefaultConnector. Return "N/A" otherwise.
    */
    public synchronized String getDriver() {
        if (getSession().getLogin().getConnector() instanceof DefaultConnector) {
            return getSession().getLogin().getDriverClassName();
        }
        return "N/A";
    }

    /**
    * PUBLIC:
    * Return the log filename. This returns the fully qualified path of the log file when
    * TopLink logging is enabled. Null is returned otherwise.
    *
    * @return String logFilename
    */
    public String getLogFilename() {
        if (this.getLogType().equals("TopLink")) {
            // returns String or null.
            return ((DefaultSessionLog)session.getSessionLog()).getWriterFilename();
        } else {
            return null;
        }
    }

    /**
    * PUBLIC:
    *    Answer the deployed sensor weight (NORMAL, HEAVY, ALL) as an String. This is read-only.
    * Although this API exists for both DMS and TopLink profiling, it only really applies to DMS.
    */
    public synchronized String getDeployedProfileWeight() {
        return this.convertProfileWeightToString(this.deployedSessionProfileWeight);
    }

    /**
    * INTERNAL:
    *    DMS integer to String converters.
    */
    private String convertProfileWeightToString(int dmsProfileWeight) {
        if (dmsProfileWeight == DMSConsole.NORMAL) {
            return "NORMAL";
        } else if (dmsProfileWeight == DMSConsole.HEAVY) {
            return "HEAVY";
        } else if (dmsProfileWeight == DMSConsole.ALL) {
            return "ALL";
        } else {
            return "NONE";
        }
    }

    private int convertProfileStringToWeight(String dmsProfileWeight) {
        if (dmsProfileWeight.equals("NORMAL")) {
            return DMSConsole.NORMAL;
        } else if (dmsProfileWeight.equals("HEAVY")) {
            return DMSConsole.HEAVY;
        } else if (dmsProfileWeight.equals("ALL")) {
            return DMSConsole.ALL;
        } else {
            return DMSConsole.NONE;
        }
    }

    /**
    * PUBLIC:
    *    Answer the current sensor weight (NORMAL, HEAVY, ALL, NONE) as a String.
    * Although this API exists for both DMS and TopLink profiling, it only really applies to DMS.
    */
    public synchronized String getCurrentProfileWeight() {
        if (this.getSession().isInProfile()) {
            return this.convertProfileWeightToString(getSession().getProfiler().getProfileWeight());
        }
        return this.convertProfileWeightToString(DMSConsole.NONE);
    }

    /**
    * PUBLIC:
    *    This method is used to change the sensor weight (NORMAL, HEAVY, ALL, NONE) as an String.
    * Although this API exists for both DMS and TopLink profiling, it only really applies to DMS.
    */
    public synchronized void setCurrentProfileWeight(String weight) {
        if(!weight.equals("NONE")){
            getSession().setIsInProfile(true);
        }
        if (getSession().isInProfile()) {
            getSession().getProfiler().setProfileWeight(this.convertProfileStringToWeight(weight));
        }
    }

    /**
    * PUBLIC:
    *    This method is used to initialize the identity maps in the session.
    */
    public synchronized void initializeAllIdentityMaps() {
        getSession().getIdentityMapAccessor().initializeAllIdentityMaps();
    }

    /**
    * PUBLIC:
    *    This method is used to initialize the identity maps specified by the Vector of classNames.
    *
    * @param classNames String[] of fully qualified classnames identifying the identity maps to initialize
    */
    public synchronized void initializeIdentityMaps(String[] classNames) throws ClassNotFoundException {
        for (int index = 0; index < classNames.length; index++) {
            initializeIdentityMap(classNames[index]);
        }
    }

    /**
    * PUBLIC:
    *    This method is used to initialize the identity maps specified by className.
    * @param className the fully qualified classname identifying the identity map to initialize
    */
    public synchronized void initializeIdentityMap(String className) throws ClassNotFoundException {
        Class registeredClass;

        //get identity map, and initialize
        registeredClass = (Class)getSession().getDatasourcePlatform().getConversionManager().convertObject(className, ClassConstants.CLASS);
        getSession().getIdentityMapAccessor().initializeIdentityMap(registeredClass);
        getSession().getSessionLog().log(SessionLog.FINER, "identity_map_initialized", className);
    }

    /**
    * PUBLIC:
    *    This method is used to invalidate the identity maps in the session.
    */
    public synchronized void invalidateAllIdentityMaps() {
        Vector classesRegistered = getSession().getIdentityMapAccessorInstance().getIdentityMapManager().getClassesRegistered();
        String registeredClassName;
        Class registeredClass;

        if (classesRegistered.isEmpty()) {
            getSession().getSessionLog().finer("no_identity_maps_in_this_session");
        }

        //get each identity map, and invalidate
        for (int index = 0; index < classesRegistered.size(); index++) {
            registeredClassName = (String)classesRegistered.elementAt(index);
            registeredClass = (Class)getSession().getDatasourcePlatform().getConversionManager().convertObject(registeredClassName, ClassConstants.CLASS);
            getSession().getIdentityMapAccessor().invalidateClass(registeredClass);
            getSession().getSessionLog().log(SessionLog.FINER, "identity_map_invalidated", registeredClassName);
        }
    }

    /**
    * PUBLIC:
    *    This method is used to invalidate the identity maps specified by the String[] of classNames.
    *
    * @param classNames String[] of fully qualified classnames identifying the identity maps to invalidate
    * @param recurse    Boolean indicating if we want to invalidate the children identity maps too
    */
    public synchronized void invalidateIdentityMaps(String[] classNamesParam, Boolean recurse) throws ClassNotFoundException {
        String[] classNames = (String[])classNamesParam;
        for (int index = 0; index < classNames.length; index++) {
            invalidateIdentityMap(classNames[index], recurse);
        }
    }

    /**
    * PUBLIC:
    *    This method is used to invalidate the identity maps specified by className. This does not
    * invalidate the children identity maps
    *
    * @param className the fully qualified classname identifying the identity map to invalidate
    */
    public synchronized void invalidateIdentityMap(String className) throws ClassNotFoundException {
        this.invalidateIdentityMap(className, Boolean.FALSE);
    }

    /**
    * PUBLIC:
    *    This method is used to invalidate the identity maps specified by className.
    *
    * @param className the fully qualified classname identifying the identity map to invalidate
    * @param recurse    Boolean indicating if we want to invalidate the children identity maps too
    */
    public synchronized void invalidateIdentityMap(String className, Boolean recurse) throws ClassNotFoundException {
        Class registeredClass;

        //get identity map, and invalidate
        registeredClass = (Class)getSession().getDatasourcePlatform().getConversionManager().convertObject(className, ClassConstants.CLASS);
        getSession().getIdentityMapAccessor().invalidateClass(registeredClass);
        getSession().getSessionLog().log(SessionLog.FINER, "identity_map_invalidated", className);
    }

    /**
    * PUBLIC:
    *    This method is used to answer whether cache synchronization is enabled or not.
    */
    public Boolean getCacheSynchEnabled() {
        //When TopLink synchronizes the caches, it does not double check if there are connections
        //So, neither will we
        if (!getSession().hasCacheSynchronizationManager()) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
    * PUBLIC:
    *    This method is used to answer whether cache synchronization is asynchronous or not.
    */
    public Boolean getCacheSynchAsynchronous() {
        if (!getSession().hasCacheSynchronizationManager()) {
            return Boolean.FALSE;
        }
        return Boolean.valueOf(getSession().getCacheSynchronizationManager().isAsynchronous());
    }


    /*
     *  BEGIN JSR-77 StateManageable interface
     */

    /**
     * The current state of this state manageable object. 
     * The value can be one of Starting(0), Running(1), Stopping(2), 
     * Stopped(3) and Failed(4). See JSR-77 5.1.1.1 for more details
     */
/*    public int getstate() {
        return getStateManagementSupport().getState();
    }*/

    /**
     * The time the managed object was last started. 
     * The value is the number of milliseconds since January 1, 1970, 00:00:00.
     * See JSR-77 5.1.1.2 for more details
     */
    /*public long getstartTime() {
        return getStateManagementSupport().getStartTime();
    }*/
    
    /**
     * Starts the state manageable object according to the JSR-77
     * state management model
     * See JSR-77 5.1.1.2 for more details
     */
    /*public void start() throws IllegalStateException {
        getStateManagementSupport().start();
    }*/

    /**
     * Starts the state manageable object and all its child(s) 
     * according to the JSR-77 state management model
     * See JSR-77 5.1.1.2 for more details
     */
    /*public void startRecursive() throws IllegalStateException {
        getStateManagementSupport().startRecursive();
    }*/

    /**
     * Stops the state manageable object according to 
     * the JSR-77 state management model
     * See JSR-77 5.1.1.3 for more details. 
     */
/*    public void stop() throws IllegalStateException {
        getStateManagementSupport().stop();
    }*/ 

    /*
     *  END JSR-77 StateManageable interface
     */

    /**
     * Used by the dependency engine to provide access to a
     * specific container's JMX environment. The MBean 
     * implementation uses the JMXSupport interface to access
     * JMX resources in a container independent fashion.
     * The @Inject annotation is used to mark a specific method
     * as a dependency point that needs to be resolved by the JMX 
     * infrastructure 
     */
/*    @Inject
    public void setJMXSupport(JMXSupport jmxSupport) {
        jmxSupport_= jmxSupport;
    }*/

    /**
     * Used by the dependency engine to provide access to the
     * Event Service.The @Inject annotation is used to mark a specific method
     * as a dependency point that needs to be resolved by the framework 
     */
/*    @Inject
    public void setEventBroadcaster(EventBroadcaster eventBroadcaster) {
        eventBroadcaster_= eventBroadcaster;
    }*/

    /**
     * Provide access to the state management functionality provided
     * by the framework through the InternalStateManagementLightSupport
     * helper class. This class emits j2ee.state.* notification 
     * and maintains state. It ultimately delegates the state management
     * operations to the management object's callback interface.
     */
/*    private InternalStateManagementFull getStateManagementSupport() {
        if ( stateManagementSupport_== null ) {
            stateManagementSupport_= 
                new InternalStateManagementLightSupport(
                    new StateManagementFunctionality(),
                    eventBroadcaster_,
                    jmxSupport_,
                    this,
                    StateManagement.Stopped.value(),
                    -1,
                    "oracle.toplink.internal.localization.i18n.JMXLocalizationResource",
                    "Oc4jRuntime.j2ee.state.starting",
                    "Oc4jRuntime.j2ee.state.started",
                    "Oc4jRuntime.j2ee.state.stopping",
                    "Oc4jRuntime.j2ee.state.stopped",
                    "Oc4jRuntime.j2ee.state.failed"
                   );
        }
    
        return stateManagementSupport_;
    }*/

    /**
     * Internal class that provides the implementation for
     * the basic start/stop functionality associated with this 
     * managed object. Notifications and state functionality 
     * are taken care of by the frameworks helper classes. 
     */
//    private class StateManagementFunctionality implements InternalStateManagementLight {

        /**
         * Manageable Object start implementation
         * @throws IllegalStateException To report an error while starting
         */
/*        public void start() throws IllegalStateException {
            //Should trigger something like session.start().  Not sure how it's done
        }*/

        /**
         * Manageable Object startRecursive implementation
         * @throws IllegalStateException To report an error while starting
         */
/*        public void startRecursive() throws IllegalStateException {
            //Should trigger something like session.start().  Not sure how it's done
        }*/

        /**
         * Manageable Object stop implementation
         * @throws IllegalStateException To report an error while stopping
         */
/*        public void stop() throws IllegalStateException {
            //Should trigger something like session.stop().  Not sure how it's done
        }*/

//    }

    // Set through dependency injection by the 
    // JMX framework. It provides container independent
    // access to JMX resources
//    private JMXSupport jmxSupport_= null;

    // Set through dependency injection by the 
    // JMX framework. It provides a  container independent
    // Event Service
//    private EventBroadcaster eventBroadcaster_= null;

    // Helper class that provides state management capability
    // emit j2ee.state.* notification and maintain state
//    private InternalStateManagementFull stateManagementSupport_= null;

}
