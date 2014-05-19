// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration;

import org.w3c.dom.*;

/**
 * INTERNAL:
 * <p>
 * <b>Purpose</b>: This class is used to enumerate over the XML elements.  This is because
 * some parsers fail to ignore whitespaces and may include them as test elements.
 * This enumerator will ensure that only NodeElements are returned.
 * @since TopLink 4.0
 * @author Gordon Yorke
 */
public class NodeListElementEnumerator {
    protected int index;
    protected NodeList list;

    public NodeListElementEnumerator(NodeList list) {
        this.index = 0;
        this.list = list;
        while ((index < list.getLength()) && (list.item(index).getNodeType() != Node.ELEMENT_NODE)) {
            ++this.index;
        }
    }

    public boolean hasMoreNodes() {
        return index < list.getLength();
    }

    public Node nextNode() {
        Node result = list.item(index);
        ++index;
        while ((index < list.getLength()) && (list.item(index).getNodeType() != Node.ELEMENT_NODE)) {
            ++this.index;
        }
        return result;
    }
}