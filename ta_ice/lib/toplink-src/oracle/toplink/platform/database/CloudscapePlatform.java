// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.platform.database;

import java.util.*;

import oracle.toplink.internal.databaseaccess.FieldTypeDefinition;

/**
 *    <p><b>Purpose</b>: Provides CloudScape DBMS specific behaviour.
 *
 * @since TOPLink/Java 3.0
 */
public class CloudscapePlatform extends oracle.toplink.platform.database.DatabasePlatform {

    /**
     * seems compatible with informix
     */
    protected Hashtable buildFieldTypes() {
        Hashtable fieldTypeMapping;

        fieldTypeMapping = new Hashtable();
        fieldTypeMapping.put(Boolean.class, new FieldTypeDefinition("SMALLINT default 0", false));

        fieldTypeMapping.put(Integer.class, new FieldTypeDefinition("INTEGER", false));
        fieldTypeMapping.put(Long.class, new FieldTypeDefinition("NUMERIC", 19));
        fieldTypeMapping.put(Float.class, new FieldTypeDefinition("FLOAT(16)", false));
        fieldTypeMapping.put(Double.class, new FieldTypeDefinition("FLOAT(32)", false));
        fieldTypeMapping.put(Short.class, new FieldTypeDefinition("SMALLINT", false));
        fieldTypeMapping.put(Byte.class, new FieldTypeDefinition("SMALLINT", false));
        fieldTypeMapping.put(java.math.BigInteger.class, new FieldTypeDefinition("DECIMAL", 32));
        fieldTypeMapping.put(java.math.BigDecimal.class, new FieldTypeDefinition("DECIMAL", 32).setLimits(32, -19, 19));
        fieldTypeMapping.put(Number.class, new FieldTypeDefinition("DECIMAL", 32).setLimits(32, -19, 19));

        fieldTypeMapping.put(String.class, new FieldTypeDefinition("VARCHAR", 255));
        fieldTypeMapping.put(Character.class, new FieldTypeDefinition("CHAR", 1));
        fieldTypeMapping.put(Byte[].class, new FieldTypeDefinition("BYTE", false));
        fieldTypeMapping.put(Character[].class, new FieldTypeDefinition("TEXT", false));
        fieldTypeMapping.put(byte[].class, new FieldTypeDefinition("BYTE", false));
        fieldTypeMapping.put(char[].class, new FieldTypeDefinition("TEXT", false));
        fieldTypeMapping.put(java.sql.Blob.class, new FieldTypeDefinition("BYTE", false));
        fieldTypeMapping.put(java.sql.Clob.class, new FieldTypeDefinition("TEXT", false));        

        fieldTypeMapping.put(java.sql.Date.class, new FieldTypeDefinition("DATE", false));
        fieldTypeMapping.put(java.sql.Time.class, new FieldTypeDefinition("DATETIME HOUR TO SECOND", false));
        fieldTypeMapping.put(java.sql.Timestamp.class, new FieldTypeDefinition("DATETIME YEAR TO FRACTION(5)", false));

        return fieldTypeMapping;
    }

    /**
     * Answers whether platform is CloudScape
     */
    public boolean isCloudscape() {
        return true;
    }

    /**
     * JDBC defines an outer join syntax which many drivers do not support. So we normally avoid it.
     */
    public boolean shouldUseJDBCOuterJoinSyntax() {
        return false;// not sure if cloudscape likes this or not. Still investigating.
    }
}