// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.transform.xml;

import java.util.Vector;
import java.util.Enumeration;
import org.w3c.dom.Document;
import oracle.toplink.sessions.DatabaseSession;
import oracle.toplink.sessions.UnitOfWork;
import oracle.toplink.sessions.Project;
import oracle.toplink.xml.stream.XMLStreamAccessor;
import oracle.toplink.xml.stream.XMLStreamDatabase;
import oracle.toplink.xml.stream.XMLStreamLogin;
import oracle.toplink.transform.DataResult;

/**
 * <p>
 * <b>Purpose</b>:
 * <p> Write objects to XML format, used by XMLTransformer
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  There is no direct replacement API.
 */
public class XMLResult implements DataResult {
    private Document resultDocument;

    public XMLResult() {
        super();
    }

    public Document getResultDocument() {
        return resultDocument;
    }

    public void storeObjects(Project project, Vector objects) {
        // Convert the Document into a TopLink compatible XMLStreamDatabase
        XMLStreamDatabase xmlStreamDatabase = new XMLStreamDatabase();

        // Create the login and assign it to the Project
        XMLStreamLogin xmlStreamLogin = new XMLStreamLogin();
        xmlStreamLogin.setXMLStreamDatabase(xmlStreamDatabase);
        xmlStreamLogin.setAccessorClass(XMLStreamAccessor.class);
        project.setLogin(xmlStreamLogin);

        DatabaseSession session = project.createDatabaseSession();
        session.login();

        // write objects to xml database
        Enumeration enumtr = objects.elements();
        if (enumtr.hasMoreElements()) {
            UnitOfWork uow = session.acquireUnitOfWork();
            do {
                uow.registerNewObject(enumtr.nextElement());
            } while (enumtr.hasMoreElements());

            uow.commit();
        }
        session.logout();

        // Set the resultDocument from xml database
        resultDocument = xmlStreamDatabase.getDocument();
    }
}