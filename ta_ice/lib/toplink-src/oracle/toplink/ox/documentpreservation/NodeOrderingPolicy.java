// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.documentpreservation;

import org.w3c.dom.Node;

/**
 * <p><b>Purpose:</b>Provide an interface that specifies how new XML Elements are added to an 
 * existing XML Document.
 * 
 * @see oracle.toplink.ox.documentpreservation.DocumentPreservationPolicy
 * @author mmacivor
 *
 */
public interface NodeOrderingPolicy {
    public void appendNode(Node parent, Node newChild, Node previousSibling);
}
