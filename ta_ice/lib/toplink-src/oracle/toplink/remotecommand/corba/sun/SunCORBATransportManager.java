// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.remotecommand.corba.sun;

import java.io.IOException;
import java.net.InetAddress;
import oracle.toplink.exceptions.RemoteCommandManagerException;
import oracle.toplink.remotecommand.RemoteCommandManager;
import oracle.toplink.internal.remotecommand.corba.*;
import oracle.toplink.internal.remotecommand.corba.sun.SunCORBAConnectionImpl;
import oracle.toplink.internal.remotecommand.corba.sun.SunCORBAConnectionHelper;
import oracle.toplink.remotecommand.corba.CORBATransportManager;

public class SunCORBATransportManager extends CORBATransportManager {
    public SunCORBATransportManager(RemoteCommandManager rcm) {
        super(rcm);
    }

    /**
     * INTERNAL:
     * Overwrite super method and return the default local URL .
     * i.e iiop://66.178.2.33:9090
     */
    public String getDefaultLocalUrl() {
        try {
            // Look up the local host name and paste it in a default URL
            String localHost = InetAddress.getLocalHost().getHostName();
            return "iiop://" + localHost + ":" + DEFAULT_URL_PORT;
        } catch (IOException exception) {
            throw RemoteCommandManagerException.errorGettingHostName(exception);
        }
    }

    public String getDefaultInitialContextFactoryName() {
        return "com.sun.jndi.cosnaming.CNCtxFactory";
    }

    /**
     * INTERNAL:
     * Implement abstract method that delegates the narrow call to the generated <code>SunCORBAConnectionHelper</code> class.
     *
     */
    public CORBAConnection narrow(org.omg.CORBA.Object object) {
        return (CORBAConnection)SunCORBAConnectionHelper.narrow(object);
    }

    /**
     * INTERNAL:
     * Implement abstract method.  The method returns a specific CORBA implementation instance that implements
     * <code>CORBAConnection</code> interface.
     *
     */
    public CORBAConnection buildCORBAConnection() {
        return new SunCORBAConnectionImpl(rcm);
    }
}