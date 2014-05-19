// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.eis.adapters.attunity;

import java.io.*;
import java.util.Properties;
import javax.resource.*;
import javax.resource.cci.*;
import com.attunity.adapter.*;
import oracle.toplink.eis.*;
import oracle.toplink.exceptions.*;

/**
 * Provides the behavoir of instantiating a Attunity Connect ConnectionSpec
 * if a username/password was specified in the login.
 *
 * @author James
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class AttunityConnectionSpec extends EISConnectionSpec {

    /** Attuntiy connection spec properties. */
    public static String SERVER_NAME = "serverName";
    public static String PORT_NUMBER = "portNumber";
    public static String EIS_NAME = "eisName";
    public static String KEEP_ALIVE = "keepAlive";
    public static String WORKSPACE = "workspace";

    /**
     * PUBLIC:
     * Construct a AttunityConnectionSpec with no settings.
     * The connection properties will still need to be set.
     */
    public AttunityConnectionSpec() {
        super();
    }

    /**
     * PUBLIC:
     * Construct a AttunityConnectionSpec with the connectionFactory.
     */
    public AttunityConnectionSpec(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Connect with the specified properties and return the Connection.
     */
    public Connection connectToDataSource(EISAccessor accessor, Properties properties) throws DatabaseException, ValidationException {
        String user = properties.getProperty(USER);
        // Bug 4117441 - Secure programming practices, store password in char[]
        String password = getPasswordFromProperties(properties);
        String serverName = properties.getProperty(SERVER_NAME);
        String portNumber = properties.getProperty(PORT_NUMBER);
        String eisName = properties.getProperty(EIS_NAME);
        String keepAlive = properties.getProperty(KEEP_ALIVE);
        String workspace = properties.getProperty(WORKSPACE);

        // If the name is null it is unmanaged.
        if (getName() == null) {
            try {
                AttuManagedConFactory mcf = new AttuManagedConFactory();
                if (eisName != null) {
                    mcf.setEisName(eisName);
                }
                if (serverName != null) {
                    mcf.setServerName(serverName);
                }
                if (portNumber != null) {
                    mcf.setPortNumber(portNumber);
                }
                if (keepAlive != null) {
                    mcf.setKeepAlive(keepAlive);
                }
                if (workspace != null) {
                    mcf.setWorkspace(workspace);
                }
                if (getLog() != null) {
                    mcf.setLogWriter(new PrintWriter(getLog(), true));
                }
                mcf.setUserName(user);
                mcf.setPassword(password);

                AttuConnectionFactory connectionFactory = (AttuConnectionFactory)mcf.createConnectionFactory();
                if (getLog() != null) {
                    connectionFactory.setLogWriter(new PrintWriter(getLog(), true));
                }
                setConnectionFactory(connectionFactory);
            } catch (ResourceException exception) {
                throw EISException.resourceException(exception, accessor, null);
            }
        }

        return super.connectToDataSource(accessor, properties);
    }
}