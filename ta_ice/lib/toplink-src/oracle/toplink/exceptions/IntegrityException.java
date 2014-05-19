// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.exceptions;

import java.io.*;
import java.util.*;
import oracle.toplink.exceptions.i18n.ExceptionMessageGenerator;

/**
 *    <p><b>Purpose</b>: IntegrityExceptions is used to throw all the Descriptors exceptions.
 *
 */
public class IntegrityException extends ValidationException {
    protected IntegrityChecker integrityChecker;

    /**
     * INTERNAL:
     * IntegrityExceptions is used to throw all the descriptor exceptions.
     */
    public IntegrityException() {
        super();
    }

    /**
     * INTERNAL:
     * To throw all the descriptor exceptions.
     */
    public IntegrityException(IntegrityChecker integrityChecker) {
        super();
        this.integrityChecker = integrityChecker;
    }

    /**
     * PUBLIC:
     * Return Integrity Checker.
     */
    public IntegrityChecker getIntegrityChecker() {
        return integrityChecker;
    }

    /**
     * PUBLIC:
     * This method is used to print out all the descriptor exceptions.
     */
    public String getMessage() {
        String cr = oracle.toplink.internal.helper.Helper.cr();
        java.io.StringWriter swriter = new java.io.StringWriter();
        java.io.PrintWriter writer = new java.io.PrintWriter(swriter);
        writer.println(cr + ExceptionMessageGenerator.getHeader("DescriptorExceptionsHeader"));
        writer.println("---------------------------------------------------------");
        for (Enumeration enumtr = getIntegrityChecker().getCaughtExceptions().elements();
                 enumtr.hasMoreElements();) {
            Exception e = (Exception)enumtr.nextElement();
            if (e instanceof DescriptorException) {
                writer.println(cr + e);
            }
        }

        if (getIntegrityChecker().hasRuntimeExceptions()) {
            writer.println(cr + ExceptionMessageGenerator.getHeader("RuntimeExceptionsHeader"));
            writer.println("---------------------------------------------------------");
            for (Enumeration enumtr = getIntegrityChecker().getCaughtExceptions().elements();
                     enumtr.hasMoreElements();) {
                Exception e = (Exception)enumtr.nextElement();
                if (!(e instanceof DescriptorException)) {
                    writer.println(cr + e);
                }
            }
        }

        writer.flush();
        swriter.flush();
        return swriter.toString();
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
        super.printStackTrace(writer);
        String cr = oracle.toplink.internal.helper.Helper.cr();
        writer.println(cr + ExceptionMessageGenerator.getHeader("DescriptorExceptionsHeader"));
        writer.println("---------------------------------------------------------");
        for (Enumeration enumtr = getIntegrityChecker().getCaughtExceptions().elements();
                 enumtr.hasMoreElements();) {
            Exception e = (Exception)enumtr.nextElement();
            if (e instanceof DescriptorException) {
                writer.println(cr);
                e.printStackTrace(writer);
            }
        }

        if (getIntegrityChecker().hasRuntimeExceptions()) {
            writer.println(cr + ExceptionMessageGenerator.getHeader("RuntimeExceptionsHeader"));
            writer.println("---------------------------------------------------------");
            for (Enumeration enumtr = getIntegrityChecker().getCaughtExceptions().elements();
                     enumtr.hasMoreElements();) {
                Exception e = (Exception)enumtr.nextElement();
                if (!(e instanceof DescriptorException)) {
                    writer.println(cr);
                    e.printStackTrace(writer);
                }
            }
        }

        writer.flush();
    }
}