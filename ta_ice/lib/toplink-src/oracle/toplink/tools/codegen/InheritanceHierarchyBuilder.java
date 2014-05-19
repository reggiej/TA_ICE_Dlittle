// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.codegen;

import oracle.toplink.sessions.Project;
import oracle.toplink.descriptors.ClassDescriptor;

import java.util.*;

/**
 * INTERNAL:
 */
public class InheritanceHierarchyBuilder {

    /**
     * INTERNAL:
     * Based on a class name either return a pre-existing node from the hierarchyTree or build one and
     * add it to the tree.
     */
    public static HierarchyNode getNodeForClass(String className, Hashtable hierarchyTree) {
        HierarchyNode node = (HierarchyNode)hierarchyTree.get(className);
        if (node == null) {
            node = new HierarchyNode(className);
            hierarchyTree.put(className, node);
        }
        return node;
    }

    public static Hashtable buildInheritanceHierarchyTree(Project project) {
        Map descriptors = project.getDescriptors();
        Hashtable hierarchyTree = new Hashtable(descriptors.size());
        for (Iterator descriptorIterator = descriptors.values().iterator();
                 descriptorIterator.hasNext();) {
            ClassDescriptor descriptor = (ClassDescriptor)descriptorIterator.next();
            String className = descriptor.getJavaClassName();
            if (className == null) {
                className = descriptor.getJavaClass().getName();
            }
            HierarchyNode node = getNodeForClass(className, hierarchyTree);
            if (descriptor.hasInheritance() && (descriptor.getInheritancePolicy().getParentClassName() != null)) {
                HierarchyNode parentNode = getNodeForClass(descriptor.getInheritancePolicy().getParentClassName(), hierarchyTree);
                node.setParent(parentNode);
            }
        }
        return hierarchyTree;
    }
}