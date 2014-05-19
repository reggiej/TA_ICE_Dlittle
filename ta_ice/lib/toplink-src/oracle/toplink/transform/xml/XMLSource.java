// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.transform.xml;

import java.util.Vector;
import org.w3c.dom.Document;
import oracle.toplink.sessions.DatabaseSession;
import oracle.toplink.sessions.Project;
import oracle.toplink.xml.stream.XMLStreamAccessor;
import oracle.toplink.xml.stream.XMLStreamDatabase;
import oracle.toplink.xml.stream.XMLStreamLogin;
import oracle.toplink.transform.DataSource;

/**
 * <p>
 * <b>Purpose</b>:
 * <p> Build objects to XML format, used by XMLTransformer
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  There is no direct replacement API.
 */
public class XMLSource implements DataSource {
    private Document sourceDocument;

    public XMLSource() {
        super();
    }

    public void setSourceDocument(Document newSourceDocument) {
        sourceDocument = newSourceDocument;
    }

    public Vector buildObjects(Project project, Class type) {
        // Convert the Document into a TopLink compatible XMLStreamDatabase
        XMLStreamDatabase xmlStreamDatabase = new XMLStreamDatabase(sourceDocument, project);

        // Create the login and assign it to the Project
        XMLStreamLogin xmlStreamLogin = new XMLStreamLogin();
        xmlStreamLogin.setXMLStreamDatabase(xmlStreamDatabase);
        xmlStreamLogin.setAccessorClass(XMLStreamAccessor.class);
        project.setLogin(xmlStreamLogin);

        // Read the objects and return the result
        DatabaseSession session = project.createDatabaseSession();
        session.login();
        Vector result = session.readAllObjects(type);
        session.logout();
        return result;
    }
}