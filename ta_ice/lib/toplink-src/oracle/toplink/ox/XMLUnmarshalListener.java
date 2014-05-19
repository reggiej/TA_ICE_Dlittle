// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox;

public interface XMLUnmarshalListener {
    public void beforeUnmarshal(Object target, Object parent);
    public void afterUnmarshal(Object target, Object parent);
}