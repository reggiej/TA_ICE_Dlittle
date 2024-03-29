// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
 
/** 
 * A Converter is used to customize the values during the reading from the 
 * database into the object model as well as during the writing back of changes 
 * into the database. This annotation allows developers to define a named 
 * converter that can be used in their mappings. A converter can be defined on 
 * an entity class, method, or field. 
 * 
 * A Converter must be be uniquely identified by name and can be defined at 
 * the class, field and property level and can be specified within an Entity, 
 * MappedSuperclass and Embeddable class.
 * 
 * The usage of a Converter is always specified via the Convert annotation and 
 * is supported on a Basic, BasicMap or BasicCollection mapping.
 * 
 * @see oracle.toplink.annotations.Convert
 * @author Guy Pelletier
 * @since Oracle TopLink 11.1.1.0.0 
 */ 
@Target({TYPE, METHOD, FIELD})
@Retention(RUNTIME)
public @interface Converter {
    /**
     * (Required) Name this converter. The name should be unique across the 
     * whole persistence unit.
     */
    String name();

    /**
     * (Required) The converter class to be used. This class must implement the
     * TopLink oracle.toplink.mappings.converters.Converter interface.
     */
    Class converterClass(); 
}
