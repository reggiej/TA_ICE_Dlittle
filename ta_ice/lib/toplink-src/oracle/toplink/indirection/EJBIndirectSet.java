// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.indirection;

import java.util.*;

/**
 * EJBIndirectSet provides all the functionality of IndirectSet while
 * being EJB friendly.  That is, when it performs contains, remove, etc...
 * with entity beans, the container uses the bean's 'isIdentical' method
 * rather than the usual 'equals'.
 * <P>
 * This is necessary since serialization of the EJBObjects can cause the
 * equality test to return false, even though they represent the same
 * underlying entity bean.
 *
 * @see oracle.toplink.indirection.IndirectSet
 * @since TopLink Java 4.0
 */
public class EJBIndirectSet extends IndirectSet {

    /**
     * Construct an empty IndirectSet.
     */
    public EJBIndirectSet() {
        super();
    }

    /**
     * Construct an empty IndirectSet with the specified initial capacity.
     *
     * @param   initialCapacity   the initial capacity of the set
     * @exception IllegalArgumentException if the specified initial capacity
     *               is negative
     */
    public EJBIndirectSet(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Construct an empty IndirectSet with the specified initial capacity and
     * load factor.
     *
     * @param   initialCapacity     the initial capacity of the set
     * @param   loadFactor   the load factor of the set
     * @exception IllegalArgumentException if the specified initial capacity
     *               is negative
     */
    public EJBIndirectSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Construct an IndirectSet containing the elements of the specified collection.
     *
     * @param   c   the initial elements of the set
     */
    public EJBIndirectSet(Collection c) {
        super(c);
    }

    /**
     * @see java.util.Set#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        if (o instanceof javax.ejb.EJBObject) {
            javax.ejb.EJBObject current = null;
            javax.ejb.EJBObject ejbObject = (javax.ejb.EJBObject)o;

            Iterator e = this.iterator();
            while (e.hasNext()) {
                try {
                    current = (javax.ejb.EJBObject)e.next();
                    if (ejbObject.isIdentical(current)) {
                        return true;
                    }
                } catch (ClassCastException wrongTypeOfElement) {
                    //we will ignore this problem
                } catch (java.rmi.RemoteException otherError) {
                    //we will ignore this also
                }
            }
        }
        //For ejb2.0 relationships
        else if (o instanceof javax.ejb.EJBLocalObject) {
            javax.ejb.EJBLocalObject current = null;
            javax.ejb.EJBLocalObject ejbLocalObject = (javax.ejb.EJBLocalObject)o;

            Iterator e = this.iterator();
            while (e.hasNext()) {
                try {
                    current = (javax.ejb.EJBLocalObject)e.next();
                    if (ejbLocalObject.isIdentical(current)) {
                        return true;
                    }
                } catch (ClassCastException wrongTypeOfElement) {
                    //we will ignore this here
                } catch (javax.ejb.EJBException otherError) {
                    //we can ignore this too
                }
            }
        } else {
            return this.getDelegate().contains(o);
        }
        return false;
    }

    /**
     * @see java.util.Vector#containsAll(java.util.Collection)
     */
    public synchronized boolean containsAll(Collection c) {
        Iterator e = c.iterator();
        while (e.hasNext()) {
            if (!this.contains(e.next())) {
                return false;
            }
        }
        return true;
    }

    /**
     * @see java.util.Set#remove(java.lang.Object)
     */
    public synchronized boolean remove(Object o) {
        if (o instanceof javax.ejb.EJBObject) {
            javax.ejb.EJBObject current = null;
            javax.ejb.EJBObject ejbObject = (javax.ejb.EJBObject)o;

            Iterator e = this.iterator();
            while (e.hasNext()) {
                try {
                    current = (javax.ejb.EJBObject)e.next();
                    if (ejbObject.isIdentical(current)) {
                        e.remove();
                        return true;
                    }
                } catch (ClassCastException wrongTypeOfElement) {
                    //we will ignore this problem
                } catch (java.rmi.RemoteException otherError) {
                    //we will ignore this also
                }
            }
        } else if (o instanceof javax.ejb.EJBLocalObject) {
            javax.ejb.EJBLocalObject current = null;
            javax.ejb.EJBLocalObject ejbLocalObject = (javax.ejb.EJBLocalObject)o;

            Iterator e = this.iterator();
            while (e.hasNext()) {
                try {
                    current = (javax.ejb.EJBLocalObject)e.next();
                    if (ejbLocalObject.isIdentical(current)) {
                        e.remove();
                        return true;
                    }
                } catch (ClassCastException wrongTypeOfElement) {
                    //we will ignore this problem
                } catch (javax.ejb.EJBException otherError) {
                    //we will ignore this also
                }
            }
        } else {
            return super.remove(o);
        }
        return false;
    }
}