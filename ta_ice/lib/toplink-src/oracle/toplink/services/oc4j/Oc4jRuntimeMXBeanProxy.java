package oracle.toplink.services.oc4j;

import java.io.IOException;
import java.util.ArrayList;

//import oracle.as.jmx.framework.proxies.StateManagementProviderProxy;


public interface Oc4jRuntimeMXBeanProxy {//extends StateManagementProviderProxy {

    /**
     * PUBLIC: Answer the name of the TopLink session this MBean represents.
     */
    public String getSessionName() throws IOException;

    /**
     * PUBLIC: Answer the type of the TopLink session this MBean represents.
     * Types include: "ServerSession", "DatabaseSession", "SessionBroker"
     */
    public String getSessionType() throws IOException;

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
    public ArrayList <ClassSummaryDetail> getClassSummaryDetailsUsingFilter(String filter) throws IOException;
    
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
    public ArrayList <ClassSummaryDetail> getClassSummaryDetails() throws IOException;
    
    /**
     * PUBLIC: getModuleName: Answer the EJB-Module I belong to. This is the name of the jar
       * the session is contained in.
     */
    public String getModuleName() throws IOException;
    
    /**
     * PUBLIC: Answer the TopLink log level at deployment time. This is read-only.
     */
    public String getDeployedTopLinkLogLevel() throws IOException;
    
    /**
     * PUBLIC: Answer the TopLink log level at deployment time for the given category.
     * This is read-only.
     *
     * @param String category: category of log level desired
     */
    public String getDeployedTopLinkLogLevel(String category) throws IOException;
    
    /**
     * PUBLIC: Answer the TopLink log level that is changeable.
     * This does not affect the log level in the project (i.e. The next
     * time the application is deployed, changes are forgotten)
     */
    public String getCurrentTopLinkLogLevel() throws IOException;
    
    /**
     * PUBLIC: Answer the TopLink log level that is changeable, given the passed
     * category.
     *
     * This does not affect the log level in the project (i.e. The next
     * time the application is deployed, changes are forgotten)
     *
     * @param String category: category for level
     */
    public String getCurrentTopLinkLogLevel(String category) throws IOException;

    /**
     * PUBLIC: Set the TopLink log level to be used at runtime.
     *
     * This does not affect the log level in the project (i.e. The next
     * time the application is deployed, changes are forgotten)
     *
     * @param String newLevel: new log level
     */
    public void setCurrentTopLinkLogLevel(String newLevel) throws IOException;

    /**
    *     PUBLIC:
    *        This method is used to get the type of profiling.
    *   Possible values are: "TopLink", "DMS" or "None".
    */
    public String getProfilingType() throws IOException;

    /**
    *     PUBLIC:
    *        This method is used to select the type of profiling.
    *   Valid values are: "TopLink", "DMS" or "None". These values are not case sensitive.
    *   null is considered  to be "None".
    */
    public void setProfilingType(String profileType) throws IOException;

    /**
    *     PUBLIC:
    *        This method answers true if TopLink Performance Profiling is on.
    */
    public Boolean getUsesTopLinkProfiling() throws IOException;

    /**
    *     PUBLIC:
    *        This method answers true if DMS Performance Profiling is on.
    */
    public Boolean getUsesDMSProfiling() throws IOException;

    /**
    * PUBLIC:
    *     This method is used to turn on Profile logging when using the Performance Profiler
    */
    public void setShouldLogPerformanceProfiler(Boolean shouldLogPerformanceProfiler) throws IOException;

    /**
    * PUBLIC:
    *     Method indicates if Performace profile should be logged
    */
    public Boolean getShouldLogPerformanceProfiler() throws IOException;

    /**
     * PUBLIC:
     *     Method returns if all Parameters should be bound or not
     */
    public Boolean getShouldBindAllParameters() throws IOException;

    /**
      * PUBLIC:
      *     Return the size of strings after which will be bound into the statement
      *     If we are not using a DatabaseLogin, or we're not using string binding,
      *     answer 0 (zero).
      */
    public Integer getStringBindingSize() throws IOException;

    /**
      * PUBLIC:
      *        This method will return if batchWriting is in use or not.
      */
    public Boolean getUsesBatchWriting() throws IOException;

    /**
      * PUBLIC:
      *        This method will return a long indicating the exact time in Milliseconds that the
    *   session connected to the database.
      */
    public Long getTimeConnectionEstablished() throws IOException;

    /**
      * PUBLIC:
      *        This method will return if batchWriting is in use or not.
      */
    public Boolean getUsesJDBCBatchWriting() throws IOException;

