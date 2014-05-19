// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.sequencing;


/**
 * INTERNAL:
 */
public class SequenceConfig {
    private String m_name;
    private Integer m_preallocationSize;

    public SequenceConfig() {
        super();
    }

    public void setName(String name) {
        m_name = name;
    }

    public String getName() {
        return m_name;
    }

    public void setPreallocationSize(Integer preallocationSize) {
        m_preallocationSize = preallocationSize;
    }

    public Integer getPreallocationSize() {
        return m_preallocationSize;
    }
}