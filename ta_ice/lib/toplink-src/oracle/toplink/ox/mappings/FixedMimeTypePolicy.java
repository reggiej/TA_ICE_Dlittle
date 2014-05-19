// Copyright (c) 1998, 2006, Oracle. All rights reserved. 
package oracle.toplink.ox.mappings;

public class FixedMimeTypePolicy implements MimeTypePolicy {
	
	private String aMimeType;

	public FixedMimeTypePolicy() {
	}
	
	public FixedMimeTypePolicy(String aMimeTypeParameter) {
		aMimeType = aMimeTypeParameter;
	}
	
	public String getMimeType(Object anObject) {
		return aMimeType;
	}

	public String getMimeType() {
		return aMimeType;
	}
	
	public void setMimeType(String aString) {
		aMimeType = aString;
	}
}
