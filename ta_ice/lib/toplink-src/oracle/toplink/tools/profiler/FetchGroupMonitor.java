// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.profiler;

import java.util.*;

/**
 * <p><b>Purpose</b>:
 * Provide a very simple low overhead means for measuring fetch group field usage.
 * This can be useful for performance analysis in a complex system.
 * This monitor is enabled through the System property "oracle.toplink.fetchgroupmonitor=true".
 * It dumps the attribute used for a class every time a new attribute is accessed.
 *
 * @author James Sutherland
 * @since TopLink 10.1.3.2
 */
public class FetchGroupMonitor {

    public static Hashtable fetchedAttributes = new Hashtable();
    public static Boolean shouldMonitor;
    
    public static boolean shouldMonitor() {
        if (shouldMonitor == null) {
            shouldMonitor = Boolean.FALSE;
            String property = System.getProperty("oracle.toplink.fetchgroupmonitor");
            if ((property != null) && (property.toUpperCase().equals("TRUE"))) {
                shouldMonitor = Boolean.TRUE;                
            }
        }
        return shouldMonitor.booleanValue();
    }
    
    public static void recordFetchedAttribute(Class domainClass, String attributeName) {
        if (! shouldMonitor()) {
            return;
        }
        synchronized (fetchedAttributes) {
            Set classesFetchedAttributes = (Set) fetchedAttributes.get(domainClass);
            if (classesFetchedAttributes == null) {
                classesFetchedAttributes = new HashSet();
                fetchedAttributes.put(domainClass, classesFetchedAttributes);
            }
            if (!classesFetchedAttributes.contains(attributeName)) {
                classesFetchedAttributes.add(attributeName);
                System.out.println("Fetched attributes: " + domainClass + " - " + classesFetchedAttributes);
            }
        }
    }
}