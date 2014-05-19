// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.eis.adapters.blackbox;

import java.util.Properties;
import javax.naming.*;
import javax.resource.*;
import javax.resource.cci.*;
import com.sun.connector.cciblackbox.*;
import oracle.toplink.eis.*;
import oracle.toplink.exceptions.*;

/**
 * Provides the behavoir of instantiating a Sun blackbox ConnectionSpec
 * if a username/password was specified in the login.
 *
 * @author James
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class BlackboxConnectionSpec extends EISConnectionSpec {

    /** Blackbox connection spec properties. */
    public static String CONNECTION_URL = "connectionURL";

    /**
     * PUBLIC:
     * Construct a BlackboxConnectionSpec with no settings.
     * The ConnectionFactory name will still need to be set.
     */
    public BlackboxConnectionSpec() {
        super();
    }

    /**
     * PUBLIC:
     * Construct a BlackboxConnectionSpec with the specified settings.
     */
    public BlackboxConnectionSpec(Context context, String name) {
        super(context, name);
    }

    /**
     * PUBLIC:
     * Construct a BlackboxConnectionSpec with the specified settings.
     */
    public BlackboxConnectionSpec(String name) {
        super(name);
    }

    /**
     * PUBLIC:
     * Construct a BlackboxConnectionSpec with the specified settings.
     */
    public BlackboxConnectionSpec(String name, ConnectionSpec connectionSpec) throws ValidationException {
        this(name);
        setConnectionSpec(connectionSpec);
    }

    /**
     * PUBLIC:
     * Construct a BlackboxConnectionSpec with the specified settings.
     */
    public BlackboxConnectionSpec(Context context, Name name) {
        super(context, name);
    }

    /**
     * PUBLIC:
     * Construct a BlackboxConnectionSpec with the specified settings.
     */
    public BlackboxConnectionSpec(Name name) {
        super(name);
    }

    /**
     * PUBLIC:
     * Construct a BlackboxConnectionSpec with the specified settings.
     */
    public BlackboxConnectionSpec(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Connect with the specified properties and return the Connection.
     */
    public Connection connectToDataSource(EISAccessor accessor, Properties properties) throws DatabaseException, ValidationException {
        String user = properties.getProperty(USER);
        // Bug 4117441 - Secure programming practices, store password in char[]
        String password = getPasswordFromProperties(properties);
        if ((user != null) && (user.length() > 0)) {
            setConnectionSpec(new CciConnectionSpec(user, password));
        }

        // If the name is null it is unmanaged.
        if (getName() == null) {
            try {
                String databaseURL = properties.getProperty(CONNECTION_URL);
                CciLocalTxManagedConnectionFactory mcf = new CciLocalTxManagedConnectionFactory();
                mcf.setConnectionURL(databaseURL);
                ConnectionFactory connectionFactory = (ConnectionFactory)mcf.createConnectionFactory();
                setConnectionFactory(connectionFactory);
            } catch (ResourceException exception) {
                throw EISException.resourceException(exception, accessor, null);
            }
        }

        return super.connectToDataSource(accessor, properties);
    }
}