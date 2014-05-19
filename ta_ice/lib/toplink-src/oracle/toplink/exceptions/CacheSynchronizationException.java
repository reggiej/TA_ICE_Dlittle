// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.exceptions;

import java.util.Vector;
import oracle.toplink.internal.sessions.UnitOfWorkChangeSet;

/**
 * <p><b>Purpose</b>: This exception is used when an error occurs during cache
 * synchronization distribution in synchronous mode.
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.exceptions.RemoteCommandManagerException}
 */
public class CacheSynchronizationException extends TopLinkException {
    public Vector errors;
    public transient UnitOfWorkChangeSet changeSet;// do not send the changeset remotely

    public CacheSynchronizationException(Vector errors, UnitOfWorkChangeSet changeSet) {
        this.errors = errors;
        this.changeSet = changeSet;
    }

    /**
     * This is the change Set that was being merged
     */
    public oracle.toplink.internal.sessions.UnitOfWorkChangeSet getChangeSet() {
        return changeSet;
    }

    /**
     * The errors that occured
     */
    public java.util.Vector getErrors() {
        return errors;
    }
}