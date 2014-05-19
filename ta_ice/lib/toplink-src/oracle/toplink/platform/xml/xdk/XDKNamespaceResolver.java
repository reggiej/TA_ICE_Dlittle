// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.platform.xml.xdk;

import oracle.toplink.platform.xml.XMLNamespaceResolver;
import oracle.xml.parser.v2.NSResolver;

/**
 * <p><b>Purpose</b>: Wrap a oracle.toplink.platform.xml.XMLNamespaceResolver 
 * and expose it as a oracle.xml.parser.v2.NSResolver.</p> 
 */

public class XDKNamespaceResolver implements NSResolver {
    private XMLNamespaceResolver xmlNamespaceResolver;

    public XDKNamespaceResolver(XMLNamespaceResolver xmlNamespaceResolver) {
        super();
        this.xmlNamespaceResolver = xmlNamespaceResolver;
    }

    public String resolveNamespacePrefix(String namespacePrefix) {
        return xmlNamespaceResolver.resolveNamespacePrefix(namespacePrefix);
    }
}