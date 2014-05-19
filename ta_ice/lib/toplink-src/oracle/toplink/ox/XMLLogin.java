// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ox;

import oracle.toplink.internal.databaseaccess.*;
import oracle.toplink.internal.helper.Helper;
import oracle.toplink.ox.documentpreservation.DocumentPreservationPolicy;
import oracle.toplink.ox.platform.SAXPlatform;
import oracle.toplink.sessions.*;

/**
 * In OX, the platform determines which parsing method will be used, DOM vs SAX.
 *
 *<p><em>Code Sample</em><br>
 * <code>
 *
 * XMLLogin xmlLogin = new XMLLogin(new oracle.toplink.ox.platform.DOMPlatform);<br>
 * Project myProject = new MyTopLinkProject(xmlLogin)<br>
 *
 * </code>
 *
 * @see oracle.toplink.ox.platform.SAXPlatform
 * @see oracle.toplink.ox.platform.DOMPlatform
 *
 */
public class XMLLogin extends DatasourceLogin {
    private boolean equalNamespaceResolvers;

    private DocumentPreservationPolicy documentPreservationPolicy;
    /**
     * Default constructor.
     * Sets the platform to be the default platform which is oracle.toplink.ox.platform.SAXPlatform.
     */
    public XMLLogin() {
        this(new SAXPlatform());        
    }

    /**
     * Constructor, create a new XMLLogin based on the given platform.
     * Valid platforms are instances of oracle.toplink.ox.platform.DOMPlaform and
     * instances of oracle.toplink.ox.platform.SAXPlatform.
     * @param platform The platform to base this login on
     */
    public XMLLogin(Platform platform) {
        super(platform);
        equalNamespaceResolvers = true;
    }

    /**
     * INTERNAL:
     * Returns the appropriate accessor
     * @return an instance of oracle.toplink.internal.ox.XMLAccessor
     */
    public Accessor buildAccessor() {
        return new oracle.toplink.internal.ox.XMLAccessor();
    }

    /**
     * Return a String representation of the object.
     * @return a string representation of the receiver
     */
    public String toString() {
        return Helper.getShortClassName(this) + "(" + this.getUserName() + ")\n\t( " + this.getPlatformClassName() + ")";
    }
    
    public DocumentPreservationPolicy getDocumentPreservationPolicy() {
        return this.documentPreservationPolicy;
    }
    
    public void setDocumentPreservationPolicy(DocumentPreservationPolicy policy) {
        this.documentPreservationPolicy = policy;
    }

    public void setEqualNamespaceResolvers(boolean equalNRs) {
        this.equalNamespaceResolvers = equalNRs;
    }

    public boolean hasEqualNamespaceResolvers() {
        return equalNamespaceResolvers;
    }
}
