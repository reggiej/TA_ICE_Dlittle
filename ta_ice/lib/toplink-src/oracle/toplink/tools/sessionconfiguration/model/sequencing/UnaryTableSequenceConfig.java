// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.sequencing;


/**
 * INTERNAL:
 */
public class UnaryTableSequenceConfig extends SequenceConfig {
    private String m_counterField;

    public UnaryTableSequenceConfig() {
        super();
    }

    public void setCounterField(String counterField) {
        m_counterField = counterField;
    }

    public String getCounterField() {
        return m_counterField;
    }
}