// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.descriptors.invalidation;

import java.util.*;
import oracle.toplink.internal.identitymaps.CacheKey;
import oracle.toplink.descriptors.invalidation.CacheInvalidationPolicy;
import oracle.toplink.internal.helper.*;

/**
 * PUBLIC:
 * A CacheInvalidationPolicy that allows objects to expire every day at a specific time.
 * A daily cache invalidation policy is created with an hour, minute, second and millisecond
 * when objects will expire.  Objects will expire in the cache every day at that time.
 * @see CacheInvalidationPolicy
 */
public class DailyCacheInvalidationPolicy extends CacheInvalidationPolicy {
    protected Calendar expiryTime = null;
    protected Calendar previousExpiry = null;// previous expiry is stored for efficiency

    /**
     * INTERNAL:
     * Default constructor for Project XML
     * if setters are not called to set expiry times, expiry time will be the time of
     * day at which this object is instantiated.
     */
    public DailyCacheInvalidationPolicy() {
        expiryTime = Calendar.getInstance();
        previousExpiry = (Calendar)expiryTime.clone();
        previousExpiry.add(Calendar.DAY_OF_YEAR, -1);
    }

    /**
     * PUBLIC:
     * Construct a daily policy that will allow objects to expire at a specific time of day.
     * Provide the hour, minute, second and millisecond.  Objects that make use of this policy
     * will be set to expire at that exact time every day.
     */
    public DailyCacheInvalidationPolicy(int hour, int minute, int second, int millisecond) {
        setExpiryTime(hour, minute, second, millisecond);
    }

    /**
     * INTERNAL:
     * return the next expiry time
     */
    public long getExpiryTimeInMillis(CacheKey key) {
        incrementExpiry();
        return JavaPlatform.getTimeInMillis(expiryTime);
    }

    /**
     * INTERNAL:
     * Get the expiry time as a Calendar.  Used for setting the expiry time in deployment XML
     */
    public Calendar getExpiryTime() {
        return expiryTime;
    }

    /**
     * INTERNAL:
     * Return true if this object has expire or is invalid
     */
    public boolean isInvalidated(CacheKey key, long currentTimeMillis) {
        if (key.getInvalidationState() == CacheKey.CACHE_KEY_INVALID) {
            return true;
        }

        long expiryMillis = JavaPlatform.getTimeInMillis(expiryTime);
        long readTime = key.getReadTime();

        if (currentTimeMillis < expiryMillis) {
            long previousExpiryMillis = JavaPlatform.getTimeInMillis(previousExpiry);
            if (readTime >= previousExpiryMillis) {
                // both current time and read time are between expiry yesterday and expiry today - not expired
                return false;
            } else if (currentTimeMillis >= previousExpiryMillis) {
                // read time is less than previous expiry and current time is after it. - expired and need update of expiry
                return true;
            } else {
                // read time is before previous expiry and so is current time - strange case, but not expired
                return false;
            }
        } else {
            if (readTime < expiryMillis) {
                // current time is greater than expiry and read time is before - expire and update expiry
                incrementExpiry();
                return true;
            } else {
                // current time and read time are greater than expiry - no expire and update expiry
                incrementExpiry();
                return false;
            }
        }
    }

    /**
     * INTERNAL:
     * Update the expiry time to be the day after the current day.
     */
    public void incrementExpiry() {
        long currentTimeMillis = System.currentTimeMillis();
        long expiryInMillis = JavaPlatform.getTimeInMillis(expiryTime);
        if (currentTimeMillis <= expiryInMillis) {
            // no updated needed.  Return for efficiency
            return;
        }
        while (currentTimeMillis > JavaPlatform.getTimeInMillis(expiryTime)) {
            // increment the expiry time until it is after the current time
            previousExpiry.add(Calendar.DAY_OF_YEAR, 1);
            expiryTime.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    /**
     * PUBLIC:
     * Set a new expiry time for this object
    * Provide the hour, minute, second and millisecond.  Objects which make use of this policy
     * will be set to expire at that exact time every day.
     */
    public void setExpiryTime(int hour, int minute, int second, int millisecond) {
        expiryTime = Calendar.getInstance();
        expiryTime.set(Calendar.HOUR_OF_DAY, hour);
        expiryTime.set(Calendar.MINUTE, minute);
        expiryTime.set(Calendar.SECOND, second);
        expiryTime.set(Calendar.MILLISECOND, millisecond);
        previousExpiry = (Calendar)expiryTime.clone();
        previousExpiry.add(Calendar.DAY_OF_YEAR, -1);
        incrementExpiry();
    }

    /**
     * INTERNAL:
     * Set the expiry time based on a Calendar. Used for setting the expiry time from
     * deployment XML.
     */
    public void setExpiryTime(Calendar calendar) {
        setExpiryTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), calendar.get(Calendar.MILLISECOND));
    }
}