// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import java.util.Vector;

/**
 * INTERNAL:
 * Represents ejb-relation element
 */
public class Relationship extends Description {
    String relationshipName;// Optional
    RelationshipRole role1;// Required
    RelationshipRole role2;// Required

    public Relationship() {
        role1 = new RelationshipRole();
        role2 = new RelationshipRole();
    }

    /**
     * @return String the relationship name String, or null if not specified
     */
    public String getRelationshipName() {
        return relationshipName;
    }

    /**
     * @return RelationshipRole the first relationship role
     */
    public RelationshipRole getRole1() {
        return role1;
    }

    /**
     * @return RelationshipRole the second relationship role
     */
    public RelationshipRole getRole2() {
        return role2;
    }
    
    public boolean hasRoleForEntityNamed(String ejbName) {
        String beanNameInRole1 = getRole1().getRelationshipRoleSource().getEjbName();
        String beanNameInRole2 = getRole2().getRelationshipRoleSource().getEjbName();
        return ((beanNameInRole1 != null) && beanNameInRole1.equals(ejbName)) || ((beanNameInRole2 != null) && beanNameInRole2.equals(ejbName));         
    }
    
    /**
     * Return true if the relationship is defined for the named ejb's cmr field.
     */
    public boolean isRelationshipForCmr(String ejbName, String cmrName) {
        return hasRoleForEntityNamed(ejbName) &&
                (getRole1().getCmrField() != null && getRole1().getCmrField().getFieldName().equals(cmrName) ||
                getRole2().getCmrField() != null && getRole2().getCmrField().getFieldName().equals(cmrName));
    }
    
    /**
     * Return true if the relationship is one-to-one
     */
     public boolean isOneToOne(){
         return getRole1().getMultiplicity().equals(EjbJarConstants.MULTIPLICITY_ONE) && getRole2().getMultiplicity().equals(EjbJarConstants.MULTIPLICITY_ONE);
     }

    /**
     * Return true if the relationship is one-to-many
     */
     public boolean isOneToMany(){
         return (getRole1().getMultiplicity().equals(EjbJarConstants.MULTIPLICITY_ONE) && getRole2().getMultiplicity().equals(EjbJarConstants.MULTIPLICITY_MANY)) ||
                (getRole2().getMultiplicity().equals(EjbJarConstants.MULTIPLICITY_ONE) && getRole1().getMultiplicity().equals(EjbJarConstants.MULTIPLICITY_MANY));  
     }
     
    /**
     * Return true if the relationship is many-to-many
     */
     public boolean isManyToMany(){
         return getRole1().getMultiplicity().equals(EjbJarConstants.MULTIPLICITY_MANY) && getRole2().getMultiplicity().equals(EjbJarConstants.MULTIPLICITY_MANY);
     }

    /**
     * @param relName the relationship name String
     */
    public void setRelationshipName(String relName) {
        relationshipName = relName;
    }

    /**
     * @param role the first RelationshipRole role
     */
    public void setRole1(RelationshipRole role) {
        role1 = role;
    }

    /**
     * @param role the second RelationshipRole role
     */
    public void setRole2(RelationshipRole role) {
        role2 = role;
    }

    /**
     * Load the data for this instance from the specified element.
     * @param e the DOM element
     */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);
        relationshipName = optionalStringFromElement(e, EJB_RELATION_NAME);
        // The DTD defines that exactly two of these will be present
        Vector v = objectsFromElement(e, EJB_RELATIONSHIP_ROLE, new RelationshipRole());
        role1 = (RelationshipRole)v.elementAt(0);
        role2 = (RelationshipRole)v.elementAt(1);
    }

    /**
     * Return the data from this instance as a DOM element.
     * @param doc a Document instance used to create elements
     */
    public Element toElement(Document doc) {
        Element e = doc.createElement(EJB_RELATION);
        inheritedFields(doc, e);

        optionallyAddText(doc, e, EJB_RELATION_NAME, relationshipName);
        e.appendChild(role1.toElement(doc));
        e.appendChild(role2.toElement(doc));
        return e;
    }
}