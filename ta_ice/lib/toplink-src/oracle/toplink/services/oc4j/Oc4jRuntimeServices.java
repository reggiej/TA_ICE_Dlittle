// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.services.oc4j;

import java.util.*;
import java.util.regex.*;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanOperationInfo;
import javax.management.IntrospectionException;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.openmbean.*;

import oracle.dms.instrument.DMSConsole;
import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.internal.helper.*;
import oracle.toplink.internal.localization.ToplinkLocalization;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.internal.sessions.DatabaseSessionImpl;
import oracle.toplink.internal.helper.ClassConstants;
import oracle.toplink.sessions.DefaultConnector;
import oracle.toplink.tools.profiler.*;
import oracle.toplink.sessions.DatabaseLogin;
import oracle.toplink.threetier.*;
import oracle.toplink.internal.databaseaccess.DatabaseAccessor;
import oracle.toplink.logging.*;
import oracle.toplink.internal.identitymaps.*;
import oracle.toplink.platform.server.oc4j.OjdlLog;

/**
 * <p>
 * <b>Purpose</b>: Provide a dynamic interface into the TopLink Session.
 * <p>
 * <b>Description</b>: This class is meant to provide facilities for managing a TopLink session external
 * to TopLink over JMX.
 * @deprecated since 11.1.1
 */
public class Oc4jRuntimeServices extends oracle.oc4j.admin.management.mbeans.J2EEManagedObjectBase {

    /** stores access to the session object that we are controlling */
    protected AbstractSession session;

    /** This is the profile weight at server startup time. This is read-only */
    private int deployedSessionProfileWeight;

    /** This contains the session log from server startup time. This is read-only. */
    private SessionLog deployedSessionLog;

    public String objectName;

    /**
     * PUBLIC:
     *  Default Constructor
     */
    public Oc4jRuntimeServices() {
        super();
        init();
    }

    /**
     *  PUBLIC:
     *  Create an instance of Oc4jRuntimeServices to be associated with the provided session
     *
     *  @param session The session to be used with these RuntimeServices
     *  @param String myBaseObjectName: "oc4j:....." (The JMX object name before it's wrapped in a ObjectName)
     */
    public Oc4jRuntimeServices(AbstractSession session, String myBaseObjectName) {
        super();
        init();
        this.session = session;
        this.updateDeploymentTimeData();

        this.setBaseObjectName(myBaseObjectName);
    }

    /**
     *  PUBLIC:
     *  Create an instance of Oc4jRuntimeServices to be associated with the provided locale
     *
     *  The user must call setSession(Session) afterwards to define the session.
     */
    public Oc4jRuntimeServices(Locale locale) {
        super(locale);
        init();
    }

