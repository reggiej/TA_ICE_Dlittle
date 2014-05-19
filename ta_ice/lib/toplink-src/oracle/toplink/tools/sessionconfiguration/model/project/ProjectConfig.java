// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.project;


/**
 *  INTERNAL:
 */
public class ProjectConfig {
    private String m_projectString;

    public ProjectConfig() {
    }

    public void setProjectString(String projectString) {
        m_projectString = projectString;
    }

    public String getProjectString() {
        return m_projectString;
    }

    public boolean isProjectXMLConfig() {
        return false;
    }

    public boolean isProjectClassConfig() {
        return false;
    }
}