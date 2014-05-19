// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.exceptions;

import java.io.*;
import oracle.toplink.internal.helper.JavaPlatform;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.exceptions.i18n.ExceptionMessageGenerator;

/**
 * <p>
 * <b>Purpose</b>: Any exception raised by TopLink should be a subclass of this exception class.
 */
public abstract class TopLinkException extends RuntimeException {
    protected transient AbstractSession session;
    protected Throwable internalException;
    protected static Boolean shouldPrintInternalException = null;
    protected String indentationString;
    protected int errorCode;
    protected static final String CR = System.getProperty("line.separator");
    //Bug#3559280  Added to avoid logging an exception twice
    protected boolean hasBeenLogged;

    /**
     * INTERNAL:
     * Return a new exception.
     */
    public TopLinkException() {
        this("");
    }

    /**
     * INTERNAL:
     * TopLink exception should only be thrown by TopLink.
     */
    public TopLinkException(String theMessage) {
        super(theMessage);
        this.indentationString = "";
        hasBeenLogged = false;
    }

    /**
     * INTERNAL:
     * TopLink exception should only be thrown by TopLink.
     */
    public TopLinkException(String message, Throwable internalException) {
        this(message);
        setInternalException(internalException);
    }

    /**
     * INTERNAL:
     * Convenience method - return a platform-specific line-feed.
     */
    protected static String cr() {
        return oracle.toplink.internal.helper.Helper.cr();
    }

    /**
     * PUBLIC:
     * Return the exception error code.
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * INTERNAL:
     * Used to print things nicely in the testing tool.
     */
    public String getIndentationString() {
        return indentationString;
    }

    /**
     * PUBLIC:
     * Return the internal native exception.
     * TopLink frequently catches Java exceptions and wraps them in its own exception
     * classes to provide more information.
     * The internal exception can still be accessed if required.
     */
    public Throwable getInternalException() {
        return internalException;
    }

    /**
     * PUBLIC:
     * Return the exception error message.
     * TopLink error messages are multi-line so that detail descriptions of the exception are given.
     */
    public String getMessage() {
        StringWriter writer = new StringWriter(100);

        // Avoid printing internal exception error message twice.
        if ((getInternalException() == null) || (!super.getMessage().equals(getInternalException().toString()))) {
            writer.write(cr());
            writer.write(getIndentationString());
            writer.write(ExceptionMessageGenerator.getHeader("DescriptionHeader"));
            writer.write(super.getMessage());
        }

        if (getInternalException() != null) {
            writer.write(cr());
            writer.write(getIndentationString());
            writer.write(ExceptionMessageGenerator.getHeader("InternalExceptionHeader"));
            writer.write(getInternalException().toString());

            if ((getInternalException() instanceof java.lang.reflect.InvocationTargetException) && ((((java.lang.reflect.InvocationTargetException)getInternalException()).getTargetException()) != null)) {
                writer.write(cr());
                writer.write(getIndentationString());
                writer.write(ExceptionMessageGenerator.getHeader("TargetInvocationExceptionHeader"));
                writer.write(((java.lang.reflect.InvocationTargetException)getInternalException()).getTargetException().toString());
            }
        }

        return writer.toString();
    }

    /**
     * PUBLIC:
     * Return the session.
     */
    public AbstractSession getSession() {
        return session;
    }

    /**
     * INTERNAL:
     * Return if this exception has been logged to avoid being logged more than once.
     */
    public boolean hasBeenLogged() {
        return hasBeenLogged;
    }

    /**
     * PUBLIC:
     * Print both the normal and internal stack traces.
     */
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * PUBLIC:
     * Print both the normal and internal stack traces.
     */
    public void printStackTrace(PrintStream outStream) {
        printStackTrace(new PrintWriter(outStream));
    }

    /**
     * PUBLIC:
     * Print both the normal and internal stack traces.
     */
    public void printStackTrace(PrintWriter writer) {
        writer.write(ExceptionMessageGenerator.getHeader("LocalExceptionStackHeader"));
        writer.write(cr());
        super.printStackTrace(writer);

        if ((getInternalException() != null) && shouldPrintInternalException()) {
            writer.write(ExceptionMessageGenerator.getHeader("InternalExceptionStackHeader"));
            writer.write(cr());
            getInternalException().printStackTrace(writer);

            if ((getInternalException() instanceof java.lang.reflect.InvocationTargetException) && ((((java.lang.reflect.InvocationTargetException)getInternalException()).getTargetException()) != null)) {
                writer.write(ExceptionMessageGenerator.getHeader("TargetInvocationExceptionStackHeader"));
                writer.write(cr());
                ((java.lang.reflect.InvocationTargetException)getInternalException()).getTargetException().printStackTrace(writer);
            }
        }
        writer.flush();
    }

    /**
     * INTERNAL:
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * INTERNAL:
     * Set this flag to avoid logging an exception more than once.
     */
    public void setHasBeenLogged(boolean logged) {
        this.hasBeenLogged = logged;
    }

    /**
     * INTERNAL:
     * Used to print things nicely in the testing tool.
     */
    public void setIndentationString(String indentationString) {
        this.indentationString = indentationString;
    }

    /**
     * INTERNAL:
     * Used to specify the internal exception.
     */
    public void setInternalException(Throwable anException) {
        internalException = anException;
        JavaPlatform.setExceptionCause(this, anException);
    }

    /**
     *  INTERNAL:
     */
    public void setSession(AbstractSession session) {
        this.session = session;
    }

    /**
     * PUBLIC:
     * Allows overiding of TopLink's exception chaining detection.
     * @param booleam printException - If printException is true, the TopLink-stored
     * Internal exception will be included in a stack traceor in the exception message of a TopLinkException.
     * If printException is false, the TopLink-stored Internal Exception will not be included
     * in the stack trace or the exception message of TopLinkExceptions
     */
    public static void setShouldPrintInternalException(boolean printException) {
        shouldPrintInternalException = new Boolean(printException);
    }

    /**
     * INTERNAL
     * Check to see if the TopLink-stored internal exception should be printed in this
     * a TopLinkException's stack trace.  This method will check the static ShouldPrintInternalException
     * variable and if it is not set, estimate based on the JDK version used.
     */
    public static boolean shouldPrintInternalException() {
        if (shouldPrintInternalException == null) {
            shouldPrintInternalException = new Boolean(JavaPlatform.shouldPrintInternalException());
        }
        return shouldPrintInternalException.booleanValue();
    }

    /**
     * INTERNAL:
     */
    public String toString() {
        return getIndentationString() + ExceptionMessageGenerator.getHeader("ExceptionHeader") + getErrorCode() + "] (" + oracle.toplink.sessions.DatabaseLogin.getVersion() + "): " + getClass().getName() + getMessage();
    }
}