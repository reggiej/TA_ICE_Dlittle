// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.session;

import oracle.toplink.tools.sessionconfiguration.model.log.*;
import oracle.toplink.tools.sessionconfiguration.model.rcm.*;
import oracle.toplink.tools.sessionconfiguration.model.csm.*;
import oracle.toplink.tools.sessionconfiguration.model.event.*;
import oracle.toplink.tools.sessionconfiguration.model.platform.*;

/**
 * INTERNAL:
 */
public abstract class SessionConfig {
    private String m_name;
    private ServerPlatformConfig m_serverPlatformConfig;
    private RemoteCommandManagerConfig m_remoteCommandManagerConfig;
    private CacheSynchronizationManagerConfig m_cacheSynchronizationManagerConfig;
    private SessionEventManagerConfig m_sessionEventManagerConfig;
    private String m_profiler;
    private String m_externalTransactionControllerClass;
    private String m_exceptionHandlerClass;
    private LogConfig m_logConfig;
    private String m_sessionCustomizerClass;

    public SessionConfig() {
    }

    public void setName(String name) {
        m_name = name;
    }

    public String getName() {
        return m_name;
    }

    public void setServerPlatformConfig(ServerPlatformConfig serverPlatformConfig) {
        m_serverPlatformConfig = serverPlatformConfig;
    }

    public ServerPlatformConfig getServerPlatformConfig() {
        return m_serverPlatformConfig;
    }

    public void setRemoteCommandManagerConfig(RemoteCommandManagerConfig remoteCommandManagerConfig) {
        m_remoteCommandManagerConfig = remoteCommandManagerConfig;
    }

    public RemoteCommandManagerConfig getRemoteCommandManagerConfig() {
        return m_remoteCommandManagerConfig;
    }

    public void setCacheSynchronizationManagerConfig(CacheSynchronizationManagerConfig cacheSynchronizationManagerConfig) {
        m_cacheSynchronizationManagerConfig = cacheSynchronizationManagerConfig;
    }

    public CacheSynchronizationManagerConfig getCacheSynchronizationManagerConfig() {
        return m_cacheSynchronizationManagerConfig;
    }

    public void setSessionEventManagerConfig(SessionEventManagerConfig sessionEventManagerConfig) {
        m_sessionEventManagerConfig = sessionEventManagerConfig;
    }

    public SessionEventManagerConfig getSessionEventManagerConfig() {
        return m_sessionEventManagerConfig;
    }

    public void setProfiler(String profiler) {
        m_profiler = profiler;
    }

    public String getProfiler() {
        return m_profiler;
    }

    public void setExceptionHandlerClass(String exceptionHandlerClass) {
        m_exceptionHandlerClass = exceptionHandlerClass;
    }

    public String getExceptionHandlerClass() {
        return m_exceptionHandlerClass;
    }

    public void setLogConfig(LogConfig logConfig) {
        m_logConfig = logConfig;
    }

    public LogConfig getLogConfig() {
        return m_logConfig;
    }

    public void setSessionCustomizerClass(String sessionCustomizerClass) {
        m_sessionCustomizerClass = sessionCustomizerClass;
    }

    public String getSessionCustomizerClass() {
        return m_sessionCustomizerClass;
    }
}