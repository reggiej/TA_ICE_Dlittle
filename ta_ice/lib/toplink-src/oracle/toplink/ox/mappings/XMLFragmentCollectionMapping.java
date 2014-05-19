// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.mappings;

import java.util.Enumeration;
import java.util.Vector;
import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.exceptions.DescriptorException;
import oracle.toplink.internal.queryframework.ContainerPolicy;
import oracle.toplink.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.mappings.foundation.AbstractCompositeDirectCollectionMapping;
import oracle.toplink.ox.XMLField;
import oracle.toplink.ox.record.DOMRecord;
import oracle.toplink.ox.record.XMLRecord;
import oracle.toplink.platform.xml.XMLPlatformFactory;
import oracle.toplink.queryframework.ObjectBuildingQuery;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 *  @version $Header: XMLFragmentCollectionMapping.java 21-aug-2007.11:06:40 bdoughan Exp $
 *  @author  mmacivor
 *  @since   release specific (what release of product did this appear in)
 */
public class XMLFragmentCollectionMapping extends AbstractCompositeDirectCollectionMapping implements XMLMapping {
    public XMLFragmentCollectionMapping() {
        super();
    }

    /**
     * INTERNAL:
     */
    public boolean isXMLMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * Initialize the mapping.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        super.initialize(session);
    }

    /**
     * Set the Mapping field name attribute to the given XPath String
     * @param xpathString String
     */
    public void setXPath(String xpathString) {
        setField(new XMLField(xpathString));
    }

    /**
     * Get the XPath String
     * @return String the XPath String associated with this Mapping
     */
    public String getXPath() {
        return getFieldName();
    }

    /**
     * INTERNAL:
     * Build the nested collection from the database row.
     */
    public Object valueFromRow(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery sourceQuery, AbstractSession executionSession) throws DatabaseException {
        ContainerPolicy cp = this.getContainerPolicy();

        Object fieldValue = ((DOMRecord)row).getValuesIndicatingNoEntry(this.getField(), true);

        Vector nestedRows = null;
        if (fieldValue instanceof Vector) {
            nestedRows = (Vector)fieldValue;
        }
        if ((nestedRows == null) || nestedRows.isEmpty()) {
            return cp.containerInstance();
        }
        Object result = cp.containerInstance(nestedRows.size());
        for (Enumeration stream = nestedRows.elements(); stream.hasMoreElements();) {
            Object next = stream.nextElement();
            if (next instanceof Element) {                
                XMLPlatformFactory.getInstance().getXMLPlatform().namespaceQualifyFragment((Element)next);
            }
            cp.addInto(next, result, executionSession);
        }
        return result;
    }

    /**
     * INTERNAL:
     */
    public void writeFromObjectIntoRow(Object object, AbstractRecord row, AbstractSession session) {
        if (this.isReadOnly()) {
            return;
        }

        Object attributeValue = this.getAttributeValueFromObject(object);
        if (attributeValue == null) {
            row.put(this.getField(), null);
            return;
        }

        ContainerPolicy cp = this.getContainerPolicy();

        Vector elements = new Vector(cp.sizeFor(attributeValue));
        for (Object iter = cp.iteratorFor(attributeValue); cp.hasNext(iter);) {
            Object element = cp.next(iter, session);
            if (element != null) {
                elements.addElement(element);
            }
        }

        Object fieldValue = null;
        if (!elements.isEmpty()) {
            fieldValue = this.getDescriptor().buildFieldValueFromDirectValues(elements, elementDataTypeName, session);
        }
        row.put(this.getField(), fieldValue);
    }

    public boolean isAbstractCompositeDirectCollectionMapping() {
        return false;
    }

    public void writeSingleValue(Object attributeValue, Object parent, XMLRecord row, AbstractSession session) {
        if (((XMLField)this.getField()).getLastXPathFragment().isAttribute()) {
            if (attributeValue instanceof Attr) {
                attributeValue = ((Attr)attributeValue).getValue();
            }
        } else if (((XMLField)this.getField()).getLastXPathFragment().nameIsText()) {
            if (attributeValue instanceof Text) {
                attributeValue = ((Text)attributeValue).getNodeValue();
            }
        }
        row.put(getField(), attributeValue);
    }
}