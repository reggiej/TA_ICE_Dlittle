// Copyright (c) 1998, 2007, Oracle. All rights reserved.
package oracle.toplink.ox.mappings;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Vector;
import java.util.ArrayList;

import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.exceptions.DescriptorException;
import oracle.toplink.exceptions.ValidationException;
import oracle.toplink.internal.descriptors.DescriptorIterator;
import oracle.toplink.internal.helper.ClassConstants;
import oracle.toplink.internal.helper.DatabaseField;
import oracle.toplink.internal.helper.IdentityHashtable;
import oracle.toplink.internal.queryframework.CollectionContainerPolicy;
import oracle.toplink.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedClassForName;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.internal.sessions.ChangeRecord;
import oracle.toplink.internal.sessions.MergeManager;
import oracle.toplink.internal.sessions.ObjectChangeSet;
import oracle.toplink.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.internal.ox.XMLChoiceFieldToClassAssociation;
import oracle.toplink.mappings.DatabaseMapping;
import oracle.toplink.ox.XMLField;
import oracle.toplink.ox.record.XMLRecord;
import oracle.toplink.queryframework.ObjectBuildingQuery;
import oracle.toplink.queryframework.ObjectLevelReadQuery;
import oracle.toplink.remote.RemoteSession;
import oracle.toplink.internal.queryframework.ContainerPolicy;


/**
 * PUBLIC:
 * <p><b>Purpose:</b>Provide a mapping that can map a single attribute to a number of
 * different elements in an XML Document. This will be used to map to Choices or Substitution
 * Groups in an XML Schema
 * <p><b>Responsibilities:</b><ul>
 * <li>Allow the user to specify XPath -> Type mappings</li>
 * <li>Handle reading and writing of XML Documents containing a collection of choice or substitution
 * group elements</li>
 * </ul>
 * <p>The XMLChoiceCollectionMapping is the collection version of the XMLChoiceMapping. This mapping
 * allows the user to specify a number of different xpaths, and types associated with those xpaths. 
 * When any of these elements are encountered in the XML Document, they are read in as the correct
 * type and added to the collection.
 * <p><b>Setting up XPath mappings:</b>Unlike other OXM Mappings, instead of setting a single xpath,
 * the addChoiceElement method is used to specify an xpath and the type assocated with this xpath.
 * <br>
 * xmlChoiceCollectionMapping.addChoiceElement("mystring/text()", String.class);
 * <br>
 * xmlChoiceCollectionMapping.addChoiceElement("myaddress", Address.class);
 * 
 */

public class XMLChoiceCollectionMapping extends DatabaseMapping implements XMLMapping {
    private Map<XMLField, Class> fieldToClassMappings;
    private Map<Class, XMLField> classToFieldMappings;
    private Map<XMLField, XMLMapping> choiceElementMappings;
    private Map<XMLField, String> fieldToClassNameMappings;
    private ContainerPolicy containerPolicy;
    
