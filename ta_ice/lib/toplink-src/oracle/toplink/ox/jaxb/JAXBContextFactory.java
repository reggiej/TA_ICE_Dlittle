// Copyright (c) 1998, 2008, Oracle and/or its affiliates. All rights reserved. 
package oracle.toplink.ox.jaxb;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBException;

import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.exceptions.ValidationException;
import oracle.toplink.internal.helper.ConversionManager;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.ox.XMLContext;
import oracle.toplink.ox.jaxb.compiler.*;
import oracle.toplink.ox.jaxb.javamodel.reflection.JavaModelImpl;
import oracle.toplink.ox.jaxb.javamodel.reflection.JavaModelInputImpl;
import oracle.toplink.sessions.Project;
/**
 * INTERNAL:
 * <p><b>Purpose:</b>A TopLink specific JAXBContextFactory. This class can be specified in a 
 * jaxb.properties file to make use of TopLink's JAXB 2.0 implementation. 
 * <p><b>Responsibilities:</b><ul>
 * <li>Create a JAXBContext from an array of Classes and a Properties object</li>
 * <li>Create a JAXBContext from a context path and a classloader</li>
 * </ul>
 * <p>This class is the entry point into in TopLink's JAXB 2.0 Runtime. It provides the required
 * factory methods and is invoked by javax.xml.bind.JAXBContext.newInstance() to create new 
 * instances of JAXBContext. When creating a JAXBContext from a contextPath, the list of classes
 * is derived either from an ObjectFactory class (schema-to-java) or a jaxb.index file (java-to-schema).
 * 
 * @author mmacivor
 * @since Oracle TopLink 11.1.1.0.0
 * @see javax.xml.bind.JAXBContext
 * @see oracle.toplink.ox.jaxb.JAXBContext
 * @see oracle.toplink.ox.jaxb.compiler.Generator
 */

public class JAXBContextFactory  {
    public static javax.xml.bind.JAXBContext createContext(Class[] classesToBeBound, java.util.Map properties) throws JAXBException {
        javax.xml.bind.JAXBContext jaxbContext = null;
        XMLContext xmlContext = null;
        Generator generator = new Generator(new JavaModelInputImpl(classesToBeBound, new JavaModelImpl()));
        try {
            Project proj = generator.generateProject();
            // need to make sure that the java class is set properly on each 
            // descriptor when using java classname - req'd for JOT api implementation 
            for (Iterator<ClassDescriptor> descriptorIt = proj.getOrderedDescriptors().iterator(); descriptorIt.hasNext(); ) {
                ClassDescriptor descriptor = descriptorIt.next();
                if (descriptor.getJavaClass() == null) {
                    descriptor.setJavaClass(ConversionManager.getDefaultManager().convertClassNameToClass(descriptor.getJavaClassName()));
                }
            }
            xmlContext = new XMLContext(proj);
            jaxbContext = new oracle.toplink.ox.jaxb.JAXBContext(xmlContext, generator);
        } catch(Exception ex) {
            throw new JAXBException(ex);
        }
        return jaxbContext;
        
    }
    
    public static javax.xml.bind.JAXBContext createContext(String contextPath, ClassLoader classLoader) throws JAXBException {
        try {
            XMLContext xmlContext = new XMLContext(contextPath, classLoader);
            return new oracle.toplink.ox.jaxb.JAXBContext(xmlContext);
        } catch(ValidationException vex) {
            if(vex.getErrorCode() != ValidationException.NO_SESSIONS_XML_FOUND && vex.getErrorCode() != ValidationException.NO_SESSION_FOUND) {
                throw new JAXBException(vex);
            }
        } catch (Exception ex) {
            throw new JAXBException(ex);
        }
        ArrayList classes = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(contextPath, ":");
        while(tokenizer.hasMoreElements()) {
            String path = tokenizer.nextToken();
            try {
                Class objectFactory = classLoader.loadClass(path + ".ObjectFactory");
                if(isJAXB2ObjectFactory(objectFactory)) {
                    classes.add(objectFactory);
                }
            } catch(Exception ex) {
                //if there's no object factory, don't worry about it. Check for jaxb.index next
            }
            try {
                //try to load package info just to be safe
                classLoader.loadClass(path + ".package-info");
            } catch(Exception ex){}
            //Next check for a jaxb.index file in case there's one available
            InputStream jaxbIndex = classLoader.getResourceAsStream(path.replace('.', '/') + "/jaxb.index");
            if(jaxbIndex != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(jaxbIndex));
                try {
                    String line = reader.readLine();
                    while(line != null) {
                        String className = path + "." + line.trim();
                        try {
                            classes.add(classLoader.loadClass(className));
                        }catch(Exception ex){
                            //just ignore for now if the class isn't available.
                        }
                        line = reader.readLine();
                    }
                } catch(Exception ex) {}
            }
        }
        if(classes.size() == 0) {
            throw new JAXBException(oracle.toplink.exceptions.JAXBException.noObjectFactoryOrJaxbIndexInPath(contextPath));
        }
        Class[] classArray = new Class[classes.size()];
        for(int i = 0; i < classes.size(); i++) {
            classArray[i] = (Class)classes.get(i);
        }
        return createContext(classArray, null);
    }  
    
    private static boolean isJAXB2ObjectFactory(Class objectFactoryClass) {
        try {
            Class xmlRegistry = PrivilegedAccessHelper.getClassForName("javax.xml.bind.annotation.XmlRegistry");
            if(objectFactoryClass.isAnnotationPresent(xmlRegistry)) {
                return true;
            }
            return false;
        } catch(Exception ex) {
            return false;
        }
    }
    
    
}
