// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.tools.workbench;

import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.ox.mappings.*;

/**
 * INTERNAL:
 * Define the TopLink OX project and descriptor information to read a OracleAS TopLink 10<i>g</i> (10.0.3) project from an XML file.
 * Note any changes must be reflected in the OPM XML schema.
 */
public class ObjectPersistenceWorkbenchXMLProject extends ObjectPersistenceRuntimeXMLProject_11_1_1 {

    /**
     * INTERNAL:
     * Return a new descriptor project.
     */
    public ObjectPersistenceWorkbenchXMLProject() {
        super();
    }

    protected ClassDescriptor buildAggregateMappingDescriptor() {
        ClassDescriptor descriptor = super.buildAggregateMappingDescriptor();

        XMLDirectMapping referenceClassMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("referenceClass");
        referenceClassMapping.setGetMethodName("getReferenceClassName");
        referenceClassMapping.setSetMethodName("setReferenceClassName");

        return descriptor;
    }

    protected ClassDescriptor buildForeignReferenceMappingDescriptor() {
        ClassDescriptor descriptor = super.buildForeignReferenceMappingDescriptor();

        XMLDirectMapping referenceClassMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("referenceClass");
        referenceClassMapping.setGetMethodName("getReferenceClassName");
        referenceClassMapping.setSetMethodName("setReferenceClassName");
        
        return descriptor;
    }

    protected ClassDescriptor buildInheritancePolicyDescriptor() {
        ClassDescriptor descriptor = super.buildInheritancePolicyDescriptor();

        XMLDirectMapping referenceClassMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("parentClass");
        referenceClassMapping.setGetMethodName("getParentClassName");
        referenceClassMapping.setSetMethodName("setParentClassName");
        
        return descriptor;
    }

    protected ClassDescriptor buildInstantiationPolicyDescriptor() {
        ClassDescriptor descriptor = super.buildInstantiationPolicyDescriptor();

        XMLDirectMapping referenceClassMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("factoryClass");
        referenceClassMapping.setGetMethodName("getFactoryClassName");
        referenceClassMapping.setSetMethodName("setFactoryClassName");

        return descriptor;
    }

    protected ClassDescriptor buildInterfacePolicyDescriptor() {
        ClassDescriptor descriptor = super.buildInterfacePolicyDescriptor();

        XMLCompositeDirectCollectionMapping referenceClassMapping = (XMLCompositeDirectCollectionMapping)descriptor.getMappingForAttributeName("parentInterfaces");
        referenceClassMapping.setGetMethodName("getParentInterfaceNames");
        referenceClassMapping.setSetMethodName("setParentInterfaceNames");

        XMLDirectMapping implementorDescriptorMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("implementorDescriptor");
        implementorDescriptorMapping.setGetMethodName("getImplementorDescriptorClassName");
        implementorDescriptorMapping.setSetMethodName("setImplementorDescriptorClassName");
                
        return descriptor;
    }

    protected ClassDescriptor buildInterfaceContainerPolicyDescriptor() {
        ClassDescriptor descriptor = super.buildInterfaceContainerPolicyDescriptor();

        XMLDirectMapping referenceClassMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("containerClass");
        referenceClassMapping.setGetMethodName("getContainerClassName");
        referenceClassMapping.setSetMethodName("setContainerClassName");

        return descriptor;
    }

    protected ClassDescriptor buildSortedCollectionContainerPolicyDescriptor() {
        ClassDescriptor descriptor = super.buildSortedCollectionContainerPolicyDescriptor();

        XMLDirectMapping referenceClassMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("comparatorClass");
        referenceClassMapping.setGetMethodName("getComparatorClassName");
        referenceClassMapping.setSetMethodName("setComparatorClassName");

        return descriptor;
    }
    
    
    protected ClassDescriptor buildMethodBaseQueryRedirectorDescriptor() {
        ClassDescriptor descriptor = super.buildMethodBaseQueryRedirectorDescriptor();

        XMLDirectMapping referenceClassMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("methodClass");
        referenceClassMapping.setGetMethodName("getMethodClassName");
        referenceClassMapping.setSetMethodName("setMethodClassName");

        return descriptor;
    }

