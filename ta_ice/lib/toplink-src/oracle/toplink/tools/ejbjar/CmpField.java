// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * INTERNAL:
 */
public class CmpField extends NameAndDescription {
    // changed visibility of constructors for compatibility with PrivilegedAccessController - TW
    public CmpField() {
        super();
    }

    public CmpField(String name) {
        super();
        setFieldName(name);
    }

    /**
     * Return the specific element tag for the name of this object
     */
    protected String nameTag() {
        return FIELD_NAME;
    }

    /**
     *@return field-name tag value
     */
    public String getFieldName() {
        return getName();
    }

    /**
     *@param name the field-name
     */
    protected void setFieldName(String name) {
        setName(name);
    }

    /**
     * Return the data from this instance as a DOM element.
     *
     * @param doc a Document instance used to create elements
     */
    public Element toElement(Document doc) {
        Element e = doc.createElement(CMP_FIELD);
        inheritedFields(doc, e);
        addText(doc, e, nameTag(), getName());
        return e;
    }
}