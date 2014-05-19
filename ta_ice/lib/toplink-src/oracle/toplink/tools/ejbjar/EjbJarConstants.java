// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;


/**
 * INTERNAL:
 * Define all of the tag names and constants contained in
 * the ejb-jar.xml deployment descriptor file. These constants
 * are defined by the EJB 2.0 specification and the EJB 2.0
 * deployment descriptor DTD.
 */
public interface EjbJarConstants {
    public static final String ABSTRACT_SCHEMA_NAME = "abstract-schema-name";
    public static final String ACKNOWLEDGE_MODE = "acknowledge-mode";
    public static final String ASSEMBLY_DESCRIPTOR = "assembly-descriptor";
    public static final String CASCADE_DELETE = "cascade-delete";
    public static final String CMP_FIELD = "cmp-field";
    public static final String CMP_VERSION = "cmp-version";
    public static final String CMR_FIELD = "cmr-field";
    public static final String CMR_FIELD_NAME = "cmr-field-name";
    public static final String CMR_FIELD_TYPE = "cmr-field-type";
    public static final String CONTAINER_TRANSACTION = "container-transaction";
    public static final String DESCRIPTION = "description";
    public static final String DESTINATION_TYPE = "destination-type";
    public static final String DISPLAY_NAME = "display-name";
    public static final String EJB_CLASS = "ejb-class";
    public static final String EJB_CLIENT_JAR = "ejb-client-jar";
    public static final String EJB_JAR = "ejb-jar";
    public static final String EJB_LINK = "ejb-link";
    public static final String EJB_LOCAL_REF = "ejb-local-ref";// added for PFD2
    public static final String EJB_NAME = "ejb-name";
    public static final String EJB_QL = "ejb-ql";
    public static final String EJB_REF = "ejb-ref";
    public static final String EJB_REF_NAME = "ejb-ref-name";
    public static final String EJB_REF_TYPE = "ejb-ref-type";
    public static final String EJB_RELATION = "ejb-relation";
    public static final String EJB_RELATIONSHIP_ROLE = "ejb-relationship-role";
    public static final String EJB_RELATIONSHIP_ROLE_NAME = "ejb-relationship-role-name";//
    public static final String EJB_RELATION_NAME = "ejb-relation-name";
    public static final String ENTERPRISE_BEANS = "enterprise-beans";
    public static final String ENTITY = "entity";
    public static final String ENV_ENTRY = "env-entry";
    public static final String ENV_ENTRY_NAME = "env-entry-name";
    public static final String ENV_ENTRY_TYPE = "env-entry-type";
    public static final String ENV_ENTRY_VALUE = "env-entry-value";
    public static final String EXCLUDE_LIST = "exclude-list";// added for PFD2
    public static final String FIELD_NAME = "field-name";
    public static final String HOME = "home";
    public static final String LARGE_ICON = "large-icon";
    public static final String LOCAL = "local";
    public static final String LOCAL_HOME = "local-home";
    public static final String MESSAGE_DRIVEN = "message-driven";
    public static final String MESSAGE_DRIVEN_DESTINATION = "message-driven-destination";
    public static final String MESSAGE_SELECTOR = "message-selector";
    public static final String METHOD = "method";
    public static final String METHOD_INTF = "method-intf";// added for PFD2
    public static final String METHOD_NAME = "method-name";
    public static final String METHOD_PARAM = "method-param";
    public static final String METHOD_PARAMS = "method-params";
    public static final String METHOD_PERMISSION = "method-permission";
    public static final String MULTIPLICITY = "multiplicity";
    public static final String PERSISTENCE_TYPE = "persistence-type";
    public static final String PRIMKEY_FIELD = "primkey-field";
    public static final String PRIM_KEY_CLASS = "prim-key-class";
    public static final String QUERY = "query";
    public static final String QUERY_METHOD = "query-method";
    public static final String REENTRANT = "reentrant";
    public static final String RELATIONSHIPS = "relationships";
    public static final String REMOTE = "remote";
    public static final String RESOURCE_ENV_REF = "resource-env-ref";
    public static final String RESOURCE_ENV_REF_NAME = "resource-env-ref-name";
    public static final String RESOURCE_ENV_REF_TYPE = "resource-env-ref-type";
    public static final String RESOURCE_REF = "resource-ref";
    public static final String RESULT_TYPE_MAPPING = "result-type-mapping";// added for PFD2
    public static final String RES_AUTH = "res-auth";
    public static final String RES_REF_NAME = "res-ref-name";
    public static final String RES_SHARING_SCOPE = "res-sharing-scope";
    public static final String RES_TYPE = "res-type";
    public static final String ROLE_LINK = "role-link";
    public static final String ROLE_NAME = "role-name";
    public static final String RELATIONSHIP_ROLE_SOURCE = "relationship-role-source";
    public static final String RUN_AS = "run-as";// added for PFD2
    public static final String SECURITY_IDENTITY = "security-identity";
    public static final String SECURITY_ROLE = "security-role";
    public static final String SECURITY_ROLE_REF = "security-role-ref";
    public static final String SESSION = "session";
    public static final String SESSION_TYPE = "session-type";
    public static final String SMALL_ICON = "small-icon";
    public static final String SUBSCRIPTION_DURABILITY = "subscription-durability";
    public static final String TRANSACTION_TYPE = "transaction-type";
    public static final String TRANS_ATTRIBUTE = "trans-attribute";
    public static final String UNCHECKED = "unchecked";// added for PFD2
    public static final String USE_CALLER_IDENTITY = "use-caller-identity";

