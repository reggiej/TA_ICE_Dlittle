// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.mappings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.xml.namespace.QName;

import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.exceptions.DescriptorException;
import oracle.toplink.exceptions.XMLMarshalException;
import oracle.toplink.internal.descriptors.DescriptorIterator;
import oracle.toplink.internal.descriptors.Namespace;
import oracle.toplink.internal.helper.ConversionManager;
import oracle.toplink.internal.helper.DatabaseField;
import oracle.toplink.internal.helper.Helper;
import oracle.toplink.internal.helper.IdentityHashtable;
import oracle.toplink.internal.ox.XMLObjectBuilder;
import oracle.toplink.internal.ox.XPathEngine;
import oracle.toplink.internal.queryframework.ContainerPolicy;
import oracle.toplink.internal.queryframework.DirectMapContainerPolicy;
import oracle.toplink.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.internal.sessions.ChangeRecord;
import oracle.toplink.internal.sessions.MergeManager;
import oracle.toplink.internal.sessions.ObjectChangeSet;
import oracle.toplink.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.mappings.DatabaseMapping;
import oracle.toplink.ox.NamespaceResolver;
import oracle.toplink.ox.XMLConstants;
import oracle.toplink.ox.XMLContext;
import oracle.toplink.ox.XMLDescriptor;
import oracle.toplink.ox.XMLField;
import oracle.toplink.ox.record.DOMRecord;
import oracle.toplink.ox.record.XMLRecord;
import oracle.toplink.queryframework.ObjectBuildingQuery;
import oracle.toplink.queryframework.ObjectLevelReadQuery;
import oracle.toplink.remote.RemoteSession;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @since Oracle TopLink 11<i>g</i>
 */
public class XMLAnyAttributeMapping extends DatabaseMapping implements XMLMapping {
    private XMLField field;
    private DirectMapContainerPolicy containerPolicy;

    public XMLAnyAttributeMapping() {
        this.containerPolicy = new DirectMapContainerPolicy(HashMap.class);
    }

    /**
    * INTERNAL:
    * Clone the attribute from the clone and assign it to the backup.
    */
    public void buildBackupClone(Object clone, Object backup, UnitOfWorkImpl unitOfWork) {
        throw DescriptorException.invalidMappingOperation(this, "buildBackupClone");
    }

    /**
    * INTERNAL:
    * Clone the attribute from the original and assign it to the clone.
    */
    public void buildClone(Object original, Object clone, UnitOfWorkImpl unitOfWork) {
        throw DescriptorException.invalidMappingOperation(this, "buildClone");
    }

    public void buildCloneFromRow(AbstractRecord Record, JoinedAttributeManager joinManager, Object clone, ObjectBuildingQuery sourceQuery, UnitOfWorkImpl unitOfWork, AbstractSession executionSession) {
        throw DescriptorException.invalidMappingOperation(this, "buildCloneFromRow");
    }

    /**
     * INTERNAL:
     * Cascade perform delete through mappings that require the cascade
     */
    public void cascadePerformRemoveIfRequired(Object object, UnitOfWorkImpl uow, IdentityHashtable visitedObjects) {
        //objects referenced by this mapping are not registered as they have
        // no identity, this is a no-op.
    }

    /**
      * INTERNAL:
      * Cascade registerNew for Create through mappings that require the cascade
      */
    public void cascadeRegisterNewIfRequired(Object object, UnitOfWorkImpl uow, IdentityHashtable visitedObjects) {
        //Our current XML support does not make use of the UNitOfWork.
    }

    public Object clone() {
        // Bug 3037701 - clone the AttributeAccessor
        XMLAnyAttributeMapping mapping = null;
        mapping = (XMLAnyAttributeMapping)super.clone();
        mapping.setContainerPolicy(this.getContainerPolicy());
        mapping.setField(this.getField());
        return mapping;
    }

    /**
    * INTERNAL:
    * This method was created in VisualAge.
    * @return prototype.changeset.ChangeRecord
    */
    public ChangeRecord compareForChange(Object clone, Object backup, ObjectChangeSet owner, AbstractSession session) {
        throw DescriptorException.invalidMappingOperation(this, "compareForChange");
    }

    /**
    * INTERNAL:
    * Compare the attributes belonging to this mapping for the objects.
    */
    public boolean compareObjects(Object firstObject, Object secondObject, AbstractSession session) {
        throw DescriptorException.invalidMappingOperation(this, "compareObjects");
    }

