// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.codegen;

import java.util.*;

/**
 * INTERNAL:
 */
public class HierarchyNode {
    //  the class that this node represents
    public String className;
    public HierarchyNode parent;
    public ArrayList children;

    /**
     * This member will hold the different definition types that should be implemented by the code generated children
     * Used mostly in CMP code generation
     */
    public ArrayList definitions;

    public HierarchyNode(String className) {
        this.className = className;
        this.children = new ArrayList();
        this.definitions = new ArrayList();
    }

    public void setParent(HierarchyNode parent) {
        this.parent = parent;
        this.parent.addChild(this);
    }

    public void addChild(HierarchyNode child) {
        if (!this.children.contains(child)) {
            this.children.add(child);
        }
    }

    public List getChildren() {
        return this.children;
    }

    public HierarchyNode getParent() {
        return this.parent;
    }

    public String getClassName() {
        return this.className;
    }

    public String toString() {
        String result = "HierarchyNode:\n\t" + className + "\n" + children + "\n end HierarchyNode\n";
        return result;
    }
}