// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.mappings;

import java.util.Enumeration;
import java.util.Vector;
import javax.activation.DataHandler;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import oracle.toplink.exceptions.XMLMarshalException;
import oracle.toplink.internal.helper.ClassConstants;
import oracle.toplink.internal.ox.XMLBinaryDataHelper;
import oracle.toplink.internal.ox.XMLConversionManager;
import oracle.toplink.internal.queryframework.ContainerPolicy;
import oracle.toplink.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.mappings.converters.Converter;
import oracle.toplink.ox.NamespaceResolver;
import oracle.toplink.ox.XMLConstants;
import oracle.toplink.ox.XMLDescriptor;
import oracle.toplink.ox.XMLField;
import oracle.toplink.ox.XMLMarshaller;
import oracle.toplink.ox.XMLUnmarshaller;
import oracle.toplink.ox.mappings.converters.XMLConverter;
import oracle.toplink.ox.record.DOMRecord;
import oracle.toplink.ox.record.XMLRecord;
import oracle.toplink.queryframework.ObjectBuildingQuery;;

/**
 * <p><b>Purpose:</b>Provide a mapping for a collection of binary data values that can be treated
 * as either inline binary values or as an attachment.
 * <p><b>Responsibilities:</b><ul>
 * <li>Handle converting binary types (byte[], Image etc) to base64</li>
 * <li>Make callbacks to AttachmentMarshaller/AttachmentUnmarshaller</li>
 * <li>Write out approriate attachment information (xop:include) </li>
 * </ul>
 *  <p>XMLBinaryDataCollectionMapping represents a mapping of a collection of binary data in the object model
 *  to XML. This can either be written directly as inline binary data (base64) or 
 *  passed through as an MTOM or SWAREF attachment.
 *  <p>The following types are allowable to be mapped using an XMLBinaryDataMapping:<ul>
 *  <li>java.awt.Image</li>
 *  <li>byte[]</li>
 *  <li>javax.activation.DataHandler</li>
 *  <li>javax.xml.transform.Source</li>
 *  <li>javax.mail.internet.MimeMultipart</li>
 *  </ul>
 *  <p><b>Setting the XPath</b>: TopLink XML mappings make use of XPath statements to find the relevant
 * data in an XML document.  The XPath statement is relative to the context node specified in the descriptor.
 * The XPath may contain path and positional information;  the last node in the XPath forms the local
 * node for the binary mapping. The XPath is specified on the mapping using the <code>setXPath</code>
 * method.
 * 
 * <p><b>Inline Binary Data</b>: Set this flag if you want to always inline binary data for this mapping.
 * This will disable consideration for attachment handling for this mapping.
 * 
 * <p><b>SwaRef</b>: Set this flag in order to specify that the target node of this mapping is of type
 * xs:swaref
 *   
 *  @see oracle.toplink.ox.attachment.XMLAttachmentMarshaller
 *  @see oracle.toplink.ox.attachment.XMLAttachmentUnmarshaller
 *  @see oracle.toplink.ox.mappings.MimeTypePolicy
 *  @since   TopLink 11.1.1.0.0g
 */
public class XMLBinaryDataCollectionMapping extends XMLCompositeDirectCollectionMapping {
    private boolean shouldInlineBinaryData;
    private MimeTypePolicy mimeTypePolicy;
    private boolean isSwaRef;
    private Class collectionContentType;
    private static final String INCLUDE = "Include";

    public XMLBinaryDataCollectionMapping() {
        collectionContentType = ClassConstants.APBYTE;
        mimeTypePolicy = new FixedMimeTypePolicy(null);
    }

    public boolean shouldInlineBinaryData() {
        return shouldInlineBinaryData;
    }

    public void setShouldInlineBinaryData(boolean b) {
        shouldInlineBinaryData = b;
    }

    /**
     * INTERNAL
     */
    public String getMimeType(Object anObject) {
        if (mimeTypePolicy == null) {
            return null;
        } else {
            return mimeTypePolicy.getMimeType(anObject);
        }
    }

    /**
     * INTERNAL
     */
    public String getMimeType() {
        return mimeTypePolicy.getMimeType(null);
    }

    public MimeTypePolicy getMimeTypePolicy() {
        return mimeTypePolicy;
    }

    /**
     * Allow implementer to set the MimeTypePolicy class FixedMimeTypePolicy or AttributeMimeTypePolicy (dynamic)
     * @param aPolicy MimeTypePolicy
     */
    public void setMimeTypePolicy(MimeTypePolicy mimeTypePolicy) {
        this.mimeTypePolicy = mimeTypePolicy;
    }

