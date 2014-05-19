// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.eis.interactions;

import java.util.*;
import javax.resource.*;
import javax.resource.cci.*;
import oracle.toplink.internal.helper.DatabaseField;
import oracle.toplink.internal.sessions.AbstractRecord;
import oracle.toplink.sessions.DatabaseRecord;
import oracle.toplink.eis.*;

/**
 * Defines the specification for a call to a JCA interaction that uses indexed records.
 * Builds the input and output records from the arguments.
 *
 * @author James
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class IndexedInteraction extends EISInteraction {

    /**
     * Default constructor.
     */
    public IndexedInteraction() {
        super();
    }

    /**
     * PUBLIC:
     * Define the argument to the interaction for the index argument.
     * This must be called in the order of the arguments in the input indexed record.
     * The argumentFieldName is the field or argument name in the descriptor that maps to the indexed value.
     */
    public void addArgument(String argumentFieldName) {
        getArguments().addElement(new DatabaseField(argumentFieldName));
    }

    /**
     * PUBLIC:
     * Define the argument to the interaction for the index argument.
     * This must be called in the order of the arguments in the input indexed record.
     * The argumentValue is the value of the argument to be used to pass to the interaction.
     */
    public void addArgumentValue(Object argumentValue) {
        getArguments().addElement(argumentValue);
    }

    /**
     * PUBLIC:
     * Define the field/argument name to be substitute for the index output argument.
     * This must be called in the order of the output arguments in the result indexed record.
     * The argumentFieldName is the field or argument name in the descriptor that maps to the indexed value.
     */
    public void addOutputArgument(String argumentFieldName) {
        getOutputArguments().addElement(new DatabaseField(argumentFieldName));
    }

    /**
     * The arguments are the values in order of occurance in the record.
     */
    public Vector getArguments() {
        return super.getArguments();
    }

    /**
     * The output arguments in order of occurance in the record.
     */
    public Vector getOutputArguments() {
        return super.getOutputArguments();
    }

    /**
     * The arguments are the values in order of occurance in the record.
     */
    public void setArguments(Vector arguments) {
        super.setArguments(arguments);
    }

    /**
     * The output arguments in order of occurance in the record.
     */
    public void setOutputArguments(Vector outputArguments) {
        super.setOutputArguments(outputArguments);
    }

    /**
     * Create an indexed input record for this interaction.
     * Populate the data into the record from this interaction's arguments.
     */
    public Record createInputRecord(EISAccessor accessor) {
        try {
            IndexedRecord record = accessor.getRecordFactory().createIndexedRecord(getInputRecordName());
            for (int index = 0; index < getParameters().size(); index++) {
                Object parameter = getParameters().get(index);

                // Allow conversion of nested rows and collections.
                record.add(createRecordElement("", parameter, accessor));
            }
            return record;
        } catch (ResourceException exception) {
            throw EISException.resourceException(exception, accessor, null);
        }
    }

    /**
     * Build a database row from the record returned from the interaction.
     * Also handles MappedRecords for case of input being indexed but mapped ouput.
     */
    public AbstractRecord buildRow(Record record, EISAccessor accessor) {
    	AbstractRecord row = null;
        if (record instanceof IndexedRecord) {
            IndexedRecord indexedRecord = (IndexedRecord)record;
            row = new DatabaseRecord(indexedRecord.size());
            for (int index = 0; index < indexedRecord.size(); index++) {
                DatabaseField field = (DatabaseField)getOutputArguments().get(index);
                row.put(field, indexedRecord.get(index));
            }
        } else if (record instanceof MappedRecord) {
            MappedRecord mappedRecord = (MappedRecord)record;

            // Handle the case of a single output argument of the entire row contained within the return record.
            if (getOutputArgumentNames().size() == 1) {
                mappedRecord = (MappedRecord)mappedRecord.get(getOutputArgumentNames().get(0));
                // Handle the case were the output row is mapped into a database row of values.
            } else if (getOutputArgumentNames().size() > 1) {
                row = new DatabaseRecord(getOutputArgumentNames().size());
                for (int index = 0; index < getOutputArgumentNames().size(); index++) {
                    DatabaseField field = (DatabaseField)getOutputArguments().get(index);
                    row.put(field, mappedRecord.get(getOutputArgumentNames().get(index)));
                }
                return row;
            }

            // Wrapped the record in a database to avoid loosing any information in conversion to database row,
            // also gets around problem of some adatpers not supporting keySet or entrySet.
            row = new EISMappedRecord(mappedRecord, accessor);
        } else {
            row = new DatabaseRecord(1);
            row.put(getOutputResultPath(), record);
        }
        return row;
    }
}