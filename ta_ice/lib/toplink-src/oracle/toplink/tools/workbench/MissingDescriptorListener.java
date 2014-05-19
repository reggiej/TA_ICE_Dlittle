// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.tools.workbench;

import java.security.AccessController;
import java.security.PrivilegedActionException;

import oracle.toplink.sessions.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.queryframework.Call;
import oracle.toplink.eis.EISObjectPersistenceXMLProject;
import oracle.toplink.xdb.XDBObjectPersistenceXMLProject;
import oracle.toplink.internal.ox.OXMObjectPersistenceRuntimeXMLProject;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedClassForName;

/**
 * INTERNAL:
 * Event listener class used to lazy-load the descriptors for EIS and XDB,
 * as they have external jar dependencies that may not be on the classpath.
 *
 * @since TopLink 10
 * @author James Sutherland
 */
public class MissingDescriptorListener extends SessionEventAdapter {
    protected static String XML_TYPE_CLASS = "oracle.toplink.xdb.DirectToXMLTypeMapping";
    protected static String EIS_DESCRIPTOR_CLASS = "oracle.toplink.eis.EISDescriptor";
    protected static String XML_INTERACTION_CLASS = "oracle.toplink.eis.interactions.XMLInteraction";
    protected static String EIS_LOGIN_CLASS = "oracle.toplink.eis.EISLogin";
    protected static String XML_BINARY_MAPPING_CLASS = "oracle.toplink.ox.mappings.XMLBinaryDataMapping";

    public void missingDescriptor(SessionEvent event) {
        String name = ((Class)event.getResult()).getName();
        DatabaseSession session = ((DatabaseSession)event.getSession());
        if (name.equals(XML_TYPE_CLASS)) {
            session.addDescriptors(new XDBObjectPersistenceXMLProject());
        }
        if (name.equals(EIS_DESCRIPTOR_CLASS) || name.equals(XML_INTERACTION_CLASS) || name.equals(EIS_LOGIN_CLASS)) {
            try {
                Class javaClass = null;
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try{
                        javaClass = (Class) AccessController.doPrivileged(new PrivilegedClassForName(XML_INTERACTION_CLASS));
                    }catch (PrivilegedActionException ex){
                        if (ex.getCause() instanceof ClassNotFoundException){
                            throw (ClassNotFoundException) ex.getCause();
                        }
                        throw (RuntimeException) ex.getCause();
                    }
                }else{
                    javaClass = PrivilegedAccessHelper.getClassForName(XML_INTERACTION_CLASS);
                }
                session.getDescriptor(Call.class).getInheritancePolicy().addClassIndicator(javaClass, "toplink:xml-interaction");
            } catch (Exception classLoadFailure) {
                throw ValidationException.fatalErrorOccurred(classLoadFailure);
            }
            session.addDescriptors(new EISObjectPersistenceXMLProject());
        }
        if(name.equals(XML_BINARY_MAPPING_CLASS)) {
            session.addDescriptors(new OXMObjectPersistenceRuntimeXMLProject());
        }
    }
}
