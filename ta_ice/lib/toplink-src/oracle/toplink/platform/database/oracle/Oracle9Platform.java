// Copyright (c) 1998, 2008, Oracle. All rights reserved.  
package oracle.toplink.platform.database.oracle;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import java.util.*;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleTypes;

import oracle.toplink.expressions.ExpressionOperator;
import oracle.toplink.internal.expressions.SpatialExpressionOperators;
import oracle.sql.OPAQUE;
import oracle.sql.TIMESTAMP;
import oracle.sql.TIMESTAMPLTZ;
import oracle.sql.TIMESTAMPTZ;
import oracle.toplink.exceptions.ConversionException;
import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.exceptions.QueryException;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.queryframework.Call;
import oracle.toplink.queryframework.ValueReadQuery;
import oracle.toplink.sessions.DatabaseLogin;
import oracle.toplink.internal.helper.*;
import oracle.toplink.internal.platform.database.oracle.TIMESTAMPHelper;
import oracle.toplink.internal.platform.database.oracle.TIMESTAMPLTZWrapper;
import oracle.toplink.internal.platform.database.oracle.TIMESTAMPTZWrapper;
import oracle.toplink.internal.platform.database.oracle.TIMESTAMPTypes;
import oracle.toplink.internal.platform.database.oracle.TopLinkXMLType;
import oracle.toplink.internal.platform.database.oracle.XMLTypeFactory;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedClassForName;
import oracle.toplink.internal.security.PrivilegedGetConstructorFor;
import oracle.toplink.internal.security.PrivilegedInvokeConstructor;
import oracle.toplink.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose:</b>
 * Supports usage of certain Oracle JDBC specific APIs.
 * <p> Supports binding NCHAR, NVARCHAR, NCLOB types as required by Oracle JDBC drivers.
 * <p> Supports Oracle JDBC TIMESTAMP, TIMESTAMPTZ, TIMESTAMPLTZ types.
 */
public class Oracle9Platform extends oracle.toplink.platform.database.oracle.Oracle8Platform {

    public static final Class NCHAR = NCharacter.class;
    public static final Class NSTRING = NString.class;
    public static final Class NCLOB = NClob.class;
    public static final Class XMLTYPE = TopLinkXMLType.class;
    
    private XMLTypeFactory xmlTypeFactory;
    private Boolean printCalendarIntoTimestampTZ = null;
    private Boolean correctTimeInTimestampTZ = null;
    
    public Oracle9Platform(){
        super();
    }
    
    /**
     * INTERNAL:
     * This class used for binding of NCHAR, NSTRING, NCLOB types.
     */
    protected static class NTypeBindCallCustomParameter extends BindCallCustomParameter {
        public NTypeBindCallCustomParameter(Object obj) {
            super(obj);
        }
        //Bug5200836, use unwrapped connection if it is NType parameter.
        public boolean shouldUseUnwrappedConnection() {
            return true;
        }

        /**
        * INTERNAL:
        * Binds the custom parameter (obj) into  the passed PreparedStatement
        * for the passed DatabaseCall.
        * Note that parameter numeration for PreparedStatement starts with 1,
        * therefore statement.set...(index + 1, ...) should be used.
        * DatabaseCall will carry this object as its parameter: call.getParameters().elementAt(index).
        * The reason for passing DatabaseCall and DatabasePlatform into this method
        * is that this method may set obj as a new value of index parameter:
        *   call.getParameters().setElementAt(obj, index);
        * and call again the method which has called it:
        *   platform.setParameterValueInDatabaseCall(call, statement, index);
        * so obj will be bound.
        *
        * Called only by DatabasePlatform.setParameterValueInDatabaseCall method
        */
        public void set(DatabasePlatform platform, PreparedStatement statement, int index, AbstractSession session) throws SQLException {
            // Binding starts with a 1 not 0. Make sure that index > 0
            ((oracle.jdbc.OraclePreparedStatement)statement).setFormOfUse(index, oracle.jdbc.OraclePreparedStatement.FORM_NCHAR);

            super.set(platform, statement, index, session);
        }
    }