    /**
     * Force mapping to set default FixedMimeTypePolicy using the MimeType string as argument
     * @param mimeTypeString
     */
    public void setMimeType(String mimeTypeString) {
        // use the following to set dynamically - mapping.setMimeTypePolicy(new FixedMimeTypePolicy(property.getMimeType()));
        mimeTypePolicy = new FixedMimeTypePolicy(mimeTypeString);
    }

    public boolean isSwaRef() {
        return isSwaRef;
    }

    public void setSwaRef(boolean swaRef) {
        isSwaRef = swaRef;
    }

    public boolean isAbstractCompositeDirectCollectionMapping() {
        return false;
    }

    /**
    * Set the Mapping field name attribute to the given XPath String
    * @param xpathString String
    */
    public void setXPath(String xpathString) {
        XMLField field = new XMLField(xpathString);
        field.setSchemaType(XMLConstants.BASE_64_BINARY_QNAME);
        setField(new XMLField(xpathString));
    }

    public void writeFromObjectIntoRow(Object object, AbstractRecord row, AbstractSession session) {
        XMLRecord record = (XMLRecord)row;
        XMLMarshaller marshaller = record.getMarshaller();
        Object attributeValue = getAttributeValueFromObject(object);

        ContainerPolicy cp = this.getContainerPolicy();
        Vector elements = new Vector(cp.sizeFor(attributeValue));
        XMLField field = (XMLField)getField();
        NamespaceResolver resolver = field.getNamespaceResolver();
        boolean isAttribute = field.getLastXPathFragment().isAttribute();
        String prefix = null;
        XMLField includeField = null;
        if (!isAttribute) {
            if ((marshaller.getAttachmentMarshaller() != null) && marshaller.getAttachmentMarshaller().isXOPPackage() && !isSwaRef() && !shouldInlineBinaryData()) {
                field = (XMLField)getField();

                // If the field's resolver is non-null and has an entry for XOP, 
                // use it - otherwise, create a new resolver, set the XOP entry, 
                // on it, and use it instead.
                // We do this to avoid setting the XOP namespace declaration on
                // a given field or descriptor's resolver, as it is only required
                // on the current element
                if (resolver != null) {
                    prefix = resolver.resolveNamespaceURI(XMLConstants.XOP_URL);
                }
                if (prefix == null) {
                    prefix = XMLConstants.XOP_PREFIX;//"xop";
                    resolver = new NamespaceResolver();
                    resolver.put(prefix, XMLConstants.XOP_URL);
                }
                includeField = new XMLField(prefix + ":" + INCLUDE + "/@href");
                includeField.setNamespaceResolver(resolver);
            } else {
                XMLField textField = new XMLField(field.getXPath() + "/text()");
                textField.setNamespaceResolver(field.getNamespaceResolver());
                textField.setSchemaType(field.getSchemaType());
                field = textField;
            }
        }

        for (Object iter = cp.iteratorFor(attributeValue); cp.hasNext(iter);) {
            Object element = cp.next(iter, session);
            element = getValueToWrite(element, object, record, field, includeField, session);
            if (element != null) {
                elements.addElement(element);
            }
        }
        Object fieldValue = null;
        if (!elements.isEmpty()) {
            fieldValue = this.getDescriptor().buildFieldValueFromDirectValues(elements, elementDataTypeName, session);
        }
        row.put(field, fieldValue);
    }

