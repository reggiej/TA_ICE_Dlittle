// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.platform;


/**
 * INTERNAL:
 */
public class WebSphere_5_1_PlatformConfig extends ServerPlatformConfig {
    public WebSphere_5_1_PlatformConfig() {
        super("oracle.toplink.platform.server.was.WebSphere_5_1_Platform");
        isSupported = false;
    }
}