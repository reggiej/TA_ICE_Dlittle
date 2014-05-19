// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.sdk;

import oracle.toplink.mappings.*;
import oracle.toplink.internal.sessions.*;

/**
 * Interface used by the <code>SDKCollectionMappingHelper</code> to interact
 * with the assorted SDK collection mappings.
 *
 * @see SDKCollectionMappingHelper
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.eis}
 */
public interface SDKCollectionMapping extends ContainerMapping {

    /**
     * Build and return a newly-added element based on the change set.
     */
    Object buildAddedElementFromChangeSet(Object changeSet, MergeManager mergeManager);

    /**
     * Build and return a change set for the specified element.
     */
    Object buildChangeSet(Object element, ObjectChangeSet owner, AbstractSession session);

    /**
     * Build and return a new element based on the specified element.
     */
    Object buildElementFromElement(Object element, MergeManager mergeManager);

    /**
     * Build and return a recently-removed element based on the change set.
     */
    Object buildRemovedElementFromChangeSet(Object changeSet, MergeManager mergeManager);

    /**
     * Compare the non-null elements and return true if they are alike.
     */
    boolean compareElements(Object element1, Object element2, AbstractSession session);

    /**
     * Compare the non-null elements and return true if they are alike.
     * This is used to build a change record.
     */
    boolean compareElementsForChange(Object element1, Object element2, AbstractSession session);

    /**
     * Return the mapping's attribute name.
     */
    String getAttributeName();

    /**
     * Return the attribute value from the specified object,
     * unwrapping the value holder if necessary.
     * If the value is null, build a new container.
     */
    Object getRealCollectionAttributeValueFromObject(Object object, AbstractSession session);

    /**
     * Return whether the element's user-defined Map key has changed
     * since it was cloned from the original version.
     */
    boolean mapKeyHasChanged(Object element, AbstractSession session);

    /**
     * Set the attribute value for the specified object,
     * wrapping it in a value holder if necessary.
     */
    void setRealAttributeValueInObject(Object object, Object attributeValue);
}