    /**
    * INTERNAL:
    * An object has been serialized from the server to the client.
    * Replace the transient attributes of the remote value holders
    * with client-side objects.
    */
    public void fixObjectReferences(Object object, IdentityHashtable objectDescriptors, IdentityHashtable processedObjects, ObjectLevelReadQuery query, RemoteSession session) {
        throw DescriptorException.invalidMappingOperation(this, "fixObjectReferences");
    }

    /**
    * INTERNAL:
    * Return the mapping's containerPolicy.
    */
    public ContainerPolicy getContainerPolicy() {
        return containerPolicy;
    }

    public DatabaseField getField() {
        return field;
    }

    public void initialize(AbstractSession session) throws DescriptorException {
        if (getField() != null) {
            setField(getDescriptor().buildField(getField()));
        }
        ContainerPolicy cp = getContainerPolicy();
        if (cp != null && cp.getContainerClass() == null) {
            Class cls = ConversionManager.getDefaultManager().convertClassNameToClass(cp.getContainerClassName());
            cp.setContainerClass(cls);
        }
    }

    /**
    * INTERNAL:
    * Iterate on the appropriate attribute value.
    */
    public void iterate(DescriptorIterator iterator) {
        throw DescriptorException.invalidMappingOperation(this, "iterate");
    }

    public void setXPath(String xpath) {
        this.field = new XMLField(xpath);
    }

    /**
    * INTERNAL:
    * Merge changes from the source to the target object.
    */
    public void mergeChangesIntoObject(Object target, ChangeRecord changeRecord, Object source, MergeManager mergeManager) {
        throw DescriptorException.invalidMappingOperation(this, "mergeChangesIntoObject");
    }

    /**
    * INTERNAL:
    * Merge changes from the source to the target object.
    */
    public void mergeIntoObject(Object target, boolean isTargetUninitialized, Object source, MergeManager mergeManager) {
        throw DescriptorException.invalidMappingOperation(this, "mergeIntoObject");
    }

    public void setContainerPolicy(ContainerPolicy cp) {
        if (!cp.isDirectMapPolicy()) {
            throw DescriptorException.invalidContainerPolicy(cp, this.getClass());
        }
        this.containerPolicy = (DirectMapContainerPolicy)cp;
    }

    public void setField(DatabaseField field) {
        this.field = (XMLField)field;
    }

    public Object valueFromRow(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery sourceQuery, AbstractSession executionSession) throws DatabaseException {
        XMLRecord record = (XMLRecord)row;

        if (getField() != null) {
            //Get the nested row represented by this field to build the collection from
            Object nested = record.get(getField());
            if (nested instanceof Vector) {
                nested = ((Vector)nested).firstElement();
            }
            if (!(nested instanceof XMLRecord)) {
                return null;
            }
            record = (XMLRecord)nested;
        }
        return buildObjectValuesFromDOMRecord((DOMRecord)record, executionSession, sourceQuery);
    }

    private Object buildObjectValuesFromDOMRecord(DOMRecord record, AbstractSession session, ObjectBuildingQuery query) {
        //This DOMRecord represents the root node of the AnyType instance
        //Grab ALL children to populate the collection.
        DirectMapContainerPolicy cp = (DirectMapContainerPolicy)getContainerPolicy();
        Object container = cp.containerInstance();
        org.w3c.dom.Element root = (Element)record.getDOM();
        NamedNodeMap attributes = root.getAttributes();
        Attr next;
        String localName;
        for (int i = 0; i < attributes.getLength(); i++) {
            next = (Attr)attributes.item(i);
            localName = next.getLocalName();
            if(null == localName) {
                localName = next.getName();
            }
            QName key = new QName(next.getNamespaceURI(), localName);
            String value = next.getValue();
            cp.addInto(key, value, container, session);
        }
        return container;
    }

    protected XMLDescriptor getDescriptor(XMLRecord xmlRecord, AbstractSession session) throws XMLMarshalException {
        XMLContext xmlContext = xmlRecord.getUnmarshaller().getXMLContext();
        QName rootQName = new QName(xmlRecord.getNamespaceURI(), xmlRecord.getLocalName());
        XMLDescriptor xmlDescriptor = xmlContext.getDescriptor(rootQName);
        if (null == xmlDescriptor) {
            throw XMLMarshalException.noDescriptorWithMatchingRootElement(xmlRecord.getLocalName());
        }
        return xmlDescriptor;
    }