    /**
     *  INTERNAL:
     *  Define the session that this instance is providing runtime services for
     *
     *  @param Session session The session to be used with these RuntimeServices
     */
    protected void setSession(AbstractSession newSession) {
        this.session = newSession;

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
    public String getsessionName() {
        return getSession().getName();
    }

    /**
     * PUBLIC: Answer the type of the TopLink session this MBean represents.
     * Types include: "ServerSession", "DatabaseSession", "SessionBroker"
     */
    public String getsessionType() {
        return Helper.getShortClassName(getSession().getClass());
    }

    /**
     * PUBLIC: Provide an instance of 2 Dimensional Array simulating tabular format information about all
     * classes in the session whose class names match the provided filter.
     *
     * The 2 Dimensional array contains each item with values being row object array. Each row object array 
     * represents toplink class details info with respect to below attributes:  
     * ["Class Name", "Parent Class Name",  "Cache Type", "Configured Size", "Current Size"]
     *
     */
    public Object[][] getClassSummaryDetailsUsingFilter(String filter){
        //bug6087185, this method is created to resolve the Toplink JMX test suites broke issue due to
        //OC4J JMX framework 11 not support the direct access of TabularData. The functionality of this method 
        //has not been verified in EM11 yet, therefore, it needs to be changed potentially once we have enough 
        //requirement from EM.
        try{
           return  tabularDataTo2DArray(buildClassSummaryDetailsUsingFilter(filter),new String[] { "Class Name", "Parent Class Name", "Cache Type", "Configured Size", "Current Size" });
        }catch(Exception exception){
           AbstractSessionLog.getLog().log(SessionLog.SEVERE, "oc4jruntime_exception", exception); 
        }
        return null;
    }
    
    
    /**
     * PUBLIC: Provide an instance of 2 Dimensional Array simulating tabular format information about all
     * classes in the session.
     *
     * The 2 Dimensional array contains each item with values being row object array. Each row object array 
     * represents toplink class details info with respect to below attributes:  
     * ["Class Name", "Parent Class Name",  "Cache Type", "Configured Size", "Current Size"]
     *
     */
    public Object[][] getClassSummaryDetails() {
        //bug6087185, this method is created to resolve the Toplink JMX test suites broke issue due to
        //OC4J JMX framework 11 not support the direct access of TabularData. The functionality of this method 
        //has not been verified in EM11 yet, therefore, it needs to be changed potentially once we have enough 
        //requirement from EM.
        try{
           return tabularDataTo2DArray(buildClassSummaryDetails(),new String[] { "Class Name", "Parent Class Name", "Cache Type", "Configured Size", "Current Size" });
        }catch (Exception exception){
           AbstractSessionLog.getLog().log(SessionLog.SEVERE, "oc4jruntime_exception", exception);
        }
        return null;
    }
    
    /**
     * INTERNAL:
     * Provide an instance of TabularData containing information about the
     * classes in the session whose class names match the provided filter.
     *
     * The TabularData contains rowData with values being CompositeData(s)
     *
     * CompositeData has:
     *    CompositeType: column names are ["Class Name", "Parent Class Name",  "Cache Type", "Configured Size", "Current Size"]
     *
     *  Each CompositeData can have get(myColumnName) sent to it.
     *
     *
     * @parm filter A comma separated list of strings to match against.
     * @return A TabularData of information for the class names that match the filter.
     */
    private TabularData buildClassSummaryDetailsUsingFilter(String filter) {
        //if the filter is null, return all the details
        if (filter == null) {
            return buildClassSummaryDetails();
        }

        try {
            Vector mappedClassNames = getMappedClassNamesUsingFilter(filter);
            String mappedClassName;

            //build the 
            TabularDataSupport rowData = new TabularDataSupport(buildTabularTypeForClassSummaryDetails());

            //Check if there aren't any classes mapped
            if (mappedClassNames.size() == 0) {
                return null;
            }

            //get details for each class, and add the details to the summary
            for (int index = 0; index < mappedClassNames.size(); index++) {
                mappedClassName = (String)mappedClassNames.elementAt(index);
                String[] key = new String[] { mappedClassName };
                rowData.put(key, buildDetailsFor(mappedClassName, rowData.getTabularType().getRowType()));
            }

            return rowData;
        } catch (Exception exception) {
            AbstractSessionLog.getLog().log(SessionLog.SEVERE, "oc4jruntime_exception", exception);
        }

        //wait to get requirements from EM
        return null;
    }

    /**
     * PUBLIC: Provide an instance of TabularData containing information about all
     * classes in the session.
     *
     * The TabularData contains rowData with values being CompositeData(s)
     *
     * CompositeData has:
     *    CompositeType: column names are ["Class Name", "Parent Class Name",  "Cache Type", "Configured Size", "Current Size"]
     *
     *  Each CompositeData can have get(myColumnName) sent to it.
     *
     */
    private TabularData buildClassSummaryDetails() {
        try {
            Vector mappedClassNames = getMappedClassNames();
            String mappedClassName;

            //build the 
            TabularDataSupport rowData = new TabularDataSupport(buildTabularTypeForClassSummaryDetails());

            //Check if there aren't any classes mapped
            if (mappedClassNames.size() == 0) {
                return null;
            }

            //get details for each class, and add the details to the summary
            for (int index = 0; index < mappedClassNames.size(); index++) {
                mappedClassName = (String)mappedClassNames.elementAt(index);
                String[] key = new String[] { mappedClassName };
                rowData.put(key, buildDetailsFor(mappedClassName, rowData.getTabularType().getRowType()));
            }

            return rowData;
        } catch (Exception exception) {
            AbstractSessionLog.getLog().log(SessionLog.SEVERE, "oc4jruntime_exception", exception);
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
    public Vector getMappedClassNamesUsingFilter(String filter) {
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
     * Answer the TabularType describing the TabularData that we return from
     * getCacheSummaryDetails() and getCacheSummaryDetails(String filter)
     *
     * This is mostly for the client side to see what kind of information is returned.
     *
     * @return javax.management.openmbean.TabularType
     */
    private TabularType buildTabularTypeForClassSummaryDetails() throws OpenDataException {
        return new TabularType(getsessionName(), "Session description", buildCompositeTypeForClassSummaryDetails(), new String[] { "Class Name" });
    }

    /**
     * INTERNAL:
     * Answer the CompositeType describing the CompositeData that we return for
     * each IdentityMap (or subclass).
     *
     * This is mostly for the client side to see what kind of information is returned.
     * @return javax.management.openmbean.CompositeType
     */
    private CompositeType buildCompositeTypeForClassSummaryDetails() throws OpenDataException {
        return new CompositeType("Class Details", "Details of class for Class Summary", new String[] { "Class Name", "Parent Class Name", "Cache Type", "Configured Size", "Current Size" }, new String[] { "Class Name", "Parent Class Name", "Cache Type", "Configured Size", "Current Size" }, new OpenType[] { SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING });
    }

    /**
     * INTERNAL:
     * Answer the CompositeData containing the cache details for the given mappedClassName
     * This uses a CompositeDataSupport, which implements CompositeData
     *
     * @param String mappedClassName: fullyQualified class name of the class
     * @param CompositeType detailsType: describes the format of the returned CompositeData

     * @return javax.management.openmbean.CompositeData
     */
    private CompositeData buildDetailsFor(String mappedClassName, CompositeType detailsType) throws Exception {
        return new CompositeDataSupport(detailsType, buildLowlevelDetailsFor(mappedClassName));
    }

    /**
     * INTERNAL:
     * Helper to build a HashMap to help in the construction of a CompositeData
     *
     * @param String mappedClassName: fullyQualified class name of the class

     * @return HashMap
     */
    private HashMap buildLowlevelDetailsFor(String mappedClassName) {
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

        HashMap details = new HashMap();

        details.put("Class Name", mappedClassName);
        details.put("Cache Type", (isChildDescriptor ? "" : cacheType));
        details.put("Configured Size", (isChildDescriptor ? "" : configuredSize));
        details.put("Current Size", currentSize);
        //If I have a parent class name, get it. Otherwise, leave blank
        if (descriptor.hasInheritance()) {
            if (descriptor.getInheritancePolicy().getParentDescriptor() != null) {
                parentClassName = descriptor.getInheritancePolicy().getParentClassName();
            }
        }
        details.put("Parent Class Name", parentClassName);

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
    public String getmoduleName() {
        return ((DatabaseSessionImpl)this.getSession()).getServerPlatform().getModuleName();
    }

    /**
     * PUBLIC: Answer the TopLink log level at deployment time. This is read-only.
     */
    public String getdeployedTopLinkLogLevel() {
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
    public String getcurrentTopLinkLogLevel() {
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
    public String getcurrentTopLinkLogLevel(String category) {
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
    public synchronized void setcurrentTopLinkLogLevel(String newLevel) {
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
    public synchronized String getprofilingType() {
        if (getusesTopLinkProfiling().booleanValue()) {
            return "TopLink";
        } else if (getusesDMSProfiling().booleanValue()) {
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
    public synchronized void setprofilingType(String profileType) {
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
        if (getusesTopLinkProfiling().booleanValue()) {
            return;
        }
        getSession().setProfiler(new PerformanceProfiler());
    }

    /**
    *     PUBLIC:
    *        This method is used to turn on DMS Performance Profiling
    */
    public void setuseDMSProfiling() {
        if (getusesDMSProfiling().booleanValue()) {
            return;
        }
        getSession().setProfiler(new DMSPerformanceProfiler(getSession()));
    }

    /**
    *     PUBLIC:
    *        This method answers true if TopLink Performance Profiling is on.
    */
    public Boolean getusesTopLinkProfiling() {
        return Boolean.valueOf(getSession().getProfiler() instanceof PerformanceProfiler);
    }

    /**
    *     PUBLIC:
    *        This method answers true if DMS Performance Profiling is on.
    */
    public Boolean getusesDMSProfiling() {
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
    *     This method is used to turn on Profile logging when using th Performance Profiler
    */
    public synchronized void setshouldLogPerformanceProfiler(Boolean shouldLogPerformanceProfiler) {
        if ((getSession().getProfiler() != null) && ClassConstants.PerformanceProfiler_Class.isAssignableFrom(getSession().getProfiler().getClass())) {
            ((PerformanceProfiler)getSession().getProfiler()).setShouldLogProfile(shouldLogPerformanceProfiler.booleanValue());
        }
    }

    /**
    * PUBLIC:
    *     Method indicates if Performace profile should be loged
    */
    public Boolean getshouldLogPerformanceProfiler() {
        if ((getSession().getProfiler() != null) && ClassConstants.PerformanceProfiler_Class.isAssignableFrom(getSession().getProfiler().getClass())) {
            return Boolean.valueOf(((PerformanceProfiler)getSession().getProfiler()).shouldLogProfile());
        }
        return Boolean.FALSE;
    }

    /**
     * PUBLIC:
     *     Method returns if all Parameters should be bound or not
     */
    public Boolean getshouldBindAllParameters() {
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
    public Integer getstringBindingSize() {
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
    public Boolean getusesBatchWriting() {
        return Boolean.valueOf(getSession().getDatasourceLogin().getPlatform().usesBatchWriting());
    }

    /**
      * PUBLIC:
      *        This method will return a long indicating the exact time in Milliseconds that the
    *   session connected to the database.
      */
    public Long gettimeConnectionEstablished() {
        return new Long(((DatabaseSessionImpl)getSession()).getConnectedTime());
    }

    /**
      * PUBLIC:
      *        This method will return if batchWriting is in use or not.
      */
    public Boolean getusesJDBCBatchWriting() {
        return Boolean.valueOf(getSession().getDatasourceLogin().getPlatform().usesJDBCBatchWriting());
    }

    /**
      * PUBLIC:
      *     Shows if Byte Array Binding is turned on or not
      */
    public Boolean getusesByteArrayBinding() {
        return Boolean.valueOf(getSession().getDatasourceLogin().getPlatform().usesByteArrayBinding());
    }

    /**
      * PUBLIC:
      *     Shows if native SQL is being used
      */
    public Boolean getusesNativeSQL() {
        return Boolean.valueOf(getSession().getDatasourceLogin().getPlatform().usesNativeSQL());
    }

    /**
      * PUBLIC:
      *     This method indicates if streams are being used for binding
      */
    public Boolean getusesStreamsForBinding() {
        return Boolean.valueOf(getSession().getDatasourceLogin().getPlatform().usesStreamsForBinding());
    }

    /**
      * PUBLIC:
      *     This method indicates if Strings are being bound
      */
    public Boolean getusesStringBinding() {
        if (!(getSession().getDatasourceLogin() instanceof DatabaseLogin)) {
            return Boolean.FALSE;
        }
        return Boolean.valueOf(((DatabaseLogin)getSession().getDatasourceLogin()).getPlatform().usesStringBinding());
    }

    /**
    * PUBLIC:
    *     Returns if statements should be cached or not
    */
    public Boolean getshouldCacheAllStatements() {
        if (!(getSession().getDatasourceLogin() instanceof DatabaseLogin)) {
            return Boolean.FALSE;
        }
        return Boolean.valueOf(((DatabaseLogin)getSession().getDatasourceLogin()).shouldCacheAllStatements());
    }

    /**
    * PUBLIC:
    *        Returns the statement cache size.  Only valid if statements are being cached
    */
    public Integer getstatementCacheSize() {
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
        getSession().getSessionLog().info("Statement cache cleared");
    }

    /**
    * PUBLIC:
    *        Method returns the value of the Sequence Preallocation size
    */
    public Integer getsequencePreallocationSize() {
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
                getSession().getSessionLog().info("Pool Name = " + poolNames.next());
            }
        } else {
            getSession().getSessionLog().info("No Connection Pools Available");
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
            getSession().getSessionLog().finest("No Classes in session");
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
            getSession().getSessionLog().info("Identity Map [" + className + "]does not exist");
            return;
        }

        //check if there are any objects in the identity map. Print if so.
        Enumeration objects = map.keys();
        if (!objects.hasMoreElements()) {
            getSession().getSessionLog().info("Identity Map [" + className + "]is empty");
        }

        CacheKey cacheKey;
        while (objects.hasMoreElements()) {
            cacheKey = (CacheKey)objects.nextElement();
            getSession().getSessionLog().info("Key [" + cacheKey.getKey().toString() + "] => Value [" + cacheKey.getObject().toString());
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
            getSession().getSessionLog().info("There are no Identity Maps in this session");
            return;
        }

        //get each identity map, and log the type
        for (int index = 0; index < classesRegistered.size(); index++) {
            registeredClassName = (String)classesRegistered.elementAt(index);
            registeredClass = (Class)getSession().getDatasourcePlatform().getConversionManager().convertObject(registeredClassName, ClassConstants.CLASS);
            IdentityMap map = getSession().getIdentityMapAccessorInstance().getIdentityMap(registeredClass);
            getSession().getSessionLog().info("Identity Map [" + registeredClassName + "] class = " + map.getClass());
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
            getSession().getSessionLog().info("There are no Identity Maps in this session");
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
    public Integer getnumberOfObjectsInAllIdentityMaps() {
        Vector classesRegistered = getSession().getIdentityMapAccessorInstance().getIdentityMapManager().getClassesRegistered();
        String registeredClassName;
        int sum = 0;

        //Check if there aren't any classes registered
        if (classesRegistered.size() == 0) {
            getSession().getSessionLog().info("There are no Identity Maps in this session");
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
    public Integer getnumberOfPersistentClasses() {
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
    *        This will log at the INFO level a summary of all elements in the profile.
    */
    public void printProfileSummary() {
        if (!this.getusesTopLinkProfiling().booleanValue()) {
            return;
        }
        PerformanceProfiler performanceProfiler = (PerformanceProfiler)getSession().getProfiler();
        getSession().getSessionLog().info(performanceProfiler.buildProfileSummary().toString());
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
    *        This will log at the INFO level a summary of all elements in the profile, categorized
    *        by Class.
    */
    public void printProfileSummaryByClass() {
        if (!this.getusesTopLinkProfiling().booleanValue()) {
            return;
        }
        PerformanceProfiler performanceProfiler = (PerformanceProfiler)getSession().getProfiler();

        //trim the { and } from the beginning at end, because they cause problems for the logger
        getSession().getSessionLog().info(trimProfileString(performanceProfiler.buildProfileSummaryByClass().toString()));
    }

    /**
    * PUBLIC:
    *        This method assumes TopLink Profiling (as opposed to Java profiling).
    *        This will log at the INFO level a summary of all elements in the profile, categorized
    *        by Query.
    */
    public void printProfileSummaryByQuery() {
        if (!this.getusesTopLinkProfiling().booleanValue()) {
            return;
        }
        PerformanceProfiler performanceProfiler = (PerformanceProfiler)getSession().getProfiler();
        getSession().getSessionLog().info(trimProfileString(performanceProfiler.buildProfileSummaryByQuery().toString()));
    }

    /**
    * PUBLIC:
    * Return the log type, either "TopLink",  "Java" or "Server"
    *
    * @return the log type
    */
    public String getlogType() {
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
    public String getdatabasePlatform() {
        return getSession().getDatasourcePlatform().getClass().getName();
    }

    /**
    *     PUBLIC:
    *        Return JDBCConnection detail information. This includes URL and datasource information.
    */
    public synchronized String getjdbcConnectionDetails() {
        return getSession().getLogin().getConnector().getConnectionDetails();
    }

    /**
    *     PUBLIC:
    *        Return connection pool type. Values include: "Internal", "External" and "N/A".
    */
    public synchronized String getconnectionPoolType() {
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
    public synchronized String getdriver() {
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
    public String getlogFilename() {
        if (this.getlogType().equals("TopLink")) {
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
    public synchronized String getdeployedProfileWeight() {
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
    public synchronized String getcurrentProfileWeight() {
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
    public synchronized void setcurrentProfileWeight(String weight) {
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
        getSession().getSessionLog().info("Identity Map [" + className + "] is initialized");
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
            getSession().getSessionLog().info("There are no Identity Maps in this session");
        }

        //get each identity map, and invalidate
        for (int index = 0; index < classesRegistered.size(); index++) {
            registeredClassName = (String)classesRegistered.elementAt(index);
            registeredClass = (Class)getSession().getDatasourcePlatform().getConversionManager().convertObject(registeredClassName, ClassConstants.CLASS);
            getSession().getIdentityMapAccessor().invalidateClass(registeredClass);
            getSession().getSessionLog().info("Identity Map [" + registeredClassName + "] is invalidated");
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
        getSession().getSessionLog().info("Identity Map [" + className + "] is invalidated");
    }

    /**
    * PUBLIC:
    *    This method is used to answer whether cache synchronization is enabled or not.
    */
    public Boolean getcacheSynchEnabled() {
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
    public Boolean getcacheSynchAsynchronous() {
        if (!getSession().hasCacheSynchronizationManager()) {
            return Boolean.FALSE;
        }
        return Boolean.valueOf(getSession().getCacheSynchronizationManager().isAsynchronous());
    }

    /**
    * INTERNAL:
    * Initialize the meta-data accessible from the client application.
    */
    private void init() {
        try {
            initWithExceptions();
        } catch (IntrospectionException introspectionException) {
            introspectionException.printStackTrace();
        } catch (NoSuchMethodException noSuchMethodException) {
            noSuchMethodException.printStackTrace();
        }
    }
    
    /**
     * 
    * INTERNAL:
     * Convert the TabularData to a two-dimensional array
     * @param tdata the TabularData to be converted
     * @param names the order of the columns
     * @return a two-dimensional array
     * @throws Exception
     */
    private Object[][] tabularDataTo2DArray(TabularData tdata, String[] names) throws Exception {
        if(tdata==null){
            return null;
        }
        Object[] rows = tdata.values().toArray();
        Object[][] data = new Object[rows.length][];

        for (int i=0; i<rows.length; i++) {
            data[i] = ((CompositeData) rows[i]).getAll(names);
        }
        return data;
    }


    /**
    * INTERNAL:
    * Initialize the meta-data accessible from the client application.
    * This is a helper, that throw exceptions. This is for readability of the
    * init()
    */
    private void initWithExceptions() throws NoSuchMethodException, IntrospectionException {
        //This is for declaring the parameters on the operations
        javax.management.MBeanParameterInfo paramInfo;

        // add attributes meta-data
        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("sessionName", "[Ljava.lang.String;", localize("session_name"), true, false, false));

        //Debugging
        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("classSummaryDetails", "javax.management.openmbean.TabularData", localize("class_summary_details"), true, false, false));

        MBeanParameterInfo filterParmInfo = new MBeanParameterInfo("filter", "java.lang.String", "Filter specification, separated by commas");
        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("getClassSummaryDetailsUsingFilter", localize("class_summary_details_using_filter"), new MBeanParameterInfo[] { filterParmInfo }, "javax.management.openmbean.TabularData", MBeanOperationInfo.ACTION_INFO));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("timeConnectionEstablished", "java.lang.Long", localize("time_connection_established"), true, false, false));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("sessionType", "java.lang.String", localize("session_type"), true, false, false));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("moduleName", "java.lang.String", localize("module_name"), true, false, false));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("driver", "java.lang.String", localize("driver"), true, false, false));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("deployedTopLinkLogLevel", "java.lang.String", localize("deployed_toplink_log_level"), true, false, false));

        MBeanParameterInfo categoryParmInfo = new MBeanParameterInfo("category", "java.lang.String", "Name of Category");
        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("getDeployedTopLinkLogLevel", localize("deployed_toplink_log_level"), new MBeanParameterInfo[] { categoryParmInfo }, "java.lang.String", MBeanOperationInfo.ACTION_INFO));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("currentTopLinkLogLevel", "java.lang.String", localize("current_toplink_log_level"), true, true, false));

        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("getCurrentTopLinkLogLevel", localize("get_current_toplink_log_level"), new MBeanParameterInfo[] { categoryParmInfo }, "java.lang.String", MBeanOperationInfo.ACTION_INFO));

        MBeanParameterInfo levelParamInfo = new MBeanParameterInfo("level", "java.lang.Integer", "LogLevel");

        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("setCurrentTopLinkLogLevel", localize("set_current_toplink_log_level"), new MBeanParameterInfo[] { levelParamInfo, categoryParmInfo }, "void", MBeanOperationInfo.ACTION));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("logType", "java.lang.String", localize("log_type"), true, false, false));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("logFilename", "java.lang.String", localize("log_filename"), true, false, false));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("databasePlatform", "java.lang.String", localize("database_platform"), true, false, false));

        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("printClassesInSession", localize("print_classes_in_session"), null,//null = no args
                                                                    "void", MBeanOperationInfo.ACTION));

        //Profiling
        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("printProfileSummary", localize("print_profile_summary"), null,//null = no args
                                                                    "void", MBeanOperationInfo.ACTION));

        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("printProfileSummaryByClass", localize("print_profile_summary_by_class"), null,//null = no args
                                                                    "void", MBeanOperationInfo.ACTION));

        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("printProfileSummaryByQuery", localize("print_profile_summary_by_query"), null,//null = no args
                                                                    "void", MBeanOperationInfo.ACTION));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("usesTopLinkProfiling", "java.lang.Boolean", localize("uses_toplink_profiling"), true, false, false));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("usesDMSProfiling", "java.lang.Boolean", localize("uses_dms_profiling"), true, false, false));

        MBeanParameterInfo profileTypeParamInfo = new MBeanParameterInfo("profileType", "java.lang.String", "profileType");

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("profilingType", "java.lang.String", localize("profiling_type"), true, true, false));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("deployedProfileWeight", "java.lang.String", localize("deployed_profile_weight"), true, false, false));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("currentProfileWeight", "java.lang.String", localize("current_profile_weight"), true, true, false));

        MBeanParameterInfo weightParamInfo = new MBeanParameterInfo("weight", "java.lang.Integer", "weight");

        MBeanParameterInfo shouldLogParamInfo = new MBeanParameterInfo("shouldLogPerformanceProfiler", "java.lang.Boolean", "ShouldLogPerformanceProfiler");
        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("shouldLogPerformanceProfiler", "java.lang.Boolean", localize("should_log_performance_profiler"), true, true, false));

        //Statements and Caching
        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("shouldCacheAllStatements", "java.lang.Boolean", localize("should_cache_all_statements"), true, false, false));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("statementCacheSize", "java.lang.Integer", localize("statement_cache_size"), true, false, false));

        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("clearStatementCache", localize("clear_statement_cache"), null, "void", MBeanOperationInfo.ACTION));

        //Sequencing
        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("sequencePreallocationSize", "java.lang.Integer", localize("sequence_preallocation_size"), true, false, false));

        //Connections and Pools
        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("printAvailableConnectionPools", localize("print_available_connection_pools"), null, "void", MBeanOperationInfo.ACTION));

        MBeanParameterInfo poolNameParamInfo = new MBeanParameterInfo("poolName", "java.lang.String", "poolName");
        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("getMaxSizeForPool", localize("get_max_size_for_pool"), new MBeanParameterInfo[] { poolNameParamInfo }, "java.lang.Integer", MBeanOperationInfo.ACTION_INFO));

        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("getMinSizeForPool", localize("get_min_size_for_pool"), new MBeanParameterInfo[] { poolNameParamInfo }, "java.lang.Integer", MBeanOperationInfo.ACTION_INFO));

        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("resetAllConnections", localize("reset_all_connections"), null, "void", MBeanOperationInfo.ACTION));

        //Identity Maps
        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("printAllIdentityMapTypes", localize("print_all_identity_map_types"), null, "void", MBeanOperationInfo.ACTION));

        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("printObjectsInIdentityMaps", localize("print_objects_in_identity_maps"), null, "void", MBeanOperationInfo.ACTION));

        MBeanParameterInfo classNameParamInfo = new MBeanParameterInfo("className", "java.lang.String", "className");
        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("printObjectsInIdentityMap", localize("print_objects_in_identity_map"), new MBeanParameterInfo[] { classNameParamInfo }, "void", MBeanOperationInfo.ACTION));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("numberOfPersistentClasses", "java.lang.Integer", localize("number_of_persistent_classes"), true, false, false));

        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("getNumberOfObjectsInIdentityMap", localize("get_number_of_objects_in_identity_map"), new MBeanParameterInfo[] { classNameParamInfo }, "java.lang.Integer", MBeanOperationInfo.ACTION));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("numberOfObjectsInAllIdentityMaps", "java.lang.Integer", localize("number_of_objects_in_all_identity_maps"), true, false, false));

        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("invalidateAllIdentityMaps", localize("invalidate_all_identity_maps"), null, "void", MBeanOperationInfo.ACTION));

        MBeanParameterInfo classNamesParamInfo = new MBeanParameterInfo("classNames", "[Ljava.lang.String;", "classNames");
        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("invalidateAllIdentityMaps", localize("invalidate_all_identity_maps"), new MBeanParameterInfo[] { classNamesParamInfo }, "void", MBeanOperationInfo.ACTION));

        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("invalidateIdentityMap", localize("invalidate_identity_map"), new MBeanParameterInfo[] { classNameParamInfo }, "void", MBeanOperationInfo.ACTION));

        MBeanParameterInfo recurseParamInfo = new MBeanParameterInfo("recurse", "java.lang.Boolean", "recurse");
        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("invalidateIdentityMaps", localize("invalidate_identity_maps"), new MBeanParameterInfo[] { classNamesParamInfo, recurseParamInfo }, "void", MBeanOperationInfo.ACTION));

        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("initializeAllIdentityMaps", localize("initialize_all_identity_maps"), null, "void", MBeanOperationInfo.ACTION));

        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("initializeIdentityMaps", localize("initialize_identity_maps"), new MBeanParameterInfo[] { classNamesParamInfo }, "void", MBeanOperationInfo.ACTION));

        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("initializeIdentityMap", localize("initialize_identity_map"), new MBeanParameterInfo[] { classNameParamInfo }, "void", MBeanOperationInfo.ACTION));

        //Locking
        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("printIdentityMapLocks", localize("print_identity_map_locks"), null, "void", MBeanOperationInfo.ACTION));

        this.addModelMBeanOperationInfo(new ModelMBeanOperationInfo("printIdentityMapLocks", localize("print_identity_map_locks"), new MBeanParameterInfo[] { classNameParamInfo }, "void", MBeanOperationInfo.ACTION));

        //Project
        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("shouldBindAllParameters", "java.lang.Boolean", localize("should_bind_all_parameters"), true, false, false));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("usesStringBinding", "java.lang.Boolean", localize("uses_string_binding"), true, false, false));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("usesBatchWriting", "java.lang.Boolean", localize("uses_batch_writing"), true, false, false));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("usesJDBCBatchWriting", "java.lang.Boolean", localize("uses_jdbc_batch_writing"), true, false, false));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("jdbcConnectionDetails", "java.lang.String", localize("jdbc_connection_details"), true, false, false));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("connectionPoolType", "java.lang.String", localize("connection_pool_type"), true, false, false));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("usesByteArrayBinding", "java.lang.Boolean", localize("uses_byte_array_binding"), true, false, false));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("usesNativeSQL", "java.lang.Boolean", localize("uses_native_sql"), true, false, false));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("usesStreamsForBinding", "java.lang.Boolean", localize("uses_streams_for_binding"), true, false, false));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("usesStringBinding", "java.lang.Boolean", localize("uses_string_binding"), true, false, false));

        //Cache Synch
        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("cacheSynchAsynchronous", "java.lang.Boolean", localize("cache_synch_asynchronous"), true, false, true));

        this.addModelMBeanAttributeInfo(new ModelMBeanAttributeInfo("cacheSynchEnabled", "java.lang.Boolean", localize("cache_synch_enabled"), true, false, true));
    }
    
    private String localize(String message){
        return ToplinkLocalization.buildMessage("JMXLocalization", message, new String[] {});
    }
}
