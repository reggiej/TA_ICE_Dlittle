// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.exceptions;

import oracle.toplink.exceptions.i18n.ExceptionMessageGenerator;

/**
 * Instances of this exception are raised if a problem is detected during the
 * discovery of a TopLink cluster. This occurs as part of the RemoteCommandManager
 * feature.
 *
 * TopLink exceptions should only ever be thrown by TopLink code.
 */
public class DiscoveryException extends oracle.toplink.exceptions.RemoteCommandManagerException implements java.io.Serializable {
    // Take the first 100 in the RCM 22000 - 23000 range 
    public static final int ERROR_JOINING_MULTICAST_GROUP = 22001;
    public static final int ERROR_SENDING_ANNOUNCEMENT = 22002;
    public static final int ERROR_LOOKING_UP_LOCAL_HOST = 22003;
    public static final int ERROR_RECEIVING_ANNOUNCEMENT = 22004;

    public DiscoveryException() {
        super();
    }

    public DiscoveryException(String theMessage) {
        super(theMessage);
    }

    public static DiscoveryException errorJoiningMulticastGroup(Exception internalEx) {
        Object[] args = {  };
        DiscoveryException ex = new DiscoveryException(ExceptionMessageGenerator.buildMessage(DiscoveryException.class, ERROR_JOINING_MULTICAST_GROUP, args));
        ex.setErrorCode(ERROR_JOINING_MULTICAST_GROUP);
        ex.setInternalException(internalEx);
        return ex;
    }

    public static DiscoveryException errorSendingAnnouncement(Exception internalEx) {
        Object[] args = {  };
        DiscoveryException ex = new DiscoveryException(ExceptionMessageGenerator.buildMessage(DiscoveryException.class, ERROR_SENDING_ANNOUNCEMENT, args));
        ex.setErrorCode(ERROR_SENDING_ANNOUNCEMENT);
        ex.setInternalException(internalEx);
        return ex;
    }

    public static DiscoveryException errorLookingUpLocalHost(Exception internalEx) {
        Object[] args = {  };
        DiscoveryException ex = new DiscoveryException(ExceptionMessageGenerator.buildMessage(DiscoveryException.class, ERROR_LOOKING_UP_LOCAL_HOST, args));
        ex.setErrorCode(ERROR_LOOKING_UP_LOCAL_HOST);
        ex.setInternalException(internalEx);
        return ex;
    }

    public static DiscoveryException errorReceivingAnnouncement(Exception internalEx) {
        Object[] args = {  };
        DiscoveryException ex = new DiscoveryException(ExceptionMessageGenerator.buildMessage(DiscoveryException.class, ERROR_RECEIVING_ANNOUNCEMENT, args));
        ex.setErrorCode(ERROR_RECEIVING_ANNOUNCEMENT);
        ex.setInternalException(internalEx);
        return ex;
    }
}