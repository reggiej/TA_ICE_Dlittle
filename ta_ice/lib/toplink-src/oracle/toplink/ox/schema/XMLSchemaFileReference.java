// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ox.schema;

import java.io.File;
import java.net.URL;
import oracle.toplink.exceptions.XMLMarshalException;

/**
 * A schema reference for accessing an XML Schema from a file.
 */
public class XMLSchemaFileReference extends XMLSchemaReference {
    public XMLSchemaFileReference() {
        super();
    }

    public XMLSchemaFileReference(File file) {
        this(file.getAbsolutePath());
    }

    public XMLSchemaFileReference(String fileName) {
        super(fileName);
    }

    public File getFile() {
        return new File(this.getFileName());
    }

    public void setFile(File file) {
        this.setFileName(file.getAbsolutePath());
    }

    public String getFileName() {
        return this.getResource();
    }

    public void setFileName(String filename) {
        this.setResource(filename);
    }

    public URL getURL() {
        try {
            return this.getFile().toURL();
        } catch (Exception e) {
            throw XMLMarshalException.errorResolvingXMLSchema(e);
        }
    }
}