    public void writeFromObjectIntoRow(Object object, AbstractRecord row, AbstractSession session) throws DescriptorException {
        if (this.isReadOnly()) {
            return;
        }
        Object attributeValue = this.getAttributeValueFromObject(object);
        writeSingleValue(attributeValue, object, (XMLRecord)row, session);
    }

    protected AbstractRecord buildCompositeRow(Object attributeValue, AbstractSession session, AbstractRecord parentRow) {
        XMLDescriptor referenceDescriptor = (XMLDescriptor)session.getDescriptor(attributeValue.getClass());
        if ((referenceDescriptor != null) && (referenceDescriptor.getDefaultRootElement() != null)) {
            XMLObjectBuilder objectBuilder = (XMLObjectBuilder)referenceDescriptor.getObjectBuilder();
            return objectBuilder.buildRow(attributeValue, session, referenceDescriptor.buildField(referenceDescriptor.getDefaultRootElement()), (XMLRecord)parentRow);
        }
        return null;
    }

    public boolean isXMLMapping() {
        return true;
    }

    public Vector getFields() {
        return this.collectFields();
    }

    public void useMapClass(Class concreteMapClass) {
        if (!Helper.classImplementsInterface(concreteMapClass, Map.class)) {
            throw DescriptorException.illegalContainerClass(concreteMapClass);
        }
        this.containerPolicy.setContainerClass(concreteMapClass);
    }
    public void writeSingleValue(Object attributeValue, Object parent, XMLRecord row, AbstractSession session) {
        DirectMapContainerPolicy cp = (DirectMapContainerPolicy)this.getContainerPolicy();
        if ((attributeValue == null) || (cp.sizeFor(attributeValue) == 0)) {
            return;
        }
        DOMRecord record = (DOMRecord)row;
        if(record.getDOM().getNodeType() != Node.ELEMENT_NODE) {
            return;
        }
        Element root = (Element)record.getDOM();

        if (field != null) {
            root = (Element)XPathEngine.getInstance().create((XMLField)getField(), root);
        }
        List extraNamespaces = new ArrayList();
        NamespaceResolver nr = row.getNamespaceResolver();
        for (Object iter = cp.iteratorFor(attributeValue); cp.hasNext(iter);) {
            Object key = cp.next(iter, session);
            if ((key != null) && key instanceof QName) {
                Object value = cp.valueFromKey(key, attributeValue);
                QName attributeName = (QName)key;
                String namespaceURI = attributeName.getNamespaceURI();
                String qualifiedName = attributeName.getLocalPart();
                                
                if (nr != null) {
                    String prefix = nr.resolveNamespaceURI(attributeName.getNamespaceURI());
                    if ((prefix != null) && !prefix.equals("")) {
                        qualifiedName = prefix + ":" + qualifiedName;
                    }else if(attributeName.getNamespaceURI() != null && !attributeName.getNamespaceURI().equals("")){
                        String generatedPrefix = nr.generatePrefix();
                        qualifiedName = generatedPrefix + ":" + qualifiedName;
                        nr.put(generatedPrefix,attributeName.getNamespaceURI());
                        extraNamespaces.add(new Namespace(generatedPrefix,attributeName.getNamespaceURI()));                  
                        row.getNamespaceResolver().put(generatedPrefix,attributeName.getNamespaceURI());                    
                    }
                }
                if (namespaceURI != null) {
                    root.setAttributeNS(namespaceURI, qualifiedName, value.toString());
                } else {
                    root.setAttribute(attributeName.getLocalPart(), value.toString());
                }
            }
        }
        ((XMLObjectBuilder)descriptor.getObjectBuilder()).writeExtraNamespaces(extraNamespaces, row);
        ((XMLObjectBuilder)descriptor.getObjectBuilder()).removeExtraNamespacesFromNamespaceResolver(row, extraNamespaces, session);
    }
    
    /**
     * INTERNAL:
     * Indicates the Map class to be used.  
     *  
     * @param concreteMapClass
     */

    /**
     * INTERNAL:
     * Indicates the name of the Map class to be used.  
     *  
     * @param concreteMapClassName
     */
    public void useMapClassName(String concreteMapClassName) {
        this.setContainerPolicy(new DirectMapContainerPolicy());
        this.getContainerPolicy().setContainerClassName(concreteMapClassName);
    }
}
