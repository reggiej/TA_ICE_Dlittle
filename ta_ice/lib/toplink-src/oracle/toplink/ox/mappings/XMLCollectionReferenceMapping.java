// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox.mappings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.namespace.QName;

import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.exceptions.DescriptorException;
import oracle.toplink.internal.descriptors.ObjectBuilder;
import oracle.toplink.internal.helper.ClassConstants;
import oracle.toplink.internal.helper.ConversionManager;
import oracle.toplink.internal.ox.Reference;
import oracle.toplink.internal.ox.ReferenceResolver;
import oracle.toplink.internal.ox.XMLConversionManager;
import oracle.toplink.internal.queryframework.CollectionContainerPolicy;
import oracle.toplink.internal.queryframework.ContainerPolicy;
import oracle.toplink.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.internal.queryframework.MapContainerPolicy;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.mappings.ContainerMapping;
import oracle.toplink.ox.XMLField;
import oracle.toplink.ox.record.UnmarshalRecord;
import oracle.toplink.ox.record.XMLRecord;
import oracle.toplink.queryframework.ObjectBuildingQuery;

/**
 * TopLink OXM version of a 1-M mapping.  A list of source-target key field
 * associations is used to link the source xpaths to their related target 
 * xpaths, and hence their primary key (unique identifier) values used when 
 * (un)marshalling.
 * 
 * It is important to note that each target xpath is assumed to be set as a primary
 * key field on the target (reference) class descriptor - this is necessary in order
 * to locate the correct target object instance in the session cache when resolving
 * mapping references.
 * 
 * The usesSingleNode flag should be set to true if the keys are to be written out in space-separated
 * lists.
 * 
 * @see XMLObjectReferenceMapping
 * @see ContainerMapping
 */
public class XMLCollectionReferenceMapping extends XMLObjectReferenceMapping implements ContainerMapping {
    protected ContainerPolicy containerPolicy;  // type of container used to hold the aggregate objects
    private static final String SPACE = " ";
    private boolean usesSingleNode;
    
	/**
	 * PUBLIC:
	 * The default constructor initializes the sourceToTargetKeyFieldAssociations
	 * and sourceToTargetKeys data structures.
	 */
	public XMLCollectionReferenceMapping() {
		sourceToTargetKeyFieldAssociations = new HashMap();
		sourceToTargetKeys = new Vector();
        this.containerPolicy = ContainerPolicy.buildPolicyFor(ClassConstants.Vector_class);
        this.usesSingleNode = false;
	}

    /**    
	 * INTERNAL:
	 * Retrieve the target object's primary key value that is mapped to a given
	 * source xpath (in the source-target key field association list).
	 * 
	 * @param targetObject - the reference class instance that holds the required pk value
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
    	XMLField tgtXMLField = (XMLField) getSourceToTargetKeyFieldAssociations().get(xmlFld);
		int idx = descriptor.getPrimaryKeyFields().indexOf(tgtXMLField);
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
    	Reference reference = resolver.getReference(this, srcObject);
    	// if reference is null, create a new instance and set it on the resolver
    	if (reference == null) {
    		reference = new Reference(this, srcObject, getReferenceClass(), new HashMap());
    		resolver.addReference(reference);
    	}

    	XMLField tgtFld = (XMLField) getSourceToTargetKeyFieldAssociations().get(xmlField);
        String tgtXPath = tgtFld.getXPath();
		Vector pks;
		HashMap primaryKeyMap = reference.getPrimaryKeyMap();
		if (primaryKeyMap.containsKey(tgtXPath)) {
			pks = (Vector) primaryKeyMap.get(tgtXPath); 
		} else {
			pks = new Vector();
			primaryKeyMap.put(tgtXPath, pks);
		}
        
        ClassDescriptor descriptor = session.getClassDescriptor(getReferenceClass());
        Class type = descriptor.getTypedField(tgtFld).getType();
        for (StringTokenizer stok = new StringTokenizer((String)object); stok.hasMoreTokens();) {
            Object value = XMLConversionManager.getDefaultXMLManager().convertObject(
                    stok.nextToken(), type);
            if (value != null) {
                pks.add(value);
            }
        }
    }
    
    /**
     * INTERNAL:
     * Return the mapping's containerPolicy.
     */
    public ContainerPolicy getContainerPolicy() {
        return containerPolicy;
    }
	    
