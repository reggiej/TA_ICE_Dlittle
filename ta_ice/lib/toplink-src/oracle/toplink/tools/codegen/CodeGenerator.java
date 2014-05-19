// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.codegen;

import java.io.*;
import oracle.toplink.internal.helper.*;
import oracle.toplink.exceptions.*;

/**
 * INTERNAL:
 * <p><b>Purpose</b>: Used to generate code
 *
 * @since TopLink 3.0
 * @author James Sutherland
 */
public class CodeGenerator {
    protected Writer output;
    protected ClassDefinition currentClass;

    /*Bug#3388703 useUnicode is added to provide user with an option to escape
    non-ASCII characters or not */
    protected boolean useUnicode = true;

    public CodeGenerator() {
        this.output = new StringWriter();
    }

    public CodeGenerator(boolean useUnicode) {
        this();
        this.useUnicode = useUnicode;
    }

    public void cr() {
        write(Helper.cr());
    }

    public ClassDefinition getCurrentClass() {
        return currentClass;
    }

    public Writer getOutput() {
        return output;
    }

    public void setCurrentClass(ClassDefinition currentClass) {
        this.currentClass = currentClass;
    }

    public void setOutput(Writer output) {
        this.output = output;
    }

    public void tab() {
        write("\t");
    }

    public void tab(int indent) {
        for (int index = 0; index < indent; index++) {
            tab();
        }
    }

    public String toString() {
        return getOutput().toString();
    }

    public void write(Object value) {
        try {

            /*Bug#3388703 useUnicode is added to provide user with an option to escape
            non-ASCII characters or not */
            if (!useUnicode) {
                getOutput().write(String.valueOf(value));
            } else {
                //Bug2906180  \\uxxxx escaped characters are used for non-ASCII characters  
                String test = String.valueOf(value);
                StringBuffer escapedStr = new StringBuffer(test.length() * 4);
                for (int i = 0; i < test.length(); i++) {
                    char c = test.charAt(i);
                    if (c < 127) {
                        escapedStr.append(c);
                    } else {
                        String escapedChar = Long.toHexString((long)(c)).toUpperCase();
                        switch (escapedChar.length()) {
                        case 1:
                            escapedStr.append("\\u000" + escapedChar);
                            break;
                        case 2:
                            escapedStr.append("\\u00" + escapedChar);
                            break;
                        case 3:
                            escapedStr.append("\\u0" + escapedChar);
                            break;
                        default:
                            escapedStr.append("\\u" + escapedChar);
                            break;
                        }
                    }
                }
                getOutput().write(escapedStr.toString());
            }
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
    }

    public void writeln(Object value) {
        write(value);
        cr();
    }

    /**
     * Write the type checking if its package is required.
     */
    public void writeType(String typeName) {
        String localTypeName = typeName;
        if (getCurrentClass() != null) {
            int index = typeName.lastIndexOf('.');
            if (index != -1) {
                String packageName = typeName.substring(index);
                if (getCurrentClass().getImports().contains(packageName)) {
                    localTypeName = typeName.substring(index, typeName.length());
                }
            }
        }
        write(localTypeName);
    }
}