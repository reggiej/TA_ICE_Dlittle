// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.tools.workbench;

// javase imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.xml.sax.Attributes;
import static java.lang.Integer.MIN_VALUE;

// TopLink imports
import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.descriptors.ClassExtractor;
import oracle.toplink.descriptors.RelationalDescriptor;
import oracle.toplink.exceptions.DescriptorException;
import oracle.toplink.internal.descriptors.InstantiationPolicy;
import oracle.toplink.internal.descriptors.Namespace;
import oracle.toplink.internal.helper.DatabaseField;
import oracle.toplink.internal.helper.DatabaseTable;
import oracle.toplink.internal.helper.DatabaseTypeWrapper;
import oracle.toplink.internal.helper.NonSynchronizedVector;
import oracle.toplink.internal.identitymaps.SoftIdentityMap;
import oracle.toplink.internal.ox.XMLConversionManager;
import oracle.toplink.internal.ox.XMLConversionPair;
import oracle.toplink.internal.ox.XMLChoiceFieldToClassAssociation;
import oracle.toplink.internal.queryframework.ContainerPolicy;
import oracle.toplink.internal.queryframework.SortedCollectionContainerPolicy;
import oracle.toplink.mappings.AggregateMapping;
import oracle.toplink.mappings.Association;
import oracle.toplink.mappings.AttributeAccessor;
import oracle.toplink.mappings.DatabaseMapping;
import oracle.toplink.mappings.ForeignReferenceMapping;
import oracle.toplink.mappings.converters.EnumTypeConverter;
import oracle.toplink.mappings.converters.ObjectTypeConverter;
import oracle.toplink.mappings.foundation.AbstractCompositeDirectCollectionMapping;
import oracle.toplink.objectrelational.ObjectRelationalDatabaseField;
import oracle.toplink.ox.NamespaceResolver;
import oracle.toplink.ox.XMLDescriptor;
import oracle.toplink.ox.XMLField;
import oracle.toplink.ox.mappings.UnmarshalKeepAsElementPolicy;
import oracle.toplink.ox.mappings.XMLAnyAttributeMapping;
import oracle.toplink.ox.mappings.XMLAnyCollectionMapping;
import oracle.toplink.ox.mappings.XMLAnyObjectMapping;
import oracle.toplink.ox.mappings.XMLBinaryDataMapping;
import oracle.toplink.ox.mappings.XMLChoiceObjectMapping;
import oracle.toplink.ox.mappings.XMLCollectionReferenceMapping;
import oracle.toplink.ox.mappings.XMLCompositeCollectionMapping;
import oracle.toplink.ox.mappings.XMLCompositeDirectCollectionMapping;
import oracle.toplink.ox.mappings.XMLCompositeObjectMapping;
import oracle.toplink.ox.mappings.XMLChoiceCollectionMapping;
import oracle.toplink.ox.mappings.XMLDirectMapping;
import oracle.toplink.ox.mappings.XMLFragmentCollectionMapping;
import oracle.toplink.ox.mappings.XMLFragmentMapping;
import oracle.toplink.ox.mappings.XMLNillableMapping;
import oracle.toplink.ox.mappings.XMLObjectReferenceMapping;
import oracle.toplink.ox.mappings.nullpolicy.AbstractNullPolicy;
import oracle.toplink.ox.mappings.nullpolicy.IsSetNullPolicy;
import oracle.toplink.ox.mappings.nullpolicy.NullPolicy;
import oracle.toplink.ox.mappings.nullpolicy.XMLNullRepresentationType;
import oracle.toplink.ox.record.DOMRecord;
import oracle.toplink.ox.record.UnmarshalRecord;
import oracle.toplink.ox.schema.XMLSchemaClassPathReference;
import oracle.toplink.ox.schema.XMLSchemaReference;
import oracle.toplink.platform.database.jdbc.JDBCTypeWrapper;
import oracle.toplink.platform.database.jdbc.JDBCTypes;
import oracle.toplink.platform.database.oracle.ComplexPLSQLTypeWrapper;
import oracle.toplink.platform.database.oracle.OraclePLSQLTypes;
import oracle.toplink.platform.database.oracle.PLSQLStoredProcedureCall;
import oracle.toplink.platform.database.oracle.PLSQLargument;
import oracle.toplink.platform.database.oracle.PLSQLrecord;
import oracle.toplink.platform.database.oracle.SimplePLSQLTypeWrapper;
import oracle.toplink.queryframework.Call;
import oracle.toplink.queryframework.CursoredStreamPolicy;
import oracle.toplink.queryframework.ScrollableCursorPolicy;
import oracle.toplink.queryframework.StoredFunctionCall;
import oracle.toplink.queryframework.StoredProcedureCall;
import oracle.toplink.sessions.Record;
import oracle.toplink.sessions.Session;
import static oracle.toplink.internal.databaseaccess.DatasourceCall.IN;
import static oracle.toplink.internal.databaseaccess.DatasourceCall.INOUT;
import static oracle.toplink.internal.databaseaccess.DatasourceCall.OUT;
import static oracle.toplink.internal.databaseaccess.DatasourceCall.OUT_CURSOR;
import static oracle.toplink.internal.helper.DatabaseField.NULL_SQL_TYPE;

/**
 * INTERNAL:
 * Define the TopLink OX project and descriptor information to read an AS 11<i>g</i>
 * (11.1.1) project from an XML file.
 * Note any changes must be reflected in the XML schema.
 * This project contains the 11gR1 mappings to the 11gR1 schema.
 */
public class ObjectPersistenceRuntimeXMLProject_11_1_1 extends ObjectPersistenceRuntimeXMLProject {

    /**
     * INTERNAL:
     * Return a new descriptor project.
     */
    public ObjectPersistenceRuntimeXMLProject_11_1_1() {
        super();
        addDescriptor(buildCursoredStreamPolicyDescriptor());
        addDescriptor(buildScrollableCursorrPolicyDescriptor());
        
        // Stored procedure arguments
        addDescriptor(buildStoredProcedureArgumentDescriptor());
        addDescriptor(buildStoredProcedureOutArgumentsDescriptor());
        addDescriptor(buildStoredProcedureInOutArgumentsDescriptor());
        addDescriptor(buildStoredProcedureOutCursorArgumentsDescriptor());
        addDescriptor(buildStoredProcedureCallDescriptor());
        // 5877994 -- add metadata support for Stored Function Calls
        addDescriptor(buildStoredFunctionCallDescriptor()); 
        
        //5963607 -- add Sorted Collection mapping support
        addDescriptor(buildSortedCollectionContainerPolicyDescriptor());
                
        //TopLink OXM
        addDescriptor(buildXMLAnyAttributeMappingDescriptor());
        addDescriptor(buildXMLCollectionReferenceMappingDescriptor());
        addDescriptor(buildXMLObjectReferenceMappingDescriptor());
        addDescriptor(this.buildXMLFragmentMappingDescriptor());
        addDescriptor(this.buildXMLFragmentCollectionMappingDescriptor());
        addDescriptor(buildXMLChoiceCollectionMappingDescriptor());
        addDescriptor(buildXMLChoiceObjectMappingDescriptor());
        addDescriptor(buildXMLChoiceFieldToClassAssociationDescriptor());

        // Add Null Policy Mappings
        addDescriptor(buildAbstractNullPolicyDescriptor());
        addDescriptor(buildNullPolicyDescriptor());
        addDescriptor(buildIsSetNullPolicyDescriptor());
        
        // 6029568 -- add metadata support for PLSQLStoredProcedureCall
        addDescriptor(buildDatabaseTypeWrapperDescriptor());
        addDescriptor(buildJDBCTypeWrapperDescriptor());
        addDescriptor(buildSimplePLSQLTypeWrapperDescriptor());
        addDescriptor(buildComplexPLSQLTypeWrapperDescriptor());
        addDescriptor(buildPLSQLargumentDescriptor());
        addDescriptor(buildPLSQLStoredProcedureCallDescriptor());
        addDescriptor(buildPLSQLrecordDescriptor());
        
        // 5757849 -- add metadata support for ObjectRelationalDatabaseField
        addDescriptor(buildObjectRelationalDatabaseFieldDescriptor());
        
        // Do not add any descriptors beyond this point or an namespaceResolver exception may occur
        
        // Set the namespaces on all descriptors.
        // Need to duplicate in subclass to ensure all NEW descriptors also get
        // NamespaceResolvers set. 
        NamespaceResolver namespaceResolver = new NamespaceResolver();
        namespaceResolver.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        namespaceResolver.put("xsd", "http://www.w3.org/2001/XMLSchema");
        namespaceResolver.put("opm", "http://xmlns.oracle.com/ias/xsds/opm");
        namespaceResolver.put("toplink", "http://xmlns.oracle.com/ias/xsds/toplink");

        for (Iterator descriptorIter = getDescriptors().values().iterator(); descriptorIter.hasNext();) {
            XMLDescriptor descriptor = (XMLDescriptor)descriptorIter.next();
            descriptor.setNamespaceResolver(namespaceResolver);
        }
    }
    
    @Override
    protected ClassDescriptor buildProjectDescriptor() {
        XMLDescriptor descriptor = (XMLDescriptor)super.buildProjectDescriptor();
        descriptor.setSchemaReference(new XMLSchemaClassPathReference("xsd/toplink-object-persistence_11_1_1.xsd"));

        XMLDirectMapping defaultTemporalMutableMapping = new XMLDirectMapping();
        defaultTemporalMutableMapping.setAttributeName("defaultTemporalMutable");
        defaultTemporalMutableMapping.setSetMethodName("setDefaultTemporalMutable");
        defaultTemporalMutableMapping.setGetMethodName("getDefaultTemporalMutable");
        defaultTemporalMutableMapping.setXPath("opm:default-temporal-mutable/text()");
        defaultTemporalMutableMapping.setNullValue(Boolean.FALSE);
        descriptor.addMapping(defaultTemporalMutableMapping);
        
        return descriptor;
    }

    @Override
    public ClassDescriptor buildDatabaseLoginDescriptor() {
        XMLDescriptor descriptor = (XMLDescriptor)super.buildDatabaseLoginDescriptor();

        XMLDirectMapping shouldBindAllParametersMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("shouldBindAllParameters");
        shouldBindAllParametersMapping.setNullValue(Boolean.TRUE);

        return descriptor;
    }

