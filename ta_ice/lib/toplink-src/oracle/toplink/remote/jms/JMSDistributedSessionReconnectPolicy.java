// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.remote.jms;

import oracle.toplink.internal.remote.RemoteConnection;
import oracle.toplink.remote.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.logging.SessionLog;
import javax.naming.*;
import javax.jms.*;

/**
 * <p>
 * <b>PURPOSE</b>:To Provide policy for reconnecting distributed sessions for cache Synch</p>
 * <p>
 * <b>Descripton</b>:This class Defines the behavior for attempting to reconnect sessions.  It will
 * attempt to reconnect based on information from the old connection</p>
 *
 * @author Gordon Yorke
 * @see oracle.toplink.remote.jms.JMSClusteringService
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.remotecommand.jms.JMSTopicTransportManager}
 */
public class JMSDistributedSessionReconnectPolicy extends DistributedSessionReconnectPolicy {
    protected AbstractClusteringService clusteringService;

    public JMSDistributedSessionReconnectPolicy(AbstractClusteringService clusteringService) {
        this.clusteringService = clusteringService;
    }

    /**
     * PUBLIC:
     * This method is called by the Cache Synchronization manager when a connection to the remote
     * service or remote sessions fails and must be re-connected.  Overload this method to provide
     * custom behaviour.  by Default the behaviour is not to attempt reconnection.  This will be
     * taken care of by the Clustering Service when the remote announcemnet from the other server comes
     * in.
     */
    public RemoteConnection reconnect(RemoteConnection oldConnection) {
        ((oracle.toplink.internal.sessions.AbstractSession)clusteringService.getSession()).log(SessionLog.FINEST, SessionLog.PROPAGATION, "attempting_to_reconnect_to_JMS_service");
        try {
            return ((JMSClusteringService)clusteringService).createRemoteConnection();
        } catch (NamingException exception) {
            throw SynchronizationException.errorLookingUpJMSService(oldConnection.getServiceName(), exception);
        } catch (JMSException exception) {
            throw SynchronizationException.errorLookingUpJMSService(oldConnection.getServiceName(), exception);
        }
    }
}