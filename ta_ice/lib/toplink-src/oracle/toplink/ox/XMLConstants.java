// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.ox;

import javax.xml.namespace.QName;

/**
 * <p>XMLConstants maintains a list of useful XMLConstants.
 *
 * <p>This includes constants for built-in schema types as well as QNames
 * which represent those built-in schema types.  These QName constants can be used,
 * for example, when adding conversion pairs to XMLFields and when adding
 * schema types to XMLUnionField.
 * <p><em>Code Sample</em><br>
 *  <code>
 * XMLUnionField unionField = new XMLUnionField("myElement"); <br>
 * unionField.addSchemaType()
 *</code>
 */
public class XMLConstants {
    public static final String SCHEMA_PREFIX = "xsd";
    public static final String SCHEMA_URL = "http://www.w3.org/2001/XMLSchema";
    public static final String SCHEMA_INSTANCE_PREFIX = "xsi";
    public static final String SCHEMA_INSTANCE_URL = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String TARGET_NAMESPACE_PREFIX = "toplinktn";
    public static final String NO_NS_SCHEMA_LOCATION = "noNamespaceSchemaLocation";
    public static final String SCHEMA_LOCATION = "schemaLocation";
    public static final String XMLNS = "xmlns";
    public static final String XMLNS_URL = "http://www.w3.org/2000/xmlns/";
    public static final String XML_NAMESPACE_PREFIX = "xml";
    public static final String XML_NAMESPACE_URL = "http://www.w3.org/XML/1998/namespace";
    public static final String SCHEMA_TYPE_ATTRIBUTE = "type";
    public static final String SCHEMA_NIL_ATTRIBUTE = "nil";
    public static final String REF_URL = "http://ws-i.org/profiles/basic/1.1/xsd";
    public static final String REF_PREFIX = "ref";
    public static final String XOP_URL = "http://www.w3.org/2004/08/xop/include";
    public static final String XOP_PREFIX = "xop";
    public static final Class QNAME_CLASS = QName.class;

    // Built-in Schema Types    
    public static final String BASE_64_BINARY = "base64Binary";
    public static final String BOOLEAN = "boolean";
    public static final String BYTE = "byte";
    public static final String DATE = "date";
    public static final String DATE_TIME = "dateTime";
    public static final String DECIMAL = "decimal";
    public static final String DOUBLE = "double";
    public static final String DURATION = "duration";
    public static final String FLOAT = "float";
    public static final String G_DAY = "gDay";
    public static final String G_MONTH = "gMonth";
    public static final String G_MONTH_DAY = "gMonthDay";
    public static final String G_YEAR = "gYear";
    public static final String G_YEAR_MONTH = "gYearMonth";    
    public static final String HEX_BINARY = "hexBinary";
    public static final String INT = "int";
    public static final String INTEGER = "integer";
    public static final String LONG = "long";
    public static final String QNAME = "QName";
    public static final String SHORT = "short";
    public static final String STRING = "string";
    public static final String TIME = "time";
    public static final String UNSIGNED_BYTE = "unsignedByte";
    public static final String UNSIGNED_INT = "unsignedInt";
    public static final String UNSIGNED_SHORT = "unsignedShort";
    public static final String ANY_SIMPLE_TYPE = "anySimpleType";
    public static final String SWA_REF = "swaRef";

    // Schema Type QNames
    public static final QName ANY_SIMPLE_TYPE_QNAME = new QName(SCHEMA_URL, ANY_SIMPLE_TYPE);
    public static final QName BASE_64_BINARY_QNAME = new QName(SCHEMA_URL, BASE_64_BINARY);
    public static final QName HEX_BINARY_QNAME = new QName(SCHEMA_URL, HEX_BINARY);
    public static final QName DATE_QNAME = new QName(SCHEMA_URL, DATE);
    public static final QName TIME_QNAME = new QName(SCHEMA_URL, TIME);
    public static final QName DATE_TIME_QNAME = new QName(SCHEMA_URL, DATE_TIME);
    public static final QName BOOLEAN_QNAME = new QName(SCHEMA_URL, BOOLEAN);
    public static final QName BYTE_QNAME = new QName(SCHEMA_URL, BYTE);
    public static final QName DECIMAL_QNAME = new QName(SCHEMA_URL, DECIMAL);
    public static final QName DOUBLE_QNAME = new QName(SCHEMA_URL, DOUBLE);
    public static final QName DURATION_QNAME = new QName(SCHEMA_URL, DURATION);
    public static final QName FLOAT_QNAME = new QName(SCHEMA_URL, FLOAT);
    public static final QName G_DAY_QNAME = new QName(SCHEMA_URL, G_DAY);
    public static final QName G_MONTH_QNAME = new QName(SCHEMA_URL, G_MONTH);
    public static final QName G_MONTH_DAY_QNAME = new QName(SCHEMA_URL, G_MONTH_DAY);
    public static final QName G_YEAR_QNAME = new QName(SCHEMA_URL, G_YEAR);
    public static final QName G_YEAR_MONTH_QNAME = new QName(SCHEMA_URL, G_YEAR_MONTH);
    public static final QName INT_QNAME = new QName(SCHEMA_URL, INT);
    public static final QName INTEGER_QNAME = new QName(SCHEMA_URL, INTEGER);
    public static final QName LONG_QNAME = new QName(SCHEMA_URL, LONG);
    public static final QName QNAME_QNAME = new QName(SCHEMA_URL, QNAME);
    public static final QName SHORT_QNAME = new QName(SCHEMA_URL, SHORT);
    public static final QName STRING_QNAME = new QName(SCHEMA_URL, STRING);
    public static final QName UNSIGNED_BYTE_QNAME = new QName(SCHEMA_URL, UNSIGNED_BYTE);
    public static final QName UNSIGNED_INT_QNAME = new QName(SCHEMA_URL, UNSIGNED_INT);
    public static final QName UNSIGNED_SHORT_QNAME = new QName(SCHEMA_URL, UNSIGNED_SHORT);
    public static final QName SWA_REF_QNAME = new QName(REF_URL, SWA_REF);
    public static final String JAXB_FRAGMENT = "jaxb.fragment";
    
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];
    
}
