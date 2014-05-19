// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.platform.xml;

import java.util.ArrayList;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementation of the org.w3c.dom.NodeList interface
 */
public class XMLNodeList implements NodeList {
    private ArrayList nodes;

    public XMLNodeList() {
        nodes = new ArrayList();
    }

    public XMLNodeList(int size) {
        nodes = new ArrayList(size);
    }

    public int getLength() {
        return nodes.size();
    }

    public Node item(int i) {
        return (Node)nodes.get(i);
    }

    public void add(Node node) {
        nodes.add(node);
    }

    public void addAll(NodeList nodelist) {
        int size = nodelist.getLength();
        for (int i = 0; i < size; i++) {
            nodes.add(nodelist.item(i));
        }
    }
}