    /**
      * PUBLIC:
      *     Shows if Byte Array Binding is turned on or not
      */
    public Boolean getUsesByteArrayBinding() throws IOException;

    /**
      * PUBLIC:
      *     Shows if native SQL is being used
      */
    public Boolean getUsesNativeSQL() throws IOException;

    /**
      * PUBLIC:
      *     This method indicates if streams are being used for binding
      */
    public Boolean getUsesStreamsForBinding() throws IOException;

    /**
      * PUBLIC:
      *     This method indicates if Strings are being bound
      */
    public Boolean getUsesStringBinding() throws IOException;

    /**
    * PUBLIC:
    *     Returns if statements should be cached or not
    */
    public Boolean getShouldCacheAllStatements() throws IOException;

    /**
    * PUBLIC:
    *        Returns the statement cache size.  Only valid if statements are being cached
    */
    public Integer getStatementCacheSize() throws IOException;

    /**
    * PUBLIC:
    *     Used to clear the statement cache. Only valid if statements are being cached
    */
    public void clearStatementCache() throws IOException;

    /**
    * PUBLIC:
    *        Method returns the value of the Sequence Preallocation size
    */
    public Integer getSequencePreallocationSize() throws IOException;

    /**
    * PUBLIC:
    *     This method will print the available Connection pools to the SessionLog.
    * @return void
    */
    public void printAvailableConnectionPools() throws IOException;

    /**
    * PUBLIC:
    *     This method will retrieve the max size of a particular connection pool
    * @param poolName the name of the pool to get the max size for
    * @return Integer for the max size of the pool. Return -1 if pool doesn't exist.
    */
    public Integer getMaxSizeForPool(String poolName) throws IOException;

    /**
    * PUBLIC:
    *     This method will retrieve the min size of a particular connection pool
    * @param poolName the name of the pool to get the min size for
    * @return Integer for the min size of the pool. Return -1 if pool doesn't exist.
    */
    public Integer getMinSizeForPool(String poolName) throws IOException;

    /**
    * PUBLIC:
    * This method is used to reset connections from the session to the database.  Please
    * Note that this will not work with a SessionBroker at this time
    */
    public void resetAllConnections() throws IOException;

    /**
    * PUBLIC:
    *        This method is used to output those Class Names that have identity Maps in the Session.
    * Please note that SubClasses and aggregates will be missing form this list as they do not have
    * separate identity maps.
    * @return void
    */
    public void printClassesInSession() throws IOException;

    /**
    * PUBLIC:
    *        This method will log the objects in the Identity Map.
    * There is no particular order to these objects.
    * @param className the fully qualified classname identifying the identity map
    * @exception  thrown then the IdentityMap for that class name could not be found
    */
    public void printObjectsInIdentityMap(String className) throws ClassNotFoundException, IOException;

    /**
    * PUBLIC:
    *        This method will log the types of Identity Maps in the session.
    */
    public void printAllIdentityMapTypes() throws IOException;

    /**
    * PUBLIC:
    *        This method will log all objects in all Identity Maps in the session.
    */
    public void printObjectsInIdentityMaps() throws IOException;

    /**
    * PUBLIC:
    *        This method is used to return the number of objects in a particular Identity Map
    * @param className the fully qualified name of the class to get number of instances of.
    * @exception  thrown then the IdentityMap for that class name could not be found
    */
    public Integer getNumberOfObjectsInIdentityMap(String className) throws ClassNotFoundException, IOException;

    /**
    * PUBLIC:
    *        This method will SUM and return the number of objects in all Identity Maps in the session.
    */
    public Integer getNumberOfObjectsInAllIdentityMaps() throws IOException;

    /**
    * PUBLIC:
    *        This method will answer the number of persistent classes contained in the session.
    *   This does not include aggregates.
    */
    public Integer getNumberOfPersistentClasses() throws IOException;

    /**
    * PUBLIC:
    *        This method will log the instance level locks in all Identity Maps in the session.
    */
    public void printIdentityMapLocks() throws IOException;

    /**
    * PUBLIC:
    *        This method will log the instance level locks in the Identity Map for the given class in the session.
    */
    public void printIdentityMapLocks(String registeredClassName) throws IOException;

    /**
    * PUBLIC:
    *        This method assumes TopLink Profiling (as opposed to Java profiling).
    *        This will log at the INFO level a summary of all elements in the profile.
    */
    public void printProfileSummary() throws IOException;

    /**
    * PUBLIC:
    *        This method assumes TopLink Profiling (as opposed to Java profiling).
    *        This will log at the INFO level a summary of all elements in the profile, categorized
    *        by Class.
    */
    public void printProfileSummaryByClass() throws IOException;

