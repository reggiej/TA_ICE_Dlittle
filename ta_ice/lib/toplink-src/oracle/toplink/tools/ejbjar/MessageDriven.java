// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * INTERNAL:
 * Message-driven object that stores all deployment descriptor
 * info pertaining to a particular message-driven bean.
 */
public class MessageDriven extends EnterpriseObject {
    String transactionType;// Required
    String messageSelector;// Optional
    String acknowledgeMode;// Optional
    MessageDrivenDestination destination;// Optional

    /**
     * @return String the acknowledge mode String, or null if not specified
     */
    public String getAcknowledgeMode() {
        return acknowledgeMode;
    }

    /**
     * @return MessageDrivenDestination the destination, or null if not specified
     */
    public MessageDrivenDestination getMessageDrivenDestination() {
        return destination;
    }

    /**
     * @return String the message selector String, or null if not specified
     */
    public String getMessageSelector() {
        return messageSelector;
    }

    /**
     * @return String the transaction type String
     */
    public String getTransactionType() {
        return transactionType;
    }

    /**
     * Return true if this is a message-driven object, false if not.
     */
    public boolean isMessageDriven() {
        return true;
    }

    /**
     * @param ackMode the acknowledge mode String
     */
    public void setAcknowledgeMode(String ackMode) {
        acknowledgeMode = ackMode;
    }

    /**
     * @param dest the MessageDrivenDestination object
     */
    public void setMessageDrivenDestination(MessageDrivenDestination dest) {
        destination = dest;
    }

    /**
     * @param msgSelector the message selector String
     */
    public void setMessageSelector(String msgSelector) {
        messageSelector = msgSelector;
    }

    /**
     * @param transType the transaction type String
     */
    public void setTransactionType(String transType) {
        transactionType = transType;
    }

    /**
     * Load the data for this instance from the specified element.
     * @param e the DOM element
     */
    public void loadFromElement(Element e) {
        super.loadFromElement(e);
        transactionType = stringFromElement(e, TRANSACTION_TYPE);
        messageSelector = optionalStringFromElement(e, MESSAGE_SELECTOR);
        acknowledgeMode = optionalStringFromElement(e, ACKNOWLEDGE_MODE);
        destination = (MessageDrivenDestination)optionalObjectFromElement(e, MESSAGE_DRIVEN_DESTINATION, new MessageDrivenDestination());
    }

    /**
     * Return the data from this instance as a DOM element.
     * @param doc a Document instance used to create elements
     */
    public Element toElement(Document doc) {
        Element e = doc.createElement(MESSAGE_DRIVEN);
        inheritedFields(doc, e);
        addText(doc, e, EJB_CLASS, ejbClass);
        addText(doc, e, TRANSACTION_TYPE, transactionType);
        optionallyAddText(doc, e, MESSAGE_SELECTOR, messageSelector);
        optionallyAddText(doc, e, ACKNOWLEDGE_MODE, acknowledgeMode);
        if (destination != null) {
            e.appendChild(destination.toElement(doc));
        }
        // messagedriven destination
        optionallyAddCollection(doc, e, envEntries);
        optionallyAddCollection(doc, e, ejbReferences);
        optionallyAddCollection(doc, e, ejbLocalReferences);
        if (securityIdentity != null) {
            e.appendChild(securityIdentity.toElement(doc));
        }
        optionallyAddCollection(doc, e, resourceReferences);
        optionallyAddCollection(doc, e, resourceEnvReferences);
        return e;
    }
}