    /**
     * INTERNAL:
     * Get a timestamp value from a result set.
     * Overrides the default behavior to specifically return a timestamp.  Added
     * to overcome an issue with the oracle 9.0.1.4 JDBC driver.
     */
    public Object getObjectFromResultSet(ResultSet resultSet, int columnNumber, int type, AbstractSession session) throws java.sql.SQLException {
        //Bug#3381652 10G Drivers return sql.Date instead of timestamp on DATE field
        if ((type == Types.TIMESTAMP) || (type == Types.DATE)) {
            return resultSet.getTimestamp(columnNumber);
        } else if (type == oracle.jdbc.OracleTypes.TIMESTAMPTZ) {
            TIMESTAMPTZ tsTZ = (TIMESTAMPTZ)resultSet.getObject(columnNumber);

            //Need to call timestampValue once here with the connection to avoid null point 
            //exception later when timestampValue is called in converObject()
            if ((tsTZ != null) && (tsTZ.getLength() != 0)) {
                tsTZ.timestampValue(getConnection(session, resultSet.getStatement().getConnection()));
                //Bug#4364359  Add a wrapper to overcome TIMESTAMPTZ not serializable as of jdbc 9.2.0.5 and 10.1.0.2.  
                //It has been fixed in the next version for both streams
                return new TIMESTAMPTZWrapper(tsTZ);
            }
            return null;
        } else if (type == oracle.jdbc.OracleTypes.TIMESTAMPLTZ) {
            //TIMESTAMPLTZ needs to be converted to Timestamp here because it requires the connection.
            //However the java object is not know here.  The solution is to store Timestamp and the 
            //session timezone in a wrapper class, which will be used later in converObject().
            TIMESTAMPLTZ tsLTZ = (TIMESTAMPLTZ)resultSet.getObject(columnNumber);
            if ((tsLTZ != null) && (tsLTZ.getLength() != 0)) {
                Timestamp ts = TIMESTAMPLTZ.toTimestamp(getConnection(session, resultSet.getStatement().getConnection()), tsLTZ.toBytes());

                //Bug#4364359  Add a separate wrapper for TIMESTAMPLTZ.  
                return new TIMESTAMPLTZWrapper(ts, ((OracleConnection)getConnection(session, resultSet.getStatement().getConnection())).getSessionTimeZone());
            }
            return null;
        } else if (type == OracleTypes.OPAQUE) {
            Object result = resultSet.getObject(columnNumber);
            if(!(result instanceof OPAQUE)) {
              // Report Queries can cause result to not be an instance of OPAQUE.
              return result;
            }
            return getXMLTypeFactory().getString((OPAQUE)result);
        } else {
            return super.getObjectFromResultSet(resultSet, columnNumber, type, session);
        }
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
        Class type = field.getType();
        if ((type != null) && isOracle9Specific(type)) {
            return true;
        }
        return super.shouldUseCustomModifyForCall(field);
    }

    /**
     * INTERNAL:
     * Allow the use of XMLType operators on this platform.
     */
    protected void initializePlatformOperators() {
        super.initializePlatformOperators();
        addOperator(ExpressionOperator.extract());
        addOperator(ExpressionOperator.extractValue());
        addOperator(ExpressionOperator.existsNode());
        addOperator(ExpressionOperator.isFragment());
        addOperator(ExpressionOperator.getStringVal());
        addOperator(ExpressionOperator.getNumberVal());
        addOperator(SpatialExpressionOperators.withinDistance());
        addOperator(SpatialExpressionOperators.relate());
        addOperator(SpatialExpressionOperators.filter());
        addOperator(SpatialExpressionOperators.nearestNeighbor());
    }

