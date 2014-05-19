// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.remote.jms.mdb;

import oracle.toplink.exceptions.JMSProcessingException;
import oracle.toplink.remote.jms.TopLinkMessageListener;
import oracle.toplink.sessions.Session;
import oracle.toplink.tools.sessionmanagement.SessionManager;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.*;
import javax.naming.*;


/**
 * <p>
 * <b>Purpose</b>: To provide a reference implementation for Message Driven Bean that can listen and process TopLink messages. 
 * <p>
 * <b>Descripton</b>: The Message Driven Bean (MDB) must be deployed along with TopLink application and configured to listener the the same
 *  destination that clustering services send their cache sync messages to.  User must defines an environment variable named with the value of
 *  ClusteringMDB.TOPLINK_SESSION_NAME.  This environment variable value is session name  used by TopLink to load the cosresponding session
 *  from the sessions.xml when the MDB is created.
 *
 * @see oracle.toplink.remote.CacheSynchronizationManager
 */
public class ClusteringMDB implements MessageDrivenBean, MessageListener {
    protected TopLinkMessageListener tlMessageListener;

    /**
     * This value is the env-entry-name element of the Message Driven Beand configured in the ejb-jar.xml.
     * The value is used to lookup the value of the session's name from the environment variable define in JNDI "java:comp/env".
     **/
    public final String TOPLINK_SESSION_NAME = "tl_session_name_for_mdb";
    MessageDrivenContext mdbCtx;

    public ClusteringMDB() {
    }

    /* MDB specific method */
    public void setMessageDrivenContext(MessageDrivenContext ctx) {
        mdbCtx = ctx;
    }

    /* MDB specific method */
    public void ejbCreate() {
        tlMessageListener = new TopLinkMessageListener(getSession());
    }

    /* MDB specific method */
    public void ejbRemove() {
        tlMessageListener = null;
    }

    /**
     * PUBLIC:
     * Return the TopLink session that has the session's name configured as an env-entry element in the ejb-jar.xml.
     * User can subclass and overwrite this method to obtain the session differently.
     *
     * @exception oracle.toplink.exceptions.JMSProcessingException if it is unable to lookup the session's name or the session is null
     */
    public Session getSession() {
        Session session;

        try {
            Context initCtx = new InitialContext();
            Context myEnv = (Context) initCtx.lookup("java:comp/env");

            // Obtain the session name configured by the Deployer.
            String sessionName = (String) myEnv.lookup(TOPLINK_SESSION_NAME);
            session = SessionManager.getManager().getSession(sessionName);
        } catch (NamingException e) {
            throw JMSProcessingException.errorLookupSessionNameInCtx(e);
        }

        if (session == null) {
            throw JMSProcessingException.mdbFoundNoSession();
        }

        return session;
    }

    /* MDB specific method */
    public void onMessage(javax.jms.Message message) {
        // Delegate the received message to TopLinkMessageListener for processing
        tlMessageListener.onMessage(message);
    }
}