    /**
    * PUBLIC:
    *        This method assumes TopLink Profiling (as opposed to Java profiling).
    *        This will log at the INFO level a summary of all elements in the profile, categorized
    *        by Query.
    */
    public void printProfileSummaryByQuery() throws IOException;

    /**
    * PUBLIC:
    * Return the log type, either "TopLink",  "Java" or "Server"
    *
    * @return the log type
    */
    public String getLogType() throws IOException;

    /**
    * PUBLIC:
    * Return the database platform used by the DatabaseSession.
    *
    * @return String databasePlatform
    */
    public String getDatabasePlatform() throws IOException;

    /**
    *     PUBLIC:
    *        Return JDBCConnection detail information. This includes URL and datasource information.
    */
    public String getJdbcConnectionDetails() throws IOException;

    /**
    *     PUBLIC:
    *        Return connection pool type. Values include: "Internal", "External" and "N/A".
    */
    public String getConnectionPoolType() throws IOException;

    /**
    *     PUBLIC:
    *        Return db driver class name. This only applies to DefaultConnector. Return "N/A" otherwise.
    */
    public String getDriver() throws IOException;

    /**
    * PUBLIC:
    * Return the log filename. This returns the fully qualified path of the log file when
    * TopLink logging is enabled. Null is returned otherwise.
    *
    * @return String logFilename
    */
    public String getLogFilename() throws IOException;

    /**
    * PUBLIC:
    *    Answer the deployed sensor weight (NORMAL, HEAVY, ALL) as an String. This is read-only.
    * Although this API exists for both DMS and TopLink profiling, it only really applies to DMS.
    */
    public String getDeployedProfileWeight() throws IOException;

    /**
    * PUBLIC:
    *    Answer the current sensor weight (NORMAL, HEAVY, ALL, NONE) as a String.
    * Although this API exists for both DMS and TopLink profiling, it only really applies to DMS.
    */
    public String getCurrentProfileWeight() throws IOException;

    /**
    * PUBLIC:
    *    This method is used to change the sensor weight (NORMAL, HEAVY, ALL, NONE) as an String.
    * Although this API exists for both DMS and TopLink profiling, it only really applies to DMS.
    */
    public void setCurrentProfileWeight(String weight) throws IOException;

    /**
    * PUBLIC:
    *    This method is used to initialize the identity maps in the session.
    */
    public void initializeAllIdentityMaps() throws IOException;

    /**
    * PUBLIC:
    *    This method is used to initialize the identity maps specified by the Vector of classNames.
    *
    * @param classNames String[] of fully qualified classnames identifying the identity maps to initialize
    */
    public void initializeIdentityMaps(String[] classNames) throws ClassNotFoundException, IOException;

    /**
    * PUBLIC:
    *    This method is used to initialize the identity maps specified by className.
    * @param className the fully qualified classname identifying the identity map to initialize
    */
    public void initializeIdentityMap(String className) throws ClassNotFoundException, IOException;

    /**
    * PUBLIC:
    *    This method is used to invalidate the identity maps in the session.
    */
    public void invalidateAllIdentityMaps() throws IOException;

    /**
    * PUBLIC:
    *    This method is used to invalidate the identity maps specified by the String[] of classNames.
    *
    * @param classNames String[] of fully qualified classnames identifying the identity maps to invalidate
    * @param recurse    Boolean indicating if we want to invalidate the children identity maps too
    */
    public void invalidateIdentityMaps(String[] classNamesParam, Boolean recurse) throws ClassNotFoundException, IOException;

    /**
    * PUBLIC:
    *    This method is used to invalidate the identity maps specified by className. This does not
    * invalidate the children identity maps
    *
    * @param className the fully qualified classname identifying the identity map to invalidate
    */
    public void invalidateIdentityMap(String className) throws ClassNotFoundException, IOException;

    /**
    * PUBLIC:
    *    This method is used to invalidate the identity maps specified by className.
    *
    * @param className the fully qualified classname identifying the identity map to invalidate
    * @param recurse    Boolean indicating if we want to invalidate the children identity maps too
    */
    public void invalidateIdentityMap(String className, Boolean recurse) throws ClassNotFoundException, IOException;

    /**
    * PUBLIC:
    *    This method is used to answer whether cache synchronization is enabled or not.
    */
    public Boolean getCacheSynchEnabled() throws IOException;

    /**
    * PUBLIC:
    *    This method is used to answer whether cache synchronization is asynchronous or not.
    */
    public Boolean getCacheSynchAsynchronous() throws IOException;



}