    /**
     * INTERNAL:
     * Add XMLType as the default database type for org.w3c.dom.Documents.
     * Add TIMESTAMP, TIMESTAMP WITH TIME ZONE and TIMESTAMP WITH LOCAL TIME ZONE
     */
    protected Hashtable buildFieldTypes() {
        Hashtable fieldTypes = super.buildFieldTypes();
        fieldTypes.put(org.w3c.dom.Document.class, new FieldTypeDefinition("sys.XMLType"));
        //Bug#3381652 10g database does not accept Time for DATE field
        fieldTypes.put(java.sql.Time.class, new FieldTypeDefinition("TIMESTAMP", false));
        fieldTypes.put(java.sql.Timestamp.class, new FieldTypeDefinition("TIMESTAMP", false));
        fieldTypes.put(oracle.sql.TIMESTAMP.class, new FieldTypeDefinition("TIMESTAMP", false));
        fieldTypes.put(oracle.sql.TIMESTAMPTZ.class, new FieldTypeDefinition("TIMESTAMP WITH TIME ZONE", false));
        fieldTypes.put(oracle.sql.TIMESTAMPLTZ.class, new FieldTypeDefinition("TIMESTAMP WITH LOCAL TIME ZONE", false));
        return fieldTypes;
    }

    /**
     * INTERNAL:
     * Add TIMESTAMP, TIMESTAMP WITH TIME ZONE and TIMESTAMP WITH LOCAL TIME ZONE
     */
    protected Hashtable buildClassTypes() {
        Hashtable classTypeMapping = super.buildClassTypes();
        classTypeMapping.put("TIMESTAMP", oracle.sql.TIMESTAMP.class);
        classTypeMapping.put("TIMESTAMP WITH TIME ZONE", oracle.sql.TIMESTAMPTZ.class);
        classTypeMapping.put("TIMESTAMP WITH LOCAL TIME ZONE", oracle.sql.TIMESTAMPLTZ.class);
        return classTypeMapping;
    }

    /**
     * INTERNAL:
     * Allow for conversion from the Oralce type to the Java type.
     */
    public Object convertObject(Object sourceObject, Class javaClass) throws ConversionException, DatabaseException {
        if ((javaClass == null) || ((sourceObject != null) && (sourceObject.getClass() == javaClass))) {
            return sourceObject;
        }

        Object valueToConvert = sourceObject;

        //Used in Type Conversion Mapping on write
        if ((javaClass == TIMESTAMPTypes.TIMESTAMP_CLASS) || (javaClass == TIMESTAMPTypes.TIMESTAMPTZ_CLASS) || (javaClass == TIMESTAMPTypes.TIMESTAMPLTZ_CLASS)) {
            return sourceObject;
        }

        if (javaClass == XMLTYPE) {
            //Don't convert to XMLTypes. This will be done by the 
            //XMLTypeBindCallCustomParameter to ensure the correct
            //Connection is used
            return sourceObject;
        }

        //Added to overcome an issue with the oracle 9.0.1.1.0 JDBC driver.
        if (sourceObject instanceof TIMESTAMP) {
            try {
                valueToConvert = ((TIMESTAMP)sourceObject).timestampValue();
            } catch (SQLException exception) {
                throw DatabaseException.sqlException(exception);
            }
        } else if (sourceObject instanceof TIMESTAMPTZWrapper) {
            //Bug#4364359 Used when database type is TIMESTAMPTZ.  Timestamp and session timezone are wrapped
            //in TIMESTAMPTZWrapper.  Separate Calendar from any other types.
            if (((javaClass == ClassConstants.CALENDAR) || (javaClass == ClassConstants.GREGORIAN_CALENDAR))) {
                try {
                    return TIMESTAMPHelper.buildCalendar((TIMESTAMPTZWrapper)sourceObject);
                } catch (SQLException exception) {
                    throw DatabaseException.sqlException(exception);
                }
            } else {
                //If not using native sql, Calendar will be converted to Timestamp just as
                //other date time types
                valueToConvert = ((TIMESTAMPTZWrapper)sourceObject).getTimestamp();
            }
        } else if (sourceObject instanceof TIMESTAMPLTZWrapper) {
            //Bug#4364359 Used when database type is TIMESTAMPLTZ.  Timestamp and session timezone id are wrapped
            //in TIMESTAMPLTZWrapper.  Separate Calendar from any other types.
            if (((javaClass == ClassConstants.CALENDAR) || (javaClass == ClassConstants.GREGORIAN_CALENDAR))) {
                try {
                    return TIMESTAMPHelper.buildCalendar((TIMESTAMPLTZWrapper)sourceObject);
                } catch (SQLException exception) {
                    throw DatabaseException.sqlException(exception);
                }
            } else {
                //If not using native sql, Calendar will be converted to Timestamp just as
                //other date time types
                valueToConvert = ((TIMESTAMPLTZWrapper)sourceObject).getTimestamp();
            }
        }

        return super.convertObject(valueToConvert, javaClass);
    }

