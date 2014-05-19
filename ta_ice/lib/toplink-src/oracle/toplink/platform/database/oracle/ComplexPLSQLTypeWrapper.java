// Copyright (c) 2007 Oracle. All rights reserved.

package oracle.toplink.platform.database.oracle;

// javse imports

// Java extension imports

// TopLink imports
import oracle.toplink.internal.helper.ComplexDatabaseType;
import oracle.toplink.internal.helper.DatabaseType;
import oracle.toplink.internal.helper.DatabaseTypeWrapper;

/**
 * <b>INTERNAL</b>: a helper class that holds DatabaseType's. Used to support marshalling
 * PLSQLStoredProcedureCall's 
 * 
 * @author Mike Norman - michael.norman@oracle.com
 * @since Oracle TopLink 11.x.x
 */
public class ComplexPLSQLTypeWrapper extends DatabaseTypeWrapper {

    public ComplexPLSQLTypeWrapper() {
        super();
    }

    public ComplexPLSQLTypeWrapper(DatabaseType wrappedType) {
        super(wrappedType);
    }

    public ComplexDatabaseType getWrappedType() {
        return (ComplexDatabaseType)wrappedDatabaseType;
    }
}
