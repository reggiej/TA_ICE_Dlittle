// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * INTERNAL:
 */
public class RelationshipRole extends Description {
    String roleName;
    String multiplicity;// Required
    boolean cascadeDelete;
    RelationshipRoleSource roleSource;// Required
    CmrField cmrField;

    public RelationshipRole() {
        multiplicity = EjbJarConstants.MULTIPLICITY_ONE;// - SOMETHING has to be default
        roleSource = new RelationshipRoleSource();
    }

    /**
     * @return boolean true if deletes should be cascaded, false if not [specified]
     */
    public boolean getCascadeDelete() {
        return cascadeDelete;
    }

    /**
     * @return CmrField the container-managed relationship field, or null if not specified
     */
    public CmrField getCmrField() {
        return cmrField;
    }

    /**
     * @return String the multiplicity String of this relationship
     */
    public String getMultiplicity() {
        if (multiplicity == null) {
            ;
        } else if (multiplicity.equalsIgnoreCase(EjbJarConstants.MULTIPLICITY_ONE)) {
            multiplicity = EjbJarConstants.MULTIPLICITY_ONE;
        } else if (multiplicity.equalsIgnoreCase(EjbJarConstants.MULTIPLICITY_MANY)) {
            multiplicity = EjbJarConstants.MULTIPLICITY_MANY;
        }

        return multiplicity;
    }

    /**
     * @return String the role name String, or null if not specified
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * @return RelationshipRoleSource the role source
     */
    public RelationshipRoleSource getRelationshipRoleSource() {
        return roleSource;
    }

    /**
     * @param flag boolean true if deletes should be cascaded, false if not
     */
    public void setCascadeDelete(boolean flag) {
        cascadeDelete = flag;
    }

    /**
     * @param cmrFld the cmr field
     */
    public void setCmrField(CmrField cmrFld) {
        cmrField = cmrFld;
    }

    /**
     * @param mult the multiplicity String
     */
    public void setMultiplicity(String mult) {
        multiplicity = mult;
    }

    /**
     * @param roleNm the role name String
     */
    public void setRoleName(String roleNm) {
        roleName = roleNm;
    }

    /**
     * @param rSource the role source
     */
    public void setRelationshipRoleSource(RelationshipRoleSource rSource) {
        roleSource = rSource;
    }

    /**
     * Load the data for this instance from the specified element.
     * @param e the DOM element
     */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);
        roleName = optionalStringFromElement(e, EJB_RELATIONSHIP_ROLE_NAME);
        multiplicity = stringFromElement(e, MULTIPLICITY);
        // This option gets set simply by defining it in XML (no value req'd)
        cascadeDelete = getFirstElementByTagName(CASCADE_DELETE, e) != null;
        roleSource = (RelationshipRoleSource)objectFromElement(e, RELATIONSHIP_ROLE_SOURCE, new RelationshipRoleSource());
        cmrField = (CmrField)optionalObjectFromElement(e, CMR_FIELD, new CmrField());
    }

    /**
     * Return the data from this instance as a DOM element.
     * @param doc a Document instance used to create elements
     */
    public Element toElement(Document doc) {
        Element e = doc.createElement(EJB_RELATIONSHIP_ROLE);
        inheritedFields(doc, e);

        optionallyAddText(doc, e, EJB_RELATIONSHIP_ROLE_NAME, roleName);
        addText(doc, e, MULTIPLICITY, multiplicity);

        if (cascadeDelete) {
            e.appendChild(doc.createElement(CASCADE_DELETE));
        }

        e.appendChild(roleSource.toElement(doc));

        if (cmrField != null) {
            e.appendChild(cmrField.toElement(doc));
        }

        return e;
    }
}