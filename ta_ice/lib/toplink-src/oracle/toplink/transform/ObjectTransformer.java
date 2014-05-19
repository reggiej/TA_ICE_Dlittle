// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.transform;

import java.util.Vector;
import oracle.toplink.sessions.Project;

/**
 * INTERNAL:
 * <p>
 * <b>Purpose</b>:
 * <p> Transform objects from one format to another.
 */
public class ObjectTransformer {
    Project project;

    public ObjectTransformer(Project newProject) {
        super();
        project = newProject;
    }

    public Vector buildObjects(Class type, DataSource source) {
        return source.buildObjects(project, type);
    }

    public void storeObjects(Vector objects, DataResult result) {
        result.storeObjects(project, objects);
    }

    public void storeObjects(Vector objects) {
    }
}