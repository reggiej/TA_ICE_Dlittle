// Copyright (c) 1998, 2008, Oracle. All rights reserved.
package oracle.toplink.ox.jaxb;

import java.io.OutputStream;
import java.io.Writer;
import java.io.File;
import java.util.HashMap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

import oracle.toplink.ox.XMLConstants;
import oracle.toplink.ox.XMLMarshaller;
import oracle.toplink.ox.XMLRoot;

import oracle.toplink.ox.jaxb.attachment.*;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>To Provide an implementation of the JAXB 2.0 Marshaller Interface
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Provide a JAXB wrapper on the XMLMarshaller API</li>
 * <li>Perform Object to XML Conversions</li>
 * </ul>
 * <p>This implementation of the JAXB 2.0 Marshaller interface provides the required functionality
 * by acting as a thin wrapper on the existing XMLMarshaller API.
 *
 * @author mmacivor
 * @since Oracle TopLink 11.1.1.0.0
 * @see javax.xml.bind.Marshaller
 * @see oracle.toplink.ox.XMLMarshaller
 */

public class JAXBMarshaller implements javax.xml.bind.Marshaller {
	private ValidationEventHandler validationEventHandler;
	private XMLMarshaller xmlMarshaller;
    public static final String XML_JAVATYPE_ADAPTERS = "xml-javatype-adapters";

	/**
	 * This constructor initializes various settings on the XML marshaller, and
	 * stores the provided JAXBIntrospector instance for usage in marshal()
	 * calls.
	 * 
	 * @param newXMLMarshaller
	 * @param newIntrospector
	 */
	public JAXBMarshaller(XMLMarshaller newXMLMarshaller, JAXBIntrospector newIntrospector) {
		super();
		validationEventHandler = new DefaultValidationEventHandler();
		xmlMarshaller = newXMLMarshaller;
		xmlMarshaller.setEncoding("UTF-8");
		xmlMarshaller.setFormattedOutput(false);
		JAXBMarshalListener listener = new JAXBMarshalListener(this);
		xmlMarshaller.setMarshalListener(listener);
	}

	/**
	 * Create an instance of XMLRoot populated from the contents of the provided
	 * JAXBElement. XMLRoot will be used to hold the contents of the JAXBElement
	 * while the marshal operation is performed by TopLink OXM. This will avoid
	 * adding any runtime dependencies to TopLink.
	 * 
	 * @param elt
	 * @return
	 */
	private XMLRoot createXMLRootFromJAXBElement(JAXBElement elt) {
		// create an XMLRoot to hand into the marshaller
		XMLRoot xmlroot = new XMLRoot();
		xmlroot.setObject(elt.getValue());
		QName qname = elt.getName();
		xmlroot.setLocalName(qname.getLocalPart());
		xmlroot.setNamespaceURI(qname.getNamespaceURI());
		return xmlroot;
	}

	public XmlAdapter getAdapter(Class javaClass) {
        HashMap result = (HashMap) xmlMarshaller.getProperty(XML_JAVATYPE_ADAPTERS);
        if (result == null) {
            return null;
        }
        return (XmlAdapter) result.get(javaClass);
	}
	
	public AttachmentMarshaller getAttachmentMarshaller() {
		return ((AttachmentMarshallerAdapter)xmlMarshaller.getAttachmentMarshaller()).getAttachmentMarshaller();
	}

	public ValidationEventHandler getEventHandler() throws JAXBException {
		return validationEventHandler;
	}

	public Marshaller.Listener getListener() {
		return ((JAXBMarshalListener) xmlMarshaller.getMarshalListener()).getListener();
	}
	
	public Node getNode(Object object) throws JAXBException {
		throw new UnsupportedOperationException();
	}

	public Object getProperty(String key) throws PropertyException {
		if (key == null) {
			throw new IllegalArgumentException();
		} else if (JAXB_FORMATTED_OUTPUT.equals(key)) {
			return new Boolean(xmlMarshaller.isFormattedOutput());
		} else if (JAXB_ENCODING.equals(key)) {
			return xmlMarshaller.getEncoding();
		} else if (JAXB_SCHEMA_LOCATION.equals(key)) {
			return xmlMarshaller.getSchemaLocation();
		} else if (JAXB_NO_NAMESPACE_SCHEMA_LOCATION.equals(key)) {
			return xmlMarshaller.getNoNamespaceSchemaLocation();
		} else if (XMLConstants.JAXB_FRAGMENT.equals(key)) {
			return new Boolean(xmlMarshaller.isFragment());
		}
		throw new PropertyException("Unsupported Property");
	}

	public Schema getSchema() {
		try {
            //TODO: Need to generate a proper schema and convert to Sun Schema model
			return SchemaFactory.newInstance(XMLConstants.SCHEMA_URL).newSchema();
		} catch (org.xml.sax.SAXException ex) {
		}
		return null;
	}

