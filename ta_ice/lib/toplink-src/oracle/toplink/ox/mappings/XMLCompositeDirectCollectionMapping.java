// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.mappings;

import java.util.Enumeration;
import java.util.Vector;
import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.exceptions.DescriptorException;
import oracle.toplink.ox.XMLField;
import oracle.toplink.internal.helper.ConversionManager;
import oracle.toplink.internal.queryframework.CollectionContainerPolicy;
import oracle.toplink.internal.queryframework.ContainerPolicy;
import oracle.toplink.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.mappings.converters.TypeConversionConverter;
import oracle.toplink.mappings.foundation.AbstractCompositeDirectCollectionMapping;
import oracle.toplink.ox.mappings.converters.XMLConverter;
import oracle.toplink.ox.record.XMLRecord;
import oracle.toplink.queryframework.ObjectBuildingQuery;

/**
 * <p>Composite direct collection XML mappings map a collection of simple types (String, Number, Date,
 * etc.) to and from a sequence of composite XML nodes.
 *
 * <p><b>Composite direct collection XML mappings can be used in the following scenarios</b>:<ul>
 * <li> Mapping to Multiple Text Nodes </li>
 * <li> Mapping to Multiple Attributes </li>
 * <li> Mapping to a Single Text Node </li>
 * <li> Mapping to a Single Attribute </li>
 * <li> Mapping to a List of Unions </li>
 * <li> Mapping a Union of Lists </li>
 * <li> Specifying the Content Type of a Collection </li>
 * </ul>
 *
 * <p><b>Setting the XPath</b>: TopLink XML mappings make use of an XPath statement to find the relevant
 * data in an XML document.  The XPath statement is relative to the context node specified in the descriptor.
 * The XPath may contain node type, path, and positional information.  The XPath is specified on the
 * mapping using the <code>setXPath</code> method.
 *
 * <p>The following XPath statements may be used to specify the location of XML data relating to an object's
 * name attribute:
 *
 * <p><table border="1">
 * <tr>
 * <th id="c1" align="left">XPath</th>
 * <th id="c2" align="left">Description</th>
 * </tr>
 * <tr>
 * <td headers="c1">@tasks</td>
 * <td headers="c2">The "@" character indicates that the node is an attribute.  This XPath applies only to the single
 * attribute node case; each member of the collection is mapped to a single node.</td>
 * </tr>
 * <tr>
 * <td headers="c1">tasks/@task</td>
 * <td headers="c2">The "@" character indicates that the node is an attribute.  The information is stored
 * in the attribute node of the tasks element.</td>
 * </tr>
 * <tr>
 * <td headers="c1">text()</td>
 * <td headers="c2">"text()" indicates that the node is a text node.  In this case the task value in the
 * text node belongs to the context node.</td>
 * </tr>
 * <tr>
 * <td headers="c1">tasks/text()</td>
 * <td headers="c2">The task information is stored in the text node of the tasks element.</td>
 * </tr>
 * <tr>
 * <td headers="c1" nowrap="true">tasks/task/text()</td>
 * <td headers="c2">The XPath statement may be used to specify any valid path.</td>
 * </tr>
 * <tr>
 * <td headers="c1">task[2]/text()</td>
 * <td headers="c2">The XPath statement may contain positional information.  In this case the task
 * information is stored in the text node of the second occurrence of the task element.</td>
 * </tr>
 * </table>
 *
 * <p><b>Mapping to a Single Text Node</b>: By default, TopLink maps each member of a collection
 * to it's own node.  It is possible, however, to mapping a collection to a single node;  here the contents of
 * the node is treated as a space-separated list.  This behavior is set on the mapping using the <code>
 * setUsesSingleNode </code> method, with 'true' as the parameter.
 *
 * <!--
 * <?xml version="1.0" encoding="UTF-8"?><br>
 * <xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"><br>
 *     <xsd:element name="employee" type="employee-type"/><br>
 *     <xsd:complexType name="employee-type"><br>
 *         <xsd:sequence><br>
 *             <xsd:element name="tasks" type="tasks-type"/><br>
 *         </xsd:sequence><br>
 *     </xsd:complexType><br>
 *     <xsd:simpleType name="tasks-type"><br>
 *         <xsd:list itemType="xsd:string"/><br>
 *     </xsd:simpleType><br>
 * </xsd:schema><br>
 * -->
 *
 * <p><em>XML Schema</em><br>
 * <code>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;<br>
 * &lt;xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;<br>
 * &nbsp;&nbsp;&lt;xsd:element name="employee" type="employee-type"/&gt;<br>
 * &nbsp;&nbsp;&lt;xsd:complexType name="employee-type"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:sequence&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:element name="tasks" type="tasks-type"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/xsd:sequence&gt;<br>
 * &nbsp;&nbsp;&lt;/xsd:complexType&gt;<br>
 * &nbsp;&nbsp;&lt;xsd:simpleType name="tasks-type"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:list itemType="xsd:string"/&gt;<br>
 * &nbsp;&nbsp;&lt;/xsd:simpleType&gt;<br>
 * &lt;/xsd:schema&gt;<br>
 * </code>
 *
 * <p><em>Code Sample</em><br>
 * <code>
 * XMLCompositeDirectCollectionMapping tasksMapping = new XMLCompositeDirectCollectionMapping();<br>
 * tasksMapping.setAttributeName("tasks");<br>
 * tasksMapping.setXPath("tasks/text()");<br>
 * tasksMapping.setUsesSingleNode(true);<br>
 * </code>
 *
 * <p><b>Specifying the Content Type of a Collection</b>: By default, TopLink will treat the node values
 * read in by a composite direct collection XML mapping as objects of type String. You can override this behavior
 * by specifying the type of the collection's contents.
 *
 * <!--
 * <?xml version="1.0" encoding="UTF-8"?><br>
 * <xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"><br>
 *     <xsd:element name="employee" type="employee-type"/><br>
 *     <xsd:complexType name="employee-type"><br>
 *         <xsd:sequence><br>
 *             <xsd:element name="vacation" type="xsd:string" maxOccurs="unbounded"/><br>
 *         </xsd:sequence><br>
 *     </xsd:complexType><br>
 * </xsd:schema><br>
 *  -->
 *
 * <p><em>XML Schema</em><br>
 * <code>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;<br>
 * &lt;xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;<br>
 * &nbsp;&nbsp;&lt;xsd:element name="employee" type="employee-type"/&gt;<br>
 * &nbsp;&nbsp;&lt;xsd:complexType name="employee-type"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:sequence&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:element name="vacation" type="xsd:string" maxOccurs="unbounded"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/xsd:sequence&gt;<br>
 * &nbsp;&nbsp;&lt;/xsd:complexType&gt;<br>
 * &lt;/xsd:schema&gt;<br>
 * </code>
 *
 * <p><em>Code Sample</em><br>
 * <code>
 * XMLCompositeDirectCollectionMapping tasksMapping = new XMLCompositeDirectCollectionMapping();<br>
 * tasksMapping.setAttributeName("vacationDays");<br>
 * tasksMapping.setXPath("vacation/text()");<br>
 * tasksMapping.setAttributeElementClass(Calendar.class);<br>
 * </code>
 *
 * <p><b>Mapping to a List of Unions</b>:
 *
 * <!--
 * <?xml version="1.0" encoding="UTF-8"?>
 * <xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema">
 *     <xsd:element name="vacation" type="listOfUnions"/>
 *     <xsd:simpleType name="listOfUnions">
 *         <xsd:list>
 *             <xsd:simpleType>
 *                 <xsd:union memberTypes="xsd:date xsd:integer"/>
 *             </xsd:simpleType>
 *         </xsd:list>
 *     </xsd:simpleType>
 * </xsd:schema>
 * -->
 *
 * <p><em>XML Schema</em><br>
 * <code>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;<br>
 * &lt;xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;<br>
 * &nbsp;&nbsp;&lt;xsd:element name="vacation" type="listOfUnions"/&gt;<br>
 * &nbsp;&nbsp;&lt;xsd:simpleType name="listOfUnions"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:list&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:simpleType&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:union memberTypes="xsd:date xsd:integer"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/xsd:simpleType&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/xsd:list&gt;<br>
 * &nbsp;&nbsp;&lt;/xsd:simpleType&gt;<br>
 * &lt;/xsd:schema&gt;<br>
 * </code>
 *
 * <p><em>Code Sample</em><br>
 * <code>
 * XMLCompositeDirectCollectionMapping mapping = new XMLCompositeDirectCollectionMapping();<br>
 * mapping.setAttributeName("myattribute");<br>
 * XMLUnionField field = new XMLUnionField("listOfUnions/text()");<br>
 * mapping.addSchemaType(new QName(url,XMLConstants.INT));<br>
 * mapping.addSchemaType(new QName(url,XMLConstants.DATE));<br>
 * mapping.setField(field);<br>
 * mapping.useSingleElement(false);<br>
 * </code>
 *
 * <p><b>More Information</b>: For more information about using the XML Composite Direct
 * Collection Mapping, see the "Understanding XML Mappings" chapter of the Oracle TopLink
 * Developer's Guide.
 *
 * @since Oracle TopLink 10<i>g</i> Release 2 (10.1.3)
 */
