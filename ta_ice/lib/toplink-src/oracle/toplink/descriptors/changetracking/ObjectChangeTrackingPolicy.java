// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.descriptors.changetracking;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.List;

import oracle.toplink.internal.descriptors.changetracking.ObjectChangeListener;
import oracle.toplink.internal.descriptors.changetracking.AggregateObjectChangeListener;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.exceptions.DescriptorException;
import oracle.toplink.mappings.DatabaseMapping;

/**
 * PUBLIC:
 * A ObjectChangeTrackingPolicy allows an object to calculate for itself whether
 * it should has changed by implementing ChangeTracker.  Changed objects will
 * be processed in the UnitOfWork commit process to include any changes in the results of the
 * commit.  Unchanged objects will be ignored.
 * @see DeferredChangeDetectionPolicy
 * @see ChangeTracker
 */
public class ObjectChangeTrackingPolicy extends DeferredChangeDetectionPolicy {

    /**
     * INTERNAL:
     * This method is used to dissable changetracking temporarily
     */
    public void dissableEventProcessing(Object changeTracker) {
        ObjectChangeListener listener = (ObjectChangeListener)((ChangeTracker)changeTracker)._persistence_getPropertyChangeListener();
        if (listener != null) {
            listener.ignoreEvents();
        }
    }

    /**
     * INTERNAL:
     * This method is used to enable changetracking temporarily
     */
    public void enableEventProcessing(Object changeTracker) {
        ObjectChangeListener listener = (ObjectChangeListener)((ChangeTracker)changeTracker)._persistence_getPropertyChangeListener();
        if (listener != null) {
            listener.processEvents();
        }
    }

    /**
     * INTERNAL:
     * Return true if the Object should be compared, false otherwise.  In ObjectChangeTrackingPolicy or
     * AttributeChangeTracking Policy this method will return true if the object is new, if the object
     * is in the OptimisticReadLock list or if the listener.hasChanges() returns true.
     * @param object the object that will be compared
     * @param unitOfWork the active unitOfWork
     * @param descriptor the descriptor for the current object
     */
    public boolean shouldCompareForChange(Object object, UnitOfWorkImpl unitOfWork, ClassDescriptor descriptor) {
        //PERF: Breakdown the logic to have the most likely scenario checked first
        ObjectChangeListener listener = (ObjectChangeListener)((ChangeTracker)object)._persistence_getPropertyChangeListener();
        if ((listener != null) && listener.hasChanges()) {
            return true;
        }        
        if (unitOfWork.isObjectNew(object)) {
            return true;
        }
        Boolean optimisticRead = null;
        if (unitOfWork.hasOptimisticReadLockObjects()) {
            optimisticRead = (Boolean)unitOfWork.getOptimisticReadLockObjects().get(object);
            // Need to always compare/build change set for new objects and those that are being forced to 
            // updated (opt. read lock and forceUpdate)
            if (optimisticRead != null) {
                return true;
            }
        }
        if ((descriptor.getCMPPolicy() != null) && descriptor.getCMPPolicy().getForceUpdate()) {
            return true;
        }
        return false;
    }

    /**
     * INTERNAL:
     * This may cause a property change event to be raised to a listner in the case that a listener exists.
     * If there is no listener then this call is a no-op
     */
    public void raiseInternalPropertyChangeEvent(Object source, String propertyName, Object oldValue, Object newValue) {
        ObjectChangeListener listener = (ObjectChangeListener)((ChangeTracker)source)._persistence_getPropertyChangeListener();
        if (listener != null) {
            listener.internalPropertyChange(new PropertyChangeEvent(source, propertyName, oldValue, newValue));
        }
    }

    /**
     * INTERNAL:
     * Assign Changelistner to an aggregate object
     */
    public void setAggregateChangeListener(Object parent, Object aggregate, UnitOfWorkImpl uow, ClassDescriptor descriptor, String mappingAttribute) {
        ((ChangeTracker)aggregate)._persistence_setPropertyChangeListener(new AggregateObjectChangeListener((ObjectChangeListener)((ChangeTracker)parent)._persistence_getPropertyChangeListener(), mappingAttribute));
    }

    /**
     * INTERNAL:
     * Assign ObjectChangeListener to PropertyChangeListener
     */
    public PropertyChangeListener setChangeListener(Object clone, UnitOfWorkImpl uow, ClassDescriptor descriptor) {
        ObjectChangeListener listener = new ObjectChangeListener();
        ((ChangeTracker)clone)._persistence_setPropertyChangeListener(listener);
        return listener;
    }

    /**
     * INTERNAL:
     * Clear the changes in the ObjectChangeListener
     */
    public void clearChanges(Object clone, UnitOfWorkImpl uow, ClassDescriptor descriptor) {
        ObjectChangeListener listener = (ObjectChangeListener)((ChangeTracker)clone)._persistence_getPropertyChangeListener();
        if (listener != null) {
            listener.clearChanges();
        } else {
            listener = (ObjectChangeListener)setChangeListener(clone, uow, descriptor);
        }
        dissableEventProcessing(clone);
        // Must also ensure the listener has been set on collections and aggregates.
        List mappings = descriptor.getMappings();
        int size = mappings.size();
        for (int index = 0; index < size; index++) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.get(index);
            mapping.setChangeListener(clone, listener, uow);
        }
        enableEventProcessing(clone);
    }

    /**
     * INTERNAL:
     * initialize the Policy
     */
    public void initialize(AbstractSession session, ClassDescriptor descriptor) {
        //3934266 If changePolicy is ObjectChangeTrackingPolicy or AttributeChangeTrackingPolicy, the class represented 
        //by the descriptor must implement ChangeTracker interface.  Otherwise throw an exception.
        Class javaClass = descriptor.getJavaClass();
        if (!ChangeTracker.class.isAssignableFrom(javaClass)) {
            session.getIntegrityChecker().handleError(DescriptorException.needToImplementChangeTracker(descriptor));
        }
    }

    /**
     * Used to track instances of the change policies without doing an instance of check
     */
    public boolean isObjectChangeTrackingPolicy() {
        return true;
    }
}