// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.documentpreservation;

import oracle.toplink.ox.mappings.XMLMapping;

import org.w3c.dom.Node;

/**
 * <b>Purpose:</b>Provides an interface for customizing how Documents are
 * preserved.
 * <p><b>Responsibilities:</b><ul>
 * <li>Access objects from the cache based on node</li>
 * <li>Access nodes from the cache based on Object</li>
 * <li>Add objects and nodes to the cache</li>
 * <li>Allow the configuration of how nodes are added into the preserved doc</li>
 * 
 * @author mmacivor
 * @since Oracle TopLink 11g
 */
public abstract class DocumentPreservationPolicy {
    private NodeOrderingPolicy nodeOrderingPolicy;
    
    /**
     * PUBLIC:
     * Sets the NodeOrderingPolicy to be used by this DocumentPreservationPolicy
     * when adding new elements into a cached XML Document.
     * @see AppendNewElementsOrderingPolicy
     * @see IgnoreNewElementsOrderingPolicy
     * @see RelativePositionNodeOrderingPolicy
     */
    public void setNodeOrderingPolicy(NodeOrderingPolicy policy) {
        this.nodeOrderingPolicy = policy;
    }
    public NodeOrderingPolicy getNodeOrderingPolicy() {
        return nodeOrderingPolicy;
    }
    
    public abstract void addObjectToCache(Object obj, Node node);
    
    public abstract void addObjectToCache(Object obj, Node node, XMLMapping selfRecordMapping);
        
    public abstract Node getNodeForObject(Object obj);
    
    public abstract Object getObjectForNode(Node node);
    
    public abstract Object getObjectForNode(Node node, XMLMapping selfRecordMapping);
    
    public abstract boolean shouldPreserveDocument();
    
}
