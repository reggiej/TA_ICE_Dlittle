// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.sdk;

import oracle.toplink.internal.descriptors.*;
import oracle.toplink.internal.helper.*;
import oracle.toplink.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.mappings.foundation.AbstractCompositeObjectMapping;
import oracle.toplink.queryframework.*;

/**
 * Chunks of data from non-relational data sources can have an
 * embedded component objects. These can be
 * mapped using this mapping. The format of the embedded
 * data is determined by the reference descriptor.
 *
 * @see SDKDescriptor
 * @see SDKFieldValue
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.eis}
 */
public class SDKAggregateObjectMapping extends AbstractCompositeObjectMapping {

    /**
     * Default constructor.
     */
    public SDKAggregateObjectMapping() {
        super();
    }

    /**
    * PUBLIC:
    * Return the name of the field mapped by the mapping.
    */
    public String getFieldName() {
        return this.getField().getName();
    }

    /**
     * PUBLIC:
     * Set the name of the field mapped by the mapping.
     */
    public void setFieldName(String fieldName) {
        this.setField(new DatabaseField(fieldName));
    }

    protected Object buildCompositeRow(Object attributeValue, AbstractSession session, AbstractRecord Record) {
    	AbstractRecord nestedRow = this.getObjectBuilder(attributeValue, session).buildRow(attributeValue, session);
        return this.getReferenceDescriptor(attributeValue, session).buildFieldValueFromNestedRow(nestedRow, session);
    }

    protected Object buildCompositeObject(ObjectBuilder objectBuilder, AbstractRecord nestedRow, ObjectBuildingQuery query, JoinedAttributeManager joinManager) {
        Object aggregateObject = objectBuilder.buildNewInstance();
        objectBuilder.buildAttributesIntoObject(aggregateObject, nestedRow, query, joinManager, false);
        return aggregateObject;
    }
}