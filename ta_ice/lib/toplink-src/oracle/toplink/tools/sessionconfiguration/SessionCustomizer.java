// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration;

import oracle.toplink.sessions.*;

/**
 * PUBLIC:
 * This interface is to allow extra customization on a TopLink Session
 */
public interface SessionCustomizer {
    public void customize(Session session) throws Exception;
}