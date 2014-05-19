// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.transport;


/**
 * INTERNAL:
 */
public class UserDefinedTransportManagerConfig extends TransportManagerConfig {
    private String m_transportClass;

    public UserDefinedTransportManagerConfig() {
        super();
    }

    public void setTransportClass(String transportClass) {
        m_transportClass = transportClass;
    }

    public String getTransportClass() {
        return m_transportClass;
    }
}