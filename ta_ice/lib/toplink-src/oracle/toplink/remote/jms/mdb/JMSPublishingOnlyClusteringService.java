// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.remote.jms.mdb;

import oracle.toplink.internal.remote.RemoteConnection;
import oracle.toplink.remote.jms.JMSClusteringService;
import oracle.toplink.sessions.Session;


public class JMSPublishingOnlyClusteringService extends JMSClusteringService {
    /**
     * PUBLIC:
     * Creates a JMSPublishingChangesOnlyClusteringService
     */
    public JMSPublishingOnlyClusteringService(Session session) {
        super(session);
    }

    /**
     * INTERNAL:
     * Initializes the clustering service.  Overwrite super method and not spawning thread
     */
    public void initialize() {
        run();
    }

    /**
     * INTERNAL:
     * This method is called by the cache synchronization manager when this server should
     * connect back ('handshake') to the server from which this remote connection came.
     */
    public void connectBackToRemote(RemoteConnection connection)
        throws Exception {
        // -- Do nothing.  This method is intended to set a JMS MessageListener that handle receving messages in the super class.
        // The sending messages part is expected to be handled by another source such as a MessageDrivenBean.
    }
}
