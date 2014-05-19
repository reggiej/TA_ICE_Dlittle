// Copyright (c) 1998, 2006, Oracle. All rights reserved. 
package oracle.toplink.ox.mappings;

public interface MimeTypePolicy {
	/**
	 * return a MIME type string
	 * @param anObject - fixed non-dynamic implementors will ignore this parameter
	 * @return String
	 */
	String getMimeType(Object anObject);
	
}
