// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.jaxb.compiler;

import javax.xml.bind.annotation.XmlAccessType;

import oracle.toplink.ox.NamespaceResolver;

/**
 *  INTERNAL:
 *  <p><b>Purpose:</b>To store some information about a schema's target namespace and some additional
 *  information gathered from XmlSchema annotation at the package (namespace) level
 *  <p><b>Responsibilities:</b><ul>
 *  <li>Store target namespace and namespace prefix information for a specific schema</li>
 *  <li>Store some additional Schema information (such as element/attribute form and XmlAccessType)</li>
 *  </ul>
 *  
 *  @see oracle.toplink.ox.jaxb.compiler.AnnotationsProcessor
 *  @author mmacivor
 *  @since Oracle TopLink 11.1.1.0.0
 */

public class NamespaceInfo {
    private String namespace;
    private boolean attributeFormQualified = false;
    private boolean elementFormQualified = false;
    private NamespaceResolver namespaceResolver;
    private XmlAccessType accessType = XmlAccessType.PUBLIC_MEMBER;
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String ns) {
        this.namespace = ns;
    }
    
    public boolean isAttributeFormQualified() {
        return attributeFormQualified;
    }
    
    public void setAttributeFormQualified(boolean b) {
        attributeFormQualified = b;
    }

    public boolean isElementFormQualified() {
        return elementFormQualified;
    }
    
    public void setElementFormQualified(boolean b) {
        elementFormQualified = b;
    }
    
    public NamespaceResolver getNamespaceResolver() {
        return namespaceResolver;
    }
    
    public void setNamespaceResolver(NamespaceResolver resolver) {
        namespaceResolver = resolver;
    }
    
    public XmlAccessType getAccessType() {
        return accessType;
    }
    public void setAccessType(XmlAccessType type) {
        this.accessType = type;
    }
    
}