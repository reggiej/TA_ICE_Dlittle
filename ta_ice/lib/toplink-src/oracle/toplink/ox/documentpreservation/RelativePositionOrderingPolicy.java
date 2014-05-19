// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.documentpreservation;

import org.w3c.dom.Node;

import oracle.toplink.ox.documentpreservation.NodeOrderingPolicy;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>An implementation of NodeOrderingPolicy that adds new elements to an XML Document
 * based on the last updated sibling in their context.
 * <p><b>Responsibilities:</b>Add a new element as a child based on the provided last updated sibling.
 * 
 * @author mmacivor
 * @see oracle.toplink.ox.documentpreservation.NodeOrderingPolicy
 */
public class RelativePositionOrderingPolicy implements NodeOrderingPolicy {
    
    public void appendNode(Node parent, Node newChild, Node previousSibling){
        if(previousSibling != null) {
            Node nextSibling = previousSibling.getNextSibling();
            if(nextSibling != null) {
                parent.insertBefore(newChild, nextSibling);
            } else {
                parent.appendChild(newChild);
            }
        }
        else {
            if(parent.hasChildNodes()) {
                parent.insertBefore(newChild, parent.getFirstChild());
            } else {
                parent.appendChild(newChild);
            }
        }        
    }        
}

