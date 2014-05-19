// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.sequencing;

import java.util.Vector;

/**
 * INTERNAL:
 */
public class SequencingConfig {
    private SequenceConfig m_defaultSequenceConfig;
    private Vector m_sequenceConfigs;

    public SequencingConfig() {
        super();
    }

    public void setDefaultSequenceConfig(SequenceConfig defaultSequenceConfig) {
        m_defaultSequenceConfig = defaultSequenceConfig;
    }

    public SequenceConfig getDefaultSequenceConfig() {
        return m_defaultSequenceConfig;
    }

    public void setSequenceConfigs(Vector sequenceConfigs) {
        m_sequenceConfigs = sequenceConfigs;
    }

    public Vector getSequenceConfigs() {
        return m_sequenceConfigs;
    }

    // backward compatibility
    public void setNativeSequencing(boolean nativeSequencing) {
        Integer preallocationSize = null;
        if (getDefaultSequenceConfig() != null) {
            if (getNativeSequencing() == nativeSequencing) {
                return;
            }
            preallocationSize = getDefaultSequenceConfig().getPreallocationSize();
        }
        setDefaultSequenceConfig(nativeSequencing);
        if (preallocationSize != null) {
            getDefaultSequenceConfig().setPreallocationSize(preallocationSize);
        }
    }

    public boolean getNativeSequencing() {
        if (getDefaultSequenceConfig() == null) {
            return false;
        } else {
            return getDefaultSequenceConfig() instanceof NativeSequenceConfig;
        }
    }

    public void setSequencePreallocationSize(Integer sequencePreallocationSize) {
        if (getDefaultSequenceConfig() == null) {
            setDefaultSequenceConfig(false);
        }
        getDefaultSequenceConfig().setPreallocationSize(sequencePreallocationSize);
    }

    public Integer getSequencePreallocationSize() {
        if (getDefaultSequenceConfig() == null) {
            return null;
        } else {
            return getDefaultSequenceConfig().getPreallocationSize();
        }
    }

    public void setSequenceTable(String sequenceTable) {
        if (getDefaultSequenceConfig() == null) {
            setDefaultSequenceConfig(false);
        }
        if (getDefaultSequenceConfig() instanceof TableSequenceConfig) {
            ((TableSequenceConfig)getDefaultSequenceConfig()).setTable(sequenceTable);
        }
    }

    public String getSequenceTable() {
        if ((getDefaultSequenceConfig() != null) && getDefaultSequenceConfig() instanceof TableSequenceConfig) {
            return ((TableSequenceConfig)getDefaultSequenceConfig()).getTable();
        } else {
            return null;
        }
    }

    public void setSequenceNameField(String sequenceNameField) {
        if (getDefaultSequenceConfig() == null) {
            setDefaultSequenceConfig(false);
        }
        if (getDefaultSequenceConfig() instanceof TableSequenceConfig) {
            ((TableSequenceConfig)getDefaultSequenceConfig()).setNameField(sequenceNameField);
        }
    }

    public String getSequenceNameField() {
        if ((getDefaultSequenceConfig() != null) && getDefaultSequenceConfig() instanceof TableSequenceConfig) {
            return ((TableSequenceConfig)getDefaultSequenceConfig()).getNameField();
        } else {
            return null;
        }
    }

    public void setSequenceCounterField(String sequenceCounterField) {
        if (getDefaultSequenceConfig() == null) {
            setDefaultSequenceConfig(false);
        }
        if (getDefaultSequenceConfig() instanceof TableSequenceConfig) {
            ((TableSequenceConfig)getDefaultSequenceConfig()).setCounterField(sequenceCounterField);
        }
    }

    public String getSequenceCounterField() {
        if ((getDefaultSequenceConfig() != null) && getDefaultSequenceConfig() instanceof TableSequenceConfig) {
            return ((TableSequenceConfig)getDefaultSequenceConfig()).getCounterField();
        } else {
            return null;
        }
    }

    protected void setDefaultSequenceConfig(boolean nativeSequencing) {
        SequenceConfig sequenceConfig;
        if (nativeSequencing) {
            NativeSequenceConfig nativeSequenceConfig = new NativeSequenceConfig();
            sequenceConfig = nativeSequenceConfig;
        } else {
            TableSequenceConfig tableSequenceConfig = new TableSequenceConfig();
            tableSequenceConfig.setTable("SEQUENCE");
            tableSequenceConfig.setNameField("SEQ_NAME");
            tableSequenceConfig.setCounterField("SEQ_COUNT");
            sequenceConfig = tableSequenceConfig;
        }
        sequenceConfig.setName("");
        sequenceConfig.setPreallocationSize(new Integer(50));
        setDefaultSequenceConfig(sequenceConfig);
    }
}