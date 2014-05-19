// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ox.schema;

import java.net.MalformedURLException;
import java.net.URL;
import oracle.toplink.exceptions.XMLMarshalException;

public class XMLSchemaURLReference extends XMLSchemaReference {
    public XMLSchemaURLReference() {
        super();
    }

    public XMLSchemaURLReference(URL url) {
        this(url.toString());
    }

    public XMLSchemaURLReference(String url) {
        super(url);
    }

    public URL getURL() {
        try {
            return new URL(this.getURLString());
        } catch (MalformedURLException e) {
            throw XMLMarshalException.errorResolvingXMLSchema(e);
        }
    }

    public void setURL(URL url) {
        this.setURLString(url.toString());
    }

    public String getURLString() {
        return this.getResource();
    }

    public void setURLString(String url) {
        this.setResource(url);
    }
}