    @Override
    protected ClassDescriptor buildDatabaseMappingDescriptor() {
        ClassDescriptor descriptor = super.buildDatabaseMappingDescriptor();
        
        descriptor.getInheritancePolicy().addClassIndicator(XMLBinaryDataMapping.class, "toplink:xml-binary-data-mapping");
        descriptor.getInheritancePolicy().addClassIndicator(XMLFragmentMapping.class, "toplink:xml-fragment-mapping");
        descriptor.getInheritancePolicy().addClassIndicator(XMLFragmentCollectionMapping.class, "toplink:xml-fragment-collection-mapping");

        descriptor.getInheritancePolicy().addClassIndicator(XMLCollectionReferenceMapping.class, "toplink:xml-collection-reference-mapping");
        descriptor.getInheritancePolicy().addClassIndicator(XMLObjectReferenceMapping.class, "toplink:xml-object-reference-mapping");
        descriptor.getInheritancePolicy().addClassIndicator(XMLAnyAttributeMapping.class, "toplink:xml-any-attribute-mapping");
        descriptor.getInheritancePolicy().addClassIndicator(XMLChoiceCollectionMapping.class, "toplink:xml-choice-collection-mapping");
        descriptor.getInheritancePolicy().addClassIndicator(XMLChoiceObjectMapping.class, "toplink:xml-choice-object-mapping");
        
        return descriptor;
    }

    @Override
    protected ClassDescriptor buildAbstractDirectMappingDescriptor() {

        XMLDescriptor descriptor = (XMLDescriptor)super.buildAbstractDirectMappingDescriptor();
        
        XMLDirectMapping attributeClassificationMapping = new XMLDirectMapping();
        attributeClassificationMapping.setAttributeName("attributeClassification");
        attributeClassificationMapping.setGetMethodName("getAttributeClassification");
        attributeClassificationMapping.setSetMethodName("setAttributeClassification");
        attributeClassificationMapping.setXPath("toplink:attribute-classification/text()");
        descriptor.addMapping(attributeClassificationMapping);
    
        return descriptor;
    }

    @Override
    protected ClassDescriptor buildObjectLevelReadQueryDescriptor() {
        XMLDescriptor descriptor = (XMLDescriptor)super.buildObjectLevelReadQueryDescriptor();
	
        XMLDirectMapping readOnlyMapping = new XMLDirectMapping();
        readOnlyMapping.setAttributeName("isReadOnly");
        readOnlyMapping.setXPath("toplink:read-only/text()");
        readOnlyMapping.setNullValue(Boolean.FALSE);
        descriptor.addMapping(readOnlyMapping);
        
        XMLDirectMapping joinSubclassesMapping = new XMLDirectMapping();
        joinSubclassesMapping.setAttributeName("shouldOuterJoinSubclasses");
        joinSubclassesMapping.setXPath("toplink:outer-join-subclasses/text()");
        descriptor.addMapping(joinSubclassesMapping);

        return descriptor;
    }

    @Override   
    protected ClassDescriptor buildInheritancePolicyDescriptor() {
        XMLDescriptor descriptor = (XMLDescriptor)super.buildInheritancePolicyDescriptor();
        
        XMLDirectMapping joinSubclassesMapping = new XMLDirectMapping();
        joinSubclassesMapping.setAttributeName("shouldOuterJoinSubclasses");
        joinSubclassesMapping.setXPath("toplink:outer-join-subclasses/text()");
        joinSubclassesMapping.setNullValue(Boolean.FALSE);
        descriptor.addMapping(joinSubclassesMapping);

        return descriptor;
    }
    
    protected ClassDescriptor buildCursoredStreamPolicyDescriptor() {
        XMLDescriptor descriptor = new XMLDescriptor();

        descriptor.setJavaClass(CursoredStreamPolicy.class);

        descriptor.getInheritancePolicy().setParentClass(ContainerPolicy.class);

        return descriptor;
    }
    
    protected ClassDescriptor buildRelationalDescriptorDescriptor() {
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(RelationalDescriptor.class);
        descriptor.getInheritancePolicy().setParentClass(ClassDescriptor.class);

        XMLCompositeCollectionMapping tablesMapping = new XMLCompositeCollectionMapping();
        tablesMapping.useCollectionClass(oracle.toplink.internal.helper.NonSynchronizedVector.class);
        tablesMapping.setAttributeName("tables/table");
        tablesMapping.setGetMethodName("getTables");
        tablesMapping.setSetMethodName("setTables");
        tablesMapping.setXPath("toplink:tables/toplink:table");
        tablesMapping.setReferenceClass(DatabaseTable.class);
        descriptor.addMapping(tablesMapping);

        XMLCompositeCollectionMapping foreignKeyForMultipleTables = new XMLCompositeCollectionMapping();
        foreignKeyForMultipleTables.setReferenceClass(Association.class);
        foreignKeyForMultipleTables.setAttributeName("foreignKeysForMultipleTables");
        foreignKeyForMultipleTables.setXPath("toplink:foreign-keys-for-multiple-table/opm:field-reference");
        foreignKeyForMultipleTables.setAttributeAccessor(new AttributeAccessor() {
                public Object getAttributeValueFromObject(Object object) {
                    ClassDescriptor descriptor = (ClassDescriptor) object;
                    Vector associations = descriptor.getMultipleTableForeignKeyAssociations();
                    
                    for (int index = 0; index < associations.size(); index++) {
                        Association association = (Association) associations.get(index);
                        String targetPrimaryKeyFieldName = (String) association.getKey();
                        association.setKey(new DatabaseField((String) association.getValue()));
                        association.setValue(new DatabaseField(targetPrimaryKeyFieldName));
                    }
                    
                    return associations;
                }

                public void setAttributeValueInObject(Object object, Object value) {
                    ClassDescriptor descriptor = (ClassDescriptor) object;
                    Vector associations = (Vector) value;
                    
                    for (int index = 0; index < associations.size(); index++) {
                        Association association = (Association) associations.get(index);
                        association.setKey(((DatabaseField) association.getKey()).getQualifiedName());
                        association.setValue(((DatabaseField) association.getValue()).getQualifiedName());
                    }
                    
                    descriptor.setForeignKeyFieldNamesForMultipleTable(associations);
                }
            });
        descriptor.addMapping(foreignKeyForMultipleTables);

        return descriptor;
    }
    
    protected ClassDescriptor buildScrollableCursorrPolicyDescriptor() {
        XMLDescriptor descriptor = new XMLDescriptor();

        descriptor.setJavaClass(ScrollableCursorPolicy.class);

        descriptor.getInheritancePolicy().setParentClass(ContainerPolicy.class);

        return descriptor;
    }

    @Override
    protected ClassDescriptor buildContainerPolicyDescriptor() {
        XMLDescriptor descriptor = (XMLDescriptor)super.buildContainerPolicyDescriptor();

        descriptor.getInheritancePolicy().addClassIndicator(ScrollableCursorPolicy.class, "toplink:scrollable-cursor-policy");
        descriptor.getInheritancePolicy().addClassIndicator(CursoredStreamPolicy.class, "toplink:cursored-stream-policy");
        descriptor.getInheritancePolicy().addClassIndicator(SortedCollectionContainerPolicy.class, "toplink:sorted-collection-container-policy");

        return descriptor;
    }

    @Override
    protected ClassDescriptor buildOneToOneMappingDescriptor() {
        XMLDescriptor descriptor = (XMLDescriptor)super.buildOneToOneMappingDescriptor();
        descriptor.removeMappingForAttributeName("usesJoiningMapping");
        
        XMLDirectMapping joinFetchMapping = new XMLDirectMapping();
        joinFetchMapping.setAttributeName("joinFetch");
        joinFetchMapping.setXPath("toplink:join-fetch/text()");
        ObjectTypeConverter joinFetchConverter = new ObjectTypeConverter();
        joinFetchConverter.addConversionValue("inner-join", new Integer(ForeignReferenceMapping.INNER_JOIN));
        joinFetchConverter.addConversionValue("outer-join", new Integer(ForeignReferenceMapping.OUTER_JOIN));
        joinFetchConverter.addConversionValue("none", new Integer(ForeignReferenceMapping.NONE));
        joinFetchMapping.setConverter(joinFetchConverter);
        joinFetchMapping.setNullValue(ForeignReferenceMapping.NONE);
        descriptor.addMapping(joinFetchMapping);
        
        return descriptor;
    }
    
	@Override
    protected ClassDescriptor buildOXXMLDescriptorDescriptor() {
        XMLDescriptor descriptor = (XMLDescriptor)super.buildOXXMLDescriptorDescriptor();

        XMLCompositeObjectMapping defaultRootElementFieldMapping = new XMLCompositeObjectMapping();
        defaultRootElementFieldMapping.setAttributeName("defaultRootElementField");
        defaultRootElementFieldMapping.setGetMethodName("getDefaultRootElementField");
        defaultRootElementFieldMapping.setSetMethodName("setDefaultRootElementField");
        defaultRootElementFieldMapping.setXPath("toplink:default-root-element-field");
        defaultRootElementFieldMapping.setReferenceClass(XMLField.class);
        /* order is important for writing out
         * don't use addMapping: set parent descriptor and add after
         * first mapping built in super.buildOXXMLDescriptorDescriptor()
         */
        defaultRootElementFieldMapping.setDescriptor(descriptor);
        descriptor.getMappings().add(1, defaultRootElementFieldMapping);

        return descriptor;
    }

    @Override
    protected ClassDescriptor buildManyToManyMappingMappingDescriptor() {
        XMLDescriptor descriptor = (XMLDescriptor)super.buildManyToManyMappingMappingDescriptor();
        
        XMLDirectMapping joinFetchMapping = new XMLDirectMapping();
        joinFetchMapping.setAttributeName("joinFetch");
        joinFetchMapping.setXPath("toplink:join-fetch/text()");
        ObjectTypeConverter joinFetchConverter = new ObjectTypeConverter();
        joinFetchConverter.addConversionValue("inner-join", new Integer(ForeignReferenceMapping.INNER_JOIN));
        joinFetchConverter.addConversionValue("outer-join", new Integer(ForeignReferenceMapping.OUTER_JOIN));
        joinFetchConverter.addConversionValue("none", new Integer(ForeignReferenceMapping.NONE));
        joinFetchMapping.setConverter(joinFetchConverter);
        joinFetchMapping.setNullValue(ForeignReferenceMapping.NONE);
        descriptor.addMapping(joinFetchMapping);
        
        return descriptor;
    }

