// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.eis.mappings;

import java.util.ArrayList;
import javax.xml.namespace.QName;
import oracle.toplink.eis.mappings.EISMapping;
import oracle.toplink.exceptions.DescriptorException;
import oracle.toplink.internal.helper.DatabaseField;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.ox.XMLField;
import oracle.toplink.mappings.converters.TypeConversionConverter;
import oracle.toplink.mappings.foundation.AbstractCompositeDirectCollectionMapping;

/**
 * <p>EIS Composite Direct Collection Mappings map a collection of simple Java attributes 
 * to and from an EIS Record according to its descriptor's record type.  
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
public class EISCompositeDirectCollectionMapping extends AbstractCompositeDirectCollectionMapping implements EISMapping {
    public EISCompositeDirectCollectionMapping() {
        super();
    }

    /**
     * INTERNAL:
     */
    public boolean isEISMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * Initialize the mapping.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        super.initialize(session);
        if (this.getField() instanceof XMLField && getValueConverter() instanceof TypeConversionConverter) {
            TypeConversionConverter converter = (TypeConversionConverter)getValueConverter();
            this.getField().setType(converter.getObjectClass());
        }
    }

    /**
     * Set the Mapping field name attribute to the given XPath String
     *
     * @param xpathString String
     *
     */
    public void setXPath(String xpathString) {
        setField(new XMLField(xpathString));
    }

    /**
     * Get the XPath String
     *
     * @return String the XPath String associated with this Mapping
     *
     */
    public String getXPath() {
        return getFieldName();
    }

    /**
     * PUBLIC:
     * Return the name of the field that holds the nested collection.
     */
    public String getFieldName() {
        return this.getField().getName();
    }

    /**
     * PUBLIC:
     * Set the name of the field that holds the nested collection.
     */
    public void setFieldName(String fieldName) {
        this.setField(new DatabaseField(fieldName));
    }
}
