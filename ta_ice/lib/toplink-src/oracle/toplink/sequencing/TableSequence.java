// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.sequencing;

import java.io.StringWriter;
import oracle.toplink.queryframework.*;
import oracle.toplink.internal.helper.DatabaseTable;

/**
 * <p>
 * <b>Purpose</b>: Defines sequencing through using a SEQUENCE table.
 * <p>
 * <b>Description</b>
 * This is the default sequencing mechanism.
 * A table defaulting to SEQUENCE is used to generate unique ids.
 * The table has a name field (SEQ_NAME) storing each sequences name,
 * and a counter (SEQ_COUNT) storing the last sequence id generated.
 * There will be a row in the table for each sequence object.
 */
public class TableSequence extends QuerySequence {
    /** Default sequence table name */
    protected static final String defaultTableName = "SEQUENCE";

    /** Hold the database table */
    protected DatabaseTable table;

    /** Hold the name of the column in the sequence table which specifies the sequence numeric value */
    protected String counterFieldName = "SEQ_COUNT";

    /** Hold the name of the column in the sequence table which specifies the sequence name */
    protected String nameFieldName = "SEQ_NAME";
    
    /** Hold the name of the table qualifier */
    protected String qualifier = "";

    public TableSequence() {
        super(false, true);
        setTableName(defaultTableName);
    }
    
    /**
     * Create a new sequence with the name.
     */
    public TableSequence(String name) {
        super(name, false, true);
        setTableName(defaultTableName);
    }
    
    /**
     * Create a new sequence with the name and sequence pre-allocation size.
     */
    public TableSequence(String name, int size) {
        super(name, size, false, true);
        setTableName(defaultTableName);
    }
    
    public TableSequence(String name, int size, int initialValue) {
        super(name, size, initialValue, false, true);
        setTableName(defaultTableName);
    }
    
    /**
     * Create a new sequence with the name, and the sequence table name.
     */
    public TableSequence(String name, String tableName) {
        this(name);
        setTableName(tableName);
    }
    
    /**
     * Create a new sequence with the name, and the sequence table information.
     */
    public TableSequence(String name, String tableName, String nameFieldName, String counterFieldName) {
        this(name);
        setTableName(tableName);
        setNameFieldName(nameFieldName);
        setCounterFieldName(counterFieldName);
    }

    public TableSequence(String name, int size, String tableName) {
        this(name, size);
        setTableName(tableName);
    }

    public TableSequence(String name, int size, String tableName, String nameFieldName, String counterFieldName) {
        this(name, size);
        setTableName(tableName);
        setNameFieldName(nameFieldName);
        setCounterFieldName(counterFieldName);
    }

    public boolean isTable() {
        return true;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof TableSequence) {
            TableSequence other = (TableSequence)obj;
            if (equalNameAndSize(this, other)) {
                return getTableName().equals(other.getTableName()) && getCounterFieldName().equals(other.getCounterFieldName()) && getNameFieldName().equals(other.getNameFieldName());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String getCounterFieldName() {
        return counterFieldName;
    }

    public void setCounterFieldName(String name) {
        counterFieldName = name;
    }

    public String getNameFieldName() {
        return nameFieldName;
    }

    public void setNameFieldName(String name) {
        nameFieldName = name;
    }

    public DatabaseTable getTable() {
        return table;
    }
    
    public String getTableName() {
        return getTable().getQualifiedName();
    }

    public String getQualifiedTableName() {
        if (qualifier.equals("")) {
            return getTableName();
        } else {
            return qualifier + "." + getTableName();
        }
    }

    public void setTable(DatabaseTable table) {
        this.table = table;
    }
    
    public void setTableName(String name) {
        table = new DatabaseTable(name);
    }

    protected ValueReadQuery buildSelectQuery() {
        ValueReadQuery query = new ValueReadQuery();
        query.addArgument(getNameFieldName());
        StringWriter writer = new StringWriter();
        writer.write("SELECT " + getCounterFieldName());
        writer.write(" FROM " + getQualifiedTableName());
        writer.write(" WHERE " + getNameFieldName());
        writer.write(" = #" + getNameFieldName());
        query.setSQLString(writer.toString());

        return query;
    }

    protected DataModifyQuery buildUpdateQuery() {
        DataModifyQuery query = new DataModifyQuery();
        query.addArgument(getNameFieldName());
        query.addArgument("PREALLOC_SIZE");
        StringWriter writer = new StringWriter();
        writer.write("UPDATE " + getQualifiedTableName());
        writer.write(" SET " + getCounterFieldName());
        writer.write(" = " + getCounterFieldName());
        writer.write(" + #PREALLOC_SIZE");
        writer.write(" WHERE " + getNameFieldName() + " = #" + getNameFieldName());
        query.setSQLString(writer.toString());

        return query;
    }

    /**
    * INTERNAL:
    */
    public void onConnect() {
        qualifier = getDatasourcePlatform().getTableQualifier();
        super.onConnect();
    }

    /**
    * INTERNAL:
    */
    public void onDisconnect() {
        super.onDisconnect();
        qualifier = "";
    }
}