    @Override    
    protected ClassDescriptor buildOneToManyMappingMappingDescriptor() {
        XMLDescriptor descriptor = (XMLDescriptor)super.buildOneToManyMappingMappingDescriptor();
        
        XMLDirectMapping joinFetchMapping = new XMLDirectMapping();
        joinFetchMapping.setAttributeName("joinFetch");
        joinFetchMapping.setXPath("toplink:join-fetch/text()");
        ObjectTypeConverter joinFetchConverter = new ObjectTypeConverter();
        joinFetchConverter.addConversionValue("inner-join", new Integer(ForeignReferenceMapping.INNER_JOIN));
        joinFetchConverter.addConversionValue("outer-join", new Integer(ForeignReferenceMapping.OUTER_JOIN));
        joinFetchConverter.addConversionValue("none", new Integer(ForeignReferenceMapping.NONE));
        joinFetchMapping.setConverter(joinFetchConverter);
        joinFetchMapping.setNullValue(ForeignReferenceMapping.NONE);
        descriptor.addMapping(joinFetchMapping);
        
        return descriptor;
    }

    @Override    
    protected ClassDescriptor buildDirectCollectionMappingDescriptor() {
        XMLDescriptor descriptor = (XMLDescriptor)super.buildDirectCollectionMappingDescriptor();
        
        XMLDirectMapping joinFetchMapping = new XMLDirectMapping();
        joinFetchMapping.setAttributeName("joinFetch");
        joinFetchMapping.setXPath("toplink:join-fetch/text()");
        ObjectTypeConverter joinFetchConverter = new ObjectTypeConverter();
        joinFetchConverter.addConversionValue("inner-join", new Integer(ForeignReferenceMapping.INNER_JOIN));
        joinFetchConverter.addConversionValue("outer-join", new Integer(ForeignReferenceMapping.OUTER_JOIN));
        joinFetchConverter.addConversionValue("none", new Integer(ForeignReferenceMapping.NONE));
        joinFetchMapping.setConverter(joinFetchConverter);
        joinFetchMapping.setNullValue(ForeignReferenceMapping.NONE);
        descriptor.addMapping(joinFetchMapping);
        
        return descriptor;
    }
    

    protected ClassDescriptor buildSortedCollectionContainerPolicyDescriptor() {
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(oracle.toplink.internal.queryframework.SortedCollectionContainerPolicy.class);

        descriptor.getInheritancePolicy().setParentClass(oracle.toplink.internal.queryframework.CollectionContainerPolicy.class);

        XMLDirectMapping keyMapping = new XMLDirectMapping();
        keyMapping.setAttributeName("comparatorClass");
        keyMapping.setGetMethodName("getComparatorClass");
        keyMapping.setSetMethodName("setComparatorClass");
        keyMapping.setXPath("toplink:comparator-class/text()");
        descriptor.addMapping(keyMapping);

        return descriptor;
    }
    

    @Override    
    protected ClassDescriptor buildAggregateCollectionMappingDescriptor() {
        XMLDescriptor descriptor = (XMLDescriptor)super.buildAggregateCollectionMappingDescriptor();
        
        XMLDirectMapping joinFetchMapping = new XMLDirectMapping();
        joinFetchMapping.setAttributeName("joinFetch");
        joinFetchMapping.setXPath("toplink:join-fetch/text()");
        ObjectTypeConverter joinFetchConverter = new ObjectTypeConverter();
        joinFetchConverter.addConversionValue("inner-join", new Integer(ForeignReferenceMapping.INNER_JOIN));
        joinFetchConverter.addConversionValue("outer-join", new Integer(ForeignReferenceMapping.OUTER_JOIN));
        joinFetchConverter.addConversionValue("none", new Integer(ForeignReferenceMapping.NONE));
        joinFetchMapping.setConverter(joinFetchConverter);
        joinFetchMapping.setNullValue(ForeignReferenceMapping.NONE);
        descriptor.addMapping(joinFetchMapping);
        
        return descriptor;
    }

    protected ClassDescriptor buildXMLAnyCollectionMappingDescriptor() {
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(XMLAnyCollectionMapping.class);

        descriptor.getInheritancePolicy().setParentClass(DatabaseMapping.class);

        XMLCompositeObjectMapping fieldMapping = new XMLCompositeObjectMapping();
        fieldMapping.setAttributeName("field");
        fieldMapping.setReferenceClass(DatabaseField.class);
        fieldMapping.setGetMethodName("getField");
        fieldMapping.setSetMethodName("setField");
        fieldMapping.setXPath("toplink:field");
        descriptor.addMapping(fieldMapping);

        XMLCompositeObjectMapping containerPolicyMapping = new XMLCompositeObjectMapping();
        containerPolicyMapping.setAttributeName("collectionPolicy");
        containerPolicyMapping.setGetMethodName("getContainerPolicy");
        containerPolicyMapping.setSetMethodName("setContainerPolicy");
        containerPolicyMapping.setReferenceClass(oracle.toplink.internal.queryframework.ContainerPolicy.class);
        containerPolicyMapping.setXPath("toplink:container");
        descriptor.addMapping(containerPolicyMapping);
        
        XMLDirectMapping xmlRootMapping = new XMLDirectMapping();
        xmlRootMapping.setAttributeName("useXMLRoot");
        xmlRootMapping.setGetMethodName("usesXMLRoot");
        xmlRootMapping.setSetMethodName("setUseXMLRoot");
        xmlRootMapping.setXPath("toplink:use-xml-root/text()");
        xmlRootMapping.setNullValue(Boolean.FALSE);
        descriptor.addMapping(xmlRootMapping);

        XMLDirectMapping keepAsElementMapping = new XMLDirectMapping();
        keepAsElementMapping.setAttributeName("keepAsElementPolicy");
        keepAsElementMapping.setGetMethodName("getKeepAsElementPolicy");
        keepAsElementMapping.setSetMethodName("setKeepAsElementPolicy");
        keepAsElementMapping.setXPath("toplink:keep-as-element-policy");
        EnumTypeConverter converter = new EnumTypeConverter(keepAsElementMapping, UnmarshalKeepAsElementPolicy.class, false);
        keepAsElementMapping.setConverter(converter);
        descriptor.addMapping(keepAsElementMapping);
        
        return descriptor;
    }
    
    protected ClassDescriptor buildXMLAnyAttributeMappingDescriptor() {
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(XMLAnyAttributeMapping.class);

        descriptor.getInheritancePolicy().setParentClass(DatabaseMapping.class);

        XMLCompositeObjectMapping fieldMapping = new XMLCompositeObjectMapping();
        fieldMapping.setAttributeName("field");
        fieldMapping.setReferenceClass(DatabaseField.class);
        fieldMapping.setGetMethodName("getField");
        fieldMapping.setSetMethodName("setField");
        fieldMapping.setXPath("toplink:field");
        descriptor.addMapping(fieldMapping);

        XMLCompositeObjectMapping containerPolicyMapping = new XMLCompositeObjectMapping();
        containerPolicyMapping.setAttributeName("collectionPolicy");
        containerPolicyMapping.setGetMethodName("getContainerPolicy");
        containerPolicyMapping.setSetMethodName("setContainerPolicy");
        containerPolicyMapping.setReferenceClass(oracle.toplink.internal.queryframework.ContainerPolicy.class);
        containerPolicyMapping.setXPath("toplink:container");
        descriptor.addMapping(containerPolicyMapping);

        return descriptor;
    }

    protected ClassDescriptor buildXMLCollectionReferenceMappingDescriptor() {
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(XMLCollectionReferenceMapping.class);
        descriptor.getInheritancePolicy().setParentClass(XMLObjectReferenceMapping.class);

        XMLCompositeObjectMapping containerPolicyMapping = new XMLCompositeObjectMapping();
        containerPolicyMapping.setAttributeName("containerPolicy");
        containerPolicyMapping.setGetMethodName("getContainerPolicy");
        containerPolicyMapping.setSetMethodName("setContainerPolicy");
        containerPolicyMapping.setReferenceClass(oracle.toplink.internal.queryframework.ContainerPolicy.class);
        containerPolicyMapping.setXPath("toplink:containerpolicy");
        descriptor.addMapping(containerPolicyMapping);
        
        XMLDirectMapping useSingleNodeMapping = new XMLDirectMapping();
        useSingleNodeMapping.setAttributeName("usesSingleNode");
        useSingleNodeMapping.setGetMethodName("usesSingleNode");
        useSingleNodeMapping.setSetMethodName("setUsesSingleNode");
        useSingleNodeMapping.setXPath("toplink:uses-single-node/text()");
        descriptor.addMapping(useSingleNodeMapping);
        
        return descriptor;
    }

    protected ClassDescriptor buildXMLObjectReferenceMappingDescriptor() {
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(XMLObjectReferenceMapping.class);
        descriptor.getInheritancePolicy().setParentClass(AggregateMapping.class);

        XMLCompositeCollectionMapping sourceToTargetKeyFieldAssociationsMapping = new XMLCompositeCollectionMapping();
        sourceToTargetKeyFieldAssociationsMapping.setReferenceClass(Association.class);
        // Handle translation of foreign key associations to hashmaps.
        sourceToTargetKeyFieldAssociationsMapping.setAttributeAccessor(new AttributeAccessor() {
                public Object getAttributeValueFromObject(Object object) {
                    Map sourceToTargetKeyFields = ((XMLObjectReferenceMapping) object).getSourceToTargetKeyFieldAssociations();
                    List associations = new ArrayList(sourceToTargetKeyFields.size());
                    Iterator iterator = sourceToTargetKeyFields.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry entry = (Map.Entry)iterator.next();
                        associations.add(new Association(entry.getKey(), entry.getValue()));
                    }
                    return associations;
                }

                public void setAttributeValueInObject(Object object, Object value) {
                    XMLObjectReferenceMapping mapping = (XMLObjectReferenceMapping) object;
                    List associations = (List)value;
                    mapping.setSourceToTargetKeyFieldAssociations(new HashMap(associations.size() + 1));
                    Iterator iterator = associations.iterator();
                    while (iterator.hasNext()) {
                        Association association = (Association)iterator.next();
                        mapping.getSourceToTargetKeyFieldAssociations().put(association.getKey(), association.getValue());
                    }
                }
            });
        sourceToTargetKeyFieldAssociationsMapping.setAttributeName("sourceToTargetKeyFieldAssociations");
        sourceToTargetKeyFieldAssociationsMapping.setXPath("toplink:source-to-target-key-field-association/opm:field-reference");
        descriptor.addMapping(sourceToTargetKeyFieldAssociationsMapping);
        
