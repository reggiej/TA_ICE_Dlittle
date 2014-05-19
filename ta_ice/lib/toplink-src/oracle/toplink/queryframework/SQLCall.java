// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import java.util.Vector;
import java.io.*;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.internal.helper.DatabaseField;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.exceptions.ValidationException;
import oracle.toplink.internal.expressions.ParameterExpression;
import oracle.toplink.objectrelational.ObjectRelationalDatabaseField;

/**
 * <b>Purpose</b>: Used as an abstraction of an SQL call.
 * A call is an SQL string with parameters.
 */
public class SQLCall extends DatabaseCall implements QueryStringCall {
    protected boolean hasCustomSQLArguments;

    /**
     * PUBLIC:
     * Create a new SQL call.
     */
    public SQLCall() {
        super();
        this.hasCustomSQLArguments = false;
    }

    /**
     * PUBLIC:
     * Create a new SQL call.
	 * Warning: Allowing an unverified SQL string to be passed into this 
	 * method makes your application vulnerable to SQL injection attacks. 
     */
    public SQLCall(String sqlString) {
        this();
        setSQLString(sqlString);
    }

    /**
     * INTERNAL:
     * Set the data passed through setCustomSQLArgumentType and useCustomSQLCursorOutputAsResultSet methods.
     */
    protected void afterTranslateCustomQuery(Vector updatedParameters, Vector updatedParameterTypes) {
        for (int i = 0; i < getParameters().size(); i++) {
            Integer parameterType = (Integer)getParameterTypes().elementAt(i);
            Object parameter = getParameters().elementAt(i);
            if ((parameterType == MODIFY) || (parameterType == OUT) || (parameterType == OUT_CURSOR) || ((parameterType == IN) && parameter instanceof DatabaseField)) {
                DatabaseField field = afterTranslateCustomQueryUpdateParameter((DatabaseField)parameter, i, parameterType, updatedParameters, updatedParameterTypes);
                if (field!=null){
                    getParameters().setElementAt(field, i);
                }
            } else if (parameterType == INOUT) {
                DatabaseField outField = afterTranslateCustomQueryUpdateParameter((DatabaseField)((Object[])parameter)[1], i, parameterType, updatedParameters, updatedParameterTypes);
                if (outField !=null){
                    if (((Object[])parameter)[0] instanceof DatabaseField){
                        if ( ((Object[])parameter)[0] != ((Object[])parameter)[1] ) {
                            DatabaseField inField = (DatabaseField)outField.clone();
                            inField.setName( ((DatabaseField)((Object[])parameter)[0]).getName());
                            ((Object[])parameter)[0] = inField;
                        }else {
                            ((Object[])parameter)[0] = outField;
                        }
                    }
                    ((Object[])parameter)[1] = outField;
                }
            } else if ((parameterType == IN)&& (parameter instanceof DatabaseField)){
                DatabaseField field = afterTranslateCustomQueryUpdateParameter((DatabaseField)parameter, i, parameterType, updatedParameters, updatedParameterTypes);
                if (field!=null){
                    getParameters().setElementAt(field, i);
                }
            }
        }
    }

    /**
     * INTERNAL:
     * Set the data passed through setCustomSQLArgumentType and useCustomSQLCursorOutputAsResultSet methods.
     * This will return the null if the user did not add the field/type usin the setCustomSQLArgumentType method
     */
    protected DatabaseField afterTranslateCustomQueryUpdateParameter(DatabaseField field, int index, Integer parameterType, Vector updatedParameters, Vector updatedParameterTypes) {
        for (int j = 0; j < updatedParameters.size(); j++) {
            DatabaseField updateField = (DatabaseField)updatedParameters.elementAt(j);
            if (field.equals(updateField)) {
                Integer updateParameterType = (Integer)updatedParameterTypes.elementAt(j);
                if (updateParameterType == null) {
                    return updateField;
                } else if (updateParameterType == OUT_CURSOR) {
                    if (parameterType == OUT) {
                        getParameterTypes().setElementAt(OUT_CURSOR, index);
                        return updateField;
                    } else {
                        throw ValidationException.cannotSetCursorForParameterTypeOtherThanOut(field.getName(), toString());
                    }
                }
                break;
            }
        }
        return null;
    }

    /**
     * INTERNAL:
     * Used to avoid misiterpreting the # in custom SQL.
     */
    public boolean hasCustomSQLArguments() {
        return hasCustomSQLArguments;
    }

    public boolean isSQLCall() {
        return true;
    }

    public boolean isQueryStringCall() {
        return true;
    }

    /**
     * INTERNAL:
     * Called by prepare method only.
     */
    protected void prepareInternal(AbstractSession session) {
        if (hasCustomSQLArguments()) {
            // hold results of setCustomSQLArgumentType and useCustomSQLCursorOutputAsResultSet methods
            Vector updatedParameters = null;
            Vector updatedParameterTypes = null;
            if (getParameters().size() > 0) {
                updatedParameters = getParameters();
                setParameters(oracle.toplink.internal.helper.NonSynchronizedVector.newInstance());
                updatedParameterTypes = getParameterTypes();
                setParameterTypes(oracle.toplink.internal.helper.NonSynchronizedVector.newInstance());
            }

            translateCustomQuery();

            if (updatedParameters != null) {
                afterTranslateCustomQuery(updatedParameters, updatedParameterTypes);
            }
        }

        super.prepareInternal(session);
    }

    /**
     * INTERNAL:
     * Used to avoid misiterpreting the # in custom SQL.
     */
    public void setHasCustomSQLArguments(boolean hasCustomSQLArguments) {
        this.hasCustomSQLArguments = hasCustomSQLArguments;
    }

