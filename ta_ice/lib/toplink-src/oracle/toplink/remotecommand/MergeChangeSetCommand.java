// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.remotecommand;

import oracle.toplink.exceptions.CommunicationException;
import oracle.toplink.internal.sessions.*;
import oracle.toplink.internal.helper.Helper;

/**
 * <p>
 * <b>Purpose</b>: Provide a remote command implementation for remote cache
 * merges of changes.
 * <p>
 * <b>Description</b>: This command provides the implementation for cache
 * synchronization using RCM.
 * <P>
 * @author Steven Vo
 * @since OracleAS TopLink 10<i>g</i> (9.0.4)
 *
 */
public class MergeChangeSetCommand extends Command {

    /** The changes to be applied remotely */
    protected transient UnitOfWorkChangeSet changeSet;
    protected byte[] changeSetBytes;

    /**
     * INTERNAL:
     * Return the changes to be applied
     */
    public UnitOfWorkChangeSet getChangeSet() {
        if ((changeSet == null) && (changeSetBytes != null)) {
            try {
                changeSet = new UnitOfWorkChangeSet(changeSetBytes);
            } catch (java.io.IOException exception) {
                throw CommunicationException.unableToPropagateChanges(getServiceId().toString(), exception);
            } catch (ClassNotFoundException exception) {
                throw CommunicationException.unableToPropagateChanges(getServiceId().toString(), exception);
            }
        }
        return changeSet;
    }

    /**
     * INTERNAL:
     * Set the changes to be applied
     */
    public void setChangeSet(UnitOfWorkChangeSet newChangeSet) {
        changeSet = newChangeSet;
    }

    /**
     * INTERNAL:
     * Custom serialize this change set by converting it to a byte array.
     * @return false if converted byte array is null.  Otherwise, return true.
     */
    public boolean convertChangeSetToByteArray(AbstractSession session) throws java.io.IOException {
        changeSetBytes = changeSet.getByteArrayRepresentation(session);
        return changeSetBytes != null;
    }

    /**
     * INTERNAL:
     * This method will be invoked by the RCM only when the CommandProcessor is a
     * TopLink session. The session will be passed in for the command to use.
     */
    public void executeWithSession(AbstractSession session) {
        MergeManager manager = new MergeManager(session);
        manager.mergeIntoDistributedCache();
        manager.setCascadePolicy(MergeManager.CASCADE_ALL_PARTS);

        // Do the main merge
        manager.mergeChangesFromChangeSet(getChangeSet());
    }

    /**
     * INTERNAL:
     * This method is used by SDK project to convert this command to XML
     */
    public String getIdForSDK() {
        // Use class name as identifier since there is always one command is process with an empty cache at a time
        return Helper.getShortClassName(this.getClass());
    }

    /**
     * INTERNAL:
     * This method is used by SDK project to convert this command to XML
     */
    public void setIdForSDK(String id) {
        // No operation - getter awlays returns the class name
    }
}