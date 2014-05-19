// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.eis.mappings;

import java.util.ArrayList;
import javax.xml.namespace.QName;
import oracle.toplink.eis.EISDescriptor;
import oracle.toplink.internal.helper.DatabaseField;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.ox.XMLField;
import oracle.toplink.mappings.foundation.AbstractDirectMapping;

/**
 * <p>EIS Direct Mappings map a simple Java attribute to and from an EIS Record according to 
 * its descriptor's record type.  
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
public class EISDirectMapping extends AbstractDirectMapping implements EISMapping {
    public EISDirectMapping() {
        super();
    }

    /**
     * INTERNAL:
     */
    public boolean isEISMapping() {
        return true;
    }

    /**
     * Set the Mapping field name attribute to the given XPath String.
     * @param xpathString String
     */
    public void setXPath(String xpathString) {
        setField(new XMLField(xpathString));
    }

    /**
     * Get the XPath String associated with this Mapping
     * @return String the XPath String associated with this Mapping
     */
    public String getXPath() {
        return getFieldName();
    }

    /**
     * PUBLIC:
     * Set the field name in the mapping.
     */
    public void setFieldName(String fieldName) {
        setField(new DatabaseField(fieldName));
    }

    protected void writeValueIntoRow(AbstractRecord row, DatabaseField field, Object fieldValue) {
        if (((EISDescriptor)this.getDescriptor()).getDataFormat() == EISDescriptor.XML) {
            row.put(field, fieldValue);
        } else {
            row.add(field, fieldValue);
        }
    }
}
