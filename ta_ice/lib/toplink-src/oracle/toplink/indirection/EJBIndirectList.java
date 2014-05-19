// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.indirection;

import java.util.*;

/**
 * EJBIndirectList provides all the functionality of IndirectList while
 * being EJB friendly.  That is, when it performs contains, remove, etc...
 * with entity beans, the container uses the bean's 'isIdentical' method
 * rather than the usual 'equals'.
 * <P>
 * This is necessary since serialization of the EJBObjects can cause the
 * equality test to return false, even though they represent the same
 * underlying entity bean.
 *
 * @see oracle.toplink.indirection.EJBIndirectList
 * @since TopLink Java 4.0
 */
public class EJBIndirectList extends IndirectList {

    /**
     * PUBLIC:
     * Construct an empty EJBIndirectList so that its internal data array
     * has size <tt>10</tt> and its standard capacity increment is zero.
     */
    public EJBIndirectList() {
        super();
    }

    /**
     * PUBLIC:
     * Construct an empty EJBIndirectList with the specified initial capacity and
     * with its capacity increment equal to zero.
     *
     * @param   initialCapacity   the initial capacity of the vector
     * @exception IllegalArgumentException if the specified initial capacity
     *               is negative
     */
    public EJBIndirectList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * PUBLIC:
     * Construct an empty EJBIndirectList with the specified initial capacity and
     * capacity increment.
     *
     * @param   initialCapacity     the initial capacity of the vector
     * @param   capacityIncrement   the amount by which the capacity is
     *                              increased when the vector overflows
     * @exception IllegalArgumentException if the specified initial capacity
     *               is negative
     */
    public EJBIndirectList(int initialCapacity, int capacityIncrement) {
        super(initialCapacity, capacityIncrement);
    }

    /**
     * PUBLIC:
     * Construct an EJBIndirectList containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c a collection containing the elements to be inserted into the list.
     */
    public EJBIndirectList(Collection c) {
        super.initialize(c);
    }

    /**
     * PUBLIC:
     * @see java.util.Vector#contains(java.lang.Object)
     */
    public boolean contains(Object elem) {
        return this.indexOf(elem) != -1;
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
     * @see java.util.Vector#indexOf(java.lang.Object)
     */
    public int indexOf(Object elem, int startIndex) {
        if (!((elem instanceof javax.ejb.EJBObject) || (elem instanceof javax.ejb.EJBLocalObject))) {
            return this.getDelegate().indexOf(elem, startIndex);
        }
        if (elem instanceof javax.ejb.EJBObject) {
            javax.ejb.EJBObject current = null;
            javax.ejb.EJBObject ejbObject = (javax.ejb.EJBObject)elem;

            int size = this.size();
            for (int i = startIndex; i < size; i++) {
                try {
                    current = (javax.ejb.EJBObject)this.get(i);
                    if (ejbObject.isIdentical(current)) {
                        return i;
                    }
                } catch (ClassCastException wrongTypeOfElement) {
                    //we will ignore this problem
                } catch (java.rmi.RemoteException otherError) {
                    //we will ignore this also
                }
            }
        }

        //needed for EJB2.0 relationships.
        if (elem instanceof javax.ejb.EJBLocalObject) {
            javax.ejb.EJBLocalObject current = null;
            javax.ejb.EJBLocalObject ejbObject = (javax.ejb.EJBLocalObject)elem;

            int size = this.size();
            for (int i = startIndex; i < size; i++) {
                try {
                    current = (javax.ejb.EJBLocalObject)this.get(i);
                    if (ejbObject.isIdentical(current)) {
                        return i;
                    }
                } catch (ClassCastException wrongTypeOfElement) {
                    //igonre
                } catch (javax.ejb.EJBException ex) {
                    //$$$ do the right thing
                }
            }
        }
        return -1;
    }

    /**
     * @see java.util.Vector#indexOf(java.lang.Object, int)
     */
    public synchronized int indexOf(Object elem) {
        return this.indexOf(elem, 0);
    }

    /**
     * @see java.util.Vector#lastIndexOf(java.lang.Object)
     */
    public int lastIndexOf(Object elem) {
        return this.getDelegate().lastIndexOf(elem, this.size() - 1);
    }

    /**
     * @see java.util.Vector#lastIndexOf(java.lang.Object, int)
     */
    public synchronized int lastIndexOf(Object elem, int startIndex) {
        if (!((elem instanceof javax.ejb.EJBObject) || (elem instanceof javax.ejb.EJBLocalObject))) {
            return this.getDelegate().lastIndexOf(elem, startIndex);
        }
        if (elem instanceof javax.ejb.EJBObject) {
            javax.ejb.EJBObject current = null;
            javax.ejb.EJBObject ejbObject = (javax.ejb.EJBObject)elem;

            for (int i = startIndex; i >= 0; i--) {
                try {
                    current = (javax.ejb.EJBObject)this.get(i);
                    if (ejbObject.isIdentical(current)) {
                        return i;
                    }
                } catch (ClassCastException wrongTypeOfElement) {
                    //we will ignore this problem
                } catch (java.rmi.RemoteException otherError) {
                    //we will ignore this also
                }
            }
        }

        //needed for EJB2.0 relationships.
        if (elem instanceof javax.ejb.EJBLocalObject) {
            javax.ejb.EJBLocalObject current = null;
            javax.ejb.EJBLocalObject ejbObject = (javax.ejb.EJBLocalObject)elem;

            for (int i = startIndex; i >= 0; i--) {
                try {
                    current = (javax.ejb.EJBLocalObject)this.get(i);
                    if (ejbObject.isIdentical(current)) {
                        return i;
                    }
                } catch (ClassCastException wrongTypeOfElement) {
                    //we will ignore this problem
                } catch (javax.ejb.EJBException ex) {
                    //$$$ do the right thing
                }
            }
        }
        return -1;
    }

    /**
     * @see java.util.Vector#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        int index = this.indexOf(o);
        if ((index != -1) && (this.remove(index) != null)) {
            return true;
        }
        return false;
    }

    /**
     * @see java.util.Vector#removeElement(java.lang.Object)
     */
    public synchronized boolean removeElement(Object obj) {
        return this.remove(obj);
    }
}