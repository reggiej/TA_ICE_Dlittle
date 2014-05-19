// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.eis.adapters.mqseries;


// TopLink imports
import oracle.toplink.eis.adapters.jms.JMSPlatform;

/**
 * This class is an extension of the generic JMSPlatform.  It is for clarity and
 * consistency.  Using the generic JMS platform for MQSeries access is not intuitive,
 * and for EIS access, both a connection specification and platform class should be
 * provided.
 *
 * @author Dave McCann
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class MQPlatform extends JMSPlatform {

    /**
     * This default constructor simply calls the JMSPlatform constructor.
     */
    public MQPlatform() {
        super();
    }
}