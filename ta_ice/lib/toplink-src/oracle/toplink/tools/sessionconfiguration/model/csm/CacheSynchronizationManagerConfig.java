// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.csm;

import oracle.toplink.tools.sessionconfiguration.model.clustering.*;

/**
 * INTERNAL:
 */
public class CacheSynchronizationManagerConfig {
    private ClusteringServiceConfig m_clusteringService;
    private boolean m_isAsynchronous;
    private boolean m_removeConnectionOnError;

    public CacheSynchronizationManagerConfig() {
    }

    public void setIsAsynchronous(boolean isAsynchronous) {
        m_isAsynchronous = isAsynchronous;
    }

    public boolean getIsAsynchronous() {
        return m_isAsynchronous;
    }

    public void setRemoveConnectionOnError(boolean removeConnectionOnError) {
        m_removeConnectionOnError = removeConnectionOnError;
    }

    public boolean getRemoveConnectionOnError() {
        return m_removeConnectionOnError;
    }

    public void setClusteringServiceConfig(ClusteringServiceConfig clusteringService) {
        m_clusteringService = clusteringService;
    }

    public ClusteringServiceConfig getClusteringServiceConfig() {
        return m_clusteringService;
    }
}