    /**
     * INTERNAL:
     * The mapping is initialized with the given session. This mapping is fully initialized
     * after this.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        super.initialize(session);
        ContainerPolicy cp = getContainerPolicy(); 
        if (cp != null) {
            if (cp.getContainerClass() == null) {
                Class cls = ConversionManager.getDefaultManager().convertClassNameToClass(cp.getContainerClassName());
                cp.setContainerClass(cls);
            }
        }
    }
    
    /**
     * INTERNAL:
     * Extract the primary key values from the row, then create an 
     * oracle.toplink.internal.ox.Reference instance and stored it 
     * on the session's oracle.toplink.internal.ox.ReferenceResolver.
     */
    public Object readFromRowIntoObject(AbstractRecord databaseRow, JoinedAttributeManager joinManager, Object targetObject, ObjectBuildingQuery sourceQuery, AbstractSession executionSession) throws DatabaseException {
        ClassDescriptor descriptor = sourceQuery.getSession().getClassDescriptor(getReferenceClass());
        ContainerPolicy cp = this.getContainerPolicy();
    	Vector pkFieldNames = referenceDescriptor.getPrimaryKeyFieldNames(); 
    	Vector primaryKeyValues = new Vector();
    	primaryKeyValues.setSize(pkFieldNames.size());
    	HashMap primaryKeyMap = new HashMap();
		// for each source xmlField, get the value from the row and store
    	for (Iterator fieldIt = getFields().iterator(); fieldIt.hasNext();) {
			XMLField fld = (XMLField) fieldIt.next();
            XMLField tgtFld = (XMLField) getSourceToTargetKeyFieldAssociations().get(fld);
			Object fieldValue = databaseRow.getValues(fld);
	        if ((fieldValue == null) || (fieldValue instanceof String) || !(fieldValue instanceof Vector)) {
	            return cp.containerInstance();
	        }
            // fix for bug# 5687430
            // need to get the actual type of the target (i.e. int, String, etc.) 
            // and use the converted value when checking the cache.
            Vector newValues = new Vector();
            for (Iterator valIt = ((Vector)fieldValue).iterator(); valIt.hasNext();) {
                for (StringTokenizer stok = new StringTokenizer((String)valIt.next()); stok.hasMoreTokens();) {
                    Object value = XMLConversionManager.getDefaultXMLManager().convertObject(
                            stok.nextToken(), descriptor.getTypedField(tgtFld).getType());
                    if (value != null) {
                        newValues.add(value);
                    }
                }
            }
            primaryKeyMap.put(tgtFld.getXPath(), newValues);
		}
		// store the Reference instance on the resolver for use during mapping resolution phase
		ReferenceResolver resolver = ReferenceResolver.getInstance(sourceQuery.getSession());
		if (resolver != null) {
			resolver.addReference(new Reference(this, targetObject, referenceClass, primaryKeyMap));
		}
		return null;
    }
    
    /**
     * ADVANCED:
     * Set the mapping's containerPolicy.
     */
    public void setContainerPolicy(ContainerPolicy containerPolicy) {
        // set reference class here if necessary
        this.containerPolicy = containerPolicy;
        if (this.containerPolicy instanceof MapContainerPolicy) {
            ((MapContainerPolicy) this.containerPolicy).setElementClass(getReferenceClass()); 
        }
    }
    
    /**
     * PUBLIC:
     * Configure the mapping to use an instance of the specified container class
     * to hold the target objects.
     * <p>jdk1.2.x: The container class must implement (directly or indirectly) the Collection interface.
     * <p>jdk1.1.x: The container class must be a subclass of Vector.
     */
    public void useCollectionClass(Class concreteContainerClass) {
        this.setContainerPolicy(ContainerPolicy.buildPolicyFor(concreteContainerClass));
    }
    
