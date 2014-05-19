// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.eis.mappings;

import oracle.toplink.eis.EISDescriptor;
import oracle.toplink.exceptions.DescriptorException;
import oracle.toplink.internal.descriptors.ObjectBuilder;
import oracle.toplink.internal.helper.DatabaseField;
import oracle.toplink.ox.XMLField;
import oracle.toplink.internal.ox.XMLObjectBuilder;
import oracle.toplink.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.mappings.foundation.AbstractCompositeObjectMapping;
import oracle.toplink.ox.record.XMLRecord;
import oracle.toplink.queryframework.ObjectBuildingQuery;
import oracle.toplink.queryframework.ObjectLevelReadQuery;

/**
 * <p>EIS Composite Object Mappings map a Java object to a privately owned, one-to-one 
 * relationship to an EIS Record according to its descriptor's record type.  
 * 
 * <p><table border="1">
 * <tr>
 * <th id="c1" align="left">Record Type</th>
 * <th id="c2" align="left">Description</th>
 * </tr>
 * <tr>
 * <td headers="c1">Indexed</td>
 * <td headers="c2">Ordered collection of record elements.  The indexed record EIS format 
 * enables Java class attribute values to be retreived by position or index.</td>
 * </tr>
 * <tr>
 * <td headers="c1">Mapped</td>
 * <td headers="c2">Key-value map based representation of record elements.  The mapped record
 * EIS format enables Java class attribute values to be retreived by an object key.</td>
 * </tr>
 * <tr>
 * <td headers="c1">XML</td>
 * <td headers="c2">Record/Map representation of an XML DOM element.</td>
 * </tr>
 * </table>
 * 
 * @see oracle.toplink.eis.EISDescriptor#useIndexedRecordFormat
 * @see oracle.toplink.eis.EISDescriptor#useMappedRecordFormat
 * @see oracle.toplink.eis.EISDescriptor#useXMLRecordFormat
 * 
 * @since Oracle TopLink 10<i>g</i> Release 2 (10.1.3)
 */
public class EISCompositeObjectMapping extends AbstractCompositeObjectMapping implements EISMapping {
    public EISCompositeObjectMapping() {
        super();
    }

    /**
     * INTERNAL:
     */
    public boolean isEISMapping() {
        return true;
    }

    /**
     * Get the XPath String
     *
     * @return String the XPath String associated with this Mapping
     *
     */
    public String getXPath() {
        return getField().getName();
    }

    /**
     * Set the Mapping field name attribute to the given XPath String
     *
     * @param xpathString String
     *
     */
    public void setXPath(String xpathString) {
        this.setField(new XMLField(xpathString));
    }

    /**
     * PUBLIC:
     * Return the name of the field mapped by the mapping.
     */
    public String getFieldName() {
        return this.getField().getName();
    }

    /**
     * PUBLIC:
     * Set the name of the field mapped by the mapping.
     */
    public void setFieldName(String fieldName) {
        this.setField(new DatabaseField(fieldName));
    }

    protected Object buildCompositeRow(Object attributeValue, AbstractSession session, AbstractRecord record) {
        if (((EISDescriptor)getDescriptor()).getDataFormat() == EISDescriptor.XML) {
            XMLObjectBuilder objectBuilder = (XMLObjectBuilder)getReferenceDescriptor(attributeValue, session).getObjectBuilder();
            return objectBuilder.buildRow(attributeValue, session, getField(), (XMLRecord)record);
        } else {
        	AbstractRecord nestedRow = this.getObjectBuilder(attributeValue, session).buildRow(attributeValue, session);
            return this.getReferenceDescriptor(attributeValue, session).buildFieldValueFromNestedRow(nestedRow, session);
        }
    }

    protected Object buildCompositeObject(ObjectBuilder objectBuilder, AbstractRecord nestedRow, ObjectBuildingQuery query, JoinedAttributeManager joinManager) {
        if (((EISDescriptor)getDescriptor()).getDataFormat() == EISDescriptor.XML) {
            return objectBuilder.buildObject(query, nestedRow, joinManager);
        } else {
            Object aggregateObject = objectBuilder.buildNewInstance();
            objectBuilder.buildAttributesIntoObject(aggregateObject, nestedRow, query, joinManager, false);
            return aggregateObject;

        }
    }

    /**
     * INTERNAL:
     * Build the value for the database field and put it in the
     * specified database row.
     */
    public void writeFromObjectIntoRow(Object object, AbstractRecord record, AbstractSession session) throws DescriptorException {
        if (this.isReadOnly()) {
            return;
        }

        Object attributeValue = this.getAttributeValueFromObject(object);

        // handle "." xpath - condition: xml data format AND xml field is "self"
        if ((((EISDescriptor)getDescriptor()).getDataFormat() == EISDescriptor.XML) && ((XMLField)getField()).isSelfField()) {
            XMLObjectBuilder objectBuilder = (XMLObjectBuilder)getReferenceDescriptor(attributeValue, session).getObjectBuilder();
            objectBuilder.buildIntoNestedRow(record, attributeValue, session);
        } else {
            Object fieldValue = null;

            if (attributeValue != null) {
                fieldValue = buildCompositeRow(attributeValue, session, record);
            }

            record.put(this.getField(), fieldValue);
        }
    }
}
