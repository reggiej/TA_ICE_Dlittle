// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.exceptions;

import oracle.toplink.exceptions.i18n.ExceptionMessageGenerator;

public class JMSProcessingException extends oracle.toplink.exceptions.TopLinkException implements java.io.Serializable {
    public static final int DEFAULT = 18001;
    public static final int NO_TOPIC_SET = 18002;
    public static final int MDB_ERROR_LOOKUP_SESSION_NAME_ENV = 18003;
    public static final int MDB_FOUND_NO_SESSION = 18004;

    public JMSProcessingException(String message, Throwable exception) {
        super(message);
        internalException = exception;

    }

    public static JMSProcessingException buildDefault(Throwable exception) {
        Object[] args = {  };
        JMSProcessingException ex = new JMSProcessingException(ExceptionMessageGenerator.buildMessage(JMSProcessingException.class, DEFAULT, args), exception);
        ex.setErrorCode(DEFAULT);
        return ex;
    }

    public static JMSProcessingException noTopicSet(Throwable exception) {
        Object[] args = {  };
        JMSProcessingException ex = new JMSProcessingException(ExceptionMessageGenerator.buildMessage(JMSProcessingException.class, NO_TOPIC_SET, args), exception);
        ex.setErrorCode(NO_TOPIC_SET);
        return ex;
    }

    public static JMSProcessingException errorLookupSessionNameInCtx(Throwable exception) {
       	Object[] args = { };
       	JMSProcessingException ex = new JMSProcessingException(ExceptionMessageGenerator.buildMessage(JMSProcessingException.class,MDB_ERROR_LOOKUP_SESSION_NAME_ENV,args), exception);
    	ex.setErrorCode(MDB_ERROR_LOOKUP_SESSION_NAME_ENV);
    	return ex;
    }
    public static JMSProcessingException mdbFoundNoSession() {
       	Object[] args = { };
       	JMSProcessingException ex = new JMSProcessingException(ExceptionMessageGenerator.buildMessage(JMSProcessingException.class, MDB_FOUND_NO_SESSION,args), null);
    	ex.setErrorCode(MDB_FOUND_NO_SESSION);
    	return ex;
    }    
}
