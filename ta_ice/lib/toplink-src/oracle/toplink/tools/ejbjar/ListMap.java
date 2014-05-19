// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.tools.ejbjar;

import java.util.*;

/**
 * INTERNAL
 * ListMap acts like a Map but also captures
 * the order in which entries are added to the Map.
 * This class behaves just like a Map with additional
 * protocol for taking advantage of the ordering information:
 *     getKey(int)
 *     getValue(int)
 *     indexOfKey(Object)
 *     orderedKeys()
 *     put(int, Object)
 *     remove(int)
 *
 * NOTE: If an entry is added to the Map that has the same
 * key as an entry already in the Map, the previous entry is
 * replaced and the ordering information remains unchanged.
 *
 * Note: It's ported from MW model. Please use java.util.LinkedHashMap (since jdk 1.4) instead if
 * you need ordered hash map. King-03-14-03.
 *
 */
public class ListMap implements Map {
    protected Map delegate;// the delegate
    protected List orderedKeys;// preserves the entry order

    public ListMap() {
        this(10);
    }

    public ListMap(int initialSize) {
        super();
        orderedKeys = new ArrayList(initialSize);
        delegate = new HashMap(initialSize * 2);
    }

    public ListMap(Map t) {
        this();
        this.putAll(t);
    }

    public void clear() {
        delegate.clear();
        orderedKeys.clear();
    }

    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    /**
     * This Set will *not* reflect the entry order.
     * Use the Iterator returned by #orderedKeys().
     */
    public Set entrySet() {
        return delegate.entrySet();
    }

    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    public Object get(Object key) {
        return delegate.get(key);
    }

    /**
     * Return the key at the specified index.
     * The index reflects the order in which
     * the key's entry was added to the Map.
     */
    public Object getKey(int index) {
        return orderedKeys.get(index);
    }

    /**
     * Return the value at the specified index.
     * The index reflects the order in which
     * the value's entry was added to the Map.
     */
    public Object getValue(int index) {
        return delegate.get(orderedKeys.get(index));
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * Return the index of the specified key.
     * The index reflects the order in which
     * the key's entry was added to the Map.
     */
    public int indexOfKey(Object key) {
        return orderedKeys.indexOf(key);
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /**
     * This Set will *not* reflect the entry order.
     * Use the Iterator returned by #orderedKeys().
     */
    public Set keySet() {
        return delegate.keySet();
    }

    /**
     * Return an iterator on the keys in the Map
     * that returns the keys in the order in which
     * their entries were added to the Map.
     */
    public Iterator orderedKeys() {
        return orderedKeys.iterator();
    }

    /**
     * Replace the value at the specified index
     * with a new value.
     * The index reflects the order in which
     * the value's entry was added to the Map.
     */
    public Object put(int index, Object value) {
        return this.put(this.getKey(index), value);
    }

    public Object put(Object key, Object value) {
        if (!delegate.containsKey(key)) {// use hashing...
            orderedKeys.add(key);
        }
        return delegate.put(key, value);
    }

    /**
     * If the specified Map is another ListMap
     * we preserve its order while adding its entries.
     */
    public void putAll(Map t) {
        if (t instanceof ListMap) {
            ListMap blmt = (ListMap)t;
            for (Iterator stream = blmt.orderedKeys(); stream.hasNext();) {
                Object key = stream.next();
                this.put(key, blmt.get(key));
            }
        } else {
            for (Iterator stream = t.entrySet().iterator(); stream.hasNext();) {
                Map.Entry e = (Map.Entry)stream.next();
                this.put(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * Remove the entry at the specified index.
     * The index reflects the order in which
     * the entry was added to the Map.
     */
    public Object remove(int index) {
        Object key = this.getKey(index);
        orderedKeys.remove(index);
        return delegate.remove(key);
    }

    public Object remove(Object key) {
        if (delegate.containsKey(key)) {// use hashing...
            orderedKeys.remove(key);
        }
        return delegate.remove(key);
    }

    /**
     * Remove all the keys in the specified Collection.
     * Return whether the ListMap changed as a result.
     */
    public boolean removeAll(Collection keys) {
        boolean changed = false;
        for (Iterator stream = keys.iterator(); stream.hasNext();) {
            if (this.remove(stream.next()) != null) {
                changed = true;
            }
        }
        return changed;
    }

    public int size() {
        return delegate.size();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append("{orderedKeys=");
        sb.append(orderedKeys);
        sb.append(", delegate=");
        sb.append(delegate);
        sb.append("}");
        return sb.toString();
    }

    /**
     * This Collection will *not* reflect the entry order.
     * Use the Iterator returned by #orderedKeys().
     */
    public Collection values() {
        return delegate.values();
    }
}