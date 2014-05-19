// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.rcm;

import oracle.toplink.tools.sessionconfiguration.model.transport.*;
import oracle.toplink.tools.sessionconfiguration.model.rcm.command.*;

/**
 * INTERNAL:
 */
public class RemoteCommandManagerConfig {
    private String m_channel;
    private CommandsConfig m_commandsConfig;
    private TransportManagerConfig m_transportManager;

    public RemoteCommandManagerConfig() {
    }

    public void setChannel(String channel) {
        m_channel = channel;
    }

    public String getChannel() {
        return m_channel;
    }

    public void setCommandsConfig(CommandsConfig commandsConfig) {
        m_commandsConfig = commandsConfig;
    }

    public CommandsConfig getCommandsConfig() {
        return m_commandsConfig;
    }

    public void setTransportManagerConfig(TransportManagerConfig transportManager) {
        m_transportManager = transportManager;
    }

    public TransportManagerConfig getTransportManagerConfig() {
        return m_transportManager;
    }
}