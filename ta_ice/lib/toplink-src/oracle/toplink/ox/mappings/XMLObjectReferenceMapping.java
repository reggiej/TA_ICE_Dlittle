// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.mappings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.namespace.QName;

import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.exceptions.ConversionException;
import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.exceptions.DescriptorException;
import oracle.toplink.internal.descriptors.ObjectBuilder;
import oracle.toplink.internal.helper.ConversionManager;
import oracle.toplink.internal.helper.ClassConstants;
import oracle.toplink.internal.helper.DatabaseField;
import oracle.toplink.internal.helper.IdentityHashtable;
import oracle.toplink.internal.ox.Reference;
import oracle.toplink.internal.ox.ReferenceResolver;
import oracle.toplink.internal.ox.XMLConversionManager;
import oracle.toplink.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.mappings.AggregateMapping;
import oracle.toplink.ox.XMLDescriptor;
import oracle.toplink.ox.XMLField;
import oracle.toplink.ox.XMLUnionField;
import oracle.toplink.ox.record.UnmarshalRecord;
import oracle.toplink.ox.record.XMLRecord;
import oracle.toplink.queryframework.ObjectBuildingQuery;

/**
 * TopLink OXM version of a 1-1 mapping.  A list of source-target key field
 * associations is used to link the source xpaths to their related target xpaths, 
 * and hence their primary key (unique identifier) values used when (un)marshalling.
 * This mapping has a Vector of XMLFields as opposed to a single XMLField.
 * 
 * It is important to note that each target xpath is assumed to be set as a primary
 * key field on the target (reference) class descriptor - this is necessary in order
 * to locate the correct target object instance in the session cache when resolving
 * mapping references.
 */
public class XMLObjectReferenceMapping extends AggregateMapping implements XMLMapping {
	protected HashMap sourceToTargetKeyFieldAssociations;
	protected Vector sourceToTargetKeys;  // maintain the order of the keys

	/**
	 * PUBLIC:
	 * The default constructor initializes the sourceToTargetKeyFieldAssociations
	 * and sourceToTargetKeys data structures.
	 */
	public XMLObjectReferenceMapping() {
		sourceToTargetKeyFieldAssociations = new HashMap();
		sourceToTargetKeys = new Vector();
	}

	/**
	 * PUBLIC:
	 * Add a source-target xpath pair to the map.
	 * 
	 * @param srcXPath
	 * @param tgtXPath
	 */
	public void addSourceToTargetKeyFieldAssociation(String srcXPath, String tgtXPath) {
		XMLField srcFld = new XMLField(srcXPath);
		sourceToTargetKeys.add(srcFld);
		sourceToTargetKeyFieldAssociations.put(srcFld, new XMLField(tgtXPath));
	}
    
    /**    
	 * INTERNAL:
	 * Retrieve the target object's primary key value that is mapped to a given
	 * source xpath (in the source-target key field association list).
	 * 
	 * @param sourceObject
	 * @param xmlFld
	 * @param session
	 * @return null if the target object is null, the reference class is null, or
	 * a primary key field name does not exist on the reference descriptor that
	 * matches the target field name - otherwise, return the associated primary 
	 * key value   
	 */
	public Object buildFieldValue(Object targetObject, XMLField xmlFld, AbstractSession session) {
		if (targetObject == null || getReferenceClass() == null) {
			return null;
		}
		ClassDescriptor descriptor = getReferenceDescriptor();
		ObjectBuilder objectBuilder = descriptor.getObjectBuilder();
		Vector pks = objectBuilder.extractPrimaryKeyFromObject(targetObject, session);
		int idx = descriptor.getPrimaryKeyFields().indexOf((XMLField) getSourceToTargetKeyFieldAssociations().get(xmlFld));
		if (idx == -1) {
			return null;
		}
		return pks.get(idx);
	}
	
    /**
     * INTERNAL:
     * Create (if necessary) and populate a reference object that will be used
     * during the mapping reference resolution phase after unmarshalling is
     * complete.
     * 
     * @param record
     * @param xmlField
     * @param object
     * @param session
     * @return
     */
    public void buildReference(UnmarshalRecord record, XMLField xmlField, Object object, AbstractSession session) {
    	ReferenceResolver resolver = ReferenceResolver.getInstance(session);
        if (resolver == null) {
            return;
        }
        
    	Object srcObject = record.getCurrentObject();
        // the order in which the primary keys are added to the vector is
        // relevant for cache lookup - it must match the ordering of the 
        // reference descriptor's primary key entries
        ClassDescriptor clsDescriptor = session.getClassDescriptor(getReferenceClass());
        Vector pkFieldNames = clsDescriptor.getPrimaryKeyFieldNames();
    	// if reference is null, create a new instance and set it on the resolver
        Reference reference = resolver.getReference(this, srcObject);
    	if (reference == null) {
    		Vector pks = new Vector();
    		pks.setSize(pkFieldNames.size());
    		reference = new Reference(this, srcObject, getReferenceClass(), pks);
    		resolver.addReference(reference);
    		record.reference(reference);
    	}
		XMLField tgtFld = (XMLField) getSourceToTargetKeyFieldAssociations().get(xmlField);
        int idx = pkFieldNames.indexOf(tgtFld.getXPath());
		Vector primaryKeys = reference.getPrimaryKeys();
        // fix for bug# 5687430
        // need to get the actual type of the target (i.e. int, String, etc.) 
        // and use the converted value when checking the cache.
        Object value = XMLConversionManager.getDefaultXMLManager().convertObject(
                object, clsDescriptor.getTypedField(tgtFld).getType());
        if (value != null) {
            primaryKeys.setElementAt(value, idx);
        }
    }
    