	public void marshal(Object object, ContentHandler contentHandler) throws JAXBException {
        if (object == null || contentHandler == null) {
            throw new IllegalArgumentException();
        }
		// TODO:  in our case, we only care if the object is an instance
        // of JAXBElement, and do not need to use the introspector...
        // let the JAXBIntrospector determine if the object is a JAXBElement
		//if (jaxbIntrospector.isElement(object)) {
            // use the JAXBElement's properties to populate an XMLRoot
            // object = createXMLRootFromJAXBElement((JAXBElement) object);
        //}

        if (object instanceof JAXBElement) {
            // use the JAXBElement's properties to populate an XMLRoot
			object = createXMLRootFromJAXBElement((JAXBElement) object);
		}
		try {
			xmlMarshaller.marshal(object, contentHandler);
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}

	// TODO: add support for StAX
	public void marshal(Object object, XMLEventWriter eventWriter) {
	}

	public void marshal(Object object, Node node) throws JAXBException {
        if (object == null || node == null) {
            throw new IllegalArgumentException();
        }
		// let the JAXBIntrospector determine if the object is a JAXBElement
		if (object instanceof JAXBElement) {
			// use the JAXBElement's properties to populate an XMLRoot
			object = createXMLRootFromJAXBElement((JAXBElement) object);
		}
		try {
			xmlMarshaller.marshal(object, node);
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}
	
	public void marshal(Object object, OutputStream outputStream) throws JAXBException {
        if (object == null || outputStream == null) {
            throw new IllegalArgumentException();
        }
		// let the JAXBIntrospector determine if the object is a JAXBElement
		if (object instanceof JAXBElement) {
			// use the JAXBElement's properties to populate an XMLRoot
			object = createXMLRootFromJAXBElement((JAXBElement) object);
		}
		try {
			xmlMarshaller.marshal(object, outputStream);
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}

	public void marshal(Object object, File file) throws JAXBException {
		try {
			java.io.FileWriter writer = new java.io.FileWriter(file);
			marshal(object, writer);
		} catch(Exception ex) {
			throw new MarshalException(ex);
		}
	}

	public void marshal(Object object, Result result) throws JAXBException {
        if (object == null || result == null) {
            throw new IllegalArgumentException();
        }
		// let the JAXBIntrospector determine if the object is a JAXBElement
		if (object instanceof JAXBElement) {
			// use the JAXBElement's properties to populate an XMLRoot
			object = createXMLRootFromJAXBElement((JAXBElement) object);
		}
		try {
			xmlMarshaller.marshal(object, result);
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}

	// TODO: add support for StAX
	public void marshal(Object object, XMLStreamWriter streamWriter) {
	}

	public void marshal(Object object, Writer writer) throws JAXBException {
        if (object == null || writer == null) {
            throw new IllegalArgumentException();
        }
		// let the JAXBIntrospector determine if the object is a JAXBElement
		if (object instanceof JAXBElement) {
			// use the JAXBElement's properties to populate an XMLRoot
			object = createXMLRootFromJAXBElement((JAXBElement) object);
		}
		try {
			xmlMarshaller.marshal(object, writer);
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}

	public void setAdapter(Class javaClass, XmlAdapter adapter) {
        HashMap result = (HashMap) xmlMarshaller.getProperty(XML_JAVATYPE_ADAPTERS);
        if (result == null) {
            result = new HashMap();
            xmlMarshaller.getProperties().put(XML_JAVATYPE_ADAPTERS, result);
        }
        result.put(javaClass, adapter);
	}

	public void setAdapter(XmlAdapter adapter) {
        setAdapter(adapter.getClass(), adapter);
	}

	public void setAttachmentMarshaller(AttachmentMarshaller attachmentMarshaller) {
		xmlMarshaller.setAttachmentMarshaller(new AttachmentMarshallerAdapter(attachmentMarshaller));
	}

	public void setEventHandler(ValidationEventHandler newValidationEventHandler) throws JAXBException {
		if (null == newValidationEventHandler) {
			validationEventHandler = new DefaultValidationEventHandler();
		} else {
			validationEventHandler = newValidationEventHandler;
		}
	}
	
	public void setListener(Marshaller.Listener listener) {
		((JAXBMarshalListener) xmlMarshaller.getMarshalListener()).setListener(listener);
	}

	public void setMarshalCallbacks(java.util.HashMap callbacks) {
		((JAXBMarshalListener) xmlMarshaller.getMarshalListener()).setClassBasedMarshalEvents(callbacks);
	}

	public void setProperty(String key, Object value) throws PropertyException {
		try {
			if (key == null) {
				throw new IllegalArgumentException();
			} else if (JAXB_FORMATTED_OUTPUT.equals(key)) {
				Boolean formattedOutput = (Boolean) value;
				xmlMarshaller.setFormattedOutput(formattedOutput.booleanValue());
			} else if (JAXB_ENCODING.equals(key)) {
				xmlMarshaller.setEncoding((String) value);
			} else if (JAXB_SCHEMA_LOCATION.equals(key)) {
				xmlMarshaller.setSchemaLocation((String) value);
			} else if (JAXB_NO_NAMESPACE_SCHEMA_LOCATION.equals(key)) {
				xmlMarshaller.setNoNamespaceSchemaLocation((String) value);
			} else if (XMLConstants.JAXB_FRAGMENT.equals(key)) {
				Boolean fragment = (Boolean) value;
				xmlMarshaller.setFragment(fragment.booleanValue());
			} else {
				throw new PropertyException(key, value);
			}
		} catch (ClassCastException exception) {
			throw new PropertyException(key, exception);
		}
	}

	public void setSchema(Schema schema) {
	}
}
