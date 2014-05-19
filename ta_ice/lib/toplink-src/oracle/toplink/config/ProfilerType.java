package oracle.toplink.config;

/**
 * Profiler type persistence property values.
 * 
 * <p>JPA persistence property Usage:
 * 
 * <p><code>properties.add(TopLinkProperties.ProfilerType, ProfilerType.DMSPerformanceProfiler);</code>
 * <p>Property values are case-insensitive.
 * 
 * @see QueryMonitor
 * @see DMSPerformanceProfiler
 * @see PerformanceProfiler
 */
public class ProfilerType {
    //DMSProfiler is a mechanism used to provide a link for TopLink performance profiling by using the DMS tool.
    public static final String DMSPerformanceProfiler = "DMSPerformanceProfiler";
    //A tool used to provide high level performance profiling information
    public static final String PerformanceProfiler = "PerformanceProfiler";
    public static final String QueryMonitor = "QueryMonitor";
    public static final String NoProfiler = "NoProfiler";

    public static final String DEFAULT = NoProfiler;
}
