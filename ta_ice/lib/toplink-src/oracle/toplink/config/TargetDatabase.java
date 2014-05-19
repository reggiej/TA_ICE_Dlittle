// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.config;

/**
 * Target database persistence property values.
 * 
 * <p>JPA persistence property Usage:
 * 
 * <p><code>properties.add(TopLinkProperties.TargetDatabase, TargetDatabase.Oracle);</code>
 * 
 * <p>Property values are case-insensitive
 */
public class TargetDatabase {
    public static final String  Auto = "Auto";
    public static final String  Oracle = "Oracle";
    public static final String  Oracle11 = "Oracle11";
    public static final String  Oracle10 = "Oracle10g";
    public static final String  Oracle9 = "Oracle9i";
    public static final String  Oracle8 = "Oracle8i";
    public static final String  Attunity = "Attunity";
    public static final String  Cloudscape = "Cloudscape";
    public static final String  Database = "Database";
    public static final String  DB2 = "DB2";
    public static final String  DB2Mainframe = "DB2Mainframe";
    public static final String  DBase = "DBase";
    public static final String  Derby = "Derby";
    public static final String  HSQL = "HSQL";
    public static final String  Informix = "Informix";
    public static final String  JavaDB = "JavaDB";
    public static final String  MySQL4 = "MySQL4";
    public static final String  PointBase = "PointBase";
    public static final String  PostgreSQL = "PostgreSQL";
    public static final String  SQLAnyWhere = "SQLAnyWhere";
    public static final String  SQLServer = "SQLServer";
    public static final String  Sybase = "Sybase";
    public static final String  TimesTen = "TimesTen";
 
    public static final String DEFAULT = Auto;
}
