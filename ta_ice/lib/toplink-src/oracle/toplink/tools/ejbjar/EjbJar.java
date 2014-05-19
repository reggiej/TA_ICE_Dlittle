// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import java.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * INTERNAL:
 * Root object that stores all deployment descriptor
 * info contained in an ejb-jar.xml file.
 */
public class EjbJar extends Description {
    String displayName;// Optional
    String smallIcon;// Optional
    String largeIcon;// Optional
    Vector enterpriseObjects;// 1 or more EnterpriseObject objects
    Relationships relationships;// Optional
    AssemblyDescriptor assemblyDescriptor;// Optional
    String ejbClientJar;// Optional

    //XML Schema stuff
    String xmlns;
    String xsi;
    String schemaLocation;
    String version;

    public EjbJar() {
        enterpriseObjects = new Vector();
    }

    /**
     * @param v a collection of concrete entity, session or message-driven objects
     */
    public void addEnterpriseObjects(Vector v) {
        if (enterpriseObjects == null) {
            enterpriseObjects = new Vector();
        }
        enterpriseObjects.addAll(v);
    }

    public void addEntity(Entity entity) {
        if (enterpriseObjects == null) {
            enterpriseObjects = new Vector();
        }
        enterpriseObjects.add(entity);
    }

    public void addRelationship(Relationship relationship) {
        if (relationships == null) {
            relationships = new Relationships();
        }
        relationships.addRelationship(relationship);
    }

    /**
     * @return AssemblyDescriptor the assembly descriptor object, or null if not set
     */
    public AssemblyDescriptor getAssemblyDescriptor() {
        return assemblyDescriptor;
    }

    /**
     * @return String the client jar String, or null if not set
     */
    public String getEjbClientJar() {
        return ejbClientJar;
    }

    /**
     * @return String the display name String, or null if not set
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return Vector collection of EnterpriseObject objects
     */
    public Vector getEnterpriseObjects() {
        return enterpriseObjects;
    }

    /**
     * @return Vector collection of Entity objects (empty if none exist)
     */
    public Vector getEntities() {
        Vector entities = new Vector();
        EnterpriseObject entObj;

        for (java.util.Iterator iter = enterpriseObjects.iterator(); iter.hasNext();) {
            entObj = (EnterpriseObject)iter.next();
            if (entObj.isEntity()) {
                entities.add(entObj);
            }
        }

        return entities;
    }

    public Entity getEntityForEjbClass(String ejbClass) {
        Iterator entIt = getEntities().iterator();
        while (entIt.hasNext()) {
            Entity nextEnt = (Entity)entIt.next();
            if (nextEnt.getEjbClass().equals(ejbClass)) {
                return nextEnt;
            }
        }
        return null;
    }

    public Entity getEntityForEjbName(String ejbName) {
        Iterator entIt = getEntities().iterator();
        while (entIt.hasNext()) {
            Entity nextEnt = (Entity)entIt.next();
            if (nextEnt.getEjbName().equals(ejbName)) {
                return nextEnt;
            }
        }
        return null;
    }

    /**
     * @return String the largeIcon String, or null if not set
     */
    public String getLargeIcon() {
        return largeIcon;
    }

    /**
     * @return Vector collection of MessageDriven objects (empty if none exist)
     */
    public Vector getMessageDrivens() {
        Vector messageDrivens = new Vector();
        EnterpriseObject entObj = null;

        for (java.util.Iterator iter = enterpriseObjects.iterator(); iter.hasNext();) {
            entObj = (EnterpriseObject)iter.next();
            if (entObj.isMessageDriven()) {
                messageDrivens.add(entObj);
            }
        }

        return messageDrivens;
    }

    /**
     * @return Relationships holds the individual relationship objects, null if none
     */
    public Relationships getRelationships() {
        return relationships;
    }

    /**
     * @return Vector collection of Session objects (empty if none exist)
     */
    public Vector getSessions() {
        Vector sessions = new Vector();
        EnterpriseObject entObj;

        for (java.util.Iterator iter = enterpriseObjects.iterator(); iter.hasNext();) {
            entObj = (EnterpriseObject)iter.next();
            if (entObj.isSession()) {
                sessions.add(entObj);
            }
        }

        return sessions;
    }

    /**
     * @return String the smallIcon String, or null if not set
     */
    public String getSmallIcon() {
        return smallIcon;
    }

    /**
     * Create a new instance loaded with all of the data from
     * the specified document obtained by parsing a ejb-jar XML file.
     *
     * @param doc the DOM document created by the XML parser
     * @return EjbJar the model object that holds the data
     */
    public static EjbJar loadFromDocument(Document doc) {
        EjbJar ejbJar = new EjbJar();

        // The following code segment loads the document element
        //      into EJBJar object, ignoring all the XML PI, DOCTYPE
        ejbJar.loadFromElement(doc.getDocumentElement());
        return ejbJar;
    }

    public void removeEntity(Entity entity) {
        enterpriseObjects.remove(entity);
    }

    public void removeRelationships(Collection rels) {
        if (relationships != null) {
            relationships.removeRelationships(rels);
        } else {
            return;
        }

        if (relationships.getRelationships().size() == 0) {
            relationships = null;
        }
    }

    /**
     * @param String the display name String
     */
    public void setDisplayName(String s) {
        displayName = s;
    }

