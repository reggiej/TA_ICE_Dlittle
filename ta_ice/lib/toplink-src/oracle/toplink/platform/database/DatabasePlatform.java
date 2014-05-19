// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.platform.database;

import java.sql.*;
import java.util.*;
import java.math.*;
import java.io.*;

import oracle.toplink.descriptors.ClassDescriptor;
import oracle.toplink.exceptions.*;
import oracle.toplink.queryframework.*;
import oracle.toplink.objectrelational.*;
import oracle.toplink.internal.databaseaccess.AppendCallCustomParameter;
import oracle.toplink.internal.databaseaccess.BindCallCustomParameter;
import oracle.toplink.internal.databaseaccess.DatabaseAccessor;
import oracle.toplink.internal.databaseaccess.DatabaseCall;
import oracle.toplink.internal.databaseaccess.FieldTypeDefinition;
import oracle.toplink.internal.expressions.ParameterExpression;
import oracle.toplink.internal.helper.*;
import oracle.toplink.sessions.SessionProfiler;
import oracle.toplink.sequencing.*;
import oracle.toplink.tools.schemaframework.FieldDefinition;
import oracle.toplink.tools.schemaframework.TableDefinition;
import oracle.toplink.internal.sequencing.Sequencing;
import oracle.toplink.internal.sessions.AbstractSession;

/**
 * DatabasePlatform is private to TopLink. It encapsulates behavior specific to a database platform
 * (eg. Oracle, Sybase, DBase), and provides protocol for TopLink to access this behavior. The behavior categories
 * which require platform specific handling are SQL generation and sequence behavior. While database platform
 * currently provides sequence number retrieval behaviour, this will move to a sequence manager (when it is
 * implemented).
 *
 * @see AccessPlatform
 * @see DB2Platform
 * @see DBasePlatform
 * @see OraclePlatform
 * @see SybasePlatform
 *
 * @since TOPLink/Java 1.0
 */
public class DatabasePlatform extends oracle.toplink.internal.databaseaccess.DatabasePlatform {

    public DatabasePlatform() {
    	super();
    }
    
}
