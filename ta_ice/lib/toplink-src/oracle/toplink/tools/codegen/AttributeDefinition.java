// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.codegen;

import java.util.*;

/**
 * INTERNAL:
 * <p><b>Purpose</b>: Model an attribute for code generation purposes.
 *
 * @since TopLink 3.0
 * @author James Sutherland
 */
public abstract class AttributeDefinition extends CodeDefinition {
    protected String initialValue;

    public AttributeDefinition() {
    }

    /**
     * Parses the initial value, removing the package name for each type
     * (and adding the appropriate import) if the type is
     * unambiguous.
     */
    private void adjustInitialValue(Map typeNameMap) {
        if (getInitialValue() == null) {
            return;
        }

        StringBuffer initialValue = new StringBuffer(getInitialValue());
        Set typeNames = parseForTypeNames(initialValue.toString());

        for (Iterator i = typeNames.iterator(); i.hasNext();) {
            String typeName = (String)i.next();
            String adjustedTypeName = adjustTypeName(typeName, typeNameMap);

            if (!typeName.equals(adjustedTypeName)) {
                int typeNameStartIndex = initialValue.toString().indexOf(typeName);

                while (typeNameStartIndex != -1) {
                    initialValue.replace(typeNameStartIndex, typeNameStartIndex + typeName.length(), adjustedTypeName);
                    typeNameStartIndex = initialValue.toString().indexOf(typeName);
                }
            }
        }

        setInitialValue(initialValue.toString());
    }

    protected void adjustTypeNames(Map typeNameMap) {
        adjustInitialValue(typeNameMap);
    }

    public String getInitialValue() {
        return initialValue;
    }

    protected abstract String getTypeName();

    /**
     * Used for calculating imports.  @see oracle.toplink.tools.codegen.ClassDefinition#calculateImports()
     */
    protected void putTypeNamesInMap(Map typeNameMap) {
        putTypeNameInMap(getTypeName(), typeNameMap);

        for (Iterator i = parseForTypeNames(getInitialValue()).iterator(); i.hasNext();) {
            putTypeNameInMap((String)i.next(), typeNameMap);
        }
    }

    public void setInitialValue(String initialValue) {
        this.initialValue = initialValue;
    }

    public void writeBody(CodeGenerator generator) {
        generator.writeType(getTypeName());
        generator.writeType(" ");
        generator.write(getName());

        if (getInitialValue() != null) {
            generator.write(" = ");
            generator.write(getInitialValue());
        }

        generator.write(";");
    }
}