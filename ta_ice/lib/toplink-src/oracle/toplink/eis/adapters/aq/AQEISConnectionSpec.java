// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.eis.adapters.aq;

import java.util.Properties;
import javax.resource.cci.*;
import oracle.toplink.eis.*;
import oracle.toplink.internal.eis.adapters.aq.*;
import oracle.toplink.exceptions.*;

/**
 * Provides the behavoir of instantiating a XML file adapter ConnectionSpec.
 *
 * @author James
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class AQEISConnectionSpec extends EISConnectionSpec {

    /** Connection spec properties. */
    public static String URL = "url";
    public static String DATASOURCE = "datasource";

    /**
     * PUBLIC:
     * Default constructor.
     */
    public AQEISConnectionSpec() {
        super();
    }

    /**
     * Connect with the specified properties and return the Connection.
     */
    public Connection connectToDataSource(EISAccessor accessor, Properties properties) throws DatabaseException, ValidationException {
        setConnectionFactory(new AQConnectionFactory());
        String user = (String)properties.get(USER);
        String password = getPasswordFromProperties(properties);
        String url = (String)properties.get(URL);
        String datasource = (String)properties.get(DATASOURCE);
        if (getConnectionSpec() == null) {
            AQConnectionSpec spec = new AQConnectionSpec(user, password, url);
            if (datasource != null) {
                spec.setDatasource(datasource);
            }
            setConnectionSpec(spec);
        }

        return super.connectToDataSource(accessor, properties);
    }
}