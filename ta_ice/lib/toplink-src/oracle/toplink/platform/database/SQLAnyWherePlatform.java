// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.platform.database;


/**
 * <p><b>Purpose</b>: Provides SQL Anywhere specific behaviour.
 * <p> For the most part this is the same as Sybase, the outer join syntax is suppose to be different.
 *
 * @since TOPLink/Java 2.1
 */
public class SQLAnyWherePlatform extends SybasePlatform {
    public boolean isSQLAnywhere() {
        return true;
    }

    public boolean isSybase() {
        return false;
    }

    /**
     * SQL Anywhere does not support the Sybase outer join syntax.
     */
    public boolean shouldPrintOuterJoinInWhereClause() {
        return false;
    }
}