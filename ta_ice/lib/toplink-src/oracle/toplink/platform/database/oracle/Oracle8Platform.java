// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.platform.database.oracle;

import java.sql.Array;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.Hashtable;
import java.sql.Connection;
import oracle.toplink.internal.databaseaccess.DatabaseAccessor;
import oracle.toplink.internal.databaseaccess.DatabaseCall;
import oracle.toplink.internal.databaseaccess.FieldTypeDefinition;
import oracle.toplink.internal.databaseaccess.Platform;
import oracle.toplink.internal.databaseaccess.SimpleAppendCallCustomParameter;
import oracle.toplink.internal.helper.ClassConstants;
import oracle.toplink.internal.helper.DatabaseField;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.logging.SessionLog;
import oracle.toplink.queryframework.Call;


/**
 * <p><b>Purpose:</b>
 * Supports certain new Oracle 8 data types, and usage of certain Oracle JDBC specific APIs.
 * <p> Supports Oracle thin JDBC driver LOB >4k binding workaround.
 * <p> Creates BLOB and CLOB type for byte[] and char[] for table creation.
 */
public class Oracle8Platform extends oracle.toplink.platform.database.oracle.OraclePlatform {

    /** Locator is required for Oracle thin driver to write LOB value exceeds the limits */
    protected boolean usesLocatorForLOBWrite = true;

    /** The LOB value limits when the Locator is required for the writing */
    protected int lobValueLimits = 0;

    /**
     * INTERNAL:
     */
    protected Hashtable buildFieldTypes() {
        Hashtable fieldTypeMapping = super.buildFieldTypes();

        fieldTypeMapping.put(Byte[].class, new FieldTypeDefinition("BLOB", false));
        fieldTypeMapping.put(Character[].class, new FieldTypeDefinition("CLOB", false));

        return fieldTypeMapping;
    }

    /**
     * INTERNAL:
     * Allow for conversion from the Oralce type to the Java type.
     */
    public void copyInto(Platform platform) {
        super.copyInto(platform);
        if (!(platform instanceof Oracle8Platform)) {
            return;
        }
        Oracle8Platform oracle8Platform = (Oracle8Platform)platform;
        oracle8Platform.setShouldUseLocatorForLOBWrite(shouldUseLocatorForLOBWrite());
        oracle8Platform.setLobValueLimits(getLobValueLimits());
    }

    /**
     * INTERNAL:
     * Return if the LOB value size is larger than the limit, i.e. 4k.
     */
    protected boolean lobValueExceedsLimit(Object value) {
        if (value == null) {
            return false;
        }
        int limit = getLobValueLimits();
        if (value instanceof byte[]) {//blob 
            return ((byte[])value).length >= limit;
        } else if (value instanceof String) {//clob 
            return ((String)value).length() >= limit;
        } else {
            return false;
        }
    }

    /**
     *  INTERNAL
     *    Used by SQLCall.translate(..)
     *  Typically there is no field translation (and this is default implementation).
     *  However on different platforms (Oracle) there are cases such that the values for
     *  binding and appending may be different (BLOB, CLOB).
     *  In these special cases the method returns a wrapper object
     *  which knows whether it should be bound or appended and knows how to do that.
     */
    public Object getCustomModifyValueForCall(Call call, Object value, DatabaseField field, boolean shouldBind) {
        Class type = field.getType();
        if (ClassConstants.BLOB.equals(type) || ClassConstants.CLOB.equals(type)) {
            if(value == null) {
                return null;
            }
            value = convertToDatabaseType(value);
            if (shouldUseLocatorForLOBWrite()) {
                if (lobValueExceedsLimit(value)) {
                    ((DatabaseCall)call).addContext(field, value);
                    if (ClassConstants.BLOB.equals(type)) {
                        if (shouldBind) {
                            value = new byte[1];
                        } else {
                            value = new SimpleAppendCallCustomParameter("empty_blob()");
                        }
                    } else {
                        if (shouldBind) {
                            value = new String(" ");
                        } else {
                            value = new SimpleAppendCallCustomParameter("empty_clob()");
                        }
                    }
                }
            }
            return value;
        }
        return super.getCustomModifyValueForCall(call, value, field, shouldBind);
    }

