// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.remote.rmi.wls;

import java.net.MulticastSocket;
import java.util.Hashtable;
import javax.naming.*;
import oracle.toplink.internal.remote.RemoteConnection;
import oracle.toplink.sessions.Session;
import oracle.toplink.exceptions.*;
import oracle.toplink.logging.SessionLog;

/**
 * <p>
 * <b>PURPOSE</b>:To Provide a framework for offering customers the ability to automatically
 * connect multiple sessions for synchrnization.</p>
 * <p>
 * <b>Descripton</b>:This thread object will place a remote dispatcher in the specified JNDI space.
 * it will also monitor the specified multicast socket to allow other sessions to connect.  This
 * Particular class has been configured to use the RMI transport protocols.  This class is only
 * valid for use in a clustered WebLogic application server.</p>
 *
 * @author Gordon Yorke
 * @see oracle.toplink.remote.CacheSynchronizationManager
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.remotecommand.rmi.RMITransportManager}
 */
public class WLSClusteringService extends oracle.toplink.remote.AbstractJNDIClusteringService {
    private static Class DEFAULT_DISPATCHER_CLASS = oracle.toplink.remote.rmi.RMIRemoteSessionControllerDispatcher.class;
    private static Class DEFAULT_CONNECTION_CLASS = oracle.toplink.remote.rmi.RMIConnection.class;

    /**
     * PUBLIC:
     * Creates a WLSClusteringService
     * @param jndiHostURL The URL of the JNDI service
     * @SBGen Constructor
     */
    public WLSClusteringService(Session session) {
        super(session);
        setAnnouncementDelay(30000);
    }

    /**
     * ADVANCED:
     *     This method should return a remote connection of the appropraite type for
     * use in the synchronizatio
     */
    public RemoteConnection createRemoteConnection(String sessionId, Object remoteObject) {
        RMIConnection connection = new RMIConnection((RMIRemoteSessionController)remoteObject);
        connection.setServiceName(sessionId);
        return connection;
    }

    /**
     * ADVANCED:
     *     This method should return a remote connection of the appropraite type for
     * use in the synchronizatio
     */
    public RemoteConnection createRemoteConnection(String sessionId, String jndiHostURL) {
        //not used
        return null;
    }

    /**
     * ADVANCED:
     * This method will register the dispatcher for this session in JNDI
     * on the specified host.  It must register the dispatcher under the SessionId
     * @param jndiHostURL This is the URL that will be used to register the synchronization service
     */
    public void registerDispatcher() {
        try {
            try {
                getLocalContext().createSubcontext("OracleTopLink");
            } catch (NameAlreadyBoundException exception) {
            }
            getLocalContext().rebind("OracleTopLink." + getSessionId(), getDispatcher());
            // must reset the context
            getLocalContext().close();
            setContext(null);
        } catch (Exception exception) {
            getSession().handleException(SynchronizationException.errorBindingController(getSessionId(), exception));
        }
    }

    /**
     * ADVANCED:
     * This method will deregister the dispatcher for this session from JNDI
     * on the specified host.  It must deregister the dispatcher under the SessionId
     */
    public void deregisterDispatcher() {
        //BUG 2700381: deregister from JNDI
        try {
            try {
                getLocalContext().createSubcontext("OracleTopLink");
            } catch (NameAlreadyBoundException exception) {
            }
            getLocalContext().unbind("OracleTopLink." + getSessionId());
            // must reset the context
            getLocalContext().close();
            setContext(null);
            RMIRemoteSessionControllerDispatcher dispatcher = (RMIRemoteSessionControllerDispatcher)getDispatcher();
            if (dispatcher != null) {
                dispatcher.unexportObject(dispatcher, true);
            }
        } catch (Exception exception) {
            getSession().handleException(SynchronizationException.errorBindingController(getSessionId(), exception));
        }
    }

    /**
     * ADVANCED:
     *      Returns the socket that will be used for the multicast communication.
     * By default this will be java.net.MulticastSocket
     * @SBGen Method get communicationSocket
     */
    public MulticastSocket getCommunicationSocket() {
        //not used
        return null;
    }

    /**
     * ADVANCED:
     * Returns the active JNDI Context to store the remote service in
     * no properties are required in WebLogic as all defaults should be globally available in
     * the VM
     * @SBGen Method get context
     */
    public Context getContext(Hashtable contextProperties) {
        try {
            Context ctx = new InitialContext();
            return ctx;
            // Do the client's work
        } catch (NamingException exception) {
            getSession().handleException(SynchronizationException.errorLookingUpController(contextProperties.toString(), exception));
        }
        return null;
    }

    /**
     * ADVANCED:
     * This is the object that will be placed in JNDI to provide remote synchronization services
     * @return
     * @SBGen Method get dispatcher
     */
    public Object getDispatcher() throws java.rmi.RemoteException {
        if (this.dispatcher == null) {
            this.dispatcher = new RMIRemoteSessionControllerDispatcher(getSession());
        }
        return this.dispatcher;
    }

    /**
     * This is the main execution method of this class.  It will create a socket to listen to and
     * register the dispatcher for this class in JNDI
     */
    public void run() {
        //Initialize the communication socket
        ((oracle.toplink.internal.sessions.AbstractSession)getSession()).log(SessionLog.FINEST, SessionLog.PROPAGATION, "initializing_local_discovery_communication_socket");
        setSessionId(buildSessionId());
        ((oracle.toplink.internal.sessions.AbstractSession)getSession()).log(SessionLog.FINEST, SessionLog.PROPAGATION, "place_local_remote_session_dispatcher_into_naming_service");
        registerDispatcher();

        getSession().getCacheSynchronizationManager().setSessionRemoteConnection(getLocalRemoteConnection());
        //Search for other Sessions
        try {
            sleep(getAnnouncementDelay());
        } catch (InterruptedException exception) {
        }
        retreiveRemoteSessions();

    }

    /**
     * ADVANCED:
     * This method should return a Remote Connection of the appropriate type that references the
     * Remote dispatcher for this Session
     */
    public RemoteConnection getLocalRemoteConnection() {
        try {
            RemoteConnection connection = new RMIConnection((RMIRemoteSessionControllerDispatcher)getDispatcher());
            connection.setServiceName(getSessionId());
            return connection;
        } catch (Exception exception) {
            return null;
        }
    }

    /**
     * INTERNAL:
     * Use this method to notify the SynchronizationManager that we have to connect to a new Session
     * that has just joined the network
     */
    public void retreiveRemoteSessions() {
        try {
            NamingEnumeration services = getLocalContext().listBindings("OracleTopLink");
            while (services.hasMoreElements()) {
                Binding binding = (Binding)services.nextElement();
                if (!binding.getName().equals(getSessionId())) {
                    RemoteConnection newConnection = createRemoteConnection(binding.getName(), binding.getObject());
                    getSession().getCacheSynchronizationManager().addRemoteConnection(newConnection);
                }
            }

            // release the contexts
            services.close();
            getLocalContext().close();
            setContext(null);
        } catch (NamingException exception) {
            getSession().handleException(SynchronizationException.errorLookingUpController("all", exception));
        }
    }
}