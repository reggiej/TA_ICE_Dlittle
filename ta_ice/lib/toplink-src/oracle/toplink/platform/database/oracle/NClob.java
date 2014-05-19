// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.platform.database.oracle;

import oracle.toplink.internal.helper.NoConversion;
import oracle.toplink.internal.platform.database.oracle.Oracle9Specific;


/**
 * This class can be used to define the dataType with an ObjectTypeConverter
 * to have TopLink bind the object string value as an NCLOB Oracle type.
 */
public class NClob implements NoConversion, Oracle9Specific {
}