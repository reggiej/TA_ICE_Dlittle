// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.codegen;


/**
 * INTERNAL:
 * <p><b>Purpose</b>: Model an attribute for code generation purposes,
 * using a java.lang.Class for the attribute type.
 *
 * @since TopLink 5.0
 * @author Paul Fullbright
 */
public class ReflectiveAttributeDefinition extends AttributeDefinition {
    protected Class type;

    public ReflectiveAttributeDefinition() {
        this.type = null;
    }

    public Class getType() {
        return type;
    }

    protected String getTypeName() {
        //fixed for CR#4228
        if (getType().isArray()) {
            String componentType = getType().getComponentType().getName();
            return componentType + "[]";
        } else {
            return getType().getName();
        }
    }

    public void setType(Class type) {
        this.type = type;
    }
}