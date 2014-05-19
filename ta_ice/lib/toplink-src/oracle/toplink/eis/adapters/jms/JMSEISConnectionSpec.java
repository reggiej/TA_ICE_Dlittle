// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.eis.adapters.jms;


// JDK imports
import java.util.Properties;
import javax.jms.ConnectionFactory;
import javax.resource.cci.*;

//TopLink imports
import oracle.toplink.eis.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.internal.eis.adapters.jms.*;

/**
 * Provides the behavior of instantiating an EIS ConnectionSpec.
 *
 * @author Dave McCann
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class JMSEISConnectionSpec extends EISConnectionSpec {
    // connection spec properties
    public static String CONNECTION_FACTORY_URL = "factoryURL";// connection factory JNDI lookup name
    public static String CONNECTION_FACTORY = "factory";// connection factory class

    /**
     * PUBLIC:
     * Default constructor.
     */
    public JMSEISConnectionSpec() {
        super();
    }

    /**
     * Connect with the specified properties and return the Connection.
     */
    public Connection connectToDataSource(EISAccessor accessor, Properties properties) throws DatabaseException, ValidationException {
        setConnectionFactory(new CciJMSConnectionFactory());

        if (getConnectionSpec() == null) {
            String username = (String)properties.get(USER);
            // Bug 4117441 - Secure programming practices, store password in char[]
            String password = getPasswordFromProperties(properties);
            String url = (String)properties.get(CONNECTION_FACTORY_URL);
            ConnectionFactory factory = (ConnectionFactory)properties.get(CONNECTION_FACTORY);

            CciJMSConnectionSpec spec = new CciJMSConnectionSpec();

            if (username != null) {
                spec.setUsername(username);
            }
            if (password != null) {
                spec.setPassword(password);
            }
            if (url != null) {
                spec.setConnectionFactoryURL(url);
            }
            if (factory != null) {
                spec.setConnectionFactory(factory);
            }

            setConnectionSpec(spec);
        }

        return super.connectToDataSource(accessor, properties);
    }
}