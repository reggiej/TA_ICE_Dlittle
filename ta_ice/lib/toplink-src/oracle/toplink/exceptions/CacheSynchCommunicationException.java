// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.exceptions;

import oracle.toplink.exceptions.i18n.ExceptionMessageGenerator;

/**
 * Instances of this exception are raised if a problem is detected
 * during synchronization of TopLink caches.
 * TopLink exceptions should only ever be thrown by TopLink code.
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.exceptions.RemoteCommandManagerException}
 */
public class CacheSynchCommunicationException extends oracle.toplink.exceptions.TopLinkException implements java.io.Serializable {
    // Note that the values not startingt with 80 are for intenal use in the exception and are not used as error
    // codes
    //Duplicate of 12001 in CommunicationException
    public static final int FAILED_TO_RECONNECT = 12001;

    //Duplicate of 15001 in SynchronizationException
    public static final int UNABLE_TO_PROPAGATE_CHANGES = 15001;

    public CacheSynchCommunicationException() {
        super();
    }

    public CacheSynchCommunicationException(String theMessage) {
        super(theMessage);
    }

    public static CacheSynchCommunicationException failedToReconnect(String sessionId, Exception internalEx) {
        CacheSynchCommunicationException ex;
        if (sessionId != null) {
            Object[] args = { sessionId };
            ex = new CacheSynchCommunicationException(ExceptionMessageGenerator.buildMessage(CacheSynchCommunicationException.class, FAILED_TO_RECONNECT, args));
        } else {
            Object[] args = {  };
            ex = new CacheSynchCommunicationException(ExceptionMessageGenerator.buildMessage(CacheSynchCommunicationException.class, FAILED_TO_RECONNECT, args));
        }
        ex.setErrorCode(FAILED_TO_RECONNECT);
        if (internalEx != null) {
            ex.setInternalException(internalEx);
        }
        return ex;
    }

    public static CacheSynchCommunicationException unableToPropagateChanges(String sessionId, Exception internalEx) {
        CacheSynchCommunicationException ex;
        Object[] args = { sessionId };
        ex = new CacheSynchCommunicationException(ExceptionMessageGenerator.buildMessage(CacheSynchCommunicationException.class, UNABLE_TO_PROPAGATE_CHANGES, args));
        ex.setErrorCode(UNABLE_TO_PROPAGATE_CHANGES);
        if (internalEx != null) {
            ex.setInternalException(internalEx);
        }
        return ex;
    }
}