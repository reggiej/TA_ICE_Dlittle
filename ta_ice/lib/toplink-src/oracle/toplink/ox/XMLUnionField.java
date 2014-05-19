// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ox;

import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.namespace.QName;
import oracle.toplink.exceptions.ConversionException;
import oracle.toplink.internal.ox.XMLConversionManager;

/**
 * <p>Subclass of XMLField for fields that are mapped to unions.
 * Maintains a list of schema types instead of just one single schema type.
 * Schema types can be added using the addSchemaType api.
 *
 * XMLConstants has a list of useful constants including a list of QNames for
 * built-in schema types that can be used when adding schema types.
 *
 * <p>When reading and writing an element that is mapped with an XMLUnionField, a conversion to
 * each of the schema types on the field (in the order they are specified ) is tried
 * until a conversion is successful. The java type to convert to is based on the list of
 * schema type to java conversion pairs specified on the field.  These conversion pairs
 * can be modified using the addXMLConversion api.
 *
 * <p><em>Code Sample</em><br>
 * <code>
 * In this example the age field could be a date or an int.<br>
 * XMLUnionField field = new XMLUnionField("age/text()");<br>
 * field.addSchemaType(XMLConstants.DATE_QNAME);<br>
 * field.addSchemaType(XMLConstants.INT_QNAME)<br>
 * </code>
 *
 * @see XMLField
 * @see XMLConstants
 */
public class XMLUnionField extends XMLField {
    private ArrayList schemaTypes;

    /**
     * Constructs an XMLUnionField
     */
    public XMLUnionField() {
        super();
        schemaTypes = new ArrayList();
    }

    /**
    * Constructs an XMLUnionField with the xpath set to the specified xPath
    * @param xPath The xpath expression for the field
    */
    public XMLUnionField(String xPath) {
        super(xPath);
        schemaTypes = new ArrayList();
    }

    /**
    * Return the list of schema types
    * @return the list of types
    */
    public ArrayList getSchemaTypes() {
        return schemaTypes;
    }

    /**
    * Sets the schema types that this attribute can be mapped to
    * Valid QName schema types can be found on oracle.toplink.ox.XMLConstants
    * @param value An ArrayList containing the schema types.
    * @see oracle.toplink.ox.XMLConstants
    */
    public void setSchemaTypes(ArrayList value) {
        schemaTypes = value;
    }

    /**
    * Adds the new type value to the list of types
    * @param value QName to be added to the list of schema types
    */
    public void addSchemaType(QName value) {
        if (value != null) {
            if (schemaTypes == null) {
                schemaTypes = new ArrayList();
            }

            // don't add things twice - for now the MW adds only some types... 
            // once they add all types this can be removed
            if (!contains(schemaTypes, value)) {
                schemaTypes.add(value);
            }
        }
    }

    /**
      * Return the first schema type in the list of schema types
      * @return the first item in the collection of schema types
      */
    public QName getSchemaType() {
        if (schemaTypes != null) {
            return (QName)getSchemaTypes().get(0);
        }
        return null;
    }

    /**
      * Adds the new type value to the list of types
      * @param value The value to be added to the list of schema types
      */
    public void setSchemaType(QName value) {
        addSchemaType(value);
    }

    /**
    * Checks for existance of a schema type in the list.
    *
    * @param types List of types
    * @param value the QName to look for in the list
    * @return true if 'value' exists in the list, false otherwise
    */
    private boolean contains(ArrayList types, QName value) {
        QName type;
        Iterator it = types.iterator();

        while (it.hasNext()) {
            type = (QName)it.next();
            if (type.equals(value)) {
                return true;
            }
        }

        return false;
    }

    /**
    * INTERNAL:
    * returns true since this is a union field
    */
    public boolean isUnionField() {
        return true;
    }

    /**
    * INTERNAL:
    */
    public Object convertValueBasedOnSchemaType(Object value, XMLConversionManager xmlConversionManager) {
        Object convertedValue = value;
        for (int i = 0; i < schemaTypes.size(); i++) {
            QName nextQName = (QName)schemaTypes.get(i);
            try {
                if (nextQName != null) {
                    Class javaClass = getType();
                    javaClass = getJavaClass(nextQName);
                    convertedValue = xmlConversionManager.convertObject(value, javaClass, nextQName);
                    break;
                }
            } catch (ConversionException ce) {
                if (i == (schemaTypes.size() - 1)) {
                    throw ce;
                }
            }
        }
        return convertedValue;
    }

    /**
      * Return the class for a given qualified XML Schema type.
      * If the class is a primitive the corresponding wrapper class is returned
      * @param qname The qualified name of the XML Schema type to use as a key in the lookup
      * @return The class associated with the specified schema type, if no corresponding match found returns null
      */
    public Class getJavaClass(QName qname) {
        if (userXMLTypes != null) {
            if (userXMLTypes.containsKey(qname)) {
                return (Class)userXMLTypes.get(qname);
            }
        }
        Class javaClass = (Class)XMLConversionManager.getDefaultXMLManager().getDefaultXMLTypes().get(qname);
        return XMLConversionManager.getObjectClass(javaClass);
    }
}