        XMLCompositeCollectionMapping sourceToTargetKeysMapping = new XMLCompositeCollectionMapping();
        sourceToTargetKeysMapping.setReferenceClass(DatabaseField.class);
        sourceToTargetKeysMapping.setAttributeName("sourceToTargetKeys");
        sourceToTargetKeysMapping.setXPath("toplink:source-to-target-key-fields/toplink:field");
        descriptor.addMapping(sourceToTargetKeysMapping);

        return descriptor;
    }
    
    protected ClassDescriptor buildXMLFragmentMappingDescriptor() {
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(XMLFragmentMapping.class);
        descriptor.getInheritancePolicy().setParentClass(XMLDirectMapping.class);
        
        return descriptor;
    }
    
    protected ClassDescriptor buildXMLFragmentCollectionMappingDescriptor() {
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(XMLFragmentCollectionMapping.class);
        descriptor.getInheritancePolicy().setParentClass(AbstractCompositeDirectCollectionMapping.class);
        
        return descriptor;
    }
    protected ClassDescriptor buildXMLAnyObjectMappingDescriptor() {
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(XMLAnyObjectMapping.class);
        descriptor.getInheritancePolicy().setParentClass(DatabaseMapping.class);

        XMLCompositeObjectMapping fieldMapping = new XMLCompositeObjectMapping();
        fieldMapping.setAttributeName("field");
        fieldMapping.setReferenceClass(DatabaseField.class);
        fieldMapping.setGetMethodName("getField");
        fieldMapping.setSetMethodName("setField");
        fieldMapping.setXPath("toplink:field");
        descriptor.addMapping(fieldMapping);

        XMLDirectMapping xmlRootMapping = new XMLDirectMapping();
        xmlRootMapping.setAttributeName("useXMLRoot");
        xmlRootMapping.setGetMethodName("usesXMLRoot");
        xmlRootMapping.setSetMethodName("setUseXMLRoot");
        xmlRootMapping.setXPath("toplink:use-xml-root/text()");
        xmlRootMapping.setNullValue(Boolean.FALSE);
        descriptor.addMapping(xmlRootMapping);

        return descriptor;
    }

    protected ClassDescriptor buildXMLFieldDescriptor() {
        XMLDescriptor descriptor = new XMLDescriptor();

        descriptor.setJavaClass(XMLField.class);
        descriptor.getInheritancePolicy().setParentClass(DatabaseField.class);

        XMLDirectMapping typedFieldMapping = new XMLDirectMapping();
        typedFieldMapping.setAttributeName("isTypedTextField");
        typedFieldMapping.setGetMethodName("isTypedTextField");
        typedFieldMapping.setSetMethodName("setIsTypedTextField");
        typedFieldMapping.setXPath("toplink:typed-text-field/text()");
        typedFieldMapping.setNullValue(Boolean.FALSE);
        descriptor.addMapping(typedFieldMapping);

        XMLDirectMapping singleNodeMapping = new XMLDirectMapping();
        singleNodeMapping.setAttributeName("usesSingleNode");
        singleNodeMapping.setGetMethodName("usesSingleNode");
        singleNodeMapping.setSetMethodName("setUsesSingleNode");
        singleNodeMapping.setXPath("toplink:single-node/text()");
        singleNodeMapping.setNullValue(Boolean.FALSE);
        descriptor.addMapping(singleNodeMapping);

        XMLDirectMapping schemaTypeMapping = new XMLDirectMapping();
        schemaTypeMapping.setAttributeName("schemaType");
        schemaTypeMapping.setGetMethodName("getSchemaType");
        schemaTypeMapping.setSetMethodName("setSchemaType");
        schemaTypeMapping.setXPath("toplink:schema-type/text()");
        descriptor.addMapping(schemaTypeMapping);

        XMLCompositeCollectionMapping xmlToJavaPairsMapping = new XMLCompositeCollectionMapping();
        xmlToJavaPairsMapping.setXPath("toplink:xml-to-java-conversion-pair");
        xmlToJavaPairsMapping.useCollectionClass(ArrayList.class);
        xmlToJavaPairsMapping.setReferenceClass(XMLConversionPair.class);
        xmlToJavaPairsMapping.setAttributeName("userXMLTypes");
        xmlToJavaPairsMapping.setGetMethodName("getUserXMLTypesForDeploymentXML");
        xmlToJavaPairsMapping.setSetMethodName("setUserXMLTypesForDeploymentXML");
        descriptor.addMapping(xmlToJavaPairsMapping);

        XMLCompositeCollectionMapping javaToXMLPairsMapping = new XMLCompositeCollectionMapping();
        javaToXMLPairsMapping.useCollectionClass(ArrayList.class);
        javaToXMLPairsMapping.setXPath("toplink:java-to-xml-conversion-pair");
        javaToXMLPairsMapping.setReferenceClass(XMLConversionPair.class);
        javaToXMLPairsMapping.setAttributeName("userJavaTypes");
        javaToXMLPairsMapping.setGetMethodName("getUserJavaTypesForDeploymentXML");
        javaToXMLPairsMapping.setSetMethodName("setUserJavaTypesForDeploymentXML");
        descriptor.addMapping(javaToXMLPairsMapping);

        XMLDirectMapping leafElementTypeMapping = new XMLDirectMapping();
        leafElementTypeMapping.setAttributeName("leafElementType");
        leafElementTypeMapping.setGetMethodName("getLeafElementType");
        leafElementTypeMapping.setSetMethodName("setLeafElementType");
        leafElementTypeMapping.setXPath("toplink:leaf-element-type/text()");
        descriptor.addMapping(leafElementTypeMapping);
        
        return descriptor;
    }
    
    protected ClassDescriptor buildClassDescriptorDescriptor() {
        ClassDescriptor descriptor = super.buildClassDescriptorDescriptor();
        
        XMLDirectMapping identityMapClassMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("identityMapClass");
        ObjectTypeConverter identityMapClassConverter = (ObjectTypeConverter)identityMapClassMapping.getConverter();
        identityMapClassConverter.addConversionValue("soft-reference", SoftIdentityMap.class);
	
	XMLDirectMapping remoteIdentityMapClassMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("remoteIdentityMapClass");
        ObjectTypeConverter remoteIdentityMapClassConverter = (ObjectTypeConverter)remoteIdentityMapClassMapping.getConverter();
        remoteIdentityMapClassConverter.addConversionValue("soft-reference", SoftIdentityMap.class); 
        
        XMLDirectMapping unitOfWorkCacheIsolationLevelMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("unitOfWorkCacheIsolationLevel");
        ObjectTypeConverter unitOfWorkCacheIsolationLevelConverter = (ObjectTypeConverter)unitOfWorkCacheIsolationLevelMapping.getConverter();
        unitOfWorkCacheIsolationLevelConverter.addConversionValue("default", new Integer(ClassDescriptor.UNDEFINED_ISOLATATION));
        unitOfWorkCacheIsolationLevelMapping.setNullValue(new Integer(ClassDescriptor.UNDEFINED_ISOLATATION));
        
        return descriptor;
    }

    // support for Stored Procedure/Function Calls
    @Override
    protected ClassDescriptor buildCallDescriptor() {
      XMLDescriptor descriptor = (XMLDescriptor)super.buildCallDescriptor();
      descriptor.getInheritancePolicy().addClassIndicator(StoredProcedureCall.class,
          "toplink:stored-procedure-call");
      descriptor.getInheritancePolicy().addClassIndicator(StoredFunctionCall.class,
          "toplink:stored-function-call");
      descriptor.getInheritancePolicy().addClassIndicator(PLSQLStoredProcedureCall.class,
          "toplink:plsql-stored-procedure-call");
      return descriptor;
    }
    
    /**
     * <p>
     * <b>Purpose</b>: helper classes - represent stored procedure arguments in XML
     * <p>
     * 
     * @author Kyle Chen
     * @since 11
     * 
     * mnorman - moved from o.t.i.workbench.storedprocedure to
     *           be nested inner classes of ObjectPersistenceRuntimeXMLProject_11_1_1
     *           so that they don't 'leak' out into the runtime
     */
    class StoredProcedureArgument {
          String argumentName;
          String argumentFieldName;
          Class argumentType;
          int argumentSQLType = NULL_SQL_TYPE;
          String argumentSqlTypeName;
          Object argumentValue;
          StoredProcedureArgument nestedType;
          StoredProcedureArgument() {
              super();
          }
          StoredProcedureArgument(DatabaseField dbfield) {
              this.setDatabaseField(dbfield);
          }
          Integer getDirection() {
              return IN;
          }
          DatabaseField getDatabaseField() {
              DatabaseField dbfield = new DatabaseField(argumentFieldName == null ? "" : argumentFieldName);
              dbfield.type = argumentType;
              dbfield.sqlType = argumentSQLType;
              if ((argumentSqlTypeName != null) && 
                  (!argumentSqlTypeName.equals(""))) {
                  dbfield = new ObjectRelationalDatabaseField(dbfield);
                  ((ObjectRelationalDatabaseField)dbfield).setSqlTypeName(argumentSqlTypeName);
                  if (nestedType != null) {
                      ((ObjectRelationalDatabaseField)dbfield).setNestedTypeField(
                          nestedType.getDatabaseField());
                  }
              }
              return dbfield;
          }
          void setDatabaseField(DatabaseField dbField) {
              String fieldName = dbField.getName();
              if (fieldName != null && fieldName.length() > 0) {
                 argumentFieldName = fieldName;
              }
              argumentType = dbField.type;
              argumentSQLType = dbField.sqlType;
              if (dbField.isObjectRelationalDatabaseField()) {
                  ObjectRelationalDatabaseField ordField = 
                      (ObjectRelationalDatabaseField)dbField;
                  argumentSqlTypeName = ordField.getSqlTypeName();
                  DatabaseField tempField = ordField.getNestedTypeField();
                  if (tempField != null) {
                      nestedType = new StoredProcedureArgument(tempField);
                  }
              }
          }
    }
    
    class StoredProcedureInOutArgument extends StoredProcedureArgument {
          String outputArgumentName;
          StoredProcedureInOutArgument() {
              super();
          }
          StoredProcedureInOutArgument(DatabaseField dbfield) {
              super(dbfield);
          }
          Integer getDirection() {
              return INOUT;
          }
    }
    
    class StoredProcedureOutArgument extends StoredProcedureArgument {
        StoredProcedureOutArgument() {
            super();
        }
        StoredProcedureOutArgument(DatabaseField dbfield){
            super(dbfield);
        }
        Integer getDirection() {
            return OUT;
        }
    }
    
    class StoredProcedureOutCursorArgument extends StoredProcedureOutArgument {
        StoredProcedureOutCursorArgument() {
            super();
        }
        StoredProcedureOutCursorArgument(DatabaseField dbfield){
            super(dbfield);
        }
        Integer getDirection() {
            return OUT_CURSOR;
        }
    }
    
    enum StoredProcedureArgumentType {
        STORED_PROCEDURE_ARG,
        STORED_PROCEDURE_INOUT_ARG,
        STORED_PROCEDURE_OUT_ARG,
        STORED_PROCEDURE_OUTCURSOR_ARG
    }
    class StoredProcedureArgumentInstantiationPolicy extends InstantiationPolicy {
        ObjectPersistenceRuntimeXMLProject_11_1_1 outer; 
        StoredProcedureArgumentType argType;
        StoredProcedureArgumentInstantiationPolicy(
            ObjectPersistenceRuntimeXMLProject_11_1_1 outer, StoredProcedureArgumentType argType) {
            this.outer = outer;
            this.argType = argType;
        }
        @Override
        public Object buildNewInstance() throws DescriptorException {
            Object arg = null;
            switch (argType) {
                case STORED_PROCEDURE_ARG:
                    arg = outer.new StoredProcedureArgument();
                    break;
                case STORED_PROCEDURE_INOUT_ARG:
                    arg = outer.new StoredProcedureInOutArgument();
                    break;
                case STORED_PROCEDURE_OUT_ARG:
                    arg = outer.new StoredProcedureOutArgument();
                    break;
                case STORED_PROCEDURE_OUTCURSOR_ARG:
                    arg = outer.new StoredProcedureOutCursorArgument();
                    break;
            }
            return arg;
        }
    }

    protected ClassDescriptor buildStoredProcedureArgumentDescriptor() {
        
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(StoredProcedureArgument.class);
        // need policy 'cause TreeBuilder cannot use default constructor
        descriptor.setInstantiationPolicy(new StoredProcedureArgumentInstantiationPolicy(this,
            StoredProcedureArgumentType.STORED_PROCEDURE_ARG));
        descriptor.descriptorIsAggregate();

        descriptor.setDefaultRootElement("argument");
        descriptor.getInheritancePolicy().setClassIndicatorField(new XMLField("@xsi:type"));
        descriptor.getInheritancePolicy().addClassIndicator(StoredProcedureArgument.class,
            "toplink:procedure-argument");
        descriptor.getInheritancePolicy().addClassIndicator(StoredProcedureInOutArgument.class,
            "toplink:procedure-inoutput-argument");
        descriptor.getInheritancePolicy().addClassIndicator(StoredProcedureOutArgument.class,
            "toplink:procedure-output-argument");
        descriptor.getInheritancePolicy().addClassIndicator(StoredProcedureOutCursorArgument.class,
            "toplink:procedure-output-cursor-argument");
         
        XMLDirectMapping argumentNameMapping = new XMLDirectMapping();
        argumentNameMapping.setAttributeName("argumentName");
        argumentNameMapping.setXPath("toplink:procedure-argument-name/text()");
        descriptor.addMapping(argumentNameMapping);

        XMLDirectMapping argumentFieldNameMapping = new XMLDirectMapping();
        argumentFieldNameMapping.setAttributeName("argumentFieldName");
        argumentFieldNameMapping.setXPath("toplink:argument-name/text()");
        argumentFieldNameMapping.setNullValue("");
        descriptor.addMapping(argumentFieldNameMapping);
         
        XMLDirectMapping argumentTypeMapping = new XMLDirectMapping();
        argumentTypeMapping.setAttributeName("argumentType");
        argumentTypeMapping.setXPath("toplink:procedure-argument-type/text()");
        descriptor.addMapping(argumentTypeMapping);
         
        XMLDirectMapping argumentSQLTypeMapping = new XMLDirectMapping();
        argumentSQLTypeMapping.setAttributeName("argumentSQLType");
        argumentSQLTypeMapping.setXPath("toplink:procedure-argument-sqltype/text()");
        argumentSQLTypeMapping.setNullValue(NULL_SQL_TYPE);
        descriptor.addMapping(argumentSQLTypeMapping);
        
        XMLDirectMapping argumentSqlTypeNameMapping = new XMLDirectMapping();
        argumentSqlTypeNameMapping.setAttributeName("argumentSqlTypeName");
        argumentSqlTypeNameMapping.setXPath("toplink:procedure-argument-sqltype-name/text()");
        descriptor.addMapping(argumentSqlTypeNameMapping);

        XMLDirectMapping argumentValueMapping = new XMLDirectMapping();
        argumentValueMapping.setAttributeName("argumentValue");
        argumentValueMapping.setField(buildTypedField("toplink:argument-value/text()"));
        descriptor.addMapping(argumentValueMapping);
        
        XMLCompositeObjectMapping nestedTypeMapping = new XMLCompositeObjectMapping();
        nestedTypeMapping.setAttributeName("nestedType");
        nestedTypeMapping.setReferenceClass(StoredProcedureArgument.class);
        nestedTypeMapping.setXPath("toplink:nested-type-field");
        descriptor.addMapping(nestedTypeMapping);
        
        return descriptor;
    }

    protected ClassDescriptor buildStoredProcedureInOutArgumentsDescriptor() {
        
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(StoredProcedureInOutArgument.class);
        descriptor.setInstantiationPolicy(new StoredProcedureArgumentInstantiationPolicy(this,
            StoredProcedureArgumentType.STORED_PROCEDURE_INOUT_ARG));
        descriptor.getInheritancePolicy().setParentClass(StoredProcedureArgument.class);

        //used in case the in databasefield is named different than the out databasefield
        XMLDirectMapping outputArgumentNameMapping = new XMLDirectMapping();
        outputArgumentNameMapping.setAttributeName("outputArgumentName");
        outputArgumentNameMapping.setXPath("toplink:output-argument-name/text()");
        descriptor.addMapping(outputArgumentNameMapping);
        
        return descriptor;
    }
    
    protected ClassDescriptor buildStoredProcedureOutArgumentsDescriptor() {

        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(StoredProcedureOutArgument.class);
        descriptor.setInstantiationPolicy(new StoredProcedureArgumentInstantiationPolicy(this,
            StoredProcedureArgumentType.STORED_PROCEDURE_OUT_ARG));
        descriptor.getInheritancePolicy().setParentClass(StoredProcedureArgument.class);

        return descriptor;
    }
    
    protected ClassDescriptor buildStoredProcedureOutCursorArgumentsDescriptor() {

        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(StoredProcedureOutCursorArgument.class);
        descriptor.setInstantiationPolicy(new StoredProcedureArgumentInstantiationPolicy(this,
            StoredProcedureArgumentType.STORED_PROCEDURE_OUTCURSOR_ARG));
        descriptor.getInheritancePolicy().setParentClass(StoredProcedureArgument.class);

        return descriptor;
    }
    
    class StoredProcedureArgumentsAccessor extends AttributeAccessor {
        StoredProcedureArgumentsAccessor() {
            super();
        }
        @Override   
        public Object getAttributeValueFromObject(Object anObject) throws DescriptorException {
            StoredProcedureCall spc = (StoredProcedureCall)anObject;
            Vector parameterTypes = spc.getParameterTypes();
            Vector parameters = spc.getParameters();
            Vector procedureArgumentNames = spc.getProcedureArgumentNames();
            Vector storedProcedureArguments = new Vector();
            for (int i = spc.getFirstParameterIndexForCallString(); i < parameterTypes.size(); i++) {
                StoredProcedureArgument spa = null;
                Integer direction = (Integer)parameterTypes.get(i);
                Object argument = parameters.get(i);
                String argumentName = (String)procedureArgumentNames.get(i);
                if (direction.equals(IN)) {
                    spa = new StoredProcedureArgument();
                }
                else if (direction.equals(OUT)) {
                    spa = new StoredProcedureOutArgument();
                }
                else if (direction.equals(INOUT)) {
                    spa = new StoredProcedureInOutArgument();
                    // outputArgumentName ??
                }
                else {
                    // assume OUT_CURSOR
                    spa = new StoredProcedureOutCursorArgument();
                }
                spa.argumentName = argumentName;
                if (argument instanceof DatabaseField) {
                    DatabaseField argField = (DatabaseField)argument;
                    spa.setDatabaseField(argField);
                }
                else {
                    if (argument instanceof Object[]) {
                       Object first = ((Object[])argument)[0];
                       DatabaseField secondField = (DatabaseField)((Object[])argument)[1];;
                       if (first instanceof DatabaseField) {
                           DatabaseField firstField = (DatabaseField)first;
                           spa.setDatabaseField(firstField);
                       }
                       else {
                           spa.argumentValue = first;
                           spa.setDatabaseField(secondField);
                       }
                       ((StoredProcedureInOutArgument)spa).outputArgumentName = 
                           secondField.getName();
                    }
                    else {
                        spa.argumentValue = argument;
                    }
                }
                storedProcedureArguments.add(spa);
            }
            return storedProcedureArguments;
        }
        @Override
        public void setAttributeValueInObject(Object domainObject, Object attributeValue) throws DescriptorException {
            StoredProcedureCall spc = (StoredProcedureCall)domainObject;
            // vector of parameters/arguments to be added the call
            Vector procedureArguments = (Vector)attributeValue;
            for (int i = 0; i < procedureArguments.size(); i++) {
                StoredProcedureArgument spa = (StoredProcedureArgument)procedureArguments.get(i);
                Integer direction = spa.getDirection();
                DatabaseField dbField = spa.getDatabaseField();
                spc.getProcedureArgumentNames().add(spa.argumentName);
                if (direction.equals(IN)) {
                    if (spa.argumentValue != null) {
                        spc.appendIn(spa.argumentValue);
                    }
                    else {
                        spc.appendIn(dbField);
                    }
                }
                else if (direction.equals(OUT)) {
                    spc.appendOut(dbField);
                }
                else if (direction.equals(OUT_CURSOR)) {
                    spc.appendOutCursor(dbField);
                }
                else  if (direction.equals(INOUT)) {
                    StoredProcedureInOutArgument spaInOut = (StoredProcedureInOutArgument)spa;
                    DatabaseField outField = new DatabaseField(spaInOut.outputArgumentName);
                    outField.type = dbField.type;
                    if (spaInOut.argumentValue != null) {
                        spc.appendInOut(spaInOut.argumentValue, outField);
                    }
                    else {
                        spc.appendInOut(dbField, outField);
                    }
                }
            }
        }
    }

    protected ClassDescriptor buildStoredProcedureCallDescriptor() {
        
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(StoredProcedureCall.class);
        descriptor.getInheritancePolicy().setParentClass(Call.class);
        descriptor.descriptorIsAggregate();

        XMLDirectMapping procedureNameMapping = new XMLDirectMapping();
        procedureNameMapping.setAttributeName("procedureName");
        procedureNameMapping.setGetMethodName("getProcedureName");
        procedureNameMapping.setSetMethodName("setProcedureName");
        procedureNameMapping.setXPath("toplink:procedure-name/text()");
        descriptor.addMapping(procedureNameMapping);
        
        XMLDirectMapping cursorOutputProcedureMapping = new XMLDirectMapping();
        cursorOutputProcedureMapping.setAttributeName("isCursorOutputProcedure");
        cursorOutputProcedureMapping.setXPath("toplink:cursor-output-procedure/text()");
        descriptor.addMapping(cursorOutputProcedureMapping);

        XMLCompositeCollectionMapping storedProcArgumentsMapping = new XMLCompositeCollectionMapping();
        storedProcArgumentsMapping.useCollectionClass(NonSynchronizedVector.class);
        storedProcArgumentsMapping.setAttributeName("procedureArguments");
        storedProcArgumentsMapping.setAttributeAccessor(new StoredProcedureArgumentsAccessor());
        storedProcArgumentsMapping.setReferenceClass(StoredProcedureArgument.class);
        storedProcArgumentsMapping.setXPath("toplink:arguments/toplink:argument");
        descriptor.addMapping(storedProcArgumentsMapping);
        
        return descriptor;
    }
    
    class StoredFunctionResultAccessor extends AttributeAccessor {
        StoredFunctionResultAccessor() {
            super();
        }
        // for StoredFunctionCalls, the return value's information
        // is stored in the parameters list at index 0
        @Override
        public Object getAttributeValueFromObject(Object anObject) throws DescriptorException {
            StoredFunctionCall sfc = (StoredFunctionCall)anObject;
            Object argument = sfc.getParameters().get(0);
            String argumentName = (String)sfc.getProcedureArgumentNames().get(0);
            StoredProcedureOutArgument outArgument = new StoredProcedureOutArgument((DatabaseField)argument);
            outArgument.argumentName = argumentName;
            return outArgument;
        }
        @Override
        public void setAttributeValueInObject(Object domainObject, Object attributeValue) throws DescriptorException {
            StoredFunctionCall sfc = (StoredFunctionCall)domainObject;
            StoredProcedureOutArgument spoa = (StoredProcedureOutArgument)attributeValue;
            // Set procedure argument name.
            sfc.getProcedureArgumentNames().set(0, spoa.argumentName);
            sfc.getParameters().set(0, spoa.getDatabaseField());
            // Set argument type.
            sfc.getParameterTypes().set(0, OUT);
        }
    }
    
    protected ClassDescriptor buildStoredFunctionCallDescriptor() {
        
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(StoredFunctionCall.class);
        descriptor.getInheritancePolicy().setParentClass(StoredProcedureCall.class);
        descriptor.descriptorIsAggregate();
        
        XMLCompositeObjectMapping storedFunctionResultMapping = new XMLCompositeObjectMapping();
        storedFunctionResultMapping.setAttributeName("storedFunctionResult");
        storedFunctionResultMapping.setReferenceClass(StoredProcedureOutArgument.class);
        storedFunctionResultMapping.setAttributeAccessor(new StoredFunctionResultAccessor());
        storedFunctionResultMapping.setXPath("toplink:stored-function-result");
        descriptor.addMapping(storedFunctionResultMapping);

        return descriptor;
    }
    
    @Override
    protected ClassDescriptor buildXMLDirectMappingDescriptor() {
        ClassDescriptor descriptor = super.buildXMLDirectMappingDescriptor();

        XMLDirectMapping isCDATAMapping = new XMLDirectMapping();
        isCDATAMapping.setAttributeName("isCDATA");
        isCDATAMapping.setGetMethodName("isCDATA");
        isCDATAMapping.setSetMethodName("setIsCDATA");
        isCDATAMapping.setXPath("toplink:is-cdata/text()");
        isCDATAMapping.setNullValue(Boolean.FALSE);
        descriptor.addMapping(isCDATAMapping);
        
        // Add Null Policy
        XMLCompositeObjectMapping aMapping = new XMLCompositeObjectMapping();
        aMapping.setReferenceClass(AbstractNullPolicy.class);
        aMapping.setAttributeName("nullPolicy");
        aMapping.setXPath("toplink:null-policy");
        ((DatabaseMapping)aMapping).setAttributeAccessor(new NullPolicyAttributeAccessor());
        descriptor.addMapping(aMapping);       
        
        return descriptor;
    }
    
    protected ClassDescriptor buildXMLCompositeDirectCollectionMappingDescriptor() {
        XMLDescriptor descriptor = new XMLDescriptor();
        descriptor.setJavaClass(XMLCompositeDirectCollectionMapping.class);

        descriptor.getInheritancePolicy().setParentClass(AbstractCompositeDirectCollectionMapping.class);
        
        XMLDirectMapping isCDATAMapping = new XMLDirectMapping();
        isCDATAMapping.setAttributeName("isCDATA");
        isCDATAMapping.setGetMethodName("isCDATA");
        isCDATAMapping.setSetMethodName("setIsCDATA");
        isCDATAMapping.setXPath("toplink:is-cdata/text()");
        isCDATAMapping.setNullValue(Boolean.FALSE);
        descriptor.addMapping(isCDATAMapping);
        
        return descriptor;
    }    
    
     protected ClassDescriptor buildXMLLoginDescriptor(){
        ClassDescriptor descriptor = super.buildXMLLoginDescriptor();
        
        XMLDirectMapping equalNamespaceResolversMapping = new XMLDirectMapping();
        equalNamespaceResolversMapping.setAttributeName("equalNamespaceResolvers");
        equalNamespaceResolversMapping.setGetMethodName("hasEqualNamespaceResolvers");
        equalNamespaceResolversMapping.setSetMethodName("setEqualNamespaceResolvers");
        equalNamespaceResolversMapping.setXPath("toplink:equal-namespace-resolvers/text()");
        equalNamespaceResolversMapping.setNullValue(Boolean.TRUE);
        descriptor.addMapping(equalNamespaceResolversMapping);
        
        return descriptor;
    }

    protected ClassDescriptor buildAbstractNullPolicyDescriptor() {
         XMLDescriptor aDescriptor = new XMLDescriptor();
         aDescriptor.setJavaClass(AbstractNullPolicy.class);
         aDescriptor.setDefaultRootElement("abstract-null-policy");

         XMLDirectMapping xnrnMapping = new XMLDirectMapping();
         xnrnMapping.setAttributeName("isNullRepresentedByXsiNil");
         xnrnMapping.setXPath("toplink:xsi-nil-represents-null/text()");
         xnrnMapping.setNullValue(Boolean.FALSE);         
         aDescriptor.addMapping(xnrnMapping);

         XMLDirectMapping enrnMapping = new XMLDirectMapping();
         enrnMapping.setAttributeName("isNullRepresentedByEmptyNode");
         enrnMapping.setXPath("toplink:empty-node-represents-null/text()");
         enrnMapping.setNullValue(Boolean.FALSE);         
         aDescriptor.addMapping(enrnMapping);

         XMLDirectMapping nrfxMapping = new XMLDirectMapping();
         nrfxMapping.setAttributeName("marshalNullRepresentation");
         nrfxMapping.setXPath("toplink:null-representation-for-xml/text()");
         // Restricted to XSI_NIL,ABSENT_NODE,EMPTY_NODE	
         EnumTypeConverter aConverter = new EnumTypeConverter(nrfxMapping, XMLNullRepresentationType.class, false);
         nrfxMapping.setConverter(aConverter);
         aDescriptor.addMapping(nrfxMapping);
         
         // Subclasses
         aDescriptor.getInheritancePolicy().setClassIndicatorField(new XMLField("@xsi:type"));
         aDescriptor.getInheritancePolicy().addClassIndicator(IsSetNullPolicy.class, "toplink:is-set-null-policy");
         aDescriptor.getInheritancePolicy().addClassIndicator(NullPolicy.class, "toplink:null-policy");

         return aDescriptor;
     }

	@Override
    protected ClassDescriptor buildNamespaceResolverDescriptor() {
        XMLDescriptor descriptor = (XMLDescriptor)super.buildNamespaceResolverDescriptor();

        XMLDirectMapping defaultNamespaceMapping = new XMLDirectMapping();
        defaultNamespaceMapping.setXPath("toplink:default-namespace-uri");
        defaultNamespaceMapping.setAttributeName("defaultNamespaceURI");
        defaultNamespaceMapping.setGetMethodName("getDefaultNamespaceURI");
        defaultNamespaceMapping.setSetMethodName("setDefaultNamespaceURI");
        descriptor.addMapping(defaultNamespaceMapping);

        return descriptor;
    }
    
    protected ClassDescriptor buildNullPolicyDescriptor() {
         XMLDescriptor aDescriptor = new XMLDescriptor();
         aDescriptor.setJavaClass(NullPolicy.class);
         aDescriptor.getInheritancePolicy().setParentClass(AbstractNullPolicy.class);

         // This boolean can only be set on the NullPolicy implementation even though the field is on the abstract class
         XMLDirectMapping xnranMapping = new XMLDirectMapping();
         xnranMapping.setAttributeName("isSetPerformedForAbsentNode");
         xnranMapping.setXPath("toplink:is-set-performed-for-absent-node/text()");
         xnranMapping.setNullValue(Boolean.TRUE);         
         aDescriptor.addMapping(xnranMapping);

         return aDescriptor;
     }
     
     protected ClassDescriptor buildIsSetNullPolicyDescriptor() {
         // The IsSetPerformedForAbsentNode flag is always false on this IsSet mapping
    	 XMLDescriptor aDescriptor = new XMLDescriptor();
         aDescriptor.setJavaClass(IsSetNullPolicy.class);
         aDescriptor.getInheritancePolicy().setParentClass(AbstractNullPolicy.class);

         XMLDirectMapping isSetMethodNameMapping = new XMLDirectMapping();
         isSetMethodNameMapping.setAttributeName("isSetMethodName");
         isSetMethodNameMapping.setXPath("toplink:is-set-method-name/text()");
         aDescriptor.addMapping(isSetMethodNameMapping);

         // 20070922: Bug#6039730 - add IsSet capability for 1+ parameters for SDO
         XMLCompositeDirectCollectionMapping isSetParameterTypesMapping = new XMLCompositeDirectCollectionMapping();
         isSetParameterTypesMapping.setAttributeName("isSetParameterTypes");
         isSetParameterTypesMapping.setXPath("toplink:is-set-parameter-type");
         ((DatabaseMapping)isSetParameterTypesMapping).setAttributeAccessor(new IsSetNullPolicyIsSetParameterTypesAttributeAccessor());         
         aDescriptor.addMapping(isSetParameterTypesMapping);

         XMLCompositeDirectCollectionMapping isSetParametersMapping = new XMLCompositeDirectCollectionMapping();
         isSetParametersMapping.setAttributeName("isSetParameters");
         isSetParametersMapping.setXPath("toplink:is-set-parameter");         
         ((DatabaseMapping)isSetParametersMapping).setAttributeAccessor(new IsSetNullPolicyIsSetParametersAttributeAccessor());         
         aDescriptor.addMapping(isSetParametersMapping);

         return aDescriptor;
     }

     /**
      * INTERNAL:
      * Wrap the isset parameter object array as a Collection.
      * Prerequisite: parameterTypes must be set.
      */
     public class IsSetNullPolicyIsSetParametersAttributeAccessor extends AttributeAccessor {
         public IsSetNullPolicyIsSetParametersAttributeAccessor() {
        	 super();
         }
         
         @Override   
         public Object getAttributeValueFromObject(Object object) throws DescriptorException {
        	 IsSetNullPolicy aPolicy = (IsSetNullPolicy)object;
       		 NonSynchronizedVector aCollection = new NonSynchronizedVector();
       		 for(int i = 0, size = aPolicy.getIsSetParameters().length; i<size;i++) {
       			 aCollection.add(aPolicy.getIsSetParameters()[i]);
       		 }
       		 return aCollection;
         }
         
         @Override
         public void setAttributeValueInObject(Object object, Object value) throws DescriptorException {
        	 // Convert the collection of Strings to an array of Object values (round-trip)
        	 if(value instanceof Collection) {
    			 int i = 0;    			 
    			 Object[] parameters = new Object[((Collection)value).size()];
    			 for(Iterator anIterator = ((Collection)value).iterator(); anIterator.hasNext();) {
   					 // Lookup the object type via the predefined parameterTypes array and convert based on that type
   					 parameters[i] = XMLConversionManager.getDefaultXMLManager().convertObject(//
   							 anIterator.next(), ((IsSetNullPolicy)object).getIsSetParameterTypes()[i++]);
    			 }
    			 ((IsSetNullPolicy)object).setIsSetParameters(parameters);
        	 } else {
        		 // Cast to object array
        		 ((IsSetNullPolicy)object).setIsSetParameters((Object[])value);
        	 }
         }
     }

     /**
      * INTERNAL:
      * Wrap the isset parameterType class array as a Collection
      */
     public class IsSetNullPolicyIsSetParameterTypesAttributeAccessor extends AttributeAccessor {
         public IsSetNullPolicyIsSetParameterTypesAttributeAccessor() {
             super();
         }
         
         @Override   
         public Object getAttributeValueFromObject(Object object) throws DescriptorException {
        	 IsSetNullPolicy aPolicy = (IsSetNullPolicy)object;
       		 NonSynchronizedVector aCollection = new NonSynchronizedVector();
       		 for(int i = 0, size = aPolicy.getIsSetParameterTypes().length; i<size;i++) {
       			 aCollection.add(aPolicy.getIsSetParameterTypes()[i]);
       		 }
       		 return aCollection;
         }
         
         @Override
         public void setAttributeValueInObject(Object object, Object value) throws DescriptorException {
        	 try {
        		 // Get the Class of each entry in the collection
        		 if(value instanceof Collection) {
        			 Class[] parameterTypes = new Class[((Collection)value).size()];
        			 int i = 0;
        			 for(Iterator anIterator = ((Collection)value).iterator(); anIterator.hasNext();) {
        				 parameterTypes[i++] = Class.forName((String)anIterator.next());
        			 }
        			 ((IsSetNullPolicy)object).setIsSetParameterTypes(parameterTypes);
        		 } else {
        			 // cast to class array
        			 ((IsSetNullPolicy)object).setIsSetParameterTypes((Class[])value);
        		 }
        	 } catch (ClassNotFoundException e) {
        		 throw new RuntimeException(e);
        	 }
         }
     }     

     @Override
     protected ClassDescriptor buildXMLCompositeObjectMappingDescriptor() {
         ClassDescriptor descriptor = super.buildXMLCompositeObjectMappingDescriptor();
         
         // Add Null Policy
         XMLCompositeObjectMapping nullPolicyClassMapping = new XMLCompositeObjectMapping();
         nullPolicyClassMapping.setReferenceClass(AbstractNullPolicy.class);
         nullPolicyClassMapping.setAttributeName("nullPolicy");
         nullPolicyClassMapping.setXPath("toplink:null-policy");

         // Handle translation of (default) Null Policy states.
         ((DatabaseMapping)nullPolicyClassMapping).setAttributeAccessor(new NullPolicyAttributeAccessor());         
         descriptor.addMapping(nullPolicyClassMapping);

         return descriptor;
     }

     /**
      * INTERNAL:
      * If the policy is the default NullPolicy with defaults set - then represent this default policy by null.
      */
     public class NullPolicyAttributeAccessor extends AttributeAccessor {
         
         public NullPolicyAttributeAccessor() {
             super();
         }
         
         @Override   
         public Object getAttributeValueFromObject(Object object) throws DescriptorException {
          	// If the policy is default (NullPolicy(ispfan=true, inrben=false, inrbxnn=false, XMLNullRep=ABSENT_NODE) return null
          	AbstractNullPolicy value = ((XMLNillableMapping)object).getNullPolicy();
          	if(value instanceof NullPolicy) {
              	NullPolicy aPolicy = (NullPolicy)value;
              	if(aPolicy.getIsSetPerformedForAbsentNode() && !aPolicy.isNullRepresentedByEmptyNode() //
              			&& !aPolicy.isNullRepresentedByXsiNil() // 
              			&& aPolicy.getMarshalNullRepresentation().equals(XMLNullRepresentationType.ABSENT_NODE)) {
              		// The default policy is represented by null
              		return null;
              	}
          	}                	
          	return ((XMLNillableMapping)object).getNullPolicy();
         }
         
         @Override
         public void setAttributeValueInObject(Object object, Object value) throws DescriptorException {
         	// If value is a default policy represented by null - return (NullPolicy(ispfan=true, inrben=false, inrbxn=false, XMLNullRep=ABSENT_NODE)
          	if(null == value) {
          		// Create and set a default policy
          		((XMLNillableMapping)object).setNullPolicy(new NullPolicy());                    	
          	} else {
          		// Set the value as policy
              	((XMLNillableMapping)object).setNullPolicy((AbstractNullPolicy)value);
          	}
         }
     }

     public static final String COMPLEX_PLSQL_TYPE = "toplink:plsql-record";
     public static final String SIMPLE_PLSQL_TYPE = "toplink:plsql-type";
     public static final String SIMPLE_JDBC_TYPE = "toplink:jdbc-type";
     public static final String TYPE_NAME = "type-name";
     
     protected ClassDescriptor buildComplexPLSQLTypeWrapperDescriptor() {

         XMLDescriptor descriptor = new XMLDescriptor();
         descriptor.setJavaClass(ComplexPLSQLTypeWrapper.class);
         descriptor.getInheritancePolicy().setParentClass(DatabaseTypeWrapper.class);

         XMLCompositeObjectMapping wrappedDatabaseTypeMapping = new XMLCompositeObjectMapping();
         wrappedDatabaseTypeMapping.setAttributeName("wrappedDatabaseType");
         wrappedDatabaseTypeMapping.setXPath(".");
         wrappedDatabaseTypeMapping.setReferenceClass(PLSQLrecord.class);
         descriptor.addMapping(wrappedDatabaseTypeMapping);

         return descriptor;
     }

     protected ClassDescriptor buildSimplePLSQLTypeWrapperDescriptor() {

         XMLDescriptor descriptor = new XMLDescriptor();
         descriptor.setJavaClass(SimplePLSQLTypeWrapper.class);
         descriptor.getInheritancePolicy().setParentClass(DatabaseTypeWrapper.class);

         XMLDirectMapping wrappedDatabaseTypeMapping = new XMLDirectMapping();
         wrappedDatabaseTypeMapping.setAttributeName("wrappedDatabaseType");
         wrappedDatabaseTypeMapping.setXPath("@" + TYPE_NAME);
         EnumTypeConverter oraclePLSQLTypesEnumTypeConverter = new EnumTypeConverter(
             wrappedDatabaseTypeMapping, OraclePLSQLTypes.class, false);
         wrappedDatabaseTypeMapping.setConverter(oraclePLSQLTypesEnumTypeConverter);
         descriptor.addMapping(wrappedDatabaseTypeMapping);

         return descriptor;
     }

     protected ClassDescriptor buildJDBCTypeWrapperDescriptor() {

         XMLDescriptor descriptor = new XMLDescriptor();
         descriptor.setJavaClass(JDBCTypeWrapper.class);
         descriptor.getInheritancePolicy().setParentClass(DatabaseTypeWrapper.class);

         XMLDirectMapping wrappedDatabaseTypeMapping = new XMLDirectMapping();
         wrappedDatabaseTypeMapping.setAttributeName("wrappedDatabaseType");
         wrappedDatabaseTypeMapping.setXPath("@" + TYPE_NAME);
         EnumTypeConverter jdbcTypesEnumTypeConverter = new EnumTypeConverter(
             wrappedDatabaseTypeMapping, JDBCTypes.class, false);
         wrappedDatabaseTypeMapping.setConverter(jdbcTypesEnumTypeConverter);
         descriptor.addMapping(wrappedDatabaseTypeMapping);

         return descriptor;
     }

     protected ClassDescriptor buildPLSQLrecordDescriptor() {

         XMLDescriptor descriptor = new XMLDescriptor();
         descriptor.setJavaClass(PLSQLrecord.class);

         XMLDirectMapping nameMapping = new XMLDirectMapping();
         nameMapping.setAttributeName("recordName");
         nameMapping.setXPath("toplink:record-name/text()");
         descriptor.addMapping(nameMapping);

         XMLDirectMapping typeNameMapping = new XMLDirectMapping();
         typeNameMapping.setAttributeName("typeName");
         typeNameMapping.setXPath("toplink:type-name/text()");
         descriptor.addMapping(typeNameMapping);

         XMLDirectMapping compatibleTypeMapping = new XMLDirectMapping();
         compatibleTypeMapping.setAttributeName("compatibleType");
         compatibleTypeMapping.setGetMethodName("getCompatibleType");
         compatibleTypeMapping.setSetMethodName("setCompatibleType");
         compatibleTypeMapping.setXPath("toplink:compatible-type/text()");
         descriptor.addMapping(compatibleTypeMapping);

         XMLCompositeCollectionMapping fieldsMapping = new XMLCompositeCollectionMapping();
         fieldsMapping.setAttributeName("fields");
         fieldsMapping.setReferenceClass(PLSQLargument.class);
         fieldsMapping.setXPath("toplink:fields/toplink:field");
         descriptor.addMapping(fieldsMapping);

         return descriptor;
     }

     protected ClassDescriptor buildDatabaseTypeWrapperDescriptor() {

         XMLDescriptor descriptor = new XMLDescriptor();
         descriptor.setJavaClass(DatabaseTypeWrapper.class);
         descriptor.getInheritancePolicy().setClassIndicatorField(
             new XMLField("@xsi:type"));
         descriptor.getInheritancePolicy().addClassIndicator(
             JDBCTypeWrapper.class, SIMPLE_JDBC_TYPE);
         descriptor.getInheritancePolicy().addClassIndicator(
             SimplePLSQLTypeWrapper.class, SIMPLE_PLSQL_TYPE);
         descriptor.getInheritancePolicy().addClassIndicator(
             ComplexPLSQLTypeWrapper.class, COMPLEX_PLSQL_TYPE);

         return descriptor;
     }

     protected ClassDescriptor buildPLSQLargumentDescriptor() {

         XMLDescriptor descriptor = new XMLDescriptor();
         descriptor.setJavaClass(PLSQLargument.class);

         XMLDirectMapping nameMapping = new XMLDirectMapping();
         nameMapping.setAttributeName("name");
         nameMapping.setXPath("toplink:name/text()");
         descriptor.addMapping(nameMapping);

         XMLDirectMapping indexMapping = new XMLDirectMapping();
         indexMapping.setAttributeName("originalIndex");
         indexMapping.setXPath("toplink:index/text()");
         indexMapping.setNullValue(-1);
         descriptor.addMapping(indexMapping);

         XMLDirectMapping directionMapping = new XMLDirectMapping();
         directionMapping.setAttributeName("direction");
         directionMapping.setXPath("toplink:direction/text()");
         ObjectTypeConverter directionConverter = new ObjectTypeConverter();
         directionConverter.addConversionValue("IN", IN);
         directionConverter.addConversionValue("INOUT", INOUT);
         directionConverter.addConversionValue("OUT", OUT);
         directionMapping.setConverter(directionConverter);
         directionMapping.setNullValue(IN);
         descriptor.addMapping(directionMapping);

         XMLDirectMapping lengthMapping = new XMLDirectMapping();
         lengthMapping.setAttributeName("length");
         lengthMapping.setXPath("toplink:length/text()");
         lengthMapping.setNullValue(255);
         descriptor.addMapping(lengthMapping);

         XMLDirectMapping precisionMapping = new XMLDirectMapping();
         precisionMapping.setAttributeName("precision");
         precisionMapping.setXPath("toplink:precision/text()");
         precisionMapping.setNullValue(MIN_VALUE);
         descriptor.addMapping(precisionMapping);

         XMLDirectMapping scaleMapping = new XMLDirectMapping();
         scaleMapping.setAttributeName("scale");
         scaleMapping.setXPath("toplink:scale/text()");
         scaleMapping.setNullValue(MIN_VALUE);
         descriptor.addMapping(scaleMapping);

         XMLDirectMapping cursorOutputMapping = new XMLDirectMapping();
         cursorOutputMapping.setAttributeName("cursorOutput");
         cursorOutputMapping.setXPath("@cursorOutput");
         cursorOutputMapping.setNullValue(Boolean.FALSE);
         descriptor.addMapping(cursorOutputMapping);

         XMLCompositeObjectMapping databaseTypeMapping = new XMLCompositeObjectMapping();
         databaseTypeMapping.setAttributeName("databaseTypeWrapper");
         databaseTypeMapping.setReferenceClass(DatabaseTypeWrapper.class);
         databaseTypeMapping.setXPath(".");
         descriptor.addMapping(databaseTypeMapping);

         return descriptor;
     }

     protected XMLDescriptor buildPLSQLStoredProcedureCallDescriptor() {

         XMLDescriptor descriptor = new XMLDescriptor();
         descriptor.setJavaClass(PLSQLStoredProcedureCall.class);
         descriptor.getInheritancePolicy().setParentClass(Call.class);
         descriptor.setDefaultRootElement("toplink:plsql-stored-procedure-call");

         XMLDirectMapping procedureNameMapping = new XMLDirectMapping();
         procedureNameMapping.setAttributeName("procedureName");
         procedureNameMapping.setXPath("toplink:procedure-name/text()");
         descriptor.addMapping(procedureNameMapping);

         XMLCompositeCollectionMapping argumentsMapping = new XMLCompositeCollectionMapping();
         argumentsMapping.setAttributeName("arguments");
         argumentsMapping.setXPath("toplink:arguments/toplink:argument");
         argumentsMapping.setReferenceClass(PLSQLargument.class);
         descriptor.addMapping(argumentsMapping);

         return descriptor;
     }

     // 5757849 -- add metadata support for ObjectRelationalDatabaseField
     
     @Override
     protected ClassDescriptor buildDatabaseFieldDescriptor() {
         XMLDescriptor descriptor = (XMLDescriptor)super.buildDatabaseFieldDescriptor();
         descriptor.getInheritancePolicy().addClassIndicator(ObjectRelationalDatabaseField.class,
             "toplink:object-relational-field");
         
         return descriptor;
     }

     class ObjectRelationalDatabaseFieldInstantiationPolicy extends InstantiationPolicy {
         
         ObjectRelationalDatabaseFieldInstantiationPolicy() {
         }
         @Override
         public Object buildNewInstance() throws DescriptorException {
           return new ObjectRelationalDatabaseField("");
         }
     }
     protected ClassDescriptor buildObjectRelationalDatabaseFieldDescriptor() {
         XMLDescriptor descriptor = new XMLDescriptor();
         descriptor.setJavaClass(ObjectRelationalDatabaseField.class);
         descriptor.getInheritancePolicy().setParentClass(DatabaseField.class);
         descriptor.setInstantiationPolicy(new ObjectRelationalDatabaseFieldInstantiationPolicy());
         
         XMLCompositeObjectMapping nestedFieldMapping = new XMLCompositeObjectMapping();
         nestedFieldMapping.setAttributeName("nestedTypeField");
         nestedFieldMapping.setXPath("toplink:nested-type-field");
         nestedFieldMapping.setReferenceClass(DatabaseField.class);
         descriptor.addMapping(nestedFieldMapping);
         
         return descriptor;
     }
     
     protected ClassDescriptor buildXMLChoiceFieldToClassAssociationDescriptor() {
         XMLDescriptor descriptor = new XMLDescriptor();
         descriptor.setJavaClass(XMLChoiceFieldToClassAssociation.class);
         
         XMLCompositeObjectMapping fieldMapping = new XMLCompositeObjectMapping();
         fieldMapping.setAttributeName("xmlField");
         fieldMapping.setGetMethodName("getXmlField");
         fieldMapping.setSetMethodName("setXmlField");
         fieldMapping.setXPath("toplink:xml-field");
         fieldMapping.setReferenceClass(XMLField.class);
         descriptor.addMapping(fieldMapping);
         
         XMLDirectMapping classNameMapping = new XMLDirectMapping();
         classNameMapping.setAttributeName("className");
         classNameMapping.setGetMethodName("getClassName");
         classNameMapping.setSetMethodName("setClassName");
         classNameMapping.setXPath("toplink:class-name/text()");
         descriptor.addMapping(classNameMapping);
         
         return descriptor;
     }
     
     protected ClassDescriptor buildXMLChoiceCollectionMappingDescriptor() {
         XMLDescriptor descriptor = new XMLDescriptor();
         descriptor.setJavaClass(XMLChoiceCollectionMapping.class);
         descriptor.getInheritancePolicy().setParentClass(DatabaseMapping.class);
         
         XMLCompositeObjectMapping containerPolicyMapping = new XMLCompositeObjectMapping();
         containerPolicyMapping.setAttributeName("containerPolicy");
         containerPolicyMapping.setReferenceClass(ContainerPolicy.class);
         containerPolicyMapping.setXPath("toplink:container-policy");
         descriptor.addMapping(containerPolicyMapping);
         
         XMLCompositeCollectionMapping fieldToClassNameMapping = new XMLCompositeCollectionMapping();
         fieldToClassNameMapping.setAttributeName("fieldToClassAssociations");
         fieldToClassNameMapping.setGetMethodName("getChoiceFieldToClassAssociations");
         fieldToClassNameMapping.setSetMethodName("setChoiceFieldToClassAssociations");
         fieldToClassNameMapping.setReferenceClass(XMLChoiceFieldToClassAssociation.class);
         fieldToClassNameMapping.useCollectionClass(ArrayList.class);
         fieldToClassNameMapping.setXPath("toplink:field-to-class-association");
         descriptor.addMapping(fieldToClassNameMapping);
         
         return descriptor;
     }
     
     protected ClassDescriptor buildXMLChoiceObjectMappingDescriptor() {
         XMLDescriptor descriptor = new XMLDescriptor();
         descriptor.setJavaClass(XMLChoiceObjectMapping.class);
         descriptor.getInheritancePolicy().setParentClass(DatabaseMapping.class);
         
         XMLCompositeCollectionMapping fieldToClassNameMapping = new XMLCompositeCollectionMapping();
         fieldToClassNameMapping.setAttributeName("fieldToClassAssociations");
         fieldToClassNameMapping.setGetMethodName("getChoiceFieldToClassAssociations");
         fieldToClassNameMapping.setSetMethodName("setChoiceFieldToClassAssociations");
         fieldToClassNameMapping.setReferenceClass(XMLChoiceFieldToClassAssociation.class);
         fieldToClassNameMapping.useCollectionClass(ArrayList.class);
         fieldToClassNameMapping.setXPath("toplink:field-to-class-association");
         descriptor.addMapping(fieldToClassNameMapping);
         
         return descriptor;
     }     
    
}
