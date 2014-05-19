// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.platform.xml;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import javax.xml.transform.Source;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

public interface XMLParser {
    public static final int NONVALIDATING = 0;
    public static final int DTD_VALIDATION = 2;
    public static final int SCHEMA_VALIDATION = 3;

    public void setNamespaceAware(boolean isNamespaceAware);

    public void setWhitespacePreserving(boolean isWhitespacePreserving);

    public int getValidationMode();

    public void setValidationMode(int validationMode);

    public EntityResolver getEntityResolver();

    public void setEntityResolver(EntityResolver entityResolver);

    public ErrorHandler getErrorHandler();

    public void setErrorHandler(ErrorHandler errorHandler);

    public void setXMLSchema(URL url) throws XMLPlatformException;

    public void setXMLSchemas(Object[] schemas) throws XMLPlatformException;

    public Document parse(InputSource inputSource) throws XMLPlatformException;

    public Document parse(File file) throws XMLPlatformException;

    public Document parse(InputStream inputStream) throws XMLPlatformException;

    public Document parse(Reader reader) throws XMLPlatformException;

    public Document parse(Source source) throws XMLPlatformException;

    public Document parse(URL url) throws XMLPlatformException;
}