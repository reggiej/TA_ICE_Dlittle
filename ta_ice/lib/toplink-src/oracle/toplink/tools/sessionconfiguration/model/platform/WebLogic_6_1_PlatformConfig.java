// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.platform;


/**
 * INTERNAL:
 */
public class WebLogic_6_1_PlatformConfig extends ServerPlatformConfig {
    public WebLogic_6_1_PlatformConfig() {
        super("oracle.toplink.platform.server.wls.WebLogic_6_1_Platform");
        isSupported = false;
    }
}