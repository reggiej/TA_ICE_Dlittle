// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.eis;

import javax.resource.cci.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <p><code>DOMRecord</code> is an extension of the JCA Record interface that 
 * provides support for XML data.  This is required as JCA currently has no 
 * formal support for XML records.  A JCA adapter will normally have its own 
 * XML/DOM record interface;  the TopLink record <code>EISDOMRecord</code> 
 * implements this interface and can be constructed with a DOM instance 
 * retrieved from the adapter XML/DOM record and converted in the platform.
 *
 * @see EISDOMRecord
 * 
 * @author James
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public interface DOMRecord extends Record {
    public Node getDOM();

    public void setDOM(Element dom);
}