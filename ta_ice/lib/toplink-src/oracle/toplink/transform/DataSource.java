// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.transform;

import java.util.Vector;
import oracle.toplink.sessions.Project;

/**
 * INTERNAL:
 * <p>
 * <b>Purpose</b>:
 * <p> Used by ObjectTransformer.
 */
public interface DataSource {
    public Vector buildObjects(Project project, Class type);
}