    public void useCollectionClassName(String concreteContainerClassName) {
        this.setContainerPolicy(new CollectionContainerPolicy(concreteContainerClassName));
    }

    /**
     * PUBLIC:
     * Configure the mapping to use an instance of the specified container class
     * to hold the target objects. The key used to index the value in the Map
     * is the value returned by a call to the specified zero-argument method.
     * The method must be implemented by the class (or a superclass) of the
     * value to be inserted into the Map.
     * <p>jdk1.2.x: The container class must implement (directly or indirectly) the Map interface.
     * <p>jdk1.1.x: The container class must be a subclass of Hashtable.
     * <p>The referenceClass must be set before calling this method.
     */
    public void useMapClass(Class concreteContainerClass, String methodName) {
        // the reference class has to be specified before coming here
        if (this.getReferenceClass() == null) {
            throw DescriptorException.referenceClassNotSpecified(this);
        }
        ContainerPolicy policy = ContainerPolicy.buildPolicyFor(concreteContainerClass);
        policy.setKeyName(methodName, getReferenceClass().getName());
        this.setContainerPolicy(policy);
    }
    
    /**
     * INTERNAL:
     * For the purpose of XMLCollectionReferenceMappings, 'usesSingleNode' 
     * refers to the fact that the source key xpath fields should all be written as
     * space-separated lists. Would be used for mapping to an IDREFS field in a schema
     */
    public boolean usesSingleNode() {
        return this.usesSingleNode;
    }
    
    public void setUsesSingleNode(boolean useSingleNode) {
        this.usesSingleNode = useSingleNode;
    }
    
    /**
     * INTERNAL:
     * Write the attribute value from the object to the row.
     */
    public void writeFromObjectIntoRow(Object object, AbstractRecord row, AbstractSession session) {
        // for each xmlField on this mapping
        for (Iterator fieldIt = getFields().iterator(); fieldIt.hasNext(); ) {
            XMLField xmlField = (XMLField) fieldIt.next();
            ContainerPolicy cp = getContainerPolicy();
            Object collection = getAttributeAccessor().getAttributeValueFromObject(object);
            if (collection == null) {
                return;
            }
            
            Object fieldValue;
            Object objectValue;
            String stringValue = "";
            QName schemaType;
            Object iterator = cp.iteratorFor(collection);
            if (usesSingleNode()) {
                while (cp.hasNext(iterator)) {
                    objectValue = cp.next(iterator, session);
                    fieldValue = buildFieldValue(objectValue, xmlField,  session);
                    if (fieldValue != null) {
                        schemaType = getSchemaType(xmlField, fieldValue);
                        String newValue = getValueToWrite(schemaType, fieldValue);
                        if (newValue != null) {
                            stringValue += newValue;
                            if (cp.hasNext(iterator)) {
                                stringValue += SPACE;
                            }
                        }
                    }
                }
                if (!(stringValue.equals(""))) {
                    row.put(xmlField, stringValue);
                }
            } else {
                ArrayList keyValues = new ArrayList();
                while (cp.hasNext(iterator)) {
                    objectValue = cp.next(iterator, session);
                    fieldValue = buildFieldValue(objectValue, xmlField, session);
                    if (fieldValue != null) {
                        schemaType = getSchemaType(xmlField, fieldValue);
                        stringValue = getValueToWrite(schemaType, fieldValue);
                        //row.add(xmlField, stringValue);
                        keyValues.add(stringValue);
                    }
                }
                row.put(xmlField, keyValues);
            }
        }
    }
    public void writeSingleValue(Object value, Object parent, XMLRecord row, AbstractSession session) {
        for (Iterator fieldIt = getFields().iterator(); fieldIt.hasNext(); ) {
            XMLField xmlField = (XMLField) fieldIt.next();
            Object fieldValue = buildFieldValue(value, xmlField, session);
            if (fieldValue != null) {
                QName schemaType = getSchemaType(xmlField, fieldValue);
                String stringValue = getValueToWrite(schemaType, fieldValue);
                row.add(xmlField, stringValue);
           }
        }
        
    }
   
}
