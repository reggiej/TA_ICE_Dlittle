// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration;

import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.ox.XMLDescriptor;
import oracle.toplink.ox.mappings.XMLDirectMapping;
import oracle.toplink.ox.mappings.nullpolicy.NullPolicy;
import oracle.toplink.ox.schema.XMLSchemaClassPathReference;
import oracle.toplink.tools.sessionconfiguration.model.platform.oc4j.Oc4j_11_1_1_PlatformConfig;
import oracle.toplink.tools.sessionconfiguration.model.platform.SunAS9PlatformConfig;
import oracle.toplink.tools.sessionconfiguration.model.platform.WebLogic_10_PlatformConfig;
import oracle.toplink.tools.sessionconfiguration.model.platform.WebLogic_9_PlatformConfig;
import oracle.toplink.tools.sessionconfiguration.model.platform.WebSphere_6_1_PlatformConfig;
import oracle.toplink.tools.sessionconfiguration.model.transport.Oc4jJGroupsTransportManagerConfig;
import oracle.toplink.tools.sessionconfiguration.model.transport.TransportManagerConfig;

/**
 * INTERNAL:
 * OX mapping project for the 11gR1 sessions XML schema.
 * This subclasses the 10.1.3 project and adds any changes.
 */
public class XMLSessionConfigProject_11_1_1 extends XMLSessionConfigProject {
    // Default null values
    public static final boolean BIND_ALL_PARAMETERS_DEFAULT = true;
    public static final boolean USE_SINGLE_THREADED_NOTIFICATION_DEFAULT = false;

    public XMLSessionConfigProject_11_1_1() {
        super();
        addDescriptor(buildOc4jJGroupsTransportManagerConfigDescriptor());
        addDescriptor(buildServerPlatformConfigDescriptorFor(Oc4j_11_1_1_PlatformConfig.class));
    	addDescriptor(buildServerPlatformConfigDescriptorFor(SunAS9PlatformConfig.class));
        addDescriptor(buildServerPlatformConfigDescriptorFor(WebLogic_9_PlatformConfig.class));
        addDescriptor(buildServerPlatformConfigDescriptorFor(WebLogic_10_PlatformConfig.class));
        addDescriptor(buildServerPlatformConfigDescriptorFor(WebSphere_6_1_PlatformConfig.class));
    }

    public ClassDescriptor buildTopLinkSessionsDescriptor() {
        XMLDescriptor descriptor = (XMLDescriptor)super.buildTopLinkSessionsDescriptor();
        descriptor.setSchemaReference(new XMLSchemaClassPathReference("xsd/sessions_11_1_1.xsd"));

        return descriptor;
    }
    
