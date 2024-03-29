// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.jndi;

import java.util.*;
import java.sql.*;
import javax.naming.*;
import javax.sql.*;
import oracle.toplink.sessions.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.internal.helper.*;
import oracle.toplink.internal.localization.*;

/**
 * Specifies the J2EE DataSource lookup options.
 * This connector is normally used with a login in a J2EE environment
 * to connect to a server's connection pool defined by the DataSource name.
 * The JNDI name that the DataSource is registered under must be specified,
 * this must include any required prefix such as "java:comp/env/", (unless a DataSource object is given).
 * A Context is only required if not running on the server, otheriwse default to a new InitialContext().
 * @author Big Country
 * @since TOPLink/Java 2.1
 */
public class JNDIConnector implements Connector {
    protected DataSource dataSource;
    protected Context context;
    protected String name;
    public static final int STRING_LOOKUP = 1;
    public static final int COMPOSITE_NAME_LOOKUP = 2;
    public static final int COMPOUND_NAME_LOOKUP = 3;
		//default setting is composite name to be consistent with previous TopLink versions
    protected int lookupType = COMPOSITE_NAME_LOOKUP;

    /**
     * PUBLIC:
     * Construct a Connector with no settings.
     * The datasource name will still need to be set.
     */
    public JNDIConnector() {
        super();
    }

    /**
     * PUBLIC:
     * Construct a Connector with the datasource name.
     */
    public JNDIConnector(Context context, String name) throws ValidationException {
        this(name);
        this.context = context;
    }

    /**
     * PUBLIC:
     * Construct a Connector with the datasource name.
     */
    public JNDIConnector(String name) {
        this.name = name;
    }

    /**
     * PUBLIC:
     * Construct a Connector with the datasource object.
     */
    public JNDIConnector(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * INTERNAL:
     * Clone the connector.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (Exception exception) {
            throw new InternalError("Clone failed");
        }
    }

    /**
     * INTERNAL:
     * Connect with the specified properties and session. Return the Connection.
     */
    public Connection connect(Properties properties, Session session) throws DatabaseException, ValidationException {
        return connect(properties);
    }
    
    /**
     * INTERNAL:
     * Connect with the specified properties and return the Connection.
     */
    public Connection connect(Properties properties) throws DatabaseException, ValidationException {
        String user = properties.getProperty("user");
        Object passwordObject = properties.get("password");
        String password = null;
        if (passwordObject instanceof char[]) {
            password = new String((char[])passwordObject);
        } else if (passwordObject instanceof String) {
            password = (String) passwordObject;
        }
        DataSource dataSource = getDataSource();
        if (dataSource == null) {
            try {
                //bug#2761428 and 4405389 JBoss needs to look up datasources based on a string not a composite or compound name
                if (lookupType == STRING_LOOKUP) {
                    dataSource = (DataSource)getContext().lookup(getName());
                } else if (lookupType == COMPOSITE_NAME_LOOKUP) {
                    dataSource = (DataSource)getContext().lookup(new CompositeName(name));
                } else {
                    dataSource = (DataSource)getContext().lookup(new CompoundName(name, new Properties()));
                }
                this.setDataSource(dataSource);
            } catch (NamingException exception) {
                throw ValidationException.cannotAcquireDataSource(getName(), exception);
            }
        }

        try {
            // WebLogic connection pools do not require a user name and password.
            // JDBCLogin usually initializes these values with an empty string.
            // WebLogic data source does not support the getConnection() call with arguments
            // it only supports the zero argument call. DM 26/07/2000
            if ((user == null) || (user.equalsIgnoreCase(""))) {
                return dataSource.getConnection();
            } else {
                return dataSource.getConnection(user, password);
            }
        } catch (SQLException exception) {
            throw DatabaseException.sqlException(exception);
        }
    }

    /**
     * PUBLIC:
     * Return the JNDI Context that can supplied the named DataSource.
     */
    public Context getContext() {
        if (context == null) {
            try {
                context = new InitialContext();
            } catch (NamingException exception) {
            }
        }
        return context;
    }

    /**
     * PUBLIC:
     * Return the javax.sql.DataSource.
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * PUBLIC:
     * Return the name of the DataSource within the
     * JNDI Context.
     */
    public String getName() {
        return name;
    }

    /**
     * PUBLIC:
     * Provide the details of my connection information. This is primarily for JMX runtime services.
     * @return java.lang.String
     */
    public String getConnectionDetails() {
        return getName();
    }

    /**
     * PUBLIC:
     * Set the JNDI Context that can supply the named DataSource.
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * PUBLIC:
     * Set the javax.sql.DataSource.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * PUBLIC:
     * Set the name of the DataSource within the
     * JNDI Context.
     */
    public void setName(String name) throws ValidationException {
        this.name = name;
    }

    public void setLookupType(int lookupType) {
        this.lookupType = lookupType;
    }

    public int getLookupType() {
        return lookupType;
    }

    /**
     * PUBLIC:
     * Print data source info.
     */
    public String toString() {
        return Helper.getShortClassName(getClass()) + ToStringLocalization.buildMessage("datasource_name", (Object[])null) + "=>" + getName();
    }

    /**
     * INTERNAL:
     * Print something useful on the log.
     */
    public void toString(java.io.PrintWriter writer) {
        writer.print(ToStringLocalization.buildMessage("connector", (Object[])null) + "=>" + Helper.getShortClassName(getClass()));
        writer.print(" ");
        writer.println(ToStringLocalization.buildMessage("datasource_name", (Object[])null) + "=>" + getName());
    }
}
