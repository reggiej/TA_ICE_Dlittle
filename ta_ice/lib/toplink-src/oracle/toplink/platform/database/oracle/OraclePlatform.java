// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.platform.database.oracle;

// javase imports
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;

// TopLink imports
import oracle.toplink.exceptions.ValidationException;
import oracle.toplink.expressions.ExpressionOperator;
import oracle.toplink.internal.databaseaccess.DatabaseCall;
import oracle.toplink.internal.databaseaccess.FieldTypeDefinition;
import oracle.toplink.internal.expressions.ExpressionSQLPrinter;
import oracle.toplink.internal.expressions.FunctionExpression;
import oracle.toplink.internal.expressions.RelationExpression;
import oracle.toplink.internal.expressions.SQLSelectStatement;
import oracle.toplink.internal.helper.DatabaseField;
import oracle.toplink.internal.helper.Helper;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.queryframework.SQLCall;
import oracle.toplink.queryframework.ValueReadQuery;

/**
 *    <p><b>Purpose</b>: Provides Oracle specific behaviour.
 *    <p><b>Responsibilities</b>:<ul>
 *    <li> Native SQL for byte[], Date, Time, & Timestamp.
 *    <li> Native sequencing named sequences.
 *    <li> Native SQL/ROWNUM support for MaxRows and FirstResult filtering.
 *    </ul>
 *
 * @since TOPLink/Java 1.0
 */
public class OraclePlatform extends oracle.toplink.platform.database.DatabasePlatform {

    /** 
     * Oracle's Rownum can be used to limit results and for pagination, 
     *   using the query's maxRows and FirstResult settings
     */ 
    protected boolean useRownumFiltering = true;
    
    /** 
     * Advanced attribute indicating whether identity is supported,
     *   see comment to setSupportsIdentity method.
     */ 
    protected boolean supportsIdentity;

    public OraclePlatform(){
        super();
        this.pingSQL = "SELECT 1 FROM DUAL";
    }
    
    /**
     * Used for sp defs.
     */
    public boolean allowsSizeInProcedureArguments() {
        return false;
    }

