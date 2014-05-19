// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.ox;

public interface XMLMarshalListener {
    public void beforeMarshal(Object target);
    public void afterMarshal(Object target);
}