    /**
     * PUBLIC:
     * This method should only be used with custom SQL:
     * it sets a type to IN, OUT or INOUT parameter (prefixed with #, ### or #### in custom SQL string).
     */
    public void setCustomSQLArgumentType(String customParameterName, Class type) {
        DatabaseField field = new DatabaseField(customParameterName);
        field.setType(type);
        getParameters().add(field);
        getParameterTypes().add(null);
    }
    
    /**
     * PUBLIC:
     * This method should only be used with custom SQL:
     * it sets a type to IN, OUT or INOUT parameter (prefixed with #, ### or #### in custom SQL string).
     * The argumentFieldName is the field or argument name used in the SQL.
     * The type is the JDBC type code for the parameter.
     */
    public void setCustomSQLArgumentType(String argumentFieldName, int type) {
        DatabaseField field = new DatabaseField(argumentFieldName);
        field.setSqlType(type);
        getParameters().add(field);
        getParameterTypes().add(null);
    }
    
    /**
     * PUBLIC:
     * This method should only be used with custom SQL:
     * it sets a type to IN, OUT or INOUT parameter (prefixed with #, ### or #### in custom SQL string).
     * TThe argumentFieldName is the field or argument name used in the SQL.
     * The type is the JDBC type code for the parameter.
     * The typeName is the JDBC type name, this may be required for ARRAY or STRUCT types.
     */
    public void setCustomSQLArgumentType(String argumentFieldName, int type, String typeName) {
        ObjectRelationalDatabaseField field = new ObjectRelationalDatabaseField(argumentFieldName);
        field.setSqlType(type);
        field.setSqlTypeName(typeName);
        getParameters().add(field);
        getParameterTypes().add(null);
    }
    
    /**
     * PUBLIC:
     * This method should only be used with custom SQL:
     * it sets a type to IN, OUT or INOUT parameter (prefixed with #, ### or #### in custom SQL string).
     * TThe argumentFieldName is the field or argument name used in the SQL.
     * The type is the JDBC type code for the parameter.
     * The typeName is the JDBC type name, this may be required for ARRAY or STRUCT types.
     * The javaType is the java class to return instead of the ARRAY and STRUCT types if a conversion is possible.
     */
    public void setCustomSQLArgumentType(String argumentFieldName, int type, String typeName, Class javaType) {
        ObjectRelationalDatabaseField field = new ObjectRelationalDatabaseField(argumentFieldName);
        field.setSqlType(type);
        field.setSqlTypeName(typeName);
        field.setType(javaType);
        getParameters().add(field);
        getParameterTypes().add(null);
    }
    
    /**
     * PUBLIC:
     * This method should only be used with custom SQL:
     * it sets a type to IN, OUT or INOUT parameter (prefixed with #, ### or #### in custom SQL string).
     * TThe argumentFieldName is the field or argument name used in the SQL.
     * The type is the JDBC type code for the parameter.
     * The typeName is the JDBC type name, this may be required for ARRAY or STRUCT types.
     * The nestedType is a DatabaseField with type information set to match the VARRAYs object types
     */
    public void setCustomSQLArgumentType(String argumentFieldName, int type, String typeName, DatabaseField nestedType) {
        ObjectRelationalDatabaseField field = new ObjectRelationalDatabaseField(argumentFieldName);
        field.setSqlType(type);
        field.setSqlTypeName(typeName);
        field.setNestedTypeField(nestedType);
        getParameters().add(field);
        getParameterTypes().add(null);
    }
    
    /**
     * PUBLIC:
     * This method should only be used with custom SQL:
     * it sets a type to IN, OUT or INOUT parameter (prefixed with #, ### or #### in custom SQL string).
     * TThe argumentFieldName is the field or argument name used in the SQL.
     * The type is the JDBC type code for the parameter.
     * The typeName is the JDBC type name, this may be required for ARRAY or STRUCT types.
     * The javaType is the java class to return instead of the ARRAY and STRUCT types if a conversion is possible.
     * The nestedType is a DatabaseField with type information set to match the VARRAYs object types
     */
    public void setCustomSQLArgumentType(String argumentFieldName, int type, String typeName, Class javaType, DatabaseField nestedType) {
        ObjectRelationalDatabaseField field = new ObjectRelationalDatabaseField(argumentFieldName);
        field.setSqlType(type);
        field.setSqlTypeName(typeName);
        field.setType(javaType);
        field.setNestedTypeField(nestedType);
        getParameters().add(field);
        getParameterTypes().add(null);
    }

    /**
     * Set the SQL string.
     * Warning: Allowing an unverified SQL string to be passed into this 
	 * method makes your application vulnerable to SQL injection attacks. 
     */
    public void setSQLString(String sqlString) {
        setSQLStringInternal(sqlString);
    }

    /**
     * INTERNAL:
     * All values are printed as ? to allow for parameter binding or translation during the execute of the call.
     */
    public void appendTranslationParameter(Writer writer, ParameterExpression expression, DatabasePlatform platform) throws IOException {
        try {
            platform.writeParameterMarker(writer, expression);
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
        getParameters().addElement(expression);
        getParameterTypes().addElement(TRANSLATION);
    }

    /**
     * PUBLIC:
     * This method should only be used with custom SQL:
     * Used for Oracle result sets through procedures.
     * It defines OUT parameter (prefixed with ### in custom SQL string)
     * as a cursor output.
     */
    public void useCustomSQLCursorOutputAsResultSet(String customParameterName) {
        DatabaseField field = new DatabaseField(customParameterName);
        getParameters().add(field);
        getParameterTypes().add(OUT_CURSOR);
        setIsCursorOutputProcedure(true);
    }
}
