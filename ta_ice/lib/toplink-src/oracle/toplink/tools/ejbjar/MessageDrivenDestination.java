// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * INTERNAL:
 * Represents message-driven-destination tag object
 */
public class MessageDrivenDestination extends DomObject {
    String destinationType;// Required
    String subscriptionDurability;// Optional

    /**
     * @return String the destination type String
     */
    public String getDestinationType() {
        return destinationType;
    }

    /**
     * @return String the subscriptionDurability String, or null if not specified
     */
    public String getSubscriptionDurability() {
        return subscriptionDurability;
    }

    /**
     * @param destType the destination type String
     */
    public void setDestinationType(String destType) {
        destinationType = destType;
    }

    /**
     * @param dur the subscriptionDurability String
     */
    public void setSubscriptionDurability(String dur) {
        subscriptionDurability = dur;
    }

    /**
     * Load the data for this instance from the specified element.
     * @param e the DOM element
     */
    public void loadFromElement(Element e) {
        destinationType = stringFromElement(e, DESTINATION_TYPE);
        subscriptionDurability = optionalStringFromElement(e, SUBSCRIPTION_DURABILITY);
    }

    /**
     * Return the data from this instance as a DOM element.
     * @param doc a Document instance used to create elements
     */
    public Element toElement(Document doc) {
        Element e = doc.createElement(MESSAGE_DRIVEN_DESTINATION);
        addText(doc, e, DESTINATION_TYPE, destinationType);
        optionallyAddText(doc, e, SUBSCRIPTION_DURABILITY, subscriptionDurability);
        return e;
    }
}