// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb;

import java.util.Iterator;

import javax.xml.bind.Binder;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;

import oracle.toplink.ox.XMLContext;
import oracle.toplink.ox.jaxb.compiler.Generator;
import oracle.toplink.ox.jaxb.compiler.MarshalCallback;
import oracle.toplink.ox.jaxb.compiler.UnmarshalCallback;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>Provide a TopLink implementation of the JAXBContext interface.
 * <p><b>Responsibilities:</b><ul>
 * <li>Create Marshaller instances</li>
 * <li>Create Unmarshaller instances</li>
 * <li>Create Binder instances</li>
 * <li>Create Introspector instances</li>
 * <li>Create Validator instances</li>
 * <li>Generate Schema Files</li>
 * <ul>
 * <p>This is the TopLink JAXB 2.0 implementation of javax.xml.bind.JAXBContext. This class
 * is created by the JAXBContextFactory and is used to create Marshallers, Unmarshallers, Validators,
 * Binders and Introspectors. A JAXBContext can also be used to create Schema Files.
 * 
 * @see javax.xml.bind.JAXBContext
 * @see oracle.toplink.ox.jaxb.JAXBMarshaller
 * @see oracle.toplink.ox.jaxb.JAXBUnmarshaller
 * @see oracle.toplink.ox.jaxb.JAXBBinder
 * @see oracle.toplink.ox.jaxb.JAXBIntrospector
 * 
 * @author mmacivor
 * @since Oracle TopLink 11.1.1.0.0
 */

public class JAXBContext extends javax.xml.bind.JAXBContext {
    private XMLContext xmlContext;
    private oracle.toplink.ox.jaxb.compiler.Generator generator;
    
    public JAXBContext(XMLContext context) {
        super();
        xmlContext = context;
    }
    
    public JAXBContext(XMLContext context, Generator generator) {
        super();
        this.xmlContext = context;
        this.generator = generator;
    }
    
    public void generateSchema(SchemaOutputResolver outputResolver) {
        if(generator == null) {
            return;
        }
        generator.generateSchemaFiles(outputResolver, null);
    }
    
    public Marshaller createMarshaller() {
    	// create a JAXBIntrospector and set it on the marshaller
        JAXBMarshaller marshaller = new JAXBMarshaller(xmlContext.createMarshaller(), new JAXBIntrospector(xmlContext));
        if (generator != null && generator.hasMarshalCallbacks()) {
            // initialize each callback in the map
            for (Iterator callIt = generator.getMarshalCallbacks().keySet().iterator(); callIt.hasNext(); ) {
                MarshalCallback cb = (MarshalCallback) generator.getMarshalCallbacks().get(callIt.next());
                // TODO:  what classloader do we want to use here?
                cb.initialize(generator.getClass().getClassLoader());
            }
            marshaller.setMarshalCallbacks(generator.getMarshalCallbacks());
        }
        return marshaller;
    }

    public Unmarshaller createUnmarshaller() {
        JAXBUnmarshaller unmarshaller = new JAXBUnmarshaller(xmlContext.createUnmarshaller());
        if (generator != null && generator.hasUnmarshalCallbacks()) {
            // initialize each callback in the map
            for (Iterator callIt = generator.getUnmarshalCallbacks().keySet().iterator(); callIt.hasNext(); ) {
                UnmarshalCallback cb = (UnmarshalCallback) generator.getUnmarshalCallbacks().get(callIt.next());
                // TODO:  what classloader do we want to use here?
                cb.initialize(generator.getClass().getClassLoader());
            }
            unmarshaller.setUnmarshalCallbacks(generator.getUnmarshalCallbacks());
        }
        return unmarshaller;
    }

    public Validator createValidator() {
        return new JAXBValidator(xmlContext.createValidator());
    }
    
    public Binder createBinder() {
        return new JAXBBinder(this.xmlContext);
    }
    
    public JAXBIntrospector createJAXBIntrospector() {
        return new JAXBIntrospector(xmlContext);
    }
}
