// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.sequencing;

import java.io.StringWriter;
import oracle.toplink.queryframework.*;

/**
 * <p>
 * <b>Purpose</b>: Defines sequencing through using a singleton sequence table.
 * <p>
 * <b>Description</b>
 * This is similar to the TableSequence but the sequence table only stores a single
 * row defining a single sequence.
 */
public class UnaryTableSequence extends QuerySequence {

    /** Hold the name of the only column in multiple single-column tables */
    protected String counterFieldName = "SEQUENCE";
    protected String updateString1;
    protected String updateString2;
    protected String selectString1;
    protected String selectString2;
    protected int updateStringBufferSize;
    protected int selectStringBufferSize;
    protected String qualifier = "";

    public UnaryTableSequence() {
        super(false, true);
    }
    
    /**
     * Create a new sequence with the name.
     */
    public UnaryTableSequence(String name) {
        super(name, false, true);
    }
    
    /**
     * Create a new sequence with the name and sequence pre-allocation size.
     */
    public UnaryTableSequence(String name, int size) {
        super(name, size, false, true);
    }

    public UnaryTableSequence(String name, String counterFieldName) {
        this(name);
        setCounterFieldName(counterFieldName);
    }

    public UnaryTableSequence(String name, int size, String counterFieldName) {
        this(name, size);
        setCounterFieldName(counterFieldName);
    }

    public boolean isUnaryTable() {
        return true;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof UnaryTableSequence) {
            UnaryTableSequence other = (UnaryTableSequence)obj;
            if (equalNameAndSize(this, other)) {
                return getCounterFieldName().equals(other.getCounterFieldName());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void setCounterFieldName(String name) {
        this.counterFieldName = name;
    }

    public String getCounterFieldName() {
        return counterFieldName;
    }

    /**
    * INTERNAL:
    */
    public void onConnect() {
        qualifier = getDatasourcePlatform().getTableQualifier();
        super.onConnect();
        initialize();
    }

    /**
    * INTERNAL:
    */
    public void onDisconnect() {
        clear();
        super.onDisconnect();
        qualifier = "";
    }

    protected ValueReadQuery buildSelectQuery(String seqName, Integer size) {
        ValueReadQuery selectQuery = new ValueReadQuery();
        selectQuery.dontBindAllParameters();
        StringWriter writer = new StringWriter(selectStringBufferSize + seqName.length());
        writer.write(selectString1);
        writer.write(seqName);
        selectQuery.setSQLString(writer.toString());
        return selectQuery;
    }

    protected DataModifyQuery buildUpdateQuery(String seqName, Number size) {
        DataModifyQuery updateQuery = new DataModifyQuery();
        updateQuery.dontBindAllParameters();
        String sizeString = size.toString();
        StringWriter writer = new StringWriter(updateStringBufferSize + seqName.length() + sizeString.length());
        writer.write(updateString1);
        writer.write(seqName);
        writer.write(updateString2);
        writer.write(sizeString);
        updateQuery.setSQLString(writer.toString());
        return updateQuery;
    }

    protected void initialize() {
        if (getSelectQuery() == null) {
            buildSelectString1();
            selectStringBufferSize = selectString1.length();
        }
        if ((getUpdateQuery() == null) && !shouldSkipUpdate()) {
            buildUpdateString1();
            buildUpdateString2();
            updateStringBufferSize = updateString1.length() + updateString2.length();
        }
    }

    protected void buildUpdateString1() {
        updateString1 = "UPDATE ";
        if (qualifier != "") {
            updateString1 = updateString1 + qualifier + '.';
        }
    }

    protected void buildUpdateString2() {
        StringWriter writer = new StringWriter();
        writer.write(" SET ");
        writer.write(getCounterFieldName());
        writer.write(" = ");
        writer.write(getCounterFieldName());
        writer.write(" + ");
        updateString2 = writer.toString();
    }

    protected void buildSelectString1() {
        selectString1 = "SELECT * FROM ";
        if (qualifier != "") {
            selectString1 = selectString1 + qualifier + '.';
        }
    }

    protected void clear() {
        updateString1 = null;
        updateString2 = null;
        selectString1 = null;
        selectString2 = null;
        updateStringBufferSize = 0;
        selectStringBufferSize = 0;
    }
}
