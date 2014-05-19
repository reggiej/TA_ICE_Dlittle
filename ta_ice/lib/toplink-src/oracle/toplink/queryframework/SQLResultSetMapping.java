// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.queryframework;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import oracle.toplink.internal.localization.ExceptionLocalization;

/**
 * <p><b>Purpose</b>:
 * Concrete class to represent the SQLResultSetMapping structure as defined by
 * the EJB 3.0 Persistence specification.  This class is used by the 
 * ResultSetMappingQuery and is a component of the TopLink Project
 * 
 * @see oracle.toplink.sessions.Project
 * @author Gordon Yorke
 * @since TopLink Java Essentials
 */

public class SQLResultSetMapping {
    /** Stores the name of this SQLResultSetMapping.  This name is unique within
     * The project.
     */
    protected String name;
    
    /** Stores the list of SQLResult in the order they were
     * added to the Mapping
     */
    protected List results;
    
   
    public SQLResultSetMapping(String name){
        this.name = name;
        if (this.name == null){
            throw new IllegalArgumentException(ExceptionLocalization.buildMessage("null_value_in_sqlresultsetmapping"));
        }
    }

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this SQLResultSetMapping to actual class-based
     * settings. This method is used when converting a project that has been built
     * with class names to a project with classes.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        Iterator iterator = getResults().iterator();
        while (iterator.hasNext()){
            ((SQLResult)iterator.next()).convertClassNamesToClasses(classLoader);
        }
    };   

    public String getName(){
        return this.name;
    }
    
    public void addResult(SQLResult result){
        if (result == null){
            return;
        }
        getResults().add(result);
    }
    
    /**
     * Accessor for the internally stored list of ColumnResult.  Calling this
     * method will result in a collection being created to store the ColumnResult
     */
    public List getResults(){
        if (this.results == null){
            this.results = new ArrayList();
        }
        return this.results;
    }

}
