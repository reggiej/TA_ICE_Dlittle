// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import static oracle.toplink.annotations.CacheType.SOFT_WEAK;
import static oracle.toplink.annotations.CacheCoordinationType.SEND_OBJECT_CHANGES;

/** 
 * The Cache annotation is used to set an 
 * oracle.toplink.descriptors.invalidation.CacheInvalidationPolicy which sets 
 * objects in TopLink's identity maps to be invalid following given rules.  
 * By default in TopLink, objects do not expire in the cache. Several different 
 * policies are available to allow objects to expire.
 * 
 * @see oracle.toplink.annotations.CacheType
 * 
 * A Cache anotation may be defined on an Entity or MappedSuperclass. In the 
 * case of inheritance, a Cache annotation should only be defined on the root 
 * of the inheritance hierarchy.
 * 
 * @author Guy Pelletier
 * @since Oracle TopLink 11.1.1.0.0 
 */ 
@Target({TYPE})
@Retention(RUNTIME)
public @interface Cache {
    /**
     * (Optional) The type of cache to use.
     */ 
    CacheType type() default SOFT_WEAK;
    
    /**
     * (Optional) The size of cache to use.
     */ 
    int size() default 100;

    /**
     * (Optional) Cached instances in the shared cache or a client 
     * isolated cache.
     */ 
    boolean isolated() default false;

    /**
     * (Optional) Expire cached instance after a fix period of time (ms). 
     * Queries executed against the cache after this will be forced back 
     * to the database for a refreshed copy
     */ 
    int expiry() default -1; // minus one is no expiry.

    /**
     * (Optional) Expire cached instance a specific time of day. Queries 
     * executed against the cache after this will be forced back to the 
     * database for a refreshed copy
     */ 
    TimeOfDay expiryTimeOfDay() default @TimeOfDay(specified=false);

    /**
     * (Optional) Force all queries that go to the database to always 
     * refresh the cache.
     */ 
    boolean alwaysRefresh() default false;

    /**
     * (Optional) For all queries that go to the database, refresh the cache 
     * only if the data received from the database by a query is newer than 
     * the data in the cache (as determined by the optimistic locking field)
     */ 
    boolean refreshOnlyIfNewer() default false;

    /**
     * (Optional) Setting to true will force all queries to bypass the 
     * cache for hits but still resolve against the cache for identity. 
     * This forces all queries to hit the database.
     */ 
    boolean disableHits() default false;

    /**
     * (Optional) The cache coordination mode.
     */ 
    CacheCoordinationType coordinationType() default SEND_OBJECT_CHANGES;
}
