// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.transport;


/**
 * INTERNAL:
 */
public abstract class TransportManagerConfig {
    private String m_onConnectionError;

    public TransportManagerConfig() {
    }

    public void setOnConnectionError(String onConnectionError) {
        m_onConnectionError = onConnectionError;
    }

    public String getOnConnectionError() {
        return m_onConnectionError;
    }
}