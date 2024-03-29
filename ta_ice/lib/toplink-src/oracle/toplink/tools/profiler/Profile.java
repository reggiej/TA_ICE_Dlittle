// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.profiler;

import java.util.*;
import java.io.*;
import oracle.toplink.internal.helper.*;
import oracle.toplink.internal.localization.*;

/**
 * <p><b>Purpose</b>: Any information stored for a profile operation.
 *
 * @since TopLink 1.0
 * @author James Sutherland
 */
public class Profile implements Serializable, Cloneable {
    protected Class queryClass;
    protected Class domainClass;
    protected long numberOfInstancesEffected;
    protected Hashtable operationTimings;
    protected long localTime;
    protected long profileTime;
    protected long totalTime;
    protected long shortestTime;
    protected long longestTime;

    public Profile() {
        this.numberOfInstancesEffected = 0;
        this.operationTimings = new Hashtable();
        this.totalTime = 0;
        this.localTime = 0;
        this.longestTime = 0;
        this.shortestTime = -1;
        this.profileTime = 0;
    }

    public void addTiming(String name, long time) {
        getOperationTimings().put(name, new Long(time));
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException exception) {
            ;//Do nothing
        }

        return null;
    }

    public Class getDomainClass() {
        return domainClass;
    }

    public long getLocalTime() {
        return localTime;
    }

    public long getLongestTime() {
        return longestTime;
    }

    public long getNumberOfInstancesEffected() {
        return numberOfInstancesEffected;
    }

    public long getObjectsPerSecond() {
        if (getTotalTime() == 0) {
            return 0;
        }
        return (getNumberOfInstancesEffected() * 1000) / getTotalTime();
    }

    public Hashtable getOperationTimings() {
        return operationTimings;
    }

    public long getProfileTime() {
        return profileTime;
    }

    public Class getQueryClass() {
        return queryClass;
    }

    public long getShortestTime() {
        return shortestTime;
    }

    public long getTimePerObject() {
        if (getNumberOfInstancesEffected() == 0) {
            return 0;
        }
        return getTotalTime() / getNumberOfInstancesEffected();
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setDomainClass(Class domainClass) {
        this.domainClass = domainClass;
    }

    public void setLocalTime(long localTime) {
        this.localTime = localTime;
    }

    public void setLongestTime(long longestTime) {
        this.longestTime = longestTime;
    }

    public void setNumberOfInstancesEffected(long numberOfInstancesEffected) {
        this.numberOfInstancesEffected = numberOfInstancesEffected;
    }

    public void setOperationTimings(Hashtable operationTimings) {
        this.operationTimings = operationTimings;
    }

    public void setProfileTime(long profileTime) {
        this.profileTime = profileTime;
    }

    public void setQueryClass(Class queryClass) {
        this.queryClass = queryClass;
    }

    public void setShortestTime(long shortestTime) {
        this.shortestTime = shortestTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public String toString() {
        StringWriter writer = new StringWriter();

        write(writer, new PerformanceProfiler());

        return writer.toString();
    }

    public void write(Writer writer, PerformanceProfiler profiler) {
        String cr = Helper.cr();
        try {
            writer.write(ToStringLocalization.buildMessage("profile", (Object[])null) + "(");
            if (getQueryClass() != null) {
                writer.write(Helper.getShortClassName(getQueryClass()) + "," + cr);
            } else {
                writer.write(cr);
            }
            if (getDomainClass() != null) {
                profiler.writeNestingTabs(writer);
                writer.write("\t" + ToStringLocalization.buildMessage("class", (Object[])null) + "=" + getDomainClass().getName() + "," + cr);
            }
            if (getNumberOfInstancesEffected() != 0) {
                profiler.writeNestingTabs(writer);
                writer.write("\t" + ToStringLocalization.buildMessage("number_of_objects", (Object[])null) + "=" + getNumberOfInstancesEffected() + "," + cr);
            }
            profiler.writeNestingTabs(writer);
            writer.write("\t" + ToStringLocalization.buildMessage("total_time", (Object[])null) + "=" + getTotalTime() + "," + cr);
            profiler.writeNestingTabs(writer);
            writer.write("\t" + ToStringLocalization.buildMessage("local_time", (Object[])null) + "=" + getLocalTime() + "," + cr);
            if (getProfileTime() != 0) {
                profiler.writeNestingTabs(writer);
                writer.write("\t" + ToStringLocalization.buildMessage("profiling_time", (Object[])null) + "=" + getProfileTime() + "," + cr);
            }

            for (Enumeration operationNames = getOperationTimings().keys();
                     operationNames.hasMoreElements();) {
                String operationName = (String)operationNames.nextElement();
                long operationTime = ((Long)getOperationTimings().get(operationName)).longValue();

                if (operationTime != 0) {
                    profiler.writeNestingTabs(writer);
                    writer.write("\t" + operationName + "=" + operationTime + "," + cr);
                }
            }

            if (getTimePerObject() != 0) {
                profiler.writeNestingTabs(writer);
                writer.write("\t" + ToStringLocalization.buildMessage("time_object", (Object[])null) + "=" + getTimePerObject() + "," + cr);
            }
            if (getObjectsPerSecond() != 0) {
                profiler.writeNestingTabs(writer);
                writer.write("\t" + ToStringLocalization.buildMessage("objects_second", (Object[])null) + "=" + getObjectsPerSecond() + "," + cr);
            }
            if (getShortestTime() != -1) {
                profiler.writeNestingTabs(writer);
                writer.write("\t" + ToStringLocalization.buildMessage("shortestTime", (Object[])null) + "=" + getShortestTime() + "," + cr);
            }
            if (getLongestTime() != 0) {
                profiler.writeNestingTabs(writer);
                writer.write("\t" + ToStringLocalization.buildMessage("longestTime", (Object[])null) + "=" + getLongestTime() + "," + cr);
            }
            profiler.writeNestingTabs(writer);
            writer.write(")");

        } catch (IOException stupidException) {
        }
    }
}