    public ClassDescriptor buildDatabaseLoginConfigDescriptor() {
        ClassDescriptor descriptor = super.buildDatabaseLoginConfigDescriptor();

        XMLDirectMapping bindAllParametersMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("m_bindAllParameters");
        bindAllParametersMapping.setNullValue(new Boolean(BIND_ALL_PARAMETERS_DEFAULT));
        
        XMLDirectMapping validateConnectionHealthOnErrorMapping = new XMLDirectMapping();
        validateConnectionHealthOnErrorMapping.setAttributeName("connectionHealthValidatedOnError");
        validateConnectionHealthOnErrorMapping.setGetMethodName("isConnectionHealthValidatedOnError");
        validateConnectionHealthOnErrorMapping.setSetMethodName("setConnectionHealthValidatedOnError");
        validateConnectionHealthOnErrorMapping.setXPath("toplink:connection-health-validated-on-error/text()");
        validateConnectionHealthOnErrorMapping.setNullPolicy(new NullPolicy(null, false, false, false));
        validateConnectionHealthOnErrorMapping.setNullValue(true);
        descriptor.addMapping(validateConnectionHealthOnErrorMapping);

        XMLDirectMapping delayBetweenReconnectAttempts = new XMLDirectMapping();
        delayBetweenReconnectAttempts.setAttributeName("delayBetweenConnectionAttempts");
        delayBetweenReconnectAttempts.setGetMethodName("getDelayBetweenConnectionAttempts");
        delayBetweenReconnectAttempts.setSetMethodName("setDelayBetweenConnectionAttempts");
        delayBetweenReconnectAttempts.setXPath("toplink:delay-between-reconnect-attempts/text()");
        delayBetweenReconnectAttempts.setNullPolicy(new NullPolicy(null, false, false, false));
        descriptor.addMapping(delayBetweenReconnectAttempts);

        XMLDirectMapping queryRetryAttemptCount = new XMLDirectMapping();
        queryRetryAttemptCount.setAttributeName("queryRetryAttemptCount");
        queryRetryAttemptCount.setGetMethodName("getQueryRetryAttemptCount");
        queryRetryAttemptCount.setSetMethodName("setQueryRetryAttemptCount");
        queryRetryAttemptCount.setXPath("toplink:query-retry-attempt-count/text()");
        queryRetryAttemptCount.setNullPolicy(new NullPolicy(null, false, false, false));
        descriptor.addMapping(queryRetryAttemptCount);

        XMLDirectMapping pingSQLMapping = new XMLDirectMapping();
        pingSQLMapping.setAttributeName("pingSQL");
        pingSQLMapping.setGetMethodName("getPingSQL");
        pingSQLMapping.setSetMethodName("setPingSQL");
        pingSQLMapping.setXPath("toplink:ping-sql/text()");
        pingSQLMapping.setNullPolicy(new NullPolicy(null, false, false, false));
        descriptor.addMapping(pingSQLMapping);


        return descriptor;
    }

    public ClassDescriptor buildOc4jJGroupsTransportManagerConfigDescriptor() {
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(Oc4jJGroupsTransportManagerConfig.class);
        descriptor.getInheritancePolicy().setParentClass(TransportManagerConfig.class);

        XMLDirectMapping useSingleThreadedNotificationMapping = new XMLDirectMapping();
        useSingleThreadedNotificationMapping.setAttributeName("m_useSingleThreadedNotification");
        useSingleThreadedNotificationMapping.setGetMethodName("useSingleThreadedNotification");
        useSingleThreadedNotificationMapping.setSetMethodName("setUseSingleThreadedNotification");
        useSingleThreadedNotificationMapping.setXPath("use-single-threaded-notification/text()");
        useSingleThreadedNotificationMapping.setNullValue(new Boolean(USE_SINGLE_THREADED_NOTIFICATION_DEFAULT));
        descriptor.addMapping(useSingleThreadedNotificationMapping);

        XMLDirectMapping topicNameMapping = new XMLDirectMapping();
        topicNameMapping.setAttributeName("m_topicName");
        topicNameMapping.setGetMethodName("getTopicName");
        topicNameMapping.setSetMethodName("setTopicName");
        topicNameMapping.setXPath("topic-name/text()");
        descriptor.addMapping(topicNameMapping);

        return descriptor;
    }

    public ClassDescriptor buildTransportManagerConfigDescriptor() {
        XMLDescriptor descriptor = (XMLDescriptor)super.buildTransportManagerConfigDescriptor();
        descriptor.getInheritancePolicy().addClassIndicator(Oc4jJGroupsTransportManagerConfig.class, "oc4j-jgroups-transport");

        return descriptor;
    }
    
    public ClassDescriptor buildServerPlatformConfigDescriptor() {
        XMLDescriptor descriptor =(XMLDescriptor)super.buildServerPlatformConfigDescriptor();
        descriptor.getInheritancePolicy().addClassIndicator(Oc4j_11_1_1_PlatformConfig.class, "oc4j-1111-platform");
        descriptor.getInheritancePolicy().addClassIndicator(SunAS9PlatformConfig.class, "sunas-9-platform");
        descriptor.getInheritancePolicy().addClassIndicator(WebLogic_9_PlatformConfig.class, "weblogic-9-platform");
        descriptor.getInheritancePolicy().addClassIndicator(WebLogic_10_PlatformConfig.class, "weblogic-10-platform");
        descriptor.getInheritancePolicy().addClassIndicator(WebSphere_6_1_PlatformConfig.class, "websphere-6-1-platform");
	
        return descriptor;
    }
}
