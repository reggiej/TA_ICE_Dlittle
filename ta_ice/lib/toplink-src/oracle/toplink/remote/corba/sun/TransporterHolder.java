// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.remote.corba.sun;


/**
 * INTERNAL:
* oracle/toplink/internal/remote/TransporterHolder.java
* Generated by the IDL-to-Java compiler (portable), version "3.0"
* from d:/StarTeam/DIDev/Pine/TopLink/DISource/IDLs/CorbaRemoteSessionControllerSun.idl
* Thursday, May 3, 2001 1:36:59 PM EDT
*/
public final class TransporterHolder implements org.omg.CORBA.portable.Streamable {
    public oracle.toplink.internal.remote.Transporter value = null;

    public TransporterHolder() {
    }

    public TransporterHolder(oracle.toplink.internal.remote.Transporter initialValue) {
        value = initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i) {
        value = oracle.toplink.remote.corba.sun.TransporterHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o) {
        oracle.toplink.remote.corba.sun.TransporterHelper.write(o, value);
    }

    public org.omg.CORBA.TypeCode _type() {
        return oracle.toplink.remote.corba.sun.TransporterHelper.type();
    }
}