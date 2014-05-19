// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.eis.adapters.mqseries;


// JDK imports
import java.util.Properties;
import javax.resource.ResourceException;
import javax.resource.cci.*;

// TopLink imports
import oracle.toplink.eis.*;
import oracle.toplink.eis.adapters.jms.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.internal.eis.adapters.jms.*;

// MQSeries imports
import com.ibm.mq.jms.*;

/**
 * Provides the behavior of instantiating an EIS ConnectionSpec.  This class provides additional
 * connection information required by MQSeries.
 *
 * @author Dave McCann
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class MQConnectionSpec extends JMSEISConnectionSpec {
    public static String TRANSPORT_TYPE = "transport";// JMSC.MQJMS_TP_CLIENT_MQ_TCPIP or JMSC.MQJMS_TP_BINDINGS_MQ (default)
    public static String CHANNEL = "channel";// connection channel
    public static String HOST = "host";// server IP
    public static String PORT = "port";// connection port number
    public static String QUEUE_MANAGER = "manager";// queue manager name

    /**
     * PUBLIC:
     * Default constructor.
     */
    public MQConnectionSpec() {
        super();
    }

    /**
     * Connect with the specified properties and return the Connection.
     */
    public Connection connectToDataSource(EISAccessor accessor, Properties properties) throws DatabaseException, ValidationException {
        setConnectionFactory(new CciJMSConnectionFactory());

        if (getConnectionSpec() == null) {
            CciJMSConnectionSpec spec = new CciJMSConnectionSpec();

            String property = (String)properties.get(CONNECTION_FACTORY_URL);
            if (property != null) {
                spec.setConnectionFactoryURL(property);
            }

            MQQueueConnectionFactory factory = (MQQueueConnectionFactory)properties.get(CONNECTION_FACTORY);
            if (factory != null) {
                try {
                    // CHANNEL, HOST and QUEUE_MANAGER are to be set by the user
                    factory.setChannel((String)properties.get(CHANNEL));
                    factory.setHostName((String)properties.get(HOST));
                    factory.setQueueManager((String)properties.get(QUEUE_MANAGER));

                    // TRANSPORT TYPE and PORT will use defaults if not set by the user
                    Integer iValue = (Integer)properties.get(TRANSPORT_TYPE);
                    if (iValue != null) {
                        factory.setTransportType(iValue.intValue());
                    }
                    iValue = (Integer)properties.get(PORT);
                    if (iValue != null) {
                        factory.setPort(iValue.intValue());
                    }
                } catch (Exception ex) {
                    throw EISException.invalidConnectionFactoryAttributes();
                }

                spec.setConnectionFactory(factory);
            }
            setConnectionSpec(spec);
        }

        return super.connectToDataSource(accessor, properties);
    }
}