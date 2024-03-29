/*
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * "The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations under
 * the License.
 *
 * The Original Code is ICEfaces 1.5 open source software code, released
 * November 5, 2006. The Initial Developer of the Original Code is ICEsoft
 * Technologies Canada, Corp. Portions created by ICEsoft are Copyright (C)
 * 2004-2006 ICEsoft Technologies Canada, Corp. All Rights Reserved.
 *
 * Contributor(s): _____________________.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"
 * License), in which case the provisions of the LGPL License are
 * applicable instead of those above. If you wish to allow use of your
 * version of this file only under the terms of the LGPL License and not to
 * allow others to use your version of this file under the MPL, indicate
 * your decision by deleting the provisions above and replace them with
 * the notice and other provisions required by the LGPL License. If you do
 * not delete the provisions above, a recipient may use your version of
 * this file under either the MPL or the LGPL License."
 *
 */

package com.icesoft.faces.context;

import javax.servlet.ServletContext;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;

import com.icesoft.util.SeamUtilities;
import com.icesoft.faces.webapp.xmlhttp.PersistentFacesState;
import com.icesoft.faces.context.BridgeFacesContext;

/**
 * This is the ICEfaces implementation of the FacesContextFactory.  We take
 * advantage of the delegation design provided by the JSF spec to create our
 * factory with a delegate factory. We then check to see if requests are being
 * handled by one of our PersistentFaces classes. This is accomplished by
 * inserting a known attribute into the request.  If the attribute is present,
 * then we know that the request for a FacesContext originated from an ICEfaces
 * servlet (or portlet).  If not, we delegate the call to the "parent" factory.
 * This allows us to run in "plain" Faces mode while still making use of our own
 * ViewHandler and renders.
 */
public class FacesContextFactoryImpl extends FacesContextFactory {
    private FacesContextFactory delegate;

    public FacesContextFactoryImpl() {
    }

    public FacesContextFactoryImpl(FacesContextFactory delegate) {
        this.delegate = delegate;
    }

    public FacesContext getFacesContext(Object context, Object request,
                                        Object response,
                                        Lifecycle lifecycle) throws FacesException {
        //In the case of Spring Web Flow 2.0, return the BridgeFacesContext
        //already created for this request
        if (context instanceof ServletContext) {
            if (SeamUtilities.isSpringEnvironment())  {
                PersistentFacesState persistentState = 
                        PersistentFacesState.getInstance();
                if (null != persistentState)  {
                    BridgeFacesContext bcontext = 
                            (BridgeFacesContext) persistentState.getFacesContext();
                    bcontext.setCurrentInstance();
                    return bcontext;
                }
            }
        }
        if (delegate == null) {
            throw new UnsupportedOperationException("ICEfaces cannot use this factory for instantiating FacesContext objects.");
        } else {
            return delegate.getFacesContext(context, request, response, lifecycle);
        }
    }
}
