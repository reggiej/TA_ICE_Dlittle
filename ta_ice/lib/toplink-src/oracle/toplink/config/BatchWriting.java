// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.config;

/**
 * Specify the use of batch writing to optimize transactions with multiple writes,
 * by default batch writing is not used.
 * 
 * <p>JPA persistence property Usage:
 * 
 * <p><code>properties.add(TopLinkProperties.BATCH_WRITING, BatchWriting.JDBC);</code>
 * 
 * <p>Property values are case-insensitive
 */
public class BatchWriting {
    public static final String  None = "None";
    public static final String  JDBC = "JDBC";
    public static final String  Buffered = "Buffered";
    public static final String  OracleJDBC = "Oracle-JDBC";
 
    public static final String DEFAULT = None;
}
