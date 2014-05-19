// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.sessions;

import java.util.*;
import java.sql.*;
import java.io.*;

/**
 * <b>Purpose</b>:
 * Define an interface for supplying TopLink with a <code>Connection</code> to
 * a JDBC database.
 * <p>
 * <b>Description</b>:
 * This interface defines the methods to be implemented that allow TopLink to
 * acquire a <code>Connection</code> to a JDBC database. There are only 2
 * methods that need to be implemented:
 * <blockquote><code>
 * java.sql.Connection connect(java.util.Properties properties)<br>
 * void toString(java.io.PrintWriter writer)
 * </code></blockquote>
 * Once these methods have been implemented, an instance of the new
 * <code>Connector</code> can be  passed
 * to a <code>JDBCLogin</code> at startup. For example:
 * <blockquote><code>
 * session.getLogin().setConnector(new FooConnector());<br>
 * session.login();
 * </code></blockquote>
 *
 * @see DatabaseLogin
 * @author Big Country
 * @since TOPLink/Java 2.1
 */
public interface Connector extends Serializable, Cloneable {

    /**
     * INTERNAL:
     * Must be cloneable.
     */
    Object clone();

    /**
     * INTERNAL:
     * Connect with the specified properties and return the <code>Connection</code>.
     * The properties are driver-specific; but usually contain the <code>"user"</code>
     * and <code>"password"</code>. Additional
     * properties can be built by using <code>JDBCLogin.setProperty(String propertyName,
     * Object propertyValue)</code>.
     * @return java.sql.Connection
     */
    Connection connect(Properties properties, Session session);

    /**
     * INTERNAL:
     * Print something useful on the log. This information will be displayed
     * on the TopLink log (by default <code>System.out</code>) at login.
     * See the other implementations of this method for examples.
     */
    void toString(PrintWriter writer);

    /**
     * INTERNAL:
     * Provide the details of my connection information. This is primarily for JMX runtime services.
     * @return java.lang.String
     */
    String getConnectionDetails();
}
