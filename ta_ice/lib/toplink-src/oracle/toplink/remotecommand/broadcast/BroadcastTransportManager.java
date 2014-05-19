// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.remotecommand.broadcast;

import oracle.toplink.internal.remotecommand.RemoteConnection;

import oracle.toplink.remotecommand.DiscoveryManager;
import oracle.toplink.remotecommand.RemoteCommandManager;
import oracle.toplink.remotecommand.ServiceId;
import oracle.toplink.remotecommand.TransportManager;

/**
 * <p>
 * <b>Purpose</b>: Base class transport manager for broadcasting RCM protocols: JMS and Oc4jJGroups.
 * <p>
 * <b>Description</b>: The class doesn't use DiscoveryManager - instead 
 * the class (and it's ancestors) implement createConnections method
 * that creates all the necessary connections. This method is called
 * by RemoteCommendManager during initialization.
 * <p>
 * @author Andrei Ilitchev
 * @since OracleAS TopLink 11<i>g</i> (11.1.1)
 */
public abstract class BroadcastTransportManager extends TransportManager {
    
    // JNDI topic name
    protected String topicName;

    public BroadcastTransportManager(RemoteCommandManager rcm) {
        this.rcm = rcm;
        rcm.setTransportManager(this);
        this.initialize();
    }

    /**
     * ADVANCED:
     * BroadcastTransportManager doesn't use DiscoveryManager - instead RemoteCommandManager
     * calls createConnections method during initialization.
     */
    public DiscoveryManager createDiscoveryManager() {
        return null;
    }

    /**
     * INTERNAL:
     * BroadcastTransportManager doesn't use DiscoveryManager
     * (createDiscoveryManager method retuns null) therefore
     * this method called during RCM initialization to create all the necessary connections.
     * BroadcastTransportManager ancestors may need to override this method.
     */
    public void createConnections() {
        createLocalConnection();
    }

    /**
     * INTERNAL:
     * No-op implementation of super abstract method since there is only one connection to a known topic.
     */
    public RemoteConnection createConnection(ServiceId serviceId) {
        return null;
    }

    /**
     * INTERNAL:
     * Add a remote Connection to a remote service.
     */
    public void addConnectionToExternalService(RemoteConnection connection) {
        // nothing to do
    }

    /**
     * INTERNAL:
     * Prepare receiving messages by registering this connection as a listener to the Subscriber.
     * This method is called by the remote command manager when this service should connect back
     * ('handshake') to the service from which this remote connection came.
     */
    public void connectBackToRemote(RemoteConnection connection) throws Exception {
        // nothing to do
    }
    
    /**
     * PUBLIC:
     * Return the topic name that this TransportManager will be connecting to.
     */
    public String getTopicName() {
        return topicName;
    }
    
    /**
     * PUBLIC:
     * Configure the Topic name for the Topic that this TransportManager will be connecting to.
     * For some subclasses (JMS) this is a required setting and must be set;
     * for other (Oc4jJGroups) it's typically not set.
     */
    public void setTopicName(String newTopicName) {
        topicName = newTopicName;
    }
}
