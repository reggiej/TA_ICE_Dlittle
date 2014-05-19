// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** 
 * A ConversionValue annotation is used within an ObjectTypeConverter.
 * 
 * @see oracle.toplink.annotations.ObjectTypeConverter
 * @author Guy Pelletier
 * @since Oracle TopLink 11.1.1.0.0 
 */ 
@Target({})
@Retention(RUNTIME)
public @interface ConversionValue {
    /**
     * (Required) Specify the database value.
     */
    String dataValue();

    /**
     * (Required) Specify the object value.
     */
    String objectValue();
}
