// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.mappings;

import oracle.toplink.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.ox.XMLField;
import oracle.toplink.ox.record.DOMRecord;
import oracle.toplink.ox.record.XMLRecord;
import oracle.toplink.platform.xml.XMLPlatformFactory;
import oracle.toplink.queryframework.ObjectBuildingQuery;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * PUBLIC:
 * <p><b>Purpose:</b>This mapping provides a means to keep a part of an XML tree as a Node.
 */
public class XMLFragmentMapping extends XMLDirectMapping {

    /**
     * INTERNAL:
     * Get a value from the object and set that in the respective field of the row.
     */
    public void writeFromObjectIntoRow(Object object, AbstractRecord row, AbstractSession session) {
        if (isReadOnly()) {
            return;
        }
        Object attributeValue = getAttributeValueFromObject(object);

        // AttributeValue should be a node, try just putting it into the row
        writeSingleValue(attributeValue, object, (XMLRecord)row, session);
    }

    public Object valueFromRow(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery query, AbstractSession executionSession) {
        Object value = ((DOMRecord)row).getIndicatingNoEntry(this.getField(), true);
        if (value instanceof Element) {
            XMLPlatformFactory.getInstance().getXMLPlatform().namespaceQualifyFragment((Element)value);
        }
        return value;
    }

    public void writeSingleValue(Object attributeValue, Object parent, XMLRecord row, AbstractSession session) {
        if (((XMLField)this.getField()).getLastXPathFragment().nameIsText()) {
            if (attributeValue instanceof Text) {
                attributeValue = ((Text)attributeValue).getNodeValue();
            }
        }
        row.put(getField(), attributeValue);
    }

    public void setXPath(String xpathString) {
        setField(new XMLField(xpathString));
    }

    public boolean isAbstractDirectMapping() {
        return false;
    }
}