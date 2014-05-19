// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.xml;

import oracle.toplink.sdk.SDKSequence;
import oracle.toplink.queryframework.*;
import oracle.toplink.internal.helper.ClassConstants;

/**
 * <p>
 * <b>Purpose</b>:
 * <p>
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.ox}
 */
public class XMLSequence extends SDKSequence {
    protected String rootElementName = "SEQUENCE";

    public XMLSequence() {
        super();
    }

    public XMLSequence(String name) {
        super(name);
    }

    public XMLSequence(String name, int size) {
        super(name, size);
    }

    public XMLSequence(String name, String rootElementName, String nameElementName, String counterElementName) {
        super(name, nameElementName, counterElementName);
        setRootElementName(rootElementName);
    }

    public XMLSequence(String name, int size, String rootElementName, String nameElementName, String counterElementName) {
        super(name, size, nameElementName, counterElementName);
        setRootElementName(rootElementName);
    }

    public boolean equals(Object obj) {
        if (obj instanceof XMLSequence) {
            XMLSequence other = (XMLSequence)obj;
            if (super.equals(other)) {
                return getRootElementName().equals(other.getRootElementName());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Build and return the default call for reading the value
     * of a given sequence number.
     */
    protected Call buildSelectCall() {
        XMLDataReadCall call = new XMLDataReadCall();
        call.setRootElementName(this.getRootElementName());
        call.setPrimaryKeyElementName(this.getNameElementName());
        call.setResultElementName(this.getCounterElementName());
        call.addResultElementType(this.getCounterElementName(), ClassConstants.BIGDECIMAL);
        return call;
    }

    /**
     * Build and return the default call for updating the value
     * of a given sequence number.
     */
    protected Call buildUpdateCall() {
        XMLDataUpdateCall call = new XMLDataUpdateCall();
        call.setRootElementName(this.getRootElementName());
        call.setPrimaryKeyElementName(this.getNameElementName());
        return call;
    }

    /**
     * Return the name of the element holding the sequence counter.
     */
    protected String getCounterElementName() {
        return this.getCounterFieldName();
    }

    /**
     * Return the name of the element holding the sequence name.
     */
    protected String getNameElementName() {
        return this.getNameFieldName();
    }

    /**
     * Return the root element name for the sequence number documents.
     */
    public String getRootElementName() {
        return rootElementName;
    }

    /**
     * Set the name of the element holding the sequence counter.
     */
    protected void setCounterElementName(String counterElementName) {
        this.setCounterFieldName(counterElementName);
    }

    /**
     * Set the name of the element holding the sequence name.
     */
    protected void setNameElementName(String nameElementName) {
        this.setNameFieldName(nameElementName);
    }

    /**
     * Specify the root element name for the sequence numbers.
     */
    public void setRootElementName(String rootElementName) {
        this.rootElementName = rootElementName;
    }
}