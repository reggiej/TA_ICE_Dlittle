// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** 
 * The Customizer annotation is used to specify a class that implements the 
 * oracle.toplink.tools.sessionconfiguration.DescriptorCustomizer interface and 
 * is to run against an enetity's class descriptor after all metadata processing 
 * has been completed.
 *
 * The Customizer annotation may be defined on an Entity, MappedSuperclass or 
 * Embeddable class. In the case of inheritance, a Customizer is not inherited 
 * from its parent classes.
 * 
 * @author Guy Pelletier
 * @since Oracle TopLink 11.1.1.0.0 
 */ 
@Target({TYPE})
@Retention(RUNTIME)
public @interface Customizer {
    /**
     * (Required) Defines the name of the descriptor customizer that should be
     * applied to this entity's descriptor.
     */
    Class value(); 
}