    /**
     * INTERNAL:
     *    Appends an Oracle specific Timestamp, if usesNativeSQL is true otherwise use the ODBC format.
     *    Native Format: to_timestamp ('1997-11-06 10:35:45.656' , 'yyyy-mm-dd hh:mm:ss.ff')
     */
    protected void appendTimestamp(java.sql.Timestamp timestamp, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("to_timestamp('");
            writer.write(Helper.printTimestamp(timestamp));
            writer.write("','yyyy-mm-dd HH24:MI:SS.FF')");
        } else {
            super.appendTimestamp(timestamp, writer);
        }
    }

    /**
     * INTERNAL:
     *    Appends an Oracle specific Timestamp, if usesNativeSQL is true otherwise use the ODBC format.
     *    Native Format: to_timestamp_tz ('1997-11-06 10:35:45.345 America/Los_Angeles' , 'yyyy-mm-dd hh:mm:ss.ff TZR')
     */
    protected void appendCalendar(Calendar calendar, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("to_timestamp_tz('");
            writer.write(TIMESTAMPHelper.printCalendar(calendar));
            writer.write("','yyyy-mm-dd HH24:MI:SS.FF TZR')");
        } else {
            super.appendCalendar(calendar, writer);
        }
    }

    /**
     * INTERNAL:
     * This method is used to unwrap the oracle connection wrapped by
     * the application server.  TopLink needs this unwrapped connection for certain
     * Oracle Specific support. (ie TIMESTAMPTZ)
     * This is added as a workaround for bug 4565190
     */
    public Connection getConnection(AbstractSession session, Connection connection) {
        if (session.getServerPlatform() != null && ((DatabaseLogin)session.getLogin()).shouldUseExternalConnectionPooling()){
        // This is added as a workaround for bug 4460996
            return session.getServerPlatform().unwrapConnection(connection);
        }
        return connection;
    }

    /**
     *  INTERNAL:
     *  Note that index (not index+1) is used in statement.setObject(index, parameter)
     *    Binding starts with a 1 not 0, so make sure that index > 0.
     *  Treat Calendar separately. Bind Calendar as TIMESTAMPTZ.
     */
    public void setParameterValueInDatabaseCall(Object parameter, PreparedStatement statement, int index, AbstractSession session) throws SQLException {
        if (parameter instanceof Calendar) {
            Calendar calendar = (Calendar)parameter;
            Connection conn = getConnection(session, statement.getConnection());
            TIMESTAMPTZ tsTZ = TIMESTAMPHelper.buildTIMESTAMPTZ(calendar, conn, getPrintCalendarIntoTimestampTZ(), getCorrectTimeInTimestampTZ());
            statement.setObject(index, tsTZ);
        } else {
            super.setParameterValueInDatabaseCall(parameter, statement, index, session);
        }
    }

    /**
     * INTERNAL:
     * Answer the timestamp from the server. Convert TIMESTAMPTZ to Timestamp
     */
    public java.sql.Timestamp getTimestampFromServer(AbstractSession session, String sessionName) {
        if (getTimestampQuery() != null) {
            getTimestampQuery().setSessionName(sessionName);
            Object ob = session.executeQuery(getTimestampQuery());
            return ((TIMESTAMPTZWrapper)ob).getTimestamp();
        }
        return super.getTimestampFromServer(session, sessionName);
    }

    /**
     * INTERNAL:
     * This method returns the query to select the SYSTIMESTAMP as TIMESTAMPTZ
     * from the server for Oracle9i.
     */
    public ValueReadQuery getTimestampQuery() {
        if (timestampQuery == null) {
            timestampQuery = new ValueReadQuery();
            timestampQuery.setSQLString("SELECT SYSTIMESTAMP FROM DUAL");
        }
        return timestampQuery;
    }

    /**
     * INTERNAL:
     * Return the current SYSTIMESTAMP as TIMESTAMPTZ from the server.
     */
    public String serverTimestampString() {
        return "SYSTIMESTAMP";
    }

    protected Vector buildToTIMESTAMPVec() {
        Vector vec = new Vector();
        vec.addElement(java.util.Date.class);
        vec.addElement(Timestamp.class);
        vec.addElement(Calendar.class);
        vec.addElement(String.class);
        vec.addElement(Long.class);
        vec.addElement(Date.class);
        vec.addElement(Time.class);
        return vec;
    }

    protected Vector buildToNStringCharVec() {
        Vector vec = new Vector();
        vec.addElement(String.class);
        vec.addElement(Character.class);
        return vec;
    }

    protected Vector buildToNClobVec() {
        Vector vec = new Vector();
        vec.addElement(String.class);
        vec.addElement(Character[].class);
        vec.addElement(char[].class);
        return vec;
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
     * ADVANCED:
     * Configure the passing of Calendar objects as a String into the creation of TIMESTAMPTZ instances (True) 
     * or pass the Calendar object directly into the TIMESTAMPTZ instance (False).
     */
    public void setPrintCalendarIntoTimestampTZ(Boolean printCalendarIntoTimestampTZ) {
        this.printCalendarIntoTimestampTZ = printCalendarIntoTimestampTZ;
    }

    /**
     * ADVANCED:
     * Answer the passing of Calendar objects as a String into the creation of TIMESTAMPTZ instances (True) 
     * or pass the Calendar object directly into the TIMESTAMPTZ instance (False).
     */
    public Boolean getPrintCalendarIntoTimestampTZ() {
        return printCalendarIntoTimestampTZ;
    }

    /**
     * ADVANCED:
     * Configure the comparison and correction of time data passed into the creation of 
     * TIMESTAMPTZ instances (True), or disable the compare and correction algorithm (False).
     */
    public void setCorrectTimeInTimestampTZ(Boolean correctTimeInTimestampTZ) {
        this.correctTimeInTimestampTZ = correctTimeInTimestampTZ;
    }

    /**
     * ADVANCED:
     * Answer the comparison and correction of time data passed into the creation of 
     * TIMESTAMPTZ instances (True), or disable the compare and correction algorithm (False).
     */
    public Boolean getCorrectTimeInTimestampTZ() {
        return correctTimeInTimestampTZ;
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
     * Return if the type is a special oracle type.
     * bug 3325122 - just checking against the 4 classes is faster than isAssignableFrom MWN.
     */
    protected boolean isOracle9Specific(Class type) {
        return (type == NCHAR) || (type == NSTRING) || (type == NCLOB) || (type == XMLTYPE);
    }
    
    /**
     * INTERNAL:
     * Used in write LOB method only to identify a CLOB.
     */
    protected boolean isClob(Class type) {
        return NCLOB.equals(type) || super.isClob(type);
    }
    
    /**
     *  INTERNAL:
     *  Used by SQLCall.translate(..)
     *  The binding *must* be performed (NCHAR, NSTRING, NCLOB).
     *  In these special cases the method returns a wrapper object
     *  which knows whether it should be bound or appended and knows how to do that.
     */
    public Object getCustomModifyValueForCall(Call call, Object value, DatabaseField field, boolean shouldBind) {
        Class type = field.getType();
        if ((type != null) && isOracle9Specific(type)) {
            if(value == null) {
                return null;
            }
            if (NCHAR.equals(type) || NSTRING.equals(type)) {
                return new NTypeBindCallCustomParameter(value);
            } else if (NCLOB.equals(type)) {
                value = convertToDatabaseType(value);
                if (shouldUseLocatorForLOBWrite()) {
                    if (lobValueExceedsLimit(value)) {
                        ((DatabaseCall)call).addContext(field, value);
                        value = new String(" ");
                    }
                }
                return new NTypeBindCallCustomParameter(value);
            } else if (XMLTYPE.equals(type)) {
                return getXMLTypeFactory().createXMLTypeBindCallCustomParameter(value);
            }
        }
        return super.getCustomModifyValueForCall(call, value, field, shouldBind);
    }  

    protected Vector buildFromStringCharVec(Class javaClass) {
        Vector vec = getConversionManager().getDataTypesConvertedFrom(javaClass);
        vec.addElement(NCHAR);
        vec.addElement(NSTRING);
        if (javaClass == String.class) {
            vec.addElement(NCLOB);
        }
        return vec;
    }
    
    /**
     * INTERNAL:
     * Return the list of Classes that can be converted to from the passed in javaClass.
     * oracle.sql.TIMESTAMP and NCHAR types are added in some lists.
     * @param javaClass - the class that is converted from
     * @return - a vector of classes
     */
    public Vector getDataTypesConvertedFrom(Class javaClass) {
        if (dataTypesConvertedFromAClass == null) {
            dataTypesConvertedFromAClass = new Hashtable(5);
        }
        Vector dataTypes = (Vector) dataTypesConvertedFromAClass.get(javaClass);
        if (dataTypes != null) {
            return dataTypes;
        }
        dataTypes = super.getDataTypesConvertedFrom(javaClass);
        if ((javaClass == String.class) || (javaClass == Character.class)) {
            dataTypes.addElement(NCHAR);
            dataTypes.addElement(NSTRING);
            if (javaClass == String.class) {
                dataTypes.addElement(NCLOB);
            }
        }
        if ((javaClass == char[].class) || (javaClass == Character[].class)) {
            dataTypes.addElement(NCLOB);
        }
        dataTypesConvertedFromAClass.put(javaClass, dataTypes);
        return dataTypes;
    }
    
    /**
     * INTERNAL:
     * Return the list of Classes that can be converted from to the passed in javaClass.
     * A list is added for oracle.sql.TIMESTAMP and NCHAR types.
     * @param javaClass - the class that is converted to
     * @return - a vector of classes
     */
    public Vector getDataTypesConvertedTo(Class javaClass) {
        if (dataTypesConvertedToAClass == null) {
            dataTypesConvertedToAClass = new Hashtable(5);
        }
        Vector dataTypes = (Vector) dataTypesConvertedToAClass.get(javaClass);
        if (dataTypes != null) {
            return dataTypes;
        }
        if ((javaClass == NCHAR) || (javaClass == NSTRING)) {
            dataTypes = buildToNStringCharVec();
        } else if (javaClass == NCLOB) {
            dataTypes = buildToNClobVec();
        } else {
            dataTypes = super.getDataTypesConvertedTo(javaClass);
        }
        dataTypesConvertedToAClass.put(javaClass, dataTypes);
        return dataTypes;
    }
    
    
    /**
     * Return the JDBC type for the given database field.
     * The Oracle driver does not like the OPAQUE type so VARCHAR must be used.
     */
    public int getJDBCType(DatabaseField field) {
        int type = super.getJDBCType(field);
        if (type == OracleTypes.OPAQUE) {
            // VARCHAR seems to work, driver does not like OPAQUE.
            return java.sql.Types.VARCHAR;
        }
        return type;
    }
    
    /**
     * Return the JDBC type for the Java type.
     * The Oracle driver does not like the OPAQUE type so VARCHAR must be used.
     */
    public int getJDBCType(Class javaType) {
        if (javaType == XMLTYPE) {
            //return OracleTypes.OPAQUE;
            // VARCHAR seems to work...
            return java.sql.Types.VARCHAR;
        }
        return super.getJDBCType(javaType);
    }
    
    /**
     * INTERNAL: This gets called on each batch statement execution
     * Needs to be implemented so that it returns the number of rows successfully modified
     * by this statement for optimistic locking purposes (if useNativeBatchWriting is enabled, and 
     * the call uses optimistic locking).  
     * 
     * @param isStatementPrepared - flag is set to true if this statement is prepared 
     * @return - number of rows modified/deleted by this statement
     */
    public int executeBatch(Statement statement,  boolean isStatementPrepared)throws java.sql.SQLException {
        if (usesNativeBatchWriting() && isStatementPrepared){
            return((OraclePreparedStatement)statement).sendBatch(); 
        }else {
            return super.executeBatch(statement, isStatementPrepared);
        }
    }
    
     /**
      * INTERNAL: This gets called on each iteration to add parameters to the batch
      * Needs to be implemented so that it returns the number of rows successfully modified
      * by this statement for optimistic locking purposes (if useNativeBatchWriting is enabled, and 
      * the call uses optimistic locking).  Is used with parameterized SQL 
      * 
      * @return - number of rows modified/deleted by this statement if it was executed (0 if it wasn't)
      */
    public int addBatch(PreparedStatement statement) throws java.sql.SQLException {
        if (usesNativeBatchWriting()){
            return statement.executeUpdate(); 
        }else{
            return super.addBatch(statement);
        }
    }
    
     /**
      * INTERNAL: Allows setting the batch size on the statement
      *  Is used with parameterized SQL, and should only be passed in prepared statements
      * 
      * @return - statement to be used for batch writing
      */
    public Statement prepareBatchStatement(Statement statement) throws java.sql.SQLException {
        if (usesNativeBatchWriting()){
            //add max statement setting
            ((OraclePreparedStatement) statement).setExecuteBatch(getMaxBatchWritingSize()); 
        }
        return statement;
    }
    
    /**
     * INTERNAL:
     * Lazy initialization of xmlTypeFactory allows to avoid loading xdb-dependent
     * class XMLTypeFactoryImpl unless xdb is used.
     * @return XMLTypeFactory
     */
    protected XMLTypeFactory getXMLTypeFactory() {
        if(xmlTypeFactory == null) {
            String className = "oracle.toplink.internal.platform.database.oracle.xdb.XMLTypeFactoryImpl";
            try {
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    Class xmlTypeFactoryClass = (Class)AccessController.doPrivileged(new PrivilegedClassForName(className, true, this.getClass().getClassLoader()));
                    Constructor xmlTypeFactoryConstructor = (Constructor)AccessController.doPrivileged(new PrivilegedGetConstructorFor(xmlTypeFactoryClass, new Class[0], true));
                    xmlTypeFactory = (XMLTypeFactory)AccessController.doPrivileged(new PrivilegedInvokeConstructor(xmlTypeFactoryConstructor, new Object[0]));
                }else{
                    Class xmlTypeFactoryClass = PrivilegedAccessHelper.getClassForName(className, true, this.getClass().getClassLoader());
                    Constructor xmlTypeFactoryConstructor = PrivilegedAccessHelper.getConstructorFor(xmlTypeFactoryClass, new Class[0], true);
                    xmlTypeFactory = (XMLTypeFactory)PrivilegedAccessHelper.invokeConstructor(xmlTypeFactoryConstructor, new Object[0]);
                }
            } catch (Exception e) {
                throw QueryException.reflectiveCallOnTopLinkClassFailed(className, e);
            }
        }
        return xmlTypeFactory;
    }
    
    /**
     * INTERNAL:
     * Indicates whether the passed object is an instance of XDBDocument.
     * @return boolean
     */
    public boolean isXDBDocument(Object obj) {
        return getXMLTypeFactory().isXDBDocument(obj);
    }

    /**
     * INTERNAL:
     * Indicates whether this Oracle platform can unwrap Oracle connection.
     */
    public boolean canUnwrapOracleConnection() {
        return true;
    }

    /**
     * INTERNAL:
     * If can unwrap returns unwrapped Oracle connection, otherwise original connection.
     */
    public Connection unwrapOracleConnection(Connection connection) {
        //Bug#4607977  Use getPhysicalConnection() instead of physicalConnectionWithin() because it's not suppported in 9.2 driver
        if(connection instanceof oracle.jdbc.internal.OracleConnection){
            return ((oracle.jdbc.internal.OracleConnection)connection).getPhysicalConnection();
        }else{
            return super.unwrapOracleConnection(connection);
        }
    }
}
