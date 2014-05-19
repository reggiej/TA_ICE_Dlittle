// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.platform.xml;

import java.security.AccessController;
import java.security.PrivilegedActionException;

import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedGetClassLoaderForClass;
import oracle.toplink.internal.security.PrivilegedNewInstanceFromClass;

public class XMLPlatformFactory {
    public static final String XML_PLATFORM_PROPERTY = "toplink.xml.platform";
    public static final String XDK_PLATFORM_CLASS_NAME = "oracle.toplink.platform.xml.xdk.XDKPlatform";
    public static final String JAXP_PLATFORM_CLASS_NAME = "oracle.toplink.platform.xml.jaxp.JAXPPlatform";
    private static XMLPlatformFactory instance;
    private Class xmlPlatformClass;

    private XMLPlatformFactory() {
        super();
    }

    /**
     * INTERNAL:
     * Return the singleton instance of XMLPlatformContext.
     * @return the the singleton instance of XMLPlatformContext.
     * @throws XMLPlatformException
     */
    public static XMLPlatformFactory getInstance() throws XMLPlatformException {
        if (null == instance) {
            instance = new XMLPlatformFactory();
        }
        return instance;
    }

    /**
     * INTERNAL:
     * Return the implementation class for the XMLPlatform.
     * @return the implementation class for the XMLPlatform.
     * @throws XMLPlatformException
     */
    public Class getXMLPlatformClass() throws XMLPlatformException {
        if (null != xmlPlatformClass) {
            return xmlPlatformClass;
        }

        String newXMLPlatformClassName = System.getProperty(XML_PLATFORM_PROPERTY);
        if (null == newXMLPlatformClassName) {
            newXMLPlatformClassName = XDK_PLATFORM_CLASS_NAME;
        }

        try {
            ClassLoader classLoader = null;
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try{
                    classLoader = (ClassLoader)AccessController.doPrivileged(new PrivilegedGetClassLoaderForClass(this.getClass()));
                }catch (PrivilegedActionException ex){
                    throw (RuntimeException) ex.getCause();
                }
            }else{
                classLoader = PrivilegedAccessHelper.getClassLoaderForClass(this.getClass());
            }
            // ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            // ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Class newXMLPlatformClass = classLoader.loadClass(newXMLPlatformClassName);
            setXMLPlatformClass(newXMLPlatformClass);
            return xmlPlatformClass;
        } catch (ClassNotFoundException e) {
            throw XMLPlatformException.xmlPlatformClassNotFound(newXMLPlatformClassName, e);
        }
    }

    /**
     * PUBLIC:
     * Set the implementation of XMLPlatform.
     */
    public void setXMLPlatformClass(Class xmlPlatformClass) {
        this.xmlPlatformClass = xmlPlatformClass;
    }

    /**
     * INTERNAL:
     * Return the XMLPlatform based on the toplink.xml.platform System property.
     * @return an instance of XMLPlatform
     * @throws XMLPlatformException
     */
    public XMLPlatform getXMLPlatform() throws XMLPlatformException {
        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try{
                    return (XMLPlatform)AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(getXMLPlatformClass()));
                }catch (PrivilegedActionException ex){
                    throw (RuntimeException) ex.getCause();
                }
            }else{
                return (XMLPlatform)PrivilegedAccessHelper.newInstanceFromClass(getXMLPlatformClass());
                
            }
        } catch (IllegalAccessException e) {
            throw XMLPlatformException.xmlPlatformCouldNotInstantiate(getXMLPlatformClass().getName(), e);
        } catch (InstantiationException e) {
            throw XMLPlatformException.xmlPlatformCouldNotInstantiate(getXMLPlatformClass().getName(), e);
        }
    }
}
