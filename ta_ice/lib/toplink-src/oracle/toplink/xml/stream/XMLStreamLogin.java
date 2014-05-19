// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.xml.stream;


/**
 * INTERNAL:
 * <p>
 * <b>Purpose</b>:
 * <p> New login for writing SDK output in to a single stream
 *
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.ox}
 */
import oracle.toplink.internal.databaseaccess.Accessor;
import oracle.toplink.internal.helper.Helper;
import oracle.toplink.xml.XMLLogin;

public class XMLStreamLogin extends XMLLogin {
    private XMLStreamDatabase xmlStreamDatabase;

    public XMLStreamLogin() {
        super();
        this.xmlStreamDatabase = new XMLStreamDatabase();
    }

    public XMLStreamDatabase getXMLStreamDatabase() {
        return xmlStreamDatabase;
    }

    public void setXMLStreamDatabase(XMLStreamDatabase newXMLStreamDatabase) {
        xmlStreamDatabase = newXMLStreamDatabase;
    }

    /**
     * Set the class of the accessor to be built.
     */
    public void setAccessorClass(Class accessorClass) {
        if (!Helper.classImplementsInterface(accessorClass, XMLStreamAccessor.class)) {
            throw this.invalidAccessClass(XMLStreamAccessor.class, accessorClass);
        }
        super.setAccessorClass(accessorClass);
    }

    public Accessor buildAccessor() {
        XMLStreamAccessor xmlStreamAccessor = (XMLStreamAccessor)super.buildAccessor();
        xmlStreamAccessor.setXMLStreamDatabase(xmlStreamDatabase);
        return xmlStreamAccessor;
    }
}