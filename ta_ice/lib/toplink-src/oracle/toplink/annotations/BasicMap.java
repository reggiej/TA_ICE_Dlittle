// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import javax.persistence.Column;
import javax.persistence.FetchType;

import static javax.persistence.FetchType.LAZY;

/**
 * A BasicMap is used to map an oracle.toplink.mappings.DirectMapMapping, which 
 * stores a collection of key-value pairs of simple types (String, Number, Date, 
 * etc.). It is used in conjunction with a CollectionTable which stores the key,
 * the value and a foreign key to the source object.
 * 
 * @see oracle.toplink.annotations.CollectionTable
 * 
 * A converter may be used if the desired object type and the data type do not 
 * match. This applied to both the key and value of the map.
 * 
 * @see oracle.toplink.annotations.Convert
 * @see oracle.toplink.annotations.Converter
 * @see oracle.toplink.annotations.ObjectTypeConverter
 * @see oracle.toplink.annotations.TypeConverter
 * 
 * A BasicMap can be specified within an Entity, MappedSuperclass and Embeddable 
 * class.
 * 
 * @author Guy Pelletier
 * @since Oracle TopLink 11.1.1.0.0 
 */ 
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface BasicMap {
    /**
     * (Optional) Defines whether the value of the field or property should
     * be lazily loaded or must be eagerly fetched. The EAGER strategy is a 
     * requirement on the persistence provider runtime that the value must be 
     * eagerly fetched. The LAZY strategy is a hint to the persistence provider 
     * runtime. If not specified, defaults to LAZY.
     */
    FetchType fetch() default LAZY;

    /**
     * (Optional) The name of the data column that holds the direct map key.
     */
    Column keyColumn();

    /**
     * (Optional) Specify the key converter. Default is equivalent to specifying
     * @Convert("none"), meaning no converter will be added to the direct map key.
     */
    Convert keyConverter() default @Convert;

    /**
     * (Optional) The name of the data column that holds the direct collection data.
     * Defaults to the property or field name.
     */
    Column valueColumn() default @Column;

    /**
     * (Optional) Specify the value converter. Default is equivalent to specifying 
     * @Convert("none"), meaning no converter will be added to the value column mapping.
     */
    Convert valueConverter() default @Convert;
}
