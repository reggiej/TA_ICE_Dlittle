// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * INTERNAL:
 */
public class CmrField extends CmpField {
    String fieldType;// Optional

    /**
     * @return String the field type String, or null if not set
     */
    public String getFieldType() {
        return fieldType;
    }

    /**
     * Return the specific element tag for the name of this object
     */
    protected String nameTag() {
        return CMR_FIELD_NAME;
    }

    /**
     * @param String the field type String
     */
    public void setFieldType(String type) {
        fieldType = type;
    }

    /**
     * Load the data for this instance from the specified element.
     * @param e the DOM element
     */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);
        fieldType = optionalStringFromElement(e, CMR_FIELD_TYPE);
    }

    /**
     * Return the data from this instance as a DOM element.
     * @param doc a Document instance used to create elements
     */
    public Element toElement(Document doc) {
        Element e = doc.createElement(CMR_FIELD);
        inheritedFields(doc, e);
        addText(doc, e, nameTag(), getName());
        optionallyAddText(doc, e, CMR_FIELD_TYPE, fieldType);
        return e;
    }
}