// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.descriptors;

import java.util.Vector;

/**
 * <p><b>Purpose</b>: Provides an empty implementation of DescriptorEventListener.
 * Users who do not require the full DescritorEventListener API can subclass this class
 * and implement only the methods required.
 *
 * @see DescriptorEventManager
 * @see DescriptorEvent
 */
public class DescriptorEventAdapter implements DescriptorEventListener {
    public void aboutToInsert(DescriptorEvent event) {}

    public void aboutToUpdate(DescriptorEvent event) {}

    public void aboutToDelete(DescriptorEvent event) {}

    public boolean isOverriddenEvent(DescriptorEvent event, Vector eventManagers) {
        return false;
    }
    
    public void postBuild(DescriptorEvent event) {}

    public void postClone(DescriptorEvent event) {}

    public void postDelete(DescriptorEvent event) {}

    public void postInsert(DescriptorEvent event) {}

    public void postMerge(DescriptorEvent event) {}

    public void postRefresh(DescriptorEvent event) {}

    public void postUpdate(DescriptorEvent event) {}

    public void postWrite(DescriptorEvent event) {}

    public void prePersist(DescriptorEvent event) {}

    public void preDelete(DescriptorEvent event) {}

    public void preRemove(DescriptorEvent event) {}
    
    public void preInsert(DescriptorEvent event) {}

    public void preUpdate(DescriptorEvent event) {}
    
    public void preUpdateWithChanges(DescriptorEvent event) {}

    public void preWrite(DescriptorEvent event) {}
}