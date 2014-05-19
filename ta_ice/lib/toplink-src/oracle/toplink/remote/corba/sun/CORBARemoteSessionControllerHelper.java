// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.remote.corba.sun;


/**
 * INTERNAL:
* oracle/toplink/remote/corba/sun/CORBARemoteSessionControllerHelper.java
* Generated by the IDL-to-Java compiler (portable), version "3.0"
* from CorbaRemoteSessionControllerSun.idl
* Monday, November 19, 2001 1:51:44 o'clock PM EST
*/
abstract public class CORBARemoteSessionControllerHelper {
    private static String _id = "IDL:oracle/toplink/remote/corba/sun/CORBARemoteSessionController:1.0";

    public static void insert(org.omg.CORBA.Any a, oracle.toplink.remote.corba.sun.CORBARemoteSessionController that) {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
        a.type(type());
        write(out, that);
        a.read_value(out.create_input_stream(), type());
    }

    public static oracle.toplink.remote.corba.sun.CORBARemoteSessionController extract(org.omg.CORBA.Any a) {
        return read(a.create_input_stream());
    }

    private static org.omg.CORBA.TypeCode __typeCode = null;

    synchronized public static org.omg.CORBA.TypeCode type() {
        if (__typeCode == null) {
            __typeCode = org.omg.CORBA.ORB.init().create_interface_tc(oracle.toplink.remote.corba.sun.CORBARemoteSessionControllerHelper.id(), "CORBARemoteSessionController");
        }
        return __typeCode;
    }

    public static String id() {
        return _id;
    }

    public static oracle.toplink.remote.corba.sun.CORBARemoteSessionController read(org.omg.CORBA.portable.InputStream istream) {
        return narrow(istream.read_Object(_CORBARemoteSessionControllerStub.class));
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream, oracle.toplink.remote.corba.sun.CORBARemoteSessionController value) {
        ostream.write_Object((org.omg.CORBA.Object)value);
    }

    public static oracle.toplink.remote.corba.sun.CORBARemoteSessionController narrow(org.omg.CORBA.Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof oracle.toplink.remote.corba.sun.CORBARemoteSessionController) {
            return (oracle.toplink.remote.corba.sun.CORBARemoteSessionController)obj;
        } else if (!obj._is_a(id())) {
            throw new org.omg.CORBA.BAD_PARAM();
        } else {
            org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate();
            return new oracle.toplink.remote.corba.sun._CORBARemoteSessionControllerStub(delegate);
        }
    }
}