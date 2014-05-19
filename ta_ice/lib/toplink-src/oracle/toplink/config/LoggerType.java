package oracle.toplink.config;

/**
 * Logger type persistence property values.
 * 
 * <p>JPA persistence property Usage:
 * 
 * <p><code>properties.add(TopLinkProperties.LoggerType, LoggerType.JavaLogger);</code>
 * <p>Property values are case-insensitive.
 * 
 * @author Wonseok Kim
 */
public class LoggerType {
    public static final String DefaultLogger = "DefaultLogger";
    public static final String JavaLogger = "JavaLogger";
    public static final String ServerLogger = "ServerLogger";

    public static final String DEFAULT = DefaultLogger;
}
