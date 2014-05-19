// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.descriptors.invalidation;

import oracle.toplink.internal.identitymaps.CacheKey;
import oracle.toplink.descriptors.invalidation.CacheInvalidationPolicy;

/**
 * PUBLIC:
 * A cache invalidation policy in which no objects will expire.  The only way for objects
 * to become invalid in the cache is for them to be explicitly set to invalid through
 * method calls on the IdentityMapAccessor.  This is the default cache invalidation policy.
 * @see CacheInvalidationPolicy
 * @see oracle.toplink.sessions.IdentityMapAccessor
 */
public class NoExpiryCacheInvalidationPolicy extends CacheInvalidationPolicy {

    /**
     * INTERNAL:
     * Since this policy implements no expiry, this will always return NO_EXPIRY
     */
    public long getExpiryTimeInMillis(CacheKey key) {
        return NO_EXPIRY;
    }

    /**
     * INTERNAL:
     * Return the remaining life of this object
     * Override the default implementation.
     */
    public long getRemainingValidTime(CacheKey key) {
        return NO_EXPIRY;
    }

    /**
     * INTERNAL:
     * This will return true if the object is set to be invalid, false otherwise.
     */
    public boolean isInvalidated(CacheKey key) {
        return key.getInvalidationState() == CacheKey.CACHE_KEY_INVALID;
    }
    
    /**
     * INTERNAL:
     * This will return true if the object is set to be invalid, false otherwise.
     */
    public boolean isInvalidated(CacheKey key, long currentTimeMillis) {
        return key.getInvalidationState() == CacheKey.CACHE_KEY_INVALID;
    }
}
