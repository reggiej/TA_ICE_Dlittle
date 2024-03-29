// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.eis;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import javax.resource.*;
import javax.resource.cci.*;
import org.w3c.dom.Element;
import oracle.toplink.internal.helper.*;
import oracle.toplink.exceptions.*;
import oracle.toplink.queryframework.*;
import oracle.toplink.eis.interactions.*;
import oracle.toplink.internal.databaseaccess.DatasourcePlatform;
import oracle.toplink.internal.security.PrivilegedAccessHelper;
import oracle.toplink.internal.security.PrivilegedGetClassLoaderForClass;
import oracle.toplink.internal.security.PrivilegedGetMethod;
import oracle.toplink.internal.security.PrivilegedMethodInvoker;
import oracle.toplink.internal.sessions.AbstractRecord;

/**
 * <p>An <code>EISPlatform</code> defines any EIS adapter specific behavoir.
 * This may include:
 * <ul>
 * <li>Instantiation of the adapter InteractionSpec
 * <li>Conversion from an adapter custom Record
 * <li>Perform platform specific record access
 * <li>Provide XML DOM record conversion
 * <li>Provide sequence support
 * </ul>
 * 
 * <p><code>EISPlatform</code> also supports behavoir for specifing the record 
 * types supported and automatic data-conversion to strings.
 *
 * <p>Although use of the generic <code>EISPlatform</code> may be sufficient, 
 * some adapters may require that a specific platform be defined.
 *
 * @author James
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class EISPlatform extends DatasourcePlatform {
    protected boolean isIndexedRecordSupported;
    protected boolean isMappedRecordSupported;
    protected boolean isDOMRecordSupported;

    /** Can be used for adapters that only support String data/XML. */
    protected boolean shouldConvertDataToStrings;

    /** Allows for usage of transaction to be disabled if not supported. */
    protected boolean supportsLocalTransactions;

    /** Can be used if a commit is required after every interaction outside of a local transaction. */
    protected boolean requiresAutoCommit;

    /** Can be used to convert from an adapter specific record. */
    protected RecordConverter recordConverter;

    /** Used to reflectively provide XML record support as DOMRecord is not part of the JCA-CCI spec. */
    protected Method domMethod;

    /**
     * Default constructor.
     */
    public EISPlatform() {
        super();
        setIsMappedRecordSupported(true);
        setIsIndexedRecordSupported(true);
        setIsDOMRecordSupported(false);
        setShouldConvertDataToStrings(false);
        setSupportsLocalTransactions(true);
        setRequiresAutoCommit(false);
    }

    /**
     * Return the record converter.
     */
    public RecordConverter getRecordConverter() {
        return recordConverter;
    }

    /**
     * Set the record converter.
     * Can be used to convert from an adapter specific record.
     */
    public void setRecordConverter(RecordConverter recordConverter) {
        this.recordConverter = recordConverter;
    }

    /**
     * Return if this platform requires auto commit of the local transaction
     * for interactions outside of an interaction.
     */
    public boolean requiresAutoCommit() {
        return requiresAutoCommit;
    }

    /**
     * Set if this platform requires auto commit of the local transaction
     * for interactions outside of an interaction.
     */
    public void setRequiresAutoCommit(boolean requiresAutoCommit) {
        this.requiresAutoCommit = requiresAutoCommit;
    }

    /**
     * Return if this platform supports local transactions.
     */
    public boolean supportsLocalTransactions() {
        return supportsLocalTransactions;
    }

    /**
     * Set if this platform supports local transactions.
     */
    public void setSupportsLocalTransactions(boolean supportsLocalTransactions) {
        this.supportsLocalTransactions = supportsLocalTransactions;
    }

    /**
     * Return if this platform supports JCA IndexedRecord.
     */
    public boolean isIndexedRecordSupported() {
        return isIndexedRecordSupported;
    }

    /**
     * Set if this platform supports JCA IndexedRecord.
     */
    public void setIsIndexedRecordSupported(boolean isIndexedRecordSupported) {
        this.isIndexedRecordSupported = isIndexedRecordSupported;
    }

    /**
     * Return if this platform supports JCA MappedRecord.
     */
    public boolean isMappedRecordSupported() {
        return isMappedRecordSupported;
    }

    /**
     * Set if this platform supports JCA MappedRecord.
     */
    public void setIsMappedRecordSupported(boolean isMappedRecordSupported) {
        this.isMappedRecordSupported = isMappedRecordSupported;
    }

    /**
     * Return if this platform supports XML/DOM Records.
     */
    public boolean isDOMRecordSupported() {
        return isDOMRecordSupported;
    }

    /**
     * Set if this platform supports XML/DOM Records.
     */
    public void setIsDOMRecordSupported(boolean isDOMRecordSupported) {
        this.isDOMRecordSupported = isDOMRecordSupported;
    }

    /**
     * Return if all data set into the adapter should be first converted to strings.
     */
    public boolean shouldConvertDataToStrings() {
        return shouldConvertDataToStrings;
    }

    /**
     * Set if all data set into the adapter should be first converted to strings.
     */
    public void setShouldConvertDataToStrings(boolean shouldConvertDataToStrings) {
        this.shouldConvertDataToStrings = shouldConvertDataToStrings;
    }

    /**
     * Allow the platform to build the interaction spec based on properties defined in the interaction.
     */
    public InteractionSpec buildInteractionSpec(EISInteraction interaction) {
        return interaction.getInteractionSpec();
    }

    /**
     * Allow the platform to create the appropiate type of record for the interaction.
     */
    public Record createInputRecord(EISInteraction interaction, EISAccessor accessor) {
        Record input = interaction.createInputRecord(accessor);
        if (getRecordConverter() != null) {
            input = getRecordConverter().converterToAdapterRecord(input);
        }
        return input;
    }

    /**
     * Allow the platform to create the appropiate type of record for the interaction.
     * If an output record is not required then null is returned.
     */
    public Record createOutputRecord(EISInteraction interaction, EISAccessor accessor) {
        return null;
    }

    /**
     * INTERNAL:
     * Allow the platform to handle record ro row conversion.
     */
    public AbstractRecord buildRow(Record record, EISInteraction interaction, EISAccessor accessor) {
        Record output = record;
        if (getRecordConverter() != null) {
            output = getRecordConverter().converterFromAdapterRecord(output);
        }
        return interaction.buildRow(output, accessor);
    }

    /**
     * Allow the platform to handle record ro row conversion.
     */
    public Vector buildRows(Record record, EISInteraction interaction, EISAccessor accessor) {
        Record output = record;
        if (getRecordConverter() != null) {
            output = getRecordConverter().converterFromAdapterRecord(output);
        }
        return interaction.buildRows(output, accessor);
    }

    /**
     * Allow the platform to handle the creation of the DOM record.
     * By default create a mapped record an assume it implements DOM as well.
     */
    public Record createDOMRecord(String recordName, EISAccessor accessor) {
        try {
            return accessor.getRecordFactory().createMappedRecord(recordName);
        } catch (ResourceException exception) {
            throw EISException.resourceException(exception, accessor, null);
        }
    }

    /**
     * INTERNAL:
     * Allow the platform to handle the creation of the Record for the DOM record.
     * By default instantiate an EISDOMRecord which introspects the record for a getDOM method.
     */
    public AbstractRecord createDatabaseRowFromDOMRecord(Record record, EISInteraction call, EISAccessor accessor) {
        return new EISDOMRecord(record);
    }

    /**
     * Retrieves the field value from the record.
     * This allows for the platform to perform any platform specific translation or conversion.
     */
    public Object getValueFromRecord(String key, MappedRecord record, EISAccessor accessor) {
        return record.get(key);
    }

    /**
     * Stores the XML DOM value into the record.
     * This must be implemented by the platform if it support XML/DOM records.
     */
    public void setDOMInRecord(Element dom, Record record, EISInteraction call, EISAccessor accessor) {
        if (domMethod == null) {
            Class[] argumentTypes = new Class[1];
            argumentTypes[0] = Element.class;
            try {
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    domMethod = (Method) AccessController.doPrivileged(new PrivilegedGetMethod(record.getClass(), "setDom", argumentTypes, false));
                }else{
                    domMethod = PrivilegedAccessHelper.getMethod(record.getClass(), "setDom", argumentTypes, false);
                }
            } catch (Exception notFound) {
                try {
                    if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                        domMethod = (Method) AccessController.doPrivileged(new PrivilegedGetMethod(record.getClass(), "setDOM", argumentTypes, false));
                    }else{
                        domMethod = PrivilegedAccessHelper.getMethod(record.getClass(), "setDOM", argumentTypes, false);
                    }
                } catch (Exception cantFind) {
                    throw new EISException(cantFind);
                }
            }
        }
        try {
            Object[] arguments = new Object[1];
            arguments[0] = dom;
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try{
                    AccessController.doPrivileged(new PrivilegedMethodInvoker(domMethod, record, arguments));
                }catch (PrivilegedActionException ex){
                    throw (Exception)ex.getCause();
                }
            }else{
                PrivilegedAccessHelper.invokeMethod(domMethod, record, arguments);
            }
        } catch (Exception error) {
            throw new EISException(error);
        }
    }

    /**
     * Stores the field value into the record.
     * This allows for the platform to perform any platform specific translation or conversion.
     */
    public void setValueInRecord(String key, Object value, MappedRecord record, EISAccessor accessor) {
        Object recordValue = value;
        if (shouldConvertDataToStrings()) {
            recordValue = getConversionManager().convertObject(value, ClassConstants.STRING);
        }
        record.put(key, recordValue);
    }

    /**
     * Add the parameter.
     * Convert the parameter to a string and write it.
     * Convert rows to XML strings.
     */
    public void appendParameter(Call call, Writer writer, Object parameter) {
        if (parameter instanceof Vector) {
            Vector records = (Vector)parameter;

            // May be a collection of record.
            for (int index = 0; index < records.size(); index++) {
                appendParameter(call, writer, ((Vector)records).elementAt(index));
            }
        } else if (parameter instanceof oracle.toplink.ox.record.DOMRecord) {
            String xml = ((oracle.toplink.ox.record.DOMRecord)parameter).transformToXML();

            // For some reason the transform always prints the XML header, so trim it off.
            int start = xml.indexOf('>');
            xml = xml.substring(start + 1, xml.length());
            try {
                writer.write(xml);
            } catch (IOException exception) {
                throw ValidationException.fileError(exception);
            }
        } else {
            super.appendParameter(call, writer, parameter);
        }
    }
}