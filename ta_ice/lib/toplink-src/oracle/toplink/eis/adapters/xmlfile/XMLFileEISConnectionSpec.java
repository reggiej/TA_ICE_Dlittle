// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.eis.adapters.xmlfile;

import java.util.Properties;
import javax.resource.cci.*;
import oracle.toplink.eis.*;
import oracle.toplink.internal.eis.adapters.xmlfile.*;
import oracle.toplink.exceptions.*;

/**
 * Provides the behavoir of instantiating a XML file adapter ConnectionSpec.
 *
 * @author James
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class XMLFileEISConnectionSpec extends EISConnectionSpec {

    /** Connection spec properties. */
    public static String DIRECTORY = "directory";

    /**
     * PUBLIC:
     * Default constructor.
     */
    public XMLFileEISConnectionSpec() {
        super();
    }

    /**
     * Connect with the specified properties and return the Connection.
     */
    public Connection connectToDataSource(EISAccessor accessor, Properties properties) throws DatabaseException, ValidationException {
        setConnectionFactory(new XMLFileConnectionFactory());
        String directory = (String)properties.get(DIRECTORY);
        if (directory != null) {
            setConnectionSpec(new XMLFileConnectionSpec(directory));
        }

        return super.connectToDataSource(accessor, properties);
    }
}