// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.eis.adapters.attunity;

import java.util.*;
import javax.resource.*;
import javax.resource.cci.*;
import com.attunity.adapter.*;
import org.w3c.dom.Element;
import oracle.toplink.internal.helper.*;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.eis.*;
import oracle.toplink.eis.interactions.*;

/**
 * Defines the Attunity Connect platform.
 * This generates Attunity Connect interaction specs.
 *
 * <p> This platform assumes that field name used in the descriptor and mappings
 * correctly use Attunity naming covention synatx of '#' for elements, '@' for attributes and '[]' for collections.
 *
 * @author James
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class AttunityPlatform extends EISPlatform {

    /**
     * Default constructor.
     */
    public AttunityPlatform() {
        super();
        this.shouldConvertDataToStrings = true;
        this.isMappedRecordSupported = true;
        this.isIndexedRecordSupported = false;
        this.isDOMRecordSupported = true;
        this.supportsLocalTransactions = false;
    }

    /**
     * Allow the platform to build the interaction spec based on properties defined in the interaction.
     */
    public InteractionSpec buildInteractionSpec(EISInteraction interaction) {
        InteractionSpec spec = (InteractionSpec)interaction.getInteractionSpec();
        if (spec == null) {
            spec = new AttuInteractionSpec(interaction.getFunctionName());
        }
        return spec;
    }

    /**
     * Stores the field value into the record.
     * This handles Attunity's syntax of requiring elements (#) to be set as '#element',
     * and collections (#[]) to be set sd '#element[]'.
     */
    public void setValueInRecord(String key, Object value, MappedRecord record, EISAccessor accessor) {
        // Check if it is an element (starts with #).
        // If this case the value is the elements from a complex field value (SDKFieldValue).
        if ((key.length() > 1) && (key.charAt(0) == '#')) {
            // Check if it is a collection (ends with []).
            if ((key.length() > 3) && (key.charAt(key.length() - 1) == ']') && (key.charAt(key.length() - 2) == '[')) {
                if (!(value instanceof List)) {
                    Vector values = new Vector(1);
                    values.add(value);
                    value = values;
                }
                record.put("#element[]", ((List)value).toArray());
            } else {
                if (value instanceof List) {
                    value = ((List)value).get(0);
                }
                record.put("#element", value);
            }
        } else {
            Object recordValue = getConversionManager().convertObject(value, ClassConstants.STRING);

            // Null must also be converted to empty string.
            if (recordValue == null) {
                recordValue = "";
            }
            record.put(key, recordValue);
        }
    }

    /**
     * Create an EISDOMRecord wrapping the records dom tree.
     */
    public AbstractRecord createDatabaseRowFromDOMRecord(Record record, EISInteraction call, EISAccessor accessor) {
        try {
            return new EISDOMRecord(record, ((DomRecord)record).getDom());
        } catch (ResourceException exception) {
            throw EISException.resourceException(exception, call, accessor, null);
        }
    }

    /**
     * Stores the XML DOM value into the record.
     * This must be implemented by the platform if it support XML/DOM records.
     */
    public void setDOMInRecord(Element dom, Record record, EISInteraction call, EISAccessor accessor) {
        try {
            ((DomRecord)record).setDom(dom);
        } catch (ResourceException exception) {
            throw EISException.resourceException(exception, call, accessor, null);
        }
    }
}