// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.eis;

import java.util.List;
import java.util.Vector;
import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.descriptors.DescriptorQueryManager;
import oracle.toplink.descriptors.InheritancePolicy;
import oracle.toplink.eis.mappings.EISDirectMapping;
import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.exceptions.DescriptorException;
import oracle.toplink.exceptions.QueryException;
import oracle.toplink.internal.databaseaccess.DatabaseCall;
import oracle.toplink.internal.expressions.SQLStatement;
import oracle.toplink.mappings.DatabaseMapping;
import oracle.toplink.internal.helper.*;
import oracle.toplink.internal.ox.*;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.ox.*;
import oracle.toplink.ox.record.*;
import oracle.toplink.sessions.DatabaseRecord;

/**
 * 
 * <p>An <code>EISDescriptor</code> defines the mapping from a JCA data 
 * structure to a Java object.  There are two types of EIS descriptors:
 * <ul>
 * <li>Root - indicates to the TopLink runtime that the EIS descriptor's 
 * reference class is a parent class: no other class will reference it by way of 
 * a composite object mapping or composite collection mapping.  For an EIS root 
 * descriptor, EIS interactions can be defined to invoke methods on an EIS
 * <li>Composite - indicates to the TopLink runtime that the EIS descriptor's 
 * reference class may be referenced by a composite object mapping or composite 
 * collection mapping
 * </ul>
 *
 * @see oracle.toplink.eis.interactions.EISInteraction
 * @see oracle.toplink.eis.mappings.EISMapping
 *
 * @author James
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class EISDescriptor extends ClassDescriptor {

    /** Define the type of data the descriptor maps to. */
    protected String dataFormat;

    /** Define the valid data formats that the descriptor can map to. */
    public static final String MAPPED = "mapped";
    public static final String INDEXED = "indexed";
    public static final String XML = "xml";

    /** Allow namespaces to be specified for XML type descriptors. */
    protected NamespaceResolver namespaceResolver;

    /**
     * Default constructor.
     */
    public EISDescriptor() {
        super();
        this.shouldOrderMappings = false;
        this.dataFormat = XML;
    }

    protected void validateMappingType(DatabaseMapping mapping) {
        if (!(mapping.isEISMapping())) {
            throw DescriptorException.invalidMappingType(mapping);
        }
    }

    /**
     * PUBLIC:
     * Specify the data type name for the class of objects the descriptor maps.
     * This may be the XML schema complex type name, or the JCA record name for the type being mapped.
     */
    public void setDataTypeName(String dataTypeName) throws DescriptorException {
        this.setTableName(dataTypeName);
    }

    /**
     * PUBLIC:
     * Return the XML namespace resolver.
     * XML type EIS descriptor can use a namespace resolver to support XML schema namespaces.
     */
    public NamespaceResolver getNamespaceResolver() {
        return namespaceResolver;
    }

    /**
     * PUBLIC:
     * The inheritance policy is used to define how a descriptor takes part in inheritance.
     * All inheritance properties for both child and parent classes is configured in inheritance policy.
     * Caution must be used in using this method as it lazy initializes an inheritance policy.
     * Calling this on a descriptor that does not use inheritance will cause problems, #hasInheritance() must always first be called.
     */
    public InheritancePolicy getInheritancePolicy() {
        if (inheritancePolicy == null) {
            if(getDataFormat() == this.XML) {
                // Lazy initialize to conserve space in non-inherited classes.
                setInheritancePolicy(new oracle.toplink.internal.ox.QNameInheritancePolicy(this));
            } else {
                setInheritancePolicy(new InheritancePolicy(this));
            }
        }
        return inheritancePolicy;
    }

    /**
     * PUBLIC:
     * Set the XML namespace resolver.
     * XML type EIS descriptor can use a namespace resolver to support XML schema namespaces.
     */
    public void setNamespaceResolver(NamespaceResolver namespaceResolver) {
        this.namespaceResolver = namespaceResolver;
    }

    /**
     * INTERNAL:
     * Avoid SDK initialization.
     */
    public void setQueryManager(DescriptorQueryManager queryManager) {
        this.queryManager = queryManager;
        if (queryManager != null) {
            queryManager.setDescriptor(this);
        }
    }

    /**
     * INTERNAL:
     * Configure the object builder for the correct dataFormat.
     */
    public void preInitialize(AbstractSession session) {
        // Must not initialize if already done.
        if (isInitialized(PREINITIALIZED)) {
            return;
        }

        if (dataFormat.equals(XML)) {
            setObjectBuilder(new XMLObjectBuilder(this));
            if(this.hasInheritance()) {
                ((QNameInheritancePolicy)getInheritancePolicy()).setNamespaceResolver(this.namespaceResolver);
            }
        }

        //		initializeQueryManager();
        super.preInitialize(session);
    }

    /**
     * PUBLIC:
     * Return the data format that the descriptor maps to.
     */
    public String getDataFormat() {
        return dataFormat;
    }

    /**
     * PUBLIC:
     * Specify the data type name for the class of objects the descriptor maps.
     * This may be the XML schema complex type name, or the JCA record name for the type being mapped.
     */
    public String getDataTypeName() throws DescriptorException {
        return this.getTableName();
    }

    /**
     * PUBLIC:
     * Configure the data format that the descriptor maps to.
     */
    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    /**
     * PUBLIC:
     * Configure the data format to use mapped records.
     */
    public void useMappedRecordFormat() {
        setDataFormat(MAPPED);
    }

    /**
     * PUBLIC:
     * Configure the data format to use indexed records.
     */
    public void useIndexedRecordFormat() {
        setDataFormat(INDEXED);
    }

    /**
     * PUBLIC:
     * Configure the data format to use xml records.
     */
    public void useXMLRecordFormat() {
        setDataFormat(XML);
    }

    /**
     * INTERNAL:
     * Build the nested row.
     */
    public AbstractRecord buildNestedRowFromFieldValue(Object fieldValue) {
        if (!getDataFormat().equals(XML)) {
            if (!(fieldValue instanceof List)) {
                return new DatabaseRecord(1);
            }
            List nestedRows = ((List)fieldValue);
            if (nestedRows.isEmpty()) {
                return new DatabaseRecord(1);
            } else {
                // BUG#2667762 if the tag was empty this could be a string of whitespace.
                if (!(nestedRows.get(0) instanceof AbstractRecord)) {
                    return new DatabaseRecord(1);
                }
                return (AbstractRecord)nestedRows.get(0);
            }
        }

        if (fieldValue instanceof XMLRecord) {
            return (XMLRecord)fieldValue;
        }

        // BUG#2667762 if the tag was empty this could be a string of whitespace.
        if (!(fieldValue instanceof Vector)) {
            return getObjectBuilder().createRecord();
        }
        Vector nestedRows = (Vector)fieldValue;
        if (nestedRows.isEmpty()) {
            return getObjectBuilder().createRecord();
        } else {
            // BUG#2667762 if the tag was empty this could be a string of whitespace.
            if (!(nestedRows.firstElement() instanceof XMLRecord)) {
                return getObjectBuilder().createRecord();
            }
            return (XMLRecord)nestedRows.firstElement();
        }
    }

    /**
     * INTERNAL:
     * Build the nested rows.
     */
    public Vector buildNestedRowsFromFieldValue(Object fieldValue, AbstractSession session) {
        if (!getDataFormat().equals(XML)) {
            if (!(fieldValue instanceof List)) {
                return new Vector();
            }
            return new Vector((List)fieldValue);
        }

        // BUG#2667762 if the tag was empty this could be a string of whitespace.
        if (!(fieldValue instanceof Vector)) {
            return new Vector(0);
        }
        return (Vector)fieldValue;
    }

    /**
     * INTERNAL:
     * Extract the direct values from the specified field value.
     * Return them in a vector.
     * The field value could be a vector or could be a text value if only a single value.
     */
    public Vector buildDirectValuesFromFieldValue(Object fieldValue) {
        if (!getDataFormat().equals(XML)) {
            return super.buildDirectValuesFromFieldValue(fieldValue);
        }

        if (!(fieldValue instanceof Vector)) {
            Vector fieldValues = new Vector(1);
            fieldValues.add(fieldValue);
            return fieldValues;
        }
        return (Vector)fieldValue;
    }

    /**
     * INTERNAL:
     * Build the appropriate field value for the specified
     * set of direct values.
     * The database better be expecting a Vector.
     */
    public Object buildFieldValueFromDirectValues(Vector directValues, String elementDataTypeName, AbstractSession session) {
        if (!getDataFormat().equals(XML)) {
            return super.buildFieldValueFromDirectValues(directValues, elementDataTypeName, session);
        }
        return directValues;
    }

    /**
     * INTERNAL:
     * Build and return the field value from the specified nested database row.
     * The database better be expecting an SDKFieldValue.
     */
    public Object buildFieldValueFromNestedRow(AbstractRecord nestedRow, AbstractSession session) throws DatabaseException {
        Vector nestedRows = new Vector(1);
        nestedRows.addElement(nestedRow);
        return this.buildFieldValueFromNestedRows(nestedRows, "", session);
    }

    /**
     * INTERNAL:
     * Build and return the appropriate field value for the specified
     * set of nested rows.
     */
    public Object buildFieldValueFromNestedRows(Vector nestedRows, String structureName, AbstractSession session) throws DatabaseException {
        return nestedRows;
    }

    /**
    * INTERNAL:
    * XML type descriptors should use XMLFields.
    */
    public DatabaseField buildField(String fieldName) {
        if (getDataFormat().equals(XML)) {
            XMLField xmlField = new XMLField(fieldName);
            xmlField.setNamespaceResolver(this.getNamespaceResolver());
            return xmlField;
        } else {
            return super.buildField(fieldName);
        }
    }

    /**
     * INTERNAL:
     * If the field is an XMLField then set the namespace resolver from the descriptor.
     * This allows the resolver to only be set in the descriptor.
     */
    public DatabaseField buildField(DatabaseField field) {
        if (field instanceof XMLField) {
            ((XMLField)field).setNamespaceResolver(getNamespaceResolver());
        }
        return super.buildField(field);
    }

    /**
        * PUBLIC:
        * Add a direct mapping to the receiver. The new mapping specifies that
        * an instance variable of the class of objects which the receiver describes maps in
        * the default manner for its type to the indicated database field.
        *
        * @param String instanceVariableName is the name of an instance variable of the
        * class which the receiver describes.
        * @param String fieldName is the name of the xml element or attribute which corresponds
        * with the designated instance variable.
        * @return The newly created DatabaseMapping is returned.
        */
    public DatabaseMapping addDirectMapping(String attributeName, String fieldName) {
        EISDirectMapping mapping = new EISDirectMapping();
        mapping.setAttributeName(attributeName);

        if (getDataFormat() == EISDescriptor.XML) {
            mapping.setXPath(fieldName);
        } else {
            mapping.setFieldName(fieldName);
        }

        return addMapping(mapping);
    }

    /**
    * PUBLIC:
    * Add a direct to node mapping to the receiver. The new mapping specifies that
    * a variable accessed by the get and set methods of the class of objects which
    * the receiver describes maps in  the default manner for its type to the indicated
    * database field.
    */
    public DatabaseMapping addDirectMapping(String attributeName, String getMethodName, String setMethodName, String fieldName) {
        EISDirectMapping mapping = new EISDirectMapping();

        mapping.setAttributeName(attributeName);
        mapping.setSetMethodName(setMethodName);
        mapping.setGetMethodName(getMethodName);
        if (getDataFormat() == EISDescriptor.XML) {
            mapping.setXPath(fieldName);
        } else {
            mapping.setFieldName(fieldName);
        }
        return addMapping(mapping);
    }

    /**
     * PUBLIC:
     * Specify the primary key field.
     * This should be called for each field that make up the primary key.
     * For EIS XML Descriptors use the addPrimaryKeyField(DatabaseField) API
     * and supply an oracle.toplink.ox.XMLField parameter instead of using this method
     */
    public void addPrimaryKeyFieldName(String fieldName) {
        super.addPrimaryKeyFieldName(fieldName);
    }

    /**
     * PUBLIC:
     * Set the sequence number field name.
     * This is the field in the descriptors table that needs its value to be generated.
     * This is normally the primary key field of the descriptor.
     * For EIS XML Descriptors use the setSequenceNumberFieldName(DatabaseField) API
     * and supply an oracle.toplink.ox.XMLField parameter instead of using this method
     */
    public void setSequenceNumberFieldName(String fieldName) {
        super.setSequenceNumberFieldName(fieldName);
    }

    /**
      *INTERNAL:
      * Override this method to throw an exception. SQL should not be generated for
      * EIS Calls.
      */
    public DatabaseCall buildCallFromStatement(SQLStatement statement, AbstractSession session) {
        throw QueryException.noCallOrInteractionSpecified();
    }

    /**
        * INTERNAL:
        * This is needed by regular aggregate descriptors
        * * but not by EIS aggregate descriptors.
        */
    public void initializeAggregateInheritancePolicy(AbstractSession session) {
        // do nothing, since the parent descriptor was already modified during pre-initialize
    }

    /**
     * INTERNAL:
     * XML descriptors are initialized normally, since they do
     * not need to be cloned by ESI aggregate mappings.
     */
    public boolean requiresInitialization() {
        return (!isDescriptorForInterface());
    }

    /**
     * Aggregates use a dummy table as default.
     */
    protected DatabaseTable extractDefaultTable() {
        if (this.isAggregateDescriptor()) {
            return new DatabaseTable();
        }
        return super.extractDefaultTable();
    }

    /**
     * INTERNAL:
     * Indicates if a return type is required for the field set on the
     * returning policy.  For EIS descriptors, this should always
     * return false.
     */
    public boolean isReturnTypeRequiredForReturningPolicy() {
        return false;
    }
}
