// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.platform.xml;

import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

public interface XMLTransformer {
    public String getEncoding();

    public void setEncoding(String encoding);

    public boolean isFormattedOutput();

    public void setFormattedOutput(boolean shouldFormat);

    public boolean isFragment();

    public void setFragment(boolean fragment);

    public String getVersion();

    public void setVersion(String version);

    public void transform(Node sourceNode, OutputStream resultOutputStream) throws XMLPlatformException;

    public void transform(Node sourceNode, ContentHandler resultContentHandler) throws XMLPlatformException;

    public void transform(Node sourceNode, Result result) throws XMLPlatformException;

    public void transform(Node sourceNode, Writer resultWriter) throws XMLPlatformException;

    public void transform(Source source, Result result) throws XMLPlatformException;

    public void transform(Document sourceDocument, Node resultParentNode, URL stylesheet) throws XMLPlatformException;
}