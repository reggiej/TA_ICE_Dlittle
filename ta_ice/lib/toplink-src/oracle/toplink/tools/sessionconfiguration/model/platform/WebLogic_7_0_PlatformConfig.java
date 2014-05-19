// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration.model.platform;

/**
 * INTERNAL:
 */
public class WebLogic_7_0_PlatformConfig extends ServerPlatformConfig {
    public WebLogic_7_0_PlatformConfig() {
        super("oracle.toplink.platform.server.wls.WebLogic_7_0_Platform");
        isSupported = false;
    }
}