    public Object getValueToWrite(Object value, Object parent, XMLRecord record, XMLField field, XMLField includeField, AbstractSession session) {
        XMLMarshaller marshaller = record.getMarshaller();
        Object element = value;
        boolean isAttribute = ((XMLField)getField()).getLastXPathFragment().isAttribute();
        if (getValueConverter() != null) {
            Converter converter = getValueConverter();
            if (converter instanceof XMLConverter) {
                element = ((XMLConverter)converter).convertObjectValueToDataValue(element, session, record.getMarshaller());
            } else {
                element = converter.convertObjectValueToDataValue(element, session);
            }
        }

        if (isAttribute) {
            if (isSwaRef() && (marshaller.getAttachmentMarshaller() != null)) {
                //should be a DataHandler here
                try {
                    String id = marshaller.getAttachmentMarshaller().addSwaRefAttachment((DataHandler)element);

                    //row.put(field, value);
                    element = id;
                } catch (ClassCastException cce) {
                    throw XMLMarshalException.invalidSwaRefAttribute(getAttributeClassification().getName());
                }
            } else {
                //inline case
                XMLBinaryDataHelper.EncodedData data = XMLBinaryDataHelper.getXMLBinaryDataHelper().getBytesForBinaryValue(element, record.getMarshaller(), mimeTypePolicy.getMimeType(parent));
                String base64Value = XMLConversionManager.getDefaultXMLManager().buildBase64StringFromBytes(data.getData());

                element = base64Value;

                //MODIFIED
                //row.put(field, base64Value);
            }
        } else {
            if ((marshaller.getAttachmentMarshaller() != null) && marshaller.getAttachmentMarshaller().isXOPPackage() && !isSwaRef() && !shouldInlineBinaryData()) {
                //write as attachment
                String c_id = "";
                if ((getCollectionContentType() == ClassConstants.ABYTE) || (getCollectionContentType() == ClassConstants.APBYTE)) {
                    if (getCollectionContentType() == ClassConstants.ABYTE) {
                        element = XMLConversionManager.getDefaultXMLManager().convertObject(element, ClassConstants.APBYTE);
                    }
                    c_id = marshaller.getAttachmentMarshaller().addMtomAttachment((byte[])element, 0, ((byte[])element).length, this.mimeTypePolicy.getMimeType(parent), field.getLastXPathFragment().getLocalName(), field.getLastXPathFragment().getNamespaceURI());
                } else if (getCollectionContentType() == XMLBinaryDataHelper.getXMLBinaryDataHelper().DATA_HANDLER) {
                    c_id = marshaller.getAttachmentMarshaller().addMtomAttachment((DataHandler)element, field.getLastXPathFragment().getLocalName(), field.getLastXPathFragment().getNamespaceURI());
                } else {
                    XMLBinaryDataHelper.EncodedData data = XMLBinaryDataHelper.getXMLBinaryDataHelper().getBytesForBinaryValue(element, marshaller, this.mimeTypePolicy.getMimeType(parent));
                    byte[] bytes = data.getData();
                    c_id = marshaller.getAttachmentMarshaller().addMtomAttachment(bytes, 0, bytes.length, data.getMimeType(), field.getLastXPathFragment().getLocalName(), field.getLastXPathFragment().getNamespaceURI());
                }
                DOMRecord include = new DOMRecord(field.getLastXPathFragment().getLocalName());
                include.put(includeField, c_id);
                element = include;
                
                // Need to call setAttributeNS on the record, unless the xop prefix
                // is defined on the descriptor's resolver already
                NamespaceResolver resolver = ((XMLField) getField()).getNamespaceResolver();
                if (resolver == null || resolver.resolveNamespaceURI(XMLConstants.XOP_URL) == null) {
                    resolver = new NamespaceResolver();
                    resolver.put(XMLConstants.XOP_PREFIX, XMLConstants.XOP_URL);
                    String xpath = XMLConstants.XOP_PREFIX+":"+INCLUDE;
                    XMLField incField = new XMLField(xpath);
                    incField.setNamespaceResolver(resolver);
                    Object obj = include.getIndicatingNoEntry(incField);
                    if (obj != null && obj instanceof DOMRecord) {
                        if(((DOMRecord)obj).getDOM().getNodeType() == Node.ELEMENT_NODE) {
                            ((Element)((DOMRecord)obj).getDOM()).setAttributeNS(XMLConstants.XMLNS_URL, XMLConstants.XMLNS + ":" + XMLConstants.XOP_PREFIX, XMLConstants.XOP_URL);
                        }
                    }
                }
            } else if (isSwaRef() && (marshaller.getAttachmentMarshaller() != null)) {
                //element should be a data-handler
                try {
                    String c_id = marshaller.getAttachmentMarshaller().addSwaRefAttachment((DataHandler)element);
                    element = c_id;
                } catch (Exception ex) {
                }
            } else {
                //inline
                if (!((getCollectionContentType() == ClassConstants.ABYTE) || (getCollectionContentType() == ClassConstants.APBYTE))) {
                    element = XMLBinaryDataHelper.getXMLBinaryDataHelper().getBytesForBinaryValue(element, marshaller, this.mimeTypePolicy.getMimeType(parent)).getData();
                }
            }
        }
        return element;
    }

    public void writeSingleValue(Object value, Object parent, XMLRecord record, AbstractSession session) {
        XMLMarshaller marshaller = record.getMarshaller();
        XMLField field = (XMLField)getField();
        NamespaceResolver resolver = field.getNamespaceResolver();
        boolean isAttribute = field.getLastXPathFragment().isAttribute();
        String prefix = null;
        XMLField includeField = null;
        if (!isAttribute) {
            if ((marshaller.getAttachmentMarshaller() != null) && marshaller.getAttachmentMarshaller().isXOPPackage() && !isSwaRef() && !shouldInlineBinaryData()) {
                field = (XMLField)getField();

                // If the field's resolver is non-null and has an entry for XOP, 
                // use it - otherwise, create a new resolver, set the XOP entry, 
                // on it, and use it instead.
                // We do this to avoid setting the XOP namespace declaration on
                // a given field or descriptor's resolver, as it is only required
                // on the current element
                if (resolver != null) {
                    prefix = resolver.resolveNamespaceURI(XMLConstants.XOP_URL);
                }
                if (prefix == null) {
                    prefix = XMLConstants.XOP_PREFIX;//"xop";
                    resolver = new NamespaceResolver();
                    resolver.put(prefix, XMLConstants.XOP_URL);
                }
                
                includeField = new XMLField(prefix + ":" + INCLUDE + "/@href");
                includeField.setNamespaceResolver(resolver);
            } else {
                XMLField textField = new XMLField(field.getXPath() + "/text()");
                textField.setNamespaceResolver(field.getNamespaceResolver());
                textField.setSchemaType(field.getSchemaType());
                field = textField;
            }
        }
        Object valueToWrite = getValueToWrite(value, parent, record, field, includeField, session);
        record.add(field, valueToWrite);
    }
    
