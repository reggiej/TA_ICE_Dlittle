// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.exceptions;

import java.io.StringWriter;
import java.sql.SQLException;
import oracle.toplink.queryframework.*;
import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.exceptions.i18n.ExceptionMessageGenerator;
import oracle.toplink.sessions.Record;

/**
 * <P><B>Purpose</B>:
 * Wrapper for any database exception that occurred through TopLink.
 */
public class DatabaseException extends TopLinkException {
    protected SQLException exception;
    protected transient Call call;
    protected transient DatabaseQuery query;
    protected transient AbstractRecord queryArguments;
    protected transient Accessor accessor;
    protected boolean isCommunicationFailure;
    public static final int SQL_EXCEPTION = 4002;
    public static final int CONFIGURATION_ERROR_CLASS_NOT_FOUND = 4003;
    public static final int DATABASE_ACCESSOR_NOT_CONNECTED = 4005;
    public static final int ERROR_READING_BLOB_DATA = 4006;
    public static final int COULD_NOT_CONVERT_OBJECT_TYPE = 4007;
    public static final int LOGOUT_WHILE_TRANSACTION_IN_PROGRESS = 4008;
    public static final int SEQUENCE_TABLE_INFORMATION_NOT_COMPLETE = 4009;
    public static final int ERROR_PREALLOCATING_SEQUENCE_NUMBERS = 4011;
    public static final int CANNOT_REGISTER_SYNCHRONIZATIONLISTENER_FOR_UNITOFWORK = 4014;
    public static final int SYNCHRONIZED_UNITOFWORK_DOES_NOT_SUPPORT_COMMITANDRESUME = 4015;
    public static final int CONFIGURATION_ERROR_NEW_INSTANCE_INSTANTIATION_EXCEPTION = 4016;
    public static final int CONFIGURATION_ERROR_NEW_INSTANCE_ILLEGAL_ACCESS_EXCEPTION = 4017;
    public static final int TRANSACTION_MANAGER_NOT_SET_FOR_JTS_DRIVER = 4018;
    public static final int ERROR_RETRIEVE_DB_METADATA_THROUGH_JDBC_CONNECTION = 4019;
    public static final int COULD_NOT_FIND_MATCHED_DATABASE_FIELD_FOR_SPECIFIED_OPTOMISTICLOCKING_FIELDS = 4020;
    public static final int UNABLE_TO_ACQUIRE_CONNECTION_FROM_DRIVER = 4021;

    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by the TopLink code.
     */
    protected DatabaseException(String message) {
        super(message);
    }

    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by the TopLink code.
     */
    protected DatabaseException(SQLException exception) {
        super(exception.toString(), exception);
    }

