// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.documentpreservation;

import org.w3c.dom.Node;

import oracle.toplink.ox.documentpreservation.NodeOrderingPolicy;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>Provide an implementation of NodeOrderingPolicy that simply appends the new child
 * element to the parent. This is the default for DescriptorLevelDocumentPreservationPolicy and 
 * NoDocumentPreservationPolicy
 * 
 * @see oracle.toplink.ox.documentpreservation.NodeOrderingPolicy
 * @author mmacivor
 *
 */
public class AppendNewElementsOrderingPolicy implements NodeOrderingPolicy {
    public void appendNode(Node parent, Node newChild, Node previousSibling) {
        parent.appendChild(newChild);
    }    
}