    /**
     *  INTERNAL
     *    Used by SQLCall.appendModify(..)
     *  If the field should be passed to customModifyInDatabaseCall, retun true,
     *  otherwise false.
     *  Methods shouldCustomModifyInDatabaseCall and customModifyInDatabaseCall should be
     *  kept in sync: shouldCustomModifyInDatabaseCall should return true if and only if the field
     *  is handled by customModifyInDatabaseCall.
     */
    public boolean shouldUseCustomModifyForCall(DatabaseField field) {
        if (shouldUseLocatorForLOBWrite()) {
            Class type = field.getType();
            if (ClassConstants.BLOB.equals(type) || ClassConstants.CLOB.equals(type)) {
                return true;
            }
        }
        return super.shouldUseCustomModifyForCall(field);
    }

    /**
     * INTERNAL:
     * Write LOB value - only on Oracle8 and up
     */
    public void writeLOB(DatabaseField field, Object value, ResultSet resultSet, AbstractSession session) throws SQLException {
        if (isBlob(field.getType())) {
            oracle.sql.BLOB blob = (oracle.sql.BLOB)resultSet.getObject(field.getName());

            //we could use the jdk 1.4 java.nio package and use channel/buffer for the writing 
            //for the time being, simply use Oracle api.
            blob.putBytes(1, (byte[])value);
            //impose the locallization
            session.log(SessionLog.FINEST, SessionLog.SQL, "write_BLOB", new Long(blob.length()), field.getName());
        } else if (isClob(field.getType())) {
            oracle.sql.CLOB clob = (oracle.sql.CLOB)resultSet.getObject(field.getName());

            //we could use the jdk 1.4 java.nio package and use channel/buffer for the writing
            //for the time being, simply use Oracle api.
            clob.putString(1, (String)value);
            //impose the locallization
            session.log(SessionLog.FINEST, SessionLog.SQL, "write_CLOB", new Long(clob.length()), field.getName());
        } else {
            //do nothing for now, open to BFILE or NCLOB types
        }
    }

    /**
     * INTERNAL:
     * Used in writeLOB method only to identify a BLOB
     */
    protected boolean isBlob(Class type) {
        return ClassConstants.BLOB.equals(type);
    }

    /**
     * INTERNAL:
     * Used in writeLOB method only to identify a CLOB
     */
    protected boolean isClob(Class type) {
        return ClassConstants.CLOB.equals(type);
    }

    /**
     * PUBLIC:
     * Set if the locator is required for the LOB write. The default is true.
     * For Oracle thin driver, the locator is recommended for large size
     * ( >4k for Oracle8, >5.9K for Oracle9) BLOB/CLOB value write.
     */
    public void setShouldUseLocatorForLOBWrite(boolean usesLocatorForLOBWrite) {
        this.usesLocatorForLOBWrite = usesLocatorForLOBWrite;
    }

    /**
     * PUBLIC:
     * Return if the locator is required for the LOB write. The default is true.
     * For Oracle thin driver, the locator is recommended for large size
     * ( >4k for Oracle8, >5.9K for Oracle9) BLOB/CLOB value write.
     */
    public boolean shouldUseLocatorForLOBWrite() {
        return usesLocatorForLOBWrite;
    }

    /**
     * PUBLIC:
     * Return the BLOB/CLOB value limits on thin driver. The default value is 0.
     * If usesLocatorForLOBWrite is true, locator will be used in case the
     * lob's size is larger than lobValueLimit.
     */
    public int getLobValueLimits() {
        return lobValueLimits;
    }

    /**
    * PUBLIC:
    * Set the BLOB/CLOB value limits on thin driver. The default value is 0.
    * If usesLocatorForLOBWrite is true, locator will be used in case the
    * lob's size is larger than lobValueLimit.
    */
    public void setLobValueLimits(int lobValueLimits) {
        this.lobValueLimits = lobValueLimits;
    }

    /**
     * INTERNAL:
     * Platforms that support java.sql.Array may override this method.
     * @return Array
     */
    public Array createArray(String elementDataTypeName, Object[] elements, Connection connection) throws SQLException {
        return new oracle.sql.ARRAY(new oracle.sql.ArrayDescriptor(elementDataTypeName, connection), connection, elements);
    }
    
    /**
     * INTERNAL:
     * Platforms that support java.sql.Struct may override this method.
     * @return Struct
     */
    public Struct createStruct(String structTypeName, Object[] attributes, Connection connection) throws SQLException {
        return new oracle.sql.STRUCT(new oracle.sql.StructDescriptor(structTypeName, connection), connection, attributes);
    }

    /**
     * INTERNAL:
     * Overrides DatabasePlatform method.
     * @return String
     */
    public Object getRefValue(Ref ref,Connection connection) throws SQLException {
        ((oracle.sql.REF)ref).setPhysicalConnectionOf(connection); 
        return ((oracle.sql.REF)ref).getValue();
    }
}
