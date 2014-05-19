// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.sdk;

import java.security.AccessController;
import java.security.PrivilegedActionException;

import oracle.toplink.sessions.*;
import oracle.toplink.internal.databaseaccess.Accessor;
import oracle.toplink.internal.databaseaccess.Platform;
import oracle.toplink.internal.helper.Helper;
import oracle.toplink.internal.helper.ClassConstants;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedNewInstanceFromClass;

/**
 * <code>SDKLogin</code> does little more than
 * parameterize <code>Accessor</code> to be used by the
 * <code>Session</code>.
 *
 * @see SDKAccessor
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.eis}
 */
public class SDKLogin extends DatasourceLogin {

    /** The class of the accessor to be built */
    private Class accessorClass;

    /**
     * Default constructor.
     */
    public SDKLogin() {
        this(new SDKPlatform());
    }

    /**
     * Constructor.
     */
    public SDKLogin(Platform platform) {
        super(platform);
        this.initialize();
    }

    /**
     * Build and return an appropriate Accessor.
     */
    public Accessor buildAccessor() {
        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try{
                    return (Accessor)AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(this.getAccessorClass()));
                }catch (PrivilegedActionException ex){
                    throw (RuntimeException)ex.getCause();
                }
            }else{
                return (Accessor)PrivilegedAccessHelper.newInstanceFromClass(this.getAccessorClass());
            }
        } catch (InstantiationException ie) {
            throw SDKDataStoreException.instantiationExceptionWhenInstantiatingAccessor(ie, this.getAccessorClass());
        } catch (IllegalAccessException iae) {
            throw SDKDataStoreException.illegalAccessExceptionWhenInstantiatingAccessor(iae, this.getAccessorClass());
        }
    }

    /**
     * Return the class of the accessor to be built.
     */
    public Class getAccessorClass() {
        return accessorClass;
    }

    /**
     * Return the default Accessor Class.
     */
    protected Class getDefaultAccessorClass() {
        return ClassConstants.SdkAccessor_Class;
    }

    /**
     * Initialize the login.
     */
    protected void initialize() {
        accessorClass = this.getDefaultAccessorClass();
    }

    /**
     * Build and return an exception indicating an invalid accessor class.
     */
    public SDKQueryException invalidAccessClass(Class expected, Class actual) {
        return SDKQueryException.invalidAccessorClass(expected, actual);
    }

    /**
     * Set the class of the accessor to be built.
     */
    public void setAccessorClass(Class accessorClass) {
        if (!Helper.classImplementsInterface(accessorClass, ClassConstants.Accessor_Class)) {
            throw this.invalidAccessClass(ClassConstants.Accessor_Class, accessorClass);
        }
        this.accessorClass = accessorClass;
    }

    /**
     * Return a String representation of the object.
     * @return a string representation of the receiver
     */
    public String toString() {
        return Helper.getShortClassName(this) + "(" + this.getUserName() + ")";
    }
}