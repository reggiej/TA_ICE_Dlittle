package oracle.toplink.services.oc4j;

//import java.beans.ConstructorProperties;

import javax.management.openmbean.CompositeType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;

/**
 * The class is used internally by the Portable JMX Framework to convert 
 * model specific classes into Open Types so that the attribute of model class can
 * be exposed by MBean. 
 */

public class ClassSummaryDetail {
    

    /**
     * Construct a ClassSummaryDetail instance. The PropertyNames annotation is used 
     * to be able to construct a ClassSummaryDetail instance out of a CompositeData
     * instance. See MXBeans documentation for more details.
     */
    //@ConstructorProperties({"Class Name", "Cache Type", "Configured Size", "Current Size","Parent Class Name"})
    public ClassSummaryDetail(String className, String cacheType, String configuredSize,String currentSize , String parentClassName) {
        this.className = className;
        this.cacheType = cacheType;
        this.configuredSize = configuredSize;
        this.currentSize = currentSize;
        this.parentClassName = parentClassName;
    }
    
    
    private String className;
    private String cacheType;
    private String configuredSize;
    private String currentSize;
    private String parentClassName;
    
    
    
    // The corresponding CompositeType for this class
    private static CompositeType cType_= null;

    private static final String[] itemNames_= 
        {"Class Name", "Cache Type", "Configured Size",
         "Current Size","Parent Class Name"}; 

    static {

        try {

            ArrayType byteArray= new ArrayType(1, SimpleType.BYTE);

            OpenType[] itemTypes = {
                    SimpleType.STRING,
                    SimpleType.STRING,
                    SimpleType.STRING,
                    SimpleType.STRING,
                    SimpleType.STRING};

            cType_ = new CompositeType("oracle.toplink.services.oc4j.ClassSummaryDetail",
                                       // this should be a localized description
                                       // but isn't really required since the attribute
                                       // or parameter description should suffice
                                       // however this value cannot be null or empty
                                       "oracle.toplink.services.oc4j.ClassSummaryDetail",
                                       itemNames_,
                                       // this should be a localized description
                                       // but isn't really required since the attribute
                                       // or parameter description should suffice
                                       // however this value cannot be null or empty
                                       itemNames_,
                                       itemTypes);
        }
        catch( OpenDataException ode) {
            // this won't happen, but in case it does we should log
            throw new RuntimeException(ode);
        }
    }

    /**
     * Returns the CompositeType that describes this model
     * specific class
     */
    public static CompositeType toCompositeType() {
        return cType_;
    } 
    
    /**
     * Convert an instance of this model specific type to 
     * a CompositeData. This ensure that clients that do not
     * have access to the model specific class can still
     * use the MBean. The MXBean framework can perform this
     * conversion automatically. However MXBeans are part of
     * JDK 6.0 and AS11g is required to support JDK 5.0 
     * 
     * @param ct - This parameter is there only for future compatibility reasons
     *             with JDK 6.0. It can be ignored at this point.
     */
    public CompositeData toCompositeData(CompositeType ct) {

        Object[] itemValues = {
                this.className,
                this.cacheType,
                this.configuredSize,
                this.currentSize,
                this.parentClassName};

        CompositeData cData= null;

        try {
            cData= new CompositeDataSupport(cType_, itemNames_, itemValues);
        }
        catch( OpenDataException ode) {
            // this won't happen, but in case it does we should log
            throw new RuntimeException(ode);
        }

        return cData;

    }
    
    /**
     * Create an instance of the model specific class out of
     * an associated CompositeData instance
     */
    public static ClassSummaryDetail from(CompositeData cd) {
   
        if (cd==null) 
            return null;

        return new ClassSummaryDetail( 
                (String)cd.get("Class Name"),
                (String)cd.get("Cache Type"),
                (String)cd.get("Current Size"),
                (String)cd.get("Parent Class Name"),
                (String)cd.get("Configured Size")
                );
    }

    public String getClassName() {
        return className;
    }


    public String getCacheType() {
        return cacheType;
    }

    public String getConfiguredSize() {
        return configuredSize;
    }

    public String getCurrentSize() {
        return currentSize;
    }

    public String getParentClassName() {
        return parentClassName;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }

    
    public void setCacheType(String cacheType) {
        this.cacheType = cacheType;
    }

    public void setConfiguredSize(String configuredSize) {
        this.configuredSize = configuredSize;
    }

    public void setCurrentSize(String currentSize) {
        this.currentSize = currentSize;
    }

    public void setParentClassName(String parentClassName) {
        this.parentClassName = parentClassName;
    }

}
