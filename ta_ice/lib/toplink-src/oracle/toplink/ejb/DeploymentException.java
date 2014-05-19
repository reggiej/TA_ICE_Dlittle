// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ejb;

import oracle.toplink.exceptions.i18n.ExceptionMessageGenerator;

/**
 * INTERNAL:
 * 
 * <p>
 * This exception is raised if a problem is detected during deployment of an EJB. TopLink
 * exceptions should only ever be thrown by TopLink.
 * </p>
 */
public class DeploymentException extends oracle.toplink.exceptions.TopLinkException implements java.io.Serializable {
    public static final int NO_PROJECT_SPECIFIED = 14001;
    public static final int NO_SUCH_PROJECT_IDENTIFIER = 14003;
    public static final int ERROR_CREATING_CUSTOMIZATION = 14004;
    public static final int ERROR_RUNNING_CUSTOMIZATION = 14005;

    /**
     * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3) There is no direct replacement API.
     */
    public static final int ERROR_UNSUPPORTED_JDBC_LEVEL = 14006;
    public static final int ERROR_INVALID_TXN_ISOLATION = 14007;
    public static final int ERROR_INVALID_CACHE_USAGE = 14008;

    /**
     * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3) There is no direct replacement API.
     */
    public static final int ERROR_INVALID_UPDATE_PROPAGATION = 14010;
    public static final int ERROR_CONNECTING_TO_DATA_SOURCE = 14011;

    /**
     * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3) There is no direct replacement API.
     */
    public static final int NO_TOPLINKSDK_XERCES_JAR = 14012;

    /**
     * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3) There is no direct replacement API.
     */
    public static final int NO_XERCES_JAR = 14013;

    /**
     * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3) There is no direct replacement API.
     */
    public static final int NO_PROJECT_SPECIFIED2 = 14014;

    /**
     * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3) There is no direct replacement API.
     */
    public static final int NO_SUCH_PROJECT_IDENTIFIER2 = 14015;
    public static final int ERROR_CREATING_PROJECT = 14016;

    /**
     * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3) There is no direct replacement API.
     */
    public static final int ERROR_CONVERTING_PLATFORM = 14017;

    /**
     * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3) There is no direct replacement API.
     */
    public static final int NO_SESSION_ID_SPECIFIED = 14018;
    public static final int NO_DEPLOYMENT_DESCRIPTOR = 14019;
    public static final int ERROR_IN_DEPLOYMENT_DESCRIPTOR = 14020;

    /**
     * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3) There is no direct replacement API.
     */
    public static final int CANT_USE_LOCAL_RELATIONSHIPS = 14021;
    public static final int CANT_USE_REMOTE_RELATIONSHIPS = 14022;
    public static final int CANNOT_FIND_GENERATED_SUBCLASS = 14023;
    public static final int CANNOT_READ_TOPLINK_PROJECT = 14024;

    /**
     * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3) There is no direct replacement API.
     */
    public static final int ERROR_GETTING_MAPPING = 14025;

    // added indirection check for (9.0.4)
    public static final int MUST_USE_TRANSPARENT_INDIRECTION = 14026;
    public static final int MUST_USE_VALUEHOLDER = 14027;
    public static final int NO_SUCH_MAPPING = 14028;

    // ejb-jar.xml validation
    public static final int MISSING_FINDER_DEF = 14029;
    public static final int MISSING_EJB_SELECT_DEF = 14030;
    public static final int MISSING_11_CMP_FIELD = 14031;
    public static final int MISSING_20_CMP_FIELD = 14032;

    // descriptor validation
    public static final int MISSING_DESCRIPTOR = 14033;

    public DeploymentException() {
        super();
    }

    public DeploymentException(String theMessage) {
        super(theMessage);
    }

    public DeploymentException(String message, Exception internalException) {
        super(message, internalException);
    }

    public static DeploymentException errorConnectingToDataSource(String dataSourceName, Throwable e) {
        Object[] args = { dataSourceName, e };

        DeploymentException ex = new DeploymentException(ExceptionMessageGenerator.buildMessage(DeploymentException.class, ERROR_CONNECTING_TO_DATA_SOURCE, args));
        ex.setErrorCode(ERROR_CONNECTING_TO_DATA_SOURCE);
        ex.setInternalException(e);
        return ex;
    }