    //	public static final String DEPENDENT = "dependent";
    //	public static final String DEPENDENTS = "dependents"; 
    //	public static final String DEPENDENT_CLASS = "dependent-class"; 
    //	public static final String DEPENDENT_NAME = "dependent-name"; 
    //	public static final String PK_FIELD = "pk-field"; 
    //	public static final String REMOTE_EJB_NAME = "remote-ejb-name"; 
    //	public static final String RUN_AS_SPECIFIED_IDENTITY = "run-as-specified-identity"; 
    // 	public static final String EJB_ENTITY_REF = "ejb-entity-ref"; 
    public static final String TRUE_VALUE = "True";
    public static final String FALSE_VALUE = "False";

    /*
     * Valid values for 'session-type' element
     */
    public static final String STATEFUL_TYPE = "Stateful";
    public static final String STATELESS_TYPE = "Stateless";

    /*
     * Valid values for 'transaction-type' element
     */
    public static final String BEAN_MANAGED = "Bean";
    public static final String CONTAINER_MANAGED = "Container";

    /*
     * Valid values for 'trans-attribute' element
     */
    public static final String NOT_SUPPORTED = "NotSupported";
    public static final String SUPPORTS = "Supports";
    public static final String REQUIRED = "Required";
    public static final String REQUIRES_NEW = "RequiresNew";
    public static final String MANDATORY = "Mandatory";
    public static final String NEVER = "Never";

    /*
     * Valid values for 'res-sharing-scope' element
     */
    public static final String SHAREABLE_SCOPE = "Shareable";
    public static final String UNSHAREABLE_SCOPE = "Unshareable";

    /*
     * Valid values for 'res-auth' element
     */
    public static final String APPLICATION_AUTH = "Application";
    public static final String CONTAINER_AUTH = "Container";

    /*
     * Valid values for 'acknowledge-mode' element
     */
    public static final String AUTO_ACKNOWLEDGE = "Auto-acknowledge";
    public static final String DUPS_OK_ACKNOWLEDGE = "Dups-ok-acknowledge";

    /*
     * Valid values for 'cmp-version' element
     */
    public static final String CMP_VERSION_1 = "1.x";
    public static final String CMP_VERSION_2 = "2.x";

    /*
     * Valid values for 'cmr-field-type' element
     */
    public static final String COLLECTION_TYPE = "java.util.Collection";
    public static final String SET_TYPE = "java.util.Set";

    /*
     * Valid values for 'destination-type' element
     */
    public static final String QUEUE_TYPE = "javax.jms.Queue";
    public static final String TOPIC_TYPE = "javax.jms.Topic";

    /*
     * Valid values for 'ejb-ref-type' element
     */
    public static final String ENTITY_REF_TYPE = "Entity";
    public static final String SESSION_REF_TYPE = "Session";

    /*
     * Valid values for 'env-entry-type' element
     */
    public static final String BOOLEAN_TYPE = "java.lang.Boolean";
    public static final String STRING_TYPE = "java.lang.String";
    public static final String INTEGER_TYPE = "java.lang.Integer";
    public static final String DOUBLE_TYPE = "java.lang.Double";
    public static final String BYTE_TYPE = "java.lang.Byte";
    public static final String SHORT_TYPE = "java.lang.Short";
    public static final String LONG_TYPE = "java.lang.Long";
    public static final String FLOAT_TYPE = "java.lang.Float";

    /*
     * Valid values for 'multiplicity' element
     */
    public static final String MULTIPLICITY_ONE = "One";
    public static final String MULTIPLICITY_MANY = "Many";

    /*
     * Valid values for 'subscription-durability' element
     */
    public static final String DURABLE = "Durable";
    public static final String NONDURABLE = "NonDurable";

    //xml schema support
    public static final String XMLNS = "xmlns";
    public static final String XMLNS_XSI = "xmlns:xsi";
    public static final String XSI_SCHEMALOCATION = "xsi:schemaLocation";
    public static final String VERSION = "version";
}