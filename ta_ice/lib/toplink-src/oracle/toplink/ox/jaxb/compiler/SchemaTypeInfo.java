// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.compiler;

import java.util.ArrayList;

import javax.xml.namespace.QName;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>Provide additional information about JAXB 2.0 Generated Schemas to 
 * callers.
 * <p><b>Responsibilities:</b><ul>
 * <li>Store information about a schema type generated for a specific class</li>
 * <li>Store information about any globalElementDeclarations that were generated for a specific class</li>
 * <li>Act as an integration point with WebServices</li>
 * </ul>
 * <p>This class was created as a means to return specific information about generated schema
 * artifacts for a particular java class. A Map of SchemaTypeInfo is returned from schema generation
 * operations on TopLinkJAXB20Generator.
 * 
 * @author mmacivor
 * @since Oracle TopLink 11.1.1.0.0
 * @see oracle.toplink.ox.jaxb.compiler.Generator
 */
public class SchemaTypeInfo {
    private QName schemaTypeName;
    private ArrayList<QName> globalElementDeclarations;
    
    public QName getSchemaTypeName() {
        return schemaTypeName;
    }
    
    public void setSchemaTypeName(QName typeName) {
        this.schemaTypeName = typeName;
    }
    
    public ArrayList<QName> getGlobalElementDeclarations() {
        if(globalElementDeclarations == null) {
            globalElementDeclarations = new ArrayList();
        }
        return globalElementDeclarations;
    }
    
    public void setGlobalElementDeclarations(ArrayList<QName> elementDeclarations) {
        this.globalElementDeclarations = elementDeclarations;
    }
}

