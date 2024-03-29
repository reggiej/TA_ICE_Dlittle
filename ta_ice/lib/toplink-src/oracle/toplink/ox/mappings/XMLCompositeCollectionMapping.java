// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.mappings;

import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Vector;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.exceptions.DescriptorException;
import oracle.toplink.exceptions.XMLMarshalException;
import oracle.toplink.internal.ox.XPathFragment;
import oracle.toplink.ox.XMLConstants;
import oracle.toplink.ox.XMLField;
import oracle.toplink.internal.helper.ConversionManager;
import oracle.toplink.internal.ox.XMLObjectBuilder;
import oracle.toplink.internal.queryframework.ContainerPolicy;
import oracle.toplink.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.internal.queryframework.MapContainerPolicy;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.mappings.foundation.AbstractCompositeCollectionMapping;
import oracle.toplink.ox.XMLDescriptor;
import oracle.toplink.ox.mappings.converters.XMLConverter;
import oracle.toplink.ox.record.DOMRecord;
import oracle.toplink.ox.record.XMLRecord;
import oracle.toplink.ox.schema.XMLSchemaReference;
import oracle.toplink.queryframework.ObjectBuildingQuery;

/**
 * <p>Composite collection XML mappings map an attribute that contains a homogeneous collection of objects
 * to multiple XML elements.  Use composite collection XML mappings to represent one-to-many relationships.
 * Composite collection XML mappings can reference any class that has a TopLink descriptor. The attribute in
 * the object mapped must implement either the Java Collection interface (for example, Vector or HashSet)
 * or Map interface (for example, Hashtable or TreeMap). The CompositeCollectionMapping class
 * allows a reference to the mapped class and the indexing type for that class.  This mapping is, by
 * definition, privately owned.
 *
 * <p><b>Setting the XPath</b>: TopLink XML mappings make use of XPath statements to find the relevant
 * data in an XML document.  The XPath statement is relative to the context node specified in the descriptor.
 * The XPath may contain path and positional information;  the last node in the XPath forms the local
 * root node for the composite object.  The XPath is specified on the mapping using the <code>setXPath</code>
 * method.
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
 * <td headers="c1">phone-number</td>
 * <td headers="c2">The phone number information is stored in the phone-number element.</td>
 * </tr>
 * <tr>
 * <td headers="c1" nowrap="true">contact-info/phone-number</td>
 * <td headers="c2">The XPath statement may be used to specify any valid path.</td>
 * </tr>
 * <tr>
 * <td headers="c1">phone-number[2]</td>
 * <td headers="c2">The XPath statement may contain positional information.  In this case the phone
 * number information is stored in the second occurrence of the phone-number element.</td>
 * </tr>
 * </table>
 *
 * <p><b>Mapping a Composite Collection</b>:
 *
 * <!--
 * <?xml version="1.0" encoding="UTF-8"?>
 * <xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema">
 *     <xsd:element name="customer" type="customer-type"/>
 *     <xsd:complexType name="customer-type">
 *         <xsd:sequence>
 *             <xsd:element name="first-name" type="xsd:string"/>
 *             <xsd:element name="last-name" type="xsd:string"/>
 *             <xsd:element name="phone-number">
 *                 <xsd:complexType>
 *                     <xsd:sequence>
 *                         <xsd:element name="number" type="xsd:string"/>
 *                     </xsd:sequence>
 *                     <xsd:attribute name="type" type="xsd:string"/>
 *                 </xsd:complexType>
 *             </xsd:element>
 *         </xsd:sequence>
 *     </xsd:complexType>
 * </xsd:schema>
 * -->
 *
 * <p><em>XML Schema</em><br>
 * <code>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;<br>
 * &lt;xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;<br>
 * &nbsp;&nbsp;&lt;xsd:element name="customer" type="customer-type"/&gt;<br>
 * &nbsp;&nbsp;&lt;xsd:complexType name="customer-type"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:sequence&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:element name="first-name" type="xsd:string"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:element name="last-name" type="xsd:string"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:element name="phone-number"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:complexType&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:sequence&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:element name="number" type="xsd:string"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/xsd:sequence&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;xsd:attribute name="type" type="xsd:string"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/xsd:complexType&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/xsd:element&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/xsd:sequence&gt;<br>
 * &nbsp;&nbsp;&lt;/xsd:complexType&gt;<br>
 * &lt;/xsd:schema&gt;<br>
 * </code>
 *
 * <p><em>Code Sample</em><br>
 * <code>
 * XMLCompositeCollectionMapping phoneNumbersMapping = new XMLCompositeCollectionMapping();<br>
 * phoneNumbersMapping.setAttributeName("phoneNumbers");<br>
 * phoneNumbersMapping.setXPath("phone-number");<br>
 * phoneNumbersMapping.setReferenceClass(PhoneNumber.class);<br>
 * </code>
 *
 * <p><b>More Information</b>: For more information about using the XML Composite Collection Mapping, see the
 * "Understanding XML Mappings" chapter of the Oracle TopLink Developer's Guide.
 *
 * @since Oracle TopLink 10<i>g</i> Release 2 (10.1.3)
 */
