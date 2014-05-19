// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ox.schema;

import java.net.URL;
import oracle.toplink.internal.helper.ConversionManager;

/**
 * A schema reference for accessing an XML Schema from the class path.
 */
public class XMLSchemaClassPathReference extends XMLSchemaReference {
    public XMLSchemaClassPathReference() {
        super();
    }

    public XMLSchemaClassPathReference(String resource) {
        super(resource);
    }

    public URL getURL() {
        // The URL must be passed to the resource, not just the input stream as it is require to
        // resolve relative URL for imports and includes.
        return ConversionManager.getDefaultManager().getLoader().getResource(this.getResource());
    }
}