// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;


/**
 * INTERNAL:
 */
public class EjbJarError {
    protected String description = "";

    public EjbJarError(String newDescription) {
        description = newDescription;
    }

    public String getDescription() {
        return description;
    }

    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        } else if (anObject instanceof EjbJarError) {
            EjbJarError anError = (EjbJarError)anObject;
            if (getDescription() == null) {
                return anError.getDescription() == null;
            } else {
                return getDescription().equals(anError.getDescription());
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        return getDescription().hashCode();
    }
}