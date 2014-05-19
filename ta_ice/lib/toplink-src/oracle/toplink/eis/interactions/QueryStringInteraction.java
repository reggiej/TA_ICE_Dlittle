// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.eis.interactions;

import java.util.*;
import java.io.*;
import oracle.toplink.internal.databaseaccess.Accessor;
import oracle.toplink.internal.databaseaccess.QueryStringCall;
import oracle.toplink.internal.helper.*;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.internal.sessions.AbstractSession;

/**
 * Defines the specification for a call to a JCA interaction that uses a query string.
 * This can be used for generic query translation support (i.e. VSAM, BETRIEVE, ADA, etc.)
 * Arguments are defined in the query string through #<field-name> (i.e. #EMP_ID)
 * Translates the query string from the query arguments.
 * Builds the input and output records.
 *
 * @author James
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class QueryStringInteraction extends MappedInteraction implements QueryStringCall {
    protected String queryString;

    /**
     * Default constructor.
     */
    public QueryStringInteraction() {
        super();
        this.queryString = "";
    }

    /**
     * Construct interaction with the query string.
     */
    public QueryStringInteraction(String queryString) {
        super();
        this.queryString = queryString;
    }

    /**
     * PUBLIC:
     * Return the query string.
     */
    public String getQueryString() {
        return queryString;
    }

    /**
     * PUBLIC:
     * Set the query string.
     */
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    /**
     * Allow the call to translate the query arguments.
     */
    public void translate(AbstractRecord translationRow, AbstractRecord modifyRow, AbstractSession session) {
        translateQueryString(translationRow, modifyRow, session);
    }

    /**
     * Translate the custom query markers.
     */
    public void prepare(AbstractSession session) {
        if (isPrepared()) {
            return;
        }
        translateCustomQuery();
        setIsPrepared(true);
    }

    /**
     * Return the string for logging purposes.
     */
    public String getLogString(Accessor accessor) {
        StringWriter writer = new StringWriter();
        writer.write("Executing ");
        writer.write(toString());
        writer.write(Helper.cr());
        writer.write("\tspec => ");
        writer.write(String.valueOf(getInteractionSpec()));
        writer.write(Helper.cr());
        writer.write("\tquery => ");
        writer.write(getQueryString());
        writer.write(Helper.cr());
        writer.write("\tparameters => [");
        if (hasParameters()) {
            // Unfortunately vectors cannot print if they have nulls in them...
            for (Enumeration paramsEnum = getParameters().elements(); paramsEnum.hasMoreElements();) {
                Object parameter = paramsEnum.nextElement();
                writer.write(String.valueOf(parameter));
                if (paramsEnum.hasMoreElements()) {
                    writer.write(", ");
                }
            }
        }
        writer.write("]");
        return writer.toString();
    }

    public boolean isQueryStringCall() {
        return true;
    }
}