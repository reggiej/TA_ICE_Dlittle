// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.profiler;

import java.util.*;
import oracle.toplink.queryframework.*;

/**
 * <p><b>Purpose</b>:
 * Provide a very simple low overhead means for measuring query executions, and cache hits.
 * This can be useful for performance analysis in a complex system.
 * This monitor is enabled through the System property "oracle.toplink.querymonitor=true".
 * It dumps the number of query cache hits, and executions (missses) once every 100s.
 *
 * @author James Sutherland
 * @since TopLink 10.1.3
 */
public class QueryMonitor {

    public static Hashtable cacheHits = new Hashtable();
    public static Hashtable cacheMisses = new Hashtable();
    public static long dumpTime = System.currentTimeMillis();
    public static Boolean shouldMonitor;
    
    public static boolean shouldMonitor() {
        if (shouldMonitor == null) {
            shouldMonitor = Boolean.FALSE;
            String property = System.getProperty("oracle.toplink.querymonitor");
            if ((property != null) && (property.toUpperCase().equals("TRUE"))) {
                shouldMonitor = Boolean.TRUE;                
            }
        }
        return shouldMonitor.booleanValue();
    }
    
    public static void checkDumpTime() {
        if ((System.currentTimeMillis() - dumpTime) > 100000) {
            dumpTime = System.currentTimeMillis();
            System.out.println("Cache Hits:" + cacheHits);
            System.out.println("Cache Misses:" + cacheMisses);
        }
    }

    public static void incrementReadObjectHits(ReadObjectQuery query) {
        checkDumpTime();
        String name = query.getReferenceClass().getName() + "-findByPrimaryKey";
        Number hits = (Number) cacheHits.get(name);
        if (hits == null) {
            hits = new Integer(0);
        }
        hits = new Integer(hits.intValue() + 1);
        cacheHits.put(name, hits);        
    }
    
    public static void incrementReadObjectMisses(ReadObjectQuery query) {
        checkDumpTime();
        String name = query.getReferenceClass().getName() + "-findByPrimaryKey";
        Number misses = (Number) cacheMisses.get(name);
        if (misses == null) {
            misses = new Integer(0);
        }
        misses = new Integer(misses.intValue() + 1);
        cacheMisses.put(name, misses);
    }
    
    public static void incrementReadAllHits(ReadAllQuery query) {
        checkDumpTime();
        String name = query.getReferenceClass().getName();
        if (query.getName() == null) {
            name = name + "-findAll";
        } else {
            name = name + "-" + query.getName();
        }
        Number hits = (Number) cacheHits.get(name);
        if (hits == null) {
            hits = new Integer(0);
        }
        hits = new Integer(hits.intValue() + 1);
        cacheHits.put(name, hits);        
    }
    
    public static void incrementReadAllMisses(ReadAllQuery query) {
        checkDumpTime();
        String name = query.getReferenceClass().getName();
        if (query.getName() == null) {
            name = name + "-findAll";
        } else {
            name = name + "-" + query.getName();
        }
        Number misses = (Number) cacheMisses.get(name);
        if (misses == null) {
            misses = new Integer(0);
        }
        misses = new Integer(misses.intValue() + 1);
        cacheMisses.put(name, misses);
    }
    
    public static void incrementInsert(WriteObjectQuery query) {
        checkDumpTime();
        String name = query.getReferenceClass().getName() + "-insert";
        Number misses = (Number) cacheMisses.get(name);
        if (misses == null) {
            misses = new Integer(0);
        }
        misses = new Integer(misses.intValue() + 1);
        cacheMisses.put(name, misses);
    }
    
    public static void incrementUpdate(WriteObjectQuery query) {
        checkDumpTime();
        String name = query.getReferenceClass().getName() + "-update";
        Number misses = (Number) cacheMisses.get(name);
        if (misses == null) {
            misses = new Integer(0);
        }
        misses = new Integer(misses.intValue() + 1);
        cacheMisses.put(name, misses);
    }
    
    public static void incrementDelete(DeleteObjectQuery query) {
        checkDumpTime();
        String name = query.getReferenceClass().getName() + "-delete";
        Number misses = (Number) cacheMisses.get(name);
        if (misses == null) {
            misses = new Integer(0);
        }
        misses = new Integer(misses.intValue() + 1);
        cacheMisses.put(name, misses);
    }
}