	/**
     * INTERNAL:
     * Cascade perform delete through mappings that require the cascade
     */
    public void cascadePerformRemoveIfRequired(Object object, UnitOfWorkImpl uow, IdentityHashtable visitedObjects) {
        // objects referenced by this mapping are not registered as they have
        // no identity, however mappings from the referenced object may need cascading.
        Object objectReferenced = getRealAttributeValueFromObject(object, uow);
        if (objectReferenced == null) {
            return;
        }
        if (!visitedObjects.containsKey(objectReferenced)) {
            visitedObjects.put(objectReferenced, objectReferenced);
            ObjectBuilder builder = getReferenceDescriptor(objectReferenced.getClass(), uow).getObjectBuilder();
            builder.cascadePerformRemove(objectReferenced, uow, visitedObjects);
        }
    }

    /**
     * INTERNAL:
     * Cascade registerNew for Create through mappings that require the cascade
     */
    public void cascadeRegisterNewIfRequired(Object object, UnitOfWorkImpl uow, IdentityHashtable visitedObjects) {
        // Aggregate objects are not registered but their mappings should be.
        Object objectReferenced = getRealAttributeValueFromObject(object, uow);
        if (objectReferenced == null) {
            return;
        }
        if (!visitedObjects.containsKey(objectReferenced)) {
            visitedObjects.put(objectReferenced, objectReferenced);
            ObjectBuilder builder = getReferenceDescriptor(objectReferenced.getClass(), uow).getObjectBuilder();
            builder.cascadeRegisterNewForCreate(objectReferenced, uow, visitedObjects);
        }
    }
    
    /**
     * INTERNAL:
     * Return a list of XMLFields based on the source XPath values
     * in the source-target key field associations list.
     */
    public Vector getFields() {
    	return sourceToTargetKeys;
    }

    /**
     * Return a QName representation the schema type for a given XMLField, if
     * applicable.
     * 
     * Note:  This method performs the same functionality as 'getSchemaType' in 
     * oracle.toplink.internal.ox.XMLSimpleMappingNodeValue.
     * 
     * @param xmlField
     * @param value
     * @return
     */
    protected QName getSchemaType(XMLField xmlField, Object value) {
        QName schemaType = null;
        if (xmlField.isTypedTextField()) {
            schemaType = xmlField.getXMLType(value.getClass());
        } else if (xmlField.isUnionField()) {
            return getSingleValueToWriteForUnion((XMLUnionField)xmlField, value);
        } else if (xmlField.getSchemaType() != null) {
            schemaType = xmlField.getSchemaType();
        }
        return schemaType;
    }

    /**
     * Return a single QName representation for a given XMLUnionField, if applicable.
     * 
     * Note:  This method performs the same functionality as 'getSingleValueToWriteForUnion'
     * in oracle.toplink.internal.ox.XMLSimpleMappingNodeValue.
     *  
     * @param xmlField
     * @param value
     * @return
     */
    protected QName getSingleValueToWriteForUnion(XMLUnionField xmlField, Object value) {
        ArrayList schemaTypes = xmlField.getSchemaTypes();
        QName schemaType = null;
        QName nextQName;
        Class javaClass;
        for (int i = 0; i < schemaTypes.size(); i++) {
            nextQName = (QName)((XMLUnionField)xmlField).getSchemaTypes().get(i);
            try {
                if (nextQName != null) {
                    javaClass = xmlField.getJavaClass(nextQName);
                    value = XMLConversionManager.getDefaultXMLManager().convertObject(value, javaClass, nextQName);
                    schemaType = nextQName;
                    break;
                }
            } catch (ConversionException ce) {
                if (i == (schemaTypes.size() - 1)) {
                    schemaType = nextQName;
                }
            }
        }
        return schemaType;
    }

    /**
	 * INTERNAL:
	 * Return a list of source-target xmlfield pairs.
	 * 
	 * @return
	 */
	public HashMap getSourceToTargetKeyFieldAssociations() {
		return sourceToTargetKeyFieldAssociations;
	}
    
    
    /**
     * Return a string representation of a given value, based on a given schema type. 
     * 
     * Note:  This method performs the same functionality as 'getValueToWrite'
     * in oracle.toplink.internal.ox.XMLSimpleMappingNodeValue.
     * 
     * @param schemaType
     * @param value
     * @return
     */
    protected String getValueToWrite(QName schemaType, Object value) {
        return (String)XMLConversionManager.getDefaultXMLManager().convertObject(value, ClassConstants.STRING, schemaType);
    }

