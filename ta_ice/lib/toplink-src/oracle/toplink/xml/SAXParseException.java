// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.xml;


/**
 * This interface allows us indirect access to the
 * <code>org.xml.sax.SAXParseException</code> class.
 *
 * @see org.xml.sax.SAXParseException
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.ox}
 */
public interface SAXParseException {
    // methods for org.xml.sax.SAXParseException:

    /**
     * @see org.xml.sax.SAXParseException#getColumnNumber()
     */
    int getColumnNumber();

    /**
     * @see org.xml.sax.SAXParseException#getLineNumber()
     */
    int getLineNumber();

    /**
     * @see org.xml.sax.SAXParseException#getPublicId()
     */
    String getPublicId();

    /**
     * @see org.xml.sax.SAXParseException#getSystemId()
     */
    String getSystemId();

    // methods for org.xml.sax.SAXException:

    /**
     * @see org.xml.sax.SAXException#getException()
     */
    Exception getException();

    // methods for java.lang.Throwable:

    /**
     * @see java.lang.Throwable#fillInStackTrace()
     */
    Throwable fillInStackTrace();

    /**
     * @see java.lang.Throwable#getLocalizedMessage()
     */
    String getLocalizedMessage();

    /**
     * @see java.lang.Throwable#getMessage()
     */
    String getMessage();

    /**
     * @see java.lang.Throwable#printStackTrace()
     */
    void printStackTrace();

    /**
     * @see java.lang.Throwable#printStackTrace(java.io.PrintStream)
     */
    void printStackTrace(java.io.PrintStream s);

    /**
     * @see java.lang.Throwable#printStackTrace(java.io.PrintWriter)
     */
    void printStackTrace(java.io.PrintWriter s);
}