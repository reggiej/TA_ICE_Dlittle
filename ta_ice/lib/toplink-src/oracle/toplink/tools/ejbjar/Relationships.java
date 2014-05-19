// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import java.util.Collection;
import java.util.Vector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * INTERNAL:
 */
public class Relationships extends Description {
    Vector relationships;// 1 or more Relationship objects

    public Relationships() {
        super();
        relationships = new Vector();
    }

    public void addRelationship(Relationship relationship) {
        relationships.add(relationship);
    }

    public Vector getRelationships() {
        return relationships;
    }

    /**
      * Load the data for this instance from the specified element.
      * @param e the DOM element
      */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);
        relationships = objectsFromElement(e, EJB_RELATION, new Relationship());
    }

    public void removeRelationships(Collection rels) {
        relationships.removeAll(rels);
    }

    /**
      * Return the data from this instance as a DOM element.
      * @param doc a Document instance used to create elements
      */
    public Element toElement(Document doc) {
        Element e = doc.createElement(RELATIONSHIPS);
        inheritedFields(doc, e);
        addCollection(doc, e, relationships);
        return e;
    }
}