    public static DeploymentException errorCreatingCustomization(String className, Throwable e) {
        Object[] args = { className, CR, e };

        DeploymentException ex = new DeploymentException(ExceptionMessageGenerator.buildMessage(DeploymentException.class, ERROR_CREATING_CUSTOMIZATION, args));
        ex.setErrorCode(ERROR_CREATING_CUSTOMIZATION);
        ex.setInternalException(e);
        return ex;
    }

    public static DeploymentException errorCreatingProject(Throwable e) {
        Object[] args = { e };

        DeploymentException ex = new DeploymentException(ExceptionMessageGenerator.buildMessage(DeploymentException.class, ERROR_CREATING_PROJECT, args));
        ex.setErrorCode(ERROR_CREATING_PROJECT);
        ex.setInternalException(e);

        return ex;
    }

    public static DeploymentException errorRunningCustomization(String className, Throwable e) {
        Object[] args = { className, CR, e };

        DeploymentException ex = new DeploymentException(ExceptionMessageGenerator.buildMessage(DeploymentException.class, ERROR_RUNNING_CUSTOMIZATION, args));
        ex.setErrorCode(ERROR_RUNNING_CUSTOMIZATION);
        ex.setInternalException(e);
        return ex;
    }

    public static DeploymentException invalidCacheUsageString(String cacheUsageString) {
        Object[] args = { cacheUsageString };

        DeploymentException ex = new DeploymentException(ExceptionMessageGenerator.buildMessage(DeploymentException.class, ERROR_INVALID_CACHE_USAGE, args));
        ex.setErrorCode(ERROR_INVALID_CACHE_USAGE);
        return ex;
    }

    public static DeploymentException invalidTxnIsolation(String txnIsolationString) {
        Object[] args = { txnIsolationString };

        DeploymentException ex = new DeploymentException(ExceptionMessageGenerator.buildMessage(DeploymentException.class, ERROR_INVALID_TXN_ISOLATION, args));
        ex.setErrorCode(ERROR_INVALID_TXN_ISOLATION);
        return ex;
    }

    public static DeploymentException noProjectSpecified() {
        Object[] args = {  };

        DeploymentException ex = new DeploymentException(ExceptionMessageGenerator.buildMessage(DeploymentException.class, NO_PROJECT_SPECIFIED, args));
        ex.setErrorCode(NO_PROJECT_SPECIFIED);
        return ex;
    }

    public static DeploymentException noSuchProjectIdentifier(String projectIdentifer) {
        Object[] args = { projectIdentifer };

        DeploymentException ex = new DeploymentException(ExceptionMessageGenerator.buildMessage(DeploymentException.class, NO_SUCH_PROJECT_IDENTIFIER, args));
        ex.setErrorCode(NO_SUCH_PROJECT_IDENTIFIER);
        return ex;
    }

    public static DeploymentException noDeploymentDescriptor(String descName) {
        Object[] args = { descName };

        DeploymentException ex = new DeploymentException(ExceptionMessageGenerator.buildMessage(DeploymentException.class, NO_DEPLOYMENT_DESCRIPTOR, args));
        ex.setErrorCode(NO_DEPLOYMENT_DESCRIPTOR);
        return ex;
    }

    public static DeploymentException errorInDeploymentDescriptor(String descName, Throwable e) {
        Object[] args = { descName, e };

        DeploymentException ex = new DeploymentException(ExceptionMessageGenerator.buildMessage(DeploymentException.class, ERROR_IN_DEPLOYMENT_DESCRIPTOR, args));
        ex.setErrorCode(ERROR_IN_DEPLOYMENT_DESCRIPTOR);
        ex.setInternalException(e);
        return ex;
    }

    public static DeploymentException cantUseRemoteRelationships(String beanName) {
        Object[] args = { beanName };

        DeploymentException ex = new DeploymentException(ExceptionMessageGenerator.buildMessage(DeploymentException.class, CANT_USE_REMOTE_RELATIONSHIPS, args));
        ex.setErrorCode(CANT_USE_REMOTE_RELATIONSHIPS);
        return ex;
    }

