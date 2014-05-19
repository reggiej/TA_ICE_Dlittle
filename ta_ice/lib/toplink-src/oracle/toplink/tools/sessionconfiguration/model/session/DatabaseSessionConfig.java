// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.session;

import java.util.Vector;
import oracle.toplink.tools.sessionconfiguration.model.login.*;
import oracle.toplink.tools.sessionconfiguration.model.project.*;

/**
 * INTERNAL:
 */
public class DatabaseSessionConfig extends SessionConfig {
    private LoginConfig m_loginConfig;
    private Vector m_additionalProjects;
    private ProjectConfig m_primaryProject;

    public DatabaseSessionConfig() {
        super();
    }

    public Vector getAdditionalProjects() {
        return m_additionalProjects;
    }

    public LoginConfig getLoginConfig() {
        return m_loginConfig;
    }

    public ProjectConfig getPrimaryProject() {
        return m_primaryProject;
    }

    public void setLoginConfig(LoginConfig loginConfig) {
        m_loginConfig = loginConfig;
    }

    public void setPrimaryProject(ProjectConfig primaryProject) {
        m_primaryProject = primaryProject;
    }

    public void setAdditionalProjects(Vector additionalProjects) {
        m_additionalProjects = additionalProjects;
    }
}