    public XMLChoiceCollectionMapping() {
        fieldToClassMappings = new HashMap<XMLField, Class>();
        fieldToClassNameMappings = new HashMap<XMLField, String>();
        classToFieldMappings = new HashMap<Class, XMLField>();
        choiceElementMappings = new HashMap<XMLField, XMLMapping>();
        this.containerPolicy = ContainerPolicy.buildPolicyFor(ClassConstants.Vector_class);
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

     public void buildCloneFromRow(AbstractRecord databaseRow, JoinedAttributeManager joinManager, Object clone, ObjectBuildingQuery sourceQuery, UnitOfWorkImpl unitOfWork, AbstractSession executionSession) {
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
       * Iterate on the appropriate attribute value.
       */
     public void iterate(DescriptorIterator iterator) {
         throw DescriptorException.invalidMappingOperation(this, "iterate");
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

     public Object valueFromRow(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery sourceQuery, AbstractSession executionSession) throws DatabaseException {
         return null;
     }
            
     public void writeFromObjectIntoRow(Object object, AbstractRecord row, AbstractSession session) throws DescriptorException {
           
     }
        
     public void writeSingleValue(Object value, Object parent, XMLRecord row, AbstractSession session) {
         
     }
        
     public boolean isXMLMapping() {
         return true;
     }

     public Vector<DatabaseField> getFields() {
         return this.collectFields();
     }

     protected Vector<DatabaseField> collectFields() {
         Vector<DatabaseField> fields = new Vector<DatabaseField>(getFieldToClassMappings().keySet());
         return fields;
     }
        
     public void addChoiceElement(String xpath, Class elementType) {
         XMLField field = new XMLField(xpath);
         addChoiceElement(field, elementType);
     }
        
        public void addChoiceElement(XMLField xmlField, Class elementType) {
            getFieldToClassMappings().put(xmlField, elementType);
            this.fieldToClassNameMappings.put(xmlField, elementType.getName());
            if(classToFieldMappings.get(elementType) == null) {
                classToFieldMappings.put(elementType, xmlField);
            }
        }
        
        public void addChoiceElement(String xpath, String elementTypeName) {
            XMLField field = new XMLField(xpath);
            addChoiceElement(field, elementTypeName);
        }
        
        public void addChoiceElement(XMLField xmlField, String elementTypeName) {
            this.fieldToClassNameMappings.put(xmlField, elementTypeName);
        }
        
        
        public Map<XMLField, Class> getFieldToClassMappings() {
            return fieldToClassMappings;
        }
        public void initialize(AbstractSession session) throws DescriptorException {
            super.initialize(session);
            if(this.fieldToClassMappings.size() == 0) {
                this.convertClassNamesToClasses(oracle.toplink.internal.helper.ConversionManager.getDefaultManager().getLoader());
            }

            //create mappings for each field.
            Iterator<XMLField> fields = getFieldToClassMappings().keySet().iterator();
            while(fields.hasNext()) {
                XMLField next = fields.next();
                if(next.getLastXPathFragment().nameIsText()) {
                    //if it's a simple value, create a Direct Mapping
                    XMLCompositeDirectCollectionMapping xmlMapping = new XMLCompositeDirectCollectionMapping();
                    xmlMapping.setAttributeName(this.getAttributeName());
                    xmlMapping.setAttributeAccessor(this.getAttributeAccessor());
                    xmlMapping.setAttributeElementClass(getFieldToClassMappings().get(next));
                    xmlMapping.setField(next);
                    xmlMapping.setDescriptor(this.getDescriptor());
                    xmlMapping.setContainerPolicy(getContainerPolicy());
                    this.choiceElementMappings.put(next, xmlMapping);
                    xmlMapping.initialize(session);
                } else {
                    XMLCompositeCollectionMapping xmlMapping = new XMLCompositeCollectionMapping();
                    xmlMapping.setAttributeName(this.getAttributeName());
                    xmlMapping.setAttributeAccessor(this.getAttributeAccessor());
                    xmlMapping.setReferenceClass(getFieldToClassMappings().get(next));
                    xmlMapping.setField(next);
                    xmlMapping.setDescriptor(this.getDescriptor());
                    xmlMapping.setContainerPolicy(getContainerPolicy());
                    this.choiceElementMappings.put(next, xmlMapping);
                    xmlMapping.initialize(session);
                }
            }
        }
        
        public Map<Class, XMLField> getClassToFieldMappings() {
            return classToFieldMappings;
        }
        
        public Map<XMLField, XMLMapping> getChoiceElementMappings() {
            return choiceElementMappings;
        }

        public ContainerPolicy getContainerPolicy() {
            return containerPolicy;
        }

        public void setContainerPolicy(ContainerPolicy cp) {
            this.containerPolicy = cp;
        }
        
        public void useCollectionClass(Class concreteContainerClass) {
            this.setContainerPolicy(ContainerPolicy.buildPolicyFor(concreteContainerClass));
        }
        
        public void useCollectionClassName(String concreteContainerClassName) {
            this.setContainerPolicy(new CollectionContainerPolicy(concreteContainerClassName));
        }

        public void convertClassNamesToClasses(ClassLoader classLoader){
            Iterator<XMLField> xpaths = fieldToClassNameMappings.keySet().iterator();
            while(xpaths.hasNext()) {
                XMLField xpath = xpaths.next();
                String className = fieldToClassNameMappings.get(xpath);
                Class elementType = null;
                try{
                    if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                        try {
                            elementType = (Class)AccessController.doPrivileged(new PrivilegedClassForName(className, true, classLoader));
                        } catch (PrivilegedActionException exception) {
                            throw ValidationException.classNotFoundWhileConvertingClassNames(className, exception.getException());
                        }
                    } else {
                        elementType = oracle.toplink.internal.security.PrivilegedAccessHelper.getClassForName(className, true, classLoader);
                    }
                } catch (ClassNotFoundException exc){
                    throw ValidationException.classNotFoundWhileConvertingClassNames(className, exc);
                }
                addChoiceElement(xpath, elementType);
            }
        }
        
        public ArrayList getChoiceFieldToClassAssociations() {
            ArrayList associations = new ArrayList();
            if(this.fieldToClassNameMappings.size() > 0) {
                for(XMLField xmlField:this.fieldToClassNameMappings.keySet()) {
                    String className = this.fieldToClassNameMappings.get(xmlField);
                    XMLChoiceFieldToClassAssociation association = new XMLChoiceFieldToClassAssociation(xmlField, className);
                    associations.add(association);
                }
            }
            return associations;
        }
        
     public void setChoiceFieldToClassAssociations(ArrayList associations) {
         if(associations.size() > 0) {
             for(Object next:associations) {
                 XMLChoiceFieldToClassAssociation association = (XMLChoiceFieldToClassAssociation)next;
                 this.addChoiceElement(association.getXmlField(), association.getClassName());
             }
         }
     }
}