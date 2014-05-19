// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.exceptions;


/**
 *     Exception handler can catch errors that occur on queries or during database access.
 *    The exception handler has the option of re-throwing the exception,throwing a different
 *    exception or re-trying the query or database operation.
 */
public interface ExceptionHandler {

    /**
     *    To re-throwing the exception,throwing a different
     *    exception or re-trying the query or database operation.
     */
    Object handleException(RuntimeException exception);
}