    protected ClassDescriptor buildObjectLevelReadQueryDescriptor() {
        ClassDescriptor descriptor = super.buildObjectLevelReadQueryDescriptor();

        XMLDirectMapping referenceClassMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("referenceClass");
        referenceClassMapping.setGetMethodName("getReferenceClassName");
        referenceClassMapping.setSetMethodName("setReferenceClassName");
        
        return descriptor;
    }

    protected ClassDescriptor buildQueryArgumentDescriptor() {
        ClassDescriptor descriptor = super.buildQueryArgumentDescriptor();

        XMLDirectMapping referenceClassMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("type");     
        referenceClassMapping.setGetMethodName("getTypeName");
        referenceClassMapping.setSetMethodName("setTypeName");

        return descriptor;
    }

    protected ClassDescriptor buildRelationshipQueryKeyDescriptor() {
        ClassDescriptor descriptor = super.buildRelationshipQueryKeyDescriptor();

        XMLDirectMapping referenceClassMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("referenceClass");
        referenceClassMapping.setGetMethodName("getReferenceClassName");
        referenceClassMapping.setSetMethodName("setReferenceClassName");

        return descriptor;
    }

    protected ClassDescriptor buildReturningFieldInfoDescriptor() {
        ClassDescriptor descriptor = super.buildReturningFieldInfoDescriptor();

        XMLDirectMapping referenceClassMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("referenceClass");
        referenceClassMapping.setGetMethodName("getReferenceClassName");
        referenceClassMapping.setSetMethodName("setReferenceClassName");

        return descriptor;
    }

    protected ClassDescriptor buildClassDescriptorDescriptor() {
        ClassDescriptor descriptor = super.buildClassDescriptorDescriptor();

        XMLDirectMapping referenceClassMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("javaClass");
        referenceClassMapping.setGetMethodName("getJavaClassName");
        referenceClassMapping.setSetMethodName("setJavaClassName");
        
        XMLDirectMapping amendmentClassMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("amendmentClass");
        amendmentClassMapping.setGetMethodName("getAmendmentClassName");
        amendmentClassMapping.setSetMethodName("setAmendmentClassName");
        
        return descriptor;
    }

    protected ClassDescriptor buildTypedAssociationDescriptor() {
        ClassDescriptor descriptor = super.buildTypedAssociationDescriptor();

        XMLDirectMapping keyMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("key");
        keyMapping.setAttributeClassification(null);
        keyMapping.setGetMethodName("getKey");
        keyMapping.setSetMethodName("setKey");

        return descriptor;
    }

    protected ClassDescriptor buildTypeConversionConverterDescriptor() {
        ClassDescriptor descriptor = super.buildTypeConversionConverterDescriptor();

        XMLDirectMapping objectClassMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("objectClass");
        objectClassMapping.setGetMethodName("getObjectClassName");
        objectClassMapping.setSetMethodName("setObjectClassName");
        
        XMLDirectMapping dataClassNameMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("dataClass");
        dataClassNameMapping.setGetMethodName("getDataClassName");
        dataClassNameMapping.setSetMethodName("setDataClassName");

        return descriptor;
    }    

    protected ClassDescriptor buildAbstractDirectMappingDescriptor() {
        ClassDescriptor descriptor = super.buildAbstractDirectMappingDescriptor();

        XMLDirectMapping attributeClassificationNameMapping = 
            (XMLDirectMapping)descriptor.getMappingForAttributeName("attributeClassification");
        attributeClassificationNameMapping.setGetMethodName("getAttributeClassificationName");
        attributeClassificationNameMapping.setSetMethodName("setAttributeClassificationName");

        return descriptor;
    }

    protected ClassDescriptor buildAbstractTransformationMappingDescriptor() {
        ClassDescriptor descriptor = super.buildAbstractTransformationMappingDescriptor();

        XMLDirectMapping objectClassMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("attributeTransformerClass");
        objectClassMapping.setGetMethodName("getAttributeTransformerClassName");
        objectClassMapping.setSetMethodName("setAttributeTransformerClassName");

        return descriptor;
    }
    
    protected ClassDescriptor buildTransformerBasedFieldTransformationDescriptor() {
        ClassDescriptor descriptor = super.buildTransformerBasedFieldTransformationDescriptor();

        XMLDirectMapping objectClassMapping = (XMLDirectMapping)descriptor.getMappingForAttributeName("transformerClass");
        objectClassMapping.setGetMethodName("getTransformerClassName");
        objectClassMapping.setSetMethodName("setTransformerClassName");
        
        return descriptor;
    }
}
