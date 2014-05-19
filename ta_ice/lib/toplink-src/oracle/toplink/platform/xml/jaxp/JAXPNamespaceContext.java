/// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.platform.xml.jaxp;

import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import oracle.toplink.exceptions.ValidationException;
import oracle.toplink.platform.xml.XMLNamespaceResolver;

/**
 * <p><b>Purpose</b>: Wrap a oracle.toplink.platform.xml.XMLNamespaceResolver 
 * and expose it as a javax.xml.namespace.NamespaceContext.</p> 
 */

public class JAXPNamespaceContext implements NamespaceContext {
	
	private XMLNamespaceResolver xmlNamespaceResolver;

	public JAXPNamespaceContext(XMLNamespaceResolver xmlNamespaceResolver) {
		this.xmlNamespaceResolver = xmlNamespaceResolver;
	}
	
	public String getNamespaceURI(String prefix) {
		return xmlNamespaceResolver.resolveNamespacePrefix(prefix); 
	}

	public String getPrefix(String namespaceURI) {
        throw ValidationException.operationNotSupported("getPrefix");
	}

	public Iterator getPrefixes(String namespaceURI) {
        throw ValidationException.operationNotSupported("getPrefixes");
	}
	
}