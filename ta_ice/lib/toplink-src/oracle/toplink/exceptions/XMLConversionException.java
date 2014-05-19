// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.exceptions;

import oracle.toplink.exceptions.i18n.ExceptionMessageGenerator;

/**
 * <P><B>Purpose</B>: XML conversion exceptions are raised for any problem when
 * converting Java types to/from XML.</P>
 */
public class XMLConversionException extends TopLinkException {
    // Error code range for this exception is 25501 - 26000.
    public static final int ERROR_CREATE_URL = 25501;
    public static final int INCORRECT_G_DAY_FORMAT = 25502;
    public static final int INCORRECT_G_MONTH_FORMAT = 25503;
    public static final int INCORRECT_G_MONTH_DAY_FORMAT = 25504;
    public static final int INCORRECT_G_YEAR_FORMAT = 25505;
    public static final int INCORRECT_G_YEAR_MONTH_FORMAT = 25506;   
    public static final int INCORRECT_TIMESTAMP_DATE_TIME_FORMAT = 25507;
    public static final int INCORRECT_TIMESTAMP_TIME_FORMAT = 25508;
    
    public XMLConversionException(String message) {
        super(message);
    }

    protected XMLConversionException(String message, Exception internalException) {
        super(message, internalException);
    }

    public static XMLConversionException errorCreateURLForFile(String fileName, Exception internalException) {
        Object[] args = { fileName };

        XMLConversionException exception = new XMLConversionException(ExceptionMessageGenerator.buildMessage(XMLConversionException.class, ERROR_CREATE_URL, args), internalException);
        exception.setErrorCode(ERROR_CREATE_URL);
        return exception;
    }

    public static XMLConversionException incorrectGDayFormat(String dateString) {
        Object[] args = { dateString };
        String message = ExceptionMessageGenerator.buildMessage(XMLConversionException.class, INCORRECT_G_DAY_FORMAT, args);
        XMLConversionException xmlConversionException = new XMLConversionException(message);
        xmlConversionException.setErrorCode(INCORRECT_G_DAY_FORMAT);
        return xmlConversionException;
    }
    
    public static XMLConversionException incorrectGMonthFormat(String dateString) {
        Object[] args = { dateString };
        String message = ExceptionMessageGenerator.buildMessage(XMLConversionException.class, INCORRECT_G_MONTH_FORMAT, args);
        XMLConversionException xmlConversionException = new XMLConversionException(message);
        xmlConversionException.setErrorCode(INCORRECT_G_MONTH_FORMAT);
        return xmlConversionException;
    }

    public static XMLConversionException incorrectGMonthDayFormat(String dateString) {
        Object[] args = { dateString };
        String message = ExceptionMessageGenerator.buildMessage(XMLConversionException.class, INCORRECT_G_MONTH_DAY_FORMAT, args);
        XMLConversionException xmlConversionException = new XMLConversionException(message);
        xmlConversionException.setErrorCode(INCORRECT_G_MONTH_DAY_FORMAT);
        return xmlConversionException;
    }

    public static XMLConversionException incorrectGYearFormat(String dateString) {
        Object[] args = { dateString };
        String message = ExceptionMessageGenerator.buildMessage(XMLConversionException.class, INCORRECT_G_YEAR_FORMAT, args);
        XMLConversionException xmlConversionException = new XMLConversionException(message);
        xmlConversionException.setErrorCode(INCORRECT_G_YEAR_FORMAT);
        return xmlConversionException;
    }

    public static XMLConversionException incorrectGYearMonthFormat(String dateString) {
        Object[] args = { dateString };
        String message = ExceptionMessageGenerator.buildMessage(XMLConversionException.class, INCORRECT_G_YEAR_MONTH_FORMAT, args);
        XMLConversionException xmlConversionException = new XMLConversionException(message);
        xmlConversionException.setErrorCode(INCORRECT_G_YEAR_MONTH_FORMAT);
        return xmlConversionException;
    }
    
    public static XMLConversionException incorrectTimestampDateTimeFormat(String dateString) {
        Object[] args = { dateString };
        String message = ExceptionMessageGenerator.buildMessage(XMLConversionException.class, INCORRECT_TIMESTAMP_DATE_TIME_FORMAT, args);
        XMLConversionException xmlConversionException = new XMLConversionException(message);
        xmlConversionException.setErrorCode(INCORRECT_TIMESTAMP_DATE_TIME_FORMAT);
        return xmlConversionException;
    }

    public static XMLConversionException incorrectTimestampTimeFormat(String dateString) {
        Object[] args = { dateString };
        String message = ExceptionMessageGenerator.buildMessage(XMLConversionException.class, INCORRECT_TIMESTAMP_TIME_FORMAT, args);
        XMLConversionException xmlConversionException = new XMLConversionException(message);
        xmlConversionException.setErrorCode(INCORRECT_TIMESTAMP_TIME_FORMAT);
        return xmlConversionException;
    }
    
}
