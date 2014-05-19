// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.sessionconfiguration;


/**
 * INTERNAL:
 * <p><b>Purpose</b>: Provide a mechanism for retrieving the DTD file
 * from the classpath
 *
 * @see org.xml.sax.EntityResolver
 * @since TopLink 10.1.3
 * @author Guy Pelletier
 */
public class WASTopLinkEntityResolver extends TopLinkEntityResolver {
    protected static final String dtdFileName_903 = "toplink-was-ejb-jar_903.dtd";
    protected static final String dtdFileName_904 = "toplink-was-ejb-jar_904.dtd";
    protected static final String docTypeId_903 = "-//Oracle Corp.//DTD TopLink 4.5 CMP for WebSphere//EN";
    protected static final String docTypeId_904 = "-//Oracle Corp.//DTD TopLink CMP WebSphere 9.0.4//EN";

    /**
     * INTERNAL:
     */
    public WASTopLinkEntityResolver() {
        super();
    }

    /**
     * INTERNAL:
     */
    protected void populateLocalResources() {
        m_localResources.put(docTypeId_903, dtdFileName_903);
        m_localResources.put(docTypeId_904, dtdFileName_904);
    }
}