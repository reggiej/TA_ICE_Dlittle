// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import oracle.toplink.sessions.Session;

/**
 * <p><b>Purpose</b>: The fetch group tracker interface provides a set of APIs which
 * the domain object must implement, in order to take advantage of the TopLink fetch group
 * performance enhancement feature.
 *
 * @see oracle.toplink.queryframework.FetchGroup
 *
 * @author King Wang
 * @since TopLink 10.1.3
 */
public interface FetchGroupTracker {

    /**
     * Return the fetch group being tracked
     */
    FetchGroup _persistence_getFetchGroup();

    /**
     * Set a fetch group to be tracked.
     */
    void _persistence_setFetchGroup(FetchGroup group);

    /**
     * Return true if the attribute is in the fetch group being tracked.
     */
    boolean _persistence_isAttributeFetched(String attribute);

    /**
     * Reset all attributes of the tracked object to the unfetched state with initial default values.
     */
    void _persistence_resetFetchGroup();

    /**
     * Return true if the fecth group attributes should be refreshed.
     */
    boolean _persistence_shouldRefreshFetchGroup();

    /**
     * Set true if the fecth group attributes should be refreshed.
     */
    void _persistence_setShouldRefreshFetchGroup(boolean shouldRefreshFetchGroup);
    
    /**
     * Return the session for the object.
     */
    Session _persistence_getSession();

    /**
     * Set true if the fecth group attributes should be refreshed
     */
    void _persistence_setSession(Session session);
}