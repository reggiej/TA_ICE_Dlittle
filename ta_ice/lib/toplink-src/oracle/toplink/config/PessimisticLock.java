// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.config;

/**
 * PessimisticLock hint values.
 * 
 * The class contains all the valid values for TopLinkQueryHints.PESSIMISTIC_LOCK query hint.
 * 
 * <p>JPA Query Hint Usage:
 * 
 * <p><code>query.setHint(TopLinkQueryHints.PESSIMISTIC_LOCK, PessimisticLock.Lock);</code>
 * <p>or 
 * <p><code>@QueryHint(name=TopLinkQueryHints.PESSIMISTIC_LOCK, value=PessimisticLock.Lock)</code>
 * 
 * <p>Hint values are case-insensitive.
 * "" could be used instead of default value PessimisticLock.DEFAULT.
 * 
 * @see TopLinkQueryHints
 */
public class PessimisticLock {
    public static final String  NoLock = "NoLock";
    public static final String  Lock = "Lock";
    public static final String  LockNoWait = "LockNoWait";
 
    public static final String DEFAULT = NoLock;
}
