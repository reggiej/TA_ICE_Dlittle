// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.xml;

import java.util.*;
import oracle.toplink.exceptions.DescriptorException;

/**
 * <code>XMLDescriptor</code> extends
 * the <code>SDKDescriptor</code> protocol with a number of defaults and
 * helper methods that simplify the use of XML data store.
 *
 * @see XMLCall
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.ox}
 */
public class XMLDescriptor extends oracle.toplink.sdk.SDKDescriptor {

    /**
     * PUBLIC:
     * Default constructor.
     */
    public XMLDescriptor() {
        super();
        // Set False to prevent order issues when writing out to files
        this.shouldOrderMappings = false;
    }

    /**
     * PUBLIC:
     * Add a primary key element for the descriptor.
     * This should be called for each element that makes up the primary key.
     * The order in which these elements are added may be significant.
     */
    public void addPrimaryKeyElementName(String elementName) {
        this.addPrimaryKeyFieldName(elementName);
    }

    /**
     * PUBLIC:
     * Return the names of all the primary key elements.
     */
    public Vector getPrimaryKeyElementNames() {
        return this.getPrimaryKeyFieldNames();
    }

    /**
     * PUBLIC:
     * Return the root element name for the class of objects the descriptor maps.
     */
    public String getRootElementName() throws DescriptorException {
        return this.getTableName();
    }

    /**
     * PUBLIC:
     * Return sequence number element name.
     */
    public String getSequenceNumberElementName() {
        return this.getSequenceNumberFieldName();
    }

    /**
     * Set all the necessary calls with defaults.
     */
    protected void initializeQueryManager() {
        this.setReadObjectCall(new XMLReadCall());
        this.setReadAllCall(new XMLReadAllCall());
        this.setInsertCall(new XMLInsertCall());
        this.setUpdateCall(new XMLUpdateCall());
        this.setDeleteCall(new XMLDeleteCall());
        this.setDoesExistCall(new XMLDoesExistCall());
    }

    /**
     * PUBLIC:
     * Set the Java class that this descriptor maps.
     * Every descriptor maps one and only one class.
     */
    public void setJavaClass(Class javaClass) {
        super.setJavaClass(javaClass);
        this.getQueryManager().getReadObjectQuery().setReferenceClass(javaClass);
        this.getQueryManager().getReadAllQuery().setReferenceClass(javaClass);
    }

    /**
     * PUBLIC:
     * Specify the primary key element for the descriptor.
     * This should only be called if primary key is made up of a single element;
     * otherwise <code>#addPrimaryKeyElementName()</code> should be called.
     *
     * @see #addPrimaryKeyElementName(String)
     */
    public void setPrimaryKeyElementName(String elementName) {
        this.setPrimaryKeyFieldName(elementName);
    }

    /**
     * PUBLIC:
     * Specify an array of all the primary key element names,
     * if the primary key is composed of multiple elements.
     * The order of these elements may be significant.
     *
     * @see #addPrimaryKeyElementName(String)
     */
    public void setPrimaryKeyElementNames(String[] primaryKeyElementNames) {
        this.setPrimaryKeyFieldNames(this.convertToVector(primaryKeyElementNames));
    }

    /**
     * PUBLIC:
     * Specify a vector of all the primary key element names,
     * if the primary key is composed of multiple elements.
     * The order of these elements may be significant.
     *
     * @see #addPrimaryKeyElementName(String)
     */
    public void setPrimaryKeyElementNames(Vector primaryKeyElementNames) {
        this.setPrimaryKeyFieldNames(primaryKeyElementNames);
    }

    /**
     * PUBLIC:
     * Specify the root element name for the class of objects the descriptor maps.
     */
    public void setRootElementName(String rootElementName) throws DescriptorException {
        this.setTableName(rootElementName);
    }

    /**
     * PUBLIC:
     * Set the sequence number element name.
     * This is the field whose value is to be generated by TopLink.
     * This is normally the primary key element for the descriptor.
     */
    public void setSequenceNumberElementName(String elementName) {
        this.setSequenceNumberFieldName(elementName);
    }
}