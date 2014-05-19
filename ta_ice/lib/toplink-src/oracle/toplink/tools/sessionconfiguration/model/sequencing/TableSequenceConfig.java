// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.sequencing;


/**
 * INTERNAL:
 */
public class TableSequenceConfig extends SequenceConfig {
    private String m_table;
    private String m_nameField;
    private String m_counterField;

    public TableSequenceConfig() {
        super();
    }

    public void setTable(String table) {
        m_table = table;
    }

    public String getTable() {
        return m_table;
    }

    public void setNameField(String nameField) {
        m_nameField = nameField;
    }

    public String getNameField() {
        return m_nameField;
    }

    public void setCounterField(String counterField) {
        m_counterField = counterField;
    }

    public String getCounterField() {
        return m_counterField;
    }
}