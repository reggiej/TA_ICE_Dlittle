// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** 
 * The Convert annotation specifies that a named converter should be used with 
 * the corresponding mapped attribute. The Convert annotation has the following 
 * reserved names:
 *  - serialized: Will use an oracle.toplink.mappings.converters.SerializedObjectConverter 
 *  on the associated mapping. 
 *  - none - Will place no converter on the associated mapping. 
 * 
 * @see oracle.toplink.annotations.Converter
 * @see oracle.toplink.annotations.ObjectTypeConverter
 * @see oracle.toplink.annotations.TypeConverter
 * @author Guy Pelletier
 * @since Oracle TopLink 11.1.1.0.0 
 */ 
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Convert {
    /**
     * (Optional) The name of the converter to be used.
     */
    String value() default "none";
}