    /**
     * INTERNAL:
     * Register a ReferenceResolver as an event listener on the session, 
     * if one doesn't already exist.  Each source/target field will have
     * a namespace resolver set as well. 
     * 
     * @see oracle.toplink.internal.ox.ReferenceResolver
     * @see oracle.toplink.ox.NamespaceResolver
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        if (getReferenceClass()==null) {
            setReferenceClass(ConversionManager.getDefaultManager().convertClassNameToClass(getReferenceClassName()));
        }
        super.initialize(session);

        ReferenceResolver resolver = new ReferenceResolver();
        if (!(session.getEventManager().getListeners().contains(resolver))) {
            session.getEventManager().addListener(resolver);
        }
        
        // iterate over each source & target XMLField and set the 
        // appropriate namespace resolver
        XMLDescriptor descriptor = (XMLDescriptor) this.getDescriptor();
        XMLDescriptor targetDescriptor = (XMLDescriptor) getReferenceDescriptor();
        for (int index = 0; index < sourceToTargetKeys.size(); index++) {
            XMLField sourceField = (XMLField) sourceToTargetKeys.get(index);
            sourceField = (XMLField)descriptor.buildField(sourceField);
            sourceToTargetKeys.set(index, sourceField);
            XMLField targetField = (XMLField) sourceToTargetKeyFieldAssociations.get(sourceField);
            targetField = (XMLField)targetDescriptor.buildField(targetField);
            sourceToTargetKeyFieldAssociations.put(sourceField, targetField);
        }
    }
    
    /**
	 * INTERNAL:
	 * Indicates that this is an XML mapping.
	 */
	public boolean isXMLMapping() {
		return true;
	}
	    
    /**
     * INTERNAL:
     * Extract the primary key values from the row, then create an 
     * oracle.toplink.internal.ox.Reference instance and store it 
     * on the session's oracle.toplink.internal.ox.ReferenceResolver.
     */
    public Object readFromRowIntoObject(AbstractRecord databaseRow, JoinedAttributeManager joinManager, Object targetObject, ObjectBuildingQuery sourceQuery, AbstractSession executionSession) throws DatabaseException {
        // the order in which the primary keys are added to the vector is
    	// relevant for cache lookup - it must match the ordering of the 
    	// reference descriptor's primary key entries
    	ClassDescriptor descriptor = sourceQuery.getSession().getClassDescriptor(getReferenceClass());
    	Vector pkFieldNames = descriptor.getPrimaryKeyFieldNames();
		Vector primaryKeys = new Vector();
		primaryKeys.setSize(pkFieldNames.size());
		Iterator keyIt = sourceToTargetKeys.iterator();
		while (keyIt.hasNext()) {
	    	XMLField keyFld = (XMLField) keyIt.next();
			XMLField tgtFld = (XMLField) getSourceToTargetKeyFieldAssociations().get(keyFld);
	    	int idx = pkFieldNames.indexOf(tgtFld.getXPath());
            if (idx == -1) {
                continue;
            }
            // fix for bug# 5687430
            // need to get the actual type of the target (i.e. int, String, etc.) 
            // and use the converted value when checking the cache.
            Object value = XMLConversionManager.getDefaultXMLManager().convertObject(
                    databaseRow.get(keyFld), descriptor.getTypedField(tgtFld).getType());
			if (value != null) {
				primaryKeys.setElementAt(value, idx);
			}
		}
		// store the Reference instance on the resolver for use during mapping
		// resolution phase
		ReferenceResolver resolver = ReferenceResolver.getInstance(sourceQuery.getSession());
		if (resolver != null) {
			resolver.addReference(new Reference(this, targetObject, referenceClass, primaryKeys));
		}
		return null;
    }	

    /**
     * @Override 
     * @param field
     */
    public void setField(DatabaseField field) {
    	// do nothing.
    }
    
    /**
     * INTERNAL:
     * Set the list of source-target xmlfield pairs.
     * 
     * @return
     */
    public void setSourceToTargetKeyFieldAssociations(HashMap sourceToTargetKeyFieldAssociations) {
        this.sourceToTargetKeyFieldAssociations = sourceToTargetKeyFieldAssociations;
    }
    
    /**
     * INTERNAL:
     * Write the attribute value from the object to the row.
     */
    public void writeFromObjectIntoRow(Object object, AbstractRecord row, AbstractSession session) {
        // for each xmlField on this mapping
        Object targetObject = getAttributeValueFromObject(object);
        writeSingleValue(targetObject, object, (XMLRecord)row, session);
    }
    
    public void writeSingleValue(Object value, Object parent, XMLRecord row, AbstractSession session) {
        for (Iterator fieldIt = getFields().iterator(); fieldIt.hasNext(); ) {
            XMLField xmlField = (XMLField) fieldIt.next();
            Object fieldValue = buildFieldValue(value, xmlField, session);
            if (fieldValue != null) {
                QName schemaType = getSchemaType(xmlField, fieldValue);
                String stringValue = getValueToWrite(schemaType, fieldValue);
                row.put(xmlField, stringValue);
           }
        }
        
    }
}
