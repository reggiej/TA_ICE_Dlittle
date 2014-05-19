// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.config;

/**
 * Cache type persistence property values.
 *
 * <p>JPA persistence property Usage:
 * 
 * <p>for all entities append DEFAULT suffix to CACHE_TYPE_ prefix:
 * <p><code>properties.add(TopLinkProperties.CACHE_TYPE_DEFAULT, CacheType.Weak);</code>
 * 
 * <p>for a single entity append either entity name or a full class name to CACHE_TYPE_ prefix:
 * <p><code>properties.add(TopLinkProperties.CACHE_TYPE_ + "Employee", CacheType.Weak);</code>
 * <p><code>properties.add(TopLinkProperties.CACHE_TYPE_ + "my.test.Employee", CacheType.Weak);</code>
 * 
 * <p>Values are case-insensitive.
 * "" could be used instead of default value CacheType.DEFAULT.
 * 
 * @see TopLinkProperties
 */
public class CacheType {
    public static final String  Weak = "Weak";
    public static final String  Soft = "Soft";
    public static final String  SoftWeak = "SoftWeak";
    public static final String  HardWeak = "HardWeak";
    public static final String  Full = "Full";
    public static final String  NONE = "NONE";
 
    public static final String DEFAULT = SoftWeak;
}
