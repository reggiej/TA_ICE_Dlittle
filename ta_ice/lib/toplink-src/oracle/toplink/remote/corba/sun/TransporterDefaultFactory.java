// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.remote.corba.sun;

import oracle.toplink.internal.remote.Transporter;

/**
 * INTERNAL:
* oracle/toplink/internal/remote/TransporterDefaultFactory.java
* Generated by the IDL-to-Java compiler (portable), version "3.0"
* from d:/StarTeam/DIDev/Pine/TopLink/DISource/IDLs/CorbaRemoteSessionControllerSun.idl
* Thursday, May 3, 2001 1:36:59 PM EDT
*/
public class TransporterDefaultFactory implements org.omg.CORBA.portable.ValueFactory {
    public java.io.Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value(new Transporter());
    }
}