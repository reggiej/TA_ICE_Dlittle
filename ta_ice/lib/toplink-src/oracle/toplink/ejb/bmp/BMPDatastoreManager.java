// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ejb.bmp;

import java.util.Hashtable;
import oracle.toplink.ejb.*;
import oracle.toplink.tools.sessionconfiguration.*;

/**
 * <p>
 * <b>Purpose</b>: Global Datastore collection location used in our suggested BMP implementation.</p>
 *
 * <p><b>Description</b>: This allows for a global location for datastore objects which can
 * be accessed globally from EntityBeans. </p>
 *
 * <p><b>Responsibilities</b>:
 * <ul>
 * <li> Store global datastores per class.
 * </ul></p>
 *
 * @author Gordon Yorke
 * @since TOPLink/Java 3.0
 */
public class BMPDatastoreManager {
    protected static BMPDatastoreManager manager;
    protected Hashtable datastoreCollection;

    /**
     * Creates a new BMPDatastoreManager.
     */
    protected BMPDatastoreManager() {
        super();
    }

    /**
     * PUBLIC:
     * This method returns a dataStore instance for a particular sesssion and
     * bean class.  This will use the default session name "default"
     *
     * @param classType java.lang.String the bean class
     */
    public EJBDataStore getDatastore(Class classType) {
        return getDatastore(null, "default", classType);
    }

    /**
     * PUBLIC:
     * This method returns a dataStore instance for a particular sesssion and
     * bean class.
     *
     * @param sessionName java.lang.String The registered name of the session
     * @param classType java.lang.String the bean class
     */
    public EJBDataStore getDatastore(XMLSessionConfigLoader xmlSessionConfigloader, String sessionName, Class classType) {
        StringBuffer buffer = new StringBuffer(sessionName);
        buffer.append('_');
        buffer.append(classType.toString());
        EJBDataStore datastore = (EJBDataStore)getManager().getDatastoreCollection().get(buffer.toString());
        if (datastore == null) {
            datastore = new EJBDataStore();
            datastore.setXMLSessionConfigLoader(xmlSessionConfigloader);
            datastore.setSessionName(sessionName);
            datastore.setBeanClass(classType);
            getManager().getDatastoreCollection().put(buffer.toString(), datastore);
        }
        return datastore;
    }

    /**
     * PUBLIC:
     * Return the reference to the collection storeing the datastore objects
     */
    protected Hashtable getDatastoreCollection() {
        if (datastoreCollection == null) {
            datastoreCollection = new Hashtable(10);
        }
        return datastoreCollection;
    }

    /**
     * PUBLIC:
     * Return the singleton datastore manager.
     * This allows global access to datastores.
     */
    public static BMPDatastoreManager getManager() {
        if (manager == null) {
            manager = new BMPDatastoreManager();
        }
        return manager;
    }
}