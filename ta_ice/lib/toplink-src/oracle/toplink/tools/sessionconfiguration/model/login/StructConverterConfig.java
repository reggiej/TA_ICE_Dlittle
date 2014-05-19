package oracle.toplink.tools.sessionconfiguration.model.login;

import java.util.Vector;

public class StructConverterConfig {
    private Vector<String> m_structConverterClasses;

    public StructConverterConfig() {
        m_structConverterClasses = new Vector<String>();
    }

    public void addStructConverterClass(String listener) {
        m_structConverterClasses.add(listener);
    }

    public void setStructConverterClasses(Vector<String> dataConverters) {
        m_structConverterClasses = dataConverters;
    }

    public Vector<String> getStructConverterClasses() {
        return m_structConverterClasses;
    }
    
}