public class XMLCompositeDirectCollectionMapping extends AbstractCompositeDirectCollectionMapping implements XMLMapping {
	private boolean isCDATA;
    public XMLCompositeDirectCollectionMapping() {
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
        if (this.getField() instanceof XMLField && getValueConverter() instanceof TypeConversionConverter) {
            TypeConversionConverter converter = (TypeConversionConverter)getValueConverter();
            this.getField().setType(converter.getObjectClass());
        }

        ContainerPolicy cp = getContainerPolicy();
        if (cp != null) {
            if (cp.getContainerClass() == null) {
                Class cls = ConversionManager.getDefaultManager().convertClassNameToClass(cp.getContainerClassName());
                cp.setContainerClass(cls);
            }
        }
        ((XMLField)this.getField()).setIsCDATA(this.isCDATA());
    }

    /**
     * Set the Mapping field name attribute to the given XPath String
     * @param xpathString String
     */
    public void setXPath(String xpathString) {
        if ((xpathString.indexOf("@") == -1) && (!xpathString.endsWith("text()"))) {
            xpathString += "/text()";
        }
        setField(new XMLField(xpathString));
    }

    /**
     * Get the XPath String
     * @return String the XPath String associated with this Mapping
     */
    public String getXPath() {
        return getFieldName();
    }
    public void useCollectionClassName(String concreteContainerClassName) {
        this.setContainerPolicy(new CollectionContainerPolicy(concreteContainerClassName));
    }
    /**
     * INTERNAL:
     * Build the nested collection from the database row.
     */
    public Object valueFromRow(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery sourceQuery, AbstractSession executionSession) throws DatabaseException {
        ContainerPolicy cp = this.getContainerPolicy();

        Object fieldValue = row.getValues(this.getField());
        if (fieldValue == null) {
            return cp.containerInstance();
        }

        Vector fieldValues = this.getDescriptor().buildDirectValuesFromFieldValue(fieldValue);
        if (fieldValues == null) {
            return cp.containerInstance();
        }

        Object result = cp.containerInstance(fieldValues.size());
        for (Enumeration stream = fieldValues.elements(); stream.hasMoreElements();) {
            Object element = stream.nextElement();
            if (hasValueConverter()) {
                if (getValueConverter() instanceof XMLConverter) {
                    element = ((XMLConverter)getValueConverter()).convertDataValueToObjectValue(element, executionSession, ((XMLRecord)row).getUnmarshaller());
                } else {
                    element = getValueConverter().convertDataValueToObjectValue(element, executionSession);
                }
            }
            cp.addInto(element, result, sourceQuery.getSession());
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
            if (hasValueConverter()) {
                if (getValueConverter() instanceof XMLConverter) {
                    element = ((XMLConverter)getValueConverter()).convertObjectValueToDataValue(element, session, ((XMLRecord)row).getMarshaller());
                } else {
                    element = getValueConverter().convertObjectValueToDataValue(element, session);
                }
            }
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
    public void writeSingleValue(Object value, Object parent, XMLRecord record, AbstractSession session) {
        Object element = value;
        if (hasValueConverter()) {
            if (getValueConverter() instanceof XMLConverter) {
                element = ((XMLConverter)getValueConverter()).convertObjectValueToDataValue(element, session, record.getMarshaller());
            } else {
                element = getValueConverter().convertObjectValueToDataValue(element, session);
            }
        }
        record.add(this.getField(), element);
    }
        
    
    
    public void setIsCDATA(boolean CDATA) {
        isCDATA = CDATA;
    }
    
    public boolean isCDATA() {
        return isCDATA;
    }    
}