public class XMLCompositeCollectionMapping extends AbstractCompositeCollectionMapping implements XMLMapping {

    public XMLCompositeCollectionMapping() {
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
     * The mapping is initialized with the given session. This mapping is fully initialized
     * after this.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        //modified so that reference class on composite mappings is no longer mandatory
        if ((getReferenceClass() == null) && (getReferenceClassName() != null)) {
            setReferenceClass(ConversionManager.getDefaultManager().convertClassNameToClass(getReferenceClassName()));
        }
        if (getReferenceClass() != null) {
            super.initialize(session);
        } else {
            //below should be the same as AbstractCompositeCollectionMapping.initialize
            if (getField() == null) {
                throw DescriptorException.fieldNameNotSetInMapping(this);
            }
            setField(getDescriptor().buildField(getField()));
            setFields(collectFields());
            if (hasConverter()) {
                getConverter().initialize(this, session);
            }
        }

        ContainerPolicy cp = getContainerPolicy();
        if (cp != null) {
            if (cp.getContainerClass() == null) {
                Class cls = ConversionManager.getDefaultManager().convertClassNameToClass(cp.getContainerClassName());
                cp.setContainerClass(cls);
            }
            if (cp instanceof MapContainerPolicy) {
                ((MapContainerPolicy)cp).setElementClass(this.getReferenceClass());
            }
        }
    }