    public Object valueFromRow(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery query, AbstractSession executionSession) {
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

            // PERF: Direct variable access.
            //Object value = row.get(field);
            //Object fieldValue = null;
            XMLUnmarshaller unmarshaller = ((XMLRecord)row).getUnmarshaller();
            if (element instanceof String) {
                if (this.isSwaRef() && (unmarshaller.getAttachmentUnmarshaller() != null)) {
                    fieldValue = unmarshaller.getAttachmentUnmarshaller().getAttachmentAsDataHandler((String)element);
                } else if (!this.isSwaRef()) {
                    //should be base64
                    byte[] bytes = XMLConversionManager.getDefaultXMLManager().convertSchemaBase64ToByteArray((String)element);
                    fieldValue = bytes;
                }
            } else {
                //this was an element, so do the XOP/SWAREF/Inline binary cases for an element
                XMLRecord record = (XMLRecord)element;

                if ((unmarshaller.getAttachmentUnmarshaller() != null) && unmarshaller.getAttachmentUnmarshaller().isXOPPackage() && !this.isSwaRef() && !this.shouldInlineBinaryData()) {
                    //look for the include element:
                    String xpath = "";

                    //  need a prefix for XOP
                    String prefix = null;
                    NamespaceResolver descriptorResolver = ((XMLDescriptor) getDescriptor()).getNamespaceResolver();
                    if (descriptorResolver != null) {
                        prefix = descriptorResolver.resolveNamespaceURI(XMLConstants.XOP_URL);
                    }
                    if (prefix == null) {
                        prefix = XMLConstants.XOP_PREFIX;
                    }
                    NamespaceResolver tempResolver = new NamespaceResolver();
                    tempResolver.put(prefix, XMLConstants.XOP_URL);
                    xpath = prefix + ":" + INCLUDE + "/@href";
                    XMLField field = new XMLField(xpath);
                    field.setNamespaceResolver(tempResolver);
                    String includeValue = (String)record.get(field);
                    if (element != null) {
                        if ((getCollectionContentType() == ClassConstants.ABYTE) || (getCollectionContentType() == ClassConstants.APBYTE)) {
                            fieldValue = unmarshaller.getAttachmentUnmarshaller().getAttachmentAsByteArray(includeValue);
                        } else {
                            fieldValue = unmarshaller.getAttachmentUnmarshaller().getAttachmentAsDataHandler(includeValue);
                        }
                    }
                } else if ((unmarshaller.getAttachmentUnmarshaller() != null) && isSwaRef()) {
                    String refValue = (String)record.get("text()");
                    if (refValue != null) {
                        fieldValue = unmarshaller.getAttachmentUnmarshaller().getAttachmentAsDataHandler(refValue);
                    }
                } else {
                    fieldValue = (String)record.get("text()");
                    //should be a base64 string
                    fieldValue = XMLConversionManager.getDefaultXMLManager().convertSchemaBase64ToByteArray((String)fieldValue);
                }
            }
            Object attributeValue = fieldValue;
            if (getValueConverter() != null) {
                if (getValueConverter() instanceof XMLConverter) {
                    attributeValue = ((XMLConverter)getValueConverter()).convertDataValueToObjectValue(fieldValue, executionSession, unmarshaller);
                } else {
                    attributeValue = getValueConverter().convertDataValueToObjectValue(fieldValue, executionSession);
                }
            }

            if (collectionContentType != null) {
                attributeValue = XMLBinaryDataHelper.getXMLBinaryDataHelper().convertObject(attributeValue, collectionContentType);
            }
            cp.addInto(attributeValue, result, query.getSession());
            //return attributeValue;
        }
        return result;
    }

    public void setCollectionContentType(Class javaClass) {
        collectionContentType = javaClass;
    }

    public Class getCollectionContentType() {
        return collectionContentType;
    }
}
