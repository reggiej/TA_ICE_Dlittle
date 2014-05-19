// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.documentpreservation;

import org.w3c.dom.Node;

import oracle.toplink.ox.documentpreservation.NodeOrderingPolicy;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>An implementation of NodeOrderingPolicy that ignores any new elements when
 * update a cached document. This is used for the JAXB 2.0 Binder implementation. 
 * @author mmacivor
 *
 */
public class IgnoreNewElementsOrderingPolicy implements NodeOrderingPolicy {
    public void appendNode(Node parent, Node newChild, Node previousSibling) {
        //no op
    }      
}
