// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.xml;

import java.util.*;
import java.io.*;

/**
 * This <code>Enumeration</code> will iterate over an array of
 * <code>File</code>s, returning a <code>File</code> with
 * each invocation of <code>#nextElement()</code>.
 *
 * @see XMLFileAccessor
 * @see java.io.File
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 * @deprecated since OracleAS TopLink 10<i>g</i> (10.1.3).  This class is replaced by
 *         {@link oracle.toplink.ox}
 */
class FileListEnumerator implements Enumeration {

    /** The list of files to iterate over. */
    private File[] fileList;

    /** An index to the next file to return. */
    private int next;

    /**
     * Default constructor.
     */
    private FileListEnumerator() {
        super();
        next = 0;
    }

    /**
     * Constructor. Provide the files to be
     * iterated over.
     */
    FileListEnumerator(File[] fileList) {
        this();
        this.fileList = fileList;
    }

    /**
     * Return whether the enumerator has any
     * more elements to return.
     */
    public boolean hasMoreElements() {
        return next < fileList.length;
    }

    /**
     * Return the next element in the
     * enumeration. This will be a File.
     */
    public Object nextElement() {
        if (next < fileList.length) {
            return fileList[next++];
        } else {
            throw new NoSuchElementException("File List Enumerator");
        }
    }
}