    /**
     * INTERNAL:
     *    If using native SQL then print a byte[] literally as a hex string otherwise use ODBC format
     *    as provided in DatabasePlatform.
     */
    protected void appendByteArray(byte[] bytes, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write('\'');
            Helper.writeHexString(bytes, writer);
            writer.write('\'');
        } else {
            super.appendByteArray(bytes, writer);
        }
    }

    /**
     * INTERNAL:
     *    Appends an Oracle specific date if usesNativeSQL is true otherwise use the ODBC format.
     *    Native FORMAT: to_date('1997-11-06','yyyy-mm-dd')
     */
    protected void appendDate(java.sql.Date date, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("to_date('");
            writer.write(Helper.printDate(date));
            writer.write("','yyyy-mm-dd')");
        } else {
            super.appendDate(date, writer);
        }
    }

    /**
     * INTERNAL:
     * Appends an Oracle specific time if usesNativeSQL is true otherwise use the ODBC format.
     * Native FORMAT: to_date(#####, 'sssss').
     */
    protected void appendTime(java.sql.Time time, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("to_date('");
            writer.write(Helper.printTime(time));
            writer.write("','hh24:mi:ss')");
        } else {
            super.appendTime(time, writer);
        }
    }

    /**
     * INTERNAL:
     *    Appends an Oracle specific Timestamp, if usesNativeSQL is true otherwise use the ODBC format.
     *    Native Format: to_date ('1997-11-06 10:35:45.0' , 'yyyy-mm-dd hh:mm:ss.n')
     */
    protected void appendTimestamp(java.sql.Timestamp timestamp, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("to_date('");
            writer.write(Helper.printTimestampWithoutNanos(timestamp));
            writer.write("','yyyy-mm-dd hh24:mi:ss')");
        } else {
            super.appendTimestamp(timestamp, writer);
        }
    }

    /**
     * INTERNAL:
     *    Appends an Oracle specific Timestamp, if usesNativeSQL is true otherwise use the ODBC format.
     *    Native Format: to_date ('1997-11-06 10:35:45.0' , 'yyyy-mm-dd hh:mm:ss.n')
     */
    protected void appendCalendar(Calendar calendar, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("to_date('");
            writer.write(Helper.printCalendarWithoutNanos(calendar));
            writer.write("','yyyy-mm-dd hh24:mi:ss')");
        } else {
            super.appendCalendar(calendar, writer);
        }
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public ExpressionOperator atan2Operator() {
        return ExpressionOperator.simpleTwoArgumentFunction(ExpressionOperator.Atan2, "ATAN2");
    }

    /**
     * INTERNAL:
     */
    protected Hashtable buildFieldTypes() {
        Hashtable fieldTypeMapping;

        fieldTypeMapping = new Hashtable();
        fieldTypeMapping.put(Boolean.class, new FieldTypeDefinition("NUMBER(1) default 0", false));

        fieldTypeMapping.put(Integer.class, new FieldTypeDefinition("NUMBER", 10));
        fieldTypeMapping.put(Long.class, new FieldTypeDefinition("NUMBER", 19));
        fieldTypeMapping.put(Float.class, new FieldTypeDefinition("NUMBER", 19, 4));
        fieldTypeMapping.put(Double.class, new FieldTypeDefinition("NUMBER", 19, 4));
        fieldTypeMapping.put(Short.class, new FieldTypeDefinition("NUMBER", 5));
        fieldTypeMapping.put(Byte.class, new FieldTypeDefinition("NUMBER", 3));
        fieldTypeMapping.put(java.math.BigInteger.class, new FieldTypeDefinition("NUMBER", 38));
        fieldTypeMapping.put(java.math.BigDecimal.class, new FieldTypeDefinition("NUMBER", 38).setLimits(38, -38, 38));
        fieldTypeMapping.put(Number.class, new FieldTypeDefinition("NUMBER", 38).setLimits(38, -38, 38));

        fieldTypeMapping.put(String.class, new FieldTypeDefinition("VARCHAR2", 255));
        fieldTypeMapping.put(Character.class, new FieldTypeDefinition("CHAR", 1));

        fieldTypeMapping.put(Byte[].class, new FieldTypeDefinition("LONG RAW", false));
        fieldTypeMapping.put(Character[].class, new FieldTypeDefinition("LONG", false));
        fieldTypeMapping.put(byte[].class, new FieldTypeDefinition("LONG RAW", false));
        fieldTypeMapping.put(char[].class, new FieldTypeDefinition("LONG", false)); 
        fieldTypeMapping.put(java.sql.Blob.class, new FieldTypeDefinition("LONG RAW", false));
        fieldTypeMapping.put(java.sql.Clob.class, new FieldTypeDefinition("LONG", false));         

        fieldTypeMapping.put(java.sql.Date.class, new FieldTypeDefinition("DATE", false));
        fieldTypeMapping.put(java.sql.Time.class, new FieldTypeDefinition("TIMESTAMP", false));
        fieldTypeMapping.put(java.sql.Timestamp.class, new FieldTypeDefinition("TIMESTAMP", false));
        //bug 5871089 the default generator requires definitions based on all java types
        fieldTypeMapping.put(java.util.Calendar.class, new FieldTypeDefinition("TIMESTAMP"));
        fieldTypeMapping.put(java.util.Date.class, new FieldTypeDefinition("TIMESTAMP"));

        return fieldTypeMapping;
    }

    /**
     * INTERNAL:
     * Returns null unless the platform supports call with returning
     */
    public DatabaseCall buildCallWithReturning(SQLCall sqlCall, Vector returnFields) {
        SQLCall call = new SQLCall();
        call.setParameters(sqlCall.getParameters());
        call.setParameterTypes(sqlCall.getParameterTypes());

        Writer writer = new CharArrayWriter(200);
        try {
            writer.write("BEGIN ");
            writer.write(sqlCall.getSQLString());
            writer.write(" RETURNING ");

            for (int i = 0; i < returnFields.size(); i++) {
                DatabaseField field = (DatabaseField)returnFields.elementAt(i);
                writer.write(field.getName());
                if ((i + 1) < returnFields.size()) {
                    writer.write(", ");
                }
            }

            writer.write(" INTO ");

            for (int i = 0; i < returnFields.size(); i++) {
                DatabaseField field = (DatabaseField)returnFields.elementAt(i);
                call.appendOut(writer, field);
                if ((i + 1) < returnFields.size()) {
                    writer.write(", ");
                }
            }

            writer.write("; END;");

            call.setQueryString(writer.toString());

        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }

        return call;
    }

    /**
     * INTERNAL:
     * Indicates whether the platform can build call with returning.
     * In case this method returns true, buildCallWithReturning method
     * may be called.
     */
    public boolean canBuildCallWithReturning() {
        return true;
    }

    /**
     * INTERNAL:
     * Used for stored function calls.
     */
    public String getAssignmentString() {
        return ":= ";
    }

    /**
     * INTERNAL:
     * DECLARE stanza header for Anonymous PL/SQL block
     */
    public String getDeclareBeginString() {
        return "DECLARE ";
    }

    /**
     * Used for batch writing and sp defs.
     */
    public String getBatchBeginString() {
        return "BEGIN ";
    }

    /**
     * Used for batch writing and sp defs.
     */
    public String getBatchEndString() {
        return "END;";
    }

    /**
     * INTERNAL:
     * returns the maximum number of characters that can be used in a field
     * name on this platform.
     */
    public int getMaxFieldNameSize() {
        return 30;
    }

    /**
     * Return the catalog information through using the native SQL catalog selects.
     * This is required because many JDBC driver do not support meta-data.
     * Willcards can be passed as arguments.
     */
    public Vector getNativeTableInfo(String table, String creator, AbstractSession session) {
        String query = "SELECT * FROM ALL_TABLES WHERE OWNER NOT IN ('SYS', 'SYSTEM')";
        if (table != null) {
            if (table.indexOf('%') != -1) {
                query = query + " AND TABLE_NAME LIKE " + table;
            } else {
                query = query + " AND TABLE_NAME = " + table;
            }
        }
        if (creator != null) {
            if (creator.indexOf('%') != -1) {
                query = query + " AND OWNER LIKE " + creator;
            } else {
                query = query + " AND OWNER = " + creator;
            }
        }
        return session.executeSelectingCall(new oracle.toplink.queryframework.SQLCall(query));
    }

    /**
     * Used for sp calls.
     */
    public String getProcedureArgumentSetter() {
        return "=>";
    }

    /**
     * Used for sp calls.
     */
    public String getProcedureCallHeader() {
        return "BEGIN ";
    }

    /**
     * Used for sp calls.
     */
    public String getProcedureCallTail() {
        return "; END;";
    }

    public String getSelectForUpdateString() {
        return " FOR UPDATE";
    }

    public String getStoredProcedureParameterPrefix() {
        return "P_";
    }

    public String getStoredProcedureTerminationToken() {
        return "";
    }

    /**
     * PUBLIC:
     * The query to select the current system change number
     * from Oracle.
     * In order to execute this query a database administrator may need
     * to grant execute permission on pl/sql package DBMS_FLASHBACK.
     */
    public ValueReadQuery getSystemChangeNumberQuery() {
        ValueReadQuery sCNQuery = new ValueReadQuery();
        sCNQuery.setSQLString("SELECT DBMS_FLASHBACK.GET_SYSTEM_CHANGE_NUMBER FROM DUAL");
        return sCNQuery;
    }

    /**
     * PUBLIC:
     * This method returns the query to select the timestamp
     * from the server for Oracle.
     */
    public ValueReadQuery getTimestampQuery() {
        if (timestampQuery == null) {
            timestampQuery = new ValueReadQuery();
            timestampQuery.setSQLString("SELECT SYSDATE FROM DUAL");
        }
        return timestampQuery;
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
        } else {
            return super.getObjectFromResultSet(resultSet, columnNumber, type, session);
        }
    }

    /**
     * Initialize any platform-specific operators
     */
    protected void initializePlatformOperators() {
        super.initializePlatformOperators();
        addOperator(operatorOuterJoin());
        addOperator(logOperator());
        addOperator(ExpressionOperator.simpleTwoArgumentFunction(ExpressionOperator.Concat, "CONCAT"));
        addOperator(todayOperator());
        addOperator(currentDateOperator());
        addOperator(currentTimeOperator());
        addOperator(ExpressionOperator.truncateDate());
        addOperator(ExpressionOperator.newTime());
        addOperator(ExpressionOperator.ifNull());
        addOperator(atan2Operator());
        addOperator(ExpressionOperator.oracleDateName());
        addOperator(operatorLocate());
        addOperator(operatorLocate2());
    }

    public boolean isOracle() {
        return true;
    }

    /**
     *  Create the log operator for this platform
     */
    protected ExpressionOperator logOperator() {
        ExpressionOperator result = new ExpressionOperator();
        result.setSelector(ExpressionOperator.Log);
        Vector v = oracle.toplink.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement("LOG(");
        v.addElement(", 10)");
        result.printsAs(v);
        result.bePrefix();
        result.setNodeClass(FunctionExpression.class);
        return result;

    }

    /**
     *    Builds a table of maximum numeric values keyed on java class. This is used for type testing but
     * might also be useful to end users attempting to sanitize values.
     * <p><b>NOTE</b>: BigInteger & BigDecimal maximums are dependent upon their precision & Scale
     */
    public Hashtable maximumNumericValues() {
        Hashtable values = new Hashtable();

        values.put(Integer.class, new Integer(Integer.MAX_VALUE));
        values.put(Long.class, new Long(Long.MAX_VALUE));
        values.put(Double.class, new Double((double)9.9999E125));
        values.put(Short.class, new Short(Short.MAX_VALUE));
        values.put(Byte.class, new Byte(Byte.MAX_VALUE));
        values.put(Float.class, new Float(Float.MAX_VALUE));
        values.put(java.math.BigInteger.class, new java.math.BigInteger("0"));
        values.put(java.math.BigDecimal.class, new java.math.BigDecimal(new java.math.BigInteger("0"), 38));
        return values;
    }

    /**
     *    Builds a table of minimum numeric values keyed on java class. This is used for type testing but
     * might also be useful to end users attempting to sanitize values.
     * <p><b>NOTE</b>: BigInteger & BigDecimal minimums are dependent upon their precision & Scale
     */
    public Hashtable minimumNumericValues() {
        Hashtable values = new Hashtable();

        values.put(Integer.class, new Integer(Integer.MIN_VALUE));
        values.put(Long.class, new Long(Long.MIN_VALUE));
        values.put(Double.class, new Double((double)-1E-129));
        values.put(Short.class, new Short(Short.MIN_VALUE));
        values.put(Byte.class, new Byte(Byte.MIN_VALUE));
        values.put(Float.class, new Float(Float.MIN_VALUE));
        values.put(java.math.BigInteger.class, new java.math.BigInteger("0"));
        values.put(java.math.BigDecimal.class, new java.math.BigDecimal(new java.math.BigInteger("0"), 38));
        return values;
    }

    /**
     * INTERNAL:
     *    Produce a DataReadQuery which updates(!) the sequence number in the db
     *  and returns it. Currently implemented on Oracle only.
     *    @param sequenceName        Name known by Oracle to be a defined sequence
     */
    public ValueReadQuery buildSelectQueryForSequenceObject(String seqName, Integer size) {
        return new ValueReadQuery("SELECT " + getQualifiedSequenceName(seqName) + ".NEXTVAL FROM DUAL");
    }

    /**
     * INTERNAL:
     *  Though Oracle doesn't support identity it could be immitated,
     *  see comment to setSupportsIdentity method.
     *  @param sequenceName        Name known by Oracle to be a defined sequence
     */
    public ValueReadQuery buildSelectQueryForIdentity(String seqName, Integer size) {
        return new ValueReadQuery("SELECT " + getQualifiedSequenceName(seqName) + ".CURRVAL FROM DUAL");
    }

    /**
     *  Prepend sequence name with table qualifier (if any)
     */
    protected String getQualifiedSequenceName(String seqName) {
        if (getTableQualifier().equals("")) {
            return seqName;
        } else {
            return getTableQualifier() + "." + seqName;
        }
    }

    /**
     *  Create the outer join operator for this platform
     */
    protected ExpressionOperator operatorOuterJoin() {
        ExpressionOperator result = new ExpressionOperator();
        result.setSelector(ExpressionOperator.EqualOuterJoin);
        Vector v = oracle.toplink.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement(" (+) = ");
        result.printsAs(v);
        result.bePostfix();
        result.setNodeClass(RelationExpression.class);
        return result;

    }

    /**
     * INTERNAL:
     * Override the default locate operator
     */
    protected ExpressionOperator operatorLocate() {
        ExpressionOperator result = new ExpressionOperator();
        result.setSelector(ExpressionOperator.Locate);
        Vector v = oracle.toplink.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement("INSTR(");
        v.addElement(", ");
        v.addElement(")");
        result.printsAs(v);
        result.bePrefix();
        result.setNodeClass(RelationExpression.class);
        return result;
    }

    /**
     * INTERNAL:
     * Override the default locate operator
     */
    protected ExpressionOperator operatorLocate2() {
        ExpressionOperator result = new ExpressionOperator();
        result.setSelector(ExpressionOperator.Locate2);
        Vector v = oracle.toplink.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement("INSTR(");
        v.addElement(", ");
        v.addElement(", ");
        v.addElement(")");
        result.printsAs(v);
        result.bePrefix();
        result.setNodeClass(RelationExpression.class);
        return result;
    }

    /**
     * INTERNAL:
     * Append the receiver's field 'NULL' constraint clause to a writer.
     */
    public void printFieldNullClause(Writer writer) throws ValidationException {
        try {
            writer.write(" NULL");
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
    }

    /** 
     * Return the current date and time from the server.
     */
    public String serverTimestampString() {
        return "SYSDATE";
    }
    
    /**
     * PUBLIC:
     * Set if Oracle ROWNUM pagination should be used for FirstResult and MaxRows settings.
     * Default is false.
     */
    public void setShouldUseRownumFiltering(boolean useRownumFiltering) {
        this.useRownumFiltering = useRownumFiltering;
    }

    /**
     * Some database require outer joins to be given in the where clause, others require it in the from clause.
     */
    public boolean shouldPrintOuterJoinInWhereClause() {
        return true;
    }

    /**
     * JDBC defines and outer join syntax, many drivers do not support this. So we normally avoid it.
     */
    public boolean shouldUseJDBCOuterJoinSyntax() {
        return false;
    }
    
    /**
     * PUBLIC:
     * Return if Oracle ROWNUM pagination should be used for FirstResult and MaxRows settings.
     * Default is false.
     */
    public boolean shouldUseRownumFiltering() {
        return this.useRownumFiltering;
    }

    /**
     *  INTERNAL:
     *  Indicates whether the platform supports sequence objects.
     *  This method is to be used *ONLY* by sequencing classes
     */
    public boolean supportsSequenceObjects() {
        return true;
    }

    /**
     *  INTERNAL:
     *  Indicates whether the platform supports identity.
     *  This method is to be used *ONLY* by sequencing classes
     */
    public boolean supportsIdentity() {
        return supportsIdentity;
    }

    /**
     *  ADVANCED:
     *  Oracle db doesn't support identity.
     *  However it's possible to get identity-like behaviour
     *  using sequence in an insert trigger - that's the only 
     *  case when supportsIdentity should be set to true:
     *  in this case all the sequences that have shouldAcquireValueAfterInsert
     *  set to true will keep this setting (it would've been reversed in case
     *  identity is not supported).
     *  Note that with supportsIdentity==true attempt to create tables that have
     *  identity fields will fail - Oracle doesn't support identity.
     *  Therefore if there's table creation reqiured it should be done
     *  with supportsIdentity==false, then set the flag to true and reset sequencing
     *  (or logout and login the session).
     */
    public void setSupportsIdentity(boolean supportsIdentity) {
        this.supportsIdentity = supportsIdentity;
    }

    /**
     *  INTERNAL
     */
    public boolean supportsStoredFunctions() {
        return true;
    }

    /**
      * Returns true if the database supports SQL syntax not to wait on a SELECT..FOR UPADTE
      * (i.e. In Oracle adding NOWAIT to the end will accomplish this)
      */
    public boolean supportsSelectForUpdateNoWait() {
        return true;
    }

    /**
     * Create the sysdate operator for this platform
     */
    protected ExpressionOperator todayOperator() {
        return ExpressionOperator.simpleFunctionNoParentheses(ExpressionOperator.Today, "SYSDATE");
    }

    protected ExpressionOperator currentDateOperator() {
        return ExpressionOperator.simpleFunctionNoParentheses(ExpressionOperator.CurrentDate, "TO_DATE(CURRENT_DATE)");
    }

    protected ExpressionOperator currentTimeOperator() {
        return ExpressionOperator.simpleFunctionNoParentheses(ExpressionOperator.CurrentTime, "SYSDATE");
    }

    /**
     * INTERNAL:
     * Indicates whether this Oracle platform can unwrap Oracle connection.
     */
    public boolean canUnwrapOracleConnection() {
        return false;
    }

    /**
     * INTERNAL:
     * If can unwrap returns unwrapped Oracle connection, otherwise original connection.
     */
    public Connection unwrapOracleConnection(Connection connection) {
        return connection;
    }
    
    //Oracle Rownum support
    private static final String SELECT = "SELECT * FROM (SELECT ";
    private static final String HINT = "/*+ FIRST_ROWS */ ";
    private static final String FROM = "a.*, ROWNUM rnum  FROM (";
    private static final String END_FROM = ") a ";
    private static final String MAX_ROW = "WHERE ROWNUM <= ";
    private static final String MIN_ROW = ") WHERE rnum > ";
    /**
     * INTERNAL:
     * Print the SQL representation of the statement on a stream, storing the fields
     * in the DatabaseCall.  This implementation works MaxRows and FirstResult into the SQL using
     * Oracle's ROWNUM to filter values if shouldUseRownumFiltering is true.  
     */
    public void printSQLSelectStatement(DatabaseCall call, ExpressionSQLPrinter printer, SQLSelectStatement statement){
        int max = 0;
        int firstRow = 0;
        if (statement.getQuery()!=null){
            max = statement.getQuery().getMaxRows();
            firstRow = statement.getQuery().getFirstResult();
        }
        
        if ( !(this.shouldUseRownumFiltering()) || ( !(max>0) && !(firstRow>0) ) ){
            super.printSQLSelectStatement(call, printer,statement);
            return;
        }else if ( max > 0 ){
            statement.setUseUniqueFieldAliases(true);
            printer.printString(SELECT);
            printer.printString(HINT);
            printer.printString(FROM);
            
            call.setFields(statement.printSQL(printer));
            printer.printString(END_FROM);
            printer.printString(MAX_ROW);
            printer.printParameter(call.MAXROW_FIELD);
            printer.printString(MIN_ROW);
            printer.printParameter(DatabaseCall.FIRSTRESULT_FIELD);
        }else {// firstRow>0
            statement.setUseUniqueFieldAliases(true);
            printer.printString(SELECT);
            printer.printString(FROM);
            
            call.setFields(statement.printSQL(printer));
            printer.printString(END_FROM);
            printer.printString(MIN_ROW);
            printer.printParameter(DatabaseCall.FIRSTRESULT_FIELD);
        }
        call.setIgnoreFirstRowMaxResultsSettings(true);
    }

    /**
     * INTERNAL:
     * A call to this method will perform a platform based check on the connection and exception
     * error code to dermine if the connection is still valid or if a communication error has occurred.
     * If a communication error has occurred then the query may be retried.
     * If this platform is unable to determine if the error was communication based it will return
     * false forcing the error to be thrown to the user.
     */
    
    public boolean wasFailureCommunicationBased(SQLException exception, Connection connection, AbstractSession sessionForProfile){
        if (exception != null){
            if (exception.getErrorCode() == 17410){
                return true;
            }
            if (exception.getErrorCode() == 17002){
        	      return true;
            }
            if (exception.getErrorCode() == 2399){
                return true;
            }
            if (exception.getErrorCode() == 2396){
                return true;
            }
        }
        return super.wasFailureCommunicationBased(exception, connection, sessionForProfile);
    }

    /**
     * INTERNAL:
     * Override this method if the platform supports sequence objects.
     * Returns sql used to create sequence object in the database.
     */
    public Writer buildSequenceObjectCreationWriter(Writer writer, String fullSeqName, int increment, int start) throws IOException {
        writer.write("CREATE SEQUENCE ");
        writer.write(fullSeqName);
        if (increment != 1) {
            writer.write(" INCREMENT BY " + increment);
        }
        writer.write(" START WITH " + start);
        return writer;
    }

    /**
     * INTERNAL:
     * Override this method if the platform supports sequence objects.
     * Returns sql used to delete sequence object from the database.
     */
    public Writer buildSequenceObjectDeletionWriter(Writer writer, String fullSeqName) throws IOException {
        writer.write("DROP SEQUENCE ");
        writer.write(fullSeqName);
        return writer;
    }

    /**
     * INTERNAL:
     * Override this method if the platform supports sequence objects
     * and isAlterSequenceObjectSupported returns true.
     * Returns sql used to alter sequence object's increment in the database.
     */
    public Writer buildSequenceObjectAlterIncrementWriter(Writer writer, String fullSeqName, int increment) throws IOException {
        writer.write("ALTER SEQUENCE ");
        writer.write(fullSeqName);
        writer.write(" INCREMENT BY " + increment);
        return writer;
    }

    /**
     * INTERNAL:
     * Override this method if the platform supports sequence objects
     * and it's possible to alter sequence object's increment in the database.
     */
    public boolean isAlterSequenceObjectSupported() {
        return true;
    }
}