    public static DeploymentException cannotFindGeneratedSubclass(String beanName) {
        Object[] args = { beanName };

        DeploymentException ex = new DeploymentException(ExceptionMessageGenerator.buildMessage(DeploymentException.class, CANNOT_FIND_GENERATED_SUBCLASS, args));
        ex.setErrorCode(CANNOT_FIND_GENERATED_SUBCLASS);
        return ex;
    }

    public static DeploymentException cannotReadTopLinkProject() {
        Object[] args = {  };

        DeploymentException ex = new DeploymentException(ExceptionMessageGenerator.buildMessage(DeploymentException.class, CANNOT_READ_TOPLINK_PROJECT, args));
        ex.setErrorCode(CANNOT_READ_TOPLINK_PROJECT);
        return ex;
    }

    public static DeploymentException mustUseTransparentIndirection(String attributeName, Class beanClass) {
        Object[] args = { attributeName, beanClass.getName() };

        DeploymentException ex = new DeploymentException(ExceptionMessageGenerator.buildMessage(DeploymentException.class, MUST_USE_TRANSPARENT_INDIRECTION, args));
        ex.setErrorCode(MUST_USE_TRANSPARENT_INDIRECTION);
        return ex;
    }

    public static DeploymentException mustUseValueHolder(String attributeName, Class beanClass) {
        Object[] args = { attributeName, beanClass.getName() };

        DeploymentException ex = new DeploymentException(ExceptionMessageGenerator.buildMessage(DeploymentException.class, MUST_USE_VALUEHOLDER, args));
        ex.setErrorCode(MUST_USE_VALUEHOLDER);
        return ex;
    }

    public static DeploymentException noSuchMapping(Class beanClass, String attributeName) {
        Object[] args = { beanClass.getName(), attributeName };

        DeploymentException ex = new DeploymentException(ExceptionMessageGenerator.buildMessage(DeploymentException.class, NO_SUCH_MAPPING, args));
        ex.setErrorCode(NO_SUCH_MAPPING);
        return ex;
    }

    // ejb-jar.xml validation
    public static DeploymentException missingFinderDefinition(String finder, String beanName) {
        Object[] args = { finder, beanName };

        DeploymentException ex = new DeploymentException(ExceptionMessageGenerator.buildMessage(DeploymentException.class, MISSING_FINDER_DEF, args));
        ex.setErrorCode(MISSING_FINDER_DEF);
        return ex;
    }

    public static DeploymentException missingEjbSelectDefinition(String finder, String beanName) {
        Object[] args = { finder, beanName };

        DeploymentException ex = new DeploymentException(ExceptionMessageGenerator.buildMessage(DeploymentException.class, MISSING_EJB_SELECT_DEF, args));
        ex.setErrorCode(MISSING_EJB_SELECT_DEF);
        return ex;
    }

    public static DeploymentException missing11CmpField(String cmpField, String beanName) {
        Object[] args = { cmpField, beanName };

        DeploymentException ex = new DeploymentException(ExceptionMessageGenerator.buildMessage(DeploymentException.class, MISSING_11_CMP_FIELD, args));
        ex.setErrorCode(MISSING_11_CMP_FIELD);
        return ex;
    }

    public static DeploymentException missing20CmpField(String cmpField, String beanName) {
        Object[] args = { cmpField, beanName };

        DeploymentException ex = new DeploymentException(ExceptionMessageGenerator.buildMessage(DeploymentException.class, MISSING_20_CMP_FIELD, args));
        ex.setErrorCode(MISSING_20_CMP_FIELD);
        return ex;
    }

    public static DeploymentException missingDescriptor(String beanName) {
        Object[] args = { beanName };

        DeploymentException ex = new DeploymentException(ExceptionMessageGenerator.buildMessage(DeploymentException.class, MISSING_DESCRIPTOR, args));
        ex.setErrorCode(MISSING_DESCRIPTOR);
        return ex;
    }
}