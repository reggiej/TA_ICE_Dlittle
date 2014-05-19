// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** 
 * A TimeOfDay annotation is used to specify a specific time of day using a 
 * Calendar instance which is to be used within an OptimisticLocking annotation.
 * 
 * @see oracle.toplink.annotations.OptimisticLocking
 * @author Guy Pelletier
 * @since Oracle TopLink 11.1.1.0.0 
 */ 
@Target({})
@Retention(RUNTIME)
public @interface TimeOfDay {
    /**
     * (Optional) Hour of the day.
     */ 
    int hour() default 0;

    /**
     * (Optional) Minute of the day.
     */ 
    int minute() default 0;

    /**
     * (Optional) Second of the day.
     */ 
    int second() default 0;

    /**
     * (Optional) Millisecond of the day.
     */ 
    int millisecond() default 0;

    /**
     * Internal use. Do not modify.
     */ 
    boolean specified() default true;
}