    public static DatabaseException cannotRegisterSynchronizatonListenerForUnitOfWork(Exception e) {
        Object[] args = { e };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, CANNOT_REGISTER_SYNCHRONIZATIONLISTENER_FOR_UNITOFWORK, args));
        databaseException.setErrorCode(CANNOT_REGISTER_SYNCHRONIZATIONLISTENER_FOR_UNITOFWORK);
        databaseException.setInternalException(e);
        return databaseException;
    }

    public static DatabaseException configurationErrorClassNotFound(String className) {
        Object[] args = { className };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, CONFIGURATION_ERROR_CLASS_NOT_FOUND, args));
        databaseException.setErrorCode(CONFIGURATION_ERROR_CLASS_NOT_FOUND);
        return databaseException;
    }

    public static DatabaseException configurationErrorNewInstanceIllegalAccessException(IllegalAccessException exception, Class javaClass) {
        Object[] args = { javaClass };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, CONFIGURATION_ERROR_NEW_INSTANCE_ILLEGAL_ACCESS_EXCEPTION, args));
        databaseException.setErrorCode(CONFIGURATION_ERROR_NEW_INSTANCE_ILLEGAL_ACCESS_EXCEPTION);
        databaseException.setInternalException(exception);
        return databaseException;
    }

    public static DatabaseException configurationErrorNewInstanceInstantiationException(InstantiationException exception, Class javaClass) {
        Object[] args = { javaClass };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, CONFIGURATION_ERROR_NEW_INSTANCE_INSTANTIATION_EXCEPTION, args));
        databaseException.setErrorCode(CONFIGURATION_ERROR_NEW_INSTANCE_INSTANTIATION_EXCEPTION);
        databaseException.setInternalException(exception);
        return databaseException;
    }

    public static DatabaseException couldNotConvertObjectType(int type) {
        Object[] args = { CR, new Integer(type) };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, COULD_NOT_CONVERT_OBJECT_TYPE, args));
        databaseException.setErrorCode(COULD_NOT_CONVERT_OBJECT_TYPE);
        return databaseException;
    }

    public static DatabaseException databaseAccessorNotConnected() {
        Object[] args = {  };
        String message = oracle.toplink.exceptions.i18n.ExceptionMessageGenerator.buildMessage(DatabaseException.class, DATABASE_ACCESSOR_NOT_CONNECTED, args);
        DatabaseException databaseException = new DatabaseException(message);
        databaseException.setErrorCode(DATABASE_ACCESSOR_NOT_CONNECTED);
        return databaseException;
    }

    public static DatabaseException databaseAccessorNotConnected(DatabaseAccessor databaseAccessor) {
        Object[] args = {  };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, DATABASE_ACCESSOR_NOT_CONNECTED, args));
        databaseException.setErrorCode(DATABASE_ACCESSOR_NOT_CONNECTED);
        databaseException.setAccessor(databaseAccessor);
        return databaseException;
    }

    public static DatabaseException errorPreallocatingSequenceNumbers() {
        Object[] args = {  };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, ERROR_PREALLOCATING_SEQUENCE_NUMBERS, args));
        databaseException.setErrorCode(ERROR_PREALLOCATING_SEQUENCE_NUMBERS);
        return databaseException;
    }

    public static DatabaseException errorReadingBlobData() {
        Object[] args = {  };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, ERROR_READING_BLOB_DATA, args));
        databaseException.setErrorCode(ERROR_READING_BLOB_DATA);
        return databaseException;
    }
    
    public static DatabaseException specifiedLockingFieldsNotFoundInDatabase(String lockingFieldName) {
        Object[] args = { lockingFieldName };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, COULD_NOT_FIND_MATCHED_DATABASE_FIELD_FOR_SPECIFIED_OPTOMISTICLOCKING_FIELDS, args));
        databaseException.setErrorCode(COULD_NOT_FIND_MATCHED_DATABASE_FIELD_FOR_SPECIFIED_OPTOMISTICLOCKING_FIELDS);
        return databaseException;
    }


    /**
     *    PUBLIC:
     *    Return the accessor.
     */
    public Accessor getAccessor() {
        return accessor;
    }

    /**
     * PUBLIC:
     * This is the database error number.
     * Since it is possible to have no internal exception the errorCode will be zero in this case.
     */
    public int getDatabaseErrorCode() {
        if (getInternalException() == null) {
            return super.getErrorCode();
        }
        return ((SQLException)getInternalException()).getErrorCode();
    }

    /**
     * PUBLIC:
     * This is the database error message.
     */
    public String getMessage() {
        if (getInternalException() == null) {
            return super.getMessage();
        } else {
            StringWriter writer = new StringWriter();
            writer.write(super.getMessage());
            writer.write(cr());
            writer.write(getIndentationString());
            writer.write(ExceptionMessageGenerator.getHeader("ErrorCodeHeader"));
            if (getInternalException() instanceof SQLException) {
                writer.write(Integer.toString(((SQLException)getInternalException()).getErrorCode()));
            } else {
                writer.write("000");
            }
            if (getCall() != null) {
                writer.write(cr());
                writer.write(getIndentationString());
                writer.write(ExceptionMessageGenerator.getHeader("CallHeader"));
                if (getAccessor() != null) {
                    writer.write(getCall().getLogString(getAccessor()));
                } else {
                    writer.write(getCall().toString());
                }
            }
            if (getQuery() != null) {
                writer.write(cr());
                writer.write(getIndentationString());
                writer.write(ExceptionMessageGenerator.getHeader("QueryHeader"));
                try {
                    writer.write(getQuery().toString());
                } catch (RuntimeException badTooString) {
                }
            }
            return writer.toString();
        }
    }

    /**
     *    PUBLIC:
     *    This method returns the databaseQuery.
     *    DatabaseQuery is a visible class to the TopLink user.
     *    Users create an appropriate query by creating an instance
     *    of a concrete subclasses of DatabaseQuery.
     */
    public DatabaseQuery getQuery() {
        return query;
    }

    /**
     *    PUBLIC:
     *    Return the call that caused the exception.
     */
    public Call getCall() {
        return call;
    }

    /**
     *    INTERNAL:
     *    Set the call that caused the exception.
     */
    public void setCall(Call call) {
        this.call = call;
    }

    /**
     * PUBLIC:
     * Return the query argements used in the original query when exception is thrown
     */
    public Record getQueryArgumentsRecord() {
        return queryArguments;
    }

    public static DatabaseException logoutWhileTransactionInProgress() {
        Object[] args = {  };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, LOGOUT_WHILE_TRANSACTION_IN_PROGRESS, args));
        databaseException.setErrorCode(LOGOUT_WHILE_TRANSACTION_IN_PROGRESS);
        return databaseException;
    }

    public static DatabaseException sequenceTableInformationNotComplete() {
        Object[] args = {  };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, SEQUENCE_TABLE_INFORMATION_NOT_COMPLETE, args));
        databaseException.setErrorCode(SEQUENCE_TABLE_INFORMATION_NOT_COMPLETE);
        return databaseException;
    }

    /**
     *    INTERNAL:
     *  Set the Accessor.
     */
    public void setAccessor(Accessor accessor) {
        this.accessor = accessor;
    }

    /**
     *    PUBLIC:
     *    This method set the databaseQuery.
     *    DatabaseQuery is a visible class to the TopLink user.
     *    Users create an appropriate query by creating an instance
     *    of a concrete subclasses of DatabaseQuery.
     */
    public void setQuery(DatabaseQuery query) {
        this.query = query;
    }

    /**
     * PUBLIC:
     * Set the query argements used in the original query when exception is thrown
     */
    public void setQueryArguments(AbstractRecord queryArguments) {
        this.queryArguments = queryArguments;
    }

    public static DatabaseException sqlException(SQLException exception) {
        DatabaseException databaseException = new DatabaseException(exception);
        databaseException.setErrorCode(SQL_EXCEPTION);
        return databaseException;
    }

    public static DatabaseException sqlException(SQLException exception, AbstractSession session) {
        if (session == null) {
            return sqlException(exception);
        } else {
            return sqlException(exception, session.getAccessor(), session, false);
        }
    }
    
    public static DatabaseException sqlException(SQLException exception, Accessor accessor, AbstractSession session, boolean isCommunicationFailure) {
        DatabaseException databaseException = new DatabaseException(exception);
        databaseException.setErrorCode(SQL_EXCEPTION);
        databaseException.setAccessor(accessor);
        databaseException.setSession(session);
        databaseException.setIsCommunicationFailure(isCommunicationFailure);
        return databaseException;
    }

    public static DatabaseException sqlException(SQLException exception, Call call, Accessor accessor, AbstractSession session, boolean isCommunicationFailure) {
        DatabaseException databaseException = new DatabaseException(exception);
        databaseException.setErrorCode(SQL_EXCEPTION);
        databaseException.setAccessor(accessor);
        databaseException.setCall(call);
        databaseException.setIsCommunicationFailure(isCommunicationFailure);
        return databaseException;
    }

    public static DatabaseException synchronizedUnitOfWorkDoesNotSupportCommitAndResume() {
        Object[] args = {  };

        String message = oracle.toplink.exceptions.i18n.ExceptionMessageGenerator.buildMessage(DatabaseException.class, SYNCHRONIZED_UNITOFWORK_DOES_NOT_SUPPORT_COMMITANDRESUME, args);
        DatabaseException databaseException = new DatabaseException(message);
        databaseException.setErrorCode(SYNCHRONIZED_UNITOFWORK_DOES_NOT_SUPPORT_COMMITANDRESUME);
        return databaseException;
    }

    public static DatabaseException transactionManagerNotSetForJTSDriver() {
        Object[] args = {  };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, TRANSACTION_MANAGER_NOT_SET_FOR_JTS_DRIVER, args));
        databaseException.setErrorCode(TRANSACTION_MANAGER_NOT_SET_FOR_JTS_DRIVER);
        return databaseException;
    }
    
    public static DatabaseException errorRetrieveDbMetadataThroughJDBCConnection() {
        Object[] args = {  };

        DatabaseException databaseException = new DatabaseException(ExceptionMessageGenerator.buildMessage(DatabaseException.class, ERROR_RETRIEVE_DB_METADATA_THROUGH_JDBC_CONNECTION, args));
        databaseException.setErrorCode(ERROR_RETRIEVE_DB_METADATA_THROUGH_JDBC_CONNECTION);
        return databaseException;
    }

    /**
	 * The connection returned from this driver was null, the driver may be
	 * missing(using the default) or the wrong one for the database.
	 * 
	 * @param driver
	 * @return
	 */
	public static DatabaseException unableToAcquireConnectionFromDriverException(
			String driver, String user, String url) {
		Object[] args = { driver, user, url };
		DatabaseException databaseException = new DatabaseException(
				ExceptionMessageGenerator.buildMessage(DatabaseException.class,
						UNABLE_TO_ACQUIRE_CONNECTION_FROM_DRIVER, args));
		databaseException.setErrorCode(UNABLE_TO_ACQUIRE_CONNECTION_FROM_DRIVER);
		return databaseException;
	}

	/**
	 * The connection returned from this driver was null, the driver may be
	 * missing(using the default) or the wrong one for the database.
	 * 
	 * @param exception
	 * @param driver
	 * @return
	 */
	public static DatabaseException unableToAcquireConnectionFromDriverException(SQLException exception, 
			String driver, String user, String url) {
		DatabaseException databaseException = unableToAcquireConnectionFromDriverException(driver, user, url);
		databaseException.setInternalException(exception);
		return databaseException;
	}

    /**
     * PUBLIC:
     * @return true if the DatabaseException was created because of a communication error with the database.
     */
    public boolean isCommunicationFailure() {
        return isCommunicationFailure;
    }

    /**
     * INTERNAL:
     * @param isConnectionError
     */
    public void setIsCommunicationFailure(boolean isCommunicationFailure) {
        this.isCommunicationFailure = isCommunicationFailure;
    }
}
