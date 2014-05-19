// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.transform;

import java.util.Vector;
import oracle.toplink.sessions.Project;

/**
 * INTERNAL:
 * <p>
 * <b>Purpose</b>:
 * <p> used by ObjectTransformer.
 */
public interface DataResult {
    public void storeObjects(Project project, Vector objects);
}