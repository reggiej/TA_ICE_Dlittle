// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.remote.jms;

import oracle.toplink.internal.remote.RemoteCommand;
import oracle.toplink.internal.sessions.AbstractSession;
import oracle.toplink.exceptions.JMSProcessingException;
import oracle.toplink.logging.SessionLog;
import javax.jms.*;

/**
 * INTERNAL:
 *
 * <p>
 * <b>PURPOSE</b>:To Provide a framework for processing incomming messages</p>
 * <p>
 * <b>Descripton</b>:This class is a JMS message Listener that process incomming JMS
 * object message.</p>
 *
 * @author Gordon Yorke
 * @see oracle.toplink.remote.jms.JMSClusteringService
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.remotecommand.jms.JMSTopicTransportManager}
 */
public class TopLinkMessageListener implements MessageListener {
    protected AbstractSession session;

    public TopLinkMessageListener(oracle.toplink.sessions.Session session) {
        this.session = (AbstractSession)session;
    }

    public AbstractSession getSession() {
        return this.session;
    }

    /**
     * INTERNAL:
     * Casts the message to an ObjectMessage and extracts the TopLink message
     **/
    public void onMessage(Message message) {
        ObjectMessage objectMessage = null;
        try {
            String topic = ((Topic)message.getJMSDestination()).getTopicName();
            getSession().log(SessionLog.FINEST, SessionLog.PROPAGATION, "retreived_remote_message_from_JMS_topic", topic);
            if (message instanceof ObjectMessage) {
                objectMessage = (ObjectMessage)message;
                Object object = objectMessage.getObject();
                if (object instanceof RemoteCommand) {
                    getSession().log(SessionLog.FINEST, SessionLog.PROPAGATION, "processing_topLink_remote_command");
                    ((RemoteCommand)object).execute(getSession(), null);
                } else if (object == null) {
                    getSession().log(SessionLog.WARNING, SessionLog.PROPAGATION, "retreived_null_message", topic);
                } else {
                    getSession().log(SessionLog.WARNING, SessionLog.PROPAGATION, "retreived_unknown_message_type", object.getClass(), topic);
                }
            } else {
                getSession().log(SessionLog.WARNING, SessionLog.PROPAGATION, "received_unexpected_message_type", message.getClass().getName(), topic);
            }
        } catch (JMSException exception) {
            getSession().log(SessionLog.FINER, SessionLog.PROPAGATION, "JMS_exception_thrown");
            getSession().handleException(JMSProcessingException.buildDefault(exception));
        } catch (Throwable exception) {
            getSession().handleException(JMSProcessingException.buildDefault(exception));
        }
    }
}