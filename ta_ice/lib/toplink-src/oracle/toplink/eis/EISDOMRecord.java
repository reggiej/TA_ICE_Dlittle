// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.eis;

import java.lang.reflect.*;
import java.security.AccessController;
import java.security.PrivilegedActionException;

import javax.resource.cci.*;
import org.w3c.dom.*;

import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedGetMethod;
import oracle.toplink.internal.security.PrivilegedMethodInvoker;
import oracle.toplink.ox.record.XMLRecord;

/**
 * <p>An <code>EISDOMRecord</code> is a wrapper for a DOM tree.  It provides a 
 * Record/Map API on an XML DOM element.  This can be used from the 
 * platform to wrap adapter XML/DOM records to be used with TopLink XML.
 * 
 * @author James
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class EISDOMRecord extends oracle.toplink.ox.record.DOMRecord implements DOMRecord, MappedRecord {

    /** The original adapter record. */
    protected Record record;

    /** The record name. */
    protected String recordName;

    /** The record name. */
    protected String recordShortDescription;

    /** Used for introspected DOM records. */
    protected static Method domMethod;

    /**
     * Default constructor.
     */
    public EISDOMRecord() {
        super();
        setRecordName("");
        setRecordShortDescription("");
    }

    /**
     * Create a TopLink record from the JCA adapter record and DOM tree.
     */
    public EISDOMRecord(Record record, Element dom) {
        super(dom);
        this.record = record;
        this.recordName = record.getRecordName();
        this.recordShortDescription = record.getRecordShortDescription();
    }

    /**
     * Create a TopLink record from a DOM tree.
     */
    public EISDOMRecord(Element dom) {
        super(dom);
        this.recordName = "";
        this.recordShortDescription = "";
    }

    /**
     * Create a TopLink record from the JCA adapter record.
     * This attempts to introspect the record to retrieve the DOM tree.
     */
    public EISDOMRecord(Record record) {
        this.record = record;
        this.recordName = record.getRecordName();
        this.recordShortDescription = record.getRecordShortDescription();
        if (domMethod == null) {
            try {
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try{
                        domMethod = (Method)AccessController.doPrivileged(new PrivilegedGetMethod(record.getClass(), "getDom", null, false));
                    }catch (PrivilegedActionException ex){
                        throw (Exception)ex.getCause();
                    }
                }else{
                    domMethod = PrivilegedAccessHelper.getMethod(record.getClass(), "getDom", null, false);
                }
            } catch (Exception notFound) {
                try {
                    if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                        try{
                            domMethod = (Method)AccessController.doPrivileged(new PrivilegedGetMethod(record.getClass(), "getDOM", null, false));
                        }catch (PrivilegedActionException ex){
                            throw (Exception)ex.getCause();
                        }
                    }else{
                        domMethod = PrivilegedAccessHelper.getMethod(record.getClass(), "getDOM", null, false);
                    }
                 } catch (Exception cantFind) {
                    throw new EISException(cantFind);
                }
            }
        }
        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try{
                    setDOM((Element)AccessController.doPrivileged(new PrivilegedMethodInvoker(domMethod, record, null)));
                }catch (PrivilegedActionException ex){
                    throw (Exception)ex.getCause();
                }
            }else{
                setDOM((Element)PrivilegedAccessHelper.invokeMethod(domMethod, record, null));
            }
        } catch (Exception error) {
            throw new EISException(error);
        }
    }

    /**
     * Return the JCA adapter record.
     */
    public Record getRecord() {
        return record;
    }

    /**
     * Set the JCA adapter record.
     */
    public void setRecord(Record record) {
        this.record = record;
        this.recordName = record.getRecordName();
        this.recordShortDescription = record.getRecordShortDescription();
    }

    /**
     * Forward to the record.
     */
    public String getRecordShortDescription() {
        return recordShortDescription;
    }

    /**
     * Forward to the record.
     */
    public void setRecordShortDescription(String recordShortDescription) {
        this.recordShortDescription = recordShortDescription;
    }

    /**
     * Forward to the record.
     */
    public String getRecordName() {
        return recordName;
    }

    /**
     * Forward to the record.
     */
    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }

    /**
     * INTERNAL:
     * Build the nested record, this can be overwriten by subclasses to use their subclass instance.
     */
    public XMLRecord buildNestedRow(Element element) {
        if (getRecord() != null) {
            return new EISDOMRecord(getRecord(), element);
        } else {
            return new EISDOMRecord(element);
        }
    }
}