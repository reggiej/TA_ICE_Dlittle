// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.platform.database.oracle;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;

import oracle.jdbc.pool.OracleOCIConnectionPool;

import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.exceptions.ValidationException;
import oracle.toplink.jndi.JNDIConnector;

public class OracleOCIProxyConnector extends JNDIConnector {
    /**
     * PUBLIC:
     * Construct a Connector with no settings.
     * The datasource name will still need to be set.
     */
    public OracleOCIProxyConnector() {
        super();
    }

    /**
     * PUBLIC:
     * Construct a Connector with the datasource name.
     */
    public OracleOCIProxyConnector(Context context, String name) throws ValidationException {
        super(context, name);
    }

    /**
     * PUBLIC:
     * Construct a Connector with the datasource name.
     */
    public OracleOCIProxyConnector(String name) {
        super(name);
    }

    /**
     * PUBLIC:
     * Construct a Connector with OracleOCIConnectionPool.
     */
    public OracleOCIProxyConnector(OracleOCIConnectionPool oracleOCIConnectionPool) {
        super(oracleOCIConnectionPool);
    }

    /**
     * INTERNAL:
     * In case "proxytype" property is specified connects using proxy connection,
     * otherwise calls its superclass.
     */
    public Connection connect(Properties properties) throws DatabaseException, ValidationException {
        String proxytype = properties.getProperty(OracleOCIConnectionPool.PROXYTYPE);
        if(proxytype == null || proxytype.length() == 0) {
            return super.connect(properties);
        } else {
            try {
                OracleOCIConnectionPool oracleOCIConnectionPool = (OracleOCIConnectionPool)getDataSource();
                if (oracleOCIConnectionPool == null) {
                    try {
                        oracleOCIConnectionPool = (OracleOCIConnectionPool)getContext().lookup(getName());
                        this.setDataSource(oracleOCIConnectionPool);
                    } catch (NamingException exception) {
                        throw ValidationException.cannotAcquireDataSource(getName(), exception);
                    }
                }
                return oracleOCIConnectionPool.getProxyConnection(proxytype, properties);
            } catch (SQLException exception) {
                throw DatabaseException.sqlException(exception);
            } catch (ClassCastException classCastException) {
                throw ValidationException.oracleOCIProxyConnectorRequiresOracleOCIConnectionPool();
            }
        }
    }    
}
