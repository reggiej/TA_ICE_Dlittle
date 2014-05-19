// Copyright (c) 1998, 2008, Oracle. All rights reserved.  
package oracle.toplink.remotecommand.rmi;

import java.io.IOException;
import javax.naming.Context;
import javax.rmi.PortableRemoteObject;
import java.rmi.Naming;
import java.net.InetAddress;
import java.rmi.NoSuchObjectException;
import java.rmi.server.UnicastRemoteObject;
import oracle.toplink.exceptions.RemoteCommandManagerException;
import oracle.toplink.internal.remotecommand.RemoteConnection;
import oracle.toplink.internal.remotecommand.rmi.*;
import oracle.toplink.remotecommand.TransportManager;
import oracle.toplink.remotecommand.RemoteCommandManager;
import oracle.toplink.remotecommand.ServiceId;

/**
 * <p>
 * <b>Purpose</b>: Provide an RMI transport implementation for RCM.
 * <p>
 * <b>Description</b>: This class manages the RMI remote connections to other
 * RCM service instances and posts the local RMI connection to this service instance
 * in a name service so that other RCM service instances can connect to it.
 * <p>
 * @author Steven Vo
 * @since OracleAS TopLink 10<i>g</i> (9.0.4)
 */
public class RMITransportManager extends TransportManager {

    /** Determines whether RMI over IIOP or not */
    public boolean isRMIOverIIOP;

    public RMITransportManager(RemoteCommandManager rcm) {
        this.rcm = rcm;
        this.initialize();
    }

    /**
     * INTERNAL:
     * Create and return an RMI remote connection to the specified service
     */
    public RemoteConnection createConnection(ServiceId connectionServiceId) {
        RemoteConnection connection = null;

        if (namingServiceType == REGISTRY_NAMING_SERVICE) {
            connection = createConnectionFromRegistry(connectionServiceId.getId(), connectionServiceId.getURL());

        } else if (namingServiceType == JNDI_NAMING_SERVICE) {
            connection = createConnectionFromJNDI(connectionServiceId.getId(), connectionServiceId.getURL());
        }
        if (connection != null) {
            connection.setServiceId(connectionServiceId);
        }
        return connection;
    }

    /**
     * INTERNAL:
     * Look the specified remote object up in JNDI and return a Connection to it.
     */
    protected RemoteConnection createConnectionFromJNDI(String remoteObjectIdentifier, String hostURL) {
        Object[] args = { remoteObjectIdentifier, hostURL };
        rcm.logDebug("looking_up_remote_conn_in_jndi", args);
        try {
            Context context = getRemoteHostContext(hostURL);

            //Use JNDI lookup(), rather than the RMI version, 
            //AND replace the Java remote interface cast with a call to javax.rmi.PortableRemoteObject.narrow(): 
            if (this.isRMIOverIIOP()) {
                return new RMIRemoteConnection((RMIRemoteCommandConnection)PortableRemoteObject.narrow(context.lookup(remoteObjectIdentifier), RMIRemoteCommandConnection.class));
            } else {
                return new RMIRemoteConnection((RMIRemoteCommandConnection)context.lookup(remoteObjectIdentifier));
            }
        } catch (Exception e) {
            try {
                rcm.handleException(RemoteCommandManagerException.errorLookingUpRemoteConnection(remoteObjectIdentifier, hostURL, e));
            } catch (Exception ex2) {
                 // Must catch this exception and log a debug message
                rcm.logDebug("unable_to_look_up_remote_conn_in_jndi", args);
            }
        }
        return null;
    }

    /**
     * INTERNAL:
     * Look the specified remote object up in the RMIRegistry and return a Connection to it.
     */
    protected RemoteConnection createConnectionFromRegistry(String remoteObjectIdentifier, String hostURL) {
        String formattedUrl = formatURLforRegistry(hostURL, remoteObjectIdentifier);
        Object[] args = { formattedUrl };
        rcm.logDebug("looking_up_remote_conn_in_registry", args);
        try {
            return new RMIRemoteConnection((RMIRemoteCommandConnection)Naming.lookup(formattedUrl));
        } catch (Exception e) {
            try {
                rcm.handleException(RemoteCommandManagerException.errorLookingUpRemoteConnection(remoteObjectIdentifier, hostURL, e));
            } catch (Exception ex2) {
                // Must catch this exception and log a debug message
                rcm.logDebug("unable_to_look_up_remote_conn_in_registry", args);
            }
        }
        return null;
    }

    /**
     * INTERNAL:
     * Create the local command connection for this transport in a naming service and
     * return it.
     */
    public void createLocalConnection() {
        if (namingServiceType == REGISTRY_NAMING_SERVICE) {
            createLocalConnectionInRegistry();

        } else if (namingServiceType == JNDI_NAMING_SERVICE) {
            createLocalConnectionInJNDI();
        }
        if (localConnection != null) {
            localConnection.setServiceId(rcm.getServiceId());
        }
    }