    /**
     * @param String the client jar String
     */
    public void setEjbClientJar(String s) {
        ejbClientJar = s;
    }

    /**
     * @param Vector the enterprise objects
     */
    public void setEnterpriseObjects(Vector enterpiseObjects) {
        this.enterpriseObjects = enterpiseObjects;
    }

    /**
     * @param String the largeIcon String
     */
    public void setLargeIcon(String s) {
        largeIcon = s;
    }

    /**
     * @param r set the relationship objects
     */
    public void setRelationships(Relationships r) {
        relationships = r;
    }
    
    /**
     * Convenience api to locate the relationship with the specified cmr field.
     */
    public Relationship getRelasionshipOfCmr(String ejbName, String cmrName) {
       Relationship foundRel = null;
       if(relationships != null) {
           for(Iterator relIter = relationships.getRelationships().iterator(); relIter.hasNext(); ){
               Relationship rel = (Relationship) relIter.next();
               
               if(rel.isRelationshipForCmr(ejbName, cmrName) ){
                    //found the relationship defining the cmr
                   foundRel = rel;
               }
           }
       }
       return foundRel;
     }
     

    /**
     * @param String the smallIcon String
     */
    public void setSmallIcon(String s) {
        smallIcon = s;
    }

    /**
      * Load the data for this instance from the specified element.
      * @param e the root DOM element for an ejb-jar file
      */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);
        displayName = optionalStringFromElement(e, DISPLAY_NAME);
        smallIcon = optionalStringFromElement(e, SMALL_ICON);
        largeIcon = optionalStringFromElement(e, LARGE_ICON);

        //XML Schema stuff
        xmlns = optionalAttributeFromElement(e, XMLNS);
        xsi = optionalAttributeFromElement(e, XMLNS_XSI);
        schemaLocation = optionalAttributeFromElement(e, XSI_SCHEMALOCATION);
        version = optionalAttributeFromElement(e, VERSION);

        // Add any of the entity, message-driven or session objects
        Vector v = optionalObjectsFromElement(e, ENTITY, new Entity());
        if (v != null) {
            addEnterpriseObjects(v);
        }

        v = optionalObjectsFromElement(e, MESSAGE_DRIVEN, new MessageDriven());

        if (v != null) {
            addEnterpriseObjects(v);
        }

        v = optionalObjectsFromElement(e, SESSION, new Session());

        if (v != null) {
            addEnterpriseObjects(v);
        }

        relationships = (Relationships)optionalObjectFromElement(e, RELATIONSHIPS, new Relationships());
        assemblyDescriptor = (AssemblyDescriptor)optionalObjectFromElement(e, ASSEMBLY_DESCRIPTOR, new AssemblyDescriptor());
        ejbClientJar = optionalStringFromElement(e, EJB_CLIENT_JAR);
    }

    /**
     * Return the data from this instance as a DOM element.
     * @param doc a Document instance used to create elements
     */
    public Element toElement(Document doc) {
        // Bug2792054 root is built on doc element instead of creating a new root. 
        Element root = doc.getDocumentElement();
        inheritedFields(doc, root);

        // Optional fluff
        optionallyAddText(doc, root, DISPLAY_NAME, displayName);
        optionallyAddText(doc, root, SMALL_ICON, smallIcon);
        optionallyAddText(doc, root, LARGE_ICON, largeIcon);

        //XML Schema stuff
        optionallyAddAttribute(root, XMLNS, xmlns);
        optionallyAddAttribute(root, XMLNS_XSI, xsi);
        optionallyAddAttribute(root, XSI_SCHEMALOCATION, schemaLocation);
        optionallyAddAttribute(root, VERSION, version);

        // Convert the entity, message-driven and session objects
        Element enterpriseElt = doc.createElement(ENTERPRISE_BEANS);
        root.appendChild(enterpriseElt);
        optionallyAddCollection(doc, enterpriseElt, getEntities());
        optionallyAddCollection(doc, enterpriseElt, getSessions());
        optionallyAddCollection(doc, enterpriseElt, getMessageDrivens());

        // Convert the relationships if present
        if (relationships != null) {
            root.appendChild(relationships.toElement(doc));
        }

        // Convert the assembly descriptor
        if (assemblyDescriptor != null) {
            root.appendChild(assemblyDescriptor.toElement(doc));
        }

        optionallyAddText(doc, root, EJB_CLIENT_JAR, ejbClientJar);
        return root;
    }

    /**
     * Return true if the ejb-jar.xml uses XML Schema rather than dtd
     */
    public boolean usesXmlSchema() {
        return (xmlns != null);
    }

    /**
     * Schema setting
     */
    public void setXmlns(String newXmlns) {
        xmlns = newXmlns;
    }

    /**
     * Schema setting
     */
    public void setXsi(String newXsi) {
        xsi = newXsi;
    }

    /**
     * Schema setting
     */
    public void setSchemaLocation(String newSchemaLocation) {
        schemaLocation = newSchemaLocation;
    }

    /**
     * Schema setting
     */
    public void setVersion(String newVersion) {
        version = newVersion;
    }

    /**
     * Initialize ejb-jar element for ejb 2.1 schema
     */
    public void initializeEjb21Schema() {
        xmlns = XMLManager.XML_NAMESPACE;
        xsi = XMLManager.XML_SCHEMA_INSTANCE;
        schemaLocation = XMLManager.XML_SCHEMA_LOCATION;
        version = "2.1";
    }
}