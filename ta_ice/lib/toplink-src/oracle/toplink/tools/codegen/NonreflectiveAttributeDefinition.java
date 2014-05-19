// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.codegen;

import java.util.Map;

/**
 * INTERNAL:
 * <p><b>Purpose</b>: Model an attribute for code generation purposes,
 * using a java.lang.String for the attribute type.
 *
 * @since TopLink 5.0
 * @author Paul Fullbright
 */
public class NonreflectiveAttributeDefinition extends AttributeDefinition {
    protected String type;

    public NonreflectiveAttributeDefinition() {
        this.type = "";
    }

    private void adjustType(Map typeNameMap) {
        String adjustedTypeName = adjustTypeName(getTypeName(), typeNameMap);

        if (!getTypeName().equals(adjustedTypeName)) {
            setType(adjustedTypeName);
        }
    }

    protected void adjustTypeNames(Map typeNameMap) {
        adjustType(typeNameMap);
        super.adjustTypeNames(typeNameMap);
    }

    protected String getTypeName() {
        return type;
    }

    public void setType(String typeName) {
        this.type = typeName;
    }
}