// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import oracle.toplink.sessions.DatabaseRecord;

/**
 * <p><b>Purpose</b>:
 * An abstract superclass that represents the comonalities between the main
 * result types of the SQLResultSetMapping
 * 
 * @see EntityResult
 * @see ColumnResult
 * @author Gordon Yorke
 * @since TopLink Java Essentials
 */

public abstract class SQLResult {

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this SQLResult to actual class-based
     * settings. This method is used when converting a project that has been built
     * with class names to a project with classes.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){};


    public boolean isColumnResult(){
        return false;
    }
    
    public boolean isEntityResult(){
        return false;
    }
    
    /**
     * INTERNAL:
     * This method is a convience method for extracting values from Results
     */
    public abstract Object getValueFromRecord(DatabaseRecord record, ResultSetMappingQuery query);
    
}