    /**
     * Get the XPath String
     * @return String the XPath String associated with this Mapping     *
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

    protected Object buildCompositeObject(ClassDescriptor descriptor, AbstractRecord nestedRow, ObjectBuildingQuery query, JoinedAttributeManager joinManger) {
        return descriptor.getObjectBuilder().buildObject(query, nestedRow, joinManger);
    }

    protected AbstractRecord buildCompositeRow(Object attributeValue, AbstractSession session, AbstractRecord parentRow) {
        ClassDescriptor classDesc = getReferenceDescriptor(attributeValue, session);
        XMLObjectBuilder objectBuilder = (XMLObjectBuilder)classDesc.getObjectBuilder();
        XMLField xmlFld = (XMLField)getField();
        if (xmlFld.hasLastXPathFragment() && xmlFld.getLastXPathFragment().hasLeafElementType()) {
            XMLRecord xmlRec = (XMLRecord)parentRow;
            xmlRec.setLeafElementType(xmlFld.getLastXPathFragment().getLeafElementType());
        }
        XMLRecord parent = (XMLRecord)parentRow;
        boolean addXsiType = shouldAddXsiType((XMLRecord)parentRow, classDesc);
        XMLRecord child = (XMLRecord)objectBuilder.createRecordFor(attributeValue, (XMLField)getField(), parent, this);
        child.setNamespaceResolver(parent.getNamespaceResolver());
        objectBuilder.buildIntoNestedRow(child, attributeValue, session, addXsiType);
        return child;
    }

    /**
     * INTERNAL:
     */
    public void writeFromObjectIntoRow(Object object, AbstractRecord row, AbstractSession session) throws DescriptorException {
        if (this.isReadOnly()) {
            return;
        }

        Object attributeValue = this.getAttributeValueFromObject(object);
        if (attributeValue == null) {
            row.put(this.getField(), null);
            return;
        }

        ContainerPolicy cp = this.getContainerPolicy();

        Vector nestedRows = new Vector(cp.sizeFor(attributeValue));
        for (Object iter = cp.iteratorFor(attributeValue); cp.hasNext(iter);) {
            Object element = cp.next(iter, session);
            // convert the value - if necessary
            if (hasConverter()) {
                if (getConverter() instanceof XMLConverter) {
                    element = ((XMLConverter)getConverter()).convertObjectValueToDataValue(element, session, ((XMLRecord)row).getMarshaller());
                } else {
                    element = getConverter().convertObjectValueToDataValue(element, session);
                }
            }
            nestedRows.addElement(buildCompositeRow(element, session, row));
        }

        Object fieldValue = null;
        if (!nestedRows.isEmpty()) {
            fieldValue = this.getDescriptor().buildFieldValueFromNestedRows(nestedRows, getStructureName(), session);
        }
        row.put(this.getField(), fieldValue);
    }
    public Object valueFromRow(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery sourceQuery, AbstractSession executionSession) throws DatabaseException {
        ContainerPolicy cp = this.getContainerPolicy();

        Object fieldValue = row.getValues(this.getField());

        // BUG#2667762 there could be whitespace in the row instead of null
        if ((fieldValue == null) || (fieldValue instanceof String)) {
            return cp.containerInstance();
        }

        Vector nestedRows = this.getDescriptor().buildNestedRowsFromFieldValue(fieldValue, executionSession);
        if (nestedRows == null) {
            return cp.containerInstance();
        }

        Object result = cp.containerInstance(nestedRows.size());
        for (Enumeration stream = nestedRows.elements(); stream.hasMoreElements();) {
            AbstractRecord nestedRow = (AbstractRecord)stream.nextElement();

            ClassDescriptor aDescriptor = getReferenceDescriptor((DOMRecord)nestedRow);
            if (aDescriptor.hasInheritance()) {
                Class newElementClass = aDescriptor.getInheritancePolicy().classFromRow(nestedRow, executionSession);
                if (newElementClass == null) {
                    // no xsi:type attribute - look for type indicator on the field
                    QName leafElementType = ((XMLField)getField()).getLeafElementType();
                    if (leafElementType != null) {
                        Object indicator = aDescriptor.getInheritancePolicy().getClassIndicatorMapping().get(leafElementType);
                        // if the inheritance policy does not contain the user-set type, throw an exception
                        if (indicator == null) {
                            throw DescriptorException.missingClassForIndicatorFieldValue(leafElementType, aDescriptor.getInheritancePolicy().getDescriptor());
                        }
                        newElementClass = (Class)indicator;
                    }
                }
                if (newElementClass != null) {
                    aDescriptor = this.getReferenceDescriptor(newElementClass, executionSession);
                } else {
                    // since there is no xsi:type attribute or leaf element type set, 
                    // use the reference descriptor -  make sure it is non-abstract
                    if (Modifier.isAbstract(aDescriptor.getJavaClass().getModifiers())) {
                        // throw an exception
                        throw DescriptorException.missingClassIndicatorField(nestedRow, aDescriptor.getInheritancePolicy().getDescriptor());
                    }
                }
            }

            Object element = buildCompositeObject(aDescriptor, nestedRow, sourceQuery, joinManager);
            if (hasConverter()) {
                if (getConverter() instanceof XMLConverter) {
                    element = ((XMLConverter)getConverter()).convertDataValueToObjectValue(element, executionSession, ((XMLRecord)nestedRow).getUnmarshaller());
                } else {
                    element = getConverter().convertDataValueToObjectValue(element, executionSession);
                }
            }
            cp.addInto(element, result, sourceQuery.getSession());
        }
        return result;
    }

