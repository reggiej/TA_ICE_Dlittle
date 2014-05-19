// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.xdb;

import java.util.*;

import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.mappings.*;
import oracle.toplink.sessions.*;
import oracle.toplink.ox.XMLDescriptor;
import oracle.toplink.ox.NamespaceResolver;
import oracle.toplink.ox.mappings.*;

/**
 * INTERNAL:
 * Define the TopLink project and descriptor information to read a TopLink project from an XML file.
 * The XDB meta-data must be defined seperately as it has seperate jar dependency that must not be required if not using XDB.
 */
public class XDBObjectPersistenceXMLProject extends Project {

    /**
     * INTERNAL:
     * Return a new descriptor project.
     */
    public XDBObjectPersistenceXMLProject() {
        addDescriptor(buildDirectToXMLTypeMappingDescriptor());

        // Set the namespaces on all descriptors.
        NamespaceResolver namespaceResolver = new NamespaceResolver();
        namespaceResolver.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        namespaceResolver.put("xsd", "http://www.w3.org/2001/XMLSchema");
        namespaceResolver.put("opm", "http://xmlns.oracle.com/ias/xsds/opm");
        namespaceResolver.put("toplink", "http://xmlns.oracle.com/ias/xsds/toplink");

        for (Iterator descriptors = getDescriptors().values().iterator(); descriptors.hasNext();) {
            XMLDescriptor descriptor = (XMLDescriptor)descriptors.next();
            descriptor.setNamespaceResolver(namespaceResolver);
        }
    }

    protected ClassDescriptor buildDirectToXMLTypeMappingDescriptor() {
        XMLDescriptor descriptor = new XMLDescriptor();

        descriptor.setJavaClass(DirectToXMLTypeMapping.class);
        descriptor.descriptorIsAggregate();
        descriptor.getInheritancePolicy().setParentClass(DirectToFieldMapping.class);

        XMLDirectMapping directtofieldmapping = new XMLDirectMapping();
        directtofieldmapping.setAttributeName("shouldReadWholeDocument");
        directtofieldmapping.setGetMethodName("shouldReadWholeDocument");
        directtofieldmapping.setSetMethodName("setShouldReadWholeDocument");
        directtofieldmapping.setXPath("toplink:read-whole-document/text()");
        directtofieldmapping.setNullValue(new Boolean(false));
        descriptor.addMapping(directtofieldmapping);

        return descriptor;
    }
}