    /**
     * INTERNAL:
     * Put the local command connection of this transport in JNDI and return it
     */
    protected void createLocalConnectionInJNDI() {
        try {
            // Register the remote connection in JNDI naming service
            RMIRemoteCommandConnection remoteConnectionObject;
            if (this.isRMIOverIIOP()) {
                remoteConnectionObject = new oracle.toplink.internal.remotecommand.rmi.iiop.RMIRemoteCommandConnectionImpl(rcm);
            } else {
                remoteConnectionObject = new RMIRemoteCommandConnectionImpl(rcm);
            }
            Object[] args = { rcm.getServiceId().getId() };
            rcm.logDebug("register_local_connection_in_jndi", args);
            getLocalHostContext().rebind(rcm.getServiceId().getId(), remoteConnectionObject);
            localConnection = new RMIRemoteConnection(remoteConnectionObject);
        } catch (Exception exception) {
            rcm.handleException(RemoteCommandManagerException.errorBindingConnection(rcm.getServiceId().toString(), exception));
        }
    }

    /**
     * INTERNAL:
     * Put the local command connection of this transport in the Registry and return it
     */
    protected RemoteConnection createLocalConnectionInRegistry() {
        String fullURL = formatURLforRegistry(rcm.getServiceId().getURL(), rcm.getServiceId().getId());

        try {
            // Register the remote connection in RMI Registry naming service
            RMIRemoteCommandConnectionImpl remoteConnectionObject = new RMIRemoteCommandConnectionImpl(rcm);
            Object[] args = { fullURL };
            rcm.logDebug("register_local_connection_in_registry", args);
            Naming.rebind(fullURL, remoteConnectionObject);
            localConnection = new RMIRemoteConnection(remoteConnectionObject);
        } catch (Exception exception) {
            rcm.handleException(RemoteCommandManagerException.errorBindingConnection(fullURL, exception));
        }
        return localConnection;
    }

    /**
     * INTERNAL:
     * Return the context used for looking up in local JNDI.
     */
    public Context getLocalHostContext() {
        return getContext(getLocalContextProperties());
    }

    /**
     * INTERNAL:
     * Format the URL so that it can be used to look up the RMI Registry.
     */
    private String formatURLforRegistry(String url, String serviceName) {
        if (url == null) {
            return null;
        }
        String fullURL = url;

        if ((fullURL != null) && (fullURL.endsWith("/") || fullURL.endsWith("\\"))) {
            fullURL = fullURL.substring(0, fullURL.length() - 1);
        }
        return fullURL + "/" + serviceName;
    }

    /**
     * INTERNAL:
     * Return the default local URL for JNDI lookups
     */
    public String getDefaultLocalUrl() {
        try {
            // Look up the local host name and paste it in a default URL
            String localHost = InetAddress.getLocalHost().getHostName();
            if (this.isRMIOverIIOP()) {
                return DEFAULT_IIOP_URL_PROTOCOL + "::" + localHost + ":" + DEFAULT_IIOP_URL_PORT;
            } else {
                return DEFAULT_URL_PROTOCOL + "://" + localHost + ":" + DEFAULT_URL_PORT;
            }
        } catch (IOException exception) {
            throw RemoteCommandManagerException.errorGettingHostName(exception);
        }
    }

    /**
     * INTERNAL:
     * Initialize default properties for RMI.
     */
    public void initialize() {
        super.initialize();
        if (rcm.getServiceId().getURL() == null) {
            rcm.getServiceId().setURL(getDefaultLocalUrl());
        }
        namingServiceType = DEFAULT_NAMING_SERVICE;
    }

    /**
     * ADVANCED:
     * Remove the local connection from remote accesses.  The implementation removes the local connection from JNDI or RMI registry and set it to null.
     * This method is invoked internally by TopLink when the RCM is shutdown and should not be invoked by user's application.
     */
    public void removeLocalConnection() {
        String unbindName = null;
        try {
            if (namingServiceType == REGISTRY_NAMING_SERVICE) {
                unbindName = formatURLforRegistry(rcm.getServiceId().getURL(), rcm.getServiceId().getId());
                Naming.unbind(unbindName);
            } else if (namingServiceType == JNDI_NAMING_SERVICE) {
                unbindName = rcm.getServiceId().getId();
                getLocalHostContext().unbind(unbindName);
            } else {
                return;
            }
            // Bug 6788643 - unexport remote command connection from local RMI server
            if (getConnectionToLocalHost() != null) {
                RMIRemoteCommandConnection commandConnection = ((RMIRemoteConnection)getConnectionToLocalHost()).getConnection();
                if (commandConnection != null) {
                    try {
                        UnicastRemoteObject.unexportObject(commandConnection, true);
                    } catch (NoSuchObjectException nso) {
                        // if the object doesn't exist, ignore since we are removing the connection
                    }
                }
            }
        } catch (Exception exception) {
            rcm.handleException(RemoteCommandManagerException.errorUnbindingLocalConnection(unbindName, exception));
        }
        localConnection = null;
    }

    /**
     * INTERNAL
     * Check whether RMI over IIOP or not
     */
    public boolean isRMIOverIIOP() {
        return isRMIOverIIOP;
    }

    /** INTERNAL
     *  set RMI ocver IIOP
     */
    public void setIsRMIOverIIOP(boolean value) {
        this.isRMIOverIIOP = value;
    }
}