    public ClassDescriptor getReferenceDescriptor(DOMRecord xmlRecord) {
        ClassDescriptor returnDescriptor = referenceDescriptor;

        if (returnDescriptor == null) {
            // Try to find a descriptor based on the schema type
            String type = ((Element)xmlRecord.getDOM()).getAttributeNS(XMLConstants.SCHEMA_INSTANCE_URL, XMLConstants.SCHEMA_TYPE_ATTRIBUTE);

            if ((null != type) && !type.equals("")) {
                XPathFragment typeFragment = new XPathFragment(type);
                String namespaceURI = xmlRecord.resolveNamespacePrefix(typeFragment.getPrefix());
                typeFragment.setNamespaceURI(namespaceURI);

                returnDescriptor = xmlRecord.getUnmarshaller().getXMLContext().getDescriptorByGlobalType(typeFragment);

            } else {
                //try leaf element type
                QName leafType = ((XMLField)getField()).getLastXPathFragment().getLeafElementType();
                if (leafType != null) {
                    XPathFragment frag = new XPathFragment();
                    String xpath = leafType.getLocalPart();
                    String uri = leafType.getNamespaceURI();
                    if ((uri != null) && !uri.equals("")) {
                        frag.setNamespaceURI(uri);
                        String prefix = ((XMLDescriptor)getDescriptor()).getNonNullNamespaceResolver().resolveNamespaceURI(uri);
                        if ((prefix != null) && !prefix.equals("")) {
                            xpath = prefix + ":" + xpath;
                        }
                    }
                    frag.setXPath(xpath);

                    returnDescriptor = xmlRecord.getUnmarshaller().getXMLContext().getDescriptorByGlobalType(frag);
                }
            }
        }
        if (returnDescriptor == null) {
            throw XMLMarshalException.noDescriptorFound(this);
        }
        return returnDescriptor;

    }

    protected ClassDescriptor getReferenceDescriptor(Class theClass, AbstractSession session) {
        if ((getReferenceDescriptor() != null) && getReferenceDescriptor().getJavaClass().equals(theClass)) {
            return getReferenceDescriptor();
        }

        ClassDescriptor subDescriptor = session.getDescriptor(theClass);
        if (subDescriptor == null) {
            throw DescriptorException.noSubClassMatch(theClass, this);
        } else {
            return subDescriptor;
        }
    }


    /**
     * INTERNAL:
     */
    public boolean shouldAddXsiType(XMLRecord record, ClassDescriptor descriptor) {
        XMLDescriptor xmlDescriptor = (XMLDescriptor)descriptor;
        if ((getReferenceDescriptor() == null) && (xmlDescriptor.getSchemaReference() != null)) {
            if (descriptor.hasInheritance()) {
                XMLField indicatorField = (XMLField)descriptor.getInheritancePolicy().getClassIndicatorField();
                if ((indicatorField.getLastXPathFragment().getNamespaceURI() != null) && indicatorField.getLastXPathFragment().getNamespaceURI().equals(XMLConstants.SCHEMA_INSTANCE_URL) && indicatorField.getLastXPathFragment().getLocalName().equals(XMLConstants.SCHEMA_TYPE_ATTRIBUTE)) {
                    return false;
                }
            }

            XMLSchemaReference xmlRef = xmlDescriptor.getSchemaReference();
            if ((xmlRef.getType() == XMLSchemaReference.COMPLEX_TYPE) && xmlRef.isGlobalDefinition()) {
                QName ctxQName = xmlRef.getSchemaContextAsQName(xmlDescriptor.getNamespaceResolver());
                QName leafType = ((XMLField)getField()).getLeafElementType();

                if ((leafType == null) || (!ctxQName.equals(record.getLeafElementType()))) {
                    return true;
                }
            }
        }
        return false;
    }
    public void writeSingleValue(Object value, Object parent, XMLRecord record, AbstractSession session) {
        Object element = value;
        if (hasConverter()) {
            if (getConverter() instanceof XMLConverter) {
                element = ((XMLConverter)getConverter()).convertObjectValueToDataValue(element, session, record.getMarshaller());
            } else {
                element = getConverter().convertObjectValueToDataValue(element, session);
            }
        }
        XMLRecord nestedRow = (XMLRecord)buildCompositeRow(element, session, record);
        record.add(getField(), nestedRow);
    }
}
